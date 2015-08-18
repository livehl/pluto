package cn.city.in.api.tools.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import cn.city.in.api.tools.common.PropertyTool;

/**
 * 静态类，提供服务器
 * 
 * @author 黄林
 * 
 */
public class SocketService {
	private static Logger log = Logger.getLogger(SocketService.class);
	private static IoAcceptor acceptor;

	/**
	 * 功能:初始化服务器，监听端口 创建者： 黄林 2011-11-4.
	 */
	public static void init() {
		int port = PropertyTool.getNumProperties("socket_bind").intValue();
		try {
			acceptor = new NioSocketAcceptor();
			acceptor.getFilterChain().addLast(
					"codec",
					new ProtocolCodecFilter(new TextLineCodecFactory(Charset
							.forName("utf-8"))));
			acceptor.setHandler(new ClientHandler());
			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 5);
			((SocketSessionConfig) acceptor.getSessionConfig()).setSoLinger(0);
			acceptor.bind(new InetSocketAddress(port));
			log.info("start socket service ok");
		} catch (IOException e) {
			log.warn("open port:" + port + " faile:" + e.getMessage(), e);
		}
	}

	/**
	 * 服务器停机
	 */
	public static void shutdown() {
		if (null != acceptor) {
			Collection<IoSession> sessions = acceptor.getManagedSessions()
					.values();
			for (IoSession ioSession : sessions) {
				ioSession.close(false);
			}
			acceptor.unbind();
			acceptor.dispose();
		}
	}
}
