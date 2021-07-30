package testng.com.insta.hms.core.masterdata;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.masterdata.MasterdataService;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;
import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class MasterdataServiceTest extends
		AbstractTransactionalTestNGSpringContextTests {

	private Logger logger = LoggerFactory.getLogger(MasterdataServiceTest.class);

	@LazyAutowired
	private MasterdataService masterdataService;
	@LazyAutowired
	private DoctorService doctorService;
	@LazyAutowired
	private DepartmentService departmentService;
	@LazyAutowired
	private CenterService centerService;
	@LazyAutowired
	private MessageUtil messageUtil;
	
	@BeforeMethod
	public void mockData() {
		logger.info("Before every MasterdataServiceTest");
		
		DatabaseHelper.delete("DELETE FROM center_integration_details");
		DatabaseHelper.delete("DELETE FROM hosp_bill_audit_seq_prefs");
		DatabaseHelper.delete("DELETE FROM hosp_bill_seq_prefs");
		DatabaseHelper.delete("DELETE FROM hosp_item_seq_prefs");
		DatabaseHelper.delete("DELETE FROM hosp_op_ip_seq_prefs");
		DatabaseHelper.delete("DELETE FROM hosp_receipt_seq_prefs");
		DatabaseHelper.delete("DELETE FROM user_center_billing_counters");
		DatabaseHelper.delete("DELETE FROM hospital_center_master");
		DatabaseHelper.delete("DELETE FROM registration_preferences");
		DatabaseHelper.delete("DELETE FROM department");
		DatabaseHelper.delete("DELETE FROM doctor_consultation_charge");
		DatabaseHelper.delete("DELETE FROM patient_appointment_plan");
		DatabaseHelper.delete("DELETE FROM doctors");
		DatabaseHelper.delete("DELETE FROM doctor_center_master");
		
		String INSERTCENTER = "insert into hospital_center_master "
				+ " (center_id,center_name,center_code,status)"
				+ " values (?,?,?,?)";
		DatabaseHelper.insert(INSERTCENTER, new Object[] { 1,
				"active center", "001", "A" });
		DatabaseHelper.insert(INSERTCENTER, new Object[] { 2,
				"inactive center", "002", "I" });
		
		String INSERTDEPT = "insert into department (dept_id,dept_name,status)"
				+ " values (?,?,?)";
		DatabaseHelper.insert(INSERTDEPT, new Object[] { "DEP_1",
				"active dept","A" });
		DatabaseHelper.insert(INSERTDEPT, new Object[] { "DEP_2",
				"inactive dept","I" });
		
		String INSERTDOCTOR = "insert into doctors (doctor_id,doctor_name,doctor_type"
				+ ",status,overbook_limit,schedule,dept_id) values (?,?,?,?,?,?,?)";
		DatabaseHelper.insert(INSERTDOCTOR, new Object[] { "DOC00001",
				"doctor", "HOSPITAL", "A",3,true,"DEP_1" });
		DatabaseHelper.insert(INSERTDOCTOR, new Object[] { "DOC00002",
				"inactive doctor", "HOSPITAL", "I",3,true,"DEP_1" });
		
		String INSERTDOCTORCENTER = "insert into doctor_center_master (doc_center_id,doctor_id,center_id"
				+ ",status) values (?,?,?,?)";
		DatabaseHelper.insert(INSERTDOCTORCENTER, new Object[] { 1,
				"DOC00001", 1, "A" });
		DatabaseHelper.insert(INSERTDOCTORCENTER, new Object[] { 2,
				"DOC00002", 1, "A" });

	}

	@Test
	public void getAllMasterdata() {
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("status", new String[] {"all"});
		 Map<String, Object> response= masterdataService.getMasterData(params);
		 List<Map<String, Object>> centers =(List<Map<String, Object>>) response.get("hospital_center_master");
		 List<Map<String, Object>> depts =(List<Map<String, Object>>) response.get("hospital_departments");
		 List<Map<String, Object>> docs =(List<Map<String, Object>>) response.get("hospital_doctors");
		 int centerCount=centers.size();
		 int deptCount=depts.size();
		 int docsCount=docs.size();
		 Assert.assertEquals(centerCount,2);
		 Assert.assertEquals(deptCount,2);
		 Assert.assertEquals(docsCount,2);
	}
	
	@Test
	public void getActiveMasterdata() {
		Map<String, String[]> params = new HashMap<String, String[]>();
		 Map<String, Object> response= masterdataService.getMasterData(null);
		 List<Map<String, Object>> centers =(List<Map<String, Object>>) response.get("hospital_center_master");
		 List<Map<String, Object>> depts =(List<Map<String, Object>>) response.get("hospital_departments");
		 List<Map<String, Object>> docs =(List<Map<String, Object>>) response.get("hospital_doctors");
		 int centerCount=centers.size();
		 int deptCount=depts.size();
		 int docsCount=docs.size();
		 Assert.assertEquals(centerCount,1);
		 Assert.assertEquals(deptCount,1);
		 Assert.assertEquals(docsCount,1);
	}
	
	@Test(expectedExceptions = ValidationException.class)
	public void getValidationException() {
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("status", new String[] {"al"});
		 Map<String, Object> response= masterdataService.getMasterData(params);
	}

}