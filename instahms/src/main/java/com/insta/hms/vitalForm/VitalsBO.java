/**
 *
 */
package com.insta.hms.vitalForm;

import com.insta.hms.common.ConversionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class VitalsBO {

	VisitVitalsDAO vvDAO = new VisitVitalsDAO();
	public boolean updateVitals(Connection con, Map params, String userName) throws SQLException, IOException, ParseException,
		Exception {
		String patientId = ConversionUtils.getParamValue(params, "patient_id", null);
		String[] vitalReadingIds = (String[]) params.get("h_vital_reading_id");
		String[] delItem = (String[]) params.get("delVitalItem");
		boolean flag = true;

		if (vitalReadingIds != null) {
			if (vitalReadingIds != null) {
				for (int i=0; i<vitalReadingIds.length-1; i++) {
					String[] paramIds = (String[]) params.get("h_param_id"+i);
					String[] paramValues = (String[]) params.get("h_param_value"+i);
					String[] paramLabels = (String[]) params.get("h_param_label"+i);
					String[] paramRemarks = (String[]) params.get("h_param_remarks"+i);
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
					String dateTimeStr = ((String[]) params.get("h_reading_date"))[i] + " " +
						((String[]) params.get("h_reading_time"))[i];
					java.util.Date parsedDate = (java.util.Date) dateFormat.parse(dateTimeStr);
					java.sql.Timestamp dateTime = new java.sql.Timestamp(parsedDate.getTime());
					String[] vitals_edited = (String[]) params.get("vital_edited");
					int vitalReadingId = 0;
					if (!vitalReadingIds[i].equals("_")) {
						vitalReadingId = Integer.parseInt(vitalReadingIds[i]);
					}
					
					if (new Boolean(delItem[i])) {
						if (!vvDAO.insertVitals(con, patientId, userName, dateTime, vitalReadingId,
								paramIds, paramValues, paramLabels, new Boolean(vitals_edited[i]), paramRemarks,true)) {
							flag = false;
							break;
						}
					} else {
						if (!vvDAO.insertVitals(con, patientId, userName, dateTime, vitalReadingId, 
								paramIds, paramValues, paramLabels, new Boolean(vitals_edited[i]), paramRemarks,false)) {
							flag = false;
							break;
						}
					}
				}
			}
		}
		return flag;
	}

}
