package server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import model.HibernateSessionFactory;
import model.User;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.google.protobuf.InvalidProtocolBufferException;

import exception.NoIpException;

import protocol.ProtoHead;
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
		if (user != null && !user.die) {
			// 发送由他人登陆消息
			OffLineMsg.OffLineReq.Builder offLineMessage = OffLineMsg.OffLineReq.newBuilder();
			offLineMessage.setCauseCode(OffLineMsg.OffLineReq.CauseCode.ANOTHER_LOGIN);
			byte[] objectBytes = offLineMessage.build().toByteArray();

			try {
				Debug.log(new String[] { "Server_User", "checkAnotherOnline" },
						"用户 " + user.userId + "在其他设备登陆，" + ServerModel.getIoSessionKey(user.ioSession) + "被踢下线！");
			} catch (NoIpException e) {
				Debug.log(new String[] { "Server_User", "checkAnotherOnline" }, "找到的用户已断线，不做处理！");
				return false;
			}
			// 向客户端发送消息
			byte[] messageBytes = NetworkMessage.packMessage(ProtoHead.ENetworkMessage.OFFLINE_REQ.getNumber(), objectBytes);
			ServerNetwork.instance.sendMessageToClient(user.ioSession, messageBytes);

			// 添加等待回复
			ServerModel.instance.addClientResponseListener(networkMessage.ioSession, NetworkMessage.getMessageID(messageBytes),
					messageBytes);

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
		user.userId = null;
		user.die = true;
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

			Session session = HibernateSessionFactory.getSession();
			Criteria criteria = session.createCriteria(User.class);

			ClientUser clientUser = ServerModel.instance.getClientUserFromTable(networkMessage.ioSession);

			// ClientUser clientUser =
			// ServerModel.instance.getClientUserFromTable(networkMessage.ioSession.getRemoteAddress().toString());
			System.out.println("get userId that have been login:" + clientUser.userId);

			criteria.add(Restrictions.eq("userId", clientUser.userId));
			if (criteria.list().size() > 0) {
				User user = (User) criteria.list().get(0);
				// 修改昵称
				if (personalSettingsObject.getUserName() != null && personalSettingsObject.getUserName() != "") {
					user.setUserName(personalSettingsObject.getUserName());
				}
				// 修改密码
				if (personalSettingsObject.getUserPassword() != null && personalSettingsObject.getUserPassword() != "") {
					user.setUserPassword(personalSettingsObject.getUserPassword());
				}

				// 修改头像
				if (personalSettingsObject.getHeadIndex() >= 1 && personalSettingsObject.getHeadIndex() <= 6) {
					BufferedImage image = null;
					user.setHeadIndex(personalSettingsObject.getHeadIndex());
					try {
						// 从默认头像文件夹获取图片
						image = ImageIO.read(new File(ResourcePath.headDefaultPath + personalSettingsObject.getHeadIndex()
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
						personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
					} catch (IOException e) {
						System.err.println("保存头像图片失败");
						personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
						e.printStackTrace();
					}
				}

				// 数据库修改昵称或密码
				try {
					Transaction trans = session.beginTransaction();
					session.update(user);
					trans.commit();
					personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.SUCCESS);
				} catch (Exception e) {
					personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
					e.printStackTrace();
				}

			} else {
				// 用户不存在
				Debug.log(new String[] { "Server_User", "personalSettings" },
						"用户" + ServerModel.getIoSessionKey(networkMessage.ioSession) + "  的用户不存在!");
				personalSettingsBuilder.setResultCode(PersonalSettingsMsg.PersonalSettingsRsp.ResultCode.FAIL);
			}
			session.close();

			// 回复客户端
			ServerNetwork.instance.sendMessageToClient(
					networkMessage.ioSession,
					NetworkMessage.packMessage(ProtoHead.ENetworkMessage.PERSONALSETTINGS_RSP.getNumber(),
							networkMessage.getMessageID(), personalSettingsBuilder.build().toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			System.err.println("Server_User : 个人设置事件： 用Protobuf反序列化 " + ServerModel.getIoSessionKey(networkMessage.ioSession)
					+ " 的包时异常！");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Server_User : 个人设置事件： " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " 返回包时异常！");
			e.printStackTrace();
		} catch (NoIpException e) {
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
						"客户端 " + ServerModel.getIoSessionKey(networkMessage.ioSession) + " 退出登录，将其设为死亡！");
			} catch (NoIpException e) {
				
				e.printStackTrace();
			}

			user.userId = null;
			user.die = true;
			logoutBuilder.setResultCode(LogoutMsg.LogoutRsp.ResultCode.SUCCESS);

			ServerNetwork.instance.sendMessageToClient(networkMessage.ioSession, NetworkMessage.packMessage(
					ProtoHead.ENetworkMessage.LOGOUT_RSP.getNumber(), networkMessage.getMessageID(), logoutBuilder.build()
							.toByteArray()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
