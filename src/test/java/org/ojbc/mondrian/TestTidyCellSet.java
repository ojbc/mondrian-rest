package org.ojbc.mondrian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.olap4j.CellSet;

public class TestTidyCellSet {
	
	private final Log log = LogFactory.getLog(TestTidyCellSet.class);
	
	private TidyCellSet tidyCellSet;
	
	@Before
	public void setUp() {
		log.debug("setUp");
		tidyCellSet = new TidyCellSet();
	}
	
	@Test
	public void testSingleAxisSingleDimension() {
		CellSet cellSet = TestCellSetFactory.getInstance().getSingleAxisSingleDimensionCellSet();
		tidyCellSet.init(cellSet);
		List<Map<String, Object>> rows = tidyCellSet.getValues();
		assertEquals(1, rows.size());
		assertEquals(1.0, rows.get(0).get("M1"));
	}
	
	@Test
	public void testDualAxisSingleDimensionCellSet() {
		CellSet cellSet = TestCellSetFactory.getInstance().getDualAxisSingleDimensionCellSet();
		tidyCellSet.init(cellSet);
		List<Map<String, Object>> rows = tidyCellSet.getValues();
		assertEquals(2, rows.size());
		assertEquals(1.0, rows.get(0).get("M1"));
		assertEquals("D1_V1", rows.get(0).get("[D1].[D1].[D1_V1]"));
		assertEquals(2.0, rows.get(1).get("M1"));
		assertEquals("D1_V2", rows.get(1).get("[D1].[D1].[D1_V2]"));
		tidyCellSet.init(cellSet, true, null);
		rows = tidyCellSet.getValues();
		assertEquals(2, rows.size());
		assertEquals(1.0, rows.get(0).get("M1"));
		assertEquals("D1_V1", rows.get(0).get("D1_V1"));
		assertEquals(2.0, rows.get(1).get("M1"));
		assertEquals("D1_V2", rows.get(1).get("D1_V2"));
	}
	
	@Test
	public void testDualAxisTwoDimensions() {
		CellSet cellSet = TestCellSetFactory.getInstance().getDualAxisTwoDimensionCellSet();
		tidyCellSet.init(cellSet);
		List<Map<String, Object>> rows = tidyCellSet.getValues();
		assertEquals(6, rows.size());
		assertEquals(1.0, rows.get(0).get("M1"));
		assertEquals("D1_V1", rows.get(0).get("[D1].[D1].[D1_V1]"));
		assertEquals("D2_V1", rows.get(0).get("[D2].[D2].[D2_V1]"));
		assertEquals(2.0, rows.get(1).get("M1"));
		assertEquals("D1_V1", rows.get(1).get("[D1].[D1].[D1_V1]"));
		assertEquals("D2_V2", rows.get(1).get("[D2].[D2].[D2_V2]"));
		assertEquals(10.0, rows.get(3).get("M1"));
		assertEquals("D1_V2", rows.get(3).get("[D1].[D1].[D1_V2]"));
		assertEquals("D2_V1", rows.get(3).get("[D2].[D2].[D2_V1]"));
		assertNull(rows.get(5).get("M1"));
		assertEquals("D1_V2", rows.get(5).get("[D1].[D1].[D1_V2]"));
		assertEquals("D2_V3", rows.get(5).get("[D2].[D2].[D2_V3]"));
	}
	
	@Test
	public void testDualAxisTwoDimensionsTwoMeasures() {
		CellSet cellSet = TestCellSetFactory.getInstance().getDualAxisTwoDimensionTwoMeasuresCellSet();
		tidyCellSet.init(cellSet);
		List<Map<String, Object>> rows = tidyCellSet.getValues();
		assertEquals(6, rows.size());
		assertEquals(1.0, rows.get(0).get("M1"));
		assertEquals(4.0, rows.get(0).get("M2"));
		assertEquals(2.0, rows.get(1).get("M1"));
		assertEquals(5.0, rows.get(1).get("M2"));
		assertEquals("D1_V1", rows.get(0).get("[D1].[D1].[D1_V1]"));
		assertEquals("D2_V1", rows.get(0).get("[D2].[D2].[D2_V1]"));
		assertEquals("D1_V1", rows.get(1).get("[D1].[D1].[D1_V1]"));
		assertEquals("D2_V2", rows.get(1).get("[D2].[D2].[D2_V2]"));
		tidyCellSet.init(cellSet, true, null);
		rows = tidyCellSet.getValues();
		assertEquals(6, rows.size());
		assertEquals(1.0, rows.get(0).get("M1"));
		assertEquals(4.0, rows.get(0).get("M2"));
		assertEquals(2.0, rows.get(1).get("M1"));
		assertEquals(5.0, rows.get(1).get("M2"));
		assertEquals("D1_V1", rows.get(0).get("D1_V1"));
		assertEquals("D2_V1", rows.get(0).get("D2_V1"));
		assertEquals("D1_V1", rows.get(1).get("D1_V1"));
		assertEquals("D2_V2", rows.get(1).get("D2_V2"));
	}
	
	@Test
	public void testLevelNameTranslation() {
		CellSet cellSet = TestCellSetFactory.getInstance().getDualAxisTwoDimensionTwoMeasuresCellSet();
		Map<String, String> levelNameTranslationMap = new HashMap<>();
		levelNameTranslationMap.put("[D1].[D1].[D1_V1]", "foobar");
		tidyCellSet.init(cellSet, true, levelNameTranslationMap);
		List<Map<String, Object>> rows = tidyCellSet.getValues();
		assertEquals(1.0, rows.get(0).get("M1"));
		assertEquals(4.0, rows.get(0).get("M2"));
		assertEquals(2.0, rows.get(1).get("M1"));
		assertEquals(5.0, rows.get(1).get("M2"));
		assertEquals("D1_V1", rows.get(0).get("foobar"));
		assertEquals("D2_V1", rows.get(0).get("D2_V1"));
	}
	
}
