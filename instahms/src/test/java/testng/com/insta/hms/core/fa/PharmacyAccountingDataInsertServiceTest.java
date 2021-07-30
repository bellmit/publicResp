package testng.com.insta.hms.core.fa;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.insta.hms.billing.accounting.AccountingConstants;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillModel;
import com.insta.hms.core.fa.AccountingDataInsertRepository;
import com.insta.hms.core.fa.PharmacyAccountingDataInsertRepository;
import com.insta.hms.core.fa.PharmacyAccountingDataInsertService;
import com.insta.hms.core.fa.PharmacyOnlyAccountingDataInsertService;
import com.insta.hms.core.inventory.sales.SalesClaimDetailsModel;
import com.insta.hms.core.inventory.sales.SalesClaimTaxDetailsModel;
import com.insta.hms.mdm.chargeheads.ChargeheadConstantsModel;
import com.insta.hms.model.HmsAccountingInfoModel;
import com.insta.hms.model.StoreSalesDetailsModel;
import com.insta.hms.model.StoreSalesMainModel;
import com.insta.hms.model.StoreSalesTaxDetailsModel;

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
public class PharmacyAccountingDataInsertServiceTest
    extends AbstractTransactionalTestNGSpringContextTests {

  @InjectMocks
  private PharmacyAccountingDataInsertService pharmacyAccountingDataInsertService;

  @InjectMocks
  private PharmacyOnlyAccountingDataInsertService pharmacyOnlyAccountingDataInsertService;

  @Autowired
  private PharmacyAccountingDataInsertRepository pharmacyAccountingDataInsertRepo;

  @Autowired
  private AccountingDataInsertRepository accountingDataInsertRepo;

  @LazyAutowired
  private GenericPreferencesService genPrefService;

  private Map<String, Object> dbDataMap = null;
  TestRepoInit testRepo = null;

  @BeforeMethod
  public void init(Method testMethod) throws IOException {
    System.out.println("@TEST : currently running -> "
        + testMethod.getName()
        + " of "
        + testMethod.getDeclaringClass());

    initMocks(this);
    ReflectionTestUtils.setField(pharmacyAccountingDataInsertService,
        "pharmacyAccountingDataInsertRepo", pharmacyAccountingDataInsertRepo);
    ReflectionTestUtils.setField(pharmacyAccountingDataInsertService, "accountingDataInsertRepo",
        accountingDataInsertRepo);
    ReflectionTestUtils.setField(pharmacyAccountingDataInsertService,
        "pharmacyOnlyAccountingDataInsertService", pharmacyOnlyAccountingDataInsertService);
    ReflectionTestUtils.setField(pharmacyOnlyAccountingDataInsertService,
        "pharmacyAccountingDataInsertRepo", pharmacyAccountingDataInsertRepo);
    ReflectionTestUtils.setField(pharmacyOnlyAccountingDataInsertService,
        "accountingDataInsertRepo", accountingDataInsertRepo);
    ReflectionTestUtils.setField(pharmacyAccountingDataInsertService, "genPrefService",
        genPrefService);
    ReflectionTestUtils.setField(pharmacyOnlyAccountingDataInsertService, "genPrefService",
        genPrefService);

    testRepo = new TestRepoInit(pharmacyAccountingDataInsertRepo);
    // relations having foreign key with hospital_center_master
    testRepo.insert("hospital_center_master");
    testRepo.insert("user_center_billing_counters");
    testRepo.insert("hosp_receipt_seq_prefs");
    testRepo.insert("hosp_op_ip_seq_prefs");
    testRepo.insert("hosp_item_seq_prefs");
    testRepo.insert("hosp_bill_seq_prefs");
    testRepo.insert("hosp_bill_audit_seq_prefs");
    testRepo.insert("center_integration_details");

    testRepo.insert("department");
    testRepo.insert("tpa_master");
    testRepo.insert("insurance_company_master");
    testRepo.insert("insurance_plan_main");
    testRepo.insert("insurance_claim");
    testRepo.insert("store_category_master");
    testRepo.insert("stores");

    testRepo.insert("patient_details");
    testRepo.insert("patient_registration");
    testRepo.insert("bill");
    testRepo.insert("store_sales_main");
    testRepo.insert("store_sales_details");
    testRepo.insert("manf_master");
    testRepo.insert("consumption_uom_master");
    testRepo.insert("store_item_details");
    testRepo.insert("store_retail_customers");
    testRepo.insert("sales_claim_details");
    testRepo.insert("store_sales_tax_details");
    testRepo.insert("sales_claim_tax_details");
    testRepo.insert("item_sub_groups");
    testRepo.insert("hms_accounting_info");

    dbDataMap = testRepo.initializeRepo();
  }

  @AfterMethod
  public void rollbackchanges() {
    testRepo.rollbackTransaction();
  }

  @Test
  public void getPharmacyAccountingDataToInsertNonInsuranceBillSuccessTest() {
    String saleBillNo = "SB027020";
    Integer jobTransaction = 2222;
    Date createdAt = new Date();
    StoreSalesMainModel storeSalesMainModel = pharmacyAccountingDataInsertRepo
        .getPharmacyBillModel(saleBillNo);
    BillModel billModel = storeSalesMainModel.getBillNo();
    billModel.setIsTpa(false);
    storeSalesMainModel.setBillNo(billModel);
    List<HmsAccountingInfoModel> resultList = pharmacyAccountingDataInsertService
        .getPharmacyAccountingDataToInsert(storeSalesMainModel, jobTransaction, createdAt);
    assertNotNull(resultList);
    assertEquals(resultList.size(), 4);

    // Assert patient amount entry
    HmsAccountingInfoModel accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))
          && accModel.getCreditAccount().equals(getSaleItemAccountHeadName(
              storeSalesMainModel.getStoreSalesDetails().iterator().next(), storeSalesMainModel))) {
        accountingModel = accModel;
        break;
      }
    }
    assertPatientEntryNonInsuranceBill(accountingModel, storeSalesMainModel);

    // Assert patient tax(CGST-2.5) amount entry
    StoreSalesTaxDetailsModel storeSalesTaxDetailsModel = null;
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      StoreSalesDetailsModel storeSalesDetailsModel = storeSalesMainModel.getStoreSalesDetails()
          .iterator().next();
      Set<StoreSalesTaxDetailsModel> ssTaxDetailsSet = storeSalesDetailsModel
          .getStoreSalesTaxDetails();
      for (StoreSalesTaxDetailsModel storeSalesTaxDetails : ssTaxDetailsSet) {
        if (accModel.getCustom4() != null
            && storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName().equals("CGST-2.5")
            && accModel.getCustom4()
                .equals(storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          storeSalesTaxDetailsModel = storeSalesTaxDetails;
          break;
        }
      }
    }
    assertPatientTaxEntryNonInsuranceBill(accountingModel, storeSalesMainModel,
        storeSalesTaxDetailsModel);

    // Assert patient tax(SGST-2.5) amount entry
    storeSalesTaxDetailsModel = null;
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      StoreSalesDetailsModel storeSalesDetailsModel = storeSalesMainModel.getStoreSalesDetails()
          .iterator().next();
      Set<StoreSalesTaxDetailsModel> ssTaxDetailsSet = storeSalesDetailsModel
          .getStoreSalesTaxDetails();
      for (StoreSalesTaxDetailsModel storeSalesTaxDetails : ssTaxDetailsSet) {
        if (accModel.getCustom4() != null
            && storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName().equals("SGST-2.5")
            && accModel.getCustom4()
                .equals(storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          storeSalesTaxDetailsModel = storeSalesTaxDetails;
          break;
        }
      }
    }
    assertPatientTaxEntryNonInsuranceBill(accountingModel, storeSalesMainModel,
        storeSalesTaxDetailsModel);
  }

  @Test
  public void getPharmacyAccountingDataToInsertInsuranceBillSuccessTest() {
    String saleBillNo = "SB027020";
    Integer jobTransaction = 2222;
    Date createdAt = new Date();
    StoreSalesMainModel storeSalesMainModel = pharmacyAccountingDataInsertRepo
        .getPharmacyBillModel(saleBillNo);
    List<HmsAccountingInfoModel> resultList = pharmacyAccountingDataInsertService
        .getPharmacyAccountingDataToInsert(storeSalesMainModel, jobTransaction, createdAt);
    assertNotNull(resultList);
    assertEquals(resultList.size(), 7);

    // Assert sponsor amount accounting entry
    StoreSalesDetailsModel saleItem = storeSalesMainModel.getStoreSalesDetails().iterator().next();
    SalesClaimDetailsModel saleClaim = saleItem.getSalesClaimDetails().iterator().next();
    HmsAccountingInfoModel accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(saleClaim.getSponsorId().getTpaName()) && accModel
          .getCreditAccount().equals(getSaleItemAccountHeadName(saleItem, storeSalesMainModel))) {
        accountingModel = accModel;
        break;
      }

    }
    assertSponsorAccountingEntry(accountingModel, saleItem, saleClaim, storeSalesMainModel);

    // Assert sponsor tax(CGST-2.5) accounting entry
    saleItem = storeSalesMainModel.getStoreSalesDetails().iterator().next();
    saleClaim = saleItem.getSalesClaimDetails().iterator().next();
    Set<SalesClaimTaxDetailsModel> saleClaimTaxes = saleClaim.getSalesClaimTaxDetails();
    accountingModel = null;
    SalesClaimTaxDetailsModel saleClaimTax = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (SalesClaimTaxDetailsModel saleClaimTaxModel : saleClaimTaxes) {
        BigDecimal taxAmount = saleClaimTaxModel.getTaxAmt() != null ? saleClaimTaxModel.getTaxAmt()
            : BigDecimal.ZERO;
        String debitAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
        if (taxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
          debitAccount = saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getTpaName()
              : null;
        }
        String creditAccount = saleClaim.getSponsorId() != null
            ? saleClaim.getSponsorId().getTpaName()
            : null;
        if (taxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
          creditAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
        }

        if (accModel != null && accModel.getCustom4() != null
            && accModel.getDebitAccount().equals(debitAccount)
            && accModel.getCreditAccount().equals(creditAccount)
            && saleClaimTaxModel.getItemSubGroupsModel().getItemSubgroupName().equals("CGST-2.5")
            && accModel.getCustom4()
                .equals(saleClaimTaxModel.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          saleClaimTax = saleClaimTaxModel;
          break;
        }
      }
    }
    assertSponsorTaxAccountingEntry(accountingModel, saleItem, saleClaimTax, saleClaim,
        storeSalesMainModel);

    // Assert sponsor tax(SGST-2.5) accounting entry
    saleItem = storeSalesMainModel.getStoreSalesDetails().iterator().next();
    saleClaim = saleItem.getSalesClaimDetails().iterator().next();
    saleClaimTaxes = saleClaim.getSalesClaimTaxDetails();
    accountingModel = null;
    saleClaimTax = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (SalesClaimTaxDetailsModel saleClaimTaxModel : saleClaimTaxes) {
        BigDecimal taxAmount = saleClaimTaxModel.getTaxAmt() != null ? saleClaimTaxModel.getTaxAmt()
            : BigDecimal.ZERO;
        String debitAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
        if (taxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
          debitAccount = saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getTpaName()
              : null;
        }
        String creditAccount = saleClaim.getSponsorId() != null
            ? saleClaim.getSponsorId().getTpaName()
            : null;
        if (taxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
          creditAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
        }

        if (accModel != null && accModel.getCustom4() != null
            && accModel.getDebitAccount().equals(debitAccount)
            && accModel.getCreditAccount().equals(creditAccount)
            && saleClaimTaxModel.getItemSubGroupsModel().getItemSubgroupName().equals("SGST-2.5")
            && accModel.getCustom4()
                .equals(saleClaimTaxModel.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          saleClaimTax = saleClaimTaxModel;
          break;
        }
      }
    }
    assertSponsorTaxAccountingEntry(accountingModel, saleItem, saleClaimTax, saleClaim,
        storeSalesMainModel);

    // Assert sponsor bill patient Amount accounting entry
    saleItem = storeSalesMainModel.getStoreSalesDetails().iterator().next();
    saleClaim = saleItem.getSalesClaimDetails().iterator().next();
    saleClaimTaxes = saleClaim.getSalesClaimTaxDetails();
    BigDecimal amount = getSaleItemAmount(saleItem);
    Map<Integer, BigDecimal> claimAmountSumMap = getClaimAmountSumMap(saleClaim);
    // get the insurance claim amount from sales_claim_details
    BigDecimal insuranceClaimAmount = BigDecimal.ZERO;
    if (claimAmountSumMap != null && claimAmountSumMap.containsKey(saleItem.getSaleItemId())) {
      insuranceClaimAmount = claimAmountSumMap.get(saleItem.getSaleItemId()) != null
          ? claimAmountSumMap.get(saleItem.getSaleItemId())
          : BigDecimal.ZERO;
    }
    BigDecimal netPatAmount = amount.subtract(insuranceClaimAmount);
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount()
          .equals(netPatAmount.compareTo(BigDecimal.ZERO) >= 0
              ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
              : getSaleItemAccountHeadName(saleItem, storeSalesMainModel))
          && accModel.getCreditAccount()
              .equals(netPatAmount.compareTo(BigDecimal.ZERO) >= 0
                  ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
                  : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))) {
        accountingModel = accModel;
        break;
      }
    }
    assertSponsorBillPatientAmountAccountingEntry(accountingModel, saleItem, storeSalesMainModel,
        claimAmountSumMap);

    // Assert sponsor bill patient tax CGST-2.5 Amount accounting entry
    saleItem = storeSalesMainModel.getStoreSalesDetails().iterator().next();
    Set<StoreSalesTaxDetailsModel> ssTaxDetailsSet = saleItem.getStoreSalesTaxDetails();
    Map<String, BigDecimal> subGroupLevelClaimTaxSumMap = getSubGroupLevelClaimTaxSumMap(
        saleItem.getSalesClaimDetails());

    accountingModel = null;
    StoreSalesTaxDetailsModel ssTaxDetailsModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (StoreSalesTaxDetailsModel ssTaxDetails : ssTaxDetailsSet) {
        String saleItemId = String.valueOf(ssTaxDetails.getId().getSaleItemId());
        String itemSubGroupId = String.valueOf(ssTaxDetails.getItemSubGroupsModel() != null
            ? ssTaxDetails.getItemSubGroupsModel().getItemSubgroupId()
            : null);
        String key = saleItemId
            + "#"
            + itemSubGroupId;
        BigDecimal claimTaxSumPerSubgroup = BigDecimal.ZERO;
        if (subGroupLevelClaimTaxSumMap.containsKey(key)) {
          BigDecimal claimTaxSum = subGroupLevelClaimTaxSumMap.get(key);
          claimTaxSumPerSubgroup = claimTaxSum != null ? claimTaxSum : BigDecimal.ZERO;
        }

        BigDecimal patTaxAmount = (ssTaxDetails.getTaxAmt() != null ? ssTaxDetails.getTaxAmt()
            : BigDecimal.ZERO).subtract(claimTaxSumPerSubgroup);

        String debitAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
                ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
                : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
        String creditAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
                ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS");

        if (accModel.getCustom4() != null && accModel.getDebitAccount().equals(debitAccount)
            && accModel.getCreditAccount().equals(creditAccount)
            && ssTaxDetails.getItemSubGroupsModel().getItemSubgroupName().equals("CGST-2.5")
            && accModel.getCustom4()
                .equals(ssTaxDetails.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          ssTaxDetailsModel = ssTaxDetails;
          break;
        }
      }
    }
    assertSponsorBillPatientTaxAmountAccountingEntry(accountingModel, saleItem, ssTaxDetailsModel,
        storeSalesMainModel, subGroupLevelClaimTaxSumMap);

    // Assert sponsor bill patient tax SGST-2.5 Amount accounting entry
    saleItem = storeSalesMainModel.getStoreSalesDetails().iterator().next();
    ssTaxDetailsSet = saleItem.getStoreSalesTaxDetails();
    subGroupLevelClaimTaxSumMap = getSubGroupLevelClaimTaxSumMap(saleItem.getSalesClaimDetails());

    accountingModel = null;
    ssTaxDetailsModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      for (StoreSalesTaxDetailsModel ssTaxDetails : ssTaxDetailsSet) {
        String saleItemId = String.valueOf(ssTaxDetails.getId().getSaleItemId());
        String itemSubGroupId = String.valueOf(ssTaxDetails.getItemSubGroupsModel() != null
            ? ssTaxDetails.getItemSubGroupsModel().getItemSubgroupId()
            : null);
        String key = saleItemId
            + "#"
            + itemSubGroupId;
        BigDecimal claimTaxSumPerSubgroup = BigDecimal.ZERO;
        if (subGroupLevelClaimTaxSumMap.containsKey(key)) {
          BigDecimal claimTaxSum = subGroupLevelClaimTaxSumMap.get(key);
          claimTaxSumPerSubgroup = claimTaxSum != null ? claimTaxSum : BigDecimal.ZERO;
        }

        BigDecimal patTaxAmount = (ssTaxDetails.getTaxAmt() != null ? ssTaxDetails.getTaxAmt()
            : BigDecimal.ZERO).subtract(claimTaxSumPerSubgroup);

        String debitAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
                ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
                : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
        String creditAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 && !String
            .valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
                ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
                : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS");

        if (accModel.getCustom4() != null && accModel.getDebitAccount().equals(debitAccount)
            && accModel.getCreditAccount().equals(creditAccount)
            && ssTaxDetails.getItemSubGroupsModel().getItemSubgroupName().equals("SGST-2.5")
            && accModel.getCustom4()
                .equals(ssTaxDetails.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          ssTaxDetailsModel = ssTaxDetails;
          break;
        }
      }
    }
    assertSponsorBillPatientTaxAmountAccountingEntry(accountingModel, saleItem, ssTaxDetailsModel,
        storeSalesMainModel, subGroupLevelClaimTaxSumMap);
  }

  @Test
  public void getPharmacyAccountingDataToInsertRetailSalesTest() {
    String saleBillNo = "SB027022";
    Integer jobTransaction = 2211;
    Date createdAt = new Date();
    StoreSalesMainModel storeSalesMainModel = pharmacyAccountingDataInsertRepo
        .getPharmacyBillModel(saleBillNo);

    List<HmsAccountingInfoModel> resultList = pharmacyAccountingDataInsertService
        .getPharmacyAccountingDataToInsert(storeSalesMainModel, jobTransaction, createdAt);
    assertNotNull(resultList);
    assertEquals(resultList.size(), 4);

    // Assert patient amount entry
    HmsAccountingInfoModel accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      if (accModel.getDebitAccount().equals(accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"))
          && accModel.getCreditAccount().equals(getSaleItemAccountHeadName(
              storeSalesMainModel.getStoreSalesDetails().iterator().next(), storeSalesMainModel))) {
        accountingModel = accModel;
        break;
      }
    }
    assertPatientEntryNonInsuranceBillRetailSale(accountingModel, storeSalesMainModel);

    // Assert patient tax(CGST-2.5) amount entry
    StoreSalesTaxDetailsModel storeSalesTaxDetailsModel = null;
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      StoreSalesDetailsModel storeSalesDetailsModel = storeSalesMainModel.getStoreSalesDetails()
          .iterator().next();
      Set<StoreSalesTaxDetailsModel> ssTaxDetailsSet = storeSalesDetailsModel
          .getStoreSalesTaxDetails();
      for (StoreSalesTaxDetailsModel storeSalesTaxDetails : ssTaxDetailsSet) {
        if (accModel.getCustom4() != null
            && storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName().equals("CGST-2.5")
            && accModel.getCustom4()
                .equals(storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          storeSalesTaxDetailsModel = storeSalesTaxDetails;
          break;
        }
      }
    }
    assertPatientTaxEntryNonInsuranceBillRetailSale(accountingModel, storeSalesMainModel,
        storeSalesTaxDetailsModel);

    // Assert patient tax(SGST-2.5) amount entry
    storeSalesTaxDetailsModel = null;
    accountingModel = null;
    for (HmsAccountingInfoModel accModel : resultList) {
      StoreSalesDetailsModel storeSalesDetailsModel = storeSalesMainModel.getStoreSalesDetails()
          .iterator().next();
      Set<StoreSalesTaxDetailsModel> ssTaxDetailsSet = storeSalesDetailsModel
          .getStoreSalesTaxDetails();
      for (StoreSalesTaxDetailsModel storeSalesTaxDetails : ssTaxDetailsSet) {
        if (accModel.getCustom4() != null
            && storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName().equals("SGST-2.5")
            && accModel.getCustom4()
                .equals(storeSalesTaxDetails.getItemSubGroupsModel().getItemSubgroupName())) {
          accountingModel = accModel;
          storeSalesTaxDetailsModel = storeSalesTaxDetails;
          break;
        }
      }
    }
    assertPatientTaxEntryNonInsuranceBillRetailSale(accountingModel, storeSalesMainModel,
        storeSalesTaxDetailsModel);
  }

  private void assertPatientEntryNonInsuranceBill(HmsAccountingInfoModel accountingModel,
      StoreSalesMainModel storeSalesMainModel) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getMrNo()
            : null);
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getOldmrno()
            : null);
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientId()
            : null);
    assertEquals(accountingModel.getVisitType(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getVisitType()
            : null);

    if (storeSalesMainModel.getBillNo().getVisitId() !=null) {
      assertEquals(
          accountingModel.getAdmittingDoctor() != null ? accountingModel.getAdmittingDoctor().trim()
              : null,
          storeSalesMainModel.getBillNo().getVisitId().getDoctor() != null
              ? storeSalesMainModel.getBillNo().getVisitId().getDoctor().getDoctorName() : null);
      assertEquals(accountingModel.getAdmittingDepartment() != null
          ? accountingModel.getAdmittingDepartment().trim()
          : null, storeSalesMainModel.getBillNo().getVisitId().getDeptName() != null
          ? storeSalesMainModel.getBillNo().getVisitId().getDeptName().getDeptName() : null);
      }
      else {
        assertEquals(
            accountingModel.getAdmittingDoctor() !=null ? accountingModel.getAdmittingDoctor().trim()
                : null, null );
        assertEquals(
            accountingModel.getAdmittingDepartment() !=null ? accountingModel.getAdmittingDepartment().trim()
                : null, null );
      }
    assertEquals(
        accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor().trim()
            : null,
        null);
    assertEquals(accountingModel.getCenterId(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? Integer
                .valueOf(storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterId())
            : Integer.valueOf(storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterId()));
    assertEquals(
        accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterName()
            : storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterName());
    assertEquals(
        accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(
        accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim() : null,
            accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PHBILLS"));
    assertEquals(
        accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim() : null,
        null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), storeSalesMainModel.getBillNo().getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(),
        storeSalesMainModel.getBillNo().getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null
        ? accountingModel.getAuditControlNumber().trim()
        : null, storeSalesMainModel.getBillNo().getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getAccountGroup()));
    assertEquals(accountingModel.getPointsRedeemed(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getPointsRedeemed()));
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(),
        storeSalesMainModel.getBillNo().getPointsRedeemedAmt() != null
            ? storeSalesMainModel.getBillNo().getPointsRedeemedAmt()
            : BigDecimal.ZERO);
    assertEquals(accountingModel.getIsTpa(),
        storeSalesMainModel.getBillNo().getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        null);

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

    ChargeheadConstantsModel chargeHeadModel = getChargeHeadConstantsModel(storeSalesMainModel);
    assertEquals(
        accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim() : null,
        (chargeHeadModel != null && chargeHeadModel.getChargeGroupModel() != null)
            ? chargeHeadModel.getChargeGroupModel().getChargegroupName()
            : null);
    assertEquals(
        accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim() : null,
        chargeHeadModel != null ? chargeHeadModel.getChargeheadId() : null);
    assertEquals(accountingModel.getModTime(), storeSalesMainModel.getDateTime());
    assertEquals(accountingModel.getIssueStore(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getDeptName()
            : null);
    assertEquals(accountingModel.getIssueStoreCenter(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName()
            : null);
    assertEquals(accountingModel.getVoucherDate(), storeSalesMainModel.getSaleDate());

    Set<StoreSalesDetailsModel> storeSalesDetailsSet = storeSalesMainModel.getStoreSalesDetails();
    StoreSalesDetailsModel saleItem = storeSalesDetailsSet.iterator().next();
    assertEquals(
        accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim() : null,
        String.valueOf(saleItem.getMedicineId()));
    assertEquals(
        accountingModel.getItemName() != null ? accountingModel.getItemName().trim() : null,
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getMedicineName()
            : null);
    assertEquals(
        accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup().trim() : null,
        getServiceGroupName(saleItem));
    assertEquals(
        accountingModel.getServiceSubGroup() != null ? accountingModel.getServiceSubGroup().trim()
            : null,
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getServiceSubGroupId() != null)
                ? saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceSubGroupName()
                : null);
    assertEquals(accountingModel.getQuantity(), saleItem.getQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), saleItem.getDisc());
    assertEquals(accountingModel.getPrescribingDoctor() != null
        ? accountingModel.getPrescribingDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null
        ? accountingModel.getPrescribingDoctorDeptName().trim()
        : null, null);
    assertEquals(accountingModel.getConductiongDoctor() != null
        ? accountingModel.getConductiongDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getConductingDepartment() != null
        ? accountingModel.getConductingDepartment().trim()
        : null, null);
    assertEquals(
        accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim() : null,
        null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(),
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getStoreCategoryMaster() != null)
                ? saleItem.getStoreItemDetails().getStoreCategoryMaster().getCategoryId()
                : null);
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(),
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getCustItemCode()
            : null);
    assertEquals(accountingModel.getCostAmount(), saleItem.getCostValue());

    BigDecimal grossAmount = getSaleItemAmount(saleItem)
        .add(saleItem.getDisc() != null ? saleItem.getDisc() : BigDecimal.ZERO);
    assertEquals(accountingModel.getGrossAmount(),
        grossAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossAmount : grossAmount.negate());
    assertEquals(accountingModel.getNetAmount(),
        getSaleItemAmount(saleItem).compareTo(BigDecimal.ZERO) >= 0 ? getSaleItemAmount(saleItem)
            : getSaleItemAmount(saleItem).negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        getSaleItemAmount(saleItem).compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    assertEquals(accountingModel.getCreditAccount(),
        getSaleItemAmount(saleItem).compareTo(BigDecimal.ZERO) >= 0
            ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    assertEquals(accountingModel.getCustom1(),
        accountingDataInsertRepo.getfaConfiguration("use_credit_ac_for_discount").equals("Y")
            ? getSaleItemAmount(saleItem).toString() : grossAmount.toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(),
        getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
  }

  private void assertPatientTaxEntryNonInsuranceBill(HmsAccountingInfoModel accountingModel,
      StoreSalesMainModel storeSalesMainModel, StoreSalesTaxDetailsModel saleItemTax) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getMrNo()
            : null);
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getOldmrno()
            : null);
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientId()
            : null);
    assertEquals(accountingModel.getVisitType(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getVisitType()
            : null);

    if (storeSalesMainModel.getBillNo().getVisitId() !=null) {
      assertEquals(
          accountingModel.getAdmittingDoctor() != null ? accountingModel.getAdmittingDoctor().trim()
              : null,
          storeSalesMainModel.getBillNo().getVisitId().getDoctor() != null
              ? storeSalesMainModel.getBillNo().getVisitId().getDoctor().getDoctorName() : null);
      assertEquals(accountingModel.getAdmittingDepartment() != null
          ? accountingModel.getAdmittingDepartment().trim()
          : null, storeSalesMainModel.getBillNo().getVisitId().getDeptName() != null
          ? storeSalesMainModel.getBillNo().getVisitId().getDeptName().getDeptName() : null);
      }
      else {
        assertEquals(
            accountingModel.getAdmittingDoctor() !=null ? accountingModel.getAdmittingDoctor().trim()
                : null, null );
        assertEquals(
            accountingModel.getAdmittingDepartment() !=null ? accountingModel.getAdmittingDepartment().trim()
                : null, null );
      }
    assertEquals(
        accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor().trim()
            : null,
        null);
    assertEquals(accountingModel.getCenterId(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? Integer
                .valueOf(storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterId())
            : Integer.valueOf(storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterId()));
    assertEquals(
        accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterName()
            : storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterName());
    assertEquals(
        accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(
        accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim() : null,
            accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PHBILLS"));
    assertEquals(
        accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim() : null,
        null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), storeSalesMainModel.getBillNo().getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(),
        storeSalesMainModel.getBillNo().getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null
        ? accountingModel.getAuditControlNumber().trim()
        : null, storeSalesMainModel.getBillNo().getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getAccountGroup()));
    assertEquals(accountingModel.getPointsRedeemed(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getPointsRedeemed()));
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(),
        storeSalesMainModel.getBillNo().getPointsRedeemedAmt() != null
            ? storeSalesMainModel.getBillNo().getPointsRedeemedAmt()
            : BigDecimal.ZERO);
    assertEquals(accountingModel.getIsTpa(),
        storeSalesMainModel.getBillNo().getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        null);

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
    assertEquals(accountingModel.getSalesVatPercent(), saleItemTax.getTaxRate());

    ChargeheadConstantsModel chargeHeadModel = getChargeHeadConstantsModel(storeSalesMainModel);
    assertEquals(
        accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim() : null,
        (chargeHeadModel != null && chargeHeadModel.getChargeGroupModel() != null)
            ? chargeHeadModel.getChargeGroupModel().getChargegroupName()
            : null);
    assertEquals(
        accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim() : null,
        chargeHeadModel != null ? chargeHeadModel.getChargeheadId() : null);
    assertEquals(accountingModel.getModTime(), storeSalesMainModel.getDateTime());
    assertEquals(accountingModel.getIssueStore(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getDeptName()
            : null);
    assertEquals(accountingModel.getIssueStoreCenter(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName()
            : null);
    assertEquals(accountingModel.getVoucherDate(), storeSalesMainModel.getSaleDate());

    Set<StoreSalesDetailsModel> storeSalesDetailsSet = storeSalesMainModel.getStoreSalesDetails();
    StoreSalesDetailsModel saleItem = storeSalesDetailsSet.iterator().next();
    assertEquals(
        accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim() : null,
        String.valueOf(saleItem.getMedicineId()));
    assertEquals(
        accountingModel.getItemName() != null ? accountingModel.getItemName().trim() : null,
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getMedicineName()
            : null);
    assertEquals(
        accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup().trim() : null,
        getServiceGroupName(saleItem));
    assertEquals(
        accountingModel.getServiceSubGroup() != null ? accountingModel.getServiceSubGroup().trim()
            : null,
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getServiceSubGroupId() != null)
                ? saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceSubGroupName()
                : null);
    assertEquals(accountingModel.getQuantity(), saleItem.getQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPrescribingDoctor() != null
        ? accountingModel.getPrescribingDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null
        ? accountingModel.getPrescribingDoctorDeptName().trim()
        : null, null);
    assertEquals(accountingModel.getConductiongDoctor() != null
        ? accountingModel.getConductiongDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getConductingDepartment() != null
        ? accountingModel.getConductingDepartment().trim()
        : null, null);
    assertEquals(
        accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim() : null,
        null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(),
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getStoreCategoryMaster() != null)
                ? saleItem.getStoreItemDetails().getStoreCategoryMaster().getCategoryId()
                : null);
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(),
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getCustItemCode()
            : null);
    assertEquals(accountingModel.getCostAmount(), saleItem.getCostValue());

    BigDecimal taxAmount = saleItemTax.getTaxAmt() != null ? saleItemTax.getTaxAmt()
        : BigDecimal.ZERO;
    BigDecimal grossAmount = taxAmount.compareTo(BigDecimal.ZERO) >= 0 ? taxAmount
        : taxAmount.negate();
    assertEquals(accountingModel.getGrossAmount(), grossAmount);
    assertEquals(accountingModel.getNetAmount(), grossAmount);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    String debitAccount = (taxAmount.compareTo(BigDecimal.ZERO) >= 0
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
    String creditAccount = (taxAmount.compareTo(BigDecimal.ZERO) >= 0
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS");
    assertEquals(accountingModel.getDebitAccount(), debitAccount);
    assertEquals(accountingModel.getCreditAccount(), creditAccount);

    assertEquals(accountingModel.getCustom1(), taxAmount.toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(),
        saleItemTax.getItemSubGroupsModel() != null
            ? saleItemTax.getItemSubGroupsModel().getItemSubgroupName()
            : null);
  }

  private void assertSponsorAccountingEntry(HmsAccountingInfoModel accountingModel,
      StoreSalesDetailsModel saleItem, SalesClaimDetailsModel saleClaim,
      StoreSalesMainModel storeSalesMainModel) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getMrNo()
            : null);
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getOldmrno()
            : null);
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientId()
            : null);
    assertEquals(accountingModel.getVisitType(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getVisitType()
            : null);
    if (storeSalesMainModel.getBillNo().getVisitId() !=null) {
    assertEquals(
        accountingModel.getAdmittingDoctor() != null ? accountingModel.getAdmittingDoctor().trim()
            : null,
        storeSalesMainModel.getBillNo().getVisitId().getDoctor() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getDoctor().getDoctorName() : null);
    assertEquals(accountingModel.getAdmittingDepartment() != null
        ? accountingModel.getAdmittingDepartment().trim()
        : null, storeSalesMainModel.getBillNo().getVisitId().getDeptName() != null
        ? storeSalesMainModel.getBillNo().getVisitId().getDeptName().getDeptName() : null);
    }
    else {
      assertEquals(
          accountingModel.getAdmittingDoctor() !=null ? accountingModel.getAdmittingDoctor().trim()
              : null, null );
      assertEquals(
          accountingModel.getAdmittingDepartment() !=null ? accountingModel.getAdmittingDepartment().trim()
              : null, null );
    }
      
    assertEquals(
        accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor().trim()
            : null,
        null);
    assertEquals(accountingModel.getCenterId(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? Integer
                .valueOf(storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterId())
            : Integer.valueOf(storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterId()));
    assertEquals(
        accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterName()
            : storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterName());
    assertEquals(
        accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(
        accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim() : null,
            accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PHBILLS"));
    assertEquals(
        accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim() : null,
        null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), storeSalesMainModel.getBillNo().getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(),
        storeSalesMainModel.getBillNo().getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null
        ? accountingModel.getAuditControlNumber().trim()
        : null, storeSalesMainModel.getBillNo().getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getAccountGroup()));
    assertEquals(accountingModel.getPointsRedeemed(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getPointsRedeemed()));
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(),
        storeSalesMainModel.getBillNo().getPointsRedeemedAmt() != null
            ? storeSalesMainModel.getBillNo().getPointsRedeemedAmt()
            : BigDecimal.ZERO);
    assertEquals(accountingModel.getIsTpa(),
        storeSalesMainModel.getBillNo().getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        null);

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

    ChargeheadConstantsModel chargeHeadModel = getChargeHeadConstantsModel(storeSalesMainModel);
    assertEquals(
        accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim() : null,
        (chargeHeadModel != null && chargeHeadModel.getChargeGroupModel() != null)
            ? chargeHeadModel.getChargeGroupModel().getChargegroupName()
            : null);
    assertEquals(
        accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim() : null,
        chargeHeadModel != null ? chargeHeadModel.getChargeheadId() : null);
    assertEquals(accountingModel.getModTime(), storeSalesMainModel.getDateTime());
    assertEquals(accountingModel.getIssueStore(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getDeptName()
            : null);
    assertEquals(accountingModel.getIssueStoreCenter(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName()
            : null);
    assertEquals(accountingModel.getVoucherDate(), storeSalesMainModel.getSaleDate());

    assertEquals(
        accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim() : null,
        String.valueOf(saleItem.getMedicineId()));
    assertEquals(
        accountingModel.getItemName() != null ? accountingModel.getItemName().trim() : null,
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getMedicineName()
            : null);
    assertEquals(
        accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup().trim() : null,
        getServiceGroupName(saleItem));
    assertEquals(
        accountingModel.getServiceSubGroup() != null ? accountingModel.getServiceSubGroup().trim()
            : null,
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getServiceSubGroupId() != null)
                ? saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceSubGroupName()
                : null);
    assertEquals(accountingModel.getQuantity(), saleItem.getQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), saleItem.getDisc());
    assertEquals(accountingModel.getPrescribingDoctor() != null
        ? accountingModel.getPrescribingDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null
        ? accountingModel.getPrescribingDoctorDeptName().trim()
        : null, null);
    assertEquals(accountingModel.getConductiongDoctor() != null
        ? accountingModel.getConductiongDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getConductingDepartment() != null
        ? accountingModel.getConductingDepartment().trim()
        : null, null);
    assertEquals(
        accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim() : null,
        null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(),
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getStoreCategoryMaster() != null)
                ? saleItem.getStoreItemDetails().getStoreCategoryMaster().getCategoryId()
                : null);
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(),
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getCustItemCode()
            : null);
    assertEquals(accountingModel.getCostAmount(), saleItem.getCostValue());

    BigDecimal grossAmount = saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
        ? saleClaim.getInsuranceClaimAmt()
        : saleClaim.getInsuranceClaimAmt().negate();
    assertEquals(accountingModel.getGrossAmount(), grossAmount);
    assertEquals(accountingModel.getNetAmount(), grossAmount);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
            ? saleClaim.getSponsorId().getTpaName()
            : getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    assertEquals(accountingModel.getCreditAccount(),
        saleClaim.getInsuranceClaimAmt().compareTo(BigDecimal.ZERO) >= 0
            ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
            : saleClaim.getSponsorId().getTpaName());

    assertEquals(accountingModel.getCustom1(), saleClaim.getInsuranceClaimAmt().toString());
    assertEquals(accountingModel.getCustom2(), saleClaim.getSponsorId().getTpaName());
    assertEquals(accountingModel.getCustom3(),
        String.valueOf(saleClaim.getSponsorId().getSponsorType()));
    assertEquals(accountingModel.getCustom4(),
        getSaleItemAccountHeadName(saleItem, storeSalesMainModel));

  }

  private void assertSponsorTaxAccountingEntry(HmsAccountingInfoModel accountingModel,
      StoreSalesDetailsModel saleItem, SalesClaimTaxDetailsModel saleClaimTax,
      SalesClaimDetailsModel saleClaim, StoreSalesMainModel storeSalesMainModel) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getMrNo()
            : null);
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getOldmrno()
            : null);
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientId()
            : null);
    assertEquals(accountingModel.getVisitType(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getVisitType()
            : null);

    if (storeSalesMainModel.getBillNo().getVisitId() !=null) {
      assertEquals(
          accountingModel.getAdmittingDoctor() != null ? accountingModel.getAdmittingDoctor().trim()
              : null,
          storeSalesMainModel.getBillNo().getVisitId().getDoctor() != null
              ? storeSalesMainModel.getBillNo().getVisitId().getDoctor().getDoctorName() : null);
      assertEquals(accountingModel.getAdmittingDepartment() != null
          ? accountingModel.getAdmittingDepartment().trim()
          : null, storeSalesMainModel.getBillNo().getVisitId().getDeptName() != null
          ? storeSalesMainModel.getBillNo().getVisitId().getDeptName().getDeptName() : null);
      }
      else {
        assertEquals(
            accountingModel.getAdmittingDoctor() !=null ? accountingModel.getAdmittingDoctor().trim()
                : null, null );
        assertEquals(
            accountingModel.getAdmittingDepartment() !=null ? accountingModel.getAdmittingDepartment().trim()
                : null, null );
      }
    assertEquals(
        accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor().trim()
            : null,
        null);
    assertEquals(accountingModel.getCenterId(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? Integer
                .valueOf(storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterId())
            : Integer.valueOf(storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterId()));
    assertEquals(
        accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterName()
            : storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterName());
    assertEquals(
        accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(
        accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim() : null,
            accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PHBILLS"));
    assertEquals(
        accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim() : null,
        null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), storeSalesMainModel.getBillNo().getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(),
        storeSalesMainModel.getBillNo().getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null
        ? accountingModel.getAuditControlNumber().trim()
        : null, storeSalesMainModel.getBillNo().getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getAccountGroup()));
    assertEquals(accountingModel.getPointsRedeemed(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getPointsRedeemed()));
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(),
        storeSalesMainModel.getBillNo().getPointsRedeemedAmt() != null
            ? storeSalesMainModel.getBillNo().getPointsRedeemedAmt()
            : BigDecimal.ZERO);
    assertEquals(accountingModel.getIsTpa(),
        storeSalesMainModel.getBillNo().getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        null);

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
    assertEquals(accountingModel.getSalesVatPercent(), saleClaimTax.getTaxRate());

    ChargeheadConstantsModel chargeHeadModel = getChargeHeadConstantsModel(storeSalesMainModel);
    assertEquals(
        accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim() : null,
        (chargeHeadModel != null && chargeHeadModel.getChargeGroupModel() != null)
            ? chargeHeadModel.getChargeGroupModel().getChargegroupName()
            : null);
    assertEquals(
        accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim() : null,
        chargeHeadModel != null ? chargeHeadModel.getChargeheadId() : null);
    assertEquals(accountingModel.getModTime(), storeSalesMainModel.getDateTime());
    assertEquals(accountingModel.getIssueStore(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getDeptName()
            : null);
    assertEquals(accountingModel.getIssueStoreCenter(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName()
            : null);
    assertEquals(accountingModel.getVoucherDate(), storeSalesMainModel.getSaleDate());

    assertEquals(
        accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim() : null,
        String.valueOf(saleItem.getMedicineId()));
    assertEquals(
        accountingModel.getItemName() != null ? accountingModel.getItemName().trim() : null,
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getMedicineName()
            : null);
    assertEquals(
        accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup().trim() : null,
        getServiceGroupName(saleItem));
    assertEquals(
        accountingModel.getServiceSubGroup() != null ? accountingModel.getServiceSubGroup().trim()
            : null,
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getServiceSubGroupId() != null)
                ? saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceSubGroupName()
                : null);
    assertEquals(accountingModel.getQuantity(), saleItem.getQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPrescribingDoctor() != null
        ? accountingModel.getPrescribingDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null
        ? accountingModel.getPrescribingDoctorDeptName().trim()
        : null, null);
    assertEquals(accountingModel.getConductiongDoctor() != null
        ? accountingModel.getConductiongDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getConductingDepartment() != null
        ? accountingModel.getConductingDepartment().trim()
        : null, null);
    assertEquals(
        accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim() : null,
        null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(),
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getStoreCategoryMaster() != null)
                ? saleItem.getStoreItemDetails().getStoreCategoryMaster().getCategoryId()
                : null);
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(),
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getCustItemCode()
            : null);
    assertEquals(accountingModel.getCostAmount(), saleItem.getCostValue());

    BigDecimal taxAmount = saleClaimTax.getTaxAmt() != null ? saleClaimTax.getTaxAmt()
        : BigDecimal.ZERO;
    BigDecimal grossAmount = taxAmount.compareTo(BigDecimal.ZERO) >= 0 ? taxAmount
        : taxAmount.negate();

    assertEquals(accountingModel.getGrossAmount(), grossAmount);
    assertEquals(accountingModel.getNetAmount(), grossAmount);
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);

    String debitAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
    if (taxAmount.compareTo(BigDecimal.ZERO) >= 0
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
      debitAccount = saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getTpaName()
          : null;
    }
    String creditAccount = saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getTpaName()
        : null;
    if (taxAmount.compareTo(BigDecimal.ZERO) >= 0
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R)) {
      creditAccount = accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
    }

    assertEquals(accountingModel.getDebitAccount(), debitAccount);
    assertEquals(accountingModel.getCreditAccount(), creditAccount);

    assertEquals(accountingModel.getCustom1(), taxAmount.toString());
    assertEquals(accountingModel.getCustom2(),
        saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getTpaName() : null);
    assertEquals(accountingModel.getCustom3(), String.valueOf(
        saleClaim.getSponsorId() != null ? saleClaim.getSponsorId().getSponsorType() : null));
    assertEquals(accountingModel.getCustom4(),
        saleClaimTax.getItemSubGroupsModel().getItemSubgroupName());
  }

  private void assertSponsorBillPatientAmountAccountingEntry(HmsAccountingInfoModel accountingModel,
      StoreSalesDetailsModel saleItem, StoreSalesMainModel storeSalesMainModel,
      Map<Integer, BigDecimal> claimAmountSumMap) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getMrNo()
            : null);
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getOldmrno()
            : null);
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientId()
            : null);
    assertEquals(accountingModel.getVisitType(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getVisitType()
            : null);

    if (storeSalesMainModel.getBillNo().getVisitId() !=null) {
      assertEquals(
          accountingModel.getAdmittingDoctor() != null ? accountingModel.getAdmittingDoctor().trim()
              : null,
          storeSalesMainModel.getBillNo().getVisitId().getDoctor() != null
              ? storeSalesMainModel.getBillNo().getVisitId().getDoctor().getDoctorName() : null);
      assertEquals(accountingModel.getAdmittingDepartment() != null
          ? accountingModel.getAdmittingDepartment().trim()
          : null, storeSalesMainModel.getBillNo().getVisitId().getDeptName() != null
          ? storeSalesMainModel.getBillNo().getVisitId().getDeptName().getDeptName() : null);
      }
      else {
        assertEquals(
            accountingModel.getAdmittingDoctor() !=null ? accountingModel.getAdmittingDoctor().trim()
                : null, null );
        assertEquals(
            accountingModel.getAdmittingDepartment() !=null ? accountingModel.getAdmittingDepartment().trim()
                : null, null );
      }
    assertEquals(
        accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor().trim()
            : null,
        null);
    assertEquals(accountingModel.getCenterId(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? Integer
                .valueOf(storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterId())
            : Integer.valueOf(storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterId()));
    assertEquals(
        accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterName()
            : storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterName());
    assertEquals(
        accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(
        accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim() : null,
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PHBILLS"));
    assertEquals(
        accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim() : null,
        null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), storeSalesMainModel.getBillNo().getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(),
        storeSalesMainModel.getBillNo().getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null
        ? accountingModel.getAuditControlNumber().trim()
        : null, storeSalesMainModel.getBillNo().getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getAccountGroup()));
    assertEquals(accountingModel.getPointsRedeemed(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getPointsRedeemed()));
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(),
        storeSalesMainModel.getBillNo().getPointsRedeemedAmt() != null
            ? storeSalesMainModel.getBillNo().getPointsRedeemedAmt()
            : BigDecimal.ZERO);
    assertEquals(accountingModel.getIsTpa(),
        storeSalesMainModel.getBillNo().getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        null);

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

    ChargeheadConstantsModel chargeHeadModel = getChargeHeadConstantsModel(storeSalesMainModel);
    assertEquals(
        accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim() : null,
        (chargeHeadModel != null && chargeHeadModel.getChargeGroupModel() != null)
            ? chargeHeadModel.getChargeGroupModel().getChargegroupName()
            : null);
    assertEquals(
        accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim() : null,
        chargeHeadModel != null ? chargeHeadModel.getChargeheadId() : null);
    assertEquals(accountingModel.getModTime(), storeSalesMainModel.getDateTime());
    assertEquals(accountingModel.getIssueStore(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getDeptName()
            : null);
    assertEquals(accountingModel.getIssueStoreCenter(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName()
            : null);
    assertEquals(accountingModel.getVoucherDate(), storeSalesMainModel.getSaleDate());

    assertEquals(
        accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim() : null,
        String.valueOf(saleItem.getMedicineId()));
    assertEquals(
        accountingModel.getItemName() != null ? accountingModel.getItemName().trim() : null,
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getMedicineName()
            : null);
    assertEquals(
        accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup().trim() : null,
        getServiceGroupName(saleItem));
    assertEquals(
        accountingModel.getServiceSubGroup() != null ? accountingModel.getServiceSubGroup().trim()
            : null,
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getServiceSubGroupId() != null)
                ? saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceSubGroupName()
                : null);
    assertEquals(accountingModel.getQuantity(), saleItem.getQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), saleItem.getDisc());
    assertEquals(accountingModel.getPrescribingDoctor() != null
        ? accountingModel.getPrescribingDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null
        ? accountingModel.getPrescribingDoctorDeptName().trim()
        : null, null);
    assertEquals(accountingModel.getConductiongDoctor() != null
        ? accountingModel.getConductiongDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getConductingDepartment() != null
        ? accountingModel.getConductingDepartment().trim()
        : null, null);
    assertEquals(
        accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim() : null,
        null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(),
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getStoreCategoryMaster() != null)
                ? saleItem.getStoreItemDetails().getStoreCategoryMaster().getCategoryId()
                : null);
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(),
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getCustItemCode()
            : null);
    assertEquals(accountingModel.getCostAmount(), saleItem.getCostValue());

    BigDecimal amount = getSaleItemAmount(saleItem);
    BigDecimal discount = saleItem.getDisc() != null ? saleItem.getDisc() : BigDecimal.ZERO;
    // get the insurance claim amount from sales_claim_details
    BigDecimal insuranceClaimAmount = BigDecimal.ZERO;
    if (claimAmountSumMap != null && claimAmountSumMap.containsKey(saleItem.getSaleItemId())) {
      insuranceClaimAmount = claimAmountSumMap.get(saleItem.getSaleItemId()) != null
          ? claimAmountSumMap.get(saleItem.getSaleItemId())
          : BigDecimal.ZERO;
    }

    BigDecimal grossPatAmount = amount.add(discount).subtract(insuranceClaimAmount);
    BigDecimal netPatAmount = amount.subtract(insuranceClaimAmount);

    assertEquals(accountingModel.getGrossAmount(),
        grossPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? grossPatAmount : grossPatAmount.negate());
    assertEquals(accountingModel.getNetAmount(),
        netPatAmount.compareTo(BigDecimal.ZERO) >= 0 ? netPatAmount : netPatAmount.negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);
    assertEquals(accountingModel.getDebitAccount(),
        netPatAmount.compareTo(BigDecimal.ZERO) >= 0
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
    assertEquals(accountingModel.getCreditAccount(),
        netPatAmount.compareTo(BigDecimal.ZERO) >= 0
            ? getSaleItemAccountHeadName(saleItem, storeSalesMainModel)
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS"));

    assertEquals(accountingModel.getCustom1(), netPatAmount.toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(),
        getSaleItemAccountHeadName(saleItem, storeSalesMainModel));
  }

  private void assertSponsorBillPatientTaxAmountAccountingEntry(
      HmsAccountingInfoModel accountingModel, StoreSalesDetailsModel saleItem,
      StoreSalesTaxDetailsModel saleItemTax, StoreSalesMainModel storeSalesMainModel,
      Map<String, BigDecimal> subGroupLevelClaimTaxSumMap) {
    assertEquals(accountingModel.getMrNo() != null ? accountingModel.getMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getMrNo()
            : null);
    assertEquals(accountingModel.getOldMrNo() != null ? accountingModel.getOldMrNo().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientDetails().getOldmrno()
            : null);
    assertEquals(accountingModel.getVisitId() != null ? accountingModel.getVisitId().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getPatientId()
            : null);
    assertEquals(accountingModel.getVisitType(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getVisitType()
            : null);

    if (storeSalesMainModel.getBillNo().getVisitId() !=null) {
      assertEquals(
          accountingModel.getAdmittingDoctor() != null ? accountingModel.getAdmittingDoctor().trim()
              : null,
          storeSalesMainModel.getBillNo().getVisitId().getDoctor() != null
              ? storeSalesMainModel.getBillNo().getVisitId().getDoctor().getDoctorName() : null);
      assertEquals(accountingModel.getAdmittingDepartment() != null
          ? accountingModel.getAdmittingDepartment().trim()
          : null, storeSalesMainModel.getBillNo().getVisitId().getDeptName() != null
          ? storeSalesMainModel.getBillNo().getVisitId().getDeptName().getDeptName() : null);
      }
      else {
        assertEquals(
            accountingModel.getAdmittingDoctor() !=null ? accountingModel.getAdmittingDoctor().trim()
                : null, null );
        assertEquals(
            accountingModel.getAdmittingDepartment() !=null ? accountingModel.getAdmittingDepartment().trim()
                : null, null );
      }
    assertEquals(
        accountingModel.getReferralDoctor() != null ? accountingModel.getReferralDoctor().trim()
            : null,
        null);
    assertEquals(accountingModel.getCenterId(),
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? Integer
                .valueOf(storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterId())
            : Integer.valueOf(storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterId()));
    assertEquals(
        accountingModel.getCenterName() != null ? accountingModel.getCenterName().trim() : null,
        storeSalesMainModel.getBillNo().getVisitId() != null
            ? storeSalesMainModel.getBillNo().getVisitId().getCenterId().getCenterName()
            : storeSalesMainModel.getBillNo().getStoreRetailCustomers()
                .getHospitalCenterMasterModel().getCenterName());
    assertEquals(
        accountingModel.getVoucherNo() != null ? accountingModel.getVoucherNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(
        accountingModel.getVoucherType() != null ? accountingModel.getVoucherType().trim() : null,
        accountingDataInsertRepo.getVoucherDefinition("VOUCHER_TYPE_PHBILLS"));
    assertEquals(
        accountingModel.getVoucherRef() != null ? accountingModel.getVoucherRef().trim() : null,
        null);
    assertEquals(accountingModel.getBillNo() != null ? accountingModel.getBillNo().trim() : null,
        storeSalesMainModel.getBillNo().getBillNo());
    assertEquals(accountingModel.getBillOpenDate(), storeSalesMainModel.getBillNo().getOpenDate());
    assertEquals(accountingModel.getBillFinalizedDate(),
        storeSalesMainModel.getBillNo().getFinalizedDate());
    assertEquals(accountingModel.getAuditControlNumber() != null
        ? accountingModel.getAuditControlNumber().trim()
        : null, storeSalesMainModel.getBillNo().getAuditControlNumber());
    assertEquals(accountingModel.getAccountGroup(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getAccountGroup()));
    assertEquals(accountingModel.getPointsRedeemed(),
        Integer.valueOf(storeSalesMainModel.getBillNo().getPointsRedeemed()));
    assertEquals(accountingModel.getPointsRedeemedRate(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPointsRedeemedAmount(),
        storeSalesMainModel.getBillNo().getPointsRedeemedAmt() != null
            ? storeSalesMainModel.getBillNo().getPointsRedeemedAmt()
            : BigDecimal.ZERO);
    assertEquals(accountingModel.getIsTpa(),
        storeSalesMainModel.getBillNo().getIsTpa() ? AccountingConstants.IS_TPA_Y
            : AccountingConstants.IS_TPA_N);
    assertEquals(accountingModel.getRemarks() != null ? accountingModel.getRemarks().trim() : null,
        null);

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
    assertEquals(accountingModel.getSalesVatPercent(), saleItemTax.getTaxRate());

    ChargeheadConstantsModel chargeHeadModel = getChargeHeadConstantsModel(storeSalesMainModel);
    assertEquals(
        accountingModel.getChargeGroup() != null ? accountingModel.getChargeGroup().trim() : null,
        (chargeHeadModel != null && chargeHeadModel.getChargeGroupModel() != null)
            ? chargeHeadModel.getChargeGroupModel().getChargegroupName()
            : null);
    assertEquals(
        accountingModel.getChargeHead() != null ? accountingModel.getChargeHead().trim() : null,
        chargeHeadModel != null ? chargeHeadModel.getChargeheadId() : null);
    assertEquals(accountingModel.getModTime(), storeSalesMainModel.getDateTime());
    assertEquals(accountingModel.getIssueStore(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getDeptName()
            : null);
    assertEquals(accountingModel.getIssueStoreCenter(),
        storeSalesMainModel.getStoreIdModel() != null
            ? storeSalesMainModel.getStoreIdModel().getCenterId().getCenterName()
            : null);
    assertEquals(accountingModel.getVoucherDate(), storeSalesMainModel.getSaleDate());

    assertEquals(
        accountingModel.getItemCode() != null ? accountingModel.getItemCode().trim() : null,
        String.valueOf(saleItem.getMedicineId()));
    assertEquals(
        accountingModel.getItemName() != null ? accountingModel.getItemName().trim() : null,
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getMedicineName()
            : null);
    assertEquals(
        accountingModel.getServiceGroup() != null ? accountingModel.getServiceGroup().trim() : null,
        getServiceGroupName(saleItem));
    assertEquals(
        accountingModel.getServiceSubGroup() != null ? accountingModel.getServiceSubGroup().trim()
            : null,
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getServiceSubGroupId() != null)
                ? saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceSubGroupName()
                : null);
    assertEquals(accountingModel.getQuantity(), saleItem.getQuantity());
    assertEquals(accountingModel.getUnit() != null ? accountingModel.getUnit().trim() : null, null);
    assertEquals(accountingModel.getDiscountAmount(), BigDecimal.ZERO);
    assertEquals(accountingModel.getPrescribingDoctor() != null
        ? accountingModel.getPrescribingDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getPrescribingDoctorDeptName() != null
        ? accountingModel.getPrescribingDoctorDeptName().trim()
        : null, null);
    assertEquals(accountingModel.getConductiongDoctor() != null
        ? accountingModel.getConductiongDoctor().trim()
        : null, null);
    assertEquals(accountingModel.getConductingDepartment() != null
        ? accountingModel.getConductingDepartment().trim()
        : null, null);
    assertEquals(
        accountingModel.getPayeeDoctor() != null ? accountingModel.getPayeeDoctor().trim() : null,
        null);
    assertEquals(accountingModel.getCounterNo(), null);
    assertEquals(accountingModel.getItemCategoryId(),
        (saleItem.getStoreItemDetails() != null
            && saleItem.getStoreItemDetails().getStoreCategoryMaster() != null)
                ? saleItem.getStoreItemDetails().getStoreCategoryMaster().getCategoryId()
                : null);
    assertEquals(accountingModel.getReceiptStore(), null);
    assertEquals(accountingModel.getReceiptStoreCenter(), null);
    assertEquals(accountingModel.getCurrency(), null);
    assertEquals(accountingModel.getOuthouseName(), null);
    assertEquals(accountingModel.getIncoimngHospital(), null);
    assertEquals(accountingModel.getCustItemCode(),
        saleItem.getStoreItemDetails() != null ? saleItem.getStoreItemDetails().getCustItemCode()
            : null);
    assertEquals(accountingModel.getCostAmount(), saleItem.getCostValue());

    String saleItemId = String.valueOf(saleItemTax.getId().getSaleItemId());
    String itemSubGroupId = String.valueOf(saleItemTax.getItemSubGroupsModel() != null
        ? saleItemTax.getItemSubGroupsModel().getItemSubgroupId()
        : null);
    String key = saleItemId
        + "#"
        + itemSubGroupId;
    BigDecimal claimTaxSumPerSubgroup = BigDecimal.ZERO;
    if (subGroupLevelClaimTaxSumMap.containsKey(key)) {
      BigDecimal claimTaxSum = subGroupLevelClaimTaxSumMap.get(key);
      claimTaxSumPerSubgroup = claimTaxSum != null ? claimTaxSum : BigDecimal.ZERO;
    }
    BigDecimal patTaxAmount = (saleItemTax.getTaxAmt() != null ? saleItemTax.getTaxAmt()
        : BigDecimal.ZERO).subtract(claimTaxSumPerSubgroup);

    assertEquals(accountingModel.getGrossAmount(),
        patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount : patTaxAmount.negate());
    assertEquals(accountingModel.getNetAmount(),
        patTaxAmount.compareTo(BigDecimal.ZERO) >= 0 ? patTaxAmount : patTaxAmount.negate());
    assertEquals(accountingModel.getTransactionType(), AccountingConstants.TRANSACTION_TYPE_N);

    String debitAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC");
    String creditAccount = (patTaxAmount.compareTo(BigDecimal.ZERO) >= 0
        && !String.valueOf(storeSalesMainModel.getType()).equals(AccountingConstants.SALE_TYPE_R))
            ? accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_TAX_LIABILITY_ACC")
            : accountingDataInsertRepo.getLedgerDefinition("ACCOUNT_TYPE_COUNTER_RECEIPTS");

    assertEquals(accountingModel.getDebitAccount(), debitAccount);
    assertEquals(accountingModel.getCreditAccount(), creditAccount);

    assertEquals(accountingModel.getCustom1(), patTaxAmount.toString());
    assertEquals(accountingModel.getCustom2(), null);
    assertEquals(accountingModel.getCustom3(), null);
    assertEquals(accountingModel.getCustom4(),
        saleItemTax.getItemSubGroupsModel() != null
            ? saleItemTax.getItemSubGroupsModel().getItemSubgroupName()
            : null);
  }

  private void assertPatientEntryNonInsuranceBillRetailSale(HmsAccountingInfoModel accountingModel,
      StoreSalesMainModel storeSalesMainModel) {
    assertPatientEntryNonInsuranceBill(accountingModel, storeSalesMainModel);
  }

  private void assertPatientTaxEntryNonInsuranceBillRetailSale(
      HmsAccountingInfoModel accountingModel, StoreSalesMainModel storeSalesMainModel,
      StoreSalesTaxDetailsModel saleItemTax) {
    assertPatientTaxEntryNonInsuranceBill(accountingModel, storeSalesMainModel, saleItemTax);
  }

  private String getSaleItemAccountHeadName(StoreSalesDetailsModel saleItem,
      StoreSalesMainModel storeSalesMainModel) {
    if (saleItem.getStoreItemDetails() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId().getAccountHeadId() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId().getAccountHeadId()
            .getAccountHeadName() != null) {
      return saleItem.getStoreItemDetails().getServiceSubGroupId().getAccountHeadId()
          .getAccountHeadName();
    } else if (getChargeHeadConstantsModel(storeSalesMainModel) != null
        && getChargeHeadConstantsModel(storeSalesMainModel).getAccountHeadId() != null) {
      return getChargeHeadConstantsModel(storeSalesMainModel).getAccountHeadId()
          .getAccountHeadName();
    }
    return null;
  }

  private Map<Integer, BigDecimal> getClaimAmountSumMap(SalesClaimDetailsModel saleClaim) {
    Map<Integer, BigDecimal> claimAmountSumMap = new HashMap<>();
    if (claimAmountSumMap.containsKey(saleClaim.getSaleItemId().getSaleItemId())) {
      BigDecimal claimSum = claimAmountSumMap.get(saleClaim.getSaleItemId().getSaleItemId());
      claimSum = claimSum != null ? claimSum : BigDecimal.ZERO;
      claimSum = claimSum
          .add(saleClaim.getInsuranceClaimAmt() != null ? saleClaim.getInsuranceClaimAmt()
              : BigDecimal.ZERO);
      claimAmountSumMap.put(saleClaim.getSaleItemId().getSaleItemId(), claimSum);
    } else {
      claimAmountSumMap.put(saleClaim.getSaleItemId().getSaleItemId(),
          saleClaim.getInsuranceClaimAmt() != null ? saleClaim.getInsuranceClaimAmt()
              : BigDecimal.ZERO);
    }
    return claimAmountSumMap;
  }

  private Map<String, BigDecimal> getSubGroupLevelClaimTaxSumMap(
      Set<SalesClaimDetailsModel> salesClaims) {
    Map<String, BigDecimal> subGroupLevelClaimTaxSumMap = new HashMap<>();
    for (SalesClaimDetailsModel saleClaim : salesClaims) {
      Set<SalesClaimTaxDetailsModel> saleClaimTaxes = saleClaim.getSalesClaimTaxDetails();
      for (SalesClaimTaxDetailsModel saleClaimTax : saleClaimTaxes) {
        String saleItemId = String.valueOf(saleClaimTax.getId().getSaleItemId());
        String itemSubGroupId = String.valueOf(saleClaimTax.getItemSubGroupsModel() != null
            ? saleClaimTax.getItemSubGroupsModel().getItemSubgroupId()
            : null);
        if (saleItemId != null && itemSubGroupId != null) {
          String key = saleItemId
              + "#"
              + itemSubGroupId;
          if (subGroupLevelClaimTaxSumMap.containsKey(key)) {
            BigDecimal claimTaxSum = subGroupLevelClaimTaxSumMap.get(key);
            claimTaxSum = claimTaxSum.add(saleClaimTax.getTaxAmt());
            subGroupLevelClaimTaxSumMap.put(key, claimTaxSum);
          } else {
            subGroupLevelClaimTaxSumMap.put(key, saleClaimTax.getTaxAmt());
          }
        }
      }
    }
    return subGroupLevelClaimTaxSumMap;
  }

  private String getServiceGroupName(StoreSalesDetailsModel saleItem) {
    if (saleItem.getStoreItemDetails() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId() != null
        && saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceGroupId() != null) {
      return saleItem.getStoreItemDetails().getServiceSubGroupId().getServiceGroupId()
          .getServiceGroupName();
    }
    return null;
  }

  private BigDecimal getSaleItemAmount(StoreSalesDetailsModel saleItem) {
    BigDecimal amount = saleItem.getAmount() != null ? saleItem.getAmount() : BigDecimal.ZERO;
    BigDecimal taxAmount = saleItem.getTax() != null ? saleItem.getTax() : BigDecimal.ZERO;
    return amount.subtract(taxAmount);
  }

  private ChargeheadConstantsModel getChargeHeadConstantsModel(
      StoreSalesMainModel storeSalesMainModel) {
    String billType = String.valueOf(
        storeSalesMainModel.getBillNo() != null ? storeSalesMainModel.getBillNo().getBillType()
            : null);
    String saleType = String.valueOf(storeSalesMainModel.getType());

    if (billType.equals("P") && saleType.equals(AccountingConstants.SALE_TYPE_S)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHMED);
    } else if (billType.equals("P") && saleType.equals(AccountingConstants.SALE_TYPE_R)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHRET);
    } else if (billType.equals("C") && saleType.equals(AccountingConstants.SALE_TYPE_S)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHCMED);
    } else if (billType.equals("C") && saleType.equals(AccountingConstants.SALE_TYPE_R)) {
      return pharmacyAccountingDataInsertRepo
          .getChargeHeadConstantsModel(AccountingConstants.PHARMACY_CHARGEHEAD_PHCRET);
    } else {
      return pharmacyAccountingDataInsertRepo.getChargeHeadConstantsModel(
          AccountingConstants.PHARMACY_CHARGEHEAD_HOSPITAL_OR_ISSUE_ITEM);
    }
  }
}
