package server;

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
}
