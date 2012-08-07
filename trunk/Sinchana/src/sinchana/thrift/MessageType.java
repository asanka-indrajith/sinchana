/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package sinchana.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum MessageType implements org.apache.thrift.TEnum {
  STORE_DATA(0),
  DELETE_DATA(1),
  GET_DATA(2),
  RESPONSE_DATA(3),
  ACKNOWLEDGE_DATA(4),
  ACKNOWLEDGE_REMOVE(5),
  PUBLISH_SERVICE(6),
  GET_SERVICE(7),
  REMOVE_SERVICE(8),
  RESPONSE_SERVICE(9),
  ACKNOWLEDGE_SERVICE(10),
  FAILURE_SERVICE(11),
  REQUEST(12),
  RESPONSE(13),
  ERROR(14),
  JOIN(15),
  DISCOVER_NEIGHBOURS(16),
  FIND_SUCCESSOR(17),
  VERIFY_RING(18),
  TEST_RING(19);

  private final int value;

  private MessageType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static MessageType findByValue(int value) { 
    switch (value) {
      case 0:
        return STORE_DATA;
      case 1:
        return DELETE_DATA;
      case 2:
        return GET_DATA;
      case 3:
        return RESPONSE_DATA;
      case 4:
        return ACKNOWLEDGE_DATA;
      case 5:
        return ACKNOWLEDGE_REMOVE;
      case 6:
        return PUBLISH_SERVICE;
      case 7:
        return GET_SERVICE;
      case 8:
        return REMOVE_SERVICE;
      case 9:
        return RESPONSE_SERVICE;
      case 10:
        return ACKNOWLEDGE_SERVICE;
      case 11:
        return FAILURE_SERVICE;
      case 12:
        return REQUEST;
      case 13:
        return RESPONSE;
      case 14:
        return ERROR;
      case 15:
        return JOIN;
      case 16:
        return DISCOVER_NEIGHBOURS;
      case 17:
        return FIND_SUCCESSOR;
      case 18:
        return VERIFY_RING;
      case 19:
        return TEST_RING;
      default:
        return null;
    }
  }
}
