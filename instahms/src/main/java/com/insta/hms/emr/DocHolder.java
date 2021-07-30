package com.insta.hms.emr;

import java.util.ArrayList;
import java.util.List;

public class DocHolder implements Comparable {

  private String filterId;
  private String label;
  private List<EMRDoc> viewDocs = new ArrayList<>();

  public String getFilterId() {
    return filterId;
  }

  public void setFilterId(String filterId) {
    this.filterId = filterId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<EMRDoc> getViewDocs() {
    return viewDocs;
  }

  public void setViewDocs(List<EMRDoc> viewDocs) {
    this.viewDocs = viewDocs;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((filterId == null) ? 0 : filterId.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((viewDocs == null) ? 0 : viewDocs.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DocHolder) {
      DocHolder doc = (DocHolder) obj;
      return filterId.equals(doc.getFilterId());
    }
    return false;
  }

  public int compareTo(Object arg0) {
    // TODO Auto-generated method stub
    return 0;
  }

}
