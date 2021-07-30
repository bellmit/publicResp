package testng.com.insta.hms.core.fa;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillChargeClaimModel;
import com.insta.hms.core.billing.BillChargeClaimTaxModel;
import com.insta.hms.core.billing.BillChargeModel;
import com.insta.hms.core.billing.BillChargeTaxModel;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.fa.AccountingDataInsertRepository;
import com.insta.hms.core.fa.AccountingDataInsertService;
import com.insta.hms.model.HmsAccountingInfoModel;

import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import testng.utils.TestRepoInit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class AccountingDataInsertServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  @InjectMocks
  private AccountingDataInsertService accountingDataInsertService;

  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  @LazyAutowired
  private GenericPreferencesService genPrefService;

  private Map<String, Object> dbDataMap = null;
  TestRepoInit testRepo = null;

  @BeforeMethod
  public void init(Method testMethod) throws IOException {
    System.out.println("@TEST : currently running -> " + testMethod.getName() + " of "
        + testMethod.getDeclaringClass());

    initMocks(this);
    ReflectionTestUtils.setField(accountingDataInsertService, "accountingDataInsertRepo",
        accountingDataInsertRepo);
    ReflectionTestUtils.setField(accountingDataInsertService, "genPrefService",
        genPrefService);

    testRepo = new TestRepoInit(accountingDataInsertRepo);
    // relations having foreign key with hospital_center_master
    testRepo.insert("hospital_center_master");
    testRepo.insert("user_center_billing_counters");
    testRepo.insert("hosp_receipt_seq_prefs");
    testRepo.insert("hosp_op_ip_seq_prefs");
    testRepo.insert("hosp_item_seq_prefs");
    testRepo.insert("hosp_bill_seq_prefs");
    testRepo.insert("hosp_bill_audit_seq_prefs");
    testRepo.insert("center_integration_details");

    testRepo.insert("chargehead_constants");
    testRepo.insert("chargegroup_constants");
    testRepo.insert("bill_account_heads");
    testRepo.insert("department");

    testRepo.insert("doctors");
    testRepo.insert("doctor_consultation_charge");
    testRepo.insert("patient_appointment_plan");
    testRepo.insert("patient_appointment_plan_details");

    testRepo.insert("tpa_master");
    testRepo.insert("insurance_company_master");
    testRepo.insert("insurance_plan_main");
    testRepo.insert("insurance_claim");

    testRepo.insert("patient_details");
    testRepo.insert("patient_registration");
    testRepo.insert("bill");
    testRepo.insert("bill_claim");
    testRepo.insert("bill_charge");
    testRepo.insert("bill_charge_tax");
    testRepo.insert("bill_charge_claim");
    testRepo.insert("bill_charge_claim_tax");
    testRepo.insert("item_sub_groups");
    testRepo.insert("hms_accounting_info");

    dbDataMap = testRepo.initializeRepo();
  }

  @AfterMethod
  public void rollbackchanges() {
    testRepo.rollbackTransaction();
  }

  @Test
  public void getAccountingDataToInsertNonInsuranceBillSuccessTest() {
    String billNo = "BC18000084";
    Integer jobLogId = 1111;
    Date createdAt = new Date();
    BillModel billModel = (BillModel) accountingDataInsertRepo.get(BillModel.class, billNo);
    billModel.setIsTpa(Boolean.FALSE);
    List<HmsAccountingInfoModel> resultList = accountingDataInsertService
        .getAccountingDataToInsert(billNo, billModel, jobLogId, createdAt);
    assertNotNull(resultList);
    assertEquals(resultList.size(), 3);

    // Assert patient amount entry
    HmsAccountingInfoModel accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))
          && accModel.getCreditAccount().equals(
              getChargeAccountHeadName(billModel.getBillCharges().iterator().next()))) {
        accountingModel = accModel;
        break;
      }
    }
    assertPatientEntryNonInsuranceBill(accountingModel, billModel);

    // Assert patient tax(CGST(5%)) amount entry
    BillChargeTaxModel billChargeTaxModel = null;
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      BillChargeModel bcModel = billModel.getBillCharges().iterator().next();
      Set<BillChargeTaxModel> bcTaxes = bcModel.getBillChargeTaxes();
      for (BillChargeTaxModel bctModel : bcTaxes) {
        if (accModel.getCustom4() != null
            && bctModel.getTaxSubGroupId().getItemSubgroupName().equals("CGST(5%)")
            && accModel.getCustom4().equals(bctModel.getTaxSubGroupId().getItemSubgroupName())) {
          accountingModel = accModel;
          billChargeTaxModel = bctModel;
          break;
        }
      }
    }
    assertPatientTaxEntryNonInsuranceBill(accountingModel, billModel, billChargeTaxModel);

    // Assert patient tax(SGST(5%)) amount entry
    billChargeTaxModel = null;
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      BillChargeModel bcModel = billModel.getBillCharges().iterator().next();
      Set<BillChargeTaxModel> bcTaxes = bcModel.getBillChargeTaxes();
      for (BillChargeTaxModel bctModel : bcTaxes) {
        if (accModel.getCustom4() != null
            && bctModel.getTaxSubGroupId().getItemSubgroupName().equals("SGST(5%)")
            && accModel.getCustom4().equals(bctModel.getTaxSubGroupId().getItemSubgroupName())) {
          accountingModel = accModel;
          billChargeTaxModel = bctModel;
          break;
        }
      }
    }
    assertPatientTaxEntryNonInsuranceBill(accountingModel, billModel, billChargeTaxModel);
  }

  @Test
  public void getAccountingDataToInsertSuccessTest() {
    String billNo = "BC18000084";
    Integer jobLogId = 1111;
    Date createdAt = new Date();
    BillModel billModel = (BillModel) accountingDataInsertRepo.get(BillModel.class, billNo);
    List<HmsAccountingInfoModel> resultList = accountingDataInsertService
        .getAccountingDataToInsert(billNo, billModel, jobLogId, createdAt);
    assertNotNull(resultList);
    assertEquals(resultList.size(), 6);

    // Assert sponsor amount accounting entry
    BillChargeModel bcModel = billModel.getBillCharges().iterator().next();
    BillChargeClaimModel chargeClaim = bcModel.getBillChargeClaims().iterator().next();
    HmsAccountingInfoModel accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(chargeClaim.getSponsorId().getTpaName())
          && accModel.getCreditAccount().equals(getChargeAccountHeadName(bcModel))) {
        accountingModel = accModel;
        break;
      }

    }
    assertSponsorAccountingEntry(accountingModel, billModel, chargeClaim);

    // Assert sponsor tax(CGST(5%)) accounting entry
    bcModel = billModel.getBillCharges().iterator().next();
    chargeClaim = bcModel.getBillChargeClaims().iterator().next();
    Set<BillChargeClaimTaxModel> billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
    accountingModel = null;
    BillChargeClaimTaxModel bcclTax = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (BillChargeClaimTaxModel bccltModel : billChargeClaimTaxes) {
        if (accModel != null
            && accModel.getCustom4() != null
            && accModel.getDebitAccount().equals(
                bccltModel.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? (bccltModel
                    .getSponsorId() != null ? bccltModel.getSponsorId().getTpaName() : null)
                    : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"))
            && accModel
                .getCreditAccount()
                .equals(
                    bccltModel.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                        : (bccltModel.getSponsorId() != null ? bccltModel.getSponsorId()
                            .getTpaName() : null))
            && bccltModel.getTaxSubGroupId().getItemSubgroupName().equals("CGST(5%)")
            && accModel.getCustom4().equals(bccltModel.getTaxSubGroupId().getItemSubgroupName())) {
          accountingModel = accModel;
          bcclTax = bccltModel;
          break;
        }
      }
    }
    assertSponsorTaxAccountingEntry(accountingModel, billModel, chargeClaim, bcclTax);

    // Assert sponsor tax(SGST(5%)) accounting entry
    bcModel = billModel.getBillCharges().iterator().next();
    chargeClaim = bcModel.getBillChargeClaims().iterator().next();
    billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
    accountingModel = null;
    bcclTax = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (BillChargeClaimTaxModel bccltModel : billChargeClaimTaxes) {
        if (accModel != null
            && accModel.getCustom4() != null
            && accModel.getDebitAccount().equals(
                bccltModel.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? (bccltModel
                    .getSponsorId() != null ? bccltModel.getSponsorId().getTpaName() : null)
                    : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"))
            && accModel
                .getCreditAccount()
                .equals(
                    bccltModel.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                        : (bccltModel.getSponsorId() != null ? bccltModel.getSponsorId()
                            .getTpaName() : null))
            && bccltModel.getTaxSubGroupId().getItemSubgroupName().equals("SGST(5%)")
            && accModel.getCustom4().equals(bccltModel.getTaxSubGroupId().getItemSubgroupName())) {
          accountingModel = accModel;
          bcclTax = bccltModel;
          break;
        }
      }
    }
    assertSponsorTaxAccountingEntry(accountingModel, billModel, chargeClaim, bcclTax);

    // Assert sponsor bill patient Amount accounting entry
    bcModel = billModel.getBillCharges().iterator().next();
    chargeClaim = bcModel.getBillChargeClaims().iterator().next();
    billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
    BigDecimal netPatAmount = bcModel.getAmount().subtract(bcModel.getInsuranceClaimAmount());
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel
          .getDebitAccount()
          .equals(
              netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
                  : getChargeAccountHeadName(bcModel))
          && accModel.getCreditAccount().equals(
              netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? getChargeAccountHeadName(bcModel)
                  : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))) {
        accountingModel = accModel;
        break;
      }
    }
    assertSponsorBillPatientAmountAccountingEntry(accountingModel, billModel);

    // Assert sponsor bill patient tax CGST(5%) Amount accounting entry
    bcModel = billModel.getBillCharges().iterator().next();
    Set<BillChargeTaxModel> billChargeTaxes = bcModel.getBillChargeTaxes();

    chargeClaim = bcModel.getBillChargeClaims().iterator().next();
    billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
    Map<String, BigDecimal> chargeTaxIdClaimTaxMap = getChargeTaxIdAndClaimTaxMapping(billChargeClaimTaxes);

    accountingModel = null;
    BillChargeTaxModel bcTaxModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (BillChargeTaxModel bcTax : billChargeTaxes) {
        BigDecimal patTaxAmount = bcTax
            .getTaxAmount()
            .subtract(
                chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId()) != null
                    && !"".equals(chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId())) ? (BigDecimal) chargeTaxIdClaimTaxMap
                    .get("CTI" + bcTax.getChargeTaxId()) : BigDecimal.ZERO);

        if (accModel.getCustom4() != null
            && accModel
                .getDebitAccount()
                .equals(
                    patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
                        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"))
            && accModel
                .getCreditAccount()
                .equals(
                    patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))
            && bcTax.getTaxSubGroupId().getItemSubgroupName().equals("CGST(5%)")
            && accModel.getCustom4().equals(bcTax.getTaxSubGroupId().getItemSubgroupName())) {
          accountingModel = accModel;
          bcTaxModel = bcTax;
          break;
        }
      }
    }
    assertSponsorBillPatientTaxAmountAccountingEntry(accountingModel, billModel, bcTaxModel,
        chargeTaxIdClaimTaxMap);

    // Assert sponsor bill patient tax SGST(5%) Amount accounting entry
    bcModel = billModel.getBillCharges().iterator().next();
    billChargeTaxes = bcModel.getBillChargeTaxes();

    chargeClaim = bcModel.getBillChargeClaims().iterator().next();
    billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
    chargeTaxIdClaimTaxMap = getChargeTaxIdAndClaimTaxMapping(billChargeClaimTaxes);

    accountingModel = null;
    bcTaxModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (BillChargeTaxModel bcTax : billChargeTaxes) {
        BigDecimal patTaxAmount = bcTax
            .getTaxAmount()
            .subtract(
                chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId()) != null
                    && !"".equals(chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId())) ? (BigDecimal) chargeTaxIdClaimTaxMap
                    .get("CTI" + bcTax.getChargeTaxId()) : BigDecimal.ZERO);

        if (accModel.getCustom4() != null
            && accModel
                .getDebitAccount()
                .equals(
                    patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
                        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"))
            && accModel
                .getCreditAccount()
                .equals(
                    patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))
            && bcTax.getTaxSubGroupId().getItemSubgroupName().equals("SGST(5%)")
            && accModel.getCustom4().equals(bcTax.getTaxSubGroupId().getItemSubgroupName())) {
          accountingModel = accModel;
          bcTaxModel = bcTax;
          break;
        }
      }
    }
    assertSponsorBillPatientTaxAmountAccountingEntry(accountingModel, billModel, bcTaxModel,
        chargeTaxIdClaimTaxMap);
  }

  @Test
  public void insertAccountingDataFromListTest() {
    String billNo = "BC18000084";
    Integer jobLogId = 1111;
    Date createdAt = new Date();
    BillModel billModel = (BillModel) accountingDataInsertRepo.get(BillModel.class, billNo);
    List<HmsAccountingInfoModel> resultList = accountingDataInsertService
        .getAccountingDataToInsert(billNo, billModel, jobLogId, createdAt);
    accountingDataInsertService
        .insertAccountingDataForHospitalItems(billNo, billModel, jobLogId, createdAt);
    List<HmsAccountingInfoModel> insertedData = accountingDataInsertRepo
        .getLastAccountingDataForBill(billNo, jobLogId);
    assertNotNull(resultList);
    assertEquals(insertedData.size(), resultList.size());
  }

  @Test
  public void postReversalsForHospitalItemsTest() {
    String billNo = "BC18000084";
    Integer jobLogId = 1111;
    Integer newJobLogId = 2222;
    Date createdAt = new Date();
    BillModel billModel = (BillModel) accountingDataInsertRepo.get(BillModel.class, billNo);
    accountingDataInsertService
        .insertAccountingDataForHospitalItems(billNo, billModel, jobLogId, createdAt);
    accountingDataInsertService.postReversalsForHospitalItems(billNo, newJobLogId, createdAt);
    List<HmsAccountingInfoModel> accountingData = (List<HmsAccountingInfoModel>) accountingDataInsertRepo
        .executeHqlQuery(getHmsAccountingInfoModelHqlQuery(), new Object[] { billNo, newJobLogId });
    assertNotNull(accountingData);
    assertTrue(accountingData.size() > 0);
    assertEquals(accountingData.get(0).getTransactionType().toString(), "R");
  }

  private String getHmsAccountingInfoModelHqlQuery() {
    return " FROM HmsAccountingInfoModel haim WHERE haim.billNo=? AND haim.jobTransaction=? ";
  }

  private void assertPatientEntryNonInsuranceBill(HmsAccountingInfoModel accountingModel,
      BillModel billModel) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getMrNo());
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getOldmrno());
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        billModel.getVisitId().getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(accountingModel.getAdmittingDoctor() != null ? accountingModel
        .getAdmittingDoctor().trim() : null, billModel.getVisitId().getDoctor() != null ? billModel
        .getVisitId().getDoctor().getDoctorName() : null);
    assertEquals(accountingModel.getAdmittingDepartment() != null ? accountingModel
        .getAdmittingDepartment().trim() : null,
        billModel.getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName()
            .getDeptName() : null);
    assertEquals(accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor()
        .trim() : null, getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim()
        : null, billModel.getVisitId().getCenterId().getCenterName());
    assertEquals(accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim()
        : null, billModel.getBillNo());
    assertEquals(accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim()
        : null, accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertEquals(accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim()
        : null, null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null ? accountingModel
        .getAuditControlNumber().trim() : null, billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        billModel.getRemarks());

    assertEquals(accountingModel.getCurrencyConversionRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUnitRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getRoundOffAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getInvoiceNo(), null);
    assertEquals(accountingModel.getInvoiceDate(), null);
    assertEquals(accountingModel.getPoNumber(), null);
    assertEquals(accountingModel.getPoDate(), null);
    assertEquals(accountingModel.getSupplierName(), null);
    assertEquals(accountingModel.getCustSupplierCode(), null);
    assertEquals(accountingModel.getGrnDate(), null);
    assertEquals(accountingModel.getPurchaseVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPurchaseVatPercent(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatPercent(), BigDecimal.ZERO);

    BillChargeModel charge = billModel.getBillCharges().iterator().next();

    assertEquals(accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim()
        : null, charge.getActDescriptionId());
    assertEquals(accountingModel.getItemName() != null ? accountingModel.getItemName().trim()
        : null, charge.getActDescription());
    assertEquals(accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim()
        : null, charge.getChargeGroup().getChargegroupId());
    assertEquals(accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim()
        : null, charge.getChargeHead().getChargeheadId());
    assertEquals(accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup()
        .trim() : null, charge.getServiceSubGroupId() != null ? charge.getServiceSubGroupId()
        .getServiceGroupId().getServiceGroupName() : null);
    assertEquals(accountingModel.getServiceSubGroup() != null ? accountingModel
        .getServiceSubGroup().trim() : null, charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceSubGroupName() : null);
    assertEquals(accountingModel.getQuantity(), charge.getActQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), charge.getDiscount());
    assertEquals(accountingModel.getPrescribingDoctor() != null ? accountingModel
        .getPrescribingDoctor().trim() : null, charge.getPrescribingDocotor() != null ? charge
        .getPrescribingDocotor().getDoctorName() : null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null ? accountingModel
        .getPrescribingDoctorDeptName().trim() : null, charge.getPrescribingDocotor() != null
        && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
        .getDeptId().getDeptName() : null);
    assertEquals(accountingModel.getConductiongDoctor() != null ? accountingModel
        .getConductiongDoctor().trim() : null, charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getConductingDepartment() != null ? accountingModel
        .getConductingDepartment().trim() : null, charge.getConductingDepartment() != null ? charge
        .getConductingDepartment().getDeptName() : null);
    assertEquals(accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim()
        : null, charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName()
        : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), null);
    assertEquals(accountingModel.getIssueStoreCenter(), null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(), Integer.valueOf(0));
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(), null);
    assertEquals(accountingModel.getTaxAmount(), null);
    assertEquals(accountingModel.getCostAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), null);

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertEquals(
        accountingModel.getGrossAmount(),
        (charge.getAmount().add(charge.getDiscount())).compareTo(BigDecimal.ZERO) >= 0 ? (charge
            .getAmount().add(charge.getDiscount()))
            : (charge.getAmount().add(charge.getDiscount())).negate());
    assertEquals(accountingModel.getNetAmount(),
        charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? charge.getAmount() : charge
            .getAmount().negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(
        accountingModel.getDebitAccount(),
        charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : getChargeAccountHeadName(charge));
    assertEquals(accountingModel.getCreditAccount(),
        charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? getChargeAccountHeadName(charge)
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    assertEquals(accountingModel.getCustom1(), charge.getAmount().toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(), getChargeAccountHeadName(charge));

  }

  private void assertPatientTaxEntryNonInsuranceBill(HmsAccountingInfoModel accountingModel,
      BillModel billModel, BillChargeTaxModel bcTax) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getMrNo());
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getOldmrno());
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        billModel.getVisitId().getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(accountingModel.getAdmittingDoctor() != null ? accountingModel
        .getAdmittingDoctor().trim() : null, billModel.getVisitId().getDoctor() != null ? billModel
        .getVisitId().getDoctor().getDoctorName() : null);
    assertEquals(accountingModel.getAdmittingDepartment() != null ? accountingModel
        .getAdmittingDepartment().trim() : null,
        billModel.getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName()
            .getDeptName() : null);
    assertEquals(accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor()
        .trim() : null, getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim()
        : null, billModel.getVisitId().getCenterId().getCenterName());
    assertEquals(accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim()
        : null, billModel.getBillNo());
    assertEquals(accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim()
        : null, accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertEquals(accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim()
        : null, null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null ? accountingModel
        .getAuditControlNumber().trim() : null, billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        billModel.getRemarks());

    assertEquals(accountingModel.getCurrencyConversionRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUnitRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getRoundOffAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getInvoiceNo(), null);
    assertEquals(accountingModel.getInvoiceDate(), null);
    assertEquals(accountingModel.getPoNumber(), null);
    assertEquals(accountingModel.getPoDate(), null);
    assertEquals(accountingModel.getSupplierName(), null);
    assertEquals(accountingModel.getCustSupplierCode(), null);
    assertEquals(accountingModel.getGrnDate(), null);
    assertEquals(accountingModel.getPurchaseVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPurchaseVatPercent(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatPercent(), BigDecimal.ZERO);

    BillChargeModel charge = billModel.getBillCharges().iterator().next();

    assertEquals(accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim()
        : null, charge.getActDescriptionId());
    assertEquals(accountingModel.getItemName() != null ? accountingModel.getItemName().trim()
        : null, charge.getActDescription());
    assertEquals(accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim()
        : null, charge.getChargeGroup().getChargegroupId());
    assertEquals(accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim()
        : null, charge.getChargeHead().getChargeheadId());
    assertEquals(accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup()
        .trim() : null, charge.getServiceSubGroupId() != null ? charge.getServiceSubGroupId()
        .getServiceGroupId().getServiceGroupName() : null);
    assertEquals(accountingModel.getServiceSubGroup() != null ? accountingModel
        .getServiceSubGroup().trim() : null, charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceSubGroupName() : null);
    assertEquals(accountingModel.getQuantity(), charge.getActQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPrescribingDoctor() != null ? accountingModel
        .getPrescribingDoctor().trim() : null, charge.getPrescribingDocotor() != null ? charge
        .getPrescribingDocotor().getDoctorName() : null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null ? accountingModel
        .getPrescribingDoctorDeptName().trim() : null, charge.getPrescribingDocotor() != null
        && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
        .getDeptId().getDeptName() : null);
    assertEquals(accountingModel.getConductiongDoctor() != null ? accountingModel
        .getConductiongDoctor().trim() : null, charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getConductingDepartment() != null ? accountingModel
        .getConductingDepartment().trim() : null, charge.getConductingDepartment() != null ? charge
        .getConductingDepartment().getDeptName() : null);
    assertEquals(accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim()
        : null, charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName()
        : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), null);
    assertEquals(accountingModel.getIssueStoreCenter(), null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(), Integer.valueOf(0));
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(), null);
    assertEquals(accountingModel.getTaxAmount(), null);
    assertEquals(accountingModel.getCostAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), null);

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertEquals(accountingModel.getGrossAmount(), bcTax.getTaxAmount());
    assertEquals(accountingModel.getNetAmount(), bcTax.getTaxAmount());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(
        accountingModel.getDebitAccount(),
        bcTax.getTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
    assertEquals(accountingModel.getCreditAccount(), bcTax.getTaxAmount()
        .compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
        : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    assertEquals(accountingModel.getCustom1(), bcTax.getTaxAmount().toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(), bcTax.getTaxSubGroupId().getItemSubgroupName());
  }

  private void assertSponsorAccountingEntry(HmsAccountingInfoModel accountingModel,
      BillModel billModel, BillChargeClaimModel chargeClaim) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getMrNo());
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getOldmrno());
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        billModel.getVisitId().getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(accountingModel.getAdmittingDoctor() != null ? accountingModel
        .getAdmittingDoctor().trim() : null, billModel.getVisitId().getDoctor() != null ? billModel
        .getVisitId().getDoctor().getDoctorName() : null);
    assertEquals(accountingModel.getAdmittingDepartment() != null ? accountingModel
        .getAdmittingDepartment().trim() : null,
        billModel.getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName()
            .getDeptName() : null);
    assertEquals(accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor()
        .trim() : null, getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim()
        : null, billModel.getVisitId().getCenterId().getCenterName());
    assertEquals(accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim()
        : null, billModel.getBillNo());
    assertEquals(accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim()
        : null, accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertEquals(accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim()
        : null, null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null ? accountingModel
        .getAuditControlNumber().trim() : null, billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        billModel.getRemarks());

    assertEquals(accountingModel.getCurrencyConversionRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUnitRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getRoundOffAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getInvoiceNo(), null);
    assertEquals(accountingModel.getInvoiceDate(), null);
    assertEquals(accountingModel.getPoNumber(), null);
    assertEquals(accountingModel.getPoDate(), null);
    assertEquals(accountingModel.getSupplierName(), null);
    assertEquals(accountingModel.getCustSupplierCode(), null);
    assertEquals(accountingModel.getGrnDate(), null);
    assertEquals(accountingModel.getPurchaseVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPurchaseVatPercent(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatPercent(), BigDecimal.ZERO);

    BillChargeModel charge = billModel.getBillCharges().iterator().next();

    assertEquals(accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim()
        : null, charge.getActDescriptionId());
    assertEquals(accountingModel.getItemName() != null ? accountingModel.getItemName().trim()
        : null, charge.getActDescription());
    assertEquals(accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim()
        : null, charge.getChargeGroup().getChargegroupId());
    assertEquals(accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim()
        : null, charge.getChargeHead().getChargeheadId());
    assertEquals(accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup()
        .trim() : null, charge.getServiceSubGroupId() != null ? charge.getServiceSubGroupId()
        .getServiceGroupId().getServiceGroupName() : null);
    assertEquals(accountingModel.getServiceSubGroup() != null ? accountingModel
        .getServiceSubGroup().trim() : null, charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceSubGroupName() : null);
    assertEquals(accountingModel.getQuantity(), charge.getActQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPrescribingDoctor() != null ? accountingModel
        .getPrescribingDoctor().trim() : null, charge.getPrescribingDocotor() != null ? charge
        .getPrescribingDocotor().getDoctorName() : null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null ? accountingModel
        .getPrescribingDoctorDeptName().trim() : null, charge.getPrescribingDocotor() != null
        && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
        .getDeptId().getDeptName() : null);
    assertEquals(accountingModel.getConductiongDoctor() != null ? accountingModel
        .getConductiongDoctor().trim() : null, charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getConductingDepartment() != null ? accountingModel
        .getConductingDepartment().trim() : null, charge.getConductingDepartment() != null ? charge
        .getConductingDepartment().getDeptName() : null);
    assertEquals(accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim()
        : null, charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName()
        : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), null);
    assertEquals(accountingModel.getIssueStoreCenter(), null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(), Integer.valueOf(0));
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(), null);
    assertEquals(accountingModel.getTaxAmount(), null);
    assertEquals(accountingModel.getCostAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), "Default Insurance Company");

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertEquals(
        accountingModel.getGrossAmount(),
        chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 ? chargeClaim
            .getInsuranceClaimAmt() : chargeClaim.getInsuranceClaimAmt().negate());
    assertEquals(
        accountingModel.getNetAmount(),
        chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 ? chargeClaim
            .getInsuranceClaimAmt() : chargeClaim.getInsuranceClaimAmt().negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 ? chargeClaim
            .getSponsorId().getTpaName() : getChargeAccountHeadName(charge));
    assertEquals(
        accountingModel.getCreditAccount(),
        chargeClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0 ? getChargeAccountHeadName(charge)
            : chargeClaim.getSponsorId().getTpaName());

    assertEquals(accountingModel.getCustom1(), chargeClaim.getInsuranceClaimAmt().toString());
    assertEquals(accountingModel.getCustom2(), chargeClaim.getSponsorId().getTpaName());
    assertEquals(accountingModel.getCustom3(),
        String.valueOf(chargeClaim.getSponsorId().getSponsorType()));
    assertEquals(accountingModel.getCustom4(), getChargeAccountHeadName(charge));
  }

  private void assertSponsorTaxAccountingEntry(HmsAccountingInfoModel accountingModel,
      BillModel billModel, BillChargeClaimModel chargeClaim, BillChargeClaimTaxModel bcclTax) {

    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getMrNo());
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getOldmrno());
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        billModel.getVisitId().getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(accountingModel.getAdmittingDoctor() != null ? accountingModel
        .getAdmittingDoctor().trim() : null, billModel.getVisitId().getDoctor() != null ? billModel
        .getVisitId().getDoctor().getDoctorName() : null);
    assertEquals(accountingModel.getAdmittingDepartment() != null ? accountingModel
        .getAdmittingDepartment().trim() : null,
        billModel.getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName()
            .getDeptName() : null);
    assertEquals(accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor()
        .trim() : null, getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim()
        : null, billModel.getVisitId().getCenterId().getCenterName());
    assertEquals(accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim()
        : null, billModel.getBillNo());
    assertEquals(accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim()
        : null, accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertEquals(accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim()
        : null, null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null ? accountingModel
        .getAuditControlNumber().trim() : null, billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        billModel.getRemarks());

    assertEquals(accountingModel.getCurrencyConversionRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUnitRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getRoundOffAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getInvoiceNo(), null);
    assertEquals(accountingModel.getInvoiceDate(), null);
    assertEquals(accountingModel.getPoNumber(), null);
    assertEquals(accountingModel.getPoDate(), null);
    assertEquals(accountingModel.getSupplierName(), null);
    assertEquals(accountingModel.getCustSupplierCode(), null);
    assertEquals(accountingModel.getGrnDate(), null);
    assertEquals(accountingModel.getPurchaseVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPurchaseVatPercent(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatPercent(), BigDecimal.ZERO);

    BillChargeModel charge = billModel.getBillCharges().iterator().next();

    assertEquals(accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim()
        : null, charge.getActDescriptionId());
    assertEquals(accountingModel.getItemName() != null ? accountingModel.getItemName().trim()
        : null, charge.getActDescription());
    assertEquals(accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim()
        : null, charge.getChargeGroup().getChargegroupId());
    assertEquals(accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim()
        : null, charge.getChargeHead().getChargeheadId());
    assertEquals(accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup()
        .trim() : null, charge.getServiceSubGroupId() != null ? charge.getServiceSubGroupId()
        .getServiceGroupId().getServiceGroupName() : null);
    assertEquals(accountingModel.getServiceSubGroup() != null ? accountingModel
        .getServiceSubGroup().trim() : null, charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceSubGroupName() : null);
    assertEquals(accountingModel.getQuantity(), charge.getActQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPrescribingDoctor() != null ? accountingModel
        .getPrescribingDoctor().trim() : null, charge.getPrescribingDocotor() != null ? charge
        .getPrescribingDocotor().getDoctorName() : null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null ? accountingModel
        .getPrescribingDoctorDeptName().trim() : null, charge.getPrescribingDocotor() != null
        && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
        .getDeptId().getDeptName() : null);
    assertEquals(accountingModel.getConductiongDoctor() != null ? accountingModel
        .getConductiongDoctor().trim() : null, charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getConductingDepartment() != null ? accountingModel
        .getConductingDepartment().trim() : null, charge.getConductingDepartment() != null ? charge
        .getConductingDepartment().getDeptName() : null);
    assertEquals(accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim()
        : null, charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName()
        : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), null);
    assertEquals(accountingModel.getIssueStoreCenter(), null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(), Integer.valueOf(0));
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(), null);
    assertEquals(accountingModel.getTaxAmount(), null);
    assertEquals(accountingModel.getCostAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), "Default Insurance Company");

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertEquals(
        accountingModel.getGrossAmount(),
        bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? bcclTax
            .getSponsorTaxAmount() : bcclTax.getSponsorTaxAmount().negate());
    assertEquals(
        accountingModel.getNetAmount(),
        bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? bcclTax
            .getSponsorTaxAmount() : bcclTax.getSponsorTaxAmount().negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(
        accountingModel.getDebitAccount(),
        bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? (bcclTax.getSponsorId() != null ? bcclTax
            .getSponsorId().getTpaName() : null)
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
    assertEquals(
        accountingModel.getCreditAccount(),
        bcclTax.getSponsorTaxAmount().compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
            : (bcclTax.getSponsorId() != null ? bcclTax.getSponsorId().getTpaName() : null));

    assertEquals(accountingModel.getCustom1(), bcclTax.getSponsorTaxAmount().toString());
    assertEquals(accountingModel.getCustom2(), bcclTax.getSponsorId() != null ? bcclTax
        .getSponsorId().getTpaName() : null);
    assertEquals(accountingModel.getCustom3(),
        String.valueOf(bcclTax.getSponsorId() != null ? bcclTax.getSponsorId().getSponsorType()
            : null));
    assertEquals(accountingModel.getCustom4(), bcclTax.getTaxSubGroupId().getItemSubgroupName());
  }

  private void assertSponsorBillPatientAmountAccountingEntry(
      HmsAccountingInfoModel accountingModel, BillModel billModel) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getMrNo());
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getOldmrno());
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        billModel.getVisitId().getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(accountingModel.getAdmittingDoctor() != null ? accountingModel
        .getAdmittingDoctor().trim() : null, billModel.getVisitId().getDoctor() != null ? billModel
        .getVisitId().getDoctor().getDoctorName() : null);
    assertEquals(accountingModel.getAdmittingDepartment() != null ? accountingModel
        .getAdmittingDepartment().trim() : null,
        billModel.getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName()
            .getDeptName() : null);
    assertEquals(accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor()
        .trim() : null, getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim()
        : null, billModel.getVisitId().getCenterId().getCenterName());
    assertEquals(accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim()
        : null, billModel.getBillNo());
    assertEquals(accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim()
        : null, accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertEquals(accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim()
        : null, null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null ? accountingModel
        .getAuditControlNumber().trim() : null, billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        billModel.getRemarks());

    assertEquals(accountingModel.getCurrencyConversionRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUnitRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getRoundOffAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getInvoiceNo(), null);
    assertEquals(accountingModel.getInvoiceDate(), null);
    assertEquals(accountingModel.getPoNumber(), null);
    assertEquals(accountingModel.getPoDate(), null);
    assertEquals(accountingModel.getSupplierName(), null);
    assertEquals(accountingModel.getCustSupplierCode(), null);
    assertEquals(accountingModel.getGrnDate(), null);
    assertEquals(accountingModel.getPurchaseVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPurchaseVatPercent(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatPercent(), BigDecimal.ZERO);

    BillChargeModel charge = billModel.getBillCharges().iterator().next();

    assertEquals(accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim()
        : null, charge.getActDescriptionId());
    assertEquals(accountingModel.getItemName() != null ? accountingModel.getItemName().trim()
        : null, charge.getActDescription());
    assertEquals(accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim()
        : null, charge.getChargeGroup().getChargegroupId());
    assertEquals(accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim()
        : null, charge.getChargeHead().getChargeheadId());
    assertEquals(accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup()
        .trim() : null, charge.getServiceSubGroupId() != null ? charge.getServiceSubGroupId()
        .getServiceGroupId().getServiceGroupName() : null);
    assertEquals(accountingModel.getServiceSubGroup() != null ? accountingModel
        .getServiceSubGroup().trim() : null, charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceSubGroupName() : null);
    assertEquals(accountingModel.getQuantity(), charge.getActQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), charge.getDiscount());
    assertEquals(accountingModel.getPrescribingDoctor() != null ? accountingModel
        .getPrescribingDoctor().trim() : null, charge.getPrescribingDocotor() != null ? charge
        .getPrescribingDocotor().getDoctorName() : null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null ? accountingModel
        .getPrescribingDoctorDeptName().trim() : null, charge.getPrescribingDocotor() != null
        && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
        .getDeptId().getDeptName() : null);
    assertEquals(accountingModel.getConductiongDoctor() != null ? accountingModel
        .getConductiongDoctor().trim() : null, charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getConductingDepartment() != null ? accountingModel
        .getConductingDepartment().trim() : null, charge.getConductingDepartment() != null ? charge
        .getConductingDepartment().getDeptName() : null);
    assertEquals(accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim()
        : null, charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName()
        : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), null);
    assertEquals(accountingModel.getIssueStoreCenter(), null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(), Integer.valueOf(0));
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(), null);
    assertEquals(accountingModel.getTaxAmount(), null);
    assertEquals(accountingModel.getCostAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), null);

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());

    BigDecimal grossPatAmount = charge.getAmount().add(charge.getDiscount())
        .subtract(charge.getInsuranceClaimAmount());
    BigDecimal netPatAmount = charge.getAmount().subtract(charge.getInsuranceClaimAmount());

    assertEquals(accountingModel.getGrossAmount(),
        grossPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossPatAmount : grossPatAmount.negate());
    assertEquals(accountingModel.getNetAmount(),
        netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? netPatAmount : netPatAmount.negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(
        accountingModel.getDebitAccount(),
        netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : getChargeAccountHeadName(charge));
    assertEquals(accountingModel.getCreditAccount(),
        netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? getChargeAccountHeadName(charge)
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    assertEquals(accountingModel.getCustom1(), netPatAmount.toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(), getChargeAccountHeadName(charge));
  }

  private void assertSponsorBillPatientTaxAmountAccountingEntry(
      HmsAccountingInfoModel accountingModel, BillModel billModel, BillChargeTaxModel bcTax,
      Map<String, BigDecimal> chargeTaxIdClaimTaxMap) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getMrNo());
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        billModel.getVisitId().getPatientDetails().getOldmrno());
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        billModel.getVisitId().getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(accountingModel.getAdmittingDoctor() != null ? accountingModel
        .getAdmittingDoctor().trim() : null, billModel.getVisitId().getDoctor() != null ? billModel
        .getVisitId().getDoctor().getDoctorName() : null);
    assertEquals(accountingModel.getAdmittingDepartment() != null ? accountingModel
        .getAdmittingDepartment().trim() : null,
        billModel.getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName()
            .getDeptName() : null);
    assertEquals(accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor()
        .trim() : null, getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim()
        : null, billModel.getVisitId().getCenterId().getCenterName());
    assertEquals(accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim()
        : null, billModel.getBillNo());
    assertEquals(accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim()
        : null, accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertEquals(accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim()
        : null, null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null ? accountingModel
        .getAuditControlNumber().trim() : null, billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        billModel.getRemarks());

    assertEquals(accountingModel.getCurrencyConversionRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUnitRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getRoundOffAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getInvoiceNo(), null);
    assertEquals(accountingModel.getInvoiceDate(), null);
    assertEquals(accountingModel.getPoNumber(), null);
    assertEquals(accountingModel.getPoDate(), null);
    assertEquals(accountingModel.getSupplierName(), null);
    assertEquals(accountingModel.getCustSupplierCode(), null);
    assertEquals(accountingModel.getGrnDate(), null);
    assertEquals(accountingModel.getPurchaseVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPurchaseVatPercent(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getSalesVatPercent(), BigDecimal.ZERO);

    BillChargeModel charge = billModel.getBillCharges().iterator().next();

    assertEquals(accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim()
        : null, charge.getActDescriptionId());
    assertEquals(accountingModel.getItemName() != null ? accountingModel.getItemName().trim()
        : null, charge.getActDescription());
    assertEquals(accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim()
        : null, charge.getChargeGroup().getChargegroupId());
    assertEquals(accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim()
        : null, charge.getChargeHead().getChargeheadId());
    assertEquals(accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup()
        .trim() : null, charge.getServiceSubGroupId() != null ? charge.getServiceSubGroupId()
        .getServiceGroupId().getServiceGroupName() : null);
    assertEquals(accountingModel.getServiceSubGroup() != null ? accountingModel
        .getServiceSubGroup().trim() : null, charge.getServiceSubGroupId() != null ? charge
        .getServiceSubGroupId().getServiceSubGroupName() : null);
    assertEquals(accountingModel.getQuantity(), charge.getActQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPrescribingDoctor() != null ? accountingModel
        .getPrescribingDoctor().trim() : null, charge.getPrescribingDocotor() != null ? charge
        .getPrescribingDocotor().getDoctorName() : null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null ? accountingModel
        .getPrescribingDoctorDeptName().trim() : null, charge.getPrescribingDocotor() != null
        && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
        .getDeptId().getDeptName() : null);
    assertEquals(accountingModel.getConductiongDoctor() != null ? accountingModel
        .getConductiongDoctor().trim() : null, charge.getPayeeDoctorId() != null ? charge
        .getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getConductingDepartment() != null ? accountingModel
        .getConductingDepartment().trim() : null, charge.getConductingDepartment() != null ? charge
        .getConductingDepartment().getDeptName() : null);
    assertEquals(accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim()
        : null, charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName()
        : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), null);
    assertEquals(accountingModel.getIssueStoreCenter(), null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(), Integer.valueOf(0));
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(), null);
    assertEquals(accountingModel.getTaxAmount(), null);
    assertEquals(accountingModel.getCostAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), null);

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());

    BigDecimal patTaxAmount = bcTax
        .getTaxAmount()
        .subtract(
            chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId()) != null
                && !"".equals(chargeTaxIdClaimTaxMap.get("CTI" + bcTax.getChargeTaxId())) ? (BigDecimal) chargeTaxIdClaimTaxMap
                .get("CTI" + bcTax.getChargeTaxId()) : BigDecimal.ZERO);

    assertEquals(accountingModel.getGrossAmount(),
        patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount : patTaxAmount.negate());
    assertEquals(accountingModel.getNetAmount(),
        patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount : patTaxAmount.negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(
        accountingModel.getDebitAccount(),
        patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));
    assertEquals(
        accountingModel.getCreditAccount(),
        patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    assertEquals(accountingModel.getCustom1(), patTaxAmount.toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(), bcTax.getTaxSubGroupId().getItemSubgroupName());
  }

  private String getReferraldoctor(BillModel billModel) {
    if (billModel.getVisitId() != null && billModel.getVisitId().getReferralDoctorDoctors() != null
        && billModel.getVisitId().getReferralDoctorDoctors().getDoctorName() != null) {
      return billModel.getVisitId().getReferralDoctorDoctors().getDoctorName();
    } else if (billModel.getVisitId() != null
        && billModel.getVisitId().getReferralDoctorReferral() != null
        && billModel.getVisitId().getReferralDoctorReferral().getReferalName() != null) {
      return billModel.getVisitId().getReferralDoctorReferral().getReferalName();
    }
    return null;
  }

  public String getChargeAccountHeadName(BillChargeModel charge) {
    if (charge.getServiceSubGroupId() != null
        && charge.getServiceSubGroupId().getAccountHeadId() != null
        && charge.getServiceSubGroupId().getAccountHeadId().getAccountHeadName() != null) {
      return charge.getServiceSubGroupId().getAccountHeadId().getAccountHeadName();
    } else if (charge.getChargeHead().getAccountHeadId() != null
        && charge.getChargeHead().getAccountHeadId().getAccountHeadName() != null) {
      return charge.getChargeHead().getAccountHeadId().getAccountHeadName();
    }
    return null;
  }

  private Map<String, BigDecimal> getChargeTaxIdAndClaimTaxMapping(
      Set<BillChargeClaimTaxModel> billChargeClaimTaxes) {
    Map<String, BigDecimal> chargeTaxIdClaimTaxMap = new HashMap<String, BigDecimal>();
    for (BillChargeClaimTaxModel bcclTax : billChargeClaimTaxes) {
      if (chargeTaxIdClaimTaxMap.containsKey("CTI" + bcclTax.getChargeTaxId())) {
        BigDecimal claimTax = (BigDecimal) chargeTaxIdClaimTaxMap.get("CTI"
            + bcclTax.getChargeTaxId());
        claimTax = claimTax.add(bcclTax.getSponsorTaxAmount());
        chargeTaxIdClaimTaxMap.put("CTI" + bcclTax.getChargeTaxId(), claimTax);
      } else {
        chargeTaxIdClaimTaxMap.put("CTI" + bcclTax.getChargeTaxId(), bcclTax.getSponsorTaxAmount());
      }
    }
    return chargeTaxIdClaimTaxMap;
  }

}
