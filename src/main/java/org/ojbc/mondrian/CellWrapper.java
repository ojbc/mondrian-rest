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

import java.util.Collections;
import java.util.List;

import org.olap4j.Cell;
import org.olap4j.OlapException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A wrapper around Mondrian Cell objects, suitable for serialization via json.
 *
 */
@Getter
@EqualsAndHashCode
@ToString
public class CellWrapper {
	
	private String formattedValue;
	private Number value;
	private int ordinal;
	public List<Integer> coordinates;
	public OlapException error;
	
	CellWrapper() { }
	
	public CellWrapper(Cell cell) {
		formattedValue = cell.getFormattedValue();
		Object v = cell.getValue();
		this.value = null;
		this.error = null;
		if (v instanceof Number) {
			value = (Number) v;
		} else if (v instanceof OlapException) {
			error = (OlapException) v;
		}
		ordinal = cell.getOrdinal();
		coordinates = cell.getCoordinateList();
	}

	public List<Integer> getCoordinates() {
		return Collections.unmodifiableList(coordinates);
	}

}
