package testng.com.insta.hms.core.allocation;

import static org.mockito.MockitoAnnotations.initMocks;

import com.insta.hms.core.billing.AllocationRepository;
import com.insta.hms.core.billing.AllocationService;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import testng.utils.TestRepoInit;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class AllocationServiceTest extends AbstractTransactionalTestNGSpringContextTests {
  private Logger logger = LoggerFactory.getLogger(AllocationServiceTest.class);
  
  @Autowired
  AllocationService allocationService;
  
  @Autowired
  AllocationRepository allocationRepository;
  
  private TestRepoInit testRepo;
  
  
  private Map<String, Object> dbDataMap = null;

  @BeforeMethod
  public void mockData() {
    initMocks(this);
    testRepo = new TestRepoInit(allocationRepository);
    testRepo.insert("bill");
    testRepo.insert("bill_charge");
    testRepo.insert("bill_charge_claim");
    testRepo.insert("patient_registration");
    testRepo.insert("patient_details");
    testRepo.insert("receipts");
    testRepo.insert("receipt_usage");
    testRepo.insert("bill_receipts");
    testRepo.insert("modules_activated");
    dbDataMap = testRepo.initializeRepo();
    logger.info("Before Method of AllocationServiceTest");
  }
  
  @AfterMethod
  public void removeData() {
    logger.info("Removing the allocation data");
    testRepo.rollbackTransaction();
  }

  @Test
  public void allocate() {
    List resultList = allocationService.allocate("BC17000271");
    Assert.assertEquals(2, resultList.size());
  }

}
