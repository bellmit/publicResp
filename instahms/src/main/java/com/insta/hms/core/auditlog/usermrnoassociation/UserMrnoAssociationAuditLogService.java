package com.insta.hms.core.auditlog.usermrnoassociation;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserMrnoAssociationAuditLogService {

  Logger logger = LoggerFactory.getLogger(this.getClass());
  @LazyAutowired
  UserMrnoAssociationAuditlogRepository repository;
  @LazyAutowired
  UserService userService;
  @LazyAutowired
  PatientDetailsService patientDetailsService;

  /**
   * Add auditlog entry when granting access to a mrno.
   * 
   * @param username
   *          employee username
   * @param mrno
   *          patient mr number
   * @param remarks
   *          remarks as to why break the glass was required
   * @return number of rows inserted
   */
  public Integer addGrantedRecord(String username, String mrno, String remarks) {
    if (validateInsertParameters(username, mrno, remarks)) {
      BasicDynaBean bean = getBean(username, mrno, remarks, new Timestamp(new Date().getTime()),
          null);
      return repository.insert(bean);
    }
    return null;
  }

  /**
   * Add record when access is revoked from a username.
   * 
   * @param username
   *          employee username
   * @return number of rows updated
   * 
   */
  public Integer addRevokedRecord(String username) {
    if (validateUpdateParameters(username)) {
      BasicDynaBean bean = getBean(username, null, null, null, new Timestamp(new Date().getTime()));
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("emp_username", username);
      return repository.update(bean, keys);
    }
    return null;
  }

  private BasicDynaBean getBean(String username, String mrno, String remarks,
      Timestamp grantedAtTimestamp, Timestamp revokedAtTimestamp) {
    Map<String, Object> auditLogParams = new HashMap<String, Object>();
    auditLogParams.put("emp_username", username);
    if (grantedAtTimestamp != null) {
      auditLogParams.put("granted_access_at", grantedAtTimestamp);
    }
    if (revokedAtTimestamp != null) {
      auditLogParams.put("revoked_access_at", revokedAtTimestamp);
    }
    if (mrno != null) {
      auditLogParams.put("mr_no", mrno);
    }
    if (remarks != null) {
      auditLogParams.put("remarks", remarks);
    }
    BasicDynaBean bean = repository.getBean();
    List<String> errorFields = new ArrayList<String>();
    ConversionUtils.copyToDynaBean(auditLogParams, bean, errorFields);
    if (errorFields.isEmpty()) {
      return bean;
    } else {
      logger.warn("Unable to convert to bean with errors:");
      for (String errorField : errorFields) {
        logger.error(errorField);
      }
      throw new ValidationException();
    }
  }

  private boolean validateInsertParameters(String username, String mrno, String remarks) {
    ValidationErrorMap validationErrors = new ValidationErrorMap();
    Boolean error = false;
    if (userService.findByKey("emp_username", username) == null) {
      validationErrors.addError("username", "exception.entity.not.found",
          Arrays.asList(new String[] { "user", "name", username }));
      error = true;
    }
    if (patientDetailsService.findByKey(mrno) == null) {
      validationErrors.addError("mr_no", "exception.entity.not.found",
          Arrays.asList(new String[] { "patient", "MR NO", mrno }));
      error = true;
    }
    if (StringUtil.isNullOrEmpty(remarks)) {
      validationErrors.addError("remarks", "exception.notnull.value",
          Arrays.asList(new String[] { "Remarks" }));
      error = true;
    }
    if (error) {
      ValidationException ex = new ValidationException(validationErrors);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      Map<String, List<String>> errors = ex.getErrors();
      for (String key : errors.keySet()) {
        nestedException.put(key, errors.get(key));
      }
      throw new NestableValidationException(nestedException);
    }
    return true;
  }

  private boolean validateUpdateParameters(String username) {
    if (userService.findByKey("emp_username", username) != null) {
      return true;
    }
    throw new EntityNotFoundException("exception.bad.request");
  }

}
