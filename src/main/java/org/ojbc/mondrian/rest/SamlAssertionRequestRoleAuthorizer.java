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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SamlAssertionRequestRoleAuthorizer extends AbstractSamlAssertionRequestAuthorizer {
	
	@Value("${samlAssertionRoleAttributeName:null}")
	private String roleAttributeName;

	public String getRoleAttributeName() {
		return roleAttributeName;
	}

	public void setRoleAttributeName(String roleAttributeName) {
		this.roleAttributeName = roleAttributeName;
	}

	@Override
	protected RequestAuthorizationStatus authorizeAssertion(String connectionName, Document assertion) {
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		ret.authorized = false;
		String role = SamlUtils.getAssertionAttributeValue(assertion, roleAttributeName);
		if (role != null) {
			ret.authorized = true;
			ret.token = getToken(assertion);
			ret.mondrianRole = role;
			if (role.equals(ALL_ACCESS_ROLE_NAME)) {
				ret.mondrianRole = null;
			}
			log.info("Authorized token " + getToken(assertion) + " with role " + role);
		} else {
			ret.message = "No role attribute with name " + roleAttributeName + " found in assertion with token " + getToken(assertion);
		}
		return ret;
	}

}
