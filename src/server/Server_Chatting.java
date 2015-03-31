package server;

import java.io.IOException;
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

	public void clientSendChatting(NetworkMessage networkMessage) throws NoIpException {

		Debug.log(new String[] { "Server_Chatting", "clientSendChatting" },
				" 用户发消息事件 ： 用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的发消息事件  的处理");

		try {
			SendChatMsg.SendChatReq sendChattingObject = SendChatMsg.SendChatReq
					.parseFrom(networkMessage.getMessageObjectBytes());
			SendChatMsg.SendChatRsp.Builder sendChattingResponse = SendChatMsg.SendChatRsp.newBuilder();

			// 构建消息对象
			Chatting chatting = new Chatting(sendChattingObject.getChatData().getSendUserId(), sendChattingObject.getChatData()
					.getReceiveUserId(), sendChattingObject.getChatData().getChatType(), sendChattingObject.getChatData()
					.getChatBody());

			// 若是接收者在线，则发送，否则加入队列
			ClientUser clientUser = ServerModel.instance.getClientUserByUserId(chatting.getSenderUserId());
			if (clientUser != null) {
				ChatItem.Builder chatItem = chatting.createChatItem();

				ReceiveChatSync.Builder receiverChatObj = ReceiveChatSync.newBuilder();
				receiverChatObj.addChatData(chatItem);

				byte[] messageWillSend = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.RECEIVE_CHAT_RSP_VALUE, receiverChatObj
						.build().toByteArray());
				// 添加回复监听
				ServerModel_Message.instance.addListenReceiveChatting(clientUser.ioSession, chatting, messageWillSend);
				
				// 发送
				ServerNetwork.instance.sendMessageToClient(clientUser.ioSession, messageWillSend);
			} else {
				ServerModel_Message.instance.addChatting(chatting);
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
}
