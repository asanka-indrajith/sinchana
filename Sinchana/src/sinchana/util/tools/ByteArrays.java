/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sinchana.util.tools;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import sinchana.thrift.Node;

/**
 *
 * @author Hiru
 */
public class ByteArrays {

	public static String idToReadableString(Node node) {
		return toReadableString(node.serverId.array()).toUpperCase();
	}

	public static String toReadableString(ByteBuffer byteBuffer) {
		return toReadableString(byteBuffer.array());
	}

	public static String toReadableString(byte[] arrayToRead) {
		return new BigInteger(1, arrayToRead).toString(16);
	}
	
	public static byte[] arrayConcat(byte[] array1, byte[] array2) {
		int pos = array1.length;
		byte[] newArray = Arrays.copyOf(array1, pos + array2.length);
		for (byte b : array2) {
			newArray[pos++] = b;
		}
		return newArray;
	}
}
