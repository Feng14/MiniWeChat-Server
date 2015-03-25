package client;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.crypto.Data;

public class Test {
	public HashMap<Integer, String> data;
	public Hashtable<Integer, A> data2;

	public static void main(String args[]) {
		final Test test = new Test();

		test.data = new HashMap<Integer, String>();
//		for (int i = 0; i < 100; i++)
//			test.data.put(i, i + "");
//		
//		test.data2 = new Hashtable<Integer, A>();
//		A a = new A();
//		test.data2.put(1, a);

		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i = 0; i < 300; i++) {
					test.data.put(new Integer(i), (new Integer(i)).toString() );
				}
			}
		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				for(int i = 300; i < 600; i++) {
					test.data.put(new Integer(i), (new Integer(i)).toString() );
				}
			}
		});
		
		t1.start();
		t2.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < 600; i++) {
			System.out.println(test.data.get(i));
		}
	}
	
}

