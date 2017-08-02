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
package org.ojbc.mondrian.util;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ojbc.mondrian.CellSetWrapper;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializationTest {
	
	private final Log log = LogFactory.getLog(JsonSerializationTest.class);
	
	private Connection jdbcConnection;
	
	@Before
	public void setUp() throws Exception {
		jdbcConnection = DatabaseUtils.getInstance().getOlap4jConnection();
	}
	
	@After
	public void tearDown() throws Exception {
		jdbcConnection.close();
	}
	
	@Test
	public void test() throws Exception {
		
		OlapConnection olapConnection = jdbcConnection.unwrap(OlapConnection.class);
		OlapStatement statement = olapConnection.createStatement();
		CellSet cellSet = statement.executeOlapQuery("select {[Measures].[F1_M1]} on columns from Test");
		
		CellSetWrapper w = new CellSetWrapper(cellSet);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(w);
		log.info(json);
		
	}
	
}
