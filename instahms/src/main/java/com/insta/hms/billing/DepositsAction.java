package com.insta.hms.billing;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.billing.paymentdetails.BillPaymentDetailsImpl;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.patient.billing.DepositMessagingJob;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.CityMaster.CityMasterDAO;
import com.insta.hms.master.CounterMaster.CounterMasterDAO;
import com.insta.hms.master.CountryMaster.CountryMasterDAO;
import com.insta.hms.master.DepositReceiptRefundTemplate.DepositReceiptRefundPrintTemplateDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ReceiptRefundPrintTemplate.ReceiptRefundPrintTemplateDAO;
import com.insta.hms.master.StateMaster.StateMasterDAO;
import com.lowagie.text.DocumentException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;

public class DepositsAction extends BaseAction {

  static Logger log = LoggerFactory.getLogger(DepositsAction.class);
  private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();

  static JobService jobService = JobSchedulingService.getJobService();
  final BillChargeTaxDAO billChargeTaxDao = new BillChargeTaxDAO(); 
  final GenericDAO receiptTaxDao = new GenericDAO("receipt_tax");
  final GenericDAO receiptRefundDAO = new GenericDAO("receipt_refund_reference");
  final GenericDAO receiptsDAO = new GenericDAO("receipts");
  final GenericDAO receiptsTaxDAO = new GenericDAO("receipt_tax");
  final AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  final PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();
  final GenericDAO patpackdao = new GenericDAO("patient_packages");
  private static final GenericDAO multivisitDepositsViewDAO = new GenericDAO("multivisit_deposits_view");

  /*
   * Filtered list of patients and their deposit details
   */
  @IgnoreConfidentialFilters
  public ActionForward getDeposits(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    Map map = getParameterMap(request);

    request.setAttribute("countryList", js.serialize(CountryMasterDAO.getCountryList(false)));
    request.setAttribute("areaList", js.serialize(AreaMasterDAO.getPatientAreaList()));
    request.setAttribute("cityList", js.serialize(CityMasterDAO.getPatientCityList(false)));
    request.setAttribute("stateList", js.serialize(StateMasterDAO.getStateIdName()));
    request.setAttribute("prefs", GenericPreferencesDAO.getGenericPreferences());
    
    PagedList list = DepositsDAO.searchPatients(map, ConversionUtils.getListingParameter(map));
    request.setAttribute("pagedList", list);

    return mapping.findForward("DepositsList");
  }

  /*
   * Get the patient (deposits) search screen
   */
  @IgnoreConfidentialFilters
  public ActionForward getDepositsScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("countryList", js.serialize(CountryMasterDAO.getCountryList(false)));
    request.setAttribute("areaList", js.serialize(AreaMasterDAO.getPatientAreaList()));
    request.setAttribute("cityList", js.serialize(CityMasterDAO.getPatientCityList(false)));
    request.setAttribute("stateList", js.serialize(StateMasterDAO.getStateIdName()));

    return mapping.findForward("DepositsList");
  }

  @IgnoreConfidentialFilters
  public ActionForward collectOrRefundDeposits(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String depositType = null;
    String mrNo = request.getParameter("mrNo");
    depositType = request.getParameter("depositType");
    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    int center_Id = (Integer) request.getSession(false).getAttribute("centerId");
    List<BasicDynaBean> packageDepositsList=null;
    // Fixed as part of Patient Confidentiality
    /*
     * if (mrNo == null) { mrNo = request.getParameter("mrNoR"); depositType = "R";//deposit } if
     * (mrNo == null) { mrNo = request.getParameter("mrNoF"); depositType = "F";//refund to patient
     * }
     */
    if ((mrNo != null) && !mrNo.equals("")) {
      Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrNo + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("saveDepositsRedirect"));
        redirect.addParameter("_method", "collectOrRefundDeposits");
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      //if the Deposit Availability needs to be shown at Center Level
      if ("E".equals(dto.getEnablePatientDepositAvailability())) {
        BasicDynaBean bean = DepositsDAO.getDepositAmounts(mrNo,center_Id);
        request.setAttribute("depositDetails", bean.getMap());
      }else{
        BasicDynaBean bean = DepositsDAO.getDepositAmounts(mrNo);
        request.setAttribute("depositDetails", bean.getMap());
      }
    }
    List<BasicDynaBean> packageDepositBeans=null;
    //if the Package Deposit Availability needs to be shown at Center Level
    if ("E".equals(dto.getEnablePatientDepositAvailability())) {
    	
    	packageDepositBeans = DepositsDAO.getMultiVisitPkgDepositAmounts(mrNo,center_Id);
    	packageDepositsList = ConversionUtils
    			.listBeanToListMap(packageDepositBeans);
    } else {
    	packageDepositBeans = multivisitDepositsViewDAO.listAll(null, "mr_no", mrNo, "package_id");
    	packageDepositsList = ConversionUtils
    			.listBeanToListMap(packageDepositBeans);
    }

    Map<Integer, Map<String, BigDecimal>> patPackageAmount = new HashMap<>();
    if (!packageDepositBeans.isEmpty()) {
      for(BasicDynaBean depositAmount : packageDepositBeans) {
    	Map<String, BigDecimal> amountMap = new HashMap<>();
    	amountMap.put("total_deposits", (BigDecimal) depositAmount.get("total_deposits"));
    	amountMap.put("total_set_offs", (BigDecimal) depositAmount.get("total_set_offs"));
    	patPackageAmount.put((Integer) depositAmount.get("pat_package_id"), amountMap);
    	
      };
    }
    
    // For multivisit packages list
    List<Map<String, Object>> patientMultiVisitPackageDetails = new ArrayList<>();
    List<BasicDynaBean> patPackDetails = DepositsDAO.getMvpPatPackagesList(mrNo);
    for (BasicDynaBean patientPackageBean : patPackDetails) {
      Map<String, Object> mvpDetailsMap = new HashMap<>();
      Map<String, BigDecimal> patPackageDeposits = new HashMap<>();
      Integer patientPackageId = (Integer) patientPackageBean.get("pat_package_id");
      if (patPackageAmount.containsKey(patientPackageId)) {
        patPackageDeposits = patPackageAmount.get(patientPackageId);
      }
      mvpDetailsMap.put("patient_package_id", patientPackageId);
      mvpDetailsMap.put("status", patientPackageBean.get("status"));
      mvpDetailsMap.put("is_discontinued", (String) (Boolean.TRUE.equals(patientPackageBean.get("is_discontinued")) ? "Package Discontinued" : ""));
      mvpDetailsMap.put("package_id", (Integer) patientPackageBean.get("package_id"));
      mvpDetailsMap.put("package_name", (String) patientPackageBean.get("package_name"));
      mvpDetailsMap.put("total_deposits", 
    		(!patPackageDeposits.isEmpty()) ? patPackageDeposits.get("total_deposits") : 0);
      mvpDetailsMap.put("total_set_offs", 
      		(!patPackageDeposits.isEmpty()) ? patPackageDeposits.get("total_set_offs") : 0);
      patientMultiVisitPackageDetails.add(mvpDetailsMap);
    }
    if (depositType == null)
      depositType = request.getParameter("depositType");
    if (depositType == null)
      depositType = (String) request.getAttribute("deposit_type");
    if (null == depositType)
      depositType = (String) request.getAttribute("printDepositType");



    request.setAttribute("msg", (String) request.getAttribute("msg"));
    request.setAttribute("patient", PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo));
    request.setAttribute("depositType", depositType);
    request.setAttribute("depositTempList",
        new DepositReceiptRefundPrintTemplateDAO().getTemplateList());
    request.setAttribute("prefs", GenericPreferencesDAO.getGenericPreferences());
    request.setAttribute("patientMultiVisitPackageDetails", patientMultiVisitPackageDetails);
    request.setAttribute("isDepositApplicableToIP", true);
    request.setAttribute("patPackDetailsJson",
        new JSONSerializer().exclude("class").serialize(ConversionUtils.listBeanToListMap(patPackDetails)));
    request.setAttribute("packDepositJson",
        new JSONSerializer().exclude("class").serialize(packageDepositsList));
    request.setAttribute("packDeposits", packageDepositsList);
    request.setAttribute("getAllCreditTypes",
        new JSONSerializer().exclude("class").serialize(ConversionUtils
            .copyListDynaBeansToMap(new CardTypeMasterDAO().listAll(null, "status", "A", null))));
    request.setAttribute("cashDepositTransactionLimit",paymentModeMasterDao.getCashLimit());
    HttpSession session = (HttpSession)request.getSession(false);
    Integer centerId = (Integer)session.getAttribute("centerId");
    BasicDynaBean centerPrefs = new CenterPreferencesDAO().getCenterPreferences(centerId);
    request.setAttribute("centerPrefs", centerPrefs);
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("centerPrefsJson", js.serialize(centerPrefs.getMap()));
       
    List<BasicDynaBean> taxSubGroups = billChargeTaxDao.getTaxSubGroupsDetailsWithValidityFilter();
    request.setAttribute("taxSubGroups", taxSubGroups);
    request.setAttribute("taxSubGroupsJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(taxSubGroups)));

    String countryCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
    if (StringUtil.isNullOrEmpty(countryCode)) {
      countryCode = centerMasterDAO.getCountryCode(0);
    }
    request.setAttribute("defaultCountryCode", countryCode);
    request.setAttribute("countryList", PhoneNumberUtil.getAllCountries());

  //To get the User Counter and center mapped for that counter
    CounterMasterDAO cmdao = new CounterMasterDAO();
	BasicDynaBean cntbean = cmdao.findByKey("counter_id",session.getAttribute("billingcounterId"));
	int counterCenterId =0;
	if (null != cntbean) {
		counterCenterId = (int) cntbean.get("center_id");
		request.setAttribute("counterCenter", counterCenterId);
	}

    if ((mrNo != null) && !mrNo.equals("")) {
        //if the IP Deposit Availability needs to be shown at Center Level
        if ("E".equals(dto.getEnablePatientDepositAvailability())) {
          BasicDynaBean ipDepositBean = DepositsDAO.getIPDepositAmounts(mrNo,center_Id);
          if (null != ipDepositBean)
          request.setAttribute("ipDepositDetails", ipDepositBean.getMap());
        }else {
          BasicDynaBean ipDepositBean = DepositsDAO.getIPDepositAmounts(mrNo);
          if (null != ipDepositBean)
          request.setAttribute("ipDepositDetails", ipDepositBean.getMap());
        }
    }
    request.setAttribute("preselectPatPackageId", (String) request.getParameter("preselectPatPackageId"));
    return mapping.findForward("CollectOrRefundDeposits");
  }

  public ActionForward saveCollectOrRefundDeposits(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    FlashScope flash = FlashScope.getScope(request);
    List<Receipt> receiptList = null;
    Map requestParams = request.getParameterMap();
    Map printParamMap = null;
    AbstractPaymentDetails bpdImpl = AbstractPaymentDetails
        .getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
    Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");

    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    String depositChequeRealizationFlow = dto.getDepositChequeRealizationFlow();
    String mrNo = request.getParameter("mr_no");
    String depositPayer = request.getParameter("deposit_payer");
    String depositAvailableFor = request.getParameter("depositAvailable");
    String applicableToIP = request.getParameter("applicable_to_ip");
    if (depositAvailableFor == null)
      depositAvailableFor = "B";

    String payerPhoneNo = request.getParameter("payer_phone");
    String payerAddress = request.getParameter("payer_address");

    String printTemplate = request.getParameter("printTemplate");
    String printerId = request.getParameter("printer");
    String packageId = request.getParameter("package_id");

    Connection con = null;
    boolean success = true;
    Map<String, Map<String, String>> receiptUsageMap = new HashMap<>();

    try {

      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      DepositsDAO dao = new DepositsDAO();
      ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);

      List<Receipt> depositList = new ArrayList<>();
      List<BasicDynaBean> taxSubGrpList = new ArrayList<BasicDynaBean>();

      receiptList = bpdImpl.processReceiptParams(requestParams);
      if (receiptList != null && !receiptList.isEmpty()) {
        int receiptIndex = -1;
        String[] patientPackageIdList = (String[]) requestParams.get("patientPackageId");
        for (Receipt receipt : receiptList) {
          receiptIndex++;
          Map<String, String> entityMap = new HashMap<>();

          String realized = "Y";

          // If payment mode requires realization and
          // the generic preference(deposit_cheque_realization_flow) is "Y" then realized is "N"
          if (depositChequeRealizationFlow != null && depositChequeRealizationFlow.equals("Y")) {
            BasicDynaBean payBean = new PaymentModeMasterDAO().findByKey("mode_id",
                receipt.getPaymentModeId());
            if (payBean != null && payBean.get("realization_required") != null
                && ((String) payBean.get("realization_required")).equals("Y")) {
              realized = "N";
            }
          }

          String receiptNo = dao.getNextDepositId((String) receipt.getPaymentType());
          receipt.setReceiptNo(receiptNo);

          if (receipt.getPackageId() != null && receipt.getPackageId() > 0) {
            Integer patPackageId = Integer.parseInt(patientPackageIdList[receiptIndex]);
            entityMap.put(receipt.getPackageId().toString(),
                BillConstants.Restrictions.PACKAGE_ID);
            entityMap.put(patPackageId.toString(),
                BillConstants.Restrictions.PAT_PACKAGE_ID);
          }
          if ("I".equalsIgnoreCase(receipt.getApplicableToIp())) {
            depositAvailableFor = "i";
            entityMap.put(depositAvailableFor, BillConstants.Restrictions.VISIT_TYPE);
          }
          if ("P".equals(depositAvailableFor) || "H".equals(depositAvailableFor)) {
            entityMap.put(depositAvailableFor, BillConstants.Restrictions.BILL_TYPE);
          }
          receiptUsageMap.put(receiptNo, entityMap);

          receipt.setReceiptType(receipt.getPaymentType());
          receipt.setMrno(mrNo);
          receipt.setTdsAmt(BigDecimal.ZERO);
          receipt.setIsSettlement(false);
          receipt.setIsDeposit(true);
          receipt.setRealized(realized);
          receipt.setPayerAddress(payerAddress);
          receipt.setPointsRedeemed(0);
          receipt.setPayerMobileNumber(payerPhoneNo);
          receipt.setPayerName(depositPayer);
          receipt.setCenterId(centerId);
          
          BigDecimal totalTax = setTaxDetails(request, taxSubGrpList, receiptNo, receipt.getAmount());
          receipt.setTotalTax(totalTax);
          depositList.add(receipt);
        }
        
        // To insert entries into receipt table
        // Multivisit package
        List<Map<String, Object>> receiptsDataList = new ArrayList<>();
        Map columndata = null;
        Map keys = null;
        for (Receipt newReceipt : receiptList) {
          success = receiptDAO.createReceiptEntry(newReceipt);
          if (!success)
            break;
          

          BigDecimal balance = BigDecimal.ZERO;
          if (newReceipt.getPackageId() != null && newReceipt.getPackageId() > 0) {
            columndata = new HashMap();
            keys = new HashMap();
            if (newReceipt.getReceiptType() != null && newReceipt.getReceiptType().equals("F")) {
              balance = balance.subtract((BigDecimal) newReceipt.getAmount());
            } else if (newReceipt.getReceiptType() != null
                && newReceipt.getReceiptType().equals("R")) {
              balance = balance.add((BigDecimal) newReceipt.getAmount());
            }
            columndata.put("deposit_balance", balance);
            keys.put("package_id", new Integer(newReceipt.getPackageId().toString()));
            keys.put("mr_no", mrNo);
            success = patpackdao.update(con, columndata, keys) >= 0;
            if (!success)
              break;
          }

          // send the receipt for accounting
          Map<String, Object> receiptData = new HashMap<>();
          receiptData.put("receiptId", newReceipt.getReceiptNo());
          receiptData.put("reversalsOnly", Boolean.FALSE);
          receiptsDataList.add(receiptData);
        }
        receiptTaxDao.insertAll(con, taxSubGrpList);
        // Save the receipts so that it is accessible by allocationService(spring).
        con.commit();

        // To insert entries into receipt refund reference table
        AllocationService allocationService = (AllocationService) ApplicationContextProvider
            .getApplicationContext().getBean("allocationService");
        for (Receipt newReceipt : receiptList) {
          if("F".equals(newReceipt.getReceiptType())) {
            success = allocationService.createDepositRefundReference(newReceipt);
            updateRefundReceiptsTaxAmounts(con, keys, newReceipt);
          }
          if (!success)
            break;
        }
        // Schedule the accounting for deposit receipt
        accountingJobScheduler.scheduleAccountingForReceiptsList(receiptsDataList);

        // To insert entries into receipt usage table
        success = receiptDAO.createReceiptUsage(receiptUsageMap);

        // To Update the Deposit Setoff Total Table
        BigDecimal setOffAmount = BigDecimal.ZERO;
        setOffAmount = receiptDAO.getSetoffAmount(receiptList.get(0).getMrno());
        BigDecimal totalTaxSetOffAmount = receiptDAO.getTotalTaxSetOffAmount(con,receiptList.get(0).getMrno());
        BigDecimal totalDepositAmount = BigDecimal.ZERO;
        totalDepositAmount = receiptDAO.getTotalDeposit(receiptList.get(0).getMrno());
        BigDecimal totalDepositTaxAmount = receiptDAO.getTotalDepositTaxAmount(con,(receiptList.get(0).getMrno()));
        success = receiptDAO.createDepositSetoffTotal(receiptList.get(0).getMrno(),
            totalDepositAmount, setOffAmount, totalDepositTaxAmount, totalTaxSetOffAmount);
      }

      if (success && receiptList != null && !receiptList.isEmpty()) {

        printParamMap = new HashMap();
        printParamMap.put("printer", printerId);
        printParamMap.put("printTemplate", printTemplate);
        printParamMap.put("mrNo", mrNo);

        List<String> printURLs = BillPaymentDetailsImpl.generatePrintDepositReceiptUrls(receiptList,
            printParamMap);
        request.getSession(false).setAttribute("printURLs", printURLs);

        String centerName = "";

        List<BasicDynaBean> centerBeanList = new GenericDAO("hospital_center_master").listAll(null,
            Arrays.asList(new String[] { "center_name" }), "center_id", centerId, null);

        if (centerBeanList != null && !centerBeanList.isEmpty()) {
          centerName = (String) centerBeanList.get(0).get("center_name");
        }
        if (!depositList.isEmpty()) {
          sendDepositSMS(depositList, centerName);
        }
      }

      if (!success)
        flash.put("error", "Failed to save deposits.");

    } finally {
      DataBaseUtil.commitClose(con, success);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("saveDepositsRedirect"));
    redirect.addParameter("mrNo", mrNo);
    redirect.addParameter("_method", "collectOrRefundDeposits");
    redirect.addParameter("printer", printerId);
    redirect.addParameter("printTemplate", printTemplate);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  private void updateRefundReceiptsTaxAmounts(Connection con, Map keys, Receipt newReceipt)
      throws SQLException, IOException {
    List<BasicDynaBean> refundRefBeanList= receiptRefundDAO.findAllByKey("refund_receipt_id", newReceipt.getReceiptNo());
    for(BasicDynaBean refundRefBean : refundRefBeanList){
      BasicDynaBean receiptBean = receiptsDAO.findByKey("receipt_id", refundRefBean.get("receipt_id"));
      BasicDynaBean refBean = receiptRefundDAO.getBean();
      refBean.set("tax_rate", (BigDecimal)receiptBean.get("total_tax_rate"));
      BigDecimal amount = (BigDecimal)refundRefBean.get("amount");
      BigDecimal taxRate = (BigDecimal)receiptBean.get("total_tax_rate");
      BigDecimal taxAmount = getTaxAggAmount(amount, taxRate);
      refBean.set("tax_amount", taxAmount);
      Map keyval = new HashMap();
      keyval.put("receipt_id", refundRefBean.get("receipt_id"));
      keyval.put("refund_receipt_id", refundRefBean.get("refund_receipt_id"));
      receiptRefundDAO.update(con, refBean.getMap(), keyval);
    }
  }

  private BigDecimal setTaxDetails(HttpServletRequest request, List<BasicDynaBean> taxSubGrpList, 
      String receiptNo, BigDecimal amount) throws SQLException {
    Object taxObjPri = request.getParameter("tax_subgrp_primary");
    
    if (taxObjPri == null || taxObjPri.equals("")) {
      return new BigDecimal(0.00);
    }
     
    Object taxObjSec = request.getParameter("tax_subgrp_secondary");
    
    List<BasicDynaBean> taxSubGroups = billChargeTaxDao.getTaxSubGroupsDetailsWithValidityFilter();
    Map<Integer,Object> taxSubGrpsMap = ConversionUtils.listBeanToMapMap(taxSubGroups, "item_subgroup_id");
    
    BigDecimal totalTaxAmount = new BigDecimal(0.00);
    BigDecimal totalTaxrate = new BigDecimal(0.00);
    
    
    if(taxObjPri != null && !taxObjPri.equals("")){
      Map priTaxDetails = (Map) taxSubGrpsMap.get(Integer.parseInt((String)taxObjPri)); 
      totalTaxrate = totalTaxrate.add((BigDecimal) priTaxDetails.get("tax_rate"));
      if(taxObjSec != null && !taxObjSec.equals("")){
        Map secTaxDetails = (Map) taxSubGrpsMap.get(Integer.parseInt((String)taxObjSec)); 
        totalTaxrate = totalTaxrate.add((BigDecimal) secTaxDetails.get("tax_rate"));
      }
      totalTaxAmount = totalTaxAmount.add((BigDecimal) priTaxDetails.get("tax_rate"));    
      setTaxSubGrpDetails(taxSubGrpList, receiptNo, amount, priTaxDetails, totalTaxrate);
    }
    
    if(taxObjSec != null && !taxObjSec.equals("")){
      Map secTaxDetails = (Map) taxSubGrpsMap.get(Integer.parseInt((String)taxObjSec)); 
      totalTaxAmount = totalTaxAmount.add((BigDecimal) secTaxDetails.get("tax_rate"));      
      setTaxSubGrpDetails(taxSubGrpList, receiptNo, amount, secTaxDetails, totalTaxrate);
    }
    return totalTaxAmount;
  }

  private void setTaxSubGrpDetails(List<BasicDynaBean> taxSubGrpList, String receiptNo,
      BigDecimal amount, Map priTaxDetails, BigDecimal totalTaxrate) throws SQLException {
    BasicDynaBean receiptTaxBean = receiptTaxDao.getBean();
    receiptTaxBean.set("receipt_id", receiptNo);
    receiptTaxBean.set("tax_sub_group_id", (int)priTaxDetails.get("item_subgroup_id"));    
    BigDecimal taxrate = (BigDecimal) priTaxDetails.get("tax_rate");
    BigDecimal totalTaxAmt = getTaxAggAmount(amount, totalTaxrate);
    BigDecimal taxAmount = BigDecimal.ZERO;
    if(totalTaxrate.compareTo(BigDecimal.ZERO) > 0) {
      taxAmount = ConversionUtils.setScale(taxrate.divide(totalTaxrate,2).multiply(totalTaxAmt),true);
    } 
    receiptTaxBean.set("tax_rate", taxrate);
    receiptTaxBean.set("tax_amount", taxAmount);
    taxSubGrpList.add(receiptTaxBean);
  }

  private void sendDepositSMS(List<Receipt> depositList, String centerName)
      throws SQLException {
    Map<String, Object> messageData = new HashMap<String, Object>();
    Receipt firstDeposit = depositList.get(0);

    BasicDynaBean patientBean = PatientDetailsDAO
        .getPatientGeneralDetailsBean((String) firstDeposit.getMrno());
    messageData.put("recipient_name", patientBean.get("patient_name"));
    messageData.put("recipient_mobile", patientBean.get("patient_phone"));
    messageData.put("recipient_email", patientBean.get("email_id"));
    messageData.put("mr_no", firstDeposit.getMrno());
    messageData.put("patient_name", patientBean.get("patient_name"));
    messageData.put("patient_phone", patientBean.get("patient_phone"));
    messageData.put("deposit_payer_name", firstDeposit.getPayerName());
    messageData.put("deposit_payer_phone", firstDeposit.getPayerMobileNumber());
    messageData.put("remarks", firstDeposit.getRemarks());
    messageData.put("counter", firstDeposit.getCounter());
    messageData.put("deposit_date", firstDeposit.getReceiptDate());
    messageData.put("receipient_id__", messageData.get("mr_no"));
    messageData.put("receipient_type__", "PATIENT");
    messageData.put("lang_code",
        PatientDetailsDAO.getContactPreference((String) firstDeposit.getMrno()));
    messageData.put("patient_local_name", patientBean.get("name_local_language"));
    messageData.put("currency_symbol", GenericPreferencesDAO.getAllPrefs().get("currency_symbol"));
    messageData.put("center_name", centerName);

    BigDecimal summedAmount = new BigDecimal(0);
    for (Receipt deposit : depositList) {
      if (deposit.getReceiptType().equals("F")) {
        continue;
      }

      summedAmount = summedAmount.add((BigDecimal) deposit.getAmount());

    }

    messageData.put("deposit_amount", summedAmount);

    if (summedAmount.compareTo(new BigDecimal(0)) > 0) {
      Map<String, Object> jobData = new HashMap<String, Object>();
      jobData.put("eventData", messageData);
      jobData.put("userName", firstDeposit.getUsername());
      jobData.put("schema", RequestContext.getSchema());
      jobData.put("eventId", "deposit_paid");

      String recipientMobile = (String) messageData.get("recipient_mobile");
      if (recipientMobile != null && !recipientMobile.isEmpty()) {
        jobService
            .scheduleImmediate(buildJob("DepositSMSJob_ToPatient_" + firstDeposit.getReceiptNo(),
                DepositMessagingJob.class, jobData));
      } else {
        log.info("Patient mobile # not available, skipping SMS on deposit");
      }

      messageData.put("recipient_mobile", messageData.get("deposit_payer_phone"));
      messageData.put("recipient_name", messageData.get("deposit_payer_name"));
      messageData.put("receipient_id__", "");
      recipientMobile = (String) messageData.get("recipient_mobile");
      if (recipientMobile != null && !recipientMobile.isEmpty()) {
        jobService
            .scheduleImmediate(buildJob("DepositSMSJob_ToPayer_" + firstDeposit.getReceiptNo(),
                DepositMessagingJob.class, jobData));
      } else {
        log.info("Payer mobile # not available, skipping SMS on deposit");
      }

    }

  }

  public ActionForward printDepositeStmt(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(request.getParameter("mrNo"));
    String reportName = mapping.getProperty("report-name");
    FtlReportGenerator fg = new FtlReportGenerator(reportName);
    fg.setParamsFromParamMap(request.getParameterMap());
    fg.setParam("cpath", request.getContextPath());
    fg.setParam("currtime", DataBaseUtil.getDateandTime());
    fg.setParam("mr_no", request.getParameter("mrNo"));
    fg.setParam("dl", patmap);
    response.setContentType("application/pdf");
    fg.runPdfReport(response.getOutputStream());

    return null;
  }

  public ActionForward depositPrint(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws IOException, JRException, SQLException, ParseException,
      TemplateException, DocumentException, XPathExpressionException, TransformerException {

    HashMap params = new HashMap();

    String printPage = null;
    String printerIdStr = req.getParameter("printerType");
    String templateName = req.getParameter("printTemplate");

    String receiptNo = req.getParameter("deposit_no");
    String mrNo = req.getParameter("mrNo");
    params.put("receiptNo", receiptNo);
    params.put("printUserName", req.getSession().getAttribute("userid"));

    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    params.put("currencySymbol", dto.getCurrencySymbol());

    String depositPrintName = null;
    List<BasicDynaBean> depositsList = DepositsDAO.depositReceiptRefundPrint(receiptNo);
    params.put("depositsList", depositsList);
    BigDecimal depositAmount = BigDecimal.ZERO;
    if (!depositsList.isEmpty()) {
      for (BasicDynaBean deposit : depositsList) {
        depositAmount = (BigDecimal) deposit.get("amount");
      }
    }

    params.put("taxSubGrps", receiptsTaxDAO.findAllByKey("receipt_id", receiptNo));
    params.put("depositRefundReference", receiptRefundDAO.findAllByKey("refund_receipt_id", receiptNo));
    params.put("AmountinWords", NumberToWordFormat.wordFormat().toRupeesPaise(depositAmount));

    if (req.getParameter("printDepositType").equalsIgnoreCase("R")) {
      depositPrintName = "Deposit Receipt";
      params.put("depositPrintName", depositPrintName);

    } else if (req.getParameter("printDepositType").equalsIgnoreCase("F")) {
      depositPrintName = "Deposit Refund ";
      params.put("depositPrintName", depositPrintName);

    }

    int printerId = 0;
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

    BasicDynaBean bean = GenericPreferencesDAO.getAllPrefs();

    if (templateName != null && templateName.equals("")) {
      if (bean.get("receipt_refund_print_default") != null
          && !bean.get("receipt_refund_print_default").equals(""))
        templateName = (String) bean.get("receipt_refund_print_default");
      else
        templateName = "BUILTIN_HTML";
    }

    DepositReceiptRefundPrintTemplateDAO templateDao = new DepositReceiptRefundPrintTemplateDAO();
    BasicDynaBean tmpBean = templateDao.getTemplateContent(templateName);
    String templateMode = null;
    String templateContent = null;
    if (tmpBean != null) {
      templateContent = (String) tmpBean.get("template_content");
      templateMode = (String) tmpBean.get("template_mode");
    }
    FtlReportGenerator t = null;
    if (templateContent != null) {
      StringReader reader = new StringReader(templateContent);
      if (templateMode.equals("H")) {
        t = new FtlReportGenerator("DepositReceiptRefundPrint", reader);
      } else {
        t = new FtlReportGenerator("DepositReceiptRefundTextPrint", reader);
      }
    } else {
      if (templateName != null && templateName.equals("BUILTIN_HTML")) {
        t = new FtlReportGenerator("DepositReceiptRefundPrint");
        templateMode = "H";
      } else {
        t = new FtlReportGenerator("DepositReceiptRefundTextPrint");
        templateMode = "T";
      }
    }

    /*
     * Process the template and get the html
     */
    StringWriter writer = new StringWriter();
    t.setReportParams(params);
    t.process(writer);

    String textContent = writer.toString();

    /*
     * Conver the html to text or PDF and send it as response
     */
    HtmlConverter hc = new HtmlConverter();
    PrintPageOptions opts = new PrintPageOptions(printPref);

    if (printPref.get("print_mode").equals("T")) {
      String textReport = null;
      if (templateMode.equals("T")) {
        textReport = textContent;
      } else {
        textReport = new String(hc.getText(textContent, "Receipt", printPref, true, true));
      }
      req.setAttribute("textReport", textReport);
      req.setAttribute("textColumns", printPref.get("text_mode_column"));
      req.setAttribute("printerType", "DMP");
      return m.findForward("textPrintApplet");

    } else if (printPref.get("print_mode").equals("H")) {
      res.setContentType("text/html");
      res.getWriter().write(textContent);
      return null;
    } else if (printPref.get("print_mode").equals("R")) {
      res.setContentType("application/rtf");
      res.getWriter().write(textContent);
      return null;
    } else {
      OutputStream os = res.getOutputStream();
      res.setContentType("application/pdf");
      try {
        if (templateMode.equals("T")) {
          hc.textToPDF(textContent, os, printPref);
        } else {
          hc.writePdf(os, textContent, "Receipt", printPref, false, false, true, true, true, false);
        }
      } finally {
        os.close();
      }
      return null;
    }
  }

  @IgnoreConfidentialFilters
  public ActionForward getDepositReceiptsListScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    ReceiptRefundPrintTemplateDAO rrdao = new ReceiptRefundPrintTemplateDAO();
    DepositReceiptRefundPrintTemplateDAO drdao = new DepositReceiptRefundPrintTemplateDAO();
    // String category = mapping.getProperty("category");
    Map map = getParameterMap(request);

    /*
     * if (category.equals("pharmacy")) { map.put("counter_type", new String[]{"P"}); }
     */

    JSONSerializer json = new JSONSerializer().exclude("class");
    PagedList list = DepositsDAO.getDepositsReceiptsPagedList(map,
        ConversionUtils.getListingParameter(map));

    request.setAttribute("pagedList", list);
    BasicDynaBean printPref = PrintConfigurationsDAO.getPrintMode("Bill");
    request.setAttribute("pref", printPref);

    // request.setAttribute("templateList", rrdao.getTemplateList());
    // request.setAttribute("jsonReceiptsTempList", json.serialize(rrdao.getTemplateNames()));

    request.setAttribute("depositTempList", drdao.getTemplateList());
    request.setAttribute("jsonDepositTempList", json.serialize(drdao.getTemplateNames()));

    request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    request.setAttribute("multiCentered",
            GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());

    // requrest.setAttribute("category", category);
    return mapping.findForward("showDepositsReceiptList");

  }

  public ActionForward getDepositReceiptsScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    DepositReceiptRefundPrintTemplateDAO drdao = new DepositReceiptRefundPrintTemplateDAO();
    JSONSerializer json = new JSONSerializer().exclude("class");

    request.setAttribute("depositTempList", drdao.getTemplateList());
    request.setAttribute("jsonDepositTempList", json.serialize(drdao.getTemplateNames()));
    String category = mapping.getProperty("category");
    request.setAttribute("category", category);
    request.setAttribute("multiCentered",
            GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1);
    request.setAttribute("centers", CenterMasterDAO.getAllCentersExceptSuper());
    return mapping.findForward("showDepositsReceiptList");

  }

  public ActionForward collectOrRefundDepositsAjax(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    HashMap returnedData = new HashMap();
    String mrNo = request.getParameter("mr_no");
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
    //if the Deposit Availability needs to be shown at Center Level
    if ("E".equals(dto.getEnablePatientDepositAvailability())) {
      BasicDynaBean bean = DepositsDAO.getDepositAmounts(mrNo,centerId);
      returnedData.put("depositDetails", bean.getMap());
    }else{
      BasicDynaBean bean = DepositsDAO.getDepositAmounts(mrNo);
      returnedData.put("depositDetails", bean.getMap());
    }

  //if the IP Deposit Availability needs to be shown at Center Level
    if ("E".equals(dto.getEnablePatientDepositAvailability())) {
      BasicDynaBean ipDepositBean = DepositsDAO.getIPDepositAmounts(mrNo,centerId);
      if (null != ipDepositBean)
        returnedData.put("ipDepositDetails", ipDepositBean.getMap());
    }else {
      BasicDynaBean ipDepositBean = DepositsDAO.getIPDepositAmounts(mrNo);
      if (null != ipDepositBean)
      returnedData.put("ipDepositDetails", ipDepositBean.getMap());
    }

  //if the Package Deposit Availability needs to be shown at Center Level
    if ("E".equals(dto.getEnablePatientDepositAvailability())) {
      List<BasicDynaBean> packageDepositsList = ConversionUtils
        .listBeanToListMap(DepositsDAO.getMultiVisitPkgDepositAmounts(mrNo,centerId));
      returnedData.put("packageDeposits", packageDepositsList);
    } else {
      List<BasicDynaBean> packageDepositsList = ConversionUtils
        .listBeanToListMap(multivisitDepositsViewDAO.listAll(null, "mr_no", mrNo, "package_id"));
      returnedData.put("packageDeposits", packageDepositsList);
    }

    JSONSerializer js = new JSONSerializer();
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.deepSerialize(returnedData));
    response.flushBuffer();
    return null;
  }
  
  protected BigDecimal getTaxAggAmount(BigDecimal amount,
      BigDecimal aggregateTaxPer) {
    BigDecimal taxAmt = BigDecimal.ZERO;

    BigDecimal denomi = aggregateTaxPer.divide(new BigDecimal("100"));
    denomi = (BigDecimal.ONE).add(denomi);

    taxAmt = ConversionUtils.divideHighPrecision(amount, denomi);

    return ConversionUtils.setScale(amount.subtract(taxAmt),true);
  }

}
