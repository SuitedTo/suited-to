package scheduler;

import models.TaskTrigger;

public class TriggerDetail {
	
	private String triggerKey;
	private String taskClassName;
	private TaskTrigger trigger;
	private TaskArgs taskArgs;
	
	TriggerDetail(String triggerKey, String taskClassName,
			TaskTrigger trigger, TaskArgs taskArgs) {
		this.triggerKey = triggerKey;
		this.taskClassName = taskClassName;
		this.trigger = trigger;
		this.taskArgs = taskArgs;
	}

	public String getTriggerKey() {
		return triggerKey;
	}

	public void setTriggerKey(String triggerKey) {
		this.triggerKey = triggerKey;
	}

	public String getTaskClassName() {
		return taskClassName;
	}

	public void setTaskClassName(String taskClassName) {
		this.taskClassName = taskClassName;
	}

	public TaskArgs getTaskArgs() {
		return taskArgs;
	}

	public void setTaskArgs(TaskArgs taskArgs) {
		this.taskArgs = taskArgs;
	}

	public TaskTrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(TaskTrigger trigger) {
		this.trigger = trigger;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TriggerDetail other = (TriggerDetail) obj;
		if (taskArgs == null) {
			if (other.taskArgs != null)
				return false;
		} else if (!taskArgs.toXML().equals(other.taskArgs.toXML()))
			return false;
		if (taskClassName == null) {
			if (other.taskClassName != null)
				return false;
		} else if (!taskClassName.equals(other.taskClassName))
			return false;
		return true;
	}

}
