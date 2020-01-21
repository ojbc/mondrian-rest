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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

public class SamlUtilsTest {
	
	private Document assertionDocument;
	
	@BeforeEach
	public void beforeAll() throws Exception {
		InputStream assertionStream = new ClassPathResource("transient-name-id-assertion.xml").getInputStream();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		assertionDocument = dbf.newDocumentBuilder().parse(assertionStream);
	}
	
	@Test
	public void testAttribute() {
		assertEquals("Adam", SamlUtils.getAssertionAttributeValue(assertionDocument, "gfipm:2.0:user:GivenName"));
	}
	
	@Test
	public void testNameId() {
		assertEquals("TransientNameId", SamlUtils.getNameIdValue(assertionDocument));
	}

}
