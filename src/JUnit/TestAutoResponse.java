package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg;
import protocol.Msg.SendChatMsg.SendChatReq;
import server.NetworkPacket;
import test.Client111;

/**
 * 测试自动回复器
 * 
 * @author Feng
 * 
 */
public class TestAutoResponse {

	@Test
	public void test() throws UnknownHostException, IOException {
		String user1 = "a", autoResponseUser = "AutoResponse", message = "Test AutoResponse!!!";
		ClientSocket clientSocket1 = new ClientSocket();

		// 登陆
		assertEquals(clientSocket1.login(user1, user1), LoginRsp.ResultCode.SUCCESS);

		// 构造消息对象
		ChatItem.Builder sendChatItem = ChatItem.newBuilder();
		sendChatItem.setSendUserId(user1);
		sendChatItem.setReceiveUserId(autoResponseUser);
		sendChatItem.setChatBody(message);
		sendChatItem.setChatType(ChatItem.ChatType.TEXT);

		SendChatReq.Builder sendChattingObj = SendChatReq.newBuilder();
		sendChattingObj.setChatData(sendChatItem);

		// 发送
		clientSocket1.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE, sendChattingObj
				.build().toByteArray()));
		
		byte[] byteArray = clientSocket1.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
		assertNotNull(byteArray);
		ChatItem receiveChatItem = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray)).getChatData(0);
		assertEquals(receiveChatItem.getChatBody(), message);
		
		clientSocket1.close();
	}

}
