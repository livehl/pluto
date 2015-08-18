package cn.city.in.task.execute.taskdeal;

import org.codehaus.jackson.JsonNode;

public abstract class BaseTaskDeal implements TaskDealInterface {

	@Override
	public abstract boolean accept(String task);

	@Override
	public abstract boolean doTask(JsonNode task) throws Exception;

	@Override
	public JsonNode getResult() {
		return null;
	}

	@Override
	public boolean hasResult() {
		return false;
	}

	@Override
	public boolean idle() {
		return true;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

}
