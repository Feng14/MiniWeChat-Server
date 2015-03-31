package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Ignore;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.ChatType;
import protocol.Msg.LoginMsg.LoginReq;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.ReceiveChatMsg;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatReq;
import protocol.Msg.SendChatMsg.SendChatRsp;
import server.NetworkMessage;
import tools.Debug;

/**
 * 测试发消息功能
 * 
 * @author Feng
 */
public class TestSendChatting {

	/**
	 * 测试双方均在线的场景
	 * 
	 * @author Feng
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	@Test
	public void test1() throws UnknownHostException, IOException {
		ClientSocket clientSocket1 = new ClientSocket();
		ClientSocket clientSocket2 = new ClientSocket();
		byte[] response;
		String userId1 = "c", userId2 = "d", message = "c fuck d";

		// 登陆
		if (clientSocket1.login(userId1, "cc") != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		if (clientSocket2.login(userId2, "dd") != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// 发消息
		sendChatting(clientSocket1, userId1, userId2, ChatType.TEXT, message);
		// 接收回复
		while (true) {
			response = clientSocket1.readFromServerWithoutKeepAlive();
			if (NetworkMessage.getMessageType(response) != ProtoHead.ENetworkMessage.SEND_CHAT_RSP)
				continue;

			SendChatRsp sendChattingResponse = SendChatRsp.parseFrom(NetworkMessage.getMessageObjectBytes(response));
			Debug.log("a 向 b 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
			assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
			clientSocket1.close();
			break;
		}

		// 用户2接收消息
		while (true) {
			response = clientSocket2.readFromServerWithoutKeepAlive();
			if (NetworkMessage.getMessageType(response) != ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC)
				continue;

			ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkMessage.getMessageObjectBytes(response));
			ChatItem chatItem = receiveChatting.getChatData(0);
			Debug.log(chatItem.getReceiveUserId() + " 收到 " + chatItem.getSendUserId() + " 发来的消息：" + chatItem.getChatType() + "  "
					+ chatItem.getChatBody());
			assertEquals(chatItem.getChatBody(), message);

			// 回复
			clientSocket2.writeToServer(NetworkMessage.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE,
					NetworkMessage.getMessageID(response), new byte[0]));
			clientSocket2.close();
			break;
		}

	}

	
	/**
	 * 测试只有发送者在线的场景
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@Test
	public void test2() throws UnknownHostException, IOException{
		ClientSocket clientSocket1 = new ClientSocket();
		byte[] response;
		String userId1 = "a", userId2 = "b", message = "a fuck b";

		// 发送者登陆
		if (clientSocket1.login(userId1, "aa") != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");

		// 发消息
		sendChatting(clientSocket1, userId1, userId2, ChatType.TEXT, message);
		// 接收回复
		while (true) {
			response = clientSocket1.readFromServerWithoutKeepAlive();
			if (NetworkMessage.getMessageType(response) != ProtoHead.ENetworkMessage.SEND_CHAT_RSP)
				continue;

			SendChatRsp sendChattingResponse = SendChatRsp.parseFrom(NetworkMessage.getMessageObjectBytes(response));
			Debug.log("a 向 b 发送消息的服务器回复：" + sendChattingResponse.getResultCode().toString());
			assertEquals(sendChattingResponse.getResultCode().getNumber(), SendChatRsp.ResultCode.SUCCESS_VALUE);
			clientSocket1.close();
			break;
		}

		// 接收者登陆
		ClientSocket clientSocket2 = new ClientSocket();
		if (clientSocket2.login(userId2, "bb") != LoginRsp.ResultCode.SUCCESS)
			fail("登陆结果错误！");
		
		while (true) {
			response = clientSocket2.readFromServerWithoutKeepAlive();
			if (NetworkMessage.getMessageType(response) != ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC)
				continue;

			ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkMessage.getMessageObjectBytes(response));
			ChatItem chatItem = receiveChatting.getChatData(0);
			Debug.log(chatItem.getReceiveUserId() + " 收到 " + chatItem.getSendUserId() + " 发来的消息：" + chatItem.getChatType() + "  "
					+ chatItem.getChatBody());
			assertEquals(chatItem.getChatBody(), message);

			// 回复
			clientSocket2.writeToServer(NetworkMessage.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE,
					NetworkMessage.getMessageID(response), new byte[0]));
			clientSocket2.close();
			break;
		}
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
		chatItem.setSendUserId(senderUserId);
		chatItem.setReceiveUserId(receiveUserId);
		chatItem.setChatType(type);
		chatItem.setChatBody(messageBody);

		SendChatReq.Builder sendChatBuilder = SendChatReq.newBuilder();
		sendChatBuilder.setChatData(chatItem);

		clientSocket.writeToServer(NetworkMessage.packMessage(ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE, sendChatBuilder
				.build().toByteArray()));
	}

}
