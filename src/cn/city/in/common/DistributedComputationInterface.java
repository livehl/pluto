package cn.city.in.common;

import java.io.Serializable;

/**
 * 分布式计算接口类
 * 
 * @author 黄林 The Interface DistributedComputationInterface.
 */
public interface DistributedComputationInterface extends Serializable {

	/**
	 * 需要执行的方法(每个客户端都会执行，只是每个客户端收到的参数不一样而已) execute执行
	 * 
	 * @param args
	 *            the args
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public Object execute(Object... args) throws Exception;

	/**
	 * 合并方法(将每个客户端计算的结果进行合并处理) master执行
	 * 
	 * @param args
	 *            the args
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public Object merge(Object... args) throws Exception;

	/**
	 * 参数拆分，结果为参数列表，每一项为每个执行线程参数 列表长度为cpu数量 master执行
	 * 
	 * @param args
	 *            the args
	 * @return the object[]
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public Object[] splitArgs(int cpuCount, Object... args) throws Exception;

	/**
	 * 异常处理（暂时不知怎么用）
	 * 
	 * @param e
	 *            the e
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	// public Object exceptionCaught(Throwable e)throws Exception;
}
