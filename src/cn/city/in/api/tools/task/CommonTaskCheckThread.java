package cn.city.in.api.tools.task;

public class CommonTaskCheckThread extends Thread {
	private boolean isLive = true;

	public CommonTaskCheckThread() {
		super();
		this.setDaemon(true);
	}

	public void die() {
		isLive = false;
	}

	@Override
	public void run() {
		while (isLive) {
			try {
				TaskTool.check();
				Thread.sleep(1000);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
