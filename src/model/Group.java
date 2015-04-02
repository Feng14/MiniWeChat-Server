package model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * 聊天群
 * @author wangfei
 * @time 2015-04-02
 */
@Entity
@Table(name="group")
public class Group {
	private int groupId;
	private String groupName;
	private List<User> memberList;
	
    @Id
	@Column(name="group_id",columnDefinition = "int(8)  COMMENT '聊天群Id'")
    @GeneratedValue(strategy = GenerationType.AUTO)
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	@Column(name="group_name",columnDefinition = "char(20)  COMMENT '聊天群昵称'")
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	
	 @ManyToMany(targetEntity = User.class,
			    cascade = {CascadeType.PERSIST, CascadeType.MERGE})
			    @JoinTable(name = "group_member",
			    joinColumns = @JoinColumn(name = "group_id"),
			    inverseJoinColumns =
			    @JoinColumn(name = "user_id")
			    )
	public List<User> getMemberList() {
		return memberList;
	}
	public void setMemberList(List<User> memberList) {
		this.memberList = memberList;
	}
	

}
