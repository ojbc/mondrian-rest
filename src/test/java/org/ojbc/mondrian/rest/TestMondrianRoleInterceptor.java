package org.ojbc.mondrian.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component("roleInterceptorTestInterceptor")
public class TestMondrianRoleInterceptor extends HandlerInterceptorAdapter {
	
	static final String TEST_INTERCEPTOR_USER_HEADER_NAME = "TestMondrianRoleInterceptor-user-header";
	static final String RESTRICTED_USER_HEADER_VALUE = "restricted";
	static final String UNLIMITED_USER_HEADER_VALUE = "unlimited";

	private final Log log = LogFactory.getLog(TestMondrianRoleInterceptor.class);
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		return authenticateRequest(request, response);
    }

	private boolean authenticateRequest(HttpServletRequest request, HttpServletResponse response) {
		
		String user = request.getHeader(TEST_INTERCEPTOR_USER_HEADER_NAME);
		boolean authenticated = false;
		
		if (user != null) {
			if (RESTRICTED_USER_HEADER_VALUE.equals(user)) {
				request.setAttribute(Application.ROLE_REQUEST_ATTRIBUTE_NAME, "Restricted_User");
				authenticated = true;
			} else if (UNLIMITED_USER_HEADER_VALUE.equals(user)) {
				authenticated = true;
			}
		}
		
		if (!authenticated) {
			response.setStatus(403);
		}
		
		return authenticated;
	}

}
