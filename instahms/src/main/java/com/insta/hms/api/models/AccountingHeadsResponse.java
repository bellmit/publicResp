package com.insta.hms.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class AccountingHeadsResponse.
 */
@JsonInclude(Include.NON_NULL)
public class AccountingHeadsResponse {

  /**
   * The Enum ReturnCode.
   */
  public enum ReturnCode {

    /** The no content. */
    NO_ACCOUNTING_HEADERS("2002", "No Accounting headers found for the request criteria"),
    /** The success. */
    SUCCESS("2001", "Success");

    /** The return code. */
    private String returnCode;

    /** The return message. */
    private String returnMessage;

    /**
     * Instantiates a new return code.
     *
     * @param returnCode the return code
     * @param returnMessage the return message
     */
    private ReturnCode(String returnCode, String returnMessage) {
      this.returnCode = returnCode;
      this.returnMessage = returnMessage;
    }

    /**
     * Gets the return code.
     *
     * @return the return code
     */
    public String getReturnCode() {
      return this.returnCode;
    }

    /**
     * Gets the return message.
     *
     * @return the return message
     */
    public String getReturnMessage() {
      return this.returnMessage;
    }
  }

  /** The accounting heads. */
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "AccountingHead")
  List<AccountingHead> accountingHeads;

  /** The return message. */
  public String return_message;

  /** The return code. */
  public String return_code;


  /**
   * Gets the return message.
   *
   * @return the return_message
   */
  public String getReturn_message() {
    return return_message;
  }



  /**
   * Sets the return message.
   *
   * @param return_message the return_message to set
   */
  public void setReturn_message(String return_message) {
    this.return_message = return_message;
  }



  /**
   * Gets the return code.
   *
   * @return the return_code
   */
  public String getReturn_code() {
    return return_code;
  }



  /**
   * Sets the return code.
   *
   * @param return_code the return_code to set
   */
  public void setReturn_code(String return_code) {
    this.return_code = return_code;
  }

  /**
   * Gets the accounting heads.
   *
   * @return the accountingHeads
   */
  public List<AccountingHead> getAccountingHeads() {
    return accountingHeads;
  }



  /**
   * Sets the accounting heads.
   *
   * @param accountingHeads the accountingHeads to set
   */
  public void setAccountingHeads(List<AccountingHead> accountingHeads) {
    this.accountingHeads = accountingHeads;
  }

  /**
   * Adds the accounting head.
   *
   * @param accountingHead the accounting head
   */
  public void addAccountingHead(AccountingHead accountingHead) {
    if (accountingHeads == null) {
      accountingHeads = new ArrayList<>();
    }
    accountingHeads.add(accountingHead);
  }


  /**
   * The Class AccountingHead.
   */
  public static class AccountingHead {

    /** The account head id. */
    @JsonProperty(value = "account_head_id")
    @JacksonXmlProperty(localName = "AccountHeadId")
    Integer accountHeadId;

    /** The account head name. */
    @JsonProperty(value = "account_head_name")
    @JacksonXmlProperty(localName = "AccountHeadName")
    String accountHeadName;

    /** The status. */
    @JsonProperty(value = "status")
    @JacksonXmlProperty(localName = "Status")
    String status;

    /**
     * Instantiates a new accounting head.
     *
     * @param accountingHead the accounting head
     */
    public AccountingHead(Map<String, Object> accountingHead) {
      this.accountHeadId = (Integer) accountingHead.get("account_head_id");
      this.accountHeadName = (String) accountingHead.get("account_head_name");
      this.status = "A".equals((String) accountingHead.get("status")) ? "Active" : "Inactive";
    }
  }


  /**
   * Post process. Sets return code and return status based on content returned.
   */
  public void postProcess() {
    ReturnCode returnCode;
    if (CollectionUtils.isEmpty(this.accountingHeads)) {
      returnCode = ReturnCode.NO_ACCOUNTING_HEADERS;
    } else {
      returnCode = ReturnCode.SUCCESS;
    }
    this.setReturn_code(returnCode.getReturnCode());
    this.setReturn_message(returnCode.getReturnMessage());
  }
}
