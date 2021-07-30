package com.insta.hms.integration.insurance.remittance;

import com.insta.hms.jobs.GenericJob;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class RemittanceJob.
 */
@Component
public class RemittanceJob extends GenericJob {

  /** The remittance service. */
  @Autowired
  private RemittanceService remittanceService;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RemittanceJob.class);

  /** The remit id. */
  private int remitId;

  /**
   * Gets the remit id.
   *
   * @return the remit id
   */
  public int getRemitId() {
    return remitId;
  }

  /**
   * Sets the remit id.
   *
   * @param remitId the new remit id
   */
  public void setRemitId(int remitId) {
    this.remitId = remitId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org
   * .quartz.JobExecutionContext)
   */
  @Override
  @Transactional
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    Integer remitId = getRemitId();
    setJobConnectionDetails();
    // Here we are going the update the job process status.
    BasicDynaBean bean = remittanceService.getBean();
    bean.set("processing_status", "P");
    bean.set("remittance_id", remitId);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("remittance_id", remitId);
    remittanceService.update(bean, keys);
    
    boolean noErrorsExist;
    try {
      noErrorsExist = remittanceService.validateXML(remitId);
      if (noErrorsExist) {
        // update claim_submissions and set status to R (received)
        // against relevant claims
        remittanceService.updateClaimRecvd(remitId);
        // update charges
        remittanceService.updateCharges(remitId);
        // update Status
        remittanceService.updateStatus(remitId);

        // Check for warnings
        if (remittanceService.doWarningsExist(remitId)) {
          // set status as partially completed/ incomplete
          bean.set("processing_status", "I");
        } else {
          // Here we are going to update the complete status.
          bean.set("processing_status", "C");
        }
        keys.put("remittance_id", remitId);
        remittanceService.update(bean, keys);
        
        remittanceService.postRemittanceHistory(remitId);
      } else {
        logger.warn("Errors exist in the uploaded remittance");
        setFailedJobStatus(bean, keys);
      }
    } catch (Exception exception) {
      setFailedJobStatus(bean, keys);
      logger.error("", exception);
      throw new JobExecutionException(exception);
    }
  }

  /**
   * Sets the failed job status in insurance_remittance table.
   *
   * @param bean the bean
   * @param keys the keys
   */
  private void setFailedJobStatus(BasicDynaBean bean, Map<String, Object> keys) {
    // Else block through exception then Here we are going to update the
    // error status.
    bean.set("processing_status", "F");
    keys.put("remittance_id", remitId);
    remittanceService.update(bean, keys);
  }
}
