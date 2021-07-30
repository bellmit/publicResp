/**
 *
 *
 */
package com.insta.hms.master.PatientHeaderPref;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Anil N
 *
 */
public class PatientHeaderPrefAction extends BaseAction {
    static Logger log = LoggerFactory.getLogger(PatientHeaderPrefAction.class);

    PatientHeaderPrefDAO dao = new PatientHeaderPrefDAO();

    public ActionForward getPatientHeaderPref(ActionMapping mapping, ActionForm form,
            HttpServletRequest req, HttpServletResponse res) throws Exception,IOException {

        Map map= req.getParameterMap();
		JSONSerializer js = new JSONSerializer().exclude("class");
        log.info("Getting Patient Header Pref data,getPatientHeaderPref");
        List fieldsList = PatientHeaderPrefDAO.getAllPatientHeaderPrefFields();
        PagedList pagedList = dao.getPatientHeaderDetails(map, ConversionUtils.getListingParameter(req.getParameterMap()));
        log.debug("End getPatientHeaderPref");
        req.setAttribute("fieldsLists", js.deepSerialize(ConversionUtils.listBeanToListMap(fieldsList)));
        req.setAttribute("pagedList", pagedList);

      return mapping.findForward("addshow");
    }

    public ActionForward update(ActionMapping mapping, ActionForm form,
            HttpServletRequest req, HttpServletResponse resp) throws Exception, IOException {

        boolean success = false;
        Connection con = null;
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        Map map = getParameterMap(req);
        map.put("pageNum", req.getParameter("pageNum"));
        try {
            con = DataBaseUtil.getConnection();
            con.setAutoCommit(false);
            log.info("updating Patient Header Pref data,update");
            success = updatePatientHeaderPref(con, req);
            log.debug("end update method for patient header pref");
        } finally {
            DataBaseUtil.commitClose(con, success);
        }
        if(success){
            redirect.addParameter("pageNum", req.getParameter("pageNum"));
        }
        return redirect;
    }

     private boolean updatePatientHeaderPref(Connection con, HttpServletRequest req) throws SQLException, Exception{

            boolean success = false;
            String[] fieldname = req.getParameterValues("field_name");
            String[] datalevel = req.getParameterValues("data_level");
            String[] datacategory = req.getParameterValues("data_category");
            String[] display = req.getParameterValues("display");
            String[] fielddesc = req.getParameterValues("field_desc");
            String[] visittype = req.getParameterValues("visit_type");
            String[] displayorder = req.getParameterValues("display_order");
            BasicDynaBean updatePatientPref = null;
            for(int i=0; i<fieldname.length; i++){
                updatePatientPref = dao.getBean();
                updatePatientPref.set("data_category", datacategory[i]);
                updatePatientPref.set("display", display[i]);
                updatePatientPref.set("field_desc", fielddesc[i]);
                updatePatientPref.set("visit_type", visittype[i]);
                updatePatientPref.set("display_order", Integer.parseInt(displayorder[i]));
                if(fieldname[i] != null){
                    Map<String,String> keys = new HashMap<String,String>();
                    keys.put("field_name", fieldname[i]);
                    updatePatientPref.set("field_name", fieldname[i]);
                    success = dao.update(con, updatePatientPref.getMap(), keys) > 0;
                }
              }
            return success;
        }

}