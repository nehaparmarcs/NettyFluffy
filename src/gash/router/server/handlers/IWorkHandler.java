package gash.router.server.handlers;

import io.netty.channel.Channel;
import pipe.work.Work.WorkMessage;

/**
 * This interface will serve as MessageHandler Interface which will ease
 * the chaining of handlers
 * @author neha
 */
public interface IWorkHandler {

	public void handleMessage(WorkMessage msg, Channel channel);
	
}
