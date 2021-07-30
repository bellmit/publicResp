package com.insta.hms.master.InsuranceCompMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class InsuCompMasterAction extends BaseAction
{
  
  private static final GenericDAO insuranceCompanyCategoryMappingDAO =
      new GenericDAO("insurance_company_category_mapping");

  private static final GenericDAO itemInsuranceCategoriesDAO =
      new GenericDAO("item_insurance_categories");
  private static final GenericDAO healthAuthorityMasterDAO =
      new GenericDAO("health_authority_master");

  private static final GenericDAO haInsCompanyCodeDAO = new GenericDAO("ha_ins_company_code");
  
	public ActionForward list(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		InsuCompMasterDAO dao = new InsuCompMasterDAO();
		Map map = req.getParameterMap();
		PagedList pagedList = dao.search(map, ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
	  
	    req.setAttribute("mappedCategory",insuranceCompanyCategoryMappingDAO.findAllByKey("insurance_co_id", req.getParameter("insurance_co_id")));
	    req.setAttribute("insuranceCategories", itemInsuranceCategoriesDAO.listAll(null,"system_category","N","insurance_category_id"));
			req.setAttribute("healthAuthorities", healthAuthorityMasterDAO.listAll());
			return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		boolean success = false;
		InsuCompMasterForm InsuForm = (InsuCompMasterForm)f;
		InsuCompMasterDTO dto = new InsuCompMasterDTO();
		InsuCompMasterDAO dao = new InsuCompMasterDAO();

		List errors = new ArrayList();
		Connection con = null;
		ActionRedirect redirect = new ActionRedirect(m.findForward("addRedirect"));
		String insurance_co_id = dao.getNextICId();

		dto.setInsuruledoc(InsuForm.getInsuruledoc());
		dto.setDefault_rate_plan(InsuForm.getDefault_rate_plan());
		dto.setInsurance_co_address(InsuForm.getInsurance_co_address());
		dto.setInsurance_co_city(InsuForm.getInsurance_co_city());
		dto.setInsurance_co_country(InsuForm.getInsurance_co_country());
		dto.setInsurance_co_email(InsuForm.getInsurance_co_email());
		dto.setInsurance_co_name(InsuForm.getInsurance_co_name());
		dto.setInsurance_co_phone(InsuForm.getInsurance_co_phone());
		dto.setInsurance_co_state(InsuForm.getInsurance_co_state());
		dto.setStatus(InsuForm.getStatus());
		dto.setTin_number(InsuForm.getTin_number());
		dto.setInterface_code(InsuForm.getInterface_code());
		FlashScope flash = FlashScope.getScope(req);

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if(errors.isEmpty()) {

			BasicDynaBean bean = dao.getBean();
			boolean exists=dao.loadByteaRecords(bean, "insurance_co_name", req.getParameter("insurance_co_name"));

			if(exists == false) {

			success = dao.insertInsuRuleDocUploadFiles(dto, con,insurance_co_id);

			if(success)
				success = saveOrUpdateHealthAuthorityCodes(insurance_co_id, con, req);
			
			if(success)
			  success = saveOrUpdateInsurnaceCategoryMapped(insurance_co_id, con, req);

			if (success) {
				con.commit();
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				flash.info("Insurance Company details inserted successfully.");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				redirect.addParameter("insurance_co_id", (String)insurance_co_id);

			 }else {
				  con.rollback();
				  flash.error("Failed to add  Insurance Company Details.");
				  redirect = new ActionRedirect(m.findForward("addRedirect"));
				  redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				  redirect.addParameter("insurance_co_id", (String)insurance_co_id);

			 }
			 }else {
				flash.error("Insurance Company name already exists.");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			 }
			 }else {
				flash.error("Incorrectly formatted values supplied.");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			 }

		   }finally {
			   DataBaseUtil.commitClose(con, success);
		     }

		      return redirect;

	}

	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		InsuCompMasterDAO dao = new InsuCompMasterDAO();
		BasicDynaBean bean = dao.getInsuranceCompanyDetails((String)req.getParameter("insurance_co_id"));
		String docName = null;
		if(null != bean && null != bean.get("insurance_rules_doc_name"))
			docName = (String)bean.get("insurance_rules_doc_name");
		if(null != docName && !docName.equals(""))
			dao.loadByteaRecords(bean, "insurance_co_id", req.getParameter("insurance_co_id"));
		
		req.setAttribute("mappedCategory",insuranceCompanyCategoryMappingDAO.findAllByKey("insurance_co_id", req.getParameter("insurance_co_id")));
		req.setAttribute("insuranceCategories", itemInsuranceCategoriesDAO.listAll(null,"system_category","N","insurance_category_id"));
		req.setAttribute("bean", bean);
		req.setAttribute("insuranceCompaniesLists", js.serialize(dao.getInsuranceCompaniesNamesAndIds()));
		req.setAttribute("healthAuthorities", healthAuthorityMasterDAO.listAll());
		req.setAttribute("healthAuthorityCodes", haInsCompanyCodeDAO.listAll(null, "insurance_co_id", req.getParameter("insurance_co_id"),"insurance_co_id"));

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		Map params = getParameterMap(req);
		List errors = new ArrayList();

		InsuCompMasterDAO dao = new InsuCompMasterDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		InsuCompMasterForm InsuForm = (InsuCompMasterForm)f;

		FormFile formFile =InsuForm.getInsuruledoc();
		String fileName = formFile.getFileName();
		String contentType = MimeTypeDetector.getMimeTypes(formFile.getInputStream()).toString();

		String extension = "";
		if(fileName.contains(".")) {
			extension = fileName.substring(fileName.indexOf(".")+1);
	        if(extension.equals("odt") || extension.equals("ods") )
	        	contentType = "application/vnd.oasis.opendocument.text";
		}

		InputStream isfile=(InputStream)formFile.getInputStream();

		bean.set("insurance_co_name", InsuForm.getInsurance_co_name());
		bean.set("insurance_co_address", InsuForm.getInsurance_co_address());
		bean.set("insurance_co_city", InsuForm.getInsurance_co_city());
		bean.set("insurance_co_state", InsuForm.getInsurance_co_state());
		bean.set("insurance_co_country", InsuForm.getInsurance_co_country());
		bean.set("insurance_co_phone",InsuForm.getInsurance_co_phone());
		bean.set("insurance_co_email", InsuForm.getInsurance_co_email());
		bean.set("default_rate_plan", InsuForm.getDefault_rate_plan());
		bean.set("status", InsuForm.getStatus());
		bean.set("insurance_rules_doc_name", fileName);
		bean.set("insurance_rules_doc_type",contentType);
		bean.set("insurance_rules_doc_bytea", isfile);
		bean.set("tin_number", InsuForm.getTin_number());
		bean.set("interface_code", InsuForm.getInterface_code());

		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("insurance_co_id",req.getParameter("insurance_co_id"));

		Object key = req.getParameter("insurance_co_id");
		Map<String, String> keys = new HashMap<String, String>();
		keys.put("insurance_co_id", key.toString());

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (errors.isEmpty()) {

				if (bean.get("status") != null && ((String)bean.get("status")).equals("I")) {
					List<BasicDynaBean> patCatDependants = getPatientCategoryDependants((String)key);
					if (patCatDependants != null && patCatDependants.size() > 0) {
						flash.error("Cannot mark Insurance Company as inactive. <br/>" +
								" One (or) more Patient Category Insurance Companys (OP Allowed companies/IP Allowed companies) are linked with this Insurance Company.");
						return redirect;
					}

					List<BasicDynaBean> compTPADependants = getCompanyTPADependants((String)key);
					if (compTPADependants != null && compTPADependants.size() > 0) {
						flash.error("Cannot mark Insurance Company as as inactive. <br/>" +
								" One (or) more Insurance Company TPAs are linked with this Insurance Company.");
						return redirect;
					}

					List<BasicDynaBean> planTypeDependants = getPlanTypeDependants((String)key);
					if (planTypeDependants != null && planTypeDependants.size() > 0) {
						flash.error("Cannot mark Insurance Company as as inactive. <br/>" +
								" One (or) more Insurance Plan Types are linked with this Insurance Company.");
						return redirect;
					}

					List<BasicDynaBean> planDependants = getPlanDependants((String)key);
					if (planDependants != null && planDependants.size() > 0) {
						flash.error("Cannot mark Insurance Company as as inactive. <br/>" +
								" One (or) more Insurance Plans are linked with this Insurance Company.");
						return redirect;
					}
				}

				int success = dao.update(con, bean.getMap(), keys);

				if(success >= 0)
					success = !saveOrUpdateHealthAuthorityCodes(key.toString(), con, req) ? 0 : 1;
				if(success >= 0)
				  success = !saveOrUpdateInsurnaceCategoryMapped(key.toString(), con, req) ? 0 :1;
				if (success > 0) {
					con.commit();
					flash.info("Insurance Company details updated successfully.");


				} else {
					con.rollback();
					flash.error("Failed to update Insurance company details..");
				}
			}
			else {
				flash.error("Incorrectly formatted values supplied");
			}
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		return redirect;
	}

	public static final String GET_PATIENT_CATEGORY_DEPENDANTS =
		"SELECT ip_allowed_insurance_co_ids, op_allowed_insurance_co_ids, status FROM patient_category_master"
		+ " WHERE status = 'A'"
		+ " AND ("
		+ "   (ip_allowed_insurance_co_ids IS NOT NULL AND ip_allowed_insurance_co_ids != '*' AND ? = any(string_to_array(replace(ip_allowed_insurance_co_ids, ' ', ''), ',')))"
		+ "   OR "
		+ "   (op_allowed_insurance_co_ids IS NOT NULL AND op_allowed_insurance_co_ids != '*' AND ? = any(string_to_array(replace(op_allowed_insurance_co_ids, ' ', ''), ',')))"
		+ ") ";

	private List<BasicDynaBean> getPatientCategoryDependants(String insCompId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_PATIENT_CATEGORY_DEPENDANTS, new Object[]{insCompId, insCompId});
	}

	public static final String GET_COMPANY_TPA_DEPENDANTS =
		" SELECT * FROM insurance_company_tpa_master WHERE insurance_co_id = ? ";

	private List<BasicDynaBean> getCompanyTPADependants(String insCompId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_COMPANY_TPA_DEPENDANTS, new Object[]{insCompId});
	}

	public static final String GET_PLAN_TYPE_DEPENDANTS =
		" SELECT * FROM insurance_category_master WHERE insurance_co_id = ? AND status = 'A' ";

	private List<BasicDynaBean> getPlanTypeDependants(String insCompId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_PLAN_TYPE_DEPENDANTS, new Object[]{insCompId});
	}

	public static final String GET_PLAN_DEPENDANTS =
		" SELECT * FROM insurance_plan_main WHERE insurance_co_id = ? AND status = 'A' ";

	private List<BasicDynaBean> getPlanDependants(String insCompId) throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_PLAN_DEPENDANTS, new Object[]{insCompId});
	}

	private boolean saveOrUpdateHealthAuthorityCodes(String tpa_id, Connection con,HttpServletRequest request)
	throws SQLException, IOException{

		boolean flag = true;
		String [] healthAuth = request.getParameterValues("h_health_authority");
		String [] InsCompCodes = request.getParameterValues("h_ins_comp_code");
		String [] h_ha_insurance_co_code_id = request.getParameterValues("h_ha_insurance_co_code_id");
		String [] hacodeoldrnew = request.getParameterValues("hacodeoldrnew");
		String [] delete = request.getParameterValues("h_ha_deleted");

		if(healthAuth != null){
			for(int i=0; i<healthAuth.length; i++){
				BasicDynaBean bean  = haInsCompanyCodeDAO.getBean();
				bean.set("insurance_co_id", tpa_id);
				bean.set("health_authority", healthAuth[i]);
				bean.set("insurance_co_code", InsCompCodes[i]);
				if (hacodeoldrnew[i].equalsIgnoreCase("new") && delete[i].equalsIgnoreCase("false")) {
					bean.set("ha_insurance_co_code_id", haInsCompanyCodeDAO.getNextSequence());
					flag = haInsCompanyCodeDAO.insert(con, bean);
				}else if (hacodeoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("false")) {
					if(h_ha_insurance_co_code_id != null && h_ha_insurance_co_code_id[i] != null && !h_ha_insurance_co_code_id[i].isEmpty()) {
						Map<String, Integer> keys = new HashMap<String, Integer>();
						bean.set("ha_insurance_co_code_id", Integer.parseInt(h_ha_insurance_co_code_id[i]));
						keys.put("ha_insurance_co_code_id", Integer.parseInt(h_ha_insurance_co_code_id[i]));

						if(flag)
							flag = haInsCompanyCodeDAO.update(con, bean.getMap(), keys) > 0;
					}
				}else if (hacodeoldrnew[i].equalsIgnoreCase("old") && delete[i].equalsIgnoreCase("true")) {
					if(h_ha_insurance_co_code_id != null && h_ha_insurance_co_code_id[i] != null && !h_ha_insurance_co_code_id[i].isEmpty()) {
						if (flag) flag = haInsCompanyCodeDAO.delete(con, "ha_insurance_co_code_id", Integer.parseInt(h_ha_insurance_co_code_id[i]));
					}
				}

			}

		}
		return flag;
	}

  private boolean saveOrUpdateInsurnaceCategoryMapped(String insuranceCompanyId, Connection con,
      HttpServletRequest request) throws SQLException, IOException {
    boolean flag = true;
    insuranceCompanyCategoryMappingDAO.delete(con, "insurance_co_id", insuranceCompanyId);
    String[] selectedInsuranceCategory = request.getParameterValues("insurance_category");
    if (null != selectedInsuranceCategory) {
      for (int i = 0; i < selectedInsuranceCategory.length; i++) {
        BasicDynaBean bean = insuranceCompanyCategoryMappingDAO.getBean();
        bean.set("insurance_co_id", insuranceCompanyId);
        bean.set("insurance_category_id", Integer.parseInt(selectedInsuranceCategory[i]));
        flag = insuranceCompanyCategoryMappingDAO.insert(con, bean);
      }
    }
    List<BasicDynaBean> systemCategoryBeans = itemInsuranceCategoriesDAO.listAll(null,"system_category","Y","insurance_category_id");
    for(BasicDynaBean systemBean : systemCategoryBeans) {
      BasicDynaBean bean = insuranceCompanyCategoryMappingDAO.getBean();
      bean.set("insurance_co_id", insuranceCompanyId);
      bean.set("insurance_category_id", systemBean.get("insurance_category_id"));
      flag &= insuranceCompanyCategoryMappingDAO.insert(con, bean);
    }
    return flag;
  }
	public ActionForward getviewInsuDocument(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		String id = req.getParameter("inscoid");
		String fileName = "";
		String contentType = "";

		if (id == null) {
			return m.findForward("error");
		}

		Map<String,Object> uploadMap = InsuCompMasterDAO.getUploadedDocInfo(id);

		if (uploadMap.isEmpty()) {
			return m.findForward("error");
		}

		fileName = (String)uploadMap.get("filename");
		contentType = (String)uploadMap.get("contenttype");
		res.setContentType(contentType);

		if (!fileName.equals("")) {
			res.setHeader("Content-disposition", "attachment; filename=\""+fileName+"\"");
		}

		OutputStream os = res.getOutputStream();

		InputStream is = (InputStream)uploadMap.get("uploadfile");
		if (is != null) {
			byte[] bytes = new byte[4096];
			int len = 0;
			while ( (len = is.read(bytes)) > 0) {
				os.write(bytes, 0, len);
			}
			os.flush();
	    is.close();
		}
		return null;
	}
}
