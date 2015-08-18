package cn.city.in.task.manager.http;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import cn.city.in.api.tools.common.ClassTool;
import cn.city.in.api.tools.objectpool.ObjectPool;
import cn.city.in.task.manager.http.codec.HttpServerProtocolCodecFactory;
import cn.city.in.task.manager.http.comment.HttpComment;

public class HttpManager {
	private static NioSocketAcceptor acceptor;
	public static final long startTime = System.currentTimeMillis();

	/**
	 * @param prot
	 * @throws IOException
	 */
	public static void init(int prot, int timeOut, int thread,
			String packageName) throws Exception {
		if (acceptor != null) {
			throw new Exception(" service has runing");
		}

		acceptor = new NioSocketAcceptor(thread);
		acceptor.getFilterChain().addLast("protocolFilter",
				new ProtocolCodecFilter(new HttpServerProtocolCodecFactory()));
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, timeOut);
		// acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		// 注解搜索
		HttpHandler handler = new HttpHandler(scanUrlBind(packageName));
		acceptor.setHandler(handler);
		acceptor.bind(new InetSocketAddress(prot));
		System.out.println("Start on " + prot + " ok");
	}

	/**
	 * url action 绑定
	 * 
	 * @param packageName
	 *            the package name
	 * @return the map
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	private static Map<String, Method> scanUrlBind(String packageName)
			throws Exception {
		List<Class> allClass = ClassTool.getClassFromPackage(packageName);
		Map<String, Method> allBind = new HashMap<String, Method>();
		for (Class clazz : allClass) {
			Method[] allMethods = clazz.getMethods();
			boolean hasMethod = false;
			for (Method method : allMethods) {
				if (method.isAnnotationPresent(HttpComment.class)) {
					HttpComment hc = method.getAnnotation(HttpComment.class);
					if (allBind.containsKey(hc.uri())) {
						throw new RuntimeException("uri has bind:" + hc.uri());
					}
					allBind.put(hc.uri(), method);
					hasMethod = true;
					System.out.println(hc.uri() + "--->"
							+ clazz.getSimpleName() + "." + method.getName());
				}
			}
			if (hasMethod) {
				// 将指定的类放入对象池
				ObjectPool.initObject(clazz, null);
			}
		}
		return allBind;
	}

	/**
	 * 关服
	 * 
	 * @author 黄林 Shutdown.
	 */
	public static void shutdown() {
		if (null != acceptor) {
			Collection<IoSession> allSessions = acceptor.getManagedSessions()
					.values();
			for (IoSession ioSession : allSessions) {
				try {
					ioSession.close(false);
				} catch (Exception e) {
				}
			}
			if (null != acceptor) {
				acceptor.unbind();
				acceptor.dispose();
				acceptor = null;
			}
		}
	}
}
