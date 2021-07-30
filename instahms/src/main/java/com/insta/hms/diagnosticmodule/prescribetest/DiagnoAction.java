package com.insta.hms.diagnosticmodule.prescribetest;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;


public class DiagnoAction extends DispatchAction{
    static Logger log4jLogger = LoggerFactory.getLogger(DiagnoAction.class);

    public ActionForward getPrescribeTestScreen(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws ServletException,Exception{

    		JSONSerializer js = new JSONSerializer().exclude("class");

	        DiagnoBOIF bo = DiagnoFactory.getDiagnoBO();
	        request.setAttribute("patientmrnos", bo.getpatients());
	        request.setAttribute("doctorlist", bo.getdoctorslist());
	        request.setAttribute("doctorjson",  js.serialize(bo.getdoctorslist()));
	        request.setAttribute("arrdeptDetails",bo.getDeapartmentlist());
	        request.setAttribute("diagdepartmentlist", bo.getdiagnosticdepartmentlist());
	        request.setAttribute("module", request.getParameter("category"));
	        request.setAttribute("diagDeptList", js.serialize(bo.getdiagnosticdepartmentlist()));
	        request.setAttribute("test_timestamp", new DiagnoDAOImpl().getCountFromDiagTimeStamp());

        return mapping.findForward("success");
    }


    public ActionForward getpatientdetails(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)	throws IOException{

        HttpSession session=null;
        try{
             session=request.getSession(false);
            Object username=session.getAttribute("userid");
            if(username==null){
                return mapping.findForward("login");
            }

            String mrno = request.getParameter("mrno");
            DiagnoBOIF bo = DiagnoFactory.getDiagnoBO();
            String xmldetails = bo.getPatientdetails(mrno);

            response.setContentType("text/xml");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            response.getWriter().write(xmldetails);
            response.flushBuffer();




        }catch(Exception exe){
            log.error("Exception occurred : "+ exe.getMessage());
        }

        return null;
    }


    @IgnoreConfidentialFilters
     public ActionForward gettestcharge(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws IOException{
            HttpSession session=null;
            try{
                session=request.getSession(false);
                Object username=session.getAttribute("userid");
                if(username==null){
                    return mapping.findForward("login");
                }

                String testid = request.getParameter("selectedtestid");
                String orgname = request.getParameter("selectedorgname");
                String priority = request.getParameter("selectedpriority");
                DiagnoBOIF bo = DiagnoFactory.getDiagnoBO();
                String xmldetails = bo.gettestcharge(testid,orgname,priority);

                response.setContentType("text/xml");
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
                response.getWriter().write(xmldetails);
                response.flushBuffer();

            }catch(Exception exe){
              log.error("Exception occurred : "+ exe.getMessage());
            }

           return null;
       }

     public ActionForward getprevtestdetails(ActionMapping mapping, ActionForm form,
    		 HttpServletRequest request, HttpServletResponse response)	throws IOException{
                try{
                    JSONSerializer js = new JSONSerializer();
                    String mrno = request.getParameter("mrno");
                    String module = request.getParameter("module");
                    DiagnoBOIF bo = DiagnoFactory.getDiagnoBO();

                    response.setContentType("application/x-json");
                    response.getWriter().print(
                            js.exclude("class").serialize(bo.getprevtestdetailsList(mrno,module)));
                }catch(Exception exe){
                  log.error("Exception occurred : "+ exe.getMessage());
                }

                return null;
            }



     public ActionForward getDuplicateReportScreen(ActionMapping mapping, ActionForm form,HttpServletRequest request,
             HttpServletResponse response)	throws IOException{
            return mapping.findForward("duplicateReportScreen");
        }

     public ActionForward getPatientReports(ActionMapping mapping, ActionForm form,HttpServletRequest request,
             HttpServletResponse response)	throws IOException, SQLException{

                 DiagnoBOIF bo = DiagnoFactory.getDiagnoBO();
                String mrno = request.getParameter("mrno");
                String patId = request.getParameter("patientId");
                ArrayList reportList = bo.getPatientReports(mrno, patId);
                String sReportList = new JSONSerializer().exclude("class").serialize(reportList);


                response.setContentType("text/plain");
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
                response.getWriter().write(sReportList);
                response.flushBuffer();

                return null;
        }


     public ActionForward getPatientReportsOn(ActionMapping mapping, ActionForm form,HttpServletRequest request,
             HttpServletResponse response)	throws SQLException{

         DiagnoBOIF bo = DiagnoFactory.getDiagnoBO();
                String mrno = request.getParameter("mrno");
                String patId = request.getParameter("patientId");
                ArrayList reportList = bo.getPatientReports(mrno, patId);
                request.setAttribute("reportList", reportList);
                request.setAttribute("mrno", mrno);
                request.setAttribute("patientId", patId);
                return mapping.findForward("duplicateReportScreen");
        }



     public ActionForward getReport(ActionMapping mapping, ActionForm form,HttpServletRequest request,
             HttpServletResponse response)	throws IOException{

                DiagnoForm dForm = (DiagnoForm)form;
                String mrno = request.getParameter("mrno");
                String visitId = request.getParameter("patientId");

                String[] presIds = dForm.getPrint();
                String presId ="";
                if(presIds!=null){
                    for(int i =0;i<presIds.length;i++){
                        if(presIds[i]!=null){
                            presId+=","+presIds[i];
                        }
                    }
                }
                request.setAttribute("mrno" ,mrno);
                request.setAttribute("patId",visitId);
                request.setAttribute("dept","");
                request.setAttribute("dashBoard", "duplicatReport");
                request.setAttribute("presId",presId.substring(1));
                request.setAttribute("method","getReportFormats");

                return mapping.findForward("PrintDuplicateReport");
        }


     public ActionForward getPatientVisits(ActionMapping mapping, ActionForm form,HttpServletRequest request,
             HttpServletResponse response) throws IOException, SQLException{
         DiagnoBOIF bo = DiagnoFactory.getDiagnoBO();
         String mrno = request.getParameter("mrno");

         String xmldetails = bo.getPatientIds(mrno);
         response.setContentType("text/xml");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.getWriter().write(xmldetails);
        response.flushBuffer();


         return null;
     }
}
