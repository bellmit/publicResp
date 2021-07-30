package com.insta.hms.documents;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.mdm.printtemplates.PrintTemplate;
import com.insta.hms.mdm.printtemplates.PrintTemplateService;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GenerateBarCodeService.
 */
@Service
public class GenerateBarCodeService extends BusinessService {

  /** The printtemplateservice. */
  @LazyAutowired
  private PrintTemplateService printtemplateservice;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(GenerateBarCodeService.class);

  /**
   * Execute.
   *
   * @param params the params
   * @param requestMap the request map
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   */
  public void execute(Map<String, String[]> params, Map<String, Object> requestMap)
      throws IOException, TemplateException, DocumentException {

    String barCodeType = null;
    if (null != params.get("barcodeType")) {
      barCodeType = params.get("barcodeType")[0];
    }
    String plainText = null;
    String templateContent = null;
    FtlReportGenerator ftlGen = null;
    Map paramMap = new HashMap();

    // Registration barcode
    if ("Reg".equals(barCodeType)) {
      paramMap.put("patient",
          PatientDetailsRepository.getPatientGeneralDetailsBean(params.get("mrno")[0]));
      templateContent = printtemplateservice.getCustomizedTemplate(PrintTemplate.RegBarCode);

      if (templateContent == null || templateContent.equals("")) {
        ftlGen = new FtlReportGenerator("RegistrationBarCodeTextTemplate");
      } else {
        StringReader reader = new StringReader(templateContent);
        ftlGen = new FtlReportGenerator("RegistrationBarCodeTextTemplatePrint", reader);
      }
      plainText = ftlGen.getPlainText(paramMap);
      requestMap.put("textReport", plainText);
    }
    // } else if ("Item".equals(barCodeType)) {
    // templateContent =
    // dao.getCustomizedTemplate(PrintTemplate.ItemBarCode);
    //
    // if (templateContent == null || templateContent.equals("")) {
    // fGen = new FtlReportGenerator("StoreItemBarcodePrint");
    // } else {
    // StringReader reader = new StringReader(templateContent);
    // fGen = new FtlReportGenerator("StoreItemBarcodePrint",reader);
    // }
    // paramMap.put("ItemDetails",
    // StockEntryDAO.getItemBarcodeDetails(request.getParameter("grnno")));
    // plainText = fGen.getPlainText(paramMap);
    // request.setAttribute("textReport", plainText);
    // } else if ("ItemMaster".equals(barCodeType)) {
    // templateContent =
    // dao.getCustomizedTemplate(PrintTemplate.ItemBarCode);
    //
    // if (templateContent == null || templateContent.equals("")) {
    // fGen = new FtlReportGenerator("StoreItemBarcodePrint");
    // } else {
    // StringReader reader = new StringReader(templateContent);
    // fGen = new FtlReportGenerator("StoreItemBarcodePrint",reader);
    // }
    // Map itemDetailsMap = null;
    // String item_batch_id = request.getParameter("item_batch_id");
    // String noOfPrints = request.getParameter("noOfPrints");
    //
    // itemDetailsMap =
    // StockEntryDAO.getItemBarcodeDetails(Integer.parseInt(
    // request.getParameter("itemId")),noOfPrints,item_batch_id);
    // paramMap.put("ItemDetails", itemDetailsMap);
    // plainText = fGen.getPlainText(paramMap);
    // request.setAttribute("textReport", plainText);
    // }
    return;
  }

}
