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
	    	user1.setUserId("user1");
	    	user1.setUserName("name1");
	    	user1.setUserPassword("121");
	    	
	    	User user2 = new User();
	    	user2.setUserId("user2");
	    	user2.setUserName("name2");
	    	user2.setUserPassword("122");
	    	
	    	User user3 = new User();
	    	user3.setUserId("user3");
	    	user3.setUserName("username3");
	    	user3.setUserPassword("123");
	    	
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
	    	User u = (User)session.load(User.class, "user2");
	    	System.out.println("userInfo:"+u.getUserId()+" "+u.getUserName()+" "+u.getUserPassword());
			List<User> friends = u.getFriends();
			System.out.print("    friends:");
			for(int i=0;i<friends.size();i++){
				System.out.println(friends.get(i).getUserId()+" ");
			}
			session.close();
	    }
	    
	   //HQL查询
	    private static void query(){
		   Session session = HibernateSessionFactory.getSession();
	       Query query = session.createQuery("select u.user,u.userName,u.userPassword from User  u");
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
		   criteria.add(Restrictions.eq("user", "user2"));
		   
		   List<User> list = criteria.list();
		   for(User u:list){
			   System.out.println("userInfo:"+u.getUserId()+" "+u.getUserName()+" "+u.getUserPassword());
			   List<User> friends = u.getFriends();
			   System.out.print("    friends:");
			   for(int i=0;i<friends.size();i++){
				   System.out.println(friends.get(i).getUserId()+" ");
			   }
		   }
		   session.close();
	   }
	   
	   private static void update(){
		   Session session = HibernateSessionFactory.getSession();

	       User u = new User();
	       u.setUserId("user1");
	       u.setUserName("newname1");
	       u.setUserPassword("newpassword1");

	       Transaction trans = session.beginTransaction();
	       session.update(u);
	       trans.commit();

	       session.close();
	   }
	   
	   private static void delete(){
		   Session session = HibernateSessionFactory.getSession();

		   Transaction trans = session.beginTransaction();

	       User u = new User();
	       u.setUserId("user2");
	       session.delete(u);
	       trans.commit();

	       session.close();
	   }
}
