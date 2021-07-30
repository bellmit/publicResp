package com.insta.hms.mdm.hl7interfaces;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class Hl7interfaceRepository.
 */
@Repository
public class Hl7interfaceRepository extends MasterRepository<String> {

  /**
   * Instantiates a new hl 7 interface repository.
   */
  public Hl7interfaceRepository() {
    super("hl7_lab_interfaces", "hl7_lab_interface_id", "interface_name");
  }

  /** The Constant GET_HL7_INTERFACE_RECORDS. */
  private static final String GET_HL7_INTERFACE_RECORDS =
      "select dei.* , hli.status from diagnostics_export_interface dei left join "
      + " hl7_lab_interfaces hli"
          + " using(hl7_lab_interface_id) where dei.test_id=? and hli.status = 'A'";

  /**
   * Gets the hl 7 mapping details.
   *
   * @param testId the test id
   * @return the hl 7 mapping details
   */
  public List<BasicDynaBean> getHl7MappingDetails(String testId) {
    return DatabaseHelper.queryToDynaList(GET_HL7_INTERFACE_RECORDS, testId);
  }

  /** The Constant GET_HL7INTERFACES. */
  private static final String GET_HL7INTERFACES =
      " SELECT interface_name, hl7_lab_interface_id FROM hl7_lab_interfaces WHERE status = 'A'";

  /**
   * Gets the hl 7 interfaces.
   *
   * @return the hl 7 interfaces
   */
  public List<BasicDynaBean> getHl7Interfaces() {
    return DatabaseHelper.queryToDynaList(GET_HL7INTERFACES);
  }
}
