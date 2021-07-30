<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Dental Supplier Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagepath" value="<%= URLRoute.DENTAL_SUPPLIER_MASTER %>" />
	
	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagepath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Dental Supplier details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.DentalSupplierSearchForm);
			autoSuppliers();
		}
		
		var suppliers = ${ifn:convertListToJson(suppliersList)};
		var sAutoComp;
		function autoSuppliers() {
			var datasource = new YAHOO.util.LocalDataSource({result: suppliers});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "supplier_name"},{key : "supplier_id"} ]
			};
			var sAutoComp = new YAHOO.widget.AutoComplete('supplier_name','suppliercontainer', datasource);
			sAutoComp.minQueryLength = 0;
		 	sAutoComp.maxResultsDisplayed = 20;
		 	sAutoComp.forceSelection = false ;
		 	sAutoComp.animVert = false;
		 	sAutoComp.resultTypeList = false;
		 	sAutoComp.typeAhead = false;
		 	sAutoComp.allowBroserAutocomplete = false;
		 	sAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			sAutoComp.autoHighlight = true;
			sAutoComp.useShadow = false;
		 	if (sAutoComp._elTextbox.value != '') {
					sAutoComp._bItemSelected = true;
					sAutoComp._sInitInputValue = sAutoComp._elTextbox.value;
			}
		}

	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Dental Supplier Master List</h1>

	<insta:feedback-panel/>
	
	<form name="DentalSupplierSearchForm" method="GET">

		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:find form="DentalSupplierSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Supplier Name</div>
					<div class="sboFieldInput">
						<input type="text" name="supplier_name" id="supplier_name" value="${ifn:cleanHtmlAttribute(param.supplier_name)}">
						<input type="hidden" name="supplier_name@op" value="ico" />
						<div id="suppliercontainer" style = "width:20em"></div>
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Supplier Phone1:</div>
							<div class="sfField">
								<input type="text" name="supplier_phone1" value="${ifn:cleanHtmlAttribute(param.supplier_phone1)}">
								<input type="hidden" name="supplier_phone1@op" value="ilike" />
							</div>
							<div class="sfLabel">Supplier Fax:</div>
							<div class="sfField">
								<input type="text" name="supplier_fax" value="${ifn:cleanHtmlAttribute(param.supplier_fax)}">
								<input type="hidden" name="supplier_fax@op" value="ilike" />
							</div>
							<div class="sfLabel">Contact Person:</div>
							<div class="sfField">
								<input type="text" name="contact_person_name" value="${ifn:cleanHtmlAttribute(param.contact_person_name)}">
								<input type="hidden" name="contact_person_name@op" value="ilike" />
							</div>
							<div class="sfLabel">Contact Pesron Mail:</div>
							<div class="sfField">
								<input type="text" name="contact_person_mailid" value="${ifn:cleanHtmlAttribute(param.contact_person_mailid)}">
								<input type="hidden" name="contact_person_mailid@op" value="ilike" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Supplier Phone2:</div>
							<div class="sfField">
								<input type="text" name="supplier_phone2" value="${ifn:cleanHtmlAttribute(param.supplier_phone2)}">
								<input type="hidden" name="supplier_phone2@op" value="ilike" />
							</div>
							<div class="sfLabel">Supplier Mail:</div>
							<div class="sfField">
								<input type="text" name="supplier_mailid" value="${ifn:cleanHtmlAttribute(param.supplier_mailid)}">
								<input type="hidden" name="supplier_mailid@op" value="ilike" />
							</div>
							<div class="sfLabel">Contact Person Mobile:</div>
							<div class="sfField">
								<input type="text" name="contact_person_mobile_number" value="${ifn:cleanHtmlAttribute(param.contact_person_mobile_number)}">
								<input type="hidden" name="contact_person_mobile_number@op" value="ilike" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['status']}"/>
								<input type="hidden" name="status@op" value="in" />
							</div>
							<div class="sfLabel">Supplier Website:</div>
							<div class="sfField">
								<input type="text" name="supplier_website" value="${ifn:cleanHtmlAttribute(param.supplier_website)}">
								<input type="hidden" name="supplier_website@op" value="ilike" />
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:find>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="supplier_name" title="Supplier Name"/>
					<th>Supplier Address</th>
					<th>Supplier Phone1</th>
					<th>Supplier Fax</th>
					<th>Supplier Mail</th>
					<th>Supplier Website</th>
					<th>Contact Person</th>
					<th>Contact Person Mobile</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{supplier_id: '${record.supplier_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if> ${record.supplier_name}
						</td>
						<td><insta:truncLabel value="${record.supplier_address}" length="15"/></td>
						<td>${record.supplier_phone1}</td>
						<td>${record.supplier_fax}</td>
						<td>${record.supplier_mailid}</td>
						<td>${record.supplier_website}</td>
						<td>${record.contact_person_name}</td>
						<td>${record.contact_person_mobile_number}</td>
					</tr>

				</c:forEach>

			</table>

			<insta:noresults hasResults="${hasResults}"/>

		</div>

		<c:url var="url" value="${pagepath}/add.htm"></c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add Supplier</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>