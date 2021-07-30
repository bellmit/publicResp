package testng.com.insta.hms.mdm.services;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.services.ServicesRepository;
import com.insta.hms.mdm.services.ServicesService;
import testng.com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsServiceTest;

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
public class ServicesServiceTest extends
		AbstractTransactionalTestNGSpringContextTests {

	private Logger logger = LoggerFactory.getLogger(PrescriptionsServiceTest.class);

	@Spy
	private ServicesRepository testingRepo;

	@InjectMocks
	private ServicesService testingService;

	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		logger.info("Before every Services Test");
	}

	@Test
	public void getServicesForPrescriptionTest_NoRecords() {
		DatabaseHelper.delete("DELETE FROM service_master_charges");
		DatabaseHelper.delete("DELETE FROM service_org_details");
		DatabaseHelper.delete("DELETE FROM services");
		List<Map<String, Object>> records = testingService
				.getServicesForPrescription("GENERAL", "ORG001", "o", 1, "test", 20);
		Assert.assertEquals(records.isEmpty(), true);
	}

	@Test
	public void getServicesForPrescriptionTest_withRecords() {
		DatabaseHelper.delete("DELETE FROM service_master_charges");
		DatabaseHelper.delete("DELETE FROM service_org_details");
		DatabaseHelper.delete("DELETE FROM services");
		DatabaseHelper.delete("DELETE FROM services_departments");
		DatabaseHelper.delete("DELETE FROM insurance_plan_details");
		DatabaseHelper.delete("DELETE FROM item_insurance_categories");
		DatabaseHelper.delete("DELETE FROM dyna_package_org_details");
		DatabaseHelper.delete("DELETE FROM rate_plan_parameters");
		DatabaseHelper.delete("DELETE FROM test_org_details");
		DatabaseHelper.delete("DELETE FROM operation_org_details");
		DatabaseHelper.delete("DELETE FROM organization_details");
		String od_q = "INSERT INTO organization_details (org_id) VALUES (?)";
		String sd_q = "INSERT INTO services_departments (department, serv_dept_id, dept_type_id) "
				+ "	VALUES (?,?,?)";
		String s_q = "INSERT INTO services (service_id, service_name, status, serv_dept_id, insurance_category_id, service_duration) "
				+ "VALUES (?, ?, ?, ?, ?,?)";
		String smc_q = "INSERT INTO service_master_charges (service_id, bed_type, org_id, unit_charge) "
				+ "	VALUES (?, ?, ?, ?)";
		String iic_q = "INSERT INTO item_insurance_categories (insurance_category_id,"
				+ "	insurance_category_name) VALUES (?,?)";
		String ood_q = "INSERT INTO service_org_details (service_id, org_id, applicable) VALUES (?,?,?)";
		DatabaseHelper.insert(od_q, new Object[]{"1"});
		DatabaseHelper.insert(sd_q, new Object[] { "DEPT", 1, "DEP001" });
		DatabaseHelper.insert(s_q, new Object[] { "SER001", "Service", "A", 1,
				1,15 });
		DatabaseHelper.insert(smc_q, new Object[] { "SER001", "GENERAL", "1",
				100 });
		DatabaseHelper.insert(iic_q, new Object[] { 1, "TEST" });
		DatabaseHelper.insert(ood_q, new Object[]{"SER001", "1", true});
		List<Map<String, Object>> records = testingService
				.getServicesForPrescription("GENERAL", "1", "o", 1, "Service", 20);
		Assert.assertEquals(records.isEmpty(), false);
	}

}
