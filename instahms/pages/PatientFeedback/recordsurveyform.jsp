<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<jsp:useBean id="date" class="java.util.Date" />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Record Patient Response - Insta HMS</title>
	<jsp:include page="/pages/yuiScripts.jsp"/>
	<insta:link type="css" file="style.css"/>
	<insta:link type="script" file="common.js"/>
	<insta:link type="script" file="jsvalidate.js"/>
	<insta:link type="script" file="instaautocomplete.js"/>
	<insta:link type="script" file="ToolBar.js"/>
	<insta:link type="script" file="infobox.js"/>
	<insta:link type="script" file="DisplayColumnsToolbar.js"/>
	<insta:js-bundle prefix="common"/>


<script>

function init() {
	if(formDetailsLength == 1) {
		document.getElementById('save_btn'+0).style.display = 'table-cell';
		document.getElementById('prev_btn'+0).style.display = 'none';
		document.getElementById('next_btn'+0).style.display = 'none';
	}
}

var token = "${_insta_transaction_token}";
var formDetailsLength = "${surveyFormDetailsLength}";

function imposeMaxLength(obj){
	var objDesc = obj.value;
	var newLines = objDesc.split("\n").length;
	var length = objDesc.length + newLines;
	var fixedLen = 5000;
	if (length > fixedLen) {
		alert("response text can not be more than " +fixedLen +" characters");
		obj.focus();
		return false;
	}
	return true;
}

function isOneQuestionAnswered() {
	var responseTexts = document.getElementsByName("response_text");
	var responseValue = document.getElementsByName("db_response_value");
	var index = 0;

	for(var i=0;i<responseValue.length;i++) {
		if(!empty(responseTexts[i].value) || !empty(responseValue[i].value)) {
			index++;
			break;
		}
	}

	if(index == 0)
		return false;

	return true;
}

function saveRecords() {
	var responseTexts = document.getElementsByName("response_text");
	for(var i=0;i<responseTexts.length;i++) {
		if(!imposeMaxLength(responseTexts[i])) {
			return false;
		}
	}
	if(!isOneQuestionAnswered()) {
		alert("please enter your response.");
		return false;
	}
	if(!confirm("Would you like to save your response ?"))
		return false;

	document.getElementById('_method').value = "recordPatientSurveyResponse";
	document.patientResponseForm.submit();
}

function changeValue(obj,indexOfHiddenVar) {
	if(obj.checked) {
		document.getElementById('db_response_value'+indexOfHiddenVar).value = obj.value;
	}
}

function checkRadioButton(obj,indexOfCheckBox,indexOfHiddenVar) {
	document.getElementById('response'+indexOfCheckBox).checked = true;
	changeValue(document.getElementById('response'+indexOfCheckBox),indexOfHiddenVar);
}

function cancelSavePatientResponse() {
	document.getElementById('_method').value = "cancelPatientSurveyResponse";
	document.patientResponseForm.submit();
}

function showNextSectionQuestion(index) {
	if(formDetailsLength != index+1) {
		document.getElementById('sectionDiv'+index).style.display = 'none';
		index++;
		document.getElementById('sectionDiv'+index).style.display = 'block';
		if(index != 0) {
			document.getElementById('prev_btn'+index).style.display = 'table-cell';
			document.getElementById('save_btn'+index).style.display = 'none';
		}
	}

	if(index == formDetailsLength-1) {
		document.getElementById('next_btn'+index).style.display = 'none';
		document.getElementById('save_btn'+index).style.display = 'table-cell';
	}
}

function showPreviousSectionQuestion(index) {
	if(index != 0) {
		document.getElementById('sectionDiv'+index).style.display = 'none';
		index--;
		document.getElementById('sectionDiv'+index).style.display = 'block';
	} else {
		document.getElementById('prev_btn'+index).style.display = 'none';
		document.getElementById('next_btn'+index).style.display = 'table-cell';
	}

	if(index != formDetailsLength-1) {
		document.getElementById('save_btn'+index).style.display = 'none';
	}
}
</script>
<style>

</style>
</head>
<table>
<body style="overflow-y: scroll; "
	class="yui-skin-sam "
	id="rootEl"
	onload="loadTransactionTokens('_insta_transaction_token', token); ${not empty allMsgs ? 'showinfo();' : ''};init()" >

<form name="patientResponseForm" action="RecordPatientResponse.do"  autocomplete="off">
	<input type="hidden" name="_method" id="_method" value=""/>
	<input type="hidden" name="visit_id" id="visit_id" value="${ifn:cleanHtmlAttribute(param.visit_id)}"/>
	<input type="hidden" name="formId" id="formId" value="${formDetails.map.form_id}"/>
	<table id="tblMain"  width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td width="10%" height="25">&nbsp;</td>
		    <td height="25" class="whitebg brTBN brRn headerPadding">&nbsp;</td>
			<td class="whitebg brTBN brLn txtRT headerContainer">&nbsp;</td>
			<td width="10%">&nbsp;</td>
		</tr>
	    <tr >
		    <td width="10%" class="Navband GbrB" height="33px">&nbsp;</td>
		    <td style="width: 300px" colspan="2" valign="bottom"  class="Navband "  >
		    	<table cellspacing="0" cellpadding="0" >
		    		<tr>
		    			<td width="123" style="padding:0 0 1px 0;" class="GbrB brRn">
		    				<div style="position:relative; width:100px; height:10px">
									<div style="background:#999; height:40px; width:109px; position:absolute; top:-30px; left:16px; ">
										<div>
											<img src="${cpath}/showScreenLogo.do" style="position:absolute; top:-3px; left:-3px;  z-index:0; height:30px; width:89px; background-color:#FFF; padding:5px 10px; border-left:1px solid #667fa1; border-top:1px solid #667fa1; border-right:1px solid #96a9c2; border-bottom:1px solid #96a9c2;"/>
										</div>
									</div>
								</div>
		    			</td>

		    			<td valign="bottom" class="GbrB"></td>
		    		</tr>
		    	</table>
			</td>
			<td width="10%" class="Navband GbrB" >&nbsp;</td>
		</tr>
		<tr>
			<td></td>
			<td class="whitebg contentarea" style="padding-top: 0px;" colspan="2">
				<table width="100%" cellspacing="0" cellpadding="0">
					<tr>
	  					<td>
	  						<div id="content" >
								<table style="width:100%">
									<tr>
										<td>
											<c:forEach var="formDetails" items="${surveyFormDetails}" varStatus="st">
												<c:set var="index" value="${st.index}"/>
												<c:if test="${not empty formDetails}">
													<div id="sectionDiv${ifn:cleanHtmlAttribute(index)}" style="display:${index == 0 ? '' : 'none'}">
														<table width="100%">
															<tr>
																<td style="border-bottom:2px solid;">&nbsp;</td>
															</tr>
															<tr><td>&nbsp;</td></tr>
															<tr>
																<td style="text-align:center;font-size:20px;">${formDetails.map.section_title}</td>
															</tr>
															<tr><td style="border-bottom:2px solid;">&nbsp;</td></tr>
															<tr><td>&nbsp;</td></tr>
															<tr>
																<td style="font-size:20px;"><c:out value="${ifn:breakContent(fn:escapeXml(formDetails.map.section_instructions))}" escapeXml="false" /></td>
															</tr>
															<tr><td>&nbsp;</td></tr>
															<tr>
																<td>
																	<table width="100%">
																		<tr id="question_index${ifn:cleanHtmlAttribute(index)}">
																			<td>
																				<table>
																						<tr>
												        									<td style="width:100%;font-size: 25px;" colspan="3">
												        										<input type="hidden" name="question_id" value="${formDetails.map.question_id}"/>
																								<input type="hidden" name="response_type" id="response_type" value="${formDetails.map.response_type}"/>
																								<input type="hidden" name="db_response_value" id="db_response_value${ifn:cleanHtmlAttribute(index)}" value=""/>
												        										<c:out value="${formDetails.map.question_detail}"/>
												        									</td>
																						</tr>
																						<c:choose>
																							<c:when test="${formDetails.map.response_type == 'Y'}">
																								<tr>
																									<td style="font-size: 25px;width:20%;" onclick="checkRadioButton(this,'${ifn:cleanJavaScript(index)}0','${ifn:cleanJavaScript(index)}')">
														          										<input type="radio" name="response_value${ifn:cleanHtmlAttribute(index)}" id="response${ifn:cleanHtmlAttribute(index)}0" value="Y" onclick="changeValue(this,'${ifn:cleanJavaScript(index)}')">&nbsp;
														          										<label id="response${ifn:cleanHtmlAttribute(s.index)}0" onclick="checkRadioButton(this,'${ifn:cleanJavaScript(index)}0','${ifn:cleanJavaScript(index)}')">Yes
														          									</td>
														          									<td style="font-size: 25px;width:80%;" onclick="checkRadioButton(this,'${ifn:cleanJavaScript(index)}1','${ifn:cleanJavaScript(index)}')">
														          										<input type="radio" name="response_value${ifn:cleanHtmlAttribute(st.index)}${ifn:cleanHtmlAttribute(s.index)}" id="response${ifn:cleanHtmlAttribute(index)}1" value="N" onclick="changeValue(this,'${ifn:cleanJavaScript(index)}')">&nbsp;
														          										<label id="response${ifn:cleanHtmlAttribute(index)}1" onclick="checkRadioButton(this,'${ifn:cleanJavaScript(index)}1','${ifn:cleanJavaScript(index)}')">No
														          									</td>
														          									<td>&nbsp;</td>
																								</tr>
																							</c:when>
																							<c:when test="${formDetails.map.response_type == 'R'}">
																								<c:set var="ratingDetailsList" value="${ratingDetailsMap[formDetails.map.rating_type_id]}"/>
																								<c:forEach var="ratingDetails" items="${ratingDetailsList}" varStatus="status">
																									<tr>
																										<td style="font-size: 25px; width:100%;" colspan="3" onclick="checkRadioButton(this,'${st.index}${s.index}${status.index}','${st.index}${s.index}')">
															          										<input type="radio" name="response_value${st.index}${s.index}" id="response${st.index}${s.index}${status.index}" value="${ratingDetails.map.rating_id}" onclick="changeValue(this,'${index}')">&nbsp;
															          										<label id="response${index}${status.index}" onclick="checkRadioButton(this,'${index}${status.index}','${index}')">${ratingDetails.map.rating_text}</label>
															          									</td>
																									</tr>
																								</c:forEach>
																							</c:when>
																							<c:otherwise>
																							</c:otherwise>
																				 		</c:choose>
																						<tr><td colspan="3">&nbsp;</td></tr>
																						<tr><td colspan="3">&nbsp;</td></tr>
																						<tr>
																							<td colspan="3">
																								<textarea cols="40" rows="4" name="response_text" style="font-size: 25px;width:85% " ></textarea>
																							</td>
																						</tr>
																					</table>
																				</td>
																			</tr>
																			<tr>
																				<td>
																					<div align="left">&nbsp;</div>
																						<div class="screenActions" align="center">
																							<table>
																								<tr>
																									<td id="prev_btn${ifn:cleanHtmlAttribute(index)}" style="display:${index == 0 ? 'none' : ''}">
																										<button type="button" accesskey="N" onclick="showPreviousSectionQuestion(${ifn:cleanJavaScript(index)})" style="font-size: 25px;">Prev</button>
																									</td>
																									<td width="30px;">&nbsp;</td>
																									<td id="next_btn${ifn:cleanHtmlAttribute(index)}">
																										<button type="button" accesskey="N" onclick="showNextSectionQuestion(${ifn:cleanJavaScript(index)})" style="font-size: 25px;">Next</button>
																									</td>
																									<td id="save_btn${ifn:cleanHtmlAttribute(index)}" style="display:${index == 0 ? 'none' : ''}">
																										<button type="button" accesskey="S"  style="font-size: 25px;" onclick="saveRecords();">Save</button>&nbsp;
																									</td>
																								</tr>
																							</table>
																						</div>
																					</div>
																				</td>
																			</tr>
																	</table>
																</td>
															</tr>
													</table>
												</div>
											</c:if>
										</c:forEach>
									</td>
								</tr>
								<tr height="5px;"><td>&nbsp;</td></tr>
								<tr>
									<td style="font-size:20px;"><c:out value="${ifn:breakContent(fn:escapeXml(formDetails.map.form_footer))}" escapeXml="false" /></td>
								</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<!-- screen ID is: ${screenId} -->
							<table  align="center" style="margin-top: 10px; width: 100%" >
								<tr >
									<td class="message" >
										<c:if test="${not empty allMsgs}">
											<div class="infoPanel" >
												<div id="infobox" style="align: center">
													<c:forEach items="${allMsgs}" var="msg" varStatus="i" >
													<li class="infomsg_${msg.map.severity}" style="align: center; display: ${i.index == 0 ? 'block' : 'none'}; opacity: 0;">
														${fn:replace(fn:replace(msg.map.messages,'$2',msg.map.param2),'$1',msg.map.param1)}
														</li>
													</c:forEach>
												</div>
											</div>
										</c:if>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td>
							<div class="foottertxt" style="margin-top:5px;">
								<table width="100%">
									<tr>
										<td>
											<a href="http://www.instahealthsolutions.com/" class="footer-supportlink"
							                   title="Visit the web site (opens in a new window)" target="_blank">
							                   Insta by Practo.
							                </a> Version <fmt:message key="insta.software.version" />.
							                 Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.
										</td>
										<td align="right">
											<a href="${cpath}/help/release_notes.pdf" target="_blank"
												class="supportlink">Release Notes</a> |
											<a href="${cpath}/help/Insta_Acknowledgements.pdf" target="_blank"
												class="supportlink">Acknowledgement</a> |
											<a href="${cpath}/help/InstaHMS_help.pdf" target="_blank"
												class="supportlink" >Help</a> |
											<a href="mailto:insta-support@practo.com"
												title="Email Insta customer support team to report a problem or request for assistance"
												target="_blank" class="supportlink">Customer Support</a>
										</td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
				</table>
			</td>
		</tr>

	</table>

	</body>
	</html>
