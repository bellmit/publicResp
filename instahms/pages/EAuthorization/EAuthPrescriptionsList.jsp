<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>



<jsp:useBean id="preauthStatusDisplay" class="java.util.HashMap"/>
<c:set target="${preauthStatusDisplay}" property="O" value="Open"/>
<c:set target="${preauthStatusDisplay}" property="S" value="Sent"/>
<c:set target="${preauthStatusDisplay}" property="D" value="Denied"/>
<c:set target="${preauthStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${preauthStatusDisplay}" property="C" value="Closed"/>
<c:set target="${preauthStatusDisplay}" property="X" value="Cancelled"/>

<html>
<head>
	<title>Prior Auth Prescriptions List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<insta:link type="script" file="EAuthorization/eAuthPrescriptions.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<script>
		var companyList    = ${insCompList};
		var categoryList   = ${insCategoryList};
		var tpaList        = ${tpaList};
		var extraDetails   = [];
	</script>
</head>

<c:set var="preauthList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty preauthList}"/>

<body onload="init(); showFilterActive(document.preauthListSearchForm);">
<h1>Prior Auth Prescriptions List</h1>

<insta:feedback-panel/>
<c:if test="${empty error}">
<form name="preauthListSearchForm" method="GET" autocompelte="off">
	<input type="hidden" name="_method"  id ="_method" value="getList">
	<input type="hidden" name="_searchMethod" value="getList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="preauthListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
		  	<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
	   </div>
	 	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Visit Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From: </div>
							<insta:datewidget name="visited_date" id="visited_date0" value="${paramValues.visited_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To: </div>
							<insta:datewidget name="visited_date" id="visited_date1" value="${paramValues.visited_date[1]}"/>
							<input type="hidden" name="visited_date@op" value="ge,le"/>
							<input type="hidden" name="visited_date@cast" value="y"/>
						</div>
						<div class="sfLabel">Patient Status:</div>
						<div class="sfField">
							<insta:checkgroup name="patstatus" selValues="${paramValues.patstatus}"
							opvalues="A,I" optexts="Active,Inactive"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Ins. Company Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
							table="insurance_company_master" orderby="insurance_co_name"
							valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeInsuranceCompany()"/>
						</div>
						<div class="sfLabel">Network/Plan Type:</div>
						<div class="sfField">
							<insta:selectdb displaycol="category_name" name="category_id"
							id="category_id" values="${paramValues.category_id}" orderby="category_name"
							multiple="multiple" class="listbox" optionTitle="true"
							style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;"
							table="insurance_category_master" valuecol="category_id" dummyvalue="(All)" onchange="onChangeOfInsuranceCategory()"/>
							<input type="hidden" name="category_id@type" value="text"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">TPA Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${param.tpa_id}"
							table="tpa_master" orderby="tpa_name"
							valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA()"/>
						</div>
						<div class="sfLabel">Plan Name:</div>
						<div class="sfField">
							<select id="plan_id" name="plan_id" class="dropdown">
								<option selected="selected" value="">(All)</option>
							</select>
							<input type="hidden" name="plan_id@type" value="integer"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Prior Auth Status:</div>
						<div class="sfField">
							<insta:checkgroup name="preauth_status" selValues="${paramValues.preauth_status}"
							opvalues="O,S,D,R,C,X" optexts="Open,Sent,Denied,ForResub,Closed,Cancelled"/>
						</div>
						<div class="sfLabel">Prior Auth Presc. ID: </div>
						<div class="sfField">
							<input type="text" name="preauth_presc_id" value="${ifn:cleanHtmlAttribute(param.preauth_presc_id)}">
							<input type="hidden" name="preauth_presc_id@type" value="integer"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Prior Auth Request ID: </div>
						<div class="sfField">
							<input type="text" name="preauth_request_id" value="${ifn:cleanHtmlAttribute(param.preauth_request_id)}">
						</div>
						<div class="sfLabel">Prior Auth Mode: </div>
						<div class="sfField">
							<insta:checkgroup name="pre_auth_mode" selValues="${paramValues.pre_auth_mode}"
							opvalues="O,M" optexts="Online,Manual"/>
						</div>
					</td>
				</tr>
			</table>
	   </div>
	</insta:search>
 </form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"  showTooltipButton="true"/>

	<form name="preauthPresResultsForm" method="POST" autocomplete="off">
		<input type="hidden" name="_method" value="clonePrescription"/>
		<div class="resultList">
			<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable"">
				<tr onmouseover="hideToolBar();">
					<th>Select</th>
					<insta:sortablecolumn name="mr_no" title="MR No"/>
					<insta:sortablecolumn name="patient_id" title="Visit ID"/>
					<insta:sortablecolumn name="preauth_presc_id" title="Prior Auth Presc. ID"/>
					<th>Prior Auth Status</th>
					<th>Patient Name</th>
					<th>Visited/Reg Date</th>
					<insta:sortablecolumn name="primary_insurance_co_name" title="Pri. Insurance Company"/>
					<insta:sortablecolumn name="primary_tpa_name" title="Pri. TPA/Sponsor"/>
					<insta:sortablecolumn name="primary_category_name" title="Pri. Network/Plan Type"/>
					<insta:sortablecolumn name="secondary_insurance_co_name" title="Sec. Insurance Company"/>
					<insta:sortablecolumn name="secondary_tpa_name" title="Sec. TPA/Sponsor"/>
					<insta:sortablecolumn name="secondary_category_name" title="Sec. Network/Plan Type"/>
					<th>Resubmit Type</th>
				</tr>
				<c:forEach var="presc" items="${preauthList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${presc.patstatus eq 'I'}">grey</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>
				<c:set var="preauthFlagColor">
					<c:choose>
						<c:when test="${presc.preauth_status eq 'O'}">empty</c:when>
						<c:when test="${presc.preauth_status eq 'S'}">violet</c:when>
						<c:when test="${presc.preauth_status eq 'D'}">red</c:when>
						<c:when test="${presc.preauth_status eq 'R'}">yellow</c:when>
						<c:when test="${presc.preauth_status eq 'C'}">green</c:when>
						<c:when test="${presc.preauth_status eq 'X'}">grey</c:when>
					</c:choose>
				</c:set>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{preauth_presc_id: ${presc.preauth_presc_id}, consultation_id: ${presc.consultation_id},
							priority: '${presc.priority}', insurance_co_id: '${presc.priority == 1 ? presc.primary_insurance_co_id : presc.secondary_insurance_co_id}' },
							[true, true, ${receiveEnable}]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td><input type="radio" class="radio noToolbar" value="${presc.preauth_presc_id}" name="preauth_presc_id" ${presc.preauth_status == 'X' ? '' : 'disabled'} /></td>
					<td>${presc.mr_no}</td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${presc.patient_id}</td>
					<td>${presc.preauth_presc_id }</td>
					<td><img class="flag" src="${cpath}/images/${preauthFlagColor}_flag.gif"/></td>
					<td><insta:truncLabel value="${presc.patname}" length="15"/></td>
				   	<td>
				   	<c:choose>
				   		<c:when test="${presc.visit_type == 'o'}">
				   			<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.visited_date}"/>
				   		</c:when>
				   		<c:otherwise>
				   			<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.reg_date_time}"/>
				   		</c:otherwise>
				   	</c:choose>
				   	</td>
					<td><insta:truncLabel value="${presc.priority == 1 ? presc.primary_insurance_co_name : ''}" length="20"/></td>
				  	<td><insta:truncLabel value="${presc.priority == 1 ? presc.primary_tpa_name : ''}" length="20"/></td>
					<td><insta:truncLabel value="${presc.priority == 1 ? presc.primary_category_name : ''}" length="20"/></td>
					<td><insta:truncLabel value="${presc.priority == 2 ? presc.secondary_insurance_co_name : ''}" length="20"/></td>
				   	<td><insta:truncLabel value="${presc.priority == 2 ? presc.secondary_tpa_name : ''}" length="20"/></td>
					<td><insta:truncLabel value="${presc.priority == 2 ? presc.secondary_category_name : ''}" length="20"/></td>
				   	<td>${presc.resubmit_type}</td>
				</tr>
				<script>
					extraDetails['toolbarRow${st.index}'] = {
						'MR No': '${presc.mr_no}',
						'Patient Id': '${presc.patient_id}',
						'Age/Gender' : '${presc.age} ${presc.age_in}/${presc.patient_gender}',
						'Mobile No.': '${presc.patient_phone}',
						'Doctor' : <insta:jsString value="${presc.doctor}"/>
					};
				</script>
				</c:forEach>
			</table>
		<c:if test="${param._method == 'getList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
    </div>

	<table class="screenActions">
		<tr>
			<td>
				<input type="button" name="clone" value="Clone Prescription" onclick="return clonePrescription();">
			</td>
			<td>
				<insta:screenlink target="_blank" screenId="preauth_presc"
					extraParam="?_method=getSponsorList"
						label="Add New Prior Auth Prescription" addPipe="false"/>
			</td>
		 </tr>
	</table>
	</form>

	<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src="${cpath}/images/violet_flag.gif"></img></div>
		<div class="flagText">Sent</div>
		<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
		<div class="flagText">Denied</div>
		<div class="flag"><img src="${cpath}/images/yellow_flag.gif"></img></div>
		<div class="flagText">ForResub.</div>
		<div class="flag"><img src="${cpath}/images/green_flag.gif"></img></div>
		<div class="flagText">Closed</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
		<div class="flagText">Cancelled</div>
	</div>
</c:if>
</body>
</html>
