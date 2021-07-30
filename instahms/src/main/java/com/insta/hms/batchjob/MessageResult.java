package com.insta.hms.batchjob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResult {
  private List<MessageResultContentPojo> result;

  public List<MessageResultContentPojo> getResult() {
    return result;
  }

  public void setResult(List<MessageResultContentPojo> result) {
    this.result = result;
  }

  @Override
  public String toString() {
    return "res [result=" + result + "]";
  }

}
