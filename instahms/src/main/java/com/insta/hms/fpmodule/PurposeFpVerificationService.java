package com.insta.hms.fpmodule;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PurposeFpVerificationService extends MasterService {

  @LazyAutowired
  private SessionService sessionService;

  public PurposeFpVerificationService(PurposeFpVerificationRepository repo,
      PurposeFpVerificationValidator validator) {
    super(repo, validator);
  }

  /**
   * List all centers.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAllCenters() {

    List<BasicDynaBean> purposeList = new ArrayList<BasicDynaBean>();
    purposeList.addAll(getRepository().listAll(null, "status", "A", "purpose"));
    return purposeList;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Integer insert(BasicDynaBean bean) {
    String userId = (String) sessionService.getSessionAttributes().get("userId");
    bean.set("created_by", userId);
    bean.set("updated_by", userId);
    return super.insert(bean);

  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  @SuppressWarnings({ "rawtypes" })
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (null != params && params.containsKey("purpose_id")) {
      String temp = ((String[]) params.get("purpose_id"))[0];

      Integer purposeId = Integer.parseInt(temp);

      filterMap.put("purpose_id", purposeId);
      map.put("purposeList", this.lookup(false, filterMap));
    }

    return map;
  }

  public int getLatestPurposeId() {
    PurposeFpVerificationRepository repo = (PurposeFpVerificationRepository) super.getRepository();
    return repo.getLatestPurposeId();
  }
}
