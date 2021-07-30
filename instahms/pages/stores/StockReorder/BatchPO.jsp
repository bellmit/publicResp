<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
	<head>
		<title><insta:ltext key="storeprocurement.createbatchpo.raisepo.title"/></title>
		<meta name="i18nSupport" content="true"/>
		<script>
			function validateForm() {
				var deptId = document.getElementById('deptId').value;
				if (deptId == '') {
					showMessage("js.stores.procurement.selectthestore");
					document.getElementById('deptId').focus();
					return false;
				}
				var csv_file = document.getElementById('csv_file').value;
				if (csv_file == '') {
					showMessage("js.stores.procurement.uploadcsvfile");
					document.getElementById('csv_file').focus();
					return false
				}
				return true;
			}
			var gRoleId = '${ifn:cleanJavaScript(roleId)}';
		    var isSuperStore = '${isSuperStore}';
			var default_store = '${default_store}';
			function userHasRights() {
		 		if(gRoleId != 1 && gRoleId != 2) {
		 			if(deptId == "" && default_store == "No") {
		 				showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
		 				document.getElementById("buttonTable").style.display = 'none';
		 				return false;
		 			}
		 		}
		 		if(document.getElementById('deptId') && !(document.getElementById('deptId').options.length >0 && document.getElementById('deptId').options[0].text!='')) {
					showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
					document.getElementById("buttonTable").style.display = 'none';
					return false;
				}
				if(document.getElementById('deptId') &&  default_store == 'Yes' && isSuperStore == 'N' && !(document.getElementById('deptId').options.length >0 && document.getElementById('deptId').options[0].text!='')) {
					showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
					document.getElementById("buttonTable").style.display = 'none';
					return false;
				}

				if(!document.getElementById('deptId') && deptId != "" &&  default_store == 'Yes' && isSuperStore == 'N'){
					showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
					document.getElementById("buttonTable").style.display = 'none';
					return false;
				}
				return true;
			}
		</script>
		<insta:js-bundle prefix="stores.procurement"/>
	</head>
<body onload="userHasRights();">
	<h1><insta:ltext key="storeprocurement.createbatchpo.raisepo.createbatchpo"/></h1>
	<insta:feedback-panel/>
	<form name="batchpoform" action="batchpo.do" method="POST" enctype="multipart/form-data">
		<input type="hidden" name="_method" value="createBatchPO"/>
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="storeprocurement.createbatchpo.raisepo.store"/>: </td>
					<td><insta:userstores username="${userid}" elename="deptId" id="deptId" onlySuperStores='Y'/></td>
					<td class="formlabel">&nbsp</td>
					<td></td>
					<td class="formlabel">&nbsp</td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="storeprocurement.createbatchpo.raisepo.uploadfile"/>: </td>
					<td><input type="file" name="csv_file" id="csv_file" accept="<insta:ltext key="upload.accept.master"/>" /></td>
				</tr>

			</table>
		</fieldset>
		<table class="screenActions" id="buttonTable">
			<tr>
				<td><input type="submit" name="save" value='<insta:ltext key="storeprocurement.createbatchpo.raisepo.raisebatchpo"/>' onclick="return validateForm();"/>
			</tr>
		</table>
	</form>
</body>
</html>