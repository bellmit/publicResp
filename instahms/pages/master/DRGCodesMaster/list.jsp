<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>DRG Codes List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '/master/DRGCodesMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit the contents of this DRG Code"
				}
		};

		function init() {
			createToolbar(toolbar);
			showFilterActive(document.DRGCodesForm);
		}

		function doUpload(formType) {

		   if(formType == "uploadDrgCodesForm"){
			var form = document.uploadDrgCodesForm;
				if (form.xlsDRGCodesFile.value == "") {
					alert("Please browse and select a file to upload");
					return false;
				}
			}
			form.submit();
		}
	</script>

</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<h1>DRG Codes Master</h1>
<insta:feedback-panel/>
<form name="DRGCodesForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


	<insta:search form="DRGCodesForm" optionsId="optionalFilter" closed="${hasResults}">

		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">DRG Code:</div>
				<div class="sboFieldInput">
				<input type="text" name="drg_code" value="${ifn:cleanHtmlAttribute(param.drg_code)}"/>
				<input type="hidden" name="drg_code@op" value="ico"/>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >

			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">DRG Code Description:</div>
						<div class="sfField">
							<input type="text" name="drg_description" value="${ifn:cleanHtmlAttribute(param.drg_description)}"/>
							<input type="hidden" name="drg_description@op" value="ico"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Patient Type:</div>
						<div class="sfField">
							<insta:checkgroup name="patient_type" selValues="${paramValues.patient_type}"
							opvalues="I,O" optexts="IP,OP"/>
							<input type="hidden" name="patient_type@op" value="in"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Status:</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
						</div>
					</td>

					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>
</form>


<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="drg_code" title="DRG Code"/>
			<insta:sortablecolumn name="drg_description" title="DRG Description"/>
			<th>Patient Type</th>
			<th>Relative Weight</th>			
			<th>Type</th>
			<insta:sortablecolumn name="hcpcs_portion_per" title="HCPCS Portion %"/>			
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{drg_code: '${record.drg_code}'});" >

				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					${record.drg_code}
				</td>
				<td>
					<insta:truncLabel value="${record.drg_description}" length="50"/>
				</td>
				<td>
					<c:out value="${record.patient_type == 'I' ? 'IP' : 'OP'}"/>
				</td>
				<td>
					<insta:truncLabel value="${record.relative_weight}" length="30"/>
				</td>				
				<td>
					<insta:truncLabel value="${record.code_type}" length="15"/>
				</td>
				<td>
					<insta:truncLabel value="${record.hcpcs_portion_per}" length="15"/>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<c:if test="${empty pagedList.dtoList}"> <insta:noresults hasResults="${hasResults}"/> </c:if>

<c:url value="DRGCodesMaster.do" var="drgUrl">
	<c:param name="_method" value="add" />
</c:url>
<div class="screenActions" style="float:left"><a href="${drgUrl}">Add New DRG Code</a></div>

<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
	<div class="flagText">Inactive</div>
</div>
<br/>
<br/>

<div id="CollapsiblePanel1" class="CollapsiblePanel">
	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
		<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Update DRG Codes</div>
		<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
		<div class="clrboth"></div>
	</div>

	<table>
	<tr>
	<td>
	<table class="search">
		<tr>
			<th>Export/Import DRG Code Details</th>
		</tr>
		<tr>
			<td>
			<table>
				<tr>
					<td>Export: </td>
					<td>
						<form name="exportDRGCodesForm" action="DRGCodesMaster.do" method="GET"
								style="padding:0; margin:0">
							<div style="float: left">
								<input type="hidden" name="_method" value="exportDRGCodeDetailsToXls">
								<button type="submit" accesskey="E">
								<b><u>D</u></b>ownload</button>
							</div>
							<div style="float: left;white-space: normal">
								<img class="imgHelpText"
									 src="${cpath}/images/help.png"
									 title="Note: The export gives a XLS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
								</div>
							</div>
						</form>
					</td>
				</tr>

				<tr>
					<td>Import:</td>
					<td>
						<form name="uploadDrgCodesForm" action="DRGCodesMaster.do" method="POST"
								enctype="multipart/form-data" style="padding:0; margin:0">
							<input type="hidden" name="_method" value="importDRGCodeDetailsFromXls">
							<input type="file" name="xlsDRGCodesFile"  accept="<insta:ltext key="upload.accept.master"/>"/>
							<button type="button" accesskey="F" onclick="return doUpload('uploadDrgCodesForm')" >
							<b><u>U</u></b>pload</button>
						</form>
					</td>
				</tr>
			</table>
			</td>
		</tr>
	</table>
	</td>
	</tr>
	</table>
</div>
<script>
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:true});
</script>
</body>
</html>
