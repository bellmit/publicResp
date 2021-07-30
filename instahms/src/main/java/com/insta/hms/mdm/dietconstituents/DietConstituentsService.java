package com.insta.hms.mdm.dietconstituents;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class DietConstituentsService.
 */
@Service
public class DietConstituentsService {

  @LazyAutowired
  private DietConstituentsRepository repository;

  @SuppressWarnings("rawtypes")
  public List getConstituentsForDiet(Integer dietId) {
    List<BasicDynaBean> listConstituents = repository.listAll(null, "diet_id", dietId);
    return ConversionUtils.listBeanToListMap(listConstituents);
  }
}
