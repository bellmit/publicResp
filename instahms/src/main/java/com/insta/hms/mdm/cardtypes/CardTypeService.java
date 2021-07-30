package com.insta.hms.mdm.cardtypes;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CardTypeService.
 */
@Service
public class CardTypeService extends MasterService {

  /**
   * Instantiates a new card type service.
   *
   * @param cardTypeRepository the card type repository
   * @param cardTypeValidator the card type validator
   */
  public CardTypeService(CardTypeRepository cardTypeRepository,
      CardTypeValidator cardTypeValidator) {
    super(cardTypeRepository, cardTypeValidator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();
    referenceMap.put("cardTypeDetails", cardTypeDetails());

    return referenceMap;
  }

  /**
   * Card type details.
   *
   * @return the list
   */
  public List<BasicDynaBean> cardTypeDetails() {
    return ((CardTypeRepository) getRepository()).getCardTypeDetails();
  }

}
