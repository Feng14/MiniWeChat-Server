package server;

public class Test2 {
	
	public Test test;

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
		System.out.println(test.fuck);
	}

	
	public void fuck(){
		System.out.println(test.fuck);
	}
}
