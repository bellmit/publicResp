package com.insta.hms.master.MessageType;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MessageConfigAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(MessageConfigAction.class);
    private static final GenericDAO messageConfigDAO = new GenericDAO("message_config");
	public ActionForward show(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String messageTypeId = request.getParameter("message_type_id");
		logger.debug("Entering show....." + messageTypeId);
		if (null != messageTypeId) {
			// Get the bean
      if (messageTypeId.equals("email_dynamic_appointment_reminder")
          || messageTypeId.equals("sms_dynamic_appointment_reminder")) {
        Map<String, Object> identifiers = new HashMap<>();
        identifiers.put("message_type_id", "sms_dynamic_appointment_reminder");
        identifiers.put("param_name", "buffer_hours");
        BasicDynaBean configList = messageConfigDAO.findByKey(identifiers);
        Map configMap = new HashMap();
        configMap.put(configList.get("param_name"), configList);

        Map<String, Object> identifiersStatus = new HashMap<>();
        identifiersStatus.put("message_type_id", messageTypeId);
        identifiersStatus.put("param_name", "status");
        BasicDynaBean configListStatus = messageConfigDAO.findByKey(identifiersStatus);
        configMap.put(configListStatus.get("param_name"), configListStatus);

        request.setAttribute("configMap", configMap);
      }
      else{
			List<BasicDynaBean> configList = messageConfigDAO.findAllByKey("message_type_id", messageTypeId);
			Map configMap = new HashMap();
			for (BasicDynaBean configItem : configList) {
				String paramName = (String)configItem.get("param_name");
				configMap.put(paramName, configItem);
			}
			request.setAttribute("configMap", configMap);
      }
			GenericDAO typeDao = new GenericDAO("message_types");
			BasicDynaBean messageType = typeDao.findByKey("message_type_id", messageTypeId);
			request.setAttribute("messageType", messageType);
		}
		
		return mapping.findForward("show");
	}

	public ActionForward update(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String messageTypeId = request.getParameter("message_type_id");
		String[] messageConfigIds = request.getParameterValues("message_configuration_id");
		logger.debug("Entering update....." + messageTypeId);

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("message_type_id", request.getParameter("message_type_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (null == messageTypeId || null == messageConfigIds) {
			flash.error("Invalid key value passed");
			return redirect;
		}


		Connection con = null;
		Integer configId = null;
		String configValue = null;
		int updateCount = 0;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for (String messageConfigId : messageConfigIds) {
				if (null != messageConfigId) {
					configId = Integer.parseInt(messageConfigId);
					configValue = request.getParameter("param_value_"+configId);
					Map keys = new HashMap();
					Map columnData = new HashMap();
					keys.put("message_configuration_id", configId);
					columnData.put("param_value", configValue);
					int updated = messageConfigDAO.update(con, columnData, keys);
					if (updated > 0) updateCount++;
				}
			}
			if (updateCount == messageConfigIds.length) {
				success = true;
			}

		} finally {
			if (success) {
					con.commit();
					flash.success("Message Configuration updated successfully");
			} else {
				if (null != con)
					con.rollback();
				flash.error("Message Configuration update failed");
			}
			if (null != con)
				con.close();
		}
		return redirect;
	}
}
