<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

	<c:choose>
		<c:when test="${param.mrdscreen eq 'issue'}">
			<title>MRD Case File List - Insta HMS</title>
		</c:when>
		<c:otherwise>
			<title>MRD Case File Users List - Insta HMS</title>
		</c:otherwise>
	</c:choose>

	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.sfFieldSub{white-space:nowrap}
	</style>

	<script type="text/javascript">

		var toolbar = {
			Edit: {
				title: "Edit/Issue",
				imageSrc: "icons/Edit.png",
				href: 'medicalrecorddepartment/MRDCaseFileIssue.do?_method=show',
				onclick: null,
				description: "Edit and/or Issue MRD case file details"
				},
			History: {
				title: "History",
				imageSrc: "icons/Edit.png",
				href: 'medicalrecorddepartment/MRDCaseFileIssue.do?_method=view',
				onclick: null,
				description: "View MRD Case File Issue Log"
				}
		};
	var mrdUserNameList = <%= request.getAttribute("mrdUserNameList") %>;

	var cpath="${cpath}";

	function searchFiles(){
		if('${ifn:cleanJavaScript(param.mrdscreen)}' == 'issue') {
			document.MRDSearchResults.action = "./MRDCaseFileIssue.do";
		}else {
			document.MRDSearchResults.action = "./MRDCaseFileReturn.do";
		}
		document.MRDSearchResults._method.value = "list";
		document.MRDSearchResults.submit();
	}

	function initCaseFileUserNameAutoComplete() {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(mrdUserNameList);
			oAutoComp = new YAHOO.widget.AutoComplete('issued_to', 'issuedToDropdown', dataSource);
			oAutoComp.maxResultsDisplayed = 5;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.forceSelection = true;
		}
	}

	function init() {
		initMrNoAutoComplete(cpath);
		initCaseFileUserNameAutoComplete();
		if('${ifn:cleanJavaScript(param.mrdscreen)}' == 'issue') {
			initMrdUserNameAutoComplete();
		}

		createToolbar(toolbar);
		showFilterActive(document.MRDSearchResults);
	}

	function initMrdUserNameAutoComplete() {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(mrdUserNameList);
			oAutoComp = new YAHOO.widget.AutoComplete('mrd_issued_to', 'issuedToContainer', dataSource);
			oAutoComp.maxResultsDisplayed = 5;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.forceSelection = true;
		}
	}

	function onCheckRadio(val) {
		var fileElmts = document.MRDSearchResults.fileCheck;
		if(fileElmts.length != undefined) {
			if(val == 'singleFile') {
				for(var i=0;i<fileElmts.length;i++) {
					fileElmts[i].disabled = false;
					fileElmts[i].checked = false;
					document.MRDSearchResults.hiddenMrno[i].value = "";
				}
			}else if(val == 'pageFiles') {
				for(var i=0;i<fileElmts.length;i++) {
					fileElmts[i].checked = true;
					fileElmts[i].disabled = false;
					document.MRDSearchResults.hiddenMrno[i].value = fileElmts[i].value;
				}
			}else {
				for(var i=0;i<fileElmts.length;i++) {
					fileElmts[i].disabled = true;
					fileElmts[i].checked = true;
					document.MRDSearchResults.hiddenMrno[i].value = fileElmts[i].value;
				}
			}
		}else {
			if(val == 'singleFile') {
				fileElmts.disabled = false;
				fileElmts.checked = false;
				document.MRDSearchResults.hiddenMrno.value = "";
			}else if(val == 'pageFiles') {
				fileElmts.checked = true;
				fileElmts.disabled = false;
				document.MRDSearchResults.hiddenMrno.value = fileElmts.value;
			}else {
				fileElmts.disabled = true;
				fileElmts.checked = true;
				document.MRDSearchResults.hiddenMrno.value = fileElmts.value;
			}
		}
	}

	function onFileCheck(fileCheck, hiddenMrnoElmt) {
		if(fileCheck.checked) {
			document.getElementById(hiddenMrnoElmt).value = fileCheck.value;
		}else {
			document.getElementById(hiddenMrnoElmt).value = "";
		}
	}

	function checkFiles() {
		var fileElmts = document.MRDSearchResults.fileCheck;
		for(var i=0;i<fileElmts.length;i++) {
			if(fileElmts[i].checked) return true;
		}
		return false;
	}

	function validate(screen) {
		for(var i=0;i<document.MRDSearchResults.selectFiles.length;i++) {
			if(document.MRDSearchResults.selectFiles[i].checked &&
				(document.MRDSearchResults.selectFiles[i].value == 'singleFile' || document.MRDSearchResults.selectFiles[i].value == 'pageFiles') ) {
				if(!checkFiles()) {
					alert("Please select any case file");
					return false;
				}
			}
		}
		if(screen == 'issue') {
			if(trim(document.MRDSearchResults.mrd_issued_to.value) == '') {
				alert("Please enter issued to");
				document.MRDSearchResults.mrd_issued_to.focus();
				return false;
			}
			document.MRDSearchResults._method.value = 'issue';
		}
		else document.MRDSearchResults._method.value = 'returnmrd';

		document.MRDSearchResults.submit();
	}
	</script>
</head>
<body class="yui-skin-sam" onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

		<c:choose>
			<c:when test="${mrdscreen eq 'issue'}">
				<h1>MRD Case Files </h1>
			</c:when>
			<c:otherwise>
				<h1>MRD Case Files Users </h1>
			</c:otherwise>
		</c:choose>

		<c:set var="dtoList" value="${pagedList.dtoList}"/>

	<insta:feedback-panel/>

	<form name="MRDCaseSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="MRDCaseSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">MR No:</div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<input type="hidden" name="mr_no@op" value="ilike" />
							<div id="mrnoContainer"></div>
						</div>
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Patient Details:</div>
							<div class="sfField">
								<div class="sfFieldSub">Patient Name:</div>
									 <input type="text" name="patient_name" class="field" value="${ifn:cleanHtmlAttribute(param.patient_name)}"/>
					    			 <input type="hidden" name="patient_name@op" value="ico" />
					    	</div>
					    	<div class="sfField">
					    		<div class="sfFieldSub">Case File No:</div>
					    			<input type="text" name="casefile_no" size="10" value="${ifn:cleanHtmlAttribute(param.casefile_no)}"/>
					    			<input type="hidden" name="casefile_no@op" value="ico" />
					    	<div class="sfField">
					    		<div class="sfFieldSub">Department:</div>
				    			<insta:selectdb displaycol="dept_name" name="dept_id" table="department"
								    dummyvalue="--Select--"	valuecol="dept_id" value="${param.dept_id}"/>
				    				<input type="hidden" name="dept_id@op" value="ico" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Issued To:</div>
							<div class="sfField">
								<div id="issuedTo_wrapper" style="width: 15em;">
									<input type="text" name="issued_to" id="issued_to" value="${ifn:cleanHtmlAttribute(param.issued_to)}"/>
									<input type="hidden" name="issued_to@op" value="ico" />
									<div id="issuedToDropdown"></div>
								</div>
					    	</div>
						</td>

						<td>
							<div class="sfLabel">Issued On:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
						    		<insta:datewidget name="fdate" valid="past" value="${param.fdate}" btnPos="left"/>
						    </div>
							<div class="sfField">
						    	<div class="sfFieldSub">To:</div>
						    		<insta:datewidget name="fdate" valid="past" value="${param.tdate}" btnPos="left"/>
						    </div>

						</td>
						<td>
							<div class="sfLabel">Patient Type:</div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" opvalues="i,o" optexts="IP,OP" selValues="${paramValues.visit_type}"/>
								<input type="hidden" name="paramValues.visit_type@op" value="in" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="visit_status" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.visit_status}" />
								<input type="hidden" name="paramValues.visit_status@op" value="in" />
							</div>
						</td>
					</tr>

					<tr>
						<td class="last"s>
							<div class="sfLabel">Case Status:</div>
							<div class="sfField">
								<insta:checkgroup name="case_status" optexts="Pending,MRD ICD Updated,Inactive" opvalues="P,A,I" selValues="${paramValues.case_status}"/>
								<input type="hidden" name="paramValues.case_status@op" value="in" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	</form>
	<form name="MRDSearchResults" method="GET">
		<input type="hidden" name="_mrdscreen" value="${ifn:cleanHtmlAttribute(mrdscreen)}"/>
		<input type="hidden" name="_method"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>Select</th>
					<insta:sortablecolumn name="mr_no" title="MR No"/>
					<insta:sortablecolumn name="casefile_no" title="Case file number"/>
					<th>Patient Name</th>
					<th>Department</th>
					<th>Doctor</th>
					<th>Ward/Bed</th>
					<th>Current Owner of the File</th>
					<th>Issued On</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="status">
					<c:set var="i" value="${status.index}"/>
					<tr class="${status.index == 0 ? 'firstRow' : ''} ${status.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${status.index}, event, 'resultTable',
							{mr_no: '${ifn:cleanJavaScript(record.mr_no)}', mrdscreen: '${ifn:cleanJavaScript(param.mrdscreen)}'},'');" id="toolbarRow${ifn:cleanHtmlAttribute(status.index)}">
						<c:choose>
							<c:when test="${record.file_status ne 'L'}">
								<td>
									<input type="checkbox" name="fileCheck" value="${record.mr_no}" onclick="onFileCheck(this, 'hiddenMrno${i}') "/>
									<input type="hidden" name="hiddenMrno" id="hiddenMrno${i}"/>
								</td>
							</c:when>
							<c:otherwise>
								<td></td>
							</c:otherwise>
						</c:choose>
						<c:choose>
							<c:when test="${record.visit_status eq 'I'}"><c:set var="color" value="empty"/></c:when>
							<c:when test="${record.visit_status eq 'A'}"><c:set var="color" value="grey"/></c:when>
							<c:otherwise><c:set var="color" value="green"/></c:otherwise>
						</c:choose>
						<td >
							<img src='${cpath}/images/${color}_flag.gif'> ${record.mr_no}
						</td>
						<td>${record.casefile_no}</td>
						<td>${record.salutation} ${record.patient_name} ${record.last_name}</td>
						<td>${record.dept_name}</td>
						<td>${record.doctor_name}</td>
						<td>
							<c:if test="${record.visit_type == 'i'}">
							<c:choose>
								<c:when test="${preferences.modulesActivatedMap['mod_ipservices'] eq 'Y'}">
									<%-- show the allocated bed, if not allocated, no need to show reg. bed type/ward --%>
									<c:choose>
										<c:when test="${empty record.alloc_bed_name}">(Not allocated)</c:when>
										<c:otherwise>${record.alloc_ward_name}/${record.alloc_bed_name}</c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>${record.reg_ward_name}/${record.bill_bed_type}</c:otherwise>
							</c:choose>
							</c:if>
						</td>
						<td>
							<c:if test="${record.file_status eq 'A'}">MRD</c:if>
							<c:if test="${record.file_status eq 'L'}">Lost</c:if>
							<c:if test="${record.file_status eq 'U'}">${record.issued_to}</c:if>
							<c:if test="${record.file_status eq 'I'}">Inactive</c:if>
						</td>
						<td><fmt:formatDate value="${record.issued_on}" pattern="dd-MM-yyyy HH:mm"/></td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>
	<c:if test="${not empty dtoList}">
		<div class="crlboth">&nbsp;</div>
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">
						<input type="radio" name="selectFiles" id="singleFile"
						value="singleFile" checked="true" onclick="onCheckRadio(this.value)">Select Files
					</td>
					<td style="white-space:nowrap">
						<input type="radio" name="selectFiles" id="pageFiles"
						value="pageFiles" onclick="onCheckRadio(this.value)">Select All Files in this Page
					</td>
					<td>
						<input type="radio" name="selectFiles" id="allFiles"
						value="allFiles" onclick="onCheckRadio(this.value)">Select All Files
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>
			<jsp:useBean id="now" class="java.util.Date"/>
			<c:choose>
				<c:when test="${mrdscreen eq 'issue'}">
					<table class="formtable">
						<tr>
							<td class="formlabel">Date of Issue:</td>
							<td >
								<c:set var="dt" value="${now}"/>
								<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
								<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
								<table>
									<tr>
										<td style="white-space:nowrap"><insta:datewidget name="issued_date" valid="past" value="${date}"/></td>
										<td><input type="text" size="5" name="issued_time"  value="${time}"/></td>
									</tr>
								</table>
							</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel">Issued To:</td>
							<td>
								<div id="wrapper_issuedTo" style="width: 15em;">
									<input type="text" name="mrd_issued_to" id="mrd_issued_to"/>
									<div id="issuedToContainer"></div>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Purpose:</td>
							<td>
								<input name="purpose" type="text" size="27"/>
							</td>
						</tr>
					</table>
				</fieldset>
				<div class="screenActions" style="float:left"><input type="button" name="" value="Issue to User" onclick="return validate('issue')"/></div>
				</c:when>
				<c:otherwise>
					<table class="formtable">
						<tr>
							<td class="formlabel">Date of Return:</td>
							<td>
								<c:set var="dt" value="${now}"/>
								<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
								<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
								<table>
									<tr>
										<td style="white-space:nowrap"><insta:datewidget name="returned_date" valid="past" value="${date}"/></td>
										<td><input type="text" size="5" name="returned_time"  value="${time}"/></td>
									</tr>
								</table>
							</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel">Remarks:</td>
							<td>
								<input name="remarks" type="text" size="30"/>
							</td>
						</tr>
					</table>
				</fieldset>
				<div class="screenActions" style="float:left"><input type="button" name="" value="Return to MRD" onclick="return validate('mrd')"/></div>
			</c:otherwise>
		</c:choose>
		</c:if>
	</form>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive Patients</div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText">Patients Without Visits</div>
	</div>
</body>
</html>
