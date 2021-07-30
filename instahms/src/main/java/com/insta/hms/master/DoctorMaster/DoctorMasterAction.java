package com.insta.hms.master.DoctorMaster;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.bob.hms.common.RequestContext;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.InputValidator;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DoctorCenterApplicability.CenterDAO;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.mdm.FileOperationService;
import com.insta.hms.mdm.FileOperationService.OperationScreenType;
import com.insta.hms.mdm.resourceavailability.ResourceAvailabilityService;
import com.insta.hms.mdm.salutations.SalutationRepository;
import com.insta.hms.usermanager.UserSignatureDAO;
import com.insta.hms.xls.exportimport.ChargesImportExporter;
import com.insta.hms.xls.exportimport.DetailsImportExporter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

public class DoctorMasterAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(DoctorMasterAction.class);
	static DoctorImagesDAO dImageDao = new DoctorImagesDAO();
	static DoctorChargeDAO docChargesdao = new DoctorChargeDAO();
	static CenterMasterDAO centerDao = new CenterMasterDAO();
	static DoctorMasterDAO dao = new DoctorMasterDAO();

	FileOperationService fos = ApplicationContextProvider.getBean(FileOperationService.class);
	SessionService sessionService = ApplicationContextProvider.getBean(SessionService.class);
	SalutationRepository salutationRepository = ApplicationContextProvider.getBean(SalutationRepository.class);
	
    ResourceAvailabilityService resourceAvailabilityService = ApplicationContextProvider.getBean(ResourceAvailabilityService.class);

    AppointmentService appointmentService = ApplicationContextProvider.getBean(AppointmentService.class);
    
	private static JSONSerializer js = new JSONSerializer().exclude("class");
    private static final GenericDAO practitionerTypeDAO = new GenericDAO("practitioner_types");
    private static final GenericDAO departmentDAO = new GenericDAO("department");
    private static final GenericDAO doctorOrgDetailsDAO = new GenericDAO("doctor_org_details");
    private static final GenericDAO doctorOpConsultationChargeDAO =
        new GenericDAO("doctor_op_consultation_charge");
    

    /** Default value for Online consultation. */
    private static final String DEFAULT_ONLINE_CONSULTATION_VALUE = "N";
 

    /** Default value for Online Visit Mode. */    
    private static final String ONLINE_VISIT_MODE = "O";
	
	public ActionForward list(ActionMapping am, ActionForm af,
            HttpServletRequest request, HttpServletResponse res)
            throws Exception {

		DoctorMasterDAO dao = new DoctorMasterDAO();

		Map chargeMap = new HashMap();
		for (int i=0; i<DoctorMasterDAO.chargeValues.length; i++) {
			chargeMap.put(DoctorMasterDAO.chargeValues[i], DoctorMasterDAO.chargeTexts[i]);
		}

		String orgId = request.getParameter("org_id");
		if ( (orgId == null) || orgId.isEmpty()) {
			orgId = "ORG0001";
		}

		String charge_type = request.getParameter("_charge_type");
		if ( (charge_type == null) || charge_type.isEmpty()) {
			charge_type = "op_charge";
		}

		Map requestParams = request.getParameterMap();
		PagedList list = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams));

		List<String> ids = new ArrayList<String>();
		for (Map obj : (List<Map>) list.getDtoList()) {
			ids.add((String) obj.get("doctor_id"));
		}

		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();
		List chargeList = null;
		Map chargesMap = null;

		if(charge_type.equals("op_charge") || charge_type.equals("op_revisit_charge") || charge_type.equals("private_cons_charge")
				|| charge_type.equals("private_cons_revisit_charge")){
			chargeList = dao.getAllOPChargesForOrg(orgId, ids);
			chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "doctor_id" ,"org_id");
		}else{
			chargeList = dao.getAllIPChargesForOrg(orgId, ids);
			chargesMap = ConversionUtils.listBeanToMapMapBean(chargeList, "doctor_name", "bed_type");
		}

		request.setAttribute("chargeValues", DoctorMasterDAO.chargeValues);
		request.setAttribute("chargeMap", chargeMap);

		request.setAttribute("pagedList", list);
		request.setAttribute("charges", chargesMap);
		request.setAttribute("bedTypes", bedTypes);
		request.setAttribute("allDoctorsCount", DoctorMasterDAO.getAllDocorsCoust());

		JSONSerializer js = new JSONSerializer();
		request.setAttribute("doctorNames", js.serialize(dao.getAllNames()));
		request.setAttribute("orgId", orgId);
		request.setAttribute("charge_type", charge_type);
		request.setAttribute("centers", centerDao.getAllCentersAndSuperCenterAsFirst());

		 return am.findForward("list");

	}

	public ActionForward add(ActionMapping am, ActionForm af,
			HttpServletRequest request, HttpServletResponse res) throws Exception{

		DoctorMasterDAO dao = new DoctorMasterDAO();
		String orgId = request.getParameter("orgId");
		orgId = (null == orgId || "".equals(orgId)) ? request.getParameter("org_id") : orgId;
		request.setAttribute("org_id", orgId);
		List<String> columns = new ArrayList<String>();
		columns.add("practitioner_id");
		columns.add("practitioner_name");
		List<BasicDynaBean> practitionerTypes = practitionerTypeDAO.listAll(columns,"status","A");
		request.setAttribute("PractitionerTypes", ConversionUtils.listBeanToListMap(practitionerTypes));

		request.setAttribute("mode", "insert");
		request.setAttribute("GDocDetails", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("OpConsultTemplates", GenericDocumentTemplateDAO.getTemplates(true, "SYS_CONSULT", "A"));
		request.setAttribute("doctorNames", new JSONSerializer().serialize(dao.getAllNames()));
		//request.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		String countryCode = centerDao.getCountryCode(RequestContext.getCenterId());
		if(StringUtil.isNullOrEmpty(countryCode)){
		  countryCode = centerDao.getCountryCode(0);
		}
		request.setAttribute("defaultCountryCode", countryCode);
		request.setAttribute("countryList", PhoneNumberUtil.getAllCountries());

		return am.findForward("doctorDetails");
	}

	public ActionForward addNewDoctor(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)throws Exception{

		ActionRedirect redirect=new ActionRedirect(am.findForward("addRedirect"));
		String doctorId = null;
		Map params = getParameterMap(req);
		String orgId = getParameter(params, "org_id");
		FlashScope flash = FlashScope.getScope(req);
		HttpSession session = req.getSession();
		String userName = (String)session.getAttribute("userid");
		//String centerId = getParameter(params,"center_id");

		DoctorMasterDAO sdao = new DoctorMasterDAO();
		DoctorChargeDAO cdao = new DoctorChargeDAO();

		ArrayList errors = new ArrayList();

		BasicDynaBean doctor = sdao.getBean();
		doctor.set("created_timestamp",DateUtil.getCurrentTimestamp());

		ConversionUtils.copyToDynaBean(params, doctor, errors);
		BasicDynaBean serviceSubGrpBean = new ServiceSubGroupDAO().getServiceSubGroupBean("Doctor", "Doctor");

		String doctorMobile = String.valueOf(doctor.get("doctor_mobile"));
		String defaultCode = centerDao.getCountryCode(RequestContext.getCenterId());
		List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
				doctorMobile, null);
		if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
				&& !splitCountryCodeAndText.get(0).isEmpty()) {
			doctor.set("doctor_mobile_country_code", "+"+splitCountryCodeAndText.get(0));
			doctor.set("doctor_mobile", "+" + splitCountryCodeAndText.get(0)
					+ splitCountryCodeAndText.get(1));
		} else if (defaultCode != null) {
			doctor.set("doctor_mobile_country_code", "+" + defaultCode);
			if (doctorMobile != null && !doctorMobile.equals("") && !doctorMobile.startsWith("+")) {
				doctor.set("doctor_mobile", "+" + defaultCode + doctorMobile);
			}
		}

		doctorId = sdao.getNextDoctorId();
		doctor.set("doctor_id", doctorId);
		doctor.set("service_sub_group_id", serviceSubGrpBean.get("service_sub_group_id"));

		if(getParameter(params, "schedule")==null)
			doctor.set("schedule",false);
		
		if(getParameter(params, "send_feedback_sms")==null)
			doctor.set("send_feedback_sms",false);

		if(getParameter(params, "available_for_online_consults")==null)
            doctor.set("available_for_online_consults", DEFAULT_ONLINE_CONSULTATION_VALUE);
		
		/*if(getParameter(params, "overbook")==null)
			doctor.set("overbook",false);*/

		String doctor_type = getParameter(params, "doctor_type");
		if(doctor_type.equals("CONSULTANT"))
			doctor.set("consulting_doctor_flag", "Y");
		else
			doctor.set("consulting_doctor_flag", "N");

	//	if(centerId == null || centerId.equals(""))
		//	doctor.set("center_id", 0);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return new ActionRedirect(am.findForward("getDashboard"));
		}

		BasicDynaBean doctorOrgBean = doctorOrgDetailsDAO.getBean();
		ConversionUtils.copyToDynaBean(req.getParameterMap(), doctorOrgBean, errors, true);

		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			String exists = sdao.getDoctorID(doctor.get("doctor_name"), doctor.get("dept_id"));
			if (exists == null) {
				// 1. Insert the operation
				success = sdao.insert(con, doctor);
				// Insert the doctor_center_master
				CenterDAO doctorCenterDAO = new CenterDAO();
				if (success) {
					BasicDynaBean cbean = doctorCenterDAO.getBean();
					cbean.set("doc_center_id", doctorCenterDAO.getNextSequence());
					cbean.set("doctor_id", doctor.get("doctor_id"));
					cbean.set("center_id", 0);
					cbean.set("status", "A");
					success &= doctorCenterDAO.insert(con, cbean);
				}

				 // insert the doctor photo
				BasicDynaBean doctorImageBean = dImageDao.getBean();
				ConversionUtils.copyToDynaBean(params, doctorImageBean, errors);
				int formFileCount = 0;
				if (success && doctorImageBean.get("photo") != null) {
					formFileCount++;
					success = false;
					doctorImageBean.set("doctor_id", doctorId);
					success = dImageDao.insert(con, doctorImageBean);
				}
				Object[] signature = ((Object[]) params.get("userSignature"));
				if (success && signature != null && signature[0] != null) {
					UserSignatureDAO sigDAO = new UserSignatureDAO();
					BasicDynaBean sigBean = sigDAO.getBean();
					sigBean.set("doctor_id", doctorId);
					sigBean.set("signature", signature[0]);
					Object content_type = ConvertUtils.convert(((Object[]) params.get("content_type"))[formFileCount], java.lang.String.class);
					sigBean.set("signature_content_type", content_type);
					success = sigDAO.insert(con, sigBean);
				}
/*				if (success) {
					doctorOrgBean.set("doctor_id", doctorId);
					doctorOrgBean.set("applicable", true);
					doctorOrgBean.set("org_id", orgId);
					doctorOrgBean.set("username", userName);
					doctorOrgBean.set("mod_time", DateUtil.getCurrentTimestamp());
					success = new GenericDAO("doctor_org_details").insert(con, doctorOrgBean);
					cdao.copyDoctorDetailsToAllOrgs(con, doctorId);
				} */
				if (success){
					//Insert Zero Charges for both OP & IP for Doctor
					cdao.initItemCharges(con, doctorId, userName);
					//cdao.insertZeroChargesToAllOrgsIP(con,doctorId);
					//cdao.insertZeroChargesToAllOrgsOP(con, doctorId);

					flash.success("Doctor details are saved successfully.." );
					redirect = new ActionRedirect(am.findForward("showRedirect"));
					redirect.addParameter("org_id", orgId);
					redirect.addParameter("doctor_id", doctorId);
					redirect.addParameter("mode", "update");
					return redirect;
				}else{
					flash.error("Faild to Save Doctor Details.." );
				}
			} else {
				flash.error("Doctor name already exists...");
			}

			} finally {
				DataBaseUtil.commitClose(con, success);
			}

		redirect.addParameter("org_id", orgId);
		redirect.addParameter("doctor_id", doctorId);
		redirect.addParameter("mode", getParameter(params, "mode"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
	public ActionForward getDoctorDetailsScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		String doctorId = req.getParameter("doctor_id");
		String orgId = req.getParameter("org_id");
		String mode = req.getParameter("mode");

		DoctorMasterDAO dao = new DoctorMasterDAO();
		BasicDynaBean docDetails = dao.getDoctorDetails(doctorId);
		List<String> columns = new ArrayList<String>();
		columns.add("practitioner_id");
		columns.add("practitioner_name");
		List<BasicDynaBean> practitionerTypes = practitionerTypeDAO.listAll(columns,"status","A");
		req.setAttribute("PractitionerTypes", ConversionUtils.listBeanToListMap(practitionerTypes));
		req.setAttribute("DocDetails", docDetails.getMap());
		req.setAttribute("GDocDetails", GenericPreferencesDAO.getGenericPreferences());
		req.setAttribute("OpConsultTemplates", GenericDocumentTemplateDAO.getTemplates(true, "SYS_CONSULT", "A"));
		req.setAttribute("mode", mode);
		req.setAttribute("org_id", orgId);
		req.setAttribute("doctor_id", doctorId);
		req.setAttribute("doctorNames", new JSONSerializer().serialize(dao.getAllNames()));
		req.setAttribute("photoExist", dImageDao.imageExist(doctorId));

		UserSignatureDAO sigDAO = new UserSignatureDAO();
		BasicDynaBean sigBean = sigDAO.getBean();
		sigDAO.loadByteaRecords(sigBean, "doctor_id", doctorId);
		req.setAttribute("signature_username", sigBean.get("doctor_id"));
		req.setAttribute("centers", centerDao.getAllCentersExceptSuper());
		String countryCode = centerDao.getCountryCode(RequestContext.getCenterId());
		if(StringUtil.isNullOrEmpty(countryCode)){
		  countryCode = centerDao.getCountryCode(0);
		}
		req.setAttribute("defaultCountryCode", countryCode);
		req.setAttribute("countryList", PhoneNumberUtil.getAllCountries());

		return m.findForward("doctorDetails");
	}

	public ActionForward updateDoctorDetails(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)throws Exception{

		ActionRedirect redirect=new ActionRedirect(am.findForward("showRedirect"));
		Map params = getParameterMap(req);
		String orgId = getParameter(params,"org_id");
		String doctor_id = getParameter(params, "doctor_id");
		FlashScope flash = FlashScope.getScope(req);
		//String centerId = getParameter(params,"center_id");

		DoctorMasterDAO sdao = new DoctorMasterDAO();
		ArrayList errors = new ArrayList();
		BasicDynaBean doctor = sdao.getBean();
		doctor.set("updated_timestamp",DateUtil.getCurrentTimestamp());

		
		if(getParameter(params, "send_feedback_sms")==null)
			doctor.set("send_feedback_sms",false);

		if(getParameter(params, "available_for_online_consults")==null)
            doctor.set("available_for_online_consults", DEFAULT_ONLINE_CONSULTATION_VALUE);
		/*if(getParameter(params, "overbook")==null)
			doctor.set("overbook",false);*/

		String doctor_type = getParameter(params, "doctor_type");
		if(doctor_type.equals("CONSULTANT"))
			doctor.set("consulting_doctor_flag", "Y");
		else
			doctor.set("consulting_doctor_flag", "N");

		ConversionUtils.copyToDynaBean(params, doctor, errors);

		//if(centerId == null || centerId.equals(""))
		//	doctor.set("center_id", 0);
		String doctorMobile = String.valueOf(doctor.get("doctor_mobile"));
		String doctorMobileCountryCode = String.valueOf(doctor.get("doctor_mobile_country_code"));
		String defaultCode = centerDao.getCountryCode(RequestContext.getCenterId());
		List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
				doctorMobile, null);
		if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
				&& !splitCountryCodeAndText.get(0).isEmpty()) {
			doctor.set("doctor_mobile_country_code", "+"+splitCountryCodeAndText.get(0));
			doctor.set("doctor_mobile", "+" + splitCountryCodeAndText.get(0)
					+ splitCountryCodeAndText.get(1));
		} else if (defaultCode != null) {
			doctor.set("doctor_mobile_country_code", "+" + defaultCode);
			if (doctorMobile != null && !doctorMobile.equals("") && !doctorMobile.startsWith("+")) {
				doctor.set("doctor_mobile", "+" + defaultCode + doctorMobile);
			}
		}
		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			redirect = new ActionRedirect("DoctorMaster.do?_method=list");
			return redirect;
		}

		Connection con = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			String exists = sdao.getDoctorID(doctor.get("doctor_name"), doctor.get("dept_id"));
			if (exists != null && !exists.equals(doctor_id)) {
				flash.error("Doctor name already exists...");
			} else {
				success = (1 == sdao.update(con, doctor.getMap(), "doctor_id",doctor_id));
				 // update the doctor photo
				BasicDynaBean doctorImageBean = dImageDao.getBean();
				ConversionUtils.copyToDynaBean(params, doctorImageBean, errors);
				int formFileCount = 0;
				if (success && doctorImageBean.get("photo") != null) {
					formFileCount++;
					success = false;
					doctorImageBean.set("doctor_id", doctor_id);
					success = dImageDao.insertOrupdatePhoto(con, doctor_id, doctorImageBean);
				}
				Object[] signature = ((Object[]) params.get("userSignature"));
				if (success) {

					UserSignatureDAO sigDAO = new UserSignatureDAO();
					BasicDynaBean sigBean = sigDAO.getBean();
					sigDAO.loadByteaRecords(sigBean, "doctor_id", doctor_id);
					if (sigBean.get("doctor_id") == null) {
						if (signature != null && signature[0] != null) {
							sigBean.set("doctor_id", doctor_id);
							sigBean.set("signature", signature[0]);
							Object content_type = ConvertUtils.convert(((Object[]) params.get("content_type"))[formFileCount], java.lang.String.class);
							sigBean.set("signature_content_type", content_type);
							success = sigDAO.insert(con, sigBean);
						}
					} else {
						if (signature != null && signature[0] != null) {
							sigBean.set("doctor_id", doctor_id);
							sigBean.set("signature", signature[0]);
							Object content_type = ConvertUtils.convert(((Object[]) params.get("content_type"))[formFileCount], java.lang.String.class);
							sigBean.set("signature_content_type", content_type);
							success = sigDAO.update(con, sigBean.getMap(), "doctor_id", doctor_id) == 1;
						} else {
							// ignore it if is empty.
						}
					}
				}
				if(success){
					flash.success("Doctor details updated successfully.." );
				}else{
					flash.error("Faild to update Doctor Details.." );
				}
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter("org_id", orgId);
		redirect.addParameter("doctor_id", doctor_id);
		redirect.addParameter("mode","update");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward getDoctorChargesScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		String doctorId = req.getParameter("doctor_id");
		String orgId = req.getParameter("org_id");
		String mode = req.getParameter("mode");

		JSONSerializer json = new JSONSerializer().exclude("class");
		DoctorMasterDAO dao = new DoctorMasterDAO();
		BasicDynaBean docDetails = dao.getAllChargesForEdit(orgId, doctorId,mode);

		DoctorChargeDAO cdao = new DoctorChargeDAO();
		List<BasicDynaBean> chargeList = cdao.getAllChargesForOrgDoctor(orgId,doctorId);
		List<String> bedTypes = new BedMasterDAO().getUnionOfBedTypes();

		List<BasicDynaBean> derivedRatePlanDetails = docChargesdao.getDerivedRatePlanDetails(orgId, doctorId);

		if(derivedRatePlanDetails.size()<0)
        	req.setAttribute("derivedRatePlanDetails", json.serialize(Collections.EMPTY_LIST));
        else
        	req.setAttribute("derivedRatePlanDetails", json.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));

		req.setAttribute("charges", ConversionUtils.listBeanToMapMap(chargeList, "bed_type"));
		if(docDetails != null)
			req.setAttribute("DocDetails", docDetails.getMap());
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("mode", mode);
		req.setAttribute("org_id", orgId);
		req.setAttribute("doctorsList", json.serialize(dao.getDoctorsNamesAndIds()));

		return m.findForward("defineCharges");
	}
	public ActionForward addOrUpdateCharges(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)throws Exception{

		ActionRedirect redirect=new ActionRedirect(am.findForward("showcharges"));
		String orgId = req.getParameter("org_id");
		String doctorId = req.getParameter("doctor_id");
		FlashScope flash = FlashScope.getScope(req);
		String mode = req.getParameter("mode");
		String[] derivedRateplanIds = req.getParameterValues("ratePlanId");

        BasicDynaBean orgDetails = doctorOrgDetailsDAO.getBean();
		DoctorChargeDAO ipChargeDao = new DoctorChargeDAO();

		ArrayList errors = new ArrayList();

		BasicDynaBean doctorOP = doctorOpConsultationChargeDAO.getBean();
		BasicDynaBean doctorIP = ipChargeDao.getBean();

		ConversionUtils.copyToDynaBean(req.getParameterMap(), doctorOP, errors);

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			return new ActionRedirect(am.findForward("getDashboard"));
		}
		doctorIP.set("doctor_name", doctorId);

		String[] beds = req.getParameterValues("bed_type");

		List<BasicDynaBean> chargeList = new ArrayList();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = ipChargeDao.getBean();
			ConversionUtils.copyToDynaBean(req.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, charge, errors);
			charge.set("doctor_name", doctorId);
			chargeList.add(charge);
		}

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if(mode.equals("insert")){

				success = (1 == doctorOpConsultationChargeDAO.updateWithNames(con, doctorOP.getMap(),  new String[] {"doctor_id", "org_id"}));

				if(success){
					for (BasicDynaBean c: chargeList) {
						ipChargeDao.updateWithNames(con, c.getMap(), new String[] {"doctor_name", "organization", "bed_type"});
					}
				}
//					 5. Copy the General Rate Plan details for all orgs
					ipChargeDao.copyGeneralChargesToAllOrgsOP(con, doctorId);
					ipChargeDao.copyGeneralChargesToAllOrgsIP(con, doctorId);
					ipChargeDao.copyGeneralChargesToInactiveBeds(con, doctorId);
					ipChargeDao.copyGeneralChargesToInactiveIcuBeds(con, doctorId);

					success = true;

			}else{

				ConversionUtils.copyToDynaBean(req.getParameterMap(), orgDetails, errors);
				orgDetails.set("applicable", true);
				success = doctorOrgDetailsDAO.updateWithNames(con, orgDetails.getMap(), new String[] {"doctor_id", "org_id"}) > 0;

				success = (1 == doctorOpConsultationChargeDAO.updateWithNames(con, doctorOP.getMap(),  new String[] {"doctor_id", "org_id"}));

				if(success){
					for (BasicDynaBean c: chargeList) {
						ipChargeDao.updateWithNames(con, c.getMap(), new String[] {"doctor_name", "organization", "bed_type"});
					}
				}

				// updating derived rateplans charges
				if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {
					// updating op consultation charges for derived rate plans
					Double op_charge = new Double(req.getParameter("op_charge"));
					Double op_revisit_charge = new Double(req.getParameter("op_revisit_charge"));
					Double private_cons_charge = new Double(req.getParameter("private_cons_charge"));
					Double private_cons_revisit_charge = new Double(req.getParameter("private_cons_revisit_charge"));
					Double op_oddhr_charge = new Double(req.getParameter("op_oddhr_charge"));

					Double op_charge_discount = new Double(req.getParameter("op_charge_discount"));
					Double op_revisit_discount = new Double(req.getParameter("op_revisit_discount"));
					Double private_cons_discount = new Double(req.getParameter("private_cons_discount"));
					Double private_revisit_discount = new Double(req.getParameter("private_revisit_discount"));
					Double op_oddhr_charge_discount = new Double(req.getParameter("op_oddhr_charge_discount"));

					success = docChargesdao.updateOpChargesForDerivedRatePlans(con,orgId,derivedRateplanIds,
							op_charge,op_revisit_charge,private_cons_charge,private_cons_revisit_charge,
							op_oddhr_charge,doctorId,doctorOpConsultationChargeDAO,op_charge_discount,op_revisit_discount,
							private_cons_discount,private_revisit_discount,op_oddhr_charge_discount);

					//updating ip consultation charges for derived rate plans
					String[] doctor_ip_charge = req.getParameterValues("doctor_ip_charge");
					String[] night_ip_charge = req.getParameterValues("night_ip_charge");
					String[] ot_charge = req.getParameterValues("ot_charge");
					String[] co_surgeon_charge = req.getParameterValues("co_surgeon_charge");
					String[] assnt_surgeon_charge = req.getParameterValues("assnt_surgeon_charge");
					String[] ward_ip_charge = req.getParameterValues("ward_ip_charge");

					String[] doctor_ip_charge_discount = req.getParameterValues("doctor_ip_charge_discount");
					String[] night_ip_charge_discount = req.getParameterValues("night_ip_charge_discount");
					String[] ot_charge_discount = req.getParameterValues("ot_charge_discount");
					String[] co_surgeon_charge_discount = req.getParameterValues("co_surgeon_charge_discount");
					String[] assnt_surgeon_charge_discount = req.getParameterValues("assnt_surgeon_charge_discount");
					String[] ward_ip_charge_discount = req.getParameterValues("ward_ip_charge_discount");

					Double[] doctorIpCharge  = new Double[doctor_ip_charge.length];
					Double[] nightIpCharge  = new Double[night_ip_charge.length];
					Double[] otCharge  = new Double[ot_charge.length];
					Double[] coSurgeonCharge  = new Double[co_surgeon_charge.length];
					Double[] assntsurgeonCharge  = new Double[assnt_surgeon_charge.length];
					Double[] wardIpCharge  = new Double[ward_ip_charge.length];

					Double[] doctorIpDiscount  = new Double[doctor_ip_charge_discount.length];
					Double[] nightIpDiscount  = new Double[night_ip_charge_discount.length];
					Double[] otDiscount  = new Double[ot_charge_discount.length];
					Double[] coSurgeonDiscount = new Double[co_surgeon_charge_discount.length];
					Double[] assntsurgeonDiscount  = new Double[assnt_surgeon_charge_discount.length];
					Double[] wardIpDiscount  = new Double[ward_ip_charge_discount.length];

			        for(int i = 0; i < doctor_ip_charge.length; i++) {
			        	doctorIpCharge[i] = new Double(doctor_ip_charge[i]);
			        	nightIpCharge[i] = new Double(night_ip_charge[i]);
			        	otCharge[i] = new Double(ot_charge[i]);
			        	coSurgeonCharge[i] = new Double(co_surgeon_charge[i]);
			        	assntsurgeonCharge[i] = new Double(assnt_surgeon_charge[i]);
			        	wardIpCharge[i] = new Double(ward_ip_charge[i]);
			        	doctorIpDiscount[i] = new Double(doctor_ip_charge_discount[i]);
						nightIpDiscount[i] = new Double(night_ip_charge_discount[i]);
						otDiscount[i] = new Double(ot_charge_discount[i]);
						coSurgeonDiscount[i] = new Double(co_surgeon_charge_discount[i]);
						assntsurgeonDiscount[i] = new Double(assnt_surgeon_charge_discount[i]);
						wardIpDiscount[i] = new Double(ward_ip_charge_discount[i]);
			        }

					success = docChargesdao.updateIPChargesForDerivedRateplans(con,orgId,derivedRateplanIds,
							doctorIpCharge,nightIpCharge,otCharge,coSurgeonCharge,assntsurgeonCharge,
							wardIpCharge,doctorId,beds,ipChargeDao,doctorIpDiscount,nightIpDiscount,otDiscount,
							coSurgeonDiscount,assntsurgeonDiscount,wardIpDiscount);

		        }

			}

			if(success){
				flash.success("Charges are Updated Successfully.." );
			}else{
				flash.error("Failed to update Charges.." );
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		redirect.addParameter("org_id", orgId);
		redirect.addParameter("doctor_id", doctorId);
		redirect.addParameter("mode","update");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

    /*
	 * Group Update: called from the main list screen, updates the charges of all/selected
	 * Equipments by a formula: +/- a certain amount or percentage,
	 */
	public ActionForward groupUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);

		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String userName = (String)req.getSession(false).getAttribute("userid");
		String orgId = req.getParameter("org_id");
		String amtType = req.getParameter("amtType");
		String incType = req.getParameter("incType");
		//String chargeType = req.getParameter("charge_type");
		String chargeType = InputValidator.getSafeSpecialString("Charge Type", req.getParameter("charge_type"), 50, false);
		String updateTable = req.getParameter("updateTable");
		String allDoctors = req.getParameter("allDoctors");
		String allBedTypes = req.getParameter("allBedTypes");

		List<String> doctors = null;
		if (!allDoctors.equals("yes"))
			doctors = ConversionUtils.getParamAsList(req.getParameterMap(), "selectDoctor");

		List<String> bedTypes = null;
		if (allBedTypes == null)
			bedTypes = ConversionUtils.getParamAsList(req.getParameterMap(), "selectBedType");

		BigDecimal amount;
		BigDecimal round;
		try {
			amount = new BigDecimal(req.getParameter("amount"));
			round = new BigDecimal(req.getParameter("round"));
		} catch (NumberFormatException e) {
			flash.put("error", "Incorrectly formatted parameters");
			return redirect;
		}

		if (incType.equals("-"))
			amount = amount.negate();

		DoctorChargeDAO dao = new DoctorChargeDAO();

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			dao.groupIncreaseCharges(con, orgId, chargeType, bedTypes,
					doctors, amount, amtType.equals("%"),round, updateTable);
			success = true;
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(success) {
			if(chargeType.equals("op_charge") || chargeType.equals("op_revisit_charge") || chargeType.equals("private_cons_charge")
					|| chargeType.equals("private_cons_revisit_charge")){
				dao.updateChargesForDerivedRatePlans(orgId, userName, "doctorOPcharges",false);
			} else {
				dao.updateChargesForDerivedRatePlans(orgId, userName, "doctorIPCharges",false);
			}
		}

		if (success)
			flash.put("success", "Charges updated successfully");
		else
			flash.put("error", "Error updating charges");

		return redirect;
	}
	private static final Map<String, String> opChgAliasMap = new HashMap<String, String>();

	static {

		opChgAliasMap.put("doctor name", "doctor_name");
		opChgAliasMap.put("department", "department");
		opChgAliasMap.put("op charge", "op_charge");
		opChgAliasMap.put("op charge discount", "op_charge_discount");
		opChgAliasMap.put("op revisit charge", "op_revisit_charge");
		opChgAliasMap.put("op revisit discount", "op_revisit_discount");
		opChgAliasMap.put("private consultation charge", "private_cons_charge");
		opChgAliasMap.put("private consultation discount", "private_cons_discount");
		opChgAliasMap.put("consultation revisit charge", "private_cons_revisit_charge");
		opChgAliasMap.put("consultation revisit discount", "private_revisit_discount");
		opChgAliasMap.put("op odd hrs charge", "op_oddhr_charge");
		opChgAliasMap.put("op odd hrs discount", "op_oddhr_charge_discount");

	}
	
	public void importChargesFromXlsJob(Map<String,Object> map) throws FileNotFoundException, IOException, SQLException {
		String orgId = map.get("orgId").toString();
		String userName = map.get("userName").toString();
		
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream((File)map.get("file")));
		DoctorChargeDAO cdao = new DoctorChargeDAO();
		cdao.backupOPCharges(orgId, userName);

		this.errors = new StringBuilder();

		String[] opHeaders = {"doctor_name", "department", "op_charge", "op_charge_discount", "op_revisit_charge", "op_revisit_discount",
				"private_cons_charge", "private_cons_discount", "private_cons_revisit_charge", "private_revisit_discount",
				"op_oddhr_charge", "op_oddhr_charge_discount"};

		String[] headers = null;

		XSSFSheet opChargeSheet = workBook.getSheetAt(0);
		Iterator opChgStRowIterator = opChargeSheet.rowIterator();


			XSSFRow headerRow = (XSSFRow)opChgStRowIterator.next();
			if (headerRow != null) {

				headers = new String[headerRow.getLastCellNum()];

				for (int i=0; i<headers.length; i++) {

					XSSFCell cell = headerRow.getCell(i);
					if (cell == null)
						headers[i] = null; /*putting null values, if found*/
					else {

						String header = cell.toString().trim().toLowerCase();
						String dbName = (String) (opChgAliasMap.get(header) == null ? header : opChgAliasMap.get(header));
						headers[i] = dbName;

						if (!Arrays.asList(opHeaders).contains(dbName)) {
							addError(0, "Unknown property found in header "+dbName);
							headers[i] = null; /*putting null values, if found unknown properties*/
						}

					}

				}
				if (!Arrays.asList(headers).contains("doctor_name") && !Arrays.asList(headers).contains("department")) {
//					addError(0, "Mandatory field is missing Doctor Name or Department");
					throw new HMSException("Mandatory field is missing Doctor Name or Department");
//					flash.put("error", this.errors);
//					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
//					return redirect;
				}

				BasicDynaBean opChargeBean = doctorOpConsultationChargeDAO.getBean();
				DynaProperty property = null;
				Map<String, BigDecimal> opChargeMap = null;

	nextRow:	while(opChgStRowIterator.hasNext()) {

					String doctorId = null;
					Object doctorName = null;
					Object department = null;
					Object deptId = null;
					opChargeMap = new HashMap<String, BigDecimal>();
					XSSFRow row = (XSSFRow)opChgStRowIterator.next();
					int lineNumber = row.getRowNum()+1;
					if (row != null) {
						for (int i=0; i<headers.length; i++) {

							String cellVal = null;
							if (headers[i] == null)
								continue;    /*next cell*/

								XSSFCell rowcell = row.getCell(i);
								if (rowcell != null && !rowcell.equals(""))
									cellVal = rowcell.toString();
								if (headers[i].equals("doctor_name")) {
									doctorName = cellVal;

								} else if (headers[i].equals("department")) {
									department = cellVal;
								} else {
									property = opChargeBean.getDynaClass().getDynaProperty(headers[i]);

									if (property != null) {
										BigDecimal chargeVal;
										try {
											if (rowcell != null && !rowcell.equals(""))
												chargeVal = new BigDecimal(rowcell.getNumericCellValue());
											else
												chargeVal = new BigDecimal(0);
											opChargeMap.put(headers[i], chargeVal);
										} catch (Exception ex) {
											addError(lineNumber, "Conversion error: "+cellVal +
													" could not be converted to "+property.getType()+" below headers of "+headers[i]);
											continue;	/*next cell*/
										}
									}

								}
						}
						if (department == null) {
							addError(lineNumber, "No master value found for the "+department);
							continue nextRow;
						} else {
                          deptId = departmentDAO.findByKey("dept_name", department).get("dept_id");
							doctorId = DoctorMasterDAO.getDoctorID(doctorName, deptId);
							if (doctorId == null) {
								addError(lineNumber, "No master value found for the "+doctorName);
								continue nextRow;
							}
						}

						Connection con = null;
						Map<String, String> keys = new HashMap<String, String>();
						keys.put("org_id", orgId);
						keys.put("doctor_id", doctorId);
						boolean success = false;

						try {
							con = DataBaseUtil.getReadOnlyConnection();
							con.setAutoCommit(false);
							success = doctorOpConsultationChargeDAO.update(con, opChargeMap, keys) > 0;
							if (success)
								con.commit();
						} finally {
							DataBaseUtil.closeConnections(con, null);

						}

					}
				}

			} else {
				throw new HMSException("Header row should not be empty");
//				addError(0, "Header row should not be empty");
//				flash.put("error", this.errors);
//				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

			}
			cdao.backupCharges(orgId, userName);
			importIpCharges(workBook, orgId);
			
    // Added try catch because the methods will throw generic exception which is not
    // handled in file operation job class
			try {
			cdao.updateChargesForDerivedRatePlans(orgId, userName, "doctorOPcharges",true);
			cdao.updateChargesForDerivedRatePlans(orgId, userName, "doctorIPCharges",true);
			} catch (Exception exception) {
			  throw new HMSException(exception);
			}
	}
	
  private File storeUploadedFile(FormFile formFile) throws IOException {
    FileOutputStream outputStream = null;
    File receivedFile = null;
    try {
      Date date = new Date();
      String currentDate = new SimpleDateFormat("yyyyMMdd").format(date);
      String userName = sessionService.getSessionAttributes().get("userId").toString();
      String schema = sessionService.getSessionAttributes().get("sesHospitalId").toString();
      receivedFile = new File(
          EnvironmentUtil.getTempDirectory() + File.separator + currentDate + File.separator
              + schema + File.separator + userName + File.separator + formFile.getFileName());
      Path path = Paths.get(receivedFile.getParent());
      if (!Files.exists(path)) {
        Files.createDirectories(path);
      }
      outputStream = new FileOutputStream(receivedFile);
      outputStream.write(formFile.getFileData());
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
    return receivedFile;
  }

  public ActionForward importChargesFromXls(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    DoctorUploadForm uploadForm = (DoctorUploadForm) form;
    FormFile formFile = uploadForm.getXlsFile();
    
    String orgId = request.getParameter("org_id");
    String userName = (String) request.getSession().getAttribute("userid");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("orgId", orgId);
    map.put("userName", userName);
    map.put("file", storeUploadedFile(formFile));
    map.put("action", "upload");
    map.put("master", OperationScreenType.DoctorCharges);
    fos.bulkDataOperation(map);

    FlashScope flash = FlashScope.getScope(request);
    String referer = request.getHeader("Referer");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    ActionRedirect redirect = new ActionRedirect(referer);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.put("info",
        "File successfully uploaded and moved to background job for processing (SETTINGS --> ADMINISTRATION --> Data Upload Download)");

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

	private static Map<String, String> ipAliasMap = new HashMap<String, String>();

	static {
		ipAliasMap.put("name", "doctor_name");
	}


	private void importIpCharges(XSSFWorkbook workBook, String orgId)throws IOException, SQLException  {

		Map<String, Map<String, Map<String, BigDecimal>>> ipChargesMap = new HashMap<String, Map<String, Map<String, BigDecimal>>>();

		String[] ipChargeSheets =  {"DOCTOR_IP_CHARGE","DOCTOR_IP_CHARGE_DISCOUNT", "NIGHT_IP_CHARGE",
				"NIGHT_IP_CHARGE_DISCOUNT","WARD_IP_CHARGE","WARD_IP_CHARGE_DISCOUNT",
				"OT_CHARGE" ,"OT_CHARGE_DISCOUNT", "CO_SURGEON_CHARGE","CO_SURGEON_CHARGE_DISCOUNT",
				"ASSNT_SURGEON_CHARGE","ASSNT_SURGEON_CHARGE_DISCOUNT"};

		int noOfSheets = workBook.getNumberOfSheets();
		ChargesImportExporter importExporter = new ChargesImportExporter();
		List<String> bedTypes = importExporter.getUnionOfBedTypes();
		Map<String, String> dbBedNamesMap = importExporter.getMapofOriginalBedtypes();
		List<String> headerList = new ArrayList<String>();
		headerList.addAll(bedTypes);
		headerList.add("doctor name");
		headerList.add("department");
		String actualBedName = null;

		for (int i=1; i<noOfSheets; i++) {
			XSSFSheet chargeSheet = workBook.getSheetAt(i);
			String sheetName = chargeSheet.getSheetName();
			Iterator rowIterator = chargeSheet.rowIterator();
			XSSFRow headerRow = (XSSFRow)rowIterator.next();
			String[] headers = new String[headerRow.getLastCellNum()];

			for (int c=0; c<headers.length; c++) {

				XSSFCell cell = headerRow.getCell(c);
				if (cell == null)
					headers[c] = null; /*putting null values, if found*/
				else {

					String header = cell.toString().toLowerCase();
					String dbName = (String) (ipAliasMap.get(header) == null ? header : ipAliasMap.get(header));
					headers[c] = dbName;

					if (!headerList.contains(dbName)) {
						addError(0, "Unknown property found in header "+dbName + " in sheet " +sheetName);
						headers[c] = null; /*putting null values, if found unknown properties*/
					}

				}

			}

			if (!Arrays.asList(headers).contains("doctor name") && !Arrays.asList(headers).contains("department")) {
				addError(0, "Mandatory field missing in the headers in sheet" + sheetName);

			}
			GenericDAO ipChargeDAO = new GenericDAO("doctor_consultation_charge");
			BasicDynaBean ipChargeBean = ipChargeDAO.getBean();
			DynaProperty property = null;
			property = ipChargeBean.getDynaClass().getDynaProperty(sheetName.toLowerCase());

nextRow:	while (rowIterator.hasNext()) {

				String doctorId = null;
				Object deptId = null;
				//opChargeMap = new HashMap<String, BigDecimal>();
				XSSFRow row = (XSSFRow)rowIterator.next();
				int lineNumber = row.getRowNum()+1;
				if (row != null) {

					for (int j=0; j<headers.length; j++) {

						String cellVal = null;
						if (headers[j] == null)
							continue;    /*next cell*/

							XSSFCell rowcell = row.getCell(j);
							if (rowcell != null && !rowcell.equals(""))
								cellVal = rowcell.toString();
							if (bedTypes.contains(headers[j])) {
								BigDecimal chargeVal;
								actualBedName = dbBedNamesMap.get(headers[j]);

									try {
										Map<String, Map<String, BigDecimal>> bedMap = ipChargesMap.get(doctorId);
										Map<String, BigDecimal> chargeMap = (Map)bedMap.get(actualBedName);
										if (chargeMap == null) {
											chargeMap = new HashMap<String, BigDecimal>();
											if (rowcell != null && !rowcell.equals(""))
												chargeVal = new BigDecimal(rowcell.getNumericCellValue());
											else
												chargeVal = new BigDecimal(0);
											chargeMap.put(sheetName.toLowerCase(), chargeVal);
											bedMap.put(actualBedName, chargeMap);
										} else {
											if (rowcell != null && !rowcell.equals(""))
												chargeVal = new BigDecimal(rowcell.getNumericCellValue());
											else
												chargeVal = new BigDecimal(0);
											chargeMap.put(sheetName.toLowerCase(), chargeVal);
										}
										//opChargeMap.put(headers[j], (BigDecimal)ConvertUtils.convert(cellVal, property.getType()));
									} catch (Exception ex) {
										addError(lineNumber, "Conversion error: "+cellVal +
												" could not be converted to "+property.getType()+" below headers of "+headers[j]+" in sheet "+sheetName);
										continue;	/*next cell*/
									}

							} else if (headers[j].equals("doctor name")) {

								if (row.getCell(1) == null) {
									addError(lineNumber, "No master value found for the "+cellVal+" below the header "+ headers[j]+" in sheet "+sheetName);
									continue nextRow;
								}
                                deptId =
                                    departmentDAO.findByKey("dept_name", row.getCell(1).toString())
                                        .get("dept_id");
								doctorId = DoctorMasterDAO.getDoctorID(cellVal, deptId);
								if (doctorId == null) {
									addError(lineNumber, "No master value found for the "+cellVal+" below the header "+ headers[j]+" in sheet "+sheetName);
									continue nextRow;
								} else {
									Map map = ipChargesMap.get(doctorId);
									if (map == null) {
										Map<String, Map<String, BigDecimal>> bedMap = new HashMap<String, Map<String, BigDecimal>>();
										ipChargesMap.put(doctorId, bedMap);
									}
								}
							}
					}

				}

			}
		}

		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			GenericDAO ipChgDAO = new GenericDAO("doctor_consultation_charge");
			Iterator ipChgItr = ipChargesMap.entrySet().iterator();
			while (ipChgItr.hasNext()) {
				Map<String, String> keys = new HashMap<String, String>();
				keys.put("organization", orgId);
				Map.Entry ipChgMap = (Map.Entry)ipChgItr.next();
				 String doctorId = (String)ipChgMap.getKey();
				 keys.put("doctor_name", doctorId);
				 Map bedMap = (Map)ipChgMap.getValue();
				 Iterator bedMapItr = bedMap.entrySet().iterator();
				while (bedMapItr.hasNext()) {
					boolean success = false;
					Map.Entry bedEntryMap = (Map.Entry)bedMapItr.next();
					String bed = (String)bedEntryMap.getKey();
					keys.put("bed_type", bed);
					success = ipChgDAO.update(con, (Map)bedEntryMap.getValue(), keys) > 0;
					if (success)
						con.commit();
				}
			}

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

	}
	
	public File exportChargesToXlsJob(Map<String,Object> map) throws SQLException {
		
		String orgId = map.get("orgId").toString();
		BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
		String orgName = (String) orgBean.get("org_name");
		File file = new File("DoctorRates_"+orgName+".xls");
		
		String[] chargeTypesIP =  {"DOCTOR_IP_CHARGE","DOCTOR_IP_CHARGE_DISCOUNT", "NIGHT_IP_CHARGE",
				"NIGHT_IP_CHARGE_DISCOUNT","WARD_IP_CHARGE","WARD_IP_CHARGE_DISCOUNT",
				"OT_CHARGE" ,"OT_CHARGE_DISCOUNT", "CO_SURGEON_CHARGE","CO_SURGEON_CHARGE_DISCOUNT",
				"ASSNT_SURGEON_CHARGE","ASSNT_SURGEON_CHARGE_DISCOUNT"};

		String[] chargeTypesOPColumns = {"Doctor Name", "Department", "Op Charge", "Op Charge Discount",
					"Op Revisit Charge", "Op Revisit Discount", "Private Consultation Charge",
					"Private Consultation Discount", "Consultation Revisit Charge",
					"Consultation Revisit Discount", "Op Odd Hrs Charge", "Op Odd Hrs Discount"};

		String[] chargeTypesOP = {"doctor_name", "dept_name", "op_charge","op_charge_discount",
				"op_revisit_charge","op_revisit_discount", "private_cons_charge","private_cons_discount",
				"private_cons_revisit_charge","private_revisit_discount",
				"op_oddhr_charge","op_oddhr_charge_discount"};

		List<String> allBedTypes = new BedMasterDAO().getUnionOfBedTypes();
		DoctorChargeDAO dao = new DoctorChargeDAO();

		try {
			XSSFWorkbook workbook = new XSSFWorkbook();

			//OP Charges filling in first sheet
			XSSFSheet worksheetOP = workbook.createSheet("OP_CHARGES");
			List<BasicDynaBean> opCharges = dao.getOPChargesXLS(orgId);

			XSSFRow row=worksheetOP.createRow(0);

			int k=0;
			for (String key: chargeTypesOPColumns) {
				row.createCell((k)).setCellValue(new XSSFRichTextString(key));
				k++;
			}

			for(int j=0;j<opCharges.size();j++){

				row=worksheetOP.createRow(j+1);
				BasicDynaBean bean = opCharges.get(j);
				int n=0;

				for (String key : chargeTypesOP) {
					if (n<1)
						row.createCell(n).setCellValue(bean.get(key).toString());
					else if(n<2)
						row.createCell(n).setCellValue(bean.get(key).toString());
					else
						row.createCell(n).setCellValue(((BigDecimal)bean.get(key)).doubleValue());
					n++;
			    }
			}

			String IPKeyArray[] = new String[allBedTypes.size()+2];
			IPKeyArray[0] ="Doctor Name";
			IPKeyArray[1] = "Department";

			//IP  Charges Filling in different sheets
			for (int c = 0; c < chargeTypesIP.length; c++) {

				XSSFSheet worksheetIP = workbook.createSheet(chargeTypesIP[c]);
				List<BasicDynaBean> ipcharges  = dao.getIPChargesForBedTypesXLS(orgId, allBedTypes, chargeTypesIP[c]);

				XSSFRow rowIP = worksheetIP.createRow(0);

				rowIP.createCell(0).setCellValue(new XSSFRichTextString(IPKeyArray[0]));
				rowIP.createCell(1).setCellValue(new XSSFRichTextString(IPKeyArray[1]));

				int b=1;
				for ( String key : allBedTypes ){
					rowIP.createCell((b+1)).setCellValue(new XSSFRichTextString(key));
					IPKeyArray[b+1] = key;
					b++;
				}

				for (int i=0;i< ipcharges.size(); i++)  {

					rowIP= worksheetIP.createRow(i+1);

					BasicDynaBean bean = ipcharges.get(i);

					int l=0;

					for ( String ipKey : IPKeyArray )
					{
						if(l<1)
							rowIP.createCell(l).setCellValue(new XSSFRichTextString
								(bean.get("doctor_name").toString()));
						else if(l<2)
							rowIP.createCell(l).setCellValue(new XSSFRichTextString
								(bean.get("dept_name").toString()));
						else
							rowIP.createCell(l).setCellValue(Double.parseDouble
								(bean.get(ipKey.toLowerCase()).toString()));

						l++;
					}
				}
			}

//		java.io.OutputStream os = res.getOutputStream();
//		workbook.write(os);
//
//		os.flush();
//		os.close();
		
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				workbook.write(bos);
			} finally {
				bos.close();
			}
			byte[] bytes = bos.toByteArray();
			InputStream inputStream = new ByteArrayInputStream(bytes);
			FileUtils.copyInputStreamToFile(inputStream, file);
			inputStream.close();
		
//		return file;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	public ActionForward exportChargesToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {

		Map<String,Object> map = new HashMap<String,Object>();
		String orgId = req.getParameter("org_id");
//		BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
//		String orgName = (String) orgBean.get("org_name");
		map.put("action", "download");
		map.put("master", OperationScreenType.DoctorCharges);
		map.put("orgId", orgId);

//		res.setHeader("Content-type","application/vnd.ms-excel");
//		res.setHeader("Content-disposition","attachment; filename=DoctorRates_" + orgName + ".xls");
//		res.setHeader("Readonly","true");

		fos.bulkDataOperation(map);
		
		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(req);

		flash.put("info", "File download is moved to background job");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
	
	public File exportDoctorDetailsToXlsJob() throws SQLException, IOException {
		File file = new File("DoctorDefinationDetails.xls");
		// commented out because of Bug#46143
		 /*	String centerName = "";
			if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
				centerName = "Center Name";
			}*/
		List<String> doctorTableColumns=Arrays.asList(new String[]
			      {"doctor_id","Doctor Name","Specialization","Doctor Address","Doctor Type","Doctor Mobile",
				   "Doctor Mail Id","Op Consultation Validity","Status","Dept Name","Prescribe By Favourites","Ot Doctor Flag",
				   "Qualification","Registration No",
				   "Residence Phone","Clinic Phone","Payment Eligible","Doctor License Number",
				   "Allowed Revisit Count","Custom Field1","Custom Field2",
				   "Custom Field3","Custom Field4","Custom Field5","Practitioner Name", "Send Feedback SMS" , "Scheduleable By"});

		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet docWorkSheet = workbook.createSheet("DOCTORS");
		List<BasicDynaBean> doctorList=new DoctorMasterDAO().getDoctorDetails();
		Map<String, List> columnNamesMap = new HashMap<String, List>();
		columnNamesMap.put("mainItems", doctorTableColumns);
		HsSfWorkbookUtils.createPhysicalCellsWithValues(doctorList,columnNamesMap, docWorkSheet, true);
		
//		res.setHeader("Content-type", "application/vnd.ms-excel");
//		res.setHeader("Content-disposition","attachment; filename=DoctorDefinationDetails.xls");
//		res.setHeader("Readonly", "true");
//		java.io.OutputStream os = res.getOutputStream();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
		    workbook.write(bos);
		} finally {
		    bos.close();
		}
		byte[] bytes = bos.toByteArray();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		FileUtils.copyInputStreamToFile(inputStream, file);
		inputStream.close();
		
		return file;
	}

	public ActionForward exportDoctorDetailsToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, java.io.IOException {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("action", "download");
		map.put("master", OperationScreenType.DoctorDefinitionDetails);
		fos.bulkDataOperation(map);
		
		String referer = req.getHeader("Referer");
		referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
		ActionRedirect redirect = new ActionRedirect(referer);
		FlashScope flash = FlashScope.getScope(req);

		flash.put("info", "File download is moved to background job");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public static DetailsImportExporter detailsImporExp;

	static {
		detailsImporExp = new DetailsImportExporter("doctors", "doctor_org_details", "doctor_consultation_charge");

	}

	public void importDoctorDetailsFromXlsJob(File file) throws FileNotFoundException, IOException, SQLException {
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(file));
		XSSFSheet sheet = workBook.getSheetAt(0);

		this.errors = new StringBuilder();
		Map deptMap = DepartmentMasterDAO.getAlldepartmentsMap();
		List<String> columns = new ArrayList<String>();
		columns.add("practitioner_id");
		columns.add("practitioner_name");
		List<BasicDynaBean> practitionerbeans = practitionerTypeDAO.listAll(columns, "status", "A");
		Map practitionerMap = new HashMap();
		for (BasicDynaBean bean : practitionerbeans) {
			practitionerMap.put(bean.get("practitioner_name"), bean.get("practitioner_id"));
		}
		Map centerMap = CenterMasterDAO.getAllCentersMap();

		// Connection con = null;
		// boolean success = false;
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("doctor name", "doctor_name");
		aliasMap.put("doctor address", "doctor_address");
		aliasMap.put("doctor type", "doctor_type");
		aliasMap.put("doctor mobile", "doctor_mobile");
		aliasMap.put("doctor mail id", "doctor_mail_id");
		aliasMap.put("status", "status");
		aliasMap.put("op consultation validity", "op_consultation_validity");
		aliasMap.put("specialization", "specialization");
		aliasMap.put("dept name", "dept_id");
		aliasMap.put("practitioner name", "practitioner_id");
		aliasMap.put("prescribe by favourites", "prescribe_by_favourites");
		aliasMap.put("ot doctor flag", "ot_doctor_flag");
		aliasMap.put("consulting doctor flag", "consulting_doctor_flag");
		aliasMap.put("qualification", "qualification");
		aliasMap.put("registration no", "registration_no");
		aliasMap.put("residence phone", "res_phone");
		aliasMap.put("clinic phone", "clinic_phone");
		aliasMap.put("payment eligible", "payment_eligible");
		aliasMap.put("doctor license number", "doctor_license_number");
		aliasMap.put("allowed revisit count", "allowed_revisit_count");
		aliasMap.put("custom field1", "custom_field1_value");
		aliasMap.put("custom field2", "custom_field2_value");
		aliasMap.put("custom field3", "custom_field3_value");
		aliasMap.put("custom field4", "custom_field4_value");
		aliasMap.put("custom field5", "custom_field5_value");
		aliasMap.put("center name", "center_id");
		aliasMap.put("send feedback sms", "send_feedback_sms");
		aliasMap.put("scheduleable by", "scheduleable_by");

		List<String> charges = Arrays.asList("doctor_ip_charge", "night_ip_charge", "ot_charge", "co_surgeon_charge",
				"assnt_surgeon_charge", "ward_ip_charge");
		List<String> mandatoryList = Arrays.asList("doctor_name", "status", "dept_id", "doctor_type");
		List<String> exemptFromNullCheck = Arrays.asList("doctor_id", "doctor_mail_id", "specialization");
		List<String> oddFields = Arrays.asList("doctor_mobile", "registration_no", "res_phone", "clinic_phone",
				"doctor_license_number", "scheduleable_by");

		detailsImporExp.setTableDbName("doctor_name");
		detailsImporExp.setAliasUnmsToDBnmsMap(aliasMap);
		detailsImporExp.setBed("bed_type");
		detailsImporExp.setCharges(charges);
		detailsImporExp.setId("doctor_id");
		detailsImporExp.setOddFields(oddFields);
		detailsImporExp.setMandatoryFields(mandatoryList);
		detailsImporExp.setOrgId("org_id");
		detailsImporExp.setType("Doctor");
		detailsImporExp.setDeptName("dept_id");
		detailsImporExp.setPractitionerName("practitioner_id");
		detailsImporExp.setDeptMap(deptMap);
		detailsImporExp.setPractitionerMap(practitionerMap);
		detailsImporExp.setIdForChgTab("doctor_name");
		detailsImporExp.setCenterName("center_id");
		detailsImporExp.setOrgNameForChgTab("organization");
		detailsImporExp.setCodeAliasRequired(false);
		detailsImporExp.setDeptNotExist(false);
		detailsImporExp.setUsingHospIdPatterns(false);
		detailsImporExp.setUsingSequencePattern(false);
		detailsImporExp.setUsingUniqueNumber(true);
		detailsImporExp.setIsDateRequired(false);
		detailsImporExp.setIsUserNameRequired(false);
		detailsImporExp.setExemptFromNullCheck(exemptFromNullCheck);
		detailsImporExp.setCenterMap(centerMap);
		detailsImporExp.setIdForOrgTab("doctor_id");

//		detailsImporExp.importDetailsToXls(sheet, null, errors, (String) req.getSession(false).getAttribute("userid"));
		detailsImporExp.importDetailsToXls(sheet, null, errors, null);
	}

  public ActionForward importDoctorDetailsFromXls(ActionMapping am, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws IOException {

    DoctorUploadForm uploadForm = (DoctorUploadForm) form;
    FormFile formFile = uploadForm.getXlsDoctorFile();
    
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("action", "upload");
    map.put("master", OperationScreenType.DoctorDefinitionDetails);
    map.put("file", storeUploadedFile(formFile));
    fos.bulkDataOperation(map);

    String referer = req.getHeader("Referer");
    referer = referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", "");
    ActionRedirect redirect = new ActionRedirect(referer);
    FlashScope flash = FlashScope.getScope(req);

    flash.put("info",
        "File successfully uploaded and moved to background job for processing (SETTINGS --> ADMINISTRATION --> Data Upload Download)");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

	private StringBuilder errors;
	private void addError(int line, String msg) {
		if (line > 0) {
			this.errors.append("Line ").append(line).append(": ");
		} else {
			this.errors.append("Error in header: ");
		}
		this.errors.append(msg).append("<br>");
		logger.error("Line " + line + ": " + msg);
	}

	public ActionForward checkUniqueLicenseNo(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ParseException {

	    String licenseNo = req.getParameter("licenseNo");
	    String responseText = "false";

	    boolean exists = DoctorMasterDAO.getDoctorLicenseNo(licenseNo);
	    if (exists)
	    	responseText = "true";

	    JSONSerializer js = new JSONSerializer();
	    res.setContentType("text/plain");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(js.serialize(responseText));
	    res.flushBuffer();

	    return null;
	}

	public ActionForward viewPhoto(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String doctorId = request.getParameter("doctor_id");
		BasicDynaBean bean = dImageDao.getBean();
		dImageDao.loadByteaRecords(bean, "doctor_id", doctorId);

		OutputStream stream = response.getOutputStream();
		if ((InputStream) bean.get("photo") != null) {
			response.setContentType((String) bean.get("content_type"));
			stream.write(DataBaseUtil.readInputStream((InputStream) bean.get("photo")));
		}

		stream.flush();
		stream.close();
		return null;
	}

	public ActionForward viewSignature(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String doctorId = request.getParameter("doctor_id");
		UserSignatureDAO sigDAO = new UserSignatureDAO();
		BasicDynaBean sigBean = sigDAO.getBean();
		sigDAO.loadByteaRecords(sigBean, "doctor_id", doctorId);

		OutputStream stream = response.getOutputStream();
		response.setContentType((String) sigBean.get("signature_content_type"));
		stream.write(DataBaseUtil.readInputStream((InputStream) sigBean.get("signature")));
		stream.flush();
		stream.close();

		return null;
	}
	
	@IgnoreConfidentialFilters
    public ActionForward getDoctorExistingDetails(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) throws IOException {
      HashMap<String, String> responseMap = new HashMap<>();
      String resSchId = request.getParameter("doctor_id");
      Timestamp currentDateTime = new Timestamp(System.currentTimeMillis());
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      response.setContentType("text/plain");
      boolean existAppointments  =  appointmentService.onlineAppointmentsExists(resSchId,currentDateTime,ONLINE_VISIT_MODE);
      if (existAppointments) {
          responseMap.put("result", "true");
      } else {
          boolean existsOverridesAndDefaultAvailabilities = resourceAvailabilityService.resourceAvailibilitesExists(resSchId,
                 currentDateTime,ONLINE_VISIT_MODE);
          responseMap.put("result", String.valueOf(existsOverridesAndDefaultAvailabilities));
      }
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    @SuppressWarnings("unchecked")
    public ActionForward getDoctorExistingDetailsAjax(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) throws IOException {
      HttpSession session = (HttpSession) request.getSession(false);
      Map responseMap = new HashMap();
      response.setContentType("text/plain");
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      List<BasicDynaBean> doctorNameList = dao.getExistingDoctorName(request.getParameter("doctor_id"),
          request.getParameter("registration_no"));
      if (!doctorNameList.isEmpty()) {
        String[] docNameArray = new String[doctorNameList.size()];
        int count = 0;
        for (BasicDynaBean bean : doctorNameList) {
          docNameArray[count] = (String) bean.get("doctor_name");
          count++;
        }
        responseMap.put("doctor_name", StringUtils.arrayToCommaDelimitedString(docNameArray));
      } else {
        responseMap.put("doctor_name", null);
      }
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }

    @SuppressWarnings("unchecked")
    @IgnoreConfidentialFilters
    public ActionForward getSortedSalutationByLengthAjax(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) throws IOException {
      HttpSession session = (HttpSession) request.getSession(false);
      Map responseMap = new HashMap();
      response.setContentType("text/plain");
      response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      responseMap.put("salutations", ConversionUtils
          .copyListDynaBeansToMap(salutationRepository.getAllSalutationSortedByLength()));
      response.getWriter().write(js.deepSerialize(responseMap));
      response.flushBuffer();
      return null;
    }
}