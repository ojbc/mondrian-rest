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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapStatement;
import org.olap4j.Position;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicOlap4jQueryTest {
	
	private Connection jdbcConnection;
	
	@BeforeEach
	public void setUp() throws Exception {
		jdbcConnection = DatabaseUtils.getInstance().getOlap4jConnection();
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		jdbcConnection.close();
	}
	
	@Test
	@Disabled
	public void testExpt() throws Exception {
		// convenience test, ignored when doing builds, as a place to play around with MDX queries, do experiments, etc.
		OlapConnection olapConnection = jdbcConnection.unwrap(OlapConnection.class);
		OlapStatement statement = olapConnection.createStatement();
		CellSet cellSet = statement.executeOlapQuery("select {[Measures].[F1_M1]} on columns, " +
				" {[D1].[D1_DESCRIPTION].members as foo} on rows" +
				" from Test_F1");
		List<CellSetAxis> axes = cellSet.getAxes();
		CellSetAxis axis = axes.get(1);
		for (Position p : axis.getPositions()) {
			List<Member> members = p.getMembers();
			for (Member m : members) {
				log.info(m.getName());
				log.info(m.getHierarchy().getUniqueName());
				Level level = m.getLevel();
				log.info(level.getName());
				log.info(level.getUniqueName());
				log.info(level.getCaption());
			}
		}
	}
	
	@Test
	public void test1Axis() throws Exception {
		OlapConnection olapConnection = jdbcConnection.unwrap(OlapConnection.class);
		OlapStatement statement = olapConnection.createStatement();
		CellSet cellSet = statement.executeOlapQuery("select {[Measures].[F1_M1]} on columns from Test_F1");
		List<CellSetAxis> axes = cellSet.getAxes();
		assertEquals(1, axes.size());
		CellSetAxis axis = axes.get(0);
		List<Position> positions = axis.getPositions();
		assertEquals(1, positions.size());
		Position p = positions.get(0);
		List<Member> members = p.getMembers();
		assertEquals(1, members.size());
		Member m = members.get(0);
		assertEquals("Measures", m.getDimension().getName());
		Cell cell = cellSet.getCell(0);
		assertEquals(3.0, cell.getValue());
	}
	
	@Test
	public void test2Axes2Dimensions() throws Exception {
		
		OlapConnection olapConnection = jdbcConnection.unwrap(OlapConnection.class);
		OlapStatement statement = olapConnection.createStatement();
		
		CellSet cellSet = statement.executeOlapQuery("select CrossJoin({[D2].[D2_DESCRIPTION].members}, {[Measures].[F3_M1]}) on columns, " +
				" {[D1].[D1_DESCRIPTION].members} on rows" +
				" from Test_F3");
		
		List<CellSetAxis> axes = cellSet.getAxes();
		assertEquals(2, axes.size());
		
		List<Position> columnPositions = axes.get(Axis.COLUMNS.axisOrdinal()).getPositions();
		List<Position> rowPositions = axes.get(Axis.ROWS.axisOrdinal()).getPositions();
		
		int nCols = columnPositions.size();
		int nRows = rowPositions.size();
		
		assertEquals(2, nRows);
		assertEquals(3, nCols);
		
		// no need to repeat all the assertions in the other tests
		
		// primarily what we demonstrate here is that the cell ordinals are set in a row-wise fashion.  that is:
		
		// cell R1, C1 = ordinal 0
		// cell R1, C2 = ordinal 1
		// cell R2, C1 = ordinal 2
		// etc.
		
		// this is a bit counter-intuitive, since the columns axis is ordinal 0, and also the CellSet.getCell(Integer[] coordinates) method takes its
		// coordinates in axis-ordinal order (so, (col, row))
		
		boolean[] expectedEmpties = new boolean[] {false, false, true, true, true, false};
		Double[] expectedValues = new Double[] {1.0, 1.0, null, null, null, 1.0};
		
		int expectedOrdinal = 0;
		for (int row=0;row < nRows;row++) {
			for (int col=0;col < nCols;col++) {
				Cell cell = cellSet.getCell(Arrays.asList(new Integer[]{col, row}));
				assertEquals(expectedOrdinal, cell.getOrdinal());
				assertEquals(expectedEmpties[expectedOrdinal], cell.isEmpty());
				assertEquals(expectedEmpties[expectedOrdinal], cell.isNull());
				assertTrue((expectedValues[expectedOrdinal] == null && cell.getValue() == null) ||
						expectedValues[expectedOrdinal].equals(cell.getValue()));
				expectedOrdinal++;
			}
		}
		
		
	}
	
	@Test
	public void test2Axes() throws Exception {
		
		OlapConnection olapConnection = jdbcConnection.unwrap(OlapConnection.class);
		OlapStatement statement = olapConnection.createStatement();
		
		CellSet cellSet = statement.executeOlapQuery("select {[Measures].[F1_M1]} on columns, " +
				" {[D1].[D1_DESCRIPTION].members} on rows" +
				" from Test_F1");
		
		List<CellSetAxis> axes = cellSet.getAxes();
		assertEquals(2, axes.size());
		
		CellSetAxis columnAxis = axes.get(0);
		CellSetAxis rowAxis = axes.get(1);

		assertEquals(1, columnAxis.getPositionCount());
		Position columnAxisPosition = columnAxis.getPositions().get(0);
		List<Member> members = columnAxisPosition.getMembers();
		assertEquals(1, members.size());
		assertEquals("Measures", members.get(0).getDimension().getName());
		assertEquals("F1_M1", members.get(0).getName());
		
		int cellCount = columnAxis.getPositionCount() * rowAxis.getPositionCount();
		assertEquals(2, cellCount);
		
		assertEquals(2, rowAxis.getPositionCount());
		Position rowAxisPosition1 = rowAxis.getPositions().get(0);
		members = rowAxisPosition1.getMembers();

		assertEquals(1, members.size());
		assertEquals("D1", members.get(0).getDimension().getName());
		assertEquals("D1 One", members.get(0).getName());
		
		Position rowAxisPosition2 = rowAxis.getPositions().get(1);
		members = rowAxisPosition2.getMembers();
		assertEquals(1, members.size());
		assertEquals("D1", members.get(0).getDimension().getName());
		assertEquals("D1 Two", members.get(0).getName());
		
		// Different ways of navigating the cube of cells...
		
		// 1. by ordinal
		Cell cell11 = cellSet.getCell(0);
		Cell cell21 = cellSet.getCell(1);
		assertFalse(cell11.getValue().equals(cell21.getValue()));
		
		// 2. by column axis + row axis coordinates (zero-based)
		assertEquals(cell11.getValue(), cellSet.getCell(Arrays.asList(new Integer[] {0,0})).getValue());
		assertEquals(cell21.getValue(), cellSet.getCell(Arrays.asList(new Integer[] {0,1})).getValue());
		
		// 3. by position object intersection
		assertEquals(cell11.getValue(), cellSet.getCell(columnAxisPosition, rowAxisPosition1).getValue());
		assertEquals(cell11.getValue(), cellSet.getCell(rowAxisPosition1, columnAxisPosition).getValue()); // order of positions unimportant
		assertEquals(cell21.getValue(), cellSet.getCell(columnAxisPosition, rowAxisPosition2).getValue());
		
	}
	
}
