/*
 * Unless explicitly acquired and licensed from Licensor under another license, the contents of
 * this file are subject to the Reciprocal Public License ("RPL") Version 1.5, or subsequent
 * versions as allowed by the RPL, and You may not copy or use this file in either source code
 * or executable form, except in compliance with the terms and conditions of the RPL
 *
 * All software distributed under the RPL is provided strictly on an "AS IS" basis, WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND LICENSOR HEREBY DISCLAIMS ALL SUCH
 * WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the RPL for specific language
 * governing rights and limitations under the RPL.
 *
 * http://opensource.org/licenses/RPL-1.5
 *
 * Copyright 2012-2017 Open Justice Broker Consortium
 */
package org.ojbc.mondrian.rest;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/**
 * RequestAuthorizer that uses information in a SAML assertion to authorize each request and determine that user's role for the connection in the query request.
 */
@Component
public class SamlAssertionRequestAuthorizer implements RequestAuthorizer {
	
	private static final String SHIB_ASSERTION_KEY = "Shib-Assertion-01";
	private final Log log = LogFactory.getLog(SamlAssertionRequestAuthorizer.class);
	
	private Map<String, Map<String, String>> federationIdRoleMappings;
	
	@Value("${samlAssertionRequestAuthorizerConfigFileName:saml-assertion-request-authorizer.json}")
	private String samlAssertionRequestAuthorizerConfigFileName;
	
	@PostConstruct
	public void init() throws Exception {
		federationIdRoleMappings = RequestAuthorizer.AuthorizerUtil.convertRoleConnectionJsonToMaps(samlAssertionRequestAuthorizerConfigFileName);
	}
	
	public void setSamlAssertionRequestAuthorizerConfigFileName(String samlAssertionRequestAuthorizerConfigFileName) {
		this.samlAssertionRequestAuthorizerConfigFileName = samlAssertionRequestAuthorizerConfigFileName;
	}

	public Map<String, Map<String, String>> getFederationIdRoleMappings() {
		return federationIdRoleMappings;
	}

	void setFederationIdRoleMappings(Map<String, Map<String, String>> federationIdRoleMappings) {
		this.federationIdRoleMappings = federationIdRoleMappings;
	}

	@Override
	public RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, QueryRequest queryRequest) throws Exception {
		
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		ret.authorized = false;
		
		fixCertificatePathError();

		// Hard coded to pick up a single assertion...could loop through assertion headers if there will be more than one
		String assertionRetrievalURL = request.getHeader(SHIB_ASSERTION_KEY);
		
		if (assertionRetrievalURL != null) {
			URL url = new URL(assertionRetrievalURL);
			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();
			Document assertion = parseAssertion(is);
			Map<String, String> connectionMappings = getConnectionMappingsForAssertion(assertion);
			if (connectionMappings != null) {
				String connectionName = queryRequest.getConnectionName();
				if (connectionName != null) {
					String role = connectionMappings.get(connectionName);
					if (role != null) {
						ret.authorized = true;
						ret.mondrianRole = role;
						ret.token = getFederationID(assertion);
						if (role.equals(ALL_ACCESS_ROLE_NAME)) {
							ret.mondrianRole = null;
						}
					} else {
						ret.message = "Authentication failed for SAML assertion with federation ID " + getFederationID(assertion) + " for connection " + connectionName;
					}
				} else {
					ret.message = "Authentication failed.  Query request did not specify a connection.";
				}
				
			} else {
				ret.message = "Authentication failed.  No connection-role mappings found for assertion with federation ID " + getFederationID(assertion);
			}
		} else {
			ret.message = "Authentication failed.  No assertion found in request.";
		}

        return ret;
        
	}

	Map<String, String> getConnectionMappingsForAssertion(Document assertion) {
		String federationID = getFederationID(assertion);
		if (federationID != null) {
			return federationIdRoleMappings.get(federationID);
		}
		return null;
	}
	
	String getFederationID(Document assertion) {

		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new NamespaceContext() {
			@Override
			public Iterator<String> getPrefixes(String uri) {
				return "urn:oasis:names:tc:SAML:2.0:assertion".equals(uri) ? Collections.singletonList("saml2").iterator() : null;
			}
			@Override
			public String getPrefix(String uri) {
				return "urn:oasis:names:tc:SAML:2.0:assertion".equals(uri) ? "saml2" : null;
			}
			@Override
			public String getNamespaceURI(String prefix) {
				return "saml2".equals(prefix) ? "urn:oasis:names:tc:SAML:2.0:assertion" : null;
			}
		});
		try {
			String expression = "/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='gfipm:2.0:user:FederationId']/saml2:AttributeValue";
			return (String) xPath.evaluate(expression, assertion, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}

	}
	
	private void fixCertificatePathError() throws GeneralSecurityException {
		/*
		 * fix for Exception in thread "main" javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed:
		 * sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
		 */
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				}
		};
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	Document parseAssertion(InputStream is) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		return documentBuilderFactory.newDocumentBuilder().parse(is);
	}

}
