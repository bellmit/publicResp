package com.insta.hms.master.ReferalDoctorApplicability;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
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

public class ReferalDoctorApplicability extends  DispatchAction {

	CenterDAO referalCenterDAO = new CenterDAO();

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String referalNo = request.getParameter("referal_no");
		request.setAttribute("referal_bean", ReferalDoctorDAO.getReferalDetails(referalNo));
		request.setAttribute("applicable_centers", referalCenterDAO.getCenters(referalNo));

		List centers = CenterMasterDAO.getCentersList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				new CityMasterDAO().listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));

		return mapping.findForward("referal_applicability");
	}


	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException  {

		String referalNo = (String) request.getParameter("referal_no");
		String error = null;
		Connection con = null;
		try {
			txn : {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String app_for_centers = request.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = referalCenterDAO.getBean();

						if (!referalCenterDAO.delete(con, referalNo)) {
							error = "Failed to delete referal doctor center association of few centers..";
							break txn;
						}

						if (referalCenterDAO.findByKey(con, "referal_no", referalNo) == null) {
							bean.set("referal_center_id", referalCenterDAO.getNextSequence());
							bean.set("referal_no", referalNo);
							bean.set("center_id", 0);
							bean.set("status", "A");
							if (!referalCenterDAO.insert(con, bean)) {
								error = "Failed to update referal doctor center association for all centers..";
								break txn;
							}
						}

					} else {
						if (!referalCenterDAO.delete(con, 0,referalNo)) {
							error = "Failed to delete referal doctor center association for all centers..";
							break txn;
						}
						String[] centerIds = request.getParameterValues("center_id");
						String[] referal_center_id = request.getParameterValues("referal_center_id");
						String[] referal_center_delete = request.getParameterValues("cntr_delete");
						String[] referal_center_edited = request.getParameterValues("cntr_edited");
						String[] center_statuses = request.getParameterValues("center_status");
						for (int i=0; i<centerIds.length-1; i++) {
							BasicDynaBean bean = referalCenterDAO.getBean();
							bean.set("referal_no", referalNo);
							bean.set("center_id", Integer.parseInt(centerIds[i]));
							bean.set("status", center_statuses[i]);

							if (referal_center_id[i].equals("_")) {
								bean.set("referal_center_id", referalCenterDAO.getNextSequence());
								if (!referalCenterDAO.insert(con, bean)) {
									error = "Failed to insert the referal doctor center association for selected centers..";
									break txn;
								}
							} else if (new Boolean(referal_center_delete[i])) {
								if (!referalCenterDAO.delete(con, "referal_center_id", Integer.parseInt(referal_center_id[i]))) {
									error = "Failed to delete the referal doctor center association for selected center..";
									break txn;
								}
							} else if (new Boolean(referal_center_edited[i])) {
								if (referalCenterDAO.update(con, bean.getMap(), "referal_center_id",
										Integer.parseInt(referal_center_id[i])) != 1) {
									error = "Failed to update the referal doctor center association for selected center..";
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
		redirect.addParameter("referal_no", referalNo);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
