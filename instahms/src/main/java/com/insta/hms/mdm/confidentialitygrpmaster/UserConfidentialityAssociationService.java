package com.insta.hms.mdm.confidentialitygrpmaster;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class UserConfidentialityAssociationService.
 */
@Service
public class UserConfidentialityAssociationService extends MasterService {

  /** The confidentiality group repository. */
  @LazyAutowired
  private ConfidentialityGroupRepository confidentialityGroupRepository;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The user confidentiality association repository. */
  UserConfidentialityAssociationRepository userConfidentialityAssociationRepository;

  /**
   * Instantiates a new user confidentiality association service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public UserConfidentialityAssociationService(UserConfidentialityAssociationRepository repository,
      UserConfidentialityAssociationValidator validator) {
    super(repository, validator);
    userConfidentialityAssociationRepository = repository;
  }

  /**
   * Update confidentiality group associations for a user.
   *
   * @param username the username
   * @param confidentialityGroupIds the confidentiality group ids
   * @return the int
   */
  public int update(String username, List<Integer> confidentialityGroupIds) {
    List<String> columns = new ArrayList<>();
    columns.add("id");
    columns.add("confidentiality_grp_id");
    List<BasicDynaBean> originalGroups = userConfidentialityAssociationRepository.listAll(columns,
        "emp_username", username);
    List<String> deletedIds = new ArrayList<>();
    Set<Integer> originalGroupIds = new HashSet<>();
    for (BasicDynaBean originalGroup : originalGroups) {
      Integer confidentialityGroupId = (Integer) originalGroup.get("confidentiality_grp_id");
      if (confidentialityGroupId != 0
          && !confidentialityGroupIds.contains(confidentialityGroupId)) {
        deletedIds.add(originalGroup.get("id").toString());
      }
      originalGroupIds.add(confidentialityGroupId);
    }
    Set<Integer> newGroupIds = new HashSet<>();
    for (Integer id : confidentialityGroupIds) {
      newGroupIds.add(id);
    }
    newGroupIds.removeAll(originalGroupIds);
    // Delete old associations
    if (!deletedIds.isEmpty()) {
      this.batchDelete((String[]) deletedIds.toArray(new String[0]), true);
    }
    // Add new associations
    List<BasicDynaBean> insertBeanList = new ArrayList<>();
    for (Integer newGroupId : newGroupIds) {
      BasicDynaBean bean = userConfidentialityAssociationRepository.getBean();
      bean.set("id", userConfidentialityAssociationRepository.getNextId());
      bean.set("emp_username", username);
      bean.set("confidentiality_grp_id", newGroupId);
      bean.set("created_by", sessionService.getSessionAttributes().get("userId"));
      bean.set("modified_by", sessionService.getSessionAttributes().get("userId"));
      bean.set("modified_at", DateUtil.getCurrentTimestamp());
      insertBeanList.add(bean);
    }
    userConfidentialityAssociationRepository.batchInsert(insertBeanList);
    return 0;
  }

}
