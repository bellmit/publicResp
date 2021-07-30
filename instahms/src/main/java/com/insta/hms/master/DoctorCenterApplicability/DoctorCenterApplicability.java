package com.insta.hms.master.DoctorCenterApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
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

/**
 * @author prasanna.kumar
 *
 */
public class DoctorCenterApplicability extends  DispatchAction {

	CenterDAO doctorCenterDAO = new CenterDAO();

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String docID = request.getParameter("doctor_id");
		request.setAttribute("doctor_bean", DoctorMasterDAO.getDoctorDetails(docID));
		request.setAttribute("applicable_centers", doctorCenterDAO.getCenters(docID));

		List centers = CenterMasterDAO.getCentersList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				new CityMasterDAO().listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));

		return mapping.findForward("doctor_applicability");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException  {

		String docID = (String) request.getParameter("doctor_id");
		String error = null;
		Connection con = null;
		try {
			txn : {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String app_for_centers = request.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = doctorCenterDAO.getBean();

						if (!doctorCenterDAO.delete(con, docID)) {
							error = "Failed to delete doctor center association of few centers..";
							break txn;
						}

						if (doctorCenterDAO.findByKey(con, "doctor_id", docID) == null) {
							bean.set("doc_center_id", doctorCenterDAO.getNextSequence());
							bean.set("doctor_id", docID);
							bean.set("center_id", 0);
							bean.set("status", "A");
							if (!doctorCenterDAO.insert(con, bean)) {
								error = "Failed to update doctor center association for all centers..";
								break txn;
							}
						}

					} else {
						if (!doctorCenterDAO.delete(con, 0,docID)) {
							error = "Failed to delete doctor center association for all centers..";
							break txn;
						}
						String[] centerIds = request.getParameterValues("center_id");
						String[] doc_center_id = request.getParameterValues("doc_center_id");
						String[] doctor_center_delete = request.getParameterValues("cntr_delete");
						String[] doctor_center_edited = request.getParameterValues("cntr_edited");
						String[] center_statuses = request.getParameterValues("center_status");
						for (int i=0; i<centerIds.length-1; i++) {
							BasicDynaBean bean = doctorCenterDAO.getBean();
							bean.set("doctor_id", docID);
							bean.set("center_id", Integer.parseInt(centerIds[i]));
							bean.set("status", center_statuses[i]);

							if (doc_center_id[i].equals("_")) {
								bean.set("doc_center_id", doctorCenterDAO.getNextSequence());
								if (!doctorCenterDAO.insert(con, bean)) {
									error = "Failed to insert the  doctor center association for selected centers..";
									break txn;
								}
							} else if (new Boolean(doctor_center_delete[i])) {
								if (!doctorCenterDAO.delete(con, "doc_center_id", Integer.parseInt(doc_center_id[i]))) {
									error = "Failed to delete the doctor center association for selected center..";
									break txn;
								}
							} else if (new Boolean(doctor_center_edited[i])) {
								if (doctorCenterDAO.update(con, bean.getMap(), "doc_center_id",
										Integer.parseInt(doc_center_id[i])) != 1) {
									error = "Failed to update the doctor center association for selected center..";
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
		redirect.addParameter("doctor_id", docID);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


}
