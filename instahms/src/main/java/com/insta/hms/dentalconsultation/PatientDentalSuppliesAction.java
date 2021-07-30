package com.insta.hms.dentalconsultation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.dentalshades.DentalShadesMasterDAO;
import com.insta.hms.master.dentalsupplieritemratemaster.DentalSupplierItemRateMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PatientDentalSuppliesAction extends DispatchAction {

  static DentalSuppliesOrderDao orderDao = new DentalSuppliesOrderDao();
  static DentalSuppliesItemDao itemDao = new DentalSuppliesItemDao();
  static DentalShadesMasterDAO shadesDao = new DentalShadesMasterDAO();

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ServletException, IOException {
    String mrNo = request.getParameter("mr_no");
    Map patient = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    List items = null;
    List doctors = Collections.EMPTY_LIST;
    List shades = Collections.EMPTY_LIST;
    JSONSerializer js = new JSONSerializer().exclude(".class");
    if (request.getParameter("emptyScreen") == null) {
      if (patient == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.error("Invalid MR. No: " + mrNo);
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter("emptyScreen", true);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }

      request.setAttribute("orders", DentalSuppliesOrderDao.getOrderDetails(mrNo));
      request.setAttribute("supplies", ConversionUtils
          .listBeanToMapListMap(DentalSuppliesItemDao.getItemDetails(mrNo), "supplies_order_id"));
      doctors = ConversionUtils.copyListDynaBeansToMap(DoctorMasterDAO.getDentalDoctors());
      items = ConversionUtils.copyListDynaBeansToMap(DentalSupplierItemRateMasterDAO.getItems());
      shades = ConversionUtils.copyListDynaBeansToMap(shadesDao.listAll());
    }
    request.setAttribute("doctors", js.deepSerialize(doctors));
    request.setAttribute("items", js.deepSerialize(items));
    request.setAttribute("shades", js.deepSerialize(shades));
    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    return mapping.findForward("show");
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException {
    Map params = request.getParameterMap();
    String[] supplierId = request.getParameterValues("h_supplier_id");
    String[] delete = request.getParameterValues("h_delete");
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    String error = null;
    String mrNo = request.getParameter("mr_no");
    boolean flag = false;
    try {
      txn: {
        if (orderDao.findByKey(con, "mr_no", mrNo) != null) {
          if (itemDao.itemsExists(con, mrNo, -1) && !itemDao.deleteItems(con, mrNo, -1)) {
            error = "Failed to delete the Supplies..";
            break txn;
          }
          if (!orderDao.delete(con, "mr_no", mrNo)) {
            error = "Failed to delete the Supplier Order main details..";
            break txn;
          }
        }
        for (int i = 0; i < supplierId.length - 1; i++) {
          if (new Boolean(delete[i])) {
            continue;
          }

          BasicDynaBean bean = orderDao.getBean();
          List errorFields = new ArrayList();
          ConversionUtils.copyIndexToDynaBeanPrefixed(params, i, bean, errorFields, "h_");
          if (errorFields.isEmpty()) {
            int suppliesOrderId = orderDao.getNextSequence();
            bean.set("supplies_order_id", suppliesOrderId);
            bean.set("mr_no", mrNo);
            if (!orderDao.insert(con, bean)) {
              error = "Failed to insert the Supplier Order Details";
              break txn;
            }
            String orderIndex = request.getParameterValues("h_order_index")[i];
            String[] supplies = request.getParameterValues("h_" + orderIndex + "_item_id");
            if (supplies != null) {
              for (int s = 0; s < supplies.length; s++) {
                if (supplies[s] != null && !supplies[s].equals("")) {
                  BasicDynaBean itemBean = itemDao.getBean();
                  ConversionUtils.copyIndexToDynaBeanPrefixed(params, s, itemBean, errorFields,
                      "h_" + orderIndex + "_");
                  if (errorFields.isEmpty()) {
                    itemBean.set("supplies_order_id", suppliesOrderId);
                    itemBean.set("supplies_order_item_id", itemDao.getNextSequence());
                    if (!itemDao.insert(con, itemBean)) {
                      error = "Failed to insert the Supplies details..";
                      break txn;
                    }
                  } else {
                    error = "Incorrectly formatted values supplied..";
                    break txn;
                  }
                }
              }
            }
          } else {
            error = "Incorrectly formatted values supplied..";
            break txn;
          }
        }
        flag = true;
      }
    } finally {
      DataBaseUtil.commitClose(con, flag);
    }
    FlashScope flash = FlashScope.getScope(request);
    flash.error(error);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("mr_no", mrNo);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

}
