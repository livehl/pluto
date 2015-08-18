package cn.city.in.api.tools.task;

/**
 * 功能:任务模块
 * 
 * @author 黄林 2011-8-23
 * @version
 */
public class TimeTaskInfo extends BaseTaskInfo {
	/** The loop count. */
	private Integer loopCount;
	/** The loop. */
	private Long loop;
	/** The time. */
	private Long time;

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * 功能:重置时间(有循环的任务中) 创建者： 黄林 2011-8-23.
	 */
	public void reSetTime() {
		if (loopCount > 1) {// 大于零
			time = loop;
			loopCount--;
		} else if (loopCount == -1) {// 一直循环
			time = loop;
		} else// 不再循环
		{
			time = -1L;
		}
	}

	/**
	 * Sets the loop count.
	 * 
	 * @param loopCount
	 *            the new loop count
	 */
	public void setLoopCount(Integer loopCount) {
		this.loopCount = loopCount;
	}

	/**
	 * Sets the time.
	 * 
	 * @param time
	 *            the new time
	 */
	public void setTime(Long time) {
		if (null == loop) {
			loop = time;
		}
		this.time = time;
	}

}
