package com.insta.hms.mdm.hospitalprints;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.BeanConversionService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class HospitalPrintService.
 */
/*
 * TODO: Table has composite primary key of (print_type , center_id)
 * So not extending with Master Service.
 */
@Service
public class HospitalPrintService implements BeanConversionService {

  /** The hospital print repository. */
  @LazyAutowired private HospitalPrintRepository hospitalPrintRepository;

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.BeanConversionService#toBean(java.util.Map)
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams) {
    return null;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.BeanConversionService#toBean(java.util.Map, java.util.Map)
   */
  @Override
  public BasicDynaBean toBean(
      Map<String, String[]> requestParams, Map<String, MultipartFile> fileMap) {
    return null;
  }

  /**
   * Gets the default printer.
   *
   * @param keyColumn the key column
   * @param identifier the identifier
   * @return the default printer
   */
  public BasicDynaBean getDefaultPrinter(String keyColumn, Object identifier) {
    return hospitalPrintRepository.findByKey(keyColumn, identifier);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.BeanConversionService#toBeanList(java.util.Map, 
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(
      Map<String, String[]> requestParams, BasicDynaBean type) {
    return null;
  }
}
