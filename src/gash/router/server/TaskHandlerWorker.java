package gash.router.server;

import java.util.Map;

import com.message.ClientMessage.Operation;
import com.message.ClientMessage.RequestMessage;
import com.message.ClientMessage.ResponseMessage;
import com.router.service.IDBService;
import com.router.service.impl.RedisDBServiceImpl;

import pipe.work.Work.Task;
import routing.Pipe.CommandMessage;


/**
 * Class to write a Worker that steals work if the thread is having less than 4 tasks to 
 * execute will keep the value as flexible that can be changed.
 * 
 * If use this handler in case of work stealing from adjacent nodes
 * 
 * @author neha
 *
 */

public class TaskHandlerWorker implements Runnable {

	private ServerState state;
	private boolean forever = true;
	
	static IDBService  dbClient = new RedisDBServiceImpl(); 
	
	@Override
	public void run() {
		
		RequestMessage requestMessage;
		
		CommandMessage commandMessage;
		Task task = state.getTasks().dequeue();
		
		if(task.hasCommandMessage()){
			commandMessage = task.getcommandMessage();
		}
		
		if(commandMessage.hasReqMsg()){
			requestMessage = commandMessage.getReqMsg();
			//Type of operation to be performed
			
			switch(requestMessage.getOperation()){
			
			case GET : 
						getMessage(requestMessage);
						break;
			case PUT : break;
			case POST : break;
			case DELETE : break;
			
			}
			
			
			
		}
		CommandMessage msg = ;
	
		
		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		ResponseMessage.Builder responseMsg = ResponseMessage.newBuilder();
		
		//Operation = msg.getQuery();

		
		while(forever){
			
			if(){
				
			}
			
		}
		
		
	}
	
	
	public static ResponseMessage getMessage(RequestMessage reqMsg){
	
		
		Task responseTask ;
		
		//RequestMessage.Builder resMsg = RequestMessage.newBuilder();
		
		ResponseMessage.Builder resMsg = ResponseMessage.newBuilder();
		
		
		resMsg.setOperation(reqMsg.getOperation());
		String rowKey = reqMsg.getKey();
		
		Map<Integer, byte[]> responseMap = dbClient.get(rowKey);
		
		
		if (responseMap !=null && responseMap.size()> 0) {
			
			
			
			Common.Failure.Builder fb = Common.Failure.newBuilder();
			fb.setMessage("Key not present");
			fb.setId(101);

			rb.setSuccess(false);
			rb.setFailure(fb);

			cb.setHeader(hb);
			cb.setResponse(rb);
		} else {
			logger.info("Retrieved sequenceNumbers", dataMap);
			for (Integer sequenceNo : dataMap.keySet()) {
				rb.setSuccess(true);

				if (sequenceNo == 0) {

					try {
						rb.setMetaData(
							Metadata.parseFrom(dataMap.get(sequenceNo)));
					} catch (InvalidProtocolBufferException e) {
						e.printStackTrace();
					}
				} else {
					rb.setData(ByteString.copyFrom(dataMap.get(sequenceNo)));
				}
				rb.setKey(key);
				rb.setSequenceNo(sequenceNo);

				cb.setHeader(hb);
				cb.setResponse(rb);

				Task.Builder returnTask = Task.newBuilder();
				returnTask.setTaskMessage(cb);
				returnTask.setSeqId(task.getSeqId());
				returnTask.setSeriesId(task.getSeriesId());

				WorkMessage workMessage = wrapMessage(returnTask.build());

				state.getCurrentState().handleCmdResponse(workMessage, null);
			}
			continue;
		}
		
		return resMsg;
		
		
	}
	

	
	
}
