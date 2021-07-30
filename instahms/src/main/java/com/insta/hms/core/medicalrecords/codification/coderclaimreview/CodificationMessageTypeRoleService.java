package com.insta.hms.core.medicalrecords.codification.coderclaimreview;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class CodificationMessageTypeRoleService.
 */
@Service
public class CodificationMessageTypeRoleService extends MasterService {

  /**
   * Instantiates a new codification message type role service.
   *
   * @param rep
   *          the CodificationMessageTypeRoleRepository rep variable
   * @param val
   *          the CodificationMessageTypeRoleValidator val variable
   */
  public CodificationMessageTypeRoleService(CodificationMessageTypeRoleRepository rep,
      CodificationMessageTypeRoleValidator val) {
    super(rep, val);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    map.put("messageTypes", lookup(false));
    return map;
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return ((CodificationMessageTypesRepository) this.getRepository()).getBean();
  }

  /**
   * Gets the message types list.
   *
   * @return the message types list
   */
  public List<BasicDynaBean> getMessageTypesList() {
    return ((CodificationMessageTypesRepository) getRepository()).getMessageTypesList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterService#autocomplete(java.lang.String, java.util.Map)
   */
  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("review_type", match, false, parameters);
  }
}
