package com.insta.hms.mdm.sequences.hospitalidpatterns;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class HospitalIdPatternsRepository.
 */
@Repository
public class HospitalIdPatternsRepository extends MasterRepository<String> {

  /** The Constant CHECK_SEQUENCE_EXISTS. */
  private static final String CHECK_SEQUENCE_EXISTS =
      "SELECT COUNT(*) "
          + "FROM information_schema.sequences"
          + " WHERE sequence_name=LOWER(?) and sequence_schema= (select current_schema()) ";

  /**
   * Instantiates a new hospital id patterns repository.
   */
  public HospitalIdPatternsRepository() {
    super("hosp_id_patterns", "pattern_id", "sequence_name");
    setStatusField(null);
  }

  /**
   * Checks if is sequence exists.
   *
   * @param sequenceName the sequence name
   * @return true, if is sequence exists
   */
  public boolean isSequenceExists(String sequenceName) {

    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(CHECK_SEQUENCE_EXISTS, sequenceName);
    if (bean != null) {
      return ((Long) bean.get("count")).intValue() > 0;
    }

    return false;
  }

  /**
   * Creates the sequence.
   *
   * @param sequenceName the sequence name
   * @return true, if successful
   */
  public boolean createSequence(String sequenceName) {
    return DatabaseHelper.createSequence(sequenceName);
  }

  /**
   * @see com.insta.hms.mdm.MasterRepository#supportsAutoId()
   */
  public boolean supportsAutoId() {
    return false;
  }

  /**
   * Gets the hosp id pattern details.
   *
   * @param patternId the pattern id
   * @return the hosp id pattern details
   */
  public BasicDynaBean getHospIdPatternDetails(String patternId) {
    return findByKey("pattern_id", patternId);
  }
}
