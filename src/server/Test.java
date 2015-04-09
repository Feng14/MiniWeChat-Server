package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.hibernate.Session;
import model.HibernateSessionFactory;
import model.User;

public class Test {
//	public static Test test = new Test();
	public int fuck = 123;
	
//	public static Test getTest() {
//		return test;
//	}
//
//	public static void setTest(Test test) {
//		Test.test = test;
//	}

	public static void main(String args[]){
//	  Scanner s = new Scanner(System.in);
//	  
//	  System.out.println(s.next());
		
//		Session session = HibernateSessionFactory.getSession();
//		User user = new User();
//		user.setUserId("Fuck");
//		user.setUserPassword("FUck");
//		user.setUserName("FUCk");
//		session.save(user);
//		HibernateSessionFactory.commitSession(session);
		
		byte[] bb = new byte[]{10, 20, 30};
		byte[] cc = new byte[]{40, 50, 60};
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(bb);
			outputStream.write(cc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (byte b : outputStream.toByteArray())
			System.out.print(b + "  ");
		
	}

}
