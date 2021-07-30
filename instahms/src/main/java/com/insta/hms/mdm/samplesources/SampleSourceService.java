package com.insta.hms.mdm.samplesources;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// RC Anupama: There should be no qualifier for a service since we are not 
// creating any bean for the service through spring configuration

/**
 * The Class SampleSourceService.
 */
@Service("sampleSourceService")
public class SampleSourceService extends MasterService {
  
  /**
   * Instantiates a new sample source service.
   *
   * @param sampleSourceRepository the sample source repository
   * @param sampleSourceValidator the sample source validator
   */
  public SampleSourceService(SampleSourceRepository sampleSourceRepository,
      SampleSourceValidator sampleSourceValidator) {
    super(sampleSourceRepository, sampleSourceValidator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    refData.put("sampleSources", lookup(false));

    return refData;
  }
}
