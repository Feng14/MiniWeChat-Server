package server;

import java.io.ByteArrayOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import tools.DataTypeTranslater;

/**
 * 接到客户端数据时的粘包方式
 * @author Feng
 *
 */
public class MinaDecoder extends CumulativeProtocolDecoder {
	private ByteArrayOutputStream byteArrayOutputStream;

	@Override
	protected boolean doDecode(IoSession ioSession, IoBuffer ioBuffer, ProtocolDecoderOutput output) throws Exception {
		// 如果没有接收完Size部分（4字节），直接返回false
		if (ioBuffer.remaining() < 4)
			return false;
		else {
			// 标记开始位置，如果一条消息没传输完成则返回到这个位置
			ioBuffer.mark();
			
			byteArrayOutputStream = new ByteArrayOutputStream();

			// 读取Size
			byte[] bytes = new byte[4];
			ioBuffer.get(bytes); // 读取4字节的Size
			byteArrayOutputStream.write(bytes);
			int bodyLength = DataTypeTranslater.bytesToInt(bytes, 0) - DataTypeTranslater.INT_SIZE; // 按小字节序转int

			// 如果body没有接收完整，直接返回false
			if (ioBuffer.remaining() < bodyLength) {
				ioBuffer.reset(); // IoBuffer position回到原来标记的地方
				return false;
			} else {
				byte[] bodyBytes = new byte[bodyLength];
				ioBuffer.get(bodyBytes);
//				String body = new String(bodyBytes, "UTF-8");
				byteArrayOutputStream.write(bodyBytes);
				
				// 创建对象
				PacketFromClient packetFromClient = new PacketFromClient(ioSession, byteArrayOutputStream.toByteArray());
				
				output.write(packetFromClient); // 解析出一条消息
				return true;
			}
		}
	}

}
