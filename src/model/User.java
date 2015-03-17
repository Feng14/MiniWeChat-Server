package model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
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

    private String account;
    private String accountName;
    private String accountPassword;
    
    public User(){
        
    }
    
    public User(String account,String accountName,String accountPassword){
        this.account=account;
        this.accountName=accountName;
        this.accountPassword=accountPassword;
    }

    @Id
    @Column(name="account")
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}

	@Column(name="account_name")
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	@Column(name="account_password")
	public String getAccountPassword() {
		return accountPassword;
	}
	public void setAccountPassword(String accountPassword) {
		this.accountPassword = accountPassword;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
   
    
}