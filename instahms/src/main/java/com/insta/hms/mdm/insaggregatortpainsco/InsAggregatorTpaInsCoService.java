package com.insta.hms.mdm.insaggregatortpainsco;

import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterDetailsService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

/** The Class InsAggregatorTpaInsCoService. */
@Service
public class InsAggregatorTpaInsCoService extends MasterDetailsService {

  /**
   * Instantiates a new ins aggregator tpa ins co service.
   *
   * @param repo the repo
   * @param detailsRepo the detailsRepo
   * @param validator the validator
   */
  public InsAggregatorTpaInsCoService(
      InsAggregatorTpaInsCoRepository repo,
      InsAggregatorTpaInsCoDetailsRepository detailsRepo,
      InsAggregatorTpaInsCoValidator validator) {
    super(repo, validator, detailsRepo);
  }

  /* (non-Javadoc)
   * @see com.insta.hms.mdm.MasterService#addFilterForLookUp
   * (com.insta.hms.common.SearchQueryAssembler,
   * java.lang.String, java.lang.String, boolean, java.util.Map)
   */
  @Override
  public void addFilterForLookUp(
      SearchQueryAssembler qb,
      String likeValue,
      String matchField,
      boolean contains,
      Map<String, String[]> parameters) {
    if (!likeValue.trim().isEmpty()) {
      String filterText = likeValue.trim() + "%";
      if (contains) {
        filterText = "%" + likeValue.trim() + "%";
      }
      ArrayList<Object> types = new ArrayList<Object>();
      types.add(QueryAssembler.STRING);
      types.add(QueryAssembler.STRING);
      ArrayList<String> values = new ArrayList<String>();
      values.add(filterText);
      qb.appendExpression(" ( tpainsco_name ILIKE ? ) ", types, values);
    }
  }
}
