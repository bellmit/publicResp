package com.insta.hms.insurance;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ReportPrinter;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class InsuranceDocumentsProvider.
 */
public class InsuranceDocumentsProvider {

  /**
   * List documents.
   *
   * @param insuranceId the insurance id
   * @return the list
   * @throws Exception the exception
   */
  public List<EMRDoc> listDocuments(String insuranceId) throws Exception {

    List<EMRDoc> emrDocList = new ArrayList();

    try {

      List<BasicDynaBean> estimateDocs = InsuranceDAO
          .getEstimateDocs(Integer.parseInt(insuranceId));
      List<BasicDynaBean> preauthDocs = InsuranceDAO.getPreauthDocs(Integer.parseInt(insuranceId));
      List<BasicDynaBean> insDocs = new GenericDAO("insurance_docs").findAllByKey("insurance_id",
          Integer.parseInt(insuranceId));

      for (BasicDynaBean estimateDoc : estimateDocs) {
        EMRDoc emrDocument = new EMRDoc();

        BasicDynaBean printpref = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
        int printerId = (Integer) printpref.get("printer_id");
        emrDocument.setPrinterId(printerId);
        emrDocument.setDocid("Estimate" + "/" + estimateDoc.get("estimate_id"));
        emrDocument.setTitle("Insurance Estimate");
        emrDocument.setDate((java.util.Date) estimateDoc.get("updated_date"));

        String displayUrl = "/pages/Enquiry/billprint.do?_method=getEstimatePrint&insuranceId="
            + insuranceId + "" + "&estimateId=" + estimateDoc.get("estimate_id") + "&printerId="
            + printerId;
        emrDocument.setDisplayUrl(displayUrl);
        emrDocument.setUpdatedBy((String) estimateDoc.get("user_id"));
        emrDocument.setProvider(null);
        emrDocument.setVisitid(null);
        emrDocument.setType("SYS_INS");

        emrDocList.add(emrDocument);
      }
      for (BasicDynaBean preAuthDoc : preauthDocs) {
        EMRDoc emrDocument = new EMRDoc();

        BasicDynaBean printpref = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
        int printerId = (Integer) printpref.get("printer_id");
        emrDocument.setPrinterId(printerId);
        emrDocument.setDocid("Preauth" + "/" + preAuthDoc.get("preauth_doc_id").toString());
        emrDocument.setTitle((String) preAuthDoc.get("doc_name"));
        emrDocument.setDate((java.util.Date) preAuthDoc.get("doc_date"));
        emrDocument.setUpdatedBy(preAuthDoc.get("preauth_username").toString());

        String displayUrl = "/Insurance/PreAuthorizationDocumentsPrint.do?_method=print&doc_id="
            + preAuthDoc.get("preauth_doc_id") + "&printerId=" + printerId;
        emrDocument.setDisplayUrl(displayUrl);
        emrDocument.setProvider(null);
        emrDocument.setVisitid(null);
        emrDocument.setType("SYS_INS");

        emrDocList.add(emrDocument);
      }
      for (BasicDynaBean insDoc : insDocs) {
        EMRDoc emrDoc = new EMRDoc();

        BasicDynaBean printpref = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
        int printerId = (Integer) printpref.get("printer_id");
        emrDoc.setPrinterId(printerId);
        emrDoc.setDocid("InsDocs" + "/" + insDoc.get("doc_id").toString());
        emrDoc.setTitle((String) insDoc.get("doc_name"));
        emrDoc.setDate((java.util.Date) insDoc.get("doc_date"));
        emrDoc.setUpdatedBy((String) insDoc.get("username"));

        String displayUrl = "/Insurance/InsuranceGenericDocumentsPrint.do"
            + "?_method=print&forcePdf=true&printerId="
            + printerId;
        displayUrl += "&doc_id=" + insDoc.get("doc_id");
        emrDoc.setDisplayUrl(displayUrl);
        emrDoc.setProvider(null);
        emrDoc.setType("SYS_INS");
        emrDoc.setVisitid(null);
        emrDocList.add(emrDoc);
      }
      List<BasicDynaBean> claimDocs = InsuranceDAO.getClaimDocs(Integer.parseInt(insuranceId));
      for (BasicDynaBean claimDoc : claimDocs) {
        EMRDoc emrDoc = new EMRDoc();
        String displayUrl = "";

        BasicDynaBean printpref = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
        int printerId = (Integer) printpref.get("printer_id");
        emrDoc.setPrinterId(printerId);
        String format = (String) claimDoc.get("template_type");
        int docid = (Integer) claimDoc.get("insurance_id");

        emrDoc.setDocid("ClaimDocs" + "/" + format + "-" + docid);
        if (claimDoc.get("template_name") == null) {
          if (claimDoc.get("template_type").equals("P")
              || claimDoc.get("template_type").equals("Y")) {
            emrDoc.setTitle("Default PDF Template");
          } else {
            emrDoc.setTitle("Default RTF Template");
          }
        } else {
          emrDoc.setTitle((String) claimDoc.get("template_name"));
        }
        if (claimDoc.get("template_type").equals("P")
            || claimDoc.get("template_type").equals("Y")) {
          displayUrl = "/Insurance/InsuranceClaim.do?_method=printHTML&insurance_id=" + docid
              + "&printerId=" + printerId;
        } else {
          displayUrl = "/Insurance/InsuranceClaim.do?_method=view&insurance_id=" + docid
              + "&printerId=" + printerId;
        }

        emrDoc.setDisplayUrl(displayUrl);
        emrDoc.setDate((java.util.Date) claimDoc.get("doc_date"));
        emrDoc.setUpdatedBy(claimDoc.get("username").toString());
        emrDoc.setProvider(null);
        emrDoc.setType("SYS_INS");
        emrDoc.setVisitid(null);
        emrDocList.add(emrDoc);
      }
      List<BasicDynaBean> tpaDocs = InsuranceDAO.getTpaDocs(Integer.parseInt(insuranceId));
      for (BasicDynaBean tpaDoc : tpaDocs) {
        EMRDoc emrDoc = new EMRDoc();

        BasicDynaBean printpref = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
        int printerId = (Integer) printpref.get("printer_id");
        emrDoc.setPrinterId(printerId);
        emrDoc.setDocid("InsTPADocs" + "/" + tpaDoc.get("tpa_doc_id").toString());
        emrDoc.setTitle((String) tpaDoc.get("document_name"));
        emrDoc.setDate((java.util.Date) tpaDoc.get("doc_recd_date"));
        emrDoc.setUpdatedBy((String) tpaDoc.get("created_by"));

        String displayUrl = "/Insurance/InsuranceViewMessage.do?_method=getDocument&doc_type=R";
        displayUrl += "&attachment_id=" + tpaDoc.get("tpa_doc_id");
        displayUrl += "&mr_no=" + tpaDoc.get("mr_no");
        emrDoc.setDisplayUrl(displayUrl);
        emrDoc.setProvider(null);
        emrDoc.setType("SYS_INS");
        emrDoc.setVisitid(null);
        emrDocList.add(emrDoc);
      }

      return emrDocList;
    } catch (Exception exception) {
      throw exception;
    }
  }

  /**
   * Gets the PDF bytes.
   *
   * @param docidFormat the docid format
   * @param allFields   the all fields
   * @param insuranceid the insurance id
   * @return the PDF bytes
   * @throws Exception the exception
   */
  public List getPDFBytes(String docidFormat, boolean allFields, String insuranceid)
      throws Exception {
    byte[] bytes = null;
    ArrayList alReturnValue = new ArrayList();
    String[] parsed = docidFormat.split("/");
    String type = parsed[0];
    String docIdStr = parsed[1];

    if ("Estimate".equals(type)) {
      HashMap hm = new HashMap();
      hm.put("estimateId", docIdStr);
      hm.put("insuranceId", new Integer(insuranceid));
      bytes = ReportPrinter.getPdfBytes("EstimatePrintSummary", hm, null, null);
      alReturnValue.add(bytes);
      alReturnValue.add("Insurance Estimate");
      alReturnValue.add("application/pdf");

    } else if ("ClaimDocs".equals(type)) {
      String[] format = docIdStr.split("-");

      if (format[0].equals("P") || format[0].equals("Y")) {
        BasicDynaBean bean = InsuranceDAO.getClaimTemplateContent(Integer.parseInt(format[1]));
        String htmlContent = bean.get("doc_content_html").toString();

        BasicDynaBean printPref = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL);

        HtmlConverter hc = new HtmlConverter();

        alReturnValue.add(hc.getPdfBytes(htmlContent, (String) bean.get("template_name"), printPref,
            false, false, false, true, false));

        if (bean.get("default_claim_template") == null
            || bean.get("default_claim_template").equals("")
            || bean.get("default_claim_template").equals("P")
            || bean.get("default_claim_template").equals("Y")) {
          alReturnValue.add("Default PDF Tempalte");
        } else {
          alReturnValue.add((String) bean.get("template_name"));
        }

        alReturnValue.add("application/pdf");

      } else if (format[0].equals("R")) {
        alReturnValue = (ArrayList) InsuranceDAO.getClaimRTFContent(Integer.parseInt(format[1]));
      }

    } else if ("InsDocs".equals(type) || "Preauth".equals(type)) {

      BasicDynaBean bean = InsuranceDAO.getMrnoDetailsFromInsDocs(Integer.parseInt(insuranceid));
      String visitId = null;
      if (bean.get("visit_id") != null) {
        visitId = bean.get("visit_id").toString();
      }
      alReturnValue = (ArrayList) InsuranceDAO.getDocumentBytes(docIdStr, allFields,
          bean.get("mr_no").toString(), visitId);

    } else if ("InsTPADocs".equals(type)) {

      alReturnValue = (ArrayList) InsuranceDAO.getTPADocumentBytes(docIdStr);
    }

    return alReturnValue;
  }

}
