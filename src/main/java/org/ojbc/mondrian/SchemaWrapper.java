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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Schema;

/**
 * A wrapper around olap4j Schema objects, suitable for serialization via json.
 *
 */
public class SchemaWrapper {
	
	private String name;
	private List<CubeWrapper> cubes;
	
	SchemaWrapper() {
	}
	
	public SchemaWrapper(Schema schema) throws OlapException {
		this.name = schema.getName();
		cubes = new ArrayList<>();
		for (Cube cube : schema.getCubes()) {
			cubes.add(new CubeWrapper(cube));
		}
	}

	public String getName() {
		return name;
	}

	public List<CubeWrapper> getCubes() {
		return Collections.unmodifiableList(cubes);
	}

	void setName(String name) {
		this.name = name;
	}

	void setCubes(List<CubeWrapper> cubes) {
		this.cubes = cubes;
	}

}
