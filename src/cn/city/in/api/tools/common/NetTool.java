package cn.city.in.api.tools.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import cn.city.in.api.tools.ipseeker.IPSeeker;
import cn.city.in.api.tools.ipseeker.IPSeeker.IPLocation;
import cn.city.in.api.tools.task.TaskTool;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

/**
 * 功能:负责与网络相关的部分(http,memcache).
 * 
 * @author 黄林 2011-7-26
 * @version
 */
public class NetTool {
	public static final NetTool NET_TOOL = new NetTool();
	/** The log. @author 黄林 The log. */
	private static Log log = LogFactory.getLog(NetTool.class);

	/** The ipseek. @author 黄林 The ipseek. */
	private static IPSeeker ipseek;

	/** The mcc. @author 黄林 The mcc. */
	private static MemCachedClient mcc = new MemCachedClient();

	/** The has init. @author 黄林 The has init. */
	private static boolean hasInit = false;

	private static Session session;

	/**
	 * Creates the name value list.
	 * 
	 * @param params
	 *            the params
	 * @return the array list
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static ArrayList<NameValuePair> createNameValueList(
			HashMap<String, String> params) throws Exception {
		if (null != params && params.size() > 0) {
			ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Entry<String, String> entry : params.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
			return nvps;
		} else {
			return new ArrayList<NameValuePair>();
		}
	}

	/**
	 * 获取http协议文本内容.
	 * 
	 * @param url
	 *            the url
	 * @return the http
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static String excuteHttpGet(String url) throws Exception {

		return excuteHttpGet(url, null);
	}

	/**
	 * 获取http协议文本内容.
	 * 
	 * @param url
	 *            the url
	 * @param params
	 *            the params
	 * @return the http
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static String excuteHttpGet(String url, HttpParams params)
			throws Exception {
		// 请求处理页面
		HttpGet httppost = new HttpGet(url);
		// 创建待处理的表单域内容文本
		// 设置请求
		if (null != params) {
			httppost.setParams(params);
		}
		// 执行
		return executeHttpRequest(httppost);
	}

	/**
	 * 功能:获取http协议文本内容 创建者： 黄林 2011-7-26.
	 * 
	 * @param url
	 *            the url
	 * @param reqEntity
	 *            the req entity
	 * @return string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static String excuteHttpPost(String url, HttpEntity reqEntity)
			throws Exception {
		// 请求处理页面
		HttpPost httppost = new HttpPost(url);
		// 创建待处理的表单域内容文本
		// 设置请求
		if (null != reqEntity) {
			httppost.setEntity(reqEntity);
		}
		return executeHttpRequest(httppost);
	}

	/**
	 * Execute http post.
	 * 
	 * @param url
	 *            the url
	 * @param params
	 *            the params
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static String executeHttpPost(String url,
			HashMap<String, String> params) throws Exception {
		return executeHttpPost(url, createNameValueList(params));
	}

	/**
	 * Execute http post.
	 * 
	 * @param url
	 *            the url
	 * @param nvps
	 *            the nvps
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static String executeHttpPost(String url, List<NameValuePair> nvps)
			throws Exception {
		return executeHttpPost(url, nvps, null);
	}

	/**
	 * Execute api http post with login.
	 * 
	 * @param url
	 *            the url
	 * @param nvps
	 *            the nvps
	 * @param reqentity
	 *            the reqentity
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static String executeHttpPost(String url, List<NameValuePair> nvps,
			MultipartEntity reqentity) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost postMethod = new HttpPost(url);
		if (null != nvps && nvps.size() > 0) {
			postMethod.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		}
		if (null != reqentity) {
			postMethod.setEntity(reqentity);
		}
		HttpResponse response = httpclient.execute(postMethod);
		int status = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == status) {
			HttpEntity entity = response.getEntity();
			// 显示内容
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
			return null;
		} else {
			HttpEntity entity = response.getEntity();
			// 显示内容
			if (entity != null) {
				log.info(url + ":" + EntityUtils.toString(entity));
			}
			throw new Exception("ERROR：" + status);
		}
	}

	/**
	 * Execute http request.
	 * 
	 * @param httpMethod
	 *            the http method
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClientProtocolException
	 *             the client protocol exception
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	private static String executeHttpRequest(HttpUriRequest httpMethod)
			throws IOException, ClientProtocolException, Exception {
		HttpClient httpclient = new DefaultHttpClient();
		// 执行
		HttpResponse response = httpclient.execute(httpMethod);
		int status = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == status) {
			HttpEntity entity = response.getEntity();
			// 显示内容
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
		} else {
			throw new Exception("ERROR：" + status);
		}
		return null;
	}

	/**
	 * Gets the mcc.
	 * 
	 * @return the mcc
	 * @author 黄林
	 */
	public static MemCachedClient getMcc() {
		if (!hasInit) {
			// 服务器列表和其权重
			String[] servers = PropertyTool.getProperties("memCachedServices")
					.split(",");
			Integer[] weights = PropertyTool.stringToArrays(PropertyTool
					.getProperties("memCachedEights"));
			// 获取socke连接池的实例对象
			SockIOPool pool = SockIOPool.getInstance();

			// 设置服务器信息
			pool.setServers(servers);
			pool.setWeights(weights);

			// 设置初始连接数、最小和最大连接数以及最大处理时间
			pool.setInitConn(PropertyTool.getNumProperties("memCachedInitConn")
					.intValue());
			pool.setMinConn(PropertyTool.getNumProperties("memCachedMinConn")
					.intValue());
			pool.setMaxConn(PropertyTool.getNumProperties("memCachedMaxConn")
					.intValue());
			pool.setMaxIdle(PropertyTool.getNumProperties("memCachedMaxIdle")
					.intValue());

			// 设置主线程的睡眠时间
			pool.setMaintSleep(PropertyTool.getNumProperties(
					"memCachedMaintSleep").intValue());

			// 设置TCP的参数，连接超时等
			pool.setNagle(Boolean.valueOf(PropertyTool
					.getProperties("memCachedNagle")));
			pool.setSocketTO(PropertyTool.getNumProperties("memCachedSocketTO")
					.intValue());
			pool.setSocketConnectTO(PropertyTool.getNumProperties(
					"memCachedSocketConnectTO").intValue());

			// 初始化连接池
			pool.initialize();
			hasInit = true;
		}
		return mcc;
	}

	/**
	 * 将url转为文件.
	 * 
	 * @param path
	 *            the path
	 * @return the url file
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static File getUrlFile(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		int status = conn.getResponseCode();
		if (status == 200) {
			BufferedInputStream in = new BufferedInputStream(
					conn.getInputStream());
			// 保存的图片文件名

			File file = SystemTool.createTempFile();

			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(file));
			byte[] buf = new byte[8192];
			int len = in.read(buf);
			while (len != -1) {
				out.write(buf, 0, len);
				len = in.read(buf);
			}
			in.close();
			out.close();
			return file;
		} else {
			throw new Exception("net error:" + status);
		}
	}

	/**
	 * 功能:查询纯真ip库文件,非快速模式 创建者： 黄林 2011-10-8.
	 * 
	 * @param ip
	 *            the ip
	 * @return map
	 * @author 黄林
	 */
	public static Map<String, String> queryIP(String ip) {
		return queryIP(ip, false);
	}

	/**
	 * 功能:查询纯真ip库文件 创建者： 黄林 2011-10-8.
	 * 
	 * @param ip
	 *            the ip
	 * @param fast
	 *            快速模式(取ip错误后不再查询网络)
	 * @return map
	 * @author 黄林
	 */
	public static Map<String, String> queryIP(String ip, boolean fast) {
		String cacheStr = "cz" + ip;
		if (StringTool.isNull(ip)) {
			return null;
		}
		if (Tool.locContainsKey(cacheStr)) {
			return (Map<String, String>) Tool.getLocCache(cacheStr);
		}
		try {
			if (null == ipseek) {
				ipseek = new IPSeeker(
						FileTool.getClassPathFile("classpath:cache/qqwry.dat"));
			}
			IPLocation location = ipseek.getIpLocation(ip);
			Map<String, String> map = new HashMap<String, String>();
			map.put("pro",
					location.getCountry().substring(0,
							location.getCountry().indexOf("省") + 1));
			map.put("addr", ipseek.getAddress(ip));
			map.put("ip", ip);
			map.put("city",
					location.getCountry().substring(
							location.getCountry().indexOf("省") + 1));
			Tool.addLocCache(cacheStr, map);
			return map;
		} catch (Exception e) {
			log.debug(ip, e);
			if (!fast) {
				return queryIPONL(ip);
			}
			return null;
		}
	}

	/**
	 * 功能:查询ip138的ip数据 创建者： 黄林 2011-8-9.
	 * 
	 * @param ip
	 *            the ip
	 * @return map
	 * @author 黄林
	 */
	public static Map<String, String> queryIP138(String ip) {
		String cacheStr = "138" + ip;
		if (StringTool.isNull(cacheStr)) {
			return null;
		}
		if (Tool.locContainsKey(cacheStr)) {
			return (Map<String, String>) Tool.getLocCache(cacheStr);
		}
		try {
			String resultStr = excuteHttpPost(
					"http://www.ip138.com/ips8.asp?ip=" + ip + "&action=2",
					null);
			if (resultStr != null) {

				resultStr = new String(resultStr.getBytes("iso-8859-1"),
						"gb2312");
				String body = resultStr.substring(7306, 7315);
				Map<String, String> map = new HashMap<String, String>();
				map.put("pro", body.substring(0, 3));
				map.put("addr", body);
				map.put("ip", ip);
				map.put("city", body.substring(3, 6));
				Tool.addLocCache(cacheStr, map);
				return map;
			}
		} catch (Exception ex) {
			log.warn("query ip:" + ip + " from ip138 fail", ex);
			return null;
		}
		return null;
	}

	/**
	 * 获取ip.
	 * 
	 * @param ip
	 *            the ip
	 * @return map
	 * @author 黄林
	 */
	public static Map<String, String> queryIPONL(String ip) {
		String cacheStr = "pco" + ip;
		if (StringTool.isNull(cacheStr)) {
			return null;
		}
		if (Tool.locContainsKey(cacheStr)) {
			return (Map<String, String>) Tool.getLocCache(cacheStr);
		}
		try {
			String resultStr = excuteHttpPost(
					"http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip
							+ "&level=1", null);
			if (resultStr != null) {
				resultStr = resultStr.substring(34);
				resultStr = resultStr.substring(0, resultStr.length() - 6);
				Map<String, String> map = JsonTool.jsonToType(resultStr,
						HashMap.class);
				map.remove("region");
				map.remove("regionNames");
				Tool.addLocCache(cacheStr, map);
				return map;
			}
		} catch (Exception ex) {
			log.warn("query ip error",ex);
			return null;
		}
		return null;
	}

	/**
	 * 发送异常邮件.
	 * 
	 * @param text
	 *            the text
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send error mail.
	 */
	public static void sendErrorMail(String text) throws Exception {
		sendErrorMail(text, null);
	}

	/**
	 * 发送异常邮件.
	 * 
	 * @param text
	 *            the text
	 * @param head
	 *            the head
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send error mail.
	 */
	public static void sendErrorMail(String text, String head) throws Exception {
		if (StringTool.isNull(head)) {
			head = "";
		}
		if (StringTool.isNull(text)) {
			text = "";
		}
		NetTool.sendMail(
				false,
				PropertyTool.getProperties("errorAddress"),
				PropertyTool.getProperties("errorMailHead") + "-"
						+ SystemTool.getHostName() + head, text);
	}

	/**
	 * 发送异常邮件.
	 * 
	 * @param th
	 *            the th
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send error mail.
	 */
	public static void sendErrorMail(Throwable th) throws Exception {
		sendErrorMail(th, null);
	}
	public static void sendErrorMailSafe(Throwable th) {
		try{
			sendErrorMail(th);
		}catch (Exception e){
			log.error(th.getMessage(),th);
		}
	}

	/**
	 * 发送异常邮件.
	 * 
	 * @param th
	 *            the th
	 * @param head
	 *            the head
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send error mail.
	 */
	public static void sendErrorMail(Throwable th, String head)
			throws Exception {
		NetTool.sendErrorMail(th, head, null);
	}

	/**
	 * 发送异常邮件.
	 * 
	 * @param th
	 *            the th
	 * @param head
	 *            the head
	 * @param text
	 *            the text
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send error mail.
	 */
	public static void sendErrorMail(Throwable th, String head, String text)
			throws Exception {
		if (StringTool.isNull(th)) {
			th = new Exception();
		}
		String cacheStr = th.getClass().getSimpleName() + th.getMessage();
		for (int i = 0; i < th.getStackTrace().length; i++) {
			if (i >= 3) {
				break;
			}
			cacheStr += th.getStackTrace()[i].toString();
		}
		if (Tool.locContainsKey(cacheStr)) {
			log.warn("has cache message", th);
			return;
		}
		StringBuffer str = new StringBuffer(TimeTool.getFormatStringByNow()
				+ ":catch Exception " + th.getClass().getSimpleName() + ":"
				+ th.getMessage() + "\r\n");
		ExceptionTool.cutStackTraceWithHead(th);
		for (int i = 0; i < th.getStackTrace().length; i++) {
			str.append(th.getStackTrace()[i].toString() + "\r\n");
		}
		if (StringTool.isNotNull(text)) {
			str.append(text + "\r\n");
		}
		NetTool.sendErrorMail(str.toString(), head);
	}

	/**
	 * Send feedback mail.
	 * 
	 * @param text
	 *            the text
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send feedback mail.
	 */
	public static void sendFeedbackMail(String text) throws Exception {
		sendMail(false, PropertyTool.getProperties("feedbackAddress"),
				PropertyTool.getProperties("feedbackMailHead"), text);
	}

	/**
	 * 功能:发送邮件 创建者： 黄林 2011-11-15.
	 * 
	 * @param snyc
	 *            the snyc
	 * @param toAddress
	 *            the to address
	 * @param txthead
	 *            the txthead
	 * @param txtmain
	 *            the txtmain
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send mail.
	 */
	public static void sendMail(Boolean snyc, String toAddress, String txthead,
			String txtmain) throws Exception {
		if (Tool.runMode() == 0) {
			return;
		}
		if (null != snyc && snyc) {
			sendMail(toAddress, txthead, txtmain);
		} else {
			TaskTool.createTask("sendMail", NET_TOOL, toAddress, txthead,
					txtmain);
		}
	}

	/**
	 * 发送邮件.
	 * 
	 * @param toAddress
	 *            the to address
	 * @param txthead
	 *            the txthead
	 * @param txtmain
	 *            the txtmain
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send mail.
	 */
	public static void sendMail(String toAddress, String txthead, String txtmain)
			throws Exception {
		if (Tool.runMode() == 0) {
			log.error(txtmain);
			return;
		}
		if (session == null) {
			final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
			Properties props = System.getProperties();
			props.setProperty("mail.smtp.host",
					PropertyTool.getProperties("hostSmtp"));
			props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
			props.setProperty("mail.smtp.socketFactory.fallback", "false");
			props.setProperty("mail.smtp.port", "465");
			props.setProperty("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.auth", "true");
			Authenticator ah = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					try {
						return new PasswordAuthentication(
								PropertyTool.getProperties("hostAddress"),
								PropertyTool.getProperties("hostPwd"));
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}

			};
			session = Session.getDefaultInstance(props, ah);
		}
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(PropertyTool
				.getProperties("hostAddress")));
		msg.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(toAddress, false));
		msg.setSubject(txthead);
		msg.setText(txtmain);
		msg.setSentDate(new Date());
		Transport.send(msg);
	}

}
