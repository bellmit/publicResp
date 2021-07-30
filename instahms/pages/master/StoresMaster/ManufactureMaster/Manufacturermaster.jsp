<%@page import="org.apache.struts.Globals"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title>Insta HMS</title>
<script>
  var manufacturerID='${manfdto.map.manf_code}';
  var manfname = ${manfNamesJSON};

  <c:if test="${not empty manfdto}">
      Insta.masterData=${manufacturersLists};
  </c:if>
</script>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="/masters/Manufacturermaster.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath }"/>
<body class="yui-skin-sam" onload="">
<c:choose>
    <c:when test="${not empty manfdto }">
        <h1 style="float:left">Edit Manufacturer Details</h1>
        <c:url var="searchUrl" value="/pages/masters/insta/stores/ManufacturerDetails.do"/>
        <insta:findbykey keys="manf_name,manf_code" fieldName="manufacturer_Id" method="getManfDetailsScreen" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
      <h1>Add Manufacturer Details</h1>
    </c:otherwise>
</c:choose>
<insta:feedback-panel/>
<form method="GET" name="manfacturer_Form" action="ManufacturerDetails.do" >
<input type="hidden" name="_method" value="insertOrUpdatemanufacturerDetails">
<input type="hidden" name="operation" value="${empty manfdto.map.manf_code ? 'insert':'update' }">
<input type="hidden" name="manf_code" value="${manfdto.map.manf_code}"/>
<input type="hidden" name="manufacturer" value="${manfdto.map.manf_name}"/>

	<table>
	   <tr>
		<td align="center"><span ><b>${ifn:cleanHtml(msg)}</b></span></td>
	</tr>
	   <tr>
	   		<td>
	   			<div class="mainfiltercontent" style="width: 400px;">
	   				<fieldset style="width:350" class="fieldSetBorder">
	   					<legend class="fieldSetLabel"> Manufacturer</legend>
							 <table   class="formtable" >
							 	<tr>
			     					<td class="formlabel">Manufacturer Name:</td>
			     					<td><input type="text" name="manf_name" id="manf_name" maxlength="100" value="${manfdto.map.manf_name }"  >
			     						<span class="star">&nbsp;*</span></td>
			   					</tr>
			   					<tr>
			     					<td class="formlabel">Status:</td>
			     					<td><insta:radio name="status" radioValues="A,I" value="${not empty manfdto.map.status ? manfdto.map.status : 'A'}"
			     					radioText="Active,Inactive" radioIds="active,inactive" /></td>
			   					</tr>
			   					<tr>
			     					<td class="formlabel">Manufacturer Code:</td>
			     					<td><input type="text" name="manf_mnemonic" id="manf_mnemonic" maxlength="5"
			     						value="${manfdto.map.manf_mnemonic }"><span class="star">&nbsp;*</span>
			     						<input type="hidden" name="manf_code" id="manf_code" maxlength="5"
			     						value=${manfdto.map.manf_code}"></td>
			   					</tr>
								<tr>
			  						<td class="formlabel"> </td>
			    					<td class="formlabel"></td>
			  					</tr>
							 </table>
	   				</fieldset>
	   			</div>
	   		</td>
	   		<td>
	   			<div class="mainfiltercontent" style="width: 150px;">
	   				<fieldset style="width:300" class="fieldSetBorder">
	   					<legend class="fieldSetLabel"> Region</legend>
			 				<table   class="formtable" >
			 					<tr>
			        				<td class="formlabel">Country:</td>
			        				<td><input type="text" name="manf_country" id="manf_country" maxlength="30"
			        					value="${manfdto.map.manf_country }"></td>
			    				</tr>
			    				<tr>
			        				<td class="formlabel">State:</td>
			        				<td><input type="text" name="manf_state" id="manf_state" maxlength="30"
			        					value="${manfdto.map.manf_state }"></td>
			    				</tr>
			    				<tr>
			        				<td class="formlabel">City:</td>
			       					<td><input type="text" name="manf_city" id="manf_city" maxlength="30"
			       						value="${manfdto.map.manf_city }"></td>
			    				</tr>
			 				</table>
	   				</fieldset>
	   			</div>
	   		</td>
	   </tr>
	</table>
	<table>
	   <tr>
	   		<td>
	   			<div class="mainfiltercontent" style="width: 500px;">
	   				<fieldset style="width:600" class="fieldSetBorder">
	   					<legend class="fieldSetLabel"> Contact Information</legend>
							<table class="formtable" >
								<tr>
								   <td class="formlabel">Address:</td>
								   <td colspan="3"><textarea rows="4" name="manf_address" id="manf_address" cols="60"   onkeypress="return chk(event);"
									        onblur="chklen();"><c:out value="${manfdto.map.manf_address}"/></textarea>(max 200 characters)</td>
								</tr>
								<tr>
								   <td class="formlabel">Phone 1:</td>
						           <td><input type="text" name="manf_phone1" id="manf_phone1" maxlength="20"
						           		value="${manfdto.map.manf_phone1 }" >(Ex:080-25252522)</td>
							        <td class="formlabel">Phone 2:</td>
							        <td><input type="text" name="manf_phone2"  id="manf_phone2" maxlength="20"
							        	value="${manfdto.map.manf_phone2 }"  ></td>
								</tr>
								<tr>
								 	<td class="formlabel">Postal Code:</td>
							        <td><input type="text" name="manf_pin" id="manf_pin" size="24"
							        	value="${manfdto.map.manf_pin }"   maxlength="6" onChange="return checkPin(document.forms[0].manf_pin)"
							        	onkeypress="return enterNumOnly(event)"></td>
						     	   <td class="formlabel">Fax:</td>
								   <td><input type="text" name="manf_fax" id="manf_fax" maxlength="20"
								   		value="${manfdto.map.manf_fax }">(Ex:080-3938388)</td>
							    </tr>
							    <tr>
									<td class="formlabel">Email:</td>
									<td><input type="text" name="manf_mailid" id="manf_mailid" maxlength="50"
										value="${manfdto.map.manf_mailid }"></td>
									<td class="formlabel">Web Site:</td>
									<td ><input type="text" name="manf_website" id="manf_website" value="${manfdto.map.manf_website }"
										maxlength="50">(www.google.com)</td>
							    </tr>
							</table>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
	<div class="screenActions">
		<button type="button" accesskey="S" name="save" class="button" onclick="return validations();" >
		<b><u>S</u></b>ave</button>
		|
		<c:if test="${not empty manfdto}">
			<a href="javascript:void(0);" onclick="window.location.href='${cpath}/pages/masters/insta/stores/ManufacturerDetails.do?_method=getManfDetailsScreen'">Add</a>
		|
		</c:if>
		<a href="${cpath }/pages/masters/insta/stores/ManufacturerDetails.do?_method=getManufacturerDetails&sortOrder=manf_name&sortReverse=false&status=A">Back To DashBoard</a>
	</div>

</form>
</body>
</html>
