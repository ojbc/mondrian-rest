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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Measure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A wrapper around olap4j Cube objects, suitable for serialization via json.
 *
 */
public class CubeWrapper {
	
	private String name;
	private String caption;
	private List<MeasureWrapper> measures;
	private List<DimensionWrapper> dimensions;
	
	CubeWrapper() {
	}

	public CubeWrapper(Cube cube, Document xmlSchema) throws OlapException {
		this.name = cube.getName();
		this.caption = cube.getCaption();
		measures = new ArrayList<>();
		for (Measure measure : cube.getMeasures()) {
			measures.add(new MeasureWrapper(measure));
		}
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

	public String getName() {
		return name;
	}

	public String getCaption() {
		return caption;
	}

	void setName(String name) {
		this.name = name;
	}

	void setCaption(String caption) {
		this.caption = caption;
	}

	void setMeasures(List<MeasureWrapper> measures) {
		this.measures = measures;
	}

	void setDimensions(List<DimensionWrapper> dimensions) {
		this.dimensions = dimensions;
	}

}
