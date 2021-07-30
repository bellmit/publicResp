<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%--
  This page is to be used as the model page for all Filtered List pattern UI screens.
	Conventions/rules are noted in comments using NOTE: as the title. Some more notes can
	also be found in bill_list.js
--%>

<html>
<head>
	<title><insta:ltext key="billing.billlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<insta:js-bundle prefix="billing.billlist"/>
	<script>
		/*
		 * NOTE:  these are used in bill_list.js to initialize the toolbar, which is why they must
		 * appear BEFORE including bill_list.js. Depending on rights, we will show/not show the order
		 * and cancel menu items in the context menu.
		 */
		var toolbarOptions = getToolbarBundle("js.billing.billlist.toolbar");
		var opOrder = '${urlRightsMap.new_op_order}';
		var ipOrder = '${urlRightsMap.new_ip_order}';
		var mod_billing_ext = '${preferences.modulesActivatedMap.mod_billing_ext}';
		var issueRights = '${urlRightsMap.patient_inventory_issue}';
		var changeRatePlanRights = '${urlRightsMap.change_visit_org}';
		var bedNames=${bedNames};
		var extraDetails = [];
		var billLaterDefaultPrint = '${genPrefs.billNowPrintDefault}';
		var billNowDefaultPrint = '${genPrefs.billNowPrintDefault}';
		var userNameInBillPrint = '${genPrefs.userNameInBillPrint}';
		var roleId = '${roleId}';
		var billPrintRights = '${urlRightsMap.bill_print}';
		var centerId = ${centerId};
		var max_centers_inc_default = ${max_centers_inc_default};
		var sampleCollectionCenterId = ${sampleCollectionCenterId};
		var patientSponsorCreditNotePath = '${urlRightsMap.create_patient_credit_note == "A" ? "PatientCreditNote" : "SponsorCreditNote"}';
		var allowcreditbilllater = '${actionRightsMap.allow_credit_bill_later}'
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="billing/bill_list.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<insta:js-bundle prefix="billing.dynapackage"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="billList" value="${pagedList.dtoList}"/>
<c:set var="billPrintRights" value="${urlRightsMap.bill_print}"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

<%-- some convenience variables initialized here for display purpose --%>
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="P" value="BN"/>
<c:set target="${typeDisplay}" property="C" value="BL"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="claimStatusDisplay" class="java.util.HashMap"/>
<c:set target="${claimStatusDisplay}" property="O" value="Open"/>
<c:set target="${claimStatusDisplay}" property="B" value="Batched"/>
<c:set target="${claimStatusDisplay}" property="M" value="For Resub."/>
<c:set target="${claimStatusDisplay}" property="C" value="Closed"/>

<jsp:useBean id="paymentStatusDisplay" class="java.util.HashMap"/>
<c:set target="${paymentStatusDisplay}" property="U" value="Unpaid"/>
<c:set target="${paymentStatusDisplay}" property="P" value="Paid"/>

<%--
  NOTE: hasResults indicates whether the search fetched any results or not. If no results
	were fetched, the screen defaults to showing "More Options >>" as well as an indicator
	saying no results were fetched
--%>
<c:set var="hasResults" value="${not empty billList}"/>

<body onload="init(); showFilterActive(document.BillSearchForm)">

<c:set var="billtype">
<insta:ltext key="billing.billlist.list.billnow"/>,
<insta:ltext key="billing.billlist.list.billlater"/>
</c:set>

<c:set var="creditnotetype">
    <insta:ltext key="billing.billlist.list.included"/>,
    <insta:ltext key="billing.billlist.list.notincluded"/>
</c:set>

<c:set var="restrictiontype">
<insta:ltext key="billing.billlist.list.hospital"/>,
<insta:ltext key="billing.billlist.list.pharmacy"/>,
<insta:ltext key="billing.billlist.list.incomingtest"/>
</c:set>
<c:set var="submissionbatch">
<insta:ltext key="billing.billlist.list.included"/>,
<insta:ltext key="billing.billlist.list.notincluded"/>
</c:set>
<c:set var="billstatus">
<insta:ltext key="billing.billlist.list.open"/>,
<insta:ltext key="billing.billlist.list.finalized"/>,
<insta:ltext key="billing.billlist.list.closed"/>,
<insta:ltext key="billing.billlist.list.cancelled"/>
</c:set>
<c:set var="pymtstatus">
<insta:ltext key="billing.billlist.list.unpaid"/>,
<insta:ltext key="billing.billlist.list.paid"/>
</c:set>
<c:set var="visittype">
<insta:ltext key="billing.billlist.list.ip"/>,
<insta:ltext key="billing.billlist.list.op"/>,
<insta:ltext key="billing.billlist.list.pharmacy"/>,
<insta:ltext key="billing.billlist.list.incomingtest"/>
</c:set>
<c:set var="visitstatus">
<insta:ltext key="billing.billlist.list.active"/>,
<insta:ltext key="billing.billlist.list.inactive"/>
</c:set>
<c:set var="optype">
<insta:ltext key="billing.billlist.list.mainvisit"/>,
<insta:ltext key="billing.billlist.list.followup.with.consultation"/>,
<insta:ltext key="billing.billlist.list.followup.without.consultation"/>,
<insta:ltext key="billing.billlist.list.revisit"/>,
<insta:ltext key="billing.billlist.list.outside"/>
</c:set>
<c:set var="istpa">
<insta:ltext key="billing.billlist.list.insurancebills"/>,
<insta:ltext key="billing.billlist.list.noninsurancebills"/>
</c:set>
<c:set var="primaryclaimstatus">
<insta:ltext key="billing.billlist.list.open"/>,
<insta:ltext key="billing.billlist.list.batched"/>,
<insta:ltext key="billing.billlist.list.forResub"/>,
<insta:ltext key="billing.billlist.list.closed"/>,
<insta:ltext key="billing.billlist.list.notapplicable"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="all">
<insta:ltext key="billing.billlist.list.all.in.brackets"/>
</c:set>
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
<insta:ltext key="billing.billlist.list.visitid"/>
</c:set>
<c:set var="billno">
<insta:ltext key="billing.billlist.list.billno"/>
</c:set>
<c:set var="type">
<insta:ltext key="billing.billlist.list.type"/>
</c:set>
<c:set var="status">
<insta:ltext key="billing.billlist.list.status"/>
</c:set>
<c:set var="opendate">
<insta:ltext key="billing.billlist.list.opendate"/>
</c:set>
<c:set var="paymentstatus">
<insta:ltext key="billing.billlist.list.paymentstatus"/>
</c:set>
<c:set var="pmtstatus">
<insta:ltext key="billing.billlist.list.pmtstatus"/>
</c:set>
<c:set var="rateplan">
<insta:ltext key="billing.billlist.list.rateplan"/>
</c:set>
<c:set var="primarytpasponser">
<insta:ltext key="billing.billlist.list.tpa.or.sponsor"/>
</c:set>
<c:set var="claim">
<insta:ltext key="billing.billlist.list.claim"/>
</c:set>
<c:set var="claimstatus">
<insta:ltext key="billing.billlist.list.claimstatus"/>
</c:set>
<c:set var="finalizeddate">
<insta:ltext key="billing.billlist.list.finalizeddate"/>
</c:set>
<c:set var="totalpaidamount">
<insta:ltext key="billing.billlist.list.totalpaidamount"/>
</c:set>
<c:set var="totaldueamount">
<insta:ltext key="billing.billlist.list.totaldueamount"/>
</c:set>
<c:set var="patientpaidamount">
<insta:ltext key="billing.billlist.list.patientpaidamount"/>
</c:set>
<c:set var="patientdue">
<insta:ltext key="billing.billlist.list.patientdue"/>
</c:set>
<c:set var="sponsoramount">
<insta:ltext key="billing.billlist.list.sponsoramount"/>
</c:set>

<c:set var="notdischarged">
	<insta:ltext key="patient.discharge.status.common.notdischarged"/>
</c:set>
<c:set var="dischargeInitiated">
	<insta:ltext key="patient.discharge.status.common.dischargeinitiated"/>
</c:set>
<c:set var="clinicalDischarge" >
	<insta:ltext key="patient.discharge.status.common.clinicaldischarge"/>
</c:set>
<c:set var="financialDischarge">
	<insta:ltext key="patient.discharge.status.common.financialdischarge"/>
</c:set>
<c:set var="physicalDischarge">
	<insta:ltext key="patient.discharge.status.common.physicaldischarge"/>
</c:set>

<jsp:useBean id="dischargeStatusMap" class="java.util.HashMap"/>
<c:set target="${dischargeStatusMap}" property="N" value="${notdischarged}"/>
<c:set target="${dischargeStatusMap}" property="I" value="${dischargeInitiated}"/>
<c:set target="${dischargeStatusMap}" property="C" value="${clinicalDischarge}"/>
<c:set target="${dischargeStatusMap}" property="F" value="${financialDischarge}"/>
<c:set target="${dischargeStatusMap}" property="D" value="${physicalDischarge}"/>

<c:choose><c:when test="${not empty param._title}">
	<h1>${ifn:cleanHtml(param._title)}</h1>
</c:when><c:otherwise>
	<h1><insta:ltext key="billing.billlist.list.billlist"/></h1>
</c:otherwise></c:choose>

<insta:feedback-panel/>

<form name="BillSearchForm" method="GET" >

	<%-- NOTE: the "method" parameter name has to be _method for Save Searches to work correctly --%>
	<input type="hidden" name="_method" value="getBills">
	<%-- NOTE: _searchMethod is used by the Save Search functionality.
		Set this to the method that is to be called for doing the search --%>
	<input type="hidden" name="_searchMethod" value="getBills"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}" />
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}" />

	<%--
		NOTE: Use the insta:search tag to write out the Search filter. The following are to be supplied
		to the tag:
		* form: name of the form which has the search fields
		* optionsId: the id of the div containing optional search fields (full set, excluding the "basic" ones)
		* closed: whether the optional fields have to be shown closed (true if any results are being shown)
		* <body>: Inside the search tag, place two sections:
			* Basic fields (one or two max) that are usually used for the search. Typically the primary key and
			  the MR No.
			* Optional fields inside a separate div, the ID of which is passed to insta:search
	--%>
	<insta:search form="BillSearchForm" optionsId="optionalFilter" closed="${hasResults}"
		validateFunction="doSearch()" clearFunction="clearSearch">
		<%-- Basic fields to contain only MR No (autocomplete) and Bill No for quick access --%>
		<div class="searchBasicOpts" >

			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="billing.billlist.list.mrno.or.patientname"/>:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>

			<div class="sboField">
				<div class="sboFieldLabel">&nbsp;
					<div class="sboFieldInput">
						<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changeStatus()"/><insta:ltext key="search.patient.visit.active.only"/>
					</div>
				</div>
			</div>

			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="billing.billlist.list.billno"/>:</div>
				<div class="sboFieldInput">
					<input type="text" name="bill_no" value="${ifn:cleanHtmlAttribute(param.bill_no)}" />
					<input type="hidden" name="bill_no@op" value="ilike">
				</div>
			</div>

			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="billing.billlist.list.claimid"/>:</div>
				<div class="sboFieldInput">
					<input type="text" name="claim_id" value="${ifn:cleanHtmlAttribute(param.claim_id)}" />
					<input type="hidden" name="claim_id@op" value="ilike">
				</div>
			</div>
		</div>

		<%-- NOTE: All the other filterable fields can be collapsed/shown using 'More Options >>'.
			The id of this needs to be passed to insta:search constructor. Things to note here:
			* The optional filter can have a max of 5 columns. If there are more fields to be
				filtered on, put them one below the other in a single column. Example, open and finalized dates.
			* The 5 columns are in the form of table/tds
			* The fields are laid out in the form of divs.
			* The field names must match the database field names so that SearchQueryBuilder can build the
				query directly from the request map. Use @op and @type hidden fields to give SearchQueryBuilder
				extra information about the fields. (See comments in SearchQueryBuilder.java for details)
		--%>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.opendate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="billing.billlist.list.from"/>:</div>
							<insta:datewidget name="open_date" id="open_date0" value="${paramValues.open_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="billing.billlist.list.to"/>:</div>
							<insta:datewidget name="open_date" id="open_date1" value="${paramValues.open_date[1]}"/>
							<%-- NOTE: tell the query-builder to use >= and <= operators for the dates --%>
							<input type="hidden" name="open_date@op" value="ge,le"/>
							<input type="hidden" name="open_date@cast" value="y"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.finalizeddate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="billing.billlist.list.from"/>:</div>
							<%-- NOTE: datewidget requires that each date field have a unique id --%>
							<insta:datewidget name="finalized_date" id="f_date0" value="${paramValues.finalized_date[0]}"/>
							<%--
								NOTE: this is not really required. Since the field name ends with _date,
								SearchQueryBuilder automatically figures out that it is a date field.
								But, specifying the type explicitly is also OK.
							--%>
							<input type="hidden" name="finalized_date@type" value="date"/>
							<%--
								NOTE: if multiple fields with the same name are present, we can specify the operator either
								as one field (comma separated like "ge,le" for open_date), or individually for each field
								as multiple fields with the same name.
							--%>
							<input type="hidden" name="finalized_date@op" value="ge"/>
							<input type="hidden" name="finalized_date@cast" value="y"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="billing.billlist.list.to"/>:</div>
							<insta:datewidget name="finalized_date" id="f_date1" value="${paramValues.finalized_date[1]}"/>
							<input type="hidden" name="finalized_date@op" value="le"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.bedtype"/></div>
						<div class="sfField">
							<select name="bed_type" class="dropdown" onchange="clearFields(this);initBedNames(this)">
								<option value=""><insta:ltext key="billing.billlist.list.all.in.brackets"/></option>
								<c:forEach items="${bedTypes}" var="bed">
									<option value="${bed.BED_TYPE}" ${bed.BED_TYPE==param.bed_type?'selected':''}>${bed.BED_TYPE}</option>
								</c:forEach>
							</select>
							<input type="hidden" name="bed_type@type" value="text"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.bedname.ward"/></div>
						<div class="sfField">
							<div id="bedNamesDiv"  style="padding-bottom:10px;">
								<input type="text" name="_bed_ward_name" id="_bed_ward_name" value="${ifn:cleanHtmlAttribute(param._bed_ward_name)}" />
								<div id="bedNamesDropDown"></div>
							</div>
							<input type="hidden" name="bed_name" value="${ifn:cleanHtmlAttribute(param.bed_name)}" />
							<input type="hidden" name="ward_name" value="${ifn:cleanHtmlAttribute(param.ward_name)}" />
						</div>
					</td>

					<td>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.billtype"/></div>
						<div class="sfField">
							<%--
								NOTE: No need to specify type and operator if type is string (or can be auto-detected) and
								operator is =
							--%>
							<insta:checkgroup name="bill_type" selValues="${paramValues.bill_type}"
							opvalues="P,C" optexts="${billtype}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.usagetype"/></div>
						<div class="sfField">
							<insta:checkgroup name="restriction_type" selValues="${paramValues.restriction_type}"
							opvalues="N,P,T" optexts="${restrictiontype}"/>
						</div>
						<c:if test="${not empty preferences.modulesActivatedMap['mod_adv_ins']
										&& preferences.modulesActivatedMap['mod_adv_ins'] == 'Y'}">
							<div class="sfLabel"><insta:ltext key="billing.billlist.list.submissionbatch"/></div>
							<div class="sfField">
								<insta:checkgroup name="submission_batch_id" selValues="${paramValues.submission_batch_id}"
								opvalues="Y,N" optexts="${submissionbatch}"/>
							</div>
						</c:if>
						
						<!-- Credit Note search bar -->
						<div class="sfLabel"><insta:ltext key="js.billing.billlist.creditnote"/></div>
						<div class="sfField">
							<insta:checkgroup name="creditnote" opvalues="Y,N" selValues="${paramValues.creditnote}"
							optexts="${creditnotetype}"/>
						</div>
					</td>

					<td>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="A,F,C,X" optexts="${billstatus}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.paymentstatus"/></div>
						<div class="sfField">
							<insta:checkgroup name="payment_status" selValues="${paramValues.payment_status}"
							opvalues="U,P" optexts="${pymtstatus}"/>
						</div>
						<c:if test="${sampleCollectionCenterId == -1}">
							<div class="sfLabel"><insta:ltext key="billing.billlist.list.collectioncenter"/>:</div>
							<div class="sfField">
								<c:choose>
									<c:when test="${max_centers_inc_default > 1 && centerId != 0}">
											<select name="collectionCenterId" id="collectionCenterId" class="dropdown">
													<option value="">${dummyvalue}</option>
													<option value="-1" ${param.collectionCenterId == -1?'selected':''}>${defautlCollectionCenter}</option>
												<c:forEach items="${collectionCenters}" var="col_Centers">
													<option value="${col_Centers.map.collection_center_id}" ${col_Centers.map.collection_center_id == param.collectionCenterId?'selected':''}>
														${col_Centers.map.collection_center}
													</option>
												</c:forEach>
											</select>
									</c:when>
									<c:otherwise>
										<insta:selectdb id="collectionCenterId"  name="collectionCenterId"
									value="${param.collectionCenterId}" table="sample_collection_centers"
									valuecol="collection_center_id" displaycol="collection_center" dummyvalue="${dummyvalue}"/>
									</c:otherwise>
								</c:choose>
							</div>
						</c:if>
						<div class="sfLabel"><insta:ltext key="patient.discharge.status.common.dischargestatus" />:</div>
						<div class="sfField">
							<insta:checkgroup name="patient_discharge_status" selValues="${paramValues.patient_discharge_status}"
								opvalues="N,I,C,F,D" optexts="${notdischarged},${dischargeInitiated},${clinicalDischarge},${financialDischarge},${physicalDischarge}"/>
						</div>
					</td>

					<td>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.patienttype"/></div>
						<div class="sfField">
							<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
							opvalues="i,o,r,t" optexts="${visittype}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.visitstatus"/>:</div>
						<div class="sfField">
								 <insta:checkgroup name="visit_status" selValues="${paramValues.visit_status}"
								 opvalues="A,I" optexts="${visitstatus}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.optype"/></div>
						<div class="sfField">
							<insta:checkgroup name="op_type" selValues="${paramValues.op_type}"
							opvalues="M,F,D,R,O" optexts="${optype}"/>
						</div>
					</td>

					<td class="last">
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.primarysponsor"/></div>
						<div class="sfField">
							<insta:selectdb name="primary_sponsor_id" table="tpa_master" valuecol="tpa_id" class="dropdown"
							displaycol="tpa_name" value="${param.primary_sponsor_id}" dummyvalue="${all}" orderby="tpa_name"/>
						</div>
						<c:if test="${corpInsurance ne 'Y'}">
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.secondarysponsor"/></div>
						<div class="sfField">
							<insta:selectdb name="secondary_sponsor_id" table="tpa_master" valuecol="tpa_id" class="dropdown"
							displaycol="tpa_name" value="${param.secondary_sponsor_id}" dummyvalue="${all}" orderby="tpa_name"/>
						</div>
						</c:if>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.billinsurance"/></div>
						<div class="sfField">
							<insta:checkgroup name="is_tpa" selValues="${paramValues.is_tpa}"
							opvalues="true,false" optexts="${istpa}"/>
							<input type="hidden" name="is_tpa@cast" value="y"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.rateplan"/></div>
						<div class="sfField">
							<insta:selectdb name="org_id" table="organization_details" valuecol="org_id" class="dropdown"
							displaycol="org_name" orderby="org_name" value="${param.org_id}" dummyvalue="${all}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="billing.billlist.list.claimstatus"/></div>
						<div class="sfField">
							<insta:checkgroup name="primary_claim_status" selValues="${paramValues.primary_claim_status}"
							opvalues="O,B,M,C,N" optexts="${primaryclaimstatus}"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<%-- NOTE: insta:paginate automatically shows the pagination --%>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" showTooltipButton="true"/>

	<%-- NOTE: use the resultList class for common settings and look-and-feel --%>
	<c:if test="${not empty billList && (billPrintRights == 'A' || roleId == '1' || roleId == '2')}">
		<div align="right">
			<insta:selectdb name="_printer" table="printer_definition" valuecol="printer_id"
			displaycol="printer_definition_name" orderby="printer_definition_name"  value="${genPrefs.default_printer_for_bill_now}" />
		</div>
	</c:if>
	<div class="resultList">
		<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th style="text-align: right">#</th>
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="visit_id" title="${visitid}"/>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<insta:sortablecolumn name="bill_no" title="${billno}"/>
				<insta:sortablecolumn name="bill_type" title="${type}"/>
				<insta:sortablecolumn name="status" title="${status}"/>
				<insta:sortablecolumn name="open_date" title="${opendate}"/>
				<th align="right"><insta:ltext key="billing.billlist.list.billamt"/></th>
				<th align="right"><insta:ltext key="billing.billlist.list.taxamt"/></th>
				<th align="right"><insta:ltext key="billing.billlist.list.patamt"/></th>
				<th align="right"><insta:ltext key="billing.billlist.list.pattax"/></th>
				<insta:sortablecolumn name="payment_status" title="${pmtstatus}" tooltip="${paymentstatus}"/>
				<th align="right" title="${totalpaidamount}"><insta:ltext key="billing.billlist.list.totpaid"/></th>
				<th align="right" title="${totaldueamount}"><insta:ltext key="billing.billlist.list.totdue"/></th>
				<insta:sortablecolumn name="org_name" title="${rateplan}"/>
				<insta:sortablecolumn name="primary_tpa_name" title="${primarytpasponser}"/>
				<th align="right" title="${patientpaidamount}"><insta:ltext key="billing.billlist.list.patpaid"/></th>
				<th align="right" title="${patientdue}"><insta:ltext key="billing.billlist.list.patdue"/></th>
				<th align="right" title="${sponseramount}"><insta:ltext key="billing.billlist.list.spnramt"/></th>
				<th align="right"><insta:ltext key="billing.billlist.list.spnrtax"/></th>
				<insta:sortablecolumn name="primary_claim_status" title="${claim}" tooltip="${claimstatus}"/>
				<th align="right"><insta:ltext key="billing.billlist.list.spnrpmt"/></th>
				<th align="right"><insta:ltext key="billing.billlist.list.spnrdue"/></th>
				<insta:sortablecolumn name="finalized_date" title="${finalizeddate}"/>
				<th><insta:ltext key="patient.discharge.status.common.dischargestatus"/></th>
			</tr>

			<c:forEach var="bill" items="${billList}" varStatus="st">
				<%-- NOTE:
					Calculate enabled/disabled status for each toolbar menu item, to be passed to showToolbar function
					onclick of the tr. Enable/disable depends on values of each row.
				--%>
				<c:set var="typeChangeEnabled"
						value="${(bill.visit_type != 'r') && (bill.bill_type == 'P') && (bill.status != 'C') && (bill.status != 'X')}"/>

				<c:choose>
					<c:when test="${((bill.visit_type == 'i')||(bill.visit_type == 'o')) && (bill.restriction_type == 'P')}">
						<c:set var="typeChangeEnabled"
							value="${typeChangeEnabled && (not empty seperate_pharmacy_credit) && (seperate_pharmacy_credit == 'Y')}"/>
					</c:when>
					<c:otherwise></c:otherwise>
				</c:choose>

				<c:set var="orderEnabled" value="${bill.status == 'A' && bill.visit_status=='A' &&
					bill.restriction_type=='N'}"/>

				<c:set var="editRatePlanEnabled" value="${bill.status == 'A' && bill.restriction_type=='N'}"/>

				<c:set var="tpaConnectEnabled" value="${bill.status == 'A' && not empty bill.primary_tpa_name}"/>
				<c:set var="issueEnabled" value="${bill.status == 'A' && bill.restriction_type=='N' && bill.visit_status=='A'}"/>
				<c:set var="togglePrimaryEnabled" value="${bill.restriction_type == 'N' && bill.bill_type == 'C'}"/>
				<c:set var="editClaimEnabled" value="${bill.status == 'A' && not empty bill.primary_tpa_name && bill.is_tpa eq true}"/>

				<%-- NOTE: Calculate the colors of the flags based on status/type of the bill --%>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${bill.status == 'C'}">grey</c:when>
						<c:when test="${bill.status == 'X'}">yellow</c:when>
						<c:when test="${(bill.bill_type == 'P' || bill.bill_type == 'C') && bill.status == 'A' && not empty bill.bill_label_id}">blue</c:when>
						<c:when test="${bill.bill_type == 'P' && bill.status == 'A' && bill.payment_status == 'U'}">red</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>

				<c:set var="printDefaultEnabled" value="true"/>
				<c:set var="printOtherEnabled" value="true"/>
				<c:if test="${not empty preferences.modulesActivatedMap['mod_ins_ext']
										&& preferences.modulesActivatedMap['mod_ins_ext'] == 'Y'}">
					<c:set var="orderEnabled" value="false" />
					<c:set var="issueEnabled" value="false" />
					<c:set var="typeChangeEnabled" value="false" />
					<c:set var="togglePrimaryEnabled" value="false" />
					<c:set var="tpaConnectEnabled" value="false" />
					<c:set var="editRatePlanEnabled" value="false" />
					<c:set var="editClaimEnabled" value="false" />
					<c:set var="printDefaultEnabled" value="false"/>
					<c:set var="printOtherEnabled" value="false"/>
				</c:if>
				
				<c:set var="creditNoteAcressRight">
					<c:choose>
						<c:when test="${ifn:afmt(bill.total_claim) eq ifn:afmt(0) && (bill.patient_writeoff == 'A' || urlRightsMap.create_patient_credit_note != 'A')}">
							false
						</c:when>
						<c:when test="${ifn:afmt(bill.total_amount - bill.total_claim) eq ifn:afmt(0) && (bill.sponsor_writeoff == 'N' || bill.sponsor_writeoff == 'A' || urlRightsMap.create_sponsor_credit_note != 'A')}">
							false
						</c:when>
						 <c:when test="${bill.restriction_type == 'P'}">
							false
						</c:when>
						<c:when test="${bill.patient_writeoff == 'A' && (bill.sponsor_writeoff == 'N' || bill.sponsor_writeoff == 'A')}">
							false
						</c:when>
						<c:when test="${bill.patient_writeoff == 'A' && urlRightsMap.create_sponsor_credit_note != 'A'}">
							false
						</c:when>
						<c:when test="${bill.patient_writeoff == 'A' && bill.is_tpa == 'f'}">
							false
						</c:when> 
						<c:when test="${(bill.sponsor_writeoff == 'N' || bill.sponsor_writeoff == 'A') && urlRightsMap.create_patient_credit_note != 'A'}">
							false
						</c:when>
						<c:when test="${urlRightsMap.create_patient_credit_note == 'A' || urlRightsMap.create_sponsor_credit_note == 'A'}">
							true
						</c:when>
					</c:choose>
				</c:set>
				<!-- Create Credit Note if status is closed or finalized and net amount > 0  -->
				<c:set var="createCreditNote">
					<c:if test="${ (bill.status  == 'C' || bill.status == 'F') && bill.total_amount > 0 && creditNoteAcressRight == 'true'}">
						true
					</c:if>
				</c:set>
				
				<!-- View Credit Note -->
				<c:set var="viewCreditNote">
					<c:if test="${(bill.status  == 'C' || bill.status == 'F') && bill.total_amount < 0 && bill.restriction_type != 'P'}">
						true
					</c:if>
				</c:set>
				<%-- NOTE:
					* The tr class has to include firstRow for the first row, and odd/even based on odd/even.
					* onclick, showToolbar parameters: 1. index, 2. event, 3. id of this table.
						4. Parameters to be passed in the request to the hyperlinks in the menu
						5. Array of which menu items are enabled/disabled. true is enabled, false is disabled.
						   (pass null if all menus are always enabled).
					* See scripts/ToolBar.js and bill_list.js for more details.
				--%>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'} ${bill.mlc_status=='Y' ? 'mlcRow' : '' }"
					onclick="updateCreditNotePath(this);showToolbar(${st.index}, event, 'resultTable',
						{mrNo: '${bill.mr_no}', billNo: '${bill.bill_no}', visitId: '${bill.visit_id}', visit_id: '${bill.visit_id}',
						patient_id: '${bill.visit_id}', bill_type: '${bill.bill_type}', bill_no: '${bill.bill_no}', visit_type: '${bill.visit_type}',
						orig_mr_no: '${bill.original_mr_no}'},
						[${!viewCreditNote},${orderEnabled},${issueEnabled},${typeChangeEnabled},${togglePrimaryEnabled},
						${tpaConnectEnabled},${editRatePlanEnabled}, ${editClaimEnabled},${printDefaultEnabled},${printOtherEnabled},${createCreditNote},${viewCreditNote}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"
					style="<c:if test="${bill.mlc_status=='Y'}"></c:if>">

					<td style="text-align: right">${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
					<td>
						${bill.mr_no}
						<input type="hidden" name="patientWriteoff" id="patientWriteoff${st.index}" value="${bill.patient_writeoff}">
						<input type="hidden" name="sponsorWriteoff" id="sponsorWriteoff${st.index}" value="${bill.sponsor_writeoff}">
					</td>
					
					<td <c:if test="${bill.mlc_status=='Y'}">class="mlcIndicator"</c:if>>${bill.visit_id}</td>
					<td <c:if test="${bill.vip_status=='Y'}">class="vipIndicator" title="VIP"</c:if>><insta:truncLabel value="${bill.patient_name}" length="30"/></td>
					<td <c:if test="${not empty bill.remarks}">class="remarkIndicator" title="<c:out value='${bill.remarks}'/>"</c:if>>${bill.bill_no} </td>
					<td>${typeDisplay[bill.bill_type]}<c:if test="${bill.restriction_type != 'N'}">-${bill.restriction_type}</c:if></td>
					<%-- NOTE: show the flag next to the status, since that is what affects the flag color --%>
					<td>
						<c:if test="${not empty bill.primary_tpa_name}">
							<c:set var="claimStatus">Claim status: ${claimStatusDisplay[bill.primary_claim_status]}</c:set>
						</c:if>
						<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
						<label title="${claimStatus}">${statusDisplay[bill.status]}</label>
					</td>
					<td><fmt:formatDate value="${bill.open_date}" pattern="dd-MM-yyyy HH:mm"/></td>
					<td style="text-align: right;">${bill.total_amount}</td>
					<td style="text-align: right;">${bill.total_tax}</td>
					<td style="text-align: right;">${bill.total_amount - bill.total_claim}</td>
					<td style="text-align: right;">${bill.total_tax - bill.total_claim_tax}</td>
					<td>${paymentStatusDisplay[bill.payment_status]}</td>
					<c:set var="totalPaidAmt" value ="${bill.total_receipts + bill.deposit_set_off + bill.points_redeemed_amt + ((bill.primary_total_sponsor_receipts + bill.secondary_total_sponsor_receipts) > 0 ? (bill.primary_total_sponsor_receipts + bill.secondary_total_sponsor_receipts) : bill.claim_recd_amount)}"/>
					<td style="text-align: right;">${totalPaidAmt}</td>
					<td style="text-align: right;">${bill.total_amount + bill.total_tax - totalPaidAmt}</td>
					<td><insta:truncLabel value="${bill.org_name}" length="16"/></td>
					<td>
						<insta:truncLabel value="${bill.primary_tpa_name}" length="16"/>
					</td>
					<td style="text-align: right;">${bill.total_receipts + bill.deposit_set_off + bill.points_redeemed_amt}</td>
					<c:set var="billDueAmt" value ="${bill.total_amount + bill.total_tax - bill.total_claim -  bill.total_claim_tax - bill.total_receipts - bill.deposit_set_off - bill.points_redeemed_amt}"/>
					<td style="text-align: right;">${billDueAmt}</td>
					<c:set var="spnsrTotal" value ="${bill.total_claim}"/>
					<td style="text-align: right;">${spnsrTotal}</td>
					<td style="text-align: right;">${bill.total_claim_tax}</td>
					<td><c:if test="${bill.is_tpa}">${claimStatusDisplay[bill.primary_claim_status]}</c:if></td>
					<td style="text-align: right;">${(bill.primary_total_sponsor_receipts + bill.secondary_total_sponsor_receipts) > 0 ? (bill.primary_total_sponsor_receipts + bill.secondary_total_sponsor_receipts) : bill.claim_recd_amount}</td>
					<c:set var="spnsrDueAmt" value ="${spnsrTotal + bill.total_claim_tax - ( (bill.primary_total_sponsor_receipts + bill.secondary_total_sponsor_receipts) > 0 ? (bill.primary_total_sponsor_receipts + bill.secondary_total_sponsor_receipts) : bill.claim_recd_amount)}"/>
					<td style="text-align: right;">${spnsrDueAmt}</td>
					<td><fmt:formatDate value="${bill.finalized_date}" pattern="dd-MM-yyyy HH:mm"/></td>
					<td>${dischargeStatusMap[bill.patient_discharge_status]}</td>
				</tr>
				<script>
					extraDetails['toolbarRow${st.index}'] = {
						'Rate Plan': '${bill.org_name}',
						'Opened By': '${bill.opened_by}',
						'Label': '${bill.bill_label_name}',
						'Remarks': '${bill.remarks}',
						'Age/Gender' : '${bill.patient_age} ${bill.patient_age_in}/${bill.patient_gender}',
						'Doctor' : <insta:jsString value="${bill.doctor_name}"/>,
						'Department' : '${bill.dept_name}',
						'Bed' : <insta:jsString value="${bill.bed_name}"/>,
						'Ward' : <insta:jsString value="${bill.ward_name}"/>,
						'Disch. Date' : '<fmt:formatDate value="${bill.discharge_date}" pattern="dd-MM-yyyy"/>',
					};
				</script>
			</c:forEach>
		</table>

		<%-- NOTE:
			Show "no results" using insta:noresults tag.
			For the Search Bills screen, we don't show "no results", because the user is yet to search. Only
			if the user has searched for something and there are no results, should we show the "no results"
		--%>
		<c:if test="${param._method == 'getBills'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
	</div>

	<%-- NOTE: legend, flag and flatText are classes to use to show the flag legends. --%>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="billing.billlist.list.openunpaidbillnowbills"/></div>
		<div class="flag"><img src="${cpath}/images/blue_flag.gif"></div>
		<div class="flagText"><insta:ltext key="billing.billlist.list.labelledbills"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"> <insta:ltext key="billing.billlist.list.closedbills"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"> <insta:ltext key="billing.billlist.list.cancelledbills"/></div>
	</div>

</form>
</body>
</html>

