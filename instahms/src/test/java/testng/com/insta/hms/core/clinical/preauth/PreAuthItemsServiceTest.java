package testng.com.insta.hms.core.clinical.preauth;

import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionActivitiesRepository;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService.PreAuthItemType;

import testng.utils.TestRepoInit;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class PreAuthItemsServiceTest extends AbstractTestNGSpringContextTests {

	private static final int TEST_QUANTITY_FOR_UPDATE_PREAUTH_ITEMS = 1;

	private static final String TEST_PREAUTH_ACT_STATUS = "C";

	private static final String TEST_PREAUTH_NUMBER = "h1";

	private static final int PREAUTH_ACT_ID_FOR_TEST = 16;

	private static final String CHARGE_ID_FOR_TEST = "CH259729";

	private Logger logger = LoggerFactory.getLogger(PreAuthItemsServiceTest.class);

	@InjectMocks
	private PreAuthItemsService preAuthItemsService;

	@Spy
	private EAuthPrescriptionActivitiesRepository prescriptionActivitiesRepo;

	/** The db data map. */
	private Map<String, Object> dbDataMap = null;

	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		TestRepoInit testRepo = new TestRepoInit();
		testRepo.insert("orderable_item");
		testRepo.insert("preauth_prescription");
		testRepo.insert("preauth_prescription_activities");
		testRepo.insert("patient_registration");
		testRepo.insert("bill_charge");
		dbDataMap = testRepo.initializeRepo();
		logger.info("Repository Initialization successful");
	}

	@Test
	public void updatePreAuthItemQuantity() {
		Map<String, String> preAuthInitDbMap = getPreAuthItemFromInitRepoMap();
		assertTrue(MapUtils.isNotEmpty(preAuthInitDbMap),
				"PreAuthItem Map is Empty from init DB for preauth_act_id " + PREAUTH_ACT_ID_FOR_TEST);
		checkNewItemQuantityUpdate(preAuthInitDbMap);
		getCancelledItemQuantityUpdate(preAuthInitDbMap);
	}

	private void getCancelledItemQuantityUpdate(Map<String, String> preAuthInitDbMap) {
		preAuthItemsService.updatePreAuthItemQuantity(CHARGE_ID_FOR_TEST, TEST_QUANTITY_FOR_UPDATE_PREAUTH_ITEMS, true, false,TEST_PREAUTH_ACT_STATUS);
		BasicDynaBean preAuthItemEntry = prescriptionActivitiesRepo.findByKey("preauth_act_id",
				PREAUTH_ACT_ID_FOR_TEST);
		assertTrue(preAuthItemEntry != null);
		assertTrue(
				((Integer) preAuthItemEntry.get("rem_qty")).equals(Integer.parseInt(preAuthInitDbMap.get("rem_qty"))),
				"Remaining quantity update Test failed for new preauth item cancelled");
	}

	private void checkNewItemQuantityUpdate(Map<String, String> preAuthInitDbMap) {
		preAuthItemsService.updatePreAuthItemQuantity(CHARGE_ID_FOR_TEST, TEST_QUANTITY_FOR_UPDATE_PREAUTH_ITEMS, false, false,TEST_PREAUTH_ACT_STATUS);
		BasicDynaBean preAuthItemEntry = prescriptionActivitiesRepo.findByKey("preauth_act_id",
				PREAUTH_ACT_ID_FOR_TEST);
		assertTrue(preAuthItemEntry != null);
		assertTrue(
				((Integer) preAuthItemEntry.get("rem_qty")).equals(
						Integer.parseInt(preAuthInitDbMap.get("rem_qty")) - TEST_QUANTITY_FOR_UPDATE_PREAUTH_ITEMS),
				"Remaining quantity update Test failed for new preauth item insert");
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getPreAuthItemFromInitRepoMap() {
		List<Map<String, String>> initValues = (List<Map<String, String>>) dbDataMap
				.get("preauth_prescription_activities");
		assertTrue(CollectionUtils.isNotEmpty(initValues), "Initializing values in db is unsuccessful");
		for (Map<String, String> initValue : initValues) {
			for (Entry<String, String> preAuthItem : initValue.entrySet())
				if (preAuthItem.getKey().equals("preauth_act_id")
						&& String.valueOf(PREAUTH_ACT_ID_FOR_TEST).equals(preAuthItem.getValue())) {
					return initValue;
				}
		}
		return null;
	}

}
