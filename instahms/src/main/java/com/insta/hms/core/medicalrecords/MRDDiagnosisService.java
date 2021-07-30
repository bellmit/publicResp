package com.insta.hms.core.medicalrecords;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.mrdcodes.MrdCodeRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * The Class MRDDiagnosisService.
 *
 * @author anup vishwas
 */

@Service
public class MRDDiagnosisService {

  /** The mrd diagnosis repository. */
  @LazyAutowired
  private MRDDiagnosisRepository mrdDiagnosisRepository;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The mrd diagnosis validator. */
  @LazyAutowired
  private MRDDiagnosisValidator mrdDiagnosisValidator;
  
  public static final Integer ITEMS_LIMIT = 10;

  /** The log. */
  private static Logger log = LoggerFactory
      .getLogger(MRDDiagnosisService.class);

  /**
   * Copy diag codes.
   *
   * @param visitDetailsBean
   *          the visit details bean
   * @param latestEpisodeVisitId
   *          the latest episode visit id
   * @param username
   *          the username
   * @return true, if successful
   */
  public boolean copyDiagCodes(BasicDynaBean visitDetailsBean,
      String latestEpisodeVisitId, String username) {
    boolean success = true;
    String opType = (String) visitDetailsBean.get("op_type");

    if (latestEpisodeVisitId != null && opType != null
        && (opType.equals("F") || opType.equals("D"))) {

      String visitId = (String) visitDetailsBean.get("patient_id");

      List<BasicDynaBean> previousVisitDiagCodes = mrdDiagnosisRepository
          .listAll(null, "visit_id", latestEpisodeVisitId);
      if (previousVisitDiagCodes != null && !previousVisitDiagCodes.isEmpty()) {
        for (BasicDynaBean diagbean : previousVisitDiagCodes) {
          BigDecimal id = new BigDecimal(
              DatabaseHelper.getNextSequence("mrd_diagnosis"));
          diagbean.set("id", id);
          diagbean.set("visit_id", visitId);
          diagbean.set("username", username);
          diagbean.set("mod_time", DateUtil.getCurrentTimestamp());
        }

        mrdDiagnosisRepository.batchInsert(previousVisitDiagCodes);
      }
    }
    return success;
  }

  /**
   * Gets the all diagnosis details.
   *
   * @param patientId
   *          the patient id
   * @return the all diagnosis details
   */
  public List<BasicDynaBean> getAllDiagnosisDetails(String patientId) {

    return mrdDiagnosisRepository.getAllDiagnosisDetails(patientId);
  }

  /**
   * Gets the prev diagnosis details.
   *
   * @param patientId
   *          the patient id
   * @return the prev diagnosis details
   */
  public List<BasicDynaBean> getPrevDiagnosisDetails(String patientId, Integer pageNo) {
    BasicDynaBean patientBean = registrationService.findByKey(patientId);
    ValidationErrorMap errMap = new ValidationErrorMap();
    if (patientBean == null) {
      errMap.addError("patient_id", "exception.form.notvalid.patient.id");
      throw new ValidationException(errMap);
    }
    LocalDate regDate = new LocalDate((Date) patientBean.get("reg_date"));
    LocalTime regTime = new LocalTime((Time) patientBean.get("reg_time"));
    LocalDateTime localDateTime = regDate.toLocalDateTime(regTime);
    Timestamp dateTime = new Timestamp(localDateTime.toDateTime().getMillis());

    return mrdDiagnosisRepository.getPreviousDiagnosisDetails(
        (String) patientBean.get("mr_no"), dateTime, ITEMS_LIMIT, pageNo);
  }

  /**
   * Gets the onset year.
   *
   * @param mrNo
   *          the mr no
   * @param diagCode
   *          the diag code
   * @return the onset year
   */
  public BasicDynaBean getOnsetYear(String mrNo, String diagCode) {
    mrdDiagnosisValidator.validateDiagnosisCodeAndPatient(mrNo, diagCode);
    return mrdDiagnosisRepository.getYearOfOnsetDetails(mrNo, diagCode);
  }

  /**
   * Find all coder entered diagnosis from hospital_claim_diagnosis table.
   *
   * @param visitId
   *          the visit id
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> findAllCoderDiagnosis(String visitId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(MrdCodeRepository.FIND_CODER_DIAGNOSIS,
        visitId);
  }

  /**
   * Find all diagnosis entered by doctor, from mrd_diagnosis table.
   *
   * @param visitId
   *          the visit id
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> findAllDiagnosis(String visitId)
      throws SQLException {
    List<BasicDynaBean> dynaList = null;
    try {
      dynaList = DataBaseUtil.queryToDynaList(MrdCodeRepository.FIND_DIAGNOSIS,
          visitId);

    } catch (SQLException exp) {
      log.info("Find all diagnosis sql exception" + exp.getMessage());
    }

    return dynaList;
  }
}
