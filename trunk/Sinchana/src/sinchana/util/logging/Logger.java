/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sinchana.util.logging;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Hiru
 */
public final class Logger {

		private static List<Log> logDB = new LinkedList<Log>();
		public static final int LEVEL_FINE = 0;
		public static final int LEVEL_INFO = 1;
		public static final int LEVEL_WARNING = 2;
		public static final int LEVEL_SEVERE = 3;
		
		public static final int CLASS_MESSAGE_HANDLER_OBJECT = 0;
		public static final int CLASS_ROUTING_TABLE = 1;
		public static final int CLASS_TESTER = 2;
		public static final int CLASS_THRIFT_SERVER = 3;
		public static final int CLASS_CONNECTION_POOL = 4;
//		public static final int CLASS_THRIFT_SERVER = 5;
//		public static final int CLASS_THRIFT_SERVER = 6;
	
		public static final int CURRENT_LOG_LEVEL = 2;
		

		private Logger() {
		}

		public static synchronized void log(int nodeId, int type, int classId, int locId, String logData) {
				Log nl = new Log();
				nl.nodeId = nodeId;
				nl.level = type;
				nl.classId = classId;
				nl.locId = (byte) locId;
				nl.logData = logData;
				logDB.add(nl);
				if(CURRENT_LOG_LEVEL > type)
						return;
//				System.out.println(nl.toString());
				switch (type) {
						case LEVEL_FINE:
								java.util.logging.Logger.getLogger(Logger.class.getName()).logp(Level.FINE,
										Logger.class.getName(), "Server " + nodeId, logData);
								break;
						case LEVEL_INFO:
								java.util.logging.Logger.getLogger(Logger.class.getName()).logp(Level.INFO,
										Logger.class.getName(), "Server " + nodeId, logData);
								break;
						case LEVEL_WARNING:
								java.util.logging.Logger.getLogger(Logger.class.getName()).logp(Level.WARNING,
										Logger.class.getName(), "Server " + nodeId, logData);
								break;
						case LEVEL_SEVERE:
								java.util.logging.Logger.getLogger(Logger.class.getName()).logp(Level.SEVERE,
										Logger.class.getName(), "Server " + nodeId, logData);
								break;
				}

		}

		public static void print() {
				Iterator<Log> listIterator = logDB.iterator();
				Log log;
				while (listIterator.hasNext()) {
						log = listIterator.next();
						System.out.println(log.toString());
				}
		}

		public static synchronized void print(int[] nodeIds, int[] levels, int[] classIds, int[] locations) {
				System.out.println("processing quaries...");
				Iterator<Log> listIterator = logDB.iterator();
				Log log;
				boolean filterByNodeId = nodeIds != null && nodeIds.length != 0;
				boolean filterBylevel = levels != null && levels.length != 0;
				boolean filterByClass = classIds != null && classIds.length != 0;
				boolean filterByLocation = locations != null && locations.length != 0;
				boolean validToPrint;
				int recordCount = 0;
				while (listIterator.hasNext()) {
						log = listIterator.next();
						validToPrint = true;
						if (filterByNodeId) {
								validToPrint = false;
								for (int i : nodeIds) {
										if (log.nodeId == i) {
												validToPrint = true;
												break;
										}
								}
						}
						if (!validToPrint) {
								continue;
						}
						if (filterBylevel) {
								validToPrint = false;
								for (int i : levels) {
										if (log.level == i) {
												validToPrint = true;
												break;
										}
								}
						}
						if (!validToPrint) {
								continue;
						}
						if (filterByClass) {
								validToPrint = false;
								for (int i : classIds) {
										if (log.classId == i) {
												validToPrint = true;
												break;
										}
								}
						}
						if (!validToPrint) {
								continue;
						}
						if (filterByLocation) {
								validToPrint = false;
								for (int i : locations) {
										if (log.locId == i) {
												validToPrint = true;
												break;
										}
								}
						}
						if (!validToPrint) {
								continue;
						}
						recordCount++;
						System.out.println(log.toString());
				}
				System.out.println(logDB.size() + " records processed. " + recordCount + " matching records found.");
		}
}