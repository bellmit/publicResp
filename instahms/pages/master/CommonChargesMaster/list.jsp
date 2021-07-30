<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
<head>
	<title>Other Charges - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

	<script type="text/javascript">
		var pa = new Array();
		var toolbar = {
		Edit: {
			title: "Edit",
			imageSrc: "icons/Edit.png",
			href: 'master/CommonChargesMaster.do?_method=show',
			onclick: null,
			description: "Edit Common Charge details"
			}
		};


		function init(){
			createToolbar(toolbar);
		}

		function selectAll(){
			var checked = document.updateform._all.checked;
			var len = document.commonChargesMasterForm._editChargeDetails.length;
			if(len == undefined){
				document.commonChargesMasterForm._editChargeDetails.checked = checked;
			}else{
			 	for (var i=0;i<len;i++){
			 		document.commonChargesMasterForm._editChargeDetails[i].checked = checked;
			 	}
			}
		}

		function doUpload() {
			if (document.importForm.uploadFile.value == '') {
				alert("Please browse and select a file to upload");
				return false;
			}
			document.importForm.submit();
		}

		function validateAll(){
			var len = document.commonChargesMasterForm._editChargeDetails.length;
			var checked = false;
			if (len == undefined){
				if(document.commonChargesMasterForm._editChargeDetails.checked){
					checked = true;
				}
			}else{
				for (var i=0;i<len;i++){
					if(document.commonChargesMasterForm._editChargeDetails[i].checked){
						checked = true;
						break;
					}
				}
			}

			var div = document.getElementById("chargeListInnerHtml");
			while (div.hasChildNodes())
				div.removeChild(div.firstChild);
			var length = document.commonChargesMasterForm._editChargeDetails.length;
			if(length == undefined){
				if(document.commonChargesMasterForm._editChargeDetails.checked ){
					checked = true;
					div.appendChild(makeHidden("_editChargeDetails", "", document.commonChargesMasterForm._editChargeDetails.value));
				}
			}else{
				for(var i=0;i<length;i++){
					if(document.commonChargesMasterForm._editChargeDetails[i].checked){
						checked = true;
						div.appendChild(makeHidden("_editChargeDetails", "", document.commonChargesMasterForm._editChargeDetails[i].value));
					}
				}
			}

			if(!checked){
				alert ("Atleast one has to be select for updation");
				return false;
			}

			if(document.updateform._varianceBy.value == "" && document.updateform._varianceValue.value == ""){
					alert("Rate Variance value is required ");
					document.updateform._varianceBy.focus();
					return false;
			}
			if(document.updateform._varianceBy.value > 100){
				alert("The percentage should not be grater than 100");
				document.updateform._varianceBy.focus();
				return false;
			}

			document.updateform.submit();
		}
</script>
</head>


<body onload="init()">

	<h1>Other Charges</h1>
	<insta:feedback-panel/>

<form name="commonChargesMasterForm" method="get"  action="CommonChargesMaster.do">
	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search-lessoptions form="commonChargesMasterForm">
		<table class="searchBasicOpts" >
			<tr>
				<td class="sboField">
					<div class="sboFieldLabel">Charge Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="charge_name" value="${ifn:cleanHtmlAttribute(param.charge_name)}">
						<input type="hidden" name="charge_name@op" value="ico" />
					</div>
				</td>
				<td class="sboField" style="height:80px">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in" />
					</div>
				</td>
				<td class="sboField" style="height:80px">
					<div class="sboFieldLabel">Service Sub Group:</div>
					<div class="sboFieldInput">
						<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value="${param.service_sub_group_id}"
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />
							<input type="hidden" name="service_sub_group_id@type" value="integer" />
					</div>
				</td>
			</tr>
		</table>

		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>


		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>Select</th>
					<th>Charge Name</th>
					<th>Charge Type</th>
					<th>Charge Group</th>
					<th>Charge</th>
				</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<script>
					pa[${st.index}] = {};
					pa[${st.index}].charge_name = <insta:jsString value="${record.charge_name}"/>;
				</script>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{charge_name: pa[${st.index}].charge_name },'');"
					id="toolbarRow${st.index}">
					<td><input type="checkbox" name="_editChargeDetails" onclick=""  value="${record.charge_name}">
					<td>
						<c:if test="${record.status eq 'I'}"><img src="${cpath}/images/grey_flag.gif"></c:if>
						<c:if test="${record.status eq 'A'}"><img src="${cpath}/images/empty_flag.gif"></c:if>
						${record.charge_name}
					</td>
					<td>${record.chargehead_name }</td>
					<td>${record.chargegroup_name }</td>
					<td>${record.charge}</td>
				</tr>
			</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>

		<c:url var="url" value="CommonChargesMaster.do">
			<c:param name="_method" value="add"></c:param>
		</c:url>

		<div class="screenActions" style="padding-bottom:10px">
			<a href="<c:out value='${url}'/>">Add New Charge</a>
			<div class="legend" style="display:${hasResults? 'block' : 'none'};" >
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText">Inactive</div>
			</div>
		</div>
	</form>
	<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
			<div class="clrboth"></div>
		</div>
		<table>
			<tr>
				<td valign="top">
				<form name="updateform" action="CommonChargesMaster.do" method="POST">
					<input type="hidden" name="_method" value="groupUpdate">
					<div style="display:none" id="chargeListInnerHtml">
						<%-- this holds the hidden inputs for the list of common charges --%>
					</div>
					<table class="search">
						<tr>
							<th>
								Select All:
								<input type="checkbox" name="_all" onclick="selectAll();">
							</th>
						</tr>
						<tr>
							<td>Rate Variance</td>
						</tr>
						<tr>
							<td>
								<insta:selectoptions name="_varianceType" value="Incr" opvalues="Incr,Dscr" optexts="Increase By,Decrese By" style="width :9em"/>
							</td>
							<td>
								<input type="text" name="_varianceBy" class="number" onkeypress="return enterNumAndDot(event);">%
								<input type="text" name="_varianceValue" class="number" onkeypress="return enterNumAndDot(event);">${currType}
							</td>
						</tr>
					</table>
				</form>
				</td>
				<td width="200px"></td>
				<td valign="top" align="center">
					<table class="search" style="padding-left: 3em">
						<tr>
							<th colspan="2">Export/Import Other Charges Details</th>
						</tr>
						<tr>
							<td>Export: </td>
							<td>
								<form name="exportForm" action="CommonChargesMaster.do" method="GET"
									style="padding:0; margin:0">
									<div style="float: left">
										<input type="hidden" name="_method" value="exportMaster">
										<button type="submit">Download</button>
									</div>
									<div style="float: left;white-space: normal">
										<img class="imgHelpText"
										src="${cpath}/images/help.png"
										title="Note: The export gives a CSV file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
									</div>
								</form>
							</td>
						</tr>

						<tr>
							<td>Import:</td>
							<td>
								<form name="importForm" action="CommonChargesMaster.do" method="POST"
									enctype="multipart/form-data">
									<input type="hidden" name="_method" value="importMaster">
									<input type="file" name="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
									<button type="button" onclick="return doUpload()">Upload</button>
								</form>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<div class="screenActions" style="float:left"><button type="submit" accesskey="U" onclick="return validateAll();">
			 			<b><u>U</u></b>pdate Charges</button>
					</div>
				</td>
			</tr>
		</table>
	</div>
	</div>
	<script>
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
		var cpath = '${cpath}';
	</script>
</body>
</html>
