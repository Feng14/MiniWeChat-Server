package model;

public enum ResultCode {
	NULL,SUCCESS,FAIL,USEREXIST;//NULL代表初始状态
	private  ResultCode code;
	
	public void setCode(ResultCode code){
		this.code = code;
	}
	public ResultCode getCode(){
		return this.code;
	}

}
