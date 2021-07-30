package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class licenseAction extends DispatchAction{

	private static final String[] SEARCH_BOOL_FIELDS = 
	  {"statusAll", "statusActive", "statusInActive", "sortReverse"};

	private static final GenericDAO contractorMasterDao = new GenericDAO("contractor_master");
  private static final licenseDAO dao = new licenseDAO();
	
  private static final String GET_CONTRACTORS_QUERY = 
      "SELECT contractor_id, contractor_name FROM contractor_master";

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		licenseForm lf = (licenseForm)f;

		String licenseTypeFilter[] = lf.getLicenseTypeFilter();
		ArrayList<String> licenseList = null;
		if(licenseTypeFilter!=null && licenseTypeFilter.length !=0 ){
			licenseList = new ArrayList<String>();
			for(int i=0;i<licenseTypeFilter.length;i++){
				licenseList.add(licenseTypeFilter[i]);
			}
		}else{
			licenseList = dao.getlicenseNames();
			licenseTypeFilter = populateListValuesTOArray(licenseTypeFilter,licenseList);
			lf.setLicenseTypeFilter(licenseTypeFilter);
		}

		ArrayList err = new ArrayList();
		HashMap params = new HashMap();
		ArrayList status = null;
		ConversionUtils.copyBooleanFields(req.getParameterMap(), params, SEARCH_BOOL_FIELDS, err, false);
		Boolean statusAll = (Boolean) params.get("statusAll");
		String _status = "A";
		if(statusAll) _status = "All";
		if (!statusAll) {
			status = new ArrayList();
			if ((Boolean)params.get("statusActive")) {
				status.add("A");
				_status = "A";
			}
			if ((Boolean)params.get("statusInActive")) {
				status.add("I");
				_status = "I";
			}
		}
		java.sql.Date RenewalFromDate = DataBaseUtil.parseDate(req.getParameter("renewalFrom"));
		java.sql.Date RenewalToDate = DataBaseUtil.parseDate(req.getParameter("renewalTo"));
		java.sql.Date expiryFromDate = DataBaseUtil.parseDate(req.getParameter("expiryFrom"));
		java.sql.Date expiryToDate = DataBaseUtil.parseDate(req.getParameter("expiryTo"));

		Map listingParams = ConversionUtils.getListingParameter(req.getParameterMap());
		PagedList pagedList = dao.getlicenseDetails(listingParams,licenseList,RenewalFromDate,RenewalToDate,expiryFromDate,expiryToDate,status);
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("licenseTypes",dao.getLicenseTypes());
		req.setAttribute("filterclosed", true);
		req.setAttribute("status", _status);

		return m.findForward("list");
	}


	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
	  JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("method", "create");
    req.setAttribute("contractors", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        DataBaseUtil.queryToDynaList(GET_CONTRACTORS_QUERY))));
		return m.findForward("addshow");
	}

	private static String[]  populateListValuesTOArray(String[] a,ArrayList<String>al){
		Iterator<String>  it = al.iterator();
		String[] array = new String[al.size()];

		int i=0;
		while(it.hasNext()){
		  array[i++]= it.next();
		}

		return array;
	}

	@IgnoreConfidentialFilters
	public ActionForward create(ActionMapping mapping, ActionForm af,
			HttpServletRequest request, HttpServletResponse response)
		throws SQLException, FileNotFoundException, IOException, ParseException {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		licenseForm f = (licenseForm) af;

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));

		int licenseId = dao.getNextSequence();

		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		Date startdate = new Date(format.parse(request.getParameter("license_start_date")).getTime());
		Date endDate = new Date(format.parse(request.getParameter("license_end_date")).getTime());
		Date renewalDate = new Date(format.parse(request.getParameter("license_renewal_date")).getTime());

		boolean duplicateName = true;
		duplicateName = dao.checkLicenseName(con,f.getLicense_desc());

		if(duplicateName == false){
			boolean status = true;
			status = dao.insertFieldValues(con,f,licenseId,startdate,endDate,renewalDate);

			FormFile ff = f.getLicenseFile();

			if (status) {
				status = dao.updateFile(con, licenseId, ff.getInputStream(), ff.getFileSize());
				if (status) {
					con.commit();
					flash.put("success", "License details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					con.close();
				}
				else {
					con.rollback();
					request.setAttribute("error", "Failed to add the details");
					return mapping.findForward("addshow");
				}
			}
		}else{
			flash.put("error", "License Name already exists..");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			con.close();
		}

		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException, Exception {
		String licenseId = req.getParameter("license_id");
		String Contractor_id = null;
		if (licenseId != null) {
			BasicDynaBean form = dao.getLicenseDetails(Integer.parseInt(licenseId));
			if(form.get("contractor_id") != null)
			Contractor_id = form.get("contractor_id").toString();
			req.setAttribute("bean", form.getMap());
		}
		JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("contractors", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        DataBaseUtil.queryToDynaList(GET_CONTRACTORS_QUERY))));
		if(Contractor_id != null) {
			String contractor_name = contractorMasterDao.findByKey("contractor_id", Integer.parseInt(Contractor_id )).get("contractor_name").toString();
			req.setAttribute("contractor_name", contractor_name);
		}
		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward update(ActionMapping m,ActionForm af,
			HttpServletRequest req, HttpServletResponse resp)
			throws ServletException,IOException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		licenseForm f = (licenseForm) af;
		String licenseId = req.getParameter("license_id");
		String licenseName = req.getParameter("licenseName");

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));

		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		format.parse(req.getParameter("license_start_date"));
		Date startdate = new Date(format.parse(req.getParameter("license_start_date")).getTime());
		Date endDate = new Date(format.parse(req.getParameter("license_end_date")).getTime());
		Date renewalDate = new Date(format.parse(req.getParameter("license_renewal_date")).getTime());

		boolean duplicateName = false;
		if(!licenseName.equals(f.getLicense_desc())){
			duplicateName = dao.checkLicenseName(con,f.getLicense_desc());
		}

		if(duplicateName == false){
			boolean status = true;
			status = dao.updateFields(con,f,Integer.parseInt(licenseId),startdate,endDate,renewalDate);

			FormFile ff = f.getLicenseFile();

			if (ff.getFileSize()!=0) {
				status = dao.updateFile(con, Integer.parseInt(licenseId), ff.getInputStream(), ff.getFileSize());
			}

			if(status){
				con.commit();
				flash.put("success", "License details updated successfully..");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				con.close();
			}else{
				con.rollback();
				req.setAttribute("error", "Failed to update license details..");
			}
		}else{
			flash.put("error", "License Name already exists..");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			con.close();
		}

		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getlicenseForm(ActionMapping mapping, ActionForm af,
			HttpServletRequest request, HttpServletResponse response)
		throws SQLException, IOException {

		String licensId = request.getParameter("licenseId");
		if (licensId != null) {
			response.setContentType("application/");
			OutputStream os = response.getOutputStream();

			InputStream s = licenseDAO.getlicenseForm(Integer.parseInt(licensId));
			if (s != null) {
				byte[] bytes = new byte[4096];
				int len = 0;
				while ( (len = s.read(bytes)) > 0) {
					os.write(bytes, 0, len);
				}

				os.flush();
				s.close();
				return null;
			} else {
				return mapping.findForward("error");
			}
		} else {
			return mapping.findForward("error");
		}
	}

}