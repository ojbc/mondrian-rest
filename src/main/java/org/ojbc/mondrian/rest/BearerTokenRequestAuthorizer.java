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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * RequestAuthorizer that uses bearer token authentication on each request and determines that user's role for the connection in the query request.
 */
@Component
@PropertySource("classpath:bearer-token-request-authorizer.properties")
@ConfigurationProperties
public class BearerTokenRequestAuthorizer implements RequestAuthorizer {
	
	private final Log log = LogFactory.getLog(BearerTokenRequestAuthorizer.class);
	
	private Map<String, String> tokenRoleMappings;
	
	public Map<String, String> getTokenRoleMappings() {
		return tokenRoleMappings;
	}

	public void setTokenRoleMappings(Map<String, String> tokenRoleMappings) {
		this.tokenRoleMappings = tokenRoleMappings;
	}

	@Override
	public RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, QueryRequest queryRequest) {
		
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.matches("^Bearer .+")) {
			String token = authHeader.replaceFirst("^Bearer (.+)", "$1");
			// todo: need to lookup role by connection name
			String role = tokenRoleMappings.get(token);
			if (role != null) {
				log.debug("Successfully authenticated via bearer token " + token + ", role=" + role);
				ret.authorized = true;
				ret.message = null;
				ret.mondrianRole = role;
			} else {
				ret.authorized = false;
				ret.message = "Authentication failed.  Token " + token + " not found in config.";
			}
		} else {
			ret.authorized = false;
			ret.message = "Authentication failed, no bearer authentication header present in request.";
		}
		
        return ret;
        
	}
	
}
