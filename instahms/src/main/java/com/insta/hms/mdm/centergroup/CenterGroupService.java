package com.insta.hms.mdm.centergroup;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.centers.CenterRepository;
import com.insta.hms.mdm.cities.CityService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CenterGroupService.
 */
@Service
public class CenterGroupService extends MasterService {
  
  /** The city service. */
  @LazyAutowired
  private CityService cityService;
  
  /** The validator. */
  @LazyAutowired
  private CenterGroupValidator validator;
  
  /** The center group repo. */
  @LazyAutowired
  private CenterGroupRepository centerGroupRepo;
  
  /** The center group details repo. */
  @LazyAutowired
  private CenterGroupDetailsRepository centerGroupDetailsRepo;
  
  /** The center repo. */
  @LazyAutowired
  private CenterRepository centerRepo;

  /**
   * Instantiates a new center group service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public CenterGroupService(CenterGroupRepository repository, CenterGroupValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    Integer centerGroupId = null;
    if (params != null) {
      if (((String[]) params.get("center_group_id")) != null) {
        centerGroupId = Integer.parseInt(((String[]) params.get("center_group_id"))[0]);
      }
    }
    List<BasicDynaBean> associatedCenters = getAssociatedCentersList(centerGroupId);
    List<BasicDynaBean> userGroupsList = getUserAssociatedCentersGroups();
    refData.put("userGroupsList", userGroupsList);
    List<BasicDynaBean> citiesList = cityService.listAllCenters();
    List<BasicDynaBean> centersList = getCenterLists();
    refData.put("applicable_centers", associatedCenters);
    refData.put("cities_json", citiesList);
    refData.put("centers_json", centersList);
    return refData;
  }

  /**
   * Gets the center lists.
   *
   * @return the center lists
   */
  public List<BasicDynaBean> getCenterLists() {
    return ((CenterGroupRepository) getRepository()).getCenterList();
  }

  /**
   * Gets the associated centers list.
   *
   * @param centerGroupId the center group id
   * @return the associated centers list
   */
  public List<BasicDynaBean> getAssociatedCentersList(Integer centerGroupId) {
    return ((CenterGroupRepository) getRepository()).getAssociatedCenters(centerGroupId);
  }

  /**
   * Gets the user associated centers groups.
   *
   * @return the user associated centers groups
   */
  public List<BasicDynaBean> getUserAssociatedCentersGroups() {
    return ((CenterGroupRepository) getRepository()).getCenterGroups();
  }

  /**
   * Gets the center group.
   *
   * @param keyColumn the key column
   * @param identifier the identifier
   * @return the center group
   */
  public BasicDynaBean getCenterGroup(String keyColumn, Integer identifier) {
    BasicDynaBean dynaBean = centerGroupRepo.findByKey(keyColumn, identifier);
    return dynaBean;
  }

  /**
   * Insert center group.
   *
   * @param parameters the parameters
   * @param requestValues the request values
   * @return the basic dyna bean
   * @throws ConversionException the conversion exception
   */
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean insertCenterGroup(Map<String, String[]> parameters, HashMap requestValues) {

    BasicDynaBean centerGroupBean = ((CenterGroupRepository) getRepository()).getBean();
    List<String> errors = new ArrayList<>();
    ConversionUtils.copyToDynaBean(parameters, centerGroupBean, errors);
    centerGroupBean.set("center_group_name", parameters.get("center_group_name")[0].trim());
    Integer centerGroupId = (Integer) centerRepo.getNextId();
    if (errors.isEmpty()) {
      validator.validateInsert(centerGroupBean);
      String centerGroupName = ((String) centerGroupBean.get("center_group_name"));
      boolean exists = ((CenterGroupRepository) getRepository()).exist("center_group_name",
          centerGroupName);
      if (exists) {
        throw new DuplicateEntityException(new String[] { "Center Group", centerGroupName });
      } else {
        centerGroupBean.set("center_group_id", centerGroupId);
        ((CenterGroupRepository) getRepository()).insert(centerGroupBean);
      }
      updateCenterGroupApplicability(requestValues, centerGroupId);
    } else {
      throw new ConversionException(errors);
    }
    return centerGroupBean;

  }

  /**
   * Update center group.
   *
   * @param parameters the parameters
   * @param requestValues the request values
   * @return the int
   * @throws ConversionException the conversion exception
   */
  @Transactional(rollbackFor = Exception.class)
  public int updateCenterGroup(Map<String, String[]> parameters, HashMap requestValues) {
    BasicDynaBean centerGroupBean = ((CenterGroupRepository) getRepository()).getBean();
    List<String> errors = new ArrayList<>();
    int success = 0;
    ConversionUtils.copyToDynaBean(parameters, centerGroupBean, errors);
    centerGroupBean.set("center_group_name", parameters.get("center_group_name")[0]);
    Integer centerGroupId = (Integer) centerGroupBean.get("center_group_id");
    validator.validateUpdate(centerGroupBean);
    Map<String, Object> keys = new HashMap<>();
    keys.put("center_group_id", centerGroupId);

    if (errors.isEmpty()) {
      success = centerGroupRepo.update(centerGroupBean, keys);
      updateCenterGroupApplicability(requestValues, centerGroupId);
    }

    return success;
  }

  /**
   * Update center group applicability.
   *
   * @param requestValues the request values
   * @param centerGroupId the center group id
   */
  public void updateCenterGroupApplicability(HashMap requestValues, Integer centerGroupId) {
    String error = null;
    try {
      txn: {
        if (!centerGroupDetailsRepo.deleteAssociation(0, centerGroupId)) {
          error = "Failed to delete doctor center association for all centers..";
          break txn;
        }
        String[] centerIds = (String[]) requestValues.get("centerIds");
        String[] assocIds = (String[]) requestValues.get("assocIds");
        String[] assocDeleted = (String[]) requestValues.get("assocDeleted");
        String[] assocEdited = (String[]) requestValues.get("assocEdited");
        String[] assocStatus = (String[]) requestValues.get("assocStatus");
        if (!centerGroupDetailsRepo.updateAssociations(centerGroupId, centerIds, assocIds,
            assocStatus, assocDeleted, assocEdited)) {
          error = "Failed to insert the  doctor center association for selected centers..";
          break txn;
        }
        ;
      }
      if (error != null) {
        throw new ValidationException(error);
      }

    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

}
