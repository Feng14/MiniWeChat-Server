package server;

import java.io.IOException;
import model.HibernateSessionFactory;
import model.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import com.google.protobuf.InvalidProtocolBufferException;
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
	
	private Server_Friend(){
		
	}
	
	/**
	 * 搜索用户
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-23
	 */
	public void getUserInfo(NetworkMessage networkMessage){
		try {
			GetUserInfoMsg.GetUserInfoReq getUserInfoObject =GetUserInfoMsg.GetUserInfoReq.parseFrom(networkMessage.getMessageObjectBytes());
			GetUserInfoMsg.GetUserInfoRsp.Builder getUserInfoBuilder = GetUserInfoMsg.GetUserInfoRsp.newBuilder();
			
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", getUserInfoObject.getTargetUserId()));
			if(criteria.list().size()>0){
				//不支持模糊搜索 所以如果有搜索结果 只可能有一个结果
				User user = (User)criteria.list().get(0);
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.SUCCESS);
				
				UserData.UserItem.Builder userBuilder = UserData.UserItem.newBuilder();
				userBuilder.setUserId(user.getUserId());
				userBuilder.setUserName(user.getUserName());
				userBuilder.setHeadIndex(user.getHeadIndex());
				getUserInfoBuilder.setUserItem(userBuilder);
				
			}
			else{
				getUserInfoBuilder.setResultCode(GetUserInfoMsg.GetUserInfoRsp.ResultCode.USER_NOT_EXIST);
			}
			//回复客户端
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.GET_USERINFO_RSP.getNumber(),
							networkMessage.getMessageID(), getUserInfoBuilder.build().toByteArray()));
			
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
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
				Session session = HibernateSessionFactory.getSession();
				Criteria criteria = session.createCriteria(User.class);
				Criteria criteria2 = session.createCriteria(User.class);
				criteria.add(Restrictions.eq("userId", clientUser.userId));
				User u = (User) criteria.list().get(0);
				criteria2.add(Restrictions.eq("userId", addFriendObject.getFriendUserId()));
				friend = (User) criteria2.list().get(0);
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
				Transaction trans = session.beginTransaction();
				if(!exist1){
					u.getFriends().add(friend);
				    session.update(u);
				  
				}
				if(!exist2){
					friend.getFriends().add(u);
					session.update(friend);
					
					ClientUser friendUser = ServerModel.instance.getClientUserByUserId(friend.getUserId());
					if(null != friendUser){
						//如果对方在线  需要发消息给对方通知好友添加
					   sendSync(friendUser,u,ChangeFriendMsg.ChangeFriendSync.ChangeType.ADD);
					}
				}
				trans.commit();
				
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
