<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.CHARGE_HEAD_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Insta HMS</title>


	<script type="text/javascript">
	var chargeHeads = ${ifn:convertListToJson(chargeHeadsJSON)};
	var serviceSubGroups = ${ifn:convertListToJson(serviceSubGroups)};
	
		function checkDuplicates(){
			var chargeHeadId = document.chargeheadsform.chargehead_id.value;
			var chargeName = document.chargeheadsform.chargehead_name.value;

				for(var i =0;i<chargeHeads.length;i++){
				 var charge = chargeHeads[i];
					if(chargeHeadId == charge.chargehead_id){
						alert(chargeHeadId+" charge head id already exists");
						return false;
					}
				}
				for(var i =0;i<chargeHeads.length;i++){
				var charge = chargeHeads[i];
						if(chargeHeadId == charge.chargehead_id && chargeName == charge.chargehead_name ){
						alert(chargeHeadId+" charge head  name already exists");
						return false;
					}
				}
			
			if(document.chargeheadsform.service_group_id.value == ''){
				alert("Select Service Group");
				document.chargeheadsform.service_group_id.focus();
				return false;
			}
			if(document.chargeheadsform.service_sub_group_id.value == ''){
				alert("Select Service Sub Group");
				document.chargeheadsform.service_sub_group_id.focus();
				return false;
			}
		}
		 function loadServiceSubGroups(){
			var filteredList = filterList(serviceSubGroups,'service_group_id',document.chargeheadsform.service_group_id.value);
			loadSelectBox(document.chargeheadsform.service_sub_group_id,filteredList,'service_sub_group_name','service_sub_group_id',"--Select--","");
		} 
	</script>

</head>
<body>
	<h1>Add Charge Head</h1>

	<insta:feedback-panel/>
	<c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
	<form action="${actionUrl}" method="POST" name="chargeheadsform" onsubmit="return checkDuplicates();">
		
			<fieldset class="fieldSetBorder">
				<table class="formtable">
					<tr>
						<td class="formlabel">Charge Head ID:</td>
						
						<td><input type="text" name="chargehead_id" class="required field"
							title="Charge Head Id is required." maxlength="10"/></td>
							
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					
					<tr>
						<td class="formlabel">Charge Group:</td>
						<td>
							<insta:selectdb name="chargegroup_id" table="chargegroup_constants"
											valuecol="chargegroup_id"  displaycol="chargegroup_name"
											value="${bean.charge_group}" filtered="false" dummyvalue="..charge group.."
											dummyvalueId="" />
						</td>
					</tr>
						
					<tr>
						<td class="formlabel">Charge Head Name:</td>
						<td><input type="text" name="chargehead_name" class="required field"
							title="Charge Head Name is required."
							value="" maxlength="50"/></td>
					</tr>
					<tr>
						<td class="formlabel">Display Order:</td>
						<td><input type="text" name="display_order"
							class="required validate-number " maxlength="4"
							title="Display Order is mandatory and it should be integer"
							value=""></td>
					</tr>
					<tr>
						<td class="formlabel">Account Head:</td>
						<td><insta:selectdb name="account_head_id"
							table="bill_account_heads" value=""
							displaycol="account_head_name" valuecol="account_head_id"
							dummyvalue="--select---" class="validate-not-empty dropdown"
							title="Account Head is required." /></td>
					</tr>
					<tr>
						<td class="formlabel">Payment Eligible:</td>
						<td>
							<insta:selectoptions name="payment_eligible" optexts="Yes,No" class="dropdown" style="width: 60px"
							opvalues="Y,N" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Insurance Payable:</td>
						
							<td><insta:selectoptions name="insurance_payable" optexts="Yes,No" class="dropdown" style="width: 60px"
							opvalues="Y,N" value=""/></td>
					</tr>
					<tr>
						<td class="formlabel">Service Tax Applicable On Claim Amount:</td>
						<td><insta:selectoptions name="claim_service_tax_applicable" optexts="Yes,No" class="dropdown" style="width: 60px"
						opvalues="Y,N" value=""/></td>
					</tr>
					<tr>
						<td class="formlabel">Codification supported:</td>
						<td>
							<insta:selectoptions name="codification_supported" optexts="Yes,No" opvalues="Y,N"
							value="" class="dropdown" style="width:60px"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel"> Service Groups:</td>
						<td>
							<insta:selectdb name="service_group_id" table="service_groups" valuecol="service_group_id"
								displaycol="service_group_name" value=""
								onchange="loadServiceSubGroups()" dummyvalue="--Select--" dummyvalueId="" />
						</td>
					</tr>
					<tr>
					 <td class="formlabel"> Service Sub Groups:</td>
					 <td >
						 <select name="service_sub_group_id" class="dropdown">
						 	<option value="">--Select--</option>
						 </select>
					 </td>
					</tr>
					<tr>
						<td class="formlabel">Allow Rate Increase:</td>
						<td>
							<insta:radio name="allow_rate_increase" radioValues="true,false" radioText="Yes,No"
									value="true" />
						</td>
					</tr>
					<tr>
						<td class="formlabel">Allow Rate Decrease:</td>
						<td>
							<insta:radio name="allow_rate_decrease" radioValues="true,false" radioText="Yes,No"
									value="true" />
						</td>
					</tr>
					<tr>
						<td class="formlabel">Service Charge Applicable:</td>
						<td><insta:selectoptions name="service_charge_applicable" optexts="Yes,No" class="dropdown" style="width: 60px"
						opvalues="Y,N" value=""/></td>
					</tr>
				</table>
			</fieldset>

			<div class="screenActions">
				<button type="submit" name="save" accesskey="S"><b><u>S</u></b>ave</button> |
				<a href="${cpath}/${pagePath}/list.htm?sortOrder=display_order" >Charge Heads List</a>
			</div>

	</form>
</body>
</html>
