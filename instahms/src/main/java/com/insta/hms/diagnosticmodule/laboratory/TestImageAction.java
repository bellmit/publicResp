package com.insta.hms.diagnosticmodule.laboratory;

import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.Encoder;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.TestReportImages;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestImageAction extends BaseAction {

	static public Logger log = LoggerFactory.getLogger(TestImageAction.class);
	JSONSerializer js = new JSONSerializer().exclude("class");
	
    private static final GenericDAO testsPrescribedDAO = new GenericDAO("tests_prescribed");

	public ActionForward getAttachImagesScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception{

			int prescribedId = Integer.parseInt(request.getParameter("prescribedId"));
			List getImagesListForReport = LaboratoryDAO.getImageDetails(prescribedId);
			request.setAttribute("imagesList", getImagesListForReport);
			request.setAttribute("imagesListjson", js.deepSerialize(
					ConversionUtils.copyListDynaBeansToMap(LaboratoryDAO.getImageDetails(prescribedId))));
			request.setAttribute("category", mapping.getProperty("category"));
			BasicDynaBean testdetails = LaboratoryDAO.getPrescribedDetails(prescribedId);
			request.setAttribute("testdetails", testdetails.getMap());
			String hospital = (String) testdetails.get("hospital");
			request.setAttribute("isIncomingPatient", hospital.equals("incoming"));

		return mapping.findForward("getAttachImagesScreenPopup");
	}

	public ActionForward getTemplateEditor(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		String formatid = request.getParameter("formatid");
		String prescribedid = request.getParameter("prescribedid");
		String testDetailsId = request.getParameter("testDetailsId");

		LaboratoryBO bo = new LaboratoryBO();
		String templ = bo.getTemplateContent(formatid, prescribedid,testDetailsId);
		request.setAttribute("templateContent", templ);

		request.setAttribute("prescribedid", prescribedid);
		request.setAttribute("testDetailsId", testDetailsId);
		BasicDynaBean testdetails = LaboratoryDAO.getPrescribedDetails( Integer.parseInt(prescribedid) );
		request.setAttribute("testdetails", testdetails.getMap());
		List getImagesListForReport = LaboratoryDAO.getImageDetails( Integer.parseInt(prescribedid) );
		request.setAttribute("imagesList", getImagesListForReport);
		request.setAttribute("imagesListjson", js.deepSerialize(
				ConversionUtils.copyListDynaBeansToMap(
						LaboratoryDAO.getImageDetails( Integer.parseInt(prescribedid)))) );


		BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		request.setAttribute("prefs",printPrefs);
		request.setAttribute("isAddendum", false);
		request.setAttribute("category", mapping.getProperty("category"));

		return mapping.findForward("templateEditor");
	}

	public ActionForward uploadImages(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception{

		LaboratoryForm rf = (LaboratoryForm)form;
		FormFile image = rf.getFormFile();
		int prescribedId = Integer.parseInt(request.getParameter("prescribedid"));
		String title = rf.getImageTitle();

        BasicDynaBean testPrescBean = testsPrescribedDAO.findByKey("prescribed_id", prescribedId);


		TestReportImages trm =null;
		boolean status = false;
		if(image !=null && image.getFileSize() >0 ){
			trm  = new TestReportImages();
			trm.setImage(image);
			trm.setImageName(image.getFileName());
			trm.setTitle(title);
			trm.setPrescribedId(prescribedId);
			trm.setDocType(image.getContentType());

			status = LaboratoryBO.uploadImage(trm);
		}

		FlashScope flash = FlashScope.getScope(request);
		if( !status )
			flash.put("uploadStatus","Failed to Upload Image..");

		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("getAttachImagesRedirect"));
   		redirect.addParameter("formatid", request.getParameter("formatid"));
   		redirect.addParameter("prescribedId", request.getParameter("prescribedid"));
   		redirect.addParameter("testDetailsId", request.getParameter("testDetailsId"));
   		redirect.addParameter("category", mapping.getProperty("category"));
   		redirect.addParameter("reportId", testPrescBean.get("report_id"));
   		redirect.addParameter("patientId", testPrescBean.get("pat_id"));
   		redirect.addParameter(FlashScope.FLASH_KEY,flash.key());

		return redirect;
	}


	public ActionForward deleteImage(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception {

		int prescribedId = Integer.parseInt(request.getParameter("prescribedId"));
		String imageTitle = request.getParameter("titleName");
		BasicDynaBean testprescBean = testsPrescribedDAO.findByKey("prescribed_id", prescribedId);
		boolean status= false;

		TestReportImages trm = new TestReportImages();
	    trm.setTitle(imageTitle);
	    trm.setPrescribedId(prescribedId);

	    status = LaboratoryBO.deleteImage(trm);

	    FlashScope flash = FlashScope.getScope(request);
		if(status){
			flash.put("uploadStatus" ,"Image is deleted Successfully..");
		}else{
			flash.put("uploadStatus","Failed to delete Image..");
		}

   		ActionRedirect redirect = new ActionRedirect(mapping.findForwardConfig("getAttachImagesRedirect"));
   		redirect.addParameter("prescribedId", prescribedId);
   		redirect.addParameter("category", mapping.getProperty("category"));
   		redirect.addParameter("reportId", testprescBean.get("report_id"));
   		redirect.addParameter("patientId", testprescBean.get("pat_id"));
   		redirect.addParameter(FlashScope.FLASH_KEY,flash.key());

		return redirect;
	}

	public ActionForward getImage(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception {

		int prescribedId = Integer.parseInt(request.getParameter("prescribedId"));
		String imageTitle = request.getParameter("titleName");
		BasicDynaBean testprescBean = testsPrescribedDAO.findByKey("prescribed_id", prescribedId);

		String url = mapping.getProperty("category").equals("DEP_LAB") ? "Laboratory" : "Radiology";
			   url = url + "/AddTestImages.do?_method=viewImage&category="+mapping.getProperty("category") +
					"&prescribedId="+prescribedId+"&titleName="+Encoder.cleanURL(imageTitle)+"&reportId=" +
					  Encoder.cleanURL(request.getParameter("reportId"))+"&patientId"+Encoder.cleanURL(request.getParameter("patientId"));
		request.setAttribute("generatedURL", url);

		List getImagesListForReport = LaboratoryDAO.getImageDetails(prescribedId);
		request.setAttribute("imagesList", getImagesListForReport);
		request.setAttribute("imagesListjson", js.deepSerialize(
				ConversionUtils.copyListDynaBeansToMap(LaboratoryDAO.getImageDetails(prescribedId))));
		request.setAttribute("testPrescDetails", testprescBean);
		request.setAttribute("category", mapping.getProperty("category"));
		BasicDynaBean testdetails = LaboratoryDAO.getPrescribedDetails( prescribedId );
		request.setAttribute("testdetails", testdetails.getMap());
		String hospital = (String) testdetails.get("hospital");
		request.setAttribute("isIncomingPatient", hospital.equals("incoming"));

		return  mapping.findForward("getAttachImagesScreenPopup");
	}

	public ActionForward viewImage(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException, Exception {

		log.debug("View Image action");
		int prescribedId = Integer.parseInt(request.getParameter("prescribedId"));
		String imageTitle = request.getParameter("titleName");

		response.setContentType("image/gif");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		InputStream image = LaboratoryDAO.getImageStream(prescribedId, imageTitle);
		ServletOutputStream os = response.getOutputStream();

		byte[] bytes = new byte[4096];
		int len = 0;
		while ( (len = image.read(bytes)) > 0) {
			os.write(bytes, 0, len);
		}

		os.flush();
		image.close();
		return null;
	}

}
