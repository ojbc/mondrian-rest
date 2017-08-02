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
package org.ojbc.mondrian;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.olap4j.Cell;
import org.olap4j.CellSet;

public class CellSetWrapperTest {
	
	@Test
	public void testSingleAxisSingleDimension() {
		
		CellSet cellSet = TestCellSetFactory.getInstance().getSingleAxisSingleDimensionCellSet();
		CellSetWrapper w = new CellSetWrapper(cellSet);
		
		List<AxisWrapper> axes = w.getAxisWrappers();
		assertEquals(1, axes.size());
		
		AxisWrapper axisWrapper = axes.get(0);
		assertEquals("COLUMNS", axisWrapper.getName());
		assertEquals(0, axisWrapper.getOrdinal());
		
		List<PositionWrapper> positions = axes.get(0).getPositionWrappers();
		assertEquals(1, positions.size());
		
		PositionWrapper positionWrapper = positions.get(0);
		assertEquals(Collections.singletonList("Measures"), positionWrapper.getMemberDimensionNames());
		assertEquals(Collections.singletonList("Measures"), positionWrapper.getMemberDimensionCaptions());
		assertEquals(Collections.singletonList("M1"), positionWrapper.getMemberDimensionValues());
		
		List<CellWrapper> cells = w.getCellWrappers();
		assertEquals(1, cells.size());
		
		CellWrapper cellWrapper = cells.get(0);
		assertEquals(1.0, cellWrapper.getValue());
		assertEquals("1.0", cellWrapper.getFormattedValue());
		assertEquals(0, cellWrapper.getOrdinal());
		assertEquals(Collections.singletonList(0), cellWrapper.getCoordinates());
		
	}
	
	@Test
	public void testDualAxisSingleDimension() {
		
		CellSet cellSet = TestCellSetFactory.getInstance().getDualAxisSingleDimensionCellSet();
		CellSetWrapper w = new CellSetWrapper(cellSet);
		
		List<AxisWrapper> axes = w.getAxisWrappers();
		assertEquals(2, axes.size());
		
		AxisWrapper axisWrapper = axes.get(0);
		assertEquals("COLUMNS", axisWrapper.getName());
		assertEquals(0, axisWrapper.getOrdinal());
		
		List<PositionWrapper> columnPositions = axisWrapper.getPositionWrappers();
		assertEquals(1, columnPositions.size());
		
		PositionWrapper positionWrapper = columnPositions.get(0);
		assertEquals(Collections.singletonList("Measures"), positionWrapper.getMemberDimensionNames());
		assertEquals(Collections.singletonList("Measures"), positionWrapper.getMemberDimensionCaptions());
		assertEquals(Collections.singletonList("M1"), positionWrapper.getMemberDimensionValues());
		
		axisWrapper = axes.get(1);
		assertEquals("ROWS", axisWrapper.getName());
		assertEquals(1, axisWrapper.getOrdinal());
		
		List<PositionWrapper> rowPositions = axisWrapper.getPositionWrappers();
		assertEquals(2, rowPositions.size());
		
		positionWrapper = rowPositions.get(0);
		assertEquals(Collections.singletonList("D1"), positionWrapper.getMemberDimensionNames());
		assertEquals(Collections.singletonList("D1"), positionWrapper.getMemberDimensionCaptions());
		assertEquals(Collections.singletonList("D1_V1"), positionWrapper.getMemberDimensionValues());
		
		positionWrapper = rowPositions.get(1);
		assertEquals(Collections.singletonList("D1"), positionWrapper.getMemberDimensionNames());
		assertEquals(Collections.singletonList("D1"), positionWrapper.getMemberDimensionCaptions());
		assertEquals(Collections.singletonList("D1_V2"), positionWrapper.getMemberDimensionValues());
		
		List<CellWrapper> cells = w.getCellWrappers();
		assertEquals(2, cells.size());
		
		CellWrapper cellWrapper = cells.get(0);
		assertEquals(1.0, cellWrapper.getValue());
		assertEquals("1.0", cellWrapper.getFormattedValue());
		assertEquals(0, cellWrapper.getOrdinal());
		List<Integer> coordinates = cellWrapper.getCoordinates();
		assertEquals(Arrays.asList(new Integer[]{0, 0}), coordinates);
		assertEquals(Collections.singletonList("Measures"), columnPositions.get(coordinates.get(0)).getMemberDimensionNames());
		assertEquals(Collections.singletonList("M1"), columnPositions.get(coordinates.get(0)).getMemberDimensionValues());
		assertEquals(Collections.singletonList("D1"), rowPositions.get(coordinates.get(1)).getMemberDimensionNames());
		assertEquals(Collections.singletonList("D1_V1"), rowPositions.get(coordinates.get(1)).getMemberDimensionValues());
		
		cellWrapper = cells.get(1);
		assertEquals(2.0, cellWrapper.getValue());
		assertEquals("2.0", cellWrapper.getFormattedValue());
		assertEquals(1, cellWrapper.getOrdinal());
		coordinates = cellWrapper.getCoordinates();
		assertEquals(Arrays.asList(new Integer[]{0, 1}), coordinates);
		assertEquals(Collections.singletonList("Measures"), columnPositions.get(coordinates.get(0)).getMemberDimensionNames());
		assertEquals(Collections.singletonList("M1"), columnPositions.get(coordinates.get(0)).getMemberDimensionValues());
		assertEquals(Collections.singletonList("D1"), rowPositions.get(coordinates.get(1)).getMemberDimensionNames());
		assertEquals(Collections.singletonList("D1_V2"), rowPositions.get(coordinates.get(1)).getMemberDimensionValues());
		
	}
	
	@Test
	public void testDualAxisTwoDimensions() {
		
		CellSet cellSet = TestCellSetFactory.getInstance().getDualAxisTwoDimensionCellSet();
		CellSetWrapper w = new CellSetWrapper(cellSet);
		
		List<AxisWrapper> axes = w.getAxisWrappers();
		assertEquals(2, axes.size());
		
		AxisWrapper columnsAxis = axes.get(0);
		
		assertEquals("COLUMNS", columnsAxis.getName());
		assertEquals(0, columnsAxis.getOrdinal());
		
		List<PositionWrapper> columnPositions = columnsAxis.getPositionWrappers();
		assertEquals(2, columnPositions.size());

		int valIndex = 1;
		for (PositionWrapper positionWrapper : columnPositions) {
			assertEquals(Arrays.asList(new String[] {"Measures", "D1"}), positionWrapper.getMemberDimensionNames());
			assertEquals(Arrays.asList(new String[] {"Measures", "D1"}), positionWrapper.getMemberDimensionCaptions());
			assertEquals(Arrays.asList(new String[] {"M1", "D1_V" + valIndex++}), positionWrapper.getMemberDimensionValues());
		}
		
		AxisWrapper rowsAxis = axes.get(1);
		
		assertEquals("ROWS", rowsAxis.getName());
		assertEquals(1, rowsAxis.getOrdinal());
		
		columnPositions = rowsAxis.getPositionWrappers();
		assertEquals(3, columnPositions.size());
		
		valIndex = 1;
		for (PositionWrapper positionWrapper : columnPositions) {
			assertEquals(Collections.singletonList("D2"), positionWrapper.getMemberDimensionNames());
			assertEquals(Collections.singletonList("D2"), positionWrapper.getMemberDimensionCaptions());
			assertEquals(Collections.singletonList("D2_V" + valIndex++), positionWrapper.getMemberDimensionValues());
		}
		
		Double[] expectedValues = new Double[] {1.0, 10.0, 2.0, 11.0, 3.0, null};
		
		List<CellWrapper> cellWrappers = w.getCellWrappers();
		
		for (int i=0;i < expectedValues.length;i++) {
			CellWrapper cellWrapper = cellWrappers.get(i);
			Cell cell = cellSet.getCell(i);
			assertEquals(i, cellWrapper.getOrdinal());
			Number cellWrapperValue = cellWrapper.getValue();
			Object cellValue = cell.getValue();
			assertTrue(expectedValues[i] == null && cellWrapperValue == null ||
					expectedValues[i].equals(cellWrapperValue));
			assertTrue(cellValue == null && cellWrapperValue == null ||
					cellValue.equals(cellWrapperValue));
		}
		
	}


}
