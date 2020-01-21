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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mondrian.olap.Axis;
import mondrian.olap.Cell;
import mondrian.olap.Connection;
import mondrian.olap.Member;
import mondrian.olap.Position;
import mondrian.olap.Query;
import mondrian.olap.Result;

public class BasicMondrianQueryTest {
	
	private final Log log = LogFactory.getLog(BasicMondrianQueryTest.class);
	
	private Connection mondrianOlapConnection;
	
	@Before
	public void setUp() throws Exception {
		log.debug("setUp");
		mondrianOlapConnection = DatabaseUtils.getInstance().getMondrianConnection();
	}
	
	@After
	public void tearDown() throws Exception {
		mondrianOlapConnection.close();
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void test() throws Exception {
		String query = "select {[Measures].[F1_M1]} on columns from Test_F1";
		Query q = mondrianOlapConnection.parseQuery(query);
		Result result = mondrianOlapConnection.execute(q);
		Axis[] axes = result.getAxes();
		List<Position> positions = axes[0].getPositions();
		assertEquals(1, positions.size());
		Position p = positions.get(0);
		Member m = p.get(0);
		assertEquals("Measures", m.getDimension().getName());
		assertTrue(m.isMeasure());
		Cell cell = result.getCell(new int[]{0});
		assertEquals(3.0, cell.getValue());
	}

}
