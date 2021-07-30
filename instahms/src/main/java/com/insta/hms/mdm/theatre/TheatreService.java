package com.insta.hms.mdm.theatre;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class TheatreService.
 */
@Service
public class TheatreService extends MasterService {

  /**
   * Instantiates a new theatre service.
   *
   * @param tr the tr
   * @param tv the tv
   */
  public TheatreService(TheatreRepository tr, TheatreValidator tv) {
    super(tr, tv);
  }

  /**
   * Gets the theatre item sub group tax details.
   *
   * @param actDescriptionId the act description id
   * @return the theatre item sub group tax details
   */
  public List<BasicDynaBean> getTheatreItemSubGroupTaxDetails(String actDescriptionId) {
    return ((TheatreRepository) getRepository()).getTheatreItemSubGroupTaxDetails(actDescriptionId);
  }

  public List<BasicDynaBean> getTheatreListForPatientId(String visitId) {
    return ((TheatreRepository) getRepository()).getTheatreListForPatientId(visitId);
  }
}
