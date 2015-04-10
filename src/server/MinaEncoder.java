package server;

import java.io.ByteArrayOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import tools.DataTypeTranslater;

/**
 * 向客户端发数据时的组成模式
 * @author Administrator
 *
 */
public class MinaEncoder extends ProtocolEncoderAdapter {
	private ByteArrayOutputStream byteArrayOutputStream;
	private PacketFromServer packetWillSend;

	@Override
	public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput output) throws Exception {
//		String msg = (String) message;  
//        byte[] bytes = msg.getBytes("UTF-8");  
		
		byteArrayOutputStream = new ByteArrayOutputStream();
		if (message.getClass().equals(PacketFromServer.class)) {
			packetWillSend = (PacketFromServer) message;
			
			// 1.加入类型
			byteArrayOutputStream.write(packetWillSend.getMessageTypeBytes());
			
			// 2.添加MessageId
			byteArrayOutputStream.write(packetWillSend.getMessageID());
			
			// 3.加入数据包
			byteArrayOutputStream.write(packetWillSend.getMessageBoty());
			
			int sizeOfAll = byteArrayOutputStream.size() + DataTypeTranslater.INT_SIZE;
			
			IoBuffer buffer = IoBuffer.allocate(sizeOfAll);
			buffer.put(DataTypeTranslater.intToByte(sizeOfAll)); // header  
	        buffer.put(byteArrayOutputStream.toByteArray()); // body  
	        buffer.flip();  
	        output.write(buffer);
	        return;
		}
		
		
		
		byte[] bytes = (byte[]) message;
        int length = bytes.length;  
//        System.out.println("MinaEncoder : " + length);
        byte[] header = DataTypeTranslater.intToByte(length); // 按小字节序转成字节数组  
          
        IoBuffer buffer = IoBuffer.allocate(length + DataTypeTranslater.INT_SIZE);  
        buffer.put(header); // header  
        buffer.put(bytes); // body  
        buffer.flip();  
        output.write(buffer);  
	}

}
