/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dhtserverclient;

import dhtserverclient.thrift.Message;

/**
 *
 * @author Hiru
 */
public interface MessageHandler {
		
		abstract boolean queueMessage(Message message);
}
