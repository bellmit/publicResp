package com.insta.hms.mdm.servicedepartments;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class ServiceDepartmentsRepository for database operations on services_departments table.
 *
 * @author tanmay.k
 */
@Repository("serviceDepartmentsRepository")
public class ServiceDepartmentsRepository extends MasterRepository<Integer> {

  /** Instantiates a new service departments repository. */
  public ServiceDepartmentsRepository() {
    super("services_departments", "serv_dept_id", "department");
  }

  /**
   * Gets the active service depts.
   *
   * @return the active service depts
   */
  public List<BasicDynaBean> getActiveServiceDepts() {
    return DatabaseHelper.queryToDynaList(
        "SELECT serv_dept_id, serv_dept_id||'' as dept_id, department as dept_name FROM "
            + " services_departments where status='A' order by dept_name");
  }
}
