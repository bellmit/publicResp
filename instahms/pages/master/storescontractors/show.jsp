<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

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

       //Insta.masterData=${contractorLists};
       Insta.masterData=${ifn:convertListToJson(contractorLists)};
	</script>
</head>
<body onload="focus()">
        <h1 style="float:left">Edit Contractor</h1>
        <insta:findbykey keys="contractor_name,contractor_id" fieldName="contractor_id" method="show" url=""/>
	<insta:feedback-panel/>
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
<form action="${actionUrl}" name="contractormasterform" method="POST">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="contractor_id" value="${bean.contractor_id}"/>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLegend">Contractor</legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel">Name:</td>
				<td>
					<input type="text" name="contractor_name" value="${bean.contractor_name}"	onblur="capWords(contractor_name);" class="required validate-length" length="50" title="Contractor name is required and max length of name can be 50" />
					<span class="star">&nbsp;*</span>
				</td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active, Inactive" /></td>
				<td class="formlabel">Grade:</td>
				<td><input type="text" name="grade" value="${bean.grade}"  /></td>
			</tr>

			<tr>
				<td class="formlabel">Address:</td>
							<td>
								<textarea  rows="5" cols="18" name="address" id="address" onblur="capWords(address);"
								 class="text-input" >${bean.address}
								</textarea>
							</td>
				<td class="formlabel">Phone:</td>
							<td>
								<input type="text" name="phone" value="${bean.phone}" onblur="capWords(phone);"  />
							</td>
				<td class="formlabel">Fax:</td>
							<td>
								<input type="text" name="fax" value="${bean.fax}"  />
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td> </td>
				<td class="formlabel">Email:</td>
							<td>
								<input type="text" name="email" value="${bean.email}"  />
							</td>
				<td class="formlabel">Website:</td>
							<td>
								<input type="text" name="website" value="${bean.website}"  />
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
								<input type="text" name="contact_person_name" value="${bean.contact_person_name}"  />
							</td>
							<td class="formlabel">Phone</td>
							<td>
								<input type="text" name="contact_person_phone" value="${bean.contact_person_phone}"  />
							</td>
							<td class="formlabel">Remarks:</td>
							<td>
								<input type="text" name="remarks" value="${bean.remarks}"  />
							</td>
						</tr>
					</table>
				</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0);" onclick="window.location.href='${cpath}/${pagePath}/add.htm?'">Add</a>
		|
		 <a href="${cpath}/${pagePath}/list.htm?sortOrder=contractor_name&sortReverse=false&status=A">Back To Dash Board</a>
	</div>

</form>

</body>
</html>
