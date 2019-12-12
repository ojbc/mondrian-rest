package org.ojbc.mondrian;

import org.olap4j.metadata.Member;

public class PositionMemberWrapper {
	
	private String memberLevelName;
	private String memberLevelCaption;
	private String memberValue;
	private PositionMemberWrapper parentMember;
	
	PositionMemberWrapper() {}
	
	PositionMemberWrapper(Member member) {
		this.memberLevelName = member.getLevel().getUniqueName();
		this.memberLevelCaption = member.getLevel().getCaption();
		this.memberValue = member.getName();
		Member parentMember = member.getParentMember();
		if (parentMember != null) {
			this.parentMember = new PositionMemberWrapper(parentMember);
		}
	}

	public String getMemberLevelName() {
		return memberLevelName;
	}

	public String getMemberLevelCaption() {
		return memberLevelCaption;
	}

	public String getMemberValue() {
		return memberValue;
	}

	public PositionMemberWrapper getParentMember() {
		return parentMember;
	}

	public void setMemberLevelName(String memberLevelName) {
		this.memberLevelName = memberLevelName;
	}

	public void setMemberLevelCaption(String memberLevelCaption) {
		this.memberLevelCaption = memberLevelCaption;
	}

	public void setMemberValue(String memberValue) {
		this.memberValue = memberValue;
	}

	public void setParentMember(PositionMemberWrapper parentMember) {
		this.parentMember = parentMember;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((memberLevelName == null) ? 0 : memberLevelName.hashCode());
		result = prime * result + ((memberLevelCaption == null) ? 0 : memberLevelCaption.hashCode());
		result = prime * result + ((memberValue == null) ? 0 : memberValue.hashCode());
		result = prime * result + ((parentMember == null) ? 0 : parentMember.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj instanceof PositionMemberWrapper && obj.hashCode()==hashCode();
	}

}
