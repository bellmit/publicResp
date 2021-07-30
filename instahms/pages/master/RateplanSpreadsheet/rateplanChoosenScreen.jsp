<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<html>
	<head>
		<insta:link type="js" file="masters/charges_common.js" />
		<script>
			function validate() {
				if (document.rateplanSpreadsheet.org_id.value == '') {
					alert("Please select Rate Plan");
					return false;
				} else if (document.getElementById('service_group_id').value == '') {
					alert('Please select service group');
					return false;
				}
				return true;
			}

			function doUpload() {

				if (document.rateplanSpreadsheet.org_id.value == '') {
					alert("Please select Rate Plan");
					return false;
				}
				if (document.rateplanSpreadsheetUpload.xlsRateplanfile.value == '') {
					alert('please browse a file to upload');
					return false;
				}
				var orgId = document.rateplanSpreadsheet.org_id.value;
				document.rateplanSpreadsheetUpload.orgId.value = orgId;
				var fileName = document.getElementById("xlsRateplanfile").value;

				if (fileName.match(/\.xls$/i) == null) {
					var ok = confirm('File does not seeems to be xls file please check the file');
					if (!ok)
						return false;
				}
				if(window.XMLHttpRequest) {
					req = new XMLHttpRequest();
				}
				else if(window.ActiveXObject) {
					req = new ActiveXObject("MSXML2.XMLHTTP");
				}
				req.open("GET", cpath+"/master/ServiceMaster.do?_method=getorgName&org_id="+orgId, true);
				req.setRequestHeader("Content-Type", "text/plain");
				req.send(null);
				req.onreadystatechange = function (){
					if (req.readyState == 4 && req.status == 200) {
						var orgName = req.responseText;

						var parts = fileName.split('_');
						var subPart = (parts.length > 2) ? parts[2].split('.') : null;
						if (subPart == null || subPart[0] != orgName) {
							if (confirm("The current rate plan "+orgName+" does not match the file's rate plan.\n "+
							 "Are you sure you want to upload? ")) {
							 	document.rateplanSpreadsheetUpload.submit();
							 } else {
							 	return false;
							 }
						} else {
							document.rateplanSpreadsheetUpload.submit();
						}
					}
				}

			}

		</script>
	<title>Rate Plan Spread Sheet-Insta HMS</title>
	</head>
	<body>
	<form name="rateplanSpreadsheet" action="RatePlanSpreadSheet.do">
	<input type="hidden" name="_method" value="downloadCharges" />
		<h1>Rate Plan Spread Sheet</h1>
		<insta:feedback-panel />
		<fieldset class="fieldSetBorder">
		<table class="formTable">
			<tr>
				<td class="formlabel">Rate Plan:</td>
				<td><insta:selectdb name="org_id" value="${param.org_id}" table="organization_details" valuecol="org_id"
							 orderby="org_name" dummyvalue="-- Select --" displaycol="org_name" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Service Group:</td>
				<td><insta:selectdb id="service_group_id" name="service_group_id" value="${groupId}"
					table="service_groups" class="dropdown"   dummyvalue="-- Select --"
					valuecol="service_group_id"  displaycol="service_group_name" /></td>
			</tr>
			<tr>
				<td align="right">Include Only Hospital Charge:</td>
				<td ><input type="checkBox" name="includeOnlyHospCharge" /></td>
			</tr>
			<tr>
				<td align="right">Include Discount:</td>
				<td ><input type="checkBox" name="includeDiscount" /></td>
			</tr>
			<tr>
				<td><button type="submit" name="submit" onclick="return validate();" >Download</button></td>
			</tr>
		</table>

		<div class="brB brT brL brR" style="height: 27px; width :637px; background-color: #FFC">
			<div style="padding-top: 7px "><font size="2">
				Please save the original file as a backup and upload the same to revert the changes if something went wrong.</font>
			</div>
		</div>

	</form>
	<form style="padding-top: 13px" action="RateplanSpreadsheetUpload.do" name="rateplanSpreadsheetUpload"
				method="POST" enctype="multipart/form-data">
		<input type="hidden" name="_method" value="importChargesBasedOnServiceGroups"/>
		<input type="hidden" name="orgId" value="" />
		<input type="file" name="xlsRateplanfile" id="xlsRateplanfile" accept="<insta:ltext key="upload.accept.master"/>"/>
		<button type="button" onclick="return doUpload();">Upload</button>
	</form>
	</fieldset>
	</body>
</html>
