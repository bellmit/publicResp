package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class contractsAction extends BaseAction {
	private static final String[] SEARCH_BOOL_FIELDS = 
	  {"statusAll", "statusActive", "statusInActive", "sortReverse"};
  private static final String GET_CONTRACTORS_QUERY = 
      "SELECT contractor_id, contractor_name FROM contractor_master";
  private static final contractsDAO dao = new contractsDAO();
  private static final JSONSerializer js = new JSONSerializer().exclude("class");
  
  private static final GenericDAO genericContractsDAO = new GenericDAO("contracts");

	
	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res)throws IOException, ServletException, Exception {

		Map<Object,Object> map= getParameterMap(req);
		ArrayList err = new ArrayList();
		HashMap params = new HashMap();
		ConversionUtils.copyBooleanFields(req.getParameterMap(), params, SEARCH_BOOL_FIELDS, err, false);
		String contract_type = req.getParameter("contract_type_id");
		ArrayList status = null;
		status = new ArrayList();
		String[] _status = req.getParameterValues("status");
		if(_status!=null) {
			for(int i=0;i<_status.length ; i++) {
				if(!_status[i].equals(""))
					status.add(_status[i]);
			}
		}
		int contract_type_id = 0;
		if(contract_type != null && !"".equals(contract_type) )
			contract_type_id = Integer.parseInt(contract_type.toString());

		java.sql.Date RenewalFromDate = DataBaseUtil.parseDate(req.getParameter("renewalFrom"));
		java.sql.Date RenewalToDate = DataBaseUtil.parseDate(req.getParameter("renewalTo"));
		java.sql.Date expiryFromDate = DataBaseUtil.parseDate(req.getParameter("expiryFrom"));
		java.sql.Date expiryToDate = DataBaseUtil.parseDate(req.getParameter("expiryTo"));

		Map listingParams = ConversionUtils.getListingParameter(req.getParameterMap());
		PagedList pagedList = dao.getcontractsDetails(listingParams,RenewalFromDate,RenewalToDate,expiryFromDate,expiryToDate,status,contract_type_id);
		req.setAttribute("pagedList", pagedList);
		req.setAttribute("contractTypes",dao.getcontractTypes());
		req.setAttribute("filterclosed", true);
		req.setAttribute("contract_type_id", contract_type_id);

		return m.findForward("list");
	}

	@IgnoreConfidentialFilters
	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res)throws IOException, ServletException, Exception {
		req.setAttribute("contractors", js.serialize(ConversionUtils.copyListDynaBeansToMap(
		    DataBaseUtil.queryToDynaList(GET_CONTRACTORS_QUERY))));
		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	@SuppressWarnings("unchecked")
	public ActionForward create(ActionMapping mapping, ActionForm af,
			HttpServletRequest request, HttpServletResponse response)
		throws SQLException, FileNotFoundException, IOException, ParseException, FileUploadException {

		Map<Object,Object> params= getParameterMap(request);
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);


		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));


		Object filename[] = (Object[])params.get("fileName");
		Object oldrnew[] = (Object[])params.get("record");
		Object file_upload[] = (Object[])params.get("file_upload");
		Object content_type[] = (Object[])params.get("content_type");
		int len = filename != null ? filename.length : 0;

		int contractId = dao.getNextSequence();

		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		//String contractId[] = (String[])params.get("contract_id");


		bean.set("contract_id", new BigDecimal(contractId));
		if (null != file_upload){
			bean.set("contract_attachment", file_upload[0]);
			if(content_type != null)
			bean.set("content_type", content_type[0].toString());
			bean.set("contract_file_name", filename!=null ? (String)filename[0] : "");
		}
		boolean success = genericContractsDAO.insert(con, bean);
		if (success) {
			con.commit();
			flash.put("success", "Contract details inserted successfully..");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			con.close();
		}else{
			con.rollback();
			request.setAttribute("error", "Failed to insert contract details..");
		}


		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException, Exception {

		String contractId = req.getParameter("contract_id");
		String contractor_id = null;
		if (contractId != null) {
			BasicDynaBean form = dao.getContractDetails(Integer.parseInt(contractId));
			req.setAttribute("bean", form.getMap());
			if(form.get("contractor_id") != null)
			contractor_id = form.get("contractor_id").toString();
		}
		if(contractor_id != null)	{
		String contractor_name = new GenericDAO("contractor_master").findByKey("contractor_id", Integer.parseInt(contractor_id)).get("contractor_name").toString();
		req.setAttribute("contractor_name", contractor_name);
		}
    req.setAttribute("contractors", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        DataBaseUtil.queryToDynaList(GET_CONTRACTORS_QUERY))));
		return m.findForward("addshow");
	}

	@IgnoreConfidentialFilters
	public ActionForward update(ActionMapping m,ActionForm af,
			HttpServletRequest req, HttpServletResponse resp)
			throws ServletException,IOException, Exception {

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		Map<Object,Object> params= getParameterMap(req);
		Object filename[] = (Object[])params.get("fileName");
		Object oldrnew[] = (Object[])params.get("record");
		Object file_upload[] = (Object[])params.get("file_upload");
		Object content_type[] = (Object[])params.get("content_type");
		int len = filename != null ? filename.length : 0;


		List errors = new ArrayList();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("contract_id", new Integer(((Object[])params.get("contract_id"))[0].toString()));
		//String contractId[] = (String[])params.get("contract_id");
		String contractName = ((Object[])params.get("contractName"))[0].toString();

		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));




		boolean duplicateName = false;
		if (!contractName.equals(((Object[])params.get("contract_company"))[0].toString())){
			duplicateName = dao.checkContractName(con,((Object[])params.get("contract_company"))[0].toString());
		}

		if(duplicateName == false){
			if (null != file_upload){
				bean.set("contract_attachment", file_upload[0]);
				if(content_type != null)
				bean.set("content_type", content_type[0].toString());
				bean.set("contract_file_name",filename!=null ? (String)filename[0] : "");
			}
			int success = genericContractsDAO.update(con, bean.getMap(),keys);
			if (success > 0) {
				con.commit();
				flash.put("success", "Contract details updated successfully..");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				con.close();
			}else{
				con.rollback();
				req.setAttribute("error", "Failed to update contract details..");
			}
		}else{
			flash.put("error", "Contract Name already exists..");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			con.close();
		}

		return redirect;
	}

	@IgnoreConfidentialFilters
	@SuppressWarnings("unchecked")
	public ActionForward getcontractForm(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException{
		String id = req.getParameter("contractId");
		String fileName = "";
		String contentType = "";

		if (id != null) {
			Map<String,Object> uploadMap = contractsDAO.getContractUpload(Integer.parseInt(id));
			if (!(uploadMap.isEmpty())){
				fileName = (String)uploadMap.get("contract_file_name");
				contentType = (String)uploadMap.get("contenttype");
				if (!(fileName.equals(""))){
					resp.setHeader("Content-disposition", "attachment; filename=\""+fileName+"\"");
					resp.setContentType(contentType);
					OutputStream os = resp.getOutputStream();

					InputStream s = (InputStream)uploadMap.get("uploadfile");
					if (s != null) {
						byte[] bytes = new byte[4096];
						int len = 0;
						while ( (len = s.read(bytes)) > 0) {
							os.write(bytes, 0, len);
						}

						os.flush();
						s.close();
						return null;
					}

				} else{
					return m.findForward("error");
				}
			}
		}

		return m.findForward("error");
	}

}