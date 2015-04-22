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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 聊天群
 * 
 * @author wangfei
 * @time 2015-04-02
 */
@Entity
@Table(name = "user_group")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Group {
	public static final String GROUP_ID = "groupId";
	
	private int groupId;
	private String createrId;
	private String groupName;
	private List<User> memberList;

	public Group() {
	}

	public Group(String groupName) {
		setGroupName(groupName);
	}

	@Id
	@Column(name = "group_id", columnDefinition = "int(8)  COMMENT '聊天群Id'")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	@Column(name = "creater_id", columnDefinition = "char(20)  COMMENT '微信号'")
	public String getCreaterId() {
		return createrId;
	}

	public void setCreaterId(String createrId) {
		this.createrId = createrId;
	}

	@Column(name = "group_name", columnDefinition = "char(20)  COMMENT '聊天群昵称'")
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@ManyToMany(targetEntity = User.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	public List<User> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<User> memberList) {
		this.memberList = memberList;
	}

	public String toString() {
		return "GroupId : " + this.groupId
				+ "; GroupName : " + this.groupName
				+ "; CreaterId : " + this.getCreaterId()
				+ "; MemberList : " + this.getMemberList().toString();
	}

}
