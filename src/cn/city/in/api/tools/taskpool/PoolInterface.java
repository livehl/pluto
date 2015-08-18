package cn.city.in.api.tools.taskpool;

/**
 * 多线程任务队列接口
 * 
 * @author 黄林 The Interface PoolInterface.
 */
public interface PoolInterface {

	/**
	 * 增加一项错误
	 * 
	 * @param obj
	 *            the obj
	 * @author 黄林 Adds the error.
	 */
	public void addError(Object obj);

	/**
	 * 增加一项成功
	 * 
	 * @param obj
	 *            the obj
	 * @author 黄林 Adds the succes.
	 */
	public void addSucces(Object obj);

	/**
	 * 添加一个任务到队列
	 * 
	 * @param task
	 *            the task
	 * @return the object
	 * @author 黄林
	 */
	public Object addTask(Object... task);

	/**
	 * 线程处理完毕
	 * 
	 * @param thread
	 *            the thread
	 * @return the object
	 * @author 黄林
	 */
	public Object doAfter(Object before, PoolThread thread);

	/**
	 * 线程处理开始
	 * 
	 * @return the object
	 * @author 黄林
	 * @throws Exception
	 */
	public Object doBefore() throws Exception;

	/**
	 * 处理任务
	 * 
	 * @param obj
	 *            the obj
	 * @return the object
	 * @author 黄林
	 */
	public Object doTask(Object before, Object... obj) throws Exception;

	/**
	 * 获取一个任务
	 * 
	 * @return the task
	 * @author 黄林
	 */
	public Object[] getTask();

	/**
	 * 等待所有任务完成
	 * 
	 * @author 黄林 Wait all task.
	 */
	public void waitAllTaskDone();

	/**
	 * 等待所有任务完成并重置
	 * 
	 * @return 错误数
	 * @author 黄林
	 */
	public int waitAllTaskDoneAndReSet();
}
