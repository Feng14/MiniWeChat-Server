package model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


@Entity
@Table(name="user")
public class User implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String userId;
    private String userName;
    private String userPassword;
    private List<User> friends;
    
    public User(){
        
    }
    
    public User(String userId,String userName,String userPassword){
        this.userId=userId;
        this.userName=userName;
        this.userPassword=userPassword;
    }

    @Id
    @Column(name="user_id",columnDefinition = "char(20)  COMMENT 'Œ¢–≈∫≈'")
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name="user_name",columnDefinition = "char(20)  COMMENT 'Í«≥∆'")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Column(name="user_password",columnDefinition = "char(20)  COMMENT '√‹¬Î'")
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
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
   
    
}