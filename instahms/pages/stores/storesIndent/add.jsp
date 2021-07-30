<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="currTime" value="<%= (new java.util.Date()) %>"/>
<c:set var="addText">
<insta:ltext key="storemgmt.pharmacyindentlist.add.add"/>
</c:set>
<c:set var="editText">
<insta:ltext key="storemgmt.pharmacyindentlist.add.edit"/>
</c:set>
<c:set var="userText">
<insta:ltext key="storemgmt.pharmacyindentlist.add.user"/>
</c:set>
<c:set var="stocktransferText">
<insta:ltext key="storemgmt.pharmacyindentlist.add.stocktransfer"/>
</c:set>
<c:set var="indentText">
<insta:ltext key="storemgmt.pharmacyindentlist.add.indent"/>
</c:set>
<c:set var="title" value="${param._method == 'add' ? addText : editText} ${indentType == 'U' ? userText : stocktransferText} ${indentText} ${indentdetails.map.indent_no}"/>
<html>
<head>
<c:set var="allowDecimalsForQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty()%>" />
<c:set var="allowCrossCenterIndents" value = "<%=GenericPreferencesDAO.getGenericPreferences().getAllow_cross_center_indents()%>" />
<c:set var="selectedStore" value="${param._method == 'add' ? '' : indentdetails.map.indent_store}"/>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title>${title} -<insta:ltext key="storemgmt.pharmacyindentlist.add.title"/></title>

<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js"/>
<insta:link type="js" file="stores/storeMedicinesAjax.js" />
<insta:link type="js" file="stores/indent.js"/>
<style>
	#dialog_mask.mask {
	    z-index: 1;
	    display:none;
	    position:absolute;
	    top:0;
	    left:0;
	    -moz-opacity: 0.0001;
	    opacity:0.0001;
	    filter: alpha(opacity=50);
	    background-color:#CCC;
	}
	input.num {text-align: right; width: 5em;}

	.scrolForContainer .yui-ac-content{
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:35em; width:35em;/* ie6 */
	}
</style>
<c:set var="disableEdit" value="${param._method ne 'add'}" />

<c:set var="masterFilter" value=""/>
<c:if test="${indentType == 'U'}">
	<c:set var="masterFilter" value="&issueType=CPL"/>
</c:if>

<script>
	var gDisableForEdit = "${disableEdit}";
	var indValue = "${param._method == 'add' ? 'Add' : 'Edit'}";
	var indentType = '${indentType}';
	var path = '${cpath}';
	var userdept = <%= request.getAttribute("userstore") %>;
	var deptId = '${ifn:cleanJavaScript(store_id)}';
	var gRoleId = '${roleId}';
	var accessstores = '${multiStoreAccess}';
	var user = '${ifn:cleanJavaScript(requester_name)}';
	var indentCreater = '${indentdetails.map.requester_name}';
	var showAvblQty = '${actionRightsMap.show_avbl_qty}';
	var allowDecimalsForQty = '${allowDecimalsForQty}';
	var actionId = '${actionId}';
	var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
	var masterFilter = '${masterFilter}';
	var orderKitJSON = ${orderkitJSON};
	var storeMedicineAjaxUrlParamQueryStr = '&hosp=${ifn:cleanURL(sesHospitalId)}&includeZeroStock=Y&includeConsignmentStock=Y&includeUnapprovedStock=Y${masterFilter}';
</script>

<%-- store wise items: used when from or to store selected for items --%>
<!--<script src="${cpath}/pages/stores/getMedicinesInStock.do?ts=${stock_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}&includeZeroStock=Y&includeConsignmentStock=Y&includeUnapprovedStock=Y${masterFilter}"></script>

-->
<script>
	var jMedicineNames = null;
</script>
<%-- item master: when using show all items --%>
<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}${masterFilter}"></script>
<insta:js-bundle prefix="stores.mgmt"/>
</head>

<c:choose>
	<c:when test="${not empty indentdetails.map.location_type}">
		<c:set var="location_type" value="${indentdetails.map.location_type}"/>
	</c:when>
	<c:otherwise>
		<c:set var="location_type" value="D"/>
	</c:otherwise>
</c:choose>

<body onload="init('${indentType}');showHideDeptWard('${location_type}'); checkstoreallocation();" class="yui-skin-sam">
<c:set var="department">
<insta:ltext key="storemgmt.pharmacyindentlist.add.department"/>
</c:set>
<c:set var="status1">
<insta:ltext key="storemgmt.pharmacyindentlist.add.showallitems"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.add.showindentstoreitems"/>
</c:set>
<c:set var="status2">
<insta:ltext key="storemgmt.pharmacyindentlist.add.showallitems"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.add.showindentstoreitems"/>,
<insta:ltext key="storemgmt.pharmacyindentlist.add.showrequeststoreitems"/>
</c:set>
<div id="storecheck" style="display: block;" >
<h1>${title}</h1>

<div id="msgDiv" style="display:none;margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" class="brB brT brL brR">
	<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;"> <img src="${cpath}/images/error.png" /></div>
	<c:out value="There is no assigned store/no store with auto fill indents"/>
	<div class="fltR" style="margin:-8px 0px 0 26px; width:17px;"> <img src="${cpath}/images/fileclose.png" onclick="document.getElementById('msgDiv').style.display='none';"/></div>
</div>

<insta:feedback-panel/>
<form action="storesIndent.do" method="POST" autocomplete="off">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="indentNo" value="${indentdetails.map.indent_no}">
	<input type="hidden" name="indenttype" value="${indentType}">
	<input type="hidden" name="indent_type" value="${indentType}">
	<input type="hidden" name="fromstore" value="">
	<input type="hidden" id="dialogId" value=""/>
	<div id="dialog" style="display: none; visibility:hidden">
		<div class="bd">
			<!-- Start Added For order kit -->
			<table class="formtable">	
				<tr>
					<td align="left" >
						<input type="radio" name="item_type" id="itemName" value="itemname" onclick="onChangeItemType()"/>
						<label for="itemName">Items</label>
					</td>
					<td align="left" id="orderKitId">
						<input type="radio" name="item_type" id="order" value="order" onclick="onChangeItemType()"/>
						<label for="order">Order Kits</label>
					</td>
				</tr>
			</table>
			<table class="formtable" style="display:none" id="orderKitDetails">
				<tr>
					<td  class="formlabel" style="width: 95px;">Order Kit Name:</td>
					<td valign="top" >
						<div id="orderkit_wrapper" style="width: 20em; padding-bottom:0.2em; z-index: 9000;" >
							<input type="text" name="orderkits" id="orderkits"  tabindex="20" style="width: 38em;" value=""/>
							<div id="orderkit_dropdown" class="scrolForContainer"></div>
						</div>
					</td>
				</tr>
				<tr>
					<td colspan="2" style="color:red;"><b><insta:ltext key="storemgmt.pharmacyindentlist.add.orderkitnote"/></b></td>
				</tr>
			</table>
			<br/>
			<div class="formlabel" id="itemInfo" style="font-size:14px;text-align:left;"></div>
			<table style="display:none;word-wrap:break-word;table-layout: fixed;padding-top:5px;padding-bottom:5px;color:black;background-color:#F0F0F5;" id="orderKitMissedItemsHeader">	
				<tr>
					<th style="width:380px;font-family:Arial-BoldMT;text-align: left;padding-left: 13px;padding-top: 8px;color:#666666;font-size:11px;"><b>ITEM NAME</b></th>
					<th style="text-align: left;padding-left: 50px;padding-top: 8px;font-family:Arial-BoldMT;color:#666666;font-size:11px;"><b>QUANTITY</b></th>
				</tr>
			</table>
			<table class="detailList" 
				style="display:none;width:100%;height:150px;overflow:auto;table-layout: fixed;border-bottom: 1px #CCCCCC solid;" id="orderKitMissedItems">	
			</table>
			<!-- End orderkit -->
			<fieldset class="fieldSetBorder" id="itemFieldSet" style="display:none">
			<legend class="fieldSetLabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.itemdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" id="itemDetails">
				<tr>
					<td><insta:ltext key="storemgmt.pharmacyindentlist.add.itemname"/>:</td>
					<td valign="top">
						<div id="item_wrapper" style="width: 20em; padding-bottom:0.2em; ">
			    		<input type="text" name="itemname" id="itemname" style="width: 20em" maxlength="100" tabIndex="1"/>
				    	<div id="item_dropdown" class="scrolForContainer"></div>
				     	</div>
					</td>
					<td><insta:ltext key="storemgmt.pharmacyindentlist.add.itemqty"/>:</td>
					<td><input type="text" name="item_qty" class="num" onkeypress="return isEventEnterEsc(event)" tabIndex="2"/></td>
				</tr>
				<tr>
					<td><insta:ltext key="storemgmt.pharmacyindentlist.add.pkgsize"/>:</td>
					<td><input type="text" name="pkg_size"  class="num" readonly />
						<input type="hidden" name="avail_qty"/>
						<input type="hidden" name="qty_avbl_for_reqstore"/>
						<input type="hidden" name="issue_type_id"/>
						<input type="hidden" name="identification"/>
						<input type="hidden" name="itemid"/>
					</td>
					<td><insta:ltext key="storemgmt.pharmacyindentlist.add.unituom"/>:</td>
					<td><input type="text" name="issue_units"  readonly /></td>
					
				</tr>
				<tr>
					<td><insta:ltext key="storemgmt.pharmacyindentlist.add.packagetype"/>:</td>
					<td><input type="text" name="pkg_type"  readonly /></td>
				</tr>
			</table>
			</fieldset>
			<table>
				<tr><td>&nbsp;</td></tr>
				<tr>
					<td><button type="button" id="Add" name="Add" accesskey="A"  style="display: inline;" class="button" onclick="handleSubmit();" tabIndex="3"><label><b><u><insta:ltext key="storemgmt.pharmacyindentlist.add.a"/></u></b><insta:ltext key="storemgmt.pharmacyindentlist.add.dd"/></label></button>
					<button type="button" id="Cancel" name="Cancel" accesskey="C"  style="display: inline;" class="button" onclick="handleCancel();" tabIndex="4"><label><b><insta:ltext key="storemgmt.pharmacyindentlist.add.cancel"/></b></label></button></td>
				</tr>
			</table>
		</div>
	</div>

	<c:choose><c:when test="${indentType == 'U'}">
		<insta:radio name="filterType" radioText="${status1}"
			radioValues="A,I" radioIds="a,i" value="I" onclick="filterItems(this.value);"/>
	</c:when><c:otherwise>
		<insta:radio name="filterType" radioText="${status2}"
			radioValues="A,I,N" radioIds="a,i,n" value="I" onclick="filterItems(this.value);"/>
	</c:otherwise></c:choose>

	<fieldset class="fieldSetBorder" >
		  <legend class="fieldSetLabel" id="legendLabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.indentdetails"/></legend>
			<table   class="formtable" >
				<tr>
					<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.raisedby"/>:</td>
					<td class="forminfo" ><c:out value="${not empty indentdetails.map.requester_name ? indentdetails.map.requester_name: userid}"/></td>
					<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.indentstore"/>:</td>
					<td class="forminfo">
						<div id="userstore">
							<c:choose>
								<c:when test="${allowCrossCenterIndents == 'Y'}">
									<insta:selectdb  name="indent_store" id="indent_store" value="${param._method == 'add' ? '' : indentdetails.map.indent_store}"
									table="stores" valuecol="dept_id"  displaycol="dept_name" onchange="return onChangeIndentStore();"
									filtercol="status,is_super_store" filtervalue="A,Y" orderby="dept_name"/>
								</c:when>
								<c:otherwise>
									<select name="indent_store" id="indent_store" class = "dropdown" onchange="return onChangeIndentStore();" >
										<c:forEach var="stores" items="${storesList}">
											<c:if  test="${stores.map.dept_id == selectedStore}">
												<option value="${stores.map.dept_id}" selected="selected">${stores.map.dept_name}</option>
											</c:if>
											<c:if test="${selectedStore == ''}">
												<option value="${stores.map.dept_id}">${stores.map.dept_name}</option>
											</c:if>
										</c:forEach>
									</select>
								</c:otherwise>
							</c:choose>
						</div>
						<input type="hidden" name="original_indent_store" value="${indentdetails.map.indent_store}" />
						</td>
					<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.status"/>:</td>
					<td>
						<c:choose>
							<c:when test="${param._method == 'add'}">
								<select name="status" class="dropdown">
									<option value="O" ${indentdetails.map.status=='O'? 'selected' : ''}><insta:ltext key="storemgmt.pharmacyindentlist.add.open"/></option>
								</select>
							</c:when>
							<c:otherwise>
								<select name="status" class="dropdown">
									<option value="O" ${indentdetails.map.status=='O'? 'selected' : ''}><insta:ltext key="storemgmt.pharmacyindentlist.add.open"/></option>
									<option value="X" ${indentdetails.map.status=='X'? 'selected' : ''}><insta:ltext key="storemgmt.pharmacyindentlist.add.cancelled"/></option>
								</select>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<c:if test="${indentType == 'U'}">
							<div id="userissuediv">
								<table>
									<tr>
										<td class="formlabel">
											<input type="radio" name="dept_ward" value="D" id="user_dept"
											onclick="showHideDeptWard(this.value)" checked="checked"/> <insta:ltext key="storemgmt.pharmacyindentlist.add.requestingdept"/>:
										</td>
										<td>
											<insta:selectdb  name="dept" value="${indentdetails.map.dept_from}" table="department"
											valuecol="dept_id" displaycol="dept_name" dummyvalue="${department}"
											orderby="dept_name"/>
											<span class="star" style="vertical-align:middle;">*</span>
										</td>
									</tr>
									<tr>
										<td class="formlabel">
											<input type="radio" name="dept_ward" value="W" id="user_ward"
											onclick="showHideDeptWard(this.value)"/><insta:ltext key="storemgmt.pharmacyindentlist.add.requestingward"/>:
										</td>
										<td>
											<select name="ward" id="ward" class="dropdown" disabled="disabled">
												<option value=""><insta:ltext key="storemgmt.pharmacyindentlist.add.ward"/></option>
												<c:forEach items="${wards }" var="ward">
													<option value="${ward.map.ward_no }"
															<c:if test="${indentdetails.map.dept_from eq ward.map.ward_no}"><insta:ltext key="storemgmt.pharmacyindentlist.add.selected"/></c:if> >
														${ward.map.ward_name }
													</option>
												</c:forEach>
											</select>
										</td>
									</tr>
								</table>
							</div>
						</c:if>

						<c:if test="${indentType == 'S'}">
						<div id="stocktransferdiv">
							<table width="100%">
								<tr>
									<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.requestingstore"/>:</td><td>
									<c:choose>
										<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
											<input type="hidden" name="dept_id" id="dept_id" value="${ifn:cleanHtmlAttribute(store_id)}" />
											<insta:userstores username="${userid}" elename="store_to" onchange="return onChangeRequestStore();" id="store_to" val="${store_id}"/>
										</c:when>
										<c:otherwise>
											<input type="hidden" name="dept_id" id="dept_id" value="${ifn:cleanHtmlAttribute(store_id)}" />
											<input type="hidden" name="store_to" id="store_to" value="${ifn:cleanHtmlAttribute(store_id)}" />
												<b>${ifn:cleanHtmlAttribute(store_name)}</b>
										</c:otherwise>
									</c:choose>

								</tr>
							</table>
						</div>
						</c:if>
					</td>
					<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.expecteddate"/>:</td>
					<td><fmt:formatDate value="${indentdetails.map.expected_date}" pattern="dd-MM-yyyy" var="expecteddt"/>
						<fmt:formatDate value="${indentdetails.map.expected_date}" pattern="HH:mm" var="expectedtime"/>
						<fmt:formatDate pattern="HH:mm" value="${currTime}" var="currenttime"/>
					<insta:datewidget name="expected_date" value="${expecteddt}"/> -
					<input type="text" name="expected_time" id="" value="${not empty expectedtime? expectedtime : currenttime}" class="timefield" >
					<span class="star">*</span>
					</td>
					<td class="formlabel"><insta:ltext key="storemgmt.pharmacyindentlist.add.reason"/>:</td>
					<td><input type="text" size="30" name="remarks" value="${indentdetails.map.remarks}" maxlength="100">
					</td>
				</tr>
			</table>
			</fieldset>
  				<table id="indentItemListTab" class="detailList" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<th width="35%"><insta:ltext key="storemgmt.pharmacyindentlist.add.item"/></th>
						<th ><insta:ltext key="storemgmt.pharmacyindentlist.add.qty"/></th>
						<c:if test="${actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2'}">
							<th ><insta:ltext key="storemgmt.pharmacyindentlist.add.qtyavbl.indentstore"/></th>
						</c:if>
						<c:if test="${(actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2') and actionId eq 'stores_transfer_indent'}">
							<th ><insta:ltext key="storemgmt.pharmacyindentlist.add.qtyavbl.reqstore"/></th>
						</c:if>
						<th ><insta:ltext key="storemgmt.pharmacyindentlist.add.packagetype"/></th>
						<th><insta:ltext key="storemgmt.pharmacyindentlist.add.pkgsize"/></th>
						<th><insta:ltext key="storemgmt.pharmacyindentlist.add.unituom"/></th>
						<th style="width:24px"></th>		<%-- trash icon --%>
						<th style="width:24px"></th>		<%-- edit icon --%>
					</tr>
					<c:choose>
						<c:when test="${not empty indentlist}">
							<c:set var="i"/>
							<c:forEach var="indent" items="${indentlist}" varStatus="status">
								<c:set var="i" value="${status.index+1}"/>
								<tr id="row${i}">
									<td><label id="itemnamelbl${i}">${indent.map.medicine_name}</label>
										<input type="hidden" id="itemidlbl${i}" value="${indent.map.medicine_id}"/>
										<input type="hidden" id="indentnolbl${i}" value="${indent.map.indent_no}"/>
										<input type="hidden" id="identificationlbl${i}" value="${indent.map.identification}">
										<input type="hidden" name="indentdelete" id="indentdelete${i}" value="false">
									</td>
									<td><label id="itemqtylbl${i}">${indent.map.qty}</label></td>
									<c:if test="${actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2'}">
										<td><label id="availqtylbl${i}">${indent.map.availableqty}</label></td>
									</c:if>
									<c:if test="${(actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2') and actionId eq 'stores_transfer_indent'}">
										<td><label id="availreqstoreqtylbl${i}">${indent.map.availableqtyfor_reqstore}</label></td>
									</c:if>
									<td><label id="pkgtypelbl${i}">${indent.map.package_type}</label></td>
									<td><label id="pkgsizelbl${i}">${indent.map.issue_base_unit}</label></td>
									<td><label id="issunitslbl${i}">${indent.map.issue_units}</label></td>
									<td><label id="itemchecklbl${i}">
<!--										<input type="checkbox" name="indentCheck" id="indentCheck${i}"-->
<!--										onclick="cancelRow(this,itemrow${i},row${i})"/>-->
										<img class="imgDelete" name="indentCheck" id="indentCheck${i}" src="${cpath}/icons/Delete.png"
									onclick="cancelRow(this,itemrow${i},${i})" style="cursor:pointer" />
										</label></td>
									<td><button type="button" id="addBut${i}" name="addBut" title='<insta:ltext key="storemgmt.pharmacyindentlist.add.edititem"/>'
									onclick="addIndent(${i});return false;"  class="imgButton">
									<img class="button" name="itemrow" id="itemrow${i}" src="${cpath}/icons/Edit.png" style="cursor:pointer;" /></button></td>


								</tr>
							</c:forEach>
								<c:set var="i" value="${i+1}"/>
								<tr id="row${i}">
									<td><label id="itemnamelbl${i}"></label>
										<input type="hidden" id="itemidlbl${i}"/>
										<input type="hidden" id="identificationlbl${i}">
										<input type="hidden" name="indentdelete" id="indentdelete${i}" value="false">
									</td>
									<td><label id="itemqtylbl${i}"></label></td>
									<c:if test="${actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2'}">
									<td><label id="availqtylbl${i}"></label></td>
									</c:if>
									<c:if test="${(actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2') and actionId eq 'stores_transfer_indent'}">
										<td><label id="availreqstoreqtylbl${i}"></label></td>
									</c:if>
									<td><label id="pkgtypelbl${i}"></label></td>
									<td><label id="pkgsizelbl${i}"></label></td>
									<td><label id="issunitslbl${i}"></label></td>
									<td><label id="itemchecklbl${i}"></label></td>
									<td><button type="button" id="addBut${i}" name="addBut" title='<insta:ltext key="storemgmt.pharmacyindentlist.add.additem"/>'
									onclick="addIndent(${i});return false;" accesskey="+" class="imgButton">
									<img class="button" name="itemrow" id="itemrow${i}" src="${cpath}/icons/Add.png" style="cursor:pointer;" /></button></td>

								</tr>
						</c:when>
						<c:otherwise>
							<tr id="row1">
								<td><label id="itemnamelbl1"></label>
									<input type="hidden" id="itemidlbl1"/>
									<input type="hidden" id="identificationlbl1"/>
									<input type="hidden" name="indentdelete" id="indentdelete1" value="false">
								</td>
								<td><label id="itemqtylbl1"></label></td>
								<c:if test="${actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2'}">
								<td><label id="availqtylbl1"></label></td>
								</c:if>
								<c:if test="${(actionRightsMap.show_avbl_qty eq 'A' or roleId eq '1' or roleId eq '2') and actionId eq 'stores_transfer_indent'}">
									<td><label id="availreqstoreqtylbl1"></label></td>
								</c:if>
								<td><label id="pkgtypelbl1"></label></td>
								<td><label id="pkgsizelbl1"></label></td>
								<td><label id="issunitslbl1"></label></td>
								<td><label id="itemchecklbl1"></label></td>
								<td><button type="button" id="addBut1" name="addBut"  class="imgButton" accesskey="+"
								onclick="addIndent(1);return false;" title='<insta:ltext key="storemgmt.pharmacyindentlist.add.addnewitem"/>' >
									<img src="${cpath}/icons/Add.png" id="itemrow1" name="itemrow" style="cursor:pointer;"/></button></td>
							</tr>
						</c:otherwise>
					</c:choose>
				</table>
  			<div class="screenActions">
				<button type="button" accesskey="S" name="save" class="button" onclick="return validateFields()" ${multiCentered && centerId == 0 ? 'disabled' : '' }><b><u><insta:ltext key="storemgmt.pharmacyindentlist.add.s"/></u></b><insta:ltext key="storemgmt.pharmacyindentlist.add.ave"/></button>
				<a href="${cpath }/stores/storesIndent.do?_method=list&status=O&sortOrder=indent_no&sortReverse=true"><insta:ltext key="storemgmt.pharmacyindentlist.add.backtodashboard"/></a>
		  </div>
	<table id="hiddenIndentItemListTab"></table>
</form>
</div>
</body>
</html>
