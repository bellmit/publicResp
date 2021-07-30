package testng.com.insta.hms.core.billing.resetbilltotals;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillService;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.Test;

@Test
@ContextConfiguration(locations = {"classpath:spring/test-spring-config.xml"})
public class ResetBillTotalsTest extends AbstractTransactionalTestNGSpringContextTests{

	@LazyAutowired
	private BillService billService;
	
	@Test
	public void TestForResetTotals(){
		billService.resetServiceTaxClaimCharge("BP026775","manci");
	}
	
}
