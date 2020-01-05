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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Abstract base class of rest controller tests
 *
 */
public abstract class AbstractMondrianRestControllerTest {
	
	@Autowired
    protected TestRestTemplate restTemplate;

	protected HttpEntity<String> buildQueryRequestEntity(String connectionName, String queryString) {
		return buildQueryRequestEntity(connectionName, queryString, null);
	}

	protected HttpEntity<String> buildQueryRequestEntity(String connectionName, String queryString, Map<String, String> headerMap) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (headerMap != null) {
			for(String key : headerMap.keySet()) {
				headers.set(key, headerMap.get(key));
			}
		}
		return new HttpEntity<String>("{ \"connectionName\" : \"" + connectionName + "\", \"query\" : \"" + queryString + "\"}", headers);
	}

	protected HttpEntity<String> buildQueryRequestEntity(String connectionName, String queryString, boolean tidy) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<String>("{ \"connectionName\" : \"" + connectionName + "\", \"query\" : \"" + queryString + "\", \"tidy\" : { \"enabled\": " +
				tidy + "}}", headers);
	}

}