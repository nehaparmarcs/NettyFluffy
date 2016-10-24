/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server.edges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.container.RoutingConf.RoutingEntry;
import gash.router.server.NewWorkChannelInit;
import gash.router.server.ServerState;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.common.Common.Header;
import pipe.work.Work.Heartbeat;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;

public class EdgeMonitor implements EdgeListener, Runnable {
	protected static Logger logger = LoggerFactory.getLogger("edge monitor");

	private EdgeList outboundEdges;
	private EdgeList inboundEdges;
	private long dt = 2000;
	private ServerState state;
	private boolean forever = true;
	private EventLoopGroup group;

	public EdgeMonitor(ServerState state) {
		if (state == null)
			throw new RuntimeException("state is null");

		this.outboundEdges = new EdgeList();
		this.inboundEdges = new EdgeList();
		this.group = new NioEventLoopGroup ();
		this.state = state;
		this.state.setEmon(this);

		if (state.getConf().getRouting() != null) {
			for (RoutingEntry e : state.getConf().getRouting()) {
				outboundEdges.addNode(e.getId(), e.getHost(), e.getPort());
			}
		}

		// cannot go below 2 sec
		if (state.getConf().getHeartbeatDt() > this.dt)
			this.dt = state.getConf().getHeartbeatDt();
	}

	public void createInboundIfNew(int ref, String host, int port) {
		inboundEdges.createIfNew(ref, host, port);
	}

	private WorkMessage createHB(EdgeInfo ei) {
		WorkState.Builder sb = WorkState.newBuilder();
		sb.setEnqueued(-1);
		sb.setProcessed(-1);

		Heartbeat.Builder bb = Heartbeat.newBuilder();
		bb.setState(sb);

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(-1);
		hb.setTime(System.currentTimeMillis());

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setBeat(bb);

		return wb.build();
	}

	public void shutdown() {
		forever = false;
	}

	@Override
	public void run() {
		while (forever) {
			try {
				for (EdgeInfo ei : this.outboundEdges.map.values()) {
					 //System.out.println("Map values: "+ this.outboundEdges.map  + " : Values : "+ this.outboundEdges.map.values() + " ei.isActive() : "+ei.isActive()+ ", ei.getChannel() : "+ei.getChannel());
					
					if (ei.isActive() && ei.getChannel() != null) {
						WorkMessage wm = createHB(ei);
						logger.info("Writing HeartBeat to  -->" + ei.getRef());
						ei.getChannel().writeAndFlush(wm);
					} else {
						// TODO create a client to the node
						
							logger.info("creating new connection with the node " + ei.getRef());

						try {
							NewWorkChannelInit wi = new NewWorkChannelInit(state,
								false);
							Bootstrap b = new Bootstrap();
							b.group(group).channel(NioSocketChannel.class).handler(wi);
							b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
							b.option(ChannelOption.TCP_NODELAY, true);
							b.option(ChannelOption.SO_KEEPALIVE, true);

							//Try making the connection here
							ChannelFuture channel = b.connect(ei.getHost(), ei.getPort())
								.syncUninterruptibly();

							// want to monitor the connection to the server s.t.
							// if we loose the
							// connection, we can try to re-establish it.
							// channel.channel().closeFuture();

							ei.setChannel(channel.channel());
							ei.setActive(channel.channel().isActive());
							ei.setLastHeartbeat(System.currentTimeMillis());
							logger.info(channel.channel().localAddress() + " -> open: "
								+ channel.channel().isOpen() + ", write: "
								+ channel.channel().isWritable() + ", reg: "
								+ channel.channel().isRegistered());

						} catch (Throwable ex) {
							logger.error("connection creation failed");
							// ex.printStackTrace();
						}
					}
				}

				Thread.sleep(dt);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void onAdd(EdgeInfo ei) {
		// TODO check connection
	}

	@Override
	public synchronized void onRemove(EdgeInfo ei) {
		// TODO ?
	}
}
