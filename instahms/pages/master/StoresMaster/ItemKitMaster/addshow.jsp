<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>

<c:set var="prefbarcode" value="${genPrefs.barcode_for_item}" scope="request"/>

<head>

	<insta:link type="script" file="master/ItemKitMaster/item_kit.js"/>
	<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}"></script>

	<script>
		<c:if test="${not empty kitItemsJSON}">var kitItemsList = ${kitItemsJSON};</c:if>
		var edit = ${param._method == 'show'};
		var prefBarCode = '${prefbarcode}';
	</script>
</head>
<body class="formtable" class="yui-skin-sam" autocomplete="off" onload="init();">
<h1>Surgery Kit</h1>
<insta:feedback-panel/>
<form name="otkitform" action="ItemKitMaster.do" onsubmit="return false">
<input type="hidden" name="_method" value="${param.kit_id == null ? 'create' : 'update' }"/>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Surgery Kit Details</legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel">Surgery Kit Name :</td>
				<td>
					<input type="text" name="kit_name" value="${kit.map.kit_name }"/>
					<input type="hidden" name="kit_id" value="${kit.map.kit_id }"/>
				</td>
				<td class="formlabel">Status :</td>
				<td>
					<insta:selectoptions name="status" value="${kit.map.status}" opvalues="A,I"
					optexts="Active,Inactive" />
				</td>
			</tr>
		</table>
	</fieldset>


	<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="kititemtable">
		<tr>
			<th title="Item Name">Item</th>
			<th title="Package Size">Package Size</th>
			<th>Qty</th>
			<th style="width: 24px"></th>
			<th style="width: 24px"></th>
		</tr>
		<c:forEach var="kitItem" items="${kit_items}">
			<tr>
				<td class="forminfo" style="width:25em;padding-left: 0.5em;white-space:normal;" valign="middle">
				<label>${kitItem.map.medicine_name }</label>
				<input type="hidden" name="kit_id" value="${kitItem.map.kit_id }">
				<input type="hidden" name="medicine_name" value="${kitItem.map.medicine_name }">
				<input type="hidden" name="kit_item_id" value="${kitItem.map.kit_item_id }">
				<input type="hidden" name="qty" value="${kitItem.map.qty }">
				<input type="hidden" name="issue_base_unit" value="${kitItem.map.issue_base_unit }">
				<input type="hidden" name="kit_item_detail_id" value="${kitItem.map.kit_item_detail_id }">
				<input type="hidden" name="deleted" value="N">
			</td>

			<td>
				<label>${kitItem.map.issue_base_unit }</label>
			</td>
			<td>
				<label>${kitItem.map.qty }</label>
			</td>
			<td>
				<label>
					<img class="imgDelete" src="${cpath}/icons/delete.gif"
					onclick="onDeleteRow(this)" style="cursor:pointer" />
				</label>
			</td>

			<td>
				<label>
					<img class="button" name="editicon" onclick="onEditRow(this)"
					src="${cpath }/icons/Edit.png">
				</label>
			</td>
			</tr>
		</c:forEach>

		<tr style="display:none">
			<td class="forminfo" style="width:25em;padding-left: 0.5em;white-space:normal;" valign="middle">
				<label></label>
				<input type="hidden" name="kit_id" value="">
				<input type="hidden" name="kit_item_id" value="">
				<input type="hidden" name="qty" value="">
				<input type="hidden" name="issue_base_unit" value="">
				<input type="hidden" name="kit_item_detail_id" value="">
				<input type="hidden" name="medicine_name" value="">
				<input type="hidden" name="deleted" value="N">
			</td>

			<td>
				<label></label>
			</td>
			<td>
				<label></label>
			</td>
			<td>
				<label>
					<img class="imgDelete" src="${cpath}/icons/delete.gif"
					onclick="onDeleteRow(this)" style="cursor:pointer" />
				</label>
			</td>

			<td>
				<label>
					<img class="button" name="editicon" onclick="onEditRow(this)"
					src="${cpath }/icons/Edit.png">
				</label>
			</td>

		</tr>
	</table>

	<table class="addButton">
		<tr>
			<td align="right">
				<button type="button" name="plusItem" id="plusItem" title="Add New Item"
					onclick="openAddDialog(); return false;" accesskey="+"
		 			class="imgButton" ><img src="${cpath}/icons/Add.png"></button>
			</td>
		</tr>
	</table>

</form>

<form name="detailForm" onsubmit="javascript:void(0); return false;">
	<div id="addEditDialog" style="visibility:hidden; display:none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Item Details</legend>
				<table  class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<c:choose>
							<c:when test="${prefbarcode eq 'Y'}">
								<td class="formLabel">Item Barcode:</td>
								<td>
									<input type="text" name="item_barcode_id" onchange="getItemBarCodeDetails(this.value);" >
								</td>
								<td class="formLabel">Item:</td>
								<td>
									<div id="item_wrapper" style="width: 20em; padding-bottom:2em; ">
										<input type="text" name="medicine_name" id="medicine_name" style="width: 20em"
											maxlength="100"/>
										<div id="item_dropdown" class="scrolForContainer"></div>
									</div>
								</td>
							</c:when>
							<c:otherwise>
								<td class="formLabel">Item:</td>
								<td>
									<div id="item_wrapper" style="width: 20em; padding-bottom:2em; ">
										<input type="text" name="medicine_name" style="width: 20em"
										maxlength="100"/>
										<div id="item_dropdown" class="scrolForContainer"></div>
									</div>
								</td>
							</c:otherwise>
						</c:choose>
						<td class="formLabel">Package Size:</td>
						<td><label id="dlgPkgSz"></label>
							<input type="hidden" name="issue_base_unit"/>
						</td>
						<td class="formLabel">Quantity:</td>
						<td>
							<input type="text" name="qty" maxlength="6" class="number"
							onkeypress="return onKeyPressAddQty(event);"/>
							<input type="hidden" name="medicine_id"/>
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
			| <a href="${pageContext.request.contextPath}/pages/master/StoresMaster/ItemKitMaster.do?_method=list&title=Kit List"
				>Surgery Kit List</a>
		</td>
	</tr>
</table>
</body>
</html>
