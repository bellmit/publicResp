<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.CONTRACTOR_MASTER_PATH %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Contractor - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "";
		}

      function focus(){
      	document.contractormasterform.contractor_name.focus();
      }

      function checkDiscount() {
		if (parseFloat(document.getElementById("discount").value ) > 100) {
			alert("Discount should be less than or equal 100 ");
			document.getElementById("discount").focus();
			return false;
		}
      }

	</script>
</head>
<body onload="focus()">
<h1>Add Contractor</h1>
<insta:feedback-panel/>

<form action="create.htm" name="contractormasterform" method="POST">

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLegend">Contractor</legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel">Name:</td>
				<td>
					<input type="text" name="contractor_name" value="${bean.map.contractor_name}"	onblur="capWords(contractor_name);" class="required validate-length" length="50" title="Contractor name is required and max length of name can be 50" />
					<span class="star">&nbsp;*</span>
				</td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active, Inactive" /></td>
				<td class="formlabel">Grade:</td>
				<td><input type="text" name="grade" value="${bean.map.grade}"  /></td>
			</tr>

			<tr>
				<td class="formlabel">Address:</td>
							<td>
								<textarea  rows="5" cols="18" name="address" id="address" onblur="capWords(address);"
								 class="text-input" >${bean.map.address}
								</textarea>
							</td>
				<td class="formlabel">Phone:</td>
							<td>
								<input type="text" name="phone" value="${bean.map.phone}" onblur="capWords(phone);"  />
							</td>
				<td class="formlabel">Fax:</td>
							<td>
								<input type="text" name="fax" value="${bean.map.fax}"  />
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td> </td>
				<td class="formlabel">Email:</td>
							<td>
								<input type="text" name="email" value="${bean.map.email}"  />
							</td>
				<td class="formlabel">Website:</td>
							<td>
								<input type="text" name="website" value="${bean.map.website}"  />
							</td>
			</tr>
			</table>
		</fieldset>
				<fieldset>
				<legend class="fieldSetLegend">Contact Person</legend>
					<table class="formtable" >
						<tr>
							<td class="formlabel">Person Name:</td>
							<td>
								<input type="text" name="contact_person_name" value="${bean.map.contact_person_name}"  />
							</td>
							<td class="formlabel">Phone</td>
							<td>
								<input type="text" name="contact_person_phone" value="${bean.map.contact_person_phone}"  />
							</td>
							<td class="formlabel">Remarks:</td>
							<td>
								<input type="text" name="remarks" value="${bean.map.remarks}"  />
							</td>
						</tr>
					</table>
				</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		<c:url var="Url" value="${pagePath}/list.htm"/>
			<a href="${Url}">Back To Dash Board</a>
	</div>

</form>

</body>
</html>
