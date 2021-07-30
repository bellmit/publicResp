package testng.com.insta.hms.mdm.itemgroups;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

import com.insta.hms.common.PagedList;
import com.insta.hms.mdm.itemgroups.ItemGroupsRepository;
import com.insta.hms.mdm.itemgroups.ItemGroupsService;
import com.insta.hms.mdm.itemgroups.ItemGroupsValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testng.utils.TestRepoInit;

// TODO: Auto-generated Javadoc
/**
 * The Class ItemGroupsServiceTest.
 */
@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class ItemGroupsServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  /** The item groups service. */
  @InjectMocks
  ItemGroupsService itemGroupsService;

  /** The item groups repository. */
  @Spy
  ItemGroupsRepository itemGroupsRepository;

  /** The item groups validator. */
  @Spy
  ItemGroupsValidator itemGroupsValidator;

  /** The db data map. */
  private Map<String, Object> dbDataMap = null;

  /**
   * Inits the.
   *
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @BeforeMethod
  public void init() throws IOException {
    initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("item_groups");

    dbDataMap = testRepo.initializeRepo();
  }

  /**
   * List.
   */
  @Test
  public void list() {
    Map<String, String[]> params = buildParamMap();
    PagedList result = this.itemGroupsService.search(params);
    assertEquals(result.getTotalRecords(), 2L);
  }

  /**
   * Builds the param map.
   *
   * @return the map
   */
  public Map<String, String[]> buildParamMap() {
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("group_code", new String[] { "BG" });
    paramMap.put("item_group_type_id", new String[] { "BILLGRP" });
    paramMap.put("status", new String[] { "A" });
    return paramMap;
  }

  /**
   * Insert.
   */
  @Test
  public void insert() {
    BasicDynaBean paramsMap = getInsertParams();
    itemGroupsService.insert(paramsMap);
    BasicDynaBean bean = itemGroupsRepository.findByKey("item_group_name",
        paramsMap.get("item_group_name"));
    Assert.assertEquals(paramsMap.get("item_group_name"), bean.get("item_group_name"));
  }

  /**
   * Gets the insert params.
   *
   * @return the insert params
   */
  private BasicDynaBean getInsertParams() {
    BasicDynaBean params = itemGroupsRepository.getBean();
    params.set("item_group_name", "Billgrptestinsert");
    params.set("group_code", "BG");
    params.set("item_group_display_order", 200);
    params.set("item_group_type_id", "BILLGRP");
    params.set("status", "A");
    return params;
  }

  /**
   * Update.
   */
  @Test
  public void update() {
    List<Map<String, Object>> groupList = (List) dbDataMap.get("item_groups");
    if (!groupList.isEmpty() && !groupList.isEmpty()) {
      BasicDynaBean updateparams = getUpdateParams(groupList.get(0));
      itemGroupsService.update(updateparams);
      BasicDynaBean bean = itemGroupsRepository.findByKey("item_group_id", 100);
      Assert.assertEquals(updateparams.get("item_group_name"), bean.get("item_group_name"));
    }
  }

  /**
   * Gets the update params.
   *
   * @param itemGrp
   *          the item grp
   * @return the update params
   */
  private BasicDynaBean getUpdateParams(Map<String, Object> itemGrp) {
    BasicDynaBean params = itemGroupsRepository.getBean();
    params.set("item_group_id", 100);
    params.set("item_group_name", "BillGrptestupdate");
    params.set("group_code", "BG");
    params.set("item_group_display_order", 1);
    params.set("status", "A");
    params.set("item_group_type_id", "BILLGRP");
    return params;
  }

  /**
   * Look up.
   */
  @Test
  public void lookUp() {
    List<BasicDynaBean> list = itemGroupsService.autocomplete("biillgrp", buildParamMap());
    if (!list.isEmpty() && !list.isEmpty()) {
      Assert.assertEquals(list.size(), 2);
    }
  }

}
