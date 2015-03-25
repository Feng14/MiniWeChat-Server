package client;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

public class Test {
	public HashMap<Integer, String> data;
	public Hashtable<Integer, A> data2;

	public static void main(String args[]) {
		final Test test = new Test();

		test.data = new HashMap<Integer, String>();
		for (int i = 0; i < 100; i++)
			test.data.put(i, i + "");
		
		test.data2 = new Hashtable<Integer, A>();
		A a = new A();
		test.data2.put(1, a);

		new Thread(new Runnable() {
			int icount = 0;
			
			@Override
			public void run() {
				A a = test.data2.get(1);
				System.err.println("A: get");
				try {
					Thread.sleep(10);
				} catch (Exception e) {
				}
				System.err.println("A change");
				a.b = 2;
				System.err.println("A : change Over!");
				System.err.println(test.data2.get(1));
					//Iterator iterator = test.data.keySet().iterator();
					//while (iterator.hasNext()) {
//					while (icount <= 99) {
//						test.data.put(icount, );
//						System.out.println("A: " + test.data.size());
//						++icount;
//					}
						//System.err.println("A : " + iterator.next().toString());
					//}
				
			}
		}).start();

		new Thread(new Runnable() {
			int icount = 0;
			@Override
			public void run() {
				try {
					Thread.sleep(2);
				} catch (Exception e) {
				}
				System.err.println("B: remove");
				test.data2.remove(1);
					//Iterator iterator = test.data.keySet().iterator();
					//while (iterator.hasNext()) {
//					while (icount <= 99) {
////						try {
////							//Thread.sleep(10);
////						} catch (InterruptedException e) {
////							e.printStackTrace();
////						}
//						System.out.println("B: " + test.data.size());
////						System.out.println("B: " + test.data.get(icount));
////						test.data.put(200+icount, ""+200+icount);
//						++icount;
//						//System.err.println("B : " + iterator.next().toString());
//					//}
//					}
			}
		}).start();
	}
	
}

