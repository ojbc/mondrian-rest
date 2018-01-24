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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.w3c.dom.Document;

/**
 * Interceptor that authenticates requests only if they have a valid SAML assertion.  Roles are assigned
 * based on a lookup of the FederationID attribute in the assertion.
 */
@Component
@PropertySource("classpath:saml-assertion-mondrian-role-interceptor.properties")
@ConfigurationProperties
public class SamlAssertionMondrianRoleInterceptor extends HandlerInterceptorAdapter {
	
	private static final String SHIB_ASSERTION_KEY = "Shib-Assertion-01";
	private final Log log = LogFactory.getLog(SamlAssertionMondrianRoleInterceptor.class);
	
	private Map<String, String> federationIdRoleMappings;
	
	public Map<String, String> getFederationIdRoleMappings() {
		return federationIdRoleMappings;
	}

	public void setFederationIdRoleMappings(Map<String, String> tokenRoleMappings) {
		this.federationIdRoleMappings = tokenRoleMappings;
	}

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return authenticateRequest(request, response);
    }

	boolean authenticateRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

		boolean ret = false;

		fixCertificatePathError();

		// Hard coded to pick up a single assertion...could loop through assertion headers if there will be more than one
		String assertionRetrievalURL = request.getHeader(SHIB_ASSERTION_KEY);

		if (assertionRetrievalURL != null) {
			URL url = new URL(assertionRetrievalURL);
			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();
			Document assertion = parseAssertion(is);
			String role = getRoleForAssertion(assertion);
			if (role != null) {
				request.setAttribute(Application.ROLE_REQUEST_ATTRIBUTE_NAME, role);
				ret = true;
			} else {
				log.debug("Authentication failed for SAML assertion with federation ID " + getFederationID(assertion));
			}
		} else {
			log.warn("No assertion found in request");
		}
		
		if (!ret) {
			response.setStatus(403);
		}

		return ret;

	}
	
	String getRoleForAssertion(Document assertion) {
		
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
			public Iterator getPrefixes(String uri) {
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
