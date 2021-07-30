<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="dummyValue">
<insta:ltext key="insurance.patientapprovallist.patientapprovals.dummyvalue"/>
</c:set>
<html>
	<head>
		<title><insta:ltext key="insurance.patientapprovallist.patientapprovals.title"/></title>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<insta:link type="script" file="Insurance/sponsorapproval.js" />
		<insta:link type="script" file="hmsvalidation.js" /> 			
	</head>
	<body>	
		<c:choose>
			<c:when test="${param._method !='add'}">
				<table width="100%">
					<tr>
						<td width="100%"><h1>Edit Patient Approvals</h1></td>						
					</tr>
				</table>				
		    </c:when>
		    <c:otherwise>
		    	<h1 style="float: left">Add Patient Approvals</h1>
				<insta:patientsearch searchType="mrNo" searchUrl="PatientSponsorsApproval.do"  buttonLabel="Find" 
					 searchMethod="find" fieldName="mr_no"/>   	     
		    </c:otherwise>
	    </c:choose>	
	  	<insta:feedback-panel/>
	    <insta:patientgeneraldetails mrno="${not empty param.mr_no ? param.mr_no : bean.map.mr_no}" />
		<form name="PatientApprovalForm" action="PatientSponsorsApproval.do" method="POST">
			<input type="hidden" name="_method" id="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>		
			<input type="hidden" name="mr_no" id= "mr_no" value="${not empty param.mr_no ? param.mr_no : bean.map.mr_no}"/>
			<input type="hidden" name="sponsor_approval_id" id= "sponsor_approval_id" value="${ifn:cleanHtmlAttribute(param.sponsor_approval_id)}"/>		
			<fieldset class="fieldSetBorder">			
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td class="formlabel" >Primary Center:</td>						
						<td class="forminfo">
							<insta:selectdb name="primary_center_id" id="primary_center_id" table="hospital_center_master" valuecol="center_id" displaycol="center_name"
								value="${bean.map.primary_center_id}" orderby="center_name" dummyvalue="${dummyValue}"/>							
							<span class="star">*</span>
						</td>												
						<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.sponsor"/>:</td>
						<td class="forminfo">
							<insta:selectdb name="sponsor_id" id="sponsor_id" table="tpa_master" valuecol="tpa_id" displaycol="tpa_name" 
								value="${bean.map.sponsor_id}" orderby="tpa_name" dummyvalue="${dummyValue}"/>							
							<span class="star">*</span>
						</td>
						<td class="formlabel" ><insta:ltext key="insurance.patientapprovallist.patientapprovals.approvalno"/>:</td>
						<td class="forminfo">
							<input type="text" name="approval_no" id="approval_no" value="${bean.map.approval_no}"/>
							<span class="star">*</span>
						</td>																																	
					</tr>
					<tr>
						<td class="formlabel" ><insta:ltext key="insurance.patientapprovallist.patientapprovals.rateplan"/>:</td>						
						<td class="forminfo">
							<insta:selectdb name="org_id" id="org_id" table="organization_details" valuecol="org_id" displaycol="org_name"
								value="${bean.map.org_id}" orderby="org_name" dummyvalue="${dummyValue}"/>							
							<span class="star">*</span>
						</td>
						<td class="formlabel" ><insta:ltext key="insurance.patientapprovallist.patientapprovals.priority"/>:</td>
						<td class="forminfo">
							<input type="text" name="priority" id="priority" value="${bean.map.priority}" onkeypress="return enterNumOnlyzeroToNine(event);"/>
							<span class="star">*</span>
						</td>
						<td class="formlabel">Approval Document:</td>
						<td>
		            		<input type="file" name="doc_id"  accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>   
					  	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.validityfrom"/>:</td>
						<fmt:parseDate value="${bean.map.validity_start}" pattern="yyyy-MM-dd" var="dt"/>
						<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="frm"/>
						<td class="forminfo">
							<insta:datewidget name="validity_start" id="validity_start" value="${frm}"/>										
							<span class="star">*</span>
						</td>
						<td class="formlabel"><insta:ltext key="insurance.patientapprovallist.patientapprovals.validityto"/>:</td>
						<fmt:parseDate value="${bean.map.validity_end}" pattern="yyyy-MM-dd" var="dt"/>
						<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="to"/>
						<td class="forminfo">
							<insta:datewidget name="validity_end" id="validity_end" value="${to}"/>										
							<span class="star">*</span>
						</td>
 					</tr>
				</table>				
			</fieldset>			
			<div class="screenActions" style="float: left">
				<button type="button" accesskey="S" onclick="return patientApprovalFormValidate();"><b><u>S</u></b>ave</button>
				<c:if test="${param._method != 'add'}">
					<a href="${cpath}/Insurance/PatientSponsorsApproval.do?_method=add">|&nbsp; Add &nbsp;</a>
				</c:if>
				<insta:screenlink screenId="patient_sponsors_approval" extraParam="?_method=list"
					label="Patient Approvals List" addPipe="true"/>
			</div>
		</form> 		
	</body>
</html>