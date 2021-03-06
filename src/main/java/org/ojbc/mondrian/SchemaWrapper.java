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
import java.util.Comparator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Schema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * A wrapper around olap4j Schema objects, suitable for serialization via json.
 *
 */
@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class SchemaWrapper implements Serializable {
	
	private static final long serialVersionUID = 6324155925760637995L;

	private String name;
	private String connectionName;
	private List<CubeWrapper> cubes;
	
	SchemaWrapper() { }
	
	public SchemaWrapper(Schema schema, String connectionName, Document xmlSchema) throws OlapException {
		this.name = schema.getName();
		this.connectionName = connectionName;
		cubes = new ArrayList<>();
		for (Cube cube : schema.getCubes()) {
			cubes.add(new CubeWrapper(cube, xmlSchema));
		}
		List<String> schemaCubeOrder = new ArrayList<>();
		XPath xp = XPathFactory.newInstance().newXPath();
		try {
			XPathExpression xpe = xp.compile("//Cube");
			NodeList cubeNodeList = (NodeList) xpe.evaluate(xmlSchema, XPathConstants.NODESET);
			for (int i=0;i < cubeNodeList.getLength();i++) {
				schemaCubeOrder.add(((Element) cubeNodeList.item(i)).getAttribute("name"));
			}
		} catch (XPathException e) {
			throw new RuntimeException(e);
		}
		if (cubes.size() == schemaCubeOrder.size()) {
			cubes.sort(new Comparator<CubeWrapper>() {
				@Override
				public int compare(CubeWrapper o1, CubeWrapper o2) {
					int pos1 = schemaCubeOrder.indexOf(o1.getName());
					int pos2 = schemaCubeOrder.indexOf(o2.getName());
					int ret = 0;
					if (pos1 > pos2) {
						ret = 1;
					} else {
						ret = -1;
					}
					return ret;
				}
			});
		} else {
			log.warn("Cannot sort cubes as olap4j has different number of cubes than xml schema for connection " + connectionName);
		}
	}

	public List<CubeWrapper> getCubes() {
		return Collections.unmodifiableList(cubes);
	}

}
