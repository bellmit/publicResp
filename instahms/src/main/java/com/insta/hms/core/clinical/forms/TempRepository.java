package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class TempRepository.
 *
 * @author krishnat
 */
@Repository
public class TempRepository extends GenericRepository {

  /**
   * Instantiates a new temp repository.
   */
  public TempRepository() {
    super("auto_save_json_data");
  }

  /**
   * Gets the sections.
   *
   * @param parameters the parameters
   * @param username the username
   * @return the sections
   */
  public List<BasicDynaBean> getSections(FormParameter parameters, String username) {
    List<Object> list = new ArrayList<>();
    list.add(parameters.getMrNo());
    list.add(parameters.getPatientId());
    list.add(parameters.getFormType());
    list.add(parameters.getItemType());
    list.add(username);
    list.add(parameters.getId());
    StringBuilder query =
        new StringBuilder("SELECT * FROM auto_save_json_data WHERE mr_no=? "
            + " and patient_id=? and form_type=? and item_type=? and user_name=? AND "
            + parameters.getFormFieldName() + "=? ");
    return DatabaseHelper.queryToDynaList(query.toString(), list.toArray());
  }

  /**
   * Gets the sections.
   *
   * @param bean the bean
   * @param username the username
   * @return the sections
   */
  public List<BasicDynaBean> getSections(BasicDynaBean bean, String username) {
    String formFieldName = (String) bean.get("form_field_name");
    StringBuilder query = new StringBuilder("SELECT * FROM auto_save_json_data WHERE ");
    query.append(formFieldName + "=?");
    query.append(" AND form_type = ? and user_name=? ");

    return DatabaseHelper.queryToDynaList(query.toString(), bean.get(formFieldName),
        bean.get("form_type"), username);
  }

}
