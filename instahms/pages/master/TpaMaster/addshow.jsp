<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>TPA / Sponsor - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="/master/TpaMaster/tpamaster.js"/>

	<script>
		var cityList = <%= request.getAttribute("cityList")%>
		var sponsorList = <%= request.getAttribute("sponsorList") %>
		function init() {
			focus(); 
			keepBackUp();
			initTpaHaCodeDialog();
			disableOrEnableLimitIncludesTax();
		}

		<c:if test="${param._method != 'add'}">
	      Insta.masterData=${tpaMastersLists};
        </c:if>

        function doClose() {
			window.location.href = "${cpath}/master/TpaMaster.do?_method=list&sortOrder=tpa_name" +
				"&sortReverse=false&status=A";
		}

	</script>

</head>
<body onload="init()">
<c:choose>
    <c:when test="${param._method !='add'}">
         <h1 style="float:left">Edit TPA / Sponsor</h1>
         <c:url var ="searchUrl" value="/master/TpaMaster.do"/>
         <insta:findbykey keys="tpa_name,tpa_id" fieldName="tpa_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add TPA / Sponsor</h1>
    </c:otherwise>
</c:choose>

<jsp:useBean id="eligibilityAuthLabelMap" class="java.util.HashMap"/>
<c:set target="${eligibilityAuthLabelMap}" property="O" value="Observation in claim xml"/>
<c:set target="${eligibilityAuthLabelMap}" property="T" value="Eligibility ID Payer"/>
<c:set target="${eligibilityAuthLabelMap}" property="N" value="Exclude in claim xml"/>

<form action="TpaMaster.do" name="tpaMasterForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="testindg" value="${bean.map.city}">
	<input type="hidden" name="claim_template_id" value=""/>
		<input type="hidden" name="default_claim_template" value=""/>

	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="tpa_id" id="tpa_id" value="${bean.map.tpa_id}"/>

	</c:if>

	<insta:feedback-panel/>

<table >
	<tr>
		<td>
			<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">TPA/Sponsor Details</legend>

				<table class="formtable">
					<tr>
						<td class="formlabel">TPA/Sponsor Name:</td>
						<td><input type="text" name="tpa_name" value="${bean.map.tpa_name}" onblur="capWords(tpa_name)" class="required" maxlength="200" title="TPA/Sponsor name is required"></td>

						<td class="formlabel">Email-Id:</td>
						<td><input type="text" name="email_id" value="${bean.map.email_id}" maxlength="60" ></td>

						<td class="formlabel">Status:</td>
						<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
					</tr>

					<tr>
						<td class="formlabel">Address:</td>
						<td><input type="text"  name="address" value="${bean.map.address}" onkeypress="restrictMaxLength(this)"/></td>

						<td class="formlabel">Fax:</td>
						<td><input type="text" name="fax" value="${bean.map.fax}" maxlength="30"></td>

						<td class="formlabel">TPA/Sponsor Prior Auth Form:</td>
						<td>
						  <insta:selectdb  name="tpa_pdf_form" value="${bean.map.tpa_pdf_form}" table="doc_pdf_form_templates"
						     valuecol="template_id" displaycol="template_name" filtercol="doc_type,status"  filtervalue="SYS_TPA,A"
						      dummyvalue="----Select----"/>
						</td>
					</tr>

					<tr>
						<td class="formlabel">Mobile No:</td>
						<td><input type="text" name="mobile_no" value="${bean.map.mobile_no}" maxlength="15"/></td>

						<td class="formlabel">Phone:</td>
						<td><input type="text" name="phone_no" value="${bean.map.phone_no}" maxlength="15"/></td>

						<td class="formlabel">Postal Code:</td>
						<td><input type="text" name="pincode" value="${bean.map.pincode}" maxlength="10"></td>
					</tr>

					<tr>
						<td class="formlabel">Country:</td>
						<td><input type="text" name="country" value="${bean.map.country}" maxlength="249"
						class="required validate-length"  title="Country  is required and max length of name can be 250"/></td>


						<td class="formlabel">State:</td>
						<td><input type="text" name="state" value="${bean.map.state}" maxlength="249"
						class="required validate-length"  title="State is required and max length of name can be 250"/></td>


						<td class="formlabel">City:</td>
						<td><input type="text" name="city" value="${bean.map.city}" maxlength="249"
						class="required validate-length"  title="City is required and max length of name can be 250"/></td>
					</tr>

					<tr>
						<td class="formlabel">Validity End Date:</td>
						<td><insta:datewidget name="validityEnd_date" id="validityEnd_date"
							valueDate="${bean.map.validity_end_date}"/></td>
						<td class="formlabel">TPA/Sponsor Claim Form:</td>
						<td>
						<c:set var="defaultHTMlattr" value=""/>
						<c:set var="defaultRTFattr" value=""/>
						<c:if test="${bean.map.default_claim_template == 'P' || bean.map.default_claim_template == 'Y' }">
							<c:set var="defaultHTMlattr" value="selected='true'"/>
						</c:if>
						<c:if test="${bean.map.default_claim_template =='R' }">
							<c:set var="defaultRTFattr" value="selected='true'"/>
						</c:if>
						  <select name="claimTemplateId" id="claimTemplateId" class="dropdown">
						  	<option value="">----Select----</option>
						  	<option  value="P" ${defaultHTMlattr}>Default Claim HTML Template</option>
						  	<option value="R" ${defaultRTFattr}>Default Claim RTF Template</option>
						  	<c:forEach var="ct" items="${claimForms}" >
						  	<c:choose>
						  		<c:when test="${bean.map.claim_template_id == ct.map.claim_template_id}">
						  			<c:set var="attr" value="selected='true'"/>
						  		</c:when>
						  		<c:otherwise><c:set var="attr" value=""/></c:otherwise>
						  	</c:choose>
						  	<option value="${ct.map.claim_template_id}"  ${attr}>${ct.map.template_name}</option>
						  	</c:forEach>
						  </select>
						</td>
						<td class="formlabel">Claim Format:</td>
						<td>
							<select id="claim_format" name="claim_format" class="dropdown">
								<option value="XML" ${bean.map.claim_format == 'XML' ? 'selected' : ''}>XML</option>
								<option value="XL" ${bean.map.claim_format == 'XL' ? 'selected' : '' }>XL</option>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel">TPA/Sponsor Type:</td>
						<td>
						  <insta:selectdb  name="sponsor_type_id" id="sponsor_type_id" value="${bean.map.sponsor_type_id}" table="sponsor_type"
						     valuecol="sponsor_type_id" displaycol="sponsor_type_name" filtercol="status"  filtervalue="A"
						      dummyvalue="----Select----" dummyvalueId="" orderby="sponsor_type_name"/>
						</td>
						<td class="formlabel">Scanned Doc/Card Upload:</td>
						<td><insta:selectoptions name="scanned_doc_required"
								value="${empty bean.map.scanned_doc_required ? 'N' : bean.map.scanned_doc_required }"
								opvalues="R,O,N" optexts="Required,Optional,Not required" /></td>
						<td class="formlabel">Prior Authorization Mode:</td>
						<td><insta:selectoptions name="pre_auth_mode" value="${bean.map.pre_auth_mode}"
							opvalues="M,O" optexts="Manual,Online" />
							<img class="imgHelpText"
 title="For this TPA, when Prior Authorization module is enabled and this mode is set as Online then Prior Auth can be received via Online otherwise Manual."
						 src="${cpath}/images/help.png"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Membership ID Pattern:</td>
						<td>
							<input type="text" name="member_id_pattern" value="${bean.map.member_id_pattern}" onkeypress="return validateMembershipId(event)"/>
							<img class="imgHelpText"
title="Membership ID Pattern Validation Rules. The pattern will be in the combination of x,9,’ ', ',' , '-', '.'. Here x is for any alphabetical character and 9 is for any numeric digit. Other than x and 9 represents constant values."
						 		src="${cpath}/images/help.png"/>
						 </td>
						 <td class="formlabel">Duplicate Membership ID:</td>
						 <td>
							<insta:selectoptions name="member_id_validation_status" value="${bean.map.tpa_member_id_validation_type}"
							onchange="disableOrEnableChildMemIdCount()"
							opvalues="A,B,W,C" optexts="Allow,Block,Warn,Allow Child Birth Only" />
						 </td>
						 
						 <td class="formlabel">Child Duplicate Memb ID Validity (Days):</td>
						 <td>
							<input type="text" 
								name="child_dup_memb_id_validity_days"
								${bean.map.tpa_member_id_validation_type != 'C' ?  'disabled' : ''}
								value="${bean.map.child_dup_memb_id_validity_days}"
								onkeypress="return enterNumOnlyzeroToNine(event)" />
						 </td>
					</tr>
					<tr>
						<td class="formlabel">Tax Identification Number:</td>
						<td><input type="text" name="tin_number" value="${bean.map.tin_number}"/></td>
						<td class="formlabel">Claim Amount Includes Tax:</td>
						<td><insta:selectoptions name="claim_amount_includes_tax"
								value="${empty bean.map.claim_amount_includes_tax ? 'Y' : bean.map.claim_amount_includes_tax }"
								opvalues="N,Y" optexts="No,Yes" onchange="disableOrEnableLimitIncludesTax();"/></td>
						 <td class="formlabel">Limit Includes Tax:</td>
						 <td><insta:selectoptions name="limit_includes_tax"
								value="${empty bean.map.limit_includes_tax ? 'Y' : bean.map.limit_includes_tax }"
								opvalues="N,Y" optexts="No,Yes" 
								disabled="${bean.map.claim_amount_includes_tax == 'N' ? 'true' : 'false'}"/></td>
					</tr>
					<tr>
						 <td class="formlabel">Max Re-Submission Count:</td>
						 <td>
						 	<input type="text" name="max_resubmission_count" value="${empty bean.map.max_resubmission_count && param._method == 'add' ? '3' : bean.map.max_resubmission_count}" onkeypress="return enterNumOnlyzeroToNine(event)" />
						 </td>
					</tr>
				</table>
			</fieldset>
		</td>
	</tr>

	<tr>
		<td>
			<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Contact Person Details</legend>

				<table class="formtable">
					<tr>
						<td class="formlabel">Name:</td>
						<td><input type="text" name="contact_name" value="${bean.map.contact_name}" maxlength="50"/></td>

						<td class="formlabel">Designation:</td>
						<td><input type="text" name="contact_designation" value="${bean.map.contact_designation}" maxlength="50"/></td>

						<td class="formlabel">Phone:</td>
						<td><input type="text" name="contact_phone" value="${bean.map.contact_phone}" maxlength="15"/></td>
					</tr>

					<tr>
						<td class="formlabel">Mobile:</td>
						<td><input type="text" name="contact_mobile" value="${bean.map.contact_mobile}" maxlength="15"/></td>

						<td class="formlabel">Email:</td>
						<td><input type="text" name="contact_email" value="${bean.map.contact_email}" maxlength="60"/></td>
					</tr>
				</table>

			</fieldset>
		</td>
	</tr>

	<tr><td>&nbsp;</td></tr>

</table>

<fieldset class="fieldSetBorder" style="width:726">
  <legend class="fieldSetLabel" >Health Authority Code</legend>
  <table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="tpaHaCodeTable">
    <tr>
      <th>Health Authority</th>
      <th style="width:200px;">TPA/Sponsor Code</th>
      <th>Eligibility Authorization</th>
      <th>Eligibility Authorization in Claim XML</th>
      <th>&nbsp;</th>
      <th>&nbsp;</th>
    </tr>
       <c:forEach items="${healthAuthorityCodes}" var="st" varStatus="status">
	   <c:set var="i" value="${status.index + 1}"/>
      	<tr>
	        <td class="forminfo" style="width:300px;" valign="middle">
	        <label id="healthAuth${i}">${st.map.health_authority}</label>

	          <input type="hidden" name="h_health_authority" id='h_health_authority${i}' value="${st.map.health_authority}"/>
	          <input type="hidden" name="h_ha_tpa_code_id" id="h_ha_tpa_code_id${i}" value="${st.map.ha_tpa_code_id}">
	          <input type="hidden" name="htpaId" id='htpaId${i}' value="${st.map.tpa_id}"/>
	          <input type="hidden" name="hacodeoldrnew" id='hacodeoldrnew${i }' value="old"/>
	        </td>
	        <td align="center" style="width:400px;">
	        	<label id="h_ha_code${i}">${st.map.tpa_code}</label>
	        	<input type="hidden" name="h_tpa_code" id="h_tpa_code${i}" value="${st.map.tpa_code}"/>
	        </td>
	        <td>
			    <label id="h_ha_enable_eligibility_authorization${i}">${st.map.enable_eligibility_authorization ? 'Enabled' : 'Disabled'}</label>
		        	<input type="hidden" name="h_eligibility_authorization" id="h_eligibility_authorization${i}" value="${st.map.enable_eligibility_authorization}"/>
	        </td>
	        <td>
			    <label id="h_ha_eligibility_authorization_in_xml${i}">${eligibilityAuthLabelMap[st.map.enable_eligibility_auth_in_xml]}</label>
		        	<input type="hidden" name="h_eligibility_authorization_in_xml" id="h_eligibility_authorization_in_xml${i}" value="${st.map.enable_eligibility_auth_in_xml}"/>
	        </td>
	        <td align="center"> <img src="${cpath}/icons/Delete.png" name="haDelItem" id="haDelItem${i }" onclick="deleteTpaHaCodeItem(this, ${i})">
	          <input type="hidden" name="h_ha_deleted" id="h_ha_deleted${i }" value="false"/>
	        </td>
	        <td class="forminfo">
	          <button name="haEditBut" id="haEditBut${i}" onclick="editTpaHaCodeDialog(${i}); return false;" class="imgButton" accesskey="U" title="Edit Tpa Health Authority Code">
	            <img class="button" name="haEdit" id="haEdit1" src="../icons/Edit.png"
	                  style="cursor:pointer;" >
	          </button>
	        </td>
      	</tr>
    </c:forEach>

    <tr>
      <td colspan="8" style="text-align:right">
        <input type="button" name="btnAddTpaHaCode" id="btnAddTpaHaCode1" value="+" class="plus"
        onclick="getTpaHaCodeDialog(1);"/> </td>
    </tr>
  </table>
</fieldset>

<div id="tpaHaCodeDialog" style="visibility:hidden; width:650px">
  <div class="bd">
    <fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add/Edit&nbsp;Health&nbsp;Authority&nbsp;Code&nbsp;</legend>
    <table class="formtable" cellpadding="0" cellspacing="0">
      <tr>
        <th>Health Authority</th>
        <th>TPA/Sponsor Code</th>
        <th>Eligibility Authorization</th>
        <th>Eligibility Authorization in Claim XML</th>
      </tr>
      <tr>
        <td>
        	<select name="health_authority" class="dropdown">
        		<option value="">-- Select --</option>
        		<c:forEach var="healthAuth" items="${healthAuthorities}">
        			<option value="${healthAuth.map.health_authority}">${healthAuth.map.health_authority}</option>
        		</c:forEach>
        	</select>
        </td>
        
        <td>
        	<input type="text" name="tpa_code" id="tpa_code" value="">
        </td>
        <td>
        		<insta:selectoptions name="enable_eligibility_authorization" id ="enable_eligibility_authorization"
				value=""
				opvalues="false,true" optexts="Disabled, Enabled"/>
		</td>
		<td>
        		<insta:selectoptions name="enable_eligibility_auth_in_xml"
				value=""
				opvalues="O,T,N" optexts="As observation, Eligibility ID Payer, Disable"/>
		</td>
        
      </tr>
    </table>
    </fieldset>
    <input type="button" value="Add" onclick="AddRecord();"/>
    <input type="button" value="Close" onclick="handleTpaHaCodeCancel();" />
  </div>
</div>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return funSetClaimTemplateId();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/TpaMaster.do?_method=add">Add</a>
		|
		</c:if>
		<a href="${cpath}/master/TpaMaster.do?_method=list&sortOrder=tpa_name&sortReverse=false&status=A">TPA/Sponsor List</a>
	</div>


</form>

</body>
</html>
