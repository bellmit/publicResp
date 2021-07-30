package com.insta.hms.core.clinical.instaforms;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The Class InstaFormsService.
 *
 * @author anup vishwas
 */

@Service
public abstract class InstaFormsService {

  /**
   * Gets the single instance of InstaFormsService.
   *
   * @param formType
   *          the form type
   * @return single instance of InstaFormsService
   */
  public static InstaFormsService getInstance(String formType) {

    InstaFormsService instaformService = null;
    if (formType.equals("Form_CONS")) {
      instaformService = new ConsultationFormsService();
    } else if (formType.equals("Form_OT")) {
      instaformService = new OTFormsService();
    }
    return instaformService;
  }

  /**
   * Gets the components.
   *
   * @param params
   *          the params
   * @return the components
   */
  /*
   * returns sections to be shown per screen. if the screen is already saved, then details will be
   * returned from the transaction tables, else from master.
   */
  public abstract BasicDynaBean getComponents(Map params);

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  public abstract Map getKeys();

  /**
   * Sort markers of image.
   *
   * @param map
   *          the map
   */
  public static void sortMarkersOfImage(Map<Object, List<List>> map) {
    for (Map.Entry<Object, List<List>> entry : map.entrySet()) {
      for (List<BasicDynaBean> list : entry.getValue()) {
        BasicDynaBean beanImage = (BasicDynaBean) list.get(0);
        if (beanImage.get("field_type").equals("image")) {
          Collections.sort(list, new Comparator<BasicDynaBean>() {
            public int compare(BasicDynaBean bean, BasicDynaBean ibean) {
              if (bean == null && ibean == null) {
                return 0;
              }

              if (bean != null
                  && ibean != null
                  && ((bean.get("coordinate_x") == null && ibean.get("coordinate_x") == null) 
                      || (bean.get("coordinate_y") == null && ibean
                      .get("coordinate_y") == null))) {
                return 0;
              }

              if (bean == null || bean.get("coordinate_x") == null
                  || bean.get("coordinate_y") == null) {
                return -1;
              }

              if (ibean == null || ibean.get("coordinate_x") == null
                  || ibean.get("coordinate_y") == null) {
                return 1;
              }

              BigDecimal posX = (BigDecimal) bean.get("coordinate_x");
              BigDecimal posY = (BigDecimal) bean.get("coordinate_y");
              BigDecimal iposX = (BigDecimal) ibean.get("coordinate_x");
              BigDecimal iposY = (BigDecimal) ibean.get("coordinate_y");

              if (posX.compareTo(iposX) == 1 && posY.compareTo(iposY) == 1) {
                return 1;
              } else if (posX.compareTo(iposX) == 1 && posY.compareTo(iposY) == 0) {
                return 0;
              } else if (posX.compareTo(iposX) == 1 && posY.compareTo(iposY) == -1) {
                return -1;
              } else if (posX.compareTo(iposX) == 0 && posY.compareTo(iposY) == 1) {
                return 1;
              } else if (posX.compareTo(iposX) == 0 && posY.compareTo(iposY) == 0) {
                return 0;
              } else if (posX.compareTo(iposX) == 1 && posY.compareTo(iposY) == 0) {
                return -1;
              } else if (posX.compareTo(iposX) == -1 && posY.compareTo(iposY) == -1) {
                return -1;
              } else if (posX.compareTo(iposX) == -1 && posY.compareTo(iposY) == 0) {
                return 0;
              } else if (posX.compareTo(iposX) == -1 && posY.compareTo(iposY) == 1) {
                return 1;
              }

              return 0;
            }
          });
        }
      }
    }
  }

}
