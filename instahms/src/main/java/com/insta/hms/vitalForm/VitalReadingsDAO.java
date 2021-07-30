/**
 *
 */
package com.insta.hms.vitalForm;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.laboratory.ResultExpressionProcessor;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class VitalReadingsDAO extends GenericDAO {
  
  private static final GenericDAO vitalMasterDAO = new GenericDAO("vital_parameter_master");
  private static final GenericDAO vitalReadingDAO = new GenericDAO("vital_reading");

	public VitalReadingsDAO() {
		super("vital_reading");
	}

	public boolean insert(Connection con, int vitalReadingId, String[] paramIds, String[] paramValues, 
			String[] paramLabels, String[] paramRemarks, String userName)
		throws SQLException, IOException, Exception {
		boolean flag = true;
		PreparedStatement ps = null;

		try {
			if (flag) {
				if (paramIds != null) {
					//List list = new ArrayList();
					for (int i=0; i<paramIds.length; i++) {
						BasicDynaBean vitalBean = vitalMasterDAO.findByKey(con, "param_id", Integer.parseInt(paramIds[i]));
						if (vitalBean != null ) {

							BasicDynaBean bean = getBean();
							if (null != vitalBean.get("expr_for_calc_result") && !vitalBean.get("expr_for_calc_result").equals("")) {
								String result = ResultExpressionProcessor.processResultExpression(Arrays.asList(paramLabels), Arrays.asList(paramValues), (String)vitalBean.get("expr_for_calc_result"));
								bean.set("vital_reading_id", vitalReadingId);
								bean.set("param_id", Integer.parseInt(paramIds[i]));
								bean.set("param_value", result);
								bean.set("username",userName);
								bean.set("mod_time",DateUtil.getCurrentTimestamp());
								bean.set("param_remarks", paramRemarks[i]);
							} else {
								bean.set("vital_reading_id", vitalReadingId);
								bean.set("param_id", Integer.parseInt(paramIds[i]));
								bean.set("param_value", paramValues[i]);
								bean.set("username",userName);
								bean.set("mod_time",DateUtil.getCurrentTimestamp());
								bean.set("param_remarks", paramRemarks[i]);
							}
							if (genericVitalFormDAO.readingExists(con, vitalReadingId, Integer.parseInt(paramIds[i]))) {
								Map vitalReadingKeys = new HashMap();
								vitalReadingKeys.put("vital_reading_id", vitalReadingId);
								vitalReadingKeys.put("param_id", Integer.parseInt(paramIds[i]));
								if (vitalReadingDAO.update(con, bean.getMap(), vitalReadingKeys) == 0) {
									flag = false;
									return flag;
								}
							} else {
								if (!vitalReadingDAO.insert(con, bean)) {
									flag = false;
									return flag;
								}
							}
						}
					}
				}
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return flag;
	}


	public boolean inactiveVitalsFromVitalReading(Connection con, int vitalReadingId) throws SQLException, Exception {
		BasicDynaBean vitalBean = getBean();
		vitalBean.set("status","I");
		return update(con, vitalBean.getMap(), "vital_reading_id", vitalReadingId) > 0;
	}

}
