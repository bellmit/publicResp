package com.insta.hms.patientcategorychange;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class PatientCategoryChangeDAO.
 */
public class PatientCategoryChangeDAO {

  /** The log 4 j logger. */
  static Logger log4jLogger = LoggerFactory
      .getLogger(PatientCategoryChangeDAO.class);
  
  private static final GenericDAO organizationDetailsDAO = new GenericDAO("organization_details");
  private static final GenericDAO tpaMasterDAO = new GenericDAO("tpa_master");

  /**
   * Sets the category details.
   *
   * @param patientCategory
   *          the patient category
   * @param request
   *          the request
   * @throws SQLException
   *           thse SQL exception
   */
  public static void setCategoryDetails(Integer patientCategory, HttpServletRequest request)
      throws SQLException {

    BasicDynaBean categoryBean = new GenericDAO("patient_category_master").findByKey("category_id",
        patientCategory);

    String ipAllowedRatePlans = null;
    String ipAllowedSponsors = null;
    String opAllowedRatePlans = null;
    String opAllowedSponsors = null;

    if (categoryBean != null) {
      ipAllowedRatePlans = (String) categoryBean.get("ip_allowed_rate_plans");
      opAllowedRatePlans = (String) categoryBean.get("op_allowed_rate_plans");
      ipAllowedSponsors = (String) categoryBean.get("ip_allowed_sponsors");
      opAllowedSponsors = (String) categoryBean.get("op_allowed_sponsors");
    }

    List<BasicDynaBean> ipAllowedRatePlansList = new ArrayList();
    if (ipAllowedRatePlans != null && !ipAllowedRatePlans.trim().equals("*")) {
      ipAllowedRatePlansList = new ArrayList<BasicDynaBean>();
      String[] ratePlans = ipAllowedRatePlans.contains(",") ? ipAllowedRatePlans.split(",")
          : new String[] { ipAllowedRatePlans };
      for (int i = 0; i < ratePlans.length; i++) {
        BasicDynaBean bean = organizationDetailsDAO.findByKey("org_id",
            ratePlans[i]);
        ipAllowedRatePlansList.add(bean);
      }
    } else if (ipAllowedRatePlans != null && ipAllowedRatePlans.trim().equals("*")) {
      List allwdList = organizationDetailsDAO.findAllByKey("status", "A");
      ipAllowedRatePlansList.addAll(allwdList);
    }
    List<BasicDynaBean> opAllowedRatePlansList = new ArrayList();
    if (opAllowedRatePlans != null && !opAllowedRatePlans.trim().equals("*")) {
      opAllowedRatePlansList = new ArrayList<BasicDynaBean>();
      String[] ratePlans = opAllowedRatePlans.contains(",") ? opAllowedRatePlans.split(",")
          : new String[] { opAllowedRatePlans };
      for (int i = 0; i < ratePlans.length; i++) {
        BasicDynaBean bean = organizationDetailsDAO.findByKey("org_id",
            ratePlans[i]);
        opAllowedRatePlansList.add(bean);
      }
    } else if (opAllowedRatePlans != null && opAllowedRatePlans.trim().equals("*")) {
      List allwdList = organizationDetailsDAO.findAllByKey("status", "A");
      opAllowedRatePlansList.addAll(allwdList);
    }

    List<BasicDynaBean> ipAllowedSponsorsList = new ArrayList();
    if (ipAllowedSponsors != null && !ipAllowedSponsors.trim().equals("*")) {
      ipAllowedSponsorsList = new ArrayList<BasicDynaBean>();
      String[] sponsors = ipAllowedSponsors.contains(",") ? ipAllowedSponsors.split(",")
          : new String[] { ipAllowedSponsors };
      for (int i = 0; i < sponsors.length; i++) {
        BasicDynaBean bean = tpaMasterDAO.findByKey("tpa_id", sponsors[i]);
        ipAllowedSponsorsList.add(bean);
      }
    } else if (ipAllowedSponsors != null && ipAllowedSponsors.trim().equals("*")) {
      List allwdList = tpaMasterDAO.findAllByKey("status", "A");
      ipAllowedSponsorsList.addAll(allwdList);
    }

    List<BasicDynaBean> opAllowedSponsorsList = new ArrayList();
    if (opAllowedSponsors != null && !opAllowedSponsors.trim().equals("*")) {
      opAllowedSponsorsList = new ArrayList<BasicDynaBean>();
      String[] sponsors = opAllowedSponsors.contains(",") ? opAllowedSponsors.split(",")
          : new String[] { opAllowedSponsors };
      for (int i = 0; i < sponsors.length; i++) {
        BasicDynaBean bean = tpaMasterDAO.findByKey("tpa_id", sponsors[i]);
        opAllowedSponsorsList.add(bean);
      }
    } else if (opAllowedSponsors != null && opAllowedSponsors.trim().equals("*")) {
      List allwdList = tpaMasterDAO.findAllByKey("status", "A");
      opAllowedSponsorsList.addAll(allwdList);
    }

    request.setAttribute("ip_allowedRatePlans", ipAllowedRatePlansList);
    request.setAttribute("op_allowedRatePlans", opAllowedRatePlansList);
    request.setAttribute("ip_allowedSponsors", ipAllowedSponsorsList);
    request.setAttribute("op_allowedSponsors", opAllowedSponsorsList);

  }
}
