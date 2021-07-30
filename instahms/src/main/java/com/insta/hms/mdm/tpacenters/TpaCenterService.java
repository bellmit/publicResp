package com.insta.hms.mdm.tpacenters;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class TpaCenterService.
 */
@Service
public class TpaCenterService extends MasterService {

  /**
   * Instantiates a new tpa center service.
   *
   * @param tcr the tcr
   * @param tcv the tcv
   */
  public TpaCenterService(TpaCenterRepository tcr, TpaCenterValidator tcv) {
    super(tcr, tcv);
  }

  /**
   * Tpa center list.
   *
   * @return the list
   */
  public List<BasicDynaBean> tpaCenterList() {
    Map xlmTpaKeyMap = new HashMap();
    xlmTpaKeyMap.put("status", "A");
    xlmTpaKeyMap.put("claim_format", "XML");
    return ((TpaCenterRepository) getRepository()).getTpaCenterList(xlmTpaKeyMap);
  }

}
