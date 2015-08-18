package cn.city.in.task.execute.taskdeal.file;

import org.codehaus.jackson.JsonNode;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.task.execute.taskdeal.BaseTaskDeal;

public class FileTaskDealImpl extends BaseTaskDeal {
	@Override
	public boolean accept(String task) {
		return task.equals("file");
	}

	@Override
	public boolean doTask(JsonNode task) throws Exception {
		JsonNode data = task.get("data");
		String type = data.get("type").asText();
		if ("save".equals(type)) {
			String path = data.get("path").asText();
			byte[] fileData = data.get("fileData").getBinaryValue();
			FileTool.saveByte(FileTool.getClassPathFile(path), fileData);
		} else {
			throw new Exception("can't deal task:" + task);
		}
		return true;
	}
}
