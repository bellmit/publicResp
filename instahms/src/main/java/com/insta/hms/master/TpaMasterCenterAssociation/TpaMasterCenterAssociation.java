package com.insta.hms.master.TpaMasterCenterAssociation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
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
 * @author nikunj.s
 *
 */
public class TpaMasterCenterAssociation extends DispatchAction {
	CenterDAO tpaCenterDAO = new CenterDAO();
	CenterMasterDAO centerDAO = new CenterMasterDAO();

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String tpaId = (String) request.getParameter("tpa_id");
		request.setAttribute("tpa_bean", TpaMasterDAO.getTpaDetails(tpaId));
		request.setAttribute("applicable_centers", tpaCenterDAO.getCenters(tpaId));

		List centers = CenterMasterDAO.getCentersList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				new CityMasterDAO().listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));

		return mapping.findForward("tpa_center_association");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException  {

		String tpaId = (String) request.getParameter("tpa_id");
		String claimformat=(String) request.getParameter("claim_format");
		String error = null;
		Connection con = null;
		try {
			txn : {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String app_for_centers = request.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = tpaCenterDAO.getBean();

						if (!tpaCenterDAO.delete(con, tpaId)) {
							error = "Failed to delete tpa center association of few centers..";
							break txn;
						}

						if (tpaCenterDAO.findByKey(con, "tpa_id", tpaId) == null) {
							bean.set("tpa_center_id", tpaCenterDAO.getNextSequence());
							bean.set("tpa_id", tpaId);
							bean.set("center_id", -1);
							bean.set("status", "A");
							bean.set("claim_format", claimformat);
							if (!tpaCenterDAO.insert(con, bean)) {
								error = "Failed to update tpa center association for all centers..";
								break txn;
							}
						}

					} else {
						if (!tpaCenterDAO.delete(con, -1,tpaId)) {
							error = "Failed to delete tpa center association for all centers..";
							break txn;
						}
						String[] centerIds = request.getParameterValues("center_id");
						String[] tpa_center_id = request.getParameterValues("tpa_center_id");
						String[] tpa_center_delete = request.getParameterValues("cntr_delete");
						String[] tpa_center_edited = request.getParameterValues("cntr_edited");
						String[] center_statuses = request.getParameterValues("center_status");
						String[] claim_format = request.getParameterValues("claim_format");
						for (int i=0; i<centerIds.length-1; i++) {
							BasicDynaBean bean = tpaCenterDAO.getBean();
							bean.set("tpa_id", tpaId);
							bean.set("center_id", Integer.parseInt(centerIds[i]));
							bean.set("status", center_statuses[i]);
							bean.set("claim_format", claim_format[i]);

							if (tpa_center_id[i].equals("_")) {
								bean.set("tpa_center_id", tpaCenterDAO.getNextSequence());
								if (!tpaCenterDAO.insert(con, bean)) {
									error = "Failed to insert the tpa center association for selected centers..";
									break txn;
								}
							} else if (new Boolean(tpa_center_delete[i])) {
								if (!tpaCenterDAO.delete(con, "tpa_center_id", Integer.parseInt(tpa_center_id[i]))) {
									error = "Failed to delete the tpa center association for selected center..";
									break txn;
								}
							} else if (new Boolean(tpa_center_edited[i])) {
								if (tpaCenterDAO.update(con, bean.getMap(), "tpa_center_id",
										Integer.parseInt(tpa_center_id[i])) != 1) {
									error = "Failed to update the tpa center association for selected center..";
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
		redirect.addParameter("tpa_id", tpaId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}
