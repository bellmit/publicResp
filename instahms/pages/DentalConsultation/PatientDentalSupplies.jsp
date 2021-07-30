<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap() %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
	<title>Patient Dental Supplies - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="dentalconsultation/patientdentalsupplies.js"/>
	<script>
		var doctors = ${doctors};
		var cpath = '${cpath}';
		var gItems = ${items};
		var gShades = ${shades};
		var gMax_centers_inc_default = ${genericPrefs.max_centers_inc_default};
		var gCenterId = ${centerId};
		var gCenterName = '${centerName}';
	</script>
</head>
<body onload="init();">
	<h1 style="float: left">Patient Dental Supplies</h1>
	<c:url var="searchUrl" value="Supplies.do"/>
	<insta:patientsearch searchType="mrNo" fieldName="mr_no" showStatusField="true" buttonLabel="Find"
		searchMethod="show" searchUrl="${searchUrl}"/>
	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" showClinicalInfo="true"/>
	<form name="suppliesForm" action="Supplies.do" method="POST" autocomplete="off">
		<input type="hidden" name="_method" value="update">
		<input type="hidden" name="mr_no" value="${param.mr_no}"/>
		<input type="hidden" name="noOfOrders" id="noOfOrders" value="${fn:length(orders)+1}"/>

		<fieldset class="fieldSetBorder">
			<div>
				Hide Completed <input type="checkbox" name="hide_completed" id="hide_completed" checked onclick="hideCompletedOrders(this);"/>
			</div>
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="orderDetails" border="0" width="100%">
				<tr>
					<th>#</th>
					<th>Order Date</th>
					<th>Treatment Name</th>
					<th>Supplier</th>
					<th>Order Status</th>
					<th>Item</th>
					<th>Order Qty</th>
					<th>Received Qty</th>
					<th>Received Date</th>
					<th style="width: 16px"></th>
					<th style="width: 16px"></th>
					<th style="width: 16px"></th>
				</tr>
				<c:set var="numOrders" value="${fn:length(orders)}"/>
				<c:forEach begin="1" end="${numOrders+1}" var="i">
					<c:set var="order" value="${orders[i-1].map}"/>
					<c:set var="numOfItems" value="0"/>
					<c:set var="items" value="${supplies[order.supplies_order_id]}"/>
					<c:if test="${not empty order}">
						<c:set var="numOfItems" value="${fn:length(items)}"/>
					</c:if>

					<c:if test="${empty order}">
						<c:set var="tr_style" value='style="display:none"'/>
					</c:if>
					<tr ${tr_style}>
						<td>
							<label>${i}</label>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${order.ordered_date}" var="ordered_date"/>
							<input type="hidden" name="h_supplies_order_id" value="${order.supplies_order_id}"/>
							<input type="hidden" name="h_ordered_date" value="${ordered_date}"/>
							<input type="hidden" name="h_treatment_id" value="${order.treatment_id}"/>
							<input type="hidden" name="h_treatment_name" value="${order.service_name}"/>
							<input type="hidden" name="h_ordered_by" value="${order.ordered_by}"/>
							<input type="hidden" name="h_ordered_by_name" value="${order.ordered_by_name}"/>
							<input type="hidden" name="h_supplier_id" value="${order.supplier_id}"/>
							<input type="hidden" name="h_supplies_order_status" value="${order.supplies_order_status}"/>
							<input type="hidden" name="h_remarks" value="${order.remarks}"/>
							<input type="hidden" name="h_order_index" value="${i}">
							<input type="hidden" name="h_center_id" value="${order.center_id}"/>
							<input type="hidden" name="h_center_name" value="${order.center_name}"/>
							<input type="hidden" name="h_delete" value="false"/>
						</td>
						<td><label>${ordered_date}</label></td>
						<td><insta:truncLabel value="${order.service_name}" length="20"/></td>
						<td><insta:truncLabel value="${order.supplier_name}" length="20"/></td>
						<td><label>
								<c:choose>
									<c:when test="${order.supplies_order_status == 'O'}">
										Ordered
									</c:when>
									<c:when test="${order.supplies_order_status == 'C'}">
										Completed
									</c:when>
									<c:when test="${order.supplies_order_status == 'P'}">
										Partial
									</c:when>
									<c:when test="${order.supplies_order_status == 'X'}">
										Cancelled
									</c:when>
									<c:when test="${order.supplies_order_status == 'N'}">
										Patient No Show
									</c:when>
									
								</c:choose>
							</label>
						</td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td style="width: 16px">
							<a href="#Cancel Supplier" onclick="return cancelSupplier(this);" title="Cancel Supplier" >
								<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
							</a>
						</td>
						<td style="width: 16px">
							<a name="_orderEditAnchor" href="#Edit Order Details" onclick="return showEditOrderDialog(this);"
								title="Edit Order Details">
								<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</td>
						<td style="width: 16px">
							<c:url value="SuppliesPrint.do" var="printUrl">
								<c:param name="_method" value="print"/>
								<c:param name="supplies_order_id" value="${order.supplies_order_id}"/>
								<c:param name="mr_no" value="${param.mr_no}"/>
							</c:url>
							<c:choose>
								<c:when test="${empty order.supplies_order_id}">
									<img src="${cpath}/icons/Print1.png">
								</c:when>
								<c:otherwise>
									<a href="${printUrl}" title="Print Supplier Order Details" target="_blank">
										<img src="${cpath}/icons/Print.png">
									</a>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
					<c:forEach begin="1" end="${empty order ? 1 : numOfItems}" var="j">
						<c:set var="item" value="${items[j-1]}"/>
						<c:set var="tr_item_style" value='style= "table-row"'/>
						<c:if test="${empty item}">
							<c:set var="tr_item_style" value='style="display:none"'/>
						</c:if>
						<tr ${tr_item_style}>
							<td >
								<fmt:formatDate pattern="dd-MM-yyyy" value="${item.received_date}" var="received_date"/>
								<input type="hidden" name="h_${i}_item_id" value="${item.item_id}"/>
								<input type="hidden" name="h_${i}_item_qty" value="${item.item_qty}"/>
								<input type="hidden" name="h_${i}_received_qty" value="${item.received_qty}"/>
								<input type="hidden" name="h_${i}_received_by" value="${item.received_by}"/>
								<input type="hidden" name="h_${i}_received_by_name" value="${item.received_by_name}"/>
								<input type="hidden" name="h_${i}_received_date" value="${received_date}">
								<input type="hidden" name="h_${i}_unit_rate" value="${item.unit_rate}"/>
								<input type="hidden" name="h_${i}_vat_perc" value="${item.vat_perc}"/>
								<input type="hidden" name="h_${i}_item_remarks" value="${item.item_remarks}"/>
								<input type="hidden" name="h_${i}_shade_id" value="${item.shade_id}"/>
							</td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td><insta:truncLabel value="${item.item_name}" length="20"/></td>
							<td><label>${item.item_qty}</label></td>
							<td><label>${item.received_qty}</label></td>
							<td><label>${received_date}</label></td>
							<td style="width: 16px"></td>
							<td style="width: 16px"></td>
							<td style="width: 16px"></td>
						</tr>
					</c:forEach>

				</c:forEach>
			</table>
			<table class="addButton" style="height: 25px;">
				<tr>
					<td style="width: 16px;text-align: right">
						<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Item"
							onclick="showAddOrderDialog(this); return false;"
							class="imgButton"><img src="${cpath}/icons/Add.png"></button>
					</td>
				</tr>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
			<tr>
				<td><input type="button" name="Save" value="Save" onclick="onSave();"></td>
				<td>
					<insta:screenlink screenId="dental_consultations" extraParam="?_method=show&mr_no=${param.mr_no}"
						addPipe="true" label="Dental Consultation"/>
				</td>
			</tr>
		</table>
		<div id="addItemDialog" style="display: none">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Add Item</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Supplier: </td>
							<td>
								<input type="hidden" name="editRowId" value=""/>
								<input type="hidden" name="d_treatment_name" id="d_treatment_name" value=""/>
								<insta:selectdb name="d_supplier_id" id="d_supplier_id" table="dental_supplier_master" displaycol="supplier_name"
								valuecol="supplier_id" filtered="true" dummyvalue="-- Select --" dummyvalueid="" onchange="deleteItemsInDialog()"/></td>
							<td class="formlabel">Order Status: </td>
							<td><insta:selectoptions name="d_ordered_status" id="d_ordered_status" value="O" opvalues="O,C,P,X,N" optexts="Ordered,Completed,Partial,Cancelled,Patient No Show"/></td>
						</tr>
						<tr>
							<td class="formlabel">Ordered by: </td>
							<td>
								<input type="hidden" name="d_ordered_by" id="d_ordered_by"/>
								<div id="orderby_autocomplete" style="padding-bottom: 20px">
									<input type="text" name="d_ordered_by_name" id="d_ordered_by_name"/>
									<div id="orderby_container"></div>
								</div>
							</td>
							<td class="formlabel">Order Date: </td>
							<td><insta:datewidget name="d_ordered_date" id="d_ordered_date" /></td>
							<c:if test="${genericPrefs.max_centers_inc_default > 1 }">
								<td class="formlabel">Center: </td>
								<td>
									<c:choose>
										<c:when test="${centerId == 0}">
											<select class="dropdown" name="d_center_id" id="d_center_id">
												<option value="">-- Select --</option>
												<c:forEach items="${centers}" var="center">
													<option value="${center.map.center_id}">${center.map.center_name}</option>
												</c:forEach>
											</select>
										</c:when>
										<c:otherwise>
											<label id="d_centerName">${centerName}</label>
											<input type="hidden" name="d_center_id" id="d_center_id" value="${centerId}"/>
										</c:otherwise>
									</c:choose>
								</td>
							</c:if>

						</tr>
						<tr>
							<td class="formlabel">Remarks: </td>
							<td colspan="3"><input type="text" name="d_remarks" id="d_remarks" style="width: 200px"/></td>
						</tr>
					</table>
					<fieldset class="fieldSetBorder" style="margin-top: 10px">
						<table width="100%" id="item_order_details" class="detailList">
							<tr>
								<th>Item</th>
								<th>Shade</th>
								<th>Ord. Qty</th>
								<th>Rec. Qty</th>
								<th>Received by</th>
								<th>Received Date</th>
								<th>Remarks</th>
								<th style="width: 16px"></th>
							</tr>
							<tr style="display: none">
								<td>
									<input type="hidden" id="d_unit_rate" name="d_unit_rate" value=""/>
									<input type="hidden" id="d_vat_perc" name="d_vat_perc" value=""/>
									<select name="d_item_id" id="d_item_id" class="dropdown" onchange="return setItemRate(this);">
										<option value="">-- Select --</option>
									</select>
								</td>
								<td>
									<select name="d_shade" class="dropdown">
										<option value="">-- Select --</option>
									</select>
								</td>
								<td><input type="text" name="d_item_qty" value="1" class="number"/></td>
								<td><input type="text" name="d_received_qty" class="number" onchange="setReceivedDate(this);"/></td>
								<td></td>
								<td></td>
								<td><input type="text" name="d_item_remarks" ></td>
								<td style="text-align: center">
									<a href="#Cancel Item" onclick="return cancelItem(this);" title="Cancel Item" >
										<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
									</a>
								</td>
							</tr>
						</table>
						<table class="addButton" style="height: 25px;">
							<tr>
								<td style="width: 16px;text-align: right">
									<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Item"
										onclick="addRowInline(this); return false;"
										class="imgButton"><img src="${cpath}/icons/Add.png"></button>
								</td>
							</tr>
						</table>
					</fieldset>
					<table style="margin-top: 10px">
						<tr>
							<td>
								<input type="button" name="Update" id="Update" value="Update"/>
								<input type="button" name="Close" id="Close" value="Close"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
	</form>
</body>
</html>