package gash.router.server.tasks;

import java.util.concurrent.LinkedBlockingQueue;

import routing.Pipe.CommandMessage;

public class CommunicationQueues {

	
	private LinkedBlockingQueue<CommandMessage> inboundTaskQueue;
	private LinkedBlockingQueue<CommandMessage> outboundTaskQueue;
	
	public CommunicationQueues(){
		inboundTaskQueue = new LinkedBlockingQueue<CommandMessage>();
		outboundTaskQueue = new LinkedBlockingQueue<CommandMessage>();
	}

	public LinkedBlockingQueue<CommandMessage> getInboundTaskQueue() {
		return inboundTaskQueue;
	}

	public void setInboundTaskQueue(LinkedBlockingQueue<CommandMessage> inboundTaskQueue) {
		this.inboundTaskQueue = inboundTaskQueue;
	}

	public LinkedBlockingQueue<CommandMessage> getOutboundTaskQueue() {
		return outboundTaskQueue;
	}

	public void setOutboundTaskQueue(LinkedBlockingQueue<CommandMessage> outboundTaskQueue) {
		this.outboundTaskQueue = outboundTaskQueue;
	}
	
	
}
