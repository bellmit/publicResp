package com.insta.hms.slida;

import java.nio.ByteBuffer;
import java.util.Date;

public class NMessage extends SlidaMessage {

  private char gender;
  private String doctor;

  /**
   * Instantiates a new n message.
   *
   * @param cardIndexNo the card index no
   * @param name the name
   * @param firstName the first name
   * @param dob the dob
   * @param gender the gender
   * @param doctor the doctor
   */
  public NMessage(String cardIndexNo, String name, String firstName, Date dob, char gender,
      String doctor) {
    super('N', cardIndexNo, name, firstName, dob);
    this.gender = gender;
    this.doctor = doctor;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.slida.SlidaMessage#format()
   */
  @Override
  public byte[] format() {
    short bufferSize = getMessageLength();
    ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
    buffer.putShort(Short.reverseBytes(bufferSize)); // Change to LSB first
    buffer.putChar(Character.reverseBytes(getToken()));
    buffer.put(getName().getBytes()).put((byte) 0);
    buffer.put(getFirstName().getBytes()).put((byte) 0);
    buffer.put(getDob().getBytes()).put((byte) 0);
    buffer.put(getCardIndexNo().getBytes()).put((byte) 0);
    buffer.putChar(Character.reverseBytes(getGender()));
    buffer.put(getDoctor().getBytes()).put((byte) 0);
    buffer.put("\r\n".getBytes());
    buffer.rewind();
    return buffer.array();
  }

  /**
   * Gets the message length.
   *
   * @return the message length
   */
  public short getMessageLength() {
    // SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    short bufferSize = (short) (2 + // 2 bytes for storing the length
        1 + 1 + // token
        Math.min(getName().length(), 32) + 1 + // length + null terminator
        Math.min(getFirstName().length(), 32) + 1 + Math.min(getDob().length(), 10) + 1
        + Math.min(getCardIndexNo().length(), 20) + 1 + 1 + 1 + // gender
        Math.min(getDoctor().length(), 12) + 1 + "\r\n".length());
    return bufferSize;
  }

  public String getDoctor() {
    return getLimitedString(doctor, 12);
  }

  public char getGender() {
    return gender;
  }

}
