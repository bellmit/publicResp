package com.insta.hms.instasubscriptions;

import com.chargebee.Environment;
import com.chargebee.ListResult;
import com.chargebee.Result;
import com.chargebee.models.Invoice;
import com.chargebee.models.Subscription;
import com.chargebee.models.enums.PaymentMethod;
import com.insta.hms.common.ConfigManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * The Class ChargebeeService.
 */
public class ChargebeeService {

  /** The props. */
  static Properties props = ConfigManager.getInstance().getProps();

  /** The log. */
  Logger log = LoggerFactory.getLogger(ChargebeeService.class);

  /** The Constant encryptionKey. */
  // HEX of Secret Key
  private static final String encryptionKey = "864E7B0121F8A93EDD47C68C733C5880";

  /** The chargebee dao. */
  InstaSubscriptionsDAO chargebeeDao = new InstaSubscriptionsDAO();

  /** The Constant dueInvoiceLimit. */
  private static final Integer dueInvoiceLimit = 100;

  /** The Constant paidInvoiceLimit. */
  private static final Integer paidInvoiceLimit = 5;

  /** The Constant otherInvoiceLimit. */
  private static final Integer otherInvoiceLimit = 100;

  /** The cipher. */
  static Cipher cipher;

  /**
   * Gets the customer id.
   *
   * @return the customer id
   * @throws SQLException
   *           the SQL exception
   */
  public String getCustomerId() throws SQLException {
    return (String) chargebeeDao.getChargebeeDetails().get("chargebee_customer_id");
  }

  /**
   * Gets the payment method.
   *
   * @param method
   *          the method
   * @return the payment method
   */
  public PaymentMethod getPaymentMethod(String method) {
    if (method.equals("bank_transfer")) {
      return PaymentMethod.BANK_TRANSFER;
    }
    if (method.equals("cash")) {
      return PaymentMethod.CASH;
    }
    if (method.equals("check")) {
      return PaymentMethod.CHECK;
    }
    if (method.equals("other")) {
      return PaymentMethod.OTHER;
    }
    return null;
  }

  /**
   * Gets the due invoice list.
   *
   * @return the due invoice list
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ListResult getDueInvoiceList() throws SQLException, IOException {
    ListResult invoiceList = Invoice.list().customerId().is(getCustomerId()).limit(dueInvoiceLimit)
        .amountDue().gt(0).request();
    return invoiceList;
  }

  /**
   * Gets the paid invoice list.
   *
   * @param offset
   *          the offset
   * @return the paid invoice list
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ListResult getPaidInvoiceList(String offset) throws IOException, SQLException {
    ListResult invoiceList = Invoice.list().customerId().is(getCustomerId()).limit(paidInvoiceLimit)
        .offset(offset).amountDue().is(0).request();
    return invoiceList;
  }

  /**
   * Gets the other invoice list.
   *
   * @return the other invoice list
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ListResult getOtherInvoiceList() throws IOException, SQLException {
    ListResult invoiceList = Invoice.list().customerId().is(getCustomerId()).recurring().is(false)
        .limit(otherInvoiceLimit).amountDue().gt(0).request();
    return invoiceList;
  }

  /**
   * Gets the customer subscription list.
   *
   * @return the customer subscription list
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ListResult getCustomerSubscriptionList() throws IOException, SQLException {
    ListResult customerSubscriptionList = Subscription.list().customerId().is(getCustomerId())
        .status().is(com.chargebee.models.Subscription.Status.ACTIVE).request();
    return customerSubscriptionList;
  }

  /**
   * Record payment.
   *
   * @param invoiceId
   *          the invoice id
   * @param recordAmount
   *          the record amount
   * @param paymentMethod
   *          the payment method
   * @param refNo
   *          the ref no
   * @param comments
   *          the comments
   * @param paymentDate
   *          the payment date
   * @return the result
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   */
  public Result recordPayment(String invoiceId, Integer recordAmount, String paymentMethod,
      String refNo, String comments, String paymentDate) throws IOException, ParseException {
    DateFormat formatter;
    formatter = new SimpleDateFormat("dd-MM-yyyy");
    Date date = (Date) formatter.parse(paymentDate);
    Result result = Invoice.recordPayment(invoiceId).comment(comments)
        .transactionAmount(recordAmount).transactionPaymentMethod(getPaymentMethod(paymentMethod))
        .transactionReferenceNumber(refNo).transactionDate(new Timestamp(date.getTime())).request();
    return result;
  }

  /**
   * Gets the invoice pdf.
   *
   * @param invoiceId
   *          the invoice id
   * @return the invoice pdf
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Result getInvoicePdf(String invoiceId) throws IOException {
    return Invoice.pdf(invoiceId).request();
  }

  /**
   * Sets the chargebee environment.
   *
   * @throws SQLException
   *           the SQL exception
   */
  public void setChargebeeEnvironment() throws SQLException {
    String decryptedApiKey = null;
    String encryptedApiKey = null;
    String siteName = null;
    String propertyPrefix = (String) chargebeeDao.getChargebeeDetails().get("aeskey");
    encryptedApiKey = props.getProperty(propertyPrefix + ".key");
    siteName = props.getProperty(propertyPrefix + ".site");
    try {
      decryptedApiKey = decryptText(encryptedApiKey);
    } catch (Exception ex) {
      log.error("", ex);
    }
    Environment.configure(siteName, decryptedApiKey);
  }

  /**
   * Decrypt text.
   *
   * @param encryptedApiKey
   *          the encrypted api key
   * @return the string
   * @throws Exception
   *           the exception
   */
  public static String decryptText(String encryptedApiKey) throws Exception {
    byte[] secKeyByte = DatatypeConverter.parseHexBinary(encryptionKey);
    SecretKey secKey = new SecretKeySpec(secKeyByte, 0, secKeyByte.length, "AES");
    Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
    aesCipher.init(Cipher.DECRYPT_MODE, secKey);
    byte[] bytePlainText = aesCipher.doFinal(DatatypeConverter.parseHexBinary(encryptedApiKey));
    return new String(bytePlainText);
  }

}
