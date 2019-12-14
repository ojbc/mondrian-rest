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
 * Copyright 2018 Open Justice Broker Consortium and Cascadia Analytics LLC
 */
package org.ojbc.mondrian;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Schema;

public class SchemaWrapperTest {
	
	@Test
	public void test() throws Exception {
		Schema schema = TestSchemaFactory.getInstance().getSchema();
		SchemaWrapper schemaWrapper = new SchemaWrapper(schema, "foo", null);
		assertEquals(schema.getName(), schemaWrapper.getName());
		assertEquals("foo", schemaWrapper.getConnectionName());
		List<Cube> cubes = schema.getCubes();
		List<CubeWrapper> cubeWrappers = schemaWrapper.getCubes();
		assertEquals(cubes.size(), cubeWrappers.size());
		for (int i=0;i < cubes.size();i++) {
			assertEquals(cubes.get(i).getName(), cubeWrappers.get(i).getName());
			assertEquals(cubes.get(i).getCaption(), cubeWrappers.get(i).getCaption());
		}
	}

}
