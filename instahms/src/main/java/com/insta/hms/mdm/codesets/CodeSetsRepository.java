package com.insta.hms.mdm.codesets;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.StringUtil;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class CodeSetsRepository extends MasterRepository<Integer> {

  public CodeSetsRepository() {
    super("code_sets", "id");
  }

  public static final String CODE = "label";

  public static final String DESCRIPTION = "short_code";

  public static final String CHK_VIRTUAL_TABLE = "isvirtualtable";

  private static final String GET_CODE_SETS =
      "SELECT c.id AS code_set_id," + " e.#entity_id# AS entity_id, e.#entity_name# AS entity_name,"
          + " c.label AS code_description, c.short_code AS code,"
          + " c.code_system_category_id, c.code_system_id" + " FROM #table_name# e"
          + " LEFT JOIN code_sets c ON (c.entity_id=e.#entity_id#"
          + " AND c.code_system_category_id = ? AND c.code_system_id = ?)";

  private static final String GET_VIRTUAL_CODE_SETS =
      "SELECT id AS code_set_id," + " entity_id, label AS code_description, short_code AS code,"
          + " code_system_category_id, code_system_id" + " FROM code_sets WHERE "
          + " code_system_category_id = ? AND code_system_id = ? ";

  /**
   * Get Code Sets.
   * 
   * @param params the map
   * @return PagedList
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List getVirtualCodeSets(Map<String, Object> params, List<Integer> entityIds) {
    ArrayList<Object> values = new ArrayList<>();
    int codeSystemCategoryId = !StringUtils.isEmpty(params.get("code_system_category_id"))
        ? Integer.parseInt((String) params.get("code_system_category_id"))
        : 0;
    int codeSystemId = !StringUtils.isEmpty(params.get("code_systems_id"))
        ? Integer.parseInt((String) params.get("code_systems_id"))
        : 0;
    values.add(codeSystemCategoryId);
    values.add(codeSystemId);
    StringBuilder query = new StringBuilder(GET_VIRTUAL_CODE_SETS);
    query.append(" AND entity_id IN (");
    String[] placeholdersArr = new String[entityIds.size()];
    Arrays.fill(placeholdersArr, "?");
    query.append(StringUtils.arrayToCommaDelimitedString(placeholdersArr));
    query.append(") ORDER BY entity_id asc ");
    values.addAll(entityIds);
    return DatabaseHelper.queryToDynaList(query.toString(), values.toArray());
  }

  /**
   * Get Code Sets.
   * 
   * @param params the map
   * @return PagedList
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public PagedList getCodeSets(Map params) {
    ArrayList<Object> values = new ArrayList<>();
    Map<String, Object> flattenMap = ConversionUtils.flatten(params);
    int codeSystemCategoryId = !StringUtils.isEmpty(flattenMap.get("code_system_category_id"))
        ? Integer.parseInt((String) flattenMap.get("code_system_category_id"))
        : 0;
    int codeSystemId = !StringUtils.isEmpty(flattenMap.get("code_systems_id"))
        ? Integer.parseInt((String) flattenMap.get("code_systems_id"))
        : 0;
    values.add(codeSystemCategoryId);
    values.add(codeSystemId);
    String query = getCodeSetsQuery(codeSystemCategoryId, GET_CODE_SETS);
    if (query != null) {
      if (query.startsWith(CHK_VIRTUAL_TABLE)) {
        VirtualTable virtualTableObj = VirtualTable.getInstance(query);
        return virtualTableObj.getMappedPagedList(flattenMap);
      } else {
        SearchQuery searchQuery = new SearchQuery("FROM (" + query + ") AS foo");
        SearchQueryAssembler qb =
            new SearchQueryAssembler(searchQuery.getFieldList(), searchQuery.getCountQuery(),
                searchQuery.getSelectTables(), null, ConversionUtils.getListingParameter(params));
        qb.setfieldValues(values);
        qb.build();
        return qb.getMappedPagedList();
      }
    }
    return null;
  }

  private static final String GET_CODE_SETS_FOR_DOWNLAOD = "SELECT"
      + " (select label from code_system_categories where id = ?) as master_name,"
      + " (select label from code_systems where id = ?) as code_system,"
      + " e.#entity_name# AS entity_name," + " c.label AS code_description, c.short_code AS code"
      + " FROM #table_name# e" + " LEFT JOIN code_sets c" + " ON (c.entity_id=e.#entity_id#"
      + " AND c.code_system_category_id = ? AND c.code_system_id = ?)";

  /**
   * Get Code Sets for download.
   * 
   * @param params the map
   * @return PagedList
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public PagedList getCodeSetsForDownload(Map params) {
    ArrayList<Object> values = new ArrayList<>();
    Map<String, Object> flattenMap = ConversionUtils.flatten(params);
    int codeSystemCategoryId = !StringUtils.isEmpty(flattenMap.get("code_system_category_id"))
        ? Integer.parseInt((String) flattenMap.get("code_system_category_id"))
        : 0;
    int codeSystemId = !StringUtils.isEmpty(flattenMap.get("code_systems_id"))
        ? Integer.parseInt((String) flattenMap.get("code_systems_id"))
        : 0;
    values.add(codeSystemCategoryId);
    values.add(codeSystemId);
    values.add(codeSystemCategoryId);
    values.add(codeSystemId);
    String query = getCodeSetsQuery(codeSystemCategoryId, GET_CODE_SETS_FOR_DOWNLAOD);
    if (query != null) {
      SearchQuery searchQuery = new SearchQuery("FROM (" + query + ") AS foo");
      SearchQueryAssembler qb =
          new SearchQueryAssembler(searchQuery.getFieldList(), searchQuery.getCountQuery(),
              searchQuery.getSelectTables(), null, ConversionUtils.getListingParameter(params));
      qb.setfieldValues(values);
      qb.build();
      return qb.getMappedPagedList();
    }
    return null;
  }

  private static final String GET_CODE_CATEGORY_DETAILS =
      "SELECT table_name, entity_name," + " entity_id, isvirtualtable "
          + " FROM code_system_categories WHERE id = :codeSystemCategoryId";

  private String getCodeSetsQuery(int codeSystemCategoryId, String query) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("codeSystemCategoryId", codeSystemCategoryId);
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(GET_CODE_CATEGORY_DETAILS, parameters);
    if (bean != null) {
      if (!StringUtil.isNullOrEmpty((String) bean.get(CHK_VIRTUAL_TABLE))
          && "Y".equals((String) bean.get(CHK_VIRTUAL_TABLE))) {
        query = "isvirtualtable:#table_name#:#entity_name#:#entity_id#";
      }
      return query.replace("#table_name#", (String) bean.get("table_name"))
          .replace("#entity_name#", (String) bean.get("entity_name"))
          .replaceAll("#entity_id#", (String) bean.get("entity_id"));
    }
    return null;
  }

  private static final String GET_CODE_SYSTEM_CATEGORY_LABEL =
      "SELECT" + " label FROM code_system_categories WHERE id = :codeSystemCategoryId";

  /**
   * Get Code System Category Label.
   * 
   * @param codeSystemCategoryId the id
   * @return bean
   */
  public BasicDynaBean getCodeSystemCategoryLabel(int codeSystemCategoryId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("codeSystemCategoryId", codeSystemCategoryId);
    return DatabaseHelper.queryToDynaBean(GET_CODE_SYSTEM_CATEGORY_LABEL, parameter);
  }

  private static final String GET_CODE_SYSTEM_LABEL =
      "SELECT" + " label FROM code_systems WHERE id = :codeSystemId";

  /**
   * Get Code System Label.
   * 
   * @param codeSystemId the id
   * @return bean
   */
  public BasicDynaBean getCodeSystemLabel(int codeSystemId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("codeSystemId", codeSystemId);
    return DatabaseHelper.queryToDynaBean(GET_CODE_SYSTEM_LABEL, parameter);
  }

  private static final String GET_DEFAULT_CODE_SETS =
      "SELECT csc.label AS master_name," + " cs.short_code AS code, cs.label AS code_description,"
          + " cs.id AS code_set_id, csc.id AS code_system_category_id,"
          + " :codeSystemId AS code_system_id" + " FROM code_system_categories csc"
          + " LEFT JOIN code_sets cs ON (csc.id = cs.code_system_category_id"
          + " AND cs.is_default is true AND cs.code_system_id = :codeSystemId)";

  /**
   * Gets default code sets.
   * 
   * @param codeSystemId the code sys id
   * @param codeSystemCategoryId the code cat id
   * @return list
   */
  public List<BasicDynaBean> getDefaultCodeSets(int codeSystemId, int codeSystemCategoryId) {
    StringBuilder query = new StringBuilder(GET_DEFAULT_CODE_SETS);
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("codeSystemId", codeSystemId);
    if (codeSystemCategoryId != 0) {
      query.append(" WHERE csc.id = :codeSystemCategoryId");
      parameter.addValue("codeSystemCategoryId", codeSystemCategoryId);
    }
    return DatabaseHelper.queryToDynaList(query.toString(), parameter);
  }

  private static final String GET_DISTINCT_CODE_SETS = "SELECT DISTINCT short_code, label"
      + " FROM code_sets WHERE code_system_id = ? AND code_system_category_id = ? ";

  public List<BasicDynaBean> getDistinctCodesets(int codeSystemId, int codeSystemCategoryId) {
    return DatabaseHelper.queryToDynaList(GET_DISTINCT_CODE_SETS,
        new Object[] {codeSystemId, codeSystemCategoryId});
  }

  private static final String REMOVE_DEFAULTS = "UPDATE code_sets SET is_default = null"
      + " WHERE code_system_id = ? AND code_system_category_id = ?";

  private static final String UPDATE_DEFAULTS = "UPDATE code_sets SET is_default = true"
      + " WHERE id = (SELECT id FROM code_sets WHERE code_system_id = ?"
      + " AND code_system_category_id = ? AND short_code = ? ORDER BY id ASC LIMIT 1)";

  /**
   * First updates null to speific category and picks one record to set default.
   * 
   * @param codeSystemId the code sys id
   * @param codeSystemCategoryId the code cat id
   * @param code the code
   * @return int
   */
  public int updateDefault(int codeSystemId, int codeSystemCategoryId, String code) {
    DatabaseHelper.update(REMOVE_DEFAULTS, new Object[] {codeSystemId, codeSystemCategoryId});
    return DatabaseHelper.update(UPDATE_DEFAULTS,
        new Object[] {codeSystemId, codeSystemCategoryId, code});
  }
}
