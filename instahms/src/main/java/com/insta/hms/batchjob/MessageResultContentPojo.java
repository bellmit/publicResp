package com.insta.hms.batchjob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResultContentPojo {

  private String status;
  private String delivery_time;
  private int attempt;
  private String app_name;
  private String mobile;
  private String message_text;
  private String operator_code;
  private String reference_type;
  private String failure_reason;
  private int reference_id;
  private String send_time;
  private String operator;
  private String notes;
  private String operator_reply;
  private String message_type;
  private int id;
  private String name;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getAttempt() {
    return attempt;
  }

  public void setAttempt(int attempt) {
    this.attempt = attempt;
  }

  public String getApp_name() {
    return app_name;
  }

  public void setApp_name(String app_name) {
    this.app_name = app_name;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getMessage_text() {
    return message_text;
  }

  public void setMessage_text(String message_text) {
    this.message_text = message_text;
  }

  public String getOperator_code() {
    return operator_code;
  }

  public void setOperator_code(String operator_code) {
    this.operator_code = operator_code;
  }

  public String getReference_type() {
    return reference_type;
  }

  public void setReference_type(String reference_type) {
    this.reference_type = reference_type;
  }

  public String getFailure_reason() {
    return failure_reason;
  }

  public void setFailure_reason(String failure_reason) {
    this.failure_reason = failure_reason;
  }

  public int getReference_id() {
    return reference_id;
  }

  public void setReference_id(int reference_id) {
    this.reference_id = reference_id;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getOperator_reply() {
    return operator_reply;
  }

  public void setOperator_reply(String operator_reply) {
    this.operator_reply = operator_reply;
  }

  public String getMessage_type() {
    return message_type;
  }

  public void setMessage_type(String message_type) {
    this.message_type = message_type;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDelivery_time() {
    return delivery_time;
  }

  public void setDelivery_time(String delivery_time) {
    this.delivery_time = delivery_time;
  }

  public String getSend_time() {
    return send_time;
  }

  public void setSend_time(String send_time) {
    this.send_time = send_time;
  }

}
