package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

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

/**
 * 测试群发消息
 * 
 * @author Administrator
 * 
 */
public class TestSendGroupChatting {

	private SendChatRsp.ResultCode sendChatting(ClientSocket clientSocket, ChatItem.Builder chatItem) throws IOException {
		// 构建消息对象
		SendChatReq.Builder sendChattingBuilder = SendChatReq.newBuilder();
		sendChattingBuilder.setChatData(chatItem);
		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE, sendChattingBuilder
				.build().toByteArray()));
		
		byte[] byteArray;
		for (int i=0; i<10; i++){
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
		String userId1 = "a", userId2 = "b", user3 = "c", message = "a fuck b and c", groupId = "1";

		// a，b 登陆
		assertEquals(clientSocket1.login(userId1, userId1), LoginRsp.ResultCode.SUCCESS);
		assertEquals(clientSocket2.login(userId2, userId2), LoginRsp.ResultCode.SUCCESS);

		// 构建消息对象
		ChatItem.Builder chatItem = ChatItem.newBuilder();
		chatItem.setChatBody(message);
		chatItem.setSendUserId(userId1);
		chatItem.setReceiveUserId(groupId);
		chatItem.setChatType(ChatType.TEXT);
		chatItem.setTargetType(TargetType.GROUP);

		// userId1发送
		assertEquals(sendChatting(clientSocket1, chatItem), SendChatRsp.ResultCode.SUCCESS);
		
		// userId2接收
		byte[] byteArray = clientSocket2.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		assertNotNull(byteArray);
		ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(receiveChatting.getChatData(0).getChatBody(), message);
		
		// user3接收
		clientSocket3 = new ClientSocket();
		byteArray = clientSocket3.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		assertNotNull(byteArray);
		receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		assertEquals(receiveChatting.getChatData(0).getChatBody(), message);
		
	}

}
