package testng.com.insta.hms.mdm.taxgroups;

import static org.testng.Assert.assertEquals;

import com.insta.hms.common.PagedList;
import com.insta.hms.mdm.taxgroups.TaxGroupRepository;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxgroups.TaxGroupValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
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
 * The Class TaxGroupServiceTest.
 */
@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class TaxGroupServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  /** The tax group service. */
  @Spy
  @InjectMocks
  TaxGroupService taxGroupService;

  /** The tax group repository. */
  @Spy
  TaxGroupRepository taxGroupRepository;

  /** The tax group validator. */
  @Spy
  TaxGroupValidator taxGroupValidator;

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
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("item_groups");
    testRepo.insert("item_group_type");
    testRepo.insert("item_sub_groups");
    dbDataMap = testRepo.initializeRepo();
  }

  /**
   * List.
   */
  @Test
  public void list() {
    Map<String, String[]> params = buildParamMap();
    PagedList result = this.taxGroupService.search(params);
    assertEquals(result.getTotalRecords(), 1L);
  }

  /**
   * Builds the param map.
   *
   * @return the map
   */
  public Map<String, String[]> buildParamMap() {
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("group_code", new String[] { "VAT" });
    paramMap.put("item_group_type_id", new String[] { "TAX" });
    paramMap.put("status", new String[] { "A" });
    return paramMap;
  }

  /**
   * Insert.
   */
  @Test
  public void insert() {
    BasicDynaBean paramsMap = getInsertParams();
    taxGroupService.insert(paramsMap);
    BasicDynaBean bean = taxGroupRepository.findByKey("item_group_name",
        paramsMap.get("item_group_name"));
    Assert.assertEquals(paramsMap.get("item_group_name"), bean.get("item_group_name"));
  }

  /**
   * Gets the insert params.
   *
   * @return the insert params
   */
  private BasicDynaBean getInsertParams() {
    BasicDynaBean params = taxGroupRepository.getBean();
    params.set("item_group_name", "testtax");
    params.set("group_code", "GST");
    params.set("item_group_display_order", 207);
    params.set("item_group_type_id", "TAX");
    params.set("status", "A");
    return params;
  }

  /**
   * Gets the all item group.
   *
   * @return the all item group
   */
  @Test
  public void getAllItemGroup() {
    List<BasicDynaBean> result = this.taxGroupService.getAllItemGroup();
    assertEquals(result.size(), 6);
  }

  /**
   * Gets the item group type.
   *
   * @return the item group type
   */
  @Test
  public void getItemGroupType() {
    List<BasicDynaBean> result = this.taxGroupService.getItemGroupType();
    assertEquals(result.size(), 1);
  }

  /**
   * Gets the item grp map.
   *
   * @return the item grp map
   */
  @Test
  public void getItemGrpMap() {
    Map<String, Integer> map = this.taxGroupService.getItemGrpMap();
    assertEquals(map.size(), 6);
  }

  /**
   * Gets the tax item groups.
   *
   * @return the tax item groups
   */
  @Test
  public void getTaxItemGroups() {
    List<BasicDynaBean> results = taxGroupService.getTaxItemGroups();
    assertEquals(results.size(), 6);
  }

  /**
   * Find by group codes.
   */
  @Test
  public void findByGroupCodes() {
    List<BasicDynaBean> list = this.taxGroupService.findByGroupCodes(new String[] { "GST" });
    assertEquals(list.size(), 2);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  @Test
  public void getAddEditPageData() {
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("item_group_id", new String[] { "202" });
    Map<String, List<BasicDynaBean>> map = this.taxGroupService.getAddEditPageData(paramMap);
    assertEquals(map.size(), 2);
  }

}
