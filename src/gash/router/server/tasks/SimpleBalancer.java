package gash.router.server.tasks;

import src.gash.router.server.tasks.TaskList;

public class SimpleBalancer implements Rebalancer {

	private TaskList taskList;
	
	public SimpleBalancer(TaskList taskList) {
		this.taskList = taskList;
	}
	
	@Override
	public boolean allow() {
		return calcLoad() > 0.50;
	}

	@Override
	public float calcLoad() {
		return taskList.numEnqueued() / taskList.MAX_SIZE;
	}

}
