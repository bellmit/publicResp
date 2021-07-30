/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.TestPrescribed;
import com.insta.hms.diagnosticmodule.radiology.RadiologyBO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author krishna
 *
 */
public class CancelTestAction extends DispatchAction {

	public ActionForward cancelPrescription(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String visitId = request.getParameter("visitid");
		String category = request.getParameter("category");
		setCancellationAttributes(request,visitId,category);
		return mapping.findForward("testCancellation");
	}

	private void setCancellationAttributes (HttpServletRequest request, String visitId, String category)
		throws Exception{
		Object roleID = null;
		HttpSession session = request.getSession();
		String message = request.getParameter("resultmsg");
		BasicDynaBean pd =  VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
		HashMap actionRightsMap= (HashMap) request.getSession(false).getAttribute("actionRightsMap");
		String allowCancellationAtAnyTime=(String) actionRightsMap.get("cancel_test_any_time");
		roleID=  request.getSession(false).getAttribute("roleId");
		if(roleID.equals(1)|| roleID.equals(2))
			allowCancellationAtAnyTime = "A";

		if(pd != null)
			request.setAttribute("patientvisitdetails",pd);
		else
			request.setAttribute("custmer",
	    			OhSampleRegistrationDAO.getIncomingCustomer(visitId));

		List<Hashtable<String,String>> testDetails=LaboratoryDAO.getTestDetails(visitId,category, allowCancellationAtAnyTime);
		request.setAttribute("testDetails", testDetails);
		String username = (String)session.getAttribute("userid");
		request.setAttribute("userName", username);
		request.setAttribute("category", category);
		request.setAttribute("resultmsg", message);

	}

	public ActionForward cancellPrescriptionDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws  Exception {


			TestPrescriptionCancellationForm lf = (TestPrescriptionCancellationForm)form;
			Object roleID=null;
			ArrayList<TestPrescribed> dtolistWithBillRefund = new ArrayList<TestPrescribed>();
			ArrayList<TestPrescribed> dtolostWithOutBillRefund = new ArrayList<TestPrescribed>();
			ArrayList<TestPrescribed> dtolist = new ArrayList<TestPrescribed>();
			HashMap actionRightsMap = new HashMap();
			ChargeBO chargeBO = new ChargeBO();
			List<Object> cancelFailedIDS = new ArrayList<Object>();

			ArrayList<TestPrescribed> dtolistWithBillRefundForIlLab = new ArrayList<TestPrescribed>();
			ArrayList<TestPrescribed> dtolostWithOutBillRefundForILab = new ArrayList<TestPrescribed>();
			ArrayList<TestPrescribed> dtoListToCancelTests = new ArrayList<TestPrescribed>();
			actionRightsMap= (HashMap) request.getSession(false).getAttribute("actionRightsMap");
			String allowCancelAfterSampleCollection=(String) actionRightsMap.get("allow_cancel_test");
			String allowCancellationAtAnyTime = (String) actionRightsMap.get("cancel_test_any_time");
			roleID=  request.getSession(false).getAttribute("roleId");
			String userName = request.getParameter("user");
			GenericDAO testPrescribedDAO = new GenericDAO("tests_prescribed");
			boolean outsourceTestsAvl = false;

			ActionRedirect redirect = new ActionRedirect(mapping.findForward("cancelRedirect"));
			FlashScope flash = FlashScope.getScope(request);
			BasicDynaBean outsourceBean = null;

			String toDate = request.getParameter("cancelledDate");
		    String visitId = lf.getPatVisitId();
		    String mrno = lf.getPresMrno();
		    String canceledBy = request.getParameter("user");
//		    String remarks = lf.getRemarks();
		    String presId[] = lf.getPresId();
		    String testName[] = lf.getTestName();
		    String category = lf.getCategory();
		    String[] outSourceDestPrescIDS = lf.getOutsourceDestPrescId();
		    String[] reportIds = lf.getReportId();
		    String[] testIds = lf.getTestId();
		    String[] currentLocationPrescId = lf.getCurrentLocationPrescId();


			String cancelType[]  = lf.getCancelType();
			Connection con = null;
			boolean success = false;
			String status = null;
			boolean allSuccess = false;
			
			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				for(int i=0;i<cancelType.length; i++){
					if (cancelType[i] != null && !cancelType[i].equals("")) {
					    TestPrescribed tp = new TestPrescribed();
					    tp.setCancelDate(DataBaseUtil.parseDate(toDate));
					    tp.setCancelledBy(canceledBy);
					    tp.setPrescribedId(Integer.parseInt(presId[i]));
					    tp.setRemarks(lf.getRemarks()[i]);
					    tp.setMrNo(mrno);
					    tp.setVisitId(visitId);
					    tp.setTestName(testName[i]);
					    tp.setSampleCollectedFlag(lf.getSflag()[i]);
					    tp.setUserName(userName);
					    if (outSourceDestPrescIDS[i] != null && !outSourceDestPrescIDS[i].equals("")) {
					    	outsourceBean = testPrescribedDAO.findByKey("prescribed_id", 
					    			Integer.parseInt(currentLocationPrescId[i]));
					    	tp.setReportId((outsourceBean != null && outsourceBean.get("report_id") != null
					    			&& !outsourceBean.get("report_id").equals("")) ? (Integer)outsourceBean.get("report_id") : -1);
					    } else {
					    	tp.setReportId(reportIds[i] != null && !reportIds[i].equals("") ? Integer.parseInt(reportIds[i]) : -1);
					    }
					    tp.setTestId(testIds[i]);
		
					    if(cancelType[i].equals("Y")){
					    	dtolistWithBillRefund.add(tp);
					    }else if(cancelType[i].equals("N")){
					    	dtolostWithOutBillRefund.add(tp);
					    }else{
					    	dtolist.add(tp);
					    }
					}
				}
				/* Check weather roleid is  equal to 1 or 2 then pass it */
				if(roleID.equals(1)|| roleID.equals(2)) {
					allowCancelAfterSampleCollection="A";
					allowCancellationAtAnyTime = "A";
				}
	
				RadiologyBO bo = new RadiologyBO();
				Map statusMap = bo.cancellPrescriptionDetails(con,
						dtolistWithBillRefund,dtolostWithOutBillRefund,
						category,allowCancelAfterSampleCollection, allowCancellationAtAnyTime, cancelFailedIDS, false);
				status = (String)statusMap.get("error");
				success = (Boolean)statusMap.get("success");
				BasicDynaBean outsourcePrescBean = null;
				boolean continueLoop = false;
				for (int j=0; j<cancelType.length; j++) {
					if (cancelType[j] != null && !cancelType[j].equals("")) {
						BasicDynaBean collectionCenterBean = testPrescribedDAO.findByKey("prescribed_id", Integer.parseInt(presId[j]));
						BasicDynaBean collectionCenterRefBean = testPrescribedDAO.findByKey("prescribed_id", collectionCenterBean.get("reference_pres"));
						if (!cancelFailedIDS.contains(Integer.parseInt(presId[j]))) {
							String outsourceDestPrescID = outSourceDestPrescIDS[j];
							do {
								if ((outsourceDestPrescID != null && !outsourceDestPrescID.equals(""))
										|| (((Boolean)collectionCenterBean.get("re_conduction")) && null != collectionCenterRefBean 
										&& null != collectionCenterRefBean.get("outsource_dest_prescribed_id") 
										&& !collectionCenterRefBean.get("outsource_dest_prescribed_id").equals(""))) {
									if (cancelType[j].equals("Y")&& chargeBO.isCancelable("DIA", Integer.parseInt(presId[j]))
											|| cancelType[j].equals("N")) {
			
										outsourceTestsAvl = true;									
										if (null != outsourceDestPrescID && !outsourceDestPrescID.equals("")) {
											outsourcePrescBean = testPrescribedDAO.findByKey("prescribed_id",
													Integer.parseInt(outsourceDestPrescID));
										} else if (((Boolean)collectionCenterBean.get("re_conduction")) 
												&& null != collectionCenterRefBean.get("outsource_dest_prescribed_id") 
												&& !collectionCenterRefBean.get("outsource_dest_prescribed_id").equals("")) {
											outsourcePrescBean = testPrescribedDAO.findByKey("prescribed_id", 
													collectionCenterRefBean.get("outsource_dest_prescribed_id"));
											//revert the changes and continue the loop
											LaboratoryDAO.revertReconductionForOutsource(collectionCenterRefBean.get("outsource_dest_prescribed_id"));
										}
										if (outsourceDestPrescID != null && !outsourceDestPrescID.equals("")) {
											TestPrescribed tp = new TestPrescribed();
											tp.setCancelDate(DataBaseUtil.parseDate(toDate));
											tp.setCancelledBy(canceledBy);
											tp.setPrescribedId((Integer)outsourcePrescBean.get("prescribed_id"));
											tp.setRemarks(lf.getRemarks()[j]);
											tp.setMrNo("");
											tp.setVisitId((String)outsourcePrescBean.get("pat_id"));
											tp.setTestName(testName[j]);
											tp.setSampleCollectedFlag((String)outsourcePrescBean.get("sflag"));
											tp.setUserName(userName);
											//For internal lab test, the report id we need to get from the conduction center
											BasicDynaBean currentLocationBean = testPrescribedDAO.findByKey("prescribed_id", 
													outsourcePrescBean.get("curr_location_presc_id"));
											tp.setReportId((currentLocationBean.get("report_id") != null
													&& !currentLocationBean.get("report_id").equals("")) ? (Integer)currentLocationBean.get("report_id") : -1);
											tp.setTestId((String)outsourcePrescBean.get("test_id"));
				
											if (cancelType[j].equals("Y")){
												dtolistWithBillRefundForIlLab.add(tp);
											}else if (cancelType[j].equals("N")){
												dtolostWithOutBillRefundForILab.add(tp);								
																				
				
											}
										}
									}
								}
								continueLoop = false;
								if ((Boolean)collectionCenterBean.get("re_conduction") 
										&& (outsourceDestPrescID == null || outsourceDestPrescID.equals(""))
										&& outsourcePrescBean != null && outsourcePrescBean.get("outsource_dest_prescribed_id") != null) {
									outsourceDestPrescID = null;
									collectionCenterRefBean = outsourcePrescBean;
									continueLoop = true;
								} else if (outsourcePrescBean != null) {
									outsourceDestPrescID = (outsourcePrescBean.get("outsource_dest_prescribed_id") != null ?
											outsourcePrescBean.get("outsource_dest_prescribed_id").toString() : null);
									if (null != outsourceDestPrescID && !outsourceDestPrescID.equals(""))
										continueLoop = true;
								}
								
							} while (continueLoop);
						}
					}
				}
	
				if (category.equals("DEP_LAB") && outsourceTestsAvl) {
					Map statusMapForILab = bo.cancellPrescriptionDetails(con,
							dtolistWithBillRefundForIlLab, dtolostWithOutBillRefundForILab,
							category, allowCancelAfterSampleCollection, allowCancellationAtAnyTime, cancelFailedIDS, true);
					status = status + "</br></br> Out Sourcing Tests Status:</br>" +(String)statusMapForILab.get("error");
					success &= (Boolean)statusMapForILab.get("success");
				}
				
				allSuccess = true;
				allSuccess &= success;
				
			} finally {
				DataBaseUtil.commitClose(con, allSuccess);
			}

			flash.info(status);
			redirect.addParameter("visitid", visitId);
			redirect.addParameter("category", category);
			redirect.addParameter("resultmsg", status);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}


}
