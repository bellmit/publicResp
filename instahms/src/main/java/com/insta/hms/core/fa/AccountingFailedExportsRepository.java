package com.insta.hms.core.fa;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericHibernateRepository;
import com.insta.hms.model.AccountingFailedExportsModel;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public class AccountingFailedExportsRepository extends GenericHibernateRepository {

  /**
   * Make an entry or update exisitng entry for accounting failed export. 
   * @param billNo  bill number
   * @param visitId visit id
   */
  @Transactional
  public void logFailedExport(String billNo, String visitId) {    
    AccountingFailedExportsModel model = get(billNo, visitId);
    if (model == null) {
      model = new AccountingFailedExportsModel(billNo, visitId);
    }
    model.setLastRun(DateUtil.getCurrentTimestamp());
    Session session = getSession();
    session.persist(model);
    session.flush();
  }
  
  /**
   * Make an entry or update exisitng entry for accounting failed export. 
   * @param receiptId  receipt number
   */
  @Transactional
  public void logFailedExport(String receiptId) {    
    AccountingFailedExportsModel model = get(receiptId);
    if (model == null) {
      model = new AccountingFailedExportsModel(receiptId);
    }
    model.setLastRun(DateUtil.getCurrentTimestamp());
    Session session = getSession();
    session.persist(model);
    session.flush();
  }
  
  /**
   * remove an entry for accounting failed export. 
   * @param billNo  bill number
   * @param visitId visit id
   */
  @Transactional
  public void remove(String billNo, String visitId) {
    AccountingFailedExportsModel model = get(billNo, visitId);
    if (model == null) {
      return;
    }
    Session session = getSession();
    session.delete(model);
    session.flush();
  }

  /**
   * remove an entry for accounting failed export. 
   * @param receiptId  receipt number
   */
  @Transactional
  public void remove(String receiptId) {
    AccountingFailedExportsModel model = get(receiptId);
    if (model == null) {
      return;
    }
    Session session = getSession();
    session.delete(model);
    session.flush();
  }

  /**
   * Get Accounting Failed Export by primary key.
   * @param id primary key
   * @return model for failed export or null if not found
   */
  public AccountingFailedExportsModel get(Integer id) {
    return (AccountingFailedExportsModel) getSession()
        .createCriteria(AccountingFailedExportsModel.class)
        .add(Restrictions.eq("id", id)).uniqueResult();    
  }
  
  /**
   * Get Accounting Failed Export by bill number and visit id.
   * @param receiptId  receipt number
   * @return model for failed export or null if not found
   */
  public AccountingFailedExportsModel get(String receiptId) {
    return (AccountingFailedExportsModel) getSession()
        .createCriteria(AccountingFailedExportsModel.class)
        .add(Restrictions.eq("receiptId", receiptId))
        .add(Restrictions.isNull("billNo"))
        .add(Restrictions.isNull("visitId")).uniqueResult();    
  }
  
  /**
   * Get Accounting Failed Export by bill number and visit id.
   * @param billNo  bill number
   * @param visitId visit id
   * @return model for failed export or null if not found
   */
  public AccountingFailedExportsModel get(String billNo, String visitId) {
    return (AccountingFailedExportsModel) getSession()
        .createCriteria(AccountingFailedExportsModel.class)
        .add(Restrictions.eq("billNo", billNo))
        .add(Restrictions.eq("visitId", visitId)).uniqueResult();    
  }
  
  @Transactional
  public List<AccountingFailedExportsModel> getAll() {
    return (List<AccountingFailedExportsModel>) getSession()
        .createCriteria(AccountingFailedExportsModel.class).list();
  }
}
