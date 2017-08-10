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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;


public class TestCellSetFactory {
	
	private static TestCellSetFactory INSTANCE = new TestCellSetFactory();
	
	private TestCellSetFactory() {
		
	}
	
	public static final TestCellSetFactory getInstance() {
		return INSTANCE;
	}
	
	public CellSet getSingleAxisSingleDimensionCellSet() {
		
		CellSet ret = mock(CellSet.class);
		
		Cell cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(1.0));
		when(cell.getFormattedValue()).thenReturn("1.0");
		when(cell.getOrdinal()).thenReturn(0);
		when(cell.getCoordinateList()).thenReturn(Collections.singletonList(new Integer(0)));
		when(cell.getCellSet()).thenReturn(ret);
		
		CellSetAxis axis = mock(CellSetAxis.class);
		Position p = mock(Position.class);
		Member m = mock(Member.class);
		Dimension d = mock(Dimension.class);
		when(d.getName()).thenReturn("Measures");
		when(d.getCaption()).thenReturn("Measures");
		when(m.getName()).thenReturn("M1");
		when(m.getDimension()).thenReturn(d);
		Level level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[Measures].[MeasuresLevel]");
		when(level.getName()).thenReturn("MeasuresLevel");
		when(m.getLevel()).thenReturn(level);
		
		List<Member> memberList = new ArrayList<>();
		memberList.add(m);
		when(p.getMembers()).thenReturn(memberList);
		
		List<Position> positionList = new ArrayList<>();
		positionList.add(p);
		when(axis.getPositions()).thenReturn(positionList);
		when(axis.getPositionCount()).thenReturn(positionList.size());
		when(axis.getAxisOrdinal()).thenReturn(Axis.COLUMNS);
		
		List<CellSetAxis> axisList = new ArrayList<>();
		axisList.add(axis);
		when(ret.getAxes()).thenReturn(axisList);
		
		when(ret.getCell(0)).thenReturn(cell);
		when(ret.getCell(p)).thenReturn(cell);
		
		return ret;
		
	}
	
	public CellSet getDualAxisSingleDimensionCellSet() {
		
		// scenario:
		//  one measure (M1)
		//  one dimension (D1) that has two levels:  D1_V1 and D1_V2
		//  so a table would look like this:
		
		/*
		 * 
		 * ----------------------------------
		 * |              | * M1 *          |
		 * ----------------------------------
		 * | D1_V1        | 1.0             |
		 * | D1_V2        | 2.0             |
		 * ----------------------------------
		 * 
		 */
		
		CellSet ret = mock(CellSet.class);
		
		List<CellSetAxis> axisList = new ArrayList<>();
		
		CellSetAxis axis = mock(CellSetAxis.class);
		when(ret.getAxes()).thenReturn(axisList);
		axisList.add(axis);
		Position p1 = mock(Position.class);
		Member m = mock(Member.class);
		Dimension d = mock(Dimension.class);
		when(d.getName()).thenReturn("Measures");
		when(d.getCaption()).thenReturn("Measures");
		when(m.getName()).thenReturn("M1");
		when(m.getDimension()).thenReturn(d);
		Level level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[Measures].[MeasuresLevel]");
		when(level.getName()).thenReturn("MeasuresLevel");
		when(m.getLevel()).thenReturn(level);
		
		List<Member> memberList = new ArrayList<>();
		memberList.add(m);
		when(p1.getMembers()).thenReturn(memberList);
		
		List<Position> positionList = new ArrayList<>();
		positionList.add(p1);
		when(axis.getPositions()).thenReturn(positionList);
		when(axis.getPositionCount()).thenReturn(positionList.size());
		when(axis.getAxisOrdinal()).thenReturn(Axis.COLUMNS);
		
		axis = mock(CellSetAxis.class);
		positionList = new ArrayList<>();
		
		when(axis.getAxisOrdinal()).thenReturn(Axis.ROWS);
		
		axisList.add(axis);

		Position p2 = mock(Position.class);
		positionList.add(p2);
		d = mock(Dimension.class);
		when(d.getName()).thenReturn("D1");
		when(d.getCaption()).thenReturn("D1");
		m = mock(Member.class);
		when(m.getName()).thenReturn("D1_V1");
		when(m.getDimension()).thenReturn(d);
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D1].[D1].[D1_V1]");
		when(level.getName()).thenReturn("D1_V1");
		when(m.getLevel()).thenReturn(level);
		memberList = new ArrayList<>();
		when(p2.getMembers()).thenReturn(memberList);
		memberList.add(m);
		
		Position p3 = mock(Position.class);
		positionList.add(p3);
		d = mock(Dimension.class);
		when(d.getName()).thenReturn("D1");
		when(d.getCaption()).thenReturn("D1");
		m = mock(Member.class);
		when(m.getName()).thenReturn("D1_V2");
		when(m.getDimension()).thenReturn(d);
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D1].[D1].[D1_V2]");
		when(level.getName()).thenReturn("D1_V2");
		when(m.getLevel()).thenReturn(level);
		memberList = new ArrayList<>();
		when(p3.getMembers()).thenReturn(memberList);
		memberList.add(m);
		
		when(axis.getPositions()).thenReturn(positionList);
		when(axis.getPositionCount()).thenReturn(positionList.size());
		
		Cell cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(1.0));
		when(cell.getFormattedValue()).thenReturn("1.0");
		when(cell.getOrdinal()).thenReturn(0);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{0, 0}));
		when(cell.getCellSet()).thenReturn(ret);
		
		when(ret.getCell(0)).thenReturn(cell);
		when(ret.getCell(p1, p2)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(2.0));
		when(cell.getFormattedValue()).thenReturn("2.0");
		when(cell.getOrdinal()).thenReturn(1);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{0, 1}));
		when(cell.getCellSet()).thenReturn(ret);
		
		when(ret.getCell(1)).thenReturn(cell);
		when(ret.getCell(p1, p3)).thenReturn(cell);
		
		return ret;
		
	}

	public CellSet getDualAxisTwoDimensionCellSet() {
		
		// scenario:
		//  one measure (M1)
		//  two dimensions:
		//    one (D1) that has two levels:  D1_V1 and D1_V2 (on the columns axis)
		//    one (D2) that has three levels:  D2_V1, D2_V2, and D2_V3 (on the rows axis)
		//  so a table would look like this:
		
		/*
		 * 
		 * -------------------------------------
		 * |              |******** M1 ********|
		 * |              | D1_V1   | D1_V2    |
		 * -------------------------------------
		 * | D2_V1        | 1.0     | 10.0     |
		 * | D2_V2        | 2.0     | 11.0     |
		 * | D2_V3        | 3.0     |          |
		 * -------------------------------------
		 * 
		 */
		
		CellSet ret = mock(CellSet.class);
		
		List<CellSetAxis> axisList = new ArrayList<>();
		
		// set up columns axis
		
		CellSetAxis axis = mock(CellSetAxis.class);
		when(axis.getAxisOrdinal()).thenReturn(Axis.COLUMNS);
		when(ret.getAxes()).thenReturn(axisList);
		axisList.add(axis);
		
		List<Position> positionList = new ArrayList<>();
		when(axis.getPositions()).thenReturn(positionList);
		
		Position c1Position = mock(Position.class);
		positionList.add(c1Position);
		
		List<Member> memberList = new ArrayList<>();
		when(c1Position.getMembers()).thenReturn(memberList);

		Dimension measuresDimension = mock(Dimension.class);
		when(measuresDimension.getName()).thenReturn("Measures");
		when(measuresDimension.getCaption()).thenReturn("Measures");
		
		Dimension d1Dimension = mock(Dimension.class);
		when(d1Dimension.getName()).thenReturn("D1");
		when(d1Dimension.getCaption()).thenReturn("D1");
		
		Member m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(measuresDimension);
		when(m.getName()).thenReturn("M1");
		Level level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[Measures].[MeasuresLevel]");
		when(level.getName()).thenReturn("MeasuresLevel");
		when(m.getLevel()).thenReturn(level);
		
		m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(d1Dimension);
		when(m.getName()).thenReturn("D1_V1");
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D1].[D1].[D1_V1]");
		when(level.getName()).thenReturn("D1_V1");
		when(m.getLevel()).thenReturn(level);
		
		Position c2Position = mock(Position.class);
		positionList.add(c2Position);
		
		memberList = new ArrayList<>();
		when(c2Position.getMembers()).thenReturn(memberList);
		
		m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(measuresDimension);
		when(m.getName()).thenReturn("M1");
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[Measures].[MeasuresLevel]");
		when(level.getName()).thenReturn("MeasuresLevel");
		when(m.getLevel()).thenReturn(level);
		
		m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(d1Dimension);
		when(m.getName()).thenReturn("D1_V2");
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D1].[D1].[D1_V2]");
		when(level.getName()).thenReturn("D1_V2");
		when(m.getLevel()).thenReturn(level);
		
		when(axis.getPositionCount()).thenReturn(positionList.size());
		
		// set up rows axis
		
		axis = mock(CellSetAxis.class);
		positionList = new ArrayList<>();
		
		when(axis.getAxisOrdinal()).thenReturn(Axis.ROWS);
		
		axisList.add(axis);

		Position r1Position = mock(Position.class);
		positionList.add(r1Position);
		Dimension d2Dimension = mock(Dimension.class);
		when(d2Dimension.getName()).thenReturn("D2");
		when(d2Dimension.getCaption()).thenReturn("D2");
		
		m = mock(Member.class);
		when(m.getName()).thenReturn("D2_V1");
		when(m.getDimension()).thenReturn(d2Dimension);
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D2].[D2].[D2_V1]");
		when(level.getName()).thenReturn("D2_V1");
		when(m.getLevel()).thenReturn(level);
		memberList = new ArrayList<>();
		when(r1Position.getMembers()).thenReturn(memberList);
		memberList.add(m);
		
		Position r2Position = mock(Position.class);
		positionList.add(r2Position);
		
		m = mock(Member.class);
		when(m.getName()).thenReturn("D2_V2");
		when(m.getDimension()).thenReturn(d2Dimension);
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D2].[D2].[D2_V2]");
		when(level.getName()).thenReturn("D2_V2");
		when(m.getLevel()).thenReturn(level);
		memberList = new ArrayList<>();
		when(r2Position.getMembers()).thenReturn(memberList);
		memberList.add(m);
		
		Position r3Position = mock(Position.class);
		positionList.add(r3Position);
		
		m = mock(Member.class);
		when(m.getName()).thenReturn("D2_V3");
		when(m.getDimension()).thenReturn(d2Dimension);
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D2].[D2].[D2_V3]");
		when(level.getName()).thenReturn("D2_V3");
		when(m.getLevel()).thenReturn(level);
		memberList = new ArrayList<>();
		when(r3Position.getMembers()).thenReturn(memberList);
		memberList.add(m);
		
		when(axis.getPositions()).thenReturn(positionList);
		when(axis.getPositionCount()).thenReturn(positionList.size());
		
		Cell cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(1.0));
		when(cell.getFormattedValue()).thenReturn("1.0");
		when(cell.getOrdinal()).thenReturn(0);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{0, 0}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(0)).thenReturn(cell);
		when(ret.getCell(c1Position, r1Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(10.0));
		when(cell.getFormattedValue()).thenReturn("10.0");
		when(cell.getOrdinal()).thenReturn(1);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{1, 0}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(1)).thenReturn(cell);
		when(ret.getCell(c2Position, r1Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(2.0));
		when(cell.getFormattedValue()).thenReturn("2.0");
		when(cell.getOrdinal()).thenReturn(2);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{0, 1}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(2)).thenReturn(cell);
		when(ret.getCell(c1Position, r2Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(11.0));
		when(cell.getFormattedValue()).thenReturn("11.0");
		when(cell.getOrdinal()).thenReturn(3);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{1, 1}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(3)).thenReturn(cell);
		when(ret.getCell(c2Position, r2Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(3.0));
		when(cell.getFormattedValue()).thenReturn("3.0");
		when(cell.getOrdinal()).thenReturn(4);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{0, 2}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(4)).thenReturn(cell);
		when(ret.getCell(c1Position, r3Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(null);
		when(cell.getFormattedValue()).thenReturn("");
		when(cell.getOrdinal()).thenReturn(5);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{1, 2}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(5)).thenReturn(cell);
		when(ret.getCell(c2Position, r3Position)).thenReturn(cell);
		
		return ret;
		
	}
	
	public CellSet getDualAxisTwoDimensionTwoMeasuresCellSet() {
		
		// scenario:
		//  two measures (M1 and M2)
		//  two dimensions:
		//    one (D1) that has two levels:  D1_V1 and D1_V2 (on the columns axis)
		//    one (D2) that has three levels:  D2_V1, D2_V2, and D2_V3 (on the rows axis)
		//  so a table would look like this:
		
		/*
		 * 
		 * ----------------------------------------------------------
		 * |              |******** M1 ********|******** M2 ********|
		 * |              | D1_V1   | D1_V2    | D1_V1   | D1_V2    |
		 * ----------------------------------------------------------
		 * | D2_V1        | 1.0     | 10.0     | 4.0     | 20.0     |
		 * | D2_V2        | 2.0     | 11.0     | 5.0     | 21.0     |
		 * | D2_V3        | 3.0     |          | 6.0     |          |
		 * ----------------------------------------------------------
		 * 
		 */
		
		CellSet ret = getDualAxisTwoDimensionCellSet();
		CellSetAxis columnAxis = ret.getAxes().get(Axis.COLUMNS.axisOrdinal());
		List<Position> positionList = columnAxis.getPositions();
		
		Dimension measuresDimension = null;
		Dimension d1Dimension = null;
		
		for (Position position : positionList) {
			for (Member member : position.getMembers()) {
				Dimension dimension = member.getDimension();
				if (dimension.getName().equals("Measures")) {
					measuresDimension = dimension;
				} else if (dimension.getName().equals("D1")) {
					d1Dimension = dimension;
				}
			}
		}
		
		Position c3Position = mock(Position.class);
		positionList.add(c3Position);
		
		List<Member> memberList = new ArrayList<>();
		memberList = new ArrayList<>();
		when(c3Position.getMembers()).thenReturn(memberList);
		
		Member m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(measuresDimension);
		when(m.getName()).thenReturn("M2");
		Level level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[Measures].[MeasuresLevel]");
		when(level.getName()).thenReturn("MeasuresLevel");
		when(m.getLevel()).thenReturn(level);
		
		m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(d1Dimension);
		when(m.getName()).thenReturn("D1_V1");
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D1].[D1].[D1_V1]");
		when(level.getName()).thenReturn("D1_V1");
		when(m.getLevel()).thenReturn(level);
		
		Position c4Position = mock(Position.class);
		positionList.add(c4Position);
		
		memberList = new ArrayList<>();
		when(c4Position.getMembers()).thenReturn(memberList);
		
		m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(measuresDimension);
		when(m.getName()).thenReturn("M2");
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[Measures].[MeasuresLevel]");
		when(level.getName()).thenReturn("MeasuresLevel");
		when(m.getLevel()).thenReturn(level);
		
		m = mock(Member.class);
		memberList.add(m);
		when(m.getDimension()).thenReturn(d1Dimension);
		when(m.getName()).thenReturn("D1_V2");
		level = mock(Level.class);
		when(level.getUniqueName()).thenReturn("[D1].[D1].[D1_V2]");
		when(level.getName()).thenReturn("D1_V2");
		when(m.getLevel()).thenReturn(level);
		
		when(columnAxis.getPositionCount()).thenReturn(positionList.size());
		
		CellSetAxis rowAxis = ret.getAxes().get(Axis.ROWS.axisOrdinal());
		Position r1Position = rowAxis.getPositions().get(0);
		Position r2Position = rowAxis.getPositions().get(1);
		Position r3Position = rowAxis.getPositions().get(2);
		
		Cell oldCell = ret.getCell(2);
		when(ret.getCell(4)).thenReturn(oldCell);
		oldCell = ret.getCell(3);
		when(ret.getCell(5)).thenReturn(oldCell);
		oldCell = ret.getCell(4);
		when(ret.getCell(8)).thenReturn(oldCell);
		oldCell = ret.getCell(5);
		when(ret.getCell(9)).thenReturn(oldCell);
		
		Cell cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(4.0));
		when(cell.getFormattedValue()).thenReturn("4.0");
		when(cell.getOrdinal()).thenReturn(0);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{2, 0}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(2)).thenReturn(cell);
		when(ret.getCell(c3Position, r1Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(20.0));
		when(cell.getFormattedValue()).thenReturn("20.0");
		when(cell.getOrdinal()).thenReturn(1);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{3, 0}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(3)).thenReturn(cell);
		when(ret.getCell(c4Position, r1Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(5.0));
		when(cell.getFormattedValue()).thenReturn("5.0");
		when(cell.getOrdinal()).thenReturn(2);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{2, 1}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(6)).thenReturn(cell);
		when(ret.getCell(c3Position, r2Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(21.0));
		when(cell.getFormattedValue()).thenReturn("21.0");
		when(cell.getOrdinal()).thenReturn(3);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{3, 1}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(7)).thenReturn(cell);
		when(ret.getCell(c4Position, r2Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(new Double(6.0));
		when(cell.getFormattedValue()).thenReturn("6.0");
		when(cell.getOrdinal()).thenReturn(4);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{2, 2}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(10)).thenReturn(cell);
		when(ret.getCell(c3Position, r3Position)).thenReturn(cell);
		
		cell = mock(Cell.class);
		when(cell.getValue()).thenReturn(null);
		when(cell.getFormattedValue()).thenReturn("");
		when(cell.getOrdinal()).thenReturn(5);
		when(cell.getCoordinateList()).thenReturn(Arrays.asList(new Integer[]{3, 2}));
		when(cell.getCellSet()).thenReturn(ret);
		when(ret.getCell(11)).thenReturn(cell);
		when(ret.getCell(c4Position, r3Position)).thenReturn(cell);
		
		return ret;
		
	}

}
