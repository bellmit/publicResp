package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class licenseTypeAction extends DispatchAction {

  @IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		licenseTypeDAO dao = new licenseTypeDAO();
		Map map = req.getParameterMap();
		PagedList pagedList = dao.search(map, ConversionUtils.getListingParameter(req.getParameterMap()),
					"license_type_id");
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

  @IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		ArrayList<String> avllicense = (ArrayList)licenseTypeDAO.getAlllicense();
		req.setAttribute("avllicense", js.serialize(avllicense));

		req.setAttribute("method", "create");
		return m.findForward("addshow");
	}

	private static final String[] STRING_FIELDS = { "license_type","status" };

	@IgnoreConfidentialFilters
	public ActionForward create(ActionMapping mapping, ActionForm af,
			HttpServletRequest request, HttpServletResponse response)
			throws SQLException, FileNotFoundException, IOException {


		licenseTypeDAO dao = new licenseTypeDAO();
		boolean status = true;
		Map requestMap = request.getParameterMap();
		String licenseType = ((String[])requestMap.get("license_type"))[0].trim();

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = null;

		BasicDynaBean beanExists = dao.findByKey("license_type", licenseType);
		if (beanExists != null) {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			flash.put("error", "License Type already exists");
		} else {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {
				HashMap fields = new HashMap();
				ConversionUtils.copyStringFields(requestMap, fields,
						STRING_FIELDS, null);
				int license_type_id = dao.getNextSequence();
				fields.put("license_type_id ", license_type_id);
				status = dao.insertLicenseTypeDetails(con, fields);
				if (status) {
					flash.put("success", "licenseType details saved successfully..");
					redirect=new ActionRedirect(mapping.findForward("showRedirect"));
					redirect.addParameter("license_type_id", license_type_id);
				} else {
					redirect = new ActionRedirect(mapping.findForward("addRedirect"));
					flash.put("error", "Failed to add the details");
				}
			} finally {
				DataBaseUtil.commitClose(con, status);
			}
		}
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException, Exception{

		JSONSerializer js = new JSONSerializer().exclude("class");

		licenseTypeDAO dao = new licenseTypeDAO();
		String license_typeId = req.getParameter("license_type_id");
		if (license_typeId != null) {
			BasicDynaBean form = dao.getLicenseDetails(Integer.parseInt(license_typeId));
			req.setAttribute("bean", form.getMap());
		}
		ArrayList<String> avllicense = (ArrayList)licenseTypeDAO.getAlllicense();
		req.setAttribute("avllicense", js.serialize(avllicense));

		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward update(ActionMapping m,ActionForm af,
			HttpServletRequest req, HttpServletResponse resp)
			throws ServletException,IOException, Exception {

		Connection con = null;
		licenseTypeDAO dao = new licenseTypeDAO();
		String license_typeId = req.getParameter("license_type_id");
		Map requestMap = req.getParameterMap();
		String licenseType = ((String[])requestMap.get("license_type"))[0].trim();

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));

		HashMap fields = new HashMap();
		ConversionUtils.copyStringFields(req.getParameterMap(), fields, STRING_FIELDS, null);
		BasicDynaBean beanExists = dao.findByKey("license_type", licenseType);

		boolean status = true;

		try {
			con =  DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (beanExists != null && ((BigDecimal)beanExists.get("license_type_id")).intValue() != Integer.parseInt(license_typeId)) {
				flash.put("error", "License Type already exists");
			} else {
				status = dao.updateFields(con,Integer.parseInt(license_typeId),fields);

				if(status){
					flash.put("success", "LicenseType details updated successfully..");

				}else{
					flash.put("error", "Failed to update LicenseType details..");
				}
			}
			redirect.addParameter("license_type_id",license_typeId);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		} finally {
			DataBaseUtil.commitClose(con, status);
		}
		return redirect;
	}
}
