package server;

/**
 * 服务器向客户端发消息，等待客户端回复，在客户端不存在，准备删除前的回调
 * @author Feng
 *
 */
public interface WaitClientResponseCallBack {

	void beforeDelete();
}
