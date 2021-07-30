package com.insta.hms.opthalmology;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class DoctorEyeExamAction.
 */
public class DoctorEyeExamAction extends DispatchAction {

  /** The main dao. */
  private static GenericDAO mainDao = new GenericDAO("opthal_doctor_exam_main");

  /** The eye dao. */
  private static GenericDAO eyeDao = new GenericDAO("opthal_doctor_overall_exam");

  /** The lens dao. */
  private static GenericDAO lensDao = new GenericDAO("opthal_doctor_lens_exam");

  /** The fundus dao. */
  private static GenericDAO fundusDao = new GenericDAO("opthal_doctor_fundus_exam");

  /** The ot main. */
  GenericDAO optMain = new GenericDAO("opthal_test_main");

  /** The m dao. */
  GenericDAO patMedDao = new GenericDAO("patient_medicine_prescriptions");

  /** The s dao. */
  GenericDAO patSerDao = new GenericDAO("patient_service_prescriptions");

  /** The t dao. */
  GenericDAO patTestDao = new GenericDAO("patient_test_prescriptions");

  /** The mm dao. */
  PrescriptionsMasterDAO mmDao = new PrescriptionsMasterDAO();

  /** The med dosage dao. */
  GenericDAO medDosageDao = new GenericDAO("medicine_dosage_master");

  /** The doctor DAO. */
  GenericDAO doctorDAO = new GenericDAO("patient_consultation_prescriptions");

  /**
   * Show doctor eye exam screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward showDoctorEyeExamScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    String patientId = request.getParameter("patient_id");
    String consultId = new GenericDAO("doctor_consultation").findByKey("patient_id", patientId)
        .get("consultation_id").toString();
    GenericDAO medDosageDao = new GenericDAO("medicine_dosage_master");
    List medDosages = medDosageDao.listAll();
    request.setAttribute("medDosages",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(medDosages)));
    Map modulesActivatedMap = ((Preferences) request.getSession(false).getAttribute("preferences"))
        .getModulesActivatedMap();
    String modPharmacy = (String) (modulesActivatedMap.get("mod_pharmacy"));

    request.setAttribute("mainBean", mainDao.findByKey("patient_id", patientId));
    request.setAttribute("eyeBean", eyeDao.findByKey("patient_id", patientId));
    request.setAttribute("lensBean", lensDao.findByKey("patient_id", patientId));
    request.setAttribute("fundusBean", fundusDao.findByKey("patient_id", patientId));
    request.setAttribute("consultId", consultId);
    request.setAttribute("prescriptions", PrescriptionsMasterDAO
        .getAllPrescriptions(Integer.parseInt(consultId), patientId, modPharmacy, null));
    request.setAttribute("otMainBean",
        new GenericDAO("opthal_test_main").findByKey("patient_id", patientId));

    return mapping.findForward("showDoctorEyeExamScreen");
  }

  /**
   * Save doctor eye exam details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  public ActionForward saveDoctorEyeExamDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    Connection con = null;
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    FlashScope flash = FlashScope.getScope(request);
    boolean success = false;
    boolean allSuccess = false;

    String status = request.getParameter("status");
    String patientId = request.getParameter("patient_id");
    Map params = request.getParameterMap();
    String key = request.getParameter("insertOrupdate");
    ArrayList errorFields = new ArrayList();

    BasicDynaBean mainBean = mainDao.getBean();
    BasicDynaBean eyeBean = eyeDao.getBean();
    BasicDynaBean lensBean = lensDao.getBean();
    BasicDynaBean fundusBean = fundusDao.getBean();
    BasicDynaBean otMainBean = optMain.findByKey("patient_id", patientId);
    ;
    int mainId = 0;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      con.setAutoCommit(false);

      ConversionUtils.copyToDynaBean(params, mainBean, errorFields);
      ConversionUtils.copyToDynaBean(params, eyeBean, errorFields);
      ConversionUtils.copyToDynaBean(params, lensBean, errorFields);
      ConversionUtils.copyToDynaBean(params, fundusBean, errorFields);

      HashMap keys = null;

      if (key.equalsIgnoreCase("insert")) {

        mainId = mainDao.getNextSequence();
        mainBean.set("doctor_exam_id", mainId);
        success = mainDao.insert(con, mainBean);
        if (!success) {
          flash.put("error", "Exception occured while inserting Doctor Exam Details");
          return redirect;
        }

        eyeBean.set("overall_exam_id", eyeDao.getNextSequence());
        eyeBean.set("notes", request.getParameter("eye_notes"));
        eyeBean.set("doctor_exam_id", mainId);
        success = eyeDao.insert(con, eyeBean);
        if (!success) {
          flash.put("error", "Exception occured while inserting Eye Deatails");
          return redirect;
        }

        lensBean.set("lens_exam_id", lensDao.getNextSequence());
        lensBean.set("doctor_exam_id", mainId);
        success = lensDao.insert(con, lensBean);
        if (!success) {
          flash.put("error", "Exception occured while inserting Lens Details");
          return redirect;
        }

        fundusBean.set("fundus_exam_id", fundusDao.getNextSequence());
        fundusBean.set("doctor_exam_id", mainId);
        success = fundusDao.insert(con, fundusBean);
        if (!success) {
          flash.put("error", "Exception occured while inserting Fundus Details");
          return redirect;
        }

      } else {
        keys = new HashMap();
        keys.put("doctor_exam_id", Integer.parseInt(key));

        success = mainDao.update(con, mainBean.getMap(), keys) > 0;
        if (!success) {
          flash.put("error", "Exception occured while updating the Doctor Exam Details");
          return redirect;
        }

        keys = new HashMap();
        keys.put("overall_exam_id", Integer.parseInt(request.getParameter("overall_exam_id")));
        eyeBean.set("notes", request.getParameter("eye_notes"));

        success = eyeDao.update(con, eyeBean.getMap(), keys) > 0;
        if (!success) {
          flash.put("error", "Exception occured while updating the Eye Details");
          return redirect;
        }
        keys = new HashMap();
        keys.put("lens_exam_id", Integer.parseInt(request.getParameter("lens_exam_id")));

        success = lensDao.update(con, lensBean.getMap(), keys) > 0;
        if (!success) {
          flash.put("error", "Exception occured while updating the Lens Details");
          return redirect;
        }
        keys = new HashMap();
        keys.put("fundus_exam_id", Integer.parseInt(request.getParameter("fundus_exam_id")));

        success = fundusDao.update(con, fundusBean.getMap(), keys) > 0;
        if (!success) {
          flash.put("error", "Exception occured while updating the Fundus Details");
          return redirect;
        }
      }

      success = savePriscriptioDetails(request, con);
      if (!success) {
        flash.put("error", "Exception occured while saving prescription Details");
        return redirect;
      }

      if (status != null && !status.equals("") && otMainBean.get("status").equals("D")) {
        otMainBean.set("status", status);
        keys = new HashMap();
        keys.put("patient_id", patientId);
        success = optMain.update(con, otMainBean.getMap(), keys) >= 0;
        if (!success) {
          flash.put("error", "Exception occured while inserting Doctor Exam Details");
          return redirect;
        }
      }

      allSuccess = true;

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("patient_id", request.getParameter("patient_id"));
      redirect.addParameter("mr_no", request.getParameter("mr_no"));
      redirect.addParameter("doctor_id", request.getParameter("doctor_id"));
      redirect.addParameter("consultation_id", request.getParameter("consult_id"));
    }
    return redirect;

  }

  /**
   * Save priscriptio details.
   *
   * @param request
   *          the request
   * @param con
   *          the con
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private boolean savePriscriptioDetails(HttpServletRequest request, Connection con)
      throws SQLException, IOException {

    String consIdStr = request.getParameter("consult_id");

    int consId = Integer.parseInt(consIdStr);
    Map params = request.getParameterMap();
    ArrayList errors = new ArrayList();

    boolean allSuccess = false;

    String[] prescribedIds = request.getParameterValues("item_prescribed_id");
    String[] itemNames = request.getParameterValues("item_name");
    String[] medDosages = request.getParameterValues("medicine_dosage");
    String[] noOfDays = request.getParameterValues("medicine_days");
    String[] medQty = request.getParameterValues("medicine_quantity");
    String[] itemRemarks = request.getParameterValues("item_remarks");
    String[] delItems = request.getParameterValues("delItem");
    String[] itemType = request.getParameterValues("itemType");
    String[] itemMaster = request.getParameterValues("item_master");
    String[] ispackage = request.getParameterValues("ispackage");
    Map<String, BasicDynaBean> insertMedDosageBeanMap = new HashMap<String, BasicDynaBean>();

    txn: {

      for (int i = 0; i < prescribedIds.length - 1; i++) {
        boolean deleteItem = new Boolean(delItems[i]);
        String prescribedId = prescribedIds[i];
        if (itemType[i].equals("Medicine")) {
          BasicDynaBean med = patMedDao.getBean();
          med.set("medicine_name", itemNames[i]);
          med.set("medicine_dosage", medDosages[i]);
          String medicineDays = noOfDays[i];
          String medicineQuantity = medQty[i];
          if (!medicineDays.equals("")) {
            med.set("medicine_days", Integer.parseInt(medicineDays));
          }
          if (!medicineQuantity.equals("")) {
            med.set("medicine_quantity", Integer.parseInt(medicineQuantity));
          }
          med.set("medicine_remarks", itemRemarks[i]);
          med.set("consultation_id", consId);

          if (prescribedId.equals("_")) {
            BigDecimal perdayqty = null;
            if (!medicineDays.equals("") && !medicineQuantity.equals("")) {
              BigDecimal qty = new BigDecimal(Integer.parseInt(medQty[i]), new MathContext(2));
              BigDecimal days = new BigDecimal(Integer.parseInt(noOfDays[i]), new MathContext(2));
              perdayqty = qty.divide(days, 2, BigDecimal.ROUND_HALF_UP);
            }

            if (itemMaster[i].equals("")) {
              BasicDynaBean presMedMasterBean = mmDao.getBean();
              presMedMasterBean.set("medicine_name", itemNames[i]);
              presMedMasterBean.set("status", "A");

              if (!mmDao.insert(con, presMedMasterBean)) {
                break txn;
              }
            }

            med.set("op_medicine_pres_id", patMedDao.getNextSequence());
            if (!patMedDao.insert(con, med)) {
              break txn;
            }
            String dosage = medDosages[i].trim();
            BasicDynaBean dbDosageBean = medDosageDao.findByKey("dosage_name", dosage);

            if (dbDosageBean == null) {
              BasicDynaBean dosagebean = medDosageDao.getBean();
              dosagebean.set("dosage_name", dosage);
              dosagebean.set("per_day_qty", perdayqty);
              if (!medDosageDao.insert(con, dosagebean)) {
                break txn;
              }
            }

          } else {
            if (deleteItem) {
              if (!patMedDao.delete(con, "op_medicine_pres_id",
                  Integer.parseInt(prescribedIds[i]))) {
                break txn;
              }
            } else {
              Map keys = new HashMap();
              keys.put("op_medicine_pres_id", Integer.parseInt(prescribedIds[i]));
              if (patMedDao.update(con, med.getMap(), keys) <= 0) {
                break txn;
              }
            }
          }
        } else if (itemType[i].equals("Test")) {
          BasicDynaBean test = patTestDao.getBean();
          test.set("test_name", itemNames[i]);
          test.set("test_remarks", itemRemarks[i]);
          test.set("consultation_id", consId);
          test.set("ispackage", new Boolean(ispackage[i]));

          if (prescribedId.equals("_")) {

            test.set("op_test_pres_id", patTestDao.getNextSequence());
            if (!patTestDao.insert(con, test)) {
              break txn;
            }
          } else {
            if (deleteItem) {
              if (!patTestDao.delete(con, "op_test_pres_id", Integer.parseInt(prescribedIds[i]))) {
                break txn;
              }
            } else {
              Map keys = new HashMap();
              keys.put("op_test_pres_id", Integer.parseInt(prescribedIds[i]));
              if (patTestDao.update(con, test.getMap(), keys) <= 0) {
                break txn;
              }
            }
          }
        } else if (itemType[i].equals("Service")) {
          BasicDynaBean ser = patSerDao.getBean();
          ser.set("service_name", itemNames[i]);
          ser.set("service_remarks", itemRemarks[i]);
          ser.set("consultation_id", consId);

          if (prescribedId.equals("_")) {

            ser.set("op_service_pres_id", patSerDao.getNextSequence());
            if (!patSerDao.insert(con, ser)) {
              break txn;
            }

          } else {
            if (deleteItem) {
              if (!patSerDao.delete(con, "op_service_pres_id",
                  Integer.parseInt(prescribedIds[i]))) {
                break txn;
              }
            } else {
              Map keys = new HashMap();
              keys.put("op_service_pres_id", Integer.parseInt(prescribedIds[i]));
              if (patSerDao.update(con, ser.getMap(), keys) <= 0) {
                break txn;
              }
            }
          }
        } else if (itemType[i].equals("Doctor")) {
          BasicDynaBean doctor = doctorDAO.getBean();
          doctor.set("cons_doctor_name", itemNames[i]);
          doctor.set("cons_remarks", itemRemarks[i]);
          doctor.set("consultation_id", consId);

          if (prescribedId.equals("_")) {
            doctor.set("prescription_id", doctorDAO.getNextSequence());
            if (!doctorDAO.insert(con, doctor)) {
              break txn;
            }

          } else {
            if (deleteItem) {
              if (!doctorDAO.delete(con, "prescription_id", Integer.parseInt(prescribedIds[i]))) {
                break txn;
              }
            } else {
              Map keys = new HashMap();
              keys.put("prescription_id", Integer.parseInt(prescribedIds[i]));
              if (doctorDAO.update(con, doctor.getMap(), keys) <= 0) {
                break txn;
              }
            }
          }
        }
      }
      allSuccess = true;
    }
    return allSuccess;
  }
}