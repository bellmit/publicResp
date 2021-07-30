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
<head>
	<insta:js-bundle prefix="storemaster.orderkit"/>
	<meta name="i18nSupport" content="true"/>
	<c:choose>
		<c:when test="${param.order_kit_id == null}">
			<title><insta:ltext key="storemaster.orderkit.list.orderkit.add"/> - Insta HMS</title>
		</c:when>
		<c:otherwise>
			<title><insta:ltext key="storemaster.orderkit.list.orderkit.edit"/> - Insta HMS</title>
		</c:otherwise>
	</c:choose>
	<insta:link type="script" file="master/OrderKitMaster/orderkit.js"/>
	<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}"></script>
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var orderkit_names = ${orderkit_names};
	</script>
</head>
<body class="formtable" class="yui-skin-sam" autocomplete="off" onload="init();">
<c:choose>
	<c:when test="${param.order_kit_id == null}">
		<h1><insta:ltext key="storemaster.orderkit.list.orderkit.add"/></h1>
	</c:when>
	<c:otherwise>
		<h1><insta:ltext key="storemaster.orderkit.list.orderkit.edit"/></h1>
	</c:otherwise>
</c:choose>
<insta:feedback-panel/>
<form name="orderkitform" action="OrderKitMaster.do" onsubmit="return false" method="post">
<input type="hidden" name="_method" value="${param.order_kit_id == null ? 'create' : 'update' }"/>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="storemaster.orderkit.addshow.details"/></legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel"><insta:ltext key="storemaster.orderkit.addshow.orderkitname"/> :</td>
				<td>
					<input type="text" name="order_kit_name" value="<c:out value='${kit.map.order_kit_name }' />" autocomplete="off" maxlength="100"/>
					<input type="hidden" name="order_kit_id" value="${kit.map.order_kit_id }"/>
				</td>
				<td class="formlabel"><insta:ltext key="storemaster.orderkit.addshow.status"/> :</td>
				<td>
					<insta:selectoptions name="status" value="${kit.map.status}" opvalues="A,I"
					optexts="Active,Inactive" />
				</td>
			</tr>
		</table>
	</fieldset>


	<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="kititemtable">
		<tr>
			<th title="Item Name"><insta:ltext key="storemaster.orderkit.addshow.item"/></th>
			<th title="Package Size"><insta:ltext key="storemaster.orderkit.addshow.packagesize"/></th>
			<th><insta:ltext key="storemaster.orderkit.addshow.qty"/></th>
			<th style="width: 24px"></th>
			<th style="width: 24px"></th>
		</tr>
		<c:forEach var="kitItem" items="${kit_items}">
			<tr>
				<td class="forminfo" style="width:25em;padding-left: 0.5em;white-space:normal;" valign="middle">
				<label>${kitItem.map.medicine_name }</label>
				<input type="hidden" name="order_kit_id" value="${kitItem.map.order_kit_id }">
				<input type="hidden" name="medicine_name" value="${kitItem.map.medicine_name }">
				<input type="hidden" name="medicine_id" value="${kitItem.map.medicine_id }">
				<input type="hidden" name="qty_needed" value="${kitItem.map.qty_needed }">
				<input type="hidden" name="issue_base_unit" value="${kitItem.map.issue_base_unit }">
				<input type="hidden" name="sqlquery" value="update">
				<input type="hidden" name="deleted" value="N">
				
			</td>

			<td>
				<label>${kitItem.map.issue_base_unit }</label>
			</td>
			<td>
				<label>${kitItem.map.qty_needed }</label>
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
				<input type="hidden" name="order_kit_id" value="">
				<input type="hidden" name="medicine_id" value="">
				<input type="hidden" name="qty_needed" value="">
				<input type="hidden" name="issue_base_unit" value="">
				<input type="hidden" name="medicine_name" value="">
				<input type="hidden" name="sqlquery" value="">
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
				<legend class="fieldSetLabel"><insta:ltext key="storemaster.orderkit.addshow.itemdetails"/></legend>
				<table  class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td class="formLabel"><insta:ltext key="storemaster.orderkit.addshow.item"/>:
						</td>
						<td>
							<div id="item_wrapper" style="width: 20em; padding-bottom:2em; ">
								<input type="text" name="medicine_name" style="width: 20em"
								maxlength="100"/>
								<div id="item_dropdown" class="scrolForContainer"></div>
							</div>
						</td>
						<td class="formLabel"><insta:ltext key="storemaster.orderkit.addshow.packagesize"/>:</td>
						<td><label id="dlgPkgSz" name="dlgPkgSz"></label>
							<input type="hidden" name="issue_base_unit"/>
						</td>
						<td class="formLabel"><insta:ltext key="storemaster.orderkit.addshow.qty"/>:</td>
						<td>
							<input type="text" name="qty_needed" maxlength="6" class="number"
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
						<button type="button" id="Add" name="Add" accesskey="A"  style="display: inline;" class="button" onclick="onDialogSave();" ><label> <u><b><insta:ltext key="storemaster.orderkit.addshow.a"/></b></u><insta:ltext key="storemaster.orderkit.addshow.dd"/></label></button>
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
			<label><u><b><insta:ltext key="storemaster.orderkit.addshow.s"/></b></u><insta:ltext key="storemaster.orderkit.addshow.ave"/></label></button>
			<c:set var="orderkitlist" ><insta:ltext key="storemaster.orderkit.addshow.orderkitlist"/></c:set>
 			<insta:screenlink addPipe="true" screenId="mas_order_kit" label="${orderkitlist}" extraParam="?_method=list&sortOrder=order_kit_name&sortReverse=false&status=A"/>
 				
		</td>
	</tr>
</table>
</body>
</html>
