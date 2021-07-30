<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@page import="com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Item - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="masters/storesitemmaster.js" />
<insta:link type="script" file="masters/supplieritemcenters.js" />
<c:set var="defaultValue" value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}"/>

<style>
.grid{margin:5px 7px 5px 7px;padding:5px 7px 5px 7px;}
input.num {text-align: right; width: 6em;}

</style>
<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>"/>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<c:set var="force_generate_cust_item_code" value='<%=GenericPreferencesDAO.getAllPrefs().get("force_generate_cust_item_code")%>'/>
  <script>
	  var popurl = '${pageContext.request.contextPath}';
	  var opeartion = '${ifn:cleanJavaScript(param._method)}';
	  var identList = ${identList};
	  var manfList = ${manfList};
	  var prefBarCode = '${prefbarcode}';
	  var savedBarCode = '${bean.map.item_barcode_id}';
	  var itemDetailsList = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER) %>;
	  var jGenericNames = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_ACTIVE_GENERICS_IN_MASTER) %>;
	  var packageUOMs = ${packageUOMList};
	  var isuuePackageList = ${isuuePackageList};
	  var issueUOMS = ${issueUOMs};
	  var subGroups = <%= new flexjson.JSONSerializer().serialize(ServiceSubGroupDAO.getAllActiveServiceSubGroups()) %>
	  var selectedCenter = '';
	  <c:if test="${max_centers_inc_default>1}">
	  	var suppliersList = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_CENTER_SUPPLIERS) %>;
	  </c:if>
	  
	  var masterModified = '${masterModified}';
	  var healthAuthSpecificCodeTypesJson = <%= request.getAttribute("healthAuthSpecificCodeTypesJson") %>;
	
	   <c:if test="${param._method != 'add'}">
	         Insta.masterData=${storeItemsLists};
	   </c:if>
	   var itemGroupList = ${itemGroupListJson};
	   var itemSubGroupList = ${itemSubGroupListJson};
  </script>
</head>
<body onload="addShowinit();addShowautomanf();initGenericAutoComplete(jGenericNames);setPackSize();changePackageUom(document.getElementById('issue_units'));init();itemsubgroupinit();" class="yui-skin-sam">
<c:choose>
    <c:when test="${param._method != 'add'}">
        <h1 style="float:left">Edit Stores Item</h1>
        <c:url var="searchUrl" value="/master/StoresItemMaster.do"/>
        <insta:findbykey keys="medicine_name,medicine_id" fieldName="medicine_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Stores Item </h1>
    </c:otherwise>
</c:choose>

<form action="StoresItemMaster.do" name="storesitemmasterform" method="post">
  <input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
  <input type="hidden" name="manf_code" id="manf_code" value="${bean.map.manf_code }"/>
  <input type="hidden" name="originalPkgSize" id="originalPkgSize" value="${bean.map.issue_base_unit}">
  <input type="hidden" name="changedPkgSize" id="changedPkgSize" value="no">
  <input type="hidden" name="updateMrpCp" id="updateMrpCp" value="no">
  <input type="hidden" name="subGroupId" value="${bean.map.service_sub_group_id}">
  <c:if test="${param._method == 'show'}">
    <input type="hidden" name="medicine_id" id="medicine_id" value="${bean.map.medicine_id}"/>
  </c:if>
  <insta:feedback-panel/>
<div id="itemDialog" style="visibility:hidden">
  <div class="bd">
    <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add/Edit&nbsp;Store&nbsp;Wise&nbsp;List</legend>
    <table class="formtable" cellpadding="0" cellspacing="0">
      <tr>
        <th>Store</th>
        <th>Bin/Rack</th>
        <th>Danger Level</th>
        <th>Min Level</th>
        <th>Reorder Level</th>
        <th>Max Level</th>
      </tr>
      <tr>
        <th><insta:selectdb name="store_id" value="" dummyvalue="-- Select --" table="stores" valuecol="dept_id" displaycol="dept_name" orderby="dept_name"/></th>
        <th><input type="text" name="store_bin" id="store_bin" maxlength="50"></th>
        <th><input type="text" name="danger_level" id="danger_level"  class="number"  onkeypress="return enterNumOnly(event)" onBlur="trimNum(event, this);"maxlength="9"></th>
        <th><input type="text" name="min_level" id="min_level"  class="number"  onkeypress="return enterNumOnly(event)" onBlur="trimNum(event, this);" maxlength="9"></th>
        <th><input type="text" name="reorder_level" id="reorder_level"   class="number" onkeypress="return enterNumOnly(event)"  onBlur="trimNum(event, this);" maxlength="9"></th>
        <th><input type="text" name="max_level" id="max_level"   class="number" onkeypress="return enterNumOnly(event)"  onBlur="trimNum(event, this);" maxlength="9"></th>
      </tr>
    </table>
    </fieldset>
    <input type="button" value="Save" onclick="Add();"/>
    <input type="button" value="Close" onclick="handleCancel();" />
  </div>
</div>

<div id="itemHaCodeTypeDialog" style="visibility:hidden">
  <div class="bd">
    <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add/Edit&nbsp;Health&nbsp;Authority&nbsp;Code&nbsp;Type</legend>
    <table class="formtable" cellpadding="0" cellspacing="0">
      <tr>
        <th>Health Authority</th>
        <th>Code Type</th>
      </tr>
      <tr>
        <th>
        	<select name="health_authority" class="dropdown" onchange="loadCodeTypes(this);">
        		<option value="">-- Select --</option>
        		<c:forEach var="healthAuth" items="${healthAuthorities}">
        			<option value="${healthAuth.map.health_authority}">${healthAuth.map.health_authority}</option>
        		</c:forEach>
        	</select>
        </th>
        <th>
        	<select name="code_type" id="code_type" class="dropdown"></select>
        </th>
      </tr>
    </table>
    </fieldset>
    <input type="button" value="Add" onclick="AddRecord();"/>
    <input type="button" value="Close" onclick="handleItemHaCodeTypesCancel();" />
  </div>
</div>


<fieldset class="fieldSetBorder">
  <legend class="fieldSetLabel"> Item Details</legend>
  <table class="formtable" width="100%">
  	<tr>

  		 <fmt:formatDate pattern="dd-MM-yyyy" value="${last_updated_item.map.updated_timestamp}" var="upated_date"/>
		<fmt:formatDate pattern="HH:mm" value="${last_updated_item.map.updated_timestamp}" var="updated_time"/>
  		<td class="formlabel" >Last Updated Item:</td>
	   	<td class="forminfo" colspan="3"> <insta:truncLabel value="${last_updated_item.map.medicine_name }" length="30"/>at <b>${upated_date } ${updated_time }</b></td>
  	</tr>
    <tr>
      <td class="formlabel">Item Name:</td>
      <td colspan="2">
      <div id="autoAddItem" class="autoComplete" >
        <input type="text" name="medicine_name" id="medicine_name" 
        value="<c:out value='${bean.map.medicine_name}'/>" style = "width:35em"
        maxlength="100" title="Item Name is required and max length of name can be 100" onchange="trimMedName(this);"/>
        <span class="star">*</span>
        <div id="itemcontainer" style = "width:35em"></div>
        </div>
      </td>
      
      <td class="formlabel">Shorter Name:</td>
      <td>
        <input type="text" name="medicine_short_name"  id="medicine_short_name" maxlength="199"
        value="${bean.map.medicine_short_name}"/><span class="star">&nbsp;*</span>
      </td>
      
      <td class="formlabel">Status:</td>
      <td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active, Inactive" /></td>
    </tr>
    
    <tr>

      <td class="formlabel">Category:</td>
      <td>
        <insta:selectdb name="med_category_id"  id="med_category_id" value="${bean.map.med_category_id}"
            dummyvalue="-- Select --" table="store_category_master"
            valuecol="category_id" displaycol="category" orderby="category" class="dropdown"
            title="Category is required" onchange="setPackSize(); putAsterick();"/><span class="star">*</span>
      </td>
      <c:if test="${param._method == 'add'}">
      <td class="formlabel" colspan="2">Generate Item Code:</td>
      <td ><input type="checkbox" name="cust_item_code_chk" id="cust_item_code_chk" ${param._method == 'add' && !(force_generate_cust_item_code == 'Y') ? '' : 'disabled'} ${param._method == 'add' && force_generate_cust_item_code == 'Y' ? 'checked' : ''} onclick="hidecustitemcode();"/></td>
      </c:if>
	  <c:if test="${param._method == 'add'}"><td class="formlabel" >Custom Item Code :</td></c:if>
	   <c:if test="${param._method != 'add'}"><td class="formlabel" colspan="2">Item Code :</td></c:if>
      <td>
        <input type="text" name="cust_item_code" id="cust_item_code" value="<c:out value='${bean.map.cust_item_code}'/>"
        maxlength="20" title="Max length of item code can be 20" ${(bean.map.cust_item_code eq null || bean.map.cust_item_code == '') && !(force_generate_cust_item_code == 'Y')  ? '' : 'disabled'} />
      </td>
      
    </tr>

    <tr>
    <td class="formlabel">Manufacturer:</td>
      <td colspan="2">
        <div id="automanf" class="autoComplete" >
          <input type="text" name="manf_name" id="manf_name" value="${bean.map.manf_name}" style="width: 35em"
          maxlength="100" title="Manufacturer Name is required and max length of name can be 100"/>
          <div id="manfcontainer" class="scrolForContainer" style="width: 35em"></div>
        </div><div style="float: left" class="star">&nbsp;*</span>
      </td>
      
      <td class="formlabel">Strength: </td>
      <td><input type="text" name="item_strength" id="item_strength" maxlength="50" value='<c:out value="${bean.map.item_strength}"/>'></td>
      
      <td class="formlabel">Strength Units: </td>
      <td>
        <insta:selectdb name="item_strength_units" id="item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
          dummyvalue="-- Select --" value="${bean.map.item_strength_units}"/>
      </td>
    </tr>
    <tr>
    <td class="formlabel">Item Form: </td>
      <td><insta:selectdb name="item_form_id" id="item_form_id" table="item_form_master" value="${bean.map.item_form_id}"
          displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
          dummyvalue="-- Select --" dummyvalueId=""/><span class="star" id="itemFormDrug" style="visibility: hidden">&nbsp;*</span></td>
      
      <td class="formlabel">Route: </td>
      <td><select name="route_of_admin" id="route_of_admin" size="5" multiple="multiple" style = "width:34em">
          <c:forEach var="route" items="${medicineRouteList}">
            <c:set var="selected" value=""/>
            <c:forTokens delims="," items="${bean.map.route_of_admin}" var="routeId" >
              <c:if test="${route.map.route_id == routeId}">
                <c:set var="selected" value="selected"/>
              </c:if>
            </c:forTokens>
            <option value="${route.map.route_id}" ${selected} title="${route.map.route_name}">${route.map.route_name}</option>
          </c:forEach>
        </select>
        <span class="star" id="routAdminDrug" style="visibility: hidden">&nbsp;*</span>
      </td>
      
      <td class="formlabel" colspan="2">Value:</td>
      <td>
        <insta:selectoptions name="value" value="${bean.map.value eq null? 'M' :bean.map.value}"
        opvalues="H,M,L" optexts="High, Medium, Low" />
      </td>
    </tr>
    <tr>
      <td class="formlabel">Unit UOM:</td>
      <td>
        <c:set var="issueValue" value="${param._method ne 'add' ? bean.map.issue_units : defIssUOM}"/>
        <c:set var="uomdisabled" value="${param._method eq 'show' ? 'disabled' : ''}"/>
        <input type="hidden" name="issue_units_hidden" id="issue_units_hidden" value="${bean.map.issue_units }">
        <select name="issue_units" id="issue_units" style="width:135px" ${ uomdisabled}
           class="dropdown" onchange="changePackageUom(this)">
          <option value="">-- Select --</option>
          <c:forEach items="${issueUOMListWithoutJson}" var="issueUomList">
          <option value="${fn:escapeXml(issueUomList.issue_uom)}"
            ${issueValue == issueUomList.issue_uom ? 'selected' :''}>
          ${issueUomList.issue_uom}
          </option>
          </c:forEach>
        </select>
      </td>
      <td class="formlabel" colspan="2">Package UOM:</td>
      <td>
        <c:set var="packageValue" value="${param._method ne 'add' ? bean.map.package_uom : defPkgUOM}"/>
        <input type="hidden" name="package_uom_hidden" id="package_uom_hidden" value="${bean.map.package_uom }">
        <select name="package_uom" id="package_uom" style="width:135px" class="dropdown"  ${ uomdisabled} onchange="changePackageSize(this)">
          <option value="">-- Select --</option>
          <c:forEach items="${packageUOMListWithoutJson}" var="packageUomList">
          <option value="${fn:escapeXml(packageUomList.package_uom)}"
            ${packageValue == packageUomList.package_uom ? 'selected' :''}>
          ${packageUomList.package_uom}
          </option>
          </c:forEach>
        </select>
      </td>
      <td class="formlabel">Package Size:</td>
      <td>
        <label id="issue_base_unit_label" style="float:left">${param._method ne 'add' ? bean.map.issue_base_unit : defPkgSize}</label>
        <input type="hidden" name="issue_base_unit" id ="issue_base_unit"value="${param._method ne 'add' ? bean.map.issue_base_unit : defPkgSize}">
      </td>
    </tr>

    <tr>
      <td class="formlabel">Package Type:</td>
      <td><input type="text" name="package_type" value="${bean.map.package_type}"/></td>
      <td class="formlabel" colspan="2">Consumption UOM:</td>
      <td><insta:selectdb name="cons_uom_id" id="cons_uom_id" table="consumption_uom_master" value="${bean.map.cons_uom_id}"
          displaycol="consumption_uom" valuecol="cons_uom_id" orderby="consumption_uom"
          dummyvalue="-- Select --" dummyvalueId=""/><span class="star" id="consUomDrug" style="visibility: hidden">&nbsp;*</span></td>
      <td class="formlabel">Consumption Capacity:</td>
      <td>
        <input type="text" name="consumption_capacity" id="consumption_capacity" value="${bean.map.consumption_capacity}">
      </td>
    </tr>

    <tr>
      <td class="formlabel">Generic Name:<span class="star" id="genericNameDrug" style="visibility: hidden">&nbsp;*</span></td>
      <td valign="top" colspan="2">
        <div id="generic_wrapper" style="width: 138px; padding-bottom:0.2em;">
          <input type="text" name="generic_name" id="generic_name" style="width: 35em"  maxlength="100"  value="${bean.map.generic_name}"/>
          <div id="generic_dropdown" class="scrolForContainer" style="width: 35em"></div>
        </div>
      </td>

      <td class="formlabel">Service Group:</td>
      <td>
        <insta:selectdb id="service_group_id" name="service_group_id" value="${bean.map.service_group_id}"
            table="service_groups" class="dropdown" dummyvalue="-- Select --"
            valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()"></insta:selectdb>
            <span class="star">*</span>
      </td>
      <td class="formlabel">Service Sub Group:</td>
      <td>
        <select name="service_sub_group_id" id="service_sub_group_id" class="dropdown">
          <option value="">-- Select --</option>
        </select><span class="star">&nbsp;*</span>
      </td>

    </tr>

    <tr>
      <td class="formlabel">Max Cost Price: </td>
      <td>
        <input id="max_cost_price" type="text" value="${bean.map.max_cost_price}"
          onkeypress="return enterNumAndDot(event);"
          maxlength="13" name="max_cost_price"/>
      </td>
      <td class="formlabel" colspan="2">Supplier Name: </td>
      <td>
        <input type="text" name="supplier_name" id="supplier_name" value="${bean.map.supplier_name}" />
      </td>
        <c:if test="${max_centers_inc_default == 1}">
     		 <td class="formlabel">Preferred Supplier:</td>
      	   	 <td>
       		 	 <insta:selectdb id="preferred_supplier" name="preferred_supplier" value="${supplierId}"
         			 table="supplier_master" valuecol="supplier_code" displaycol="supplier_name" dummyvalue="-- Select --" orderby="supplier_name"/>
        	 </td>
       </c:if>
    </tr>
    <tr>
      <td class="formlabel">Invoice Details: </td>
      <td>
        <input type="text" name="invoice_details" id="invoice_details" value="${bean.map.invoice_details}" />
      </td>
      <td class="formlabel" colspan="2">Control Type:</td>
      <td>
        <c:if test="${param._method == 'add'}">
          <insta:selectdb id="control_type_id" name="control_type_id" value="${defaultControlTypeID}"
          table="store_item_controltype" valuecol="control_type_id" displaycol="control_type_name" filtered="false"/>
        </c:if>
        <c:if test="${param._method == 'show'}">
          <insta:selectdb id="control_type_id" name="control_type_id" value="${bean.map.control_type_id}"
          table="store_item_controltype" valuecol="control_type_id" displaycol="control_type_name" filtered="false"/>
        </c:if>
      </td>
      <td class="formlabel">Insurance Category:</td>
      <td>
        <insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/>
      </td>
    </tr>
    <tr>
      <td class="formlabel">Pre Auth Required</td>
      <td><insta:selectoptions name="prior_auth_required" value="${bean.map.prior_auth_required}" optexts="Never,Sometimes,Always" opvalues="N,S,A" /></td>
      <td class="formlabel" colspan="2">Billing Group:</td>
	  <td>
		<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
			displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
	  </td>
    </tr>
    <tr>
      <c:if test="${prefbarcode eq 'Y'}">
        <c:if test="${param._method == 'add'}">
          <td class="formlabel">Generate Barcode:</td>
          <td ><input type="checkbox" name="itembarcodechk" id="itembarcodechk" onclick="hidecode();" /></td>
        </c:if>
        <c:if test="${param._method == 'add'}"><td class="formlabel" colspan="2">Custom Barcode :</td> </c:if>
        <c:if test="${param._method != 'add'}"><td class="formlabel">Barcode :</td> </c:if>
        <td><input type="text" id="item_barcode_id" name="item_barcode_id"  value="${bean.map.item_barcode_id}"/></td>
      </c:if>
    </tr>
    <tr>
      <td class="formlabel">Tax Basis:</td>
      <td>
        <insta:selectoptions name="tax_type" value="${bean.map.tax_type}" opvalues="MB,M,CB,C" optexts="MRP Based(with bonus),MRP Based(without bonus),CP Based(with bonus),CP Based(without bonus)" />
      </td>
      <td class="formlabel" colspan="2">Tax(%):</td>
      <td>
        <input type="text" name="tax_rate" onkeypress="return enterNumAndDot(event);"
            onblur="onChangeTaxRate(this);" value="${empty bean ? defaultValue : bean.map.tax_rate}" />
      </td>
      <td class="formlabel">Bin/Rack:</td>
      <td>
        <input type="text" name="bin" maxlength="25" value="${bean.map.bin}" />
      </td>
    </tr>
    <tr>
      <td class="formlabel">Batch Number Applicable:</td>
      <td>
        <insta:selectoptions name="batch_no_applicable" value="${bean.map.batch_no_applicable}"
          opvalues="Y,N" optexts="Yes,No" />
      </td>
      <td class="formlabel" colspan="2">Item Selling Price:</td>
      <td>
        <input type="text" name="item_selling_price" onkeypress="return enterNumAndDot(event);"
            value="${bean.map.item_selling_price}" />
      </td>
	  <td class="formlabel">High Cost Consumables:</td>
      <td>
        <insta:selectoptions name="high_cost_consumable" value="${bean.map.high_cost_consumable}"
          opvalues="N,Y" optexts="No,Yes" />
      </td>
    </tr>
    <tr>
		<td class="formlabel">Allow Zero Claim Amount:</td>
		<td>
			<insta:selectoptions name="allow_zero_claim_amount" value="${empty bean.map.allow_zero_claim_amount ? 'n' : bean.map.allow_zero_claim_amount}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
		</td>
    </tr>
  </table>
</fieldset>

<fieldset class="fieldSetBorder">
  <legend class="fieldSetLabel" >Store Wise List</legend>
  <table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="medtabel">
    <tr >
      <th>Store</th>
      <th>Bin/Rack</th>
      <th>Danger Level</th>
      <th>Min Level</th>
      <th>Reorder Level</th>
      <th>Max Level</th>
      <th></th>
      <th></th>
    </tr>

    <c:forEach items="${swItemList}" var="st" varStatus="status">
      <c:set var="i" value="${status.index + 1}"/>
      <tr>
        <td class="forminfo" style="width:10em" valign="middle">
        <label id="depLabel${i }">${st.map.dept_name }</label>

          <input type="hidden" name="hdepartment" id='hdepartment${i }' value="${st.map.dept_id }"/>
          <input type="hidden" name="hdepartmentname" id="hdepartmentname${i }" value="${st.map.dept_name }">
          <input type="hidden" name="hmedicineId" id='hmedicineId${i }' value="${st.map.medicine_id }"/>
          <input type="hidden" name="deptoldrnew" id='deptoldrnew${i }' value="old"/>
        </td>
        <td align="center"><input type="text"  name="hiddenBin" id='hiddenBin${i}' value="${st.map.bin}"   readonly/></td>
        <td align="center"><input type="text" class="num"  name="hiddenDangerLevel" id='hiddenDangerLevel${i }' value="${st.map.danger_level }"  readonly/></td>
        <td align="center"><input type="text" class="num"  name="hiddenMinLevel" id='hiddenMinLevel${i }' value="${st.map.min_level }"   readonly/></td>
        <td align="center"><input type="text" class="num"  name="hiddenReorderLevel" id='hiddenReorderLevel${i }' value="${st.map.reorder_level }"  readonly/></td>
        <td align="center"><input type="text" class="num"  name="hiddenMaxLevel" id='hiddenMaxLevel${i }' value="${st.map.max_level }"   readonly/></td>
        <td align="center"> <img src="${cpath}/icons/Delete.png" name="delItem" id="delItem${i }" onclick="deleteItem(this, ${i})">
          <input type="hidden" name="hdeleted" id="hdeleted${i }" value="false"/>
        </td>
        <td class="forminfo">
          <button name="editBut" id="editBut${i}" onclick="editItemGroupDialog(${i}); return false;" class="imgButton" accesskey="U" title="Edit Item ">
            <img class="button" name="edit" id="edit1" src="../icons/Edit.png"
                  style="cursor:pointer;" >
          </button>
        </td>
      </tr>
    </c:forEach>

    <tr>
      <td colspan="8" style="text-align:right">
        <input type="button" name="btnAddItems" id="btnAddItems1" value="+" class="plus"
        onclick="getItemGroupDialog(1);"/> </td>
    </tr>
  </table>
</fieldset>

<fieldset class="fieldSetBorder" style="width:726">
  <legend class="fieldSetLabel" >Health Authority Code Type</legend>
  <table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="itemHaCodeTypeTable">
    <tr>
      <th>Health Authority</th>
      <th style="width:200px;">Code Type</th>
      <th>&nbsp;</th>
      <th>&nbsp;</th>
    </tr>
       <c:forEach items="${healthAuthorityCodeTypes}" var="st" varStatus="status">
	   <c:set var="i" value="${status.index + 1}"/>
      	<tr>
	        <td class="forminfo" style="width:300px;" valign="middle">
	        <label id="healthAuth${i}">${st.map.health_authority}</label>

	          <input type="hidden" name="h_health_authority" id='h_health_authority${i}' value="${st.map.health_authority}"/>
	          <input type="hidden" name="h_ha_code_type_id" id="h_ha_code_type_id${i}" value="${st.map.ha_code_type_id}">
	          <input type="hidden" name="hmedicineId" id='hmedicineId${i}' value="${st.map.medicine_id}"/>
	          <input type="hidden" name="hacodeoldrnew" id='hacodeoldrnew${i }' value="old"/>
	        </td>
	        <td align="center" style="width:400px;">
	        	<label id="h_ha_code_type${i}">${st.map.code_type}</label>
	        	<input type="hidden" name="h_code_type" id="h_code_type${i}" value="${st.map.code_type}"/>
	        </td>
	        <td align="center"> <img src="${cpath}/icons/Delete.png" name="haDelItem" id="haDelItem${i}" onclick="deleteHaCodeTypeItem(this, ${i})">
	          <input type="hidden" name="h_ha_deleted" id="h_ha_deleted${i }" value="false"/>
	        </td>
	        <td class="forminfo">
	          <button name="haEditBut" id="haEditBut${i}" onclick="editItemHaCodeDialog(${i}); return false;" class="imgButton" accesskey="U" title="Edit Item Health Authority Code Type Dialog">
	            <img class="button" name="haEdit" id="haEdit1" src="../icons/Edit.png"
	                  style="cursor:pointer;" >
	          </button>
	        </td>
      	</tr>
    </c:forEach>

    <tr>
      <td colspan="8" style="text-align:right">
        <input type="button" name="btnAddItemsHaCodeTypes" id="btnAddItemsHaCodeTypes1" value="+" class="plus"
        onclick="getItemHaCodeTypesDialog(1);"/> </td>
    </tr>
  </table>
</fieldset>
		
<insta:taxations/>
 
<c:if test="${max_centers_inc_default > 1}">
<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Preferred Suppliers</legend>
			<table style="width:300px" class="detailList" id="centersupplierTbl" >
				<tr class="header">
					<th class="first">Center</th>
					<th>Supplier</th>
					<th>&nbsp;</th>
				</tr>
					<c:forEach items="${supplierCenters}" var="supp" varStatus="st">
					<tr id="row${st.index}">
						<input type="hidden" name="center_id" id="" value="${supp.center_id}" />
						<input type="hidden" name="supplier_code" id="" value="${supp.supplier_code}" />
						<input type="hidden" name="prefer_supplier_id" id="" value="${supp.prefer_supplier_id}" />
						<input type="hidden" name="selectedrow" id="selectedrow${st.index+1}" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<td>${supp.center_name}</td>
						<td>${supp.supplier_name}</td>
						<td style="text-align: center"><img src="${cpath}/icons/Delete.png" onclick="changeElsColor(${st.index+1}, this);"/></td>
					</tr>
					<c:set var="newIndexFORdummyRow" value="${st.index+1}"/>
					</c:forEach>
					<tr id="" style="display: none">
						<input type="hidden" name="center_id" id="" value="" />
						<input type="hidden" name="supplier_code" id="" value="" />
						<input type="hidden" name="prefer_supplier_id" id="" value="" />
						<input type="hidden" name="selectedrow" id="selectedrow0" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<td></td>
						<td></td>
						<td style="text-align: center"><img src="${cpath}/icons/Delete.png" onclick="changeElsColor('${newIndexFORdummyRow}', this);"/></td>
					</tr>
					<tr>
						<td colspan="2"></td>
						<td style="text-align: center">
							<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="showDialog(this)" >
								<img src="${cpath}/icons/Add.png"/>
							</button>
						</td>
					</tr>
			</table>
		</fieldset>
</c:if>
<div class="screenActions">
  <input type="submit" name="save" value="Save" class="button" onclick="return validateUpdateMRP(issue_base_unit);">
  |
  <c:if test="${param._method != 'add'}">
  <a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/StoresItemMaster.do?_method=add'">Add</a>
  |
  </c:if>
  <a href="${cpath }/master/StoresItemMaster.do?_method=list&sortOrder=medicine_name&sortReverse=false&status=A">Back To DashBoard</a>
  |
  <a href="${cpath }/pages/master/SupplierRateContractMaster.do?_method=list&status=A">Supplier Rate Contracts</a>
  <c:if test="${param._method == 'show' && prefbarcode eq 'Y'}">
  |
  <a href="javascript:void(0)" onclick="return getBarcodePrint();">Barcode Print</a>
  </c:if>
  <c:if test="${param._method == 'show' && not empty storeRatePlans}">
  <insta:screenlink addPipe="true" screenId="stores_item_rates_mas" label="Edit Selling Rates" extraParam="?_method=show&medicine_id=${bean.map.medicine_id}"/>
  </c:if>|
  <c:if test="${param._method == 'show'}">
 	  <a href="${cpath }/master/StoresItemMaster.do?_method=editItemCode&medicine_id=${bean.map.medicine_id}">Edit Item Codes</a>
  </c:if>
</div>

</form>
<div name="centersupplierDIV" id="centersupplierDIV" style="visibility: none">
		<div class="bd">
			<fieldSet class="fieldSetBorder">
				<table class="formTable">
					<tr>
						<td>Center:</td>
						<td>
							<select class="dropdown" name="centerId" id="centerId" onchange="onSelectCenter();">
								<option value="">-- Select --</option>
								<c:forEach items="${centerList}" var="center">
										<c:if test="${center.map.center_id != 0}">
											<option value="${center.map.center_id}">${center.map.center_name}</option>
										</c:if>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td>Supplier</td>
						<td>
							<select class="dropdown" name="supplier_id" id="supplier_id">
								<option value="">-- Select --</option>
							</select>
						</td>
					</tr>
				</table>
			</fieldSet>
			<div>
				<input type="button" value="Add" onclick="addToTable()"> |
				<input type="button" value="Cancel" onclick="handleSupplierCancel()">
			</div>
		</div>
	</div>
	
</body>
</html>
