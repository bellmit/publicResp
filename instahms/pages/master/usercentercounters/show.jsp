<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.common.Encoder" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.BILLING_COUNTER_MAPPING_TO_CENTER %>"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>User Center Billing Counter Mapping - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="usercentercounters/counterMapping.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var countersList = ${ifn:convertListToJson(counterList)};
	</script>
</head>

<body onload="init();">

	<insta:feedback-panel/>

	<form onsubmit="return validateForm();" name="billingCounterCenterMappingForm" action="${cpath}${pagePath}/update.htm" method="POST" >

		<input type="hidden" name="userName" id="userName" value="${bean.emp_username}" /> 
		<input type="hidden" name="centerId" id="centerId" value="${bean.center_id}" />
		<input type="hidden" name="loggedInUserid" id="loggedInUserid"
                        value="<%=Encoder.cleanHtmlAttribute((String)session.getAttribute("userid"))%>" />

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">User Center Billing Counter Mapping Details</legend>

			<table class="formtable" cellpadding="0" cellspacing="0" width="100%"
				align="left">
				<tr>
					<td class="formlabel">User Name :</td>
					<td class="forminfo"><label>${bean.emp_username}</label></td>
					<td class="formlabel">User Center :</td>
					<td class="forminfo"><label> 
					<c:choose>
						<c:when test="${bean.center_id !=0 }">
							<c:forEach items="${centerList}" var="centerBean">
								<c:if test="${bean.center_id == centerBean.get('center_id')}">
									${centerBean.get('center_name')}
								</c:if>
							</c:forEach>
						</c:when>
						<c:otherwise>
							${defaultCenterBean[0].center_name}
						</c:otherwise>
					</c:choose>
					</label></td>
				</tr>
			</table>
		</fieldset>



		<fieldset class="fieldSetBorder" id="mappedCounterId"><legend class="fieldSetLabel"> Mapped Counters </legend>		
			<table class="detailList"  id="mappedBillingCounterId" name="mappedBillingCounterId" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<th>User Center Name</th>
					<th>Billing Counter Name</i></th>
				    <th >&nbsp;&nbsp;</th>  
				</tr>
				<c:forEach var="record" items="${mappedCounterList}" varStatus="st">
						<tr>
							<td>
								${record.center_name}
								<input type="hidden" name="center_counter_id" value="${record.center_counter_id}" />
								<input type="hidden" name="emp_username" value="${record.emp_username}" />
								<input type="hidden" name="center_id" value="${record.center_id}" />
								<input type="hidden" name="counter_id" value="${record.counter_id}" />
								<input type="hidden" name="default_counter" value="${record.default_counter}" />
								<input type="hidden" name="created_by" value="${record.created_by}" />
								<input type="hidden" name="deleted" id="${record.center_id}_deleted" value="false" />
								
							</td>
							<td>
								${record.counter_no}
							</td>
							<td style="text-align:right;">
								<a name="trashIcon" href="javascript:Cancel Item" onclick="return changeElsColor('${record.center_id}_deleted', this);" title="Cancel Billing Counter Mapping">
									<img src="${cpath}/icons/delete.gif" /></a>
							</td>
						</tr>
				</c:forEach>
				<tr style="display: none">
			</table>
			<table class="addButton">
					<tr style="text-align:right;">
						<td width="1000">Add New Counter Mapping</td>
						<td>
							<button type="button" name="addCounter" Class="imgButton" id="addCounter" onclick="addCounterMapping(this)" >
								<img name="addButton" src="${cpath}/icons/Add.png" />
							</button>
						</td>
					</tr>
			</table>						
		 </fieldset>   
				
		<div id="addCounterMappingDialog" style="display:block;visibility:hidden;">
			<div class="bd" id="bd3">
				<table class="formTable" align="center">
					<tr>
						<td>
							<fieldset class="fieldSetBorder" style="width:460px;"><legend class="fieldSetLabel">Add New Center Counter Mapping</legend>
									<br/>
									<table class="formTable" align="center">
										<tr>
											<td class="formlabel">Select Center:</td>
										 	 	<td class="forminfo">
										 	 		<select name="user_center" class="dropdown" id="user_center" onchange ="populateCounters(this)">
										 	 			<option value="" >Select</option>
										 	 			<c:choose>
										 	 				<c:when test="${bean.center_id !=0 }">
										 	 					<c:forEach items="${centerList}" var="centerBean">
										 	 						<c:if test="${centerBean.get('center_id') == bean.center_id}">
																		<option value="${centerBean.get('center_id')}">${centerBean.get('center_name')}</option>
																	</c:if>
																</c:forEach>
										 	 				</c:when>
										 	 				<c:otherwise>
										 	 					<c:forEach items="${centerList}" var="centerBean">
																	<option value="${centerBean.get('center_id')}">${centerBean.get('center_name')}</option>
																</c:forEach>
										 	 				</c:otherwise>
										 	 			</c:choose>
													</select>
										 	 	</td>
										</tr>
										<tr>
											<td class="formlabel">Select Counter:</td>
										 	 	<td class="forminfo">
										 	 		<select name="user_counter" class="dropdown" id="user_counter">
										 	 			<option value="" >Select</option>
													</select>
										 	 	</td>
										</tr>
									</table>
							 </fieldset>
						</td>
					</tr>
					<tr>
						<td align="left">
							<input type="button" id="ok_button" value="Add" onclick="addToTable();"/>
							<input type="button" id="cancel_button" value="Close" onclick="handleDialogCancel();"/>
						</td>
					</tr>
				</table>
			</div>
		</div>

		<div class="screenActions">
			<button type="button" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ubmit</button>
			|
			<c:set var="url2" value="${cpath}/pages/usermanager/UserAction.do?method=getUserScreen&userName=${bean.emp_username}"/>
			<a href="${url2}">Edit User</a>
			|
			<c:set var="url1" value="${cpath}/pages/usermanager/UserDashBoard.do?_method=list"/>
			<a href="${url1}">User DashBoard</a>
		</div>
	</form>
</body>
</html>

