package com.insta.hms.mdm.indicationforcaesareansection;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class IndicationForCaesareanSectionRepository.
 *
 * @author anil.n
 */
@Repository
public class IndicationForCaesareanSectionRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new indication for caesarean section repository.
   */
  public IndicationForCaesareanSectionRepository() {
    super("indication_for_caesarean_section", "id", "indication");
  }
  

  private static final String PAGE_SIZE = "page_size";
  private static final String PAGE_NUMBER = "page_number";

  private static final String BASE_QUERY = "SELECT *, count(id) OVER (PARTITION BY 1)"
      + " FROM indication_for_caesarean_section ics & "
      + " ORDER BY id";
 

  /**
   * Find indication for caesarean sections by filters.
   *
   * @param params filter map
   * @return indication for caesarean sections response map
   */
  public Map<String, Object> findByFilters(Map<String, String> params) {

    StringBuilder query = new StringBuilder().append(BASE_QUERY);
    List<Object> queryArguments = new ArrayList<>();
    List<String> filters = new ArrayList<>();

    String indication = params.get("indication");
    if (!StringUtils.isEmpty(indication)) {
      filters.add(" indication ILIKE ? ");
      queryArguments.add('%' + indication.trim() + '%');
    }

    String status = params.get("status");
    if (!StringUtils.isEmpty(status)) {
      filters.add(" status =? ");
      queryArguments.add(status.trim());
    }

    if (!filters.isEmpty()) {
      query = new StringBuilder(query.toString().replaceAll("&",
          "WHERE " + StringUtils.collectionToDelimitedString(filters, " AND")));
    } else {
      query = new StringBuilder(query.toString().replaceAll("&", ""));
    }

    Integer pageSize;
    if (!StringUtils.isEmpty(params.get(PAGE_SIZE))) {
      pageSize = Integer.parseInt(params.get(PAGE_SIZE));
    } else {
      pageSize = 0;
    }
    if (pageSize != 0) {
      query.append(" LIMIT ?");
      queryArguments.add(pageSize);
    }

    Integer pageNum;
    if (!StringUtils.isEmpty(params.get(PAGE_NUMBER))) {
      pageNum = Integer.parseInt(params.get(PAGE_NUMBER));
    } else {
      pageNum = 0;
    }
    if (pageNum != 0) {
      query.append(" OFFSET ?");
      queryArguments.add((pageNum) * pageSize);
    }

    Map<String, Object> resultMap = new HashMap<>();
    List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(query.toString(),
        queryArguments.toArray());
    resultMap.put(PAGE_SIZE, pageSize);
    resultMap.put(PAGE_NUMBER, pageNum);
    resultMap.put("indication_for_caesarean_section", ConversionUtils.listBeanToListMap(results));
    resultMap.put("total_records", !results.isEmpty() ? results.get(0).get("count") : 0);

    return resultMap;
  }
  
}
