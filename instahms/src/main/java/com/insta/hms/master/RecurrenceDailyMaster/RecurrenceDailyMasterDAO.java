package com.insta.hms.master.RecurrenceDailyMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.MigratedTo;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author:mithun saha
 */
@Deprecated
@MigratedTo(value=com.insta.hms.mdm.dailyrecurrences.RecurrenceDailyRepository.class)
public class RecurrenceDailyMasterDAO extends GenericDAO{
	public RecurrenceDailyMasterDAO() {
		super("recurrence_daily_master");
	}

	public static final String GET_AVL_DISPLAY_NAMES = "Select display_name,recurrence_daily_id From recurrence_daily_master";

	public List getAvlDisplayNames() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_AVL_DISPLAY_NAMES));
	}

	private static final String RECURRENCE_DAILY_ID = "Select recurrence_daily_id FROM recurrence_daily_master" +
			" where display_name=?";

	public String getRecurrenceDailyIdByDisplayName(String displayName) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String success = null;
		try{
			ps = con.prepareStatement(RECURRENCE_DAILY_ID);
			ps.setString(1, displayName);
			rs = ps.executeQuery();
			if(rs.next()) {
				success = rs.getString(1);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		  }
		return success;
	}

	/*
	 * checkDate is the date from which next due date is calculated.
	 */
	public Timestamp getNextDueDateTime(String freqType, Integer recurrenceDailyId, Integer interval,
			String intervalUnits, java.sql.Timestamp checkDate) throws SQLException, ParseException {
		if (freqType.equals("F")) {
			BasicDynaBean recurrenceBean =
				new RecurrenceDailyMasterDAO().findByKey("recurrence_daily_id", recurrenceDailyId);
			String timingStr = ((String) recurrenceBean.get("timings"));
			if (timingStr == null || timingStr.equals(""))
				return checkDate;

			List<String> timings = Arrays.asList(((String) recurrenceBean.get("timings")).split(","));
			for (int i = 0; i < timings.size(); i++ ) {
			  timings.set(i, timings.get(i).trim());
			}
			Collections.sort(timings);
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			String checkDateTime = timeFormat.format(checkDate).toString();
            Integer index = -1;
            for (int i = 0; i < timings.size(); i++ ) {
                if (checkDateTime.compareTo(timings.get(i)) < 0) {
                  index = i;
                  break;
                }
            }
            Date nextDueDate = null;
            SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            if (index == - 1) {
              Calendar cal = Calendar.getInstance();
              cal.setTime(checkDate);
              cal.add(Calendar.DATE, 1);
              cal.set(Calendar.HOUR_OF_DAY, 0);
              cal.set(Calendar.MINUTE, 0);
              cal.set(Calendar.SECOND, 0);
              cal.set(Calendar.MILLISECOND, 0);
              nextDueDate = sdf.parse(sd.format(cal.getTime()) + " " + timings.get(0));
            } else {
              nextDueDate = sdf.parse(sd.format(checkDate) + " " + timings.get(index));
            }
            return new java.sql.Timestamp(nextDueDate.getTime());
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(checkDate);
			if (interval != null && interval != 0) {
				if (intervalUnits.equals("M"))
					calendar.add(Calendar.MINUTE, interval);
				if (intervalUnits.equals("H"))
					calendar.add(Calendar.HOUR, interval);
				if (intervalUnits.equals("D"))
					calendar.add(Calendar.DATE, interval);
			}

			return new java.sql.Timestamp(calendar.getTimeInMillis());
		}
	}

	private static final String RECURRENCE_DETAILS_FOR_ACTIVITY =
		" SELECT pa.due_date, pp.recurrence_daily_id, coalesce(pp.repeat_interval, 0) AS repeat_interval, " +
		"	pp.freq_type, coalesce(pp.repeat_interval_units, 'M') AS repeat_interval_units " +
		" FROM patient_activities pa " +
		" 	LEFT JOIN patient_prescription pp ON (pa.prescription_id=pp.patient_presc_id) " +
		"	LEFT JOIN recurrence_daily_master rdm ON (rdm.recurrence_daily_id=pp.recurrence_daily_id) " +
		" WHERE activity_id=?";
	public Timestamp getNextDueDateForActivity(int activityId) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(RECURRENCE_DETAILS_FOR_ACTIVITY);
			ps.setInt(1, activityId);
			BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
			String freqType = (String) bean.get("freq_type");
			Integer repeatInterval = (Integer) bean.get("repeat_interval");
			Date dueDate = (Date) bean.get("due_date");
			Integer recurrence_daily_id = (Integer) bean.get("recurrence_daily_id");
			String repeat_interval_units = (String) bean.get("repeat_interval_units");
			return getNextDueDateTime(freqType, recurrence_daily_id, repeatInterval, repeat_interval_units,
					new Timestamp(dueDate.getTime()));
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
