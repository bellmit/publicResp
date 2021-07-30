package com.insta.hms.core.clinical.documentsforms;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.documents.PatientGeneralDocsRepository;
import com.insta.hms.forms.PatientFormDetailsRepository;
import com.insta.hms.mdm.documenttypes.DocumentTypeRepository;
import com.insta.hms.mdm.formcomponents.FormComponentsRepository;
import com.insta.hms.mdm.formcomponents.FormComponentsService.FormType;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DocumentsFormsService.
 */
@Service
public class DocumentsFormsService extends SystemSectionService {

  @LazyAutowired
  private PatientFormDetailsRepository patFormDetsRepository;

  @LazyAutowired
  private PatientGeneralDocsRepository patientGeneralDocsRepository;

  @LazyAutowired
  private FormComponentsRepository formCompRepo;

  @LazyAutowired
  private DocumentTypeRepository documentTypeRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;


  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {
    return null;
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    return getSectionDetailsFromCurrentForm(parameter);
  }

  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    Map<String, Object> recordsResponse = new HashMap<>();
    recordsResponse.put("records", getDocumentsAndForms(parameter.getPatientId(), "CLN"));
    return recordsResponse;
  }

  /**
   * Gets the documents and forms.
   *
   * @param visitId the visit id
   * @param docCategory the doc category
   * @return the documents and forms
   */
  public Map<String, Object> getDocumentsAndForms(String visitId, String docCategory) {
    String documentList = "documents_list";
    Map<String, Object> patientDocMap = new HashMap<>();
    HashMap urlRightsMap = (HashMap) sessionService
        .getSessionAttributes(new String[] {"urlRightsMap"}).get("urlRightsMap");
    String genericFormScreenRights = (String) (urlRightsMap.get("patient_generic_form_list") != null
        ? urlRightsMap.get("patient_generic_form_list")
        : "N");
    String genericDocumentScreenRights =
        (String) (urlRightsMap.get("generic_documents_list") != null
            ? urlRightsMap.get("generic_documents_list")
            : "N");

    List<BasicDynaBean> documentListBeans = Collections.emptyList();
    List<BasicDynaBean> formListBeans = Collections.emptyList();

    if (genericDocumentScreenRights.equals("A")) {
      documentListBeans = patientGeneralDocsRepository.listPatientDocuments(visitId, docCategory);
    }
    if (genericFormScreenRights.equals("A")) {
      formListBeans = patFormDetsRepository.listPatientForms(visitId, FormType.Form_Gen.toString(),
          docCategory);
    }
    List<BasicDynaBean> documentationList =
        mergeDocumentsAndFormsList(documentListBeans, formListBeans);
    patientDocMap.put(documentList, ConversionUtils.copyListDynaBeansToMap(documentationList));
    patientDocMap.put("doc_category", docCategory);
    return patientDocMap;
  }

  private List<BasicDynaBean> mergeDocumentsAndFormsList(List<BasicDynaBean> documentListBeans,
      List<BasicDynaBean> formListBeans) {
    List<BasicDynaBean> mergedList = new ArrayList<>();
    if (documentListBeans.size() == 0) {
      return formListBeans;
    }
    if (formListBeans.size() == 0) {
      return documentListBeans;
    }
    int formIndex = 0;
    int docIndex = 0;
    String createdDate = "created_date";
    while (formIndex < formListBeans.size() && docIndex < documentListBeans.size()) {
      BasicDynaBean formBean = formListBeans.get(formIndex);
      BasicDynaBean docBean = documentListBeans.get(docIndex);
      Date formDate = (Timestamp) formBean.get(createdDate);
      Date docDate = (Timestamp) docBean.get(createdDate);
      formBean.set(createdDate, formDate);
      docBean.set(createdDate, docDate);
      if (formDate.compareTo(docDate) >= 0) {
        mergedList.add(formBean);
        formIndex++;
      } else {
        mergedList.add(docBean);
        docIndex++;
      }
    }
    while (formIndex < formListBeans.size()) {

      mergedList.add(formListBeans.get(formIndex));
      formIndex++;
    }
    while (docIndex < documentListBeans.size()) {
      mergedList.add(documentListBeans.get(docIndex));
      docIndex++;
    }
    return mergedList;
  }
}
