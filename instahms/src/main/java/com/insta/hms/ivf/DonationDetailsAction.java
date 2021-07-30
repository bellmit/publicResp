package com.insta.hms.ivf;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DonationDetailsAction.
 */
public class DonationDetailsAction extends DispatchAction {
  
  private static final GenericDAO ivfDonorDetailsDAO = new GenericDAO("ivf_donor_details");
  
  /**
   * Gets the donation details.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the donation details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward getDonationDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ParseException, IOException {
    String mrNo = request.getParameter("mr_no");
    List<BasicDynaBean> list = ivfDonorDetailsDAO.findAllByKey("donor_mr_no", mrNo);
    request.setAttribute("donationdetails", list);
    return mapping.findForward("donationDetails");

  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException {
    String mrNo = request.getParameter("mr_no");
    String[] isDeleted = request.getParameterValues("selectedrow");
    Map requestParams = request.getParameterMap();
    ArrayList errorFields = new ArrayList();
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = ivfDonorDetailsDAO.delete(con, "donor_mr_no", mrNo);
      for (int i = 0; i < request.getParameterValues("recipient_mr_no").length - 1; i++) {
        BasicDynaBean bean = ivfDonorDetailsDAO.getBean();
        ConversionUtils.copyIndexToDynaBean(requestParams, i, bean, errorFields);
        bean.set("donor_mr_no", mrNo);
        if (isDeleted[i].equalsIgnoreCase("false")) {
          success = ivfDonorDetailsDAO.insert(con, bean);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    return redirect;
  }
}
