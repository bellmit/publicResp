package com.insta.hms.slida;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class SlidaMessage {

  private char token;
  private String name;
  private String firstName;
  private Date dob;
  private String cardIndexNo;

  public abstract byte[] format();

  /**
   * Instantiates a new sidexis message.
   *
   * @param token the token
   * @param cardIndexNo the card index no
   * @param name the name
   * @param firstName the first name
   * @param dob the dob
   */
  public SlidaMessage(char token, String cardIndexNo, String name, String firstName, Date dob) {
    this.token = token;
    this.cardIndexNo = cardIndexNo;
    this.name = name;
    this.firstName = firstName;
    this.dob = dob;
  }

  public String getCardIndexNo() {
    return getLimitedString(cardIndexNo, 20);
  }

  /**
   * Gets the date of birth.
   *
   * @return the dob
   */
  public String getDob() {
    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    String formattedDob = "";
    if (null != dob) {
      formattedDob = df.format(dob);
    }
    return formattedDob;
  }

  public String getFirstName() {
    return getLimitedString(firstName, 32);
  }

  public String getName() {
    return getLimitedString(name, 32);
  }

  public char getToken() {
    return token;
  }

  protected String getLimitedString(String str, int idx) {
    return (null == str) ? "" : (idx < 0 || idx > str.length()) ? str : (str.substring(0, idx));
  }

}
