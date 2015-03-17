package model;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;



public class test {

	 public static void main(String[] args) {
	        add();
	        load();
	    	query();
	        query2();
	        update();
	        delete();  	
	        
	        HibernateSessionFactory.sessionFactory.close();
	    }
	    
	    private static void add(){
	    	User user1 = new User();
	    	user1.setAccount("account1");
	    	user1.setAccountName("name1");
	    	user1.setAccountPassword("121");
	    	
	    	User user2 = new User();
	    	user2.setAccount("account2");
	    	user2.setAccountName("name2");
	    	user2.setAccountPassword("122");
	    	
	    	User user3 = new User();
	    	user3.setAccount("account3");
	    	user3.setAccountName("accountname3");
	    	user3.setAccountPassword("123");
	    	List<User> friends = new ArrayList<User>();
	    	friends.add(user3);
	    	user2.setFriends(friends);
	    	       
	        Session session = HibernateSessionFactory.getSession();
	        Transaction trans = session.beginTransaction();
//	        session.persist(user1);
//	        session.persist(user2);
	        session.save(user1);
	        session.save(user3);
	        session.save(user2);
	        trans.commit();     
	    
	        session.close();
	    }
	    
	    private static void load(){
	    	Session session = HibernateSessionFactory.getSession();
	    	User u = (User)session.load(User.class, "account2");
	    	System.out.println("userInfo:"+u.getAccount()+" "+u.getAccountName()+" "+u.getAccountPassword());
			List<User> friends = u.getFriends();
			System.out.print("    friends:");
			for(int i=0;i<friends.size();i++){
				System.out.println(friends.get(i).getAccount()+" ");
			}
			session.close();
	    }
	    
	   //HQL查询
	    private static void query(){
		   Session session = HibernateSessionFactory.getSession();
	       Query query = session.createQuery("select u.account,u.accountName,u.accountPassword from User  u");
	       List list = query.list();
	       for(int i=0;i<list.size();i++){
	    	   Object []o = (Object[])list.get(i);  //转型为数组
	    	   System.out.println((String)o[0]+" "+(String)o[1]+" "+(String)o[2]);
	       }
	       
	       session.close();
	    }
	   
	   //criteria查询
	   private static void query2(){
		   Session session = HibernateSessionFactory.getSession();
		   Criteria criteria = session.createCriteria(User.class);
		   criteria.add(Restrictions.eq("account", "account2"));
		   
		   List<User> list = criteria.list();
		   for(User u:list){
			   System.out.println("userInfo:"+u.getAccount()+" "+u.getAccountName()+" "+u.getAccountPassword());
			   List<User> friends = u.getFriends();
			   System.out.print("    friends:");
			   for(int i=0;i<friends.size();i++){
				   System.out.println(friends.get(i).getAccount()+" ");
			   }
		   }
		   session.close();
	   }
	   
	   private static void update(){
		   Session session = HibernateSessionFactory.getSession();

	       User u = new User();
	       u.setAccount("account1");
	       u.setAccountName("newname1");
	       u.setAccountPassword("newpassword1");

	       Transaction trans = session.beginTransaction();
	       session.update(u);
	       trans.commit();

	       session.close();
	   }
	   
	   private static void delete(){
		   Session session = HibernateSessionFactory.getSession();

		   Transaction trans = session.beginTransaction();

	       User u = new User();
	       u.setAccount("account2");//用下面那句效果一样，只是多了句select
	       session.delete(u);
	       trans.commit();

	       session.close();
	   }
}
