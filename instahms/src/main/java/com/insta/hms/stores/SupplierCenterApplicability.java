package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SupplierCenterApplicability extends DispatchAction {

  static SupplierCenterDAO supplierCenterDAO = new SupplierCenterDAO();

  @IgnoreConfidentialFilters
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException {
    String suppID = request.getParameter("supplier_id");
    request.setAttribute("supplier_bean", SupplierMasterDAO.getSelectedSuppDetails(suppID));
    request.setAttribute("applicable_centers", supplierCenterDAO.getCenters(suppID));

    List centers = CenterMasterDAO.getCentersList();
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("cities_json", js.deepSerialize(
        ConversionUtils.copyListDynaBeansToMap(new CityMasterDAO().listAll("city_name"))));
    request.setAttribute("centers_json",
        js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));

    return mapping.findForward("supplier_applicability");
  }

  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException {

    String suppID = (String) request.getParameter("supplier_id");
    String error = null;
    Connection con = null;
    try {
      txn : {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        String app_for_centers = request.getParameter("applicable_for_centers");
        if (app_for_centers != null) {
          if (app_for_centers.equals("all")) {
            BasicDynaBean bean = supplierCenterDAO.getBean();

            if (!supplierCenterDAO.delete(con, suppID)) {
              error = "Failed to delete supplier center association of few centers..";
              break txn;
            }

            if (supplierCenterDAO.findByKey(con, "supplier_code", suppID) == null) {
              bean.set("supp_center_id", supplierCenterDAO.getNextSequence());
              bean.set("supplier_code", suppID);
              bean.set("center_id", 0);
              bean.set("status", "A");
              if (!supplierCenterDAO.insert(con, bean)) {
                error = "Failed to update supplier center association for all centers..";
                break txn;
              }
            }

          } else {
            if (!supplierCenterDAO.delete(con, 0, suppID)) {
              error = "Failed to delete supplier center association for all centers..";
              break txn;
            }
            String[] centerIds = request.getParameterValues("center_id");
            String[] supplier_center_id = request.getParameterValues("supp_center_id");
            String[] doctor_center_delete = request.getParameterValues("cntr_delete");
            String[] doctor_center_edited = request.getParameterValues("cntr_edited");
            String[] center_statuses = request.getParameterValues("center_status");
            for (int i = 0; i < centerIds.length - 1; i++) {
              BasicDynaBean bean = supplierCenterDAO.getBean();
              bean.set("supplier_code", suppID);
              if (centerIds[i] != null && !centerIds[i].equals(""))
                bean.set("center_id", Integer.parseInt(centerIds[i]));
              bean.set("status", center_statuses[i]);

              if (supplier_center_id[i].equals("_")) {
                bean.set("supp_center_id", supplierCenterDAO.getNextSequence());
                if (!supplierCenterDAO.insert(con, bean)) {
                  error = "Failed to insert the  supplier center association for selected centers..";
                  break txn;
                }
              } else if (new Boolean(doctor_center_delete[i])) {
                if (!supplierCenterDAO.delete(con, "supp_center_id",
                    Integer.parseInt(supplier_center_id[i]))) {
                  error = "Failed to delete the supplier center association for selected center..";
                  break txn;
                }
              } else if (new Boolean(doctor_center_edited[i])) {
                if (supplierCenterDAO.update(con, bean.getMap(), "supp_center_id",
                    Integer.parseInt(supplier_center_id[i])) != 1) {
                  error = "Failed to update the supplier center association for selected center..";
                  break txn;
                }
              }
            }
          }
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, error == null);
    }
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    FlashScope flash = FlashScope.getScope(request);

    flash.put("error", error);
    redirect.addParameter("supplier_id", suppID);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }
}
