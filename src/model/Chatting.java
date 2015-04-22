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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import protocol.Data.ChatData.ChatItem;
import protocol.Data.ChatData.ChatItem.ChatType;
import protocol.Data.ChatData.ChatItem.TargetType;

/**
 * 聊天消息对象
 * 
 * @author Feng
 * 
 */
@Entity
@Table(name = "chatting_message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Chatting {
	public static final String TABLE_NAME = "chatting_message";

	// 消息id
	private long id;
	// 聊天类型 文字和图片
	private ChatType chattingType;
	// 是群聊?单聊？系统消息
	private TargetType targetType;
	// 群聊天对应的群Id
	private int groupId;
	// 时间
	private long time;
	private String senderUserId, receiverUserId, message;

	public Chatting() {

	}

	public Chatting(String senderUserId, String receiverUserId, ChatType chattingType, String message, long time, int groupId,
			TargetType targetType) {
		setSenderUserId(senderUserId);
		setReceiverUserId(receiverUserId);
		setChattingType(chattingType);
		setMessage(message);
		setTime(time);
		setGroupId(groupId);
		setTargetType(targetType);
	}

	public Chatting(String senderUserId, String receiverUserId, ChatType chattingType, String message, long time,
			TargetType targetType) {
		setSenderUserId(senderUserId);
		setReceiverUserId(receiverUserId);
		setChattingType(chattingType);
		setMessage(message);
		setTargetType(targetType);
		setTime(time);
	}

	@Id
	@Column(name = "id", columnDefinition = "bigint  COMMENT '聊天消息Id'")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "chatting_type", columnDefinition = "int(8)  COMMENT '聊天类型'")
	public ChatType getChattingType() {
		return chattingType;
	}

	public void setChattingType(ChatType chattingType) {
		this.chattingType = chattingType;
	}

	@Column(name = "group_id", columnDefinition = "int(8)  COMMENT '时间'")
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	@Column(name = "time", columnDefinition = "bigint(20)  COMMENT '时间'")
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Column(name = "sender_user_id", columnDefinition = "char(20)  COMMENT '发送者id'")
	public String getSenderUserId() {
		return senderUserId;
	}

	public void setSenderUserId(String senderUserId) {
		this.senderUserId = senderUserId;
	}

	@Column(name = "receiver_user_id", columnDefinition = "char(20)  COMMENT '接收者id'")
	public String getReceiverUserId() {
		return receiverUserId;
	}

	public void setReceiverUserId(String receiverUserId) {
		this.receiverUserId = receiverUserId;
	}

	@Column(name = "message", columnDefinition = "char(100)  COMMENT '消息'")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	// public void createFromChatItem(ChatItem chatItem) {
	// setSenderUserId(chatItem.getSendUserId());
	// setReceiverUserId(chatItem.getReceiveUserId());
	// }

	public TargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(TargetType targetType) {
		this.targetType = targetType;
	}

	public ChatItem.Builder createChatItem() {
		return createChatItem(this);
	}

	public static ChatItem.Builder createChatItem(Chatting chatting) {
		ChatItem.Builder chatItem = ChatItem.newBuilder();
		chatItem.setTargetType(chatting.targetType);
		chatItem.setSendUserId(chatting.getSenderUserId());
		if (chatting.targetType == TargetType.INDIVIDUAL)
			chatItem.setReceiveUserId(chatting.getReceiverUserId());
		else
			chatItem.setReceiveUserId(chatting.getGroupId() + "");
		chatItem.setChatType(chatting.getChattingType());
		chatItem.setChatBody(chatting.getMessage());
		chatItem.setDate(new Date().getTime());

		return chatItem;
	}

	public String toString() {
		return this.getSenderUserId() + " " + this.getReceiverUserId() + " " + this.getChattingType() + " " + this.getMessage();
	}
}
