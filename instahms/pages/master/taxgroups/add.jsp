<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.ITEM_GROUP_PATH %>"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Tax Group - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	

var namesList = ${ifn:convertListToJson(nameList)};
	
	function checkDuplicates() {
		document.getElementById('item_group_name').value = trim(document.getElementById('item_group_name').value);
		var type= document.getElementById('item_group_type_id').value;
		var name = document.getElementById('item_group_name').value;
		//var code = document.getElementById('group_code').value;
		var GoupName = document.getElementById('item_group_name').value.toLowerCase();
		var groupCode = document.getElementById('group_code').value;
		var id = document.itemGroupForm.item_group_id.value;
		
		if (name == '') {
			alert("Please enter tax group.");
			document.getElementById('item_group_name').focus();
			return false;
		}
		if(groupCode == " "){
			alert("Please enter tax group code.");
			document.getElementById('group_code').focus();
			return false;
		}
		for (var i = 0; i<namesList.length; i++) {
			//var dbCode = codesList[i].group_code;
			var dbGroupName = namesList[i].item_group_name.toLowerCase();
			var dbId = namesList[i].item_group_id;
			if (id != dbId ) {
				if (GoupName == dbGroupName) {
					alert("Tax group  already exists, Please enter another tax group.");
					document.getElementById('item_group_name').focus();
					return false;
				}
			}
		}
		return true;
	}
	
function numbersonly(e) 
{
    var key;
    var keychar;

    if (window.event) 
        key = window.event.keyCode;
    else if (e) 
        key = e.which;
    else 
        return true;

    keychar = String.fromCharCode(key);

    if ((key==null) || (key==0) || (key==8) ||  (key==9) || (key==13) || (key==27))
       return true;     
    else if ((("0123456789").indexOf(keychar) > -1))
       return true;
    else
       return false;
}
</script>

</head>

<c:set var="actionUrl" value="${cpath}${pagePath}/create.htm"/>
<form action="${actionUrl}" method="POST" name="itemGroupForm">
	<input type="hidden" name="item_group_id" value="${bean.item_group_id}"/>

	<h1>Add Tax Group</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Tax Group Type:</td>
				<td>
					<insta:selectdb name="item_group_type_id" id="item_group_type_id" table="item_group_type" valuecol="item_group_type_id"
							displaycol="item_group_type_name"
							value="${item_group_type_name}"  orderby="item_group_type_name" filtercol ="item_group_type_id"  filtervalue ="TAX"/> 
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Tax Group:</td>
				<td>
					<input type="text" name="item_group_name" id="item_group_name" value=""  class="required validate-length"
					length="50" title="Max length of tax group can be 50"  />
					<span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Tax Group Code:</td>
				<td>
			       <insta:selectoptions name="group_code"  id= "group_code" value="" opvalues=" ,GST,IGST,VAT,KSACTA,KSACEX" optexts=" ----Select----,India GST,India IGST,GCC VAT,KSA VAT Citizen Taxable,KSA VAT Citizen Exempt" />
			       <span class="star">*</span>
			     </td>
			</tr>
			
			<tr>
				<td class="formlabel">Display Order:</td>
				<td>
					<input type="text" name="item_group_display_order" value=""  onkeypress="return numbersonly(event);"  />
				</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkDuplicates()"><b><u>S</u></b>ave</button>
		| <a href="${cpath}${pagePath}/list.htm?status=A&amp;item_group_type_id=TAX">Tax Group List</a>
	</div>
</form>

</body>
</html>
