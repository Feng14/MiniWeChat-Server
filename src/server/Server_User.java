package server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import model.HibernateSessionFactory;
import model.User;

import observer.ObserverMessage_Login;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.org.apache.bcel.internal.generic.NEW;

import exception.NoIpException;

import protocol.ProtoHead;
import protocol.Data.UserData;
import protocol.Data.UserData.UserItem;
import protocol.Msg.GetPersonalInfoMsg;
import protocol.Msg.GetUserInfoMsg;
import protocol.Msg.LoginMsg;
import protocol.Msg.LogoutMsg;
import protocol.Msg.OffLineMsg;
import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.RegisterMsg;
import tools.Debug;

/**
 * 主服务器下的子服务器，负责处理用户相关事件
 * 
 * @author Feng
 * 
 */
public class Server_User {
	public static Server_User instance = new Server_User();

	private Server_User() {

	}

	/**
	 * 对 用户心跳包回复 的处理 将online值设为True
	 * 
	 * @param networkMessage
	 * @author Feng
	 */
	public void keepAlive(NetworkMessage networkMessage) {
		// System.out.println((networkMessage == null) + "      " +
		// (networkMessage.ioSession == null));
		// System.out.println(ServerModel.instance.clientUserTable.keySet().size());
		// System.out.println("fuck   " +
		// ServerModel.instance.clientUserTable.containsKey(ServerModel.getIoSessionKey(networkMessage.ioSession)));
		// 如果ClientUser已经掉线被删除，那么就不管了
		try {
			Debug.log("Server_User", "对  用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  回复的心跳包  的处理");

			if (ServerModel.instance.getClientUserFromTable(networkMessage.ioSession) == null) {
				Debug.log("Server_User", "用户表(ClientUserTalbe)中找不到 用户" + ServerModel.getIoSessionKey(networkMessage.ioSession)
						+ "，心跳回复不作处理!");
				return;
			}

			ServerModel.instance.getClientUserFromTable(networkMessage.ioSession).onLine = true;
		} catch (NullPointerException e) {
			System.out.println("Server_User: 异常，用户" + networkMessage.ioSession + "已掉线，心跳回复不作处理!");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理新用户注册事件
	 * 
	 * @param networkMessage
	 * @author Feng
	 * @throws NoIpException
	 */
	public void register(NetworkMessage networkMessage) throws NoIpException {
		try {
			Debug.log("Server_User", "注册事件： 对  用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的注册事件  的处理");

			RegisterMsg.RegisterReq registerObject = RegisterMsg.RegisterReq.parseFrom(networkMessage.getMessageObjectBytes());
			RegisterMsg.RegisterRsp.Builder responseBuilder = RegisterMsg.RegisterRsp.newBuilder();

			// 查找是否存在同名用户
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", registerObject.getUserId()));
			if (criteria.list().size() > 0) { // 已存在
				// 已存在相同账号用户，告诉客户端
				// System.out.println("什么鬼？");
				Debug.log("Server_User", "注册事件：用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的注册账号重复，返回错误!");

				responseBuilder.setResultCode(RegisterMsg.RegisterRsp.ResultCode.USER_EXIST);
			} else { // 没问题，可以开始注册
				User user = new User();
				user.setUserId(registerObject.getUserId());
				user.setUserName(registerObject.getUserName());
				user.setUserPassword(registerObject.getUserPassword());

				session = HibernateSessionFactory.getSession();
				session.save(user);
				HibernateSessionFactory.commitSession(session);

				// 成功，设置回包码
				Debug.log("Server_User", "注册事件：用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  注册成功，返回消息!");
				responseBuilder.setResultCode(RegisterMsg.RegisterRsp.ResultCode.SUCCESS);
			}

			// 回复客户端
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.REGISTER_RSP.getNumber(), networkMessage.getMessageID(), responseBuilder.build()
							.toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : 注册事件： 用Protobuf反序列化 " + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ " 的包时异常！");
		} catch (IOException e) {
			System.err.println("Server_User : 注册事件： " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " 返回包时异常！");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理Client的“登陆请求”
	 * 
	 * @param networkMessage
	 * @author Feng
	 * @throws NoIpException
	 */
	public void login(NetworkMessage networkMessage) throws NoIpException {
		try {
			Debug.log(new String[] { "Server_User", "login" }, " 对  用户" + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ "  的登陆事件  的处理");

			LoginMsg.LoginReq loginObject = LoginMsg.LoginReq.parseFrom(networkMessage.getMessageObjectBytes());
			LoginMsg.LoginRsp.Builder loginBuilder = LoginMsg.LoginRsp.newBuilder();

			// 查找是否存在同名用户
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", loginObject.getUserId()));
			if (criteria.list().size() > 0) { // 已存在
				// 用户存在，开始校验
				User user = (User) criteria.list().get(0);
				if (user.getUserPassword().equals(loginObject.getUserPassword())) { // 密码正确
					Debug.log(new String[] { "Server_User", "login" },
							"用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的登陆校验成功!");

					// 检查是否有重复登陆
					checkAnotherOnline(networkMessage, loginObject.getUserId());

					// 记录到表中
					ClientUser clientUser = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
					if (clientUser != null)
						clientUser.userId = loginObject.getUserId();

					// 记录回复位
					loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.SUCCESS);
				} else { // 密码错误
					Debug.log(new String[] { "Server_User", "login" },
							"用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的登陆密码错误!");
					loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
				}
			} else { // 用户不存在
				Debug.log(new String[] { "Server_User", "login" }, "用户" + ServerModel.getIoSessionKey(networkMessage.ioSession)
						+ "  的用户不存在!");
				loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
			}
			session.close();

			// 回复客户端
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.LOGIN_RSP.getNumber(), networkMessage.getMessageID(), loginBuilder.build()
							.toByteArray()));
			
			// 广播“由用户登陆消息
			ServerModel.instance.notifyObservers(new ObserverMessage_Login(networkMessage.ioSession, loginObject.getUserId()));
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : 注册事件： 用Protobuf反序列化 " + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ " 的包时异常！");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Server_User : 注册事件： " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " 返回包时异常！");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查是否有另一个同账号的用户登陆，有的话踢下去
	 * 
	 * @param networkMessage
	 * @return
	 * @throws IOException
	 * @throws NoIpException
	 */
	private boolean checkAnotherOnline(NetworkMessage networkMessage, String userId) throws IOException {
		ClientUser user = ServerModel.instance.getClientUserByUserId(userId);
		if (user != null) {
			// 发送有他人登陆消息
			OffLineMsg.OffLineSync.Builder offLineMessage = OffLineMsg.OffLineSync.newBuilder();
			offLineMessage.setCauseCode(OffLineMsg.OffLineSync.CauseCode.ANOTHER_LOGIN);
			byte[] objectBytes = offLineMessage.build().toByteArray();

			try {
				Debug.log(new String[] { "Server_User", "checkAnotherOnline" },
						"用户 " + user.userId + "在其他设备登陆，" + ServerModel.getIoSessionKey(user.ioSession) + "被踢下线！");
			} catch (NoIpException e) {
				Debug.log(new String[] { "Server_User", "checkAnotherOnline" }, "找到的用户已断线，不做处理！");
				return false;
			}
			// 向客户端发送消息
			byte[] messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.OFFLINE_SYNC.getNumber(), objectBytes);
			ServerNetwork.instance.sendMessageToClient(user.ioSession, messageBytes);

			// 添加等待回复
			ServerModel.instance.addClientResponseListener(networkMessage.ioSession, NetworkMessage.getMessageID(messageBytes),
					messageBytes, null);

			return true;
		}
		return false;
	}

	/**
	 * 
	 * 另一个人登陆，本用户被踢下的通知的回复
	 * 
	 * @param networkMessage
	 * @author Feng
	 * @throws NoIpException
	 */
	public void clientOfflineResponse(NetworkMessage networkMessage) throws NoIpException {
		ClientUser user = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
		Debug.log(new String[] { "Srever_User", "clientOfflineResponse" },
				"客户端 " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " 已接到被踢下的消息，将其设为死亡！");
		// 删掉连接中用户信息表的登陆数据
		user.userId = null;
	}

	/**
	 * 处理个人设置请求
	 * 
	 * @param networkMessage
	 * @author wangfei
	 * @throws NoIpException
	 * @time 2015-03-21
	 */
	public void personalSettings(NetworkMessage networkMessage) throws NoIpException {
		try {
			Debug.log(new String[] { "Server_User", "personalSettings" },
					" 对  用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的个人设置事件  的处理");

			PersonalSettingsMsg.PersonalSettingsReq personalSettingsObject = PersonalSettingsMsg.PersonalSettingsReq
					.parseFrom(networkMessage.getMessageObjectBytes());
			PersonalSettingsMsg.PersonalSettingsRsp.Builder personalSettingsBuilder = PersonalSettingsMsg.PersonalSettingsRsp
					.newBuilder();
			System.out.println(personalSettingsObject.getUserName()+" "+personalSettingsObject.getUserPassword()+" "+personalSettingsObject.getHeadIndex());

			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);

			ClientUser clientUser = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
			boolean changePassword = false;
			criteria.add(Restrictions.eq("userId", clientUser.userId));
			if (criteria.list().size() > 0) {
				User user = (User) criteria.list().get(0);
				// 修改昵称
				if (personalSettingsObject.getUserName() != null && personalSettingsObject.getUserName() != "") {
					changeUserName(personalSettingsBuilder,networkMessage,user,personalSettingsObject.getUserName());
				}
				// 修改密码
				if (personalSettingsObject.getUserPassword() != null && personalSettingsObject.getUserPassword() != "") {
					changeUserPassword(personalSettingsBuilder,networkMessage,clientUser,user,personalSettingsObject.getUserPassword());
				}

				// 修改头像
				if (personalSettingsObject.getHeadIndex() >= 1 && personalSettingsObject.getHeadIndex() <= 6) {
					changeHeadIndex(personalSettingsBuilder,networkMessage,clientUser,user,personalSettingsObject.getHeadIndex());
				}
			} else {
				// 用户不存在
				Debug.log(new String[] { "Server_User", "personalSettings" },
						"用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的用户不存在!");
				personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			}
			session.close();
			
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : 个人设置事件： 用Protobuf反序列化 " + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ " 的包时异常！");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}
	
	private void changeUserName(PersonalSettingsMsg.PersonalSettingsRsp.Builder builder,NetworkMessage networkMessage,User u,String userName){
		try {
			Session session = HibernateSessionFactory.getSession();
			Transaction trans = session.beginTransaction();
			u.setUserName(userName);
			session.update(u);
			trans.commit();
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
		} catch (Exception e) {
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			e.printStackTrace();
		}
		try {
			// 回复客户端
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP.getNumber(), networkMessage.getMessageID(), builder.build().toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void changeUserPassword(PersonalSettingsMsg.PersonalSettingsRsp.Builder builder,NetworkMessage networkMessage,ClientUser clientUser,User u,String userPassword){
		try {
			Session session = HibernateSessionFactory.getSession();
			Transaction trans = session.beginTransaction();
			u.setUserPassword(userPassword);
			session.update(u);
			trans.commit();
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
		} catch (Exception e) {
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			e.printStackTrace();
		}
		// 向客户端发送消息 更改密码后客户端需要下线重新登录
		OffLineMsg.OffLineSync.Builder offLineMessage = OffLineMsg.OffLineSync.newBuilder();
		offLineMessage.setCauseCode(OffLineMsg.OffLineSync.CauseCode.CHANGE_PASSWORD);
		byte[] objectBytes = offLineMessage.build().toByteArray();
		byte[] messageBytes = null;
		try {
			messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.OFFLINE_SYNC.getNumber(), objectBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientUser.userId = null;
		ServerNetwork.instance.sendMessageToClient(clientUser.ioSession, messageBytes);

		// 添加等待回复
		ServerModel.instance.addClientResponseListener(networkMessage.ioSession,
				NetworkMessage.getMessageID(messageBytes), messageBytes, null);
		try {
			// 回复客户端
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP.getNumber(), networkMessage.getMessageID(), builder.build().toByteArray()));
			} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	private void changeHeadIndex(PersonalSettingsMsg.PersonalSettingsRsp.Builder builder,NetworkMessage networkMessage,ClientUser clientUser,User u,int headInx){
		BufferedImage image = null;
		u.setHeadIndex(headInx);
		try {
			Session session = HibernateSessionFactory.getSession();
			Transaction trans = session.beginTransaction();
			u.setHeadIndex(headInx);
			session.update(u);
			trans.commit();
			// 从默认头像文件夹获取图片
			image = ImageIO.read(new File(ResourcePath.headDefaultPath + headInx
					+ ".png"));
			File file = new File(ResourcePath.headPath);
			// 检查保存头像的文件夹是否存在
			if (!file.exists() && !file.isDirectory()) {
				// 如果不存在 则创建文件夹
				file.mkdir();
			}
			// 保存获取的默认头像到头像文件夹
			File saveFile = new File(ResourcePath.headPath + clientUser.userId + ".png");
			ImageIO.write(image, "png", saveFile);
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
		} catch (IOException e) {
			System.err.println("保存头像图片失败");
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			e.printStackTrace();
		}
		try {
			// 回复客户端
			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP.getNumber(), networkMessage.getMessageID(), builder.build().toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户退出登录
	 * 
	 * @param networkMessage
	 * @author wangfei
	 * @time 2015-03-25
	 */
	public void logout(NetworkMessage networkMessage) {
		try {
			ClientUser user = null;
			LogoutMsg.LogoutRsp.Builder logoutBuilder = null;
			try {
				user = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
				logoutBuilder = LogoutMsg.LogoutRsp.newBuilder();
				Debug.log(new String[] { "Srever_User", "logout" },
						"客户端 " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " 退出登录！");

				user.userId = null;
				logoutBuilder.setResultCode(LogoutMsg.LogoutRsp.ResultCode.SUCCESS);
			} catch (NoIpException e) {
				logoutBuilder.setResultCode(LogoutMsg.LogoutRsp.ResultCode.SUCCESS);
				e.printStackTrace();
			}

			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.LOGOUT_RSP.getNumber(), networkMessage.getMessageID(), logoutBuilder.build()
							.toByteArray()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * 获取个人信息 包括基本信息和好友列表
	 * @param networkMessage
	 */
	public void getPersonalInfo(NetworkMessage networkMessage){
		try {
			
			GetPersonalInfoMsg.GetPersonalInfoReq getPersonalInfoObject =GetPersonalInfoMsg.GetPersonalInfoReq.parseFrom(networkMessage.getMessageObjectBytes());
			GetPersonalInfoMsg.GetPersonalInfoRsp.Builder getPersonalInfoBuilder = GetPersonalInfoMsg.GetPersonalInfoRsp.newBuilder();
			ClientUser user = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);
			
			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);
			criteria.add(Restrictions.eq("userId", user.userId));
			if(criteria.list().size()>0){
				//不支持模糊搜索 所以如果有搜索结果 只可能有一个结果
				User u = (User)criteria.list().get(0);
				getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.SUCCESS);
				//获取用户的基本信息
				if(getPersonalInfoObject.getUserInfo() == true){
					UserItem.Builder userItemBuilder = UserItem.newBuilder();
					userItemBuilder.setUserId(u.getUserId());
					userItemBuilder.setUserName(u.getUserName());
					userItemBuilder.setHeadIndex(u.getHeadIndex());
					getPersonalInfoBuilder.setUserInfo(userItemBuilder);
				}
				//获取用户的好友信息
				if(getPersonalInfoObject.getFriendInfo() == true){
					for(User ui:u.getFriends()){
						UserItem.Builder userItemBuilder2 = UserItem.newBuilder();
						userItemBuilder2.setUserId(ui.getUserId());
						userItemBuilder2.setUserName(ui.getUserName());
						userItemBuilder2.setHeadIndex(ui.getHeadIndex());
						getPersonalInfoBuilder.addFriends(userItemBuilder2);
					}
				}
				getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.SUCCESS);
			}
			else{
				getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.FAIL);
			}
			//回复客户端
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.GET_PERSONALINFO_RSP.getNumber(),
							networkMessage.getMessageID(), getPersonalInfoBuilder.build().toByteArray()));
			
		} catch (NoIpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
