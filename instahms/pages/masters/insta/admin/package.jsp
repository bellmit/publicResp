<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="mod_adv_packages" value="${preferences.modulesActivatedMap.mod_adv_packages}"/>
<c:set var="pkg_approval_rights" value="${actionRightsMap.package_approval}"/>
<c:set var="packDetailsJSON" value="<%= new java.util.HashMap() %>"/>
<c:if test="${param._method == 'show'}">
	<c:set var="packDetailsJSON" value="${packageDetailsJSON}"/>
</c:if>
<c:set var="package_type" value="${param._method == 'add' ? param.package_type : packageDetails.map.package_type}"/>
<c:set var="type" value="${param._method == 'add' ? 'Package' : packageDetails.map.type}"/>
<c:set var="org_id" value="${param._method == 'add' ? 'ORG0001' : param.org_id}"/>
<c:set var="multi_visit_package" value="${param.multi_visit_package}"/>

<c:set var="is_multi_visit_package" value="N"></c:set>
<c:if test="${param.multi_visit_package == 'true'}">
	<c:set var="is_multi_visit_package" value="Y"></c:set>
</c:if>

<html>
<head>
	<title>Package Definition - Insta HMS</title>

	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="orderdialog.js" />
	<insta:link type="js" file="ordertable.js" />
	<insta:link type="js" file="masters/packmaster.js"/>
	<insta:link type="js" file="masters/orderCodes.js" />
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<c:set var="hasOpe" value="true"></c:set>
	<c:if test="${packageDetails.map.operation_id == null || packageDetails.map.operation_id == ''}">
		<c:set var="hasOpe" value="false"></c:set>
	</c:if>

	<c:if test="${package_type == 'd'}">
		<c:set var="filter" value="Radiology,Laboratory"/>
	</c:if>

    <c:set var="showOperations" value="${(package_type == 'i') || (package_type == 'o' && genPrefs.operationApplicableFor == 'b')}"/>

    <c:set var="orderableItemUrl" value="${cpath}/patients/orders/getorderableitem.json?filter=${filter}"/>
    <c:set var="orderableItemUrl" value="${orderableItemUrl}&org_id=${ifn:cleanURL(param.org_id)}&visit_type=${ifn:cleanURL(package_type)}&package_applicable=Y&is_multi_visit_package=${ifn:cleanURL(is_multi_visit_package)}" />
	<script>
		var enabledOrderableItemApi = true;
		var orderableItemUrl = '${orderableItemUrl}';
		var doctorsList = ${doctorsList};
		var packages = ${packages};
		var cpath="${cpath}";
		var opIdOfpackage = '${packageDetails.map.operation_id}';
		var doctors = { "doctors": doctorsList };
		var anaList = filterList(doctorsList, 'dept_id', 'DEP0002');
		var anaesthetists = { "doctors": anaList };
		var hasOperation = ${hasOpe};
		var jChargeHeads = <%= request.getAttribute("chargeHeadsJSON") %>;
		gPrescDocRequired = 'N';
		var serviceGroupsJSON = ${serviceGroupsJSON};
		var servicesSubGroupsJSON = ${servicesSubGroupsJSON};
		var forceSubGroupSelection = '${genPrefs.forceSubGroupSelection}';
		var packageDetailsJSON = ${packDetailsJSON};
		var showOperations = ${showOperations};
		var mrno = null;
		var anaeTypes = ${anaeTypesJSON};
		var allOrdersJSON = null;
		var mod_adv_packages = '${mod_adv_packages}';
		var packType = '${ifn:cleanJavaScript(package_type)}';
		var clonePackage = ${not empty param.clone_package};
		var isMultiVisitPackage = '${ifn:cleanJavaScript(multi_visit_package)}';
		var screenid = '';
		var allDoctorConsultationTypes = ${allDoctorConsultationTypes} ;
		var itemGroupList = ${itemGroupListJson};
		var itemSubGroupList = ${itemSubGroupListJson};
	</script>
</head>

<body onload="init();initEditDialog();initAddDialog();itemsubgroupinit();" class="yui-skin-sam">

	<h1>Package Elements</h1>
	<insta:feedback-panel/>
	<form name="packagemasterform" method="post" action="PackagesMasterAction.do" autocomplete="off">
		<input type="hidden" name="editRowId" id="editRowId" value=""/>
		<input type="hidden" name="_method" value="${param._method == 'add' || not empty param.clone_package ? 'create' : 'update'}"/>
		<input type="hidden" name="package_id" id="package_id" value="${packageDetails.map.package_id}"/>
		<input type="hidden" name="clone_package" id="clone_package" value="${not empty param.clone_package}"/>
		<input type="hidden" name="org_id" id="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
		<input type="hidden" name="opeCharge" id="opeCharge"/>
		<input type="hidden" name="editDialogId" id="editDialogId" />
		<input type="hidden" name="multi_visit_package" value="${ifn:cleanHtmlAttribute(param.multi_visit_package)}"/>

		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel">Package Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel" >Package Name:</td>
					<td>
						<input type="text" name="package_name" id="package_name" value="${not empty param.clone_package ? '(Copy of) ' : ''}${packageDetails.map.package_name}" />
				    </td>
				    <td class="formlabel">Package Category:</td>
					<td>
						<insta:selectdb  name="package_category_id" id="package_category_id"  value="${packageDetails.map.package_category_id}" table="package_category_master" valuecol="package_category_id" displaycol="package_category" orderby="package_category_id" />
					</td>
					<td class="formlabel" >Package Type:</td>
					<td>
						<label><b>
						   <c:if test="${package_type == 'i'}">IP</c:if>
						   <c:if test="${package_type == 'o'}">OP</c:if>
						   <c:if test="${package_type == 'd'}">Diag</c:if>
						</b></label>
						<input type="hidden" name="package_type" id="package_type" value="${ifn:cleanHtmlAttribute(package_type)}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Service Group:</td>
					<td>
						<insta:selectdb id="service_group_id" name="service_group_id" value="${packageDetails.map.service_group_id}"
							table="service_groups" class="dropdown"   dummyvalue="-- Select --"
							valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()" />
					</td>
					<td class="formlabel">Service Sub Group:</td>
					<td>
						<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown" onchange="getOrderCode();">
							<option value="0">-- Select --</option>
						</select>
					</td>
					<td class="formlabel">Type:</td>
					<td>
						<c:choose>
							<c:when test="${multi_visit_package}">
								<input type="hidden" name="type" value="P"/>
								<b>${type}</b>
							</c:when>
							<c:when test="${param._method == 'add'}">
								<input type="radio" name="type" id="templateP" value="P" ${type == 'Package' ? 'checked' : ''} onclick="onTypeChange()"/>
								<label for="templateP">Package</label>
							</c:when>
							<c:when test="${param._method == 'show'}">
								<input type="hidden" name="type" value="${type == 'Package' ? 'P' : 'T'}"/>
								<b>${type}</b>
							</c:when>
						</c:choose>

					</td>
				</tr>
				<tr>
					<td class="formlabel">Package Code:</td>
					<td>
						<input type="text" name="package_code" id="package_code" class="field1"
							value="${packageDetails.map.package_code}">
					</td>
					<td class="formlabel">Package Completion:</td>
					<td>
						<c:set var="showDropdown" value="${!multi_visit_package && type == 'Package'}"/>
						<div id="handover_dropdown_div" >
							<insta:selectoptions name="dd_handover_to" opvalues="P,S" optexts="Handover to Patient,Handover to Sponsor"
									value="${packageDetails.map.handover_to}" onchange="setHandoverValue(this);"/>
						</div>
						<div id="handover_label_div" >
							<label><b>Handover to Patient</b></label>
						</div>
						<input type="hidden" name="handover_to" value="${packageDetails.map.handover_to}"/>
					</td>
				</tr>
				<tr>
					<td colspan="6" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold">Rate: </font></td>
				</tr>
				<tr>
					<td class="formlabel">Rate Sheet:</td>
					<td>
						<label><b>${param._method == 'add' ? 'GENERAL' : orgDetails.map.org_name}</b></label>
					</td>
					<td class="formlabel">Allow Rate Increase:</td>
					<td>
						<insta:radio name="allow_rate_increase" radioValues="true,false" radioText="Yes,No"
								value="${empty packageDetails.map.allow_rate_increase ? 'true' : packageDetails.map.allow_rate_increase}" />
					</td>
					<td class="formlabel">Allow Rate Decrease:</td>
					<td>
						<insta:radio name="allow_rate_decrease" radioValues="true,false" radioText="Yes,No"
								value="${empty packageDetails.map.allow_rate_decrease ? 'true' : packageDetails.map.allow_rate_decrease}" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Further Discounts:</td>
					<td>
						<c:choose>
							<c:when test="${param._method == 'show'}">
								<insta:radio name="allow_discount" radioValues="true,false" radioText="Allow,Disallow"
								             value="${packageDetails.map.allow_discount}"/>
							</c:when>
							<c:when test="${param._method == 'add'}">
								<insta:radio name="allow_discount" radioValues="true,false"
								             radioText="Allow,Disallow" value="false"/>
							</c:when>
						</c:choose>
					</td>
				</tr>
				<tr>
					<td colspan="6" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold">Insurance: </font></td>
				</tr>
				<tr>
					<td class="formlabel">Insurance Category:</td>
					<td>
						<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true" />
					</td>
					<td class="formlabel">Prior Auth Required:</td>
			 	 	<td>
			 	 		<insta:selectoptions name="prior_auth_required" opvalues="N,S,A" optexts="Never,SomeTimes,Always"
									value="${packageDetails.map.prior_auth_required}" />
			 	 	</td>
					<td class="formlabel">Billing Group:</td>
					<td>
						<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${packageDetails.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
							displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
					</td>
				</tr>
				<tr>
					<td colspan="6" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold">Others: </font></td>
				</tr>
				<tr>
					<td class="formlabel" valign="top">Description:<br>(max 4000 characters)</td>
					<td colspan="2" valign="top">
						<textarea name="description" id="description" rows="2" cols="30"
							onblur="chklen();"><c:out value="${packageDetails.map.description }"/></textarea>
					</td>
				</tr>
				<tr>
					<td colspan="6" style="border-bottom: 1px #e6e6e6 solid" id="advn_pack_label_row"><font style="font-weight: bold">Advanced Pack: </font></td>
				</tr>
				<c:if test="${!multi_visit_package}">
					<tr>
						<td class="formlabel" valign="top" id="dtLabelTd">Required Document Types: </td>
						<td id="dtValueTd"><insta:selectdb name="doc_type_id" id="doc_type_id" table="doc_type" valuecol="doc_type_id"
								displaycol="doc_type_name" multiple="true" orderby="doc_type_name" values="${doc_types}" style="height: 100px"/></td>
					</tr>
				</c:if>
				<tr>
					<td class="formlabel" >Package Charge:&nbsp;</td>
					<td >
						<c:choose>
							<c:when test="${param._method == 'add'}">
								<input type="text" name="totAmt" id="totAmt" class="number" onblur="checkAmt();"
								onkeypress="return enterNumOnly(event);" value="0"/>
							</c:when>
							<c:otherwise>
								<input type="text" name="totAmt" id="totAmt" class="number" onblur="checkAmt();"
								onkeypress="return enterNumOnly(event);" value="${pkgCharge }"/>
								<div id="applyToAllRateSheetsDiv" style="display: ${not empty param.clone_package ? 'inline' : 'none'}">
									<input type="checkbox" name="applyChargeToAll" value="Y"/> Apply
									<img class="imgHelpText" src="${cpath}/images/help.png" title="Apply Charge to All Rate Plans"/>
								</div>
							</c:otherwise>
						</c:choose>

					</td>
					<c:set var="rateplanflagcolor" value="empty"/>
					<c:if test="${param._method == 'show'}">
						<c:if test="${ operationIncompatibility == false}">
							<c:set var="rateplanflagcolor" value="yellow"/>
						</c:if>
					</c:if>
					<td  class="formlabel" id="operation_label_td"><img src="${cpath}/images/${rateplanflagcolor}_flag.gif" />Operations:</td>
					<td id="operation_dropdown_td">
						<select name="operation_id" class="dropdown" onchange="getCharge(this)" ${multi_visit_package ? 'disabled' : ''}>
							<option value="">...select operation...</option>
							<c:forEach items="${rateplanwiseoperations}" var="operation">
								<option value="${operation.OP_ID }"
								<c:if test="${operation.OP_ID  == packageDetails.map.operation_id  }">
										selected='true'
										</c:if>>${operation.OPERATION_NAME }</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td colspan="6" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold">Statuses: </font></td>
				</tr>
				<tr>
					<td class="formlabel">Status:</td>
					<td>
						<c:choose>
							<c:when test="${param._method == 'add'}">
								<insta:radio name="package_active" radioValues="A,I" radioText="Active,Inactive"
									value="A"/>
							</c:when>
							<c:when test="${param._method == 'show'}">
								<insta:radio name="package_active" radioValues="A,I" radioText="Active,Inactive"
									value="${packageDetails.map.package_active}"/>
							</c:when>
						</c:choose>
					</td>
					<td class="formlabel">Valid From Date: </td>
					<td>
						<fmt:formatDate var="valid_from_date" value="${packageDetails.map.valid_from_date}" pattern="dd-MM-yyyy"/>
						<insta:datewidget id="valid_from_date" name="valid_from_date" value="${valid_from_date}"/>
					</td>
					<td class="formlabel">Valid To Date: </td>
					<td>
						<fmt:formatDate var="valid_to_date" value="${packageDetails.map.valid_to_date}" pattern="dd-MM-yyyy"/>
						<insta:datewidget id="valid_to_date" name="valid_to_date" value="${valid_to_date}"/>
					</td>
				</tr>
			</table>
		</fieldset>

	<div><font style="color:#666; font-weight: bold">Package Components: </font></div>
	<div style="clear: both"></div>
	<table class="detailList" cellspacing="0" cellpadding="0" id="packageComponentTable" width="100%" style="margin-top: 10px">
	<tbody id="chargeTBody">
		<tr id="chRow0">
			<th>Display Order</th>
			<th>Head</th>
			<th>Description</th>
			<th>Consultation Type</th>
			<th>Remarks</th>
			<th>Quantity</th>
			<th class="number" style="width: 10px"></th>
			<th></th>
			<th></th>
		</tr>
		<c:set var="totalDiscount" value="0"/>
		<c:set var="totalAmount" value="0"/>
		<c:set var="numItems" value="${fn:length(packageComponentDetails)}"/>

		<c:forEach  begin="1" end="${numItems+1}"  var="i" varStatus="status">
			<c:set var="packageComponent" value="${packageComponentDetails[i-1]}"/>

			<c:choose>
				<c:when test="${empty packageComponent}">
					<c:set var="rowId" value="chRow_template" />
					<c:set var="style" value='style="display:none"'/>
					<c:set var="flagColor" value="green" />
				</c:when>
				<c:otherwise>
					<c:set var="rowId" value="chRow${i}"/>
					<c:set var="flagColor" value="empty" />
				</c:otherwise>
			</c:choose>
			<c:if test="${packageComponent.map.applicable != true && not empty packageComponent}">
				<c:set var="flagColor" value="yellow" />
			</c:if>
			<tr id="${rowId}" ${style} >
				<td>${packageComponent.map.display_order}</td>
				<td ><img src="${cpath}/images/${flagColor}_flag.gif" id="stFlag${i}" />
					<label >${packageComponent.map.chargehead_name} </label>
					<input type="hidden" name="charge_head"  value='${packageComponent.map.charge_head}'>
					<input type="hidden" name="chargegroup_id"  value='${packageComponent.map.chargegroup_id}'>
					<input type="hidden" name="pack_ob_id"  value="${not empty param.clone_package ? "_" : packageComponent.map.pack_ob_id}"/>
					<input type="hidden" name="activity_id"  value='${packageComponent.map.activity_id}' >
					<input type="hidden" name="rateId"  value="0">
					<input type="hidden" name="activity_remarks"  value="${packageComponent.map.activity_remarks}">
					<input type="hidden" name="cancelled"  value="N"/>
					<input type="hidden" name="edited"  value="N"/>
					<input type="hidden" name="activity_type" value="${packageComponent.map.activity_type}"/>
					<input type="hidden" name="activity_charge" value="${packageComponent.map.activity_charge }"/>
					<input type="hidden" name="activity_qty" value="${packageComponent.map.activity_qty}"/>
					<input type="hidden" name="display_order" id="display_order" value="${packageComponent.map.display_order}"/>
					<input type="hidden" name="activity_units" value="${packageComponent.map.activity_units }"/>
					<input type="hidden" name="consultation_type_id" value="${packageComponent.map.consultation_type_id }"/>
				</td>
				<td>
					<c:set var="description"> ${packageComponent.map.activity_description}
						<c:if test="${packageComponent.map.charge_head == 'MISOTC'}">
							(Rate:${packageComponent.map.activity_charge })
						</c:if>
					</c:set>
					<insta:truncLabel value="${description}" length="40"/>
					<input type="hidden" name="activity_description"
					       value="${packageComponent.map.activity_description}"/>
				</td>
				<td>
					<insta:truncLabel value="${packageComponent.map.consultation_type_name}" length="40"/>
				</td>
				<td>
					<insta:truncLabel value="${packageComponent.map.activity_remarks}" length="20"/>
				</td>
				<td style="text-align:right;width:20px" >
					<label >${packageComponent.map.activity_qty} </label>
				</td>
				<td style="text-align:right;width:10px" >
					<label >${packageComponent.map.display_units}</label>
				</td>
				<td style="text-align:right">
					<a onclick="return cancelPackageComponent(this);" href="javascript:void(0)">
						<img src="${cpath}/icons/delete.gif" class="imgDelete button"/>
					</a>
				</td>
				<td>
					<a onclick="return showEditChargeDialog(this);" href="javascript:void(0)" >
						<img src="${cpath}/icons/Edit.png" class="button" />
					</a>
				</td>
			</tr>
			</c:forEach>

			<tr>
				<td colspan="8" ></td>
				<td>
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Item"
						onclick="addOrderDialog.start(this, false, ''); return false;"
						accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
		</tbody>
	</table>
	<c:set var="approval_status" value="${not empty param.clone_package ? '' : packageDetails.map.approval_status}"/>
	<div id="CollapsiblePanel1" class="CollapsiblePanel" style="margin-top: 10px; display: ${mod_adv_packages == 'Y' && type == 'Package' ? 'block' : 'none'}">
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Package Approval</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<fieldset class="fieldSetBorder">
			<table >
				<tr>
					<td style="width: 150px; padding:5px 0px 2px 0px;">
						<input type="hidden" name="hidden_approval_status" value="${packageDetails.map.approval_status}"/>
						<input type="radio" name="approval_status" value="A"
							${approval_status == 'A' ? 'checked' : ''} ${roleId == 1 || roleId == 2 || pkg_approval_rights == 'A' ? '' : 'disabled'}> Approved<br/>
						<input type="radio" name="approval_status" value="R"
							${approval_status == 'R' ? 'checked' : ''} ${roleId == 1 || roleId == 2 || pkg_approval_rights == 'A' ? '' : 'disabled'}> Rejected
					</td>
					<td style="text-align: right; padding:5px 5px 2px 0px;">Remarks: </td>
					<td style="padding:5px 0px 2px 0px;">
						<textarea name="approval_remarks" id="approval_remarks" cols="70" rows="3"
							${roleId == 1 || roleId == 2 || pkg_approval_rights == 'A' ? '' : 'disabled'}><c:out value="${not empty param.clone_package ? '' : packageDetails.map.approval_remarks}"/></textarea>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
	
	<insta:taxations/>
		
</form>
<form name="editForm">
	<div id="editDialog">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Item Details</legend>
				<table class="formtable" width="100%">
					<tr>
					 	<td class="formlabel">Display Order:</td>
						<td><input type="text" name="eDisplayOrder" id="eDisplayOrder" /></td>
					</tr>

					<tr>
						<td class="formlabel">Remarks:</td>
						<td><input type="text" name="eRemarks" id="eRemarks"/></td>
					</tr>
					<tr>
						<td class="formlabel">Quantity</td>
						<td>
							<input type="text" name="eQty" id="eQty"  size=5 class="number" onkeypress="return enterNumOnlyzeroToNine(event)" disabled="disabled"/>
						</td>
					</tr>
				</table>
			</fieldset>

			<table>
				<tr>
					<td><input type="button" value="OK" onclick="saveEdit()"/></td>
					<td><input type="button" value="Cancel" onclick="cancelEdit()"/></td>
				</tr>
			</table>

		</div>
	</div>
</form>

	<table class="screenActions">
	<tr>
		<td>
			<button type="submit" name="save" id="save" accesskey="S"
				class="button" onclick="return onSave()"
				${type == 'Package' && (roleId != 1 && roleId != 2 && pkg_approval_rights != 'A') && approval_status == 'A' ? 'disabled' : ''}>
			<label><u><b>S</b></u>ave</label></button>
		</td>
		<td>&nbsp;|&nbsp;</td>
		<td>
			<a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?
				_method=getPackageListScreen&amp;package_active=A&amp;sortReverse=false&approval_status=A">Packages List
			</a>
		</td>
		<c:if test="${param._method == 'show'}">
			<td>&nbsp;|&nbsp;</td>
			<td>
				<a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=o&multi_visit_package=false">
					Add OP Package
				</a>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td>
				<a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=i&multi_visit_package=false">
					Add IP Package
				</a>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td>
				<a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=d&multi_visit_package=false">
					Add Diag Package
				</a>
			</td>
			<c:if test="${mod_adv_packages == 'Y'}">
				<td>&nbsp;|&nbsp;</td>
				<td>
				 <a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=add&package_type=o&multi_visit_package=true">Add Multi Visit Package</a>
				</td>
			</c:if>
			<c:if test="${packageDetails.map.type == 'Package'}">
				<td>&nbsp;|&nbsp;</td>
				<td>
					<a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=getEditPackageCharges
						&amp;packId=${packageDetails.map.package_id }&amp;org_id=${ifn:cleanURL(param.org_id)}&amp;multi_visit_package=${ifn:cleanURL(param.multi_visit_package)}">Edit Charges
					</a>
					&nbsp;
				</td>
			</c:if>
			<td>
				<insta:screenlink screenId="mas_packages_applicability" extraParam="?_method=getScreen&packId=${param.packId}&org_id=${param.org_id}&multi_visit_package=${param.multi_visit_package}"
					label="Package Applicability" addPipe="true"/>
			</td>
		</c:if>
	</tr>
	</table>


	<div class="legend">
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></img></div>
		<div class="flagText">Cancelled</div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></img></div>
		<div class="flagText">New</div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></img></div>
		<div class="flagText">Incompatible Rateplan</div>
	</div>
	<insta:AddOrderDialog visitType="${package_type}" includeOtDocCharges="${showOperations}?'Y':'N'"/>
	<script type="text/javascript">
	packageComponents = ${numItems};
	mealTimingsRequired = false;
	equipTimingsRequired = false;
	var collapsiblePanel1 = null;
	if (${mod_adv_packages == 'Y' && type == 'Package'})
		collapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
	</script>
</body>
</html>
