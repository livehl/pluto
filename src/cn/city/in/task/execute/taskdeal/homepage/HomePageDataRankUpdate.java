package cn.city.in.task.execute.taskdeal.homepage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

import cn.city.in.task.execute.db.DataBaseTool;
import cn.city.in.task.execute.taskdeal.BaseTaskDeal;

public class HomePageDataRankUpdate extends BaseTaskDeal {
	
	protected static Log log = LogFactory.getLog(HomePageDataRankUpdate.class);

	@Override
	public boolean accept(String task) {
		return task.equalsIgnoreCase("HomePageRank") || task.equalsIgnoreCase("HomePageDelete");
	}

	@Override
	public boolean doTask(JsonNode task) throws Exception {
		JsonNode data = task.get("data");
		Integer itemType = data.get("itemType").asInt();
		Integer itemId = data.get("itemId").asInt();
		Integer rankOrder = data.get("rankOrder").asInt();
		Boolean addPraise = false;
		if(data.get("addPraise")!=null){			
			addPraise = data.get("addPraise").asBoolean();
		}
		Boolean isDelete = false;
		if(data.get("isDelete")!=null){
			isDelete = data.get("isDelete").asBoolean();
		}
		String sql = "";
		if(isDelete!=null&&isDelete){			
			sql = "DELETE FROM HomePageData  ";
			sql += " WHERE itemType = "+itemType+" AND itemId = "+itemId;
		}else{
			sql = "UPDATE HomePageData SET rankOrder = rankOrder + ";
			sql += rankOrder;
			if(addPraise!=null&&addPraise){
				sql += ",praiseCount = praiseCount + 1";
			}
			sql += " WHERE itemType = "+itemType+" AND itemId = "+itemId;
		}
		DataBaseTool.update(sql);
		return true;
	}

}
