package model;

import org.hibernate.Session;
import org.hibernate.Transaction;


public class test {

	public static void main(String args[]){
		User user1 = new User();
    	user1.setAccount("account1");
    	user1.setAccountName("name1");
    	user1.setAccountPassword("121");
    	
    	Session session = HibernateSessionFactory.getSession();
        Transaction trans = session.beginTransaction();
        session.save(user1);
        trans.commit();     
        
        session.close();
        HibernateSessionFactory.sessionFactory.close();
	}
}
