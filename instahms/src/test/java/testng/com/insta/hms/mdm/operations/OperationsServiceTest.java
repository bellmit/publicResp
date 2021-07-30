package testng.com.insta.hms.mdm.operations;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.operations.OperationsRepository;
import com.insta.hms.mdm.operations.OperationsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class OperationsServiceTest extends
		AbstractTransactionalTestNGSpringContextTests {

	private Logger logger = LoggerFactory.getLogger(OperationsServiceTest.class);

	@Spy
	private OperationsRepository testingRepo;

	@InjectMocks
	private OperationsService testingService;

	@BeforeMethod
	public void logInfo() {
		MockitoAnnotations.initMocks(this);
		logger.info("Before every OperationsMasterTest");
	}

	@Test
	public void getOperationsForPrescriptionTest_withRecords() {
		DatabaseHelper.delete("DELETE FROM operation_charges");
		DatabaseHelper.delete("DELETE FROM operation_org_details");
		DatabaseHelper.delete("DELETE FROM operation_master");
		DatabaseHelper.delete("DELETE FROM item_insurance_categories");
		DatabaseHelper.delete("DELETE FROM insurance_plan_details");
		DatabaseHelper.delete("DELETE FROM dyna_package_org_details");
		DatabaseHelper.delete("DELETE FROM rate_plan_parameters");
		DatabaseHelper.delete("DELETE FROM service_org_details");
		DatabaseHelper.delete("DELETE FROM test_org_details");
		DatabaseHelper.delete("DELETE FROM organization_details");
		String om_q = "INSERT INTO operation_master (op_id, operation_name, operation_code, status,"
				+ "	insurance_category_id,operation_duration) VALUES (?,?,?,?,?,?)";
		String oc_q = "INSERT INTO operation_charges (op_id, org_id, bed_type, surg_asstance_charge,"
				+ "	surgeon_charge, anesthetist_charge) VALUES (?,?,?,?,?,?)";
		String iic_q = "INSERT INTO item_insurance_categories (insurance_category_id,"
				+ "	insurance_category_name) VALUES (?,?)";
		String od_q = "INSERT INTO organization_details (org_id) VALUES (?)";
		String ood_q = "INSERT INTO operation_org_details (operation_id, org_id, applicable) VALUES (?,?,?)";

		DatabaseHelper.insert(od_q, new Object[]{"1"});
		DatabaseHelper.insert(om_q, new Object[] { "OPR001", "Test", "OP245",
				"A", 1 ,30 });
		DatabaseHelper.insert(oc_q, new Object[] { "OPR001", "1", "GENERAL",
				100, 200, 300 });
		DatabaseHelper.insert(iic_q, new Object[] { 1, "TEST" });
		DatabaseHelper.insert(ood_q, new Object[]{"OPR001", "1", true});
		List<Map<String, Object>> records = testingService
				.getOperationsForPrescription("GENERAL", "1", "o", 1, "test", 20);
		Assert.assertEquals(records.size(), 1);
	}

	@Test
	public void getOperationsForPrescriptionTest_NoRecords() {
		DatabaseHelper.delete("DELETE FROM operation_charges");
		DatabaseHelper.delete("DELETE FROM operation_org_details");
		DatabaseHelper.delete("DELETE FROM operation_master");
		DatabaseHelper.delete("DELETE FROM item_insurance_categories");
		DatabaseHelper.delete("DELETE FROM insurance_plan_details");
		List<Map<String, Object>> records = testingService
				.getOperationsForPrescription("GENERAL", "1", "o", 1, "test", 20);
		Assert.assertEquals(records.isEmpty(), true);
	}
}
