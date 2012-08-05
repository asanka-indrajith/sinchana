/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sinchana.test;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import sinchana.thrift.Message;
import sinchana.thrift.MessageType;
import sinchana.util.tools.CommonTools;

/**
 *
 * @author Hiru
 */
public class TesterController {

		public static int NUM_OF_TESTING_NODES = 0;
		public static int NUM_OF_AUTO_TESTING_NODES = 1;
		public static final boolean GUI_ON = false;
		public static final boolean USE_REMOTE_CACHE_SERVER = false;
		public static final int AUTO_TEST_TIMEOUT = 2;
		public static final int ROUND_TIP_TIME = 50;
		public static final int AUTO_TEST_MESSAGE_LIFE_TIME = 120;
		public static int max_buffer_size = 0;
		private final Map<Integer, Tester> testServers = new HashMap<Integer, Tester>();
		private final ControllerUI cui = new ControllerUI(this);
		private int completedCount = 0;
		private final Semaphore startLock = new Semaphore(0);
		private final Timer timer = new Timer();
		private final Timer timer2 = new Timer();
		private long numOfTestMessages = 0;

		/**
		 * 
		 * @param args
		 */
		public static void main(String[] args) {
				if (TesterController.USE_REMOTE_CACHE_SERVER) {
						try {
								URL yahoo = new URL("http://cseanremo.appspot.com/remoteip?clear=true");
								URLConnection yc = yahoo.openConnection();
								InputStreamReader isr = new InputStreamReader(yc.getInputStream());
								isr.close();
						} catch (Exception e) {
								throw new RuntimeException("Error in clearing the cache server.", e);
						}
				} else {
						LocalCacheServer.clear();
				}
				new TesterController();
		}

		private TesterController() {
				cui.setVisible(true);
				timer.scheduleAtFixedRate(new TimerTask() {

						long totalMessageIncome, totalInputMessageQueue,
								totalOutputMessageQueue, totalResolves,
								maxInputMessageQueueSize, maxOutputMessageQueueSize,
								totalLifeTime;
						long newTime, oldTime = Calendar.getInstance().getTimeInMillis();
						int mxaTester = -1;

						@Override
						public void run() {
								totalMessageIncome = 0;
								totalInputMessageQueue = 0;
								totalOutputMessageQueue = 0;
								maxInputMessageQueueSize = 0;
								maxOutputMessageQueueSize = 0;
								totalLifeTime = 0;
								totalResolves = 0;
								mxaTester = -1;
								long[] testData;
								Set<Integer> keySet = testServers.keySet();
								for (int tid : keySet) {
										testData = testServers.get(tid).getTestData();
										totalMessageIncome += testData[0];
										totalInputMessageQueue += testData[1];
										totalOutputMessageQueue += testData[2];
										if (maxInputMessageQueueSize < testData[3]) {
												maxInputMessageQueueSize = testData[3];
										}
										if (maxOutputMessageQueueSize < testData[4]) {
												maxOutputMessageQueueSize = testData[4];
												mxaTester = tid;
										}
										totalResolves += testData[5];
										totalLifeTime += testData[6];

								}
								newTime = Calendar.getInstance().getTimeInMillis();

								if (completedCount != 0) {
										cui.setStat("IC: " + (totalMessageIncome / completedCount)
												+ "    IB: " + (totalInputMessageQueue / completedCount)
												+ "    MI: " + maxInputMessageQueueSize
												+ "    OB: " + (totalOutputMessageQueue / completedCount)
												+ "    MO: " + maxOutputMessageQueueSize
												+ "    TR: " + totalResolves
												+ "    TP: " + (newTime > oldTime ? (totalResolves * 1000 / (newTime - oldTime)) : "INF") + "/S"
												+ "    AL: " + (totalResolves != 0 ? (totalLifeTime / totalResolves) : "NA"));
								}
								oldTime = newTime;
								if (mxaTester != -1) {
//										System.out.println(mxaTester + ": " + testServers.get(mxaTester).temp);
								}
						}
				}, 1000, 1000);
				timer2.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
								if (numOfTestMessages != 0) {
										testMessages(numOfTestMessages / 10);
								}
						}
				}, 100, 100);
		}

		/**
		 * 
		 * @param numOfTesters
		 */
		public void startNodeSet(int portRange, int numOfTesters) {
				try {
						Tester tester;
						for (int i = NUM_OF_TESTING_NODES; i < NUM_OF_TESTING_NODES + numOfTesters; i++) {
								tester = new Tester(i, portRange + i, this);
								testServers.put(i, tester);
						}
						NUM_OF_TESTING_NODES += numOfTesters;
						String[] testServerIds = new String[NUM_OF_TESTING_NODES];

						for (int i = 0; i < NUM_OF_TESTING_NODES; i++) {
								testServerIds[i] = testServers.get(i).getServerId();
						}

						Arrays.sort(testServerIds);
						for (String id : testServerIds) {
								System.out.print(id + " ");
						}
						System.out.println("");
						Set<Integer> keySet = testServers.keySet();
						for (int key : keySet) {
								tester = testServers.get(key);
								if (!tester.isRunning()) {
										tester.startServer();
										System.out.println("Server " + tester.getServerId() + " is running...");
								}
						}
						startLock.acquire();
				} catch (InterruptedException ex) {
						Logger.getLogger(TesterController.class.getName()).log(Level.SEVERE, null, ex);
				}
		}

		/**
		 * 
		 * @param numOfAutoTesters
		 */
		public void startAutoTest(long numOfTestMessages) {
				this.numOfTestMessages = numOfTestMessages;
		}

		public void testMessages(long numOfTestMessages) {
				int numOfTestServers = testServers.size();
				int randomId;
				long randomAmount = 0;
				while (numOfTestMessages > 0) {
						randomId = (int) (Math.random() * numOfTestServers);
						if (numOfTestMessages > 10) {
								randomAmount = (long) (Math.random() * numOfTestMessages);
								numOfTestMessages -= randomAmount;
						} else {
								randomAmount = numOfTestMessages;
								numOfTestMessages = 0;
						}
						testServers.get(randomId).startTest(randomAmount);
				}
		}

		/**
		 * 
		 */
		public void startRingTest() {
				Set<Integer> keySet = testServers.keySet();
				for (int key : keySet) {
						testServers.get(key).startRingTest();
						break;
				}

		}

		/**
		 * 
		 * @param id
		 */
		public synchronized void incrementCompletedCount(int id) {
				completedCount++;
//				System.out.println(completedCount + " of " + NUM_OF_TESTING_NODES + " are stable... \t\t" + id);
				cui.setStatus(completedCount + " of " + NUM_OF_TESTING_NODES + " are stable...");
				if (completedCount == NUM_OF_TESTING_NODES) {
						startLock.release();
				}
		}

		/**
		 * 
		 * @param text
		 * @param destination
		 * @param requester
		 */
		public void send(String text, String destination, String requester) {
				Set<Integer> keySet = testServers.keySet();
				for (int key : keySet) {
						if (testServers.get(key).getServerId().equals(requester)) {
								Message msg = new Message(testServers.get(key).getServer(), MessageType.REQUEST, 10);
								msg.setTargetKey(destination);
								msg.setMessage(text);
								testServers.get(key).getServer().send(msg);
						}
				}
		}
		String[] serviceArray = null;
		String[] keyArray = null;
		int serviceID = 0;

		public void publishService(int noOfServices) {

				serviceArray = new String[noOfServices];
				keyArray = new String[noOfServices];

				for (int i = 0; i < noOfServices; i++) {
						serviceArray[i] = String.valueOf("Service " + serviceID);
						keyArray[i] = CommonTools.generateId(serviceArray[i]).toString();
						serviceID++;
				}

				int randomId;
				int randomAmount;
				int x = 0;
				while (noOfServices > 0) {

						randomId = (int) (Math.random() * testServers.size());
						randomAmount = (int) (Math.random() * noOfServices);
						noOfServices = noOfServices - randomAmount;
						while (randomAmount > 0) {
								testServers.get(randomId).getServer().publishService(keyArray[x], serviceArray[x]);
								x++;
								randomAmount--;
						}
				}
		}

		public void retrieveService() {
				int randomId;
				int randomAmount;
//                            while(noOfServices>0){
				randomId = (int) (Math.random() * testServers.size());
				randomAmount = (int) (Math.random() * serviceArray.length);
				for (int i = 0; i < serviceArray.length; i++) {

						testServers.get(randomId).getServer().getService(keyArray[i]);


				}


		}

		/**
		 * 
		 * @param nodeIdsString
		 * @param typesString
		 * @param classIdsString
		 * @param locationsString
		 */
		public void printLogs(String nodeIdsString, String typesString, String classIdsString,
				String locationsString, String containTextString) {
				String[] temp;
				String[] nodeIds = null;
				int[] levels = null, classIds = null, locations = null;
				if (nodeIdsString.length() > 0) {
						nodeIds = nodeIdsString.split(" ");
				}
				if (typesString.length() > 0) {
						temp = typesString.split(" ");
						levels = new int[temp.length];
						for (int i = 0; i < temp.length; i++) {
								levels[i] = Integer.parseInt(temp[i]);
						}
				}
				if (classIdsString.length() > 0) {
						temp = classIdsString.split(" ");
						classIds = new int[temp.length];
						for (int i = 0; i < temp.length; i++) {
								classIds[i] = Integer.parseInt(temp[i]);
						}
				}
				if (locationsString.length() > 0) {
						temp = locationsString.split(" ");
						locations = new int[temp.length];
						for (int i = 0; i < temp.length; i++) {
								locations[i] = Integer.parseInt(temp[i]);
						}
				}
				sinchana.util.logging.Logger.print(nodeIds, levels, classIds, locations, containTextString);
		}

		public void trigger() {
				Set<Integer> keySet = testServers.keySet();
				for (int key : keySet) {
						testServers.get(key).trigger();
				}
		}
}