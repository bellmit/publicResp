<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<html>
<head>
<title>Store Supplier Details - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="/masters/Supplier.js"/>
  <script>
     <c:if test="${not empty suppdto}">
         Insta.masterData=${suppliersLists};
     </c:if>
     var cityStateCountryJSON = JSON.parse('${ifn:cleanJavaScript(cityStateCountryList)}');
     var stateJSON = JSON.parse('${ifn:cleanJavaScript(stateList)}');
     var stateCountryJSON = JSON.parse('${ifn:cleanJavaScript(stateCountryList)}');
     var isRegistered = '${suppdto.map.is_registered}';
     var isEdit = '${not empty suppdto }';
     var isTcsApplicable = '${suppdto.map.tcs_applicable}';
     
  </script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath }"/>
<body onload="init();" class="yui-skin-sam" >
<c:choose>
    <c:when test="${not empty suppdto }">
        <h1 style="float:left">Edit Supplier Details</h1>
        <c:url var="searchUrl" value="/pages/masters/insta/stores/suppdetails.do"/>
        <insta:findbykey keys="supplier_name,supplier_code" fieldName="supplier_id" method="getSupplierDetailsScreen" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
      <h1>Add Supplier Details</h1>
    </c:otherwise>
</c:choose>
<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>

<insta:feedback-panel/>
<form method="GET" action="" name="supplierForm">
<input type="hidden" name="operation" value="${not empty suppdto.map.supplier_code? 'update' : 'insert'}">
<input type="hidden" name="supplier_code" value = "${suppdto.map.supplier_code }">
<input type="hidden" name="supplier" value = "${suppdto.map.supplier_name }">
<input type="hidden" name="supplier_id" value = "${supplier_id}">
<input type="hidden" name="_method" value="saveSupplierDetails">

	<table>
	   <tr>
	   		<td>
	   			<div class="mainfiltercontent" style="width: 600px;">
	   				<fieldset style="width:580" class="fieldSetBorder">
	   					<legend class="fieldSetLabel"> Supplier</legend>
							 <table   class="formtable" >
							 	<tr>
			     					<td class="formlabel">Supplier Name:</td>
			     					<td><input type="text" name="supplier_name" id="supplier_name" maxlength="100" value="${suppdto.map.supplier_name }"  >
			     						<span class="star">&nbsp;*</span></td>
			   					
			     					<td class="formlabel">Supplier Code:</td>
			     					<td><input type="text" name="cust_supplier_code" id="cust_supplier_code" maxlength="100" value="${suppdto.map.cust_supplier_code }" ${(suppdto.map.cust_supplier_code eq null || suppdto.map.cust_supplier_code == '' ) ? '' : 'disabled'} ></td>
			   					</tr>
			   					<tr>
			     					<td class="formlabel">Status:</td>
			     					<td><insta:radio name="status" radioValues="A,I" value="${not empty suppdto.map.status ? suppdto.map.status : 'A'}"
			     					radioText="Active,Inactive" radioIds="active,inactive" /></td>
			   						
			   						<td class="formlabel">Registered Supplier:</td>
			   						<td>
			   							<select name="is_registered" class="dropdown" style="width:8em;" onchange="setMandotoryspan()">
			   								<option value="" selected>-- Select --</option>
			   								<option value="Y">Yes</option>
			   								<option value="N">No</option>
										</select><span class="star">&nbsp;*</span>
									</td>
			   					</tr>
			   					<tr>
			     					<td class="formlabel">Supplier Category:</td>
			     					<td><insta:selectdb name="supp_category_id" table="supplier_category_master" displaycol="supp_category_name"
			     					     valuecol="supp_category_id" filtered="false" value="${not empty suppdto.map.supp_category_id ? suppdto.map.supp_category_id : 0}"/></td>
			     					
			     					<td class="formlabel">${taxLabel == 'V' ? 'Tin' : 'GSTIN' } Number:</td>
			     					<td><input type="text" name="supplier_tin_no" id="supplier_tin_no" value="${suppdto.map.supplier_tin_no }">
										<span class="star" id="tin_mandatory" style= display:${suppdto.map.is_registered == 'Y' ? 'inline-block' :'none'}>&nbsp;*</span>
			     					</td>
			   					</tr>
			   					<tr>
			     					<td class="formlabel">Credit Period (Days):</td>
			     					<td><input type="text" name="credit_period" id="credit_period" onkeypress="return enterNumOnlyzeroToNine(event)"
			     						value="${not empty suppdto.map.credit_period ? suppdto.map.credit_period : 0}"></td>
			   					
			   						<td class="formlabel">Drug License No:</td>
			        				<td>
			        					<input type="text" name="drug_license_no" id="drug_license_no" value="${suppdto.map.drug_license_no}"/>
									</td>
			   					</tr>
			   					<tr>
			     					<td class="formlabel">PAN No:</td>
			        				<td>
			        					<input type="text" name="pan_no" id="pan_no" value="${suppdto.map.pan_no}"/>
									</td>
			   						<td class="formlabel">Corporate Identification Number (CIN):</td>
			        				<td>
			        					<input type="text" name="cin_no" id="cin_no" value="${suppdto.map.cin_no}"/>
									</td>
			   					</tr>
			   					<tr>
			     					<td class="formlabel">Apply TCS for PO/Stock Entry:</td>
			        				<td>
			        					<select name="tcs_applicable" class="dropdown" style="width:8em;">
			   								<option value="Y">Yes</option>
			   								<option value="N" selected>No</option>
										</select>
									</td>
								</tr>
							 </table>
	   				</fieldset>
	   			</div>
	   		</td>
	   		<td>
	   			<div class="mainfiltercontent" style="width: 150px;">
	   				<fieldset style="width:300" class="fieldSetBorder">
	   					<legend class="fieldSetLabel"> Region</legend>
			 				<table   class="formtable" >
			 					<tr>
			        				<td class="formlabel">City:</td>
			       					<td><input type="hidden" name="city_id" id="city_id" value="${suppdto.map.city_id}"/>
										<div id="city_state_country_wrapper" class="autoComplete">
											<input type="text" name="supplier_city" id="supplier_city"	value="${suppdto.map.supplier_city}" maxlength="250"/>
											<div id="city_state_country_dropdown" style="width:250px"></div>
										</div>
									</td>
			    				</tr>
			    				<tr>
			        				<td class="formlabel">State<span class="star" id="state_mandatory" style= display:${suppdto.map.is_registered eq 'Y' ? 'inline-block' :'none'}>&nbsp;*</span>:</td>
			       					<td><input type="hidden" name="state_id" id="state_id" value="${suppdto.map.state_id}"/>
										<div id="state_wrapper" class="autoComplete">
											<input type="text" name="supplier_state" id="supplier_state" value="${suppdto.map.supplier_state}" onchange="changeCountry()"/>
											<div id="state_dropdown" style="width:250px"></div>
										</div>
									</td>
			    				</tr>
			    				<tr>
			        				<td class="formlabel">Country:</td>
			        				<td>
			        					<input type="text" name="supplier_country" id="supplier_country" value="${suppdto.map.supplier_country}"/>
									</td>
			    				</tr>
			    				<tr>
			    					<td>&nbsp;</td>
			    					<td>&nbsp;</td>
			    				</tr>
			    				<tr>
			    					<td>&nbsp;</td>
			    					<td>&nbsp;</td>
			    				</tr>
			 				</table>
	   				</fieldset>
	   			</div>
	   		</td>
	   </tr>
	</table>
		<table>
	   <tr>
	   		<td>
	   			<div class="mainfiltercontent" style="width: 600px;">
	   				<fieldset style="width:580" class="fieldSetBorder">
	   					<legend class="fieldSetLabel"> Contact Information</legend>
							<table class="formtable" >
								<tr>
								   <td class="formlabel">Address:</td>
								   <td colspan="3"><textarea rows="4" name="supplier_address" id="supplier_address" cols="60"   onkeypress="return chk(event);"
									        onblur="chklen();"><c:out value="${suppdto.map.supplier_address}"/></textarea>(max 500 characters)</td>
								</tr>
								<tr>
								   <td class="formlabel">Phone 1:</td>
						           <td><input type="text" name="supplier_phone1" id="supplier_phone1" maxlength="20"
						           		value="${suppdto.map.supplier_phone1 }" >(Ex:080-25252522)</td>
							        <td class="formlabel">Phone 2:</td>
							        <td><input type="text" name="supplier_phone2"  id="supplier_phone2" maxlength="20"
							        	value="${suppdto.map.supplier_phone2 }"  ></td>
								</tr>
								<tr>
								 	<td class="formlabel">Postal Code:</td>
							        <td><input type="text" name="supplier_pin" id="supplier_pin" size="24"
							        	value="${suppdto.map.supplier_pin }"   maxlength="6" onChange="return checkPin(document.forms[0].supplier_pin)"
							        	onkeypress="return enterNumOnly(event)"></td>
						     	   <td class="formlabel">Fax:</td>
								   <td><input type="text" name="supplier_fax" id="supplier_fax" maxlength="20"
								   		value="${suppdto.map.supplier_fax }">(Ex:080-3938388)</td>
							    </tr>
							    <tr>
									<td class="formlabel">Email:</td>
									<td><input type="text" name="supplier_mailid" id="supplier_mailid" maxlength="50"  
										value="${suppdto.map.supplier_mailid }"  onchange="return checkmail()"></td>
									<td class="formlabel">Web Site:</td>
									<td ><input type="text" name="supplier_website" id="supplier_website" value="${suppdto.map.supplier_website }"
										maxlength="50">(www.google.com)</td>
							    </tr>
							</table>
					</fieldset>
				</div>
			</td>
			<td>
	   			<div class="mainfiltercontent" style="width: 250px;">
	   				<fieldset style="width:250" class="fieldSetBorder">
	   					<legend class="fieldSetLabel"> Help Desk</legend>
			 				<table   class="formtable" >
			 					<tr>
			        				<td class="formlabel">Name:</td>
			        				<td><input type="text" name="contact_person_name" id="contact_person_name" maxlength="99"
			        					value="${suppdto.map.contact_person_name }"></td>
			    				</tr>
			    				<tr>
			        				<td class="formlabel">Phone No:</td>
			        				<td><input type="text" name="contact_person_mobile_number" id="contact_person_mobile_number" maxlength="99"
			        					value="${suppdto.map.contact_person_mobile_number }"></td>
			    				</tr>
			    				<tr>
			        				<td class="formlabel">Email:</td>
			       					<td><input type="text" name="contact_person_mailid" id="contact_person_mailid" maxlength="99"
			       						value="${suppdto.map.contact_person_mailid }"></td>
			    				</tr>
			    				<tr>
			    					<td>&nbsp;</td>
			    					<td>&nbsp;</td>
			    				</tr>
			 				</table>
	   				</fieldset>
	   			</div>
	   		</td>
		</tr>
	</table>

	<div class="screenActions">
		<button type="button" accesskey="S" name="save"  class="button" onclick="return ValidateSupplierMaster();" >
		<b><u>S</u></b>ave</button>
		|
		<c:if test="${not empty suppdto}">
		<a href="${cpath}/pages/masters/insta/stores/suppdetails.do?_method=getSupplierDetailsScreen">Add</a>
		|
		</c:if>
		<a href="${cpath }/pages/masters/insta/stores/suppdetails.do?_method=getSupplierDashBoard&sortOrder=supplier_name&sortReverse=false&status=A">Back To DashBoard</a>
		
		<c:if test="${not empty suppdto && max_centers_inc_default > 1}">
		|	<insta:screenlink screenId="mas_center_suppliers" extraParam="?_method=getScreen&supplier_id=${param.supplier_id}"
					label="Center Applicability" />
		</c:if>
	</div>

</form>
</body>
</html>
