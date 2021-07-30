package com.insta.hms.batchjob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class OpDeactivateJob.
 *
 * @author insta
 * 
 *         # # Daily run of deactivate OP/Diag patients who have no pending activities #
 */
public class OpDeactivateJob extends SQLUpdateJob {
  
  @LazyAutowired
  protected ModulesActivatedService modulesActivatedService;

  private static Logger logger = LoggerFactory.getLogger(OpDeactivateJob.class);
  
  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.batchjob.SQLUpdateJob#executeInternal(org.quartz.JobExecutionContext)
   */
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    setJobConnectionDetails();
    
    boolean modMalaffiEnabled = modulesActivatedService.isModuleActivated("mod_hie");
    
    String dischargeDetailsUpdate =
        ",discharge_date=current_timestamp,discharge_time=current_timestamp";

    /* Will execute for paid bill schema */
    String preparedBillSchema = "select auto_close_visit from generic_preferences "
        + " where auto_close_visit = 'P'";
    if (DatabaseHelper.queryToDynaBean(preparedBillSchema) != null) {

      String deActivateOPPaidBillsschemas = " UPDATE patient_registration pr SET status = 'I',"
          + " mod_time = current_timestamp #dischargeDataUpdate#"
          + " WHERE pr.patient_id in (select pr.patient_id from patient_registration pr "
          + " where pr.visit_type = 'o' AND pr.status = 'A' " + " AND reg_date < current_date "
          + " AND NOT EXISTS (SELECT b.visit_id FROM bill b WHERE b.visit_id=pr.patient_id "
          + " AND b.status NOT IN ('X','C') "
          + " AND ((b.bill_type = 'P' AND b.payment_status != 'P') OR (b.bill_type = 'C' "
          + " AND b.discharge_status != 'Y'))) limit 100)";
      
      String getVisitIdsQuery = "SELECT pr.patient_id,pr.center_id,pr.op_type"
          + " FROM patient_registration pr"
          + " WHERE pr.visit_type = 'o' AND pr.status = 'A' AND reg_date < current_date"
          + " AND NOT EXISTS (SELECT b.visit_id FROM bill b WHERE b.visit_id=pr.patient_id"
          + " AND b.status NOT IN ('X','C')"
          + " AND ((b.bill_type = 'P' AND b.payment_status != 'P') OR (b.bill_type = 'C'"
          + " AND b.discharge_status != 'Y'))) LIMIT 100";
      
      logger.debug(
          "Deactivating OP OP Patient in schema *** " + RequestContext.getSchema() + " *** ");
      int total = 0;
      int count = 1;
      List<BasicDynaBean> visitIds = new ArrayList<>();
      while (count > 0) {
        visitIds.addAll(DatabaseHelper.queryToDynaList(getVisitIdsQuery));
        count = DatabaseHelper.update(modMalaffiEnabled
            ? deActivateOPPaidBillsschemas.replace("#dischargeDataUpdate#", dischargeDetailsUpdate)
            : deActivateOPPaidBillsschemas.replace("#dischargeDataUpdate#", ""));
        total = total + count;
      }
      logger.info(RequestContext.getSchema() + " Deactivated (" + total + ") Paid bill");
      triggerEvents(visitIds);
    }

    String allBillSchemasSql = "select auto_close_visit from generic_preferences "
        + " where auto_close_visit = 'A'";
    if (DatabaseHelper.queryToDynaBean(allBillSchemasSql) != null) {
      
      String deActivatingOPAllSchemas = "WITH cte AS ("
          + " select patient_id from patient_registration pr where pr.visit_type = 'o' AND "
          + " pr.status = 'A' AND reg_date < current_date limit 100) "
          + " UPDATE patient_registration pr SET status = 'I', "
          + " mod_time = current_timestamp #dischargeDataUpdate# FROM cte"
          + " WHERE  cte.patient_id = pr.patient_id;";

      String getVisitIdsQuery = "SELECT patient_id,center_id,op_type"
          + " FROM patient_registration"
          + " WHERE visit_type = 'o' AND status = 'A'"
          + " AND reg_date < current_date LIMIT 100";

      logger.debug("Deactivating All OP patients (all) in  in schema *** "
          + RequestContext.getSchema() + " *** ");
      int total = 0;
      int count = 1;
      List<BasicDynaBean> visitIds = new ArrayList<>();
      while (count > 0) {
        visitIds.addAll(DatabaseHelper.queryToDynaList(getVisitIdsQuery));
        count = DatabaseHelper.update(modMalaffiEnabled
            ? deActivatingOPAllSchemas.replace("#dischargeDataUpdate#", dischargeDetailsUpdate)
            : deActivatingOPAllSchemas.replace("#dischargeDataUpdate#", ""));
        total = total + count;
      }
      logger.info(RequestContext.getSchema() + " Deactivated (" + total + ") All bill");
      triggerEvents(visitIds);
    }
  }

  /**
   * Triggers HL7 close event trigger.
   * 
   * @param visitIds the visit id list
   */
  private void triggerEvents(List<BasicDynaBean> visitIds) {
    if (!visitIds.isEmpty()) {
      for (BasicDynaBean bean : visitIds) {
        if (bean != null && !StringUtils.isEmpty(bean.get("patient_id"))) {
          interfaceEventMappingService.visitCloseEvent(bean.get("patient_id").toString(),
              (int) bean.get("center_id"));
        }
      }
    }
  }
}
