/**
 *
 */
package com.insta.hms.ipservices;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class PrescriptionViewForm.
 *
 * @author sirisha.rachkonda
 */
public class PrescriptionViewForm extends ActionForm {
  
  /** The quantityvalue. */
  private int[] quantityvalue;
  
  /** The quantityid. */
  private String[] quantityid;
  
  /** The chargehead. */
  private String[] chargehead;

  /**
   * Gets the chargehead.
   *
   * @return the chargehead
   */
  public String[] getChargehead() {
    return chargehead;
  }

  /**
   * Sets the chargehead.
   *
   * @param chargehead the new chargehead
   */
  public void setChargehead(String[] chargehead) {
    this.chargehead = chargehead;
  }

  /**
   * Gets the quantityid.
   *
   * @return the quantityid
   */
  public String[] getQuantityid() {
    return quantityid;
  }

  /**
   * Sets the quantityid.
   *
   * @param quantityid the new quantityid
   */
  public void setQuantityid(String[] quantityid) {
    this.quantityid = quantityid;
  }

  /* (non-Javadoc)
   * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
   */
  public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
    return super.validate(arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
   */
  public void reset(ActionMapping arg0, HttpServletRequest arg1) {
    try {
      arg1.setCharacterEncoding("UTF-8");
    } catch (Exception e) {
    }

    super.reset(arg0, arg1);
    this.quantityvalue = null;
    this.quantityid = null;
    this.chargehead = null;

  }

  /**
   * Gets the quantityvalue.
   *
   * @return the quantityvalue
   */
  public int[] getQuantityvalue() {
    return quantityvalue;
  }

  /**
   * Sets the quantityvalue.
   *
   * @param quantityvalue the new quantityvalue
   */
  public void setQuantityvalue(int[] quantityvalue) {
    this.quantityvalue = quantityvalue;
  }

}
