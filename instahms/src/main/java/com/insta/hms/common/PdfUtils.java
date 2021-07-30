package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.genericdocuments.PatientPDFDocValuesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.usermanager.UserSignatureDAO;
import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfBorderDictionary;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PushbuttonField;
import com.lowagie.text.pdf.TextField;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The Class PdfUtils.
 */
public class PdfUtils {

  static Logger logger = LoggerFactory.getLogger(PdfUtils.class);

  /** The document builder factory. */
  static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  /**
   * Send fillable form. Send back a fillable form, either filled or flattened back to the browser.
   * Submit button handling: - We need a special field called _submit in order to be able to create
   * a submit action.
   * - The best approach is to create a text field without a border called _submit at the required
   * location and dimensions. This will be replaced by a push button with the same dimensions, in
   * case the given submit URL is non-null. If the submit URL is null (for viewing/printing), no new
   * button is added.
   * - If, instead, there is a submit button called _submit, itself, we will leave it as is, but
   * replace the value of the submit URL to what is supplied. If URL is null (while viewing) we
   * attempt to remove the field, but it does not work properly (TODO: explory why).
   * 
   * @param os           the os
   * @param is           the is
   * @param values       the values
   * @param flatten      the flatten
   * @param url          the url
   * @param hiddenParams the hidden params
   * @param listOptions  the list options
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws SQLException      the SQL exception
   */
  public static void sendFillableForm(OutputStream os, InputStream is, Map<String, String> values,
      boolean flatten, String url, Map<String, String> hiddenParams, Map<String, List> listOptions)
      throws IOException, DocumentException, SQLException {
    sendFillableForm(os, is, values, flatten, url, hiddenParams, listOptions, -1);
  }

  /**
   * Send fillable form.
   *
   * @param os           the os
   * @param is           the is
   * @param values       the values
   * @param flatten      the flatten
   * @param url          the url
   * @param hiddenParams the hidden params
   * @param listOptions  the list options
   * @param centerId     the center id
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws DocumentException the document exception
   * @throws SQLException      the SQL exception
   */
  public static void sendFillableForm(OutputStream os, InputStream is, Map<String, String> values,
      boolean flatten, String url, Map<String, String> hiddenParams, Map<String, List> listOptions,
      int centerId) throws IOException, DocumentException, SQLException {

    if (!flatten) {
      sendHtmlForm(os, is, values, url, hiddenParams, listOptions, centerId);
      return;
    } else {

      PdfReader reader = new PdfReader(is);
      PdfStamper stamper = new PdfStamper(reader, os);

      AcroFields form = stamper.getAcroFields();

      BaseFont unicode = BaseFont.createFont(
          "/usr/share/fonts/truetype/unicodefonts/arialunicodems.ttf",
          BaseFont.IDENTITY_H, 
          BaseFont.EMBEDDED);
      form.addSubstitutionFont(unicode);

      String mrNo = "";
      String patientId = "";
      String doctorId = "";
      String userId = "";

      // Change the submit URL to what we want
      if (url != null) {
        int type = form.getFieldType("_submit");
        if (type == AcroFields.FIELD_TYPE_PUSHBUTTON) {
          AcroFields.Item field = form.getFieldItem("_submit");
          if (field != null) {
            PRIndirectReference ref = (PRIndirectReference) field.getWidgetRef(0);
            PdfDictionary object = (PdfDictionary) reader.getPdfObject(ref.getNumber());
            // AA and D -- don't know what these are, got this to work by trial and error.
            // From examples in the book, it should have been A and F, and does work in
            // the document that came along with the example (learning_agreement.pdf)
            PdfDictionary action = (PdfDictionary) object.get(PdfName.AA);
            if (action != null) {
              PdfDictionary file = (PdfDictionary) action.get(PdfName.D);
              file.put(PdfName.F, new PdfString(url));
            }
            // logger.debug("setting field bgcolor to white (URL is not null)");
            // form.setFieldProperty("_submit", "bgcolor", Color.white, null);
          }
        } else {
          float[] pos = new float[5];
          int page = reader.getNumberOfPages();
          if (type == AcroFields.FIELD_TYPE_NONE) {
            logger.debug("Adding a brand new push button field");
            pos[1] = 100;
            pos[2] = 100;
            pos[3] = 200;
            pos[4] = 200;
          } else {
            logger.debug("Adding a push over an existing field of type: " + type);
            pos = form.getFieldPositions("_submit");
            logger.debug("Positions: " + pos.length + " " + pos[0] + " " + pos[1] + " " + pos[2]
                + " " + pos[3] + " " + pos[4]);
            form.removeField("_submit");
            page = (int) pos[0];
            // pos[0] = 100; pos[1] = 100; pos[2] = 200; pos[3] = 200;
          }

          PushbuttonField bt = new PushbuttonField(stamper.getWriter(),
              new Rectangle(pos[1], pos[2], pos[3], pos[4]), "_submit");
          bt.setText("Submit");
          bt.setFontSize(0);
          bt.setBackgroundColor(Color.lightGray);
          bt.setBorderStyle(PdfBorderDictionary.STYLE_BEVELED);
          bt.setBorderWidth(1);

          PdfFormField formField = bt.getField();
          PdfAction ac = PdfAction.createSubmitForm(url, null, PdfAction.SUBMIT_HTML_FORMAT);
          formField.setAction(ac);
          stamper.addAnnotation(formField, page);
        }

      } else {
        /*
         * No URL given, hide the submit button: best effort, this does not work very well.
         */
        /*
         * logger.debug("Hiding field _submit"); PushbuttonField bt =
         * form.getNewPushbuttonFromField("_submit"); bt.setVisibility(bt.HIDDEN); bt.setText(
         * "New Text" ); form.replacePushbuttonField("_submit", bt.getField());
         */
        // with above approach, old field remains in addition to new field

        // logger.debug("setting field bgcolor to white (URL is null)");
        // form.setFieldProperty("_submit", "bgcolor", Color.white, null);
        // When flatten does not happen, the field overlays the grey button making it invisible,
        // but when flatten happens, this field itself is removed, and the underlying text/object
        // remains on the page.

        logger.debug("Removing field and unused objects");
        form.removeField("_submit");
        reader.removeUnusedObjects();
      }

      // fill the form with existing values
      if (values != null) {
        for (Map.Entry<String, String> e : values.entrySet()) {
          logger.debug("Filling form field: " + e.getKey());
          logger.debug("Filling form value: " + "=" + e.getValue());
          form.setField(e.getKey(), e.getValue());
          if (e.getKey().startsWith("_")) {
            for (int i = 1; i <= 10; i++) {
              form.setField(e.getKey() + "_" + i, e.getValue());
            }
          }
          if (e.getKey().equals("_mr_no")) {
            mrNo = e.getValue();
          }
          if (e.getKey().equals("_patient_id")) {
            patientId = e.getValue();
          }
          if ("_doctor".equals(e.getKey())) {
            doctorId = e.getValue();
          }
          if ("_username".equals(e.getKey())) {
            userId = e.getValue();
          }
        }
      }

      // fill in any list values
      if (listOptions != null) {
        for (Map.Entry<String, List> e : listOptions.entrySet()) {
          String fieldName = e.getKey();
          int type = form.getFieldType("_submit");
          if ((type == AcroFields.FIELD_TYPE_COMBO) || (type == AcroFields.FIELD_TYPE_LIST)) {
            String[] listItems = (String[]) ((List) e.getValue()).toArray();
            form.setListOption(fieldName, listItems, listItems);
          }
        }
      }

      // Add a hidden field to page 1 for every item in hiddenParams
      if (hiddenParams != null) {
        for (Map.Entry<String, String> e : hiddenParams.entrySet()) {

          logger.info("Adding hidden param: " + e.getKey() + " " + e.getValue());
          TextField textField = new TextField(stamper.getWriter(), new Rectangle(0, 0, 0, 0),
              e.getKey());
          textField.setVisibility(textField.HIDDEN);
          textField.setText(e.getValue());
          PdfFormField pf = textField.getTextField();
          stamper.addAnnotation(pf, 1);

          if (e.getKey().equals("_mr_no")) {
            mrNo = e.getValue();
          }
          if (e.getKey().equals("patient_id") || e.getKey().equals("_patient_id")) {
            patientId = e.getValue();
          }
          if ("doctor".equals(e.getKey()) || "_doctor".equals(e.getKey())) {
            doctorId = e.getValue();
          }
          if ("username".equals(e.getKey()) || "_username".equals(e.getKey())) {
            userId = e.getValue();
          }
        }
      }

      // inserting digital signatures with date and ip address into pdf form
      processPDFExternalImages(hiddenParams, stamper, form);

      // inserting logo into pdf form
      float[] fieldPositions = form.getFieldPositions("_logo");
      if (fieldPositions != null) {
        if (centerId == -1) {
          centerId = RequestContext.getCenterId();
        }
        InputStream logoStream = PrintConfigurationsDAO.getLogo(centerId);
        if (logoStream == null || logoStream.available() == 0) {
          logoStream = PrintConfigurationsDAO.getLogo(0);
        }
        addImageToPDF(logoStream, stamper, fieldPositions);
      }

      // inserting patient photo into the pdf form
      float[] photoPositions = form.getFieldPositions("_patient_photo");
      if (photoPositions != null) {
        if (!mrNo.equals("")) {
          InputStream photoStream = PatientDetailsDAO.getPatientPhoto(mrNo);
          addImageToPDF(photoStream, stamper, photoPositions);
        }
      }

      // inserting insurance card image into the pdf form
      float[] insuranceCardPhotoPositions = form.getFieldPositions("_plan_card");
      if (insuranceCardPhotoPositions != null) {
        if (!patientId.equals("")) {
          InputStream photoStream = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "I");
          addImageToPDF(photoStream, stamper, insuranceCardPhotoPositions);
        }
      }

      // inserting corporate card image into the pdf form
      float[] corporateCardPhotoPositions = form.getFieldPositions("_corporate_card");
      if (corporateCardPhotoPositions != null) {
        if (!patientId.equals("")) {
          InputStream photoStream = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "C");
          addImageToPDF(photoStream, stamper, corporateCardPhotoPositions);
        }
      }

      // inserting national card image into the pdf form
      float[] nationalCardPhotoPositions = form.getFieldPositions("_national_card");
      if (nationalCardPhotoPositions != null) {
        if (!patientId.equals("")) {
          InputStream photoStream = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "N");
          addImageToPDF(photoStream, stamper, nationalCardPhotoPositions);
        }
      }

      // inserting doctor signature into the pdf form
      float[] doctorSignaturePositions = form.getFieldPositions("_doctor_signature");
      if (doctorSignaturePositions != null) {
        if (!StringUtil.isNullOrEmpty(doctorId)) {
          InputStream photoStream = UserSignatureDAO.getDoctorSignature(doctorId);
          addImageToPDF(photoStream, stamper, doctorSignaturePositions);
        }
      }

      // inserting user signature into the pdf form
      float[] userSignaturePositions = form.getFieldPositions("_user_signature");
      if (userSignaturePositions != null) {
        if (!StringUtil.isNullOrEmpty(userId)) {
          InputStream photoStream = UserSignatureDAO.getUserSignature(userId);
          addImageToPDF(photoStream, stamper, userSignaturePositions);
        }
      }

      // flatten for view-only option
      stamper.setFormFlattening(flatten);

      stamper.close();
    }

  }

  /**
   * Send html form.
   *
   * @param os           the os
   * @param is           the is
   * @param values       the values
   * @param url          the url
   * @param hiddenParams the hidden params
   * @param listOptions  the list options
   * @param centerId     the center id
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws BadElementException the bad element exception
   * @throws SQLException        the SQL exception
   */
  public static void sendHtmlForm(OutputStream os, InputStream is, Map<String, String> values,
      String url, Map<String, String> hiddenParams, Map<String, List> listOptions, int centerId)
      throws IOException, BadElementException, SQLException {
    StringBuilder stringBuilder = new StringBuilder();
    String line = null;
    String mrNo = "";
    String patientId = "";
    String doctorId = "";
    String userId = "";

    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
      }
    }
    Document document = Jsoup.parse(stringBuilder.toString(), "UTF-8", Parser.xmlParser());
    Element form = document.getElementById("form");

    // Add the save button to the form.
    Element submitButton = form.appendElement("input");
    submitButton.attr("type", "submit");
    submitButton.attr("value", "Save");
    submitButton.attr("id", "saveBtn");
    
    Element saveAndFinaliseSubmitButton = form.appendElement("input");
    saveAndFinaliseSubmitButton.attr("type", "submit");
    saveAndFinaliseSubmitButton.attr("value", "Save & Finalise");
    saveAndFinaliseSubmitButton.attr("id", "save_finalise");
    saveAndFinaliseSubmitButton.attr("style","display:none");

    // Add the url to the form element.
    form.attr("action", url);

    for (Entry<String, String> input : values.entrySet()) {
      String key = input.getKey();
      String value = input.getValue();
      Elements inputElements;
      if (key.startsWith("_")) {
        inputElements = document.getElementsByAttributeValueMatching("name",
            key + "(_([0-9]|10))?$");
      } else {
        inputElements = document.getElementsByAttributeValue("name", key);
      }
      Element currentInput = inputElements.first();

      // Check if there are no input elements with the name.
      if (null != currentInput) {

        String tagName = currentInput.tagName();
        if ("select".equals(tagName)) {
          Elements options = currentInput.children();
          for (Element option : options) {
            if (value.equals(option.attr("value"))) {
              option.attr("selected", true);
            }
          }
          continue;
        }

        String inputType = currentInput.attr("type");
        // If it is radio button have to check which option to be checked.
        if ("radio".equals(inputType)) {
          for (Element currentOption : inputElements) {
            if (value.equals(currentOption.val())) {
              currentOption.attr("checked", true);
            }
          }
        } else if ("checkbox".equals(inputType)) {
          if (value.equals(currentInput.val())) {
            currentInput.attr("checked", true);
          }
        } else {
          // If text type element just update all input elements with the value.
          for (Element currentElement : inputElements) {
            currentElement.val(value);
          }
        }
      }

      if (input.getKey().equals("_mr_no")) {
        mrNo = input.getValue();
      }
      if (input.getKey().equals("patient_id") || input.getKey().equals("_patient_id")) {
        patientId = input.getValue();
      }
      if ("doctor".equals(input.getKey()) || "_doctor".equals(input.getKey())) {
        doctorId = input.getValue();
      }
      if ("username".equals(input.getKey()) || "_username".equals(input.getKey())) {
        userId = input.getValue();
      }
    }

    // Add all hidden params as hidden form inputs.
    for (Entry<String, String> input : hiddenParams.entrySet()) {
      String key = input.getKey();
      String value = input.getValue();
      Element hiddenInput = form.appendElement("input");
      hiddenInput.attr("type", "hidden");
      hiddenInput.attr("value", value);
      hiddenInput.attr("name", key);

      if (input.getKey().equals("_mr_no")) {
        mrNo = input.getValue();
      }
      if (input.getKey().equals("patient_id") || input.getKey().equals("_patient_id")) {
        patientId = input.getValue();
      }
      if ("doctor".equals(input.getKey()) || "_doctor".equals(input.getKey())) {
        doctorId = input.getValue();
      }
      if ("username".equals(input.getKey()) || "_username".equals(input.getKey())) {
        userId = input.getValue();
      }
    }

    addDefaultImagesToHtml(document, mrNo, patientId, centerId, doctorId, userId);
    addExternalImagesToHtml(document, hiddenParams);

    os.write(document.toString().getBytes());

  }

  /**
   * Adds the image to PDF.
   *
   * @param stream         the stream
   * @param stamper        the stamper
   * @param fieldPositions the field positions
   * @throws DocumentException the document exception
   * @throws IOException       Signals that an I/O exception has occurred.
   */
  private static void addImageToPDF(InputStream stream, PdfStamper stamper, float[] fieldPositions)
      throws DocumentException, IOException {
    if (stream != null && stream.available() > 0) {
      // inserting image into the pdf form
      float fieldPage = fieldPositions[0];
      float fieldLlx = fieldPositions[1];
      float fieldLly = fieldPositions[2];
      float fieldUrx = fieldPositions[3];
      float fieldUry = fieldPositions[4];

      Rectangle rec = new Rectangle(fieldLlx, fieldLly, fieldUrx, fieldUry);
      try {
        Image img = Image.getInstance(DataBaseUtil.readInputStream(stream));

        img.scaleToFit(rec.getWidth(), rec.getHeight());
        img.setAbsolutePosition(fieldLlx + (rec.getWidth() - img.getScaledWidth()) / 2,
            fieldLly + (rec.getHeight() - img.getScaledHeight()) / 2);

        PdfContentByte cb = stamper.getOverContent((int) fieldPage);
        cb.addImage(img);
      } catch (IOException exception) {
        logger.error("Error: Error while adding image to PDF... " + exception.getMessage());
      }
    }
  }

  /**
   * Process PDF external images.
   *
   * @param hiddenParams the hidden params
   * @param stamper      the stamper
   * @param form         the form
   * @throws SQLException      the SQL exception
   * @throws DocumentException the document exception
   * @throws IOException       Signals that an I/O exception has occurred.
   */
  private static void processPDFExternalImages(Map<String, String> hiddenParams, PdfStamper stamper,
      AcroFields form) throws SQLException, DocumentException, IOException {

    if (hiddenParams == null) {
      return;
    }

    Integer docId = (hiddenParams.get("doc_id") != null && !hiddenParams.get("doc_id").equals(""))
        ? Integer.parseInt(hiddenParams.get("doc_id"))
        : null;
    if (docId != null) {
      List<BasicDynaBean> imageFieldvalues = PatientPDFDocValuesDAO.getPDFDocImageValues(docId);
      if (imageFieldvalues != null && imageFieldvalues.size() > 0) {
        for (Object imgObj : imageFieldvalues) {
          BasicDynaBean imgbean = (BasicDynaBean) imgObj;
          Integer docImgeId = (imgbean.get("doc_image_id") != null
              && !imgbean.get("doc_image_id").equals("")) ? (Integer) imgbean.get("doc_image_id")
                  : null;
          if (docImgeId != null) {
            String deviceIp = (String) imgbean.get("device_ip");
            String deviceInfo = (String) imgbean.get("device_info");
            Timestamp captureTime = (Timestamp) imgbean.get("capture_time");

            String fieldName = (String) imgbean.get("field_name");
            Map<String,Object> formFields = form.getFields();
            for (Map.Entry<String, Object> entry : formFields.entrySet()) {
              String regExp = "^(" + fieldName + "_" + "\\d+$" + ")";
              Pattern pattern = Pattern.compile(regExp);
              if (entry.getKey().equalsIgnoreCase(fieldName)
                  || pattern.matcher(entry.getKey()).find()) {
                String tokenToBeProcessed = entry.getKey();
                float[] fieldPositions = form.getFieldPositions(tokenToBeProcessed);
                if (fieldPositions != null) {
                  InputStream fieldStream = PatientPDFDocValuesDAO.getPDFDocImage(docImgeId);
                  addImageToPDF(fieldStream, stamper, fieldPositions);
                }
              }
            }
            form.setField("__date" + fieldName, DateUtil.formatTimestamp(captureTime));
            form.setField("__ip" + fieldName, deviceIp);
            // form.setField("__info"+fieldName, deviceInfo);
          }
        }
      }

      Integer templateId = (hiddenParams.get("template_id") != null
          && !hiddenParams.get("template_id").equals(""))
              ? Integer.parseInt(hiddenParams.get("template_id"))
              : null;

      if (templateId != null) {
        List<BasicDynaBean> imageTemplateFieldvalues = PatientPDFDocValuesDAO
            .getPDFTemplateImageValues(templateId);
        if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {
          String captureTime = DateUtil.formatTimestamp(DateUtil.getCurrentTimestamp());

          for (int i = 0; i < imageTemplateFieldvalues.size(); i++) {
            String fieldId = (String) hiddenParams.get("field_id" + "_" + i);
            String fldInput = (String) hiddenParams.get("field_input" + "_" + i);
            String deviceIp = (String) hiddenParams.get("device_ip" + "_" + i);
            String deviceInfo = (String) hiddenParams.get("device_info" + "_" + i);
            String fldImgText = (String) hiddenParams.get("fieldImgText" + "_" + i);

            if (fieldId != null) {

              BasicDynaBean imgbean = null;
              for (BasicDynaBean bean : imageTemplateFieldvalues) {
                if (Integer.parseInt(fieldId) == (Integer) bean.get("field_id")) {
                  imgbean = bean;
                }
              }

              if (imgbean != null && !imgbean.get("field_name").equals("")) {
                String fieldName = (String) imgbean.get("field_name");
                float[] fieldPositions = form.getFieldPositions(fieldName);
                if (fieldPositions != null) {
                  byte[] decodedBytes = Base64.decodeBase64(fldImgText.getBytes());
                  InputStream fieldStream = new ByteArrayInputStream(decodedBytes);
                  addImageToPDF(fieldStream, stamper, fieldPositions);
                }

                form.setField("__date" + fieldName, captureTime);
                form.setField("__ip" + fieldName, deviceIp);
                // form.setField("__info"+fieldName, deviceInfo);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Adds the default images to html.
   *
   * @param document  the document
   * @param mrNo      the mr no
   * @param patientId the patient id
   * @param centerId  the center id
   * @throws SQLException          the SQL exception
   * @throws BadElementException   the bad element exception
   * @throws MalformedURLException the malformed URL exception
   * @throws IOException           Signals that an I/O exception has occurred.
   */
  private static void addDefaultImagesToHtml(Document document, String mrNo, String patientId,
      int centerId, String doctorId,
      String userId) throws SQLException, BadElementException, MalformedURLException, IOException {
    // String[] imageFieldNames = {"_logo","_patient_photo", "_plan_card", "_corporate_card",
    // "_national_card"};
    // Adding patient image
    Element element = document.getElementsByAttributeValue("name", "_patient_photo").first();
    if (null != element && (!mrNo.equals(""))) {
      InputStream photoStream = PatientDetailsDAO.getPatientPhoto(mrNo);
      if (null != photoStream) {
        createImageElement(element, photoStream, "_patient_photo");
      }
    }

    // Adding logo
    element = document.getElementsByAttributeValue("name", "_logo").first();
    if (null != element) {
      if (centerId == -1) {
        centerId = RequestContext.getCenterId();
      }
      InputStream logoStream = PrintConfigurationsDAO.getLogo(centerId);
      if (logoStream == null || logoStream.available() == 0) {
        logoStream = PrintConfigurationsDAO.getLogo(0);
      }
      if (null != logoStream) {
        createImageElement(element, logoStream, "_logo");
      }
    }

    element = document.getElementsByAttributeValue("name", "_plan_card").first();
    if (null != element && !patientId.equals("")) {
      InputStream photoStream = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "I");
      if (null != photoStream) {
        createImageElement(element, photoStream, "_plan_card");
      }

    }

    element = document.getElementsByAttributeValue("name", "_corporate_card").first();
    if (null != element && !patientId.equals("")) {
      InputStream photoStream = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "C");
      if (null != photoStream) {
        createImageElement(element, photoStream, "_corporate_card");
      }
    }

    element = document.getElementsByAttributeValue("name", "_national_card").first();
    if (null != element && !patientId.equals("")) {
      InputStream photoStream = PatientDetailsDAO.getCurrentPatientCardImage(patientId, "N");
      if (null != photoStream) {
        createImageElement(element, photoStream, "_national_card");
      }
    }

    // Adding doctor signature
    element = document.getElementsByAttributeValue("name", "_doctor_signature").first();
    if (null != element && (!StringUtil.isNullOrEmpty(doctorId))) {
      InputStream photoStream = UserSignatureDAO.getDoctorSignature(doctorId);
      if (null != photoStream) {
        createImageElement(element, photoStream, "_doctor_signature");
      }
    }

    // Adding user signature
    element = document.getElementsByAttributeValue("name", "_user_signature").first();
    if (null != element && (!StringUtil.isNullOrEmpty(userId))) {
      InputStream photoStream = UserSignatureDAO.getUserSignature(userId);
      if (null != photoStream) {
        createImageElement(element, photoStream, "_user_signature");
      }
    }

  }

  /**
   * Adds the external images to html.
   *
   * @param document     the document
   * @param hiddenParams the hidden params
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  private static void addExternalImagesToHtml(Document document, Map<String, String> hiddenParams)
      throws SQLException, IOException {
    if (hiddenParams == null) {
      return;
    }

    Integer docId = (hiddenParams.get("doc_id") != null && !hiddenParams.get("doc_id").equals(""))
        ? Integer.parseInt(hiddenParams.get("doc_id"))
        : null;
    if (docId != null) {
      List<BasicDynaBean> imageFieldvalues = PatientPDFDocValuesDAO.getPDFDocImageValues(docId);
      if (imageFieldvalues != null && !imageFieldvalues.isEmpty()) {
        for (Object imgObj : imageFieldvalues) {
          BasicDynaBean imgbean = (BasicDynaBean) imgObj;
          Integer docImgeId = (imgbean.get("doc_image_id") != null
              && !imgbean.get("doc_image_id").equals("")) ? (Integer) imgbean.get("doc_image_id")
                  : null;
          if (docImgeId != null) {
            // String deviceIp = (String) imgbean.get("device_ip");
            // String deviceInfo = (String) imgbean.get("device_info");
            // Timestamp captureTime = (Timestamp) imgbean.get("capture_time");

            String fieldName = (String) imgbean.get("field_name");
            Element element = document.getElementsByAttributeValue("name", fieldName).first();
            if (null != element) {
              InputStream fieldStream = PatientPDFDocValuesDAO.getPDFDocImage(docImgeId);
              createImageElement(element, fieldStream, fieldName);
            }
          }
        }
      }
    }

  }

  /**
   * Creates the image element.
   *
   * @param element     the element
   * @param imageStream the image stream
   * @param name        the name
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static void createImageElement(Element element, InputStream imageStream, String name)
      throws IOException {
    String style = element.attr("style");
    byte[] imgBytes = DataBaseUtil.readInputStream(imageStream);
    Element div = new Element("div");
    div.attr("style", style);
    div.attr("class", "r");
    Element image = div.appendElement("img");
    String contentType = MimeTypeDetector.getMimeUtil().getMimeTypes(imageStream).toString();
    image.attr("src", "data:" + contentType + ";base64," + Base64.encodeBase64String(imgBytes));
    image.attr("style", "max-width:100%;max-height:100%;");
    image.attr("class", "r");
    image.attr("name", name);

    // element.remove();
    element.parent().appendChild(div);
    element.remove();

  }

  /**
   * Generate user pdf password.
   *
   * @param mrNo the mr no
   * @return the string
   */
  public static String generateUserPdfPassword(String mrNo) {
    String pwd = null;
    String visitSql = "SELECT RIGHT(mr_no, 4) as mr_no, patient_name, expected_dob FROM "
        + " patient_details WHERE mr_no = ? limit 1";
    try {
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(visitSql, mrNo);
      if (bean != null) {
        pwd = (String) bean.get("mr_no");
      }
    } catch (SQLException exception) {
      logger.error(exception.getMessage());
    }
    return pwd;
  }

}
