package com.insta.hms.mdm.diagtestresultcenters;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * The Class DiagTestResultCenterService.
 *
 * @author anil.n
 */
@Service
public class DiagTestResultCenterService extends MasterService {

  /** The diag test result center repository. */
  @LazyAutowired
  private DiagTestResultCenterRepository diagTestResultCenterRepository;

  /**
   * Instantiates a new diag test result center service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public DiagTestResultCenterService(DiagTestResultCenterRepository repo,
      DiagTestResultCenterValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the bean repository.
   *
   * @return the bean repository
   */
  public BasicDynaBean getBeanRepository() {
    return diagTestResultCenterRepository.getBean();
  }

  /**
   * Insert test result centers.
   *
   * @param results the results
   * @return true, if successful
   */
  public boolean insertTestResultCenters(ArrayList<Result> results) {

    boolean success = true;
    for (int i = 0; i < results.size(); i++) {
      if (!"".equals(results.get(i).getExpression())) {
        BasicDynaBean diagbean = diagTestResultCenterRepository.getBean();
        diagbean.set("result_center_id", diagTestResultCenterRepository.getNextSequence());
        diagbean.set("resultlabel_id", Integer.parseInt(results.get(i).getResultlabel_id()));
        diagbean.set("center_id", com.bob.hms.common.RequestContext.getCenterId());
        diagbean.set("status", "A");
        int result = diagTestResultCenterRepository.insert(diagbean);
        if (result > 0) {
          success = success && true;
        } else {
          success = success && false;
        }
      } else {
        BasicDynaBean diagbean = diagTestResultCenterRepository.getBean();
        diagbean.set("result_center_id", diagTestResultCenterRepository.getNextSequence());
        diagbean.set("resultlabel_id", Integer.parseInt(results.get(i).getResultlabel_id()));
        diagbean.set("center_id", com.bob.hms.common.RequestContext.getCenterId());
        diagbean.set("status", "A");
        int result = diagTestResultCenterRepository.insert(diagbean);
        if (result > 0) {
          success = success && true;
        } else {
          success = success && false;
        }
      }
    }
    return success;
  }

  /**
   * Delete results center.
   *
   * @param deletedResults the deleted results
   * @param success the success
   * @return true, if successful
   */
  public boolean deleteResultsCenter(ArrayList<Result> deletedResults, boolean success) {

    if (success) {
      BasicDynaBean diagbean = getRepository().getBean();
      for (Result modifedResultRange : deletedResults) {
        diagbean = getRepository().findByKey("resultlabel_id",
            new Integer(modifedResultRange.getResultlabel_id()));
        if (diagbean != null) {
          success &= getRepository().delete("resultlabel_id",
              new Integer(modifedResultRange.getResultlabel_id())) > 0;
        }
      }
      return success;
    } else {
      return false;
    }
  }

  /**
   * Gets the next sequence.
   *
   * @return the next sequence
   */
  public int getNextSequence() {
    return diagTestResultCenterRepository.getNextSequence();
  }

  /**
   * Delete centers.
   *
   * @param bean the bean
   * @return the int
   */
  public int deleteCenters(BasicDynaBean bean) {
    Integer resultLabelId = (Integer) bean.get("resultlabel_id");
    Integer centerId = (Integer) bean.get("center_id");
    return diagTestResultCenterRepository.deleteCenters(resultLabelId, centerId);
  }
}
