package com.insta.hms.documents;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class GenerateRegistrationCardService.
 */
@Service
public class GenerateRegistrationCardService extends BusinessService {

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;
  
  /** The patdetailsrepo. */
  @LazyAutowired
  private PatientDetailsRepository patdetailsrepo;
  
  /** The regcardrepo. */
  @LazyAutowired
  private RegistrationCardsRepository regcardrepo;
  
  /** The doctorrepo. */
  @LazyAutowired
  private DoctorRepository doctorrepo;

  /** The photograph. */
  private float[] photograph;
  
  /** The Constant GET_TOKEN_RIGHTS. */
  public static final String GET_TOKEN_RIGHTS = "SELECT op_generate_token "
      + "FROM registration_preferences";

  /** The patregrepo. */
  @LazyAutowired
  private PatientRegistrationRepository patregrepo;

  /**
   * Gets the parameter.
   *
   * @param params the params
   * @param key the key
   * @return the parameter
   */
  public String getParameter(Map<String, String[]> params, String key) {
    Object[] obj = params.get(key);
    if (obj == null || obj[0] == null) {
      return null;
    }
    return obj[0].toString();
  }

  /**
   * Execute.
   *
   * @param params the params
   * @param requestMap the request map
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   */
  public void execute(Map<String, String[]> params, Map<String, Object> requestMap,
      HttpServletResponse response) throws IOException, DocumentException {

    String patientId = getParameter(params, "patient_id");
    String mrNo = getParameter(params, "mr_no");
    List<String> columns = new ArrayList<>();
    columns.add("mr_no");
    columns.add("visit_type");
    List<BasicDynaBean> patientRegBeans = patregrepo.listAll(columns, "patient_id", patientId);
    if (mrNo == null || mrNo.equals("")) {
      mrNo = (String) patientRegBeans.get(0).get("mr_no");
    }
    String visitType = null;
    if (patientId != null && !patientId.equals("")) {
      visitType = patientRegBeans.get(0).get("visit_type").toString().toUpperCase();
    }
    String orgId = getParameter(params, "org_id");
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("visit_type", visitType);
    filterMap.put("rate_plan", orgId);
    filterMap.put("status", 'A');
    List<String> regCardColumns = new ArrayList<>();
    regCardColumns.add("custom_reg_card_template");
    List<BasicDynaBean> beans = regcardrepo.listAll(regCardColumns, filterMap, null);
    if (beans.isEmpty()) {
      filterMap.put("visit_type", 'A');
      filterMap.put("rate_plan", orgId);
      filterMap.put("status", 'A');
      beans = regcardrepo.listAll(regCardColumns, filterMap, null);
    }
    if (beans.isEmpty()) {
      filterMap.put("visit_type", visitType);
      filterMap.put("rate_plan", "");
      filterMap.put("status", 'A');
      beans = regcardrepo.listAll(regCardColumns, filterMap, null);
    }
    if (beans.isEmpty()) {
      filterMap.put("visit_type", 'A');
      filterMap.put("rate_plan", "");
      filterMap.put("status", 'A');
      beans = regcardrepo.listAll(regCardColumns, filterMap, null);
    }
    InputStream templ = null;
    if (!beans.isEmpty()) {
      templ = (InputStream) beans.get(0).get("custom_reg_card_template");
    }

    if (templ == null) {
      String error = "No Valid Registration Card Template for this Patient to print.";
      requestMap.put("error", error);
      return;
    }
    Image gif = null;
    InputStream imageStream = (InputStream) patdetailsrepo.findByKey("mr_no", mrNo)
        .get("patient_photo");
    byte[] image = null;
    if (null != imageStream) {
      image = IOUtils.toByteArray(imageStream);
    }
    if (image != null) {
      gif = Image.getInstance(image);
    }

    /*
     * They have defined a registration card template, use that
     */
    // PdfReader pdfIn = new PdfReader("/tmp/RegistrationCard.pdf");
    // PdfStamper pdfOut = new PdfStamper(pdfIn, new
    // FileOutputStream("/tmp/filled.pdf"));
    PdfReader pdfIn = new PdfReader(templ);
    PdfStamper pdfOut = new PdfStamper(pdfIn, response.getOutputStream());
    AcroFields pdfForm = pdfOut.getAcroFields();
    if (pdfForm == null) {
      String error = "could not get acro fields from PDF form";
      requestMap.put("error", error);
    }
    response.setContentType("application/pdf");
    SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");

    /**
     * All the patient details fetched are set as pdf form fields. and all the form fields names are
     * same as the column names in the query used to fecth patient details.
     * If any of the field names (which are being used in the existing Card Pdf form) does not match
     * with the query column names then set them separately using the pdf form field name.
     */
    Map patientDetailsMap = null;
    if (patientId != null && !patientId.equals("") && !patientId.equals("No")) {
      patientDetailsMap = patregrepo.getPatientVisitDetailsMap(patientId);
    } else {
      if (null != PatientDetailsRepository.getPatientGeneralDetailsBean(mrNo)) {
        patientDetailsMap = PatientDetailsRepository.getPatientGeneralDetailsBean(mrNo).getMap();
      }
    }

    for (Map.Entry e : (Collection<Map.Entry>) patientDetailsMap.entrySet()) {
      String key = (String) e.getKey();
      Object value = e.getValue();
      String valueString = null;
      if (value != null) {
        if (value instanceof java.sql.Date) {
          valueString = fmt.format(value);
        } else if (value instanceof java.sql.Time) {
          valueString = fmt.format(value);
        } else if (value instanceof String) {
          valueString = (String) value;
        } else if (value instanceof java.sql.Timestamp) {
          SimpleDateFormat timeStampFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
          valueString = timeStampFormatter.format((java.sql.Timestamp) value);
        } else {
          valueString = value.toString();
        }
        pdfForm.setField(key, valueString);
        for (int i = 1; i <= 2; i++) {
          pdfForm.setField(key + i, valueString);
        }
      }
    }

    if (patientDetailsMap == null) {
      String error = "Invalid patient ID: " + patientId;
      requestMap.put("error", error);
    }

    /**
     * 1. Set the form field values if any of the field names does not match with the query column
     * names then set them separately using the pdf form field name. This is for backward
     * compatibility of the existing form fields.
     * 2. Also, if any new fields which are not available in the fetched patient details, then set
     * the form fields separately.
     */

    String doctorId = patientDetailsMap.containsKey("doctor")
        ? (String) patientDetailsMap.get("doctor")
        : null;

    if (doctorId != null && !doctorId.equals("")) {
      BasicDynaBean docBean = doctorrepo.findByKey("doctor_id", doctorId);
      if (docBean.get("specialization") != null) {
        pdfForm.setField("specialization", docBean.get("specialization").toString());
      }
      if (docBean.get("qualification") != null) {
        pdfForm.setField("qualification", docBean.get("qualification").toString());
      }
      if (docBean.get("registration_no") != null) {
        pdfForm.setField("registration_no", docBean.get("registration_no").toString());
      }
    }

    pdfForm.setField("mrNo", patientDetailsMap.get("mr_no").toString());
    pdfForm.setField("patientId", patientId);
    pdfForm.setField("title", patientDetailsMap.get("salutation").toString());
    pdfForm.setField("firstName", patientDetailsMap.get("patient_name").toString());
    pdfForm.setField("lastName",
        (patientDetailsMap.get("last_name") != null ? patientDetailsMap.get("last_name").toString()
            : ""));
    // pdfForm.setField("gender",
    // patientDetailsMap.get("patient_gender").equals("M") ? "Male" :
    // "Female");
    if (patientDetailsMap.get("patient_gender").equals("M")) {
      pdfForm.setField("gender", "Male");
    } else if (patientDetailsMap.get("patient_gender").equals("F")) {
      pdfForm.setField("gender", "Female");
    } else {
      pdfForm.setField("gender", "Couple");
    }
    pdfForm.setField("fullName", patientDetailsMap.get("full_name").toString());

    pdfForm.setField("age",
        patientDetailsMap.get("age").toString() + " " + patientDetailsMap.get("agein").toString());

    if (patientDetailsMap.containsKey("reg_time") && patientDetailsMap.get("reg_time") != null) {
      SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
      pdfForm.setField("regtime", timeFormatter.format(patientDetailsMap.get("reg_time")));
    }
    if (patientDetailsMap.get("patient_phone") != null) {
      pdfForm.setField("phone",
          (patientDetailsMap.get("patient_phone") != null
              ? patientDetailsMap.get("patient_phone").toString()
              : ""));
    }
    if (patientDetailsMap.get("patient_address") != null) {
      pdfForm.setField("address",
          (patientDetailsMap.get("patient_address") != null
              ? patientDetailsMap.get("patient_address").toString()
              : ""));
    }
    if (patientDetailsMap.get("dateofbirth") != null) {
      pdfForm.setField("dateOfBirth", fmt.format(patientDetailsMap.get("dateofbirth")));
    }
    if (patientDetailsMap.get("reg_date") != null) {
      pdfForm.setField("regDate", fmt.format(patientDetailsMap.get("reg_date")));
    }
    if (patientDetailsMap.get("reg_date") != null) {
      pdfForm.setField("regDate", fmt.format(patientDetailsMap.get("reg_date")));
    }
    if (patientDetailsMap.get("cityname").toString() != null) {
      pdfForm.setField("city", patientDetailsMap.get("cityname").toString());
    }
    if (patientDetailsMap.get("statename") != null) {
      pdfForm.setField("state", patientDetailsMap.get("statename").toString());
    }
    if (patientDetailsMap.get("custom_list5_value") != null) {
      pdfForm.setField("religion", patientDetailsMap.get("custom_list5_value").toString());
    }
    if (patientDetailsMap.get("custom_list6_value") != null) {
      pdfForm.setField("occupation", patientDetailsMap.get("custom_list6_value").toString());
    }
    if (patientDetailsMap.containsKey("doctor_name")
        && patientDetailsMap.get("doctor_name") != null) {
      pdfForm.setField("admitdoctor", patientDetailsMap.get("doctor_name").toString());
    }
    if (patientDetailsMap.containsKey("dept_name") && patientDetailsMap.get("dept_name") != null) {
      pdfForm.setField("department", patientDetailsMap.get("dept_name").toString());
    }
    if (patientDetailsMap.get("patrelation") != null) {
      pdfForm.setField("relationName", patientDetailsMap.get("patrelation").toString());
    }
    if (patientDetailsMap.get("pataddress") != null) {
      pdfForm.setField("contactPersonAddress", patientDetailsMap.get("pataddress").toString());
    }
    if (patientDetailsMap.get("patcontactperson") != null) {
      pdfForm.setField("contactPerson", patientDetailsMap.get("patcontactperson").toString());
    }
    if (patientDetailsMap.get("custom_list4_value") != null) {
      pdfForm.setField("bloodgroup", patientDetailsMap.get("custom_list4_value").toString());
    }
    if (patientDetailsMap.containsKey("refdoctorname")
        && patientDetailsMap.get("refdoctorname") != null) {
      pdfForm.setField("refferdBy", patientDetailsMap.get("refdoctorname").toString());
    }
    if (patientDetailsMap.containsKey("tpa_name") && patientDetailsMap.get("tpa_name") != null) {
      pdfForm.setField("tpa", patientDetailsMap.get("tpa_name").toString());
    }
    if (patientDetailsMap.containsKey("alloc_bed_type")
        && patientDetailsMap.get("alloc_bed_type") != null) {
      pdfForm.setField("bed", patientDetailsMap.get("alloc_bed_type").toString());
    }
    if (patientDetailsMap.containsKey("alloc_ward_name")
        && patientDetailsMap.get("alloc_ward_name") != null) {
      pdfForm.setField("ward", patientDetailsMap.get("alloc_ward_name").toString());
    }
    if (patientDetailsMap.containsKey("tpa_name") && patientDetailsMap.get("tpa_name") != null) {
      pdfForm.setField("tpaName", patientDetailsMap.get("tpa_name").toString());
    }
    if (patientDetailsMap.containsKey("org_name") && patientDetailsMap.get("org_name") != null) {
      pdfForm.setField("orgName", patientDetailsMap.get("org_name").toString());
    }
    if (patientDetailsMap.get("custom_field1") != null) {
      pdfForm.setField("customField1", patientDetailsMap.get("custom_field1").toString());
    }
    if (patientDetailsMap.get("custom_field2") != null) {
      pdfForm.setField("customField2", patientDetailsMap.get("custom_field2").toString());
    }
    if (patientDetailsMap.get("custom_field3") != null) {
      pdfForm.setField("customField3", patientDetailsMap.get("custom_field3").toString());
    }

    String tokenRights = DatabaseHelper.getString(GET_TOKEN_RIGHTS);
    if ("O".equals(visitType) && "Y".equals(tokenRights) && doctorId != null
        && !doctorId.equals("")) {
      String tokenDBQuery = "SELECT consultation_token FROM doctor_consultation WHERE patient_id='"
          + patientId + "' AND doctor_name = '" + doctorId + "'" 
          + " ORDER BY consultation_id  LIMIT 1";
      pdfForm.setField("tokenNo", DatabaseHelper.getString(tokenDBQuery));
    }
    photograph = pdfForm.getFieldPositions("patientphoto");
    if ((image != null) && (photograph != null)) {
      Image img = Image.getInstance(gif);
      PdfContentByte cb = pdfOut.getOverContent((int) photograph[0]);
      img.scaleToFit(70, 80);
      img.setAbsolutePosition(photograph[1] + (10) / 2f, photograph[2] + (20) / 2f);

      cb.addImage(img);
    }

    setCardImage(pdfForm, pdfOut, patientId, "I");
    setCardImage(pdfForm, pdfOut, patientId, "C");
    setCardImage(pdfForm, pdfOut, patientId, "N");

    pdfForm.setField("mr_no_barcode", "*" + patientDetailsMap.get("mr_no").toString() + "*");
    pdfForm.setField("mrNos", patientDetailsMap.get("mr_no").toString());
    pdfForm.setField("fullNames", patientDetailsMap.get("salutation") + " "
        + patientDetailsMap.get("patient_name") + " " + patientDetailsMap.get("last_name"));
    pdfForm.setField("ages",
        patientDetailsMap.get("age").toString() + " " + patientDetailsMap.get("agein").toString());
    if (patientDetailsMap.get("patient_gender").equals("M")) {
      pdfForm.setField("genders", "Male");
    } else if (patientDetailsMap.get("patient_gender").equals("F")) {
      pdfForm.setField("genders", "Female");
    } else {
      pdfForm.setField("genders", "Couple");
    }
    String username = (String) sessionService.getSessionAttributes().get("userid");
    pdfForm.setField("userName", username);

    pdfOut.setFormFlattening(true);
    pdfOut.close();

  }

  /**
   * Sets the card image.
   *
   * @param pdfForm the pdf form
   * @param pdfOut the pdf out
   * @param patientId the patient id
   * @param sponsorType the sponsor type
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   */
  public static void setCardImage(AcroFields pdfForm, PdfStamper pdfOut, String patientId,
      String sponsorType) throws IOException, DocumentException {
    float[] cardPhotoPositions = null;
    if (sponsorType.equals("I")) {
      cardPhotoPositions = pdfForm.getFieldPositions("_plan_card");
    } else if (sponsorType.equals("C")) {
      cardPhotoPositions = pdfForm.getFieldPositions("_corporate_card");
    } else if (sponsorType.equals("N")) {
      cardPhotoPositions = pdfForm.getFieldPositions("_national_card");
    }
    if (cardPhotoPositions != null && (!patientId.equals(""))) {
      InputStream cardImageStream = (InputStream) PatientDetailsRepository
          .getCurrentPatientCardImage(patientId, sponsorType).get("doc_content_bytea");
      if (cardImageStream != null && cardImageStream.available() > 0) {
        // inserting plan card image into the pdf form
        float fieldPage = cardPhotoPositions[0];
        float fieldLlx = cardPhotoPositions[1];
        float fieldLly = cardPhotoPositions[2];
        float fieldUrx = cardPhotoPositions[3];
        float fieldUry = cardPhotoPositions[4];

        Rectangle rec = new Rectangle(fieldLlx, fieldLly, fieldUrx, fieldUry);
        Image png = Image.getInstance(IOUtils.toByteArray(cardImageStream));

        png.scaleToFit(rec.getWidth(), rec.getHeight());
        png.setAbsolutePosition(fieldLlx + (rec.getWidth() - png.getScaledWidth()) / 2,
            fieldLly + (rec.getHeight() - png.getScaledHeight()) / 2);
        PdfContentByte pcb = pdfOut.getOverContent((int) fieldPage);
        pcb.addImage(png);
      }
    }
  }

}
