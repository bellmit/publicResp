/**
 *
 */
package com.insta.hms.master.MedicineDosage;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class MedicineDosageAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(MedicineDosageAction.class);

	MedicineDosageDAO dao = new MedicineDosageDAO();
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException {
		Map requestParams = request.getParameterMap();
		request.setAttribute("pagedList",
				dao.search(requestParams, ConversionUtils.getListingParameter(requestParams), "dosage_name"));
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		String dosage = request.getParameter("med_dosage_name");
		request.setAttribute("dosageBean", dao.findByKey("dosage_name", dosage));
		request.setAttribute("medicineDosageLists", js.serialize(dao.getMedicineDosagesNamesAndIds()));


		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);
		String error = null;
		String msg = null;
		boolean success = true;
		FlashScope flash = FlashScope.getScope(request);

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {
				String dosage = (String) bean.get("dosage_name");
				if (dosage != null)
					bean.set("dosage_name", dosage.trim());
				if (dao.exist("dosage_name", bean.get("dosage_name"))) {
					error = "Medicine Dosage : " + dosage + " already exists";
					flash.put("error", error);
					ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

					return redirect;
				} else {
					if (dao.insert(con, bean)) {
						msg = "Medicine Dosage inserted successfully..";
						success = true;
					} else {
						success = false;
						error = "Failed to insert Medicine Dosage..";
					}
				}
			} catch (SQLException se) {
				success = false;
				error = "Failed to insert Medicine Dosage";
				throw se;
			} finally {
				DataBaseUtil.commitClose(con, success);
			}
		} else {
			error = "Incorrectly formatted details supplied..";
		}
		flash.put("success", msg);
		flash.put("error", error);

		ActionRedirect redirect = null;
		if (success) {
			redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("med_dosage_name", bean.get("dosage_name"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		else
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		Map params = request.getParameterMap();
		BasicDynaBean bean = dao.getBean();
		List errorFields = new ArrayList();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);
		String error = null;
		String msg = null;
		boolean success = true;
		FlashScope flash = FlashScope.getScope(request);

		if (errorFields.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {
				if (dao.update(con, bean.getMap(), "dosage_name",
						request.getParameter("keyForUpdate")) != 0) {
					msg = "Medicine Dosage updated successfully..";
					success = true;
				} else {
					success = false;
					error = "Failed to update Medicine Dosage..";
				}
			} catch (SQLException se) {
				success = false;
				error = "Failed to update Medicine Dosage";
				throw se;
			} finally {
				DataBaseUtil.commitClose(con, success);
			}
		} else {
			error = "Incorrectly formatted details supplied..";
		}
		flash.put("success", msg);
		flash.put("error", error);

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("med_dosage_name", bean.get("dosage_name"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException {
		String[] names = request.getParameterValues("checked");
		String dosageName = request.getParameter("medDosageDoasageName");

		String error = null;
		String msg = null;
		FlashScope flash = FlashScope.getScope(request);

		if (names == null) {
			error = "No data supplied for delete..";
			flash.put("warning", error);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			return redirect;
		}

		Boolean success = true;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		int deleteCount = 0;
		try {
			for (String value: names) {
				if (!dao.delete(con, "dosage_name", value)) break;
				deleteCount++;
			}
			if (deleteCount == names.length) success = true;
			else success = false;
		} catch (SQLException se) {
			success = false;
			log.error("", se);
			throw se;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (success) msg = (names.length == 1?"Medicine Dosage":"Medicine Dosages") + " deleted successfully..";
		else error = "Failed to delete " + (names.length == 1?"Medicine Dosage":"Medicine Dosages");


		flash.put("success", msg);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("dosage_name", dosageName);
		redirect.addParameter("dosage_name@op", "ico");

		return redirect;
	}


}
