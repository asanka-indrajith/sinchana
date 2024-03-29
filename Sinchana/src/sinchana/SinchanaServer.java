/************************************************************************************

 * Sinchana Distributed Hash table 

 * Copyright (C) 2012 Sinchana DHT - Department of Computer Science &               
 * Engineering, University of Moratuwa, Sri Lanka. Permission is hereby 
 * granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files of Sinchana DHT, to deal 
 * in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.

 * Neither the name of University of Moratuwa, Department of Computer Science 
 * & Engineering nor the names of its contributors may be used to endorse or 
 * promote products derived from this software without specific prior written 
 * permission.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.                                                                    
 ************************************************************************************/
package sinchana;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import sinchana.dataStore.SinchanaDataCallback;
import sinchana.dataStore.SinchanaDataStoreImpl;
import sinchana.dataStore.SinchanaDataStoreInterface;
import sinchana.service.SinchanaServiceCallback;
import sinchana.service.SinchanaServiceInterface;
import sinchana.service.SinchanaServiceStore;
import sinchana.exceptions.SinchanaInvalidArgumentException;
import sinchana.exceptions.SinchanaInvalidRoutingAlgorithmException;
import sinchana.exceptions.SinchanaJoinException;
import sinchana.exceptions.SinchanaTimeOutException;
import sinchana.routing.chord.ChordTable;
import sinchana.routing.pastry.PastryTable;
import sinchana.routing.tapastry.TapestryTable;
import sinchana.thrift.Message;
import sinchana.thrift.MessageType;
import sinchana.thrift.Node;
import sinchana.util.tools.ByteArrays;
import sinchana.util.tools.Hash;

/**
 *
 * @author S.A.H.S.Subasinghe
 */
public class SinchanaServer implements SinchanaDHT {

	/**
	 * Size of the Sinchana ring which is equal to 2^160.
	 */
	public static final BigInteger GRID_SIZE = new BigInteger("2", 16).pow(160);
	private final IOHandler iOHandler;
	private final RoutingHandler routingHandler;
	private final MessageHandler messageHandler;
	private final ConnectionPool connectionPool;
	private final ClientHandler clientHandler;
	private final SinchanaServiceStore sinchanaServiceStore;
	private final Node node;
	private final String serverIdAsString;
	private final Semaphore joinLock = new Semaphore(0);
	private boolean joined = false;
	private SinchanaRequestCallback sinchanaRequestCallback = null;
	private SinchanaTestInterface sinchanaTestInterface = null;
	private SinchanaDataStoreInterface sinchanaDataStoreInterface = new SinchanaDataStoreImpl(this);

	/**
	 * SinchanaServer constructor.
	 * @param localAddress url of the server in [host address]:[port id] format.
	 */
	public SinchanaServer(String localAddress, String routingAlgorithm) throws SinchanaInvalidRoutingAlgorithmException {
		this.node = new Node(ByteBuffer.wrap(Hash.generateId(localAddress)), localAddress);
		this.serverIdAsString = ByteArrays.idToReadableString(node.serverId);
		this.iOHandler = new IOHandler(this);
		this.messageHandler = new MessageHandler(this);
		this.connectionPool = new ConnectionPool(this);
		this.clientHandler = new ClientHandler(this);
		this.sinchanaServiceStore = new SinchanaServiceStore();
		if (SinchanaDHT.CHORD.equals(routingAlgorithm)) {
			this.routingHandler = new ChordTable(this);
		} else if (SinchanaDHT.TAPESTRY.equals(routingAlgorithm)) {
			this.routingHandler = new TapestryTable(this);
		} else if (SinchanaDHT.PASTRY.equals(routingAlgorithm)) {
			this.routingHandler = new PastryTable(this);
		} else {
			throw new SinchanaInvalidRoutingAlgorithmException(SinchanaDHT.ERROR_MSG_INVALID_ROUTING_ALGORITHM);
		}
		System.out.println(this.serverIdAsString + " @ " + localAddress + " " + routingAlgorithm);
	}

	/**
	 * Starts the server. Thread is blocked until the Thrift server is ready.
	 * @throws TTransportException if the Thrift server has encountered a problem while starting on the given address and the port id.
	 * @throws InterruptedException if the thread is interrupted before the thrift server is ready.
	 */
	@Override
	public void startServer() throws TTransportException, InterruptedException {
		this.routingHandler.init();
		System.out.println(this.serverIdAsString + ": Starting on " + this.node.address);
		this.iOHandler.startServer();
		System.out.println(this.serverIdAsString + ": Started on " + this.node.address);
	}

	/**
	 * Stops the server.
	 */
	@Override
	public void stopServer() {
		iOHandler.stopServer();
	}

	/**
	 * Returns whether the server is running or not.
	 * @return <code>true</code> if the Thrift server is serving. <code>false</code> otherwise.
	 */
	@Override
	public boolean isRunning() {
		return iOHandler.isRunning();
	}

	/**
	 * Join the ring by connecting to the Sinchana server in the given address. Thread is blocked until the join is completed.
	 * @param remoteNodeAddress address of the remote Sinchana server in [host name]:[port id] format. Passing <code>null</code> or
	 * the same address which is used to initialize the server will cause the server to start alone as a root node.
	 * @throws TException if a problem encountered while connecting to the remote server.
	 * @throws InterruptedException if the thread is interrupted before the join is completed.
	 */
	@Override
	public void join(String remoteNodeAddress) throws TException, InterruptedException {
		Message msg = new Message(MessageType.JOIN, this.node, SinchanaDHT.JOIN_MESSAGE_LIFETIME);
		if (remoteNodeAddress != null && !remoteNodeAddress.equals(this.node.address)) {
			Node remoteNode = new Node(ByteBuffer.wrap(Hash.generateId(remoteNodeAddress)), remoteNodeAddress);
			msg.setDestination(remoteNode);
			int joinAttempt = 0;
			while (!joined) {
				if (++joinAttempt > SinchanaDHT.MAX_JOIN_RETRIES) {
					throw new SinchanaJoinException(SinchanaDHT.ERROR_MSG_JOIN_FAILED);
				}
				System.out.println(this.serverIdAsString + ": Attempt " + joinAttempt + ": Connecting to " + remoteNodeAddress);
				this.iOHandler.directSend(msg);
				joinLock.tryAcquire(SinchanaDHT.JOIN_RETRY_TIME_OUT, TimeUnit.SECONDS);
			}
		} else {
			msg.setStation(this.node);
			msg.setSuccess(true);
			messageHandler.queueMessage(msg);
		}
	}

	/**
	 * Initiates the ring as the root node.
	 */
	@Override
	public void join() {
		Message msg = new Message(MessageType.JOIN, this.node, SinchanaDHT.JOIN_MESSAGE_LIFETIME);
		msg.setStation(this.node);
		msg.setSuccess(true);
		messageHandler.queueMessage(msg);
	}

	void setJoined(boolean joined, byte[] status) {
		if (joined) {
			this.joined = joined;
			joinLock.release();
		} else {
			System.out.println(new String(status));
		}
	}

	/**
	 * Returns whether this server has joined to a ring or not.
	 * @return <code>true</code> if the server has joined a ring. <code>false</code> otherwise.
	 */
	@Override
	public boolean isJoined() {
		return joined;
	}

	/**
	 * Prints routing table information in console.
	 */
	@Override
	public void printTableInfo() {
		routingHandler.printInfo();
	}

	/**
	 * 
	 * @param src
	 */
	@Override
	public void registerSinchanaRequestCallback(SinchanaRequestCallback src) {
		this.sinchanaRequestCallback = src;
	}

	/**
	 * 
	 * @param sinchanaTestInterface
	 */
	@Override
	public void registerSinchanaTestInterface(SinchanaTestInterface sinchanaTestInterface) {
		this.sinchanaTestInterface = sinchanaTestInterface;
	}

	/**
	 * 
	 * @param sinchanaDataStoreInterface
	 */
	@Override
	public void registerSinchanaStoreInterface(SinchanaDataStoreInterface sinchanaDataStoreInterface) {
		this.sinchanaDataStoreInterface = sinchanaDataStoreInterface;
	}

	MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public IOHandler getIOHandler() {
		return iOHandler;
	}

	RoutingHandler getRoutingHandler() {
		return routingHandler;
	}

	public ConnectionPool getConnectionPool() {
		return connectionPool;
	}

	ClientHandler getClientHandler() {
		return clientHandler;
	}

	SinchanaServiceStore getSinchanaServiceStore() {
		return sinchanaServiceStore;
	}

	public SinchanaDataStoreInterface getSinchanaDataStoreInterface() {
		return sinchanaDataStoreInterface;
	}

	SinchanaTestInterface getSinchanaTestInterface() {
		return sinchanaTestInterface;
	}

	SinchanaRequestCallback getSinchanaRequestCallback() {
		return sinchanaRequestCallback;
	}

	public Node getNode() {
		return node;
	}

	/**
	 * Returns server id in hex string format.
	 * @return server id.
	 */
	public String getServerIdAsString() {
		return serverIdAsString;
	}

	/**
	 * Tests the ring. If the ring is completed, it will be printed on console.
	 */
	@Override
	public void testRing() {
		Message message = new Message(MessageType.TEST_RING, this.node, 1024);
		message.setStation(this.node);
		this.getMessageHandler().queueMessage(message);
	}

	/**
	 * Sends a synchronous request to the given destination. It will be received by the successor of that destination. 
	 * The thread is blocked until the response is received or time out occurs. 
	 * @param destination Destination id to send request. This should be exactly 20 byte length.
	 * @param message Message to send to the destination.
	 * @return Response from the destination.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 * @throws InterruptedException If the thread is interrupted before the response is received.
	 * @throws SinchanaInvalidArgumentException  If the destination id has a length other than 20 bytes.
	 */
	@Override
	public byte[] sendRequest(byte[] destination, byte[] message)
			throws SinchanaTimeOutException, SinchanaInvalidArgumentException, InterruptedException {
		if (destination.length != 20) {
			throw new SinchanaInvalidArgumentException(SinchanaDHT.ERROR_MSG_INVALID_ADDRESS_LENGTH);
		}
		return this.clientHandler.addRequest(destination, message, MessageType.REQUEST, -1, null).data;
	}

	/**
	 * Sends a synchronous request to the given destination. It will be received by the successor of that destination. 
	 * The thread is blocked until the response is received or time out occurs. 
	 * @param destination Destination id to send request. This should be exactly 20 byte length.
	 * @param message Message to send to the destination.
	 * @param timeOut Time out.
	 * @param timeUnit Time Unit.
	 * @return Response from the destination.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 * @throws InterruptedException If the thread is interrupted before the response is received.
	 * @throws SinchanaInvalidArgumentException If the destination id has a length other than 20 bytes.
	 */
	@Override
	public byte[] sendRequest(byte[] destination, byte[] message, long timeOut, TimeUnit timeUnit)
			throws SinchanaTimeOutException, SinchanaInvalidArgumentException, InterruptedException {
		if (destination.length != 20) {
			throw new SinchanaInvalidArgumentException(SinchanaDHT.ERROR_MSG_INVALID_ADDRESS_LENGTH);
		}
		return this.clientHandler.addRequest(destination, message, MessageType.REQUEST, timeOut, timeUnit).data;
	}

	/**
	 * Sends an asynchronous request to the given destination. It will be received by the successor of that destination.
	 * The thread is blocked until the request is queued to send.
	 * @param destination Destination id to send request. This should be exactly 20 byte length.
	 * @param message Message to send to the destination.
	 * @param callBack SinchanaResponseCallback to handle responses. Pass <code>null</code> if no response is expected.
	 * @throws InterruptedException If the thread is interrupted before the request is queued.
	 * @throws SinchanaInvalidArgumentException If the destination id has a length other than 20 bytes.
	 */
	@Override
	public void sendRequest(byte[] destination, byte[] message, SinchanaResponseCallback callBack)
			throws InterruptedException, SinchanaInvalidArgumentException {
		if (destination.length != 20) {
			throw new SinchanaInvalidArgumentException(SinchanaDHT.ERROR_MSG_INVALID_ADDRESS_LENGTH);
		}
		this.clientHandler.addRequest(destination, message, MessageType.REQUEST, callBack);
	}

	/**
	 * Stores a given key-value pair synchronously. The thread is blocked until the task is completed.
	 * @param key Data key
	 * @param data Data
	 * @return <code>true</code> if the key-data is stored in the DHT successfully. <code>false</code> otherwise.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public boolean storeData(byte[] key, byte[] data) throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(key, data, MessageType.STORE_DATA, -1, null).success;
	}

	/**
	 * Stores a given key-value pair synchronously. The thread is blocked until the task is completed.
	 * @param key Data key
	 * @param data Data
	 * @param timeOut Time out
	 * @param timeUnit Time Unit
	 * @return <code>true</code> if the key-data is stored in the DHT successfully. <code>false</code> otherwise.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public boolean storeData(byte[] key, byte[] data, long timeOut, TimeUnit timeUnit)
			throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(key, data, MessageType.STORE_DATA, timeOut, timeUnit).success;
	}

	/**
	 * Stores a given key-value pair asynchronously. The thread is blocked until the task is queued to send.
	 * @param key Data key
	 * @param data Data
	 * @param callBack SinchanaDataCallback to handle responses. Pass <code>null</code> if no response is expected.
	 * @throws InterruptedException If the thread is interrupted before the task is queued.
	 */
	@Override
	public void storeData(byte[] key, byte[] data, SinchanaDataCallback callBack) throws InterruptedException {
		this.clientHandler.addRequest(key, data, MessageType.STORE_DATA, callBack);
	}

	/**
	 * Retrieves value for a given key synchronously. The thread is blocked until the task is completed.
	 * @param key Data key to retrieve
	 * @return Value which is associated with the key. <code>null</code> if the value is not found.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public byte[] getData(byte[] key) throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(key, null, MessageType.GET_DATA, -1, null).data;
	}

	/**
	 * Retrieves value for a given key synchronously. The thread is blocked until the task is completed.
	 * @param key Data key to retrieve
	 * @param timeOut Time out
	 * @param timeUnit Time Unit
	 * @return Value which is associated with the key. <code>null</code> if the value is not found.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public byte[] getData(byte[] key, long timeOut, TimeUnit timeUnit) throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(key, null, MessageType.GET_DATA, timeOut, timeUnit).data;
	}

	/**
	 * Retrieves value for a given key asynchronously. The thread is blocked until the task is queued.
	 * @param key Data key to retrieve
	 * @param callBack SinchanaDataCallback to handle responses. Pass <code>null</code> if no response is expected.
	 * @throws InterruptedException If the thread is interrupted before the task is queued.
	 */
	@Override
	public void getData(byte[] key, SinchanaDataCallback callBack) throws InterruptedException {
		this.clientHandler.addRequest(key, null, MessageType.GET_DATA, callBack);
	}

	/**
	 * Deletes value for a given key synchronously. The thread is blocked until the task is completed.
	 * @param key Data key to delete
	 * @return <code>true</code> if the deletion is completed, <code>false</code> otherwise.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public boolean deleteData(byte[] key) throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(key, null, MessageType.DELETE_DATA, -1, null).success;
	}

	/**
	 * Deletes value for a given key synchronously. The thread is blocked until the task is completed.
	 * @param key Data key to delete
	 * @param timeOut Time out
	 * @param timeUnit Time Unit
	 * @return <code>true</code> if the deletion is completed, <code>false</code> otherwise.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public boolean deleteData(byte[] key, long timeOut, TimeUnit timeUnit)
			throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(key, null, MessageType.DELETE_DATA, timeOut, timeUnit).success;
	}

	/**
	 * Deletes value for a given key asynchronously. The thread is blocked until the task is queued.
	 * @param key Data key to delete
	 * @param callBack SinchanaDataCallback to handle responses. Pass <code>null</code> if no response is expected.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 */
	@Override
	public void deleteData(byte[] key, SinchanaDataCallback callBack) throws InterruptedException {
		this.clientHandler.addRequest(key, null, MessageType.DELETE_DATA, callBack);
	}

	/**
	 * Invokes a service given by the references synchronously. The thread is blocked until the task is completed.
	 * @param reference 
	 * @param data Data to process.
	 * @return Response from the service. <code>null</code> if no such service is found.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public byte[] invokeService(byte[] reference, byte[] data) throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(reference, data, MessageType.GET_SERVICE, -1, null).data;
	}

	/**
	 * Invokes a service given by the references synchronously. The thread is blocked until the task is completed.
	 * @param reference 
	 * @param data Data to process.
	 * @param timeOut Time out
	 * @param timeUnit Time Unit
	 * @return Response from the service. <code>null</code> if no such service is found.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public byte[] invokeService(byte[] reference, byte[] data, long timeOut, TimeUnit timeUnit)
			throws InterruptedException, SinchanaTimeOutException {
		return this.clientHandler.addRequest(reference, data, MessageType.GET_SERVICE, timeOut, timeUnit).data;
	}

	/**
	 * Invokes a service given by the references synchronously. The thread is blocked until the task is queued.
	 * @param reference 
	 * @param data Data to process.
	 * @param callBack SinchanaServiceCallback to handle responses. Pass <code>null</code> if no response is expected.
	 * @throws InterruptedException If the thread is interrupted before the task is queued.
	 */
	@Override
	public void invokeService(byte[] reference, byte[] data, SinchanaServiceCallback callBack) throws InterruptedException {
		this.clientHandler.addRequest(reference, data, MessageType.GET_SERVICE, callBack);
	}

	/**
	 * Discovers the reference for a given service key synchronously. The thread is blocked until the task is completed.
	 * @param key Service key to discover
	 * @return Reference associated with the service key. <code>null</code> if the service is not found.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public byte[] discoverService(byte[] key) throws InterruptedException, SinchanaTimeOutException {
		byte[] formattedKey = ByteArrays.arrayConcat(key, SinchanaDHT.SERVICE_TAG);
		return this.clientHandler.addRequest(formattedKey, null, MessageType.GET_DATA, -1, null).data;
	}

	/**
	 * Discovers the reference for a given service key synchronously. The thread is blocked until the task is completed.
	 * @param key Service key to discover
	 * @param timeOut Time out
	 * @param timeUnit Time Unit
	 * @return Reference associated with the service key. <code>null</code> if the service is not found.
	 * @throws InterruptedException If the thread is interrupted before the task is completed.
	 * @throws SinchanaTimeOutException If the response is not arrived within the time out.
	 */
	@Override
	public byte[] discoverService(byte[] key, long timeOut, TimeUnit timeUnit)
			throws InterruptedException, SinchanaTimeOutException {
		byte[] formattedKey = ByteArrays.arrayConcat(key, SinchanaDHT.SERVICE_TAG);
		return this.clientHandler.addRequest(formattedKey, null, MessageType.GET_DATA, timeOut, timeUnit).data;
	}

	/**
	 * Discovers the reference for a given service key asynchronously. The thread is blocked until the task is queued.
	 * @param key Service key to discover
	 * @param callBack SinchanaServiceCallback to handle responses. Pass <code>null</code> if no response is expected.
	 * @throws InterruptedException If the thread is interrupted before the task is queued.
	 */
	@Override
	public void discoverService(byte[] key, SinchanaServiceCallback callBack) throws InterruptedException {
		byte[] formattedKey = ByteArrays.arrayConcat(key, SinchanaDHT.SERVICE_TAG);
		this.clientHandler.addRequest(formattedKey, null, MessageType.GET_DATA, callBack);
	}

	/**
	 * Publish the service asynchronously. The thread is blocked until the task is queued to send.
	 * @param key service key
	 * @param sinchanaServiceInterface SinchanaServiceInterface to handle responses.
	 * @throws InterruptedException If the thread is interrupted before the task is queued.
	 * @throws SinchanaInvalidArgumentException If the SinchanaServiceInterface is null
	 */
	@Override
	public void publishService(byte[] key, SinchanaServiceInterface sinchanaServiceInterface)
			throws InterruptedException, SinchanaInvalidArgumentException {
		if (sinchanaServiceInterface == null) {
			throw new SinchanaInvalidArgumentException(SinchanaDHT.ERROR_MSG_NULL_ARGUMENTS);
		}
		byte[] formattedKey = ByteArrays.arrayConcat(key, SinchanaDHT.SERVICE_TAG);
		byte[] formattedReference = ByteArrays.arrayConcat(this.node.serverId.array(), formattedKey);
		boolean success = this.sinchanaServiceStore.publishService(formattedKey, sinchanaServiceInterface);
		if (success) {
			this.clientHandler.addRequest(formattedKey, formattedReference, MessageType.STORE_DATA, sinchanaServiceInterface);
		} else {
			sinchanaServiceInterface.isPublished(key, false);
		}
	}

	/**
	 * Removes the service asynchronously. The thread is blocked until the task is queued to send.
	 * @param key service key
	 * @throws InterruptedException If the thread is interrupted before the task is queued.
	 * @throws SinchanaInvalidArgumentException If the SinchanaServiceInterface is null
	 */
	@Override
	public void removeService(byte[] key) throws InterruptedException, SinchanaInvalidArgumentException {
		byte[] formattedKey = ByteArrays.arrayConcat(key, SinchanaDHT.SERVICE_TAG);
		SinchanaServiceInterface ssi = this.sinchanaServiceStore.get(formattedKey);
		if (ssi == null) {
			throw new SinchanaInvalidArgumentException(SinchanaDHT.ERROR_MSG_NO_SUCH_SERVICE_FOUND);
		}
		this.clientHandler.addRequest(formattedKey, null, MessageType.DELETE_DATA, ssi);
	}
}
