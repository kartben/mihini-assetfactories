/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.eclipse.mihini.coap.assetfactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.api.AbstractIoFutureListener;
import org.apache.mina.api.AbstractIoHandler;
import org.apache.mina.api.IdleStatus;
import org.apache.mina.api.IoSession;
import org.apache.mina.coap.CoapCode;
import org.apache.mina.coap.CoapMessage;
import org.apache.mina.coap.CoapOption;
import org.apache.mina.coap.CoapOptionType;
import org.apache.mina.coap.codec.CoapDecoder;
import org.apache.mina.coap.codec.CoapEncoder;
import org.apache.mina.coap.resource.AbstractResourceHandler;
import org.apache.mina.coap.resource.CoapResponse;
import org.apache.mina.coap.resource.ResourceRegistry;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.query.RequestFilter;
import org.apache.mina.transport.bio.BioUdpServer;

/**
 * A CoAP UDP server serving some resources.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class CoapServer {

	public ResourceRegistry reg;

	public void start() {

		final Map<String, IoSession> registration = new ConcurrentHashMap<String, IoSession>();

		reg = new ResourceRegistry();

		BioUdpServer server = new BioUdpServer();
		final RequestFilter<CoapMessage, CoapMessage> rq = new RequestFilter<CoapMessage, CoapMessage>();

		server.setFilters(
				/* new LoggingFilter(), */new ProtocolCodecFilter<CoapMessage, ByteBuffer, Void, Void>(
						new CoapEncoder(), new CoapDecoder()), rq);
		// idle in 10 minute
		server.getSessionConfig().setIdleTimeInMillis(IdleStatus.READ_IDLE,
				10 * 60 * 1000);
		server.setIoHandler(new AbstractIoHandler() {

			long start = System.currentTimeMillis();
			int count = 0;

			@Override
			public void messageReceived(IoSession session, Object message) {
				System.err.println("rcv : " + message);

				CoapMessage resp = reg.respond((CoapMessage) message, session);
				System.err.println("resp : " + resp);
				session.write(resp);
				count++;
				if (count >= 100000) {
					System.err.println("time for 100k msg : "
							+ (System.currentTimeMillis() - start));
					count = 0;
					start = System.currentTimeMillis();
				}
			}

			@Override
			public void messageSent(IoSession session, Object message) {
				System.err.println("sent : " + message);
			}

			@Override
			public void sessionIdle(IoSession session, IdleStatus status) {
				System.err.println("idle closing");
				session.close(false);
			}
		});

		try {
			server.bind(5683);
			new Thread() {
				@Override
				public void run() {
					for (;;) {
						for (IoSession s : registration.values()) {
							rq.request(s, CoapMessage.get("st", true), 15000)
									.register(
											new AbstractIoFutureListener<CoapMessage>() {
												@Override
												public void completed(
														CoapMessage result) {
													System.err
															.println("status : "
																	+ result);
												}
											});
						}

						try {
							// let's poll every 10 seconds
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							break;
						}
					}
				}
			}.start();

			for (;;) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		final CoapServer cs = new CoapServer();

		new Thread(new Runnable() {
			public void run() {
				cs.start();

			}
		}).start();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cs.reg.register(new AbstractResourceHandler() {

			@Override
			public String getPath() {
				return "demo";
			}

			@Override
			public CoapResponse handle(CoapMessage request, IoSession session) {
				return new CoapResponse(CoapCode.CONTENT.getCode(),
						"niah niah niah niah niah\n niah niah niah\n"
								.getBytes(),
						new CoapOption(CoapOptionType.CONTENT_FORMAT,
								new byte[] { 0 }));
			}

			@Override
			public String getTittle() {
				return "Some demo resource";
			}

		});

		cs.reg.register(new AbstractResourceHandler() {

			@Override
			public String getPath() {
				return "demo/1";
			}

			@Override
			public CoapResponse handle(CoapMessage request, IoSession session) {
				return new CoapResponse(CoapCode.CONTENT.getCode(),
						"niah niah niah niah niah\n niah niah niah\n"
								.getBytes(),
						new CoapOption(CoapOptionType.CONTENT_FORMAT,
								new byte[] { 0 }));
			}

			@Override
			public String getTittle() {
				return "Some demo resource";
			}

		});

		cs.reg.register(new AbstractResourceHandler() {

			@Override
			public String getPath() {
				return "demo/2";
			}

			@Override
			public CoapResponse handle(CoapMessage request, IoSession session) {
				return new CoapResponse(CoapCode.CONTENT.getCode(),
						"niah niah niah niah niah\n niah niah niah\n"
								.getBytes(),
						new CoapOption(CoapOptionType.CONTENT_FORMAT,
								new byte[] { 0 }));
			}

			@Override
			public String getTittle() {
				return "Some demo resource";
			}

		});

	}

}
