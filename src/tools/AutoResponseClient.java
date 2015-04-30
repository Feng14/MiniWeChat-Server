package tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.TargetType;
import protocol.Msg.LoginMsg.LoginRsp;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatReq;
import server.NetworkPacket;

import JUnit.ClientSocket;

/**
 * 一个自动回复其他用户发来消息的插件
 * 
 * @author Feng
 * 
 */
public class AutoResponseClient {
	private ClientSocket clientSocket;
	private Logger logger = Logger.getLogger(this.getClass());

	public AutoResponseClient() throws UnknownHostException, IOException {
		String user = "AutoResponse";
		clientSocket = new ClientSocket();
		clientSocket.host = "127.0.0.1";
//		clientSocket.link();
		if (clientSocket.login(user, user) != LoginRsp.ResultCode.SUCCESS) {
			// Debug.log("AutoResponseClient", "自动回复器登陆失败！");
			logger.error("AutoResponseClient : AutoResponse Login Error!");
//			System.err.println("AutoResponseClient : 自动回复器登陆失败！");
			return;
		}
		logger.info("AutoResponse Login Successful! Start working");

		byte[] arrayBytes;
		while (true) {
			arrayBytes = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC);
//			if (NetworkPacket.getMessageType(arrayBytes) != ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC)
//				continue;

			// 将同样的消息发回给发送者
			ReceiveChatSync receiveChatting = ReceiveChatSync.parseFrom(NetworkPacket.getMessageObjectBytes(arrayBytes));
			ChatItem receiveChatItem = receiveChatting.getChatData(0);
			logger.info("AutoResponse get a Chatting : " + ClientSocket.getChatItemInfo(receiveChatItem));

			ChatItem.Builder sendChatItem = ChatItem.newBuilder();
			if (receiveChatItem.getTargetType() == TargetType.INDIVIDUAL){
				sendChatItem.setSendUserId(receiveChatItem.getReceiveUserId());
				sendChatItem.setReceiveUserId(receiveChatItem.getSendUserId());
			} else if (receiveChatItem.getTargetType() == TargetType.GROUP){
				sendChatItem.setSendUserId(user);
			} 
			sendChatItem.setChatBody(receiveChatItem.getChatBody());
			sendChatItem.setChatType(receiveChatItem.getChatType());

			SendChatReq.Builder sendChattingObj = SendChatReq.newBuilder();
			sendChattingObj.setChatData(sendChatItem);

			clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.SEND_CHAT_REQ_VALUE, sendChattingObj
					.build().toByteArray()));
			logger.info("AutoResponse response a Chatting");
		}
	}

}
