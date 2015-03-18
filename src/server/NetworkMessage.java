package server;

import java.io.IOException;

import org.apache.mina.core.session.IoSession;

import protocol.ProtoHead;

import sun.awt.datatransfer.DataTransferer;
import tools.DataTypeTranslater;

/**
 * 存放网络通信每一个请求的对象（将放入队列）
 * 
 * @author Administrator
 * 
 */
public class NetworkMessage {
	// 信息中的int所占的字节数
	public static final int HEAD_INT_SIZE = 4;

	public IoSession ioSession;
	public byte[] arrayBytes;
	
	public NetworkMessage(IoSession ioSession, byte[] arrayBytes){
		this.ioSession = ioSession;
		this.arrayBytes = arrayBytes;
	}

	// 获取请求的长度
	public int getMessageLength() {
		return arrayBytes.length;
	}

	public static int getMessageLength(byte[] array) {
		return array.length;
	}

	// 获取请求类型
	public ProtoHead.ENetworkMessage getMessageType() {
		return getMessageType(arrayBytes);
	}

	public static ProtoHead.ENetworkMessage getMessageType(byte[] array) {
		return ProtoHead.ENetworkMessage.valueOf(DataTypeTranslater.bytesToInt(array, HEAD_INT_SIZE));
	}
	
	// 获取消息的对象byte数组
	public byte[] getMessageObjectBytes(){
		byte[] response = new byte[getMessageLength() - HEAD_INT_SIZE * 2];
		for (int i=0; i<response.length; i++)
			response[i] = arrayBytes[HEAD_INT_SIZE * 2 + i];
		
		return response;
	}
	
	// 打包成可以发送的byte[]
	public static byte[] packMessage(int messageType, byte[] packetBytes) throws IOException {
		int size = NetworkMessage.HEAD_INT_SIZE * 2 + packetBytes.length;
		byte[] messageBytes = new byte[size];
		
		// 1.添加size
		byte[] sizeBytes = DataTypeTranslater.intToByte(size);
		for (int i=0; i<sizeBytes.length; i++)
			messageBytes[i] = sizeBytes[i];
		
		// 2.加入类型
		int offset = sizeBytes.length;
		byte[] typeBytes = DataTypeTranslater.intToByte(messageType);
		for (int i=0; i<typeBytes.length; i++)
			messageBytes[offset + i] = typeBytes[i];
		offset += typeBytes.length;
		
		// 3.加入数据包
		for (int i=0; i<packetBytes.length; i++)
			messageBytes[offset + i] = packetBytes[i];
		
		return messageBytes;
	}
}
