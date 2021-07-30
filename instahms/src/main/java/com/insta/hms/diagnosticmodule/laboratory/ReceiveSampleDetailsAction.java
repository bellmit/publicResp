/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

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
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flexjson.JSONSerializer;

/**
 * @author krishna
 *
 */
public class ReceiveSampleDetailsAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(ReceiveSampleDetailsAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");

	public ReceiveSamplesDAO dao = new ReceiveSamplesDAO();

	public ActionForward getReceiveSamplesDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {

		BasicDynaBean bean = dao.findByKey("sample_collection_id", Integer.parseInt(request.getParameter("sampleCollectionId")));
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		BasicDynaBean receiveSampleDetailsBean = dao.getReceiveSampleDetails(Integer.parseInt(request.getParameter("sampleCollectionId")));
		BasicDynaBean incomingSampleRegistrationBean = dao.getIncomingSampleRegistrationDetails(request.getParameter("patient_id"));
		request.setAttribute("bean", bean);
		request.setAttribute("diagGenericPref", diagGenericPref);
		request.setAttribute("receiveSampleDetailsBean", receiveSampleDetailsBean);
		request.setAttribute("incomingSampleRegistrationBean", incomingSampleRegistrationBean);
		String receiptUser = (String)request.getSession(false).getAttribute("userid");
		request.setAttribute("receiptUser", receiptUser);

		return mapping.findForward("getReceiveSamplesDetails");
	}

	public ActionForward saveReceiveSamplesDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException, Exception {

		java.util.Date parsedDate = new java.util.Date();
		Timestamp receiveTime = new Timestamp(parsedDate.getTime());
		String samplereceipt = request.getParameter("_sample_receive");
		String receiptUser = (String)request.getSession(false).getAttribute("userid");
		String receiptDate = request.getParameter("receiptDate");
		String receiptTime = request.getParameter("receiptTime");
		String receiptOtherDetails = request.getParameter("receiptOtherDetails");
		int sampleCollectionId = Integer.parseInt(request.getParameter("sampleCollectionId"));
		String patientId = request.getParameter("patient_id");
		
		GenericDAO sampleCollectionDAO = new GenericDAO("sample_collection");
		
		Connection con = null;
		Boolean success = false;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("saveReceiveSamplesDetailsRedirect"));
			BasicDynaBean sampleCollectionBean = sampleCollectionDAO.findByKey("sample_collection_id",sampleCollectionId);
			receiveTime = DateUtil.parseTimestamp(receiptDate+" "+receiptTime);
			if(samplereceipt != null) {
				sampleCollectionBean.set("sample_receive_status", "R");
			} else {
				sampleCollectionBean.set("sample_receive_status", "P");
			}
			sampleCollectionBean.set("receipt_user", receiptUser);
			sampleCollectionBean.set("receipt_time", receiveTime);
			sampleCollectionBean.set("receipt_other_details", receiptOtherDetails);
			Map<String,Object> keys = new HashMap<String, Object>();
			keys.put("sample_collection_id", sampleCollectionId);
			int j= sampleCollectionDAO.update(con, sampleCollectionBean.getMap(), keys);
			if(j>0) success = true;
			redirect.addParameter("sampleCollectionId", request.getParameter("sampleCollectionId"));
			redirect.addParameter("patient_id", patientId);
			return redirect;

		}finally{
			DataBaseUtil.commitClose(con, success);
		}
	}


}
