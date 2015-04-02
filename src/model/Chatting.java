package model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.Builder;
import protocol.Data.ChatData.ChatItem.ChatType;

/**
 * 聊天消息对象
 * @author Feng
 *
 */

@Entity
@Table(name="chatting")
public class Chatting {
<<<<<<< HEAD
	//消息id
	private long id;
	//聊天类型  文字和图片
=======
	private long id;
>>>>>>> 【新功能】聊天记录存入硬盘初步
	private ChatType chattingType;
	//是否群聊 true群聊 false个人聊天
	private boolean isGroup;
	//群聊天对应的群Id
	private int groupId;
	//时间
	private long time;
	private String senderUserId, receiverUserId, message;
	
<<<<<<< HEAD
	public Chatting(String senderUserId, String receiverUserId, ChatType chattingType, String message,long time,
			boolean isGroup,int groupId){
=======
	public Chatting(long id, String senderUserId, String receiverUserId, ChatType chattingType, String message) {
		setId(id);
		setSenderUserId(senderUserId);
		setReceiverUserId(receiverUserId);
		setChattingType(chattingType);
		setMessage(message);
	}
	
	public Chatting(String senderUserId, String receiverUserId, ChatType chattingType, String message) {
		setId(id);
>>>>>>> 【新功能】聊天记录存入硬盘初步
		setSenderUserId(senderUserId);
		setReceiverUserId(receiverUserId);
		setChattingType(chattingType);
		setMessage(message);
		setTime(time);
		setIsGroup(isGroup);
		setGroupId(groupId);
	}
	
	public Chatting(String senderUserId, String receiverUserId, ChatType chattingType, String message){
		setSenderUserId(senderUserId);
		setReceiverUserId(receiverUserId);
		setChattingType(chattingType);
		setMessage(message);
		
	}
	
	

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public ChatType getChattingType() {
		return chattingType;
	}
	public void setChattingType(ChatType chattingType) {
		this.chattingType = chattingType;
	}


	public boolean getIsGroup() {
		return isGroup;
	}
	public void setIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid",strategy = "uuid")
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}


	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}


	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
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
	
	public  ChatItem.Builder createChatItem(){
		return  createChatItem(this);
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
