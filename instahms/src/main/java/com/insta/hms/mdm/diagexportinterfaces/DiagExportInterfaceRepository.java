package com.insta.hms.mdm.diagexportinterfaces;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DiagExportInterfaceRepository.
 */
@Repository
public class DiagExportInterfaceRepository extends MasterRepository<String> {

  /**
   * Instantiates a new diag export interface repository.
   */
  public DiagExportInterfaceRepository() {
    super("diagnostics_export_interface", "test_id");
  }

  /** The Constant INSERT_DIAG_EXPORT_INTERFACES. */
  private static final String INSERT_DIAG_EXPORT_INTERFACES = 
      "INSERT INTO diagnostics_export_interface "
      + "(test_id, hl7_lab_interface_id, interface_name, item_type) " + " VALUES(?,?,?,?)";

  /**
   * Insert diag export interfaces.
   *
   * @param testId
   *          String
   * @param interfaces
   *          String[]
   * @param itemTypes
   *          String[]
   * @param hl7LabInterfaceIds
   *          String[]
   * @return true, if successful
   */
  public boolean insertDiagInterfaces(String testId, String[] interfaces, String[] itemTypes,
      String[] hl7LabInterfaceIds) {

    List<Object[]> queryParamsList = new ArrayList<>();
    boolean success = true;
    String query = INSERT_DIAG_EXPORT_INTERFACES;
    for (int i = 0; i < interfaces.length; i++) {
      if (null != hl7LabInterfaceIds && null != hl7LabInterfaceIds[i]
          && !hl7LabInterfaceIds[i].equals("")) {
        List<Object> queryParams = new ArrayList<>();
        queryParams.add(testId);
        queryParams.add(Integer.parseInt(hl7LabInterfaceIds[i]));
        queryParams.add(interfaces[i]);
        queryParams.add(itemTypes[i]);

        queryParamsList.add(queryParams.toArray());
      }
    }

    int[] results = DatabaseHelper.batchInsert(query, queryParamsList);

    for (int result : results) {
      if (result < 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  /** The Constant DELETE_HL7_MAPPING. */
  private static final String DELETE_HL7_MAPPING = 
      "DELETE FROM diagnostics_export_interface WHERE test_id = ? ";

  /**
   * Update diag export interfaces.
   *
   * @param testId
   *          String
   * @param hl7LabInterfaceIds
   *          String[]
   * @param interfaceNames
   *          String[]
   * @param itemTypes
   *          String[]
   * @param mappingDeleted
   *          String[]
   * @return true, if successful
   */
  public boolean updateDiagExprtInterfaces(String testId, String[] hl7LabInterfaceIds,
      String[] interfaceNames, String[] itemTypes, String[] mappingDeleted) {
    boolean success = true;
    DatabaseHelper.delete(DELETE_HL7_MAPPING, testId);
    for (int i = 0; i < interfaceNames.length; i++) {
      if (mappingDeleted[i].equals("false") && !mappingDeleted[i].equals("")
          && !interfaceNames[i].equals("")
          && (null != hl7LabInterfaceIds[i] && !hl7LabInterfaceIds[i].equals(""))) {
        success &= DatabaseHelper.insert(INSERT_DIAG_EXPORT_INTERFACES, testId,
            Integer.parseInt(hl7LabInterfaceIds[i]), interfaceNames[i], itemTypes[i]) > 0;
      }
    }
    return success;
  }
}
