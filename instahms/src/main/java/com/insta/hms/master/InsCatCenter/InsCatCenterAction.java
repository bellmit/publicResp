package com.insta.hms.master.InsCatCenter;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
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

public class InsCatCenterAction extends DispatchAction {

	InsCatCenterDAO insplantypeCenterDAO = new InsCatCenterDAO();

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String categoryid = request.getParameter("category_id");
		int categoryID = Integer.parseInt(categoryid);
		request.setAttribute("category_bean", InsuranceCategoryMasterDAO.getPlanTypeDetails(categoryID));
		request.setAttribute("applicable_centers", insplantypeCenterDAO.getCenters(categoryID));

		List centers = CenterMasterDAO.getCentersList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				new CityMasterDAO().listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));

		return mapping.findForward("category_applicability");
	}


	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException  {

		String  categoryid = request.getParameter("category_id");
		int categoryID = Integer.parseInt(categoryid);
		String error = null;
		Connection con = null;
		try {
			txn : {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String app_for_centers = request.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = insplantypeCenterDAO.getBean();

						if (!insplantypeCenterDAO.delete(con, categoryID)) {
							error = "Failed to delete network/plan category type center association of few centers..";
							break txn;
						}

						if (insplantypeCenterDAO.findByKey(con, "category_id", categoryID) == null) {
							bean.set("inscat_center_id", insplantypeCenterDAO.getNextSequence());
							bean.set("category_id", categoryID);
							bean.set("center_id", 0);
							bean.set("status", "A");
							if (!insplantypeCenterDAO.insert(con, bean)) {
								error = "Failed to update network/plan type category center association for all centers..";
								break txn;
							}
						}

					} else {
						if (!insplantypeCenterDAO.delete(con, 0,categoryID)) {
							error = "Failed to delete network/plan type category center association for all centers..";
							break txn;
						}
						String[] centerIds = request.getParameterValues("center_id");
						String[] inscat_center_id = request.getParameterValues("inscat_center_id");
						String[] inscat_center_delete = request.getParameterValues("cntr_delete");
						String[] inscat_center_edited = request.getParameterValues("cntr_edited");
						String[] center_statuses = request.getParameterValues("center_status");
						for (int i=0; i<centerIds.length-1; i++) {
							BasicDynaBean bean = insplantypeCenterDAO.getBean();
							bean.set("category_id", categoryID);
							bean.set("center_id", Integer.parseInt(centerIds[i]));
							bean.set("status", center_statuses[i]);

							if (inscat_center_id[i].equals("_")) {
								bean.set("inscat_center_id", insplantypeCenterDAO.getNextSequence());
								if (!insplantypeCenterDAO.insert(con, bean)) {
									error = "Failed to insert the network/plan type category center association for selected centers..";
									break txn;
								}
							} else if (new Boolean(inscat_center_delete[i])) {
								if (!insplantypeCenterDAO.delete(con, "inscat_center_id", Integer.parseInt(inscat_center_id[i]))) {
									error = "Failed to delete the network/plan type category center association for selected center..";
									break txn;
								}
							} else if (new Boolean(inscat_center_edited[i])) {
								if (insplantypeCenterDAO.update(con, bean.getMap(), "inscat_center_id",
										Integer.parseInt(inscat_center_id[i])) != 1) {
									error = "Failed to update the network/plan type category center association for selected center..";
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
		redirect.addParameter("category_id", categoryID);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
