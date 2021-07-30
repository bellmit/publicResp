<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>ADD Supported Codes - Insta HMS</title>
<script>
		function validateInput(){
			var code_category = document.getElementById('code_category');
			var code_type = document.getElementById('code_type');
			if(code_category.selectedIndex == 0){
				alert("Please Select Code Category");
				code_category.focus();
				return false;
			}
			if(code_type.selectedIndex == 0){
				alert("Please Select Code Type");
				code_type.focus();
				return false;
			}
			return true;
		}
		function populateCodeTypeClassification(){
			var code_category = document.getElementById('code_category').value;
			var classification = document.getElementById('code_type_classification');
			if (code_category == '') {
				classification.length = 1;
			} else {
				classification.length = 1;
				for (var i=0; i<codeTypeClassificationList.length; i++) {
					var record = codeTypeClassificationList[i];
					if (record.code_category == code_category) {
						var len = classification.length;
						classification.length = len+1;
						classification.options[len].value = record.code_type_classification;
						classification.options[len].text = record.code_type_classification;
					}
				}
			}
		}
		function init() {
			populateCodeTypeClassification();
		}
		var codeTypeClassificationList = ${ifn:convertListToJson(codeTypeClassificationListJson)};
</script>

	</head>
	<body onload="init();">
		<form name="supportedCodeType" action="create.htm" method="POST">
			<div class=pageHeader>Add Supported Codes </div>
			<insta:feedback-panel/>
			<fieldset class="fieldSetBorder">
				<table class="formTable">
					<tr>
						<td class="formlabel">Code Category:</td>
						<td>
							<insta:selectdb name="code_category" table="mrd_supported_code_categories" id="code_category"
							valuecol="code_category" displaycol="code_category"
							dummyvalue="--Select--" onchange="populateCodeTypeClassification();"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Code Type</td>
						<td><insta:selectdb name="code_type" table="mrd_supported_code_types" id="code_type"
							valuecol="code_type" displaycol="code_type" dummyvalue="--Select--"/>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="formlabel">Code Classification: </td>
						<td>
							<select name="code_type_classification" id="code_type_classification" class="dropdown">
								<option value="">-- Select --</option>
							</select>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
			</fieldset>
			<div class="screenActions">
				<button type="submit" name="save" accesskey="S" onclick="return validateInput();"><b><u>S</u></b>ave</button>
				|
				<a href="list.htm?">Supported Codes List</a>
			</div>
		</form>
	</body>
</html>
