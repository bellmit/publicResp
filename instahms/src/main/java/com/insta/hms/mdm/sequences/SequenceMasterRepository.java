package com.insta.hms.mdm.sequences;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.StringUtil;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class SequenceMasterRepository.
 *
 * @param <E> the generic type
 */
public abstract class SequenceMasterRepository<E> extends MasterRepository<E> {

  /** The transaction type. */
  private String transactionType;
  
  /** The field array. */
  private String[] fieldArray = null;

  /**
   * Gets the transaction type.
   *
   * @return the transaction type
   */
  public String getTransactionType() {
    return transactionType;
  }

  /**
   * Instantiates a new sequence master repository.
   *
   * @param table the table
   * @param keyColumn the key column
   * @param transactionType the transaction type
   */
  public SequenceMasterRepository(String table, String keyColumn, String transactionType) {
    super(table, keyColumn);
    this.transactionType = transactionType;
  }

  /**
   * Instantiates a new sequence master repository.
   *
   * @param table the table
   * @param keyColumn the key column
   * @param transactionType the transaction type
   * @param fieldArray the field array
   */
  public SequenceMasterRepository(
      String table, String keyColumn, String transactionType, String[] fieldArray) {
    super(table, keyColumn);
    this.transactionType = transactionType;
    this.fieldArray = fieldArray;
  }

  /**
   * Gets the confict rules.
   *
   * @param bean the bean
   * @return the confict rules
   */
  public List<BasicDynaBean> getConfictRules(BasicDynaBean bean) {

    ArrayList<Object> filterValues = new ArrayList<Object>();
    List<String> fields = new ArrayList<String>();
    List<String> filters = new ArrayList<String>();

    for (String fieldName : fieldArray) {
      filterValues.add(bean.get(fieldName));
    }

    fields.add(StringUtil.join(fieldArray, ","));

    for (int i = 0; i < fieldArray.length; i++) {
      if (i != 0) {
        filters.add(" AND " + fieldArray[i] + "=?");
      } else {
        filters.add(" " + fieldArray[i] + "=?");
      }
    }

    Object keyColumnValue = bean.get(getKeyColumn());
    if (keyColumnValue != null) {
      filters.add(" AND " + getKeyColumn() + "!= ?");
      filterValues.add(keyColumnValue);
    }

    return DatabaseHelper.queryToDynaList(
        getQueryFromList(getTable(), fields, filters), filterValues.toArray());
  }
}
