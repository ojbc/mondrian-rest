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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.w3c.dom.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A wrapper around olap4j Cube objects, suitable for serialization via json.
 *
 */
@Getter
@EqualsAndHashCode
@ToString
public class CubeWrapper implements Serializable {
	
	private static final long serialVersionUID = 8561468085905307671L;
	
	private String name;
	private String caption;
	private List<MeasureWrapper> measures;
	private List<DimensionWrapper> dimensions;
	
	CubeWrapper() { }

	public CubeWrapper(Cube cube, Document xmlSchema) throws OlapException {
		
		this.name = cube.getName();
		this.caption = cube.getCaption();
		measures = cube.getMeasures().stream().map(measure -> new MeasureWrapper(measure)).collect(Collectors.toList());
		
		dimensions = new ArrayList<>();
		for (Dimension d : cube.getDimensions()) {
			dimensions.add(new DimensionWrapper(d));
		}
		
	}

	public List<MeasureWrapper> getMeasures() {
		return Collections.unmodifiableList(measures);
	}

	public List<DimensionWrapper> getDimensions() {
		return Collections.unmodifiableList(dimensions);
	}

}
