package com.insta.hms.documents;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AwsS3Util;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.minio.MinioPatientDocumentsService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.exception.HMSException;
import com.insta.hms.genericdocuments.CommonHelper;

import eu.medsea.mimeutil.MimeUtil2;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;


// TODO: Auto-generated Javadoc
/**
 * The Class AbstractDocumentStore.
 */
public abstract class AbstractDocumentStore {

  /** The document type. */
  private String documentType;

  /** The specialized. */
  private boolean specialized;

  /** The Constant MAXIMUM_SIZE. */
  private static final Long MAXIMUM_SIZE = 10L * 1024L * 1024L;

  /** The RedisTemplate. */
  @LazyAutowired
  private RedisTemplate<String, Object> redisTemplate;

  /**
   * Gets the document type.
   *
   * @return the document type
   */
  public String getDocumentType() {
    return documentType;
  }

  /**
   * Checks if is specialized.
   *
   * @return true, if is specialized
   */
  public boolean isSpecialized() {
    return specialized;
  }

  /**
   * Instantiates a new abstract document store.
   *
   * @param documentType the document type
   * @param specialized the specialized
   */
  public AbstractDocumentStore(String documentType, boolean specialized) {
    this.documentType = documentType;
    this.specialized = specialized;
  }

  /** The patientdocrepo. */
  @LazyAutowired
  private PatientDocumentRepository patientdocrepo;

  /** The patient gen doc repo. */
  @LazyAutowired
  private PatientGeneralDocsRepository patientGenDocRepo;

  /** The session service. */
  @LazyAutowired
  protected SessionService sessionService;

  /** The aws S3 util. */
  @LazyAutowired
  private AwsS3Util awsS3Util;

  /** The minio patient documents service. */
  @LazyAutowired
  MinioPatientDocumentsService minioPatientDocumentsService;

  /** The hvfdocvaluesrepo. */
  @LazyAutowired
  private PatientHvfDocValuesRepository hvfdocvaluesrepo;

  /** The pdfdocvaluesrepo. */
  @LazyAutowired
  private PatientPdfDocImagesRepository pdfdocvaluesrepo;

  /** The hvf image valuesrepo. */
  @LazyAutowired
  private PatientHvfDocImagesRepository hvfImageValuesrepo;

  /** The pdfvaluesrepo. */
  @LazyAutowired
  private PatientPdfFormValuesRepository pdfvaluesrepo;

  /** The hvf fieldsrepo. */
  @LazyAutowired
  private HvfTemplateFieldsRepository hvfFieldsrepo;

  /** The hvfimagevaluesrepo. */
  @LazyAutowired
  private PatientHvfDocImagesRepository hvfimagevaluesrepo;

  /**
   * Gets the documents list.
   *
   * @param key the key
   * @param valueCol the value col
   * @param specialized the specialized
   * @param specializedDocType the specialized doc type
   * @return the documents list
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract List<BasicDynaBean> getDocumentsList(String key, Object valueCol,
      Boolean specialized, String specializedDocType) throws IOException;

  /**
   * Search documents.
   *
   * @param listingParams the listing params
   * @param extraParams the extra params
   * @param specialized the specialized
   * @return the list
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public abstract List<BasicDynaBean> searchDocuments(Map listingParams, Map extraParams,
      Boolean specialized) throws IOException, ParseException;

  /*
   * Replaceable fields are static fields that can be plugged into the document. This is done for
   * both template based documents (at the time of generating the document before edits) as well as
   * field based documents (at the time of display/print). Replaceable fields depends on the entity
   * that the document is attached to.
   *
   * Example: Patient documents: all patient_details fields can be plugged in Visit related
   * documents: all patient_details and patient_registration (and ext.) fields Insurance Documents:
   * this is attached to a case, so all case details (in addition to the patient details) can be
   * plugged in.
   *
   * In order to find the set of field values that need to be plugged in, we need to identify what
   * the document is attached to. This is the "key" for the document. Example keys: Patient
   * documents: mr_no Visit documents: patient_id Insurance documents: insurance_id
   *
   * Note that there is nothing specific to the doc_id that can affect replaceable fields. But, we
   * may be required to fill in a document with replaceable fields given a doc_id. In that case, the
   * entity to which the doc is attached can be got from the doc_id and then we can get the key of
   * the entity to get the replaceable fields.
   *
   * Also note that this does not enforce that a document be related to a patient.
   */

  /**
   * Copy replaceable fields.
   *
   * @param fields the fields
   * @param keyParams the key params
   * @param underscore the underscore
   * @throws SQLException the SQL exception
   */
  public abstract void copyReplaceableFields(Map fields, Map keyParams, boolean underscore)
      throws SQLException;

  /**
   * Gets the doc key params.
   *
   * @param docid the docid
   * @return the doc key params
   */
  /*
   * Return the key param values for the given doc ID
   */
  public abstract Map<String, Object> getDocKeyParams(int docid);

  /**
   * Copy document details.
   *
   * @param docid the docid
   * @param to the to
   */
  public abstract void copyDocumentDetails(int docid, Map to);

  /**
   * Post create.
   *
   * @param docid the docid
   * @param requestParams the request params
   * @param errors the errors
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract boolean postCreate(int docid, Map requestParams, List errors) throws IOException;

  /**
   * Post update.
   *
   * @param docid the docid
   * @param requestParams the request params
   * @param errors the errors
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract boolean postUpdate(int docid, Map requestParams, List errors) throws IOException;

  /**
   * Post delete.
   *
   * @param docId the doc id
   * @param format the format
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract boolean postDelete(Object docId, String format) throws IOException;

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  /*
   * Get the key(s) as a map pointing to a null value: this is a list of field names that uniquely
   * determines the object/entity that the document is attached to. Eg, insurance_id for Insurance
   * Documents.
   */
  public abstract Map<String, Object> getKeys();

  /**
   * Populate keys.
   *
   * @param requestParams the request params
   * @param keys the keys
   * @return the map
   */
  public Map populateKeys(Map requestParams, Map<String, Object> keys) {

    if (keys == null) {
      keys = getKeys();
    }

    for (Entry<String, Object> entry : keys.entrySet()) {
      keys.put(entry.getKey(), CommonHelper.getValueFromMap(requestParams, entry.getKey()));
    }

    return keys;
  }

  /**
   * Gets the center id.
   *
   * @param requestParams the request params
   * @return the center id
   */
  public abstract int getCenterId(Map requestParams);

  /** The mime util. */
  private static MimeUtil2 mimeUtil = null;

  /**
   * Gets the mime util.
   *
   * @return the mime util
   */
  private MimeUtil2 getMimeUtil() {
    if (mimeUtil == null) {
      mimeUtil = new MimeUtil2();
      mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }
    return mimeUtil;
  }

  /**
   * Creates the.
   *
   * @param requestParams the request params
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, Object> create(Map<String, Object[]> requestParams) throws IOException {
    List errors = new ArrayList();

    BasicDynaBean patientdocbean = patientdocrepo.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientdocbean, errors);
    String fileName = null;
    MultipartFile file = null;
    if (requestParams.get("fileName") != null) {
      file = ((MultipartFile) requestParams.get("fileName")[0]);
      fileName = file.getOriginalFilename();
    }
    int docId = 0;
    docId = patientdocrepo.getNextSequence();
    patientdocbean.set("doc_id", docId);
    String format = CommonHelper.getValueFromMap(requestParams, "format", 0);
    patientdocbean.set("doc_format", format);
    patientdocbean.set("is_migrated", "0");
    if (requestParams.get("center_id") == null) {
      patientdocbean.set("center_id", RequestContext.getCenterId());
    }
    patientdocbean.set("doc_type",
        requestParams.get("mr_no") != null && requestParams.get("document_type") != null
            ? requestParams.get("document_type")[0] : getDocumentType());
    String docSeqPattern = CommonHelper.getValueFromMap(requestParams, "doc_seq_pattern_id", 0);
    if (docSeqPattern != null) {
      patientdocbean.set("doc_number", DatabaseHelper.getNextPatternId(docSeqPattern));
    }

    MimeUtil2 mimeUtil = getMimeUtil();

    if (fileName != null && fileName.toString().contains(".")) {
      String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
      patientdocbean.set("original_extension", extension);

      if (extension.equals("odt") || extension.equals("ods")) {
        patientdocbean.set("content_type", "application/vnd.oasis.opendocument.text");
      } else {
        String fileMime = file.getContentType();
        if (fileMime == null) {
          fileMime = mimeUtil.getMimeTypes(file.getBytes()).toString();
        }
        patientdocbean.set("content_type", fileMime);
      }
    }

    Object[] object = requestParams.get("field_id");
    List list = new ArrayList();
    if (object != null && object[0] != null) {
      for (int i = 0; i < object.length; i++) {
        if (CommonHelper.getValueFromMap(requestParams, "field_value", i) != null) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesrepo.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          hvfdocvaluesbean.set("doc_id", docId);
          int valueId = hvfdocvaluesrepo.getNextSequence();
          hvfdocvaluesbean.set("value_id", valueId);
          list.add(hvfdocvaluesbean);
        }
      }
    }
    String success = null;
    String error = null;
    boolean status = true;
    try {
      if (errors.isEmpty()) {
        boolean minioUploadStatus = true;
        if (file != null) {
          minioUploadStatus = saveDocument(patientdocbean, file, requestParams);
        }
        HttpSession session = RequestContext.getSession();
        String username = StringUtils.isEmpty(requestParams.get("username"))
            ? (String) session.getAttribute("userid") : (String) requestParams.get("username")[0];
        String schema = RequestContext.getSchema();
        String redisKey = "docId:" + docId + "userId:" + username + "sch:" + schema;
        redisTemplate.opsForValue().set(redisKey, patientdocbean.getMap().toString());
        redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
        boolean patientdocflag = patientdocrepo.insert(patientdocbean) > 0;
        boolean patientdocvaluesflag = true;
        boolean patienthvfimagevaluesflag = true;

        if (format != null && format.equals("doc_hvf_templates")) {
          if (!list.isEmpty()) {
            // inserts hvf field values.
            patientdocvaluesflag = false;

            List imgValueBeanList = new ArrayList();

            PatientHvfDocImagesRepository hvfimagevaluesrepo = new PatientHvfDocImagesRepository();

            String[] fieldIdArr = (String[]) requestParams.get("field_id");
            String[] fldInputArr = (String[]) requestParams.get("field_input");
            String[] deviceIpArr = (String[]) requestParams.get("device_ip");
            String[] deviceInfoArr = (String[]) requestParams.get("device_info");
            String[] fldImgTextArr = (String[]) requestParams.get("fieldImgText");

            for (int i = 0; i < fieldIdArr.length; i++) {

              if (fldInputArr[i] != null && fldInputArr[i].equals("E")) {

                BasicDynaBean imgValueBean = hvfimagevaluesrepo.getBean();
                imgValueBean.set("doc_image_id", hvfimagevaluesrepo.getNextSequence());
                imgValueBean.set("doc_id", docId);
                imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

                byte[] decodedBytes = Base64.decodeBase64(fldImgTextArr[i].getBytes());
                InputStream is = new ByteArrayInputStream(decodedBytes);

                imgValueBean.set("field_image", is);
                imgValueBean.set("field_image_content_type", "image/png");
                imgValueBean.set("device_ip", deviceIpArr[i]);
                imgValueBean.set("device_info", deviceInfoArr[i]);
                imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());

                imgValueBeanList.add(imgValueBean);
              }
            }

            if (!imgValueBeanList.isEmpty()) {
              patienthvfimagevaluesflag = hvfimagevaluesrepo.insertAll(imgValueBeanList);
            }

            patientdocvaluesflag = patienthvfimagevaluesflag && hvfdocvaluesrepo.insertAll(list);

          }

        } else if (format != null && format.equals("doc_pdf_form_templates")) {
          patientdocvaluesflag = false;
          PatientPdfFormValuesRepository patientpdfrepo = new PatientPdfFormValuesRepository();
          patientdocvaluesflag = patientpdfrepo.insertPDFFormFieldValues(requestParams, docId,
              username);
        }
        if (requestParams.containsKey("path")) {
          String path = (String) requestParams.get("path")[0];
          minioPatientDocumentsService.insert(docId, path);
        }

        boolean otherTx = postCreate(docId, requestParams, errors);

        if (patientdocflag && patientdocvaluesflag && otherTx && minioUploadStatus) {
          status = true;
        } else {
          status = false;
        }

      } else {
        status = false;
        error = "Incorrectly formatted values supplied";
      }
    } finally {
      if (status) {
        success = "Document inserted successfully..";
      } else {
        if (!errors.isEmpty()) {
          error = Arrays.toString(requestParams.get("error"));
        }
        error = error == null ? "Failed to insert Document.." : error;
      }

      requestParams.put("success", new String[] { success });
      requestParams.put("error", new String[] { error });
    }
    Map<String, Object> result = new HashMap<>();
    result.put("status", status);
    result.put("docId", docId);
    return result;
  }

  protected Boolean isMigratedToMinio() {
    return false;
  }

  private Boolean saveDocument(BasicDynaBean patientdocbean, MultipartFile file,
      Map<String, Object[]> requestParams) {
    if (isMigratedToMinio() && EnvironmentUtil.isMinioEnabled()) {
      return saveDocumentToMinio(patientdocbean, file, requestParams);
    }
    return setDocContentBytea(patientdocbean, file);
  }

  private Boolean updateDocument(BasicDynaBean patientdocbean, MultipartFile file,
      Boolean isMigrated) {
    if (isMigrated) {
      return updateDocumentInMinio(patientdocbean, file);
    }
    return setDocContentBytea(patientdocbean, file);
  }

  protected Boolean setDocContentBytea(BasicDynaBean patientdocbean, MultipartFile file) {
    Object fileObj;
    try {
      fileObj = file.getInputStream();
    } catch (IOException ioException) {
      throw new HMSException("Error while getting the inputStream");
    }
    patientdocbean.set("doc_content_bytea", fileObj);
    return true;
  }

  protected Boolean saveDocumentToMinio(BasicDynaBean patientdocbean, MultipartFile file,
      Map<String, Object[]> requestParams) {
    String path = generateMinioDocumentPath(patientdocbean);
    requestParams.put("path", new String[] { path });
    patientdocbean.set("is_migrated", "1");
    return awsS3Util.setDocumentForPatientDocuments(path, file);
  }

  protected Boolean updateDocumentInMinio(BasicDynaBean patientdocbean, MultipartFile file) {
    String path = generateMinioDocumentPath(patientdocbean);
    Boolean uploaded = awsS3Util.setDocumentForPatientDocuments(path, file);
    Integer docId = (Integer) patientdocbean.get("doc_id");
    Boolean rowUpdated = minioPatientDocumentsService.update(docId, path) > 0;
    return uploaded && rowUpdated;
  }

  protected String generateMinioDocumentPath(BasicDynaBean patientdocbean) {
    Integer docId = (Integer) patientdocbean.get("doc_id");
    String extension = (String) patientdocbean.get("original_extension");
    String schemaName = (String) sessionService.getSessionAttributes().get("sesHospitalId");
    String timeStamp = Long.toString(new Date().getTime());
    return schemaName + "/" + docId + timeStamp + "." + extension;
  }

  /**
   * Bulk upload.
   *
   * @param requestParams the request params
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, Object> bulkUpload(Map<String, Object[]> requestParams) throws IOException {
    List errors = new ArrayList();

    Integer docId = 0;
    List<String> fileSizeError = new ArrayList<>();
    List<Integer> docIds = new ArrayList<>();
    String docSeqPattern = CommonHelper.getValueFromMap(requestParams, "doc_seq_pattern_id", 0);
    String format = CommonHelper.getValueFromMap(requestParams, "format", 0);
    Object fileObj = null;
    String fileName = null;
    MultipartFile[] files = null;
    List<BasicDynaBean> patientdocbeans = null;
    String error = null;
    if (requestParams.get("fileName") != null) {
      files = (MultipartFile[]) (requestParams.get("fileName"));
      patientdocbeans = new ArrayList<>();
    }
    for (MultipartFile file : files) {
      BasicDynaBean patientdocbean = patientdocrepo.getBean();
      if (file.getSize() > MAXIMUM_SIZE) {
        errors.add("File Size Issue");
        fileSizeError.add("Uploaded file " + file.getOriginalFilename() + " exceeds "
            + MAXIMUM_SIZE / (1024 * 1024) + " MB");
      }
      ConversionUtils.copyToDynaBean(requestParams, patientdocbean, errors);
      fileObj = file.getInputStream();
      fileName = file.getOriginalFilename();
      patientdocbean.set("doc_content_bytea", fileObj);
      docId = patientdocrepo.getNextSequence();
      docIds.add(docId);
      patientdocbean.set("doc_id", docId);
      patientdocbean.set("doc_format", format);
      patientdocbean.set("doc_type", getDocumentType());

      if (docSeqPattern != null) {
        patientdocbean.set("doc_number", DatabaseHelper.getNextPatternId(docSeqPattern));
      }

      MimeUtil2 mimeUtil2 = getMimeUtil();

      if (fileName != null && fileName.contains(".")) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        patientdocbean.set("original_extension", extension);

        if (extension.equals("odt") || extension.equals("ods")) {
          patientdocbean.set("content_type", "application/vnd.oasis.opendocument.text");
        } else {
          String fileMime = file.getContentType();
          if (fileMime == null) {
            fileMime = mimeUtil2.getMimeTypes(file.getBytes()).toString();
          }
          patientdocbean.set("content_type", fileMime);
        }
      }

      int centerId = getCenterId(requestParams);
      patientdocbean.set("center_id", centerId);
      HttpSession session = RequestContext.getSession();
      String schema = RequestContext.getSchema();
      String username = (String) session.getAttribute("userid");
      String redisKey = "docId:" + docId + "userId:" + username + "sch:" + schema;
      String data = (patientdocbean.getMap()).toString();
      redisTemplate.opsForValue().set(redisKey, data);
      redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES);
      patientdocbeans.add(patientdocbean);
    }
    String success = null;
    boolean status = false;
    try {
      if (errors.isEmpty()) {
        // inserts into the patient_documents
        patientdocrepo.batchInsert(patientdocbeans);
        status = true;
      }
    } finally {
      if (status) {
        success = "Document inserted successfully..";
      } else {
        if (!errors.isEmpty()) {
          error = "Failed to insert Document..";
        }
      }

      requestParams.put("success", new String[] { success });
      if (!fileSizeError.isEmpty()) {
        String[] fileSizeErrorArray = new String[fileSizeError.size()];
        fileSizeError.toArray(fileSizeErrorArray);
        requestParams.put("error", fileSizeErrorArray);
      } else {
        requestParams.put("error", new String[] { error });
      }
    }
    Map<String, Object> result = new HashMap<>();
    result.put("status", status);
    result.put("docId", docIds);
    return result;
  }

  /**
   * Update.
   *
   * @param requestParams the request params
   * @param saveExtFields the save ext fields
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean update(Map<String, Object[]> requestParams, boolean saveExtFields)
      throws IOException {

    String format = CommonHelper.getValueFromMap(requestParams, "format", 0);
    int docId = Integer.parseInt(CommonHelper.getValueFromMap(requestParams, "doc_id", 0));
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);

    List errors = new ArrayList();
    List<Map<String, BasicDynaBean>> list = new ArrayList<>();
    copyHvfDocValues(requestParams, list, errors);

    List<Map<String, BasicDynaBean>> hvfImageList = new ArrayList<>();
    if (format != null && format.equals("doc_hvf_templates")) {
      copyHvfDocImageValues(requestParams, hvfImageList, errors);
    }

    List<Map<String, BasicDynaBean>> pdfImageList = new ArrayList<>();
    if (format != null && format.equals("doc_pdf_form_templates")) {
      copyPdfDocImageValues(requestParams, pdfImageList, errors, saveExtFields);
    }

    BasicDynaBean patientdocbean = patientdocrepo.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientdocbean, errors);
    /*
     * int center_id = getCenterId(requestParams); patientdocbean.set("center_id", center_id);
     */
    boolean commit = false;

    if (errors.isEmpty()) {
      boolean patientdocvaluesflag = true;
      boolean patientdocimagevaluesflag = true;
      boolean patientdocflag = false;
      boolean otherTx = false;
      boolean minioUploadStatus = false;

      try {
        String fileName = null;
        MultipartFile file = null;
        if (requestParams.get("fileName") != null) {
          file = ((MultipartFile) requestParams.get("fileName")[0]);
          fileName = file.getOriginalFilename();
        }

        if (fileName != null && fileName.toString().contains(".")) {
          String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
          patientdocbean.set("original_extension", extension);

          if (extension.equals("odt") || extension.equals("ods")) {
            patientdocbean.set("content_type", "application/vnd.oasis.opendocument.text");
          } else {
            String fileMime = file.getContentType();
            if (fileMime == null) {
              patientdocbean.set("content_type", mimeUtil.getMimeTypes(file.getBytes()).toString());
            }
            patientdocbean.set("content_type", fileMime);
          }
        }
        BasicDynaBean docBean = patientdocrepo.findByKey("doc_id", docId);
        minioUploadStatus = updateDocument(patientdocbean, file,
            docBean.get("is_migrated").equals("1"));
        // updates the patient documents.
        if (minioUploadStatus && (patientdocrepo.update(patientdocbean, keys) > 0)) {
          patientdocflag = true;
        }

        if (format != null && format.equals("doc_hvf_templates")) {

          if (!list.isEmpty()) {

            // updates the hvf document image field values.
            if (!hvfImageList.isEmpty()) {
              patientdocimagevaluesflag = hvfdocvaluesrepo.updateHVFDocImageValues(hvfImageList,
                  docId);
            }

            // updates the hvf document field values.
            patientdocvaluesflag = false;
            patientdocvaluesflag = hvfdocvaluesrepo.updateHVFDocValues(list, docId);

            patientdocvaluesflag = patientdocvaluesflag && patientdocimagevaluesflag;

          }
        } else if (format != null && format.equals("doc_pdf_form_templates")) {

          // updates the pdf form image field values.
          if (!pdfImageList.isEmpty()) {
            patientdocimagevaluesflag = pdfdocvaluesrepo.updatePDFDocImageValues(pdfImageList,
                docId);
          }

          if (!saveExtFields) {
            // updates the pdf form field values.
            patientdocvaluesflag = false;
            patientdocvaluesflag = patientdocrepo.updatePDFFormFieldValues(requestParams, docId);
          }

          patientdocvaluesflag = patientdocvaluesflag && patientdocimagevaluesflag;
        }
        otherTx = postUpdate(docId, requestParams, errors);

        if (otherTx && patientdocflag && patientdocvaluesflag) {
          commit = true;
        } else {
          commit = false;
        }

      } finally {
        if (commit) {
          requestParams.put("success", new String[] { "Document updated Successfully.." });
        } else {
          String error = null;
          if (!errors.isEmpty()) {
            error = (String) requestParams.get("error")[0];
          }
          error = (error == null ? "Failed to insert Document.." : error);
          requestParams.put("error", new String[] { error });
        }
      }
    } else {
      requestParams.put("error", new String[] { "Incorrectly formatted values supplied" });
    }
    return commit;
  }

  /**
   * Delete.
   *
   * @param requestParams the request params
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean delete(Map requestParams) throws IOException {
    String[] docIds = (String[]) requestParams.get("deleteDocument");
    String msg = null;
    String error = null;
    boolean success = false;

    try {
      if (docIds != null) {
        for (String docidFormat : docIds) {
          String format = docidFormat.split(",")[1];
          int docId = Integer.parseInt(docidFormat.split(",")[0]);

          boolean flag = patientdocrepo.delete("doc_id", docId) > 0;
          if (flag) {
            if (format.equals("doc_hvf_templates")) {
              if (hvfdocvaluesrepo.exist("doc_id", docId)) {
                flag = hvfdocvaluesrepo.delete("doc_id", docId) > 0;
              }
              hvfImageValuesrepo.delete("doc_id", docId);

            } else if (format.equals("doc_pdf_form_templates")) {
              flag = false;
              flag = pdfvaluesrepo.delete("doc_id", docId) > 0;
              pdfdocvaluesrepo.delete("doc_id", docId);
            }
          }
          boolean otherTxFlag = postDelete(docId, format);

          if (flag && otherTxFlag) {
            success = true;
          } else {
            success = false;
            break;
          }
        }
        if (success) {
          msg = ((docIds.length > 1) ? "Documents" : "Document") + " deleted successfully..";
          requestParams.put("success", msg);
        } else {
          error = "Failed to delete " + ((docIds.length > 1) ? "Documents" : "Document..");
          requestParams.put("error", error);
        }
      }
    } finally {
      // finally
    }
    return success;
  }

  /**
   * Finalize document.
   *
   * @param requestParams the request params
   * @return true, if successful
   */
  public boolean finalizeDocument(Map requestParams) {
    String[] docIds = (String[]) requestParams.get("deleteDocument");
    boolean success = false;
    String msg = null;
    String error = null;
    if (docIds != null) {
      for (String docidFormat : docIds) {
        int docId = Integer.parseInt(docidFormat.split(",")[0]);

        BasicDynaBean fields = patientdocrepo.getBean();
        fields.set("doc_id", docId);
        fields.set("doc_status", "F");
        Map<String, Object> map = new HashMap<>();
        map.put("doc_id", fields.get("doc_id"));
        int result = patientdocrepo.update(fields, map);
        success = (result > 0);
        if (!success) {
          break;
        }
      }
      if (success) {
        msg = ((docIds.length > 1) ? "Documents" : "Document") + " finalized successfully..";
        requestParams.put("success", msg);
      } else {
        error = "Failed to finalize " + ((docIds.length > 1) ? "Documents" : "Document..");
        requestParams.put("error", error);
      }
    }

    return success;
  }

  /**
   * Copy hvf doc values.
   *
   * @param requestParams the request params
   * @param addTo the add to
   * @param errors the errors
   */
  private void copyHvfDocValues(Map requestParams, List<Map<String, BasicDynaBean>> addTo,
      List errors) {

    Object[] object = (Object[]) requestParams.get("field_id");
    if (object != null && object[0] != null) {
      for (int i = 0; i < object.length; i++) {
        BasicDynaBean templatevalueBean = hvfFieldsrepo.findByKey("field_id",
            new Integer(object[i].toString()));

        if (templatevalueBean == null || templatevalueBean.get("field_status") == null) {
          continue;
        }

        String valueId = CommonHelper.getValueFromMap(requestParams, "value_id", i);
        String fieldValue = CommonHelper.getValueFromMap(requestParams, "field_value", i);

        if ((fieldValue != null && valueId != null)
            && !((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesrepo.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("update", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        } else if ((fieldValue != null && valueId == null)
            && !((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesrepo.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("insert", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        } else if ((valueId != null && fieldValue == null)
            && !((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesrepo.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("delete", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        }

        if (((String) templatevalueBean.get("field_status")).equals("I")) {
          BasicDynaBean hvfdocvaluesbean = hvfdocvaluesrepo.getBean();
          ConversionUtils.copyIndexToDynaBean(requestParams, i, hvfdocvaluesbean, errors);
          if (errors.isEmpty()) {
            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("deleteField", hvfdocvaluesbean);
            addTo.add(map);
          } else {
            break;
          }
        }
      }
    }
  }

  /**
   * Copy hvf doc image values.
   *
   * @param requestParams the request params
   * @param addToImageList the add to image list
   * @param errors the errors
   */
  private void copyHvfDocImageValues(Map requestParams,
      List<Map<String, BasicDynaBean>> addToImageList, List errors) {
    int docId = Integer.parseInt(CommonHelper.getValueFromMap(requestParams, "doc_id", 0));
    String username = RequestContext.getUserName();

    String[] fieldIdArr = (String[]) requestParams.get("field_id");
    String[] fldInputArr = (String[]) requestParams.get("field_input");
    String[] deviceIpArr = (String[]) requestParams.get("device_ip");
    String[] deviceInfoArr = (String[]) requestParams.get("device_info");
    String[] fldImgTextArr = (String[]) requestParams.get("fieldImgText");

    if (fieldIdArr != null && fieldIdArr.length > 0) {
      for (int i = 0; i < fieldIdArr.length; i++) {
        if (fldInputArr[i] != null && fldInputArr[i].equals("E")) {

          BasicDynaBean templatevalueBean = null;
          Integer fldId = null;

          if (fieldIdArr[i] != null && !fieldIdArr[i].equals("")) {
            fldId = Integer.parseInt(fieldIdArr[i]);
            templatevalueBean = hvfFieldsrepo.findByKey("field_id", fldId);
          }
          if (fldId != null) {

            LinkedHashMap hvfkeys = new LinkedHashMap();
            hvfkeys.put("doc_id", docId);
            hvfkeys.put("field_id", fldId);

            List cols = new ArrayList();
            cols.add("doc_image_id");
            List<BasicDynaBean> hvfimagelist = hvfimagevaluesrepo.listAll(cols, hvfkeys, null);
            BasicDynaBean hvfimagebean = hvfimagelist.get(0);
            Integer imgId = null;

            if (!((String) templatevalueBean.get("field_status")).equals("I")) {
              if (hvfimagebean != null && hvfimagebean.get("doc_image_id") != null) {
                imgId = (Integer) hvfimagebean.get("doc_image_id");

                if (fldImgTextArr[i] != null && !fldImgTextArr[i].trim().equals("")) {
                  BasicDynaBean imgValueBean = hvfimagevaluesrepo.getBean();
                  imgValueBean.set("doc_image_id", imgId);
                  imgValueBean.set("doc_id", docId);
                  imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

                  byte[] decodedBytes = Base64.decodeBase64(fldImgTextArr[i].getBytes());
                  InputStream is = new ByteArrayInputStream(decodedBytes);

                  imgValueBean.set("field_image", is);
                  imgValueBean.set("field_image_content_type", "image/png");
                  imgValueBean.set("device_ip", deviceIpArr[i]);
                  imgValueBean.set("device_info", deviceInfoArr[i]);
                  imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
                  imgValueBean.set("username", username);

                  Map<String, BasicDynaBean> map = new HashMap<>();
                  map.put("update", imgValueBean);
                  addToImageList.add(map);
                }
              } else {
                imgId = hvfimagevaluesrepo.getNextSequence();

                BasicDynaBean imgValueBean = hvfimagevaluesrepo.getBean();
                imgValueBean.set("doc_image_id", imgId);
                imgValueBean.set("doc_id", docId);
                imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

                byte[] decodedBytes = Base64.decodeBase64(fldImgTextArr[i].getBytes());
                InputStream is = new ByteArrayInputStream(decodedBytes);

                imgValueBean.set("field_image", is);
                imgValueBean.set("field_image_content_type", "image/png");
                imgValueBean.set("device_ip", deviceIpArr[i]);
                imgValueBean.set("device_info", deviceInfoArr[i]);
                imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
                imgValueBean.set("username", username);

                Map<String, BasicDynaBean> map = new HashMap<>();
                map.put("insert", imgValueBean);
                addToImageList.add(map);
              }
            } else if (hvfimagebean != null && hvfimagebean.get("doc_image_id") != null) {
              imgId = (Integer) hvfimagebean.get("doc_image_id");
              BasicDynaBean imgValueBean = hvfimagevaluesrepo.getBean();
              imgValueBean.set("doc_image_id", imgId);
              imgValueBean.set("doc_id", docId);
              imgValueBean.set("field_id", Integer.parseInt(fieldIdArr[i]));

              Map<String, BasicDynaBean> map = new HashMap<>();
              map.put("deleteField", imgValueBean);
              addToImageList.add(map);
            }
          }
        }
      }
    }
  }

  /**
   * Copy pdf doc image values.
   *
   * @param requestParams the request params
   * @param addToImageList the add to image list
   * @param errors the errors
   * @param saveExtFields the save ext fields
   */
  private void copyPdfDocImageValues(Map requestParams,
      List<Map<String, BasicDynaBean>> addToImageList, List errors, boolean saveExtFields) {
    int docId = Integer.parseInt(CommonHelper.getValueFromMap(requestParams, "doc_id", 0));
    String username = RequestContext.getUserName();

    Integer templateId = (requestParams.get("template_id") != null
        && !((String[]) requestParams.get("template_id"))[0].equals(""))
            ? Integer.parseInt(((String[]) requestParams.get("template_id"))[0]) : null;

    List<BasicDynaBean> imageTemplateFieldvalues = PatientPdfDocImagesRepository
        .getPDFTemplateImageValues(templateId);

    if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {

      for (int i = 0; i < imageTemplateFieldvalues.size(); i++) {
        String fieldId = null;
        String fldInput = null;
        String deviceIp = null;
        String deviceInfo = null;
        String fldImgText = null;

        if (!saveExtFields) {
          fieldId = (requestParams.get("field_id" + "_" + i) != null
              && !((String[]) requestParams.get("field_id" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("field_id" + "_" + i))[0] : null;
          fldInput = (requestParams.get("field_input" + "_" + i) != null
              && !((String[]) requestParams.get("field_input" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("field_input" + "_" + i))[0] : null;
          deviceIp = (requestParams.get("device_ip" + "_" + i) != null
              && !((String[]) requestParams.get("device_ip" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("device_ip" + "_" + i))[0] : null;
          deviceInfo = (requestParams.get("device_info" + "_" + i) != null
              && !((String[]) requestParams.get("device_info" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("device_info" + "_" + i))[0] : null;
          fldImgText = (requestParams.get("fieldImgText" + "_" + i) != null
              && !((String[]) requestParams.get("fieldImgText" + "_" + i))[0].equals(""))
                  ? ((String[]) requestParams.get("fieldImgText" + "_" + i))[0] : null;
        } else {
          fieldId = (requestParams.get("field_id") != null
              && !((String[]) requestParams.get("field_id"))[i].equals(""))
                  ? ((String[]) requestParams.get("field_id"))[i] : null;
          fldInput = (requestParams.get("field_input") != null
              && !((String[]) requestParams.get("field_input"))[i].equals(""))
                  ? ((String[]) requestParams.get("field_input"))[i] : null;
          deviceIp = (requestParams.get("device_ip") != null
              && !((String[]) requestParams.get("device_ip"))[i].equals(""))
                  ? ((String[]) requestParams.get("device_ip"))[i] : null;
          deviceInfo = (requestParams.get("device_info") != null
              && !((String[]) requestParams.get("device_info"))[i].equals(""))
                  ? ((String[]) requestParams.get("device_info"))[i] : null;
          fldImgText = (requestParams.get("fieldImgText") != null
              && !((String[]) requestParams.get("fieldImgText"))[i].equals(""))
                  ? ((String[]) requestParams.get("fieldImgText"))[i] : null;
        }

        deviceIp = (deviceIp == null) ? "" : deviceIp;
        deviceInfo = (deviceInfo == null) ? "" : deviceInfo;
        fldImgText = (fldImgText == null) ? "" : fldImgText;

        if (fieldId != null && fldInput != null && fldInput.equals("E")) {

          LinkedHashMap pdfkeys = new LinkedHashMap();
          pdfkeys.put("doc_id", docId);
          pdfkeys.put("field_id", Integer.parseInt(fieldId));

          List cols = new ArrayList();
          cols.add("doc_image_id");
          List<BasicDynaBean> pdfimagelist = pdfdocvaluesrepo.listAll(cols, pdfkeys, null);
          BasicDynaBean pdfimagebean = pdfimagelist.get(0);
          Integer imgId = null;

          if (pdfimagebean != null && pdfimagebean.get("doc_image_id") != null) {
            imgId = (Integer) pdfimagebean.get("doc_image_id");

            if (fldImgText != null && !fldImgText.trim().equals("")) {
              BasicDynaBean imgValueBean = pdfdocvaluesrepo.getBean();
              imgValueBean.set("doc_image_id", imgId);
              imgValueBean.set("doc_id", docId);
              imgValueBean.set("field_id", Integer.parseInt(fieldId));

              byte[] decodedBytes = Base64.decodeBase64(fldImgText.getBytes());
              InputStream is = new ByteArrayInputStream(decodedBytes);

              imgValueBean.set("field_image", is);
              imgValueBean.set("field_image_content_type", "image/png");
              imgValueBean.set("device_ip", deviceIp);
              imgValueBean.set("device_info", deviceInfo);
              imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
              imgValueBean.set("username", username);

              Map<String, BasicDynaBean> map = new HashMap<>();
              map.put("update", imgValueBean);
              addToImageList.add(map);
            }
          } else {
            imgId = pdfdocvaluesrepo.getNextSequence();

            BasicDynaBean imgValueBean = pdfdocvaluesrepo.getBean();
            imgValueBean.set("doc_image_id", imgId);
            imgValueBean.set("doc_id", docId);
            imgValueBean.set("field_id", Integer.parseInt(fieldId));

            byte[] decodedBytes = Base64.decodeBase64(fldImgText.getBytes());
            InputStream is = new ByteArrayInputStream(decodedBytes);

            imgValueBean.set("field_image", is);
            imgValueBean.set("field_image_content_type", "image/png");
            imgValueBean.set("device_ip", deviceIp);
            imgValueBean.set("device_info", deviceInfo);
            imgValueBean.set("capture_time", DateUtil.getCurrentTimestamp());
            imgValueBean.set("username", username);

            Map<String, BasicDynaBean> map = new HashMap<>();
            map.put("insert", imgValueBean);
            addToImageList.add(map);
          }
        }
      }
    }
  }
}