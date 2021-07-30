package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterDetailsService;
import com.insta.hms.mdm.role.RoleMasterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ReviewTypesService.
 */
@Service
public class ReviewTypesService extends MasterDetailsService {

  /** The review category service. */
  @LazyAutowired
  ReviewCategoryService reviewCategoryService;

  /** The role service. */
  @LazyAutowired
  RoleMasterService roleService;

  /** The review type center role repository. */
  @Autowired
  ReviewTypeCenterRoleRepository reviewTypeCenterRoleRepository;

  /**
   * Instantiates a new review types service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param reviewTypeCenterRoleRepository
   *          the review type center role repository
   */
  public ReviewTypesService(ReviewTypesRepository repository,
      ReviewTypesValidator validator,
      ReviewTypeCenterRoleRepository reviewTypeCenterRoleRepository) {
    super(repository, validator, reviewTypeCenterRoleRepository);
  }

  /**
   * Meta data.
   *
   * @return the map
   */
  public Map<String, Object> metaData() {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("review_category",
        ConversionUtils.listBeanToListMap(reviewCategoryService.lookup(false)));
    metadata.put("roles",
        ConversionUtils.listBeanToListMap(roleService.lookup(true)));
    return metadata;
  }

  /*
   * (non-Javadoc)
   * @see com.insta.hms.mdm.MasterService#search(java.util.Map, java.util.Map,
   * boolean)
   */
  @Override
  public PagedList search(Map params, Map<LISTING, Object> listingParams,
      boolean filterByLoggedInCenter) {
    PagedList result = super.search(params, listingParams,
        filterByLoggedInCenter);
    List<Map> dtoList = new ArrayList();

    for (Map dto : (List<Map>) result.getDtoList()) {
      Map reviewTypeMap = new HashMap(dto);
      reviewTypeMap.put("review_type_center_role",
          ConversionUtils.listBeanToListMap(reviewTypeCenterRoleRepository
              .getByMessageId((Integer) dto.get("review_type_id"))));
      dtoList.add(reviewTypeMap);
    }

    result.setDtoList(dtoList);
    return result;
  }

}
