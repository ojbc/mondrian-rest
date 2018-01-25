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

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for objects that can authorize query requests incoming to the API.
 *
 */
public interface RequestAuthorizer {
	
	public static final String ALL_ACCESS_ROLE_NAME = RequestAuthorizer.class.getName() + "-All-Access";
	
	static final class RequestAuthorizationStatus {
		public boolean authorized;
		public String message;
		public String mondrianRole;
	}
	
	static final class AuthorizerUtil {
		
		public static final Map<String, Map<String, String>> convertRoleConnectionJsonToMaps(String jsonFileName) throws Exception {
			
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath*:" + jsonFileName);
			
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<Map<String, Map<String, String>>> typeRef = new TypeReference<Map<String, Map<String, String>>>() {};
			
			Map<String, Map<String, String>> ret = null;
			
			for (int i=0;i < resources.length && ret == null;i++) {
				Resource resource = resources[i];
				if (resource.exists()) {
					ret = mapper.readValue(resource.getInputStream(), typeRef);
				}
			}
			
			return ret;
			
		}
		
	}
	
	public RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, QueryRequest queryRequest) throws Exception;

}
