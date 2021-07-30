<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>



<script>
	var messageTypeJson = ${ifn:convertListToJson(messageTypeList)};
</script>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Coder Claim Review - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="coderClaimReview.js"/>
<insta:link type="js" file="moment.min.js"/>
<insta:link type="script" file="coderClaimReview.js"/>
<insta:link type="script" file="yui2.8.0r4/tabview/tabview-min.js"/>
<insta:link type="css" path="scripts/yui2.8.0r4/assets/skins/sam/tabview.css"/>
<insta:link type="css" path="css/coderClaimReviews.css"/>
<insta:js-bundle prefix="common"/>


	<style>

  		input:value{
  			height: 20px;
  			font-family: Arial;
  			font-size: 12px;
  			font-weight: normal;
  			font-style: normal;
  			font-stretch: normal;
  			line-height: 1.67;
  			letter-spacing: normal;
  			color: #414146;
  		}
 
 		.messageSubmitButton input{
 		    width: 77px;
			height: 18px;
			border-radius: 3px;
			border: solid 1px #d4d4dc;
			background-color: #ffffff;
			margin: 30px 18px 24px 13px
 		}
 		
  		
	</style>
	<script>
        var pagePath = '${pagePath}';
		var messageTypeJson = ${ifn:convertListToJson(messageTypeList)};
		var reviewTypeRolesJson = ${ifn:convertListToJson(reviewTypeRoles)};
		var usersJson = ${ifn:convertListToJson(users)};
		var ticketDetails = ${ifn:convertListToJson(ticketDetails)};
		var patientDetails = ${ifn:convertListToJson(patientDetails)};
		var duty_doctor_user_id = (patientDetails.length>0)?patientDetails[0].duty_doctor_user_id:null;
		var duty_doctor_role_id = (patientDetails.length>0)?patientDetails[0].duty_doctor_role_id:null;
		var selectedMessageType = "";
		var ticket_created_role_id = '${ticketDetails[0].created_role_id}';
		var role_id = '${roleId}';
		var codification_screen_access = '${urlRightsMap["update_mrd"]}';
		var codificationIsInActive = false;
	    var patientId = '${ifn:cleanHtmlAttribute(param.patient_id)}';
		YAHOO.util.Event.onContentReady("content", init);
	</script>
</head>
<body >
<h1>Add Review</h1>
<insta:feedback-panel/>
<c:set var="ticketDetailValues" value="${ticketDetails[0]}" />
<c:set var="consultationDetails" value="${consultationDetails[0]}" />
<c:set var="actionUrl" value="${cpath}/coderreviews/create.htm"/>
<c:choose>
	<c:when test="${not empty ifn:cleanHtmlAttribute(param.patient_id)}">
		<insta:patientdetails visitid="${ifn:cleanHtmlAttribute(param.patient_id)}" showClinicalInfo="true"/>
	</c:when>
</c:choose>
<form name="ticketForm" action="${actionUrl}" method="POST">
	<c:choose>
		
		<c:when test="${not empty ifn:cleanHtmlAttribute(param.patient_id)}">
		
		<fieldset class="fieldsetReview" ><legend class="fieldSetLabel">Review</legend>
			<table class="formtable" id="drg_codes">
			<tr>
				<td class="forminfo">
					<label id="">Review Type:</label>
					<input type="hidden" name="patient_id" value="${param.patient_id}" />
					<input type="hidden" name="review_type_id" id="review_type_id" value=""/>
					<input type="hidden" name="review_type_category" id="review_type_category" value=""/>
					<input type="hidden" name="loggedin_user_id" id="loggedin_user_id" value="${user_id}"/>
					
					<div id="msgTypeBlock">
						<input type="text" maxlength="200" name="messageType" id="msgTypeText"  />
						<div id="msgTypeDropDown" class="scrolForContainer"></div>
					</div>
				</td>
				<td class="forminfo">
					<label id="reason">Reason:</label>
					<div>
						<input name="title" maxlength="200" id="ticketTitle" value="${bean.title}" />
					</div>
				</td>
				<td class="forminfo">
					<label id="">Role:</label>
					<div>
						<insta:selectdb name="role_id" id="role_id" table="u_role"
							valuecol="role_id" displaycol="role_name" dummyvalue=""
							dummuvalueId="" filtercol="role_status" orderby="role_name" filtervalue="A"
							onchange="loadRecipients()"/>
					</div>
				</td>
				<td class="forminfo">
					<label id="recipientsLable">Recipient:</label>
					<div>
						<select class="dropdown" id="recipients" name="recipientsDropDown">
							<option value="">--Not Assigned--</option>
						</select>
					</div>
				</td>
				
			</tr>
	
			</table>
		</fieldset>
		<div class="activityBlock">
		</div>
		
		<div class="messageBodyBlock">
			<div class="messageBodyTextBox">
				<textarea name="body" id="ticketBody" onPaste="return restrictSpecialCharacter(event)" onKeyPress="return restrictSpecialCharacter(event)" placeholder="Add Review details here..." value=""></textarea> 
			</div>
			<div class="messageSubmitButton">
				<input type="submit" value="Send" class="saveStatusButton" name="updateButton"/>
			</div>
		</div>
		
		<div class="statusBlock">
			<label id="status">Status</label>
			<insta:selectoptions name="status" value="${bean.status}" opvalues="open" optexts="Open" />
			<div class="codificationStatusBlock">
				<label>Codification status:</label>
				<span class="codification_status_label">${patientDetails[0].codification_status_label}</span>
			</div>
		</div>
		
		<div class=" screenActions" >
		<c:url var="listUrl" value="${pagePath}list.htm?patient_id=${ifn:cleanHtmlAttribute(param.patient_id)}&sortOrder=assigned_to_role_name"></c:url>
		<c:url var="coderScreenUrl" value="/pages/medicalrecorddepartment/MRDUpdate.do?_method=getMRDUpdateScreen&patient_id=${ifn:cleanHtmlAttribute(param.patient_id)}"></c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${listUrl}' />">Coder Claim Reviews</a><label>|</label><a href="<c:out value='${coderScreenUrl}' />">Codification Screen</a></div>
		</div>
		</c:when>
	</c:choose>
	
</form>

</body>
</html>
