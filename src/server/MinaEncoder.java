package server;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import tools.DataTypeTranslater;

/**
 * 向客户端发数据时的组成模式
 * @author Administrator
 *
 */
public class MinaEncoder extends ProtocolEncoderAdapter {

	@Override
	public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput output) throws Exception {
//		String msg = (String) message;  
//        byte[] bytes = msg.getBytes("UTF-8");  
		byte[] bytes = (byte[]) message;
        int length = bytes.length;  
        byte[] header = DataTypeTranslater.intToByte(length); // 按小字节序转成字节数组  
          
        IoBuffer buffer = IoBuffer.allocate(length + DataTypeTranslater.INT_SIZE);  
        buffer.put(header); // header  
        buffer.put(bytes); // body  
        buffer.flip();  
        output.write(buffer);  
	}

}
