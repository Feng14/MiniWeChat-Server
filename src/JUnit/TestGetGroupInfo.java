package JUnit;

import static org.junit.Assert.*;

import java.io.IOException;

import model.Group;

import org.junit.After;
import org.junit.Test;

import protocol.ProtoHead;
import protocol.Msg.GetGroupInfoMsg.GetGroupInfoReq;
import protocol.Msg.GetGroupInfoMsg.GetGroupInfoRsp;
import server.NetworkPacket;

/**
 * 测试获取群资料功能
 * 
 * @author Administrator
 * 
 */
public class TestGetGroupInfo {

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		String groupId = "13";
		ClientSocket clientSocket = new ClientSocket();

		GetGroupInfoReq.Builder requestBuilder = GetGroupInfoReq.newBuilder();
		requestBuilder.setGroupId(groupId);

		clientSocket.writeToServer(NetworkPacket.packMessage(ProtoHead.ENetworkMessage.GET_GROUP_INFO_REQ_VALUE, requestBuilder
				.build().toByteArray()));
		byte[] byteArray = clientSocket.readFromServerWithoutKeepAlive(ProtoHead.ENetworkMessage.GET_GROUP_INFO_RSP);
		GetGroupInfoRsp getGroupInfoRsp = GetGroupInfoRsp.parseFrom(NetworkPacket.getMessageObjectBytes(byteArray));
		
		assertEquals(getGroupInfoRsp.getResultCode(), GetGroupInfoRsp.ResultCode.SUCCESS);
		assertEquals(getGroupInfoRsp.getGroupItem().getGroupId(), groupId);
		assertEquals(getGroupInfoRsp.getGroupItem().getCreaterUserId(), "a");
		assertEquals(getGroupInfoRsp.getGroupItem().getGroupName(), "a,b,c,...");
		System.out.println("Member Count : " + getGroupInfoRsp.getGroupItem().getMemberUserIdCount());
	}

}
