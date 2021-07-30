package com.insta.hms.mdm.encountertype;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List; 

@Repository
public class EncounterTypeRepository extends MasterRepository<Integer> {

  public EncounterTypeRepository() {
    super("encounter_type_codes", "encounter_type_id");
  }

  /** Encountere type query based on IP/OP applicability. **/
  private static String GET_IP_OP_APPLICABLE_ENCOUNTER_TYPES = "SELECT encounter_type_id, "
      + " encounter_type_desc FROM encounter_type_codes "
      + " WHERE status = 'A' ##IP_OP_APPLICABLITY##";

  /**
   * Update undefault values.
   *
   * @param type
   *          the type
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the int
   */
  public int updateUndefaultValues(String type, int key, String value) {

    String updateQuery = "UPDATE encounter_type_codes SET @  WHERE encounter_type_id != ? ";

    if (type.equals("o")) {
      updateQuery = updateQuery.replace("@", " op_encounter_default=? ");
    } else if (type.equals("i")) {
      updateQuery = updateQuery.replace("@", " ip_encounter_default=? ");
    } else if (type.equals("d")) {
      updateQuery = updateQuery.replace("@", " daycare_encounter_default=? ");
    }

    return DatabaseHelper.update(updateQuery, new Object[] { value, key });
  }

  /**
   * Gets the visit default encounter.
   *
   * @param visitType
   *          the visit type
   * @param isDaycare
   *          the is daycare
   * @return the visit default encounter
   */
  public BasicDynaBean getVisitDefaultEncounter(String visitType, boolean isDaycare) {

    String getEncounterQuery = " SELECT encounter_type_id, encounter_type_desc, op_applicable,"
        + " ip_applicable, daycare_applicable FROM encounter_type_codes WHERE ";

    if (visitType.equals("i")) {
      if (isDaycare) {
        getEncounterQuery = getEncounterQuery + " daycare_encounter_default = 'Y' ";
      } else {
        getEncounterQuery = getEncounterQuery + " ip_encounter_default = 'Y' ";
      }

    } else if (visitType.equals("o")) {
      getEncounterQuery = getEncounterQuery + " op_encounter_default = 'Y' ";
    }
    getEncounterQuery = getEncounterQuery + " LIMIT 1 ";
    return DatabaseHelper.queryToDynaBean(getEncounterQuery);
  }

  /**
   * Get ip op applicable encounter types.
   * @param isOpApplicable boolean op applicable
   * @param isIpApplicable boolean ip applicable
   * @return list of basic dyanbeans
   */
  public List<BasicDynaBean> getOpIpApplicableEncounterTypes(boolean isOpApplicable,
      boolean isIpApplicable) {
    String query = GET_IP_OP_APPLICABLE_ENCOUNTER_TYPES;
    if ((!isIpApplicable && !isOpApplicable) || (isIpApplicable && isOpApplicable)) {
      query = query.replace("##IP_OP_APPLICABLITY##", "");
    } else if (isOpApplicable) {
      query = query.replace("##IP_OP_APPLICABLITY##", "AND op_applicable = 'Y'");
    } else if (isIpApplicable) {
      query = query.replace("##IP_OP_APPLICABLITY##", "AND ip_applicable = 'Y'");
    }
    return DatabaseHelper.queryToDynaList(query);
  }

}
