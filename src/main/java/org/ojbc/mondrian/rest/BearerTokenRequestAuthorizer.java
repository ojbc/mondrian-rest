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

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RequestAuthorizer that uses bearer token authentication on each request and determines that user's role for the connection in the query request.
 */
@Component
public class BearerTokenRequestAuthorizer implements RequestAuthorizer {
	
	private final Log log = LogFactory.getLog(BearerTokenRequestAuthorizer.class);
	
	private Map<String, Map<String, String>> tokenRoleMappings;
	
	@Value("${bearerTokenRequestAuthorizerConfigFileName:bearer-token-request-authorizer.json}")
	private String bearerTokenRequestAuthorizerConfigFileName;
	
	@PostConstruct
	public void init() throws Exception {
		tokenRoleMappings = RequestAuthorizer.AuthorizerUtil.convertRoleConnectionJsonToMaps(bearerTokenRequestAuthorizerConfigFileName);
	}
	
	public void setBearerTokenRequestAuthorizerConfigFileName(String bearerTokenRequestAuthorizerConfigFileName) {
		this.bearerTokenRequestAuthorizerConfigFileName = bearerTokenRequestAuthorizerConfigFileName;
	}

	public Map<String, Map<String, String>> getTokenRoleMappings() {
		return tokenRoleMappings;
	}
	
	void setTokenRoleMappings(Map<String, Map<String, String>> tokenRoleMappings) {
		this.tokenRoleMappings = tokenRoleMappings;
	}
	
	@Override
	public RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, QueryRequest queryRequest) {
		
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		ret.authorized = false;
		
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.matches("^Bearer .+")) {
			String token = authHeader.replaceFirst("^Bearer (.+)", "$1");
			ret.token = token;
			Map<String, String> connectionMappings = tokenRoleMappings.get(token);
			if (connectionMappings != null) {
				String connectionName = queryRequest.getConnectionName();
				if (connectionName != null) {
					String role = connectionMappings.get(connectionName);
					if (role != null) {
						ret.authorized = true;
						ret.mondrianRole = role;
						if (role.equals(ALL_ACCESS_ROLE_NAME)) {
							ret.mondrianRole = null;
						}
					} else {
						ret.message = "Authentication failed.  Token " + token + " found in config but not mapped to any connections.";
					}
				} else {
					ret.message = "Authentication failed.  Query request did not specify a connection.";
				}
				
			} else {
				ret.message = "Authentication failed.  Token " + token + " not found in config.";
			}
		} else {
			ret.message = "Authentication failed, no bearer authentication header present in request.";
		}
		
        return ret;
        
	}
	
}
