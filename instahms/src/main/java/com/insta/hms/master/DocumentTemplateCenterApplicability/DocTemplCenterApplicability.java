/**
 *
 */
package com.insta.hms.master.DocumentTemplateCenterApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author preeti
 *
 */
public class DocTemplCenterApplicability extends BaseAction{
	CenterDAO docTemplCenterDAO = new CenterDAO();

	public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String templId = request.getParameter("template_id");
		String format = request.getParameter("format");
		String templName = request.getParameter("template_name");
		String status = request.getParameter("templ_cen_status");
		/*GenericDAO gdtDAO = new GenericDAO(format);
		BasicDynaBean bean = gdtDAO.findByKey("template_id", Integer.parseInt(templId));*/
		//request.setAttribute("doctempl_bean", gdtDAO.findByKey("template_id", Integer.parseInt(templId)));
		request.setAttribute("template_id", Integer.parseInt(templId));
		request.setAttribute("template_name", templName);
		request.setAttribute("status", status);
		request.setAttribute("format", format);
		request.setAttribute("applicable_centers", docTemplCenterDAO.getApplicableCenters(Integer.parseInt(templId), format));

		List centers = CenterMasterDAO.getCentersList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("cities_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
				new CityMasterDAO().listAll("city_name"))));
		request.setAttribute("centers_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(centers)));

		return mapping.findForward("doctemplcen_applicability");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException  {

		String docTemplID = (String) request.getParameter("doc_templ_id");
		String templName = (String) request.getParameter("template_name");
		int docTemplIdInt = -1;
		if(docTemplID != null && !docTemplID.equals("")) {
			docTemplIdInt = Integer.parseInt(docTemplID);
		}
		String format = (String)request.getParameter("format");
		String type = null;
		if(format != null && format.contains("pdf")) {
			type = "P";
		}
		if(format != null && format.contains("hvf")) {
			type = "H";
		}
		if(format != null && format.contains("rich")) {
			type = "R";
		}
		if(format != null && format.contains("rtf")) {
			type = "T";
		}
		String error = null;
		Connection con = null;
		try {
			txn : {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				String app_for_centers = request.getParameter("applicable_for_centers");
				if (app_for_centers != null) {
					if (app_for_centers.equals("all")) {
						BasicDynaBean bean = docTemplCenterDAO.getBean();

						if (!docTemplCenterDAO.delete(con, docTemplIdInt)) {
							error = "Failed to delete document template center association of few centers..";
							break txn;
						}

						if (docTemplCenterDAO.findByKey(con, "template_id", docTemplIdInt) == null) {
							bean.set("template_id", docTemplIdInt);
							bean.set("center_id", 0);
							bean.set("doc_template_type", type);
							bean.set("status", "A");
							if (!docTemplCenterDAO.insert(con, bean)) {
								error = "Failed to update doctor center association for all centers..";
								break txn;
							}
						}

					} else {
						if (!docTemplCenterDAO.delete(con, 0,docTemplIdInt)) {
							error = "Failed to delete document template center association for all centers..";
							break txn;
						}
						String[] centerIds = request.getParameterValues("center_id");
						String[] doc_templ_center_id = request.getParameterValues("doc_template_center_id");
						String[] doc_templ_center_delete = request.getParameterValues("cntr_delete");
						String[] doc_templ_center_edited = request.getParameterValues("cntr_edited");
						String[] center_statuses = request.getParameterValues("center_status");
						for (int i=0; i<centerIds.length-1; i++) {
							BasicDynaBean bean = docTemplCenterDAO.getBean();
							bean.set("template_id", docTemplIdInt);
							bean.set("center_id", Integer.parseInt(centerIds[i]));
							bean.set("status", center_statuses[i]);
							bean.set("doc_template_type", type);
							if (doc_templ_center_id[i].equals("_")) {
								//bean.set("doc_template_center_id", docTemplCenterDAO.getNextSequence());
								if (!docTemplCenterDAO.insert(con, bean)) {
									error = "Failed to insert the  document template center association for selected centers..";
									break txn;
								}
							} else if (new Boolean(doc_templ_center_delete[i])) {
								if (!docTemplCenterDAO.delete(con, "doc_template_center_id", Integer.parseInt(doc_templ_center_id[i]))) {
									error = "Failed to delete the doctor center association for selected center..";
									break txn;
								}
							} else if (new Boolean(doc_templ_center_edited[i])) {
								if (docTemplCenterDAO.update(con, bean.getMap(), "doc_template_center_id",
										Integer.parseInt(doc_templ_center_id[i])) != 1) {
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
		redirect.addParameter("template_id", docTemplID);
		redirect.addParameter("template_name", templName);
		redirect.addParameter("format", format);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


}
