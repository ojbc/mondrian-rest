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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestDatabaseAvailabilityTest {
	
	private final Log log = LogFactory.getLog(TestDatabaseAvailabilityTest.class);
	
	private Connection connection;
	
	@BeforeEach
	public void setUp() throws Exception {
		connection = DatabaseUtils.getInstance().getTestDatabaseConnection(true);
		//dumpTableList();
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		connection.close();
	}

	@Test
	public void test() throws Exception {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select count(*) from F1");
		assertTrue(resultSet.next());
		int rowCount = resultSet.getInt(1);
		assertEquals(3, rowCount);
		resultSet = statement.executeQuery("select count(*) from D1");
		assertTrue(resultSet.next());
		rowCount = resultSet.getInt(1);
		assertEquals(2, rowCount);
		resultSet = statement.executeQuery("select * from D1");
	}
	
	@Test
	public void testCase() throws Exception {
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select F3_value from F3");
		assertTrue(resultSet.next());
		resultSet = statement.executeQuery("select F3_VALUE from F3");
		assertTrue(resultSet.next());
	}

	@Test
	@Disabled // because it takes awhile for the database to load up
	public void testFoodmartAvailable() throws Exception {
		
		// verify that we can load up the hsqldb with foodmart in it
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:res:foodmart;set schema \"foodmart\"", "FOODMART", "FOODMART");
		Statement s = conn.createStatement();
		s.executeQuery("select \"employee_id\" from \"employee\"");
		
	}
	
	void dumpTableList() throws SQLException {
		DatabaseMetaData dbmd = connection.getMetaData();
		ResultSet rs = dbmd.getTables(null, null, "%", null);
		while (rs.next()) {
			log.info(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3));
		}
	}
	
}
