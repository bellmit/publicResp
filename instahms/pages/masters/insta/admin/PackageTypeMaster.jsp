<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
	<script type="text/javascript">
		var serviceSubGroupsList = ${serviceSubGroupsList};
		var operationApplicableFor = '${genPrefs.operationApplicableFor}';
		function validate(){
			if(document.forms[0].package_name.value == ''){
				alert("Package name can not be empty");
				document.forms[0].package_name.focus();
				return false;
			}
			if(document.forms[0].package_type.value == ''){
				alert("Select Package type ");
				document.forms[0].package_type.focus();
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
			if(document.forms[0].org_id.value == ''){
				alert("Select rateplan");
				document.forms[0].org_id.focus();
				return false;
			}
		}
		function setFocus(){
			document.getElementById("package_name").focus()
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
	 function chklen(){
	  	document.forms[0].description.value = trim(document.forms[0].description.value);

	  	 if(document.forms[0].description.value.length>4000)
	  	 {
	  	    var s = document.forms[0].description.value;
	  	    s = s.substring(0,4000);
	    	document.forms[0].description.value = s;
	  	    alert("description should be 4000 characters only");
	  	    document.forms[0].description.focus();
	  	 }
	  }
	</script>
</head>
<body onload="setFocus();" >
	<form action="PackagesMasterAction.do" method="POST" >
	<input type="hidden" name="_method" value="addPackage"/>
		<h1>Package Type</h1>
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Package Details</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Package Name:</td>
	     		<td><input type="text" name="package_name" id="package_name"  maxlength="100"/></td>
	      		<td class="formlabel">Package Type:</td>
				<td class="forminfo">
					<select name="package_type" id="package_type"  class="dropdown"
							>
						<option value="">...Select...</option>
						<option value="i">IP</option>
						<option value="o">OP</option>
						<option value="d">Diag</option>
					</select>
				</td>
				<td class="formlabel">Package Category:</td>
				<td>
					<insta:selectdb  name="package_category_id" id="package_category_id"  value="${package_category_id}" table="package_category_master" valuecol="package_category_id" displaycol="package_category" orderby="package_category_id" />
				<td>
			</tr>
			<tr>
				<td class="formlabel">Service Group:</td>
				<td>
					<insta:selectdb id="service_group_id" name="service_group_id" value=""
						table="service_groups" class="dropdown"   dummyvalue="-- Select --"
						valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()" />
				</td>
				<td class="formlabel">Service Sub Group:</td>
				<td>
					<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown">
						<option value="">-- Select --</option>
					</select>
				</td>
				<td class="formlabel">Rate Sheet: </td>
				<td class="forminfo">GENERAL
					<input type="hidden" name="org_id" id="org_id" value="ORG0001"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Type:</td>
				<td>
					 <input type="radio" name="type" id="templateT" value="T" >
					 <label for="templateT">Template</label>
					 <input type="radio" name="type" id="templateP" value="P" checked="checked">
					 <label for="templateP">Package</label>
			         <input type="hidden" name="packagetype" id="packagetype" value="Package"/>
				</td>
				<td class="formlabel">Status:</td>
				<td >
					<insta:radio name="package_active" radioValues="A,I"
								radioText="Active,Inactive" value="A"/>
				</td>
				<td class="formlabel">Further Discounts:</td>
				<td >
					<insta:radio name="allow_discount" radioValues="true,false"
								radioText="Allow,Disallow" value="true"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Insurance Category:</td>
				<td>
					<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_category_id}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtered="false"/>
				</td>

			</tr>
			<tr>
				<td class="formlabel" valign="top">Description:<br>(max 4000 characters)</td>
				<td colspan="2"><textarea name="description" id="description" rows="3" cols="30"
				onblur="chklen();"></textarea></td>
			</tr>
			<tr>
				<td>
					<input type="submit" name="add" value="Next>>" onclick="return validate();"/>
				</td>
			</tr>
		</table>
		</fieldset>
	</form>
</body>
</html>