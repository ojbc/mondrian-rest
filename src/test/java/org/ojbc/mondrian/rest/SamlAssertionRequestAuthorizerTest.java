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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojbc.mondrian.rest.RequestAuthorizer.RequestAuthorizationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SpringBootTest
@TestPropertySource(properties = { "samlAssertionTokenAttributeName=gfipm:2.0:user:FederationId", "samlTokenStrategyBeanName=attributeSamlTokenStrategy" })
public class SamlAssertionRequestAuthorizerTest {
	
	private final Log log = LogFactory.getLog(SamlAssertionRequestAuthorizerTest.class);
	
	@Autowired
	private AbstractSamlAssertionRequestAuthorizer samlAssertionRequestAuthorizer;
	
	private Document transientNameIdAssertion;
	
	@BeforeEach
	public void beforeAll() throws Exception {
		InputStream assertionStream = new ClassPathResource("transient-name-id-assertion.xml").getInputStream();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		transientNameIdAssertion = dbf.newDocumentBuilder().parse(assertionStream);
	}
	
	@Test
	public void testTransientNameIdAssertionSuccess() throws Exception {
		RequestAuthorizationStatus status = samlAssertionRequestAuthorizer.authorizeAssertion("test", transientNameIdAssertion);
		assertEquals(status.authorized, true);
		assertEquals(status.mondrianRole, "ROLE_ADMIN");
	}
	
	@Test
	public void testTransientNameIdAssertionFailure() throws Exception {
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(SamlUtils.SAML_NAMESPACE_CONTEXT);
		String expression = "/saml2:Assertion/saml2:AttributeStatement/saml2:Attribute[@Name='gfipm:2.0:user:FederationId']/saml2:AttributeValue";
		Element valueElement = (Element) xPath.evaluate(expression, transientNameIdAssertion, XPathConstants.NODE);
		valueElement.setTextContent("STATE:AGENCY:IDP:nonexistent");
		RequestAuthorizationStatus status = samlAssertionRequestAuthorizer.authorizeAssertion("test", transientNameIdAssertion);
		assertEquals(status.authorized, false);
		assertTrue(status.message.matches(".+No connection-role mappings found.+"));
	}

}
