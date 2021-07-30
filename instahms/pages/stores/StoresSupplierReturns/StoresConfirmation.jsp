<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<title>Stores Confirmation Screen - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
<script>
var deptId = '${ifn:cleanJavaScript(dept_id)}';
var gRoleId = '${ifn:cleanJavaScript(roleId)}';

function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 		alert("There is no assigned store, hence you dont have any access to this screen");
 		document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}
</script>
</head>
<body onload="checkstoreallocation();">
<div id="storecheck" style="display: block;">
<form method="GET" action="StoresSupplierReturnslist.do" >
<input type="hidden" name="_method" value="conformStatus"/>
<input type="hidden" name="chgstatus" value="${ifn:cleanHtmlAttribute(st)}"/>
<input type="hidden" name="retNo" value="${ifn:cleanHtmlAttribute(retno)}"/>
<h1>Pharmacy Status Confirmation Screen</h1>
<fieldset class="fieldSetBorder"  style="width:54ex;" >
  <legend class="fieldSetLabel">Return No: ${retDet.map.return_no } Details</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" border="0" >
		<tr>
		 <td class="formlabel">Supplier:</td>
		 <td class="forminfo">${retDet.map.supplier_name }</td>
	    </tr>
	    <tr>
		 <td class="formlabel">Return Date:</td>
		 <td class="forminfo">${retDet.map.returndate }</td>
	    </tr>
	    <tr>
		 <td class="formlabel">Return Type:</td>
		 <td class="forminfo">${retDet.map.return_type }</td>
	    </tr>
	    <tr>
		 <td class="formlabel">Status:</td>
		 <td class="forminfo">${retDet.map.status }</td>
	    </tr>
	    <tr>
		 <td class="formlabel">Return By:</td>
		 <td class="forminfo">${retDet.map.dept_name }</td>
	    </tr>

	</table>
  </fieldset>
  <div class="screenActions">
         <button type="submit" class="button" name="status" id="status" accessKey="${st != 'C' ? 'C' : 'R'}">
         	<c:if test="${st != 'C' }"><b><u>C</u></b>lose</c:if>
         	<c:if test="${st == 'C' }"><b><u>R</u></b>eopen</c:if>
         	</button>
         <a href="${pageContext.request.contextPath}/stores/StoresSupplierReturns.do?_method=getSupplierReturns&sortOrder=return_no&sortReverse=true">Back To DashBoard</a>
  </div>
</form>
</div>
</body>
</html>