<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Feedback Forms List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		var psAc = null;
		var toolbar = {
			Print : {
				title: "Print Response",
				imageSrc: "icons/Edit.png",
				href: 'patientfeedback/SurveyPatientResponses.do?_method=printPatientResponse',
				description: "Print Patient Feedback Response Details"
			}
		}

		function init() {
			createToolbar(toolbar);
			psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
			document.getElementById('_mr_no').checked = true;
			document.getElementById('mrno').focus();
		}

		function changeStatus() {
			var status = '';

			if (document.getElementById('_mr_no').checked) {
				status = 'active';
			} else {
				status = 'all';
			}
			if (status == 'active') {
				psAc.destroy();
				psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
			} else {
				psAc.destroy();
				psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
			}
		}

	</script>
</head>

<body onload="init()">
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="dtoList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<h1>Patient Responses</h1>
	<insta:feedback-panel/>

	<form name="patientResponsesSearchForm" action="SurveyPatientResponses.do">
		<input type="hidden" name="_method" value="getAllPatientResponses">
			<insta:search form="patientResponsesSearchForm" optionsId="optionalFilter" closed="${hasResults}">
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
						<div class="sboFieldLabel">&nbsp;
							<div class="sboFieldInput">
								<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changeStatus()"/>Active Only
							</div>
						</div>
					</div>
				</div>
				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Form Name:</div>
							<div class="sfField">
								<input type="text" name="form_name" id="form_name" value="${ifn:cleanHtmlAttribute(param.form_name)}" />
								<input type="hidden" name="form_name@op" value="ilike" />
							</div>
						</td>
						<td>
							<div style="clear:both"/ >
								<div class="sfLabel" >Form Filled Date</div>
								<div class="sfField">
									<div class="sfFieldSub">From:</div>
									<insta:datewidget name="survey_date" id="survey_date0"
										value="${paramValues.survey_date[0]}"/>
								</div>
								<div class="sfField">
									<div class="sfFieldSub">To:</div>
									<insta:datewidget name="survey_date" id="survey_date1"
										value="${paramValues.survey_date[1]}"/>
									<input type="hidden" name="survey_date@op" value="ge,le"/>
									</divinput type="hidden" name="survey_date@type" value="date">
									</divinput type="hidden" name="survey_date@cast" value="y">
								</div>
						</td>
					</tr>
				</table>
				</div>
			</insta:search>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr onmouseover="hideToolBar();">
			    <th>#</th>
				<insta:sortablecolumn name="mr_no" title="Mr No"/>
				<insta:sortablecolumn name="visit_id" title="Visit Id"/>
				<th>Feedback Form</th>
				<insta:sortablecolumn name="survey_date" title="Form Filled On"/>
			</tr>
           	<c:forEach items="${dtoList}" var="record" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{survey_response_id:'${record.survey_response_id}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${record.mr_no}</td>
					<td>${record.visit_id}</td>
					<td>${record.form_name}</td>
					<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${record.survey_date}" var="surveyDateTime"/>
					<td>${surveyDateTime}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>

	<div class="screenActions" style="float:left">
		<a href="RecordPatientResponse.do?_method=getAllActiveSurveyForms">Record Patient Response</a>
	</div>
</body>
</html>