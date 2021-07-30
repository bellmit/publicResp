package com.insta.hms.stores;

import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoresPatientViewIndentAction extends BaseAction {

  public ActionForward view(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws IOException, ServletException, Exception {

    String patientIndentNo = req.getParameter("patient_indent_no");
    StoresPatientIndentDAO indentMainDAO = new StoresPatientIndentDAO();
    BasicDynaBean patIndentMain = indentMainDAO.findByKey("patient_indent_no", patientIndentNo);
    Integer storeId = (Integer) patIndentMain.get("indent_store");
    BasicDynaBean storeDetails = StoreDAO.findByStore(storeId);
    Integer centerId = (Integer) storeDetails.get("center_id");
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

    List<BasicDynaBean> patIndentDetList = indentMainDAO.getPatientIndentDetails(patientIndentNo,
        healthAuthority);
    JSONSerializer js = new JSONSerializer().exclude("class");

    req.setAttribute("indentMain", patIndentMain);
    req.setAttribute("indentDetails", patIndentDetList);
    req.setAttribute("indentDetailsJson",
        ConversionUtils.listBeanToMapListMap(patIndentDetList, "medicine_id"));
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    req.setAttribute("patient",
        VisitDetailsDAO.getPatientVisitDetailsMap((String) patIndentMain.get("visit_id")));
    req.setAttribute("returns", am.getProperty("category") != null);
    req.setAttribute("titlePrefix", "View");
    req.setAttribute("returnIndentableItems",
        js.deepSerialize(ConversionUtils.listBeanToMapListMap(new StoresPatientIndentDAO()
            .getPatientReturnIndentableItems(req.getParameter("patient_id"), healthAuthority),
            "store_id")));
    req.setAttribute("returnIndentableBatchItems",
        js.deepSerialize(ConversionUtils
            .listBeanToMapListMap(new GenericDAO("patient_return_indentable_batch_items")
                .findAllByKey("visit_id", req.getParameter("patient_id")), "store_id")));
    req.setAttribute("doctorDetails", js.serialize(DoctorMasterDAO.getDoctorsandCharges()));

    return am.findForward("viewIndent");

  }

}
