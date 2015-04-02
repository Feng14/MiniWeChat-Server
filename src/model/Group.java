package model;

import java.util.List;

/**
 * 聊天群
 * @author wangfei
 * @time 2015-04-02
 */
public class Group {
	private int groupId;
	private String groupName;
	private List<User> memberList;
	
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public List<User> getMemberList() {
		return memberList;
	}
	public void setMemberList(List<User> memberList) {
		this.memberList = memberList;
	}
	

}
