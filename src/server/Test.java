package server;

import java.util.Scanner;

import org.hibernate.Session;

import model.HibernateSessionFactory;
import model.User;

public class Test {
	public static void main(String args[]){
//	  Scanner s = new Scanner(System.in);
//	  
//	  System.out.println(s.next());
		
		Session session = HibernateSessionFactory.getSession();
		User user = new User();
		user.setUserId("Fuck");
		user.setUserPassword("FUck");
		user.setUserName("FUCk");
		session.save(user);
		HibernateSessionFactory.commitSession(session);
		
	}

}
