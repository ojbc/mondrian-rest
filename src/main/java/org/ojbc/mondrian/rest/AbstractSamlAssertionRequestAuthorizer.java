package org.ojbc.mondrian.rest;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;

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

import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;

/**
 * Abstract template method class for RequestAuthorizers that use SAML.
 *
 */
public abstract class AbstractSamlAssertionRequestAuthorizer implements RequestAuthorizer {

	private static final String SHIB_ASSERTION_KEY = "Shib-Assertion-01";
	
	protected static final NamespaceContext SAML_NAMESPACE_CONTEXT = new NamespaceContext() {
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
	};

	@Value("${samlAssertionTokenAttributeName:null}")
	protected String tokenAttributeName;

	@Override
	public final RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, QueryRequest queryRequest) throws Exception {
		
		String connectionName = queryRequest.getConnectionName();
	
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

	public String getTokenAttributeName() {
		return tokenAttributeName;
	}

	public void setTokenAttributeName(String tokenAttributeName) {
		this.tokenAttributeName = tokenAttributeName;
	}

	protected String getToken(Document assertion) {
		return getAssertionAttributeValue(assertion, tokenAttributeName);
	}
	
	protected String getAssertionAttributeValue(Document assertion, String attributeName) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(SAML_NAMESPACE_CONTEXT);
		try {
			String expression = "/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='" + attributeName + "']/saml2:AttributeValue";
			return (String) xPath.evaluate(expression, assertion, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
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

}