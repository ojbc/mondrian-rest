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
