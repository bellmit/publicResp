<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
    <insta:js-bundle prefix="sponsortype"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Sponsor Type Master- Insta HMS</title>
<!-- R.C:Duplicate cpath -->
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagePath" value="<%=URLRoute.SPONSOR_TYPE_PATH %>"/>

	<script>
 /*
  *R.C:Java script in seperate js file.
  */
		function markApplicable(Object){
			if(Object.checked){
				if(Object.id == 'member_id_show1') {
					document.getElementById('member_id_mandatory1').disabled = false;
					document.getElementById('member_id_show').value = 'Y';

				}else if(Object.id == 'member_id_mandatory1') {
					document.getElementById('member_id_mandatory').value = 'Y';

				}else if(Object.id == 'policy_id_show1') {
					document.getElementById('policy_id_mandatory1').disabled = false;
					document.getElementById('policy_id_show').value = 'Y';

				}else if(Object.id == 'policy_id_mandatory1') {
					document.getElementById('policy_id_mandatory').value = 'Y';

				}else if(Object.id == 'validity_period_show1') {
					document.getElementById('validity_period_mandatory1').disabled = false;
					document.getElementById('validity_period_editable1').disabled = false;
					document.getElementById('validity_period_show').value = 'Y';

				}else if(Object.id == 'validity_period_mandatory1') {
					document.getElementById('validity_period_mandatory').value = 'Y';

				}else if(Object.id == 'prior_auth_show1') {
					document.getElementById('prior_auth_show').value = 'Y';

				}else if(Object.id == 'visit_limits_show1') {
					document.getElementById('visit_limits_show').value = 'Y';

				}else if(Object.id == 'validity_period_editable1') {
					document.getElementById('validity_period_editable').value = 'Y';

				}
			}else{
				if(Object.id == 'member_id_show1') {
					document.getElementById('member_id_mandatory1').disabled = true;
					document.getElementById('member_id_mandatory1').checked = false;
					document.getElementById('member_id_show').value = 'N';
					document.getElementById('member_id_mandatory').value = 'N';


				}else if(Object.id == 'policy_id_show1') {
					document.getElementById('policy_id_mandatory1').disabled = true;
					document.getElementById('policy_id_mandatory1').checked = false;
					document.getElementById('policy_id_show').value = 'N';
					document.getElementById('policy_id_mandatory').value = 'N';


				}else if(Object.id == 'validity_period_show1') {
					document.getElementById('validity_period_mandatory1').disabled = true;
					document.getElementById('validity_period_mandatory1').checked = false;
					document.getElementById('validity_period_show').value = 'N';
					document.getElementById('validity_period_mandatory').value = 'N';

					document.getElementById('validity_period_editable1').disabled = true;
					document.getElementById('validity_period_editable1').checked = false;
					document.getElementById('validity_period_editable').value = 'N';

				}else if(Object.id == 'prior_auth_show1') {
					document.getElementById('prior_auth_show').value = 'N';

				}else if(Object.id == 'member_id_mandatory1'){
						document.getElementById('member_id_mandatory').value = 'N';

				}else if(Object.id == 'policy_id_mandatory1'){
					document.getElementById('policy_id_mandatory').value = 'N';

				}else if(Object.id == 'validity_period_mandatory1'){
					document.getElementById('validity_period_mandatory').value = 'N';

				}else if(Object.id == 'visit_limits_show1'){
					document.getElementById('visit_limits_show').value = 'N';

				}else if(Object.id == 'validity_period_editable1'){
					document.getElementById('validity_period_editable').value = 'N';

				}
			 }
	    }

		var sponsorTypeNames = ${ifn:convertListToJson(sponsorTypeNames)};

		function validateForm(){

			var sponsor_type_name = document.getElementById("sponsor_type_name").value.trim();

			if(!sponsor_type_name){
			/* R.C:Use application.properties*/
				//alert(getString("js.sponsortype.sponsorname"));
			    alert("Please enter sponsor type name.");
				document.getElementById("sponsor_type_name").focus();
				return false;
			}

			for(var i=0;i<sponsorTypeNames.length;i++){
				if(sponsor_type_name == sponsorTypeNames[i].sponsor_type_name){
					alert("Sponsor type name already exist, please choose another name.");
					document.getElementById("sponsor_type_name").focus();
					return false;
				}
			}

		    document.sponsorMasterForm.submit();
			return true;
		}

	</script>
</head>

<body>
	<c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
	<form  name="sponsorMasterForm" action="${actionUrl}" method="POST">

		<input type="hidden" name="_method" value="create"/>
		<input type="hidden" name="sponsor_type_id" value="${ifn:cleanHtmlAttribute(param.sponsor_type_id)}"/>

		<table width="100%">
						<tr>
							<td width="100%"><h1>Add Sponsor Type Details</h1></td>
						</tr>
				</table>

		<insta:feedback-panel/>

		<fieldset class="fieldSetBorder" id="corporateInsu1">
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
				<tr>
					<td class="formlabel" >Sponsor Type Name: </td>
					<td class="forminfo" >
						<input type="text" name="sponsor_type_name" id="sponsor_type_name" maxlength="100" value="${bean.map.sponsor_type_name}" />
					</td>
					<td class="formlabel" width="100px" >Status: </td>
					<td class="forminfo" >
						<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I"
						optexts="Active,Inactive" />
					</td>
					<td></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Description: </td>
					<td class= "forminfo" colspan="5">
						<textarea COLS=61 ROWS=3 name="sponsor_type_description"   id="sponsor_type_description" >${bean.map.sponsor_type_description}</textarea>
				    </td>
				</tr>
			 </table>
		 </fieldset>

		<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel"> Insurance Hierarchy </legend>
		<table  class="formtable" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<%--
				<td class="formlabel">Sponsor Alternate Label: </td>
				<td class="forminfo">
					<input type="text" name="sponsor_type_label" id="sponsor_type_label" maxlength="100" value="${bean.map.sponsor_type_label}" />
				</td>
				--%>
				<td class="formlabel">Plan Type Label: </td>
				<td class="forminfo">
					<input type="text" name="plan_type_label" id="plan_type_label" maxlength="100" value="${bean.map.plan_type_label}" />
				</td>
				<td class="formlabel">
				</td>
				<td></td>
				<td class="formlabel">
				</td>
				<td></td>
			</tr>
		</table>
		</fieldset>

		<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel"> Policy Details </legend>
	    <table class="detailList"  id="" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<th></th>
				<th>Display</th>
				<th>Mandatory</i></th>
				<th>Label</i></th>
				<th>Editable</th>
			</tr>
			<tr>
				<td>
					Membership ID
				</td>
				<td>
					<input type="hidden" name="member_id_show" id="member_id_show" value="N"/>
							<input type="checkbox" name="member_id_show1" id="member_id_show1" value="N" onclick="markApplicable(this);"  />
						
				</td>
				<td>
					<input type="hidden" name="member_id_mandatory" id="member_id_mandatory" value="N"/>
							<input type="checkbox" name="member_id_mandatory1" id="member_id_mandatory1" value="N" disabled onclick="markApplicable(this);" />
					
				</td>
				<td>
					 <input type="text" name="member_id_label" id="member_id_label" maxlength="100"  value="${bean.map.member_id_label}" />
				</td>
				<td></td>
			</tr>

			<tr>
				<td>
					Policy No
				</td>
				<td>
					<input type="hidden" name="policy_id_show" id="policy_id_show" value="N"/>
							<input type="checkbox" name="policy_id_show1" id="policy_id_show1" value="N" onclick="markApplicable(this);"  />
						
				</td>
				<td>
					<input type="hidden" name="policy_id_mandatory" id="policy_id_mandatory" value="N"/>
							<input type="checkbox" name="policy_id_mandatory1"  id="policy_id_mandatory1" value="N" onclick="markApplicable(this);" disabled />
					
				</td>
				<td></td>
				<td></td>
			</tr>

			<tr>
				<td>
					Validity Period
				</td>
				<td>
					<input type="hidden" name="validity_period_show" id="validity_period_show" value="N"/>
							<input type="checkbox" name="validity_period_show1" id="validity_period_show1" value="N" onclick="markApplicable(this);"  />
						
					
				</td>
				<td>
					<input type="hidden" name="validity_period_mandatory" id="validity_period_mandatory" value="N"/>
							<input type="checkbox" name="validity_period_mandatory1" id="validity_period_mandatory1" value="N" onclick="markApplicable(this);" disabled />
						
					
				</td>
				<td></td>
				<td>
						<input type="hidden" name="validity_period_editable" id="validity_period_editable" value="N"/>
							<input type="checkbox" name="validity_period_editable1" id="validity_period_editable1" value="N" onclick="markApplicable(this);"  disabled />
						
				</td>
			</tr>

			<tr>
				<td>
					Prior Auth Details
				</td>
				<td>
					<input type="hidden" name="prior_auth_show" id="prior_auth_show" value="N"/>
							<input type="checkbox" name="prior_auth_show1" id="prior_auth_show1"  value="N" onclick="markApplicable(this);"  />
						
				</td>
				<td></td>
				<td></td>
				<td></td>
			</tr>

			<tr>
				<td>Visit Limits</td>
				<td>
					<input type="hidden" name="visit_limits_show" id="visit_limits_show" value="N"/>
							<input type="checkbox" name="visit_limits_show1" id="visit_limits_show1"  value="N" onclick="markApplicable(this);"  />
						
				</td>
				<td></td>
				<td></td>
				<td></td>
			</tr>

		</table>
		</fieldset>


		<div class="screenActions">
				  <button type="button" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
			
				<a href="${cpath}/${pagePath}/list.htm?_method=list&sortOrder=sponsor_type_name">Sponsor Type List</a>
		</div>
	</form>
</body>
</html>
