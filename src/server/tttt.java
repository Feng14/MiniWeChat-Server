package server;

import org.hibernate.Session;

import model.Chatting;
import model.HibernateSessionFactory;
import protocol.Data.ChatData.ChatItem.ChatType;

public class tttt {
	public static void main(String args[]){
		Session session = HibernateSessionFactory.getSession();
		session = HibernateSessionFactory.getSession();
		Chatting a = new Chatting("a3", "a4", ChatType.TEXT,"abcde"); 
		a.setId(1);
		session.save(a);
	}

}
