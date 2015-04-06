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

	public static void main(String args[]) {
//		Hashtable<String, LinkedBlockingQueue<Chatting>> chattingHashtable = ServerModel_Chatting.instance.chattingHashtable;
//		ServerModel_Chatting.instance.addChatting(new Chatting("a", "b", ChatType.TEXT, "Fuck"));
//		ServerModel_Chatting.instance.addChatting(new Chatting("c", "d", ChatType.TEXT, "Fuck"));
//		ServerModel_Chatting.instance.a();
		
		Date date = new Date();
		System.out.println(1900 + date.getYear());
		System.out.println(date.getMonth() + 1);
		System.out.println(date.getDay());
		System.out.println(date.getHours());
		System.out.println(date.getMinutes());
		System.out.println(date.getSeconds());
		System.out.println((new Date()).toString());
		
		Calendar calendar = Calendar.getInstance();
		System.out.println(calendar.get(Calendar.YEAR));
		System.out.println(calendar.get(Calendar.MONTH));
		System.out.println(calendar.get(Calendar.DAY_OF_MONTH));
		System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
		System.out.println(calendar.get(Calendar.MINUTE));
		System.out.println(calendar.get(Calendar.SECOND));
		System.out.println((new Date()).getTime());
		System.out.println(calendar.getTimeInMillis());
	}
}
