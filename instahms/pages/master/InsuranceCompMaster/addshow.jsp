<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insurance Company - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="/master/InsuranceCompMaster/insurancecompmaster.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<insta:js-bundle prefix="masters.insurance.common"/>
	<script>

		function doCancel() {
			window.location.href="${cpath}/master/InsuranceCompMaster.do?_method=list";
		}
		<c:if test="${param._method != 'add'}">
      		Insta.masterData=${insuranceCompaniesLists};
		</c:if>




	</script>
</head>

<body onload="init();">
<c:choose>
	<c:when test="${param._method !='add'}">
		<h1 style="float:left">Edit Insurance Company Details</h1>
		<c:url var ="searchUrl" value="/master/InsuranceCompMaster.do"/>
		<insta:findbykey keys="insurance_co_name,insurance_co_id" fieldName="insurance_co_id" method="show" url="${searchUrl}"/>
	</c:when>
	<c:otherwise>
		<h1>Add Insurance Company Details</h1>
	</c:otherwise>
</c:choose>


<insta:feedback-panel/>

<form onsubmit="return validateForm();" name="insuranceCompMasterForm"  enctype="multipart/form-data" action="InsuranceCompMaster.do?_method=${param._method == 'add' ? 'create' : 'update'}" method="POST" >

<input type="hidden" name="insurance_co_id" id="insurance_co_id" value="${bean.map.insurance_co_id}">

<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Inurance Company Details</legend>

	<table class="formtable">
		<tr>
			<td class="formlabel">Insurance Comp Name:</td>
			<td><input type="text" name="insurance_co_name" value="${bean.map.insurance_co_name}"/></td>
			<td class="formlabel">Default Rate Plan:</td>
			<td>
				<insta:selectdb name="default_rate_plan" value="${bean.map.default_rate_plan}"
					table="organization_details" valuecol="org_id" displaycol="org_name"
					dummyvalue="-- Select --" dummyvalueId="" orderby="org_name"/>
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
					optexts="Active,Inactive" />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Address:</td>
			<td colspan="2"><textarea rows="2" name="insurance_co_address">${bean.map.insurance_co_address}</textarea></td>
		</tr>

		<tr>
			<td class="formlabel">City:</td>
			<td><input type="text" name="insurance_co_city" value="${bean.map.insurance_co_city}"/></td>
			<td class="formlabel">State:</td>
			<td><input type="text" name="insurance_co_state" value="${bean.map.insurance_co_state}"/></td>
			<td class="formlabel">Country:</td>
			<td><input type="text" name="insurance_co_country" value="${bean.map.insurance_co_country}"/></td>
		</tr>

		<tr>
			<td class="formlabel">Mobile:</td>
			<td><input type="text" name="insurance_co_phone" value="${bean.map.insurance_co_phone}"/></td>
			<td class="formlabel">Email:</td>
			<td><input type="text" name="insurance_co_email" value="${bean.map.insurance_co_email}"/></td>
			<td class="formlabel">Tax Identification Number:</td>
			<td><input type="text" name="tin_number" value="${bean.map.tin_number}"/></td>
		</tr>
		<tr>
		    <td class="formlabel">Company Rules Document:</td>
		    <td colspan="3">
		            <input type="file" name="insuruledoc"  accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
		            <c:if test="${not empty bean.map.insurance_rules_doc_name}">
						<c:url var="insUrl" value="/master/InsuranceCompMaster.do">
							<c:param name="_method" value="getviewInsuDocument"/>
							<c:param name="inscoid" value="${bean.map.insurance_co_id}"/>
						</c:url>
						<a href="${insUrl}">View Current Document</a>
					</c:if>
		    </td>
			<td class="formlabel"><insta:ltext key="masters.insurance.common.interfacecode" />:</td>
			<td><input type="text" name="interface_code" maxlength="100" value="${ifn:cleanHtmlAttribute(bean.map.interface_code)}"/></td>		    
		</tr>
		<tr>
			<td class="formlabel">Insurance Item Category:</td>
			<td><select name="insurance_category" id="insurance_category"
				class="listbox" multiple="true" style="width:12em;height:10em;padding-left:3px;color:#666666;">
				<c:forEach items="${insuranceCategories}" var="insuCat">
					<c:set var="attr" value="" />
					<c:forEach items="${mappedCategory}"
						var="selectedCategory">
						<c:if test="${selectedCategory.map.insurance_category_id==insuCat.map.insurance_category_id}">
							<c:set var="attr" value="selected='true'" />
						</c:if>
					</c:forEach>
					<option value="${insuCat.map.insurance_category_id}" ${attr}>${insuCat.map.insurance_category_name}</option>
				</c:forEach>
			</select></td>
		</tr>

 	</table>

</fieldset>

<fieldset class="fieldSetBorder" style="width:726">
  <legend class="fieldSetLabel" >Health Authority Code</legend>
  <table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="insCompHaCodeTable">
    <tr>
      <th>Health Authority</th>
      <th style="width:200px;">Insurance Company Code</th>
      <th>&nbsp;</th>
      <th>&nbsp;</th>
    </tr>
       <c:forEach items="${healthAuthorityCodes}" var="st" varStatus="status">
	   <c:set var="i" value="${status.index + 1}"/>
      	<tr>
	        <td class="forminfo" style="width:300px;" valign="middle">
	        <label id="healthAuth${i}">${st.map.health_authority}</label>

	          <input type="hidden" name="h_health_authority" id='h_health_authority${i}' value="${st.map.health_authority}"/>
	          <input type="hidden" name="h_ha_insurance_co_code_id" id="h_ha_insurance_co_code_id${i}" value="${st.map.ha_insurance_co_code_id}">
	          <input type="hidden" name="hInsCompId" id='hInsCompId${i}' value="${st.map.insurance_co_id}"/>
	          <input type="hidden" name="hacodeoldrnew" id='hacodeoldrnew${i }' value="old"/>
	        </td>
	        <td align="center" style="width:400px;">
	        	<label id="h_ha_code${i}">${st.map.insurance_co_code}</label>
	        	<input type="hidden" name="h_ins_comp_code" id="h_ins_comp_code${i}" value="${st.map.insurance_co_code}"/>
	        </td>
	        <td align="center"> <img src="${cpath}/icons/Delete.png" name="haDelItem" id="haDelItem${i }" onclick="deleteInsCompHaCodeItem(this, ${i})">
	          <input type="hidden" name="h_ha_deleted" id="h_ha_deleted${i }" value="false"/>
	        </td>
	        <td class="forminfo">
	          <button name="haEditBut" id="haEditBut${i}" onclick="editInsCompHaCodeDialog(${i}); return false;" class="imgButton" accesskey="U" title="Edit Insurance Company Health Authority Code">
	            <img class="button" name="haEdit" id="haEdit1" src="../icons/Edit.png"
	                  style="cursor:pointer;" >
	          </button>
	        </td>
      	</tr>
    </c:forEach>

    <tr>
      <td colspan="8" style="text-align:right">
        <input type="button" name="btnAddInsCompHaCode" id="btnAddInsCompHaCode1" value="+" class="plus"
        onclick="getInsCompHaCodeDialog(1);"/> </td>
    </tr>
  </table>
</fieldset>

<div id="insCompHaCodeDialog" style="visibility:hidden">
  <div class="bd">
    <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add/Edit&nbsp;Health&nbsp;Authority&nbsp;Code&nbsp;</legend>
    <table class="formtable" cellpadding="0" cellspacing="0">
      <tr>
        <th>Health Authority</th>
        <th>Insurance Company Code</th>
      </tr>
      <tr>
        <th>
        	<select name="health_authority" class="dropdown">
        		<option value="">-- Select --</option>
        		<c:forEach var="healthAuth" items="${healthAuthorities}">
        			<option value="${healthAuth.map.health_authority}">${healthAuth.map.health_authority}</option>
        		</c:forEach>
        	</select>
        </th>
        <th>
        	<input type="text" name="insurance_co_code" id="insurance_co_code" value="">
        </th>
      </tr>
    </table>
    </fieldset>
    <input type="button" value="Add" onclick="AddRecord();"/>
    <input type="button" value="Close" onclick="handleInsCompHaCodeCancel();" />
  </div>
</div>

		<div class="screenActions">
			<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/InsuranceCompMaster.do?_method=add'">Add</a>
			|
			</c:if>
			<a href="#" onclick="doCancel();">Insurance Company List</a>
		</div>
	</form>
</body>
</html>

