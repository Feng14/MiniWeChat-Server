package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.logging.LoggingFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import exception.MyException;

import protocol.ProtoHead;
import server.LoggerRule.LoggerType;
import tools.DataTypeTranslater;

public class MyLogger extends LoggingFilter {
	private Logger logger = Logger.getLogger(this.getClass());
	private ServerModel serverModel;
	public static final String LoggerXML = "LoggerRule.xml";
	
	private LoggerRule loggerRule;
	
	public MyLogger(){
		readXML();
	}

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}

	// @Override
	// public void messageReceived(NextFilter nextFilter, IoSession session,
	// Object message) throws Exception {
	// // TODO Auto-generated method stub
	// super.messageReceived(nextFilter, session, message);
	// }

	@Override
	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		super.messageSent(nextFilter, session, writeRequest);
		showPacket(session, writeRequest);
	}

	/**
	 * log要发送给client的包
	 * 
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
		packetFromServer = (PacketFromServer) writeRequest.getMessage();
		
		try {
			ProtoHead.ENetworkMessage messageType = ProtoHead.ENetworkMessage.valueOf(packetFromServer.getMessageType());
			if (loggerRule.loggerType == LoggerType.Contain && !loggerRule.loggerSet.contains(messageType))
				return;
			if (loggerRule.loggerType == LoggerType.Ignore && loggerRule.loggerSet.contains(messageType))
				return;
			
			logger.info("Server send packet to client(" + ServerModel.getIoSessionKey(ioSession) + "); MessageType : "
					+ messageType.toString() + "; MessageId : "
					+ DataTypeTranslater.bytesToInt(packetFromServer.getMessageID(), 0) + "; UserId : "
					+ (userId == null ? "null" : userId));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readXML() {
        DocumentBuilderFactory domfac=DocumentBuilderFactory.newInstance();
		try {
            DocumentBuilder dombuilder=domfac.newDocumentBuilder();
            InputStream is=new FileInputStream(LoggerXML);    
            Document doc=dombuilder.parse(is);        
//			File file  = new File(LoggerXML);
//			System.out.println(file.exists());
//			BufferedReader reader = new BufferedReader(new FileReader(file));
//			System.out.println(reader.readLine());

			Element root = doc.getDocumentElement(); // 获取根元素
			String typeContain = root.getElementsByTagName("TypeContain").item(0).getFirstChild().getNodeValue().toString();

			loggerRule = new LoggerRule(typeContain.equals("True") ? LoggerType.Contain
					: LoggerType.Ignore);
			
			NodeList nodeList = root.getElementsByTagName("ProtoHead");
			String nodeName;
			for (int i=0; i<nodeList.getLength(); i++) {
				nodeName = nodeList.item(i).getFirstChild().getNodeValue().toString();
				loggerRule.loggerSet.add(ProtoHead.ENetworkMessage.valueOf(nodeName));
			}

		} catch (Exception e) {
			logger.error("MyLogger : load " + LoggerXML + " file error!\n" + MyException.getStackStr(e.getStackTrace()));
		}
	}

}

class LoggerRule {
	public static enum LoggerType {
		Contain, Ignore
	};

	public LoggerType loggerType;
	HashSet<ProtoHead.ENetworkMessage> loggerSet;

	public LoggerRule(LoggerType loggerType) {
		this.loggerType = loggerType;
		loggerSet = new HashSet<ProtoHead.ENetworkMessage>();
	}
}
