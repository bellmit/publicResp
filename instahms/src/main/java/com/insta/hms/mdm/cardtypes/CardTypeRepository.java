package com.insta.hms.mdm.cardtypes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CardTypeRepository extends MasterRepository<Integer> {

  public CardTypeRepository() {
    super("card_type_master", "card_type_id", "card_type");
  }

  private static final String CARD_TYPE_TABLES = " from card_type_master";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(CARD_TYPE_TABLES);
  }

  private static final String CARD_TYPE_DETAILS = "Select card_type_id, "
      + " card_type from card_type_master";

  public List<BasicDynaBean> getCardTypeDetails() {
    return DatabaseHelper.queryToDynaList(CARD_TYPE_DETAILS);
  }

}
