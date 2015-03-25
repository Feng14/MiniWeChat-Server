package tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

// 数据转换器
public class DataTypeTranslater {

	/**
	 *  int转byte[]
	 * @param number
	 * @return
	 * @throws IOException
	 */
	public static byte[] intToByte(int number) throws IOException {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		doutput.writeInt(number);
		return boutput.toByteArray();
	}
	
	/**
	 *  byte[4] 转int
	 * @param bytes
	 * @param offset
	 * @return
	 */
	public static int bytesToInt(byte[] bytes, int offset) {
		int value= 0;
	       for (int i = 0; i < 4; i++) {
	           int shift= (4 - 1 - i) * 8;
	           value +=(bytes[i + offset] & 0x000000FF) << shift;
	       }
	       return value;
	}
	
	private static ByteBuffer bbuf;
	/**
	 * float 转 byte[4]
	 * @param number
	 * @return
	 */
	public static byte[] floatToBytes(float number) {
		bbuf = ByteBuffer.allocate(4);
		bbuf.putFloat(number);  
		return bbuf.array();  
	}
}
