package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.section.fields.FieldsService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InstaSectionImageService.
 *
 * @author krishnat
 */
@Service
public class InstaSectionImageService extends MasterService {

  /** The field service. */
  @LazyAutowired
  FieldsService fieldService;

  /** The v. */
  InstaSectionImageValidator validator = null;

  /** The sd service. */
  @LazyAutowired
  SectionDetailsService sdService = null;

  /**
   * Instantiates a new insta section image service.
   *
   * @param repo the repo
   * @param val the val
   */
  public InstaSectionImageService(InstaSectionImageRepository repo,
      InstaSectionImageValidator val) {
    super(repo, val);
    this.validator = val;
  }

  /**
   * Insert image.
   *
   * @param requestParams the request params
   * @param fileMap the file map
   * @return the int
   */
  public int insertImage(Map<String, String[]> requestParams, Map<String, MultipartFile> fileMap) {
    InstaSectionImageRepository repo = (InstaSectionImageRepository) getRepository();
    ValidationErrorMap errMap = new ValidationErrorMap();
    BasicDynaBean bean = toBean(requestParams, fileMap);
    validator.validateInsert(bean, errMap);

    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }
    int imageId = repo.getNextSequence();
    bean.set("image_id", imageId);
    return repo.insert(bean) == 1 ? imageId : 0;
  }

  /**
   * Update image.
   *
   * @param requestParams the request params
   * @param fileMap the file map
   * @return the int
   */
  public int updateImage(Map<String, String[]> requestParams, Map<String, MultipartFile> fileMap) {
    InstaSectionImageRepository repo = (InstaSectionImageRepository) getRepository();
    ValidationErrorMap errMap = new ValidationErrorMap();
    BasicDynaBean bean = toBean(requestParams, fileMap);
    validator.validateUpdate(bean, errMap);

    if (!errMap.getErrorMap().isEmpty()) {
      throw new ValidationException(errMap);
    }

    String[] fieldDetailIds = (String[]) requestParams.get("field_detail_id");
    int fieldDetailId = fieldDetailIds != null && fieldDetailIds[0] != null
        && !fieldDetailIds[0].equals("") ? Integer
            .parseInt(fieldDetailIds[0]) : 0;
    int imageId = (Integer) bean.get("image_id");
    if (fieldDetailId != 0) {
      if (!sdService.isImageUsed(fieldDetailId, (Integer) bean.get("image_id"))) {
        repo.delete("image_id", imageId);
      }
      bean.set("image_id", repo.getNextSequence());
      return repo.insert(bean) == 1 ? (Integer) bean.get("image_id") : 0;
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("image_id", bean.get("image_id"));
    return repo.update(bean, keys) == 1 ? (Integer) bean.get("image_id") : 0;
  }

  /**
   * Gets the image.
   *
   * @param imageId the image id
   * @param fieldId the field id
   * @return the image
   */
  public BasicDynaBean getImage(int imageId, int fieldId) {
    BasicDynaBean bean = null;
    ValidationErrorMap errMap = new ValidationErrorMap();
    String field = null;
    if (imageId != 0) {
      InstaSectionImageRepository repo = (InstaSectionImageRepository) getRepository();
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("image_id", imageId);
      bean = repo.findByPk(params);
      field = "image_id";
    }
    if (imageId == 0 && fieldId != 0) {
      bean = fieldService.getField(fieldId);
      field = "field_id";
    }

    if (bean == null) {
      errMap.addError(field, "exception.instasection.image.notvalid." + field);
      throw new ValidationException(errMap);
    }
    return bean;
  }

  /**
   * Gets the images.
   *
   * @param imageIds the image ids
   * @return the images
   */
  public List getImages(List<Integer> imageIds) {
    InstaSectionImageRepository repo = (InstaSectionImageRepository) getRepository();
    return repo.getImages(imageIds);
  }
}
