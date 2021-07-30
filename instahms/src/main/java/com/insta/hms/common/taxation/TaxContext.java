package com.insta.hms.common.taxation;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class TaxContext.
 *
 * @author irshadmohammed
 */
public class TaxContext {

  /** The supplier bean. */
  private BasicDynaBean supplierBean;

  /** The center bean. */
  private BasicDynaBean centerBean;

  /** The transaction id. */
  private String transactionId;

  /** The item bean. */
  private BasicDynaBean itemBean;

  /** The subgroups. */
  private List<BasicDynaBean> subgroups;

  /** The patient bean. */
  private BasicDynaBean patientBean;
  
  /** The visit bean. */
  private BasicDynaBean visitBean;

  /** The bill bean. */
  private BasicDynaBean billBean;

  /** The tpa bean. */
  private BasicDynaBean tpaBean;

  /**
   * Gets the transaction id.
   *
   * @return the transaction id
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Sets the transaction id.
   *
   * @param transactionId
   *          the new transaction id
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Gets the supplier bean.
   *
   * @return the supplier bean
   */
  public BasicDynaBean getSupplierBean() {
    return supplierBean;
  }

  /**
   * Sets the supplier bean.
   *
   * @param supplierBean
   *          the new supplier bean
   */
  public void setSupplierBean(BasicDynaBean supplierBean) {
    this.supplierBean = supplierBean;
  }

  /**
   * Gets the center bean.
   *
   * @return the center bean
   */
  public BasicDynaBean getCenterBean() {
    return centerBean;
  }

  /**
   * Sets the center bean.
   *
   * @param centerBean
   *          the new center bean
   */
  public void setCenterBean(BasicDynaBean centerBean) {
    this.centerBean = centerBean;
  }

  /**
   * Gets the item bean.
   *
   * @return the item bean
   */
  public BasicDynaBean getItemBean() {
    return itemBean;
  }

  /**
   * Sets the item bean.
   *
   * @param itemBean
   *          the new item bean
   */
  public void setItemBean(BasicDynaBean itemBean) {
    this.itemBean = itemBean;
  }

  /**
   * Gets the subgroups.
   *
   * @return the subgroups
   */
  public List<BasicDynaBean> getSubgroups() {
    return subgroups;
  }

  /**
   * Sets the subgroups.
   *
   * @param subgroups
   *          the new subgroups
   */
  public void setSubgroups(List<BasicDynaBean> subgroups) {
    this.subgroups = Collections.unmodifiableList(subgroups);
  }

  /**
   * Gets the patient bean.
   *
   * @return the patient bean
   */
  public BasicDynaBean getPatientBean() {
    return patientBean;
  }

  /**
   * Sets the patient bean.
   *
   * @param patientBean
   *          the new patient bean
   */
  public void setPatientBean(BasicDynaBean patientBean) {
    this.patientBean = patientBean;
  }

  /**
   * Gets current visit bean.
   * 
   * @return the visit bean
   */
  public BasicDynaBean getVisitBean() {
    return visitBean;
  }

  /**
   * Sets visit bean.
   * 
   * @param visitBean
   *          the new visit bean
   */
  public void setVisitBean(BasicDynaBean visitBean) {
    this.visitBean = visitBean;
  }

  /**
   * Gets the bill bean.
   *
   * @return the bill bean
   */
  public BasicDynaBean getBillBean() {
    return billBean;
  }

  /**
   * Sets the bill bean.
   *
   * @param billBean
   *          the new bill bean
   */
  public void setBillBean(BasicDynaBean billBean) {
    this.billBean = billBean;
  }

  /**
   * Gets the tpa bean.
   *
   * @return the tpa bean
   */
  public BasicDynaBean getTpaBean() {
    return tpaBean;
  }

  /**
   * Sets the tpa bean.
   *
   * @param tpaBean
   *          the new tpa bean
   */
  public void setTpaBean(BasicDynaBean tpaBean) {
    this.tpaBean = tpaBean;
  }

}
