package com.insta.hms.core.fa;

import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.core.billing.BillActivityChargeModel;
import com.insta.hms.core.billing.BillChargeModel;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.insurance.InsuranceClaimModel;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyMasterModel;
import com.insta.hms.mdm.insuranceplans.InsurancePlanMainModel;
import com.insta.hms.model.HmsAccountingInfoModel;


import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class AccountingDataInsertRepository.
 */
@Repository
public class AccountingDataInsertRepository extends GenericHibernateRepository {

  /**
   * Gets the bill charge models by bill no.
   *
   * @param billNo the bill no
   * @return list of bill charge model by bill no
   */
  public List<BillChargeModel> getBillChargeModelsByBillNo(String billNo) {
    return (List<BillChargeModel>) getSession()
            .createCriteria(BillChargeModel.class)
            .add(Restrictions.eq("billNo.billNo", billNo)).list();
  }

  /**
   * Get Insurance Company Master Model.
   *
   * @param claimId claim id
   * @return insurance company master model
   */
  public InsuranceCompanyMasterModel getInsuranceCoModelByClaimId(String claimId) {
    Session session = getSession();
    InsuranceClaimModel claim =  (InsuranceClaimModel) session
        .load(InsuranceClaimModel.class, claimId);
    if (claim == null) {
      return null;
    }
    InsurancePlanMainModel plan = claim.getPlanId();
    if (plan == null) {
      return null;
    }
    return plan.getInsuranceCoId();
  }

  /**
   * Gets the bill model by bill no.
   *
   * @param billNo
   *          the bill no
   * @return the bill model by bill no
   */
  public BillModel getBillModelByBillNo(String billNo) {
    return (BillModel) load(BillModel.class, billNo);
  }

  /**
   * Insert accounting data from list.
   *
   * @param accountingDataToInsert
   *          the accounting data to insert
   */
  public void insertAccountingDataFromList(List<HmsAccountingInfoModel> accountingDataToInsert) {
    batchInsert(accountingDataToInsert);
  }

  /** The Constant ACCOUNTING_NEXT_ID. */
  private static final String ACCOUNTING_NEXT_ID = "SELECT generate_id('ACCOUNTING_VOUCHER')";

  /**
   * Generate accounting next id.
   *
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public String generateAccountingNextId() {
    List<String> resultList = (List<String>) executeQuery(ACCOUNTING_NEXT_ID);
    if (resultList != null && resultList.size() > 0) {
      return resultList.get(0);
    }
    return null;
  }

  /** The Constant LAST_JOB_TRANSACTION_ID_FOR_REVERSALS. */
  private static final String LAST_JOB_TRANSACTION_ID_FOR_REVERSALS = " SELECT MAX(jobTransaction) "
      + " AS max_job_transaction FROM HmsAccountingInfoModel haim "
      + " WHERE haim.voucherNo=? AND jobTransaction is not null "
      + " AND jobTransaction > 0 "
      + " AND haim.voucherType in (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in ('VOUCHER_TYPE_HOSPBILLS','VOUCHER_TYPE_CREDITNOTE') ) GROUP BY haim.jobTransaction "
      + " ORDER BY jobTransaction DESC ";
  

  /**
   * Gets the last job transaction id for reversal posts.
   *
   * @param billNo
   *          the bill no
   * @return the last job transaction id for reversal posts
   */
  @SuppressWarnings("unchecked")
  public Integer getLastJobTransactionIdForReversalPosts(String billNo) {
    List<Integer> resultList = (List<Integer>) executeHqlQuery(
        LAST_JOB_TRANSACTION_ID_FOR_REVERSALS, new Object[] { billNo }, 1);
    if (resultList != null && resultList.size() > 0) {
      return (Integer) resultList.get(0);
    }
    return null;
  }

  /** The Constant GET_LAST_ACCOUNTING_DATA_FOR_BILL. */
  private static final String GET_LAST_ACCOUNTING_DATA_FOR_BILL = " FROM "
      + " HmsAccountingInfoModel haim WHERE haim.transactionType='N' "
      + " AND haim.voucherType IN (select voucherDefinition from FaVoucherDefinitionsModel fvd where fvd.voucherKey in('VOUCHER_TYPE_HOSPBILLS','VOUCHER_TYPE_INVTRANS','VOUCHER_TYPE_CREDITNOTE')) AND sale_bill_no is null "
      + " AND haim.voucherNo=? AND haim.jobTransaction=? ";

  /**
   * Gets the last accounting data for bill.
   *
   * @param billNo
   *          the bill no
   * @param jobTransaction
   *          the job transaction
   * @return the last accounting data for bill
   */
  @SuppressWarnings("unchecked")
  public List<HmsAccountingInfoModel> getLastAccountingDataForBill(String billNo,
      Integer jobTransaction) {
    return (List<HmsAccountingInfoModel>) executeHqlQuery(GET_LAST_ACCOUNTING_DATA_FOR_BILL,
        new Object[] { billNo, jobTransaction });
  }

  /** The Constant ACCOUNTING_NEXT_JOB_TRANSACTION. */
  private static final String ACCOUNTING_NEXT_JOB_TRANSACTION = "SELECT CAST ( "
      + " nextval('job_transaction_seq') AS INTEGER) AS job_transaction ";

  /**
   * Generate next job transaction.
   *
   * @return the integer
   */
  @SuppressWarnings("unchecked")
  public Integer generateNextJobTransaction() {
    List<Integer> resultList = (List<Integer>) executeQuery(ACCOUNTING_NEXT_JOB_TRANSACTION);
    if (resultList != null && !resultList.isEmpty()) {
      return resultList.get(0);
    }
    return null;
  }

  /** The Constant POST_REVERSALS_FOR_MIGRATED_DATA_OF_BILL. */
  private static final String POST_REVERSALS_FOR_MIGRATED_DATA_OF_BILL = " SELECT "
      + " post_reversals_for_migrated_bill(?) ";

  /**
   * Post reversals for migrated data of bill. This method will post the reversals of a bill if
   * accounting data is there via old cron
   *
   * @param billNo
   *          the bill no
   */
  public void postReversalsForMigratedDataOfBill(String billNo) {
    executeQuery(POST_REVERSALS_FOR_MIGRATED_DATA_OF_BILL, new Object[] { billNo });
  }

  /** The Constant STOCK_QUERY. */
  private static final String STOCK_QUERY = "SELECT bacm FROM BillChargeModel bcm "
      + " join bcm.billActivityChargeModel bacm with bacm.activityCode='PHI' "
      + " WHERE bcm.chargeId=? ";

  @SuppressWarnings("unchecked")
  /**
   * Get Inventory Item Activity Charge Model.
   *
   * @param chargeId
   *          charge id
   */
  public List<BillActivityChargeModel> getInventoryItemActivityChargeModel(String chargeId) {
    return (List<BillActivityChargeModel>) executeHqlQuery(STOCK_QUERY, new Object[] { chargeId });
  }
  
  
  /** The Constant GET_BILL_NO_FOR_CREDIT_NOTE. */
  private static final String GET_BILL_NO_FOR_CREDIT_NOTE =   "SELECT bill_no FROM bill_credit_notes  WHERE credit_note_bill_no = ?";

  /**
   * Gets the bill credit notes model by bill no.
   *
   * @param creditNoteBillNo the credit note bill no
   * @return the bill credit notes model by bill no
   */
  public List  getBillCreditNotesModelByBillNo(String creditNoteBillNo) {
   return executeQuery(GET_BILL_NO_FOR_CREDIT_NOTE, new Object[] { creditNoteBillNo });
  }
  
  /** The Constant GET_FA_LEDGER_DEFINITION. */
  private static final String GET_FA_LEDGER_DEFINITION = "SELECT ledger_description from fa_ledger_definitions WHERE ledger_key= ?";
  
  /**
   * Gets the ledger definition.
   *
   * @param ledgerKey the ledger key
   * @return the ledger definition
   */
  public String getLedgerDefinition(String ledgerKey) {
    return  String.valueOf(executeQuery(GET_FA_LEDGER_DEFINITION, new Object[] {ledgerKey}).get(0));   
  }
  
  /** The Constant GET_VOUCHER_DEFINITION. */
  private static final String GET_VOUCHER_DEFINITION = "SELECT voucher_definition from fa_voucher_definitions WHERE voucher_key = ?";
  
  /**
   * Gets the voucher definition.
   *
   * @param voucherKey the voucher key
   * @return the voucher definition
   */
  public String getVoucherDefinition(String voucherKey) {
    return  String.valueOf(executeQuery(GET_VOUCHER_DEFINITION, new Object[] {voucherKey}).get(0));   
  }
  
  /** The Constant GET_FA_CONFIGURATION. */
  private static final String GET_FA_CONFIGURATION = "SELECT ## from fa_configuration ";
  
  /**
   * Gets the fa configuration.
   *
   * @param config the config
   * @return the fa configuration
   */
  public String getfaConfiguration(String config) {
    String query = GET_FA_CONFIGURATION.replace("##", config);
    return String.valueOf(executeQuery(query).get(0));
  }
}
