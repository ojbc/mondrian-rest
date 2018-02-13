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

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

/**
 * RequestAuthorizer that authorizes all requests and assigns all requests a null role.
 */
@Component
public class DefaultRequestAuthorizer implements RequestAuthorizer {
	
	@Override
	public RequestAuthorizationStatus authorizeRequest(HttpServletRequest request, QueryRequest queryRequest) throws Exception {
		RequestAuthorizationStatus ret = new RequestAuthorizationStatus();
		ret.authorized = true;
		ret.token = "[None]";
		return ret;
	}

}
