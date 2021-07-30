package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillActivityCharge;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ReceiptRelatedDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class InsuranceClaimAction.
 *
 * @author pragna.p
 */

public class InsuranceClaimAction extends DispatchAction {

  /** The dao. */
  private static GenericDAO dao = new GenericDAO("insurance_claim_docs");

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(InsuranceClaimAction.class);

  /**
   * Gets the string value from bean.
   *
   * @param bean the bean
   * @param key  the key
   * @return the string value from bean
   */
  public static String getStringValueFromBean(BasicDynaBean bean, String key) {
    return getStringValueFromBean(bean, key, false);
  }

  /**
   * Gets the string value from bean.
   *
   * @param bean    the bean
   * @param key     the key
   * @param nullify the nullify
   * @return the string value from bean
   */
  public static String getStringValueFromBean(BasicDynaBean bean, String key, boolean nullify) {
    String init = nullify ? null : "";
    if (bean == null || bean.get(key) == null) {
      return init;
    } else {
      return (String) bean.get(key);
    }
  }

  /**
   * Show.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String insuranceId = req.getParameter("insurance_id");

    BasicDynaBean bean = InsuranceDAO.getClaimContent(Integer.parseInt(insuranceId));

    StringBuilder path = new StringBuilder("/pages/Insurance/InsuranceClaim/");

    String billNos = getStringValueFromBean(bean, "bill_no");
    String visitId = getStringValueFromBean(bean, "patient_id");
    String templateType = getStringValueFromBean(bean, "template_type");
    int claimTempId = (Integer) bean.get("claim_template_id");
    String defaultClaimTemp = getStringValueFromBean(bean, "default_claim_template");

    if (claimTempId == 0 && (defaultClaimTemp.equals("P") || defaultClaimTemp.equals("Y"))) {
      path.append("InsClaimHtmlTemplate.jsp");
      if (bean.get("claim_docs_id") == null) {

        String htmlContent = fillValuesInFTL(billNos, visitId, insuranceId,
            "InsuranceClaimTemplate.ftl", null);
        bean.set("doc_content_html", htmlContent);
        req.setAttribute("mode", "insert");
      }

    } else if (claimTempId == 0 && defaultClaimTemp.equals("R")) {
      path.append("InsClaimRtfTemplate.jsp");

      if (bean.get("claim_docs_id") == null) {
        req.setAttribute("mode", "insert");
      }

    } else {
      if (templateType.equals("R")) {
        path.append("InsClaimRtfTemplate.jsp");
        if (bean.get("claim_docs_id") == null) {
          req.setAttribute("mode", "insert");
        }
      } else {
        path.append("InsClaimHtmlTemplate.jsp");

        if (bean.get("doc_content_html") == null) {
          String htmlContent = fillValuesInFTL(billNos, visitId, insuranceId,
              "InsuranceClaimTemplate.ftl", (String) bean.get("template_content"));
          bean.set("doc_content_html", htmlContent);
          req.setAttribute("mode", "insert");

        }
      }
    }
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_INSURENCE);
    req.setAttribute("pref", printPref);

    req.setAttribute("ClaimDetails", bean);

    return new ActionForward(path.toString());
  }

  /**
   * Adds the or edit HT ml.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward addOrEditHTMl(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String mode = req.getParameter("mode");

    List errors = new ArrayList();
    boolean result = false;
    String error = null;
    String success = null;

    BasicDynaBean bean = dao.getBean();
    Map params = req.getParameterMap();
    Connection con = null;
    HttpSession session = req.getSession();
    String username = session.getAttribute("userid").toString();

    ConversionUtils.copyToDynaBean(params, bean, errors);
    bean.set("username", username);

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (errors.isEmpty()) {
        if (mode.equals("insert")) {
          bean.set("doc_date", DateUtil.getCurrentDate());
          result = dao.insert(con, bean);
        } else {
          Map<String, Integer> keys = new HashMap<String, Integer>();
          keys.put("insurance_id", Integer.parseInt(bean.get("insurance_id").toString()));
          int val = dao.update(con, bean.getMap(), keys);
          if (val > 0) {
            result = true;
          }
        }
        if (result) {
          success = "Claim Processing Done Successfully";
        }
      } else {
        error = "Error in Claim Processing..";
      }
    } finally {
      DataBaseUtil.commitClose(con, result);
    }
    FlashScope flash = FlashScope.getScope(req);
    flash.put("success", success);
    flash.put("error", error);

    String path = "InsuranceClaim.do?_method=show";
    ActionRedirect redirect = new ActionRedirect(path);
    redirect.addParameter("insurance_id", req.getParameter("insurance_id"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Join.
   *
   * @param collection the c
   * @return the string
   */
  public String join(Collection<String> collection) {
    StringBuilder sb = new StringBuilder();
    for (String element : collection) {
      sb.append(element);
    }
    return sb.toString();
  }

  /**
   * Fill values in FTL.
   *
   * @param billNos         the bill nos
   * @param visitId         the visit id
   * @param insuranceId     the insurance id
   * @param ftlName         the ftl name
   * @param templateContent the template content
   * @return the string
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws SQLException      the SQL exception
   * @throws TemplateException the template exception
   */
  private String fillValuesInFTL(String billNos, String visitId, String insuranceId, String ftlName,
      String templateContent) throws IOException, SQLException, TemplateException {
    // params to be passed to the template processor
    Map params = new HashMap();
    /*
     * Get the bill and patient details
     */
    Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
    params.put("patient", patientDetails);

    BasicDynaBean caseDetails = new InsuranceDAO().getCaseDetails(Integer.parseInt(insuranceId));
    String caseStatus = caseDetails.get("status").toString();

    if (caseStatus.equals("A")) {
      caseDetails.set("status", "Approved");
    } else if (caseStatus.equals("F")) {
      caseDetails.set("status", "Finalized");
    } else if (caseStatus.equals("C")) {
      caseDetails.set("status", "Closed");
    } else if (caseStatus.equals("D")) {
      caseDetails.set("status", "Denied");
    } else {
      caseDetails.set("status", "Preauth");
    }

    params.put("case", caseDetails.getMap());

    if (billNos != null && !billNos.equals("")) {
      String[] billNosArr = billNos.split(",");
      for (int i = 0; i < billNosArr.length; i++) {
        billNosArr[i] = "'" + billNosArr[i] + "'";
      }

      String billNosSqlStr = join(Arrays.asList(billNos.split(",")));
      BasicDynaBean bill = InsuranceDAO.getBillBean(billNosSqlStr);
      bill.set("bill_no", billNos);
      params.put("bill", bill);

      /*
       * Get the charge details of the bill
       */
      List<BasicDynaBean> charges = new ArrayList();
      String[] billNumbers = billNos.split(",");
      for (int i = 0; i < billNos.split(",").length; i++) {
        String billNo = billNumbers[i];
        List tempCharges = ChargeDAO.getChargeDetailsBean(billNumbers[i]);
        charges.addAll(tempCharges);
      }
      params.put("charges", charges);
      List<Integer> pkgIdList = new ArrayList<Integer>();
      List<String> ratePlanList = new ArrayList<String>();
      List<String> bedTypeList = new ArrayList<String>();
      // organize into Charge Group based map, this will result in a map
      // like REG => [bean1, bean2], DOC => [bean3, bean4], ...
      Map chargeGroupMap = ConversionUtils.listBeanToMapListBean(charges, "chargegroup_name");
      params.put("chargeGroupMap", chargeGroupMap);
      params.put("chargeGroups", chargeGroupMap.keySet());

      // organize into Charge Group based map, this will result in a map
      // like GREG => [bean1], LTDIA => [bean2, bean3], ...
      Map chargeHeadMap = ConversionUtils.listBeanToMapListBean(charges, "chargehead_name");
      params.put("chargeHeadMap", chargeHeadMap);
      params.put("chargeHeads", chargeHeadMap.keySet());

      // organize into Service Group based map, this will result in a map
      // like Direct Charge => [bean1, bean2], Laboratory => [bean3,
      // bean4], ...
      Map serviceGroupMap = ConversionUtils.listBeanToMapListBean(charges, "service_group_name");
      params.put("serviceGroupMap", serviceGroupMap);
      params.put("serviceGroups", serviceGroupMap.keySet());

      // organize into Service Sub Group based map, this will result in a
      // map
      // like Dept1 => [bean1], Dept2 => [bean2, bean3], ...
      Map serviceSubGroupMap = ConversionUtils.listBeanToMapListBean(charges,
          "service_sub_group_name");
      params.put("serviceSubGroupMap", serviceSubGroupMap);
      params.put("serviceSubGroups", serviceSubGroupMap.keySet());

      /*
       * Receipts for this bill
       */
      List<BasicDynaBean> receipts = ReceiptRelatedDAO.getReceptRefundListForBills(billNosSqlStr);
      params.put("receipts", receipts);

      // organize receipts by recpt_type
      Map receiptTypeMap = ConversionUtils.listBeanToMapListBean(receipts, "recpt_type");
      params.put("receiptTypeMap", receiptTypeMap);
      // no need of all receipt types, this list is static: A,S,F

      // Generic Preferences
      BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
      params.put("genPrefs", genPrefs);

      /*
       * Some totals: total bill amount, claim amount, total discounts, net payments
       */
      BigDecimal totalAmount = BigDecimal.ZERO;
      BigDecimal totalDiscount = BigDecimal.ZERO;
      BigDecimal totalClaimAmount = BigDecimal.ZERO;
      BigDecimal totalTaxOnClaim = BigDecimal.ZERO;
      boolean hasDiscounts = false;
      for (BasicDynaBean charge : charges) {
        String chargeStatus = (String) charge.get("status");
        if (!chargeStatus.equals("X")) {
          BigDecimal chargeAmount = (BigDecimal) charge.get("amount");
          BigDecimal discount = (BigDecimal) charge.get("discount");
          BigDecimal claimAmount = (BigDecimal) charge.get("insurance_claim_amount");

          totalAmount = totalAmount.add(chargeAmount);
          totalDiscount = totalDiscount.add(discount);
          totalClaimAmount = totalClaimAmount.add(claimAmount);
          if (discount.compareTo(BigDecimal.ZERO) != 0) {
            hasDiscounts = true;
          }
        }
      }

      for (BasicDynaBean charge : charges) {
        String chargeStatus = (String) charge.get("status");
        if (!chargeStatus.equals("X")) {
          if (((String) charge.get("charge_group")).equals("TAX")
              && ((String) charge.get("charge_head")).equals("CSTAX")) {
            totalTaxOnClaim = (BigDecimal) charge.get("insurance_claim_amount");
            break;
          }
        }
      }

      params.put("totalAmount", totalAmount);
      params.put("totalDiscount", totalDiscount);
      params.put("totalClaimAmount", totalClaimAmount);
      params.put("totalTaxOnClaim", totalTaxOnClaim);
      params.put("hasDiscounts", hasDiscounts);

      BigDecimal netPayments = BigDecimal.ZERO;
      for (BasicDynaBean receipt : receipts) {
        BigDecimal amt = (BigDecimal) receipt.get("amount");
        netPayments = netPayments.add(amt);
      }
      params.put("netPayments", netPayments);
      params.put("netPaymentsWords", NumberToWordFormat.wordFormat().toRupeesPaise(netPayments));
      params.put("totalClaimAmountWords",
          NumberToWordFormat.wordFormat().toRupeesPaise(totalClaimAmount));
      params.put("totalAmountWords", NumberToWordFormat.wordFormat().toRupeesPaise(totalAmount));

      Connection con = null;
      BillActivityCharge activity = null;
      try {
        con = DataBaseUtil.getConnection();

        for (BasicDynaBean charge : charges) {
          String chargeStatus = (String) charge.get("status");
          BigDecimal chargeAmount = (BigDecimal) charge.get("amount");
          BigDecimal discount = (BigDecimal) charge.get("discount");
          BigDecimal claimAmount = (BigDecimal) charge.get("insurance_claim_amount");

          totalAmount = totalAmount.add(chargeAmount);
          totalDiscount = totalDiscount.add(discount);
          totalClaimAmount = totalClaimAmount.add(claimAmount);
          if (discount.compareTo(BigDecimal.ZERO) != 0) {
            hasDiscounts = true;
          }

          BillActivityChargeDAO bacDAO = new BillActivityChargeDAO(con);
          activity = bacDAO.getActivity((String) charge.get("charge_id"));

          // Search if PKGPKG charge exists in the bill charges.
          if (((String) charge.get("charge_head")).equals("PKGPKG") && activity != null) {
            pkgIdList.add(new Integer((String) charge.get("act_description_id")));
            ratePlanList.add((String) charge.get("bill_rate_plan_id"));
            bedTypeList.add((String) patientDetails.get("bill_bed_type"));
          }
        }
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
      // Get package details
      List pkgComponentDetails = ChargeDAO.getPackageComponentsList(pkgIdList, 
          ratePlanList ,bedTypeList);
      Map pkgDetails = ConversionUtils.listBeanToMapListBean(pkgComponentDetails, "package_id");
      params.put("packageDetailsMap", pkgDetails);
    }
    /*
     * Get the template for the print
     */
    Template template = null;
    if (templateContent == null || templateContent.equals("")) {
      template = AppInit.getFmConfig().getTemplate(ftlName);
    } else {
      StringReader reader = new StringReader(templateContent);
      template = new Template("InsuranceClaimTemplate.ftl", reader, AppInit.getFmConfig());
    }
    /*
     * Process the template and get the html
     */
    StringWriter writer = new StringWriter();
    template.process(params, writer);
    String htmlContent = writer.toString();
    return htmlContent;
  }

  /**
   * Adds the or edit RTF.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws ServletException    the servlet exception
   * @throws SQLException        the SQL exception
   * @throws FileUploadException the file upload exception
   * @throws ParseException      the parse exception
   */
  public ActionForward addOrEditRTF(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, FileUploadException, ParseException {

    BasicDynaBean bean = dao.getBean();
    Map<String, Object[]> params = getParameterMap(request);

    String mode = (String) params.get("mode")[0];

    String path = "InsuranceClaim.do?_method=show";
    ActionRedirect redirect = new ActionRedirect(path);
    redirect.addParameter("insurance_id", params.get("insurance_id")[0]);
    FlashScope flash = FlashScope.getScope(request);
    HttpSession session = request.getSession();
    String username = session.getAttribute("userid").toString();

    String error = null;
    String success = null;

    if (params.get("fileSizeError") != null) {
      // if the file size is greater than 10 MB prompting the user with the failure message.
      error = (String) params.get("fileSizeError")[0];
      flash.put("error", error);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    List errors = new ArrayList();

    boolean result = false;

    ConversionUtils.copyToDynaBean(params, bean, errors);
    bean.set("username", username);
    if (errors.isEmpty()) {
      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      try {
        if (mode.equals("insert")) {
          bean.set("doc_date", DateUtil.getCurrentDate());
          result = dao.insert(con, bean);
        } else {
          Map<String, Integer> keys = new HashMap<String, Integer>();
          keys.put("insurance_id", Integer.parseInt(bean.get("insurance_id").toString()));
          int val = dao.update(con, bean.getMap(), keys);
          if (val > 0) {
            result = true;
          }
        }

        if (result) {
          success = "Uploaded successfully..";
        } else {
          error = "Failed to upload..";
        }

      } finally {
        DataBaseUtil.commitClose(con, result);
      }
    } else {
      error = "Incorrectly formatted details supplied..";
    }
    flash.put("success", success);
    flash.put("error", error);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * View.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws SQLException     the SQL exception
   */
  public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException {
    String insuranceId = request.getParameter("insurance_id");

    BasicDynaBean bean = dao.getBean();
    dao.loadByteaRecords(bean, "insurance_id", Integer.parseInt(insuranceId));
    String contentType = bean.get("content_type").toString();
    response.setContentType(contentType);
    String fileName = "";

    BasicDynaBean insDetailsBean = InsuranceDAO.getClaimContent(Integer.parseInt(insuranceId));

    if (Integer.parseInt(insDetailsBean.get("claim_template_id").toString()) == 0
        && insDetailsBean.get("default_claim_template").toString().equals("R")) {
      fileName = "Default RTF Template";
    } else {
      fileName = insDetailsBean.get("template_name").toString();
    }

    if (contentType.equals("application/rtf") || contentType.equals("text/rtf")) {
      fileName = fileName + ".rtf";
    }

    String mrno = insDetailsBean.get("mr_no").toString();
    response.setHeader("Content-disposition",
        "attachment; filename=\"" + mrno + "_" + fileName + "\"");

    byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream) bean.get("doc_content_rtf"));
    OutputStream stream = response.getOutputStream();
    stream.write(bytes);
    stream.flush();
    stream.close();

    return null;
  }

  /**
   * Gets the parameter map.
   *
   * @param request the request
   * @return the parameter map
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws FileUploadException the file upload exception
   */
  public Map<String, Object[]> getParameterMap(HttpServletRequest request)
      throws IOException, FileUploadException {

    Map<String, Object[]> params = new HashMap<String, Object[]>();
    String contentType = null;

    if (!request.getContentType().split("/")[0].equals("multipart")) {
      // Create a factory for disk-based file items

      DiskFileItemFactory factory = new DiskFileItemFactory();

      // Set factory constraints
      // factory.setSizeThreshold(yourMaxMemorySize);
      // factory.setRepository(yourTempDirectory);

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Set overall request size constraint
      // upload.setSizeMax(yourMaxRequestSize);

      // Parse the request
      List<FileItem> items = upload.parseRequest(request);

      // Process the uploaded items
      Iterator iter = items.iterator();
      while (iter.hasNext()) {
        FileItem item = (FileItem) iter.next();

        if (item.isFormField()) {
          String name = item.getFieldName();
          String value = item.getString();
          params.put(name, new Object[] { value });

        } else {
          String fieldName = item.getFieldName();
          String fileName = item.getName();
          contentType = item.getContentType();
          long sizeInBytes = item.getSize();

          if (!fileName.equals("")) {
            if (sizeInBytes > 10 * 1024 * 1024) {
              params.put("fileSizeError",
                  new Object[] { "Unable to upload the file: " + "file size greater than 10 MB" });
            }
            params.put(fieldName, new InputStream[] { item.getInputStream() });
            params.put("content_type",
                new Object[] { MimeTypeDetector.getMimeTypes(item.getInputStream()) });
          }
        }
      }
    } else {
      params.putAll(request.getParameterMap());
    }
    return params;
  }

  /**
   * Generate.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward generate(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    String insuranceId = req.getParameter("insurance_id");
    BasicDynaBean bean = InsuranceDAO.getClaimContent(Integer.parseInt(insuranceId));

    String billNo = (String) bean.get("bill_no");
    String visitId = (String) bean.get("patient_id");

    String fileName = "";

    if (Integer.parseInt(bean.get("claim_template_id").toString()) == 0
        && bean.get("default_claim_template").toString().equals("R")) {
      fileName = "Default RTF Template";
    } else if (Integer.parseInt(bean.get("claim_template_id").toString()) == 0
        && bean.get("default_claim_template").toString().equals("P")) {
      fileName = "Default HTML Template";
    } else {
      fileName = bean.get("template_name").toString();
    }

    fileName = fileName + ".rtf";

    String mrno = bean.get("mr_no").toString();
    OutputStream os = res.getOutputStream();
    String htmlContent = fillValuesInFTL(billNo, visitId, insuranceId, "ClaimTemplateRTF.ftl",
        (String) bean.get("template_content"));
    res.setHeader("Content-disposition", "attachment; filename=\"" + mrno + "_" + fileName + "\"");

    res.setContentType("application/rtf");
    os.write(htmlContent.getBytes());
    os.flush();
    os.close();
    return null;
  }

  /**
   * Prints the HTML.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward printHTML(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws Exception {

    int printerId = 0;
    if ((null != req.getParameter("printDefType"))
        && !("").equals(req.getParameter("printDefType"))) {
      printerId = Integer.parseInt(req.getParameter("printDefType"));
    }
    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_INSURENCE, printerId);
    HtmlConverter hc = new HtmlConverter();
    String insuranceId = req.getParameter("insurance_id");
    BasicDynaBean bean = InsuranceDAO.getClaimTemplateContent(Integer.parseInt(insuranceId));
    if (printPref.get("print_mode").equals("P")) {
      OutputStream os = res.getOutputStream();
      res.setContentType("application/pdf");
      try {
        hc.writePdf(os, (String) bean.get("doc_content_html"), (String) bean.get("template_name"),
            printPref, false, false, true, true, true, false);
      } catch (Exception exception) {
        logger.error("Generated HTML content:");
        logger.error((String) bean.get("doc_content_html"));
      }
      os.close();
      return null;
    } else {
      String textReport = null;
      textReport = (String) bean.get("doc_content_html");
      textReport = new String(hc.getText(textReport, "Claim Template", printPref, true, true));
      req.setAttribute("textReport", textReport);
      req.setAttribute("textColumns", printPref.get("text_mode_column"));
      req.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }

  }

  /**
   * Regenerate HTML.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws NumberFormatException the number format exception
   * @throws SQLException          the SQL exception
   * @throws IOException           Signals that an I/O exception has occurred.
   * @throws TemplateException     the template exception
   */
  public ActionForward regenerateHTML(ActionMapping mapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res)
      throws NumberFormatException, SQLException, IOException, TemplateException {

    String insuranceId = req.getParameter("insurance_id");
    String mode = req.getAttribute("mode") != null ? (String) req.getAttribute("mode") : "";

    BasicDynaBean bean = InsuranceDAO.getClaimContent(Integer.parseInt(insuranceId));

    String billNo = (String) bean.get("bill_no");
    String visitId = (String) bean.get("patient_id");

    req.setAttribute("mode", mode);

    String htmlContent = fillValuesInFTL(billNo, visitId, insuranceId, "InsuranceClaimTemplate.ftl",
        (String) bean.get("template_content"));
    bean.set("doc_content_html", htmlContent);

    if (bean.get("claim_docs_id") == null) {
      req.setAttribute("mode", "insert");
    }
    req.setAttribute("ClaimDetails", bean);
    String path = "/pages/Insurance/InsuranceClaim/InsClaimHtmlTemplate.jsp";

    return new ActionForward(path);
  }
}
