<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Common Charges - Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/orderCodes.js" />

<script type="text/javascript">
	var serviceSubGroupsList = ${serviceSubGroupsList};
	var chargeHeads = ${chargeHeadsJSON};
	// this variable is used taxations.tag
	var itemGroupList = ${itemGroupListJson};
	var itemSubGroupList = ${itemSubGroupListJson};
	function checkDuplicate(){
	document.forms[0].charge_name.value = trimAll(document.forms[0].charge_name.value);
	var commonCharges = ${allCommonCharges};
		var orginalName = <insta:jsString value="${bean.map.charge_name}" />;
		var chargeName = document.forms[0].charge_name.value;

		if (trimAll(orginalName) != chargeName){
			for (var i=0 ; i<commonCharges.length;i++){
				var item = commonCharges[i];
				var cName = item.charge_name;
				if (chargeName.toLowerCase() == trimAll(cName.toLowerCase())){
					document.forms[0].charge_name.focus();
					alert(chargeName+' '  + "already exists pls enter other name");
					return false;
				}
			}
		}
		if (document.forms[0].charge_name.value=="") {
			alert("Charge Name is required");
			document.forms[0].charge_name.focus();
			return false;
		}
		if (document.getElementById('charge_group_id').selectedIndex==0) {
			alert("Charge Group is required");
			document.getElementById('charge_group_id').focus();
			return false;
		}
		if (document.getElementById('charge_head_id').selectedIndex==0) {
			alert("Charge Head is required");
			document.getElementById('charge_head_id').focus();
			return false;
		}

		var isInsuranceCatIdSelected = false;
		var insuranceCatId = document.getElementById('insurance_category_id');
		for (var i=0; i<insuranceCatId.options.length; i++) {
		  if (insuranceCatId.options[i].selected) {
			  isInsuranceCatIdSelected = true;
		  }
		}
		if (!isInsuranceCatIdSelected) {
			alert("Please select at least one insurance category");
			return false;
		}

		if (document.getElementById('service_group_id').selectedIndex==0) {
			alert("Service Group is required");
			document.getElementById('service_group_id').focus();
			return false;
		}
		if (document.getElementById('service_sub_group_id').selectedIndex==0) {
			alert("Service Sub Group is required");
			document.getElementById('service_sub_group_id').focus();
			return false;
		}
		if (document.forms[0].charge.value=="") {
			alert("Charge is required");
			document.forms[0].charge.focus();
			return false;
		}
		document.forms[0].submit();
	}
	function addChargeHeads(){
		var selectedChargeGroup = document.forms[0].charge_group.value;
		var chargeTypeValue = document.forms[0].charge_type.value;
		document.forms[0].charge_type.options.length = 0;
	    var selectBox = document.forms[0].charge_type;
		for (i = selectBox.length - 1; i>=0; i--) {
		      selectBox.remove(i);
		}
		var optn ;
		document.forms[0].charge_type.options.length=1;
	    document.forms[0].charge_type.options[0].value = '';
		document.forms[0].charge_type.options[0].text = "-- Select --";
		for(var i =0;i<chargeHeads.length;i++){
		var head = chargeHeads[i];
			if(head.chargegroup_id == selectedChargeGroup){
			    optn = document.createElement("OPTION");
				optn.value = head.chargehead_id;
				optn.text = head.chargehead_name;
				document.forms[0].charge_type.options.add(optn);
			}
		}
		document.forms[0].charge_type.value = chargeTypeValue;
		if (document.getElementById('serviceSubGroup').value!="") {
			loadServiceSubGroup();
			setSelectedIndex(document.getElementById('service_sub_group_id'), document.getElementById('serviceSubGroup').value);
		}
	}

	function loadServiceSubGroup() {
		var serviceGroupId = document.getElementById('service_group_id').value;
		var index = 1;
		document.getElementById("service_sub_group_id").length = 1;
		for (var i=0; i<serviceSubGroupsList.length; i++) {
			var item = serviceSubGroupsList[i];
		 	if (serviceGroupId == item["SERVICE_GROUP_ID"]) {
		 		document.getElementById("service_sub_group_id").length = document.getElementById("service_sub_group_id").length+1;
		 		document.getElementById("service_sub_group_id").options[index].text = item["SERVICE_SUB_GROUP_NAME"];
		  		document.getElementById("service_sub_group_id").options[index].value = item["SERVICE_SUB_GROUP_ID"];
		 		index++;
		 	}
		}
	}
	function getOrderCode(){
		var group = document.getElementById("service_group_id").value;
		var subGroup = document.getElementById("service_sub_group_id").value;
		ajaxForOrderCode('Other Charges','Other Charges',group,subGroup,document.forms[0].othercharge_code);
	}
</script>

</head>
<body onload="addChargeHeads();itemsubgroupinit();">

<form action="CommonChargesMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Charge</h1>
	<input type="hidden" id="serviceSubGroup" value="${bean.map.service_sub_group_id}">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Charge Name:</td>
			<td>
				<input type="hidden" name="dbCharge_name" value="${bean.map.charge_name}" />
				<input type="text" name="charge_name" value="${bean.map.charge_name}"
					onblur="capWords(charge_name);"
				  class="required validate-length" maxlength="50"
				  title="Name is required,not Editable after creation and max length of name can be 50" 
				  ${param._method == 'add' ? '' : 'readonly'}/>
			</td>
			<td class="formlabel">Charge Group</td>
			<td>
				<insta:selectdb id="charge_group_id" name="charge_group" table="chargegroup_constants"
								valuecol="chargegroup_id"  displaycol="chargegroup_name"
								value="${bean.map.charge_group}" filtered="false" dummyvalue="-- Select --"
								dummyvalueId="" onchange="addChargeHeads(this.value)"/>
			</td>
			<td class="formlabel">Charge Head</td>
			<td>
				<insta:selectdb id="charge_head_id" name="charge_type" table="chargehead_constants"
								valuecol="chargehead_id"  displaycol="chargehead_name"
								value="${bean.map.charge_type}" filtered="false" dummyvalue="-- Select --"
								dummyvalueId=""  />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Service Group:</td>
			<td>
				<insta:selectdb id="service_group_id" name="service_group_id" value="${groupId}"
					table="service_groups" class="dropdown"   dummyvalue="-- Select --"
					valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()" />
			</td>
			<td class="formlabel">Service Sub Group:</td>
			<td>
				<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown" onchange="getOrderCode();">
					<option value="">-- Select --</option>
				</select>
			</td>
			<td class="formlabel">Order Code / Alias:</td>
			<td><input type="text" name="othercharge_code" id="othercharge_code"
				size="8" maxlength="20" value="${bean.map.othercharge_code}"/></td>
			<td></td>
		</tr>

		<tr>
			<td class="formlabel">Charge:</td>
			<td>
				<input type="text" name="charge" value="${bean.map.charge}"
				class="required validate-number" title="charge should be a number" />
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}"
				opvalues="A,I" optexts="Active,Inactive" />
			</td>
			<td class="formlabel">Insurance Category:</td>
			<td>
				<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Allow Rate Increase:</td>
			<td>
				<insta:radio name="allow_rate_increase" radioValues="true,false" radioText="Yes,No"
						value="${empty bean.map.allow_rate_increase ? 'true' : bean.map.allow_rate_increase}" />
			</td>
			<td class="formlabel">Allow Rate Decrease:</td>
			<td>
				<insta:radio name="allow_rate_decrease" radioValues="true,false" radioText="Yes,No"
						value="${empty bean.map.allow_rate_decrease ? 'true' : bean.map.allow_rate_decrease}" />
			</td>
			<td class="formlabel">Billing Group:</td>
			<td>
				<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
					displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
			</td>
		</tr>
	</table>
	<insta:taxations/>
	</fieldset>
	<div class="screenAcitons"><button type="button" accesskey="S" onclick="return checkDuplicate();">
	<b><u>S</u></b>ave</button>
	<c:if test="${param._method=='show'}">
	|
	<a href="${cpath}/master/CommonChargesMaster.do?_method=add" >Add</a>
	</c:if>
	|
	<a href="${cpath}/master/CommonChargesMaster.do?_method=list&status=A&sortReverse=false" >Available Charges List<a/></div>

</form>

</body>
</html>
