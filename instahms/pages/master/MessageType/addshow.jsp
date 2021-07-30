<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Message Type - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<script>

	var dispatcherListJSON = ${(not empty dispatcherListJSON) ? dispatcherListJSON : '[]'} ;

	function modeChange() {

		var mode = document.getElementById("message_mode").value;

		var actionTable = "NOTIFICATION";
		if(document.getElementById("notification")!=null){
   		if (mode == actionTable) {
   		   document.getElementById("notification").style.display="";
  		 	} else {
  	    	document.getElementById("notification").style.display="none";
    	}
		}

		var dispatcher = findInList(dispatcherListJSON, "message_mode", mode);
		var disableAttachments = (dispatcher.attachment_allowed == 'N');

		document.getElementById("attachment").disabled = disableAttachments;
		var attachments = getElementsByName(document.forms[0], "attached_files");
		for (var i = 0; i < attachments.length; i++) {
			attachments[i].disabled = disableAttachments;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/messages/MessageType.do?_method=list";
	}

	function validateForm() {
		var mode = document.getElementById("message_mode").value;
		var subjectEl = document.getElementById("message_subject");
		if (mode == 'EMAIL') {
			if (!validateRequired(subjectEl, "Please enter a subject for the message")) {
				return false;
			}
		}
		var messageEl = document.getElementById("message_body");
		if (!validateRequired(messageEl, "Please enter the message")) {
			return false;
		}
	}

	function setActionID(idx, actionId) {
		var msgActionCheckbox = document.getElementById("message_Action_Available"+idx);
	  	if(msgActionCheckbox.checked)
			document.getElementById("msgActionId"+idx).value = actionId;
		else
			document.getElementById("msgActionId"+idx).value = 0;
	}

	function setOverride(idx, actionId) {
		var msgOverrideCheckbox = document.getElementById("message_Action_Override"+idx);
		if(msgOverrideCheckbox.checked)
			document.getElementById("actionoverride"+idx).value = actionId;
		else
			document.getElementById("actionoverride"+idx).value = 0;
	}

</script>

</head>
<body>
<!--  variable for practo activation status -->
<c:set var="practoSmsMod" value="${practoSmsModule.map.activation_status}"/>
<c:if test="${empty practoSmsMod}"> <c:set var="practoSmsMod" value="N" /> </c:if>

<c:set var="modeEditability" value="none" />
<c:set var="customMessage" value="false"/>
<c:forEach items="${customMessageTypes}" var="msgCat">
<!--xx ${msgCat.map.message_category_id} ${bean.map.category_id} -->
	<c:if test="${msgCat.map.message_category_id == bean.map.category_id}" >
		<c:set var="customMessage" value="true"/>
	</c:if>
</c:forEach>

<c:if test="${param._method == 'add' || customMessage == 'true'}">
	<c:set var="modeEditability" value="auto" />
</c:if>

<c:choose>
  <c:when test="${modeEditability != 'auto' && (bean.map.recipient_category == 'Practo' || bean.map.recipient_category == 'Patient')}">
	  <c:set var="modeEditability" value="none" />
  </c:when>
  <c:otherwise>
    <c:set var="modeEditability" value="auto" />
  </c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${bean.map.message_mode == 'NOTIFICATION'}"> 
		<c:if test="${practoSmsMod=='Y' && roleId != 1}">
			<c:set var="textboxEditability" value="" />
			<c:set var="dropdownEditability" value="auto" />
			<c:set var="buttonDisplay" value="" />
			<c:set var="addbuttonDisplay" value="style='display: none'" />
			<c:set var="backgroundColor" value="" />
			<c:set var="attachmentDisabled" value="" />	
			</c:if>
	</c:when>
	<c:otherwise>
			<c:choose>			
				<c:when test="${bean.map.editability == 'N'}">
					<c:set var="textboxEditability" value="readonly" />
					<c:set var="dropdownEditability" value="none" />
					<c:set var="buttonDisplay" value="style='display: none'" />
					<c:set var="addbuttonDisplay" value="style='display: none'" />
					<c:set var="backgroundColor" value="background-color:#EFEBE7;" />
					<c:set var="attachmentDisabled" value="disabled" />
				</c:when>
				<c:when test="${bean.map.editability == 'I'}">
					<c:set var="textboxEditability" value="readonly" />
					<c:set var="dropdownEditability" value="none" />
					<c:set var="buttonDisplay" value="style='display: none'" />
					<c:set var="addbuttonDisplay" value="style='display: none'" />
					<c:set var="backgroundColor" value="background-color:#EFEBE7;" />
					<c:set var="attachmentDisabled" value="disabled" />
				</c:when>
				<c:otherwise>
					<c:set var="textboxEditability" value="" />
					<c:set var="dropdownEditability" value="auto" />
					<c:set var="buttonDisplay" value="" />
					<c:set var="addbuttonDisplay" value="" />
					<c:set var="backgroundColor" value="" />
					<c:set var="attachmentDisabled" value="" />
				</c:otherwise>
			</c:choose>
	</c:otherwise>
</c:choose>
<c:if test="${bean.map.message_mode == 'EMAIL' && practoSmsMod=='Y' && roleId != 1}"> 
	<c:set var="addbuttonDisplay" value="style='display: none'" />
</c:if>
<form action="MessageType.do" method="POST" enctype="multipart/form-data">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="message_type_id" value="${bean.map.message_type_id}"/>
	</c:if>

	<div class="pageHeader">${param._method == 'add' ? 'Add' : 'Edit'} Message Template</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Message Type:</td>
				<td>
					<input type="text" name="message_type_name" ${textboxEditability}
						value="${bean.map.message_type_name}" class="required validate-length" length="100"
						title="Name is required and max length of name can be 100"
						onblur="capWords(message_type_name);"/><span class="star">*</span>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Message Description: </td>
				<td colspan=2 >
					<textarea type="text" name="message_type_description" ${textboxEditability} maxlength ="250" 
						style="width:350pt;height:36pt;${backgroundColor}" 
						title="Description of the message">${bean.map.message_type_description}</textarea>
				</td>
				<td>
						<img class="imgHelpText" title="This field defines what is this message for, when is it sent and how" src="${cpath}/images/help.png"/></td>						
				</td>
			</tr>
			<tr>
				<td class="formlabel">Mode: </td>
					<td>
						<insta:selectdb name="message_mode" table="message_dispatcher_config" id="message_mode" 
							value="${bean.map.message_mode}" style="pointer-events: ${modeEditability}"
							valuecol="message_mode" displaycol="display_name" filtered="false" onchange="modeChange();"/>
					</td>
					
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<c:choose>
					<c:when test="${bean.map.editability == 'N' && roleId == 1}">
					    <td><insta:selectoptions name="status" style="pointer-events: auto" value="${bean.map.status}"
					    opvalues="A,I" optexts="Active,Inactive" /></td>
					</c:when>
					<c:otherwise>
					    <td><insta:selectoptions name="status" style="pointer-events: ${dropdownEditability}" value="${bean.map.status}"
					    opvalues="A,I" optexts="Active,Inactive" /></td>
					</c:otherwise>
				</c:choose>
			</tr>
			<tr>
				<td class="formlabel">Category:</td>
				<td>
					<c:choose>
						<c:when test="${param._method == 'add' || customMessage == 'true'}">
						<select name="category_id" id="category_id" class="dropdown" >
							<c:forEach items="${customMessageTypes}" var="msg">
								<option value="${msg.map.message_category_id }" ${msg.map.message_category_name } <c:if test="${bean.map.category_id == msg.map.message_category_id}">selected='true'</c:if>>
									${msg.map.message_category_name}
								</option>
							</c:forEach>
						</select>
							</c:when>
						<c:otherwise>
						  <insta:selectdb  name="category_id" value="${bean.map.category_id}" style="pointer-events: none" table="message_category"
                   valuecol="message_category_id" displaycol="message_category_name" dummyvalue="---Select---"/>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<c:if test="${param._method == 'add' || customMessage == 'true'}">
				<tr>
				<td class="formlabel">Recipient Category:</td>
					<td>
						<insta:selectoptions name="recipient_category" value="${bean.map.recipient_category}" id="recipient_category" opvalues="Patient,Others" optexts="Patient,Others" />
					</td>
				</tr>
			</c:if>
			
			<tr>
			<!-- ${(editable == false ) ? readonly :readonly} -->
				<td class="formlabel">From:</td>
				<td><input type="text" name="message_sender" value="${bean.map.message_sender}" ${textboxEditability} style="width:160pt;"/></td>
				<td><img class="imgHelpText" title="If you leave this field blank, message will be sent from the address configured in message preferences" src="${cpath}/images/help.png"/></td>
			</tr>
			<tr>
				<td class="formlabel">To:</td>
				<td><input type="text" name="message_to" value="${bean.map.message_to}" ${textboxEditability} style="width:160pt;"/></td>
				<td><img class="imgHelpText" title="Anything you enter here will be added to the list of recipients selected at the time of sending the message." src="${cpath}/images/help.png"/></td>
			</tr>
			<tr>
				<td class="formlabel">Cc:</td>
				<td><input type="text" name="message_cc" value="${bean.map.message_cc}" ${textboxEditability} style="width:160pt;"/></td>
			</tr>
			<tr>
				<td class="formlabel">Bcc:</td>
				<td><input type="text" name="message_bcc" value="${bean.map.message_bcc}" ${textboxEditability} style="width:160pt;"/></td>
			</tr>
			<tr>
				<td class="formlabel">Subject:</td>
				<td><input type="text" name="message_subject" id="message_subject" value='<c:out value="${bean.map.message_subject}" />' ${textboxEditability} style="width:160pt;"/></td>
				<td><img class="imgHelpText" title="Any valid token used in this field, will be automatically substituted with appropriate values, when the message is sent. Any token not in the 'Available Token List', will be sent as it is." src="${cpath}/images/help.png"/></td>
			</tr>
			<tr>
				<td class="formlabel">Message Body:</td>
				<td colspan="5">
					<textarea id="message_body" name="message_body" ${textboxEditability}
					style="width: 350pt; height: 180pt; ${backgroundColor}"><c:out value="${bean.map.message_body}"/></textarea>
				</td>
			</tr>
			<c:if test="${not empty attachmentList}">
				<c:forEach var="attachment" items="${attachmentList}" varStatus="status">
					<tr>
					<c:choose>
						<c:when test="${status.index == 0}">
							<td class="formlabel">Attachments:</td>
						</c:when>
						<c:otherwise>
							<td class="formlabel"></td>
						</c:otherwise>
					</c:choose>
					<td colspan="4" >
					<input type="checkbox" name="attached_files" value="${attachment.map.attachment_id}" checked/><a href="#">${attachment.map.attachment_name}</a>
					</td>
					</tr>
				</c:forEach>
			</c:if>
 			<tr>
			<c:choose>
				<c:when test="${not empty attachmentList}">
					<td class="formlabel"></td>
					<td colspan="4">Add another attachment: <input type="file" ${attachmentDisabled} name="attachment" id="attachment" tabindex="150"   accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>
					</td>
				</c:when>
				<c:otherwise>
					<td class="formlabel">Attachments:</td>
					<td colspan="5"><input type="file" name="attachment" ${attachmentDisabled} id="attachment" tabindex="150" accept="<insta:ltext key="upload.accept.medical_image"/>,<insta:ltext key="upload.accept.document"/>"/>
					</td>
				</c:otherwise>
			</c:choose>
			</tr>
		</table>

	<c:set var="notification" value="${bean.map.message_mode}"/>
	<c:set var="notificationValue" value="NOTIFICATION"/>
	<c:if test="${notification eq notificationValue}">
			</br>
			<div class="resultList">
			<table id="notification" class="resultList"  align="left" cellspacing="0" cellpadding="0" border="0" style="width: 450px;">
				<tr bgcolor="#8FBC8F">
					<th style="width: 50px" colspan="4">Action</th>
					<th style="width: 10px" colspan="4">Available</th>
					<th style="width: 10px;display:none" colspan="4">Sender Can Override</th>
				</tr>
				<c:set var="idx" value="0"/>
				<c:forEach var="action" items="${msgActionLists}" varStatus="pstatus">
				 <tr>
					<td colspan="4">
						${action.map.message_action_name}
					</td>
					<td colspan="4">
					<c:choose>
						<c:when test="${msgTypeAction[idx] eq 49}">
							<input type="checkbox"
						     	   name="message_Action_Available"
			    			 	   id="message_Action_Available${idx}"
        						   value="${action.map.message_action_id}" checked
        						   onchange="setActionID(${idx},${action.map.message_action_id});" /> Available
							<input type="hidden" name="msgActionId" id = "msgActionId${idx}"
	                    	       value="${action.map.message_action_id}"/>
						</c:when>
						<c:otherwise>
							<input type="checkbox"
							   	   name="message_Action_Available"
								   id="message_Action_Available${idx}"
		                	       value="${action.map.message_action_id}"
								   onchange="setActionID(${idx},${action.map.message_action_id});" /> Available
							<input type="hidden" name="msgActionId" id = "msgActionId${idx}"
							       value="0"/>
						</c:otherwise>
					</c:choose>
					</td>
					<td colspan="4" style="display:none">
					<c:choose>
						<c:when test="${msgTypeOverride[idx] eq 49}">
							<input type="checkbox"
						       name="message_Action_Override"
						       id="message_Action_Override${idx}"
						       value="${action.map.message_action_id}" checked
						       onchange="setOverride(${idx},${action.map.message_action_id})"/> Override
							<input type="hidden" name="actionoverride" id="actionoverride${idx}"
						       value="${action.map.message_action_id}"/>
						</c:when>
						<c:otherwise>
							<input type="checkbox"
						       name="message_Action_Override"
        					   id = "message_Action_Override${idx}"
        					   value="${action.map.message_action_id}"
         					   onchange="setOverride(${idx},${action.map.message_action_id})"/> Override
							<input type="hidden" name="actionoverride" id="actionoverride${idx}"
						       value="0"/>
						</c:otherwise>
					</c:choose>
					</td>
				 </tr>
				<c:set var="idx" value="${idx+1}"/>
				</c:forEach>
	 		 </table>
	 		</div>
	</c:if>
	</fieldset>

	<table class="screenActions">
		<tr>
		    <c:choose>
			    <c:when test="${bean.map.editability == 'N' && roleId == 1}">
			        <td><button type="submit" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button></td>
			    </c:when>
			    <c:otherwise>
			        <td ${buttonDisplay}><button type="submit" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button></td>
			    </c:otherwise>
			</c:choose>
			<c:if test="${param._method=='show'}">
			<td ${addbuttonDisplay}>
			<insta:screenlink screenId="mas_message_type" extraParam="?_method=add" label="Add" addPipe="true"/>
			<c:if test="${configParamCount > 0}">
			<td ${buttonDisplay}>&nbsp;|&nbsp;</td>
			<td ${buttonDisplay}><a href="${cpath}/master/messages/${bean.map.message_type_id}/MessageConfig.do?_method=show&message_type_id=${bean.map.message_type_id}">Configure</a></td>
			</c:if>
			</c:if>
			<td ${buttonDisplay}>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Message Type List</a></td>
		</tr>
    </table>
	<div class="clrboth"></div><br/>
	<div style="display:none">${fn:length(tokenMap)}</div>
	<c:if test="${not empty tokenMap}">
	<table class="dataTable" >
		<c:choose>
		<c:when test="${fn:length(tokenMap) gt 1}">
			<tr><th colspan="${fn:length(tokenMap)}"> Available Tokens </th></tr>
			<tr>
				<c:forEach var="tokenEntry" items="${tokenMap}" varStatus="status">
				<c:if test="${status.count % 2  == 1}">
                    <tr>
                    </c:if>
				<c:set var="tokenList" value="${tokenEntry.value}"/>
				<c:set var="nrows" value="8"></c:set>
				<c:set var="ncols" value="${fn:length(tokenList) / nrows}"/>
				<fmt:formatNumber var="ncols"
					value="${ncols - (ncols % 1 ) + (((fn:length(tokenList) mod nrows) gt 0) ? 1 : 0)}" pattern="#"/>
				<div style="display:none;">Rows : ${nrows} Columns : ${ncols}</div>
		  		<td valign="top" style="padding:0px">
					<table class="dataTable" style="width:100%;">
					<tr><th colspan="${ncols}">${tokenEntry.key}</th></tr>
					<c:forEach var="row" begin="0" end="${(nrows > 0) ? nrows-1 : 0}">
						<tr>
						<c:forEach var="col" begin="0" end="${(ncols > 0) ? ncols-1 : 0}">
								<td>&nbsp;${tokenList[(ncols * row) + col]}</td>
						</c:forEach>
						</tr>
					</c:forEach>
					</table>
				</td>
				<c:if test="${status.count % 2  == 0}">
                    </tr>
                    </c:if>
				</c:forEach>
			</tr>
		</c:when>
		<c:otherwise>
			<c:forEach var="tokenEntry" items="${tokenMap}" varStatus="status">
				<c:set var="tokenList" value="${tokenEntry.value}"/>
			</c:forEach>
			<c:set var="ncols" value="5"></c:set>
			<c:set var="nrows" value="${fn:length(tokenList) / ncols}"/>
			<fmt:formatNumber var="nrows"
				value="${nrows - (nrows % 1 )+ (((fn:length(tokenList) mod ncols) gt 0) ? 1 : 0)}" pattern="#"/>
			<tr><th colspan="${ncols}"> Available Tokens </th></tr>
			<div style="display:none;">Rows : ${nrows} Columns : ${ncols}</div>
			<c:forEach var="row" begin="0" end="${(nrows > 0) ? nrows-1 : 0}">
				<tr>
				<c:forEach var="col" begin="0" end="${(ncols > 0) ? ncols-1 : 0}">
						<td>&nbsp;${tokenList[(ncols * row) + col]}</td>
				</c:forEach>
				</tr>
			</c:forEach>
		</c:otherwise>
		</c:choose>
	</table>
	</c:if>

</form>

</body>
</html>
