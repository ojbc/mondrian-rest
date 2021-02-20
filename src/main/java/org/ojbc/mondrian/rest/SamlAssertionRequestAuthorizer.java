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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/**
 * RequestAuthorizer that uses information in a SAML assertion to authorize each request and determine that user's role for the connection in the query request.  It determines the role
 * by looking up the token in a map in the config file.
 */
@Component
public class SamlAssertionRequestAuthorizer extends AbstractSamlAssertionRequestAuthorizer {
	
	private Map<String, Map<String, String>> tokenRoleMappings;
	
	@Value("${samlAssertionRequestAuthorizerConfigFileName:saml-assertion-request-authorizer.json}")
	private String samlAssertionRequestAuthorizerConfigFileName;
	
	@PostConstruct
	public void init() throws Exception {
		tokenRoleMappings = RequestAuthorizer.AuthorizerUtil.convertRoleConnectionJsonToMaps(samlAssertionRequestAuthorizerConfigFileName);
	}
	
	public void setSamlAssertionRequestAuthorizerConfigFileName(String samlAssertionRequestAuthorizerConfigFileName) {
		this.samlAssertionRequestAuthorizerConfigFileName = samlAssertionRequestAuthorizerConfigFileName;
	}

	@Override
	protected RequestAuthorizationStatus authorizeAssertion(String connectionName, Document assertion) {
		Map<String, String> connectionMappings = getConnectionMappingsForAssertion(assertion);
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		ret.authorized = false;
		if (connectionMappings != null) {
			String role = connectionMappings.get(connectionName);
			if (role != null) {
				ret.authorized = true;
				ret.mondrianRole = role;
				ret.token = getToken(assertion);
				if (role.equals(ALL_ACCESS_ROLE_NAME)) {
					ret.mondrianRole = null;
				}
			} else {
				ret.message = "Authentication failed for SAML assertion with token " + getToken(assertion) + " for connection " + connectionName;
			}
		} else {
			ret.message = "Authentication failed.  No connection-role mappings found for assertion with token " + getToken(assertion);
		}
		return ret;
	}

	private Map<String, String> getConnectionMappingsForAssertion(Document assertion) {
		String token = getToken(assertion);
		if (token != null) {
			return tokenRoleMappings.get(token);
		}
		return null;
	}

}
