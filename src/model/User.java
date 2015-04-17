package model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import protocol.Data.UserData.UserItem;


@Entity
@Table(name="user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User implements Serializable {
//	public static final String TABLE_NAME = User.class.getSimpleName();
	public static final String TABLE_NAME = "user";
	public static final String TABLE_USER_ID = "user_id";
	public static final String HQL_USER_ID = "userId";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private String userPassword;
    private int headIndex;
    private List<User> friends;
    private List<Group> groups;
    
    public User(){
        
    }
    
    public User(String userId,String userName,String userPassword){
        this.userId=userId;
        this.userName=userName;
        this.userPassword=userPassword;
    }

    @Id
    @Column(name="user_id",columnDefinition = "char(20)  COMMENT '微信号'")
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name="user_name",columnDefinition = "char(20)  COMMENT '昵称'")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Column(name="user_password",columnDefinition = "char(20)  COMMENT '密码'")
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	
	
	@Column(name="user_headIndex",columnDefinition = "int(4) COMMENT '头像编号'")
	public int getHeadIndex() {
		return headIndex;
	}

	public void setHeadIndex(int headIndex) {
		this.headIndex = headIndex;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
	@ManyToMany(targetEntity=User.class)
	@JoinTable(name = "user_friends", 
	joinColumns = @JoinColumn(name = "user_id"), 	
	inverseJoinColumns = @JoinColumn(name = "friend_id"))
	public List<User> getFriends() {
		return friends;
	}
	public void setFriends(List<User> friends) {
		this.friends = friends;
	}
	
	@ManyToMany(targetEntity = Group.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "group_members", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    public List<Group> getGroups() {
		return groups;
	}
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
	
	

	public String toString(){
    	return this.getUserId()+" "+this.getUserName();
    }
    
    public static UserItem.Builder createUserItemBuilder(User user) {
    	UserItem.Builder builder = UserItem.newBuilder();
    	builder.setHeadIndex(user.getHeadIndex());
    	builder.setUserId(user.getUserId());
    	builder.setUserName(user.getUserName());
    	return builder;
    }
}
