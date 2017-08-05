package org.ojbc.mondrian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Member.Type;

public class TidyCellSet {
	
	private static final String VALUE_LABEL = "CellValue";
	
	private final Log log = LogFactory.getLog(TidyCellSet.class);
	
	private List<Map<String, Object>> rows = new ArrayList<>();
	
	public void init(CellSet cellSet) {
		
		rows = new ArrayList<>();
		
		List<CellSetAxis> axes = cellSet.getAxes();
		List<List<Position>> positionLists = new ArrayList<>();
		for (CellSetAxis axis : axes) {
			positionLists.add(axis.getPositions());
		}
		positionLists = MondrianUtils.permuteLists(positionLists);
		
		for (List<Position> positionList : positionLists) {
			
			Cell cell = cellSet.getCell(positionList.toArray(new Position[0]));
			Map<String, Object> map = new HashMap<>();
			
			for (Position position : positionList) {
				List<Member> members = position.getMembers();
				for (Member member : members) {
					map.putAll(getHierarchyMap(member));
				}
			}
			
			map.put(VALUE_LABEL, cell.getValue());
			rows.add(map);
			
		}
		
	}
	
	private Map<String, String> getHierarchyMap(Member member) {
		Map<String, String> ret = new HashMap<>();
		Member m = member;
		while (m != null) {
			if (m.getMemberType() != Type.ALL) {
				ret.put(m.getLevel().getUniqueName(), m.getName());
			}
			m = m.getParentMember();
		}
		return ret;
	}
	
	public List<Map<String, Object>> getRows() {
		return Collections.unmodifiableList(rows);
	}

}
