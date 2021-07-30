package com.insta.hms.diagnosticmodule.radiology;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillActivityCharge;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.TestPrescribed;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.wardactivities.PatientActivitiesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class RadiologyBO {

	static Logger logger = LoggerFactory.getLogger(RadiologyBO.class);
	private GenericDAO billChargeDao = new GenericDAO("bill_charge");
    
	private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");
	private static final GenericDAO testVisitReportsDAO = new GenericDAO("test_visit_reports");
	
	public Map cancellPrescriptionDetails(Connection con, ArrayList<TestPrescribed> dtolistWithBillRefund,
			ArrayList<TestPrescribed> dtolostWithOutBillRefund,String category,
			String allowCancelAfterSampleCollection, String allowCancellationAtAnyTime, 
			List<Object> failedCancelList, boolean cancelConductionCenterTests)throws Exception {
		boolean status=false;
		ArrayList<String> billsList = new ArrayList<String>();
		Map<String, Object> map = new HashMap<String, Object>();
		String string = "Test Cancelled  for:";
		String unableCancel = "<br>The following test prescriptions are Either part of package (OR)" +
								" Bill is closed ...So Cancellation is not allowed<br/>";
		String notAuthorizedToCancel="<br>You are not authorized to cancel the test which are sample collected tests are: <br/>";
		String internalLabTestsStatus = "<br>The Following tests are part of Internal Lab Tests So Cancellation with bill refund is allowed" +
											"  only when the outsourcing test does not have any report in the collection center:</br>";
		String faildtoCancel = "Failed to Cancel the test for :";
		String failedToCancelReportGenTests = "<br><br> <b>Tests selected for cancellation has report, All of the below tests, connected to same report, needs to be cancelled</b><br>";

		boolean allSuccess = false;
		boolean addflagForsuccess=false;
		boolean addflagForbill = false;
		boolean addflagForFailed = false;
		boolean addflagAuthorization= false;
		boolean addCancelFlagForILabTests = false;

		DiagnosticsDAO dao = new DiagnosticsDAO();
		Iterator<TestPrescribed> it = dtolistWithBillRefund.iterator();
		ChargeBO chargeBO = new ChargeBO();
		String chargeHead = null;

		try {
			if(category.equals("DEP_LAB")){
				chargeHead = ChargeDTO.CH_DIAG_LAB;
			}else{
				chargeHead = ChargeDTO.CH_DIAG_RAD;
			}

			while(it.hasNext()){
				TestPrescribed tp = it.next();
				if(allowCancelAfterSampleCollection.equals("A") && tp.getSampleCollectedFlag().equals("1")
						|| tp.getSampleCollectedFlag().equals("0") || allowCancellationAtAnyTime.equals("A")){
					if (chargeBO.isCancelable("DIA", tp.getPrescribedId())) {
						if (cancellableInternalLabTest(con, tp.getPrescribedId())) {
							status = dao.cancellPrescriptionDetails(con,tp,chargeHead);
							if ( status && PatientActivitiesDAO.getActivity(con, tp.getPrescribedId(), "I") != null) {
								status = PatientActivitiesDAO.updateStatus(con, tp.getPrescribedId(), "I");
							}
							if (status) {
								addflagForsuccess = true;
								string +="<br/><b>"+tp.getTestName()+"</b>";
							} else {
								faildtoCancel = "<br/><b>" + tp.getTestName() + "</b>";
								addflagForFailed = true;
							}
							String chargeId = BillActivityChargeDAO.getChargeId("DIA", tp.getPrescribedId());
							BasicDynaBean chargeBean = billChargeDao.findByKey("charge_id", chargeId);
							String billNo = (chargeBean != null) ? (String)chargeBean.get("bill_no") : null;
							if (billNo != null && !billsList.contains(billNo))
								billsList.add(billNo);
						} else {
							internalLabTestsStatus +="<br/><b>" + tp.getTestName() + "</b>";
							addCancelFlagForILabTests = true;
							failedCancelList.add(tp.getPrescribedId());
						}

					} else {
							unableCancel += "<br/><b>" + tp.getTestName() + "</b>";
							addflagForbill = true;
							failedCancelList.add(tp.getPrescribedId());
					}
				} else {
					notAuthorizedToCancel += "<br/><b>" + tp.getTestName() + "</b>";
					addflagAuthorization = true;
					failedCancelList.add(tp.getPrescribedId());
				}
			}


			BasicDynaBean tpBean = null;

			Map<Integer, List<String>> testCancelFailedMap = cancelAllTestsBelongsToTheReport(con, dtolostWithOutBillRefund, failedCancelList);

	        Iterator<TestPrescribed> it1 = dtolostWithOutBillRefund.iterator();
	        while (it1.hasNext()) {
				TestPrescribed tp = it1.next();

				tpBean = testsPrescribedDAO.findByKey(con,"prescribed_id", tp.getPrescribedId());
					//reference test if any
				BasicDynaBean reftpBean = testsPrescribedDAO.findByKey(con,"prescribed_id", tpBean.get("reference_pres"));

				if ( (allowCancelAfterSampleCollection.equals("A")//need to check one more action right
						&& tp.getSampleCollectedFlag().equals("1") || tp.getSampleCollectedFlag().equals("0")) || allowCancellationAtAnyTime.equals("A")) {
					status = DiagnosticsDAO.closePrescriptions(con, tp);//cancling activity

				}

				if ( status && PatientActivitiesDAO.getActivity(con, tp.getPrescribedId(), "I") != null) {
					status = PatientActivitiesDAO.updateStatus(con, tp.getPrescribedId(), "I");
				}
				if ( reftpBean != null && !reftpBean.get("conducted").equals("X")) {//possible in case if reconducting test is cancled
					 //update bill activity to new activity id
					 status &= new BillActivityChargeDAO(con).updateActivityId(
							 String.valueOf(tp.getPrescribedId()),
							 BillActivityCharge.DIAG_ACTIVITY_CODE, reftpBean.get("prescribed_id").toString() );

				} else {
					if ( !(Boolean)tpBean.get("re_conduction")//cancelling a reconducted test needs to be treated differently
							&& allowCancelAfterSampleCollection.equals("A")
							&& tp.getSampleCollectedFlag().equals("1")
							|| tp.getSampleCollectedFlag().equals("0") || allowCancellationAtAnyTime.equals("A")) {

						if (chargeBO.isCancelable("DIA", tp.getPrescribedId())) {
							ChargeDAO chargeDao = new ChargeDAO(con);
							BillActivityChargeDAO bacDAO = new BillActivityChargeDAO(con);
							BillActivityChargeDAO billActivtyDao = new BillActivityChargeDAO(con);
							BillActivityCharge billActivityDto = null;
							ChargeDTO chargeDto = bacDAO.getCharge("DIA", tp.getPrescribedId());
							String chargeId = null;
							if (chargeDto != null) {
								chargeId = (String) chargeDto.getChargeId();
								status = chargeDao.updateHasActivityStatus(chargeId,
										false);
								billActivityDto = new BillActivityCharge();
								billActivityDto.setActivityId(tp.getPrescribedId());
								billActivityDto.setActivityCode("DIA");
								billActivityDto.setChargeId(chargeId);
								status = billActivtyDao.deleteActivity(billActivityDto);
							}
						}
						if (status) {
							addflagForsuccess = true;
							string += "<br/><b>" + tp.getTestName() + "</b>";
						} else {
							faildtoCancel = "<br/><b>" + tp.getTestName() + "</b>";
							addflagForFailed = true;
							failedCancelList.add(tp.getPrescribedId());
						}
					} else {
						notAuthorizedToCancel += "<br/><b>" + tp.getTestName();
						addflagAuthorization = true;
						failedCancelList.add(tp.getPrescribedId());
					}
				}
				if ( (Boolean)tpBean.get("re_conduction") ) {
					addflagForsuccess = onCancleReconductTest( con,tpBean );
					string += "<br/><b>" + tp.getTestName() + "</b>";
				}
				if (!addflagForsuccess)
					failedCancelList.add(tp.getPrescribedId());

			}
	        
	        
	        if (cancelConductionCenterTests) {
	        	ArrayList<TestPrescribed> dtoListToCancelTests = cancelReferenceTestsForConductioncenter(dtolostWithOutBillRefund);	        
		        Iterator<TestPrescribed> it2 = dtoListToCancelTests.iterator();
		        while (it2.hasNext()) {		        	
		        	TestPrescribed tp = it2.next();	
					tpBean = testsPrescribedDAO.findByKey(con,"prescribed_id", tp.getPrescribedId());
					
					if ( (allowCancelAfterSampleCollection.equals("A")//need to check one more action right
							&& tp.getSampleCollectedFlag().equals("1") || tp.getSampleCollectedFlag().equals("0")) || allowCancellationAtAnyTime.equals("A")) {
						status = DiagnosticsDAO.closePrescriptions(con, tp);//cancelling activity
	
					}
	
					if ( status && PatientActivitiesDAO.getActivity(con, tp.getPrescribedId(), "I") != null) {
						status = PatientActivitiesDAO.updateStatus(con, tp.getPrescribedId(), "I");
					}
		        	
		        }
	        }

	        if (!addflagForsuccess) {
				string = "";
			}
			if (addflagForbill) {
				string += unableCancel;
			}
			if (addCancelFlagForILabTests) {
				string += internalLabTestsStatus;
			}
			if (addflagForFailed) {
				string += faildtoCancel;
			}
			if (addflagAuthorization) {
				string += notAuthorizedToCancel;
			}
			if (testCancelFailedMap.keySet().size() > 0) {
				String testCancelString = getFailedTestsString(testCancelFailedMap);
				if (testCancelString != null && !testCancelString.equals("") && testCancelString.length() > 10)//checking string length to avoid '<br>' string
					string += failedToCancelReportGenTests + testCancelString;
			}

			allSuccess = true;

		} catch (Exception e) {
			allSuccess = false;
			throw e;

		} finally {
			//DataBaseUtil.commitClose(con, allSuccess);
			if (allSuccess) {
				if (billsList != null) {
					for(String bill_no: billsList) {
						if (bill_no != null && !bill_no.equals("")) {
							BillDAO.resetTotalsOrReProcess(bill_no, false);
						}
					}
				}
			}
		}
		map.put("error", string);
		map.put("success", allSuccess);
		return map;
	}

	/**
	 * Two things can happen in this method
	 * 1.Reverting reconduction of old test which was reconducted
	 * 2.Reverting activity_id of bill_activity_table to old activity_id which was reconducted
	 * @param con
	 * @param recTestBean
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public boolean onCancleReconductTest( Connection con,BasicDynaBean recTestBean )
	throws SQLException,IOException{
		boolean status = true;

		GenericDAO billActivityChargeDAO = new GenericDAO("bill_activity_charge");
		Map<String, Object> identifiers = new HashMap<String, Object>();
		
		BasicDynaBean refTpBean = testsPrescribedDAO.findByKey(con,"prescribed_id", recTestBean.get("reference_pres"));
		//reference prescription if this is reconducting test
		if(refTpBean != null){//this is the old test which is reconducted
			BasicDynaBean tvrBean = testVisitReportsDAO.findByKey(con,"report_id", refTpBean.get("report_id"));
			refTpBean.set("conducted", tvrBean != null && tvrBean.get("signed_off").equals("Y") ? "S" :
				( tvrBean == null ) ? "P" : "C");//reverting reconduction
			status = testsPrescribedDAO.update(con, refTpBean.getMap(), "prescribed_id",refTpBean.get("prescribed_id")) > 0;
		}

		//update bill activity to new activity id
		identifiers.put("activity_id", recTestBean.get("prescribed_id").toString());
		identifiers.put("activity_code", "DIA");
		if (billActivityChargeDAO.findByKey(con, identifiers) != null) {
			 status &= new BillActivityChargeDAO(con).updateActivityId(
					 recTestBean.get("prescribed_id").toString(),
					 BillActivityCharge.DIAG_ACTIVITY_CODE, refTpBean.get("prescribed_id").toString());
		}
		String userName = RequestContext.getUserName();
		 //update activity_conducted to C in bill_charge,bill_activity_charge
		 BillActivityChargeDAO.updateActivityDetails(
				 con, BillActivityCharge.DIAG_ACTIVITY_CODE, refTpBean.get("prescribed_id").toString(),null,"C",null, userName);
		 
		 //update newPresc id to old prescid
		 status &= LaboratoryDAO.updateNewprescidForIncomingTest(con, recTestBean.get("prescribed_id"), recTestBean.get("reference_pres"));

		return status;
	}

	public static boolean cancelChildTest(Connection con, BasicDynaBean childTestBean)throws SQLException, IOException {
		boolean status = true;
		Map<String, Object> columndata = new HashMap<String, Object>();
		Map<String, Object> keys = new HashMap<String, Object>();
		String username = RequestContext.getUserName();

		columndata.put("cancelled_by", username);
		columndata.put("conducted", "X");
		columndata.put("cancel_date", DateUtil.getCurrentTimestamp());

		keys.put("prescribed_id", childTestBean.get("prescribed_id"));

		status = testsPrescribedDAO.update(con, columndata, keys) > 0;

		return status;
	}

	private boolean cancellableInternalLabTest(Connection con, Object prescribedID)throws SQLException {
		BasicDynaBean bean = null;
		bean = testsPrescribedDAO.findByKey(con, "prescribed_id", prescribedID);
		Object outsourcePrescribedID = bean.get("outsource_dest_prescribed_id");
		if (outsourcePrescribedID != null && !outsourcePrescribedID.equals("")) {
			bean = testsPrescribedDAO.findByKey(con, "prescribed_id", bean.get("curr_location_presc_id"));
			//Need to check cancellation status also, where in sample rejection the outsource dest prescribed id will be cancelled.
			return (bean.get("conducted").toString().equals("N") ||
					bean.get("conducted").toString().equals("NRN") ||
					bean.get("conducted").toString().equals("X")); 
		}


		return true;
	}

	private Map<Integer, List<String>> cancelAllTestsBelongsToTheReport(Connection con,
			ArrayList<TestPrescribed> dtolistWithOutBillRefund, List<Object> cancelFailedIDs)throws SQLException, IOException {
		List<String> needToSelectTstsForCanList = null;
		Map<Integer, List<String>> testCancelFailedMap = new HashMap<Integer, List<String>>();

		Map<String, Object> reportColData = new HashMap<String, Object>();
		reportColData.put("report_state", "D");
		Map<String, Object> reportKeys = new HashMap<String, Object>();
		List<BasicDynaBean> testPrescList = null;
		boolean areAllTstsSelectedForcan = true;

		for (int i=0; i<dtolistWithOutBillRefund.size(); i++) {
			needToSelectTstsForCanList = new ArrayList<String>();
			List<String> tempList = new ArrayList<String>();
			TestPrescribed tp = dtolistWithOutBillRefund.get(i);
			Integer reportID = tp.getReportId();
			/*Excluded reportId 0 also why because when the report set to No Report through manage reports screen then
			 the reportid set to 0 in testprescribed table, if this test is selected for cancellation, then only we need to
			 cancel the test, not required to discard the report since the report is not associated with the test.*/
			if (testCancelFailedMap.get(reportID) == null && reportID != -1 && reportID != 0) {
				testCancelFailedMap.put(reportID, needToSelectTstsForCanList);
				testPrescList = testsPrescribedDAO.findAllByKey(con, "report_id", reportID);
				for (int k=0; k<dtolistWithOutBillRefund.size(); k++) {
					if (((Integer)dtolistWithOutBillRefund.get(k).getReportId()).equals(reportID))
						tempList.add(dtolistWithOutBillRefund.get(k).getTestId());
				}
				for (int j=0; j<testPrescList.size(); j++) {
					String testID = testPrescList.get(j).get("test_id").toString();
					if (!tempList.contains(testID)) {
						areAllTstsSelectedForcan = false;
						break;
					}
				}

				if (!areAllTstsSelectedForcan)
					for (int l=0; l<testPrescList.size(); l++) {
						needToSelectTstsForCanList.add(testPrescList.get(l).get("test_id").toString());
					}
			}			

		}
		
		 for (Integer key :testCancelFailedMap.keySet()) {
			 for (int m=0; m<testCancelFailedMap.get(key).size(); m++) {
				 List<String> testList = testCancelFailedMap.get(key);
				 for (int n=0; n<dtolistWithOutBillRefund.size(); n++) {
					 TestPrescribed tp = dtolistWithOutBillRefund.get(n);
					 if ((((Integer)tp.getReportId()).equals(key)) && (tp.getTestId().equals(testList.get(m)))) {
						 cancelFailedIDs.add(tp.getPrescribedId());
						 dtolistWithOutBillRefund.remove(n);
					 }
				 }
			 }
		 }

		 //After removing the tests in the dtolistWIthoutBillRefund list, mark report as discarded which are to be cancelled.

		 for (int k=0; k<dtolistWithOutBillRefund.size(); k++) {
			 TestPrescribed tp = dtolistWithOutBillRefund.get(k);
			 if (tp.getReportId() != -1 && tp.getReportId() != 0) {
				 if (reportKeys.get("report_id") != null ?
						 !((Integer)reportKeys.get("report_id")).equals(tp.getReportId()) : true) {
					 reportKeys.put("report_id", tp.getReportId());
					 testVisitReportsDAO.update(con, reportColData, reportKeys);
				 }
			 }
		 }

		return testCancelFailedMap;
	}

	private String getFailedTestsString(Map<Integer, List<String>> cancelFailedMap)throws SQLException {
		String message = "";
		GenericDAO diagnosticsDAO = new GenericDAO("diagnostics");
		for (Integer key : cancelFailedMap.keySet()) {
			if (!cancelFailedMap.get(key).isEmpty() && cancelFailedMap.get(key).size() > 0)
				message = message + "<br><b>Report Name: </b>" + testVisitReportsDAO.findByKey("report_id", key).get("report_name")+"</br>";
			for (String test : cancelFailedMap.get(key)) {
				message = message + "  <b>Test: </b>" +diagnosticsDAO.findByKey("test_id", test).get("test_name") + "<br>";
			}
			message = message + "<br>";
		}
		return message;
	}
	
	private ArrayList<TestPrescribed> cancelReferenceTestsForConductioncenter(ArrayList<TestPrescribed> dtoListWithOutBillRefund)throws SQLException {
		Iterator<TestPrescribed> it = dtoListWithOutBillRefund.iterator();
		BasicDynaBean conductionCenterBean = null;
		BasicDynaBean conductionCenterRefBean = null;
		ArrayList<TestPrescribed> dtoListToCancelTests = new ArrayList<TestPrescribed>();
		while (it.hasNext()) {
			TestPrescribed tp = it.next();
			conductionCenterBean = testsPrescribedDAO.findByKey("prescribed_id", tp.getPrescribedId());
			conductionCenterRefBean = testsPrescribedDAO.findByKey("prescribed_id", conductionCenterBean.get("reference_pres"));
			boolean isReferencePrescExists = null != conductionCenterRefBean 
						&& ((Boolean)conductionCenterRefBean.get("re_conduction")).equals(true)
						&& conductionCenterRefBean.get("reference_pres") != null && !conductionCenterRefBean.get("reference_pres").equals("");
			
			while (isReferencePrescExists) {
				BasicDynaBean toBeCancelledBean = testsPrescribedDAO.findByKey("prescribed_id",
						(conductionCenterRefBean.get("reference_pres")));
				TestPrescribed tp1 = new TestPrescribed();
				tp1.setCancelDate((tp.getCancelDate()));
				tp1.setCancelledBy(tp.getCancelledBy());
				tp1.setPrescribedId((Integer)toBeCancelledBean.get("prescribed_id"));
				tp1.setRemarks(tp.getRemarks());				
				tp1.setUserName(tp.getUserName());
				tp1.setSampleCollectedFlag((String)toBeCancelledBean.get("sflag"));
								
				if (((Boolean)toBeCancelledBean.get("re_conduction")).equals(true)
						&& toBeCancelledBean.get("reference_pres") != null && !toBeCancelledBean.get("reference_pres").equals("")) {
					isReferencePrescExists = true;
					conductionCenterRefBean = toBeCancelledBean;
				} else {
					isReferencePrescExists = false;
				}

				dtoListToCancelTests.add(tp1);
			}			
				
		}
		return dtoListToCancelTests;
	}

}
