<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Contracts Type - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="ajax.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script>
	var contractTypeList = <%= request.getAttribute("avllist") %>;
	var hiddenId = '${bean.contract_type_id}';
	function validate(){
		if(document.forms[0].contract_type.value==""){
			alert("Name is required");
			document.forms[0].contract_type.focus();
			return false;
		}
		var newLicensName = trimAll(document.forms[0].contract_type.value);
			for(i=0;i<contractTypeList.length;i++){
				item = contractTypeList[i];
				if(hiddenId!=item.CONTRACT_TYPE_ID){
					if(newLicensName==item.CONTRACT_TYPE){
						alert("Name already exists");
						document.forms[0].contract_type.focus();
						return false;
					}
				}
			}
		return true;

	}

	function doClose() {
		window.location.href = "${cpath}/master/contracttypes/list.htm?sortOrder=contract_type&sortReverse=false&status=A";
	}

</script>

</head>
<body>

<form action="create.htm" method="POST">

	<div class="pageHeader">Add Contract Type</div>
	
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Name</td>
				<td>
					<input type="text" name="contract_type" value="${bean.contract_type}" onblur="capWords(contract_type);"
							class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Status</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>

	</fieldset>
	
	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return validate()"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="#" onclick="javascript:doClose();">Contract Type</a></td>
		</tr>
	</table>

</form>

</body>
</html>
