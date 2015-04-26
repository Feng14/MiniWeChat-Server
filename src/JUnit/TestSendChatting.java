package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.ChatType;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LogoutMsg.LogoutRsp;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatReq;
import protocol.Msg.SendChatMsg.SendChatRsp;
import server.NetworkPacket;
import server.ServerModel;
import tools.Debug;

/**
 * 测试发消息功能
 * 
 * @author Feng
 */
public class TestSendChatting {

	/**
	 * 接收一条微信消息
	 * 
	 * @param clientSocket
	 * @return
	 * @throws IOException
	 * @author Feng
	 */
	public static List<ChatItem> receiveChatting(ClientSocket clientSocket) throws IOException {
		byte[] response = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(response));
		return receiveChatting.getChatDataList();
	}

	private void showChatItems(List<ChatItem> chatItems) {
		for (ChatItem c : chatItems) {
			Date date = new Date(c.getDate());
			System.out.println("Sender : " + c.getSendUserId()
					+ "; receiver : " + c.getReceiveUserId()
					+ "; TargetType : " + c.getTargetType()
					+ "; ChatType : " + c.getChatType()
					+ "; ChatBody : " + c.getChatBody()
					+ "; Date : " + (1970 + date.getYear()) + "-"
					+ date.getMonth() + "-" + date.getDay() + "  " + date.getHours() + ":"
					+ date.getMinutes() + ":" + date.getSeconds());
		}
	}

	/**
	 * 测试双方均在线的场景
	 * 
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	// @Test
	public void test1() throws UnknownHostException, IOException {
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();
		byte[] response;
		String userId1 = "c", userId2 = "d", message = "c fuck d";
		// String userId1 = "123", userId2 = "1234", message = "c fuck d";

		// 登陆
		if (clientSocket1.login(userId1, userId1) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		if (clientSocket2.login(userId2, userId2) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		System.out.println("test1 登陆成功");
		// 发消息
		sendChatting(clientSocket1, userId1, userId2, ChatType.TEXT, message);
		// 接收回复
		while (true) {
			response = clientSocket1.readFromServerWithoutKeepAlive();
			if (NetworkPacket.getMessageType(response) != ProtoHead.ENetworkMessage.SEND_CHAT_RSP)
				continue;

			SendChatRsp sendChattingResponse = SendChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			Debug.log("a 向 b 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
			assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
			break;
		}
		clientSocket1.close();

		// 用户2接收消息
		List<ChatItem> chatItems = receiveChatting(clientSocket2);
		// Debug.log(chatItem.getReceiveUserId() + " 收到 " +
		// chatItem.getSendUserId() + " 发来的消息：");
		showChatItems(chatItems);
		assertEquals(chatItems.get(0).getChatBody(), message);

		clientSocket2.close();

	}

	/**
	 * 测试只有发送者在线的场景(不同连接)
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	// @Test
	public void test2() throws UnknownHostException, IOException {
		ClientSocket clientSocket1 = new ClientSocket();
		byte[] response;
		String userId1 = "a", userId2 = "b", message = "a fuck b";

		// 发送者登陆
		if (clientSocket1.login(userId1, userId1) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// 发消息
		sendChatting(clientSocket1, userId1, userId2, ChatType.TEXT, message);
		// 接收发送结果
		while (true) {
			response = clientSocket1.readFromServerWithoutKeepAlive();
			if (NetworkPacket.getMessageType(response) != ProtoHead.ENetworkMessage.SEND_CHAT_RSP)
				continue;

			SendChatRsp sendChattingResponse = SendChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			Debug.log("a 向 b 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
			assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
			break;
		}
		clientSocket1.close();

		// 接收者登陆
		ClientSocket clientSocket2 = new ClientSocket();
		if (clientSocket2.login(userId2, userId2) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		List<ChatItem> chatItems = receiveChatting(clientSocket2);
		showChatItems(chatItems);
		assertEquals(chatItems.get(0).getChatBody(), message);

		clientSocket2.close();
	}

	/**
	 * 测试只有发送者在线的场景,消息对发(相同连接)
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@Test
	public void test2_2() throws UnknownHostException, IOException {
		ClientSocket clientSocket1 = new ClientSocket();
		byte[] response;
		String userId1 = "a", userId2 = "b", message1 = "a fuck b", message2 = "b fuck a";

		// user1发送者登陆
		if (clientSocket1.login(userId1, userId1) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// user1发消息
		sendChatting(clientSocket1, userId1, userId2, ChatType.TEXT, message1);
		// user1接收发送回复
		response = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.SEND_CHAT_RSP);
		SendChatRsp sendChattingResponse = SendChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
		System.out.println("a 向 b 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
		assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
		assertEquals(LogoutRsp.ResultCode.SUCCESS, clientSocket1.logout());

		// user2接收者登陆
		clientSocket1 = new ClientSocket();
		if (clientSocket1.login(userId2, userId2) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// user2接收user1发的消息
		List<ChatItem> chatItems = receiveChatting(clientSocket1);
		showChatItems(chatItems);
		
		assertEquals(chatItems.get(0).getChatBody(), message1);

		// user2回发
		sendChatting(clientSocket1, userId2, userId1, ChatType.TEXT, message2);
		response = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.SEND_CHAT_RSP);
		sendChattingResponse = SendChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
		System.out.println(userId2 + " 向 " + userId1 + " 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
		assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
		assertEquals(LogoutRsp.ResultCode.SUCCESS, clientSocket1.logout());

		// user1发送者登陆
		if (clientSocket1.login(userId1, userId1) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// user2接收user1发的消息
		chatItems = receiveChatting(clientSocket1);
		showChatItems(chatItems);
		assertEquals(chatItems.get(0).getChatBody(), message2);

		clientSocket1.close();
	}

	/**
	 * 测试只有发送者在线的场景(发多条消息)
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	// @Test
	public void test3() throws UnknownHostException, IOException {
		ClientSocket clientSocket1 = new ClientSocket();
		byte[] response;
		String userId1 = "e", userId2 = "f", message = "e fuck f ";
		int times = 5;

		// 发送者登陆
		if (clientSocket1.login(userId1, userId1) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// 发消息
		for (int i = 0; i < times; i++) {
			sendChatting(clientSocket1, userId1, userId2, ChatType.TEXT, message + (i + ""));
			// 接收回复
			while (true) {
				response = clientSocket1.readFromServerWithoutKeepAlive();
				if (NetworkPacket.getMessageType(response) != ProtoHead.ENetworkMessage.SEND_CHAT_RSP)
					continue;

				SendChatRsp sendChattingResponse = SendChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
				Debug.log("e 向 f 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
				assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
				break;
			}
		}
		clientSocket1.close();

		// 接收者登陆
		ClientSocket clientSocket2 = new ClientSocket();
		if (clientSocket2.login(userId2, userId2) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		while (true) {
			response = clientSocket2.readFromServerWithoutKeepAlive();
			if (NetworkPacket.getMessageType(response) != ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC)
				continue;

			ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			List<ChatItem> chatItems = receiveChatting.getChatDataList();
			showChatItems(chatItems);

			assertEquals(chatItems.get(0).getChatBody(), message + (0 + ""));

			// 回复
			clientSocket2.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE,
					NetworkPacket.getMessageID(response), new byte[0]));
			break;
		}
		clientSocket2.close();
	}

	/**
	 * 测试只有发送者在线的场景(发多条消息),接受者上线后接收后不返回（装断线），然后再上线，测试服务器是否保存
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	// @Test
	public void test4() throws UnknownHostException, IOException, InterruptedException {
		ClientSocket clientSocket1 = new ClientSocket();
		byte[] response;
		String userId1 = "g", userId2 = "h", message = "e fuck f ";
		int times = 5;

		// 发送者登陆
		if (clientSocket1.login(userId1, userId1) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// 发消息
		for (int i = 0; i < times; i++) {
			sendChatting(clientSocket1, userId1, userId2, ChatType.TEXT, message + (i + ""));
			// 接收回复
			while (true) {
				response = clientSocket1.readFromServerWithoutKeepAlive();
				if (NetworkPacket.getMessageType(response) != ProtoHead.ENetworkMessage.SEND_CHAT_RSP)
					continue;

				SendChatRsp sendChattingResponse = SendChatRsp.parseFrom(NetworkPacket.getMessageObjectBytes(response));
				System.out.println("g 向 h 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
				assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
				break;
			}
		}
		clientSocket1.close();

		// 接收者登陆
		ClientSocket clientSocket2 = new ClientSocket();
		if (clientSocket2.login(userId2, userId2) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// while (true) {
		// response = clientSocket2.readFromServerWithoutKeepAlive();
		// if (NetworkPacket.getMessageType(response) !=
		// ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC)
		// continue;
		//
		// ReceiveChatSync receiveChatting =
		// ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(response));
		// ChatItem chatItem;
		//
		// for (int i = 0; i < times; i++) {
		// chatItem = receiveChatting.getChatData(i);
		// Debug.log(chatItem.getReceiveUserId() + " 收到 " +
		// chatItem.getSendUserId() + " 发来的消息：" + chatItem.getChatType() + "  "
		// + chatItem.getChatBody());
		// assertEquals(chatItem.getChatBody(), message + (i + ""));
		// }
		//
		// Debug.log("接受者不回复！下线");
		// break;
		// }
		// // 等待服务器把自己踢下线
		// Thread.sleep(ServerModel.KEEP_ALIVE_PACKET_TIME +
		// ServerModel.CHECK_WAIT_CLIENT_RESPONSE_DELTA_TIME);
		// Thread.sleep(9*1000);

		// 重新连接，登录，接收消息
		clientSocket2.close();
		clientSocket2 = new ClientSocket();
		if (clientSocket2.login(userId2, userId2) != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		while (true) {
			response = clientSocket2.readFromServerWithoutKeepAlive();
			if (NetworkPacket.getMessageType(response) != ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC)
				continue;

			ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(response));
			List<ChatItem> chatItems = receiveChatting.getChatDataList();
			showChatItems(chatItems);

			assertEquals(chatItems.get(0).getChatBody(), message + (0 + ""));

			// 回复
			clientSocket2.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE,
					NetworkPacket.getMessageID(response), new byte[0]));
			break;
		}
		clientSocket2.close();

	}

	/**
	 * 发送一条聊天
	 * 
	 * @param clientSocket
	 * @param senderUserId
	 * @param receiveUserId
	 * @param type
	 * @param messageBody
	 * @author Feng
	 * @throws IOException
	 */
	private void sendChatting(ClientSocket clientSocket, String senderUserId, String receiveUserId, ChatItem.ChatType type,
			String messageBody) throws IOException {
		ChatItem.Builder chatItem = ChatItem.newBuilder();
		chatItem.setTargetType(ChatItem.TargetType.INDIVIDUAL);
		chatItem.setSendUserId(senderUserId);
		chatItem.setReceiveUserId(receiveUserId);
		chatItem.setChatType(type);
		chatItem.setChatBody(messageBody);

		SendChatReq.Builder sendChatBuilder = SendChatReq.newBuilder();
		sendChatBuilder.setChatData(chatItem);

		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE, sendChatBuilder
				.build().toByteArray()));
	}

}
