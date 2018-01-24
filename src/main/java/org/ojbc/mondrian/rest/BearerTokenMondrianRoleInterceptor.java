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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Interceptor that performs bearer token authentication on each request, and determines that user's roles and
 * sets the appropriate property on the passed-along http request.
 */
@Component
@PropertySource("classpath:bearer-token-mondrian-role-interceptor.properties")
@ConfigurationProperties
public class BearerTokenMondrianRoleInterceptor extends HandlerInterceptorAdapter {
	
	private final Log log = LogFactory.getLog(BearerTokenMondrianRoleInterceptor.class);
	
	private Map<String, String> tokenRoleMappings;
	
	public Map<String, String> getTokenRoleMappings() {
		return tokenRoleMappings;
	}

	public void setTokenRoleMappings(Map<String, String> tokenRoleMappings) {
		this.tokenRoleMappings = tokenRoleMappings;
	}

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		return authenticateRequest(request, response);
    }

	boolean authenticateRequest(HttpServletRequest request, HttpServletResponse response) {
		
		boolean ret = false;
		
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.matches("^Bearer .+")) {
			String token = authHeader.replaceFirst("^Bearer (.+)", "$1");
			String role = tokenRoleMappings.get(token);
			if (role != null) {
				request.setAttribute(Application.ROLE_REQUEST_ATTRIBUTE_NAME, role);
				log.debug("Successfully authenticated via bearer token " + token + ", role=" + role);
				ret = true;
			} else {
				log.debug("Authentication failed.  Token " + token + " not found in config.");
			}
		} else {
			log.debug("Authentication failed, no bearer authentication header present in request.");
		}
		
		if (!ret) {
			response.setStatus(403);
		}
		
        return ret;
        
	}
	
}
