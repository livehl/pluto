package cn.city.in.task.execute.taskdeal.log;

import org.codehaus.jackson.JsonNode;

import cn.city.in.task.execute.db.DataBaseTool;
import cn.city.in.task.execute.taskdeal.BaseTaskDeal;

public class DataBaseLogWriter extends BaseTaskDeal {

	private static final int CONNECT_LOG = 1;
	private static final int GLOBAL_MESSAGE_RECIEVE_LOG = 2;
	private static final int ITEM_AWARD_LOG = 3;
	private static final int ITEM_USE_LOG = 4;
	private static final int ORDER_PAY_LOG = 5;
	private static final int USER_EXP_LOG = 6;
	private static final int USER_SEARCH_LOG = 7;
	private static final int ITEM_EXCHANGE_LOG = 8;

	@Override
	public boolean accept(String task) {
		return task.equalsIgnoreCase("writeLog");
	}

	@Override
	public boolean doTask(JsonNode task) throws Exception {
		JsonNode data = task.get("data");
		Integer logType = data.get("type").asInt();
		JsonNode log = data.get("log");
		switch (logType) {
		case CONNECT_LOG:
			saveConnectLog(log);
			break;
		case GLOBAL_MESSAGE_RECIEVE_LOG:
			saveGlobalMessageRecieveLog(log);
			break;
		case ITEM_AWARD_LOG:
			saveItemAwardLog(log);
			break;
		case ITEM_USE_LOG:
			saveItemUseLog(log);
			break;
		case USER_EXP_LOG:
			saveUserExpLog(log);
			break;
		case ORDER_PAY_LOG:
			saveOrderPayLog(log);
			break;
		case USER_SEARCH_LOG:
			saveUserSearchLog(log);
			break;
		case ITEM_EXCHANGE_LOG:
			saveItemExchangeLog(log);
			break;
		}
		return true;
	}

	private void saveConnectLog(JsonNode log) throws Exception {
		String sql = "INSERT INTO ConnectLog (uid,deviceType,appVersion,channelId,ip,deviceCode) VALUES ("
				+ log.get("uid").asInt()
				+ ""
				+ ","
				+ log.get("deviceType").asInt()
				+ ","
				+ log.get("appVersion")
				+ ","
				+ log.get("channelId").asInt()
				+ ","
				+ log.get("ip")
				+ "," + log.get("deviceCode") + ")";
		DataBaseTool.update(sql);
	}

	private void saveGlobalMessageRecieveLog(JsonNode log) throws Exception {
		String sql = "INSERT INTO GlobalMessageRecieveLog (gmid,uid,deviceCode,DeviceType) VALUES"
				+ " ("
				+ log.get("gmid").asInt()
				+ ","
				+ log.get("uid")
				+ ","
				+ log.get("deviceCode")
				+ ","
				+ log.get("deviceType").asInt()
				+ ")";
		DataBaseTool.update(sql);
	}

	private void saveItemAwardLog(JsonNode log) throws Exception {
		// TODO 记录道具获奖日志
		// TODO 不能用pluto
	}

	private void saveItemUseLog(JsonNode log) throws Exception {
		String sql = "INSERT INTO ItemUseLog (uid,userOwnItemId,itemId,result) VALUES"
				+ " ("
				+ log.get("uid").asInt()
				+ ","
				+ log.get("userOwnItemId").asInt()
				+ ","
				+ log.get("itemId").asInt() + ",'" + log.get("result") + "')";
		DataBaseTool.update(sql);
	}

	private void saveItemExchangeLog(JsonNode log) throws Exception {
		String sql = "INSERT INTO ItemExchangeLog (uid,itemId,type,recieverId) VALUES"
				+ " ("
				+ log.get("uid").asInt()
				+ ","
				+ log.get("itemId").asInt()
				+ ","
				+ log.get("type").asInt()
				+ "," + log.get("recieverId") + ")";
		DataBaseTool.update(sql);
	}

	private void saveUserExpLog(JsonNode log) throws Exception {
		// TODO 记录用户获得经验值日志
	}

	private void saveOrderPayLog(JsonNode log) throws Exception {
		// TODO 记录订单支付日志
	}

	private void saveUserSearchLog(JsonNode log) throws Exception {
		String sql = "INSERT INTO UserSearchLog (uid,keyword) VALUES ("
				+ log.get("uid").asInt() + "," + log.get("keyword") + ")";
		DataBaseTool.update(sql);
	}

}
