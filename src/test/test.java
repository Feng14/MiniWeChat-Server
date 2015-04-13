package test;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.org.apache.bcel.internal.generic.NEW;

import protocol.Data.ChatData.ChatItem.ChatType;

import server.ServerModel;
import server.ServerModel_Chatting;

import model.Chatting;

public class test {
	
	public test(){
//		System.out.println(this.getClass().toString());
		
		long t = Calendar.getInstance().getTimeInMillis();
		Date date = new Date(t);
		System.out.println(date.toString());
	}

	public static void main(String args[]) {
//		Hashtable<String, LinkedBlockingQueue<Chatting>> chattingHashtable = ServerModel_Chatting.instance.chattingHashtable;
//		ServerModel_Chatting.instance.addChatting(new Chatting("a", "b", ChatType.TEXT, "Fuck"));
//		ServerModel_Chatting.instance.addChatting(new Chatting("c", "d", ChatType.TEXT, "Fuck"));
//		ServerModel_Chatting.instance.a();
		
		new test();
	}
}
