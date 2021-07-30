package com.insta.hms.master.DiagnosisCodeFavourites;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Anil N
 *
 */
public class DiagnosisCodeFavouritesAction extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(DiagnosisCodeFavouritesAction.class);

	DoctorMasterDAO dmDao = new DoctorMasterDAO();
	MRDCodesMasterDAO dcDAO = new MRDCodesMasterDAO();
	GenericDAO gDAO = new GenericDAO("mrd_supported_codes");

    public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
            HttpServletResponse resp) throws Exception, IOException {

    	String code = req.getParameter("code");
    	String searchCode = req.getParameter("searchCode");
    	String favourite = req.getParameter("fav");
    	PagedList pagedList = null;
    	JSONSerializer js = new JSONSerializer().exclude("class");
    	List<BasicDynaBean> selectedCodes = null;
    	Map requestParams = req.getParameterMap();
    	Map<LISTING, Object> pagingParams = ConversionUtils.getListingParameter(requestParams);
    	Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("code_category", "Diagnosis");

    	HttpSession session = req.getSession(false);
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		String doctorId = req.getParameter("doctor_id");
		//BasicDynaBean doctorBean = dmDao.findByKey("doctor_id", doctorId);
		BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorFavouriteCen(doctorId);
		int centerId;
		if (doctorBean != null && !doctorBean.equals("") && ((Integer)doctorBean.get("center_id") != 0)) {
			centerId = (Integer) doctorBean.get("center_id");
		} else if ((Integer) session.getAttribute("centerId") != 0) {
			centerId = (Integer) session.getAttribute("centerId");
		} else {
			centerId = 0;
		}
		List<BasicDynaBean> codeType = gDAO.listAll(null, filterMap, "code_type");
		if(!"".equals(code) && code != null){
			List codesSearchList = null;
			if(favourite != null &&  !"".equals(favourite)){
				pagedList = dcDAO.getSelectedFavourites(requestParams, pagingParams, doctorId, code, searchCode);
				codesSearchList = MRDCodesMasterDAO.getAllFavDocSearchCodes(doctorId, code);
				}
			else {
				pagedList = dcDAO.getMRDCodesMasterList(requestParams, pagingParams, code, searchCode);
				codesSearchList = MRDCodesMasterDAO.getAllCodeTypesCodes(code);
			}
		    req.setAttribute("code", code);
		    selectedCodes = MRDCodesMasterDAO.getSelectedCodesList(doctorId, code);
		    Map codesMap = ConversionUtils.listBeanToMapMap(selectedCodes,"code");
			req.setAttribute("codesMap", codesMap);
			req.setAttribute("codeSearchList", js.deepSerialize(ConversionUtils.listBeanToListMap(codesSearchList)));
		}
		req.setAttribute("selectedCodes", selectedCodes);
		req.setAttribute("codeType", codeType);
		req.setAttribute("fav", favourite);
		req.setAttribute("Max_centers_inc_default", (Integer)genericPrefs.get("max_centers_inc_default"));
		req.setAttribute("center_id_js",centerId);
		req.setAttribute("doctor_id", doctorId);
		req.setAttribute("doctor_bean", doctorBean);;
		req.setAttribute("pagedList", pagedList);
        return mapping.findForward("list");

    }

    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest req,
    		                           HttpServletResponse resp) throws Exception {

    	Connection con = null;
    	ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    	String doctorId = req.getParameter("doctor_id");
    	String[] avlcode = req.getParameterValues("avlcode");
    	String[] selcode = req.getParameterValues("selcode");
    	String favourite = req.getParameter("searchFav");
    	HttpSession session = req.getSession(false);
    	BasicDynaBean bean = dcDAO.getBean();
		//BasicDynaBean doctorBean = dmDao.findByKey("doctor_id", doctorId);
    	BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorFavouriteCen(doctorId);
		int centerId;
		FlashScope flash = FlashScope.getScope(req);
		if (doctorBean != null && !doctorBean.equals("") && ((Integer)doctorBean.get("center_id") != 0)) {
			centerId = (Integer) doctorBean.get("center_id");
		} else if ((Integer) session.getAttribute("centerId") != 0) {
			centerId = (Integer) session.getAttribute("centerId");
		} else {
			centerId = 0;
		}

		String codeType = req.getParameter("code_type");
		LinkedHashMap<String, Object> del_code = new LinkedHashMap<String,Object>();
 	    del_code.put("code_type",codeType);
 	    del_code.put("doctor_id", doctorId);
        req.setAttribute("centerId", centerId);
        boolean success = true;
        List<BasicDynaBean> selectedCodes = MRDCodesMasterDAO.getSelectedCodesList(doctorId, codeType);
        Map codesMap = ConversionUtils.listBeanToMapMap(selectedCodes,"code");
        try{
        	con = DataBaseUtil.getConnection();
     	    con.setAutoCommit(false);
     	    if(MRDCodesMasterDAO.exists(doctorId, codeType)){
     	    	for(int i = 0; i< avlcode.length ; i++){
     	    		if(codesMap.keySet().contains(avlcode[i])){
     	    			del_code.put("code", avlcode[i]);
     	    			success = dcDAO.delete(con, del_code);
     	    		}
     	    	}
     	    	if( selcode != null){
     	    			for(int j=0; j<selcode.length; j++){
         	    				bean.set("code", selcode[j]);
         	    				bean.set("code_type", codeType);
         	    				bean.set("doctor_id", doctorId);
         	    				success = dcDAO.insert(con, bean);
     	    		}
     	    	}
     	    }
     	    else if(selcode != null){
     	    	for(int j=0; j<selcode.length; j++){
	    				bean.set("code", selcode[j]);
	    				bean.set("code_type", codeType);
	    				bean.set("doctor_id", doctorId);
	    				success = dcDAO.insert(con, bean);
	    			}
     	    	}
        }
           finally {
       		DataBaseUtil.commitClose(con, success);
       		}
        if(success){
        	flash.success(getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.success"));
        }
        else{
        	flash.error(getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.failure"));
        }
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        redirect.addParameter("doctor_id", doctorId);
        redirect.addParameter("code", codeType);
        redirect.addParameter("fav", favourite);
        redirect.addParameter("pageNum", req.getParameter("pageNum"));
    	return redirect;
    }

    public ActionForward exportICDFavDetails(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse
    		resp ) throws Exception {

    	String doctorId = req.getParameter("doctor_id");
    	List<String> icdColumnNames = Arrays.asList(new String[]
    	                             {"code_doc_id","Code Type","Code","Doctor Name"});

    	XSSFWorkbook workbook = new XSSFWorkbook();
    	XSSFSheet icdWorkSheet = workbook.createSheet("ICDFAVOURITES");
    	Map<String, List> columnNamesMap = new HashMap<String, List>();
    	if(doctorId != null || !"".equals(doctorId)){
    		List<BasicDynaBean> icdCodeFavourites = dcDAO.getICDFavourites(doctorId);
    		columnNamesMap.put("mainItems", icdColumnNames);
     		HsSfWorkbookUtils.createPhysicalCellsWithValues(icdCodeFavourites, columnNamesMap, icdWorkSheet, true);
    	}
 		resp.setHeader("Content-type", "application/vnd.ms-excel");
 		resp.setHeader("Content-disposition","attachment; filename=ICDFavouriteDetails.xls");
 		resp.setHeader("Readonly", "true");
 		java.io.OutputStream os = resp.getOutputStream();
 		workbook.write(os);
 		os.flush();
 		os.close();

 		return  null;
    }

    public ActionForward importICDFavDetailsFromXls (ActionMapping mapping, ActionForm form, HttpServletRequest req,
    		HttpServletResponse resp ) throws SQLException , IOException {

    	ICDFavForm icdForm = (ICDFavForm) form;
    	ByteArrayInputStream byteStream = new ByteArrayInputStream(icdForm.getXlsICDFile().getFileData());
    	XSSFWorkbook workBook = new XSSFWorkbook(byteStream);
		XSSFSheet sheet = workBook.getSheetAt(0);

		String doctorId = req.getParameter("doctor_id");
		BasicDynaBean doctorBean = dmDao.findByKey("doctor_id", doctorId);
		StringBuilder errors = new StringBuilder();
		this.errors = errors ;
		Map<String, String> aliasMap = new HashMap<String, String>();

		aliasMap.put("code type", "code_type");
		aliasMap.put("code", "code");
		aliasMap.put("doctor name", "doctor_id");

		List<String> expectFileds = Arrays.asList(new String[] {"code_type", "code", "doctor_id"});

		importICDFavDetails(sheet, aliasMap, expectFileds, errors, doctorBean, req);

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listshow"));
		FlashScope flash = FlashScope.getScope(req);

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.file.save.success"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("doctor_id", doctorId);
		redirect.addParameter("doctor_bean", doctorBean);
		return redirect;
    }

    public void importICDFavDetails(XSSFSheet sheet, Map aliasMap, List<String> expectFileds, StringBuilder errors,
    		BasicDynaBean doctorBean, HttpServletRequest req) throws SQLException, IOException {

    	Iterator rowIterator = sheet.rowIterator();
		XSSFRow row1 = (XSSFRow)rowIterator.next();
		String sheetName = sheet.getSheetName();
		this.errors = errors;

		String doctorId = doctorBean.getMap().get("doctor_id").toString();
		String doctorName = doctorBean.getMap().get("doctor_name").toString();
		Connection con = null;
		List<BasicDynaBean> avlbleCodeList = null;
		boolean lineHasError = false;
		boolean success = false;

		BasicDynaBean bean = dcDAO.getBean();
		String[] headers = new String[row1.getLastCellNum()];
		String[] xlHeaders = new String[row1.getLastCellNum()];

		for (int i=1; i<headers.length; i++) {

			XSSFCell cell = row1.getCell(i);
			if (cell == null)
				headers[i] = null; /*putting null values, if found*/
			else {

				String header = cell.getStringCellValue().trim().toLowerCase();
				String dbName = (String) (aliasMap.get(header) == null ? header : aliasMap.get(header));
				headers[i] = dbName;
				xlHeaders[i] = header;


				if (bean.getDynaClass().getDynaProperty(dbName) == null && !expectFileds.contains(dbName) ) {
					addError(0, getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.header.unknown")+" "+dbName +getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.sheet")+" "+sheetName,req);
					headers[i] = null;
					xlHeaders[i] = null;
					lineHasError = true;
				}

			}
		}

		if(this.errors.length() < 1){
			List<BasicDynaBean> favCodesList = new ArrayList<BasicDynaBean>();
			List<String> codeTypes = null;
			avlbleCodeList = MRDCodesMasterDAO.getAllAvlbeCodes();
			Map codeMap = ConversionUtils.listBeanToMapMapMap(avlbleCodeList, "code_type", "code");
			codeTypes = dcDAO.getCodeTypes();

			nxtLine:while(rowIterator.hasNext()) {
				BasicDynaBean docMasterBean = dcDAO.getBean();
				String codeType = null;
				XSSFRow row = (XSSFRow)rowIterator.next();
				int lineNumber = row.getRowNum()+1;
				String code = null;

				nxtCell:for (int j=1; j<headers.length; j++) {
			        Object cellVal = null;
			        XSSFCell rowcell = row.getCell(j);
			        if(rowcell != null ){
			        if(headers[j].equals("code_type")){
			        	switch (rowcell.getCellType()) {
						case XSSFCell.CELL_TYPE_NUMERIC: {
							cellVal = rowcell.getNumericCellValue();
							break;
						}

						case XSSFCell.CELL_TYPE_STRING: {
							cellVal = rowcell.getStringCellValue();
							break;
						}
						}
			        	codeType = cellVal.toString();
			        	if(!codeTypes.contains(codeType)){
			        		     addError(lineNumber, getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.codetype.unknown")+" "+sheetName,req);
			        		      lineHasError = true;
			        		}
			        	codeType = cellVal.toString();
			        	if(!lineHasError){
			        		continue nxtCell;
			        	}
			        	}
			        else if(headers[j].equals("code")){
			        	switch (rowcell.getCellType()) {
						case XSSFCell.CELL_TYPE_NUMERIC: {
							cellVal = rowcell.getNumericCellValue();
							break;
						}

						case XSSFCell.CELL_TYPE_STRING: {
							cellVal = rowcell.getStringCellValue();
							break;
						}
						}
			        	Map codes = (Map) codeMap.get(codeType);
			        	if(codes != null ){
			        		if(!codes.containsKey(cellVal)){
			        			addError(lineNumber, getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.code.unknown")+" "+sheetName,req);
			        		     lineHasError = true;
			        		}
			        	}
			        	code = cellVal.toString();
			        	if(!lineHasError){
			        		continue nxtCell;
			        	}
			        	}
			        else if(headers[j].equals("doctor_id")){
			        	switch (rowcell.getCellType()) {
						case XSSFCell.CELL_TYPE_NUMERIC: {
							cellVal = rowcell.getNumericCellValue();
							break;
						}

						case XSSFCell.CELL_TYPE_STRING: {
							cellVal = rowcell.getStringCellValue();
							break;
						}
						}
			        	if(!cellVal.toString().equalsIgnoreCase(doctorName)){
			        		    addError(lineNumber,getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.doctor.unknown")+" "+sheetName,req);
			        		    lineHasError = true;
			        		}
			        	if(!lineHasError){
			        		continue nxtCell;
			        	}
			        	}
				   }
			        else {
			        	addError(lineNumber,getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.nullvalue")+" "+sheetName, req);
	        		    lineHasError = true;
			        }
			        continue nxtCell;
				}
				docMasterBean.set("code_type", codeType );
				docMasterBean.set("code", code);
				docMasterBean.set("doctor_id", doctorId);
				favCodesList.add(docMasterBean);
				continue nxtLine;
			  }
			if(!lineHasError){
				boolean insertStatus = false;
					if(dcDAO.exist("doctor_id",doctorId, false )){
						try {
						con = DataBaseUtil.getConnection();
						con.setAutoCommit(false);
						success = dcDAO.delete(con, "doctor_id", doctorId);
						}
						finally {
							DataBaseUtil.commitClose(con, success);
						}
					}
					insertStatus = dcDAO.insertICDFavourites(favCodesList);
				}
			}
		}

    private StringBuilder errors;
    private void addError(int line, String msg, HttpServletRequest req) {

        if (line > 0) {

            this.errors.append(getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.line")).append(" ").append(line).append(": ");

        } else {

            this.errors.append(getResources(req).getMessage("patient.doctor.diagnosiscodefavourites.action.header.error"));
        }

        this.errors.append(msg).append("<br>");
        logger.error("Line " + line + ": " + msg);

    }
}
