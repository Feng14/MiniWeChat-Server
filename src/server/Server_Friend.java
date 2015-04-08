package server;

import java.io.IOException;
import java.util.List;

import model.HibernateDataOperation;
import model.HibernateSessionFactory;
import model.ResultCode;
import model.User;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import com.google.protobuf.InvalidProtocolBufferException;

import exception.NoIpException;
import protocol.ProtoHead;
import protocol.Data.UserData;
import protocol.Data.UserData.UserItem;
import protocol.Msg.AddFriendMsg;
import protocol.Msg.ChangeFriendMsg;
import protocol.Msg.DeleteFriendMsg;
import protocol.Msg.GetUserInfoMsg;


/**
 * 主服务器下的子服务器 负责通讯录相关事件
 * 
 * @author wangfei
 *
 */
public class Server_Friend {
	public static Server_Friend instance = new Server_Friend();
	Logger logger = Logger.getLogger(Server_Friend.class);
	
	private Server_Friend(){
		
	}
	
	/**
	 * 搜索用户
	 * @param networkMessage
	 * @author wangfei
	 * @throws NoIpException 
	 * @time 2015-03-23
	 */
	public void getUserInfo(NetworkMessage networkMessage) throws NoIpException{
		logger.info("Server_Friend.getUserInfo:begin to getUserInfo!");
		GetUserInfoMsg.GetUserInfoRsp.Builder getUserInfoBuilder = GetUserInfoMsg.GetUserInfoRsp.newBuilder();
		try {
			GetUserInfoMsg.GetUserInfoReq getUserInfoObject =GetUserInfoMsg.GetUserInfoReq.parseFrom(networkMessage.getMessageObjectBytes());
			
			ResultCode code = ResultCode.NULL;
			List list = HibernateDataOperation.query("userId", getUserInfoObject.getTargetUserId(), User.class, code);
			
			if(code.getCode().equals(ResultCode.SUCCESS) && list.size()>0){
				//不支持模糊搜索 所以如果有搜索结果 只可能有一个结果
				User user = (User)list.get(0);
				UserData.UserItem.Builder userBuilder = UserData.UserItem.newBuilder();
				userBuilder.setUserId(user.getUserId());
				userBuilder.setUserName(user.getUserName());
				userBuilder.setHeadIndex(user.getHeadIndex());
				getUserInfoBuilder.setUserItem(userBuilder);
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS);
			}
			else if(code.getCode().equals(ResultCode.FAIL)){
				logger.error("Server_Friend.getUserInfo: Hibernate error");
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.FAIL);
			}
			else if(list.size()<1){
				logger.info("Server_Friend.getUserInfo:User:" + ServerModel.getIoSessionKey(networkMessage.ioSession) + " not exist!");
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.USER_NOT_EXIST);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Server_Friend.getUserInfo:Error was found when using Protobuf to deserialization "+ ServerModel.getIoSessionKey(networkMessage.ioSession) + " packet！");
			logger.error(e.getStackTrace());
			getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.FAIL);
		}
		try{
			//回复客户端
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession,NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.GET_USERINFO_RSP.getNumber(),networkMessage.getMessageID(), getUserInfoBuilder.build().toByteArray()));
		}
		catch(IOException e){
			logger.error("Server_Friend.getUserInfo deal with user:"+ServerModel.getIoSessionKey(networkMessage.ioSession)+" Send result Fail!");
			logger.error(e.getStackTrace());
		}
	}

	/**
	 * 添加好友
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-24
	 */
	public void addFriend(NetworkMessage networkMessage){
		try {
			AddFriendMsg.AddFriendReq addFriendObject = AddFriendMsg.AddFriendReq.
					parseFrom(networkMessage.getMessageObjectBytes());
			AddFriendMsg.AddFriendRsp.Builder addFriendBuilder = AddFriendMsg.AddFriendRsp.newBuilder();
			
			ClientUser clientUser = ServerModel.instance.getClientUserFromTable(
					networkMessage.ioSession.getRemoteAddress().toString());
			User friend =null;
			try{
				ResultCode code1 = ResultCode.NULL;
				ResultCode code2 = ResultCode.NULL;
				List list1 = HibernateDataOperation.query("userId", clientUser.userId, User.class, code1);
				List list2 = HibernateDataOperation.query("userId", addFriendObject.getFriendUserId(), User.class, code2);
				
				User u = (User) list1.get(0);
				friend = (User) list2.get(0);
				//检测双方是否已经是好友关系
				boolean exist1 = false,exist2 = false;
				for(User user:u.getFriends()){
					if(user.getUserId().equals(friend.getUserId())){
						exist1 = true;
						break;
					}
				}
				for(User user:friend.getFriends()){
					if(user.getUserId().equals(u.getUserId())){
						exist2 = true ;
						break;
					}
				}
				//如果不存在好友关系 则添加好友
				
				if(!exist1){
					u.getFriends().add(friend);
				    ResultCode code = ResultCode.NULL;
				    HibernateDataOperation.update(u, code);
				    sendSync(clientUser,friend,ChangeFriendMsg.ChangeFriendSync.ChangeType.ADD);
				}
				if(!exist2){
					friend.getFriends().add(u);
					ResultCode code = ResultCode.NULL;
					HibernateDataOperation.update(friend, code);
					
					ClientUser friendUser = ServerModel.instance.getClientUserByUserId(friend.getUserId());
					if(null != friendUser){
						//如果对方在线  需要发消息给对方通知好友添加
					   sendSync(friendUser,u,ChangeFriendMsg.ChangeFriendSync.ChangeType.ADD);
					}
				}
				
			    addFriendBuilder.setResultCode(AddFriendMsg.AddFriendRsp.ResultCode.SUCCESS);
			}catch(Exception e){
				addFriendBuilder.setResultCode(AddFriendMsg.AddFriendRsp.ResultCode.FAIL);
				e.printStackTrace();
			}
			

			//回复客户端
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.ADD_FRIEND_RSP.getNumber(),
							networkMessage.getMessageID(), addFriendBuilder.build().toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	private void add(User user,User friend){
		
	}
	
	/**
	 * 删除好友
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-24
	 */
	public void deleteFriend(NetworkMessage networkMessage){
		try {
			DeleteFriendMsg.DeleteFriendReq deleteFriendObject = DeleteFriendMsg.DeleteFriendReq.
					parseFrom(networkMessage.getMessageObjectBytes());
			DeleteFriendMsg.DeleteFriendRsp.Builder DeleteFriendBuilder = DeleteFriendMsg.DeleteFriendRsp.newBuilder();
			
			ClientUser clientUser = ServerModel.instance.getClientUserFromTable(
					networkMessage.ioSession.getRemoteAddress().toString());
			User friend = null;
			try{
				Session session = HibernateSessionFactory.getSession();
				Criteria criteria = session.createCriteria(User.class);
				Criteria criteria2 = session.createCriteria(User.class);
				criteria.add(Restrictions.eq("userId", clientUser.userId));
				User u = (User) criteria.list().get(0);
				criteria2.add(Restrictions.eq("userId", deleteFriendObject.getFriendUserId()));
				friend = (User) criteria2.list().get(0);
				User x=null,y=null;
				//检测双方之前是否是好友关系
				for(User a:u.getFriends()){
					if(a.getUserId().equals(friend.getUserId()))
						x=a;
				}
				for(User b:friend.getFriends()){
					if(b.getUserId().equals(u.getUserId()))
						y=b;
				}
				//如果是存在好友关系 则删除
				Transaction trans = session.beginTransaction();
				if(null!=x){
					u.getFriends().remove(x);
					session.update(u);
					sendSync(clientUser,friend,ChangeFriendMsg.ChangeFriendSync.ChangeType.DELETE);
				}
				if(null != y){
					friend.getFriends().remove(y);
					session.update(friend);
					ClientUser friendUser = ServerModel.instance.getClientUserByUserId(friend.getUserId());
					if(null != friendUser){
						sendSync(friendUser,u,ChangeFriendMsg.ChangeFriendSync.ChangeType.DELETE);
					}
				}
				
				trans.commit();
			    
			    DeleteFriendBuilder.setResultCode(DeleteFriendMsg.DeleteFriendRsp.ResultCode.SUCCESS);
			}catch(Exception e){
				DeleteFriendBuilder.setResultCode(DeleteFriendMsg.DeleteFriendRsp.ResultCode.FAIL);
				e.printStackTrace();
			}
			

			//回复客户端
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.DELETE_FRIEND_RSP.getNumber(),
							networkMessage.getMessageID(), DeleteFriendBuilder.build().toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void delete(User user,User friend){
		
	}
	
	private void sendSync(ClientUser clientUser,User user,ChangeFriendMsg.ChangeFriendSync.ChangeType type){
		 UserItem.Builder uib = UserItem.newBuilder();
		 uib.setUserId(user.getUserId());
		 uib.setUserName(user.getUserName());
		 uib.setHeadIndex(user.getHeadIndex());
		 ChangeFriendMsg.ChangeFriendSync.Builder cfb = ChangeFriendMsg.ChangeFriendSync.newBuilder();
		 cfb.setChangeType(type);
		 cfb.setUserItem(uib);
		 //向客户端发送消息
		 byte[] messageBytes;
		try {
			messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.CHANGE_FRIEND_SYNC.getNumber(), cfb.build().toByteArray());
			 ServerNetwork.instance.sendMessageToClient(clientUser.ioSession, messageBytes);
			 // 添加等待回复
			 ServerModel.instance.addClientResponseListener(clientUser.ioSession, NetworkMessage.getMessageID(messageBytes),messageBytes, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
