/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sinchana;

import java.util.Arrays;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import sinchana.dataStore.SinchanaDataCallback;
import sinchana.service.SinchanaServiceCallback;
import sinchana.service.SinchanaServiceInterface;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import sinchana.exceptions.SinchanaTimeOutException;
import sinchana.thrift.Message;
import sinchana.thrift.MessageType;
import sinchana.thrift.Node;
import sinchana.util.tools.Hash;

/**
 *
 * @author Hiru
 */
public class ClientHandler {

	private static final String ERROR_MSG_TIMED_OUT = "Timed out!";
	private static final String ERROR_MSG_INTERRUPTED = "Interrupted!";
	private final SinchanaServer server;
	private final Node thisNode;
	private final ConcurrentHashMap<Long, ClientData> clientsMap = new ConcurrentHashMap<Long, ClientData>();
	private final Timer timer = new Timer();

	ClientHandler(SinchanaServer svr) {
		this.server = svr;
		this.thisNode = server.getNode();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				Collection<ClientData> values = clientsMap.values();
				long currentTimeMillis = System.currentTimeMillis();
				for (ClientData cd : values) {
					if (cd.time + CONFIGURATIONS.ASYNCHRONOUS_REQUEST_TIME_OUT < currentTimeMillis) {
						ClientData clientData = clientsMap.remove(cd.key);
						if (clientData != null) {
							clientData.resolved = false;
							if (clientData.waiting) {
								clientData.success = false;
								clientData.lock.release();
							} else {
								clientData.sinchanaCallBackHandler.error(ERROR_MSG_TIMED_OUT.getBytes());
							}
						}
					}
				}
			}
		}, 1000, 1000);
	}

	void setResponse(Message message) {
		ClientData clientData = clientsMap.remove(message.getId());
		if (clientData != null) {
			clientData.resolved = true;
			switch (message.type) {
				case RESPONSE_SERVICE:
					if (clientData.waiting) {
						clientData.data = message.getData();
						clientData.success = message.isSuccess();
						clientData.lock.release();
					} else {
						((SinchanaServiceCallback) clientData.sinchanaCallBackHandler).serviceResponse(
								Arrays.copyOf(clientData.dataKey, clientData.dataKey.length - CONFIGURATIONS.SERVICE_TAG.length),
								message.isSuccess(), message.getData());
					}
					break;

				case RESPONSE_DATA:
					if (clientData.waiting) {
						clientData.data = message.getData();
						clientData.lock.release();
					} else {
						if (clientData.sinchanaCallBackHandler instanceof SinchanaDataCallback) {
							((SinchanaDataCallback) clientData.sinchanaCallBackHandler).response(clientData.dataKey, message.getData());
						} else if (clientData.sinchanaCallBackHandler instanceof SinchanaServiceCallback) {
							((SinchanaServiceCallback) clientData.sinchanaCallBackHandler).serviceFound(
									Arrays.copyOf(clientData.dataKey, clientData.dataKey.length - CONFIGURATIONS.SERVICE_TAG.length),
									message.isSuccess(), message.getData());
						}
					}
					break;
				case ACKNOWLEDGE_DATA_STORE:
					if (clientData.waiting) {
						clientData.data = message.getData();
						clientData.lock.release();
					} else {
						if (clientData.sinchanaCallBackHandler instanceof SinchanaDataCallback) {
							((SinchanaDataCallback) clientData.sinchanaCallBackHandler).isStored(clientData.dataKey, message.success);
						} else if (clientData.sinchanaCallBackHandler instanceof SinchanaServiceInterface) {
							((SinchanaServiceInterface) clientData.sinchanaCallBackHandler).isPublished(
									Arrays.copyOf(clientData.dataKey, clientData.dataKey.length - CONFIGURATIONS.SERVICE_TAG.length),
									message.success);
						}
					}
					break;
				case ACKNOWLEDGE_DATA_REMOVE:
					if (clientData.waiting) {
						clientData.data = message.getData();
						clientData.lock.release();
					} else {
						if (clientData.sinchanaCallBackHandler instanceof SinchanaDataCallback) {
							((SinchanaDataCallback) clientData.sinchanaCallBackHandler).isRemoved(clientData.dataKey, message.success);
						} else if (clientData.sinchanaCallBackHandler instanceof SinchanaServiceInterface) {
							if (message.success) {
								boolean success = this.server.getSinchanaServiceStore().removeService(clientData.dataKey);
								((SinchanaServiceInterface) clientData.sinchanaCallBackHandler).isRemoved(
										Arrays.copyOf(clientData.dataKey, clientData.dataKey.length - CONFIGURATIONS.SERVICE_TAG.length),
										success);
							} else {
								((SinchanaServiceInterface) clientData.sinchanaCallBackHandler).isRemoved(
										Arrays.copyOf(clientData.dataKey, clientData.dataKey.length - CONFIGURATIONS.SERVICE_TAG.length),
										false);
							}
						}
					}
					break;
				case RESPONSE:
					if (server.getSinchanaTestInterface() != null) {
						server.getSinchanaTestInterface().incRequestCount(message.lifetime, message.routedViaPredecessors);
					}
					if (clientData.waiting) {
						clientData.data = message.getData();
						clientData.lock.release();
					} else {
						((SinchanaResponseCallback) clientData.sinchanaCallBackHandler).response(message.getData());
					}
					break;
				case ERROR:
					if (clientData.waiting) {
						clientData.data = message.getData();
						clientData.success = false;
						clientData.error = message.getError();
						clientData.lock.release();
					} else {
						((SinchanaCallBack) clientData.sinchanaCallBackHandler).error(message.getError());
					}
					break;
			}
		}
	}

	ClientData addRequest(byte[] key, byte[] data, MessageType type, long timeOut, TimeUnit timeUnit) throws SinchanaTimeOutException, InterruptedException {
		ClientData clientData = null;
		long requestId = -1;
		Message message = new Message(type, thisNode, CONFIGURATIONS.REQUEST_MESSAGE_LIFETIME);
		switch (message.type) {
			case REQUEST:
				message.setDestinationId(key);
				message.setKey(key);
				break;
			case GET_SERVICE:
				message.setDestinationId(Arrays.copyOf(key, 20));
				message.setKey(Arrays.copyOfRange(key, 20, key.length));
				break;
			case GET_DATA:
			case STORE_DATA:
			case DELETE_DATA:
				message.setDestinationId(Hash.generateId(new String(key)));
				message.setKey(key);
				break;
		}
		message.setData(data);
		message.setStation(thisNode);

		requestId = System.currentTimeMillis();
		clientData = new ClientData();
		clientData.time = requestId;
		clientData.dataKey = key;
		clientData.waiting = true;
		clientData.sinchanaCallBackHandler = null;
		while (clientsMap.putIfAbsent(requestId, clientData) != null) {
			requestId++;
		}
		clientData.key = requestId;
		message.setResponseExpected(true);
		message.setId(requestId);
		try {
			server.getMessageHandler().addRequest(message);
			if (timeOut != -1) {
				clientData.lock.tryAcquire(timeOut, timeUnit);
			} else {
				clientData.lock.acquire();
			}
		} catch (InterruptedException ex) {
			throw ex;
		} finally {
			clientsMap.remove(requestId);
		}
		if (!clientData.resolved) {
			throw new SinchanaTimeOutException(ERROR_MSG_TIMED_OUT);
		}
		return clientData;
	}

	void addRequest(byte[] key, byte[] data, MessageType type, SinchanaCallBack scbh) throws InterruptedException {
		long requestId = -1;
		Message message = new Message(type, thisNode, CONFIGURATIONS.REQUEST_MESSAGE_LIFETIME);
		switch (message.type) {
			case REQUEST:
				message.setDestinationId(key);
				message.setKey(key);
				break;
			case GET_SERVICE:
				message.setDestinationId(Arrays.copyOf(key, 20));
				message.setKey(Arrays.copyOfRange(key, 20, key.length));
				break;
			case GET_DATA:
			case STORE_DATA:
			case DELETE_DATA:
				message.setDestinationId(Hash.generateId(new String(key)));
				message.setKey(key);
				break;
		}
		message.setData(data);
		message.setStation(thisNode);
		if (scbh != null) {
			requestId = System.currentTimeMillis();
			ClientData clientData = new ClientData();
			clientData.time = requestId;
			clientData.dataKey = key;
			clientData.waiting = false;
			clientData.sinchanaCallBackHandler = scbh;
			while (clientsMap.putIfAbsent(requestId, clientData) != null) {
				requestId++;
			}
			clientData.key = requestId;
			message.setResponseExpected(true);
			message.setId(requestId);
		} else {
			message.setResponseExpected(false);
		}
		server.getMessageHandler().addRequest(message);
	}

	class ClientData {

		long key;
		final Semaphore lock = new Semaphore(0);
		byte[] dataKey;
		byte[] data;
		byte[] error;
		boolean valid = true, success = false, waiting = false, resolved = false;
		long time;
		SinchanaCallBack sinchanaCallBackHandler;
	}
}