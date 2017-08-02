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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olap4j.Position;
import org.olap4j.metadata.Member;

public class PositionWrapper {
	
	private List<String> memberDimensionNames;
	private List<String> memberDimensionCaptions;
	private List<String> memberDimensionValues;
	
	PositionWrapper() {
		
	}
	
	public PositionWrapper(Position position) {
		
		memberDimensionNames = new ArrayList<>();
		memberDimensionCaptions = new ArrayList<>();
		memberDimensionValues = new ArrayList<>();
		
		for (Member member : position.getMembers()) {
			memberDimensionNames.add(member.getDimension().getName());
			memberDimensionCaptions.add(member.getDimension().getCaption());
			memberDimensionValues.add(member.getName());
		}
		
	}
	
	public List<String> getMemberDimensionNames() {
		return Collections.unmodifiableList(memberDimensionNames);
	}

	public List<String> getMemberDimensionCaptions() {
		return Collections.unmodifiableList(memberDimensionCaptions);
	}

	public List<String> getMemberDimensionValues() {
		return Collections.unmodifiableList(memberDimensionValues);
	}

	void setMemberDimensionNames(List<String> memberDimensionNames) {
		this.memberDimensionNames = memberDimensionNames;
	}

	void setMemberDimensionCaptions(List<String> memberDimensionCaptions) {
		this.memberDimensionCaptions = memberDimensionCaptions;
	}

	void setMemberDimensionValues(List<String> memberDimensionValues) {
		this.memberDimensionValues = memberDimensionValues;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((memberDimensionCaptions == null) ? 0 : memberDimensionCaptions.hashCode());
		result = prime * result + ((memberDimensionNames == null) ? 0 : memberDimensionNames.hashCode());
		result = prime * result + ((memberDimensionValues == null) ? 0 : memberDimensionValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof PositionWrapper && obj.hashCode()==hashCode();
	}

}
