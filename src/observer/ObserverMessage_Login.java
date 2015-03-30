package observer;

import org.apache.mina.core.session.IoSession;

public class ObserverMessage_Login extends ObserverMessage {
	public IoSession ioSession;
	public String userId;

	public ObserverMessage_Login(IoSession ioSession, String userId){
		type = Type.Login;
		this.ioSession = ioSession;
		this.userId = userId;
	}
}
