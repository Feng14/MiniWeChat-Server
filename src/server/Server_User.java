package server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

import model.Group;
import model.HibernateDataOperation;
import model.HibernateSessionFactory;
import model.ResultCode;
import model.User;
import observer.ObserverMessage_Login;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import com.google.protobuf.InvalidProtocolBufferException;
import exception.NoIpException;
import protocol.ProtoHead;
import protocol.Data.GroupData.GroupItem;
import protocol.Data.UserData.UserItem;
import protocol.Msg.GetPersonalInfoMsg;
import protocol.Msg.LoginInitAllInfoMsg;
import protocol.Msg.LoginMsg;
import protocol.Msg.LogoutMsg;
import protocol.Msg.OffLineMsg;
import protocol.Msg.PersonalSettingsMsg;
import protocol.Msg.PersonalSettingsMsg.PersonalSettingsRsp;
import protocol.Msg.RegisterMsg.RegisterRsp;
import protocol.Msg.RegisterMsg;
import protocol.Msg.GetPersonalInfoMsg.GetPersonalInfoRsp;
import protocol.Msg.LoginMsg.LoginRsp;
import tools.GetImage;

/**
 * 主服务器下的子服务器，负责处理用户相关事件
 * 
 * @author Feng
 * 
 */
public class Server_User {
	Logger logger = Logger.getLogger(Server_User.class);

	private ServerModel serverModel;
	private ServerNetwork serverNetwork;

	public ServerModel getServerModel() {
		return serverModel;
	}

	public void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}

	public ServerNetwork getServerNetwork() {
		return serverNetwork;
	}

	public void setServerNetwork(ServerNetwork serverNetwork) {
		this.serverNetwork = serverNetwork;
	}

	/**
	 * 对 用户心跳包回复 的处理 将online值设为True
	 * 
	 * @param packetFromServer
	 * @author Feng
	 */
	public void keepAlive(NetworkPacket packetFromServer) {
		// System.out.println((packetFromServer == null) + "      " +
		// (packetFromServer.ioSession == null));
		// System.out.println(serverModel.clientUserTable.keySet().size());
		// System.out.println("fuck   " +
		// serverModel.clientUserTable.containsKey(ServerModel.getIoSessionKey(packetFromServer.ioSession)));
		// 如果ClientUser已经掉线被删除，那么就不管了
		try {
			// Debug.log("Server_User", "Deal with user's" +
			// ServerModel.getIoSessionKey(packetFromServer.ioSession)
			// + " 'keepAlivePacket' reply");
			logger.info("Server_User : Deal with user's" + ServerModel.getIoSessionKey(packetFromServer.ioSession)
					+ " 'keepAlivePacket' reply");

			if (serverModel.getClientUserFromTable(packetFromServer.ioSession) == null) {
				// Debug.log(Debug.LogType.EXCEPTION, "Server_User",
				// "Can't find user in 'ClientUserTalbe'" +
				// ServerModel.getIoSessionKey(packetFromServer.ioSession)
				// + "，user's 'KeepAlivePacket' reply will be ignore!");
				logger.warn("Server_User : Can't find user in 'ClientUserTalbe'"
						+ ServerModel.getIoSessionKey(packetFromServer.ioSession)
						+ "，user's 'KeepAlivePacket' reply will be ignore!");
				return;
			}

			serverModel.getClientUserFromTable(packetFromServer.ioSession).onLine = true;
		} catch (NullPointerException e) {
			System.out.println("Server_User: 异常，用户" + packetFromServer.ioSession + "已掉线，心跳回复不作处理!");
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理新用户注册事件
	 * 
	 * @param packetFromServer
	 * @author Feng
	 * @throws NoIpException
	 */
	public void register(NetworkPacket packetFromServer) throws NoIpException {
		logger.info("Server_User.register:begin to register");
		RegisterMsg.RegisterRsp.Builder responseBuilder = RegisterMsg.RegisterRsp.newBuilder();
		responseBuilder.setResultCode(RegisterRsp.ResultCode.USER_EXIST);

		try {
			RegisterMsg.RegisterReq registerObject = RegisterMsg.RegisterReq.parseFrom(packetFromServer.getMessageObjectBytes());

			logger.info("Server_User" + "'RegisterEvent': Deal with user's"
					+ ServerModel.getIoSessionKey(packetFromServer.ioSession) + " 'RegisterEvent'");

			// 查找是否存在同名用户
			ResultCode code = ResultCode.NULL;
			List list = HibernateDataOperation.query("userId", registerObject.getUserId(), User.class, code);

			if (code.getCode().equals(ResultCode.FAIL)) {
				// 数据库查询出错
				logger.error("Server_User.register:query from database fail");
			} else if (list.size() > 0) { // 已存在
				// 已存在相同账号用户，告诉客户端
				// System.out.println("什么鬼？");
				logger.info("Server_User" + "'RegisterEvent'：User's" + ServerModel.getIoSessionKey(packetFromServer.ioSession)
						+ "  register userID repeated，response Error!");

				responseBuilder.setResultCode(RegisterMsg.RegisterRsp.ResultCode.USER_EXIST);
			} else { // 没问题，可以开始注册
				User user = new User();
				user.setUserId(registerObject.getUserId());
				user.setUserName(registerObject.getUserName());
				user.setUserPassword(registerObject.getUserPassword());

				ResultCode code2 = ResultCode.NULL;
				HibernateDataOperation.add(user, code2);
				if (code2.getCode().equals(ResultCode.SUCCESS)) {
					// 成功，设置回包码
					logger.info("Server_User" + "'RegisterEvent'：User's"
							+ ServerModel.getIoSessionKey(packetFromServer.ioSession)
							+ "  Register Successful，response to Client!");
					responseBuilder.setResultCode(RegisterMsg.RegisterRsp.ResultCode.SUCCESS);
				}
			}

		} catch (InvalidProtocolBufferException e) {
			logger.error("Server_User : 'RegisterEvent'： Error was found when using Protobuf to deserialization "
					+ ServerModel.getIoSessionKey(packetFromServer.ioSession) + "！");
		} catch (IOException e) {
			logger.error("Server_User : 'RegisterEvent'： " + ServerModel.getIoSessionKey(packetFromServer.ioSession) + " 返回包时异常！");
			logger.error(e.getStackTrace());
		} catch (NoIpException e) {
			logger.error(e.getStackTrace());
		}
		// 回复客户端
		serverNetwork.sendToClient(new WaitClientResponse(packetFromServer.ioSession, new PacketFromServer(NetworkPacket
				.getMessageID(packetFromServer.arrayBytes), ProtoHead.ENetworkMessage.REGISTER_RSP_VALUE, responseBuilder.build()
				.toByteArray())));
		// serverNetwork.sendMessageToClient(
		// packetFromServer.ioSession,
		// PacketFromClient.packMessage(ProtoHead.ENetworkMessage.REGISTER_RSP.getNumber(),
		// packetFromServer.getMessageID(),
		// responseBuilder.build().toByteArray()));
	}

	/**
	 * 处理Client的“登陆请求”
	 * 
	 * @param packetFromServer
	 * @author Feng
	 * @throws NoIpException
	 */
	public void login(NetworkPacket networkPacket) throws NoIpException {
		boolean success = false;
		LoginMsg.LoginReq loginObject = null;

		LoginMsg.LoginRsp.Builder loginBuilder = LoginMsg.LoginRsp.newBuilder();
		loginBuilder.setResultCode(LoginRsp.ResultCode.FAIL);
		ClientUser clientUser = serverModel.getClientUserFromTable(networkPacket.ioSession);
		
		try {
			// Debug.log(new String[] { "Server_User", "login" },
			// "Deal with user's" +
			// ServerModel.getIoSessionKey(networkPacket.ioSession) +
			// " 'Login' event");
			logger.info("Server_User : login : Deal with user's" + ServerModel.getIoSessionKey(networkPacket.ioSession)
					+ " 'Login' event");

			loginObject = LoginMsg.LoginReq.parseFrom(networkPacket.getMessageObjectBytes());

			// 查找是否存在同名用户
			ResultCode code = ResultCode.NULL;
			List<User> list = HibernateDataOperation.query("userId", loginObject.getUserId(), User.class, code);
			if (code.getCode() != ResultCode.SUCCESS) {
				logger.error("Server_User.login:query from database fail");
				loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
			} else if (list.size() > 0) { // 已存在
				// 用户存在，开始校验
				User user = (User) list.get(0);
				if (user.getUserPassword().equals(loginObject.getUserPassword())) { // 密码正确
					// Debug.log(new String[] { "Server_User", "login" },
					// "User " +
					// ServerModel.getIoSessionKey(networkPacket.ioSession) +
					// " Login successful!");
					logger.debug("Server_User : login : User " + ServerModel.getIoSessionKey(networkPacket.ioSession)
							+ " Login successful!");

					// 检查是否有重复登陆
					checkAnotherOnline(networkPacket, loginObject.getUserId());

					// 记录到表中
					if (clientUser != null)
						clientUser.userId = loginObject.getUserId();

					// 记录回复位
					loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.SUCCESS);

					success = true;
				} else { // 密码错误
					// Debug.log(new String[] { "Server_User", "login" },
					// "User " +
					// ServerModel.getIoSessionKey(networkPacket.ioSession) +
					// " Login password Error!");
					logger.debug("Server_User : login : User " + ServerModel.getIoSessionKey(networkPacket.ioSession)
							+ " Login password Error!");
					loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
				}
			} else { // 用户不存在
				// Debug.log(new String[] { "Server_User", "login" },
				// "User" + ServerModel.getIoSessionKey(networkPacket.ioSession)
				// +
				// "  UserId not exist!");
				logger.debug("Server_User : login : User" + ServerModel.getIoSessionKey(networkPacket.ioSession)
						+ "  UserId not exist!!");
				loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
			}

		} catch (InvalidProtocolBufferException e) {
			// System.err.println("Server_User : 'LoginEvent'：Error was found when using Protobuf to deserialization "
			// + ServerModel.getIoSessionKey(networkPacket.ioSession) + " ！");
			logger.error("Server_User : 'LoginEvent'：Error was found when using Protobuf to deserialization "
					+ ServerModel.getIoSessionKey(networkPacket.ioSession) + " !");
			loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
			e.printStackTrace();
		} catch (IOException e) {
			// System.err.println("Server_User : 'LoginEvent'： Error was found when response to client"
			// + ServerModel.getIoSessionKey(networkPacket.ioSession) + " ！");
			logger.error("Server_User : 'LoginEvent'： Error was found when response to client"
					+ ServerModel.getIoSessionKey(networkPacket.ioSession) + " ！");
			loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
			e.printStackTrace();
		} catch (NoIpException e) {
			e.printStackTrace();
			loginBuilder.setResultCode(LoginMsg.LoginRsp.ResultCode.FAIL);
		}

		// 回复给客户端
		// Debug.log(new String[] { "Server_User", "login" },
		// "Response User " +
		// ServerModel.getIoSessionKey(networkPacket.ioSession) + " !");
		logger.debug("Server_User : login : Response User " + ServerModel.getIoSessionKey(networkPacket.ioSession) + " !");
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.LOGIN_RSP_VALUE, loginBuilder.build().toByteArray()), null));
		// serverNetwork.sendMessageToClient(networkPacket.ioSession,
		// networkPacket.packMessage(
		// ProtoHead.ENetworkMessage.LOGIN_RSP.getNumber(),
		// networkPacket.getMessageID(), loginBuilder.build()
		// .toByteArray()));

		// 广播“由用户登陆消息"
		if (success) {
			// Debug.log(new String[] { "Server_User", "login" },
			// "Broadcast user" +
			// ServerModel.getIoSessionKey(networkPacket.ioSession) +
			// " Login successful event!");
			logger.debug("Server_User : login : Broadcast user" + ServerModel.getIoSessionKey(networkPacket.ioSession)
					+ " Login successful event!");
			LoginInitAllInfo(clientUser);
			serverModel.setChange();
			serverModel.notifyObservers(new ObserverMessage_Login(networkPacket.ioSession, loginObject.getUserId()));
		}
	}

	/**
	 * 检查是否有另一个同账号的用户登陆，有的话踢下去
	 * 
	 * @param packetFromServer
	 * @return
	 * @throws IOException
	 * @throws NoIpException
	 */
	private boolean checkAnotherOnline(NetworkPacket networkPacket, String userId) throws IOException, NoIpException {
		ClientUser user = serverModel.getClientUserByUserId(userId);
		if (user != null
				&& !ServerModel.getIoSessionKey(networkPacket.ioSession).equals(ServerModel.getIoSessionKey(user.ioSession))) {
			// 发送有他人登陆消息
			OffLineMsg.OffLineSync.Builder offLineMessage = OffLineMsg.OffLineSync.newBuilder();
			offLineMessage.setCauseCode(OffLineMsg.OffLineSync.CauseCode.ANOTHER_LOGIN);
			// byte[] objectBytes = offLineMessage.build().toByteArray();

			try {
				// Debug.log(new String[] { "Server_User", "checkAnotherOnline"
				// }, "User " + user.userId
				// + " has been login at other device，" +
				// ServerModel.getIoSessionKey(user.ioSession)
				// + "will be logout forced！");
				logger.info("Server_User : checkAnotherOnline : User " + user.userId + " has been login at other device，"
						+ ServerModel.getIoSessionKey(user.ioSession) + "will be logout forced！");
			} catch (NoIpException e) {
				// Debug.log(new String[] { "Server_User", "checkAnotherOnline"
				// },
				// "The user has been found which was offline，ignore event!");
				logger.error("Server_User : checkAnotherOnline : The user has been found which was offline，ignore event!");
				return false;
			}
			// 向客户端发送消息
			serverNetwork.sendToClient(new WaitClientResponse(user.ioSession, new PacketFromServer(
					ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE, offLineMessage.build().toByteArray()), null));
			// byte[] messageBytes =
			// networkPacket.packMessage(ProtoHead.ENetworkMessage.OFFLINE_SYNC.getNumber(),
			// objectBytes);
			// serverNetwork.sendMessageToClient(user.ioSession, messageBytes);

			// 添加等待回复
			// serverModel.addClientResponseListener(networkPacket.ioSession,
			// networkPacket.getMessageID(messageBytes),
			// messageBytes, null);

			return true;
		}
		return false;
	}

	/**
	 * 
	 * 另一个人登陆，本用户被踢下的通知的回复
	 * 
	 * @param packetFromServer
	 * @author Feng
	 * @throws NoIpException
	 */
	// public void clientOfflineResponse(networkPacket packetFromServer)
	// throws
	// NoIpException {
	// ClientUser user =
	// serverModel.getClientUserFromTable(packetFromServer.ioSession);
	// Debug.log(new String[] { "Srever_User", "clientOfflineResponse" },
	// "Client " + ServerModel.getIoSessionKey(packetFromServer.ioSession)
	// + " get the 'logoutForcedEvent'，now delete at Server！");
	// // 删掉连接中用户信息表的登陆数据
	// user.userId = null;
	// }

	/**
	 * 处理个人设置请求
	 * 
	 * @param packetFromServer
	 * @author wangfei
	 * @throws NoIpException
	 * @throws
	 * @time 2015-03-21
	 */
	public void personalSettings(NetworkPacket networkPacket) throws NoIpException {
		logger.info("Server_User.personalSettings deal with user:" + ServerModel.getIoSessionKey(networkPacket.ioSession));

		PersonalSettingsMsg.PersonalSettingsRsp.Builder personalSettingsBuilder = PersonalSettingsMsg.PersonalSettingsRsp
				.newBuilder();
		personalSettingsBuilder.setResultCode(PersonalSettingsRsp.ResultCode.FAIL);
		try {
			PersonalSettingsMsg.PersonalSettingsReq personalSettingsObject = PersonalSettingsMsg.PersonalSettingsReq
					.parseFrom(networkPacket.getMessageObjectBytes());

			ClientUser clientUser = serverModel.getClientUserFromTable(networkPacket.ioSession);
			ResultCode code = ResultCode.NULL;
//			System.out.println("-----------------------------" + clientUser.userId);
			List list = HibernateDataOperation.query("userId", clientUser.userId, User.class, code);
			if (code.getCode().equals(ResultCode.SUCCESS) && list.size() > 0) {
				User user = (User) list.get(0);
				// 修改昵称
				if (personalSettingsObject.getUserName() != null && personalSettingsObject.getUserName() != "") {
					changeUserName(personalSettingsBuilder, networkPacket, user, personalSettingsObject.getUserName());
				}
				// 修改密码
				if (personalSettingsObject.getUserPassword() != null && personalSettingsObject.getUserPassword() != "") {
					changeUserPassword(personalSettingsBuilder, networkPacket, clientUser, user,
							personalSettingsObject.getUserPassword());
				}
				// 修改头像
				if (personalSettingsObject.getHeadIndex() >= 1 && personalSettingsObject.getHeadIndex() <= 6) {
					changeHeadIndex(personalSettingsBuilder, networkPacket, clientUser, user,
							personalSettingsObject.getHeadIndex());
				}
			} else if (code.getCode().equals(ResultCode.FAIL)) {
				// Hibernate数据库处理出错
				logger.error("Server_User.personalSettings: Hibernate error");
				personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			} else if (list.size() < 1) {
				// 用户不存在
				logger.info("Server_User.personalSettings:User:" + ServerModel.getIoSessionKey(networkPacket.ioSession)
						+ " not exist!");
				personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			}

		} catch (InvalidProtocolBufferException e) {
			logger.error("Server_User.personalSettings:Error was found when using Protobuf to deserialization "
					+ ServerModel.getIoSessionKey(networkPacket.ioSession) + " packet！");
			logger.error(e.getStackTrace());

			personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
		}
		// 回复客户端
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP_VALUE, personalSettingsBuilder.build()
				.toByteArray())));
		// try {
		// // 回复客户端
		// serverNetwork.sendMessageToClient(
		// networkPacket.ioSession,
		// networkPacket.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP.getNumber(),
		// networkPacket.getMessageID(),
		// personalSettingsBuilder.build().toByteArray()));
		// } catch (IOException e) {
		// // 回复客户端出错
		// logger.error("Server_User.personalSettings deal with user:" +
		// ServerModel.getIoSessionKey(networkPacket.ioSession)
		// + " Send result Fail!");
		// logger.error(e.getStackTrace());
		// }
	}

	/**
	 * 修改用户昵称
	 * 
	 * @param builder
	 * @param packetFromServer
	 * @param u
	 * @param userName
	 * @author wangfei
	 */
	private void changeUserName(PersonalSettingsMsg.PersonalSettingsRsp.Builder builder, NetworkPacket packetFromServer, User u,
			String userName) {
		logger.info("Server_User.changeUserName:begin to change User:" + u.getUserId() + " userName to " + userName);
		ResultCode code = ResultCode.NULL;
		u.setUserName(userName);
		// Hibernate更新数据库
		HibernateDataOperation.update(u, code);
		if (code.getCode().equals(ResultCode.SUCCESS))
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
		else if (code.getCode().equals(ResultCode.FAIL))
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
	}

	/**
	 * 修改用户密码
	 * 
	 * @param builder
	 * @param packetFromServer
	 * @param clientUser
	 * @param u
	 * @param userPassword
	 * @author WangFei
	 * @throws NoIpException
	 */
	private void changeUserPassword(PersonalSettingsMsg.PersonalSettingsRsp.Builder builder, NetworkPacket networkPacket,
			ClientUser clientUser, User u, String userPassword) throws NoIpException {
		logger.info("Server_User.changeUserPassword:begin to change User:" + u.getUserId() + " userPassword to " + userPassword);
		ResultCode code = ResultCode.NULL;
		u.setUserPassword(userPassword);
		HibernateDataOperation.update(u, code);
		if (code.getCode().equals(ResultCode.SUCCESS)) {
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
			// 向客户端发送消息 更改密码后客户端需要下线重新登录
			OffLineMsg.OffLineSync.Builder offLineMessage = OffLineMsg.OffLineSync.newBuilder();
			offLineMessage.setCauseCode(OffLineMsg.OffLineSync.CauseCode.CHANGE_PASSWORD);
			byte[] objectBytes = offLineMessage.build().toByteArray();
			byte[] messageBytes = null;
			try {
				messageBytes = NetworkPacket.packMessage(ProtoHead.ENetworkMessage.OFFLINE_SYNC.getNumber(), objectBytes);
			} catch (IOException e) {
				logger.error("Server_User.personalSettings deal with user:"
						+ ServerModel.getIoSessionKey(networkPacket.ioSession) + " Send sync Fail!");
				logger.error(e.getStackTrace());
			}
			clientUser.userId = null;

			// 回复客户端
			serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
					.getMessageID(), ProtoHead.ENetworkMessage.OFFLINE_SYNC_VALUE, objectBytes)));
			// serverNetwork.sendMessageToClient(clientUser.ioSession,
			// messageBytes);

			// 添加等待回复
			// serverModel.addClientResponseListener(packetFromServer.ioSession,
			// networkPacket.getMessageID(messageBytes),
			// messageBytes, null);
		} else if (code.getCode().equals(ResultCode.FAIL)) {
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
		}
	}

	/**
	 * 修改用户头像
	 * 
	 * @param builder
	 * @param packetFromServer
	 * @param clientUser
	 * @param u
	 * @param headInx
	 * @author WangFei
	 */
	private void changeHeadIndex(PersonalSettingsMsg.PersonalSettingsRsp.Builder builder, NetworkPacket networkPacket,
			ClientUser clientUser, User u, int headIndex) {
		logger.info("Server_User.changeUserHeadIndex:begin to change User:" + u.getUserId() + " userHeadIndex to " + headIndex);
		BufferedImage image = null;
		ResultCode code = ResultCode.NULL;
		u.setHeadIndex(headIndex);
		HibernateDataOperation.update(u, code);
		if (code.getCode().equals(ResultCode.SUCCESS)) {
			// 从默认头像文件夹获取图片
			image = GetImage.getImage(headIndex + ".png");
			File file = new File(ResourcePath.getHeadPath());
			// 检查保存头像的文件夹是否存在
			if (!file.exists() && !file.isDirectory()) {
				// 如果不存在 则创建文件夹
				file.mkdir();
			}
			// 保存获取的默认头像到头像文件夹
			File saveFile = new File(ResourcePath.getHeadPath() + clientUser.userId + ".png");
			try {
				ImageIO.write(image, "png", saveFile);
				builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
			} catch (IOException e) {
				logger.error("Server_User.changeHeadIndex:save head image to " + saveFile.getAbsolutePath() + " fail");
				logger.error(e.getStackTrace());
				builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			}
		} else if (code.getCode().equals(ResultCode.FAIL)) {
			builder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
		}
	}

	/**
	 * 用户退出登录
	 * 
	 * @param packetFromServer
	 * @author wangfei
	 * @time 2015-03-25
	 * @author WangFei
	 */
	public void logout(NetworkPacket networkPacket) {
		// try {
		ClientUser user = null;
		LogoutMsg.LogoutRsp.Builder logoutBuilder = LogoutMsg.LogoutRsp.newBuilder();
		logoutBuilder.setResultCode(LogoutMsg.LogoutRsp.ResultCode.FAIL);
		try {
			user = serverModel.getClientUserFromTable(networkPacket.ioSession);
			logger.info("Srever_User.logout:" + ServerModel.getIoSessionKey(networkPacket.ioSession) + " logout!");
			// 将登录的用户注销掉
			user.userId = null;
			logoutBuilder.setResultCode(LogoutMsg.LogoutRsp.ResultCode.SUCCESS);
		} catch (NoIpException e) {
			logoutBuilder.setResultCode(LogoutMsg.LogoutRsp.ResultCode.FAIL);
			logger.info("Srever_User.logout:fail to logout");
			logger.info(e.getStackTrace());
		}
		// 回复客户端
		serverNetwork.sendToClient(new WaitClientResponse(networkPacket.ioSession, new PacketFromServer(networkPacket
				.getMessageID(), ProtoHead.ENetworkMessage.LOGOUT_RSP_VALUE, logoutBuilder.build().toByteArray())));
		// serverNetwork.sendMessageToClient(
		// packetFromServer.ioSession,
		// networkPacket.packMessage(ProtoHead.ENetworkMessage.LOGOUT_RSP.getNumber(),
		// packetFromServer.getMessageID(),
		// logoutBuilder.build().toByteArray()));
		// } catch (IOException e) {
		// logger.error("Server_User.logout:Send result Fail!");
		// logger.error(e.getStackTrace());
		// }

	}

	/**
	 * 获取个人信息 包括基本信息和好友列表
	 * 
	 * @param packetFromServer
	 * @author WangFei
	 * @throws NoIpException
	 */
	public void getPersonalInfo(NetworkPacket packetFromServer) throws NoIpException {
		logger.info("Server_User.getPersonalInfo:");
		GetPersonalInfoMsg.GetPersonalInfoRsp.Builder getPersonalInfoBuilder = GetPersonalInfoMsg.GetPersonalInfoRsp.newBuilder();
		getPersonalInfoBuilder.setResultCode(GetPersonalInfoRsp.ResultCode.FAIL);
		Session session = HibernateSessionFactory.getSession();
		try {
//
			GetPersonalInfoMsg.GetPersonalInfoReq getPersonalInfoObject = GetPersonalInfoMsg.GetPersonalInfoReq
					.parseFrom(packetFromServer.getMessageObjectBytes());

			ClientUser user = serverModel.getClientUserFromTable(packetFromServer.ioSession);

			ResultCode code = ResultCode.NULL;
			List list = HibernateDataOperation.query("userId", user.userId, User.class, code, session);
			if (code.getCode().equals(ResultCode.SUCCESS) && list.size() > 0) {
				// 不支持模糊搜索 所以如果有搜索结果 只可能有一个结果
				User u = (User) list.get(0);
				getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.SUCCESS);
				// 获取用户的基本信息
				if (getPersonalInfoObject.getUserInfo() == true) {
					UserItem.Builder userItemBuilder = UserItem.newBuilder();
					userItemBuilder.setUserId(u.getUserId());
					userItemBuilder.setUserName(u.getUserName());
					userItemBuilder.setHeadIndex(u.getHeadIndex());
					getPersonalInfoBuilder.setUserInfo(userItemBuilder);
				}
				// 获取用户的好友信息
				if (getPersonalInfoObject.getFriendInfo() == true) {
					for (User ui : u.getFriends()) {
						UserItem.Builder userItemBuilder2 = UserItem.newBuilder();
						userItemBuilder2.setUserId(ui.getUserId());
						userItemBuilder2.setUserName(ui.getUserName());
						userItemBuilder2.setHeadIndex(ui.getHeadIndex());
						getPersonalInfoBuilder.addFriends(userItemBuilder2);
					}
				}
				getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.SUCCESS);
			} else if (code.getCode().equals(ResultCode.FAIL)) {
				logger.error("Server_User.getPersonalInfo: Hibernate error");
				getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.FAIL);
			} else if (list.size() < 1) {
				logger.info("Server_User.getPersonalInfo:User:" + ServerModel.getIoSessionKey(packetFromServer.ioSession)
						+ " not exist!");
				getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.FAIL);
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("Server_User.getPersonalInfo:Error was found when using Protobuf to deserialization "
					+ ServerModel.getIoSessionKey(packetFromServer.ioSession) + " packet！");
			logger.error(e.getStackTrace());
			getPersonalInfoBuilder.setResultCode(GetPersonalInfoMsg.GetPersonalInfoRsp.ResultCode.FAIL);
		}
		// 回复客户端
		serverNetwork.sendToClient(new WaitClientResponse(packetFromServer.ioSession, new PacketFromServer(packetFromServer
				.getMessageID(), ProtoHead.ENetworkMessage.GET_PERSONALINFO_RSP_VALUE, getPersonalInfoBuilder.build()
				.toByteArray())));
	}

	/**
	 * 获取User对象
	 * 
	 * @author Feng
	 */
	public User getUser(String userId, Session session) {
		ResultCode resultCode = ResultCode.NULL;
		List<User> userList = HibernateDataOperation.query(User.HQL_USER_ID, userId, User.class, resultCode, session);
		if (resultCode.getCode() == resultCode.SUCCESS && userList.size() > 0)
			return userList.get(0);
		return null;
	}

	/**
	 * 获取User对象列表
	 * 
	 * @author Feng
	 */
	public List<User> getUsers(List<String> userIds, Session session) {
		String[] userList = new String[userIds.size()];
		return getUsers(userIds.toArray(userList), session);
	}

	public List<User> getUsers(String[] userIds, Session session) {
		StringBuffer hqlSB = new StringBuffer("from " + User.class.getSimpleName() + " where " + User.HQL_USER_ID + " in(");
		for (String userId : userIds)
			hqlSB.append("'" + userId + "',");
		// String hql = hqlSB.substring(0, hqlSB.length()-1) + ")";
		return session.createQuery(hqlSB.substring(0, hqlSB.length() - 1) + ")").list();
	}
	
	/**
	 * 客户端登录成功后 发送用户所有信息--个人信息、好友信息、聊天群信息
	 * @time 2015-04-17
	 *@author wangfei 
	 */
	private void LoginInitAllInfo(ClientUser user)throws NoIpException{
		logger.info("Server_User.LoginInitAllInfo:");
		LoginInitAllInfoMsg.LoginInitAllInfoSync.Builder loginInitAllInfoBuilder = LoginInitAllInfoMsg.LoginInitAllInfoSync.newBuilder();
		Session session = HibernateSessionFactory.sessionFactory.openSession();
		ResultCode code = ResultCode.NULL;
		List list = HibernateDataOperation.query("userId", user.userId, User.class, code, session);
		if (code.getCode().equals(ResultCode.SUCCESS) && list.size() > 0) {
			// 不支持模糊搜索 所以如果有搜索结果 只可能有一个结果
			User u = (User) list.get(0);
			// 获取用户的基本信息
			loginInitAllInfoBuilder.setUserInfo(userToUserItem(u));
			// 获取用户的好友信息
			for (User ui : u.getFriends()) {
				loginInitAllInfoBuilder.addFriends(userToUserItem(ui));
			}
			//获取用户所加入群的群信息 包括所有群成员
			List groupList = u.getGroups();
			for(int i=0;i<groupList.size();i++){
				Group group =(Group) groupList.get(i);  //转型为数组
				GroupItem.Builder groupItemBuilder = GroupItem.newBuilder();
				groupItemBuilder.setGroupId(Integer.toString(group.getGroupId()));
				groupItemBuilder.setGroupName(group.getGroupName());
				groupItemBuilder.setCreater(userToUserItem(getUser(group.getCreaterId())));
				for(User us:group.getMemberList()){
					groupItemBuilder.addMemberUser(userToUserItem(us));
				}
				loginInitAllInfoBuilder.addGroups(groupItemBuilder);
			}
		} else if (code.getCode().equals(ResultCode.FAIL)) {
			logger.error("Server_User.LoginInitAllInfo: Hibernate error");
		} else if (list.size() < 1) {
			logger.info("Server_User.LoginInitAllInfo:User not exist!");
		}
		session.close();
		// 向客户端发送消息
		serverNetwork.sendToClient(new WaitClientResponse(user.ioSession, new PacketFromServer(
				ProtoHead.ENetworkMessage.LOGIN_INIT_ALL_INFO_SYNC_VALUE, loginInitAllInfoBuilder.build().toByteArray()), null));
		
	}
	
	private User getUser(String userId){
		ResultCode code = ResultCode.NULL;
		List list = HibernateDataOperation.query("userId", userId, User.class,code);
		if(code.getCode().equals(ResultCode.SUCCESS) && list.size()>0)
			return (User)list.get(0);
		return null;
	}
	private UserItem.Builder userToUserItem(User user){
		UserItem.Builder userItem = UserItem.newBuilder();
		userItem.setUserId(user.getUserId());
		userItem.setUserName(user.getUserName());
		userItem.setHeadIndex(user.getHeadIndex());
		
		return userItem;
	}
	 
}
