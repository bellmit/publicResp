package com.insta.hms.core.clinical.forms;

import com.bob.hms.common.Constants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ConsultationFormService;
import com.insta.hms.core.clinical.dischargesummary.DischargeSummaryService;
import com.insta.hms.core.clinical.ipemr.IpEmrFormService;
import com.insta.hms.core.clinical.surgery.OTRecordPrintService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.core.prints.PrintService;
import com.insta.hms.forms.PatientFormDetailsRepository;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ClinicalFormHl7Adapter {

  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;
  @LazyAutowired
  private IpEmrFormService ipEmrFormService;
  @LazyAutowired
  private RegistrationService registrationService;
  @LazyAutowired
  private ConsultationFormService consultationFormService;
  @LazyAutowired
  private DischargeSummaryService dischargeSummaryService;
  @LazyAutowired
  private OTRecordPrintService otRecordPrintService;
  @LazyAutowired
  private PatientFormDetailsRepository patientFormDetailsRepository;

  private static final String FORM_PDF_DATA = "form_pdf_data";
  private static final String CONSULTATION_ID = "consultation_id";
  private static final String FORM_TYPE = "form_type";
  private static final String FORM_SEGMENT_DATA = "form_segment_data";
  private static final String FORM = "form";
  private static final String ITEM_TYPE = "item_type";


  /**
   * ipemrSaveAndFinaliseEvent triggered on IPEMR save and close postFormSave.
   * 
   * @param patientId the patient id.
   */
  public void ipemrSaveAndFinaliseEvent(String patientId) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(Constants.VISIT_ID, patientId);
    eventData.put(FORM_TYPE, FormComponentsService.FormType.Form_IP.toString());
    eventData.put(ITEM_TYPE, FORM);
    String event =
        ipEmrFormService.isFormReopened(patientId) ? "Ip_Form_Reopen_And_Save" : "Ip_Form_Save";
    interfaceEventMappingService.saveAndFinaliseFormEvent(event.toUpperCase(), eventData);
  }

  /**
   * Triggered on Cons save and finalise postFormSave.
   * 
   * @param consultationId the cons id.
   * @param mrNo the mr NO
   * @param visitId the visit Id
   */
  public void consultationSaveAndFinaliseEvent(int consultationId,  String visitId, String mrNo) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(CONSULTATION_ID, consultationId);
    eventData.put(FORM_TYPE, FormComponentsService.FormType.Form_CONS.toString());
    eventData.put(ITEM_TYPE, FORM);
    eventData.put(Constants.VISIT_ID, visitId);
    eventData.put(Constants.MR_NO, mrNo);
    String event = consultationFormService.isFormReopened(consultationId)
        ? "Cons_Form_Reopen_And_Save" :
        "Cons_Form_Save";
    interfaceEventMappingService.saveAndFinaliseFormEvent(event.toUpperCase(), eventData);
  }

  /**
   * Triggered on Operation finaliseAll from struts OT Forms screen.
   * 
   * @param operationProcId the cons id.
   * @param patientId the patient id.
   * @param mrNo the mr no.
   * @param printerId the printer id.
   * @param printTemplateName print template name.
   */
  public void operationFormSaveAndFinaliseEvent(String operationProcId, String patientId,
      String mrNo, String printerId, String printTemplateName) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("operation_proc_id", Integer.parseInt(operationProcId));
    eventData.put(Constants.VISIT_ID, patientId);
    eventData.put(Constants.MR_NO, mrNo);
    eventData.put("printer_id", printerId);
    eventData.put("print_template", printTemplateName);
    eventData.put(FORM_TYPE, FormComponentsService.FormType.Form_OT.toString());
    eventData.put(ITEM_TYPE, FORM);
    BasicDynaBean bean = patientFormDetailsRepository
        .findByFormId(Integer.parseInt(operationProcId),
            FormComponentsService.FormType.Form_OT.toString());
    boolean isReopened = bean != null ? (boolean) bean.get("is_reopened") : false;
    String event = isReopened ? "Surgery_Form_Reopen_And_Save" : "Surgery_Form_Save";
    interfaceEventMappingService.saveAndFinaliseFormEvent(event.toUpperCase(), eventData);
  }
  
  /**
   * Triggered on Discharge Summary Save and Finalise.
   * @param patientId the patientId
   * @param format the formId
   * @param dischargeDocId the dischargeDocId
   */
  public void dischargeSummarySaveAndFinaliseEvent(String patientId, String format,
      int dischargeDocId) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(Constants.VISIT_ID, patientId);
    eventData.put("format", format);
    eventData.put("discharge_doc_id", dischargeDocId);
    eventData.put(FORM_TYPE, "Form_DIS");
    eventData.put(ITEM_TYPE, FORM);
    String event = registrationService.isFormReopened(patientId, "discharge_summary_reopened")
        ? "Discharge_Form_Reopen_And_Save" :
        "Discharge_Form_Save";
    interfaceEventMappingService.saveAndFinaliseFormEvent(event.toUpperCase(), eventData);
  }

  /**
   * gets clinical form segment data for MDM-T04,T02 for OBX, TXA segments.
   * @param dataMap eventDataMap.
   */
  @SuppressWarnings("unchecked")
public List<Map<String, Object>> getClinicalFormSegmentData(Map<String, Object> dataMap) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();
    String formType = (String) dataMap.get(FORM_TYPE);
    Map<String, Object> map = new HashMap<>();
    switch (formType) {
      case "Form_CONS":
        //OBX segment data
        map.put(FORM_PDF_DATA, consultationFormService
            .getFormDataEncodedByteArray(dataMap.get(CONSULTATION_ID)));
        // TXA segment data
        map.putAll(consultationFormService
            .getFormSegmentInformation(dataMap.get(CONSULTATION_ID)));
        break;
      case "Form_IP":
        map.put(FORM_PDF_DATA, ipEmrFormService
            .getFormDataEncodedByteArray(dataMap.get(Constants.VISIT_ID)));
        map.putAll(
            ipEmrFormService.getFormSegmentInformation(dataMap.get(Constants.VISIT_ID)));
        break;
      case "Form_DIS":
        map.put(FORM_PDF_DATA, dischargeSummaryService
            .getFormDataEncodedByteArray(dataMap.get(Constants.VISIT_ID),
            dataMap.get("format"), dataMap.get("discharge_doc_id")));
        map.putAll(dischargeSummaryService
            .getFormSegmentInformation(dataMap.get(Constants.VISIT_ID)));
        break;
      case "Form_OT":
        map.put(FORM_PDF_DATA, otRecordPrintService
            .getFormDataEncodedByteArray(dataMap.get(Constants.VISIT_ID),
            dataMap.get(Constants.MR_NO), dataMap.get("printer_id"),
            dataMap.get("operation_proc_id"), dataMap.get("print_template"),
            PrintService.ReturnType.PDF_BYTES));
        map.put(FORM_SEGMENT_DATA, patientFormDetailsRepository
            .getFormSegmentInformation((int) dataMap.get("operation_proc_id"), formType));
        break;
      default:
        return Collections.emptyList();
    }
    dataMapList.add(map);
    return dataMapList;
  }
}
