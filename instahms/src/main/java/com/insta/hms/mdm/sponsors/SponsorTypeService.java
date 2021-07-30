package com.insta.hms.mdm.sponsors;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class SponsorTypeService. */
@Service
public class SponsorTypeService extends MasterService {

  /**
   * Instantiates a new sponsor type service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public SponsorTypeService(SponsorTypeRepository repo, SponsorTypeValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the ALL sponsor type.
   *
   * @param sponsortypeid the sponsortypeid
   * @return the ALL sponsor type
   */
  public List<BasicDynaBean> getAllSponsorType(int sponsortypeid) {
    return ((SponsorTypeRepository) getRepository()).getAllSponsorTypeNames(sponsortypeid);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();

    if (params.get("sponsor_type_id") != null) {
      int sponsortypeid = Integer.valueOf(((String[]) params.get("sponsor_type_id"))[0]);
      referenceMap.put("sponsorTypeNames", getAllSponsorType(sponsortypeid));
    } else {
      referenceMap.put("sponsorTypeNames", getAllSponsorType(0));
    }
    return referenceMap;
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return ((SponsorTypeRepository) getRepository()).listAll();
  }
}
