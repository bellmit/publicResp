package com.insta.hms.master.MessageType;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.messaging.MessageForm;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.modules.ModulesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MessageTypeAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(MessageTypeAction.class);

	private static final String DEFAULT_EVENT_ID = "ui_trigger";

	private static final String messageTemplateFields = "SELECT * ";
	private static final String messageTemplateCount = "SELECT count(*) ";
	private static final String messageTemplateTables = " from (SELECT mt.message_type_id, mt.message_type_name, " +
			"mt.message_mode, mt.status, mt.category_id, me.event_name, mc.message_category_name " +
			"from message_types mt LEFT OUTER JOIN " +
			"message_events me ON (mt.event_id = me.event_id) " +
			"LEFT OUTER JOIN message_category mc on mt.category_id = mc.message_category_id) as foo";
	
    private static final GenericDAO messageActionDAO = new GenericDAO("message_actions");
    private static final GenericDAO messageCategoryDAO = new GenericDAO("message_category");
    private static final GenericDAO messageTypesDAO = new GenericDAO("message_types");
    private static final GenericDAO messageAttachmentsDAO = new GenericDAO("message_attachments");
    
	public ActionForward list(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		request.setAttribute("practoSmsModule", new ModulesDAO().findByKey("module_id", "mod_practo_sms"));

		PagedList messageTypeList = getMessageTypeList(mapping, form, request, response);
		request.setAttribute("pagedList", messageTypeList);

		List<BasicDynaBean> dispatcherList = getDispatcherList(null);
		Map dispatcherMap = ConversionUtils.listBeanToMapBean(dispatcherList, "message_mode");
		request.setAttribute("dispatcherMap", dispatcherMap);

		if (null != dispatcherList && dispatcherList.size() > 0) {
			String dispatcherListJSON = new JSONSerializer().exclude("*.class").
			deepSerialize(ConversionUtils.listBeanToListMap(dispatcherList));
			request.setAttribute("dispatcherListJSON", dispatcherListJSON);
		}
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List<BasicDynaBean> msgActionLists = messageActionDAO.listAll();
		List <BasicDynaBean> customMessageTypes = new ArrayList <BasicDynaBean>();
		customMessageTypes.add(messageCategoryDAO.findByKey("message_category_name", "Custom"));
		customMessageTypes.add(messageCategoryDAO.findByKey("message_category_name",
		    "Custom Promotional"));
		request.setAttribute("customMessageTypes", customMessageTypes);
		request.setAttribute("msgActionLists", msgActionLists);
		List<BasicDynaBean> dispatcherList = getDispatcherList(null);

		if (null != dispatcherList && dispatcherList.size() > 0) {
			String dispatcherListJSON = new JSONSerializer().exclude("*.class").
			deepSerialize(ConversionUtils.listBeanToListMap(dispatcherList));
			request.setAttribute("dispatcherListJSON", dispatcherListJSON);
		}
		return mapping.findForward("addshow");
	}


	public ActionForward create(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Map params = request.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = messageTypesDAO.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (!errors.isEmpty()) {
			flash.error("Incorrectly formatted values supplied");
			return redirect;
		}

		boolean success = false;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			DynaBean existingBean = messageTypesDAO.findByKey("message_type_name", bean.get("message_type_name"));
			if (null != existingBean) {
				flash.error("Message Type with the same name already exists");
				return redirect;
			}

			String messageTypeId = getMessageTypeId((String)bean.get("message_type_name"));
			bean.set("message_type_id", messageTypeId);
			bean.set("message_group_name", (String)bean.get("message_type_name"));
			String actionIds[] = request.getParameterValues("msgActionId");
			String override[] =  request.getParameterValues("actionoverride");
			messageActionType(actionIds, override, messageTypeId, con, success);
			if (null == bean.get("event_id") || bean.get("event_id").toString().isEmpty()) {
				bean.set("event_id", DEFAULT_EVENT_ID);
			}

			success = messageTypesDAO.insert(con, bean);

			if (success) {
				// add attachments if any
				if (null != form) {
					MessageForm msgForm = (MessageForm) form;
					FormFile attachment = msgForm.getAttachment();
					if (null != attachment &&
							null != attachment.getFileName() &&
							attachment.getFileName().trim().length() != 0) {
						success &= addAttachment(con, messageTypeId, attachment);
					}

				}
			}

			if (success) {
				flash.success("Message type inserted successfully");
				redirect = new ActionRedirect(mapping.findForward("showRedirect"));
				redirect.addParameter("message_type_id", bean.get("message_type_id"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			} else {
				flash.error("Failed to add message type");
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		return redirect;
	}

	public ActionForward show(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String messageTypeId = request.getParameter("message_type_id");
		// Get the bean
		BasicDynaBean bean = messageTypesDAO.findByKey("message_type_id", messageTypeId);
		request.setAttribute("bean", bean);

		String msgTypeName =  (String)bean.get("message_type_name");
		List<BasicDynaBean> msgActionLists = messageActionDAO.listAll("message_action_id");
		request.setAttribute("msgActionLists", msgActionLists);
		request.setAttribute("practoSmsModule", new ModulesDAO().findByKey("module_id", "mod_practo_sms"));
		GenericDAO messageTypeActionDAO = new GenericDAO("message_type_actions");
		BasicDynaBean existingBeanAction = messageTypeActionDAO.findExistsByKey("message_type_id", messageTypeId);
		if(existingBeanAction != null){
		Integer maskAction = (Integer)existingBeanAction.get("message_action_mask");
		Integer maskOverrid = (Integer)existingBeanAction.get("sender_override_mask");
		if(null != maskAction || null != maskOverrid) {
		request.setAttribute("msgTypeAction", reverseBinaryValue(Integer.toString(maskAction)).toArray());
		request.setAttribute("msgTypeOverride", reverseBinaryValue(Integer.toString(maskOverrid)).toArray());
		  }
		}
    List <BasicDynaBean> customMessageTypes = new ArrayList <BasicDynaBean>();
    customMessageTypes.add(messageCategoryDAO.findByKey("message_category_name", "Custom"));
    customMessageTypes.add(messageCategoryDAO.findByKey("message_category_name",
        "Custom Promotional"));
    request.setAttribute("customMessageTypes", customMessageTypes);
		// Get the attachments

		List<String> columns = new ArrayList();
		columns.add("attachment_id");
		columns.add("attachment_name");
		columns.add("attachment_type");

		Map filter = new HashMap();
		filter.put("message_type_id", messageTypeId);

		List<BasicDynaBean> attachments = messageAttachmentsDAO.listAll(columns, filter, null);
		request.setAttribute("attachmentList", attachments);
		// Do not show the tokens if editability is N
		if(bean.get("editability") != null && !bean.get("editability").equals("N")){
			// Get the tokens
			MessageManager mgr = new MessageManager();
			Map<String, List> tokenMap = mgr.getMessageTokens(messageTypeId);
			request.setAttribute("tokenMap", tokenMap);
		}
		List<BasicDynaBean> dispatcherList = getDispatcherList(null);
		if (null != dispatcherList && dispatcherList.size() > 0) {
			String dispatcherListJSON = new JSONSerializer().exclude("*.class").
			deepSerialize(ConversionUtils.listBeanToListMap(dispatcherList));
			request.setAttribute("dispatcherListJSON", dispatcherListJSON);
		}

		GenericDAO configDao = new GenericDAO("message_config");
		List<BasicDynaBean> configList = configDao.findAllByKey("message_type_id", messageTypeId);
		request.setAttribute("configParamCount", (null == configList) ? 0 : configList.size());

		return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List errors = new ArrayList();
		String messageTypeId = request.getParameter("message_type_id");
		String messageTypeName = request.getParameter("message_type_name");

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("message_type_id", request.getParameter("message_type_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		if (null == messageTypeId || null == messageTypeName) {
			flash.error("Invalid key value passed");
			return redirect;
		}

		BasicDynaBean bean = messageTypesDAO.getBean();
		ConversionUtils.copyToDynaBean(request.getParameterMap(), bean, errors);

		if (!errors.isEmpty()) {
			flash.error("Incorrectly formatted values supplied");
			return redirect;
		}

		Connection con = null;
		boolean success = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			String actionIds[] = request.getParameterValues("msgActionId");
			String override[] = request.getParameterValues("actionoverride");
			BasicDynaBean existingBean = messageTypesDAO.findByKey("message_type_name", messageTypeName);
			if (null != existingBean && !existingBean.get("message_type_id").equals(messageTypeId)) {
				flash.error("Message Type with the same name already exists");
			} else {
				success = messageTypesDAO.updateWithName(con, bean.getMap(), "message_type_id") > 0;
			}
			messageActionType(actionIds, override, messageTypeId, con, success);
			if (success) {
				// add attachments if any
				if (null != form) {
					MessageForm msgForm = (MessageForm) form;
					FormFile attachment = msgForm.getAttachment();
					if (null != attachment &&
							null != attachment.getFileName() &&
							0 != attachment.getFileName().trim().length()) {
						success &= addAttachment(con, messageTypeId, attachment);
					}
				}
				String[] selectedIds = null;
				selectedIds = request.getParameterValues("attached_files");
				//remove attachments if they were unselected
				removeUnselectedAttachments(con, messageTypeId, selectedIds);
			}

			if (success) {
				flash.success("Message Type updated successfully");
			} else {
				flash.error("Message Type update failed");
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		return redirect;
	}

	private void messageActionType(String[] actionIds, String[] override,
			String messageTypeId, Connection con, boolean success)
			throws SQLException, IOException {
	     GenericDAO messageActionDao = new GenericDAO(
			"message_type_actions");
	     BasicDynaBean messageActionBean = messageActionDao.getBean();
	     BasicDynaBean existingBeanAction = messageActionDao.findByKey("message_type_id", messageTypeId);
		 messageActionBean.set("message_type_id", messageTypeId);
	     int actionIdsValue = 0;
	     int overrideValue = 0;
	     if(null != actionIds || null!= override){
	     for (int i = 0; i < actionIds.length; i++) {
	    	 actionIdsValue += Integer.parseInt(actionIds[i]);
			}
	     for (int i = 0; i < override.length; i++) {
	    	 overrideValue += Integer.parseInt(override[i]);
			}
	     }
	     messageActionBean.set("message_action_mask", actionIdsValue);
	     messageActionBean.set("sender_override_mask", overrideValue);

	     if (null != existingBeanAction && existingBeanAction.get("message_type_id").equals(messageTypeId)){
	    	 success = messageActionDao.updateWithName(con, messageActionBean.getMap(), "message_type_id") > 0;
	     }else{
	    	 success = messageActionDao.insert(con, messageActionBean);
	     }
	}

	private void removeUnselectedAttachments(Connection con, String messageTypeId,
			String[] selectedIds) throws SQLException {
			logger.debug("removeUnselectedAttachments...." + messageTypeId + " : " + selectedIds);
			List<BasicDynaBean> attachmentList = getAttachments("message_attachments", "message_type_id", messageTypeId, false);
			List<Integer> deleteAttachmentList = new ArrayList<Integer>();
			for (BasicDynaBean attachment : attachmentList) {
				Integer attachmentId = (Integer)attachment.get("attachment_id");
				boolean found = false;
				if (null != selectedIds) { // will be null where all template attachments were removed
					for (String selectedId : selectedIds) {
						if (attachmentId.equals(new Integer(selectedId))) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					deleteAttachmentList.add(attachmentId);
				}
			}
			for (Integer deleteId : deleteAttachmentList) {
			  messageAttachmentsDAO.delete(con, "attachment_id", deleteId);
			}
	}

	private List<BasicDynaBean> getAttachments(String tableName, String keycolumn,
			Object identifier, boolean includeByteaData) throws SQLException {
	logger.debug("getAttachments...." + tableName + " : " + keycolumn + " : " + identifier);

		List<BasicDynaBean> attachList = new ArrayList<BasicDynaBean>();

		if (null == tableName || null == keycolumn || null == identifier ||
				tableName.trim().length() == 0 || keycolumn.trim().length() == 0 ) {
			return attachList; // empty list
		}

		GenericDAO attachDao = new GenericDAO(tableName);

		List<String> columns = new ArrayList<String>();
		columns.add("attachment_id");
		columns.add("attachment_name");
		columns.add("attachment_type");

		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put(keycolumn, identifier);

		List<BasicDynaBean> attachments = attachDao.listAll(columns, filter, null);

		if (!includeByteaData) {
			return attachments;
		}

		List<BasicDynaBean> attachmentDataList = new ArrayList<BasicDynaBean>();
		for (BasicDynaBean attachment : attachments) {
			BasicDynaBean attachBean = attachDao.getBean();
			attachDao.loadByteaRecords(attachBean, "attachment_id", attachment.get("attachment_id"));
			attachmentDataList.add(attachBean);
		}

		return attachmentDataList;
	}

	private List<Character> reverseBinaryValue(String mask){
		String binaryMask = Integer.toBinaryString(Integer.parseInt(mask));
		String binaryMaskRev = "";
		for (int i = binaryMask.length() - 1; i >= 0; i--) {
			binaryMaskRev = binaryMaskRev + binaryMask.charAt(i);
		}
		char[] MaskCharArray = binaryMaskRev.toCharArray();
		List<Character> msgTypeActionListsArray = new ArrayList<Character>();
		for(char temp: MaskCharArray){
			msgTypeActionListsArray.add(temp);
		}
		return msgTypeActionListsArray;
	}

	private PagedList getMessageTypeList(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Connection con = null;
		PagedList pagedList = null;
		SearchQueryBuilder templateListBuilder = null;
		try {
			con = DataBaseUtil.getConnection();
			Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
			templateListBuilder =
					new SearchQueryBuilder(con, messageTemplateFields, messageTemplateCount,
							messageTemplateTables, listingParams);

			Map filterMap = request.getParameterMap();
			templateListBuilder.addFilterFromParamMap(filterMap);
			templateListBuilder.build();

			pagedList = templateListBuilder.getDynaPagedList();
		} finally {
			if (null != templateListBuilder) templateListBuilder.close();
			if (null != con) con.close();
		}

		return pagedList;
	}

	private String getMessageTypeId(String messageTypeName) {
		logger.debug("generating message id " + messageTypeName);
		String messageTypeId = null;
		if (null != messageTypeName) {
			messageTypeId = messageTypeName.replaceAll(" ", "_").toLowerCase().replaceAll("'", "");
		}
		return messageTypeId;
	}

	private List<BasicDynaBean> getDispatcherList(String messageMode) throws SQLException {

		GenericDAO dispatcherDao = new GenericDAO("message_dispatcher_config");
		List<String> dispatcherColumns = new ArrayList<String>();
		dispatcherColumns.add("message_mode");
		dispatcherColumns.add("display_name");
		dispatcherColumns.add("attachment_allowed");
		dispatcherColumns.add("max_attachment_kb");

		Map filterMap = new HashMap();

		if (null != messageMode && messageMode.trim().length() > 0) {
			filterMap.put("message_mode", messageMode);
		}
		// filterMap.put("status", "A");

		List<BasicDynaBean> dispatcherList = dispatcherDao.listAll(dispatcherColumns, filterMap, "display_name");
		logger.debug("getDispatcherList....dispatcher" + dispatcherList.size());

		return dispatcherList;
	}

	private boolean addAttachment(Connection con, String messageTypeId, FormFile attachment)
	throws SQLException, IOException {

		logger.debug("addAttachment...." + messageTypeId );

		if (null == con || null == messageTypeId || null == attachment) {
			return false;
		}

		BasicDynaBean attachBean = messageAttachmentsDAO.getBean();
		int attachmentId = messageAttachmentsDAO.getNextSequence();
		attachBean.set("attachment_id", attachmentId);
		attachBean.set("message_type_id", messageTypeId);
		attachBean.set("attachment_name", attachment.getFileName());
		attachBean.set("attachment_type", attachment.getContentType());
		attachBean.set("attachment_bytes", attachment.getInputStream());
		return messageAttachmentsDAO.insert(con, attachBean);
	}


}
