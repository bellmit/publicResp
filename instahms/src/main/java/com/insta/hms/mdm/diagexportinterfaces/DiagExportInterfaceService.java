package com.insta.hms.mdm.diagexportinterfaces;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class DiagExportInterfaceService.
 *
 * @author anil.n
 */

@Service
public class DiagExportInterfaceService extends MasterService {

  /** The diag export interface repository. */
  @LazyAutowired
  private DiagExportInterfaceRepository diagExportInterfaceRepository;

  /**
   * Instantiates a new diag export interface service.
   *
   * @param repository
   *          DiagExportInterfaceRepository
   * @param validator
   *          DiagExportInterfaceValidator
   */
  public DiagExportInterfaceService(DiagExportInterfaceRepository repository,
      DiagExportInterfaceValidator validator) {
    super(repository, validator);
  }

  /**
   * Insert diag export interfaces.
   *
   * @param testId
   *          String
   * @param interfaceNames
   *          String[]
   * @param itemTypes
   *          String[]
   * @param hl7LabInterfaceIds
   *          String[]
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean insertDiagExprtInterfaces(String testId, String[] interfaceNames,
      String[] itemTypes, String[] hl7LabInterfaceIds) {
    return diagExportInterfaceRepository.insertDiagInterfaces(testId, interfaceNames, itemTypes,
        hl7LabInterfaceIds);
  }

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
  @Transactional(rollbackFor = Exception.class)
  public boolean updateDiagExprtInterfaces(String testId, String[] hl7LabInterfaceIds,
      String[] interfaceNames, String[] itemTypes, String[] mappingDeleted) {
    return diagExportInterfaceRepository.updateDiagExprtInterfaces(testId, hl7LabInterfaceIds,
        interfaceNames, itemTypes, mappingDeleted);
  }
}
