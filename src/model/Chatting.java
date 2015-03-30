package model;

import java.util.Date;

import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.ChatType;

/**
 * 聊天对象
 * @author Feng
 *
 */
public class Chatting {
	private ChatType chattingType;
	private String senderUserId, receiverUserId, message;
	
	public Chatting(String senderUserId, String receiverUserId, ChatType chattingType, String message) {
		setSenderUserId(senderUserId);
		setReceiverUserId(receiverUserId);
		setChattingType(chattingType);
		setMessage(message);
	}

	public String getSenderUserId() {
		return senderUserId;
	}

	public void setSenderUserId(String senderUserId) {
		this.senderUserId = senderUserId;
	}

	public String getReceiverUserId() {
		return receiverUserId;
	}

	public void setReceiverUserId(String receiverUserId) {
		this.receiverUserId = receiverUserId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ChatType getChattingType() {
		return chattingType;
	}

	public void setChattingType(ChatType chattingType) {
		this.chattingType = chattingType;
	}
	
	public ChatItem.Builder createChatItem(){
		return createChatItem(this);
	}
	
	public static ChatItem.Builder createChatItem(Chatting chatting){
		ChatItem.Builder chatItem = ChatItem.newBuilder();
		chatItem.setSendUserId(chatting.getSenderUserId());
		chatItem.setReceiveUserId(chatting.getReceiverUserId());
		chatItem.setChatType(chatting.getChattingType());
		chatItem.setChatBody(chatting.getMessage());
		chatItem.setDate(new Date().getTime());
		
		return chatItem;
	}
}
