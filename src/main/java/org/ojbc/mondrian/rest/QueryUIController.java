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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class QueryUIController {

	@Autowired
	private ServletContext servletContext;

	@Value("${queryUIEnabled:true}")
	private boolean queryUIEnabled;

	private String queryUIBody;
	private HttpStatus status;

	@PostConstruct
	public void init() throws IOException {
		queryUIBody = queryUIEnabled ? readResource("/query-ui/QueryUI.html").toString() : null;
		status = queryUIEnabled ? HttpStatus.OK : HttpStatus.FORBIDDEN;
	}

	@RequestMapping(value="/query-ui/*", method=RequestMethod.GET, produces="text/html")
	public ResponseEntity<String> getQueryUI() {
		return new ResponseEntity<String>(queryUIBody, status);
	}

	private StringBuffer readResource(String htmlPath) {
		InputStream is = servletContext.getResourceAsStream(htmlPath);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer(1024*10);
		String line = null;
		try {
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return sb;
	}

}
