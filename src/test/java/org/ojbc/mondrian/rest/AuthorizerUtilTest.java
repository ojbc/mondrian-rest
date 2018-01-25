package org.ojbc.mondrian.rest;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class AuthorizerUtilTest {
	
	@Test
	public void testJsonParsing() throws Exception {
		Map<String, Map<String, String>> map = RequestAuthorizer.AuthorizerUtil.convertRoleConnectionJsonToMaps("test-authorizer-util.json");
		assertEquals(2, map.size());
		assertTrue(map.containsKey("outerKey1"));
		Map<String, String> innerMap = map.get("outerKey1");
		assertTrue(innerMap.containsKey("innerKey1.1"));
	}

}
