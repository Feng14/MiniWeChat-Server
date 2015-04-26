package test;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import org.xml.sax.InputSource;

public class TestMessage {

	public static void main(String args[]) {
//		A a = new A();
//		B b = new B(a);
//		a.x = 3;
//		a.notifyObservers();
		int x = 123;
		byte y = (byte)x;
		System.out.println(y);
		int z = (int)y;
		System.out.println(z);
		
	}
}

class A extends Observable {
	public int x=0;
	public A() {

	}
	public void fuck(){
		setChanged();
	}
}

class B {
	public B(A a) {
		a.addObserver(new Observer() {
			
			@Override
			public void update(Observable o, Object arg) {
				System.out.println("Fuck you every Where");
			}
		});
		a.fuck();
		a.notifyObservers("Fuck");
	}
}