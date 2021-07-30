package com.insta.hms.mdm.operations;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The Class OperationsService.
 */
@Service
public class OperationsService extends MasterService {

  /**
   * Instantiates a new operations service.
   *
   * @param operationsRepository
   *          the operations repository
   * @param operationsValidator
   *          the operations validator
   */
  public OperationsService(OperationsRepository operationsRepository,
      OperationsValidator operationsValidator) {
    super(operationsRepository, operationsValidator);
  }

  /**
   * Gets the operations for prescription.
   *
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   * @param patientType
   *          the patient type
   * @param insPlanId
   *          the ins plan id
   * @param searchQuery
   *          the search query
   * @param itemLimit
   *          the item limit
   * @return the operations for prescription
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getOperationsForPrescription(String bedType, String orgId,
      String patientType, Integer insPlanId, String searchQuery, Integer itemLimit) {
    return ConversionUtils.listBeanToListMap(
        ((OperationsRepository) getRepository()).getOperationsForPrescription(bedType, orgId,
            patientType, insPlanId, searchQuery, itemLimit));
  }

  /**
   * List all.
   *
   * @param columns
   *          the columns
   * @param filterBy
   *          the filter by
   * @param filterValue
   *          the filter value
   * @param sortColumn
   *          the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue,
      String sortColumn) {
    return getRepository().listAll(columns, filterBy, filterValue, sortColumn);

  }

  /**
   * Gets the operation item sub group tax details.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the operation item sub group tax details
   */
  public List<BasicDynaBean> getOperationItemSubGroupTaxDetails(String actDescriptionId) {
    return ((OperationsRepository) getRepository())
        .getOperationItemSubGroupTaxDetails(actDescriptionId);
  }

  public BasicDynaBean getOperationCharge(String opId, String bedType, String orgId) {
    return ((OperationsRepository) getRepository()).getOperationCharge(opId, bedType, orgId);
  }

  public List<BasicDynaBean> getAllOperationCharge(String opId, String orgId) {
    return ((OperationsRepository) getRepository()).getAllOperationCharge(opId, orgId);
  }

}
