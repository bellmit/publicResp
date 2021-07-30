package com.bob.hms.common;

import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import java.math.BigDecimal;
import java.sql.SQLException;

public class NumberToStringConversionMillionIndoLang {

  String[] ones = null;
  String[] tens = null;
  String[] elevens = null;
  String[] hundreds = null;
  String sep = null;

  /**
   * Convert Integer or float to words in bahasa.
   * @param one value for one
   * @param ten  value for ten
   * @param eleven  value for eleven
   * @param hundred  value for hundred
   */
  public NumberToStringConversionMillionIndoLang(String one, String ten, String eleven,
      String hundred) {
    int len = one.split(",").length - 1;
    this.ones = new String[one.split(",").length - 1];
    this.sep = one.split(",")[0]; // first element is the separator.
    System.arraycopy(one.split(","), 1, this.ones, 0, len);
    for (String s : ones) {
      System.out.println("==" + s);
    }

    this.tens = ten.split(",");
    this.elevens = eleven.split(",");
    this.hundreds = hundred.split(",");
    this.sep = sep;
  }

  /**
   * Convert to word.
   * @param amount amount value
   * @return amount in words
   */
  public String toWord(long amount) {

    String strAmount = "";
    int position = 1;
    int reminder = 0;

    if (amount == 0) {
      return "Zero";
    }
    if (amount < 0) {
      amount = -1 * amount;
    }

    while (amount > 0) {
      switch (position) {

        case 1:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = getStrAmount(reminder) + strAmount;
          }
          if (amount > 100 && reminder != 0) {
            strAmount = " " + strAmount;
          }
          amount = amount / 100;
          break;

        case 2:
          reminder = (int) (amount % 10);
          if (reminder != 0) {
            strAmount = " " + hundreds[0] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 10;
          break;

        case 3:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[1] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;

        case 4:
          reminder = (int) (amount % 10);
          if (reminder == 0) {
            strAmount = "" + strAmount;
          }
          if (reminder != 0 && !(strAmount.contains(hundreds[1]))) {
            strAmount = " " + hundreds[4] + " " + strAmount;
          } else if (reminder != 0) {
            strAmount = " " + hundreds[0] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 10;
          break;
        case 5:
          int divisor = (int) amount;
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[5] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;

        case 6:
          reminder = (int) (amount % 10);
          if (reminder != 0 && !(strAmount.contains(hundreds[5]))) {
            strAmount = " " + hundreds[0] + " " + hundreds[5] + " " + strAmount;
          } else if (reminder != 0) {
            strAmount = " " + hundreds[0] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 10;
          break;
        case 7:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[6] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;
        case 8:
          reminder = (int) (amount % 10);
          if (reminder != 0) {
            strAmount = " " + hundreds[0] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 10;
          break;
        case 9:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[7] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;
        default:
      }
      position = position + 1;
    }
    return strAmount;
  }

  /**
   * Get Str Amount.
   * @param reminder reminder value
   * @return amount in words
   */
  private String getStrAmount(int reminder) {

    String amount = "";

    if (reminder < 10 && reminder > 0) {
      amount = ones[reminder - 1];
    } else if (reminder < 20 && reminder > 0) {
      amount = elevens[reminder - 10];
    } else if (reminder > 19 && reminder > 0) {
      int reminderTens = reminder / 10;
      reminder = reminder % 10;
      amount = tens[reminderTens - 2] + " " + getStrAmount(reminder);
    } else if (reminder != 0) {
      int reminderOnes = reminder / 10;
      amount = ones[reminderOnes] + " " + tens[reminderOnes - 2] + getStrAmount(reminderOnes);
    }
    return amount;
  }

  /**
   * Convert BigDecimal to Major and Minor currency word representation.
   * @param amount amount value
   * @return String representing value in words
   * @throws SQLException SQL Exception
   */
  public String toRupeesPaise(BigDecimal amount) throws SQLException {
    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    int numOfDecimals = dto.getDecimalDigits();
    int paiseDividedBy = new Double(Math.pow(10, numOfDecimals)).intValue();
    if (amount == null) {
      return null;
    }
    amount = amount.abs();
    long rupeeValue = amount.longValue();
    long paiseValue = amount.multiply(new BigDecimal(paiseDividedBy)).longValue() % paiseDividedBy;

    String strRupees = toWord(rupeeValue) + " " + dto.getWhole();
    String strPaise = "";
    if (paiseValue > 0) {
      strPaise = " " + this.sep + " " + toWord(paiseValue) + " " + dto.getDecimal();
    }

    return strRupees + strPaise;

  }
}
