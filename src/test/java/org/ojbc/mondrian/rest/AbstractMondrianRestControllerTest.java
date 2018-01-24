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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Abstract base class of rest controller tests
 *
 */
public class AbstractMondrianRestControllerTest {

	protected StringEntity buildQueryRequestEntity(String connectionName, String queryString) {
		return new StringEntity("{ \"connectionName\" : \"" + connectionName + "\", \"query\" : \"" + queryString + "\"}", ContentType.APPLICATION_JSON);
	}

	protected StringEntity buildQueryRequestEntity(String connectionName, String queryString, boolean tidy) {
		return new StringEntity("{ \"connectionName\" : \"" + connectionName + "\", \"query\" : \"" + queryString + "\", \"tidy\" : { \"enabled\": " +
				tidy + "}}", ContentType.APPLICATION_JSON);
	}

	protected String getBodyContent(HttpResponse response) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		
		StringBuffer contentBuffer = new StringBuffer();
		String output = null;
		
		while ((output = br.readLine()) != null) {
			contentBuffer.append(output);
		}
		
		return contentBuffer.toString();
		
	}

	protected final Map<String, String> getContentAsMap(String jsonContent) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		TypeFactory factory = TypeFactory.defaultInstance();
		MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
		return mapper.readValue(jsonContent, type);
	}

}