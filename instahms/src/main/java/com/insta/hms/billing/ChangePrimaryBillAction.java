package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChangePrimaryBillAction extends DispatchAction {


	public ActionForward getScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException {
		String referer = req.getHeader("Referer");
		req.setAttribute("refererUrl", referer);

		String billNo = req.getParameter("billNo");
		String isNewUX = req.getParameter("isNewUX");
		String isNewUXSuccess = req.getParameter("isNewUXSuccess");
		req.setAttribute("isNewUX", isNewUX);
		req.setAttribute("isNewUXSuccess", isNewUXSuccess);
		BasicDynaBean billBean = BillDAO.getBillBean(billNo);
		req.setAttribute("bill", billBean.getMap());
		return m.findForward("changebillprimary");
	}

	public ActionForward changePrimaryBill(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {

		String billNo = req.getParameter("billNo");
		String primary = req.getParameter("primary");
		String isNewUX = req.getParameter("isNewUX");

		Connection con = null;
		boolean success = false;
		FlashScope flash = FlashScope.getScope(req);

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BillDAO bDao = new BillDAO(con);

			Bill bill = bDao.getBill(billNo);
			String visitId = bill.getVisitId();
			BasicDynaBean visitbean = new VisitDetailsDAO().findByKey("patient_id", visitId);
			String useDRG = (visitbean != null && visitbean.get("use_drg") != null) ? (String)visitbean.get("use_drg") : "N";
    		Map drgCodeMap = new MRDUpdateScreenBO().getDRGCode(visitId);

    		String usePerdiem = (visitbean != null && visitbean.get("use_perdiem") != null) ? (String)visitbean.get("use_perdiem") : "N";
    		Map perdiemCodeMap = new MRDUpdateScreenBO().getPerdiemCode(visitId);

			if (primary.equals("Y")) {
				int count = bDao.setPrimaryBillConditional(billNo);
				if (count != 1) {
					// error: maybe another primary bill exists. Stay in the same screen
					String error = "Error: A Primary Bill Later bill already exists for this patient.";
					flash.put("error", error);
					ActionRedirect redirect = new ActionRedirect(m.findForward("selfRedirect"));
					redirect.addParameter("billNo", billNo);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}

				if (useDRG.equals("Y") && drgCodeMap != null
							&& drgCodeMap.get("drg_code") != null && !drgCodeMap.get("drg_code").equals("")
							&& !billNo.equals((String)drgCodeMap.get("drg_bill_no"))) {
					String error = "Error: Patient has DRG code required. Primary bill :"
									+(String)drgCodeMap.get("drg_bill_no")+" has DRG Code. </br> " +
    								" Cancel DRG Code to make this bill as primary bill.";
					flash.put("error", error);
					ActionRedirect redirect = new ActionRedirect(m.findForward("selfRedirect"));
					redirect.addParameter("billNo", billNo);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
		    	}

				if (usePerdiem.equals("Y") && perdiemCodeMap != null
							&& perdiemCodeMap.get("per_diem_code") != null && !perdiemCodeMap.get("per_diem_code").equals("")
							&& !billNo.equals((String)perdiemCodeMap.get("perdiem_bill_no"))) {
					String error = "Error: Patient has Perdiem Code required. Primary bill :"
									+(String)perdiemCodeMap.get("perdiem_bill_no")+" has Perdiem Code. </br> " +
									" Cancel Perdiem Code to make this bill as primary bill.";
					flash.put("error", error);
					ActionRedirect redirect = new ActionRedirect(m.findForward("selfRedirect"));
					redirect.addParameter("billNo", billNo);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
		    	}

			} else {

				if (useDRG.equals("Y") && drgCodeMap != null
						&& drgCodeMap.get("drg_code") != null && !drgCodeMap.get("drg_code").equals("")
						&& billNo.equals((String)drgCodeMap.get("drg_bill_no"))) {
					String error = "Error: Patient has DRG code required. This primary bill has DRG Code. </br> " +
									" Cancel DRG Code to make this bill as secondary bill.";
					flash.put("error", error);
					ActionRedirect redirect = new ActionRedirect(m.findForward("selfRedirect"));
					redirect.addParameter("billNo", billNo);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
		    	}

				if (usePerdiem.equals("Y") && perdiemCodeMap != null
						&& perdiemCodeMap.get("per_diem_code") != null && !perdiemCodeMap.get("per_diem_code").equals("")
						&& billNo.equals((String)perdiemCodeMap.get("perdiem_bill_no"))) {
					String error = "Error: Patient has Perdiem Code required. This primary bill has Perdiem Code. </br> " +
									" Cancel Perdiem Code to make this bill as secondary bill.";
					flash.put("error", error);
					ActionRedirect redirect = new ActionRedirect(m.findForward("selfRedirect"));
					redirect.addParameter("billNo", billNo);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
		    	}

				String bedFinalized = OrderDAO.isBillBedDetailsFinalized(visitId, billNo);
				String error = null;

				if (bedFinalized != null && bedFinalized.equals("Not Finalized")) {
					// error: Unfinalized beds may exist in the Primary bill. Stay in the same screen
					error = "Error: Cannot change to secondary bill. Bed finalization has to be done.";
					flash.put("error", error);
					ActionRedirect redirect = new ActionRedirect(m.findForward("selfRedirect"));
					redirect.addParameter("billNo", billNo);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}

				bDao.updatePrimaryBill(billNo, "N");
			}

			success = true;

		} finally {
			DataBaseUtil.commitClose(con, success);

			if (success) {
				 if (billNo != null && !billNo.equals(""))
					 BillDAO.resetTotalsOrReProcess(billNo);
			 }
		}
		if (isNewUX.equals("Y")) {
			ActionRedirect redirect = new ActionRedirect(m.findForward("selfRedirect"));
			redirect.addParameter("billNo", billNo);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("isNewUXSuccess", "Y");
			return redirect;
		}
		// success: go to the bill screen
		ActionRedirect redirect = new ActionRedirect(m.findForward("goToBill"));
		redirect.addParameter("billNo", billNo);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}

