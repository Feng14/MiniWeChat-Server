package server;

import java.io.IOException;
import java.util.Calendar;

import model.Chatting;
import com.google.protobuf.InvalidProtocolBufferException;
import protocol.ProtoHead;
import protocol.Data.ChatData.ChatItem;
import protocol.Msg.ReceiveChatMsg.ReceiveChatSync;
import protocol.Msg.SendChatMsg.SendChatRsp;
import protocol.Msg.SendChatMsg;
import exception.NoIpException;
import tools.Debug;

/**
 * 主服务器下的子服务器，负责处理发消息相关事件
 * 
 * @author Feng
 */
public class Server_Chatting {
	private ServerModel serverModel;
	private ServerModel_Chatting serverModel_Chatting;
	private ServerNetwork serverNetwork;

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}

	public ServerModel_Chatting getServerModel_Chatting() {
		return serverModel_Chatting;
	}

	public void setServerModel_Chatting(ServerModel_Chatting serverModel_Chatting) {
		this.serverModel_Chatting = serverModel_Chatting;
	}

	public ServerNetwork getServerNetwork() {
		return serverNetwork;
	}

	public void setServerNetwork(ServerNetwork serverNetwork) {
		this.serverNetwork = serverNetwork;
	}

	/**
	 * 对 用户发送微信消息 的事件进行处理 对方在线就立刻发送，不在则存如内存
	 * 
	 * @param networkPacket
	 * @throws NoIpException
	 * @author Feng
	 */
	public void clientSendChatting(NetworkPacket networkPacket) throws NoIpException {

		Debug.log(new String[] { "Server_Chatting", "clientSendChatting" }, " User sendChatting Event :Deal with user's "
				+ ServerModel.getIoSessionKey(networkPacket.ioSession) + "  send chatting event");

		// 构造回复对象
		SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();
		sendChattingResponse.setResultCode(SendChatRsp.ResultCode.FAIL);

		try {
			SendChatMsg.SendChatReq sendChattingObject = SendChatMsg.SendChatReq.parseFrom(networkPacket
					.getMessageObjectBytes());

			// 构建消息对象
			Chatting chatting = new Chatting(sendChattingObject.getChatData().getSendUserId(), sendChattingObject.getChatData()
					.getReceiveUserId(), sendChattingObject.getChatData().getChatType(), sendChattingObject.getChatData()
					.getChatBody(), Calendar.getInstance().getTimeInMillis());

			// 设置日期
			chatting.setTime(Calendar.getInstance().getTimeInMillis());

			// 若是接收者在线，则发送，否则加入队列
			ClientUser clientUser = serverModel.getClientUserByUserId(chatting.getReceiverUserId());
			if (clientUser != null) {
				Debug.log(new String[] { "Server_Chatting", "clientSendChatting" }, "Receiver online,send to receier("
						+ ServerModel.getIoSessionKey(clientUser.ioSession) + ") now!");

				// 发送给接收者
				serverModel_Chatting.sendChatting(networkPacket.ioSession, chatting);
			} else {
				// 不在线，保存
				Debug.log(new String[] { "Server_Chatting", "clientSendChatting" }, "Receiver offline,save to memory!");
				serverModel_Chatting.addChatting(chatting);
			}

			// 回复客户端说发送成功(保存在服务器成功)
			sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
			serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
					.getMessageID(), ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, sendChattingResponse.build().toByteArray())));
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发消息自动回复（调试测试用）
	 * 
	 * @param networkPacket
	 * @param chatting
	 * @author Feng
	 * @throws IOException
	 */
	private void sendChattingAutoResponse(NetworkPacket networkPacket, Chatting chatting) throws IOException {
		// 回复发送者：发送成功
		SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();
		sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
		serverNetwork.sendToClient(networkPacket.ioSession, new PacketFromServer(networkPacket.getMessageID(), ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, sendChattingResponse.build()
						.toByteArray()));

		// 自动回复
		ReceiveChatSync.Builder receiverChatObj = ReceiveChatSync.newBuilder();
		ChatItem.Builder chatItem = chatting.createChatItem();
		chatItem.setSendUserId(chatting.getReceiverUserId());
		chatItem.setReceiveUserId(chatting.getSenderUserId());
		receiverChatObj.addChatData(chatItem);

		// 发送
		serverNetwork.sendToClient(networkPacket.ioSession, new PacketFromServer(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE, receiverChatObj
				.build().toByteArray()));
		
//		byte[] messageWillSend = networkPacket.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_SYNC_VALUE, receiverChatObj
//				.build().toByteArray());
		// 添加回复监听
//		serverModel_Chatting.addListenReceiveChatting(networkPacket.ioSession, chatting, messageWillSend);
	}

//	private void sendChatSuperCommand(networkPacket networkPacket, Chatting chatting) throws IOException {
//		// 回复发送者：发送成功
//		SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();
//		sendChattingResponse.setResultCode(SendChatMsg.SendChatRsp.ResultCode.SUCCESS);
//		serverNetwork.sendMessageToClient(networkPacket.ioSession, networkPacket.packMessage(
//				ProtoHead.ENetworkMessage.SEND_CHAT_RSP_VALUE, networkPacket.getMessageID(), sendChattingResponse.build()
//						.toByteArray()));
//	}

	/**
	 * 客户端已接收到服务其发送的“未接收消息”， 删除对客户端回复的等待
	 * 
	 * @param networkPacket
	 */
//	public void clientReceiveChatting(networkPacket networkPacket) {
//		byte[] key = networkPacket.getMessageID(networkPacket.arrayBytes);
//		serverModel.removeClientResponseListener(key);
//	}
}
