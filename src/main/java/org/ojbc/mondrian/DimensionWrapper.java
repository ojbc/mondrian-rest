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

import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A wrapper around olap4j Dimension objects, suitable for serialization via json.
 *
 */
@Getter
@EqualsAndHashCode
@ToString
public class DimensionWrapper implements Serializable {
	
	private static final long serialVersionUID = 4499740199331630404L;
	
	private String name;
	private String caption;
	private String type;
	private List<HierarchyWrapper> hierarchies;
	
	DimensionWrapper() {
	}

	public DimensionWrapper(Dimension d) throws OlapException {
		this.name = d.getName();
		this.caption = d.getCaption();
		this.type = d.getDimensionType().getDescription();
		hierarchies = new ArrayList<>();
		for(Hierarchy h : d.getHierarchies()) {
			hierarchies.add(new HierarchyWrapper(h));
		}
	}

	public List<HierarchyWrapper> getHierarchies() {
		return Collections.unmodifiableList(hierarchies);
	}

}
