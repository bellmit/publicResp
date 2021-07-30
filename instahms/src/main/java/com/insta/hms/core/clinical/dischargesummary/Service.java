package com.insta.hms.core.clinical.dischargesummary;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anup vishwas.
 *
 */
public class Service {

  private BasicDynaBean serviceDetails;
  private String serviceName;
  private String format;
  private String notes;
  private List hvfValues = new ArrayList();
  private String richTextContent;

  public Service(String serviceName, String notes) {
    this.serviceName = serviceName;
    this.notes = notes;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getNotes() {
    return notes;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setRichTextContent(String richTextContent) {
    this.richTextContent = richTextContent;
  }

  public List getHvfValues() {
    return hvfValues;
  }

  public BasicDynaBean getServiceDetails() {
    return serviceDetails;
  }

  public void setServiceDetails(BasicDynaBean serviceDetails) {
    this.serviceDetails = serviceDetails;
  }

}
