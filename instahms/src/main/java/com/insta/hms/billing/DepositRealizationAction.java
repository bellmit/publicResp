/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.usermanager.UserDashBoardDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lakshmi.p
 *
 */
public class DepositRealizationAction extends BaseAction {


	@IgnoreConfidentialFilters
	public ActionForward getDepositRealizationScreen(ActionMapping am, ActionForm af,
				HttpServletRequest req,HttpServletResponse res) throws Exception {

		Map map = getParameterMap(req);

		DepositRealizationDAO dao = new DepositRealizationDAO();
		Map listingParams = ConversionUtils.getListingParameter(map);
		
		String dateRange = req.getParameter("date_range");
		String startDate = null;
		if (dateRange != null && dateRange.equals("month")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.MONTH, -1);
	        Date openDt = cal.getTime();
	        startDate = dateFormat.format(openDt);

	        map.put("deposit_date", new String[]{startDate,""});
	        map.remove("date_range");
		}

		PagedList list = dao.getDepositRealizationList(map, listingParams);
		req.setAttribute("pagedList", list);
		List allUserNames = UserDashBoardDAO.getAllUserNames();
		req.setAttribute("userNameList", new JSONSerializer().serialize(allUserNames));
		ActionForward forward = new ActionForward(am.findForward("DepositRealizationList.jsp").getPath());
		if (dateRange != null && dateRange.equals("month") && req.getParameter("deposit_date") == null) {
			addParameter("deposit_date", startDate, forward);
	    }
		return forward;
	}

	@IgnoreConfidentialFilters
	public ActionForward saveRealized(ActionMapping am, ActionForm af,
			HttpServletRequest req,HttpServletResponse res) throws Exception {

		FlashScope flash = FlashScope.getScope(req);
		String[] realizeDepositChecks = req.getParameterValues("_realizeDeposit");

		String error = null;
		if (realizeDepositChecks.length > 0) {
			error = DepositRealizationDAO.realizeDeposits(realizeDepositChecks);
			
			try (Connection con = DataBaseUtil.getConnection()) {
			  ReceiptRelatedDAO receiptDao = new ReceiptRelatedDAO(con);
			  GenericDAO genericDao = new GenericDAO("receipts");
  			// Have to trigger this as the trigger was dropped.
  			for(String depositNo : realizeDepositChecks) {
  			// To Update the Deposit Setoff Total Table
  			  boolean success = false;
  			  BasicDynaBean deposit = genericDao.findByKey("receipt_id",depositNo);
  			  String mrNo = (String) deposit.get("mr_no");
          BigDecimal setOffAmount = BigDecimal.ZERO;
          setOffAmount = receiptDao.getSetoffAmount(mrNo);
          BigDecimal totalTaxSetOffAmount = receiptDao.getTotalTaxSetOffAmount(con,mrNo);
          BigDecimal totalDepositAmount = BigDecimal.ZERO;
          totalDepositAmount = receiptDao.getTotalDeposit(mrNo);
          BigDecimal totalDepositTaxAmount = receiptDao.getTotalDepositTaxAmount(con,mrNo);
          success = receiptDao.createDepositSetoffTotal(mrNo, totalDepositAmount, setOffAmount,totalDepositTaxAmount, totalTaxSetOffAmount);
  			}
			}
		}

		ActionRedirect redirect = new ActionRedirect(am.findForward("getDepositRealizationList"));
		redirect.addParameter("deposit_date",req.getParameterValues("deposit_date")[0]);
		redirect.addParameter("deposit_date",req.getParameterValues("deposit_date")[1]);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (error != null) {
			flash.put("error", error);
		} else {
			flash.put("success", "Deposit realization successful... ");
		}
		return redirect;
	}
}
