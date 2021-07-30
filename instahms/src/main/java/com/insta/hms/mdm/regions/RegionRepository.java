package com.insta.hms.mdm.regions;

import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The Class RegionRepository.
 */
@Repository
public class RegionRepository extends GenericRepository {

  /**
   * Instantiates a new region repository.
   */
  public RegionRepository() {
    super("region_master");
  }

  /** The Constant GET_ALL_REGION_MASTER. */
  private static final String GET_ALL_REGION_MASTER = " SELECT region_id,region_name, status "
      + " FROM region_master ";

  /**
   * Gets the all regions.
   *
   * @return the all regions
   */
  public List<BasicDynaBean> getAllRegions() {
    return DatabaseHelper.queryToDynaList(GET_ALL_REGION_MASTER);
  }

  /** The Constant REGION_FIELDS. */
  private static final String REGION_FIELDS = "SELECT * ";

  /** The Constant REGION_FROM. */
  private static final String REGION_FROM = " FROM region_master ";

  /** The Constant COUNT. */
  private static final String COUNT = " SELECT count(region_id) ";

  /**
   * Gets the region master details.
   *
   * @param parameters
   *          the parameters
   * @param listingParameters
   *          the listing parameters
   * @return the region master details
   */
  public PagedList getRegionMasterDetails(Map<String, String[]> parameters,
      Map<LISTING, Object> listingParameters) {
    SearchQueryAssembler qb = new SearchQueryAssembler(REGION_FIELDS, COUNT, REGION_FROM,
        listingParameters);
    qb.addFilterFromParamMap(parameters);
    qb.build();

    return qb.getDynaPagedList();
  }
}
