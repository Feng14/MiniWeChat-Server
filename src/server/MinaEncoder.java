package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import exception.NoIpException;

import tools.DataTypeTranslater;

/**
 * 向客户端发数据时的组成模式
 * 
 * @author Administrator
 * 
 */
public class MinaEncoder extends ProtocolEncoderAdapter {
	Logger logger = Logger.getLogger(this.getClass());
	private ByteArrayOutputStream byteArrayOutputStream;
	private PacketFromServer packetWillSend;

	@Override
	public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput output) throws Exception {
		// String msg = (String) message;
		// byte[] bytes = msg.getBytes("UTF-8");

		byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] byteArray;
		if (message.getClass().equals(PacketFromServer.class)) {
			packetWillSend = (PacketFromServer) message;

			// 1.加入类型
			byteArrayOutputStream.write(packetWillSend.getMessageTypeBytes());

			// 2.添加MessageId
			byteArrayOutputStream.write(packetWillSend.getMessageID());

			// 3.加入数据包
			byteArrayOutputStream.write(packetWillSend.getMessageBoty());

			int sizeOfAll = byteArrayOutputStream.size() + DataTypeTranslater.INT_SIZE;
			byteArray = byteArrayOutputStream.toByteArray();

			IoBuffer buffer = IoBuffer.allocate(sizeOfAll);
			buffer.put(DataTypeTranslater.intToByte(sizeOfAll)); // header
			buffer.put(byteArray); // body
			buffer.flip();
			output.write(buffer);

			showPacket(ioSession, sizeOfAll, byteArray);
			return;
		}

		byte[] bytes = (byte[]) message;
		int length = bytes.length;
		// System.out.println("MinaEncoder : " + length);
		byte[] header = DataTypeTranslater.intToByte(length); // 按小字节序转成字节数组

		IoBuffer buffer = IoBuffer.allocate(length + DataTypeTranslater.INT_SIZE);
		buffer.put(header); // header
		buffer.put(bytes); // body
		buffer.flip();
		output.write(buffer);
	}

	private ByteArrayOutputStream byteArrayOutputStream2;

	private void showPacket(IoSession ioSession, int size, byte[] byteArray) {
		byteArrayOutputStream2 = new ByteArrayOutputStream();
		try {
			byteArrayOutputStream2.write(DataTypeTranslater.intToByte(size));
			byteArrayOutputStream2.write(byteArray);
			byte[] newByteArray = byteArrayOutputStream2.toByteArray();
			try {
				logger.info("111111111111111111111111111111111111111111111111");
				logger.info("Server send Message to Client(" + ServerModel.getIoSessionKey(ioSession).toString() + ")  ,Type : "
						+ NetworkPacket.getMessageType(newByteArray) + "  MessageId : "
						+ DataTypeTranslater.bytesToInt(NetworkPacket.getMessageID(newByteArray), 0));
			} catch (NoIpException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		NetworkPacket.showPacket(logger, ioSession, byteArrayOutputStream2.toByteArray());
	}

}
