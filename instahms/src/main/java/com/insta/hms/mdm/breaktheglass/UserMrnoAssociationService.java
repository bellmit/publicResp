package com.insta.hms.mdm.breaktheglass;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.auditlog.usermrnoassociation.UserMrnoAssociationAuditLogService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserMrnoAssociationService extends MasterService {

  @LazyAutowired
  UserMrnoAssociationAuditLogService userMrnoAssociationAuditLogService;
  
  @LazyAutowired
  SessionService sessionService;
  
  public UserMrnoAssociationService(UserMrnoAssociationRepository repository,
      UserMrNoAssociationValidator validator) {
    super(repository, validator);
  }

  /**
   * Insert into usermrnoassocation table after inserting into the auditlog.
   * @param bean the usermrnoassocaiton bean
   * @param remarks to be recorded in auditlog table
   * @return number of rows inserted
   */
  @Transactional(rollbackFor = Exception.class)
  public Integer insert(BasicDynaBean bean, String remarks) {
    bean.set("emp_username",
        sessionService.getSessionAttributes(new String[] { "userId" }).get("userId"));
    userMrnoAssociationAuditLogService.addGrantedRecord((String)bean.get("emp_username"),
        (String)bean.get("mr_no"), remarks);
    return super.insert(bean);
  }
  
  /**
   * Delete user-mrno associations for a given user. 
   * Called either when the user initiates logout and when the user logins again.
   *
   * @param username the username
   */
  @Transactional(rollbackFor = Exception.class)
  public void deleteUserMrNoAssociations(String username) {
    if (this.getRepository().exist("emp_username", username)) {
      userMrnoAssociationAuditLogService.addRevokedRecord(username);
      this.getRepository().delete("emp_username", username);
    }
  }

}
