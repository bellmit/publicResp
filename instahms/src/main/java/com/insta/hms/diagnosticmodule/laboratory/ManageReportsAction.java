/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.TestPrescribed;
import com.insta.hms.diagnosticmodule.common.TestVisitReports;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryBO.Report;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.messaging.MessageManager;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class ManageReportsAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(ManageReportsAction.class);
	static final PrintTemplatesDAO printTemplateDAO = new PrintTemplatesDAO();
	private static final GenericDAO testVisitReportDAO = new GenericDAO("test_visit_reports");

	public ActionForward getLabReport(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception {

		String visitId = request.getParameter("visitid");
		// todo: get category from mapping
		String category = request.getParameter("category");
		setManageAttributes(request,visitId,category);

		return mapping.findForward("getManageScreen");
	}

	private void setManageAttributes(HttpServletRequest request,
			String visitId,String category)throws Exception{

		List<Hashtable<String, String>> testLists = null;
		List<Hashtable<String, String>> internalLabTestList = Collections.EMPTY_LIST;
		BasicDynaBean printPrefs=null;

		if ((visitId != null) && !visitId.equals("")) {
			testLists = new LaboratoryDAO().getTestsLists(visitId,category);
		}

		int centers = (Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"));

		if (category.equals("DEP_LAB") && centers > 1 && testLists != null)
			internalLabTestList = excludeReportsForInternalLab(testLists);


		ArrayList<Report> repotList = LaboratoryBO.gethierarchicalStructer(testLists);
		request.setAttribute("repotList", repotList);
		JSONSerializer js = new JSONSerializer();
		request.setAttribute("JSONReportPrescriptionList", js.deepSerialize(testLists));
		List collectedSampleList=LaboratoryDAO.getCollectedSampleList(request.getParameter("visitid"));
		List<BasicDynaBean> outSourceSampleList=LaboratoryDAO.getOutHouseSourceList(request.getParameter("visitid"));
		if (category.equals("DEP_LAB") && centers > 1)
			excludeInternalLabtestsForSampleList(collectedSampleList, internalLabTestList, outSourceSampleList);

		request.setAttribute("collectedSampleList", collectedSampleList);
		request.setAttribute("outSourceSampleList", outSourceSampleList);
		ArrayList al =   DiagnosticsDAO.getReportList(visitId,category, false);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("reposts", al);
		request.setAttribute("JSONReportList", js.serialize(al));
		request.setAttribute("totalReports", al.size());
		request.setAttribute("islabNoReq", GenericPreferencesDAO.getdiagGenericPref().get("autogenerate_labno"));


		BasicDynaBean Patientdetails = VisitDetailsDAO.getPatientVisitDetailsBean(request.getParameter("visitid"));
		if (Patientdetails != null) {
			request.setAttribute("patientvisitdetails", Patientdetails);
		} else {
	    	request.setAttribute("custmer",
	    			OhSampleRegistrationDAO.getIncomingCustomer(request.getParameter("visitid")));
		}
		request.setAttribute("testLists", testLists);
		request.setAttribute("category", category);
		if(category.equals("DEP_LAB"))
			printPrefs =PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		else
			 printPrefs =PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD);

		request.setAttribute("prefs", printPrefs);

	}

	public ActionForward saveReportPrescriptions(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception {

		   boolean status= true;

			LaboratoryForm rf = (LaboratoryForm)form;
			String prescribedid[] = rf.getPrescribedid();
			String testid[] = rf.getTestid();
			String reportId[] = rf.getReportId();//Actually Report Names
			String category = rf.getCategory();
			String mrno = rf.getMrno();
			String visitid = rf.getVisitid();
			String reportIdsForRegeneration = request.getParameter("reportIdsForRegeneration");
			String userName = (String) request.getSession(false).getAttribute("userid");

			//related to reports grid
			String manageReportNames[] = rf.getManageReportName();
			String manageReportIds[] = rf.getManageReportId();

			 ArrayList<TestVisitReports> tvrList = new ArrayList<TestVisitReports>();
			 TestVisitReports tvr = null;

			 ArrayList<TestPrescribed> tpList = new ArrayList<TestPrescribed>();
			 TestPrescribed tp = null;
			 Integer pheader_template_id = printTemplateDAO.getPatientHeaderTemplateId(
					 category.equals("DEP_LAB") ? PrintTemplate.Lab : PrintTemplate.Rad);

			 if(testid !=null){
				 for(int i =0 ;i<reportId.length;i++){
					 if( reportId[i].equals("N") ){
						 tvr = new TestVisitReports();
						 tvr.setReportName(reportId[i]);
						 tvr.setReportId(0);
						 tvrList.add(tvr);

						 tp = new TestPrescribed();
						 tp.setPrescribedId(Integer.parseInt(prescribedid[i]));
						 tp.setMrNo(mrno);
						 tpList.add(tp);

					 } else {
						 tvr = new  TestVisitReports();
						 tvr.setCategory(category);
						 tvr.setReportName(reportId[i]);
						 tvr.setVisitId(visitid);
						 tvr.setReportMode("P");
						 tvr.setUserName(userName);
						 tvr.setPheader_template_id(pheader_template_id);
						 tvrList.add(tvr);

						 tp = new TestPrescribed();
						 tp.setPrescribedId(Integer.parseInt(prescribedid[i]));
						 tp.setMrNo(mrno);
						 tpList.add(tp);
					 }
				 }
			 }
			 if(isUserOperatingOnMultiTab(rf)){
			 //incase of central lab we dont get mrno as parameter, so get it from the incoming sample registration table
         status = false;
         String MRNO = mrno;
         ActionRedirect SearchRedirect = null;
         if (null == MRNO || MRNO.equals("")) {
           BasicDynaBean incomingRegBean = new GenericDAO("incoming_sample_registration").findByKey("incoming_visit_id", visitid);
           MRNO = (incomingRegBean != null && incomingRegBean.get("mr_no") != null) ? incomingRegBean.get("mr_no").toString() : null;
         }
         if (category.equals("DEP_LAB"))
           SearchRedirect = new ActionRedirect("/Laboratory/schedules.do?_method=getScheduleList");
         else 
           SearchRedirect = new ActionRedirect("/Radiology/schedules.do?_method=getScheduleList");
         FlashScope flashScope = FlashScope.getScope(request);
         SearchRedirect.addParameter(FlashScope.FLASH_KEY, flashScope.key());
         flashScope.info("Test Details already exist.");
         
         SearchRedirect.addParameter("category", category);
         SearchRedirect.addParameter("mr_no", MRNO);     
         
         return SearchRedirect;
			 }
			 status = LaboratoryBO.saveReportPrescriptions(tvrList, tpList, reportIdsForRegeneration, userName,
					 true, pheader_template_id,request.getContextPath());

			 Map<String, String> keys = null;

			 Connection con = null;
			 //update report names
			 try {

				 con = DataBaseUtil.getConnection();
				 con.setAutoCommit(false);
				 if ( status && manageReportIds != null ) {
					 for ( int i = 0;i<manageReportIds.length;i++ ) {

						 if ( manageReportIds[i].equals("") ||
								 Integer.parseInt( manageReportIds[i] ) == 0 )//possible in case of adding a new report
							 continue;
						 keys = new HashMap<String, String>();
						 keys.put("report_name", manageReportNames[i]);

						 status = testVisitReportDAO.update( con,keys, "report_id", Integer.parseInt( manageReportIds[i] ) ) > 0;
					 }
				 }

			 }finally{
				 DataBaseUtil.commitClose(con, status);
			 }


			 FlashScope flash = FlashScope.getScope(request);
			 if(status){
				 flash.put("reportMsg", "Changes to the Reports are done Successfully");
			 }else{
				 flash.put("reportMsg", "Failed to Save Report Changes");
			 }
		   	 ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("manageReportRedirect"));
		   	 redirect.addParameter("visitid", visitid);
		   	 redirect.addParameter("category", category);
		   	 redirect.addParameter(FlashScope.FLASH_KEY,flash.key());

		 return redirect;
	}

	public ActionForward generateReport(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws SQLException {

			String reportId = request.getParameter("reportId");
			String visitid = request.getParameter("visitid");
			String category = request.getParameter("category");
			ActionRedirect redirect=null;
		    String userName = (String)request.getSession(false).getAttribute("userid");

			Integer pheader_template_id = printTemplateDAO.getPatientHeaderTemplateId(
					 category.equals("DEP_LAB") ? PrintTemplate.Lab : PrintTemplate.Rad);
			boolean status = DiagnosticsDAO.setReportContent(Integer.parseInt(reportId), visitid,
					userName, true, pheader_template_id);
			FlashScope flash = FlashScope.getScope(request);
			if(status){
				flash.put("reportMsg", "Report is Generated Successfully..");
			}else{
				flash.put("reportMsg", "Failed to Generate Report..");
			}


		 	redirect = new ActionRedirect(mapping.findForwardConfig("manageReportRedirect"));
		 	redirect.addParameter("visitid", visitid);
		 	redirect.addParameter("category", category);
	     	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
	}


	public ActionForward deleteReport(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception{

		String reportId = request.getParameter("reportId");
		String visitid = request.getParameter("visitid");
		String category = request.getParameter("category");
		new DiagnosticsDAO().deleteReport(reportId);

		request.setAttribute("reportMsg", "Report is Deleted Successfully..");

		setManageAttributes(request,visitid,category);
		return mapping.findForward("getManageScreen");
	}

	public ActionForward getAddAddendumsScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{

		String visitId = request.getParameter("visitid");
		String category = request.getParameter("category");
		setManageAttributes(request,visitId,category);
		request.setAttribute("foraddendums", true);

		return mapping.findForward("getManageScreen");
	}

	public ActionForward getAddendumTemplateEditor(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String reportId = request.getParameter("reportid");
		JSONSerializer js = new JSONSerializer().exclude("class");

		String templ = new LaboratoryDAO().getAddendumTemplateContent( reportId);
		request.setAttribute("templateContent", templ);

		request.setAttribute("reportid", reportId);

		BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		request.setAttribute("prefs",printPrefs);
		request.setAttribute("isAddendum", true);

		List getImagesListForReport = LaboratoryDAO.getReportImageDetails( Integer.parseInt(request.getParameter("reportid")) );
		request.setAttribute("imagesList", getImagesListForReport);
		request.setAttribute("imagesListjson", js.deepSerialize(
				ConversionUtils.copyListDynaBeansToMap(getImagesListForReport)) );


		return mapping.findForward("templateEditor");
	}

	public ActionForward saveAddendum(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, ParseException {
		Map requestMap = request.getParameterMap();
		String reportId = ((String[])requestMap.get("reportid"))[0];
		BasicDynaBean visitreportBean = testVisitReportDAO.findByKey("report_id", Integer.parseInt(reportId));
		visitreportBean.set("report_addendum", ((String[])requestMap.get("templateContent"))[0]);
		FlashScope flash = FlashScope.getScope(request);
		boolean success = false;

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			int repId = Integer.parseInt(reportId);
			success = testVisitReportDAO.update(con, visitreportBean.getMap(), "report_id", repId) > 0;
			sendReportNotification(repId);
			if(!success)
				flash.error("Failed to save Addendum to the report");
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("templateEditorRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("reportid", reportId);
		redirect.addParameter("status", success);

		return redirect;
	}
	
	private void sendReportNotification(Integer reportId)
	      throws SQLException, ParseException, IOException {
	  
	    List<BasicDynaBean> beans = new LaboratoryDAO().getNotificationUser(reportId, false);
	    for (BasicDynaBean bean : beans) {
	      if (bean != null) {
	        Map<String, Object> reportData = new HashMap<>();
	        reportData.put("receipient_id__", bean.get("emp_username"));
	        reportData.put("entity_id", reportId);
	        GenericDAO dao1 = new GenericDAO("message_log_batch_id");
	        int batchId = dao1.getNextSequence();
	        reportData.put("batch_id", Integer.toString(batchId));
	        MessageManager mgr = new MessageManager();
	        mgr.processEvent("diag_report_signedoff_notification", reportData);
	      }
	    }
	  }

	private ArrayList<Hashtable<String, String>> excludeReportsForInternalLab(List<Hashtable<String, String>> testsList)
			 {
		Integer userCenter = RequestContext.getCenterId();
		ArrayList<Hashtable<String, String>> internalLabTestList = new ArrayList<Hashtable<String, String>>();

		for (int i=0; i<testsList.size(); i++){
			Hashtable<String, String> ht = testsList.get(i);
			if ((null != ht.get("OUTHOUSE") && ht.get("OUTHOUSE").equals("O") && (null == ht.get("OUTSOURCE_DEST") || ht.get("OUTSOURCE_DEST").equals(""))) ||
					(ht.get("OUTSOURCE_DEST_TYPE") != null && !ht.get("OUTSOURCE_DEST_TYPE").equals("")
					&& ht.get("OUTSOURCE_DEST_TYPE").equals("C") && !ht.get("OUTSOURCE_DEST").equals(userCenter.toString()))
					|| ((null != ht.get("INCOMING_VISIT_ID") && !ht.get("INCOMING_VISIT_ID").equals("")) &&
							ht.get("INCOMING_SOURCE_TYPE").equals("C"))
							&& userCenter.intValue() == 0 ) {
				internalLabTestList.add(testsList.get(i));
			}
		}

		for (int j=0; j<internalLabTestList.size(); j++) {
			Hashtable<String, String> ht = internalLabTestList.get(j);
			for (int k=0; k<testsList.size(); k++) {
				Hashtable<String, String> test = testsList.get(k);
				if (ht.get("PRESCRIBED_ID").equals(test.get("PRESCRIBED_ID")) && ht.get("TEST_ID").equals(test.get("TEST_ID"))) {
					testsList.remove(k);
				}
			}
		}

		return internalLabTestList;

	}

	private void excludeInternalLabtestsForSampleList(List<Hashtable<String, String>> sampleSourcesList,
			List<Hashtable<String, String>> internalLabTests, List<BasicDynaBean> outsourcesList) {
		String testName = "";
		ArrayList<Hashtable<String, String>> sampleRemovedList = new ArrayList<Hashtable<String,String>>();
		for (int i=0; i<internalLabTests.size(); i++) {
			testName = internalLabTests.get(i).get("TEST_NAME");
			for (int j=0; j<sampleSourcesList.size(); j++) {
				if (sampleSourcesList.get(j).get("D_TEST_NAME").equals(testName)) {
					sampleRemovedList.add(sampleSourcesList.get(j));
					sampleSourcesList.remove(j);
				}
			}
		}

		for (int k=0; k<sampleRemovedList.size(); k++) {
			String sampleNo = "";
			sampleNo = sampleRemovedList.get(k).get("SAMPLE_SNO");

			for (int z=0; z<outsourcesList.size(); z++) {
				if (outsourcesList.get(z).get("current_center_sample_no").equals(sampleNo)) {
					outsourcesList.remove(z);
				}
			}

		}

	}
	
private boolean isUserOperatingOnMultiTab(LaboratoryForm labForm)throws SQLException {

    String[] prescribedIDs = labForm.getPrescribedid();
    String[] revisionNumbers = labForm.getRevisionNumber();    
    GenericDAO testPresDAO = new GenericDAO("tests_prescribed");
   
    if (null != prescribedIDs && prescribedIDs.length > 0) {
      for (int j=0; j<prescribedIDs.length; j++) {
        if (null != prescribedIDs[j] && !prescribedIDs[j].equals("") && null != revisionNumbers[j] && !revisionNumbers[j].equals("")) {   
          if (!revisionNumbers[j].equals(testPresDAO.findByKey("prescribed_id", Integer.parseInt(prescribedIDs[j]))
              .get("revision_number").toString()))
            return true;
        }
      }
    }
    
    return false;
  }

}
