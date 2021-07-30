package com.insta.hms.mdm.regions;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class RegionService.
 * author - tanmay.k
 */
@Service
public class RegionService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RegionService.class);

  /** The Constant DAO regionDAO. */
  @LazyAutowired
  private RegionRepository regionRepository;

  /** The validator. */
  @LazyAutowired
  private RegionValidator validator;

  /**
   * Gets all the regions.
   *
   * @return List of all regions the data access exception
   */
  public List<BasicDynaBean> getAllRegions() {
    return regionRepository.getAllRegions();
  }

  /**
   * Gets the region master details.
   *
   * @param parameters
   *          the parameters
   * @return the region master details
   */
  public PagedList getRegionMasterDetails(Map<String, String[]> parameters) {
    return regionRepository.getRegionMasterDetails(parameters,
        ConversionUtils.getListingParameter(parameters));
  }

  /**
   * Insert region.
   *
   * @param parameters
   *          the parameters
   * @return the basic dyna bean
   */
  @Transactional
  public BasicDynaBean insertRegion(Map<String, String[]> parameters) {
    BasicDynaBean regionBean = regionRepository.getBean();
    List<String> errors = new ArrayList<>();
    ConversionUtils.copyToDynaBean(parameters, regionBean, errors);

    if (errors.isEmpty()) {

      validator.validateInsert(regionBean);

      String regionName = ((String) regionBean.get("region_name")).trim();
      boolean exists = regionRepository.exist("region_name", regionName);

      if (exists) {
        logger.error("Region already exists with name :" + regionName);
        throw new DuplicateEntityException(new String[] { "Region", regionName });
      } else {
        Integer regionId = regionRepository.getNextSequence();
        regionBean.set("region_id", regionId);
        regionRepository.insert(regionBean);
        logger.debug(
            "Added new region with regionId :" + regionId + " and region name :" + regionName);
      }
    } else {
      throw new ConversionException(errors);
    }
    return regionBean;
  }

  /**
   * Insert region programmatic transaction.
   *
   * @param parameters
   *          the parameters
   * @return the basic dyna bean
   */
  @SuppressWarnings("rawtypes")
  public BasicDynaBean insertRegionProgrammaticTransaction(Map<String, String[]> parameters) {
    BasicDynaBean regionBean = regionRepository.getBean();
    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(parameters, regionBean, errors);

    TransactionStatus txStatus = DatabaseHelper.startTransaction("regionMasterTransaction");
    try {
      if (errors.isEmpty()) {
        String regionName = ((String) regionBean.get("region_name")).trim();
        boolean exists = regionRepository.exist("region_name", regionName);
        if (exists) {
          logger.error("Region already exists with name :" + regionName);
          throw new DuplicateEntityException(new String[] { "Region", regionName });
        } else {
          Integer regionId = regionRepository.getNextSequence();
          regionBean.set("region_id", regionId);
          regionRepository.insert(regionBean);
          logger.debug(
              "Added new region with regionId :" + regionId + " and region name :" + regionName);
        }
      } else {
        throw new ConversionException("Error copying params to regionBean.");
      }
      DatabaseHelper.commit(txStatus);
    } catch (Exception ex) {
      DatabaseHelper.rollback(txStatus);
      throw ex;
    }
    return regionBean;
  }

  /**
   * Update region.
   *
   * @param parameters
   *          the parameters
   * @return the int
   */
  @Transactional
  public int updateRegion(Map<String, String[]> parameters) {

    BasicDynaBean regionBean = regionRepository.getBean();

    List<String> errors = new ArrayList<>();
    ConversionUtils.copyToDynaBean(parameters, regionBean, errors);
    validator.validateUpdate(regionBean);
    Integer regionId = null;
    String strRegionId = parameters.get("region_id")[0];
    try {
      regionId = Integer.parseInt(strRegionId);
    } catch (NumberFormatException exception) {
      errors.add("region_id is null");
    }

    List<String> columns = new ArrayList<>();
    columns.add("region_name");
    columns.add("region_id");
    String regionName = (String) regionBean.get("region_name");
    List<BasicDynaBean> listDynaBean = regionRepository.listAll(columns, "region_name", regionName);
    if (listDynaBean.size() == 1) {
      BasicDynaBean bean = listDynaBean.get(0);
      Integer id = (Integer) bean.get("region_id");
      if (!id.equals(regionId)) {
        throw new DuplicateEntityException(new String[] { "Region", regionName });
      }
    } else if (listDynaBean.size() > 1) {
      throw new DuplicateEntityException(new String[] { "Region", regionName });
    }

    Map<String, Object> keys = new HashMap<>();
    keys.put("region_id", regionId);
    int success = 0;
    if (errors.isEmpty()) {
      success = regionRepository.update(regionBean, keys);
    }
    return success;
  }

  /**
   * Lookup region name.
   *
   * @param parameters
   *          the parameters
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<String> lookupRegionName(Map<String, String[]> parameters) {
    String regionFields = "SELECT * ";
    String regionForm = " FROM region_master ";
    SearchQueryAssembler qb = new SearchQueryAssembler(regionFields, null, regionForm,
        ConversionUtils.getListingParameter(parameters));
    qb.addFilter(QueryAssembler.STRING, "region_name", "ILIKE", parameters.get("filterText")[0]);
    qb.build();
    PagedList pagedList = qb.getMappedPagedList();
    return pagedList.getDtoList();
  }

  /**
   * Gets the region.
   *
   * @param keyColumn
   *          the key column
   * @param identifier
   *          the identifier
   * @return the region
   */
  public BasicDynaBean getRegion(String keyColumn, String identifier) {
    return regionRepository.findByKey(keyColumn, Integer.parseInt(identifier));
  }

  /**
   * Test transactional.
   *
   * @param parameters
   *          the parameters
   * @return the basic dyna bean
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Transactional
  public BasicDynaBean testTransactional(Map<String, String[]> parameters) {
    BasicDynaBean regionBean = regionRepository.getBean();
    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(parameters, regionBean, errors);

    if (errors.isEmpty()) {
      String regionName = ((String) regionBean.get("region_name")).trim();
      boolean exists = regionRepository.exist("region_name", regionName);
      if (exists) {
        logger.warn("Region already exists with name :" + regionName);
        throw new DuplicateEntityException(new String[] { "Region", regionName });
      } else {
        Integer regionId = regionRepository.getNextSequence();
        regionBean.set("region_id", regionId);
        regionRepository.insert(regionBean);
        logger.debug(
            "Added new region with regionId :" + regionId + " and region name :" + regionName);
      }

    } else {
      throw new ConversionException(errors);
    }
    throw new RuntimeException("Testing rollback");
  }
}
