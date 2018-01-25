package org.ojbc.mondrian.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component("requestAuthorizerTestAuthorizer")
public class TestRequestAuthorizer implements RequestAuthorizer {
	
	static final String TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME = "TestRequestAuthorizer-user-header";
	static final String RESTRICTED_USER_HEADER_VALUE = "restricted";
	static final String UNLIMITED_USER_HEADER_VALUE = "unlimited";

	@Override
	public RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, QueryRequest queryRequest) {
		
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		ret.authorized = false;
		
		String user = request.getHeader(TEST_REQUEST_AUTHORIZER_USER_HEADER_NAME);
		
		if (user != null) {
			if (RESTRICTED_USER_HEADER_VALUE.equals(user)) {
				ret.authorized = true;
				ret.mondrianRole = "Restricted_User";
			} else if (UNLIMITED_USER_HEADER_VALUE.equals(user)) {
				ret.authorized = true;
			}
		}
		
        return ret;
        
	}

}
