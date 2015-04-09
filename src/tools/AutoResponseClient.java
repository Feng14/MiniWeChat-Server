package tools;

import java.io.IOException;
import java.net.UnknownHostException;

import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.LoginMsg.LoginRsp.ResultCode;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatReq;
import server.PacketFromClient;

import JUnit.ClientSocket;

/**
 * 一个自动回复其他用户发来消息的插件
 * 
 * @author Feng
 * 
 */
public class AutoResponseClient {
	private ClientSocket clientSocket;

	public AutoResponseClient() throws UnknownHostException, IOException {
		clientSocket = new ClientSocket();
		clientSocket.link();
		if (clientSocket.login("AutoResponse", "AutoResponse") != LoginRsp.ResultCode.SUCCESS) {
			// Debug.log("AutoResponseClient", "自动回复器登陆失败！");
			System.err.println("AutoResponseClient : 自动回复器登陆失败！");
			return;
		}

		byte[] arrayBytes;
		while (true) {
			arrayBytes = clientSocket.readFromServerWithoutKeepAlive();
			if (PacketFromClient.getMessageType(arrayBytes) != ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC)
				continue;

			// 回复服务器说接收成功
			clientSocket.writeToServer(PacketFromClient.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE,
					PacketFromClient.getMessageID(arrayBytes), new byte[0]));

			// 将同样的消息发回给发送者
			ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(PacketFromClient.getMessageObjectBytes(arrayBytes));
			ChatItem receiveChatItem = receiveChatting.getChatData(0);

			ChatItem.Builder sendChatItem = ChatItem.newBuilder();
			sendChatItem.setSendUserId(receiveChatItem.getReceiveUserId());
			sendChatItem.setReceiveUserId(receiveChatItem.getSendUserId());
			sendChatItem.setChatBody(receiveChatItem.getChatBody());
			sendChatItem.setChatType(receiveChatItem.getChatType());

			SendChatReq.Builder sendChattingObj = SendChatReq.newBuilder();
			sendChattingObj.setChatData(sendChatItem);

			clientSocket.writeToServer(PacketFromClient.packMessage(ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE, sendChattingObj
					.build().toByteArray()));
		}
	}

}
