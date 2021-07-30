package com.insta.hms.mdm.packages;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PackagesService.
 */
@Service
public class PackagesService extends MasterService {

  /**
   * Instantiates a new packages service.
   *
   * @param packagesRepository the packages repository
   * @param packagesValidator the packages validator
   */
  public PackagesService(PackagesRepository packagesRepository, 
      PackagesValidator packagesValidator) {
    super(packagesRepository, packagesValidator);
  }

  /**
   * Gets the package details.
   *
   * @param packageId the package id
   * @param orgId the org id
   * @param bedType the bed type
   * @return the package details
   */
  public BasicDynaBean getPackageDetails(int packageId, String orgId, String bedType) {
    return ((PackagesRepository) getRepository()).getPackageDetails(packageId, orgId, bedType);
  }

  /**
   * Gets the package details.
   *
   * @param packageId the package id
   * @param orgId     the org id
   * @return the package details
   */
  public List<BasicDynaBean> getAllBedTypePackageDetails(int packageId, String orgId) {
    return ((PackagesRepository) getRepository()).getAllBedTypePackageDetails(packageId, orgId);
  }

  /**
   * Gets the package components.
   *
   * @param packageId the package id
   * @return the package components
   */
  public List<BasicDynaBean> getPackageComponents(Integer packageId) {
    return getPackageComponents(packageId, "ORG0001", "GENERAL");
  }

  /**
   * Gets the package components.
   *
   * @param packageId the package id
   * @param orgId     orgId
   * @param bedType   bed type
   * @return the package components
   */
  public List<BasicDynaBean> getPackageComponents(Integer packageId, String orgId, String bedType) {
    List<Integer> packageIdList = Arrays.asList(packageId);
    return ((PackagesRepository) getRepository())
            .getPackageComponents(packageIdList, orgId, bedType);
  }

  /**
   * Gets the components across the given packages.
   *
   * @param packageIdList the package id list
   * @param orgId     orgId
   * @param bedType   bed type
   * @return the package components
   */
  public List<BasicDynaBean> getPackagesComponents(List<Integer> packageIdList, String orgId,
                                                   String bedType) {
    if (packageIdList.isEmpty()) {
      return new ArrayList<>();
    }
    return ((PackagesRepository) getRepository())
            .getPackageComponents(packageIdList, orgId, bedType);
  }
  
  //To Be removed after 12.4- packages4.0 
  public List<BasicDynaBean> getPackageComponentsNew(int packageId) {
    return ((PackagesRepository) getRepository()).getPackageComponentsNew(packageId);
  }

  /**
   * Gets the components across the given packages.
   *
   * @param mrNo     MR no
   * @param packageIdList the package id list
   * @return the package components
   */
  public List<BasicDynaBean> getPatientPackagesComponents(String mrNo,
      List<Integer> packageIdList) {
    if (packageIdList.isEmpty()) {
      return new ArrayList<>();
    }
    return ((PackagesRepository) getRepository())
            .getPatientPackagesComponents(mrNo, packageIdList);
  }

  /**
   * Gets the package item sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the package item sub group tax details
   */
  public List<BasicDynaBean> getPackageItemSubGroupTaxDetails(String actDescriptionId) {
    return ((PackagesRepository) getRepository())
        .getPackageItemSubGroupTaxDetails(actDescriptionId);
  }

  /**
   * Gets the package component details.
   *
   * @param packageId the package id
   * @return the package component details
   */
  public List<BasicDynaBean> getPackageComponentDetails(int packageId) {
    return ((PackagesRepository) getRepository()).getPackgeComponentDetails(packageId);
  }

  /**
   * Find by key.
   *
   * @param keyColumn the key column
   * @param identifier the identifier
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keyColumn, Object identifier) {
    return ((PackagesRepository) getRepository()).findByKey(keyColumn, identifier);
  }
}
