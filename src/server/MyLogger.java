package server;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.logging.LoggingFilter;

import protocol.ProtoHead;
import tools.DataTypeTranslater;

public class MyLogger extends LoggingFilter {
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerModel serverModel;

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		super.messageReceived(nextFilter, session, message);
	}

	@Override
	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		
		
		// TODO Auto-generated method stub
		super.messageSent(nextFilter, session, writeRequest);
		showPacket(session, writeRequest);
	}

	/**
	 * log要发送给client的包
	 * @param ioSession
	 * @param size
	 * @param byteArray
	 */
	private PacketFromServer packetFromServer;
	private String userId;
	private void showPacket(IoSession ioSession, WriteRequest writeRequest) {
		userId = null;
		try {
			userId = serverModel.getClientUserFromTable(ioSession).userId;
		} catch (Exception e) {
		}
		packetFromServer = (PacketFromServer)writeRequest.getMessage();
		try {
			logger.info("Server send packet to client(" + ServerModel.getIoSessionKey(ioSession) + "); MessageType : "
					+ ProtoHead.ENetworkMessage.valueOf(packetFromServer.getMessageType()).toString() + "; MessageId : "
					+ DataTypeTranslater.bytesToInt(packetFromServer.getMessageID(), 0) + "; UserId : " + userId.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
