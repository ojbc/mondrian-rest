package org.ojbc.mondrian;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class TestMondrianUtils {
	
	private final Log log = LogFactory.getLog(TestMondrianUtils.class);
	
	@Test
	public void testPermuteLists() {
		
		List<List<String>> lists = new ArrayList<>();
		lists.add(Arrays.asList(new String[] {"A", "B", "C"}));
		lists.add(Arrays.asList(new String[] {"W", "X", "Y", "Z"}));
		
		List<List<String>> permutations = MondrianUtils.permuteLists(lists);
		
		assertEquals(12, permutations.size());
		assertEquals(2, permutations.get(0).size());
		
		lists.add(Arrays.asList(new String[] {"one", "two"}));

		permutations = MondrianUtils.permuteLists(lists);
		
		assertEquals(24, permutations.size());
		assertEquals(3, permutations.get(0).size());
		
		lists.add(Arrays.asList(new String[] {null}));

		permutations = MondrianUtils.permuteLists(lists);
		
		assertEquals(24, permutations.size());
		List<String> outputElement1 = permutations.get(0);
		assertEquals(4, outputElement1.size());
		assertEquals(Arrays.asList(new String[] {"A", "W", "one", null}), outputElement1);
		assertEquals(Arrays.asList(new String[] {"A", "W", "two", null}), permutations.get(1));
		assertEquals(Arrays.asList(new String[] {"A", "X", "one", null}), permutations.get(2));
		assertEquals(Arrays.asList(new String[] {"B", "W", "one", null}), permutations.get(8));
		
	}

}
