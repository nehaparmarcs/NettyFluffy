package gash.router.server.handlers;

import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import io.netty.channel.Channel;
import pipe.work.Work.WorkMessage;
import src.gash.router.server.messages.wrk_messages.handlers.BeatMessage;

public class HeartBeatHandler implements IMessageHandler {

	private ServerState state;
	
	public HeartBeatHandler(ServerState state){
		this.state = state;
	}
	private final Logger logger = (Logger) LoggerFactory.getLogger("HeartBeatHandler");
	@Override
	public void chainedTo(IMessageHandler nextHandler) {
		
	}

	@Override
	public void handleMessage(WorkMessage msg, Channel channel) {
		
		if(msg.hasBeat()){
			logger.info("Received Heartbeat from: " + msg.getHeader().getNodeId());
			
			//Don't think will need the code below.
			/*
			 * 
			EdgeList outBoundEdges = state.getEmon().getOutboundEdges();
			EdgeInfo obedgeInfo = outBoundEdges.getNode(msg.getHeader().getNodeId());
			if(obedgeInfo!=null){
				obedgeInfo.setLastHeartbeat(System.currentTimeMillis());
			}
			*/
			
			EdgeList inBoundEdges = state.getEmon().getInboundEdges();
			EdgeInfo inbedgeInfo = inBoundEdges.getNode(msg.getHeader().getNodeId());

			if (inbedgeInfo != null) {
				inbedgeInfo.setLastHeartbeat(System.currentTimeMillis());
			}
			
			BeatMessage beatMessage = new BeatMessage(state.getConf().getNodeId());
			beatMessage.setDestination(msg.getHeader().getNodeId());
			channel.writeAndFlush(beatMessage.getMessage());
			
		}
		
	}

	@Override
	public IMessageHandler getNextInChain() {
		// TODO Auto-generated method stub
		return null;
	}

}