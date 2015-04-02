package test;

import java.util.Date;

public class test {

	public static void main(String args[]) {
		Date date = new Date();
		System.out.println(date);
		date.setDate(30);
		System.out.println(date);
		date.setDate(31);
		System.out.println(date);
	}
}
