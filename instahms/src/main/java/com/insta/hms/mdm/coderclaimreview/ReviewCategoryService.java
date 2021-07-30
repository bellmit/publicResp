package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterDetailsService;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ReviewCategoryService.
 */
@Service
public class ReviewCategoryService extends MasterDetailsService {

  /** The role category repository. */
  @LazyAutowired
  RoleCategoryRepository roleCategoryRepository;

  /**
   * Instantiates a new review category service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param roleCategoryRepository
   *          the role category repository
   */
  public ReviewCategoryService(ReviewCategoryRepository repository,
      ReviewCategoryValidator validator,
      RoleCategoryRepository roleCategoryRepository) {
    super(repository, validator, roleCategoryRepository);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#search(java.util.Map, java.util.Map,
   * boolean)
   */
  @Override
  public PagedList search(Map params, Map<LISTING, Object> listingParams,
      boolean filterByLoggedInCenter) {
    PagedList result = super.search(params, listingParams,
        filterByLoggedInCenter);
    List<Map> dtoList = new ArrayList<>();

    for (Map dto : (List<Map>) result.getDtoList()) {
      Map reviewMap = new HashMap(dto);
      try {
        reviewMap.put("roles",
            ConversionUtils.listBeanToListMap(roleCategoryRepository
                .getRoles((Integer) reviewMap.get("category_id"))));
      } catch (SQLException exc) {
        exc.printStackTrace();
      }
      dtoList.add(reviewMap);
    }

    result.setDtoList(dtoList);
    return result;
  }

}
