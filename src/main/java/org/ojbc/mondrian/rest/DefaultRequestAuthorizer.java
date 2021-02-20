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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * RequestAuthorizer that authorizes all requests and assigns all requests a pre-defined role, or a null role if none is configured.
 */
@Component
@Slf4j
public class DefaultRequestAuthorizer implements RequestAuthorizer {
	
	@Value("${defaultRequestAuthorizerRole:#{null}}")
	private String defaultRequestAuthorizerRole;
	
	@PostConstruct
	public void init() throws Exception {
		log.info("DefaultRequestAuthorizer will use role " + defaultRequestAuthorizerRole + " for connections");
	}
	
	@Override
	public RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, String connectionName) throws Exception {
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		ret.authorized = true;
		ret.token = "[None]";
		ret.mondrianRole = defaultRequestAuthorizerRole;
		return ret;
	}

}
