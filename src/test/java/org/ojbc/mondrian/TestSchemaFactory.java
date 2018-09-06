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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.olap4j.OlapException;
import org.olap4j.impl.ArrayNamedListImpl;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Schema;

public class TestSchemaFactory {
	
	private static final TestSchemaFactory INSTANCE = new TestSchemaFactory();
	
	private TestSchemaFactory() {
	}
	
	public static final TestSchemaFactory getInstance() {
		return INSTANCE;
	}
	
	public Schema getSchema() throws OlapException {
		
		Schema ret = mock(Schema.class);
		when(ret.getName()).thenReturn("schema1");
		
		Cube cube = mock(Cube.class);
		when(cube.getName()).thenReturn("cube1");
		when(cube.getCaption()).thenReturn("Cube 1");
		
		List<Cube> cubes = new ArrayList<>();
		cubes.add(cube);
		
		when(ret.getCubes()).thenReturn(new ArrayNamedListImpl<Cube>(cubes) {
			private static final long serialVersionUID = 1L;
			@Override
			public String getName(Object element) {
				return ((Cube) element).getName();
			}
		});
		
		// we do a fair amount of testing of this structure in the controller test
		// if we find we need to do more, we can mock more objects
		
		when(cube.getMeasures()).thenReturn(new ArrayList<>());
		List<Dimension> dimensions = new ArrayList<>();
		when(cube.getDimensions()).thenReturn(new ArrayNamedListImpl<Dimension>(dimensions) {
			private static final long serialVersionUID = 1L;
			@Override
			public String getName(Object element) {
				return "";
			}
		});
		
		return ret;
		
	}

}
