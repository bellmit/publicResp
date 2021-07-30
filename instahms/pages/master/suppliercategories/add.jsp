<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.SUPPLIER_CATEGORY_PATH %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Supplier Category - Insta HMS</title>
<script type="text/javascript">
	
	function checkDuplicates(){
		var supplierCategoryName = document.supplierCategoryForm.supp_category_name.value;
		var supplierCatList = ${ifn:convertListToJson(supplierCatList)};
	    for(var i =0; i<supplierCatList.length; i++ ) {
	        var supplierCatName = supplierCatList[i].supp_category_name;
            if(supplierCategoryName == supplierCatName){
                alert("The Supplier Category Name Already Exists Please Enter Another Name");
                document.supplierCategoryForm.supp_category_name.focus();
                document.supplierCategoryForm.supp_category_name.value = "";
                return false;
            }
	        
		}
	}
</script>
</head>
<body>
<h1>Add Supplier Category</h1>
<insta:feedback-panel/>
<form action="create.htm" method="POST" name="supplierCategoryForm">
		<fieldset class="fieldsetborder" style="width: 25em;">

		<table class="formtable">
			<tr>
				<td class="formlabel">Supplier Category:</td>
				<td><input type="text" name="supp_category_name" id="supp_category_name" class="required validate-length" length="100" value="" onchange="return checkDuplicates();"/></td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S">
			  <b><u>S</u></b>ave
			</button>
			|
			<a href="${cpath}${pagePath}/list.htm?sortOrder=supp_category_name&sortReverse=false">Back To Dash Board</a></td>
		</tr>
	</table>
</form>
</body>
</html>
