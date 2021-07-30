package testng.com.insta.hms.mdm.patientsearch;

import com.insta.hms.core.patient.outpatientlist.PatientSearchService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class PatientSearchServiceTest extends
               AbstractTransactionalTestNGSpringContextTests {
	
	private Logger logger = LoggerFactory.getLogger(PatientSearchServiceTest.class);
	   @Autowired
       private PatientSearchService patientSearchService;
	  
	   @BeforeMethod
		public void mockData() {
//		   MockitoAnnotations.initMocks(this);
//			logger.info("Before every patientSearchServiceTest");
//			DatabaseHelper.delete("DELETE FROM patient_details");
//			DatabaseHelper.delete("DROP extension IF EXISTS fuzzystrmatch;");
//			DatabaseHelper.insert("CREATE EXTENSION fuzzystrmatch", new Object[]{});
//			String INSERTPATIENTDETAILS ="INSERT INTO patient_details (mr_no, patient_name, patient_gender,  patient_phone,salutation,middle_name,"
//					+ " last_name, patient_address, patient_city, patient_category_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
//			DatabaseHelper.insert(INSERTPATIENTDETAILS, new Object[] {"MR001", "Janaki", "F", "+919245536902", "SALU0001","v", "g"," ", "CT0017",1});
		}
       @Test
       public void findPatientMatch() {
//    	   logger.info("find patient name match");
//    	   List<BasicDynaBean> patientNameList = null;
//    	   patientNameList = patientSearchService.findPatientMatch("Janaki v g", "+919245536902", true, 2);
//    	   List<String> mrNo = new ArrayList<String>();
//    	   mrNo.add("MR001");
//           List<String> patientName = new ArrayList<String>();
//           patientName.add("Janaki");
//           List<String> middleName = new ArrayList<String>();
//           middleName.add("v");
//           List<String> lastName = new ArrayList<String>();
//           lastName.add("g");
//           
//           Assert.assertEquals(getStringAttribute("mr_no", patientNameList), mrNo);
//           Assert.assertEquals(getStringAttribute("patient_name", patientNameList), patientName);
//           Assert.assertEquals(getStringAttribute("middle_name", patientNameList), middleName);
//           Assert.assertEquals(getStringAttribute("last_name", patientNameList), lastName);
                      
       }
       public List<String> getStringAttribute(String attribute, List<BasicDynaBean> listBean) {
   		List<String> listAttribute = new ArrayList<String>();
   		for(BasicDynaBean bean : listBean) {
   			listAttribute.add((String) bean.get(attribute));
   		}
   		return listAttribute;
   	}

}
