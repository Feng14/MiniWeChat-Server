package tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// 数据转换器
public class DataTypeTranslater {

	// int转byte[]
	public static byte[] intToByte(int number) throws IOException {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		doutput.writeInt(number);
		return boutput.toByteArray();
	}
	
	// byte[4] 转int
	public static int bytesToInt(byte[] bytes, int offset) {
		int value= 0;
	       for (int i = 0; i < 4; i++) {
	           int shift= (4 - 1 - i) * 8;
	           value +=(bytes[i + offset] & 0x000000FF) << shift;
	       }
	       return value;
	}
}
