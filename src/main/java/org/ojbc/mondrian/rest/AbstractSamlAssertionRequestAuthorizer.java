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
 * Copyright 2012-2020 Open Justice Broker Consortium
 */
package org.ojbc.mondrian.rest;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Abstract template method class for RequestAuthorizers that use SAML.
 *
 */
public abstract class AbstractSamlAssertionRequestAuthorizer implements RequestAuthorizer {

	private static final String SHIB_ASSERTION_KEY = "Shib-Assertion-01";
	
   	@Resource(name="${samlTokenStrategyBeanName:#{null}}")
	private SamlTokenStrategy samlTokenStrategy;

	@Override
	public final RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, String connectionName) throws Exception {
		
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
			if (connectionName == null) {
				ret.message = "Authentication failed.  Query request did not specify a connection.";
			} else {
				ret = authorizeAssertion(connectionName, assertion);
			}
		} else {
			ret.message = "Authentication failed.  No assertion found in request.";
		}
	
	    return ret;
	    
	}

	protected final String getToken(Document assertion) {
		return samlTokenStrategy.getToken(assertion);
	}
	
	/**
	 * Implementers implement this method to determine authorization and mondrian role, given the supplied connection and SAML assertion
	 * @param connectionName the name of the Mondrian connection
	 * @param assertion the SAML assertion
	 * @return status of authorization
	 */
	protected abstract RequestAuthorizationStatus authorizeAssertion(String connectionName, Document assertion);

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

	private Document parseAssertion(InputStream is) throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		return documentBuilderFactory.newDocumentBuilder().parse(is);
	}

	public SamlTokenStrategy getSamlTokenStrategy() {
		return samlTokenStrategy;
	}

	public void setSamlTokenStrategy(SamlTokenStrategy samlTokenStrategy) {
		this.samlTokenStrategy = samlTokenStrategy;
	}

}