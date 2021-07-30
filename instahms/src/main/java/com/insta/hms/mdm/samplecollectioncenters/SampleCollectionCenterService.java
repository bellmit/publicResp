package com.insta.hms.mdm.samplecollectioncenters;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SampleCollectionCenterService.
 */
@Service
public class SampleCollectionCenterService extends MasterService {
  
  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /**
   * Instantiates a new sample collection center service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public SampleCollectionCenterService(SampleCollectionCenterRepository repository,
      SampleCollectionCenterValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the list page data.
   *
   * @param requestParams the request params
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map<String, String[]> requestParams) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();

    List<BasicDynaBean> centers = centerService.listAll(false);
    map.put("centers", centers);
    return map;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> centers = centerService.listAll(false);
    refData.put("sampleCenters", lookup(false));
    refData.put("centers", centers);
    return refData;
  }

  /** The Constant MAX_COLLECTION_CENTERS. */
  private static final String MAX_COLLECTION_CENTERS = "SELECT count(*) "
      + "FROM sample_collection_centers where status='A'";

  /**
   * Gets the max collection centers.
   *
   * @return the max collection centers
   */
  public Integer getMaxCollectionCenters() {
    return DatabaseHelper.getInteger(MAX_COLLECTION_CENTERS);
  }

  /** The Constant MAX_COLLECTION_CENTERS_LIMIT. */
  private static final String MAX_COLLECTION_CENTERS_LIMIT = "SELECT "
      + "max_collection_centers_count FROM generic_preferences";

  /**
   * Gets the limited collection centers.
   *
   * @return the limited collection centers
   */
  public Integer getLimitedCollectionCenters() {
    return DatabaseHelper.getInteger(MAX_COLLECTION_CENTERS_LIMIT);
  }

  /**
   * List by center.
   *
   * @param centerIds the center ids
   * @param includeDefault the include default
   * @return the list
   */
  public List<BasicDynaBean> listByCenter(List<Integer> centerIds, boolean includeDefault) {
    if (includeDefault) {
      centerIds.add(0);
    }
    return ((SampleCollectionCenterRepository) this.getRepository()).listByCenter(centerIds);
  }

}
