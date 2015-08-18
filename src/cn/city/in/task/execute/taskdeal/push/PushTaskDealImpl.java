package cn.city.in.task.execute.taskdeal.push;

import java.io.File;

import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.NumberTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.TimeTool;
import cn.city.in.api.tools.common.Tool;
import cn.city.in.task.execute.db.DataBaseTool;
import cn.city.in.task.execute.taskdeal.BaseTaskDeal;

public class PushTaskDealImpl extends BaseTaskDeal {
	protected static Log log = LogFactory.getLog(PushTaskDealImpl.class);

	public static void main(String... args) throws Exception {
		PropertyTool.init("classpath:serverconf/task.execute.properties");
		PushTaskDealImpl ptd = new PushTaskDealImpl();
//		for (int i = 1; i <= 10; i++) {
//			int mark = TimeTool.addTimeMark();
			ptd.sendIpMsg(
					"农村小伙买不起软卧票被分手，写书变百万富翁 “那一年因为我没钱买一张软卧车票给她，我失恋了。而今，我已经不用再买车票。我用稿费在我们这买了房，不用出门奔波……”近日，网友“大头猫神”因发帖讲述自身“屌丝逆袭”的经历，引来数十万网友围观。昨日，媒体调查发现网帖内容基本属实。" +
					"最怕的就是遇到猪一样的厂商，派个猪一样的人跟我联系。告诉了几次今年2013，人家还是死活填2012，然后天天找我闹她发的开服找不出来而且为嘛你要一副你是我上帝的口气。滚，该滚哪儿滚哪儿" +
					"会说话的照片，你知道吗？我刚安装了#啪啪#，它可以给照片录音，让你的照片更加生动。听照片讲故事，感受不同的声音，很有意思哦，快试试" 
//			+ i
					,
					"0e4cd1a751004887d65445e2423e2fc4256bbf5af134e66896edc2a64ef7f4fb",
					1, 11, 0, "https://www.google.com/url?sa=t&rct=j&q=");
//			Thread.sleep(200);
//			TimeTool.printTimeMarkDiff(mark);
//		}
		System.exit(0);
	}

	protected AppleNotificationServer server;

	protected PushNotificationManager pushManager;

	@Override
	public boolean accept(String task) {
		return task.equals("push");
	}

	/**
	 * Delete wp push url by url.
	 * 
	 * @param url
	 *            the url
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Delete wp push url by url.
	 */
	private void deleteWpPushUrlByUrl(String url) throws Exception {
		DataBaseTool.update("DELETE FROM WpPushInfo WHERE pushUrl = '" + url
				+ "'");
	}

	@Override
	public boolean doTask(JsonNode task) throws Exception {
		JsonNode data = task.get("data");
		String type = data.get("type").asText();
		if ("ip".equals(type)) {
			sendIpMsg(data.get("msg").asText(), data.get("device_id").asText(),
					data.get("badge").asInt(), data.get("item_type").asInt(),
					data.get("item_id").asInt(), data.get("link").asText());
		} else if ("wp".equals(type)) {
			sendWpMsg(data.get("device_id").asText(), data.get("msg").asText(),
					data.get("target").asText());
		} else {
			throw new Exception("can't deal task:" + task);
		}
		return true;
	}

	@Override
	public boolean idle() {
		try {
			if (null != pushManager) {
				pushManager.stopConnection();
			}
			server = null;
			pushManager = null;
		} catch (Exception e) {
			log.error("stop connection fail", e);
		}
		return true;
	}

	@Override
	public boolean init() {
		try {
			File file = FileTool.getClassPathFile(PropertyTool
					.getProperties("ip_cert"));
			server = new AppleNotificationServerBasicImpl(file, "inchengdu",
					Tool.runMode() != 2);
			pushManager = new PushNotificationManager();
			pushManager.initializeConnection(server);
		} catch (Exception e) {
			log.error("init apns fail", e);
			return false;
		}
		return true;
	}

	/**
	 * ip 推送
	 * 
	 * @param msg
	 *            the msg
	 * @param deviceToken
	 *            the device token
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send ip msg.
	 */
	private void sendIpMsg(String msg, String deviceToken, int badge, int type,
			int id, String link) throws Exception {
		//裁剪消息
		PushNotificationPayload payload =PushNotificationPayload.complex();
		payload.addAlert(msg);
		if (!TimeTool.isBetween0to7()) {
			payload.addSound("nm.wav");
		}
		payload.addIcdMessageType(type, id, link);
		if (NumberTool.isValid(badge)) {
			payload.addBadge(badge);
		}
		int maxCutCount=payload.toString().getBytes("UTF-8").length-payload.getMaximumPayloadSize();
		if(maxCutCount>0){
			if(msg.getBytes("utf-8").length-3<maxCutCount){
				throw new Exception("uri to long");
			}
			msg=truncateWhenUTF8(msg, msg.getBytes("utf-8").length-3-maxCutCount) + "...";
			payload =PushNotificationPayload.complex();
			payload.addAlert(msg);
			if (!TimeTool.isBetween0to7()) {
				payload.addSound("nm.wav");
			}
			payload.addIcdMessageType(type, id, link);
			if (NumberTool.isValid(badge)) {
				payload.addBadge(badge);
			}
		}	
//		PushedNotifications notifications = new PushedNotifications();
		BasicDevice device = new BasicDevice();
		device.setToken(deviceToken);
		if (null == server) {
			File file = FileTool.getClassPathFile(PropertyTool
					.getProperties("ip_cert"));
			server = new AppleNotificationServerBasicImpl(file, "inchengdu",
					Tool.runMode() == 2);
			pushManager = new PushNotificationManager();
			pushManager.initializeConnection(server);
		}
//		notifications.setMaxRetained(1);
		BasicDevice.validateTokenFormat(device.getToken());
		try {
			PushedNotification notification = pushManager.sendNotification(
					device, payload, false);
//			notifications.add(notification);
			if (!notification.isSuccessful()) {
				// 发送失败,直接抛出异常
				throw new Exception("send push fail:",
						notification.getException());
			}
		} catch (Exception e) {
			try {
				pushManager.stopConnection();
			} catch (Exception e2) {
			}
			server = null;
			pushManager = null;
			throw e;
		}
	}

	/**
	 * wp推送
	 * 
	 * @param url
	 *            the url
	 * @param xml
	 *            the xml
	 * @param type
	 *            the type
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send wp msg.
	 */
	private void sendWpMsg(String url, String xml, String type)
			throws Exception {
		boolean sound = TimeTool.isBetween0to7();
		if (sound && "toast".equals(type)) {
			return;
		}
		HttpClient client = new DefaultHttpClient();
		HttpPost postMethod = new HttpPost(url);
		postMethod.addHeader("X-MessageID", java.util.UUID.randomUUID()
				.toString());
		Integer notificationClass = 2;
		if ("token".equals(type)) {
			notificationClass = 1;
		}
		postMethod.addHeader("X-NotificationClass",
				notificationClass.toString());
		postMethod.addHeader("X-WindowsPhone-Target", type.trim());
		postMethod.setHeader("Content-type", "text/xml");
		String toastMessage = xml;
		postMethod
				.setEntity(new ByteArrayEntity(toastMessage.getBytes("UTF-8")));
		HttpResponse response = client.execute(postMethod);
		if (response.getStatusLine().getStatusCode() != 200) {
			log.debug("del url:" + url);
			deleteWpPushUrlByUrl(url);
		}
	}

	@Override
	public boolean stop() {
		try {
			if (null != pushManager) {
				pushManager.stopConnection();
			}
		} catch (Exception e) {
			log.error("stop connection fail", e);
			return false;
		}
		return true;
	}
	public static String truncateWhenUTF8(String s, int maxBytes) {
	    int b = 0;
	    for (int i = 0; i < s.length(); i++) {
	        char c = s.charAt(i);
	        int skip = 0;
	        int more;
	        if (c <= 0x007f) {
	            more = 1;
	        }
	        else if (c <= 0x07FF) {
	            more = 2;
	        } else if (c <= 0xd7ff) {
	            more = 3;
	        } else if (c <= 0xDFFF) {
	            // surrogate area, consume next char as well
	            more = 4;
	            skip = 1;
	        } else {
	            more = 3;
	        }

	        if (b + more > maxBytes) {
	            return s.substring(0, i);
	        }
	        b += more;
	        i += skip;
	    }
	    return s;
	}
}
