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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class TestMondrianUtils {
	
	private final Log log = LogFactory.getLog(TestMondrianUtils.class);
	
	@BeforeEach
	public void setUp() throws Exception {
		log.debug("setUp");
	}
	
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
