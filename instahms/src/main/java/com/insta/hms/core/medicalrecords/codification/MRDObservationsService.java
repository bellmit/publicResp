package com.insta.hms.core.medicalrecords.codification;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.documents.PatientDocumentService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.mdm.section.fields.FieldsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class MRDObservationsService.
 *
 * @author krishnat
 */
@Service
public class MRDObservationsService {

  /** The fservice. */
  @LazyAutowired
  FieldsService fservice;

  /** The obsvr repo. */
  @LazyAutowired
  MRDObservationsRepository obsvrRepo;

  /** The patient doc service. */
  @LazyAutowired
  PatientDocumentService patientDocService;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MRDObservationsService.class);

  /** The Constant DOCUMENT_ID. */
  private static final String DOCUMENT_ID = "document_id";

  /** The Constant CHARGE_ID. */
  private static final String CHARGE_ID = "charge_id";

  /** The Constant OBSERVATION_TYPE. */
  private static final String OBSERVATION_TYPE = "observation_type";

  /** The Constant OBSERVATION_CODE. */
  private static final String OBSERVATION_CODE = "observation_code";

  /** The Constant VALUE. */
  private static final String VALUE = "value";

  /**
   * Gets the observation id.
   *
   * @param consultationId
   *          the consultation id
   * @param obsType
   *          the obs type
   * @param obsCode
   *          the obs code
   * @return the observation id
   */
  public int getObservationId(int consultationId, String obsType,
      String obsCode) {
    BasicDynaBean bean = obsvrRepo.getObservationRecord(consultationId, obsType,
        obsCode);
    Integer observationId = 0;
    if (bean != null) {
      observationId = (Integer) bean.get("observation_id");
    }
    return observationId;
  }

  /**
   * Insert patient document.
   *
   * @param file
   *          the file
   * @return the integer
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private Integer insertPatientDocument(MultipartFile file) throws IOException {
    Integer patientDocId = patientDocService.getNextSequence();
    BasicDynaBean patientDocBean = patientDocService.getBean();
    patientDocBean.set("doc_id", patientDocId);
    patientDocBean.set("filename", file.getOriginalFilename());
    patientDocBean.set("doc_content_bytea",
        new ByteArrayInputStream(file.getBytes()));
    patientDocBean.set("doc_format", "doc_fileupload");
    patientDocBean.set("doc_type", "");
    if (!file.getOriginalFilename().isEmpty()) {
      patientDocBean.set("original_extension",
          FilenameUtils.getExtension(file.getOriginalFilename()));
    }
    patientDocBean.set("content_type", file.getContentType());
    patientDocService.insert(patientDocBean);
    return patientDocId;
  }

  /**
   * Save mrd observations given a charge id and observations array.
   *
   * @param dto
   *          the dto
   * @param files
   *          the files
   * @return true, if successful. false if no update was done.
   * @throws SQLException
   *           Signals that an I/O exception has occurred.
   */
  public MrdObservationsModel[] saveMrdObservations(MrdObservationsModel[] dto,
      List<MultipartFile> files) throws SQLException {
    Map<String, Object> sessionAttributes = sessionService
        .getSessionAttributes();
    BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id",
        sessionAttributes.get("centerId"));
    String healthAuthority = (String) centerbean.get("health_authority");
    try {
      int fileIndex = 0;

      List<BasicDynaBean> mrdObsBeanList = new ArrayList<>();
      HashSet<Integer> ignoreDocumentIds = new HashSet<>();
      for (int i = 0; i < dto.length; i++) {
        if (dto[i].getObservationType().equalsIgnoreCase("file")) {
          // if the user doesn't choose a file ie,. size==-1 then put doc_id as
          // 0
          if ((dto[i].getDocumentId() == null || dto[i].getDocumentId() == 0)
              && files.get(fileIndex).getBytes().length > 0) {
            dto[i].setDocumentId(insertPatientDocument(files.get(fileIndex)));
            fileIndex++;
          } else {
            // don't delete this record
            ignoreDocumentIds.add(dto[i].getDocumentId());
          }

          // set code and value type based on health authority
          if (healthAuthority.equalsIgnoreCase("DHA")) {
            dto[i].setValueType("File");
          } else if (healthAuthority.equalsIgnoreCase("HAAD")) {
            dto[i].setValueType("Base64PDF");
          }
        }
        mrdObsBeanList.add(setDtoToBean(dto[i]));
      }
      deleteObservations(dto[0].getChargeId(), ignoreDocumentIds);
      obsvrRepo.batchInsert(mrdObsBeanList);
    } catch (IOException exception) {
      logger.error("", exception);
    }

    return dto;

  }

  /**
   * Delete observations based on given chargeId. Finds the patient document
   * entries for given charge and deletes them as well.
   *
   * @param chargeId
   *          the charge id
   * @param ignoreDocumentIds
   *          a hashset containing the document ids to not delete.
   */
  public void deleteObservations(String chargeId,
      Set<Integer> ignoreDocumentIds) {
    // delete corresponding patient document entries
    List<String> selectCols = new ArrayList<>();
    selectCols.add(DOCUMENT_ID);
    List<BasicDynaBean> obsBeanList = obsvrRepo.listAll(selectCols, CHARGE_ID,
        chargeId);
    if (!obsBeanList.isEmpty()) {
      List<Object> obsObjList = new ArrayList<>();
      for (BasicDynaBean bean : obsBeanList) {
        if (bean.get(DOCUMENT_ID) != null
            && !ignoreDocumentIds.contains(bean.get(DOCUMENT_ID))) {
          obsObjList.add(bean.get(DOCUMENT_ID));
        }
      }
      if (!obsObjList.isEmpty()) {
        patientDocService.batchDelete(obsObjList);
      }
    }
    // delete observations from mrd_observations
    if (StringUtils.isNotEmpty(chargeId)) {
      Map<String, Object> key = new HashMap<>();
      key.put(CHARGE_ID, chargeId);
      obsvrRepo.delete(key);
    }
  }

  /**
   * Sets the mrd observations dto to bean.
   *
   * @param dto
   *          the dto
   * @return the basic dyna bean
   */
  private BasicDynaBean setDtoToBean(MrdObservationsModel dto) {
    BasicDynaBean obsBean = obsvrRepo.getBean();
    obsBean.set(CHARGE_ID, dto.getChargeId());
    obsBean.set(OBSERVATION_TYPE, dto.getObservationType());
    obsBean.set("code", dto.getCode());
    obsBean.set(VALUE, dto.getValue());
    obsBean.set("value_type", dto.getValueType());
    if (dto.getDocumentId() != null) {
      obsBean.set(DOCUMENT_ID, dto.getDocumentId());
    }
    return obsBean;
  }

  /**
   * Save observations.
   *
   * @param fields
   *          the fields
   * @param options
   *          the options
   * @param fieldDescList
   *          the field desc list
   * @param consultationId
   *          the consultation id
   * @param paramFields
   *          the param fields
   * @return true, if successful
   */
  public boolean saveObservations(List<BasicDynaBean> fields,
      List<BasicDynaBean> options,
      Map<Integer, List<BasicDynaBean>> fieldDescList, int consultationId,
      List<Map> paramFields) {
    boolean flag = true;
    for (Map field : paramFields) {
      List<BasicDynaBean> optionDesc = fieldDescList.get(field.get("field_id"));

      String fieldType = (String) optionDesc.get(0).get("field_type");
      String obsType = (String) optionDesc.get(0).get(OBSERVATION_TYPE);
      String obsCode = (String) optionDesc.get(0).get(OBSERVATION_CODE);

      if (obsCode != null && !obsCode.equals("") && obsType != null
          && !obsType.equals("")) {

        int obsId = getObservationId(consultationId, obsType, obsCode);
        if (obsId > 0) {
          if (fieldType.equals("text") || fieldType.equals("wide text")) {
            String fieldRemarks = (String) field.get("field_remarks");
            if (fieldRemarks == null || !fieldRemarks.equals("")) {
              obsvrRepo.updateObservations(consultationId, obsId, obsType,
                  obsCode, fieldRemarks);
            }
          } else if (fieldType.equals("dropdown")
              || fieldType.equals("checkbox")) {
            obsvrRepo.deleteObservations(consultationId, obsType, obsCode);
            saveDropDownMrdObservation(consultationId, field, obsType, obsCode);
          }
        } else {

          if (fieldType.equals("text") || fieldType.equals("wide text")) {
            String fieldRemarks = (String) field.get("field_remarks");
            if (fieldRemarks != null && !fieldRemarks.equals("")) {
              obsvrRepo.insertObservations(consultationId, obsType, obsCode,
                  fieldRemarks);
            }
          } else if (fieldType.equals("dropdown")
              || fieldType.equals("checkbox")) {
            saveDropDownMrdObservation(consultationId, field, obsType, obsCode);
          }
        }
      }

    }

    return flag;
  }

  /**
   * Save drop down mrd observation.
   *
   * @param consultationId
   *          the consultation id
   * @param field
   *          the field
   * @param obsType
   *          the obs type
   * @param obsCode
   *          the obs code
   */
  private void saveDropDownMrdObservation(int consultationId, Map field,
      String obsType, String obsCode) {
    List<Map<String, String>> options = (List) field.get("options");

    for (Map<String, String> option : options) {
      if (option.get("available") != null
          && !option.get("available").trim().isEmpty()) {
        obsvrRepo.insertObservations(consultationId, obsType, obsCode,
            (String) option.get("value_code"));
      }
    }
  }

  /**
   * Gets the presenting complaint.
   *
   * @param chargeId
   *          the charge id
   * @return the presenting complaint
   */
  public BasicDynaBean getPresentingComplaint(String chargeId) {
    return obsvrRepo.getPresentingComplaint(chargeId);
  }

  /**
   * Insert presenting complaint.
   *
   * @param complaintVal
   *          the complaint val
   * @param chargeId
   *          the charge id
   * @return true, if successful
   */
  public boolean insertPresentingComplaint(String complaintVal,
      String chargeId) {
    BasicDynaBean bean = obsvrRepo.getBean();
    bean.set(CHARGE_ID, chargeId);
    bean.set(OBSERVATION_TYPE, "Text");
    bean.set("code", "Presenting-Complaint");
    bean.set(VALUE, complaintVal);
    bean.set("value_type", "Presenting-Complaint");
    bean.set("value_editable", "Y");
    return obsvrRepo.insert(bean) == 1;
  }

  /**
   * Update presenting complaint.
   *
   * @param complaintVal
   *          the complaint val
   * @param observationId
   *          the observation id
   * @return true, if successful
   */
  public boolean updatePresentingComplaint(String complaintVal,
      Integer observationId) {
    BasicDynaBean bean = obsvrRepo.getBean();
    bean.set(VALUE, complaintVal);
    Map<String, Object> keys = new HashMap<>();
    keys.put("observation_id", observationId);
    return obsvrRepo.update(bean, keys) == 1;
  }

  /**
   * Gets the attach document file given the document Id. 
   * If the given mrNo has access to the docId.
   *
   * @param docId the doc id
   * @param mrNo the mr no
   * @return the attach document file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public File getAttachDocumentFile(Integer docId, String mrNo) throws IOException {
    List<String> docIds = new ArrayList<String>();
    docIds.add(Integer.toString(docId));
    if (patientDocService.getAssociatedMrNo(docIds).contains(mrNo)) {
      return patientDocService.getAttachDocumentFile(docId);
    } else {
      return null;
    }
  }
}
