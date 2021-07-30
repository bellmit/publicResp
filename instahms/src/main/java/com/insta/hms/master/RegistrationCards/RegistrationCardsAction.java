package com.insta.hms.master.RegistrationCards;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RegistrationCardsAction extends DispatchAction{

	public ActionForward list(ActionMapping m, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
			throws SQLException, IOException, ParseException, Exception {

		RegistrationCardsDAO dao = new RegistrationCardsDAO();

		PagedList pagedList = dao.getCustomerRegCardList(req.getParameterMap(), ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");

	}

	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{

		JSONSerializer js = new JSONSerializer().exclude("class");

		RegistrationCardsDAO rdao=new RegistrationCardsDAO();
		request.setAttribute("ratePlanlists", rdao.ratePlansList());

		ArrayList<String> avlCards = (ArrayList)RegistrationCardsDAO.getAllCards();
		request.setAttribute("avlCardsList", js.serialize(avlCards));
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{
		JSONSerializer js = new JSONSerializer().exclude("class");
		int cardId=Integer.parseInt(request.getParameter("card_id"));

		RegistrationCardsDAO rdao=new RegistrationCardsDAO();
		request.setAttribute("ratePlanlists", rdao.ratePlansList());

		BasicDynaBean bean = rdao.getBean();
		rdao.loadByteaRecords(bean, "card_id", cardId);

		request.setAttribute("selectedRegcardList", js.serialize(bean.getMap()));

		ArrayList<String> avlCards = (ArrayList)RegistrationCardsDAO.getAllCards();
		request.setAttribute("avlCardsList", js.serialize(avlCards));

		request.setAttribute("bean", bean);
		return mapping.findForward("addshow");
	}

	private static final String[] STRING_FIELDS = {"card_name", "visit_type", "rate_plan","status"};

	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{

		Connection con = null;
		boolean status = false;
	    RegistrationCardsDAO dao = new RegistrationCardsDAO();
	    FlashScope flash = FlashScope.getScope(request);
	    ActionRedirect redirect = null;
	    HashMap fields = new HashMap();
		ConversionUtils.copyStringFields(request.getParameterMap(), fields, STRING_FIELDS, null);

		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			int cardId = dao.getNextSequence();
			fields.put("card_id", cardId);
			status =  dao.insertCustomerRegCardDetails(con,fields);
			RegistrationCardsForm f = (RegistrationCardsForm) form;
			FormFile ff = f.getCustom_reg_card_template();
			FormFile odtFile = f.getOdtFile();
			if (!odtFile.getContentType().equals("application/vnd.oasis.opendocument.text")) {
				flash.put("error", "File type not supported. Please upload the odt file.");
				redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		    	redirect.addParameter("from", "add");
		    	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		    	return redirect;
			}
			String userName = (String) request.getSession(false).getAttribute("userid");

			if (status) {
				status = dao.updateRegistrationCustomerCardTemplate(con, cardId, ff.getInputStream(), ff.getFileSize());
				status = status && dao.updateOdtFile(con, cardId, odtFile.getInputStream(), odtFile.getFileSize(), userName);
				if (status) {
					flash.put("success", "Registration Card details inserted successfully..");
					redirect = new ActionRedirect(mapping.findForward("showRedirect"));
					redirect.addParameter("card_id", new Integer(cardId));
					redirect.addParameter("from", "edit");
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
				else {
					flash.put("error", "Failed to add the details");
				}
			}
		}finally{
				DataBaseUtil.commitClose(con, status);
		}

		if (con!=null) con.close();
    	redirect = new ActionRedirect(mapping.findForward("addRedirect"));
    	redirect.addParameter("from", "add");
    	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping m, ActionForm af,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{

		RegistrationCardsForm rForm = (RegistrationCardsForm) af;
		FlashScope flash = FlashScope.getScope(request);
		FormFile odtFile = rForm.getOdtFile();
		FormFile ff = rForm.getCustom_reg_card_template();
		RegistrationCardsDAO dao = new RegistrationCardsDAO();
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		int cardId = Integer.parseInt(rForm.getCardId());

		BasicDynaBean existBean = dao.getBean();
		dao.loadByteaRecords(existBean, "card_id", cardId);
		Object cardTemplate = existBean.get("custom_reg_card_template");
		Object odt = existBean.get("odt_file");

		if ((odt == null || odt.equals("")) && !(odtFile.getFileSize() != 0)) {
			flash.put("error", "File not exist in table. Please upload the odt file.");
			redirect.addParameter("from", "edit");
			redirect.addParameter("card_id", new Integer(cardId));
	    	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
	    	return redirect;
		}

		if (odtFile.getFileSize() != 0 && !odtFile.getContentType().equals("application/vnd.oasis.opendocument.text")) {
			flash.put("error", "File type not supported. Please upload the odt file.");
			redirect.addParameter("from", "edit");
			redirect.addParameter("card_id", new Integer(cardId));
	    	redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
	    	return redirect;
		}

		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean status = false;
		Map params = request.getParameterMap();
		List errors = new ArrayList();

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		HashMap fields = new HashMap();
		ConversionUtils.copyStringFields(request.getParameterMap(), fields, STRING_FIELDS, null);

		status = dao.updateCustomerRegCardDetails(con,cardId,fields);

		if (ff.getFileSize()!=0) {
			status = dao.updateCoustmerRegCardTemplate(con, cardId, ff.getInputStream(), ff.getFileSize());
		}
		String userName = (String) request.getSession(false).getAttribute("userid");
		if (odtFile.getFileSize() != 0) {
			status = status && dao.updateOdtFile(con, cardId, odtFile.getInputStream(), odtFile.getFileSize(), userName);
		}
		if (status) {
			con.commit();
			flash.put("success", "Registration Card details updated successfully..");

		} else {
			con.rollback();
			flash.put("error", "Failed to update Registration Card details..");
		}

		if (con != null) con.close();
		redirect.addParameter("card_id", new Integer(cardId));
		redirect.addParameter("from", "edit");
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward showCustomerRegcard(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, SQLException {

		RegistrationCardsDAO dao = new RegistrationCardsDAO();
		res.setContentType("application/pdf");
		OutputStream os = res.getOutputStream();
		String cardId = req.getParameter("cardId");
		InputStream logo = dao.getCustomRegCardTemplate(Integer.parseInt(cardId));
		byte[] bytes = new byte[4096];
		int len = 0;
		// If invalid file was uploaded, the file will not be saved/existing
		// so need to check for file content.
		if (logo != null) {
			while ((len = logo.read(bytes)) > 0) {
				os.write(bytes, 0, len);
			}
			os.flush();
			logo.close();
			return null;
		}else {
			return m.findForward("fileNotFound");
		}
	}

	public ActionForward getOdtFile(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		RegistrationCardsDAO dao = new RegistrationCardsDAO();
		response.setContentType("application/vnd.oasis.opendocument.text");
		OutputStream os = response.getOutputStream();
		String cardId = request.getParameter("cardId");

		BasicDynaBean bean = dao.getBean();
		dao.loadByteaRecords(bean, "card_id", Integer.parseInt(cardId));

		response.setContentType("application/vnd.oasis.opendocument.text");

		byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream)bean.get("odt_file"));
		OutputStream stream = response.getOutputStream();
		stream.write(bytes);
		stream.flush();
		stream.close();

		return null;
	}
}
