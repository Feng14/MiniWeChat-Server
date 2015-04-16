package server;

import java.awt.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
		String a = "fuck";
		String b = "fuck";
		ArrayList<String> list = new ArrayList<String>();
		list.add(a);
		System.out.println(list.contains(b));
		
	}

}
