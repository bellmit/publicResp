<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html:html>

<head>
	<title>MRD Codification Search -Insta HMS </title>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="/medicalrecorddepartment/icdpatientsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

	<script type="text/javascript">
	
	var companyTpaList = ${insCompTpaList};
	var tpaList        = ${tpaList};

		var cpath="${cpath}";

		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'pages/medicalrecorddepartment/MRDUpdate.do?_method=getMRDUpdateScreen',
				onclick: null,
				description: "View and/or Edit the contents MRD Code"
			},
		  	Print: {
		        title: "Print",
		        imageSrc: "icons/Print.png",
		        href: 'pages/medicalrecorddepartment/MRDUpdate.do?_method=print',
		        onclick: null,
		        description: "Print the MRD Code",
		        target: '_blank'
			},
			VisitEMR : {
				title: 'Visit EMR',
				imageSrc : 'icons/Edit.png',
				href	: 'emr/VisitEMRMainDisplay.do?_method=list'
			},
			VisitBasedPatientEMR : {
				title : "Patient EMR(Visit Based)",
				imageSrc : "icons/Edit.png",
				href	: 'emr/EMRMainDisplay.do?_method=list&filterType=visits'
			},
			DocumentBasedPatientEMR : {
				title : "Patient EMR(Docmt Based)",
				imageSrc : "icons/Edit.png",
				href : 'emr/EMRMainDisplay.do?_method=list&filterType=docType&fromDate=&toDate='
			}
		};

	</script>

</head>

<c:set var="patientList" value="${pagedList.dtoList}"/>

<body onload="init()">

	<h1>MRD Codification Search</h1>

	<insta:feedback-panel/>

	<form method="GET" name="icdSearchForm" onsubmit="return validateSearchForm()">

		<input type="hidden" name="_method" value="searchICDPatients">
		<input type="hidden" name="_searchMethod" value="searchICDPatients"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<c:set var="hasResults" value="${not empty patientList}"/>

		<insta:search form="icdSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">MR No:</div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<input type="hidden" name="mr_no@op" value="ico" />
							<div id="mrnoContainer"></div>
						</div>
					</div>
				</div>

				<c:if test="${regPref.oldRegNumField != '' && regPref.oldRegNumField != null}">
					<div class="sboField">
							<div class="sboFieldLabel">${regPref.oldRegNumField}:</div>
							<div class="sboFieldInput">
								<input type="text" name="oldmrno" value="${param.oldmrno}"/>
								<input type="hidden" name="oldmrno@op" value="ico" />
							</div>
	                </div>
                 </c:if>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Admission Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="reg_date" id="reg_date1" valid="past" value="${paramValues.reg_date[0]}"/>
								<input type="hidden" name="reg_date@op" value="ge,le"/>
								<input type="hidden" name="reg_date@cast" value="date"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="reg_date" id="reg_date2" valid="past" value="${paramValues.reg_date[1]}"/>
							</div>
							<div class="sfLabel">Type:</div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" opvalues="i,o" optexts="IP,OP" selValues="${paramValues.visit_type}"/>
							</div>
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="visit_status"
									selValues="${paramValues.visit_status}"
									opvalues="A,I" optexts="Active,Inactive"/>
							</div>
						</td>
						<td>
							<div class="sfLabel">Discharge Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="discharge_date" id="discharge_date1" valid="past" value="${param.discharge_date}"/>
								<input type="hidden" name="discharge_date@op" value="ge,le"/>
								<input type="hidden" name="discharge_date@cast" value="date"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="discharge_date" id="discharge_date2" valid="past" value="${paramValues.discharge_date[1]}"/>
							</div>
							<div class="sfLabel">OP Type</div>
							<div class="sfField">
								<insta:checkgroup name="op_type" selValues="${paramValues.op_type}"
								opvalues="M,F,D,R,O" optexts="Main Visit,FollowUp,FollowUp No Cons.,Revisit,Outside"/>
							</div>
						</td>

						<td>
							<div class="sfLabel">Insurance type:</div>
							<div class="sfField">
								<insta:checkgroup name="insurance_status" selValues="${paramValues.insurance_status}"
								opvalues="Y,N" optexts="Insured,Not Insured"/>

							</div>
							<div class="sfLabel">Insurance Plan type:</div>
							<div class="sfField">
								<insta:checkgroup name="insurance_plan_status" selValues="${paramValues.insurance_plan_status}"
								opvalues="Y,N" optexts="With Plan,Without Plan"/>

							</div>
							<div class="sfLabel"><insta:ltext key="search.patient.visit.department"/></div>
								<div class="sfField">
									<insta:selectdb name="dept_id" table="department" valuecol="dept_id" displaycol="dept_name"
										dummyvalue="(All)" values="${paramValues.dept_id}" orderby="dept_name"/>
								</div>
							<div class="sfLabel"><insta:ltext key="search.patient.visit.doctor"/></div>
								<div class="sfField">
								<c:set var="docSelected" value="${fn:join(paramValues.doctor_id, ' ')}"/>
								<select name="doctor_id" id="doctor_id"  multiple="multiple" class="listbox" optionTitle="true"
											style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;">
									<option value="">(All)</option>
									<c:forEach items="${doclist}" var="doctors" >
										<c:set var="selected" value="${fn:contains(docSelected,doctors.map.doctor_id)?'selected':''}"/>
										<option value="${doctors.map.doctor_id}" ${selected}>${doctors.map.doctor_name}</option>
									</c:forEach>
								</select>
								</div>
						</td>
						<td>
							<div class="sfLabel">Pri. Insurance Company</div>
							<div class="sfField">
								<insta:selectdb displaycol="insurance_co_name" name="primary_insurance_co_id" id="primary_insurance_co_id" value="${param.primary_insurance_co_id}"
								table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" orderby="insurance_co_name" onchange="onChangePrimaryInsuranceCompany()"/>
							</div>
							<div class="sfLabel">Pri. Sponsor Name:</div>
							<div class="sfField">
								<insta:selectdb displaycol="tpa_name" name="primary_sponsor_id" id="primary_sponsor_id" value="${param.primary_sponsor_id}"
								table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" orderby="tpa_name" onchange="onChangePrimaryTPA()"/>
							</div>
							<div class="sfLabel">Pri. Network/Plan Type:</div>
							<div class="sfField">
								<c:set var="inscatSelected" value="${fn:join(paramValues.primary_category_id, ' ')}"/>
							<select name="primary_category_id" id="primary_category_id" multiple="multiple" class="listbox" optionTitle="true" 
							style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;">
								<option value="(All)">(All)</option>
								<c:forEach items="${inscatname}" var="inscat" >
									<c:set var="selected" value="${fn:contains(inscatSelected,inscat.map.category_id)?'selected':''}"/>
									<option value="${inscat.map.category_id}" ${selected}>${inscat.map.category_name}</option>
								</c:forEach>
									<input type="hidden" name="primary_category_id@type" value="text"/>
							</select>
							</div>
							<div class="sfLabel">Sec. Insurance Company</div>
							<div class="sfField">
								<insta:selectdb displaycol="insurance_co_name" name="secondary_insurance_co_id" id="secondary_insurance_co_id" value="${param.secondary_insurance_co_id}"
								table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" orderby="insurance_co_name" onchange="onChangeSecondaryInsuranceCompany()"/>
							</div>
							<div class="sfLabel">Sec. Sponsor Name:</div>
							<div class="sfField">
								<insta:selectdb displaycol="tpa_name" name="secondary_sponsor_id" id="secondary_sponsor_id" value="${param.secondary_sponsor_id}"
								table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" orderby="tpa_name" onchange="onChangeSecondaryTPA()"/>
							</div>
							<div class="sfLabel">Sec. Network/Plan Type:</div>
							<div class="sfField">
							<c:set var="inscatSelected" value="${fn:join(paramValues.secondary_category_id, ' ')}"/>
							<select name="secondary_category_id" id="secondary_category_id" multiple="multiple" class="listbox" optionTitle="true" >
								<option value="(All)">(All)</option>
								<c:forEach items="${inscatname}" var="inscat" >
									<c:set var="selected" value="${fn:contains(inscatSelected,inscat.map.category_id)?'selected':''}"/>
									<option value="${inscat.map.category_id}" ${selected}>${inscat.map.category_name}</option>
								</c:forEach>
									<input type="hidden" name="secondary_category_id@type" value="text"/>
							</select>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Coding Status:</div>
							<div class="sfField">
								<insta:checkgroup name="codification_status"
									selValues="${paramValues.codification_status}"
									opvalues="P,C,R,V" optexts="In-Progress,Completed,Completed-Needs Verification,Verified and Closed"/>
							<div class="sfLabel">Assigned:</div>
							<div class="sfField">
								<insta:checkgroup name="assigned"
									selValues="${paramValues._assigned}"
									opvalues="Y,N" optexts="Assigned,Not Assigned"/>
							</div>
							<div class="sfLabel">Codified By/Assignee:</div>
							<div class="sfField">
								<insta:selectdb name="codified_by" value="${param.codified_by}" dummyvalue="(All)"
								table="u_user" valuecol="emp_username" displaycol="emp_username"
								filtercol="hosp_user" filtervalue="Y" orderby="emp_username"/>
							<div class="sfLabel">Diagnosis:</div>
							<div class="sfField">
								<input type="text" name="diagnosis_icd" value="${fn:substring(ifn:cleanHtmlAttribute(param.diagnosis_icd), 0, 20)}"/>
								<input type="hidden" name="diagnosis_icd@op" value="ico" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>
	</form>
	<c:set var="url" value="${cpath}/pages/medicalrecorddepartment/ICDPatientSearch.do?"/>
	
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" showPageSize = "true" 
		currPageSize = "${pagedList.pageSize}"/>
		
	<form name="icdSelectForm" method="post" action='<c:out value="${url}"/>'>
	 	<input type="hidden" name="_method" value="">	
		<c:if test="${param._method ne 'getScreen'}">
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
				<tr onmouseover="hideToolBar();">
					<th><input type="checkbox" name="_selectedPatientAll" onclick="return checkOrUncheckAll('_selectedPatient', this)"></th>
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="MR No"/>
					<insta:sortablecolumn name="patient_id" title="Visit No"/>
					<insta:sortablecolumn name="visit_date" title="Visited/Reg Date"/>
					<%--  no sorting on patient name--%>
					<th>Patient Name</th>
					<th>Department</th>
					<th>Doctor</th>
					<th title="Primary TPA/Sponsor Name">Pri. TPA/Sponsor</th>
					<th title="Primary Insurance Company Name">Pri Insurance Co. Name</th>
					<th title="Primary Network/Plan Type">Pri. Network/Plan Type</th>
					<th>Codified by</th>
				</tr>
				<c:forEach var="patient" items="${patientList}" varStatus="st">
					<c:set var="patient_emr" value="${urlRightsMap['emr_screen'] == 'A'}"/>
					<c:set var="visit_emr" value="${urlRightsMap['visit_emr_screen'] == 'A'}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{patient_id:'${patient.patient_id}', visit_id: '${patient.patient_id}', mr_no: '${patient.mr_no}',
							oldmrno:'${patient.oldmrno}'}, [true, true, ${visit_emr}, ${patient_emr}, ${patient_emr}]);">
						<td>
							<input type="checkbox" name="_selectedPatient" value="${patient.patient_id}">
							<input type="hidden" name="_patient_codification_status" id="_patient_codification_status" value="${patient.codification_status}"/>
						</td>
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
						<td>
							<c:set var="flag">
								<c:choose>
									<c:when test="${patient.codification_status == 'P'}">empty</c:when>
									<c:when test="${patient.codification_status == 'C'}">yellow</c:when>
									<c:when test="${patient.codification_status == 'R'}">red</c:when>
									<c:when test="${patient.codification_status == 'V'}">green</c:when>
								</c:choose>
							</c:set>
							<img src='${cpath}/images/${ifn:cleanURL(flag)}_flag.gif'> ${patient.mr_no}
						</td>
						<td>
							${patient.patient_id}
						</td>
						<td>${patient.visit_date}</td>
						<td>${patient.salutation} ${patient.patient_name} ${patient.last_name}</td>
						<td><insta:truncLabel value="${patient.dept_name}" length="25"/></td>
						<td>${patient.doctor_name}</td>
						<td><insta:truncLabel value="${patient.primary_sponsor_name}" length="30"/></td>
						<td><insta:truncLabel value="${patient.primary_insurance_co_name}" length="30"/></td>
						<td><insta:truncLabel value="${patient.primary_network_type}" length="30"/></td>
						<td>${patient.codified_by}</td>
					</tr>
				</c:forEach>
			</table>


			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>


		</div>
		<div class="screenActions" style="float: left">
			<table cellpadding="0" cellspacing="0"  border="0" width="100%">
				<tr>
					<td>
						<button type="button" accesskey="C" name="codification_complete" onclick="checkCompleted();" />
						<b><u>C</u></b>lose</button>
					</td>
					<td>&nbsp;|&nbsp;</td>
					<td>
						<button type="button" accesskey="R" name="reopen_for_codification" onclick="checkReopenForCodification();" />
						<b><u>R</u></b>eopen For Codification</button>
					</td>
					<td>&nbsp;|&nbsp;</td>
					<td>Assign To:</td>
					<td>&nbsp;</td>
					<td>
						<select class="dropdown" name="_assign_to">
							<option value="">--select--</option>
							<c:forEach var="asigneeNames" items="${asigneeNames}">
								<option value="${asigneeNames.map.emp_username}">
									${asigneeNames.map.emp_username}
								</option>
							</c:forEach>
						</select>
					</td>
					<td>&nbsp;</td>
					<td>
						<button type="button" accesskey="A" name="Assign" onclick="checkAssignee();" />
						<b><u>A</u></b>ssign</button>
					</td>
					<td>
					<insta:screenlink screenId="raise_claim_batch_submission" extraParam="?_method=add"
						target="_blank" label="Claim Batch Submission" addPipe="true"/>
					</td>
				</tr>
			</table>
		</div>
	</form>
	<form name="icdSearchForm">
		<div class="legend">
			<button type="button" accesskey="P" name="print" onclick="printSearch();" />
			<b><u>P</u></b>rint</button>
		</div>
		<div style="clear: both"></div>
	</form>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText">Completed</div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Completed - Requires Verification</div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText">Verified and Closed</div>
	</div>
	</c:if>
</body>
</html:html>
