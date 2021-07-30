package com.insta.hms.core.patient.communication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

@Repository
public class ContactPreferencesRepository extends GenericRepository {

	public ContactPreferencesRepository() {
			super("contact_preferences");
		}
	
	private static final String GET_CONTACT_PREFERENCE = "select receive_communication "
			+ "from contact_preferences where mr_no=? ";
	
	private static final String UPDATE_CONTACT_PREFERENCE = "update contact_preferences set "
			+ " receive_communication =? , lang_code=? where mr_no=? ";
	
	private static final String INSERT_CONTACT_PREFERENCE = "insert into contact_preferences "
			+ "(mr_no,receive_communication,lang_code)  values (?,?,?) ";
	
	public String getContactPreference(String mrNo) {
		return DatabaseHelper.getString(GET_CONTACT_PREFERENCE, mrNo);
	}

	 private static final String GET_EXISTING_COMM_PREFERENCE = "SELECT receive_communication FROM "
		  		+ "contact_preferences WHERE mr_no = ?";
	 
	public Integer updateContactPreference(String mrNo, String preference, String preferredlang) {
		String oldPref = DatabaseHelper.getString(GET_CONTACT_PREFERENCE, mrNo);
		if(oldPref == null || oldPref.equals("")) {
			return DatabaseHelper.insert(INSERT_CONTACT_PREFERENCE,  mrNo, preference,preferredlang);
		}
		String existingCommPref = DatabaseHelper.getString(GET_EXISTING_COMM_PREFERENCE,
    			mrNo);
		if(!preference.equals("B") && !preference.equals(existingCommPref)) {
    		updateExistingMessagePrefs(existingCommPref,preference,mrNo );
    	}
		return DatabaseHelper.update(UPDATE_CONTACT_PREFERENCE, preference,preferredlang, mrNo);
		
	}
	
	private static final String UPDATE_MESSAGE_PREFS = "update patient_communication_preferences "
	  		+ " set communication_type = ? where mr_no = ? ";
	  
	private static final String UPDATE_MESSAGE_PREFS_WHERE = " AND communication_type = ? ";
	  
	private static void updateExistingMessagePrefs(String existingCommPref, String communication, String mrNo) {
      if(communication.equals("N")) {
    	  DatabaseHelper.update(UPDATE_MESSAGE_PREFS, communication,mrNo);
      } else {
    	  String query = UPDATE_MESSAGE_PREFS + UPDATE_MESSAGE_PREFS_WHERE;
    	  String oppositeMode = communication.equals("S") ? "E" : "S";
    	  DatabaseHelper.update(query, communication, mrNo,"B");
	      DatabaseHelper.update(query,"N", mrNo, oppositeMode);
    	}
	}
	
  private static final String UPDATE_LANG_AND_CONSENT = "update "
      + "contact_preferences  set promotional_consent = ? , lang_code= ? "
      + "where mr_no = ? ";

  private static final String INSERT_LANG_AND_CONSENT = "insert into contact_preferences "
      + "(mr_no,promotional_consent,lang_code)  values (?,?,?) ";

  public void updateLangAndConsent(String mrNo, String promotionalConsent, String preferredlang) {
    String oldPref = DatabaseHelper.getString(GET_CONTACT_PREFERENCE, mrNo);
    if (oldPref == null || oldPref.equals("")) {
      DatabaseHelper.insert(INSERT_LANG_AND_CONSENT, mrNo, promotionalConsent, preferredlang);
    } else {
      DatabaseHelper.update(UPDATE_LANG_AND_CONSENT, promotionalConsent, preferredlang, mrNo);
    }
  }
	
}
