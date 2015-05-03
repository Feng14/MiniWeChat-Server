package test;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import protocol.Data.ChatData.ChatItem.ChatType;

import server.ServerModel;
import server.ServerModel_Chatting;

import model.Chatting;

public class test {
	
	public test(){
//		System.out.println(this.getClass().toString());
		Logger logger1 = Logger.getLogger(this.getClass());
		System.out.println(logger1.getName());
		System.out.println(this.getClass().getName().toString());
		System.out.println(logger1 == Logger.getLogger(this.getClass().getName().toString()));
	}

	public static void main(String args[]) {
//		Hashtable<String, LinkedBlockingQueue<Chatting>> chattingHashtable = ServerModel_Chatting.instance.chattingHashtable;
//		ServerModel_Chatting.instance.addChatting(new Chatting("a", "b", ChatType.TEXT, "Fuck"));
//		ServerModel_Chatting.instance.addChatting(new Chatting("c", "d", ChatType.TEXT, "Fuck"));
//		ServerModel_Chatting.instance.a();
		
		new test();
	}
}
