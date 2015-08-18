package cn.city.in.task.execute.taskdeal;

import org.codehaus.jackson.JsonNode;

/**
 * 任务处理接口,所有处理任务实现类必须继承此接口
 * 
 * @author 黄林 The Interface TaskDealInterface.
 */
public interface TaskDealInterface {
	/**
	 * 验证是否能够处理该任务
	 * 
	 * @param task
	 *            the task
	 * @return true, if can accept
	 * @author 黄林
	 */
	public boolean accept(String task);

	/**
	 * 处理任务
	 * 
	 * @param task
	 *            the task
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public boolean doTask(JsonNode task) throws Exception;

	/**
	 * 获取返回的数据
	 * 
	 * @return the result
	 * @author 黄林
	 */
	public JsonNode getResult();

	/**
	 * 本次任务是否有需要返回的数据
	 * 
	 * @return true, if successful
	 * @author 黄林
	 */
	public boolean hasResult();

	/***
	 * 空闲方法，5秒没有收到任务的时候会触发此方法
	 * 
	 * @return
	 */
	public boolean idle();

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init();

	/***
	 * 停止
	 * 
	 * @return
	 */

	public boolean stop();
}
