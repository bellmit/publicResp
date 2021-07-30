package com.insta.hms.mdm.diagnosisstatus;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DiagnosisStatusService.
 *
 * @author sonam
 */
@Service
public class DiagnosisStatusService extends MasterService {

  /** The diagnosis status repo. */
  @LazyAutowired
  private DiagnosisStatusRepository diagnosisStatusRepo;

  /**
  * Instantiates a new diagnosis status service.
  *
  * @param repo the repo
  * @param validator the validator
  */
  public DiagnosisStatusService(DiagnosisStatusRepository repo,
      DiagnosisStatusValidation validator) {
   super(repo, validator);

  }

  /**
  * Gets the diagnosis status list.
  *
  * @return the diagnosis status list
  */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<BasicDynaBean> getDiagnosisStatusList() {
    List columns = new ArrayList();
    columns.add("diagnosis_status_id");
    columns.add("diagnosis_status_name");
    return diagnosisStatusRepo.listAll(columns, "status", "A",
    "diagnosis_status_name");
  }

}
