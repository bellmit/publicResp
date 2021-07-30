package com.insta.hms.payments;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;


public class AutopostPaymentsAction extends DispatchAction {
	
	static PaymentsDAO paymentsDao = new PaymentsDAO();

	@IgnoreConfidentialFilters
	public ActionForward getAutoPaymentScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception {


		return mapping.findForward("autopaymentscreen");
	}

	public ActionForward postPayments(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception {

		String[] paymentType = request.getParameterValues("_paymentType");
		List<String> paymentTypeList = new ArrayList<String>();
		if (paymentType != null) {
			 paymentTypeList = Arrays.asList(paymentType);
		}


		String userId = (String) request.getSession(false).getAttribute("userid");
		boolean success = true;

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("autopaymentredirect"));
		FlashScope flash = FlashScope.getScope(request);
		redirect.addParameter(FlashScope.FLASH_SCOPE, flash.key());
		Map param = request.getParameterMap();
		Map FilterMap = new HashMap();

		String[] bill_status = request.getParameterValues("bill_status");
		if (bill_status != null) {
			for (int i=0; i<bill_status.length; i++) {
				redirect.addParameter("bill_status", bill_status[i]);
			}
			FilterMap.put("b.status", bill_status);
			FilterMap.put("b.status@op", new String[] {"in"});
		}

		String[] visit_type = request.getParameterValues("visit_type");
		if (visit_type != null) {
			for (int i=0; i<visit_type.length; i++) {
				redirect.addParameter("visit_type", visit_type[i]);

			}
			FilterMap.put("b.visit_type", visit_type);
			FilterMap.put("b.visit_type@op", new String[] {"in"});
		}
		String[] insurancestatus = request.getParameterValues("insurancestatus");
		if (insurancestatus != null) {
			String[] tpaId = new String[insurancestatus.length];
			for (int i=0; i<insurancestatus.length; i++) {
				redirect.addParameter("insurancestatus", insurancestatus[i]);
				if (insurancestatus[i].equals("Y"))
					tpaId[i] = "true";
				if (insurancestatus[i].equals("N"))
					tpaId[i] = "false";
				if (insurancestatus[i].equals(""))
					tpaId[i] = "";
			}
			FilterMap.put("b.is_tpa", tpaId);
			FilterMap.put("b.is_tpa@op", new String[] {"in"});
			FilterMap.put("b.is_tpa@type", new String[] {"boolean"});
			FilterMap.put("b.is_tpa@cast", new String[] {"y"});
		}
		String[] _paymentType = request.getParameterValues("_paymentType");
		if (_paymentType != null) {
			for (int i=0; i<_paymentType.length; i++)
				redirect.addParameter("_paymentType", _paymentType[i]);
			FilterMap.put("_paymentType", _paymentType);
		}
		String[] bc_posted_date = request.getParameterValues("bc_posted_date");
		if (bc_posted_date != null) {
			for (int i=0; i<bc_posted_date.length; i++) {
				redirect.addParameter("bc_posted_date", bc_posted_date[i]);
			}
			FilterMap.put("bc.posted_date", bc_posted_date);
			FilterMap.put("bc.posted_date@op", new String[] {"ge","le"});
			FilterMap.put("bc.posted_date@type", new String[] {"date"});
			FilterMap.put("bc.posted_date@cast", new String[] {"y"});

		}
		String[] b_finalized_date = request.getParameterValues("b_finalized_date");
		if (b_finalized_date != null) {
			for (int i=0; i<b_finalized_date.length; i++) {
				redirect.addParameter("b_finalized_date", b_finalized_date[i]);
			}
			FilterMap.put("b.finalized_date", b_finalized_date);
			FilterMap.put("b.finalized_date@op", new String[] {"ge","le"});
			FilterMap.put("b.finalized_date@type", new String[] {"date"});
			FilterMap.put("b.finalized_date@cast", new String[] {"y"});
		}

		String[] b_closed_date = request.getParameterValues("b_closed_date");
		if (b_closed_date != null) {
			for (int i=0; i<b_closed_date.length; i++) {
				redirect.addParameter("b_closed_date", b_closed_date[i]);
			}
			FilterMap.put("b.closed_date", b_closed_date);
			FilterMap.put("b.closed_date@op", new String[] {"ge","le"});
			FilterMap.put("b.closed_date@type", new String[] {"date"});
			FilterMap.put("b.closed_date@cast", new String[] {"y"});
		}

		String selectedPostedDate = request.getParameter("posting_date_by");
		redirect.addParameter("posting_date_by", selectedPostedDate);

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection(60);
			con.setAutoCommit(false);

			if (paymentTypeList != null && paymentTypeList.contains("")) {
				success &= paymentsDao.saveConductingDoctorsAutoPayments(con,userId, FilterMap, selectedPostedDate);
				success &= paymentsDao.savePrescribingDoctorsAutoPayments(con,userId, FilterMap, selectedPostedDate);
				success &= paymentsDao.saveReferralDoctorsAutoPayments(con,userId, FilterMap, selectedPostedDate);
			}

			if (paymentTypeList != null && paymentTypeList.contains("C")) {
				success &= paymentsDao.saveConductingDoctorsAutoPayments(con,userId, FilterMap, selectedPostedDate);
			}

			if (paymentTypeList != null && paymentTypeList.contains("P")) {
				success &= paymentsDao.savePrescribingDoctorsAutoPayments(con, userId, FilterMap, selectedPostedDate);
			}

			if (paymentTypeList != null && paymentTypeList.contains("R")) {
				success &= paymentsDao.saveReferralDoctorsAutoPayments(con, userId, FilterMap, selectedPostedDate);
			}
		if(success) {
		  flash.put("success", "Successfully posted payments");
		} else {
		  flash.put("error", "Unable to post payments");
		}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		return redirect;
	}


}
