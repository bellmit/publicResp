package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.diagnosticmodule.common.TestPrescribed;
import com.insta.hms.diagnosticmodule.radiology.RadiologyBO;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;


public class PendingSamplesAssertion extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(PendingSamplesAssertion.class);
	final GenericDAO sampleDAO = new GenericDAO("sample_collection");
	final GenericDAO testsPrescribeDAo = new GenericDAO("tests_prescribed");
	final GenericDAO sampleRejectionDAo = new GenericDAO("sample_rejections");
	final GenericDAO incSampleDAo = new GenericDAO("incoming_sample_registration");
	final GenericDAO diagnosDAo = new GenericDAO("diagnostics");
	final GenericDAO incSampleRegDetDAo = new GenericDAO("incoming_sample_registration_details");
	private static final GenericDAO sampleCollectionCentersDAO = new GenericDAO("sample_collection_centers");

	@IgnoreConfidentialFilters
	public ActionForward searchScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException {

		Map filterParams = new HashMap(request.getParameterMap());
		JSONSerializer js = new JSONSerializer().exclude("class");
		HttpSession session = request.getSession(false);
		String userId =(String)session.getAttribute("userid");
		String department = request.getParameter("ddept_id");
		String sampleSno = request.getParameter("sampleSno");
		String origsampleSno = request.getParameter("origSampleSno");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}
		String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);

	        filterParams.put("sample_date", new String[]{week_start_date, ""});
	        filterParams.put("sample_date@op", new String[]{"ge,le"});
	        filterParams.put("sample_date@cast", new String[]{"y"});
	        filterParams.remove("date_range");
		}
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		filterParams.put("ddept_id", new String[]{department});
		//filterParams.remove("sampleSno");
		//filterParams.put("sample_sno", new String[]{sampleSno});
		filterParams.remove("collectionCenterId");

		String collectionCenterId = request.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) request.getSession(false).getAttribute("sampleCollectionCenterId");
		if(null != collectionCenterId && !"".equals(collectionCenterId)) {
			filterParams.put("collection_center_id", new String[]{collectionCenterId+""});
			filterParams.put("collection_center_id@type", new String[]{"integer"});
		} else {
			if(userSampleCollectionCenterId != -1){
				filterParams.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				filterParams.put("collection_center_id@type", new String[]{"integer"});
			}
		}

		String outsource_dest_id = (String)request.getParameter("outsource_dest_id");
		if(null != outsource_dest_id && !"".equals(outsource_dest_id)) {
			filterParams.put("outsource_dest_id", new String[]{outsource_dest_id+""});
			filterParams.put("outsource_dest_id@type", new String[]{"integer"});
		} else {
                filterParams.remove("outsource_dest_id");
				//filterParams.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				//filterParams.put("collection_center_id@type", new String[]{"integer"});

		}
		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

		List<BasicDynaBean> outHouselist = OutHouseMasterDAO.getAllOutSourcesList(centerId);
		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		request.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));
		request.setAttribute("outhouses", js.serialize(outHouselist));
		request.setAttribute("userDept", department);
		
		ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
		// when ever user uses a pagination sample_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("sample_date") == null) {
			addParameter("sample_date", week_start_date, forward);
	    }
		return forward;
	}

	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
			ParseException {

		Map filterParams = new HashMap(request.getParameterMap());
		JSONSerializer js = new JSONSerializer().exclude("class");
		HttpSession session = request.getSession(false);
		String userId =(String)session.getAttribute("userid");
		String department = request.getParameter("ddept_id");
	    String sampleSno = request.getParameter("sampleSno");
		String origsampleSno = request.getParameter("origSampleSno");
		String userDept = new DiagnosticDepartmentMasterDAO().getUserDepartment(userId);
		if (department == null ) {
			if (userDept != null && !userDept.equals(""))
				department = userDept;
		}
		String date_range = request.getParameter("date_range");
		String week_start_date = null;
		if (date_range != null && date_range.equals("week")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -7);
	        Date openDt = cal.getTime();
	        week_start_date = dateFormat.format(openDt);

	        filterParams.put("sample_date", new String[]{week_start_date, ""});
	        filterParams.put("sample_date@op", new String[]{"ge,le"});
	        filterParams.put("sample_date@cast", new String[]{"y"});
	        filterParams.remove("date_range");
		}
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		filterParams.put("ddept_id", new String[]{department});
    	//filterParams.remove("sampleSno");
	   // filterParams.put("sample_sno", new String[] {sampleSno});
		String sampleNos = null;
		String[] sampleNosArray = null;
		if(sampleSno != null && !sampleSno.equals("")) {
			sampleNos = sampleSno;
			sampleNosArray = sampleNos.split(",");
		}
		String origsampleSnos = null;
		String[] origsampleSnosArray = null;
		if(origsampleSno != null && !origsampleSno.equals("")) {
			origsampleSnos = origsampleSno;
			origsampleSnosArray = origsampleSnos.split(",");
		}
		filterParams.remove("sampleSno");
		filterParams.remove("origSampleSno");
		filterParams.remove("collectionCenterId");
        String collectionCenterId = request.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) request.getSession(false).getAttribute("sampleCollectionCenterId");
		if(null != collectionCenterId && !"".equals(collectionCenterId)) {
			filterParams.put("collection_center_id", new String[]{collectionCenterId+""});
			filterParams.put("collection_center_id@type", new String[]{"integer"});
		} else {
			if(userSampleCollectionCenterId != -1){
				filterParams.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				filterParams.put("collection_center_id@type", new String[]{"integer"});
			}
		}

		String outsource_dest_id = (String)request.getParameter("outsource_dest_id");
		if( null != outsource_dest_id && !"".equals(outsource_dest_id)) {
			filterParams.put("outsource_dest_id", new String[]{outsource_dest_id+""});
			filterParams.put("outsource_dest_id@type", new String[]{"integer"});
		} else {
                filterParams.remove("outsource_dest_id");
				//filterParams.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				//filterParams.put("collection_center_id@type", new String[]{"integer"});

		}
		String sortorder = request.getParameter("sortOrder");
			if(sortorder == null || "".equals(sortorder)){
				sortorder = "sample_date";
			}
			filterParams.put("sortOrder", new String[]{sortorder});

		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

		PagedList pagedList = new PendingSamplesDAO().getPendingSampleAssertionList(filterParams,
				ConversionUtils.getListingParameter(filterParams), sampleNosArray, origsampleSnosArray);


		List<BasicDynaBean> sampleAssertionList = pagedList.getDtoList();
		List<BasicDynaBean> outHouselist = OutHouseMasterDAO.getAllOutSourcesList(centerId);
		List<BasicDynaBean> incomingHospitals = new InComingHospitalDAO().listAll(
				Arrays.asList(new String[] {"hospital_id","hospital_name"}), "status", "A", "hospital_name");
		request.setAttribute("inHouses", js.serialize(ConversionUtils.copyListDynaBeansToMap(incomingHospitals)));
		request.setAttribute("outhouses", js.serialize(outHouselist));
		request.setAttribute("userDept", department);
		request.setAttribute("pagedList", pagedList);
		request.setAttribute("sampleAssertionListJSON", js.serialize(ConversionUtils.listBeanToListMap(sampleAssertionList)));
		ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
		// when ever user uses a pagination sample_date should not append again.
		if (date_range != null && date_range.equals("week") && request.getParameter("sample_date") == null) {
			addParameter("sample_date", week_start_date, forward);
	    }
		return forward;
	}

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException, IOException,ParseException,FileUploadException{

		Map params = getParameterMap(request);
		List<BasicDynaBean > sampleAssertionsList = new ArrayList<BasicDynaBean>();
		getAssertionSampleBeans( params,sampleAssertionsList );

		//trasaction starts
		Connection con = null;
		String error = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for( BasicDynaBean assertion : sampleAssertionsList ){
				if(!(sampleDAO.update(con, assertion.getMap(),"sample_collection_id",assertion.get("sample_collection_id")) > 0))
					error = "Failed to do assertion";
			}

		}finally{
			DataBaseUtil.commitClose(con, error == null);
		}
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		if (error != null) {
			flash.put("error", error);
		} else {
			flash.put("success", "Assertion is succesfull");
		}
		return redirect;
	}

	public void getAssertionSampleBeans(Map params, List<BasicDynaBean> sampleAssertionsList)
	throws SQLException{

		String[] sample_no = (String[])params.get("sample_collection_id");
		String[] asserted = (String[]) params.get("asserted");
		List errors = new ArrayList();
		if( sample_no != null ){
			for(int i = 0;i<sample_no.length;i++ ){
				if(asserted[i].equals("Y")){
					BasicDynaBean sampleAssertionBean = sampleDAO.getBean();
					ConversionUtils.copyIndexToDynaBean(params, i, sampleAssertionBean, errors);
					sampleAssertionBean.set("assertion_time", DataBaseUtil.getDateandTime());
					sampleAssertionBean.set("sample_status", "A");
					if( errors.size() == 0 )
						sampleAssertionsList.add(sampleAssertionBean);
					else
						break;
				}
			}
		}
	}
	public ActionForward reject(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException, IOException,ParseException,FileUploadException, Exception{

			Map params = getParameterMap(request);
			List<BasicDynaBean > sampleCollectionsList = new ArrayList<BasicDynaBean>();
			List<BasicDynaBean > testsPrescribedList = new ArrayList<BasicDynaBean>();
			List<BasicDynaBean > sRejectionsList = new ArrayList<BasicDynaBean>();
			List<TestPrescriptionCancellationForm> lfList = new ArrayList<TestPrescriptionCancellationForm>();
			String[] sample_no = (String[])params.get("sample_collection_id");
			String[] rejected = (String[]) params.get("rejected");
			String rejected_by = request.getParameter("received_by");
			
			FlashScope flash = FlashScope.getScope(request);
			ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			
			List errors = new ArrayList();
			if( sample_no != null ){
				for(int i = 0;i<sample_no.length;i++ ){
	            	if(rejected[i].equals("Y")){
	   					BasicDynaBean sampleCollectionBean = sampleDAO.getBean();
						ConversionUtils.copyIndexToDynaBean(params, i, sampleCollectionBean, errors);
						sampleCollectionBean.set("rejected_time", DataBaseUtil.getDateandTime());
						sampleCollectionBean.set("sample_status", "R");
						sampleCollectionBean.set("rejected_by", rejected_by);

						List<BasicDynaBean> testPreList = testsPrescribeDAo.findAllByKey("sample_collection_id",Integer.parseInt(sample_no[i]));
						for (BasicDynaBean testPresCribedBean : testPreList) {
							Map map = setRejectionMap(testPresCribedBean, sampleCollectionBean);
							sampleCollectionsList.addAll((List<BasicDynaBean>)map.get("sampleCollectionList"));
							testsPrescribedList.addAll((List<BasicDynaBean>)map.get("testsPrescribedList"));
							sRejectionsList.addAll((List<BasicDynaBean>)map.get("sRejectionList"));
							lfList.addAll((List<TestPrescriptionCancellationForm>)map.get("cancelFormList"));
							
						}
					}
				}
			}

			Connection con = null;
			String error = null;
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				for( BasicDynaBean sampleCol : sampleCollectionsList ){
					if(!(sampleDAO.update(con, sampleCol.getMap(),"sample_collection_id",sampleCol.get("sample_collection_id")) > 0)) {
					     error = "Failed to do rejection";
					     flash.error(error);
					     return redirect;
					}
				}
				for( BasicDynaBean tpl : testsPrescribedList ){
					if(!( testsPrescribeDAo.update(con, tpl.getMap(),"prescribed_id",tpl.get("prescribed_id")) > 0)) {
					     error = "Failed to do rejection";
					     flash.error(error);
					     return redirect;
					}
					PendingSamplesDAO.nullifySourcetestPrescribedID(con, (Integer)tpl.get("prescribed_id"));
				}
				if(sRejectionsList.size()>0){
					if(!( sampleRejectionDAo.insertAll(con, sRejectionsList))) {
					     error = "Failed to do rejection";
					     flash.error(error);
					     return redirect;
					}
				}
				
				for(TestPrescriptionCancellationForm lf : lfList) {
					boolean success = cancelIncTestwithRefund(con, lf, request);
					if (!success) {
						error = "Failed to cancel the tests";
						break;
					}
					if(null != lf.getCollectionTestPrescribedID() && !lf.getCollectionTestPrescribedID().equals("") && success) {
						success = rejectParentSample(con, lf);
						if (!success) {
							error = "Failed to do rejection";
							break;
						}
					}
				}
			} finally {
				DataBaseUtil.commitClose(con, error == null);
			}
			
			if (error != null) {
				flash.put("error", error);
			} else {
				flash.put("success", "Rejection is succesfull");
			}
			
			return redirect;
			
	}


	public boolean cancelIncTestwithRefund(Connection con, ActionForm form,HttpServletRequest request)
	throws Exception{
			TestPrescriptionCancellationForm lf = (TestPrescriptionCancellationForm)form;
			Object roleID=null;
			ArrayList<TestPrescribed> dtolistWithBillRefund = new ArrayList<TestPrescribed>();
			ArrayList<TestPrescribed> dtolostWithOutBillRefund = new ArrayList<TestPrescribed>();
			ArrayList<TestPrescribed> dtolist = new ArrayList<TestPrescribed>();
			HashMap actionRightsMap = new HashMap();
			List<Object> cancelFailedIDS = new ArrayList<Object>();

			actionRightsMap= (HashMap) request.getSession(false).getAttribute("actionRightsMap");
			String allowCancelAfterSampleCollection=(String) actionRightsMap.get("allow_cancel_test");
			String allowCancellationAtAnyTime=(String) actionRightsMap.get("cancel_test_any_time");
			roleID=  request.getSession(false).getAttribute("roleId");
		    String mrno = lf.getPresMrno();
		    String canceledBy = lf.getCancelledBy();
		    String presId[] = lf.getPresId();
		    String testName[] = lf.getTestName();
		    String category = lf.getCategory();
		    String[] incomingVisitIDs = lf.getIncomingVisitIds();

			String cancelType[]  = lf.getCancelType();
			for(int i=0;i<cancelType.length;i++){
			    TestPrescribed tp = new TestPrescribed();
			    tp.setCancelledBy(canceledBy);
			    tp.setPrescribedId(Integer.parseInt(presId[i]));
			    tp.setRemarks(lf.getRemarks()[i]);
			    tp.setMrNo(mrno);
			    tp.setVisitId(incomingVisitIDs[i]);
			    tp.setTestName(testName[i]);
			    tp.setSampleCollectedFlag(lf.getSflag()[i]);

			    if(cancelType[i].equals("Y")){
			    	dtolistWithBillRefund.add(tp);
			    }else if(cancelType[i].equals("N")){
			    	dtolostWithOutBillRefund.add(tp);
			    }else{
			    	dtolist.add(tp);
			    }
			}
			/* Check weather roleid is  equal to 1 or 2 then pass it */
			if(roleID.equals(1)|| roleID.equals(2))
				allowCancelAfterSampleCollection="A";

			RadiologyBO bo = new RadiologyBO();
			Map resultMap;
				resultMap = bo.cancellPrescriptionDetails(con,
						dtolistWithBillRefund,dtolostWithOutBillRefund,
						category,allowCancelAfterSampleCollection, allowCancellationAtAnyTime, cancelFailedIDS, false);

			return (Boolean)resultMap.get("success");
		}



	private boolean rejectParentSample(Connection con, TestPrescriptionCancellationForm lf) throws SQLException, IOException {
		String cancelledBy = lf.getCancelledBy();
		String[] remarks = lf.getRemarks();
		boolean success = true;
		String collectionPrescribedID = lf.getCollectionTestPrescribedID();
		if (collectionPrescribedID != null && !collectionPrescribedID.equals("")) {
			BasicDynaBean tpBean = testsPrescribeDAo.findByKey("prescribed_id",Integer.parseInt(collectionPrescribedID));
			if(tpBean != null){
				BasicDynaBean scBean = sampleDAO.findByKey("sample_collection_id", tpBean.get("sample_collection_id"));
				BasicDynaBean incSRDetailBean = incSampleRegDetDAo.findByKey("source_test_prescribed", tpBean.get("prescribed_id"));
				tpBean.set("sflag", "0");
				tpBean.set("sample_no", null);
				tpBean.set("sample_collection_id", null);
				tpBean.set("outsource_dest_id", null);
				tpBean.set("outsource_dest_prescribed_id", null);
				tpBean.set("curr_location_presc_id", (Integer)tpBean.get("prescribed_id"));
				scBean.set("rejected_time", DataBaseUtil.getDateandTime());
				scBean.set("sample_status", "R");
				scBean.set("rejected_by", cancelledBy);
				scBean.set("rejection_remarks", remarks[0]);
				incSRDetailBean.set("source_test_prescribed", null);

				//No need to insert sample rejection entry it is already insrted
				//TODO need code cleanup for this rejection.
				
				success &= sampleDAO.update(con, scBean.getMap(),"sample_collection_id",scBean.get("sample_collection_id")) > 0;

				success &= testsPrescribeDAo.update(con, tpBean.getMap(),"prescribed_id",tpBean.get("prescribed_id")) > 0;
				
				//No need to check for the status here whether true or false, because in re-conduction conduction center will get two records in tests prescribed
				//table with same source center id so, when we iterate it, in the first iteration only it will set NULL to all records,In the 
				//second iteration we wont get any records since we nullified already.
				PendingSamplesDAO.nullifySourcetestPrescribedID(con, (Integer)tpBean.get("prescribed_id"));

			}
		  }
		return success;
	  }
	
	private Map setRejectionMap(BasicDynaBean prescribedBean, BasicDynaBean baseSampleCollectionBean)throws SQLException {
		
		boolean continueLoop = false;
		List<String> prescId = new ArrayList<String>();
		List<String> sflag = new ArrayList<String>();
		List<String> cancelType = new ArrayList<String>();
		List<String> testName = new ArrayList<String>();
		List<String> remarks = new ArrayList<String>();
		List<String> incomingVisitIDs = new ArrayList<String>();
		
		String cancelvisitId = null;
		String category = null;
		BasicDynaBean sampleCollectionBean = null;
		
		List<BasicDynaBean> sampleCollectionsList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> testsPrescribedList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> sRejectionsList = new ArrayList<BasicDynaBean>();
		List<TestPrescriptionCancellationForm> cancelFormsList = new ArrayList<TestPrescriptionCancellationForm>();
		Map<String, Object> map = new HashMap<String, Object>();
		String collectionPrescribedID = null;
		
		do {			
			BasicDynaBean sRejectionBean = sampleRejectionDAo.getBean();
			sRejectionBean.set("test_prescribed_id", prescribedBean.get("prescribed_id"));
			sRejectionBean.set("sample_collection_id", (Integer)prescribedBean.get("sample_collection_id"));
	
			BasicDynaBean incSampleBean = incSampleDAo.findByKey("incoming_visit_id",prescribedBean.get("pat_id"));
			if(incSampleBean != null){
				cancelvisitId = (String)incSampleBean.get("incoming_visit_id");
				incomingVisitIDs.add((String)incSampleBean.get("incoming_visit_id"));
				category = (String)incSampleBean.get("category");
				
				prescId.add(String.valueOf(prescribedBean.get("prescribed_id")));
				sflag.add((String)prescribedBean.get("sflag"));
				if(incSampleBean.get("incoming_source_type").equals("C")) {
					if(prescribedBean.get("coll_prescribed_id") != null) {
						collectionPrescribedID = (String.valueOf(prescribedBean.get("coll_prescribed_id")));
					}
				} else if (null != prescribedBean && null != prescribedBean.get("outsource_dest_prescribed_id")) {
					collectionPrescribedID = null;
				}
				
				BasicDynaBean diagnosticsBean = diagnosDAo.findByKey("test_id",prescribedBean.get("test_id"));
				testName.add((String)(diagnosticsBean.get("test_name")));
				remarks.add((String)baseSampleCollectionBean.get("rejection_remarks"));
				cancelType.add("Y");
	
			}
			sampleCollectionBean = sampleDAO.findByKey("sample_collection_id", prescribedBean.get("sample_collection_id"));			
			sampleCollectionBean.set("rejected_time", DataBaseUtil.getDateandTime());
			sampleCollectionBean.set("sample_status", "R");
			sampleCollectionBean.set("rejected_by", baseSampleCollectionBean.get("rejected_by"));
			sampleCollectionBean.set("rejection_remarks", (String)baseSampleCollectionBean.get("rejection_remarks"));
			
			prescribedBean.set("sflag", "0");
			prescribedBean.set("sample_no", null);
			prescribedBean.set("sample_collection_id", null);
			
			sampleCollectionsList.add(sampleCollectionBean);
			testsPrescribedList.add(prescribedBean);
			sRejectionsList.add(sRejectionBean);
			
			continueLoop = (prescribedBean.get("source_test_prescribed_id") != null 
					&& ((!prescribedBean.get("source_test_prescribed_id").equals(prescribedBean.get("coll_prescribed_id"))) || (incSampleBean != null)));
			prescribedBean = testsPrescribeDAo.findByKey("prescribed_id", prescribedBean.get("source_test_prescribed_id"));
		} while(continueLoop);

		if(cancelvisitId != null){
			TestPrescriptionCancellationForm prescCancelForm = new TestPrescriptionCancellationForm();
			
			prescCancelForm.setPresId(prescId.toArray(new String[prescId.size()]));
			prescCancelForm.setCancelledBy((String)baseSampleCollectionBean.get("rejected_by"));
			prescCancelForm.setCategory(category);
			prescCancelForm.setPatVisitId(cancelvisitId);
			prescCancelForm.setIncomingVisitIds(incomingVisitIDs.toArray(new String[incomingVisitIDs.size()]));
			prescCancelForm.setPresMrno("");
			prescCancelForm.setRemarks(remarks.toArray(new String[remarks.size()]));
			prescCancelForm.setCollectionTestPrescribedID(collectionPrescribedID);
			prescCancelForm.setSflag(sflag.toArray(new String[sflag.size()]));
			prescCancelForm.setTestName(testName.toArray(new String[testName.size()]));
			prescCancelForm.setToDate(DataBaseUtil.getCurrentDate());
			prescCancelForm.setCancelType(cancelType.toArray(new String[cancelType.size()]));
			cancelFormsList.add(prescCancelForm);
		}
		
		map.put("sampleCollectionList", sampleCollectionsList);
		map.put("testsPrescribedList", testsPrescribedList);
		map.put("sRejectionList", sRejectionsList);
		map.put("cancelFormList", cancelFormsList);
		
		return map;
	}
	

}
