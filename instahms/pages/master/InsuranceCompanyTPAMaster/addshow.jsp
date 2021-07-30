<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Insurance Company TPA/Sponsor - Insta HMS</title>
<insta:link type="script" file="billing/claimsCommon.js"/>
<script>

	function doClose() {
		window.location.href = "${cpath}/master/InsuranceCompanyTPAMaster.do?_method=list&sortOrder=insurance_co_id&sortReverse=false";
	}

	function validate() {
		var insCompObj = mainform.insurance_co_id;
		var tpaObj     = mainform.tpa_id;

		if (insCompObj.value == '') {
			alert("Select insurance company");
			insCompObj.focus();
			return false;
		}
		if (tpaObj.value == '') {
			alert("Select TPA/Sponsor");
			tpaObj.focus();
			return false;
		}
		return true;
	}

	var companyList  = ${insCompList};
	var tpaList      = ${tpaList};

</script>
</head>

<body>

<h1>Add Insurance Company TPA/Sponsor</h1>

<insta:feedback-panel/>

<form action="InsuranceCompanyTPAMaster.do" method="POST" name="mainform">
	<input type="hidden" name="_method" value="create">

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Insurance Company:</td>
			<td>
				<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${insurance_co_id}"
					table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="-- Select --"  orderby="insurance_co_name"/>
			</td>
			<td class="formlabel">TPA/Sponsor:</td>
			<td>
				<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" filtercol="sponsor_type" filtervalue="I"
					table="tpa_master" valuecol="tpa_id" dummyvalue="-- Select --" orderby="tpa_name"/>
			</td>
			<td></td>
			<td></td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/InsuranceCompanyTPAMaster.do?_method=add'">Add</a>
		|
		<a href="javascript:void(0)" onclick="doClose();">Insurance Company TPA/Sponsor List</a>
	</div>

</form>
</body>
</html>
