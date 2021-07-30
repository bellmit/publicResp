/**
 *
 */

package com.bob.hms.common;

import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * The Class NumberToStringConversionLakh.
 *
 * @author krishna
 */
public class NumberToStringConversionLakh extends NumberToWordFormat {
  // static String ones[]={"One","Two","Three","Four","Five","Six","Seven","Eight","Nine"};
  // static String
  // tens[]={"Twenty","Thirty","Forty","Fifty","Sixty","Seventy","Eighty","Ninety","Twenty"};
  // static String
  // elevens[]={"Ten","Eleven","Twelve","Thirteen","Fourteen","Fifteen","Sixteen",
  // "Seventeen","Eighteen","Nineteen"};
  // static String hundreds[]={"Hundred","Thousand","Lakh","Crore"};

  /*
   * (non-Javadoc)
   * 
   * @see com.bob.hms.common.NumberToWordFormat#toWord(java.math.BigDecimal)
   */
  @Override
  public String toWord(BigDecimal amount) throws SQLException {

    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    int numOfDecimals = dto.getDecimalDigits();
    int paiseDividedBy = new Double(Math.pow(10, numOfDecimals)).intValue();
    if (amount == null) {
      return null;
    }
    amount = amount.abs();
    long rupeeValue = amount.longValue();
    long decimalValue = amount.multiply(new BigDecimal(paiseDividedBy)).longValue()
        % paiseDividedBy;

    String strRupees = toWord(rupeeValue);
    String strPaise = "";
    int index = numOfDecimals;
    if (index > 0) {
      index = index - 1;
    }
    if (decimalValue > 0) {
      strPaise = " " + super.decimalSeparator + " " + toWord(decimalValue) + " "
          + (index >= 0 ? decimalsInWords[index] : "");
    }

    return strRupees + strPaise;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.bob.hms.common.NumberToWordFormat#toWord(java.math.BigDecimal)
   */
  @Override
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
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[2] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;

        case 5:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[3] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;

        case 6:
          reminder = (int) (amount % 10);
          if (reminder != 0) {
            strAmount = " " + hundreds[0] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 10;
          break;

        case 7:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[1] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;

        case 8:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[2] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;

        case 9:
          reminder = (int) (amount % 100);
          if (reminder != 0) {
            strAmount = " " + hundreds[3] + " " + strAmount;
          }
          strAmount = getStrAmount(reminder) + strAmount;
          amount = amount / 100;
          break;
        default:
          break;

      }
      position = position + 1;
    }
    return strAmount;
  }

  /**
   * Gets the str amount.
   *
   * @param reminder the reminder
   * @return amount
   */

  private String getStrAmount(int reminder) {

    String amount = "";

    if (reminder < 10 && reminder > 0) {
      amount = ones[reminder - 1];
    } else if (reminder < 20 && reminder > 0) {
      amount = elevens[reminder - 10];
    } else if (reminder > 19 && reminder > 0) {
      int temp = reminder / 10;
      reminder = reminder % 10;
      amount = tens[temp - 2] + " " + getStrAmount(reminder);
    } else if (reminder != 0) {
      int temp = reminder / 10;
      amount = ones[temp] + " " + tens[temp - 2] + getStrAmount(temp);
    }
    return amount;
  }

  @Override
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
      strPaise = " " + super.sep + " " + toWord(paiseValue) + " " + dto.getDecimal();
    }

    return strRupees + strPaise;
  }

}
