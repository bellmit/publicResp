package com.insta.hms.mdm.services;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.mdm.MasterService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** The Class ServicesService. */
@Service
public class ServicesService extends MasterService {

  /**
   * Instantiates a new services service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public ServicesService(ServicesRepository repo, ServicesValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the service charge bean.
   *
   * @param serviceId the service id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the service charge bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getServiceChargesBean(String serviceId, String bedType, String orgId)
      throws SQLException {

    return ((ServicesRepository) getRepository()).getServiceChargesBean(serviceId, bedType, orgId);
  }
  
  /**
   * Gets the service charge bean.
   *
   * @param serviceId the service id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the service charge bean
   */
  public BasicDynaBean getServiceChargeBean(String serviceId, String bedType, String orgId) {
    return ((ServicesRepository) getRepository()).getServiceChargeBean(serviceId, bedType, orgId);
  }

  /**
   * Gets the services for prescription.
   *
   * @param bedType the bed type
   * @param orgId the org id
   * @param patientType the patient type
   * @param insPlanId the ins plan id
   * @param searchQuery the search query
   * @param itemLimit the item limit
   * @return the services for prescription
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getServicesForPrescription(
      String bedType,
      String orgId,
      String patientType,
      Integer insPlanId,
      String searchQuery,
      Integer itemLimit) {
    return ConversionUtils.listBeanToListMap(
        ((ServicesRepository) getRepository())
            .getServicesForPrescription(
                bedType, orgId, patientType, insPlanId, searchQuery, itemLimit));
  }

  /**
   * List all.
   *
   * @param columns the columns
   * @param filterBy the filter by
   * @param filterValue the filter value
   * @param sortColumn the sort column
   * @return the list
   */
  public List<BasicDynaBean> listAll(
      List<String> columns, String filterBy, Object filterValue, String sortColumn) {
    return getRepository().listAll(columns, filterBy, filterValue, sortColumn);
  }

  /**
   * Gets the service item sub group tax details.
   *
   * @param serviceId the service id
   * @return the service item sub group tax details
   */
  public List<BasicDynaBean> getServiceItemSubGroupTaxDetails(String serviceId) {
    return ((ServicesRepository) getRepository()).getServiceItemSubGroupTaxDetails(serviceId);
  }
  
  public BasicDynaBean findByKey(String serviceId) {
    return ((ServicesRepository) getRepository()).findByKey("service_id", serviceId);
  }
}
