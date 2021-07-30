package com.insta.hms.slida;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AMessage extends SlidaMessage {

  private String stationName;

  public AMessage(String cardIndexNo, String name, String firstName, Date dob, String stationName) {
    super('A', cardIndexNo, name, firstName, dob);
    this.stationName = stationName;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.slida.SlidaMessage#format()
   */
  @Override
  public byte[] format() {
    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    SimpleDateFormat tf = new SimpleDateFormat("kk:mm:ss");
    Date today = new Date();
    final String dateOfCall = df.format(today);
    final String timeOfCall = tf.format(today);
    short bufferSize = getMessageLength();
    ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
    buffer.putShort(Short.reverseBytes(bufferSize));
    buffer.putChar(Character.reverseBytes(getToken()));
    buffer.put(getName().getBytes()).put((byte) 0);
    buffer.put(getFirstName().getBytes()).put((byte) 0);
    buffer.put(getDob().getBytes()).put((byte) 0);
    buffer.put(getCardIndexNo().getBytes()).put((byte) 0);
    buffer.put(getStationName().getBytes()).put((byte) 0);
    buffer.put(dateOfCall.getBytes()).put((byte) 0);
    buffer.put(timeOfCall.getBytes()).put((byte) 0);
    buffer.put(getSender().getBytes()).put((byte) 0);
    buffer.put(getReciever().getBytes()).put((byte) 0);
    buffer.put("0".getBytes()).put((byte) 0); // image number, always zero
    buffer.put("\r\n".getBytes());
    buffer.rewind();
    return buffer.array();
  }

  private short getMessageLength() {
    short bufferSize = (short) (2 + // 2 bytes for storing the length
        1 + 1 + // token
        Math.min(getName().length(), 32) + 1 + // length + null terminator
        Math.min(getFirstName().length(), 32) + 1 + Math.min(getDob().length(), 10) + 1
        + Math.min(getCardIndexNo().length(), 20) + 1 + Math.min(getStationName().length(), 20) + 1
        + 10 + 1 + // Date of call
        8 + 1 + // time of call
        Math.min(getSender().length(), 20) + 1 + Math.min(getReciever().length(), 20) + 1 + 1 + 1
        // for image number, 0 always, since we don't have image number
        + "\r\n".length());
    return bufferSize;
  }

  public String getStationName() {
    return this.getLimitedString(stationName, 9);
  }

  public String getSender() {
    return "\\\\" + getStationName() + "\\InstaHMS";
  }

  public String getReciever() {
    return "\\\\" + getStationName() + "\\*";
  }

}
