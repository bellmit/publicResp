<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.CRON_JOB_PATH %>"/>

<html>
<head>
  <insta:js-bundle prefix="scheduler.cron"/>
  <title>
    <insta:ltext key="scheduler.cron.schedulercronjob"></insta:ltext>
  </title>
  <insta:link type="css" file="widgets.css" />
  <insta:link type="css" file="panel.css"></insta:link>
</head>

<body onload="populateData(getRowIndexFromURL(window.location.href));
    highlightRow(getRowIndexFromURL(window.location.href))">
  <h1>
    <insta:ltext key="js.topnav.menu.hospital.preferences.cronjob" />
  </h1>
  <insta:feedback-panel />
  
  <div class="resultList" style='box-sizing: content-box'>
    <div class="left-pannel-main">
      <div class="left-pannel-header">
        <h2> 
          <insta:ltext key="scheduler.cron.scheduledjobslist"></insta:ltext>
        </h2>
      </div>
      <div class="left-pannel">
        <table  class="resultList dialog_displayColumns" id="resultTable" onmouseover="hideToolBar('');">
          <tr onmouseover="hideToolBar();">
            <th>#</th>
            <th><insta:ltext key="scheduler.cron.jobname"></insta:ltext> </th>
            <th><insta:ltext key="scheduler.cron.jobstatus"></insta:ltext> </th>
          </tr>
          <c:forEach var="job" items="${scheduledJobList}" varStatus="st">
            <tr id="toolBarRow${st.index}" class="toolBarRow" onclick="populateData(${st.index});highlightRow(${st.index})">
              <td>${st.index +1}</td>
              <td>${job.job_name}</td>
              <td>${job.job_status }</td>
  
              <input type="hidden" name="job_id${st.index}" id="job_id${st.index}" value='${job.job_id }' />
              <input type="hidden" name="job_name${st.index}" id="job_name${st.index}" value='${job.job_name }' />
              <input type="hidden" name="job_group${st.index}" id="job_group${st.index}" value='${job.job_group }' />
              <input type="hidden" name="job_status${st.index}" id="job_status${st.index}" value='${job.job_status }' />
              <input type="hidden" name="job_time${st.index}" id="job_time${st.index}" value='${job.job_time }' />
              <input type="hidden" name="job_params${st.index}" id="job_params${st.index}" value='${job.job_params }' />
              <input type="hidden" name="job_mod_dependency${st.index}" id="job_mod_dependency${st.index}" value='${job.job_mod_dependency }' />
              <input type="hidden" name="job_allow_disable${st.index}" id="job_allow_disable${st.index}" value='${job.job_allow_disable }' />
              <input type="hidden" name="job_next_runtime${st.index}" id="job_next_runtime${st.index}" value='${job.job_next_runtime }' />
              <input type="hidden" name="job_last_runtime${st.index}" id="job_last_runtime${st.index}" value='${job.job_last_runtime }' />
              <input type="hidden" name="job_last_status${st.index}" id="job_last_status${st.index}" value='${job.job_last_status }' />
              <input type="hidden" name="job_description${st.index}" id="job_description${st.index}" value='${job.job_description }' />
            </tr>
          </c:forEach>
        </table>
      </div>
      <div class="left-pannel-btn">
          <button ><a href="${cpath}${pagePath}/reschedule.htm?job_index=0" style="color: black;">Re-Schedule All Jobs</a></button>
      </div>   
    </div>
    <div class="right-pannel">
      <div class="right-pannel-header">
        <form name="mainform" method="POST" action="${cpath}${pagePath }/update.htm">
          <input type="hidden" name="job_index" id="job-index" value="" />
          <div class="card-header">
            <h1><insta:ltext key="scheduler.cron.details"></insta:ltext></h1>
          </div>
          
          <div class="card-details">
            <table>
              <tr>
                <td>
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobname"></insta:ltext> : 
                    <input id="job-name" class="input-style" name="job_name" value ="" readonly/>
                    <input type="hidden" id="job-id" name="job_id" value =""/>
                  </div>
                </td>
                <td>
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobgroup"></insta:ltext> : 
                    <input id="job-group" class="input-style" name="job_group" value ="" readonly/>
                  </div>
                </td>
                <td>
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobeditable"></insta:ltext> :
                    <select  id="job-allow-disable" class="input-style" name="job_allow_disable" disabled>
                      <option value="N">System Cron</option>
                      <option value="Y">User Cron</option>
                    </select> 
                  </div>
                </td>
              </tr>
              
              <tr>
                <td class="menu-options">
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobpreviousstatus"></insta:ltext> : 
                    <input id="job-pre-status" class="input-style"  name="job_last_status" readonly/>
                  </div>
                </td>
                <td class="menu-options">
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.joblastruntime"></insta:ltext> : 
                    <input id="job-last-run-time" class="input-style" name="job_last_runtime_tm" readonly/>
                  </div>
                </td>
                <td class="menu-options">
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobnextruntime"></insta:ltext> : 
                    <input id="job-next-run-time" class="input-style" name="job_next_runtime_tm" readonly/>
                  </div>
                </td>
              </tr>
              
              <tr>
                <td class="menu-options">
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobparams"></insta:ltext> : 
                    <input type="text" id="job-params" class="input-style" name="job_params" onChange="updateJobParams()"/>
                  </div>
                </td>
                
                <td class="menu-options">
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobcurrentstatus"></insta:ltext> :
                    <select onChange="updateJobStatus()" id="job-status" name="job_status" class="input-style">
                      <option value="A">Active</option>
                      <option value="I">Inactive</option>
                    </select>
                  </div>
                </td>
                
                <td class="menu-options">
                  <div class="card-details-element truncate positioning">
                    <insta:ltext key="scheduler.cron.jobtime"></insta:ltext> : 
                    <input type="text" id="job-time" class="input-style" name="job_time" onChange="updateJobTime()"/>
                    <img class="imgHelpText" src="${cpath}/images/help.png" 
                        title="Schedule Pattern : Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (optional field).Month can set like JAN, FEB, MAR... Day-of-Week can set like SUN, MON, TUE .." />
                  </div>
                </td>
              </tr>
              <tr>
                <td class="menu-options" colspan="3">
                  <div class="card-details-element truncate positioning" style="width:500px" >
                    <insta:ltext key="scheduler.cron.jobdescription"></insta:ltext> : 
                    <textarea id="job-description" class="input-style" name="job_description" style="width:100%"></textarea>         
                  </div>
                </td>
                
              </tr>
            </table>
          </div>
          <div>
            <button class="update-button">Update</button>
          </div>
        </form>
      </div>
    </div>
  </div>
  <insta:link type="js" file="schedulerCron.js" />
</body>
</html>
