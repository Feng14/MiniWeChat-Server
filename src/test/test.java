package test;

import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import protocol.Data.ChatData.ChatItem.ChatType;

import server.ServerModel;
import server.ServerModel_Chatting;

import model.Chatting;

public class test {

	public static void main(String args[]) {
//		Hashtable<String, LinkedBlockingQueue<Chatting>> chattingHashtable = ServerModel_Chatting.instance.chattingHashtable;
//		ServerModel_Chatting.instance.addChatting(new Chatting("a", "b", ChatType.TEXT, "Fuck"));
//		ServerModel_Chatting.instance.addChatting(new Chatting("c", "d", ChatType.TEXT, "Fuck"));
		ServerModel_Chatting.instance.a();
	}
}
