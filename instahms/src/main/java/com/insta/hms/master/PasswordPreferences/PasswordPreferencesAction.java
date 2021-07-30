package com.insta.hms.master.PasswordPreferences;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PasswordPreferencesAction extends BaseAction{
	static Logger log = LoggerFactory.getLogger(PasswordPreferencesAction.class);
	
	private static final GenericDAO passwordRuleDAO = new GenericDAO("password_rule");

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, Exception, IOException {

		BasicDynaBean bean = passwordRuleDAO.getRecord();
		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, Exception, IOException, SQLException {
		Connection con = DataBaseUtil.getConnection();
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		BasicDynaBean bean = passwordRuleDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		FlashScope flash = FlashScope.getScope(request);
		try {
			if(errors.isEmpty()){
				String specialCharsList = ((String) bean.get("specail_char_list")).replaceAll(
								"\\s", "");
				if(Pattern.matches("[^A-Za-z0-9]*",specialCharsList) && hasAllUniqueChars(specialCharsList)) {
					bean.set("specail_char_list",specialCharsList);
					int success = passwordRuleDAO.update(con, bean.getMap(), null);
					if(success>0)
						flash.success("Password preferences are updated successfully.");
					else
						flash.error("Failed to update password preferences.");
				} else {
					flash.error("Enter valid Unique Special Character List");
				}
			} else {
				flash.error("Incorrectly formatted values supplied ");
			}

		}finally {
			DataBaseUtil.closeConnections(con, null);
		}
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public static boolean hasAllUniqueChars (String word) {
		HashSet<Character> charsSet = new HashSet<>();
		for(int index=0; index < word.length(); index ++) {
			char c = word.charAt(index);
			if(!charsSet.add(c))
				return false;
		}
		return true;
	}

}