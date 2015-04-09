package server;

import java.io.IOException;

import com.google.protobuf.Message.Builder;

import tools.DataTypeTranslater;

/**
 * 服务器要发给客户端的包
 * 
 * @author Feng
 * 
 */
public class PacketFromServer {
	private byte[] messageID = null;
	private int messageType = -1;
	private byte[] messageBoty = null;

	public PacketFromServer(int messageType, byte[] messageBoty) {
		setMessageID(ServerModel.createMessageId());
		setMessageType(messageType);
		setMessageBoty(messageBoty);
	}

	public PacketFromServer(byte[] messageID, int messageType, byte[] messageBoty) {
		setMessageID(messageID);
		setMessageType(messageType);
		setMessageBoty(messageBoty);
	}

	public byte[] getMessageID() {
		return messageID;
	}

	public void setMessageID(byte[] messageID) {
		this.messageID = messageID;
	}

	public int getMessageType() {
		return messageType;
	}

	public byte[] getMessageTypeBytes() throws IOException {
		return DataTypeTranslater.intToByte(getMessageType());
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public byte[] getMessageBoty() {
		return messageBoty;
	}

	public void setMessageBoty(byte[] messageBoty) {
		this.messageBoty = messageBoty;
	}

}
