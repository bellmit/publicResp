package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationRepository;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.billing.ReceiptModel;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.ForeignCurrency.ForeignCurrencyDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ReceiptRefundPrintTemplate.ReceiptRefundPrintTemplateDAO;
import com.insta.hms.mdm.paymentmode.PaymentModeMasterModel;
import com.insta.hms.security.usermanager.UUserModel;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ReceiptsAction extends BaseAction {

  static Logger logger = LoggerFactory.getLogger(ReceiptsAction.class);

	private final AllocationRepository allocationRepository = (AllocationRepository) ApplicationContextProvider
	    .getApplicationContext().getBean("allocationRepository");
	
	private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
	      .getApplicationContext().getBean("allocationService");
	
	private GenericDAO billReceiptsDAO = new GenericDAO("bill_receipts");
	
	private static final GenericDAO sampleCollectionCentersDAO = new GenericDAO("sample_collection_centers");

  private final AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);

	@IgnoreConfidentialFilters
	public ActionForward getReceipts(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		ReceiptRefundPrintTemplateDAO rrdao = new ReceiptRefundPrintTemplateDAO();
		String category = mapping.getProperty("category");
		Map map = getParameterMap(req);

		if (category.equals("pharmacy")) {
			map.put("counter_type", new String[]{"P"});
		}
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
			map.put("center_id", new String[]{centerId+""});
			map.put("center_id@type", new String[]{"integer"});
		}
		map.remove("collectionCenterId");
		String collectionCenterId = req.getParameter("collectionCenterId");
		int userSampleCollectionCenterId = (Integer) req.getSession(false).getAttribute("sampleCollectionCenterId");
		if(null != collectionCenterId && !collectionCenterId.equals("")) {
			map.put("collection_center_id", new String[]{collectionCenterId+""});
			map.put("collection_center_id@type", new String[]{"integer"});
		} else {
			if(userSampleCollectionCenterId != -1){
				map.put("collection_center_id", new String[]{userSampleCollectionCenterId+""});
				map.put("collection_center_id@type", new String[]{"integer"});
			}
		}
	
		String[] paymentType = req.getParameterValues("payment_type");
		if (!ArrayUtils.contains(paymentType,"S")) {
		  map.put("tpa_id", new String[]{""});
		  map.put("tpa_id@cast", new String[]{"y"});
		}
		
		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		req.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		req.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

		JSONSerializer json = new JSONSerializer().exclude("class");
		PagedList list = ReceiptRelatedDAO.searchReceiptsExtended(map,
				ConversionUtils.getListingParameter(map));

		req.setAttribute("pagedList", list);
		BasicDynaBean printPref = PrintConfigurationsDAO.getPrintMode("Bill");
		req.setAttribute("pref",printPref);

		req.setAttribute("templateList", rrdao.getTemplateList());
		req.setAttribute("jsonReceiptsTempList", json.serialize(rrdao.getTemplateNames()));
		req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());

		req.setAttribute("category", category);
		return mapping.findForward("showlist");
	}

	@IgnoreConfidentialFilters
	public ActionForward getScreen(ActionMapping mapping, ActionForm f,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		ReceiptRefundPrintTemplateDAO rrdao = new ReceiptRefundPrintTemplateDAO();
		JSONSerializer json = new JSONSerializer().exclude("class");
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");

		List<BasicDynaBean> collectionCenters = sampleCollectionCentersDAO.findAllByKey("center_id", centerId);
		request.setAttribute("collectionCenters", collectionCenters);
		BasicDynaBean colcenterBean = sampleCollectionCentersDAO.findByKey("collection_center_id", -1);
		request.setAttribute("defautlCollectionCenter", colcenterBean.get("collection_center"));

		request.setAttribute("templateList", rrdao.getTemplateList());
		request.setAttribute("jsonReceiptsTempList", json.serialize(rrdao.getTemplateNames()));
		String category = mapping.getProperty("category");
		request.setAttribute("category", category);
		return mapping.findForward("showlist");
	}

	public ActionForward getReceipt(ActionMapping mapping, ActionForm f,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String receiptNo;
		String screen = request.getParameter("screen");
		if(null != screen && screen.equalsIgnoreCase("depositReceipts"))
			receiptNo = request.getParameter("deposit_no");
		else
			receiptNo = request.getParameter("receiptNo");
		
		if (null == receiptNo || receiptNo.trim().isEmpty()) {
			request.setAttribute("error", "Receipt number is empty, please contact admin.");
			request.setAttribute("screen", screen);
			return mapping.findForward("editReceiptScreen");
		} else {
					receiptNo = receiptNo.trim();
		}
	
		if(null != screen && !screen.isEmpty()){
      BasicDynaBean receiptBean = null;
      int centerId = (Integer) request.getSession(false).getAttribute("centerId");
      if (centerId == 0) {
        receiptBean = ReceiptRelatedDAO.getReceiptDetails(receiptNo,
            screen.equals("billReceipts"));
      } else {
        receiptBean = ReceiptRelatedDAO.getReceiptDetailsCenterwise(receiptNo, centerId,
            screen.equals("billReceipts"));
      }
	    if (receiptBean == null) {
	      request.setAttribute("error", "There is no receipt found with number: " + receiptNo+ " in logged-in center");
	      request.setAttribute("screen", screen);
	      return mapping.findForward("editReceiptScreen");
	    }
	    String paymentType = receiptBean.get("payment_type").toString();
	    if(paymentType.equals("R") || paymentType.equals("F") || paymentType.equals("S"))
	    {
	      String billNo = receiptBean.get("bill_no").toString();
	      Bill bill = new BillBO().getBill(billNo);
	      request.setAttribute("bill", bill);
	    }
	    request.setAttribute("bean", receiptBean);
	    request.setAttribute("screen", screen);
		}
		
		request.setAttribute("paymentModesJSON", new JSONSerializer().serialize(
				ConversionUtils.listBeanToListMap(new PaymentModeMasterDAO().listAll())));

		request.setAttribute("foreignCurrencyList", new ForeignCurrencyDAO().listAll());
		request.setAttribute("foreignCurrencyListJSON", js.serialize(
					ConversionUtils.copyListDynaBeansToMap(new ForeignCurrencyDAO().listAll(null, "status","A"))));
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("getAllCreditTypes", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new CardTypeMasterDAO().listAll(null,"status","A",null))));

		return mapping.findForward("editReceiptScreen");
	}

	public ActionForward updateReceiptDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,  
      HttpServletResponse response) throws Exception {  
    Connection con = null;  
    Boolean success = false;  

    try { 
      con = DataBaseUtil.getConnection(); 
      con.setAutoCommit(false); 

      String receiptNo = request.getParameter("receiptNo"); 
      String paymentType = request.getParameter("paymentType");
      String billNo = request.getParameter("billNo");

      String displaydate = request.getParameter("display_date");  
      String displaytime = request.getParameter("display_time");  
      Timestamp displayDateTime = DateUtil.getCurrentTimestamp(); 
      displayDateTime = DateUtil.parseTimestamp(displaydate +" "+ displaytime); 

      BasicDynaBean receiptBean = null; 
      /*  
       * If the receipt belongs to patient deposits then  
       *  receiptPaymentType is Deposit.  
       * If receiptPaymentType is not Deposit then the receipt/refund is for bill payments. 
       */ 
      String receiptPaymentType = request.getParameter("receiptPaymentType"); 

       
      receiptBean = billReceiptsDAO.getBean();  

      ReceiptModel receipt = (ReceiptModel) allocationRepository.get(ReceiptModel.class, receiptNo);  

      if (paymentType.equalsIgnoreCase(Receipt.PATIENT_ADVANCE) 
          || paymentType.equalsIgnoreCase(Receipt.PATIENT_SETTLEMENT)) {  

        receipt.setIsSettlement(paymentType.equalsIgnoreCase(Receipt.PATIENT_SETTLEMENT));  
        receipt.setReceiptType("R");
      } else if (paymentType.equalsIgnoreCase(Receipt.REFUND)) {  
        receipt.setReceiptType("F");  
      } else if (paymentType.equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_ADVANCE)) { 
        receiptBean.set("sponsor_index","P"); 
        receipt.setIsSettlement(false); 

      } else if (paymentType.equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_SETTLEMENT)) {  
        receipt.setIsSettlement(true);  
        receiptBean.set("sponsor_index","P"); 

      }else if (paymentType.equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_ADVANCE)) {  
        receipt.setIsSettlement(false); 
        receiptBean.set("sponsor_index","S"); 

      } else if (paymentType.equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_SETTLEMENT)) {  
        receipt.setIsSettlement(true);  
        receiptBean.set("sponsor_index","S"); 
      } 
      receipt.setDisplayDate(displayDateTime);  

      if(request.getParameter("tds_amt") != null && !request.getParameter("tds_amt").equals("")) {  
        receipt.setTdsAmount(new BigDecimal(request.getParameter("tds_amt").equals("") ? "0" : request.getParameter("tds_amt"))); 
      } 
      receiptBean.set("receipt_no", receiptNo); 
      setReceiptDetails(request, receiptBean, receipt); 

      allocationRepository.update(receipt);
      success = true;
      
      BasicDynaBean bean = billReceiptsDAO.findByKey("receipt_no", receiptNo);
      if(bean != null) {
        // Update the bill total amount.
        allocationService.updateBillTotal(bean.get("bill_no").toString());
        // Call the allocation method here
        Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");
        allocationService.allocate(bean.get("bill_no").toString(), centerId);
      }

      // Schedule accounting for receipt
      Map<String, Object> receiptData = new HashMap<>();
      receiptData.put("receiptId", receiptNo);
      receiptData.put("reversalsOnly", Boolean.FALSE);
      if (receipt.getIsDeposit() && StringUtils.isNotEmpty(billNo) && bean != null) {
        receiptData.put("setOffBillNo", billNo);
        receiptData.put("setOffType", paymentType.equalsIgnoreCase(Receipt.REFUND) ? "F" : "R");
      }
      accountingJobScheduler.scheduleAccountingForReceipt(receiptData);

      ActionRedirect redirect = new ActionRedirect("/billing/editReceipt.do?_method=getReceipt");
      if(receiptPaymentType != null && receiptPaymentType.equals("Deposit")) {
        redirect.addParameter("deposit_no", request.getParameter("receiptNo"));  
      }
      redirect.addParameter("receiptNo", request.getParameter("receiptNo")); 
      redirect.addParameter("screen", request.getParameter("screen"));  
      return redirect;  

    } finally { 
      DataBaseUtil.commitClose(con, success); 
    } 
  }
	
	public static void setReceiptDetails(HttpServletRequest request, BasicDynaBean receiptBean)
	    throws SQLException, ParseException  {

	      String modeId = request.getParameter("payment_mode_id");
	      BasicDynaBean payBean =new PaymentModeMasterDAO().findByKey("mode_id", Integer.parseInt(modeId));
	      HttpSession session=request.getSession();
	      String userId = (String)session.getAttribute("userid");
	      receiptBean.set("username", userId);
	      receiptBean.set("mod_time", DateUtil.getCurrentTimestamp());
	      receiptBean.set("counter", request.getParameter("counter"));
	      receiptBean.set("amount",  new BigDecimal(request.getParameter("amount").equals("") ? "0" : request.getParameter("amount")));
	      receiptBean.set("payment_mode_id", Integer.parseInt(modeId));
	      
	      if (!request.getParameter("commissionPercent").equals("")  && !request.getParameter("commissionAmount").equals("")) {
	        receiptBean.set("credit_card_commission_percentage",new BigDecimal(request.getParameter("commissionPercent")));
	        receiptBean.set("credit_card_commission_amount",new BigDecimal(request.getParameter("commissionAmount")));
	      }else{
	        receiptBean.set("credit_card_commission_percentage",BigDecimal.ZERO);
	        receiptBean.set("credit_card_commission_amount",BigDecimal.ZERO);
	      }
	      
	      if(request.getParameter("remarks")!= null && !request.getParameter("remarks").equals("")) {
	        receiptBean.set("remarks", request.getParameter("remarks"));
	      }else {
	        receiptBean.set("remarks", null);
	      }

	      if(request.getParameter("paid_by") != null && !request.getParameter("paid_by").equals("")) {
	        receiptBean.set("paid_by", request.getParameter("paid_by"));
	      }else {
	        receiptBean.set("paid_by", null);
	      }

	      if(request.getParameter("currency_id") != null && !request.getParameter("currency_id").equals("")) {
	        if(Integer.parseInt(request.getParameter("currency_id")) > 0) {
	          receiptBean.set("currency_id", Integer.parseInt(request.getParameter("currency_id")));
	        }
	      }else {
	        receiptBean.set("currency_id", null);
	      }

	      if(request.getParameter("currency_amt") != null) {
	        receiptBean.set("currency_amt", new BigDecimal(request.getParameter("currency_amt").equals("") ? "0" : request.getParameter("currency_amt")));
	      }else {
	        receiptBean.set("currency_amt", null);
	      }

	      if(request.getParameter("exchange_date") != null && !request.getParameter("exchange_date").equals("")) {
	        String exchangedate = request.getParameter("exchange_date");
	        String exchangetime = request.getParameter("exchange_time");
	        Timestamp exchangeDateTime = DateUtil.getCurrentTimestamp();
	        exchangeDateTime = DateUtil.parseTimestamp(exchangedate +" "+ exchangetime);
	        receiptBean.set("exchange_date", exchangeDateTime);
	      }else {
	        receiptBean.set("exchange_date", null);
	      }

	      if(request.getParameter("exchange_rate") != null) {
	        receiptBean.set("exchange_rate", new BigDecimal(request.getParameter("exchange_rate").equals("") ? "0" : request.getParameter("exchange_rate")));
	      }else {
	        receiptBean.set("exchange_rate", null);
	      }

	      if (payBean != null) {
	        if (payBean.get("bank_required") != null && ((String)payBean.get("bank_required")).equals("Y")
	            && request.getParameter("bank_name") != null && !request.getParameter("bank_name").equals("")) {
	          receiptBean.set("bank_name", request.getParameter("bank_name"));
	        }else {
	          receiptBean.set("bank_name", null);
	        }
	        if (payBean.get("ref_required") != null && ((String)payBean.get("ref_required")).equals("Y")
	            && request.getParameter("reference_no") != null && !request.getParameter("reference_no").equals("")) {
	          receiptBean.set("reference_no", request.getParameter("reference_no"));
	        }else {
	          receiptBean.set("reference_no", null);
	        }
	        if (payBean.get("card_type_required") != null && ((String)payBean.get("card_type_required")).equals("Y")
	            && request.getParameter("card_type_id") != null && !request.getParameter("card_type_id").equals("")) {
	          receiptBean.set("card_type_id", Integer.parseInt(request.getParameter("card_type_id")));
	        }else {
	          receiptBean.set("card_type_id", null);
	        }
	        if (payBean.get("bank_batch_required") != null && ((String)payBean.get("bank_batch_required")).equals("Y")
	            && request.getParameter("bank_batch_no") != null && !request.getParameter("bank_batch_no").equals("")) {
	          receiptBean.set("bank_batch_no", request.getParameter("bank_batch_no"));
	        }else {
	          receiptBean.set("bank_batch_no", null);
	        }
	        if (payBean.get("card_auth_required") != null && ((String)payBean.get("card_auth_required")).equals("Y")
	            && request.getParameter("card_auth_code") != null && !request.getParameter("card_auth_code").equals("")) {
	          receiptBean.set("card_auth_code", request.getParameter("card_auth_code"));
	        }else {
	          receiptBean.set("card_auth_code", null);
	        }
	        if (payBean.get("card_holder_required") != null && ((String)payBean.get("card_holder_required")).equals("Y")
	            && request.getParameter("card_holder_name") != null &&  !request.getParameter("card_holder_name").equals("")) {
	          receiptBean.set("card_holder_name",request.getParameter("card_holder_name"));

	        }else {
	          receiptBean.set("card_holder_name", null);
	        }
	        if (payBean.get("card_number_required") != null && ((String)payBean.get("card_number_required")).equals("Y")
	            && request.getParameter("card_number") != null && !request.getParameter("card_number").equals("")) {
	          receiptBean.set("card_number", request.getParameter("card_number"));

	        }else {
	          receiptBean.set("card_number", null);
	        }
	        if (payBean.get("card_expdate_required") != null && ((String)payBean.get("card_expdate_required")).equals("Y")
	            && request.getParameter("card_expdate") != null && !request.getParameter("card_expdate").equals("")) {

	          String dateSeparator = request.getParameter("card_expdate").contains("/") ? "/" : "-";
	          String[] arr = request.getParameter("card_expdate").split(dateSeparator);
	          int year = DateUtil.convertTwoDigitYear(Integer.parseInt(arr[1]), "future");

	          receiptBean.set("card_expdate",DateUtil.parseDate(arr[0]+dateSeparator+year, dateSeparator, "short"));

	        }else {
	          receiptBean.set("card_expdate", null);
	        }
	      }
	    }

	public static void setReceiptDetails(HttpServletRequest request, BasicDynaBean receiptBean, ReceiptModel receipt)
	throws SQLException, ParseException  {

		String modeId = request.getParameter("payment_mode_id");
		BasicDynaBean payBean =new PaymentModeMasterDAO().findByKey("mode_id", Integer.parseInt(modeId));
		HttpSession session=request.getSession();
		String userId = (String)session.getAttribute("userid");
		receipt.setModifiedBy(new UUserModel(userId));
		receipt.setModifiedAt(new Date());
		receipt.setCounter(request.getParameter("counter"));
		// Update amount only if it is not a refund receipt.
		if(!(Receipt.MAIN_TYPE_REFUND.equals(receipt.getReceiptType()))) {
		  BigDecimal amount = new BigDecimal(request.getParameter("amount").equals("") ? "0" : request.getParameter("amount"));
		  receipt.setAmount(amount);
		}
		receipt.setPaymentModeId(new PaymentModeMasterModel(Integer.parseInt(modeId)));
		receiptBean.set("username", userId);
		receiptBean.set("mod_time", DateUtil.getCurrentTimestamp());
		
		if (!request.getParameter("commissionPercent").equals("")  && !request.getParameter("commissionAmount").equals("")) {
		  receipt.setCreditCardCommissionPercentage(new BigDecimal(request.getParameter("commissionPercent")));
		  receipt.setCreditCardCommissionAmount(new BigDecimal(request.getParameter("commissionAmount")));
		}else{
		  receipt.setCreditCardCommissionPercentage(BigDecimal.ZERO);
		  receipt.setCreditCardCommissionAmount(BigDecimal.ZERO);
		}
		
		if(request.getParameter("remarks")!= null && !request.getParameter("remarks").equals("")) {
		  receipt.setRemarks(request.getParameter("remarks"));
		}

		if(request.getParameter("paid_by") != null && !request.getParameter("paid_by").equals("")) {
		  receipt.setPaidBy(request.getParameter("paid_by"));
		}

		if(request.getParameter("currency_id") != null && !request.getParameter("currency_id").equals("")) {
			if(Integer.parseInt(request.getParameter("currency_id")) > 0) {
			  receipt.setCurrencyId(Integer.parseInt(request.getParameter("currency_id")));
			}
		}

		if(request.getParameter("currency_amt") != null) {
		  receipt.setCurrencyAmt(new BigDecimal(request.getParameter("currency_amt").equals("") ? "0" : request.getParameter("currency_amt")));
		}

		if(request.getParameter("exchange_date") != null && !request.getParameter("exchange_date").equals("")) {
			String exchangedate = request.getParameter("exchange_date");
			String exchangetime = request.getParameter("exchange_time");
			Timestamp exchangeDateTime = DateUtil.getCurrentTimestamp();
			exchangeDateTime = DateUtil.parseTimestamp(exchangedate +" "+ exchangetime);
			receipt.setExchangeDate(exchangeDateTime);
		}

		if(request.getParameter("exchange_rate") != null) {
		  receipt.setExchangeRate(new BigDecimal(request.getParameter("exchange_rate").equals("") ? "0" : request.getParameter("exchange_rate")));
		}

		if (payBean != null) {
			if (payBean.get("bank_required") != null && ((String)payBean.get("bank_required")).equals("Y")
					&& request.getParameter("bank_name") != null && !request.getParameter("bank_name").equals("")) {
			  receipt.setBankName(request.getParameter("bank_name"));
			}
			if (payBean.get("ref_required") != null && ((String)payBean.get("ref_required")).equals("Y")
					&& request.getParameter("reference_no") != null && !request.getParameter("reference_no").equals("")) {
			  receipt.setReferenceNo(request.getParameter("reference_no"));
			}
			if (payBean.get("card_type_required") != null && ((String)payBean.get("card_type_required")).equals("Y")
					&& request.getParameter("card_type_id") != null && !request.getParameter("card_type_id").equals("")) {
			  receipt.setCardTypeId(Integer.parseInt(request.getParameter("card_type_id")));
			}
			if (payBean.get("bank_batch_required") != null && ((String)payBean.get("bank_batch_required")).equals("Y")
					&& request.getParameter("bank_batch_no") != null && !request.getParameter("bank_batch_no").equals("")) {
			  receipt.setBankBatchNo(request.getParameter("bank_batch_no"));
			}
			if (payBean.get("card_auth_required") != null && ((String)payBean.get("card_auth_required")).equals("Y")
					&& request.getParameter("card_auth_code") != null && !request.getParameter("card_auth_code").equals("")) {
			  receipt.setCardAuthCode(request.getParameter("card_auth_code"));
			}
			if (payBean.get("card_holder_required") != null && ((String)payBean.get("card_holder_required")).equals("Y")
					&& request.getParameter("card_holder_name") != null &&  !request.getParameter("card_holder_name").equals("")) {
			  receipt.setCardHolderName(request.getParameter("card_holder_name"));

			}
			if (payBean.get("card_number_required") != null && ((String)payBean.get("card_number_required")).equals("Y")
					&& request.getParameter("card_number") != null && !request.getParameter("card_number").equals("")) {
//				receiptBean.set("card_number", request.getParameter("card_number"));
			  receipt.setCardNumber(request.getParameter("card_number"));

			}
			if (payBean.get("card_expdate_required") != null && ((String)payBean.get("card_expdate_required")).equals("Y")
					&& request.getParameter("card_expdate") != null && !request.getParameter("card_expdate").equals("")) {

				String dateSeparator = request.getParameter("card_expdate").contains("/") ? "/" : "-";
				String[] arr = request.getParameter("card_expdate").split(dateSeparator);
				int year = DateUtil.convertTwoDigitYear(Integer.parseInt(arr[1]), "future");

				receipt.setCardExpDate(DateUtil.parseDate(arr[0]+dateSeparator+year, dateSeparator, "short"));
			}
		}
	}

}

