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
<title>Edit Supplier Category - Insta HMS</title>
<script type="text/javascript">
function checkDuplicates(){
	var supplierCategoryName = document.supplierCategoryForm.supp_category_name.value;
	var supplierCategoryId = document.supplierCategoryForm.supp_category_id.value;
	var supplierCatList = ${ifn:convertListToJson(supplierCatList)};
    for(var i =0; i<supplierCatList.length; i++ ) {
        var supplierCatName = supplierCatList[i].supp_category_name;
        var supplierCatId = supplierCatList[i].supp_category_id;
        if (supplierCatId != supplierCategoryId) {
        	if(supplierCategoryName == supplierCatName){
                alert("The Supplier Category Name Already Exists Please Enter Another Name");
                document.supplierCategoryForm.supp_category_name.focus();
                document.supplierCategoryForm.supp_category_name.value = '${bean.supp_category_name}';
                return false;
            } 
        }
  	}
    
}
</script>
</head>
<body>
<h1>Edit Supplier Category</h1>
<insta:feedback-panel/>
<form action="update.htm" method="POST" name="supplierCategoryForm">
	<input type="hidden" name="supp_category_id" id="supp_category_id" value="${bean.supp_category_id}"/>

	<fieldset class="fieldsetborder" style="width: 25em;">

		<table class="formtable">
			<tr>
				<td class="formlabel">Supplier Category:</td>
				<td><input type="text" name="supp_category_name" id="supp_category_name" class="required validate-length" length="100" value="${bean.supp_category_name}" onchange="return checkDuplicates();"/></td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="U">
			    <b><u>U</u></b>pdate
			</button>
			|
				<a href="javascript:void(0);" onclick="window.location.href='${cpath}${pagePath}/add.htm?_method=add'">Add</a>
			|
			<a href="${cpath}${pagePath}/list.htm?sortOrder=supp_category_name&sortReverse=false">Back To Dash Board</a></td>
		</tr>
	</table>
</form>
</body>
</html>
