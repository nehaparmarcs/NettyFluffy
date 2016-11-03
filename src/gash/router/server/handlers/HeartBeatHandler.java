package gash.router.server.handlers;

import org.slf4j.LoggerFactory;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeList;
import io.netty.channel.Channel;
import pipe.common.Common.Header;
import pipe.work.Work.Heartbeat;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;

public class HeartBeatHandler implements IWorkHandler {

	private ServerState state;
	
	public HeartBeatHandler(ServerState state){
		this.state = state;
	}
	private final org.slf4j.Logger logger =  LoggerFactory.getLogger("HeartBeatHandler");
	

	@Override
	public void handleMessage(WorkMessage msg, Channel channel) {
		System.out.println("Is it even called?");
		if(msg.hasBeat()){
			logger.info("Received Heartbeat from: " + msg.getHeader().getNodeId());
			
			//Don't think will need to set for outbound edge.
			 
			EdgeList outBoundEdges = state.getEmon().getOutboundEdges();
			EdgeInfo obedgeInfo = outBoundEdges.getNode(msg.getHeader().getNodeId());
			if(obedgeInfo!=null){
				obedgeInfo.setLastHeartbeat(System.currentTimeMillis());
			}
			
			EdgeList inBoundEdges = state.getEmon().getInboundEdges();
			EdgeInfo inbedgeInfo = inBoundEdges.getNode(msg.getHeader().getNodeId());

			if (inbedgeInfo != null) {
				inbedgeInfo.setLastHeartbeat(System.currentTimeMillis());
			}
			
			// Reply back with the HeartBeat
			WorkMessage hbMsg = createHB(state.getConf().getNodeId(), msg.getHeader().getNodeId());   
			channel.writeAndFlush(hbMsg);
			
		}
		
	}

	/**
	 * This Message sends the HeartBeat message back to the sender. 
	 * @param nodeId current Node ID
	 * @param desNodeId destination Node ID
	 * @return WorkMessage response HeartBeat to be sent back
	 */
	private WorkMessage createHB(int nodeId, int desNodeId) {
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);
		
		
		Heartbeat.Builder bb = Heartbeat.newBuilder();
		bb.setState(sb);

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(nodeId);
		hb.setDestination(desNodeId);
		hb.setTime(System.currentTimeMillis());

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		//Adding a default value for a secret
		wb.setSecret(1);
		wb.setBeat(bb);

		return wb.build();
	}
	
	

}