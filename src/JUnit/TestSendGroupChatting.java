package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.ChatType;
import protocol.Data.ChatData.ChatItem.TargetType;
import protocol.Msg.LoginMsg.LoginReq;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.ReceiveChatMsg;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatReq;
import protocol.Msg.SendChatMsg.SendChatRsp;

import server.NetworkPacket;
import test.Client111;
import tools.DataTypeTranslater;

/**
 * 测试群发消息
 * 
 * @author Administrator
 * 
 */

//@FixMethodOrder(MethodSorters.DEFAULT)
public class TestSendGroupChatting {

	private SendChatRsp.ResultCode sendChatting(ClientSocket clientSocket, ChatItem.Builder chatItem) throws IOException {
		// 构建消息对象
		SendChatReq.Builder sendChattingBuilder = SendChatReq.newBuilder();
		sendChattingBuilder.setChatData(chatItem);
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE, sendChattingBuilder
				.build().toByteArray()));

		byte[] byteArray;
		for (int i = 0; i < 10; i++) {
			byteArray = clientSocket.readFromServerWithoutKeepAlive();
			if (NetworkPacket.getMessageType(byteArray) != ProtoHead.ENetworkMessage.SEND_CHAT_RSP)
				continue;

			return SendChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray)).getResultCode();
		}
		return null;
	}

	@Test
	public void test1() throws UnknownHostException, IOException {
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();
		ClientSocket clientSocket3;
		byte[] response;
		String user1 = "a", user2 = "b", user3 = "c", message = "a fuck b and c", groupId = "13";

		// a，b 登陆
		assertEquals(clientSocket1.login(user1, user1), LoginRsp.ResultCode.SUCCESS);
		assertEquals(clientSocket2.login(user2, user2), LoginRsp.ResultCode.SUCCESS);
		System.out.println(user1 + " , " + user2 + " Login Over!");

		// 构建消息对象
		ChatItem.Builder chatItem = ChatItem.newBuilder();
		chatItem.setChatBody(message);
		chatItem.setSendUserId(user1);
		chatItem.setReceiveUserId(groupId);
		chatItem.setChatType(ChatType.TEXT);
		chatItem.setTargetType(TargetType.GROUP);

		// user1发送
		assertEquals(sendChatting(clientSocket1, chatItem), SendChatRsp.ResultCode.SUCCESS);
		System.out.println(user1 + " Send Chatting Over!");

		// user2接收
		byte[] byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		assertNotNull(byteArray);
		ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		ChatItem chatItem2 = receiveChatting.getChatData(0);
		assertEquals(chatItem2.getChatBody(), message);
		System.out.println(user2 + " Get Chatting");
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis(chatItem2.getDate()-99999);
		System.out.println(ClientSocket.getChatItemInfo(chatItem2));

		// user3接收
		clientSocket3 = new ClientSocket();
		// user3登陆
		assertEquals(clientSocket3.login(user3, user3), LoginRsp.ResultCode.SUCCESS);

		byteArray = clientSocket3.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		assertNotNull(byteArray);
		receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(receiveChatting.getChatData(0).getChatBody(), message);
		System.out.println(user3 + " Get Chatting");

	}

}
