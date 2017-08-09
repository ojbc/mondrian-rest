package org.ojbc.mondrian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	private static final String HASH_KEY = ".H";
	private static final String ORDER_KEY = ".O";
	
	private final Log log = LogFactory.getLog(TidyCellSet.class);
	
	private List<Map<String, Object>> values = new ArrayList<>();
	
	public void init(CellSet cellSet) {
		List<Map<String, Object>> positionIntersectionList = buildPositionIntersectionList(cellSet);
		values = reducePositionIntersectionList(positionIntersectionList);
	}

	private List<Map<String, Object>> reducePositionIntersectionList(List<Map<String, Object>> positionIntersectionList) {
		
		Map<Object, Map<String, Object>> reducedMap = new HashMap<>();
		
		for (int i=0;i < positionIntersectionList.size();i++) {
			Map<String, Object> map = positionIntersectionList.get(i);
			Object hashKey = map.get(HASH_KEY);
			if (reducedMap.containsKey(hashKey)) {
				String measureName = (String) map.get("[Measures].[MeasuresLevel]");
				reducedMap.get(hashKey).put(measureName, map.get(measureName));
			} else {
				map.remove("[Measures].[MeasuresLevel]");
				reducedMap.put(hashKey, map);
			}
		}
		
		List<Map<String, Object>> ret = new ArrayList<>();
		ret.addAll(reducedMap.values());
		ret.sort(new Comparator<Map<String, Object>>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				return ((Comparable<Integer>) o1.get(ORDER_KEY)).compareTo((Integer) o2.get(ORDER_KEY));
			}
		});
		
		return ret;
		
	}

	private List<Map<String, Object>> buildPositionIntersectionList(CellSet cellSet) {
		
		List<Map<String, Object>> ret = new ArrayList<>();
		
		List<CellSetAxis> axes = cellSet.getAxes();
		List<List<Position>> positionLists = new ArrayList<>();
		for (CellSetAxis axis : axes) {
			positionLists.add(axis.getPositions());
		}
		
		positionLists = MondrianUtils.permuteLists(positionLists);
		
		int valueIndex = 0;
		for (List<Position> positionList : positionLists) {
			
			Cell cell = cellSet.getCell(positionList.toArray(new Position[0]));
			Map<String, Object> map = new HashMap<>();
			
			for (Position position : positionList) {
				List<Member> members = position.getMembers();
				for (Member member : members) {
					map.putAll(getHierarchyMap(member));
				}
			}
			
			String measureValue = (String) map.get("[Measures].[MeasuresLevel]");
			map.remove("[Measures].[MeasuresLevel]");
			map.put(HASH_KEY, map.hashCode());
			map.put("[Measures].[MeasuresLevel]", measureValue);

			map.put(measureValue, cell.getValue());
			map.put(ORDER_KEY, valueIndex++);
			ret.add(map);
			
		}
		
		return ret;
		
	}
	
	private Map<String, String> getHierarchyMap(Member member) {
		Map<String, String> ret = new HashMap<>();
		Member m = member;
		while (m != null) {
			if (m.getMemberType() != Type.ALL) {
				String uniqueName = m.getLevel().getUniqueName();
				String value = m.getName();
				ret.put(uniqueName, value);
			}
			m = m.getParentMember();
		}
		return ret;
	}
	
	public List<Map<String, Object>> getValues() {
		return Collections.unmodifiableList(values);
	}

}
