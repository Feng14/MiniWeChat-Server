package server;

import java.awt.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.hibernate.Session;
import model.HibernateSessionFactory;
import model.ResultCode;
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
		ResultCode resultCode = ResultCode.NULL;
		System.out.println(resultCode.getCode());
		a(resultCode);
		System.out.println(resultCode.getCode());
		b(resultCode);
		System.out.println(resultCode.getCode());
		
	}
	public static void a(ResultCode resultCode) {
		resultCode = ResultCode.SUCCESS;
	}
	public static void b(ResultCode resultCode) {
		resultCode.setCode(ResultCode.SUCCESS);
	}

}
