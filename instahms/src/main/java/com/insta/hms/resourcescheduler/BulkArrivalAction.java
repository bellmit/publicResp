package com.insta.hms.resourcescheduler;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.TestDocumentDTO;
import com.insta.hms.Registration.RegistrationBO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.AppointmentSource.AppointmentSourceDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BulkArrivalAction extends BaseAction {
  
  private static final GenericDAO schedulerAppointmentsDAO =
      new GenericDAO("scheduler_appointments");


	public ActionForward markBulkArrived(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws SQLException, IOException, Exception {

		String appIds = (String)request.getParameter("appIds");
    	String[] appIdsArr =appIds.split(",");
    	String patientId = null;
    	RegistrationBO regBO = new RegistrationBO();

    	VisitDetailsDAO rdao = new VisitDetailsDAO();
    	Connection con = null;
    	String userName = (String)request.getSession(false).getAttribute("userid");
    	GenericDAO patientDetailsDAO = new GenericDAO("patient_details");
    	GenericDAO doctorDao = new GenericDAO("doctor_consultation");

    	String[] columns = {"salutation", "patient_name", "middle_name", "last_name", "patient_phone", "patient_category_id"};
    	Map identifiers = new HashMap();
    	String orgId = "ORG0001";
    	GenericDAO packPresDao = new GenericDAO("package_prescribed");
    	boolean allSuccess = false;

    	if (null != appIdsArr && appIdsArr.length > 0) {
    		for (int i=0; i<appIdsArr.length; i++) {
    			try {
	    			allSuccess = false;
	    			boolean visitSuccess = true;
	    			int appointmentId = 0;
					if (null != appIdsArr[i] && !appIdsArr[i].equals("")) {
						appointmentId =  new Integer(appIdsArr[i]);
					} else {
						continue;
					}
					Map map = getChannelingOrders(appointmentId);
					if (null != map.get("channellingOrdersList") && ((List)map.get("channellingOrdersList")).size() > 0) {
		    	    	Map channelingOrder = (Map)((List)map.get("channellingOrdersList")).get(0);
		    	    	if (channelingOrder.get("activity_type").equals("Doctor")) {
							Bill bill = new Bill();
			    			con = DataBaseUtil.getConnection();
			    			con.setAutoCommit(false);
			    			String billNo = null;
			    			int consultationTypeId = 0;

			    			OrderBO orderBo = new OrderBO();
							boolean chargeable = true;

			    		    BasicDynaBean visitDetailsBean = rdao.getBean();
			    		    BasicDynaBean schedulerAppBean = schedulerAppointmentsDAO.findByKey(con, "appointment_id", appointmentId);
			    		    identifiers.put("mr_no", schedulerAppBean.get("mr_no"));
			        		BasicDynaBean patientDetailsBean = patientDetailsDAO.findByKey(con, Arrays.asList(columns), identifiers);
			    		    patientId = VisitDetailsDAO.getNextVisitId("o", (Integer)schedulerAppBean.get("center_id"));
			    		    visitDetailsBean = getVisitDetailsBean(patientId, schedulerAppBean, visitDetailsBean, map);

			    			visitDetailsBean.set("user_name", userName);
			    			visitDetailsBean.set("patient_category_id", patientDetailsBean.get("patient_category_id"));
			    	    	visitSuccess &= rdao.insert(con, visitDetailsBean);

			    	    	//setting to regANDBill to Y, isTpa false,
							BigDecimal billDeduction = BigDecimal.ZERO;
							visitSuccess &= regBO.regBill(con, patientId, "o", userName, bill, "P", false,
										   null, null, billDeduction, orgId);
							billNo = bill.getBillNo();

			        		if(schedulerAppBean != null && schedulerAppBean.get("consultation_type_id") != null) {
			        			consultationTypeId = (Integer)schedulerAppBean.get("consultation_type_id");
			        		}
			    			if (appointmentId > 0) {
			    				 visitSuccess &= ResourceBO.updateScheduler(con, appointmentId, (String)schedulerAppBean.get("mr_no"), patientId, patientDetailsBean, "", userName,
			    						 consultationTypeId, null, null, "Reg");

			    		    }

			    			if(appointmentId > 0) {
								Map columndata = new HashMap();
								columndata.put("scheduler_prior_auth_no", null);
								columndata.put("scheduler_prior_auth_mode_id", 1);
								Map keys = new HashMap();
								keys.put("appointment_id", appointmentId);
								visitSuccess &= schedulerAppointmentsDAO.update(con, columndata, keys) > 0;
			    			}
			    			if (appointmentId > 0) {
			    				visitSuccess &= ResourceBO.updateAppointmentStatus(con, "DOC", appointmentId, userName);
			    			}

							if (billNo != null)
								orderBo.setBillInfo(con, patientId, billNo, false, userName);

							BasicDynaBean newMultiVisitPackageOrdersBean = packPresDao.getBean();
							newMultiVisitPackageOrdersBean.set("package_id", ((BigDecimal)channelingOrder.get("package_id")).intValue());
							newMultiVisitPackageOrdersBean.set("remarks", "");

							List orders = new ArrayList();
							List newPreAuths = Collections.EMPTY_LIST;
							List newSecPreAuths = Collections.EMPTY_LIST;
							List<Integer> newPreAuthModes = Collections.EMPTY_LIST;
							List<Integer> newSecPreAuthModes = Collections.EMPTY_LIST;
							List firstOfCategoryList  = new ArrayList();
							List<String> condDoctrsList = new ArrayList<String>();
							List<Map<String,Object>> operAnaesTypeList = new ArrayList<Map<String,Object>>();
							List<Boolean> multiVisitPackageList = new ArrayList<Boolean>();

							BasicDynaBean docConsBean = null;
							Timestamp currentTimeStamp = DateUtil.getCurrentTimestamp();
							Map channelingApptBean = (Map)((List)map.get("appointmentDetailsList")).get(0);

							docConsBean = doctorDao.getBean();
							docConsBean.set("doctor_name", channelingOrder.get("activity_id").toString());
							docConsBean.set("operation_ref", null);
							docConsBean.set("status", "U");
							docConsBean.set("presc_date", currentTimeStamp);
							docConsBean.set("visited_date", (java.sql.Timestamp)channelingApptBean.get("appointment_date_time"));
							docConsBean.set("head", channelingApptBean.get("consultation_type_id").toString());
							docConsBean.set("presc_doctor_id", channelingApptBean.get("presc_doc_id"));
							orders.add(docConsBean);
						    multiVisitPackageList.add(new Boolean(true));

							Integer pat_package_id = (Integer)schedulerAppBean.get("pat_package_id");
							if(null != newMultiVisitPackageOrdersBean) {
								if(pat_package_id != -1) {
					        	    visitSuccess &= orderBo.orderMultiVisitPackageForChannelling(con, newMultiVisitPackageOrdersBean, pat_package_id) == null;
					        	    visitSuccess &= orderBo.updateChannellingMultivisitPackageStatus(con, newMultiVisitPackageOrdersBean, pat_package_id) == null;
					        	    visitSuccess &= (orderBo.orderItems(con, orders, newPreAuths, newPreAuthModes, firstOfCategoryList, condDoctrsList,
											multiVisitPackageList, null, null, null, appointmentId, chargeable, true, true, newSecPreAuths, newSecPreAuthModes,
											operAnaesTypeList, new ArrayList<List<TestDocumentDTO>>()) == null);
								}
							}

							visitSuccess &= regBO.updateBillStatus(con, "Y", bill, false);
							String error = new BillBO().updateBillStatus(con, bill, bill.getStatus(), bill.BILL_PAYMENT_PAID, "Y", currentTimeStamp, userName, false, false, false);

							allSuccess = true;
							allSuccess &= visitSuccess;
		    	    	} else {
		    	    		continue;
		    	    	}
					} else {
						continue;
					}

	    		} catch(Exception e) {
	    		  allSuccess = false;
	    		  throw e;
	    		} finally {
	    			DataBaseUtil.commitClose(con, allSuccess);
	    		}
    		}
    	}

    	FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer").
				                replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    	flash.info("Transaction Successful");
		return redirect;
	}

	public Map getChannelingOrders(Integer appointmentId) throws SQLException,IOException{

        Map<String, Object> map = new HashMap<String, Object>();
        if (appointmentId != null && !appointmentId.equals("")) {
        	BasicDynaBean apptBean = schedulerAppointmentsDAO.findByKey("appointment_id", appointmentId);
        	List appointmentDetails = ResourceDAO.getAppointmentDetails(appointmentId);
        	if (appointmentDetails != null && appointmentDetails.size() > 0) {
        		BasicDynaBean appointBean = (BasicDynaBean)appointmentDetails.get(0);
        		String status = (String)appointBean.get("appointment_status");
            map.put("status", false);
        	} else {
        		map.put("status", false);
        		map.put("appointmentDetailsList", null);
        	}
        }
        return map;
    }

	private BasicDynaBean getVisitDetailsBean(String patientId, BasicDynaBean schedulerAppBean,
			BasicDynaBean visitDetailsBean, Map map)throws SQLException {

		visitDetailsBean.set("patient_id", patientId);
	    visitDetailsBean.set("bed_type", "GENERAL");
	    visitDetailsBean.set("op_type", "M");
	    visitDetailsBean.set("visit_type", "o");
		visitDetailsBean.set("reg_date", new java.sql.Date((new java.util.Date()).getTime()));
		visitDetailsBean.set("reg_time", new java.sql.Time((new java.util.Date()).getTime()));
		visitDetailsBean.set("status", "A");
    	visitDetailsBean.set("center_id", (Integer)schedulerAppBean.get("center_id"));
    	visitDetailsBean.set("mr_no", schedulerAppBean.get("mr_no"));
    	visitDetailsBean.set("main_visit_id", patientId);
    	visitDetailsBean.set("revisit", "Y");
    	visitDetailsBean.set("org_id", "ORG0001");
		visitDetailsBean.set("dept_name", map.get("dept_id"));
		visitDetailsBean.set("admitted_dept", map.get("dept_id"));
		visitDetailsBean.set("mlc_status", "N");
		visitDetailsBean.set("prior_auth_mode_id", 0);

		return visitDetailsBean;
	}

}
