package com.insta.hms.batchjob;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillChargeRepository;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.insurance.SponsorBO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxCalculationJob extends SQLUpdateJob {

  private static Logger logger = LoggerFactory.getLogger(TaxCalculationJob.class);

  @LazyAutowired
  private BillChargeTaxService billChargeTaxService;

  @LazyAutowired
  private BillChargeRepository billChargeRepo;

  private static final String GET_BILL_CHARGE_TAX_LIST = "SELECT "
      + " b.visit_id, b.bill_no, foo.charge_id "
      + " FROM bill b" + " JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "
      + " JOIN (SELECT charge_id FROM bill_charge_tax "
      + " GROUP BY charge_id HAVING count(charge_id) > 1) as foo ON "
      + " (bc.charge_id = foo.charge_id) " + " WHERE b.status IN('A','F') ";

  private static final String DELETE_BILL_CHARGE_TAXES = "DELETE FROM bill_charge_tax "
      + " WHERE charge_id IN("
      + "SELECT foo.charge_id " + " FROM bill b"
      + " JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "
      + " JOIN (SELECT charge_id FROM bill_charge_tax "
      + " GROUP BY charge_id HAVING count(charge_id) > 1) as "
      + " foo ON (bc.charge_id = foo.charge_id) " + " WHERE b.status IN('A','F') ) ";

  private static final String DELETE_BILL_CHARGE_CLAIM_TAXES = "DELETE FROM bill_charge_claim_tax "
      + " WHERE charge_id IN("
      + "SELECT foo.charge_id " + " FROM bill b"
      + " JOIN bill_charge bc ON(b.bill_no = bc.bill_no) "
      + " JOIN (SELECT charge_id FROM bill_charge_tax "
      + " GROUP BY charge_id HAVING count(charge_id) > 1) as foo "
      + " ON (bc.charge_id = foo.charge_id) " + " WHERE b.status IN('A','F') ) ";

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    setJobConnectionDetails();

    try {
      List<BasicDynaBean> billChargeTaxList = DatabaseHelper
          .queryToDynaList(GET_BILL_CHARGE_TAX_LIST);
      DatabaseHelper.update(DELETE_BILL_CHARGE_TAXES);
      DatabaseHelper.update(DELETE_BILL_CHARGE_CLAIM_TAXES);

      List<BasicDynaBean> billChargeList = new ArrayList<BasicDynaBean>();

      for (BasicDynaBean taxBean : billChargeTaxList) {
        String chargeId = (String) taxBean.get("charge_id");
        BasicDynaBean chgBean = billChargeRepo.findByKey("charge_id", chargeId);
        billChargeList.add(chgBean);
      }
      billChargeTaxService.batchInsert(billChargeList);

      Map<String, List<BasicDynaBean>> visitTaxMap = new HashMap<String, List<BasicDynaBean>>();
      visitTaxMap = ConversionUtils.listBeanToMapListBean(billChargeTaxList, "visit_id");

      for (String key : visitTaxMap.keySet()) {
        new SponsorBO().recalculateSponsorAmount(key);
      }

    } catch (Exception exception) {
      logger.info(" Error in saving bill charge taxes.. : " + exception.getMessage());
      throw new JobExecutionException();
    }

  }

}
