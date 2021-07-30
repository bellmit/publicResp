package testng.com.insta.hms.core.fa;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
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
import com.insta.hms.core.fa.InventoryAccountingDataInsertService;
import com.insta.hms.core.fa.InventoryReturnsAccountingDataInsertService;
import com.insta.hms.mdm.item.StoreItemDetailsModel;
import com.insta.hms.model.HmsAccountingInfoModel;
import com.insta.hms.model.StockIssueDetailsModel;

import org.apache.commons.lang3.StringUtils;
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
public class InventoryAccountingDataInsertServiceTest extends
    AbstractTransactionalTestNGSpringContextTests {
  @InjectMocks
  private AccountingDataInsertService accountingDataInsertService;

  @Autowired
  private InventoryAccountingDataInsertService inventoryAccountingDataInsertService;

  @Autowired
  private InventoryReturnsAccountingDataInsertService invReturnsAccountingDataInsertService;

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
    ReflectionTestUtils.setField(accountingDataInsertService, "invAccountingDataInsertService",
        inventoryAccountingDataInsertService);
    ReflectionTestUtils.setField(accountingDataInsertService,
        "invReturnsAccountingDataInsertService", invReturnsAccountingDataInsertService);
    ReflectionTestUtils.setField(inventoryAccountingDataInsertService, "accountingDataInsertRepo",
        accountingDataInsertRepo);
    ReflectionTestUtils.setField(inventoryAccountingDataInsertService, "genPrefService",
        genPrefService);
    ReflectionTestUtils.setField(accountingDataInsertService, "accountingDataInsertRepo",
        accountingDataInsertRepo);
    ReflectionTestUtils.setField(accountingDataInsertService, "genPrefService", genPrefService);

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

    testRepo.insert("bill_account_heads");
    testRepo.insert("department");
    testRepo.insert("service_groups");
    testRepo.insert("service_sub_groups");

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

    testRepo.insert("stores");
    testRepo.insert("store_category_master");
    testRepo.insert("account_group_master");

    testRepo.insert("consumption_uom_master");
    testRepo.insert("stock_issue_main");
    testRepo.insert("manf_master");
    testRepo.insert("store_item_details");
    testRepo.insert("stock_issue_details");
    testRepo.insert("bill_activity_charge");

    testRepo.insert("store_issue_returns_main");
    testRepo.insert("store_issue_returns_details");

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
    String billNo = "BC18000118";
    Integer jobTransaction = 3333;
    Date createdAt = new Date();
    BillModel billModel = (BillModel) accountingDataInsertRepo.get(BillModel.class, billNo);
    BillChargeModel charge = (BillChargeModel) accountingDataInsertRepo.get(BillChargeModel.class,
        "CH259818");

    billModel.setIsTpa(Boolean.FALSE);
    StockIssueDetailsModel stockIssueDetailsModel = (StockIssueDetailsModel) accountingDataInsertRepo
        .load(StockIssueDetailsModel.class, 79357);
    StoreItemDetailsModel storeItemDetails = stockIssueDetailsModel.getStoreItemDetails();
    List<HmsAccountingInfoModel> resultList = accountingDataInsertService
        .getAccountingDataToInsert(billNo, billModel, jobTransaction, createdAt);
    assertNotNull(resultList);
    assertEquals(resultList.size(), 6);

    // Assert patient amount entry
    HmsAccountingInfoModel accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))
          && accModel.getChargeReferenceId().equals("CH259818")
          && accModel.getCreditAccount().equals(
              getInventoryChargeAccountHeadName(charge, storeItemDetails))) {
        accountingModel = accModel;
        break;
      }
    }
    assertPatientAmountEntryNonInsuranceBill(accountingModel, billModel, charge,
        stockIssueDetailsModel, storeItemDetails);

    // Assert patient tax amount entry
    BillChargeTaxModel billChargeTaxModel = charge.getBillChargeTaxes().iterator().next();
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getChargeReferenceId().equals("CH259818")
          && accModel.getPrimaryId().equals("506")) {
        accountingModel = accModel;
      }
    }
    assertPatientTaxEntryNonInsuranceBill(accountingModel, billModel, charge, billChargeTaxModel,
        stockIssueDetailsModel, storeItemDetails);
  }

  @Test
  public void getAccountingDataToInsertInsuranceBillSuccessTest() {
    String billNo = "BC18000118";
    Integer jobTransaction = 3333;
    Date createdAt = new Date();
    BillModel billModel = (BillModel) accountingDataInsertRepo.get(BillModel.class, billNo);
    BillChargeModel charge = (BillChargeModel) accountingDataInsertRepo.get(BillChargeModel.class,
        "CH259818");
    StockIssueDetailsModel stockIssueDetailsModel = (StockIssueDetailsModel) accountingDataInsertRepo
        .load(StockIssueDetailsModel.class, 79357);
    StoreItemDetailsModel storeItemDetails = stockIssueDetailsModel.getStoreItemDetails();
    List<HmsAccountingInfoModel> resultList = accountingDataInsertService
        .getAccountingDataToInsert(billNo, billModel, jobTransaction, createdAt);
    assertNotNull(resultList);
    assertEquals(resultList.size(), 8);

    // Assert sponsor amount accounting entry
    BillChargeClaimModel chargeClaim = charge.getBillChargeClaims().iterator().next();
    HmsAccountingInfoModel accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(chargeClaim.getSponsorId().getTpaName())
          && accModel.getPrimaryId().equals("CH259818")
          && accModel.getSecondaryId().equals("CLD000579")) {
        accountingModel = accModel;
        break;
      }
    }
    assertSponsorAccountingEntry(accountingModel, billModel, charge, chargeClaim,
        stockIssueDetailsModel, storeItemDetails);

    // Assert sponsor tax amount accounting entry
    chargeClaim = charge.getBillChargeClaims().iterator().next();
    Set<BillChargeClaimTaxModel> billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(chargeClaim.getSponsorId().getTpaName())
          && accModel.getPrimaryId().equals("198") && accModel.getSecondaryId().equals("21")) {
        accountingModel = accModel;
        break;
      }
    }
    assertSponsorTaxAccountingEntry(accountingModel, billModel, charge, billChargeClaimTaxes
        .iterator().next(), stockIssueDetailsModel, storeItemDetails);

    // Assert sponsor bill patient Amount accounting entry
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))
          && accModel.getCreditAccount().equals(
              getInventoryChargeAccountHeadName(charge, storeItemDetails))) {
        accountingModel = accModel;
        break;
      }
    }
    assertSponsorBillPatientAmountAccountingEntry(accountingModel, billModel, charge,
        stockIssueDetailsModel, storeItemDetails);

    // Assert sponsor bill patient tax Amount accounting entry
    chargeClaim = charge.getBillChargeClaims().iterator().next();
    billChargeClaimTaxes = chargeClaim.getBillChargeClaimTaxes();
    Map<String, BigDecimal> chargeTaxIdClaimTaxMap = getChargeTaxIdAndClaimTaxMapping(billChargeClaimTaxes);

    accountingModel = null;
    BillChargeTaxModel bcTaxModel = charge.getBillChargeTaxes().iterator().next();
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getChargeReferenceId().equals("CH259818")
          && accModel.getPrimaryId().equals("506")) {
        accountingModel = accModel;
      }
    }
    assertSponsorBillPatientTaxAmountAccountingEntry(accountingModel, billModel, charge,
        bcTaxModel, stockIssueDetailsModel, storeItemDetails, chargeTaxIdClaimTaxMap);
  }

  private void assertPatientAmountEntryNonInsuranceBill(HmsAccountingInfoModel accountingModel,
      BillModel billModel, BillChargeModel charge, StockIssueDetailsModel stockIssueDetailsModel,
      StoreItemDetailsModel storeItemDetails) {
    assertEquals(StringUtils.trimToNull(accountingModel.getMrNo()), billModel.getVisitId()
        .getPatientDetails().getMrNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getOldMrNo()), billModel.getVisitId()
        .getPatientDetails().getOldmrno());
    assertEquals(StringUtils.trimToNull(accountingModel.getVisitId()), billModel.getVisitId()
        .getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDoctor()), billModel
        .getVisitId().getDoctor() != null ? billModel.getVisitId().getDoctor().getDoctorName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDepartment()), billModel
        .getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName().getDeptName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getReferralDoctor()),
        getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(StringUtils.trimToNull(accountingModel.getCenterName()), billModel.getVisitId()
        .getCenterId().getCenterName());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherNo()), billModel.getBillNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherType()),
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertNull(StringUtils.trimToNull(accountingModel.getVoucherRef()));
    assertEquals(StringUtils.trimToNull(accountingModel.getBillNo()), billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(StringUtils.trimToNull(accountingModel.getAuditControlNumber()),
        billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertTrue(accountingModel.getPointsRedeemedRate().compareTo(BigDecimal.ZERO) == 0);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(StringUtils.trimToNull(accountingModel.getRemarks()), billModel.getRemarks());

    assertTrue(accountingModel.getCurrencyConversionRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getUnitRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getRoundOffAmount().compareTo(BigDecimal.ZERO) == 0);
    assertNull(accountingModel.getInvoiceNo());
    assertNull(accountingModel.getInvoiceDate());
    assertNull(accountingModel.getPoNumber());
    assertNull(accountingModel.getPoDate());
    assertNull(accountingModel.getSupplierName());
    assertNull(accountingModel.getCustSupplierCode());
    assertNull(accountingModel.getGrnDate());
    assertTrue(accountingModel.getPurchaseVatAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPurchaseVatPercent().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getSalesVatAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertEquals(accountingModel.getSalesVatPercent(), stockIssueDetailsModel.getVat());

    assertEquals(StringUtils.trimToNull(accountingModel.getItemCode()),
        charge.getActDescriptionId());
    assertEquals(StringUtils.trimToNull(accountingModel.getItemName()), charge.getActDescription());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeGroup()), charge.getChargeGroup()
        .getChargegroupId());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeHead()), charge.getChargeHead()
        .getChargeheadId());
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceGroup()),
        getServiceGroupName(storeItemDetails));
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceSubGroup()),
        getServiceSubGroupName(storeItemDetails));
    assertTrue(accountingModel.getQuantity().compareTo(stockIssueDetailsModel.getQty()) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getUnit()), null);
    assertTrue(accountingModel.getDiscountAmount().compareTo(charge.getDiscount()) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctor()),
        charge.getPrescribingDocotor() != null ? charge.getPrescribingDocotor().getDoctorName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctorDeptName()),
        charge.getPrescribingDocotor() != null
            && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
            .getDeptId().getDeptName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductiongDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductingDepartment()),
        charge.getConductingDepartment() != null ? charge.getConductingDepartment().getDeptName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPayeeDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), getIssueStore(stockIssueDetailsModel));
    assertEquals(accountingModel.getIssueStoreCenter(),
        getIssueStoreCenterName(stockIssueDetailsModel));
    assertNull(accountingModel.getCounterNo());
    assertEquals(accountingModel.getItemCategoryId(), getItemCategoryId(storeItemDetails));
    assertNull(accountingModel.getReceiptStore());
    assertNull(accountingModel.getReceiptStoreCenter());
    assertNull(accountingModel.getCurrency());
    assertNull(accountingModel.getOuthouseName());
    assertNull(accountingModel.getIncoimngHospital());
    assertEquals(accountingModel.getCustItemCode(),
        storeItemDetails != null ? storeItemDetails.getCustItemCode() : null);
    assertEquals(accountingModel.getTaxAmount(), BigDecimal.valueOf(stockIssueDetailsModel
        .getFinalTax() != null ? stockIssueDetailsModel.getFinalTax() : 0.00));
    assertTrue(accountingModel.getCostAmount().compareTo(
        stockIssueDetailsModel.getCostValue() == null ? BigDecimal.ZERO : stockIssueDetailsModel
            .getCostValue()) == 0);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertNull(accountingModel.getInsuranceCo());

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertTrue(accountingModel.getGrossAmount().compareTo(
        (charge.getAmount().add(charge.getDiscount())).compareTo(BigDecimal.ZERO) >= 0 ? (charge
            .getAmount().add(charge.getDiscount()))
            : (charge.getAmount().add(charge.getDiscount())).negate()) == 0);
    assertTrue(accountingModel.getNetAmount().compareTo(
        charge.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? charge.getAmount() : charge
            .getAmount().negate()) == 0);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    assertEquals(accountingModel.getCreditAccount(),
        getInventoryChargeAccountHeadName(charge, storeItemDetails));

    assertEquals(accountingModel.getCustom1(), charge.getAmount().toString());
    assertNull(accountingModel.getCustom2());
    assertNull(accountingModel.getCustom3());
    assertEquals(accountingModel.getCustom4(),
        getInventoryChargeAccountHeadName(charge, storeItemDetails));
  }

  private void assertPatientTaxEntryNonInsuranceBill(HmsAccountingInfoModel accountingModel,
      BillModel billModel, BillChargeModel charge, BillChargeTaxModel bcTax,
      StockIssueDetailsModel stockIssueDetailsModel, StoreItemDetailsModel storeItemDetails) {
    assertEquals(StringUtils.trimToNull(accountingModel.getMrNo()), billModel.getVisitId()
        .getPatientDetails().getMrNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getOldMrNo()), billModel.getVisitId()
        .getPatientDetails().getOldmrno());
    assertEquals(StringUtils.trimToNull(accountingModel.getVisitId()), billModel.getVisitId()
        .getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDoctor()), billModel
        .getVisitId().getDoctor() != null ? billModel.getVisitId().getDoctor().getDoctorName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDepartment()), billModel
        .getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName().getDeptName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getReferralDoctor()),
        getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(StringUtils.trimToNull(accountingModel.getCenterName()), billModel.getVisitId()
        .getCenterId().getCenterName());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherNo()), billModel.getBillNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherType()),
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertNull(StringUtils.trimToNull(accountingModel.getVoucherRef()));
    assertEquals(StringUtils.trimToNull(accountingModel.getBillNo()), billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(StringUtils.trimToNull(accountingModel.getAuditControlNumber()),
        billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertTrue(accountingModel.getPointsRedeemedRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPointsRedeemedAmount()
        .compareTo(billModel.getPointsRedeemedAmt()) == 0);
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(StringUtils.trimToNull(accountingModel.getRemarks()), billModel.getRemarks());

    assertTrue(accountingModel.getCurrencyConversionRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getUnitRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getRoundOffAmount().compareTo(BigDecimal.ZERO) == 0);
    assertNull(accountingModel.getInvoiceNo());
    assertNull(accountingModel.getInvoiceDate());
    assertNull(accountingModel.getPoNumber());
    assertNull(accountingModel.getPoDate());
    assertNull(accountingModel.getSupplierName());
    assertNull(accountingModel.getCustSupplierCode());
    assertNull(accountingModel.getGrnDate());
    assertTrue(accountingModel.getPurchaseVatAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPurchaseVatPercent().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getSalesVatAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertTrue(accountingModel.getSalesVatPercent().compareTo(stockIssueDetailsModel.getVat()) == 0);

    assertEquals(StringUtils.trimToNull(accountingModel.getItemCode()),
        charge.getActDescriptionId());
    assertEquals(StringUtils.trimToNull(accountingModel.getItemName()), charge.getActDescription());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeGroup()), charge.getChargeGroup()
        .getChargegroupId());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeHead()), charge.getChargeHead()
        .getChargeheadId());
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceGroup()),
        getServiceGroupName(storeItemDetails));
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceSubGroup()),
        getServiceSubGroupName(storeItemDetails));
    assertTrue(accountingModel.getQuantity().compareTo(stockIssueDetailsModel.getQty()) == 0);
    assertNull(StringUtils.trimToNull(accountingModel.getUnit()));
    assertTrue(accountingModel.getDiscountAmount().compareTo(BigDecimal.ZERO) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctor()),
        charge.getPrescribingDocotor() != null ? charge.getPrescribingDocotor().getDoctorName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctorDeptName()),
        charge.getPrescribingDocotor() != null
            && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
            .getDeptId().getDeptName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductiongDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductingDepartment()),
        charge.getConductingDepartment() != null ? charge.getConductingDepartment().getDeptName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPayeeDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), getIssueStore(stockIssueDetailsModel));
    assertEquals(accountingModel.getIssueStoreCenter(),
        getIssueStoreCenterName(stockIssueDetailsModel));
    assertNull(accountingModel.getCounterNo());
    assertEquals(accountingModel.getItemCategoryId(), getItemCategoryId(storeItemDetails));
    assertNull(accountingModel.getReceiptStore());
    assertNull(accountingModel.getReceiptStoreCenter());
    assertNull(accountingModel.getCurrency());
    assertNull(accountingModel.getOuthouseName());
    assertNull(accountingModel.getIncoimngHospital());
    assertEquals(accountingModel.getCustItemCode(),
        storeItemDetails != null ? storeItemDetails.getCustItemCode() : null);
    assertTrue(accountingModel.getTaxAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getCostAmount().compareTo(
        stockIssueDetailsModel.getCostValue() == null ? BigDecimal.ZERO : stockIssueDetailsModel
            .getCostValue()) == 0);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), null);

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertTrue(accountingModel.getGrossAmount().compareTo(bcTax.getTaxAmount()) == 0);
    assertTrue(accountingModel.getNetAmount().compareTo(bcTax.getTaxAmount()) == 0);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    assertEquals(accountingModel.getCreditAccount(),
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));

    assertEquals(accountingModel.getCustom1(), bcTax.getTaxAmount().toString());
    assertNull(accountingModel.getCustom2());
    assertNull(accountingModel.getCustom3());
    assertEquals(accountingModel.getCustom4(), bcTax.getTaxSubGroupId().getItemSubgroupName());
  }

  private void assertSponsorAccountingEntry(HmsAccountingInfoModel accountingModel,
      BillModel billModel, BillChargeModel charge, BillChargeClaimModel chargeClaim,
      StockIssueDetailsModel stockIssueDetailsModel, StoreItemDetailsModel storeItemDetails) {
    assertEquals(StringUtils.trimToNull(accountingModel.getMrNo()), billModel.getVisitId()
        .getPatientDetails().getMrNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getOldMrNo()), billModel.getVisitId()
        .getPatientDetails().getOldmrno());
    assertEquals(StringUtils.trimToNull(accountingModel.getVisitId()), billModel.getVisitId()
        .getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDoctor()), billModel
        .getVisitId().getDoctor() != null ? billModel.getVisitId().getDoctor().getDoctorName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDepartment()), billModel
        .getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName().getDeptName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getReferralDoctor()),
        getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(StringUtils.trimToNull(accountingModel.getCenterName()), billModel.getVisitId()
        .getCenterId().getCenterName());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherNo()), billModel.getBillNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherType()),
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertNull(StringUtils.trimToNull(accountingModel.getVoucherRef()));
    assertEquals(StringUtils.trimToNull(accountingModel.getBillNo()), billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(StringUtils.trimToNull(accountingModel.getAuditControlNumber()),
        billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertTrue(accountingModel.getPointsRedeemedRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPointsRedeemedAmount()
        .compareTo(billModel.getPointsRedeemedAmt()) == 0);
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(StringUtils.trimToNull(accountingModel.getRemarks()), billModel.getRemarks());

    assertTrue(accountingModel.getCurrencyConversionRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getUnitRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getRoundOffAmount().compareTo(BigDecimal.ZERO) == 0);
    assertNull(accountingModel.getInvoiceNo());
    assertNull(accountingModel.getInvoiceDate());
    assertNull(accountingModel.getPoNumber());
    assertNull(accountingModel.getPoDate());
    assertNull(accountingModel.getSupplierName());
    assertNull(accountingModel.getCustSupplierCode());
    assertNull(accountingModel.getGrnDate());
    assertTrue(accountingModel.getPurchaseVatAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPurchaseVatPercent().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getSalesVatAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertTrue(accountingModel.getSalesVatPercent().compareTo(stockIssueDetailsModel.getVat()) == 0);

    assertEquals(StringUtils.trimToNull(accountingModel.getItemCode()),
        charge.getActDescriptionId());
    assertEquals(StringUtils.trimToNull(accountingModel.getItemName()), charge.getActDescription());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeGroup()), charge.getChargeGroup()
        .getChargegroupId());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeHead()), charge.getChargeHead()
        .getChargeheadId());
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceGroup()),
        getServiceGroupName(storeItemDetails));
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceSubGroup()),
        getServiceSubGroupName(storeItemDetails));
    assertTrue(accountingModel.getQuantity().compareTo(stockIssueDetailsModel.getQty()) == 0);
    assertNull(StringUtils.trimToNull(accountingModel.getUnit()));
    assertTrue(accountingModel.getDiscountAmount().compareTo(charge.getDiscount()) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctor()),
        charge.getPrescribingDocotor() != null ? charge.getPrescribingDocotor().getDoctorName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctorDeptName()),
        charge.getPrescribingDocotor() != null
            && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
            .getDeptId().getDeptName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductiongDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductingDepartment()),
        charge.getConductingDepartment() != null ? charge.getConductingDepartment().getDeptName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPayeeDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), getIssueStore(stockIssueDetailsModel));
    assertEquals(accountingModel.getIssueStoreCenter(),
        getIssueStoreCenterName(stockIssueDetailsModel));
    assertNull(accountingModel.getCounterNo());
    assertEquals(accountingModel.getItemCategoryId(), getItemCategoryId(storeItemDetails));
    assertNull(accountingModel.getReceiptStore());
    assertNull(accountingModel.getReceiptStoreCenter());
    assertNull(accountingModel.getCurrency());
    assertNull(accountingModel.getOuthouseName());
    assertNull(accountingModel.getIncoimngHospital());
    assertEquals(accountingModel.getCustItemCode(),
        storeItemDetails != null ? storeItemDetails.getCustItemCode() : null);
    assertTrue(accountingModel.getTaxAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertTrue(accountingModel.getCostAmount().compareTo(
        stockIssueDetailsModel.getCostValue() == null ? BigDecimal.ZERO : stockIssueDetailsModel
            .getCostValue()) == 0);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), "Default Insurance Company");

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertTrue(accountingModel.getGrossAmount().compareTo(chargeClaim.getInsuranceClaimAmt()) == 0);

    assertTrue(accountingModel.getNetAmount().compareTo(chargeClaim.getInsuranceClaimAmt()) == 0);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(), chargeClaim.getSponsorId().getTpaName());
    assertEquals(accountingModel.getCreditAccount(),
        getInventoryChargeAccountHeadName(charge, storeItemDetails));

    assertEquals(accountingModel.getCustom1(), chargeClaim.getInsuranceClaimAmt().toString());
    assertEquals(accountingModel.getCustom2(), chargeClaim.getSponsorId().getTpaName());
    assertEquals(accountingModel.getCustom3(),
        String.valueOf(chargeClaim.getSponsorId().getSponsorType()));
    assertEquals(accountingModel.getCustom4(),
        getInventoryChargeAccountHeadName(charge, storeItemDetails));
  }

  private void assertSponsorTaxAccountingEntry(HmsAccountingInfoModel accountingModel,
      BillModel billModel, BillChargeModel charge, BillChargeClaimTaxModel bcclTax,
      StockIssueDetailsModel stockIssueDetailsModel, StoreItemDetailsModel storeItemDetails) {
    assertEquals(StringUtils.trimToNull(accountingModel.getMrNo()), billModel.getVisitId()
        .getPatientDetails().getMrNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getOldMrNo()), billModel.getVisitId()
        .getPatientDetails().getOldmrno());
    assertEquals(StringUtils.trimToNull(accountingModel.getVisitId()), billModel.getVisitId()
        .getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDoctor()), billModel
        .getVisitId().getDoctor() != null ? billModel.getVisitId().getDoctor().getDoctorName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDepartment()), billModel
        .getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName().getDeptName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getReferralDoctor()),
        getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(StringUtils.trimToNull(accountingModel.getCenterName()), billModel.getVisitId()
        .getCenterId().getCenterName());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherNo()), billModel.getBillNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherType()),
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertNull(StringUtils.trimToNull(accountingModel.getVoucherRef()));
    assertEquals(StringUtils.trimToNull(accountingModel.getBillNo()), billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(StringUtils.trimToNull(accountingModel.getAuditControlNumber()),
        billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertTrue(accountingModel.getPointsRedeemedRate().compareTo(BigDecimal.ZERO) == 0);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        billModel.getRemarks());

    assertTrue(accountingModel.getCurrencyConversionRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getUnitRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getRoundOffAmount().compareTo(BigDecimal.ZERO) == 0);
    assertNull(accountingModel.getInvoiceNo());
    assertNull(accountingModel.getInvoiceDate());
    assertNull(accountingModel.getPoNumber());
    assertNull(accountingModel.getPoDate());
    assertNull(accountingModel.getSupplierName());
    assertNull(accountingModel.getCustSupplierCode());
    assertNull(accountingModel.getGrnDate());
    assertTrue(accountingModel.getPurchaseVatAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPurchaseVatPercent().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getSalesVatAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertTrue(accountingModel.getSalesVatPercent().compareTo(stockIssueDetailsModel.getVat()) == 0);

    assertEquals(StringUtils.trimToNull(accountingModel.getItemCode()),
        charge.getActDescriptionId());
    assertEquals(StringUtils.trimToNull(accountingModel.getItemName()), charge.getActDescription());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeGroup()), charge.getChargeGroup()
        .getChargegroupId());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeHead()), charge.getChargeHead()
        .getChargeheadId());
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceGroup()),
        getServiceGroupName(storeItemDetails));
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceSubGroup()),
        getServiceSubGroupName(storeItemDetails));
    assertEquals(accountingModel.getQuantity(), stockIssueDetailsModel.getQty());
    assertEquals(StringUtils.trimToNull(accountingModel.getUnit()), null);
    assertTrue(accountingModel.getDiscountAmount().compareTo(BigDecimal.ZERO) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctor()),
        charge.getPrescribingDocotor() != null ? charge.getPrescribingDocotor().getDoctorName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctorDeptName()),
        charge.getPrescribingDocotor() != null
            && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
            .getDeptId().getDeptName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductiongDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductingDepartment()),
        charge.getConductingDepartment() != null ? charge.getConductingDepartment().getDeptName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPayeeDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), getIssueStore(stockIssueDetailsModel));
    assertEquals(accountingModel.getIssueStoreCenter(),
        getIssueStoreCenterName(stockIssueDetailsModel));
    assertNull(accountingModel.getCounterNo());
    assertEquals(accountingModel.getItemCategoryId(), getItemCategoryId(storeItemDetails));
    assertNull(accountingModel.getReceiptStore());
    assertNull(accountingModel.getReceiptStoreCenter());
    assertNull(accountingModel.getCurrency());
    assertNull(accountingModel.getOuthouseName());
    assertNull(accountingModel.getIncoimngHospital());
    assertEquals(accountingModel.getCustItemCode(),
        storeItemDetails != null ? storeItemDetails.getCustItemCode() : null);
    assertTrue(accountingModel.getTaxAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getCostAmount().compareTo(
        stockIssueDetailsModel.getCostValue() == null ? BigDecimal.ZERO : stockIssueDetailsModel
            .getCostValue()) == 0);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), "Default Insurance Company");

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    assertTrue(accountingModel.getGrossAmount().compareTo(bcclTax.getSponsorTaxAmount()) == 0);

    assertTrue(accountingModel.getNetAmount().compareTo(bcclTax.getSponsorTaxAmount()) == 0);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(), bcclTax.getSponsorId().getTpaName());
    assertEquals(accountingModel.getCreditAccount(),
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));

    assertEquals(accountingModel.getCustom1(), bcclTax.getSponsorTaxAmount().toString());
    assertEquals(accountingModel.getCustom2(), bcclTax.getSponsorId().getTpaName());
    assertEquals(accountingModel.getCustom3(),
        String.valueOf(bcclTax.getSponsorId().getSponsorType()));
    assertEquals(accountingModel.getCustom4(), bcclTax.getTaxSubGroupId().getItemSubgroupName());
  }

  private void assertSponsorBillPatientAmountAccountingEntry(
      HmsAccountingInfoModel accountingModel, BillModel billModel, BillChargeModel charge,
      StockIssueDetailsModel stockIssueDetailsModel, StoreItemDetailsModel storeItemDetails) {
    assertEquals(StringUtils.trimToNull(accountingModel.getMrNo()), billModel.getVisitId()
        .getPatientDetails().getMrNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getOldMrNo()), billModel.getVisitId()
        .getPatientDetails().getOldmrno());
    assertEquals(StringUtils.trimToNull(accountingModel.getVisitId()), billModel.getVisitId()
        .getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDoctor()), billModel
        .getVisitId().getDoctor() != null ? billModel.getVisitId().getDoctor().getDoctorName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDepartment()), billModel
        .getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName().getDeptName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getReferralDoctor()),
        getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(StringUtils.trimToNull(accountingModel.getCenterName()), billModel.getVisitId()
        .getCenterId().getCenterName());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherNo()), billModel.getBillNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherType()),
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertNull(StringUtils.trimToNull(accountingModel.getVoucherRef()));
    assertEquals(StringUtils.trimToNull(accountingModel.getBillNo()), billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(StringUtils.trimToNull(accountingModel.getAuditControlNumber()),
        billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertTrue(accountingModel.getPointsRedeemedRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPointsRedeemedAmount()
        .compareTo(billModel.getPointsRedeemedAmt()) == 0);
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(StringUtils.trimToNull(accountingModel.getRemarks()), billModel.getRemarks());

    assertTrue(accountingModel.getCurrencyConversionRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getUnitRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getRoundOffAmount().compareTo(BigDecimal.ZERO) == 0);
    assertNull(accountingModel.getInvoiceNo());
    assertNull(accountingModel.getInvoiceDate());
    assertNull(accountingModel.getPoNumber());
    assertNull(accountingModel.getPoDate());
    assertNull(accountingModel.getSupplierName());
    assertNull(accountingModel.getCustSupplierCode());
    assertNull(accountingModel.getGrnDate());
    assertTrue(accountingModel.getPurchaseVatAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPurchaseVatPercent().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getSalesVatAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertTrue(accountingModel.getSalesVatPercent().compareTo(stockIssueDetailsModel.getVat()) == 0);

    assertEquals(StringUtils.trimToNull(accountingModel.getItemCode()),
        charge.getActDescriptionId());
    assertEquals(StringUtils.trimToNull(accountingModel.getItemName()), charge.getActDescription());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeGroup()), charge.getChargeGroup()
        .getChargegroupId());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeHead()), charge.getChargeHead()
        .getChargeheadId());
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceGroup()),
        getServiceGroupName(storeItemDetails));
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceSubGroup()),
        getServiceSubGroupName(storeItemDetails));
    assertTrue(accountingModel.getQuantity().compareTo(stockIssueDetailsModel.getQty()) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getUnit()), null);
    assertTrue(accountingModel.getDiscountAmount().compareTo(charge.getDiscount()) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctor()),
        charge.getPrescribingDocotor() != null ? charge.getPrescribingDocotor().getDoctorName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctorDeptName()),
        charge.getPrescribingDocotor() != null
            && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
            .getDeptId().getDeptName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductiongDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductingDepartment()),
        charge.getConductingDepartment() != null ? charge.getConductingDepartment().getDeptName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPayeeDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), getIssueStore(stockIssueDetailsModel));
    assertEquals(accountingModel.getIssueStoreCenter(),
        getIssueStoreCenterName(stockIssueDetailsModel));
    assertNull(accountingModel.getCounterNo());
    assertEquals(accountingModel.getItemCategoryId(), getItemCategoryId(storeItemDetails));
    assertNull(accountingModel.getReceiptStore());
    assertNull(accountingModel.getReceiptStoreCenter());
    assertNull(accountingModel.getCurrency());
    assertNull(accountingModel.getOuthouseName());
    assertNull(accountingModel.getIncoimngHospital());
    assertEquals(accountingModel.getCustItemCode(),
        storeItemDetails != null ? storeItemDetails.getCustItemCode() : null);
    assertTrue(accountingModel.getTaxAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertTrue(accountingModel.getCostAmount().compareTo(
        stockIssueDetailsModel.getCostValue() == null ? BigDecimal.ZERO : stockIssueDetailsModel
            .getCostValue()) == 0);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertNull(accountingModel.getInsuranceCo());

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());
    BigDecimal grossPatAmount = charge.getAmount().add(charge.getDiscount())
        .subtract(charge.getInsuranceClaimAmount());
    BigDecimal netPatAmount = charge.getAmount().subtract(charge.getInsuranceClaimAmount());
    assertTrue(accountingModel.getGrossAmount().compareTo(grossPatAmount) == 0);
    assertTrue(accountingModel.getNetAmount().compareTo(netPatAmount) == 0);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    assertEquals(accountingModel.getCreditAccount(),
        getInventoryChargeAccountHeadName(charge, storeItemDetails));

    assertEquals(accountingModel.getCustom1(), netPatAmount.toString());
    assertNull(accountingModel.getCustom2());
    assertNull(accountingModel.getCustom3());
    assertEquals(accountingModel.getCustom4(),
        getInventoryChargeAccountHeadName(charge, storeItemDetails));
  }

  private void assertSponsorBillPatientTaxAmountAccountingEntry(
      HmsAccountingInfoModel accountingModel, BillModel billModel, BillChargeModel charge,
      BillChargeTaxModel bcTax, StockIssueDetailsModel stockIssueDetailsModel,
      StoreItemDetailsModel storeItemDetails, Map<String, BigDecimal> chargeTaxIdClaimTaxMap) {
    assertEquals(StringUtils.trimToNull(accountingModel.getMrNo()), billModel.getVisitId()
        .getPatientDetails().getMrNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getOldMrNo()), billModel.getVisitId()
        .getPatientDetails().getOldmrno());
    assertEquals(StringUtils.trimToNull(accountingModel.getVisitId()), billModel.getVisitId()
        .getPatientId());
    assertEquals(accountingModel.getVisitType(), billModel.getVisitType());
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDoctor()), billModel
        .getVisitId().getDoctor() != null ? billModel.getVisitId().getDoctor().getDoctorName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getAdmittingDepartment()), billModel
        .getVisitId().getDeptName() != null ? billModel.getVisitId().getDeptName().getDeptName()
        : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getReferralDoctor()),
        getReferraldoctor(billModel));
    assertEquals(accountingModel.getCenterId(), (Integer) billModel.getVisitId().getCenterId()
        .getCenterId());
    assertEquals(StringUtils.trimToNull(accountingModel.getCenterName()), billModel.getVisitId()
        .getCenterId().getCenterName());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherNo()), billModel.getBillNo());
    assertEquals(StringUtils.trimToNull(accountingModel.getVoucherType()),
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_HOSPBILLS"));
    assertNull(StringUtils.trimToNull(accountingModel.getVoucherRef()));
    assertEquals(StringUtils.trimToNull(accountingModel.getBillNo()), billModel.getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), billModel.getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(), billModel.getFinalizedDate());
    assertEquals(StringUtils.trimToNull(accountingModel.getAuditControlNumber()),
        billModel.getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(), (Integer) billModel.getAccountGroup());
    assertEquals(accountingModel.getPointsRedeemed(), (Integer) billModel.getPointsRedeemed());
    assertTrue(accountingModel.getPointsRedeemedRate().compareTo(BigDecimal.ZERO) == 0);
    assertEquals(accountingModel.getPointsRedeemedAmount(), billModel.getPointsRedeemedAmt());
    assertEquals(accountingModel.getIsTpa(), billModel.getIsTpa() ? AccountingConstants.IS_TPA_Y
        : AccountingConstants.IS_TPA_N);
    assertEquals(StringUtils.trimToNull(accountingModel.getRemarks()), billModel.getRemarks());

    assertTrue(accountingModel.getCurrencyConversionRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getUnitRate().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getRoundOffAmount().compareTo(BigDecimal.ZERO) == 0);
    assertNull(accountingModel.getInvoiceNo());
    assertNull(accountingModel.getInvoiceDate());
    assertNull(accountingModel.getPoNumber());
    assertNull(accountingModel.getPoDate());
    assertNull(accountingModel.getSupplierName());
    assertNull(accountingModel.getCustSupplierCode());
    assertNull(accountingModel.getGrnDate());
    assertTrue(accountingModel.getPurchaseVatAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getPurchaseVatPercent().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getSalesVatAmount().compareTo(
        BigDecimal.valueOf(stockIssueDetailsModel.getFinalTax() != null ? stockIssueDetailsModel
            .getFinalTax() : 0.00)) == 0);
    assertTrue(accountingModel.getSalesVatPercent().compareTo(stockIssueDetailsModel.getVat()) == 0);

    assertEquals(StringUtils.trimToNull(accountingModel.getItemCode()),
        charge.getActDescriptionId());
    assertEquals(StringUtils.trimToNull(accountingModel.getItemName()), charge.getActDescription());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeGroup()), charge.getChargeGroup()
        .getChargegroupId());
    assertEquals(StringUtils.trimToNull(accountingModel.getChargeHead()), charge.getChargeHead()
        .getChargeheadId());
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceGroup()),
        getServiceGroupName(storeItemDetails));
    assertEquals(StringUtils.trimToNull(accountingModel.getServiceSubGroup()),
        getServiceSubGroupName(storeItemDetails));
    assertTrue(accountingModel.getQuantity().compareTo(stockIssueDetailsModel.getQty()) == 0);
    assertNull(StringUtils.trimToNull(accountingModel.getUnit()));
    assertTrue(accountingModel.getDiscountAmount().compareTo(BigDecimal.ZERO) == 0);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctor()),
        charge.getPrescribingDocotor() != null ? charge.getPrescribingDocotor().getDoctorName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPrescribingDoctorDeptName()),
        charge.getPrescribingDocotor() != null
            && charge.getPrescribingDocotor().getDeptId() != null ? charge.getPrescribingDocotor()
            .getDeptId().getDeptName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductiongDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getConductingDepartment()),
        charge.getConductingDepartment() != null ? charge.getConductingDepartment().getDeptName()
            : null);
    assertEquals(StringUtils.trimToNull(accountingModel.getPayeeDoctor()),
        charge.getPayeeDoctorId() != null ? charge.getPayeeDoctorId().getDoctorName() : null);
    assertEquals(accountingModel.getModTime(), charge.getModTime());
    assertEquals(accountingModel.getIssueStore(), getIssueStore(stockIssueDetailsModel));
    assertEquals(accountingModel.getIssueStoreCenter(),
        getIssueStoreCenterName(stockIssueDetailsModel));
    assertNull(accountingModel.getCounterNo());
    assertEquals(accountingModel.getItemCategoryId(), getItemCategoryId(storeItemDetails));
    assertNull(accountingModel.getReceiptStore());
    assertNull(accountingModel.getReceiptStoreCenter());
    assertNull(accountingModel.getCurrency());
    assertNull(accountingModel.getOuthouseName());
    assertNull(accountingModel.getIncoimngHospital());
    assertEquals(accountingModel.getCustItemCode(),
        storeItemDetails != null ? storeItemDetails.getCustItemCode() : null);
    assertTrue(accountingModel.getTaxAmount().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(accountingModel.getCostAmount().compareTo(
        stockIssueDetailsModel.getCostValue() == null ? BigDecimal.ZERO : stockIssueDetailsModel
            .getCostValue()) == 0);
    assertEquals(accountingModel.getUpdateStatus(), Integer.valueOf(0));
    assertEquals(accountingModel.getInsuranceCo(), null);

    assertEquals(accountingModel.getVoucherDate(), charge.getPostedDate());

    String key = "CTI" + bcTax.getChargeTaxId();
    BigDecimal claimTax = chargeTaxIdClaimTaxMap.get(key) != null ? chargeTaxIdClaimTaxMap.get(key)
        : BigDecimal.ZERO;

    BigDecimal patTaxAmount = bcTax.getTaxAmount().subtract(claimTax);

    assertTrue(accountingModel.getGrossAmount().compareTo(patTaxAmount) == 0);
    assertTrue(accountingModel.getNetAmount().compareTo(patTaxAmount) == 0);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));
    assertEquals(accountingModel.getCreditAccount(),
        accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC"));

    assertEquals(accountingModel.getCustom1(), patTaxAmount.toString());
    assertNull(accountingModel.getCustom2());
    assertNull(accountingModel.getCustom3());
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

  private String getServiceSubGroupName(StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getServiceSubGroupId() != null) {
      return storeItemDetails.getServiceSubGroupId().getServiceSubGroupName();
    }
    return null;
  }

  private String getServiceGroupName(StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getServiceSubGroupId() != null
        && storeItemDetails.getServiceSubGroupId().getServiceGroupId() != null) {
      return storeItemDetails.getServiceSubGroupId().getServiceGroupId().getServiceGroupName();
    }
    return null;
  }

  private String getIssueStore(StockIssueDetailsModel stockIssueDetailsModel) {
    if (stockIssueDetailsModel != null && stockIssueDetailsModel.getStockIssueMain() != null
        && stockIssueDetailsModel.getStockIssueMain().getStoresModel() != null) {
      return stockIssueDetailsModel.getStockIssueMain().getStoresModel().getDeptName();
    }
    return null;

  }

  private String getIssueStoreCenterName(StockIssueDetailsModel stockIssueDetailsModel) {
    if (stockIssueDetailsModel != null && stockIssueDetailsModel.getStockIssueMain() != null
        && stockIssueDetailsModel.getStockIssueMain().getStoresModel() != null
        && stockIssueDetailsModel.getStockIssueMain().getStoresModel().getCenterId() != null) {
      return stockIssueDetailsModel.getStockIssueMain().getStoresModel().getCenterId()
          .getCenterName();
    }
    return null;
  }

  private Integer getItemCategoryId(StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getStoreCategoryMaster() != null) {
      return storeItemDetails.getStoreCategoryMaster().getCategoryId();
    }
    return null;
  }

  public String getInventoryChargeAccountHeadName(BillChargeModel charge,
      StoreItemDetailsModel storeItemDetails) {
    if (storeItemDetails != null && storeItemDetails.getServiceSubGroupId() != null
        && storeItemDetails.getServiceSubGroupId().getAccountHeadId() != null) {
      return storeItemDetails.getServiceSubGroupId().getAccountHeadId().getAccountHeadName();
    } else if (charge != null && charge.getChargeHead() != null
        && charge.getChargeHead().getAccountHeadId() != null) {
      return charge.getChargeHead().getAccountHeadId().getAccountHeadName();
    }
    return null;
  }

  private Map<String, BigDecimal> getChargeTaxIdAndClaimTaxMapping(
      Set<BillChargeClaimTaxModel> billChargeClaimTaxes) {
    Map<String, BigDecimal> chargeTaxIdClaimTaxMap = new HashMap<>();
    for (BillChargeClaimTaxModel bcclTax : billChargeClaimTaxes) {
      if (chargeTaxIdClaimTaxMap.containsKey("CTI" + bcclTax.getChargeTaxId())) {
        BigDecimal claimTax = chargeTaxIdClaimTaxMap.get("CTI" + bcclTax.getChargeTaxId());
        claimTax = claimTax.add(bcclTax.getSponsorTaxAmount());
        chargeTaxIdClaimTaxMap.put("CTI" + bcclTax.getChargeTaxId(), claimTax);
      } else {
        chargeTaxIdClaimTaxMap.put("CTI" + bcclTax.getChargeTaxId(), bcclTax.getSponsorTaxAmount());
      }
    }
    return chargeTaxIdClaimTaxMap;
  }

}
