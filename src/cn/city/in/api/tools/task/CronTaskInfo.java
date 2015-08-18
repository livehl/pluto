package cn.city.in.api.tools.task;

/**
 * 功能:Corn任务描述类
 * 
 * @author 黄林 2011-8-23
 * @version
 */
public class CronTaskInfo extends BaseTaskInfo {
	/** The cron. */
	private String cron;

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

}
