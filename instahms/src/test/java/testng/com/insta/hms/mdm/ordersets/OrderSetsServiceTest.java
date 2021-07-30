package testng.com.insta.hms.mdm.ordersets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.mdm.ordersets.CenterPackageApplicabilityRepository;
import com.insta.hms.mdm.ordersets.DeptPackageApplicabilityRepository;
import com.insta.hms.mdm.ordersets.OrderSetsService;
import com.insta.hms.mdm.ordersets.OrderSetsValidator;
import com.insta.hms.mdm.ordersets.PackageContentsRepository;
import com.insta.hms.mdm.ordersets.PackagesRepository;
import com.insta.hms.mdm.ordersets.TpaPackageApplicabilityRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.ui.ModelMap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class OrderSetsServiceTest extends AbstractTransactionalTestNGSpringContextTests {

	private Logger logger = LoggerFactory.getLogger(OrderSetsServiceTest.class);

	private static final String FILENAME = "MockData.json";

	@Spy
	private PackagesRepository packagesRepository;

	@Spy
	private PackageContentsRepository packageContentsRepo;

	@Spy
	private CenterPackageApplicabilityRepository centerPackApplicabilityRepo;

	@Spy
	private TpaPackageApplicabilityRepository tpaPackApplicabilityRepo;

	@Spy
	private DeptPackageApplicabilityRepository deptPackApplicabilityRepo;

	@Spy
	private OrderSetsValidator orderSetsValidator;

	@Mock
	private SessionService sessionService;

	@InjectMocks
	private OrderSetsService orderSetsService;
	
	private String mockOrderableItemsData;

	private List<ModelMap> mockJsonData;

	private List<ModelMap> dataProviderInsertJson;

	private List<ModelMap> dataProviderUpdateJson;

	private ModelMap quantityRuleJson;
	
	private ModelMap badRequestJson;
	
	private ModelMap entityNotFoundJson;

	@BeforeTest
	public void stringToJsonData() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		String jsonString = FileUtils.readFileToString(new File(classLoader.getResource(FILENAME)
				.getFile()), "UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(jsonString,
				new TypeReference<Map<String, List<ModelMap>>>() {
				});
		this.mockOrderableItemsData = "INSERT INTO orderable_item (entity,entity_id,item_name) VALUES "
				+ " ('Service','1','item 1'), ('Radiology','2','item 2'), ('Equipment','3','item 3') ; ";
		this.mockJsonData = (List<ModelMap>) map.get("mockData");
		this.dataProviderInsertJson = (List<ModelMap>) map.get("dataProviderInsert");
		this.dataProviderUpdateJson = (List<ModelMap>) map.get("dataProviderUpdate");
		this.quantityRuleJson = ((List<ModelMap>) map.get("quantityRule")).get(0);
		this.badRequestJson = ((List<ModelMap>) map.get("BadRequest")).get(0);
		this.entityNotFoundJson = ((List<ModelMap>) map.get("entityNotFound")).get(0);
	}

	@BeforeMethod
	public void mockData() {
		MockitoAnnotations.initMocks(this);
		DatabaseHelper.insert(this.mockOrderableItemsData, new Object[]{});
		packagesRepository.deleteAllRecords();
		packageContentsRepo.deleteAllRecords();
		centerPackApplicabilityRepo.deleteAllRecords();
		tpaPackApplicabilityRepo.deleteAllRecords();
		deptPackApplicabilityRepo.deleteAllRecords();
		Mockito.when(sessionService.getSessionAttributes()).thenReturn(
				getSessionServiceAttributes());
		for (ModelMap item : mockJsonData) {
			BasicDynaBean parentBean = orderSetsService.toBean(item);
			Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = orderSetsService
					.toBeansMap(item, parentBean);
			orderSetsService.insertDetailsMap(parentBean, detailMapBean);
		}
	}

	@Test
	public void searchOrderSets() {
		logger.info("search Test");
		PagedList orderSetsSearchList = orderSetsService.search(Collections.EMPTY_MAP);
		Assert.assertEquals(orderSetsSearchList.getTotalRecords(), 2);
	}

	@Test(dataProvider = "insertData")
	public void insertOrderSets(ModelMap jsonRequest) {
		logger.info("insert Test");
		BasicDynaBean parentBean = orderSetsService.toBean(jsonRequest);
		Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = orderSetsService
				.toBeansMap(jsonRequest, parentBean);
		orderSetsService.insertDetailsMap(parentBean, detailMapBean);

		BasicDynaBean bean = orderSetsService.findByUniqueName(
				(String) jsonRequest.get("package_name"), "package_name");
		Assert.assertNotNull(bean);

		Integer packageId = (Integer) bean.get("package_id");

		List<BasicDynaBean> packContents = packageContentsRepo.listAll(null, "package_id",
				packageId);

		Assert.assertEquals(packContents.size(),
				((List) jsonRequest.get("package_contents")).size());

		List<BasicDynaBean> centerPackMappings = centerPackApplicabilityRepo.listAll(null,
				"package_id", packageId);
		Assert.assertEquals(centerPackMappings.size(),
				((List) jsonRequest.get("center_package_applicability")).size());

		List<BasicDynaBean> deptPackMappings = deptPackApplicabilityRepo.listAll(null,
				"package_id", packageId);
		Assert.assertEquals(deptPackMappings.size(),
				((List) jsonRequest.get("dept_package_applicability")).size());

		List<BasicDynaBean> tpaPackMappings = tpaPackApplicabilityRepo.listAll(null, "package_id",
				packageId);
		Assert.assertEquals(tpaPackMappings.size(),
				((List) jsonRequest.get("tpa_package_applicability")).size());
	}

	@Test(dataProvider = "updateData")
	public void updateOrderSets(ModelMap insertJsonRequest, ModelMap updateJsonRequest) {
		logger.info("update Test");
		BasicDynaBean parentBean = orderSetsService.toBean(insertJsonRequest);
		Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = orderSetsService
				.toBeansMap(insertJsonRequest, parentBean);
		orderSetsService.insertDetailsMap(parentBean, detailMapBean);

		/* Set foriegn and primary keys in update Json Request. */
		BasicDynaBean bean = orderSetsService.findByUniqueName(
				(String) insertJsonRequest.get("package_name"), "package_name");

		Integer packageId = (Integer) bean.get("package_id");

		updateJsonRequest.put("package_id", packageId);

		Map<String, String[]> requestMap = new HashMap<String, String[]>();
		requestMap.put("package_id", new String[] { String.valueOf(packageId) });
		Map<String, List<Map>> detailBeans = orderSetsService.findDetailsByPk(requestMap);

		List<Map> packContentBeans = detailBeans.get("list_package_contents");
		for (int i = 0; i < packContentBeans.size(); i++) {
			Map<String, Object> item = ((List<Map<String, Object>>) updateJsonRequest
					.get("package_contents")).get(i);
			item.put("package_id", packageId);
			item.put("package_content_id", packContentBeans.get(i).get("package_content_id"));
		}

		List<Map> deptPackAppBeans = detailBeans.get("list_dept_package_applicability");
		List deptUpdateList = ((List) updateJsonRequest.get("dept_package_applicability"));
		for (Map item : deptPackAppBeans) {
			deptUpdateList.add(item);
		}

		/* Update Order Set */
		BasicDynaBean parentUpdateBean = orderSetsService.toBean(updateJsonRequest);
		Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapUpdateBean = orderSetsService
				.toBeansMap(updateJsonRequest, parentUpdateBean);
		orderSetsService.updateDetailsMap(parentUpdateBean, detailMapUpdateBean);

		BasicDynaBean updatedBean = orderSetsService.findByUniqueName(
				(String) updateJsonRequest.get("package_name"), "package_name");

		Map<String, List<Map>> UpdatedDetailBeans = orderSetsService.findDetailsByPk(requestMap);

		Assert.assertEquals(updatedBean.get("package_name"), updateJsonRequest.get("package_name"));

		packContentBeans = UpdatedDetailBeans.get("list_package_contents");
		for (int i = 0; i < packContentBeans.size(); i++) {
			Map<String, Object> item = ((List<Map<String, Object>>) updateJsonRequest
					.get("package_contents")).get(i);
			Assert.assertEquals(packContentBeans.get(i).get("activity_id"), item.get("activity_id"));
		}

		Assert.assertEquals(UpdatedDetailBeans.get("list_tpa_package_applicability").size(), 0);
		Assert.assertEquals(UpdatedDetailBeans.get("list_dept_package_applicability").size(),
				((List<Map>) updateJsonRequest.get("dept_package_applicability")).size());
		Assert.assertEquals(UpdatedDetailBeans.get("list_center_package_applicability").size(),
				((List<Map>) updateJsonRequest.get("center_package_applicability")).size());
	}

	@Test(expectedExceptions = NestableValidationException.class)
	public void insertQuantityRuleException() {
		logger.info("Quantity Rule Test");
		BasicDynaBean parentBean = orderSetsService.toBean(this.quantityRuleJson);
		Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = orderSetsService
				.toBeansMap(this.quantityRuleJson, parentBean);
		orderSetsService.insertDetailsMap(parentBean, detailMapBean);
	}

	@Test(expectedExceptions = DuplicateEntityException.class)
	public void insertDuplicateNameException() {
		logger.info("Duplicate Name Test");
		ModelMap jsonRequest = this.mockJsonData.get(0);
		BasicDynaBean parentBean = orderSetsService.toBean(jsonRequest);
		Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = orderSetsService
				.toBeansMap(jsonRequest, parentBean);
		orderSetsService.insertDetailsMap(parentBean, detailMapBean);
	}

	@Test(expectedExceptions = HMSException.class)
	public void insertBadRequestException() {
		logger.info("Bad Request Test");
		ModelMap jsonRequest = this.badRequestJson;
		BasicDynaBean parentBean = orderSetsService.toBean(jsonRequest);
		Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = orderSetsService
				.toBeansMap(jsonRequest, parentBean);
		orderSetsService.insertDetailsMap(parentBean, detailMapBean);
	}
	
	@Test(expectedExceptions = EntityNotFoundException.class)
	public void entityNotFoundException() {
		logger.info("entity not Test");
		ModelMap jsonRequest = this.entityNotFoundJson;
		BasicDynaBean parentBean = orderSetsService.toBean(jsonRequest);
		Map<String, Map<String, Map<String, BasicDynaBean>>> detailMapBean = orderSetsService
				.toBeansMap(jsonRequest, parentBean);
		orderSetsService.updateDetailsMap(parentBean, detailMapBean);
	}

	@DataProvider(name = "insertData")
	public Object[][] insertData() {
		Object[][] returnData = new Object[dataProviderInsertJson.size()][1];
		for (int i = 0; i < dataProviderInsertJson.size(); i++) {
			returnData[i][0] = dataProviderInsertJson.get(i);
		}
		return returnData;
	}

	@DataProvider(name = "updateData")
	public Object[][] updateData() {
		Object[][] returnData = new Object[dataProviderUpdateJson.size()][2];
		if (dataProviderInsertJson.size() == dataProviderUpdateJson.size()) {
			for (int i = 0; i < dataProviderUpdateJson.size(); i++) {
				returnData[i][0] = dataProviderInsertJson.get(i);
				returnData[i][1] = dataProviderUpdateJson.get(i);
			}
		}
		return returnData;
	}

	public Map<String, Object> getSessionServiceAttributes() {
		Map<String, Object> sessionAttributes = new HashMap<String, Object>();
		sessionAttributes.put("userId", "InstaAdmin");
		return sessionAttributes;
	}

}
