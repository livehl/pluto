package cn.city.in.task.manager.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import cn.city.in.api.tools.common.ExceptionTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.api.tools.objectpool.ObjectPool;
import cn.city.in.task.manager.http.codec.HttpRequestMessage;
import cn.city.in.task.manager.http.codec.HttpResponseMessage;
import cn.city.in.task.manager.http.comment.HttpParam;
import cn.city.in.task.manager.http.controller.BaseController;

/**
 * 功能:连接线程，处理每个客户端的请求
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class HttpHandler extends IoHandlerAdapter {
	private static Logger log = Logger.getLogger(HttpHandler.class);

	private Map<String, Method> uriBind;

	public HttpHandler(Map<String, Method> uriBind) {
		super();
		this.uriBind = uriBind;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable e)
			throws Exception {
		log.debug(
				"client " + session.getRemoteAddress() + " on error"
						+ e.getMessage(), e);
//		session.close(true);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		// Check that we can service the request context

		HttpRequestMessage request = (HttpRequestMessage) message;
		String actin = request.getAction();
		actin = actin.substring(1);
		HttpResponseMessage response = new HttpResponseMessage();
		response.setContentType("text/html; charset=UTF-8");
		response.setResponseCode(HttpResponseMessage.HTTP_STATUS_SUCCESS);
		Method m = null;
		if (uriBind.containsKey(actin)) {
			m = uriBind.get(actin);
		} else {
			// 模糊匹配,数组匹配
			Set<String> keys = uriBind.keySet();
			for (String uriMode : keys) {
				if (StringTool.isArray(uriMode)) {
					String[] rules = StringTool.getArray(uriMode);
					for (String rule : rules) {
						if (rule.equals(actin)) {
							m = uriBind.get(uriMode);
							break;
						} else if (rule.contains("*")
								&& StringTool.matche(actin, rule)) {
							m = uriBind.get(uriMode);
							break;
						}
					}
				} else {
					if (uriMode.contains("*")) {
						if (StringTool.matche(actin, uriMode)) {
							m = uriBind.get(uriMode);
							break;
						}
					}
				}
			}
		}
		if (null != m) {
			try {
				// 注解参数注入
				Annotation[][] an = m.getParameterAnnotations();
				Object[] params = new Object[an.length];
				if (an.length > 0) {
					for (int i = 0; i < an.length; i++) {
						for (int j = 0; j < an[i].length; j++) {
							HttpParam t = (HttpParam) an[i][j];
							params[i] = request.getData(t.value());
							if (params[i] == null) {
								if (t.required()) {
									throw new Exception("need param :"
											+ t.value());
								}
								params[i] = t.defaultValue();
							} else {
								// 参数类型转换
								Class paramType = m.getParameterTypes()[i];
								if (paramType.isAssignableFrom(Integer.class)) {
									params[i] = Integer
											.valueOf((String) params[i]);
								} else if (paramType
										.isAssignableFrom(Long.class)) {
									params[i] = Long
											.valueOf((String) params[i]);
								} else if (paramType
										.isAssignableFrom(Double.class)) {
									params[i] = Double
											.valueOf((String) params[i]);
								} else if (paramType
										.isAssignableFrom(Float.class)) {
									params[i] = Float
											.valueOf((String) params[i]);
								} else if (paramType
										.isAssignableFrom(int.class)) {
									params[i] = Integer.valueOf(
											(String) params[i]).intValue();
								} else if (paramType
										.isAssignableFrom(long.class)) {
									params[i] = Long
											.valueOf((String) params[i])
											.longValue();
								} else if (paramType
										.isAssignableFrom(double.class)) {
									params[i] = Double.valueOf(
											(String) params[i]).doubleValue();
								} else if (paramType
										.isAssignableFrom(float.class)) {
									params[i] = Float.valueOf(
											(String) params[i]).floatValue();
								}
							}
						}
					}
				}
				// 对象池获取对象
				Object controller = ObjectPool.borrowObject(m
						.getDeclaringClass());
				if (controller instanceof BaseController) {
					((BaseController) controller).setContext(request, response);
					((BaseController) controller).init();
				}
				Object result = null;
				try {
					result = m.invoke(controller, params);
					if (controller instanceof BaseController) {
						result = ((BaseController) controller).doAfter(result);
					}
				} catch (Exception e) {
					response.error(e);
				}
				ObjectPool.returnObject(controller);
				if (StringTool.isNotNull(result)) {
					if (result instanceof String) {
						response.appendBody((String) result);
					} else if (result instanceof byte[]) {
						response.appendBody((byte[]) result);
					} else {
						response.appendBody(result.toString());
					}
				}
			} catch (Exception e) {
				ExceptionTool.cutStackTrace(e, "cn.city.in", true);
				e.printStackTrace();
				response.setResponseCode(HttpResponseMessage.HTTP_STATUS_ERROR);
				if (StringTool.isNotNull(e.getMessage())) {
					response.appendBody(e.getMessage());
					Throwable el = e.getCause();
					while (StringTool.isNotNull(el)) {
						if (StringTool.isNotNull(el.getMessage())) {
							response.appendBody(el.getMessage());
						}
						el = el.getCause();
					}
				}
			}
		} else {
			response.notFound();
		}
		session.write(response).addListener(IoFutureListener.CLOSE);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		log.warn("client from" + session.getRemoteAddress() + " idle");
	}
}
