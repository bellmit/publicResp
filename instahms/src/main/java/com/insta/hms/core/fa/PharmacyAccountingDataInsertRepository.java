package com.insta.hms.core.fa;

import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.mdm.chargeheads.ChargeheadConstantsModel;
import com.insta.hms.model.HmsAccountingInfoModel;
import com.insta.hms.model.StoreSalesMainModel;

import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PharmacyAccountingDataInsertRepository.
 */
@Repository
public class PharmacyAccountingDataInsertRepository extends GenericHibernateRepository {

  /**
   * Gets the pharmacy bill model.
   *
   * @param phBillNo the ph bill no
   * @return the pharmacy bill model
   */
  public StoreSalesMainModel getPharmacyBillModel(String phBillNo) {
    return (StoreSalesMainModel) load(StoreSalesMainModel.class, phBillNo);
  }

  /**
   * Gets the charge head constants model.
   *
   * @param chargeHeadId the charge head id
   * @return the charge head constants model
   */
  public ChargeheadConstantsModel getChargeHeadConstantsModel(String chargeHeadId) {
    return (ChargeheadConstantsModel) load(ChargeheadConstantsModel.class, chargeHeadId);
  }

  /** The Constant LAST_JOB_TRANSACTION_ID_FOR_REVERSALS. */
  private static final String LAST_JOB_TRANSACTION_ID_FOR_REVERSALS = " SELECT MAX(jobTransaction) "
      + " AS max_job_transaction FROM HmsAccountingInfoModel haim "
      + " WHERE haim.saleBillNo=? AND jobTransaction is not null "
      + " AND jobTransaction > 0 AND haim.voucherType=(select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey ='VOUCHER_TYPE_PHBILLS') "
      + " GROUP BY haim.jobTransaction ORDER BY jobTransaction DESC ";

  /**
   * Gets the last job log id for reversal posts.
   *
   * @param phBillNo the ph bill no
   * @return the last job log id for reversal posts
   */
  @SuppressWarnings("unchecked")
  public Integer getLastJobTransactionIdForReversalPosts(String phBillNo) {
    List<Integer> resultList = (List<Integer>) executeHqlQuery(
        LAST_JOB_TRANSACTION_ID_FOR_REVERSALS, new Object[] { phBillNo }, 1);
    if (resultList != null && !resultList.isEmpty()) {
      return resultList.get(0);
    }
    return null;
  }

  /** The Constant GET_LAST_ACCOUNTING_DATA_FOR_BILL. */
  private static final String GET_LAST_ACCOUNTING_DATA_FOR_BILL = " FROM "
      + " HmsAccountingInfoModel haim WHERE haim.transactionType='N' "
      + " AND haim.voucherType IN (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in ('VOUCHER_TYPE_PHBILLS','VOUCHER_TYPE_INVTRANS')) AND haim.saleBillNo=? "
      + " AND haim.jobTransaction=? ";

  /**
   * Gets the last accounting data for bill.
   *
   * @param phBillNo       the ph bill no
   * @param jobTransaction the job log id
   * @return the last accounting data for bill
   */
  @SuppressWarnings("unchecked")
  public List<HmsAccountingInfoModel> getLastAccountingDataForBill(String phBillNo,
      Integer jobTransaction) {
    return (List<HmsAccountingInfoModel>) executeHqlQuery(GET_LAST_ACCOUNTING_DATA_FOR_BILL,
        new Object[] { phBillNo, jobTransaction });
  }
}
