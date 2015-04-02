package model;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.ChatType;

/**
 * 聊天消息对象
 * @author Feng
 *
 */
@Entity
@Table(name="chatting_message")
public class Chatting {
	//消息id
	private long id;
	//聊天类型  文字和图片
	private ChatType chattingType;
	//是否群聊 true群聊 false个人聊天
	private boolean isGroup;
	//群聊天对应的群Id
	private int groupId;
	//时间
	private long time;
	private String senderUserId, receiverUserId, message;
	
	public Chatting(String senderUserId, String receiverUserId, ChatType chattingType, String message,long time,
			boolean isGroup,int groupId){
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
	
	
	@Id
	@Column(name="chatting_id",columnDefinition = "int(8)  COMMENT '聊天消息Id'")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@Column(name="chatting_type",columnDefinition = "int(1)  COMMENT '聊天类型'")
	public ChatType getChattingType() {
		return chattingType;
	}
	public void setChattingType(ChatType chattingType) {
		this.chattingType = chattingType;
	}


	@Column(name="is_group",columnDefinition = "int(1)  COMMENT '是否群聊'")
	public boolean getIsGroup() {
		return isGroup;
	}
	public void setIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	
	@ManyToOne(targetEntity=Group.class, fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	@JoinColumn(name="group_id",columnDefinition = "int(8)  COMMENT '聊天群Id'")
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}


	@Column(name="time",columnDefinition = "int(20)  COMMENT '时间'")
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}


	@ManyToOne(targetEntity=User.class, fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	@JoinColumn(name="sender_user_id",columnDefinition = "int(8)  COMMENT '发送者Id'")
	public String getSenderUserId() {
		return senderUserId;
	}
	public void setSenderUserId(String senderUserId) {
		this.senderUserId = senderUserId;
	}


	@ManyToOne(targetEntity=User.class, fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	@JoinColumn(name="receiver_user_id",columnDefinition = "int(8)  COMMENT '接受者Id'")
	public String getReceiverUserId() {
		return receiverUserId;
	}
	public void setReceiverUserId(String receiverUserId) {
		this.receiverUserId = receiverUserId;
	}

	@Column(name="message",columnDefinition = "char(100)  COMMENT '消息'")
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
