package com.insta.hms.common;

import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.stores.StockEntryDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class GenerateBarCode.
 */
public class GenerateBarCode extends Action {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(GenerateBarCode.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @IgnoreConfidentialFilters
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException,
      ParseException, TemplateException, DocumentException {

    Map paramMap = new HashMap();
    String barCodeType = request.getParameter("barcodeType");
    String plainText = null;
    String templateContent = null;
    FtlReportGenerator ftlReportGen = null;
    PrintTemplatesDAO dao = new PrintTemplatesDAO();
    String visitId = request.getParameter("visitId");

    // Registration barcode
    if ("Reg".equals(barCodeType)) {
      BasicDynaBean patientDynaBean = PatientDetailsDAO
          .getPatientGeneralDetailsBean(request.getParameter("mrno"));
      paramMap.put("patient", patientDynaBean);
      if (visitId != null) {
        paramMap.put("visitId", visitId);
      } else {
        visitId = " ";
        paramMap.put("visitId", visitId);
      }
      templateContent = dao.getCustomizedTemplate(PrintTemplate.RegBarCode);

      if (templateContent == null || templateContent.equals("")) {
        ftlReportGen = new FtlReportGenerator("RegistrationBarCodeTextTemplate");
      } else {
        StringReader reader = new StringReader(templateContent);
        ftlReportGen = new FtlReportGenerator("RegistrationBarCodeTextTemplatePrint", reader);
      }
      plainText = ftlReportGen.getPlainText(paramMap);
      request.setAttribute("textReport", plainText);
    } else if ("Item".equals(barCodeType)) {
      templateContent = dao.getCustomizedTemplate(PrintTemplate.ItemBarCode);

      if (templateContent == null || templateContent.equals("")) {
        ftlReportGen = new FtlReportGenerator("StoreItemBarcodePrint");
      } else {
        StringReader reader = new StringReader(templateContent);
        ftlReportGen = new FtlReportGenerator("StoreItemBarcodePrint", reader);
      }
      paramMap.put("ItemDetails",
          StockEntryDAO.getItemBarcodeDetails(request.getParameter("grnno")));
      plainText = ftlReportGen.getPlainText(paramMap);
      request.setAttribute("textReport", plainText);
    } else if ("ItemMaster".equals(barCodeType)) {
      templateContent = dao.getCustomizedTemplate(PrintTemplate.ItemBarCode);

      if (templateContent == null || templateContent.equals("")) {
        ftlReportGen = new FtlReportGenerator("StoreItemBarcodePrint");
      } else {
        StringReader reader = new StringReader(templateContent);
        ftlReportGen = new FtlReportGenerator("StoreItemBarcodePrint", reader);
      }
      Map itemDetailsMap = null;
      String itemBatchId = request.getParameter("item_batch_id");
      String noOfPrints = request.getParameter("noOfPrints");

      itemDetailsMap = StockEntryDAO.getItemBarcodeDetails(
          Integer.parseInt(request.getParameter("itemId")), noOfPrints, itemBatchId);
      paramMap.put("ItemDetails", itemDetailsMap);
      plainText = ftlReportGen.getPlainText(paramMap);
      request.setAttribute("textReport", plainText);
    }
    request.setAttribute("printerType", "BARCODE");
    return mapping.findForward("textPrintApplet");
  }
}
