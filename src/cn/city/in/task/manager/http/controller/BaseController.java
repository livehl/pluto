package cn.city.in.task.manager.http.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.api.tools.common.Tool;
import cn.city.in.task.manager.http.codec.HttpRequestMessage;
import cn.city.in.task.manager.http.codec.HttpResponseMessage;

/**
 * 功能:基础action,提供公共操作
 * 
 * @author 黄林 2012-3-2
 * @version
 */
public class BaseController {
	protected static Log log = LogFactory.getLog(Tool.class);
	private static final ThreadLocal<HttpRequestMessage> threadRequest = new ThreadLocal<HttpRequestMessage>();
	private static final ThreadLocal<HttpResponseMessage> threadResponse = new ThreadLocal<HttpResponseMessage>();

	/**
	 * 设置ajax消息体
	 * 
	 * @param message
	 *            the message
	 * @param data
	 *            the data
	 * @return the object node
	 * @author 黄林
	 */
	public ObjectNode createAjaxMessage() {
		ObjectNode root = new ObjectNode(JsonNodeFactory.instance);
		root.put("message", "ok");
		return root;
	}

	public String cutUri(String uri) {
		int lastIndex = uri.lastIndexOf("/");
		String last = uri.substring(lastIndex);
		uri = uri.substring(0, lastIndex);
		String middle = uri.substring(uri.lastIndexOf("/"));
		if (last.indexOf("?") != -1) {
			last = last.substring(0, last.indexOf("?"));
		}
		return middle + last;
	}

	/**
	 * 后置控制处理方法
	 * 
	 * @param body
	 *            the body
	 * @return the object
	 * @author 黄林
	 */
	public Object doAfter(Object body) throws Exception {
		return body;
	}

	public String getIP() {
		String ip;
		if (getRequest().getHeader("x-forwarded-for") == null) {
			ip = getRequest().getRemoteAddr();
		} else {
			ip = getRequest().getHeader("x-forwarded-for")[0];
		}
		if (null != ip && ip.indexOf(",") != -1) {
			ip = ip.split(",")[0];
		}
		return ip;
	}

	public HttpRequestMessage getRequest() {
		return threadRequest.get();
	}

	/**
	 * 获取请求信息
	 * 
	 * @return the request info
	 * @author 黄林
	 */
	public String getRequestInfo() {
		StringBuffer sb = new StringBuffer();
		HttpRequestMessage request = getRequest();
		if (StringTool.isNull(request)) {
			return "no request";
		}
		sb.append("request:" + request.getUri() + "\r\n");
		sb.append("head:\r\n");
		Iterator<String> e = request.getHeaders().entrySet().iterator();
		while (e.hasNext()) {
			String key = e.next();
			sb.append(key + ":" + request.getHeader(key) + "\r\n");
		}
		sb.append("method:" + request.getMethod() + " \r\n");
		return sb.toString();
	}

	public HttpResponseMessage getResponse() {
		return threadResponse.get();
	}

	/**
	 * 初始化,每个方法执行前均会执行,用于处理安全验证
	 * 
	 * @throws Exception
	 */
	public boolean init() throws Exception {
		// HttpRequestMessage request = getRequest();
		// System.out.println(request.getRequestURI()+":"+getUid());
		// Enumeration<String> e=request.getHeaderNames();
		// while (e.hasMoreElements()) {
		// String key=e.nextElement();
		// System.out.println(key+":"+request.getHeader(key));
		// // log.debug(key+":"+request.getHeader(key));
		// }

		return true;
	}

	/**
	 * 设置分页
	 * 
	 * @param list
	 *            the list
	 * @param allcount
	 *            the allcount
	 * @throws Exception
	 *             the exception
	 */
	public void putPage(Map<String, Object> model, ArrayList<?> list,
			int allcount) throws Exception {
		HashMap<String, Object> pageMap = new HashMap<String, Object>();
		pageMap.put("page_data", list);
		HashMap<String, Object> pageInfoMap = new HashMap<String, Object>();
		pageInfoMap.put("total_count", allcount);
		pageMap.put("page_info", pageInfoMap);
		model.put("result_list", pageMap);
	}

	/**
	 * 设置分页.
	 * 
	 * @param model
	 *            the model
	 * @param list
	 *            the list
	 * @param allcount
	 *            the allcount
	 * @param isInclued
	 *            the is inclued
	 * @param fileds
	 *            the filed filter
	 * @throws Exception
	 *             the exception
	 */
	public void putPageWithFilter(Map<String, Object> model, ArrayList<?> list,
			int allcount, boolean isInclued, String... fileds) throws Exception {
		putPage(model, JsonTool.listToMapWithFiledFilter(list, isInclued,
				false, fileds), allcount);
	}

	/**
	 * 功能:设置请求上下文 创建者： 黄林 2012-3-2.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 */
	public void setContext(HttpRequestMessage request,
			HttpResponseMessage response) {
		threadResponse.set(response);
		threadRequest.set(request);
	}

	/**
	 * 向客户端输出内容,
	 * 
	 * @param str
	 *            the str
	 * @throws IOException
	 */
	public void write(String str) throws IOException {
		getResponse().write(str);
	}

}
