package com.insta.hms.documents;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.documentsforms.DocumentsFormsService;
import com.insta.hms.core.emr.EmrDocFilterService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.documenttypes.DocumentTypeRepository;
import com.insta.hms.mdm.documenttypes.DocumentTypeService;
import com.insta.hms.mdm.formcomponents.FormComponentsRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDocumentsSevice.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Service
public class GenericDocumentsService extends DocumentsService {

  /**
   * Instantiates a new generic documents sevice.
   *
   * @param store
   *          the store
   */
  public GenericDocumentsService(GeneralDocumentStore store) {
    super(store);
    // TODO Auto-generated constructor stub
  }

  /** The emr doc filter service. */
  @LazyAutowired
  EmrDocFilterService emrDocFilterService;

  /** The document type service. */
  @LazyAutowired
  DocumentTypeService documentTypeService;

  /** The patient general docs repository. */
  @LazyAutowired
  PatientGeneralDocsRepository patientGeneralDocsRepository;

  @LazyAutowired
  private PatientRegistrationRepository patRegRepository;
  
  @LazyAutowired
  private DocumentsFormsService docFormService;

  @LazyAutowired
  private FormComponentsRepository formComponentsRepository;

  @LazyAutowired
  private DocumentTypeRepository documentTypeRepository;

  /**
   * Get uploded documents.
   * 
   * @param request
   *          request object
   * @return returns list of uploded documents
   * @throws IOException
   *           throws IO Exception
   * @throws ParseException
   *           throws Parse Exception
   */
  @Override
  public List<Map> getUploadedDocuments(HttpServletRequest request) throws IOException,
      ParseException {

    Map<String, String[]> params = request.getParameterMap();
    String mrNo = params.get("mr_no") != null ? params.get("mr_no")[0] : null;
    String patientId = params.get("patient_id") != null ? params.get("patient_id")[0] : null;
    List<Map> result = new ArrayList<>();
    List<Map> map = null;
    if (patientId == null) {
      map = ConversionUtils.copyListDynaBeansToMap(getUploadedDocumentsList("mrNo", mrNo, null,
          null));
    } else {
      map = ConversionUtils.copyListDynaBeansToMap(getUploadedDocumentsList("patientId", patientId,
          null, null));
    }
    String category = params.get("category") != null ? params.get("category")[0] : null;
    List<Map> documentsFilteredByCat = emrDocFilterService.applyCategoryFilter(map, category);
    if (documentsFilteredByCat == null || documentsFilteredByCat.size() <= 0) {
      return documentsFilteredByCat;
    }
    result = emrDocFilterService.applyFilter(documentsFilteredByCat, request);
    return result;
  }

  /**
   * Get document contents by docId.
   * 
   * @param docId
   *          document id
   * @return returns byte array
   */
  @Override
  public byte[] getDocumentContentByDocId(Integer docId) {
    BasicDynaBean bean = patientGeneralDocsRepository.getDocumentContent(docId);
    InputStream is = (InputStream) bean.get("doc_content_bytea");
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int noOfRead;
    byte[] data = new byte[1024];
    try {
      while ((noOfRead = is.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, noOfRead);
      }
    } catch (IOException ioException) {
      // TODO Auto-generated catch block
      ioException.printStackTrace();
    }

    try {
      buffer.flush();
    } catch (IOException ioException) {
      // TODO Auto-generated catch block
      ioException.printStackTrace();
    }
    byte[] byteArray = buffer.toByteArray();
    return byteArray;
  }

  /**
   * Gets the uploaded documents list.
   *
   * @param key
   *          the key
   * @param valueCol
   *          the value col
   * @param specialized
   *          the specialized
   * @param specializedDocType
   *          the specialized doc type
   * @return the uploaded documents list
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public List<BasicDynaBean> getUploadedDocumentsList(String key, Object valueCol,
      Boolean specialized, String specializedDocType) throws IOException {
    return patientGeneralDocsRepository.getUploadedDocList(key, (String) valueCol);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.documents.DocumentsService#getDocumentTypesByCatAndEmrRules(java.lang.String,
   * java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map> getDocumentTypesByCatAndEmrRules(String docTypeCatName, String visitId,
      String specialized, HttpServletRequest request) throws ParseException {
    List<BasicDynaBean> docTypesByCategory = documentTypeService.getDocTypesByCategory(
        docTypeCatName, specialized);
    List<BasicDynaBean> results = emrDocFilterService.applyFilterOnDocTypes(docTypesByCategory,
        visitId, request);
    return ConversionUtils.listBeanToListMap(results);
  }

  /**
   * List patient documentation.
   *
   * @param visitId the visit id
   * @param docCategory the doc category
   * @return the map
   */
  public Map<String, Object> listPatientDocumentation(String visitId, String docCategory) {
    ValidationErrorMap errorMap = new ValidationErrorMap();
    BasicDynaBean patRegBean = patRegRepository.findByKey("patient_id", visitId);
    if (patRegBean == null) {
      errorMap.addError("patient_id", "exception.documentation.patient.id.not.found");
      throw new ValidationException(errorMap);
    }
    return docFormService.getDocumentsAndForms(visitId, docCategory);
  }

  /**
   * look up document or form names from master.
   *
   * @param docTypeId doc type
   * @param docCategory doc category
   * @param docItemType document item (form or doc)
   * @param deptId department id
   * @param searchQuery search query word
   * @return template names map
   */
  public Map<String, Object> lookUpTemplateNames(String docTypeId, String docCategory,
      String docItemType, String deptId, String searchQuery) {
    Map<String, Object> templateNames = new HashMap<>();
    switch (docItemType.charAt(0)) {
      case 'F':
        templateNames.put("template_names", ConversionUtils.copyListDynaBeansToMap(
            formComponentsRepository
                .getGenericFormNamesByCategory(docCategory, docTypeId, deptId, searchQuery)));
        break;
      case 'D':
        templateNames.put("template_names", ConversionUtils.copyListDynaBeansToMap(
            documentTypeRepository
                .getDocTemplateNamesByCategory(docCategory, docTypeId, false, searchQuery)));
        break;
      default:
        return templateNames;
    }
    return templateNames;
  }
}
