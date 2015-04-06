package server;

import java.io.IOException;
import java.util.Calendar;

import model.Chatting;
import com.google.protobuf.InvalidProtocolBufferException;
import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg;
import exception.NoIpException;
import tools.Debug;

/**
 * 主服务器下的子服务器，负责处理发消息相关事件
 * 
 * @author Feng
 */
public class Server_Chatting {
	public static Server_Chatting instance = new Server_Chatting();

	private Server_Chatting() {

	}

	/**
	 * 对 用户发送微信消息 的事件进行处理 对方在线就立刻发送，不在则存如内存
	 * 
	 * @param networkMessage
	 * @throws NoIpException
	 * @author Feng
	 */
	public void clientSendChatting(NetworkMessage networkMessage) throws NoIpException {

		Debug.log(new String[] { "Server_Chatting", "clientSendChatting" }, " User sendChatting Event ：Deal with sser's "
				+ ServerModel.getIoSessionKey(networkMessage.ioSession) + "  send chatting event");

		try {
			SendChatMsg.SendChatReq sendChattingObject = SendChatMsg.SendChatReq
					.parseFrom(networkMessage.getMessageObjectBytes());

			SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();

			// 构建消息对象
			Chatting chatting = new Chatting(sendChattingObject.getChatData().getSendUserId(), sendChattingObject.getChatData()
					.getReceiveUserId(), sendChattingObject.getChatData().getChatType(), sendChattingObject.getChatData()
					.getChatBody(), Calendar.getInstance().getTimeInMillis());
			
			// 设置日期
			chatting.setTime(Calendar.getInstance().getTimeInMillis());

			// 若接收者是 自动回复账号，则单独处理
			if (sendChattingObject.getChatData().getReceiveUserId().equals("AutoChat")) {
				sendChattingAutoResponse(networkMessage, chatting);
				return;
			}

			// 如果是特殊指令，则做特殊处理
			if (sendChattingObject.getChatData().getChatBody().startsWith("/")) {

			}

			// 若是接收者在线，则发送，否则加入队列
			ClientUser clientUser = ServerModel.instance.getClientUserByUserId(chatting.getReceiverUserId());
			if (clientUser != null) {
				Debug.log(new String[] { "Server_Chatting", "clientSendChatting" }, "Receiver online，send to receier("
						+ ServerModel.getIoSessionKey(clientUser.ioSession) + ") now!");
				ChatItem.Builder chatItem = chatting.createChatItem();

				Debug.log(chatItem.getSendUserId() + " " + chatItem.getReceiveUserId() + " " + chatItem.getChatType() + " "
						+ chatItem.getChatBody());

				ReceiveChatSync.Builder receiverChatObj = ReceiveChatSync.newBuilder();
				receiverChatObj.addChatData(chatItem);

				byte[] messageWillSend = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE,
						receiverChatObj.build().toByteArray());
				// 添加回复监听
				ServerModel_Chatting.instance.addListenReceiveChatting(clientUser.ioSession, chatting, messageWillSend);

				// 发送
				ServerNetwork.instance.sendMessageToClient(clientUser.ioSession, messageWillSend);
			} else {
				Debug.log(new String[] { "Server_Chatting", "clientSendChatting" }, "Receiver offline，save to memory!");
				ServerModel_Chatting.instance.addChatting(chatting);
			}

			// 回复客户端说发送成功(保存在服务器成功)
			sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, networkMessage.getMessageID(), sendChattingResponse.build()
							.toByteArray()));

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发消息自动回复（调试测试用）
	 * 
	 * @param networkMessage
	 * @param chatting
	 * @author Feng
	 * @throws IOException
	 */
	private void sendChattingAutoResponse(NetworkMessage networkMessage, Chatting chatting) throws IOException {
		// 回复发送者：发送成功
		SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();
		sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
		ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
				ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, networkMessage.getMessageID(), sendChattingResponse.build()
						.toByteArray()));

		// 自动回复
		ReceiveChatSync.Builder receiverChatObj = ReceiveChatSync.newBuilder();
		ChatItem.Builder chatItem = chatting.createChatItem();
		chatItem.setSendUserId(chatting.getReceiverUserId());
		chatItem.setReceiveUserId(chatting.getSenderUserId());
		receiverChatObj.addChatData(chatItem);

		byte[] messageWillSend = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE, receiverChatObj
				.build().toByteArray());
		// 添加回复监听
		ServerModel_Chatting.instance.addListenReceiveChatting(networkMessage.ioSession, chatting, messageWillSend);

		// 发送
		ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, messageWillSend);
	}

	private void sendChatSuperCommand(NetworkMessage networkMessage, Chatting chatting) throws IOException {
		// 回复发送者：发送成功
		SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();
		sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
		ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
				ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, networkMessage.getMessageID(), sendChattingResponse.build()
						.toByteArray()));
	}

	/**
	 * 客户端已接收到服务其发送的“未接收消息”， 删除对客户端回复的等待
	 * 
	 * @param networkMessage
	 */
	public void clientReceiveChatting(NetworkMessage networkMessage) {
		byte[] key = NetworkMessage.getMessageID(networkMessage.arrayBytes);
		ServerModel.instance.removeClientResponseListener(key);
	}
}
