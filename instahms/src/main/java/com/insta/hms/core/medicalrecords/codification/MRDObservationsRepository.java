package com.insta.hms.core.medicalrecords.codification;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class MRDObservationsRepository.
 */
@Repository
public class MRDObservationsRepository extends GenericRepository {

  /** The log. */
  private static Logger log = LoggerFactory
      .getLogger(MRDObservationsRepository.class);

  /**
   * Instantiates a new MRD observations repository.
   */
  public MRDObservationsRepository() {
    super("mrd_observations");
  }

  /** The Constant SELECT_OBSERVATIONS. */
  private static final String SELECT_OBSERVATIONS = "SELECT "
      + " observation_id FROM mrd_observations ob " + " WHERE ob.charge_id=("
      + "   SELECT charge_id FROM bill_activity_charge bac "
      + "   WHERE bac.activity_id=?::text "
      + " AND bac.activity_code='DOC') AND ob.observation_type=? AND ob.code=?";

  /**
   * Gets the observation record.
   *
   * @param consultationId
   *          the consultation id
   * @param obsType
   *          the obs type
   * @param obsCode
   *          the obs code
   * @return the observation record
   */
  public BasicDynaBean getObservationRecord(int consultationId, String obsType,
      String obsCode) {
    Object[] paramObject = new Object[] { consultationId, obsType, obsCode };
    return DatabaseHelper.queryToDynaBean(SELECT_OBSERVATIONS, paramObject);
  }

  /** The Constant UPDATE_OBSERVATIONS. */
  public static final String UPDATE_OBSERVATIONS = "UPDATE "
      + " mrd_observations mo " + " SET (charge_id, observation_type, "
      + " code, value, value_type, value_editable) "
      + " = (bac.charge_id, ?, ?, ?, 'Observation', 'Y') "
      + " FROM  bill_activity_charge bac "
      + " WHERE  bac.activity_id=?::text AND bac.activity_code='DOC' "
      + " AND  mo.observation_id = ? ";

  /**
   * Update observations.
   *
   * @param consultationId
   *          the consultation id
   * @param obsId
   *          the obs id
   * @param obsType
   *          the obs type
   * @param obsCode
   *          the obs code
   * @param optionRemarks
   *          the option remarks
   * @return the int
   */
  public int updateObservations(int consultationId, int obsId, String obsType,
      String obsCode, String optionRemarks) {
    Object[] paramObject = new Object[] { obsType, obsCode, optionRemarks,
        consultationId, obsId };
    return DatabaseHelper.update(UPDATE_OBSERVATIONS, paramObject);
  }

  /** The Constant DELETE_OBSERVATIONS. */
  public static final String DELETE_OBSERVATIONS = "DELETE "
      + " FROM mrd_observations mo "
      + " WHERE mo.observation_type = ? AND mo.code = ? "
      + " AND mo.charge_id IN (SELECT "
      + " charge_id FROM bill_activity_charge bac "
      + " WHERE bac.activity_id = ?::text) ";

  /**
   * Delete observations.
   *
   * @param consultationId
   *          the consultation id
   * @param obsType
   *          the obs type
   * @param obsCode
   *          the obs code
   * @return the int
   */
  public int deleteObservations(int consultationId, String obsType,
      String obsCode) {
    Object[] paramObject = new Object[] { obsType, obsCode, consultationId };
    return DatabaseHelper.delete(DELETE_OBSERVATIONS, paramObject);
  }

  /** The Constant INSERT_OBSERVATIONS. */
  public static final String INSERT_OBSERVATIONS = "INSERT "
      + " INTO mrd_observations (charge_id, "
      + " observation_type, code, value, value_type, value_editable) "
      + " SELECT bac.charge_id, ?, ?, ?, 'Observation', 'Y'"
      + " FROM bill_activity_charge bac "
      + " WHERE bac.activity_id=?::text AND bac.activity_code='DOC'";

  /**
   * Insert observations.
   *
   * @param consultationId
   *          the consultation id
   * @param obsType
   *          the obs type
   * @param obsCode
   *          the obs code
   * @param valueCode
   *          the value code
   * @return the int
   */
  public int insertObservations(int consultationId, String obsType,
      String obsCode, String valueCode) {
    Object[] paramObj = new Object[] { obsType, obsCode, valueCode,
        consultationId };
    return DatabaseHelper.insert(INSERT_OBSERVATIONS, paramObj);
  }

  /** The Constant FIND_OBS_WO_PC. */
  public static final String FIND_OBS_WO_PC = "SELECT " + "value, value_type, "
      + "observation_type AS TYPE, " + "code " + "FROM mrd_observations mo "
      + "WHERE mo.charge_id = ? " + "  AND code != 'Presenting-Complaint' ";

  /**
   * Find all observations.
   *
   * @param chargeId
   *          the charge id
   * @return the list
   */
  public List<BasicDynaBean> findAllObservations(String chargeId) {
    List<BasicDynaBean> queryList = null;
    try {
      queryList = DatabaseHelper.queryToDynaList(FIND_OBS_WO_PC, chargeId);
    } catch (Exception exp) {
      log.info(
          "Unable to prepage dynalist. Exception message ::" + exp.getMessage());
    }

    return queryList;
  }

  /** The get presenting complaint. */
  public static final String GET_PRESENTING_COMPLAINT = " SELECT ob.* "
      + " FROM mrd_observations ob WHERE ob.charge_id = ? "
      + " AND ob.observation_type='Text' AND ob.code='Presenting-Complaint' "
      + " ORDER BY observation_id LIMIT 1 ";

  /**
   * Gets the presenting complaint.
   *
   * @param chargeId
   *          the charge id
   * @return the presenting complaint
   */
  public BasicDynaBean getPresentingComplaint(String chargeId) {
    return DatabaseHelper.queryToDynaBean(GET_PRESENTING_COMPLAINT, chargeId);
  }

}
