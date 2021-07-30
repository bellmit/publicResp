package testng.com.insta.hms.mdm.doctors;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.mdm.doctors.DoctorService;

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
public class DoctorServiceTest extends AbstractTransactionalTestNGSpringContextTests{

	private Logger logger = LoggerFactory.getLogger(DoctorServiceTest.class);

	@Spy
	private DoctorRepository testingRepo;

	@InjectMocks
	private DoctorService testingService;

	@BeforeMethod
	public void intiMocks() {
		MockitoAnnotations.initMocks(this);
		logger.info("Before every DoctorServiceTest...");
	}

	@Test
	public void getDoctorsForPrescription_NoRecords() {
		DatabaseHelper.delete("DELETE FROM doctor_consultation_charge");
		DatabaseHelper.delete("DELETE FROM doctors");
		List<Map<String, Object>> records = testingService
				.getDoctorsForPrescription("GENERAL", "1", "o", 1, "test", 20);
		Assert.assertEquals(records.isEmpty(), true);
	}

}
