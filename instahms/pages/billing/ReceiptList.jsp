<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>

<head>
	<title><insta:ltext key="salesissues.salespharmacyreceipt.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

	<insta:js-bundle prefix="billing.receiptlist"/>
	<script>
	var toolbarOptions = getToolbarBundle("js.billing.receiptlist.toolbar");
		var receiptsTempList = ${jsonReceiptsTempList};
		var roleId = '${roleId}';
		var receiptPrintRights = '${urlRightsMap.receipt_print}';
		var centerId = ${centerId};
		var max_centers_inc_default = ${max_centers_inc_default};
		var sampleCollectionCenterId = ${sampleCollectionCenterId};
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="billing/receipt_list.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<jsp:include page="/pages/Common/BillNoPrefix.jsp" />

	<style type="text/css">
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }
		.status_DR { background-color: #EAD6BB }
		.status_DF { background-color: #DDDA8A}
		table.legend { border-collapse: collapse; margin-left: 6px; }
		table.legend td { border: 1px solid grey; padding: 2px 5px;}
	</style>
		<insta:js-bundle prefix="billing.receiptlist"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="receiptPrintRights" value="${urlRightsMap.receipt_print}"/>

<%-- some convenience variables initialized here for display purpose --%>
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="P" value="Bill Now"/>
<c:set target="${typeDisplay}" property="C" value="Bill Later"/>
<c:set target="${typeDisplay}" property="M" value="Other"/>
<c:set target="${typeDisplay}" property="R" value="Pharmacy Return"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="S" value="Settled"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="modeDisplay" class="java.util.HashMap"/>
<c:set target="${modeDisplay}" property="C" value="Cash"/>
<c:set target="${modeDisplay}" property="R" value="Credit Card"/>
<c:set target="${modeDisplay}" property="B" value="Debit Card"/>
<c:set target="${modeDisplay}" property="Q" value="Cheque"/>
<c:set target="${modeDisplay}" property="D" value="Draft"/>

<jsp:useBean id="recptTypeDisplay" class="java.util.HashMap"/>
<c:set target="${recptTypeDisplay}" property="A" value="Advance"/>
<c:set target="${recptTypeDisplay}" property="S" value="Settlement"/>

<jsp:useBean id="sponsorIndexDisplay" class="java.util.HashMap"/>
<c:set target="${sponsorIndexDisplay}" property="P" value="Pri."/>
<c:set target="${sponsorIndexDisplay}" property="S" value="Sec."/>

<jsp:useBean id="paymentTypeDisplay" class="java.util.HashMap"/>
<c:set target="${paymentTypeDisplay}" property="R" value="Receipt"/>
<c:set target="${paymentTypeDisplay}" property="F" value="Refund"/>
<c:set target="${paymentTypeDisplay}" property="S" value="Sponsor"/>
<c:set target="${paymentTypeDisplay}" property="DR" value="Deposit"/>
<c:set target="${paymentTypeDisplay}" property="DF" value="Deposit Return"/>

<jsp:useBean id="patientTypeDisplay" class="java.util.HashMap"/>
<c:set target="${patientTypeDisplay}" property="i" value="In Patient"/>
<c:set target="${patientTypeDisplay}" property="o" value="Out Patient"/>
<c:set target="${patientTypeDisplay}" property="r" value="Pharmacy"/>
<c:set target="${patientTypeDisplay}" property="t" value="Incoming"/>

<c:set var="method_name" value= "getScreen"/>
<c:set var="receiptList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty receiptList}"/>

<body onload="init()">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="billno">
<insta:ltext key="salesissues.salespharmacyreceipt.list.billno"/>
</c:set>
<c:set var="bill_type">
<insta:ltext key="salesissues.salespharmacyreceipt.list.billtype"/>
</c:set>
<c:set var="mode">
<insta:ltext key="salesissues.salespharmacyreceipt.list.mode"/>
</c:set>
<c:set var="receipt_amount">
<insta:ltext key="billing.receiptno.editreceipt.receipt.amount"/>
</c:set>
<c:set var="bill_amount">
<insta:ltext key="services.show.list.billamount"/>
</c:set>
<c:set var="receiptno">
<insta:ltext key="salesissues.salespharmacyreceipt.list.receiptno"/>
</c:set>
<c:set var="type">
<insta:ltext key="salesissues.salespharmacyreceipt.list.type"/>
</c:set>
<c:set var="sponserstatus">
<insta:ltext key="salesissues.salespharmacyreceipt.list.primary"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.secondary"/>
</c:set>
<c:set var="paymentstatus">
<insta:ltext key="salesissues.salespharmacyreceipt.list.receipts"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.sponsorreceipts"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.refunds"/>
</c:set>
<c:set var="receiptstatus">
<insta:ltext key="salesissues.salespharmacyreceipt.list.advance"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.settlement"/>
</c:set>
<c:set var="status">
<insta:ltext key="salesissues.salespharmacyreceipt.list.open"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.finalized"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.closed"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.cancelled"/>
</c:set>
<c:set var="counterstatus">
<insta:ltext key="salesissues.salespharmacyreceipt.list.billingcounter"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.pharmacycounter"/>
</c:set>
<c:set var="billtype">
<insta:ltext key="salesissues.salespharmacyreceipt.list.billnow"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.billlater"/>
</c:set>
<c:set var="restrictiontype">
<insta:ltext key="salesissues.salespharmacyreceipt.list.hospital"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.pharmacy"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.incomingtest"/>
</c:set>
<c:set var="visittype">
<insta:ltext key="salesissues.salespharmacyreceipt.list.ip"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.op"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.pharmacy"/>,
<insta:ltext key="salesissues.salespharmacyreceipt.list.incomingtest"/>
</c:set>
<c:set var="date">
<insta:ltext key="salesissues.salespharmacyreceipt.list.date"/>
</c:set>
	<c:choose><c:when test="${not empty param.title}">
		<div class="pageHeader">${ifn:cleanHtml(param.title)}</div>
	</c:when><c:otherwise>
		<div class="pageHeader"><insta:ltext key="salesissues.salespharmacyreceipt.list.search"/> <c:if test="${category eq 'pharmacy'}"><insta:ltext key="salesissues.salespharmacyreceipt.list.pharmacy"/> </c:if><insta:ltext key="salesissues.salespharmacyreceipt.list.receipts"/></div>
	</c:otherwise></c:choose>

<form name="ReceiptSearchForm" method="GET">
	<input type="hidden" name="_method" value="getReceipts">
	<input type="hidden" name="_searchMethod" value="getReceipts"/>
	<insta:search form="ReceiptSearchForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="ui.label.mrno"/>:</div>
			<div class="sboFieldInput">
				<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
				<div id="mrnoContainer"></div>
		</div>
	</div>
	<div class="sboField">
		<div class="sboFieldLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.billno"/>:</div>
		<div class="sboFieldInput">
			<input type="text" name="bill_no" value="${ifn:cleanHtmlAttribute(param.bill_no)}"/>
		</div>
	</div>
</div>
<div id="optionalFilter" style="clear: both; display : ${hasResult ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.receiptdate"/>:</div>
				<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="salesissues.salespharmacyreceipt.list.from"/>:</div>
						<insta:datewidget name="display_date" id="display_date0" value="${paramValues.display_date[0]}"/>
					</div>
					<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="salesissues.salespharmacyreceipt.list.to"/>:</div>
							<insta:datewidget name="display_date" id="display_date1" value="${paramValues.display_date[1]}"/>
							<input type="hidden" name="display_date@op" value="ge,le"/>
							<input type="hidden" name="display_date@cast" value="y"/>
					</div>
					<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.counter"/>:</div>
					<div class="sfField">
						<insta:selectdb name="counter" table="counters"  dummyvalue="ALL COUNTERS"
						valuecol="counter_id" displaycol="counter_no" value="${param.counter}" />
					</div>
					<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.sponsortype"/></div>
						<div class="sfField">
							<c:choose>
			             		 <c:when test="${corpInsurance ne 'Y'}">
									<insta:checkgroup name="sponsor_index" selValues="${paramValues.sponsor_index}"
					              		opvalues="P,S" optexts="${sponserstatus}"/>
			           	   	 	 </c:when>
			            	     <c:otherwise>
			                   		<insta:checkgroup name="sponsor_index" selValues="${paramValues.sponsor_index}"
			              	  	 		opvalues="P" optexts="${sponserstatus}"/>
			              	 	 </c:otherwise>
			              	</c:choose>
						</div>
				</td>
				<c:if test="${category ne 'pharmacy'}">
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.paymenttype"/></div>
						<div class="sfField">
								<insta:checkgroup name="payment_type" selValues="${paramValues.payment_type}"
								opvalues="R,S,F" optexts="${paymentstatus}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.receipttype"/></div>
						<div class="sfField">
							<insta:checkgroup name="recpt_type" selValues="${paramValues.recpt_type}"
							opvalues="A,S" optexts="${receiptstatus}"/>
						</div>
					</td>

					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.billstatus"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="A,F,C,X" optexts="${status}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.countertype"/></div>
						<div class="sfField">
							<insta:checkgroup name="counter_type" selValues="${paramValues.counter_type}"
							opvalues="B,P" optexts="${counterstatus}"/>
						</div>
					</td>
				</c:if>
				<td>
					<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.billtype"/>:</div>
					<div class="sfField">
						<insta:checkgroup name="bill_type" selValues="${paramValues.bill_type}"
						opvalues="P,C" optexts="${billtype}" />
					</div>
					<c:if test="${category ne 'pharmacy'}">
						<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.usagetype"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="restriction_type" selValues="${paramValues.restriction_type}"
							opvalues="N,P,T" optexts="${restrictiontype}"/>
					</c:if>

					</div>
				</td>
				<td class="last">
					<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.patienttype"/>:</div>
					<div class="sfField">
						<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
						opvalues="i,o,r,t" optexts="${visittype}"/>
					</div>
					<c:if test="${sampleCollectionCenterId == -1}">
						<div class="sfLabel"><insta:ltext key="salesissues.salespharmacyreceipt.list.collectioncenter"/>:</div>
						<div class="sfField">
							<c:choose>
								<c:when test="${max_centers_inc_default > 1 && centerId != 0}">
										<select name="collectionCenterId" id="collectionCenterId" class="dropdown">
												<option value="">--Select--</option>
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
								valuecol="collection_center_id" displaycol="collection_center" dummyvalue="-- Select --"/>
								</c:otherwise>
							</c:choose>
						</div>
					</c:if>
				</td>
			</tr>
	</table>
</div>
</insta:search>
</form>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<c:choose>
		<c:when test="${empty (receiptList) && (param.method!=method_name)}">
			<p><insta:ltext key="salesissues.salespharmacyreceipt.list.noresultfound"/> </p>
		</c:when>
		<c:otherwise>

			<c:set var="templateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
			<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>

			<c:if test="${not empty templateList}">
				<c:forEach var="temp" items="${templateList}">
					<c:set var="templateValues" value="${templateValues},${temp.map.template_name}"/>
					<c:set var="templateTexts" value="${templateTexts},${temp.map.template_name} (Receipt/Refund)"/>
				</c:forEach>
			</c:if>
			<c:if test="${not empty receiptList && (receiptPrintRights == 'A' || roleId == '1' || roleId == '2')}">
				<div align="right">
					<form name="printerSelectForm">
					<insta:selectoptions name="printTemplate" onchange="changePrinter()"
					opvalues="${templateValues}" optexts="${templateTexts}"
					value="${genPrefs.receiptRefundPrintDefault}" />

					<insta:selectdb name="printer" table="printer_definition" valuecol="printer_id"
					displaycol="printer_definition_name" onchange="changePrinter()" value="${pref.map.printer_id}" />
				</form>
				</div>
			</c:if>

			<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
				onmouseover="hideToolBar('');">
				<tr>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="salesissues.salespharmacyreceipt.list.patienttype"/></th>
					<insta:sortablecolumn name="receipt_no" title="${receiptno}"/>
					<insta:sortablecolumn name="payment_type" title="${type}"/>
					<insta:sortablecolumn name="display_date" title="${date}"/>
					<insta:sortablecolumn name="receipt_amount" title="${receipt_amount}"/>
                    <insta:sortablecolumn name="bill_amount" title="${bill_amount}"/>
					<insta:sortablecolumn name="payment_mode" title="${mode}"/>
					<insta:sortablecolumn name="bill_no" title="${billno}"/>
					<insta:sortablecolumn name="bill_type" title="${bill_type}"/>
					<th><insta:ltext key="salesissues.salespharmacyreceipt.list.username"/></th>
				</tr>

				<c:forEach var="r" items="${receiptList}" varStatus="st">
					<c:set var="i" value="${st.index}"/>
					<c:set var="flagColor">
					<c:choose>
						<c:when test="${r.payment_type == 'DR'}">green</c:when>
						<c:when test="${r.payment_type == 'DF'}">yellow</c:when>
						<c:when test="${r.status == 'C'}">grey</c:when>
						<c:when test="${r.status == 'X'}">black</c:when>
						<c:when test="${(r.status == 'A')||(r.status == 'F')||(r.status == 'S')}">empty</c:when>
					</c:choose>
					</c:set>
					<c:set var="printEnable"
					value="${(r.bill_type != 'M') && (r.bill_type != 'R') && (r.bill_type != 'P') && !r.is_deposit} "/>

					<c:set var="editBillEnable" value ="${r.bill_no !='' && preferences.modulesActivatedMap['mod_ins_ext'] ne 'Y' && r.is_credit_note eq false}"/>
					<c:set var="editReceiptEnable" value ="${ r.receipt_no !='' && !r.is_deposit && r.payment_mode_id != '-9' }"/>
					<c:choose>
					<c:when test="${r.payment_type != 'DF' && r.payment_type != 'DR' && r.payment_mode_id != '-9' }">
						<c:set var="receipt" value="receipt_no"/>
					</c:when>
					<c:otherwise>
						<c:set var="receipt" value="deposit_no"/>
					</c:otherwise>
					</c:choose>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{ billNo:'${r.bill_no}', type: '${r.payment_type}', receiptNo: '${r.receipt_no}',
						al_table: 'all_receipts_audit_view',${receipt}:'${r.receipt_no}'},
						[${editBillEnable},${editReceiptEnable},${printEnable},${editReceiptEnable}])"
						onmouseover="hideToolBar(${st.index})" 	id="toolbarRow${st.index}">

						<td>${r.mr_no}</td>
						<c:choose><c:when test="${r.visit_type == 'r'}">
							<td>${r.customer_name}</td>
						</c:when>
						<c:when test="${r.visit_type == 't'}">
							<td>${r.incoming_patient_name}</td>
						</c:when><c:otherwise>
							<td><insta:truncLabel value="${r.patient_full_name}" length="30"/></td>
						</c:otherwise></c:choose>
						<td>${patientTypeDisplay[r.visit_type]}</td>
						<td>${r.receipt_no}</td>
						<td><img src="${cpath}/images/${flagColor}_flag.gif"/>
							<c:choose>
								<c:when test="${r.is_deposit}">
									${recptTypeDisplay['S']}
								</c:when>								
								<c:when test="${r.payment_type == 'R'}">
									${recptTypeDisplay[r.recpt_type]}
								</c:when>
								<c:when test="${r.payment_type == 'S'}">
									${sponsorIndexDisplay[r.sponsor_index]} - ${paymentTypeDisplay[r.payment_type]} - ${recptTypeDisplay[r.recpt_type]}
								</c:when>
								<c:otherwise>
									${paymentTypeDisplay[r.payment_type]}
								</c:otherwise>
							</c:choose>
						 </td>
						<td><fmt:formatDate value="${r.display_date}" pattern="dd-MM-yyyy"/></td>
						<td style="text-align: right">${r.amount}</td>
                        <td style="text-align: right">${r.allocated_amount}</td>
						<td>
						    <c:choose>
								<c:when test="${r.is_deposit && 
								    r.deposit_available_for == 'B' && r.package_id > '0'}">
								    Package Deposit
								</c:when>
								<c:when test="${r.is_deposit && 
								    (r.deposit_available_for == 'B' || r.deposit_available_for == 'H' || r.deposit_available_for == 'P') }">
									General Deposit
								</c:when>
								<c:when test="${r.is_deposit && r.deposit_available_for == 'I'}">
									IP Deposit
								</c:when>
								<c:otherwise>
							        ${r.payment_mode}
								</c:otherwise>
							</c:choose>
						</td>

						<c:set var="billAttrs">
							<insta:ltext key="salesissues.salespharmacyreceipt.list.patientid"/>: ${r.visit_id}&nbsp;&nbsp;<insta:ltext key="salesissues.salespharmacyreceipt.list.billstatus"/>: ${statusDisplay[r.status]}
						</c:set>

						<td>${r.bill_no}</td>
						<td>
							<c:choose>
							<c:when test="${(r.visit_type == 'r')}"><insta:ltext key="salesissues.salespharmacyreceipt.list.pharmacysales"/></c:when>
							<c:otherwise>
								${typeDisplay[r.bill_type]}
							</c:otherwise>
							</c:choose>
						</td>
						<td>${r.username}</td>
					</tr>
				</c:forEach>
			</table>
		</c:otherwise>
		</c:choose>
	<div class="legend" style="display:${hasResult ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/black_flag.gif"/></div>
		<div class="flagText"><insta:ltext key="salesissues.salespharmacyreceipt.list.cancelledbillreceipts"/> </div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText"><insta:ltext key="salesissues.salespharmacyreceipt.list.closedbillreceipts"/></div>
	</div>
</body>
</html>

