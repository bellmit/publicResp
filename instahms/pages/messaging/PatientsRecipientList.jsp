<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
	<title>Send Message - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
		<insta:link type="js" file="patientdetailssearch/patientdetailssearch.js"/>
	<insta:link type="js" file="messaging/messaging.js"/>
	<insta:link type="css" file="widgets.css"/>
		<style>
			.scrolForContainer .yui-ac-content{
				 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
			    _height:11em; /* ie6 */
			}
			.autocomplete {
				padding-bottom: 20px;
				width: 138px;
			}

		</style>
		<script>
			var areaListmain = ${areaList};
			var countryList = ${countryList};
			var stateList = ${stateList};
			var cityList = ${cityList};
		/*	var occupationList = ${occupationList};
			var bloodgroupList = ${bloodgroupList};
			var religionList = ${religionList};*/
			var orgNameJSONList = ${orgNameJSONList};
            var tpasponsorList = ${tpasponsorList};
            var customList1 = ${customList1};
            var customList2 = ${customList2};
            var customList3 = ${customList3};
            var customList4 = ${customList4};
            var customList5 = ${customList5};
            var customList6 = ${customList6};
            var customList7 = ${customList7};
            var customList8 = ${customList8};
            var customList9 = ${customList9};
			var contextPath = '${cpath}';
			var searchForm = document.visitSearchForm;

			function validateSearchForm() {
				var birthDateEl = document.visitSearchForm.patient_birth_day;
				var birthMonthEl = document.visitSearchForm.patient_birth_month;
				if (!empty(birthDateEl.value)) {
					if (!validateIntMinMax(document.visitSearchForm.patient_birth_day, 1, 31,
						'Invalid Birth Day. Please select a date between 1 and 31')) {
						document.visitSearchForm.patient_birth_day.focus();
						return false;
					}
				}
				if (!empty(birthMonthEl.value)) {
					if (!validateIntMinMax(document.visitSearchForm.patient_birth_month, 1, 12,
						'Invalid Birth Day. Please select a month between 1 and 12')) {
						document.visitSearchForm.patient_birth_day.focus();
						return false;
					}
				}
				if (!empty(birthDateEl.value) && !empty(birthMonthEl.value)) {
					// When both are specified, validate the combination
					var date = parseInt(birthDateEl.value,10);
					var month = parseInt(birthMonthEl.value,10);
					// allow feb 29 since we do not have a year reference
					if (date == 29 && month == 2) {
						return true;
					}
					// Any other date should be a valid date in the current year.
					var dateStr = cleanDateStr(birthDateEl.value + "-" +
								birthMonthEl.value + "-" + new Date().getFullYear());

					// validateDateStr returns the error message. So if it is not null, the date is invalid

					if (null != validateDateStr(dateStr, "")) {
						alert("Invalid Bith Day. Please enter a valid date / month combination");
						birthDateEl.focus();
						return false;
					}
				}

				// update the visit date strings - we need them in yyyy-mm-dd format.


				if (!empty(document.getElementById("_patient_visit_date0").value)) {
					var date0 = parseDateStr(document.getElementById("_patient_visit_date0").value);
					document.getElementById("patient_visit_date0").value = formatDate(date0, 'yyyymmdd');
				}
				if (!empty(document.getElementById("_patient_visit_date1").value)) {
					var date1 = parseDateStr(document.getElementById("_patient_visit_date1").value);
					document.getElementById("patient_visit_date1").value = formatDate(date1, 'yyyymmdd');
				}
				return true;
			}
		</script>
  <script>
	contextPath = "${pageContext.request.contextPath}";
	publicPath = contextPath + "/ui/";
  </script>			
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResult" value="${not empty messageDataList}"/>

<body onload="init(document.visitSearchForm);initMrNoAutoComplete(cpath);onLoadRecipientList();">
<div class="pageHeader">Send Message</div>
<div class="fieldSetLabel">
	Select Recipients :
	<c:forEach var="provider" items="${providerList}" varStatus="pstatus">
		<c:choose>
			<c:when test="${provider ne _currentProvider}">
			<a href="#" onclick="selectProvider('${provider}');">${provider}</a>
			</c:when>
			<c:otherwise>
			${provider}
			</c:otherwise>
		</c:choose>
		&nbsp;&nbsp;
	</c:forEach>
</div>
<div>&nbsp;</div>
		<form action="Message.do" method="GET" name="visitSearchForm">
		<input type="hidden" name="_method" value="searchRecipients"/>
		<input type="hidden" name="_searchMethod" value="searchRecipients"/>
		<input type="hidden" name="_currentProvider" value="${_currentProvider}"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="patient_country" value="${ifn:cleanHtmlAttribute(param.country)}" />
		<input type="hidden" name="patient_state" value="${ifn:cleanHtmlAttribute(param.patient_state)}" />
		<input type="hidden" name="patient_city" value="${ifn:cleanHtmlAttribute(param.patient_city)}" />
			<insta:search form="visitSearchForm" optionsId="optionalFilter" closed="${hasResult}" validateFunction="validateSearchForm()">
				<div class="searchBasicOpts" >
					<div class="sboField">
						<div class="sboFieldLabel">MR No/Patient Name:</div>
						<div class="sboFieldInput">
							<div id="mrnoAutoComplete">
								<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
								<input type="hidden" name="mr_no@op" value="ilike" />
								<div id="mrnoContainer"></div>
							</div>
						</div>
					</div>
					<div class="sboField">
						<c:set var="emailchecked" value="${(param.recipient_email == 'n')?'checked':''}"/>
						<div class="sboFieldLabel">&nbsp;&nbsp;</div>
 						<div class="sboFieldInput">
							<input type="hidden" name="recipient_email@op" value="null"/>
							<input type="checkbox" name="recipient_email" value="n" ${emailchecked}/>With Email Only
 						</div>
					</div>
					<div class="sboField">
						<c:set var="mobilechecked" value="${(param.recipient_mobile == 'n')?'checked':''}"/>
						<div class="sboFieldLabel">&nbsp;&nbsp;</div>
						<div class="sboFieldInput">
							<input type="hidden" name="recipient_mobile@op" value="null"/>
							<input type="checkbox" name="recipient_mobile" value="n" ${mobilechecked}/>With Mobile Only
						</div>
					</div>
					<div class="sboField">
						<c:set var="mobileaccesschecked" value="${(param.mobile_access == 'Y')?'checked':''}"/>
						<div class="sboFieldLabel">&nbsp;&nbsp;</div>
						<div class="sboFieldInput">
							<input type="hidden" name="mobile_access@op" value="Y"/>
							<input type="checkbox" name="mobile_access" value="Y" ${mobileaccesschecked}/>Mobile App Access
						</div>
					</div>
				</div>
				<div id="optionalFilter" style="clear: both; display: ${hasResult ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel">Patient Details:</div>
								<div class="sfField">
									<div class="sfFieldSub" style="white-space: nowrap">Patient Full Name:</div>
									<div style="clear: both"/>
									<input type="text" name="patient_full_name" value="${ifn:cleanHtmlAttribute(param.patient_full_name)}"/>
									<input type="hidden" name="patient_full_name@type" value="text"/>
									<input type="hidden" name="patient_full_name@op" value="ico"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">Mobile No.:</div>
									<div style="clear: both"/>
									<input type="text" name="patient_phone" value="${ifn:cleanHtmlAttribute(param.patient_phone)}"/>
									<input type="hidden" name="patient_phone@op" value="ico"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">Complaint:</div>
									<div style="clear: both"/>
									<input type="text" name="patient_complaint" id="patient_complaint" value="${ifn:cleanHtmlAttribute(param.patient_complaint)}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub" style="white-space: nowrap">Birth Day:</div>
									<div style="clear: both"/>
									<div>Date:<input style="width:26pt;" type="text" name="patient_birth_day" value="${ifn:cleanHtmlAttribute(param.patient_birth_day)}"/>&nbsp;
									Month:<input style="width:26pt;" type="text" name="patient_birth_month" value="${ifn:cleanHtmlAttribute(param.patient_birth_month)}"/></div>
									<input type="hidden" name="patient_birth_day@cast" value="y"/>
									<input type="hidden" name="patient_birth_month@cast" value="y"/>
								</div>
							</td>
							<td>
								<div class="sfLabel">Status</div>
								<div class="sfField">
									<insta:checkgroup name="patient_status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['mv.status']}"/>
								</div>
								<div class="sfLabel">Type</div>
								<div class="sfField">
									<insta:checkgroup name="patient_visit_type" opvalues="i,o" optexts="IP,OP" selValues="${paramValues.visit_type}"/>
								</div>

							</td>
							<td>
								<div class="sfLabel">Department</div>
								<div class="sfField">
									<insta:selectdb name="dept_id__" table="department" valuecol="dept_id" displaycol="dept_name"
										dummyvalue="---Select---" values="${paramValues.dept_id__}"/>
								</div>
								<div class="sfLabel">Doctor</div>
								<div class="sfField">
									<insta:selectdb name="doctor__" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
										dummyvalue="---Select---" values="${paramValues.doctor__}"/>
								</div>
								<div class="sfLabel">Admission Date:</div>
								<div class="sfField">
									<div class="sfFieldSub">From:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="_patient_visit_date" id="_patient_visit_date0" value="${paramValues._patient_visit_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">To:</div>
									<div style="clear:both"></div>
									<insta:datewidget name="_patient_visit_date" id="_patient_visit_date1" value="${paramValues._patient_visit_date[1]}"/>
									<input type="hidden" name="patient_visit_date" value="" id="patient_visit_date0"/>
									<input type="hidden" name="patient_visit_date" value="" id="patient_visit_date1"/>
									<input type="hidden" name="patient_visit_date@type" value="text"/>
									<input type="hidden" name="patient_visit_date@op" value="ge,le"/>
								</div>
							</td>
							<td>
								<div class="sfLabel">Location</div>
								<div class="sfField">
									<div class="sfFieldSub">Country:</div>
									<div style="clear: both"/>
									<div id="autocountry" class="autocomplete">
										<input type="text" name="_country" id="_country" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._country)}"/>
										<div id="countrycontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">State:</div>
									<div style="clear: both"/>
									<div id="autostate" class="autocomplete">
										<input type="text"	name="_patientstate" id="_patientstate" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._patientstate)}"/>
										<div id="statecontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">City:</div>
									<div style="clear: both"/>
									 <div id="autocity" class="autocomplete">
										<input type="text" name="_patientcity" id="_patientcity" size="8" class="field" value="${ifn:cleanHtmlAttribute(param._patientcity)}"/>
										<div id="citycontainer" class="scrolForContainer"></div>
									</div>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">Area:</div>
									<div style="clear: both"/>
									<div id="autoarea" class="autocomplete">
										<input type="text"	name="patient_area" id="patient_area" class="field" size="8" value="${ifn:cleanHtmlAttribute(param.patient_area)}"/>
										<div id="areacontainer" class="scrolForContainer"></div>
									</div>
								</div>
							</td>
							<td class="last" >
								<div class="sfLabel" style="white-space: nowrap;">Custom Registration Field</div>
								<div class="sfField">
									<select name="_customRegFieldName" class="dropdown">
										<option value="">...Select...</option>
										<c:forEach var="c" items="${customRegFieldsMap}">
											<option value="${c.key}" ${param._customRegFieldName == c.key ? 'selected' : ''}>${c.value}</option>
										</c:forEach>
									</select>
								</div>
								<div class="sfField">
									<input type="text" name="_customRegFieldValue" id="_customRegFieldValue" value="${ifn:cleanHtmlAttribute(param._customRegFieldValue)}" size="18" />
								</div>
								<div class="sfLabel">Registration Field</div>
								<div class="sfField">
									<select name="_regFieldName" onchange="onChangeRegField()" class="dropdown">
										<option value="">...Registration Field...</option>
										<option value="patient_gender" ${param._regFieldName == 'patient_gender' ? 'selected' : ''}>Gender</option>
										<option value="org_id" ${param._regFieldName == 'org_id' ? 'selected' : ''}>Rateplan</option>
										<option value="tpa_id" ${param._regFieldName == 'tpa_id' ? 'selected' : ''}>Tpa/Sponsor</option>
										<c:if test="${not empty regPref.custom_list1_name}">
										<option value="custom_list1_value" ${param._regFieldName == 'custom_list1_value' ? 'selected' : ''}>${regPref.custom_list1_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list2_name}">
										<option value="custom_list2_value" ${param._regFieldName == 'custom_list2_value' ? 'selected' : ''}>${regPref.custom_list2_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list3_name}">
										<option value="custom_list3_value" ${param._regFieldName == 'custom_list3_value' ? 'selected' : ''}>${regPref.custom_list3_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list4_name}">
										<option value="custom_list4_value" ${param._regFieldName == 'custom_list4_value' ? 'selected' : ''}>${regPref.custom_list4_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list5_name}">
										<option value="custom_list5_value" ${param._regFieldName == 'custom_list5_value' ? 'selected' : ''}>${regPref.custom_list5_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list6_name}">
										<option value="custom_list6_value" ${param._regFieldName == 'custom_list6_value' ? 'selected' : ''}>${regPref.custom_list6_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list7_name}">
										<option value="custom_list7_value" ${param._regFieldName == 'custom_list7_value' ? 'selected' : ''}>${regPref.custom_list7_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list8_name}">
										<option value="custom_list8_value" ${param._regFieldName == 'custom_list8_value' ? 'selected' : ''}>${regPref.custom_list8_name}</option>
										</c:if>
										<c:if test="${not empty regPref.custom_list9_name}">
										<option value="custom_list9_value" ${param._regFieldName == 'custom_list9_value' ? 'selected' : ''}>${regPref.custom_list9_name}</option>
										</c:if>
									</select>
								</div>
								<div class="sfField">
									<input type="hidden" name="_hiddenRegFieldValue" value="${ifn:cleanHtmlAttribute(param._regFieldValue)}">
						    		<select name="_regFieldValue" class="dropdown">
						    			<option value="">---Select---</option>
						    		</select>
								</div>
							</td>
						</tr>
					</table>
				</div>
			</insta:search>
</form>
<form method="POST" action="Message.do" name="MessageForm">

<input type="hidden" name="_method" value="saveRecipients" id="_method">
<input type="hidden" name="_currentProvider" value="${_currentProvider}"/>
<input type="hidden" name="_nextProvider" value=""/>
<input type="hidden" name="_removed_selections" value=""/>
<input type="hidden" name="message_mode" value="${message_mode}"/>

<!-- new css and new list pattern-->
	<c:if test ="${not empty messageDataList}" >
	<div class="resultList">
		<insta:paginate curPage="${pagingInfo['pageNumber']}" numPages="${pagingInfo['numPages']}" totalRecords="${pagingInfo['totalRecords']}"/>
		<table class="resultList dialog_displayColumns" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
					<th>
					<c:set var="all_checked" value=""/>

					<c:if test="${_select_all}">
						<c:set var="all_checked" value="checked"/>
					</c:if>

					<input type="checkbox" name="_select_all" value="true"
						onclick="allRecipients();" ${all_checked}/>
					</th>
					<th>MR No</th>
					<th>Patient Name</th>
 					<th>City</th>
					<th>Email</th>
					<th>Mobile</th>
					<th>Visit/Adm. Date</th>
					<th>Department</th>
					<th>Doctor</th>
				</tr>
				<c:forEach var="recipientData" items="${messageDataList}" varStatus="status">
					<c:set var="checked" value=""/>
					<tr class="${status.index == 0 ?'firstRow': ''} ${status.index % 2 == 0? 'even':'odd' }">
						<c:forEach var="selection" items="${currentSelections}" varStatus="selStatus">
							<c:if test="${selection eq recipientData.key}">
							<c:set var="checked" value="checked"/>
							</c:if>
						</c:forEach>
						<td><input type="checkbox" name="_selected_recipients"
								value="${recipientData['key']}" ${checked} onclick="clickRecipient(this, '${recipientData.key}');"/></td>
						<td>${recipientData.mr_no}</td>
						<td>
						<c:if test="${messageLog.map.message_mode eq 'SMS' && empty recipientData.recipient_mobile}"><img src='${cpath}/images/red_flag.gif'></c:if>
						<c:if test="${messageLog.map.message_mode eq 'EMAIL' && empty recipientData.recipient_email}"><img src='${cpath}/images/red_flag.gif'></c:if>
						${recipientData.recipient_name}
						</td>
						<td>${recipientData.patient_city_name}</td>
						<td>${recipientData.recipient_email}</td>
						<td>${recipientData.recipient_mobile}</td>
						<td>${recipientData.patient_visit_date}</td>
						<td>${recipientData.patient_dept}</td>
						<td>${recipientData.patient_doctor}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
	<insta:noresults hasResults="${hasResult}"/>
	<div class="screenActions">
	<button type="button"  class="button" accesskey="N"  onclick="return saveRecipients('${_currentProvider}');"><b><u>N</u></b>ext</button>
	<div class="legend" style="display: ${hasResult? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Recipient without contact information </div>
	</div>
	</div>
	</form>
</body>
</html>
