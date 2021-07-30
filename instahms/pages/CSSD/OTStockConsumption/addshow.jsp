<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
	<head>
		<title>OT Stock Consumption/Return - Insta HMS</title>

		<insta:link type="script" file="CSSD/ot_stock_consumption.js"/>
	</head>
	<body onload="init();">
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<h1>OT Stock Consumption/Return</h1>
		<insta:feedback-panel/>
		<form name="otstockconsumeform" action="OTStockConsumption.do" onsubmit="return false">
			<input type="hidden" name="_method" value="consume"/>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Operation Details</legend>
				<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
					<tr>
						<td class="formlabel">Patient Name:</td>
						<td class="forminfo">${op.map.patient_name }</td>
						<td class="formlabel">MR NO:</td>
						<td class="forminfo">${op.map.mr_no }</td>
					</tr>
					<tr>
						<td class="formlabel">Operation Name:</td>
						<td class="forminfo">${op.map.operation_name }</td>
						<td class="formlabel">OT:</td>
						<td class="forminfo">${op.map.theatre_name }</td>
					</tr>
					<tr>
						<td class="formlabel">Kit Name:</td>
						<td class="forminfo">${op.map.kit_name }</td>
						<td class="formlabel">Appointment Time:</td>
						<td class="forminfo">
							<fmt:formatDate value="${op.map.appointment_time}" pattern="dd-MM-yyyy HH:mm"/>
							<input type="hidden" name="appointment_id" value = "${op.map.appointment_id }"/>
							<input type="hidden" name="transfer_no" value = "${op.map.transfer_no }"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">OT Store:</td>
						<td class="forminfo">${op.map.dept_name}
							<input type="hidden" name="ot_store" value="${op.map.store_id }"/>
						</td>
						<td class="formlabel">Non Sterile Store:</td>
						<td>
							<insta:userstores username="${userid}" elename="non_sterile_store" id="non_sterile_store"
								tabindex="5" sterileStores="N"/>
						</td>
					</tr>
				</table>
			</fieldset>

			<div class="resultList" style="margin: 10px 0px 5px 0px;">
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="otconumablestable" border="0" width="100%">
					<tr id="chRow0">
						<th>Medicine Name</th>
						<th>Consumed Qty</th>
						<th>Returning Qty</th>
						<th>Total Qty</th>
						<th></th><!-- edit icon -->
					</tr>
					<c:forEach items="${kititems}" var="kititem">
						<tr>
							<td>${kititem.map.medicine_name }
								<input type="hidden" name="consumed" value="Y"/>
								<input type="hidden" name="qty" value="${kititem.map.qty }"/>
								<input type="hidden" name="medicine_name" value="${kititem.map.medicine_name }"/>
								<input type="hidden" name="medicine_id" value="${kititem.map.medicine_id }"/>
								<input type="hidden" name="qty_consumed" value="${kititem.map.issue_type == 'L' || kititem.map.issue_type == 'P' ? 0 : kititem.map.qty }"/>
								<input type="hidden" name="qty_returned" value="${kititem.map.issue_type == 'L' || kititem.map.issue_type == 'P' ? kititem.map.qty : 0 }"/>
								<input type="hidden" name="issue_type" value="${kititem.map.issue_type }"/>
								<input type="hidden" name="item_batch_id" value="${kititem.map.item_batch_id}"/>
								<input type="hidden" name="item_unit" value="${kititem.map.item_unit}"/>
								<input type="hidden" name="trn_pkg_size" value="${kititem.map.trn_pkg_size}"/>
							</td>
							<td>${kititem.map.issue_type == 'L' || kititem.map.issue_type == 'P' ? 0 : kititem.map.qty }</td>
							<td>${kititem.map.issue_type == 'L' || kititem.map.issue_type == 'P' ? kititem.map.qty : 0 }</td>
							<td>${kititem.map.qty }</td>
							<td>
								<label>
									<img class="button" name="editicon" onclick="onEditRow(this)"
									src="${cpath }/icons/Edit.png">
								</label>
							</td>
						</tr>
					</c:forEach>
				</table>
				</div>

		</form>

		<form name="editForm" onsubmit="javascript:void(0); return false;">
			<div id="editDialog" style="visibility:hidden; display:none">
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Item Details</legend>
						<table  class="detailList" cellpadding="0" cellspacing="0" border="0" width="100%">
							<tr id="chRow0">
								<th>Medicine Name</th>
								<th>Consumed Qty</th>
								<th>Returning Qty</th>
							</tr>
							<tr>
								<td class="formlabel" id="e_medicine_name" ></td>
								<td>
									<input type="text" name="e_qty_consumed" class="number" onblur="setRemainingQty(this,e_qty_returned);"/>
									<input type="hidden" name="qty"/>
									<input type="hidden" name="e_issue_type"/>
								</td>
								<td>
									<input type="text" name="e_qty_returned" class="number" onblur="setRemainingQty(this,e_qty_consumed);"/>
								</td>
							</tr>
						</table>
					</fieldset>

					<table width="100%">
						<tr>
							<td>
							<div style="float: left">
								<button type="button" id="Add" name="Add" accesskey="A"  style="display: inline;" class="button" onclick="onDialogSave();" ><label> <u><b>A</b></u>dd</label></button>
								<input type="button" name="CancelBut" id="CancelBut" value="Cancel" onclick="handleDetailDialogCancel();" />
								<button type="button" id="prevDialog" name="prevDialog" accesskey="P"  style="display: inline;" class="button" onclick="onNextPrev(this);"  disabled="disabled"><label> << <u><b>P</b></u>revious</label></button>
								<button type="button" id="nextDialog" name="nextDialog" accesskey="N"  style="display: inline;" class="button" onclick="onNextPrev(this);"  disabled="disabled"><label> <u><b>N</b></u>ext >></label></button>
							</div>
							</td>
						</tr>
					</table>
				</div>
			</div>
		</form>

		<table style="float: left" class="screenActions">
			<tr>
				<td>
					<button type="submit" name="save" id="save" accesskey="S" class="button" onclick="onSubmit(this);">
					<label><u><b>S</b></u>ave</label></button>&nbsp;
					|<a href="${pageContext.request.contextPath}/pages/cssd/OTStockConsumption.do?_method=list&title=OT Stock Consumption"
						>OT Stock Consumption</a>
				</td>
			</tr>
		</table>
	</body>
</html>
