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

import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class SamlUtils {
	
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

	public static final String getAssertionAttributeValue(Document assertion, String attributeName) {
		return getStringXPathValue(assertion, "/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='" + attributeName + "']/saml2:AttributeValue");
	}

	public static String getNameIdValue(Document assertion) {
		return getStringXPathValue(assertion, "/saml2:Assertion/saml2:Subject/saml2:NameID");
	}

	private static String getStringXPathValue(Document assertion, String expression) {
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(SAML_NAMESPACE_CONTEXT);
		try {
			return (String) xPath.evaluate(expression, assertion, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

}
