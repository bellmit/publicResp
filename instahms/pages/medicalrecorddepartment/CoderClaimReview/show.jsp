<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Coder Claim Review - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="moment.min.js"/>
<insta:link type="script" file="coderClaimReview.js"/>
<insta:link type="script" file="diff.min.js"/>
<insta:link type="script" file="yui2.8.0r4/tabview/tabview-min.js"/>
<insta:link type="css" path="scripts/yui2.8.0r4/assets/skins/sam/tabview.css"/>
<insta:link type="css" path="css/coderClaimReviews.css"/>

	<style>
		
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
		var codificationIsInActive = ${ ((patientDetails[0].codification_status == 'V' or patientDetails[0].codification_status == 'R') or (bean.status == 'closed-resolved' or bean.status == 'closed-unresolved' ))?true:false };
	    var patientId = '${ifn:cleanHtmlAttribute(patientDetails[0].patient_id)}';
	</script>
</head>
<body onload="init();">
<h1>View Review</h1>
<insta:feedback-panel/>
<c:set var="ticketDetailValues" value="${ticketDetails}" />
<c:set var="disableFields" value="" />
<c:set var="visibilityHidden" value="block" />
<%--  review block is disabled, if review status is not open, and logged user does't have codification screen access  --%>
<c:if test="${ (not empty urlRightsMap['update_mrd'] and urlRightsMap['update_mrd'] != 'A' ) or ((patientDetails[0].codification_status == 'V' or patientDetails[0].codification_status == 'R') or (bean.status == 'closed-resolved' or bean.status == 'closed-unresolved' )) or commentsList[0].comments_count > 0 }">
	<c:set var="disableFields" value="disabled = 'disabled'" />
	<c:set var="visibilityHidden" value="none" />
</c:if>

<c:set var="actionUrl" value="${cpath}/coderreviews/update.htm?id=${bean.id}"/>
<c:choose>
	<c:when test="${not empty ifn:cleanHtmlAttribute(param.patient_id)}">
		<insta:patientdetails visitid="${ifn:cleanHtmlAttribute(param.patient_id)}" showClinicalInfo="true"/>
	</c:when>
</c:choose>
<form name="ticketForm" action="${actionUrl}" method="POST">
	<c:choose>
		
		<c:when test="${not empty ifn:cleanHtmlAttribute(bean.patient_id)}">
		
		<fieldset class="fieldsetReview" ><legend class="fieldSetLabel">Review</legend>
			<table class="formtable" id="drg_codes">
			<tr>
				<td class="forminfo">
					<label id="">Review Type:</label>
					<input type="hidden" name="patient_id" value="${bean.patient_id}" />
					<input type="hidden" name="ticket_id" value="${bean.id}" />
					<input type="hidden" name="review_type_id" id="review_type_id" value=""/>
					<input type="hidden" name="review_type_category" id="review_type_category" value=""/>
					<input type="hidden" name="loggedin_user_id" id="loggedin_user_id" value="${user_id}"/>
					<input type="hidden" name="review_type_changed" id="review_type_changed" value="false"/>
					
					<div id="msgTypeBlock">
						<input ${ disableFields } type="text" name="messageType" id="msgTypeText"  />
						<div id="msgTypeDropDown" class="scrolForContainer"></div>
					</div>
				</td>
				<td class="forminfo">
					<label id="reason">Reason:</label>
					<div>
						<input ${ disableFields } type="text" maxlength="200" name="title" id="ticketTitle" value="${bean.title}" />
					</div>
				</td>
				<td class="forminfo">
					<label id="">Role:</label>
					<div>
						<insta:selectdb name="role_id" id="role_id" class="disableRole" table="u_role"
							valuecol="role_id" displaycol="role_name" dummyvalue=""
							dummuvalueId="" filtercol="role_status" filtervalue="A"
							onchange="loadRecipients()" orderby="role_name"/>
					</div>
				</td>
				<td class="forminfo">
					<label id="recipientsLable">Recipient:</label>
					<div>
						<select data-recipient="${ticketDetailValues[0].assignedto}"  class="dropdown" id="recipients" name="recipientsDropDown">
							<option value="">--Not Assigned--</option>
						</select>
					</div>
				</td>
			</tr>
		</table>
		</fieldset>
		<div class="activityBlock">
			<div class="activityList">
				<div class="activityItemLeft">
					<div class="activityByBlock">
						${ ticketDetails[0].created_by_fullname }, <fmt:formatDate value="${ticketDetails[0].created_at}" pattern="hh:mm a dd-MM-yyyy" />
					</div>
					<div class="activityBodyBlock">
						${bean.body}
					</div>
				</div>
				<%-- Activity starts here --%>
				<c:if test="${ not empty activityMap }" >
					<c:set var="sameFieldset" value="true"/>
					<c:set var="prevChangeset" value="0"/>
					
					<c:forEach var="changeSet" items="${activityMap}" varStatus="status">
						<c:set var="changeSetLength" value="${fn:length(changeSet)}"></c:set>
						<c:set var="activityTimeFlag" value="false" />
						<c:if test="${changeSetLength == 1 and changeSet[0].activity != 'COMMENT' }">
							<div class="activityBlockCenter">
						    	<div class="activityBodyBlockCenter">
					    </c:if>
						<c:if test="${changeSetLength>1}">
							<div class="activityBlockCenter">
						    	<div class="activityBodyBlockCenter">
						</c:if>
						<c:forEach var="activityItem" items="${changeSet}" varStatus="activityStatus">
							<c:choose>
								<c:when test="${ activityItem.activity == 'COMMENT' }">
									
									<div class="<c:choose><c:when test='${ activityItem.activtiy_role_id <= 2 }'>activityItemLeft</c:when><c:when test='${activityItem.codification_screen_right_status != "A" }' >activityItemRight</c:when><c:otherwise>activityItemLeft</c:otherwise></c:choose>">
										<div class="<c:choose><c:when test='${ activityItem.activtiy_role_id <= 2  }'>activityByBlock</c:when><c:when test='${activityItem.codification_screen_right_status != "A" }' >activityByBlockRight</c:when><c:otherwise>activityByBlock</c:otherwise></c:choose>">
											${activityItem.user_fullname}, <fmt:formatDate value="${activityItem.change_at}" pattern="hh:mm a dd-MM-yyyy" />
										</div>						
										<div class="<c:choose><c:when test='${ activityItem.activtiy_role_id <= 2}'>activityBodyBlock</c:when><c:when test='${activityItem.codification_screen_right_status != "A" }'>activityBodyBlockRight</c:when><c:otherwise>activityBodyBlock</c:otherwise></c:choose>" >
											${activityItem.new_value}
										</div>
									</div>
								</c:when>
								<c:when test="${activityItem.activity == 'UPDATE_STATUS' }">
									<c:set var="activityTimeFlag" value="true" />
									<span class="activityBodyCenterContent">Status changed <span class="activityCapitalized">${activityItem.old_value}</span> to <span class="activityCapitalized">${activityItem.new_value}</span> by <span class="activityCapitalized">${activityItem.user_fullname}</span></span>
								</c:when>
								<c:when test="${activityItem.activity == 'UPDATE_ASSIGNEE' }">
								    <c:set var="activityTimeFlag" value="true" />
									<c:choose>
									    <c:when test="${ !empty activityItem.old_value and !empty activityItem.new_value }">
									        <span class="activityBodyCenterContent">Recipient changed <span class="activityCapitalized">${activityItem.old_value}</span> to <span class="activityCapitalized">${activityItem.new_value}</span> by <span class="activityCapitalized">${activityItem.user_fullname}</span></span>
										</c:when>
									    <c:when test="${ empty activityItem.new_value }">
									        <span class="activityBodyCenterContent">Recipient changed <span class="activityCapitalized">${activityItem.old_value}</span> to Unassigned by <span class="activityCapitalized">${activityItem.user_fullname}</span></span>
									    </c:when>
									    <c:otherwise>
									        <span class="activityBodyCenterContent">Recipient assigned to <span class="activityCapitalized">${activityItem.new_value}</span> by <span class="activityCapitalized">${activityItem.user_fullname}</span></span>
									    </c:otherwise>
								    </c:choose>
								</c:when>
								<c:when test="${activityItem.activity == 'UPDATE_ROLE' }">
									<c:set var="activityTimeFlag" value="true" />
									<span class="activityBodyCenterContent">Role changed <span class="activityCapitalized">${activityItem.old_value}</span> to <span class="activityCapitalized">${activityItem.new_value}</span> by <span class="activityCapitalized">${activityItem.user_fullname}</span></span>
								</c:when>
								<c:when test="${activityItem.activity == 'UPDATE_TITLE' }">
									<c:set var="activityTimeFlag" value="true" />
									<span class="activityBodyCenterContent">
										<span class="diff">
											Reason changed <span class="old-value">${activityItem.old_value}</span> to <span class="new-value">${activityItem.new_value}</span> by <span class="activityCapitalized">${activityItem.user_fullname}</span>
										</span>
									</span>
								</c:when>
								<c:when test="${activityItem.activity == 'UPDATE_MESSAGE_TYPE' }">
									<c:set var="activityTimeFlag" value="true" />
									<span class="activityBodyCenterContent">
										<span class="diff">
											Review type changed <span class="old-value">${activityItem.old_value}</span> to <span class="new-value">${activityItem.new_value}</span> by <span class="activityCapitalized">${activityItem.user_fullname}</span>
										</span>
									</span>												
								</c:when>
							</c:choose>
						</c:forEach>
						<c:if test="${changeSetLength>1}">
							</div>						
							<c:if test="${activityTimeFlag ==true}">
								<div class="activityBlockCenterDateTime">
									<fmt:formatDate value="${changeSet[0].change_at}" pattern="hh:mm a dd-MM-yyyy" />
								</div>
							</c:if>				
							</div>
						</c:if>
						<c:if test="${changeSetLength == 1 and changeSet[0].activity != 'COMMENT' }">
							</div>
							<c:if test="${activityTimeFlag ==true}">
								<div class="activityBlockCenterDateTime">
									<fmt:formatDate value="${changeSet[0].change_at}" pattern="hh:mm a dd-MM-yyyy" />
								</div>
							</c:if>		
							</div>
					    </c:if>
					</c:forEach>
				</c:if>
				
			</div>
		</div>
		<c:set var="showCodificationPage" value="true" />
		<c:if test="${ bean.status == 'open' or bean.status == 'inprogress' }" >
			<div class="messageBodyBlock">
				<div class="messageBodyTextBox">
					<textarea name="commentText" id="commentTextBox" onPaste="return restrictSpecialCharacter(event)" onKeyPress="return restrictSpecialCharacter(event)" placeholder="Add your comment here..." value=""></textarea> 
				</div>
				<div class="messageSubmitButton">
					<input type="submit" onclick="return checkCommentEmpty();" value="Add Comment"  class="saveStatusButton" name="commentSubmit"/>
				</div>
			</div>
		</c:if>
		<div class="statusBlock">
			<label id="status">Review status:</label>
			<c:choose>
			<%-- Either admin/superadmin can edit OR User must have codification screen access and review status must be open/inprogress --%>
				<c:when test="${ (patientDetails[0].codification_status == 'P' or patientDetails[0].codification_status == 'C') and (not empty urlRightsMap.update_mrd and 'A' == urlRightsMap.update_mrd) }">
					<insta:selectoptions id="status"  name="status" value="${bean.status}" opvalues="open,inprogress,closed-resolved,closed-unresolved" optexts="Open,Inprogress,Closed-resolved,Closed-unresolved" />
				</c:when>
				<c:when test="${ loggedInUser[0].role_id <= 2  and (patientDetails[0].codification_status == 'R' or patientDetails[0].codification_status == 'V' )}">
					<select name="status" id="status" >
					  <c:if test="${bean.status == 'open' or bean.status == 'inprogress'}">
					    <option value="${bean.status}">${bean.status}</option>
				    </c:if>
				    <option <c:if test="${bean.status == 'closed-resolved' }">selected="selected"</c:if> value="closed-resolved">Closed-resolved</option>
						<option <c:if test="${bean.status == 'closed-unresolved'}">selected="selected"</c:if> value="closed-unresolved">Closed-unresolved</option>
					</select>
				</c:when>
				<c:otherwise>
                	<c:set var="showCodificationPage" value="false" />
				    <span class="codification_status_label">${bean.status}</span>
				</c:otherwise>
			</c:choose>
			<div class="codificationStatusBlock">
				<label>Codification status:</label>
				<span class="codification_status_label">${patientDetails[0].codification_status_label}</span>
			</div>
		</div>
		<div class=" screenActions" >
			<c:url var="listUrl" value="${pagePath}list.htm?patient_id=${bean.patient_id}&status=open&status=inprogress&sortOrder=assigned_to_role_name&mr_no=${patientDetails[0].mr_no}"></c:url>
			<c:url var="coderScreenUrl" value="/pages/medicalrecorddepartment/MRDUpdate.do?_method=getMRDUpdateScreen&patient_id=${bean.patient_id}&mr_no=${patientDetails[0].mr_no}"></c:url>
			<div class="screenActions" style="float:left">
				<input type="submit" value="Save"  name="updateButton"/>|
				<c:if test="${  not empty urlRightsMap.update_mrd and urlRightsMap.update_mrd == 'A' &&  not empty patientDetails[0].codification_status  && ( patientDetails[0].codification_status == 'P' || patientDetails[0].codification_status == 'C' )}" >
				  <c:url var="url" value="${pagePath}add.htm?patient_id=${ifn:cleanHtmlAttribute(patientDetails[0].patient_id)}&mr_no=${ifn:cleanHtmlAttribute(patientDetails[0].mr_no)}"></c:url>
				  <c:if test="${ifn:cleanHtmlAttribute(patientDetails[0].patient_id) ne null and ifn:cleanHtmlAttribute(patientDetails[0].patient_id) ne '' }">
				    <a href="<c:out value='${url}' />">Add Review</a> <label>|</label>
				  </c:if>
			    </c:if>
				<c:if test="${ showCodificationPage == true }"><a href="<c:out value='${coderScreenUrl}' />">Codification Screen </a>|</c:if>
				<a href="<c:out value='${listUrl}' />">Coder Claim Reviews</a>
				<c:if test="${ (genricPrefs[0].max_centers_inc_default > 1 and centerId > 0) or (genricPrefs[0].max_centers_inc_default == 1) }" >
					<c:if test="${ not empty urlRightsMap['op_prescribe'] and 'A' == urlRightsMap['op_prescribe'] and patientDetails[0].consultation_id > 0  }"  >
						<label>|</label><c:url var="consultationUrl" value="/coderreviews/openReopenConsultation.htm?_method=openReopenConsultation&consultation_id=${patientDetails[0].consultation_id}&patient_id=${patientDetails[0].patient_id}&doctor_id=${patientDetails[0].doctor_id}&mr_no=${patientDetails[0].mr_no}&ticket_id=${bean.id}" ></c:url>
	 						<a href="${consultationUrl}" target="_sametab">Consultation and Management</a>
					</c:if>
				</c:if>
			</div>
		</div>
		</c:when>
	</c:choose>
</form>


</body>
</html>
