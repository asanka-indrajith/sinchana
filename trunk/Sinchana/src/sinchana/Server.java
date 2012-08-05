/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sinchana;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import sinchana.connection.ThriftServer;
import sinchana.chord.ChordTable;
import sinchana.thrift.Message;
import sinchana.thrift.MessageType;
import sinchana.thrift.Node;
import sinchana.util.tools.CommonTools;

/**
 *
 * @author S.A.H.S.Subasinghe
 */
public class Server extends Node {

		public static final BigInteger GRID_SIZE = new BigInteger("2", 16).pow(160);
		private final PortHandler portHandler = new ThriftServer(this);
		private final RoutingHandler routingHandler = new ChordTable(this);
		private final MessageHandler messageHandler = new MessageHandler(this);
		private SinchanaInterface sinchanaInterface = null;
		private SinchanaTestInterface sinchanaTestInterface = null;
		private SinchanaServiceInterface sinchanaServiceInterface = null;
		private SinchanaStoreInterface sinchanaStoreInterface = null;
                
		/**
		 * Default life time of a message. At each hop, the lifetime decrements 
		 * and when it reaches 0, the message is discarded.
		 */
		public static final int MESSAGE_LIFETIME = 1024;
		/**
		 * 
		 */
		private String remoteNodeAddress = null;
		private BigInteger serverIdAsBigInt;

		/**
		 * Start a new node with the given server ID and next hop.
		 * @param serverId		Server ID. Generated using a hash function. 
		 * @param anotherNode	Another node is the network. New node first
		 * communicate with this node to discover the rest of the network.
		 * @param address		URL of the server.
		 * @param portId		Port Id number where the the server is running.
		 */
		public Server(int localPortId) {
				try {
						InetAddress inetAddress = InetAddress.getLocalHost();
						this.init(inetAddress.getHostAddress() + ":" + localPortId, null);
				} catch (UnknownHostException ex) {
						throw new RuntimeException("Error getting local host ip.", ex);
				}
		}

		public Server(String localAddress) {
				this.init(localAddress, null);
		}

		public Server(int localPortId, String remoteNodeAddress) {
				try {
						InetAddress inetAddress = InetAddress.getLocalHost();
						this.init(inetAddress.getHostAddress() + ":" + localPortId, remoteNodeAddress);
				} catch (UnknownHostException ex) {
						throw new RuntimeException("Error getting local host ip.", ex);
				}
		}

		public Server(String localAddress, String remoteNodeAddress) {
				this.init(localAddress, remoteNodeAddress);
		}

		private void init(String address, String remoteNodeAddress) {
				this.address = address;
				this.serverIdAsBigInt = CommonTools.generateId(this.address);
				StringBuilder sb = new StringBuilder(serverIdAsBigInt.toString(16));
				while (sb.length() < 40) {
						sb.insert(0, "0");
				}
				this.serverId = sb.toString();
				this.remoteNodeAddress = remoteNodeAddress;
		}

		/**
		 * Start the server.
		 */
		public void startServer() {
				this.routingHandler.init();
				this.portHandler.startServer();
		}

		public void join() {
				if (this.remoteNodeAddress != null) {
						Message msg = new Message(this, MessageType.JOIN, MESSAGE_LIFETIME);
						Node remoteNode = new Node("n/a", remoteNodeAddress);
						this.portHandler.send(msg, remoteNode);
				} else {
						this.messageHandler.startAsRootNode();
						if (this.sinchanaTestInterface != null) {
								this.sinchanaTestInterface.setStable(true);
						}
				}
		}

		/**
		 * Stop the server.
		 */
		public void stopServer() {
				portHandler.stopServer();
				messageHandler.terminate();
		}

		/**
		 * Register SinchanaInterface. The callback functions in this interface 
		 * will be called when an event occurs. 
		 * @param sinchanaInterface		SinchanaInterface instance.
		 */
		public void registerSinchanaInterface(SinchanaInterface sinchanaInterface) {
				this.sinchanaInterface = sinchanaInterface;
		}

		/**
		 * Register SinchanaTestInterface. This is only for the testing purposes. 
		 * The callback functions in this interface will be called when an event occurs. 
		 * @param sinchanaTestInterface			SinchanaTestInteface instance.
		 */
		public void registerSinchanaTestInterface(SinchanaTestInterface sinchanaTestInterface) {
				this.sinchanaTestInterface = sinchanaTestInterface;
		}

		public void registerSinchanaServiceInterface(SinchanaServiceInterface sinchanaServiceInterface) {
				this.sinchanaServiceInterface = sinchanaServiceInterface;
		}

		public void registerSinchanaStoreInterface(SinchanaStoreInterface sinchanaStoreInterface) {
				this.sinchanaStoreInterface = sinchanaStoreInterface;
		}

		/**
		 * 
		 * @return
		 */
		public MessageHandler getMessageHandler() {
				return messageHandler;
		}

		/**
		 * 
		 * @return
		 */
		public PortHandler getPortHandler() {
				return portHandler;
		}

		/**
		 * 
		 * @return
		 */
		public RoutingHandler getRoutingHandler() {
				return routingHandler;
		}

		public BigInteger getServerIdAsBigInt() {
				return serverIdAsBigInt;
		}

		/**
		 * 
		 * @return
		 */
		public SinchanaTestInterface getSinchanaTestInterface() {
				return sinchanaTestInterface;
		}

		/**
		 * 
		 * @return
		 */
		public SinchanaInterface getSinchanaInterface() {
				return sinchanaInterface;
		}

		public SinchanaServiceInterface getSinchanaServiceInterface() {
				return sinchanaServiceInterface;
		}

		public SinchanaStoreInterface getSinchanaStoreInterface() {
				return sinchanaStoreInterface;
		}

		/**
		 * Send a message. If the message type is MessageType.GET, targetKey field should be set.
		 * @param message		Message to pass to the network.
		 */
		public void send(Message message) {
				message.setSource(this);
				message.setStation(this);
				this.getMessageHandler().queueMessage(message);
		}

		/**
		 * Send a MessageType.GET message.
		 * @param destination	Destination ID to receive message.
		 * @param message		Message string.
		 */
		public void send(String destination, String message) {
				Message msg = new Message(this, MessageType.REQUEST, MESSAGE_LIFETIME);
				msg.setTargetKey(destination);
				msg.setMessage(message);
				msg.setStation(this);
				this.getMessageHandler().queueMessage(msg);
		}

		public void publishService(String key, String service) {
				Message msg = new Message(this, MessageType.PUBLISH_SERVICE, MESSAGE_LIFETIME);
				msg.setTargetKey(key);
				msg.setMessage(service);
				msg.setStation(this);
				this.getMessageHandler().queueMessage(msg);
		}

		public void removeService(String key) {
				Message msg = new Message(this, MessageType.REMOVE_SERVICE, MESSAGE_LIFETIME);
				msg.setTargetKey(key);
				msg.setStation(this);
				this.getMessageHandler().queueMessage(msg);
		}

		public void getService(String key) {
				Message msg = new Message(this, MessageType.GET_SERVICE, MESSAGE_LIFETIME);
				msg.setTargetKey(key);
				msg.setStation(this);
				this.getMessageHandler().queueMessage(msg);
		}

		public void storeData(String key, String data) {
				Message msg = new Message(this, MessageType.STORE_DATA, MESSAGE_LIFETIME);
				msg.setTargetKey(key);
				msg.setMessage(data);
				msg.setStation(this);
				this.getMessageHandler().queueMessage(msg);
		}

		public void deleteData(String key) {
				Message msg = new Message(this, MessageType.DELETE_DATA, MESSAGE_LIFETIME);
				msg.setTargetKey(key);
				msg.setStation(this);
				this.getMessageHandler().queueMessage(msg);
		}

		public void getData(String key) {
				Message msg = new Message(this, MessageType.GET_DATA, MESSAGE_LIFETIME);
				msg.setTargetKey(key);
				msg.setStation(this);
				this.getMessageHandler().queueMessage(msg);
		}

		public void trigger() {
				this.routingHandler.optimize();
		}
}