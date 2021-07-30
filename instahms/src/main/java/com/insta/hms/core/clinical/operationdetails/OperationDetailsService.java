package com.insta.hms.core.clinical.operationdetails;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class OperationDetailsService.
 *
 * @author anup vishwas
 */

@Service
public class OperationDetailsService {

  /** The operation details repo. */
  @LazyAutowired
  private OperationDetailsRepository operationDetailsRepo;

  /**
   * List all.
   *
   * @param operationDetailsList
   *          the operation details list
   * @param filterBy
   *          the filter by
   * @param filterValue
   *          the filter value
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> operationDetailsList, String filterBy,
      Object filterValue) {

    return operationDetailsRepo.listAll(operationDetailsList, filterBy, filterValue);
  }

  /**
   * Gets the operation details for FTL.
   *
   * @param opDetailsId
   *          the op details id
   * @return the operation details for FTL
   */
  public BasicDynaBean getOperationDetailsForFTL(Integer opDetailsId) {

    return operationDetailsRepo.getOperationDetailsForFTL(opDetailsId);
  }

  /**
   * Gets the operation team.
   *
   * @param opDetailsId
   *          the op details id
   * @return the operation team
   */
  public List<BasicDynaBean> getOperationTeam(Integer opDetailsId) {

    return operationDetailsRepo.getOperationTeam(opDetailsId);
  }

  /**
   * Gets the surgery list for FTL.
   *
   * @param opDetailsId
   *          the op details id
   * @return the surgery list for FTL
   */
  public List<BasicDynaBean> getSurgeryListForFTL(Integer opDetailsId) {

    return operationDetailsRepo.getSurgeryListForFTL(opDetailsId);
  }

  /**
   * Gets the op details by proc id.
   *
   * @param opProcId
   *          the op proc id
   * @return the op details by proc id
   */
  public BasicDynaBean getOpDetailsByProcId(int opProcId) {

    return operationDetailsRepo.getOpDetailsByProcId(opProcId);
  }

  /**
   * Gets the operations.
   *
   * @param patientId
   *          the patient id
   * @param opDetailsId
   *          the op details id
   * @return the operations
   */
  public List<BasicDynaBean> getOperations(String patientId, int opDetailsId) {

    return operationDetailsRepo.getOperations(patientId, opDetailsId);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return operationDetailsRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param bean
   *          the bean
   * @return the int
   */
  public int insert(BasicDynaBean bean) {
    return operationDetailsRepo.insert(bean);
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return operationDetailsRepo.getNextSequence();
  }

  /**
   * Gets the primary operation details by prescribed id.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the primary operation details by prescribed id
   */
  public BasicDynaBean getPrimaryOperationDetailsByPrescribedId(Integer prescribedId) {
    return operationDetailsRepo.getPrimaryOperationDetailsByPrescribedId(prescribedId);
  }

  /**
   * Cancel advanced OT surgery.
   *
   * @param prescribedIds
   *          the prescribed ids
   * @return the int
   */
  public int cancelAdvancedOTSurgery(List<Integer> prescribedIds) {
    return operationDetailsRepo.cancelAdvancedOTSurgery(prescribedIds);
  }

}
