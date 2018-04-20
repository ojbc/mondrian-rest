package org.ojbc.mondrian.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class SamlAssertionRequestRoleAuthorizer extends AbstractSamlAssertionRequestAuthorizer {
	
	private final Log log = LogFactory.getLog(SamlAssertionRequestRoleAuthorizer.class);
	
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
		String role = getAssertionAttributeValue(assertion, roleAttributeName);
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
