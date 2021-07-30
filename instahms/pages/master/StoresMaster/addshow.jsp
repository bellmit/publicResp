<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Item Category - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var gRoleId = '${role}';
		var action = '${ifn:cleanJavaScript(param._method)}';
		var inDiscPlan = '${inDiscPlan}';
		function doClose() {
			window.location.href = "";
		}

      function focus(){
      	document.categorymasterform.category.focus();
      }

      function checkDiscount() {
		if (parseFloat(document.getElementById("discount").value ) > 100) {
			alert("Discount should be less than or equal 100 ");
			document.getElementById("discount").focus();
			return false;
		}
		if (document.categorymasterform.status.value == 'I') {
			if (inDiscPlan && inDiscPlan != 'false') {
				alert("Item Category is mapped to an active Discount Plan");
				return false;
			}
		}
      }

     function editaccess() {
 		if ((gRoleId != 1 ) && (action != 'add')) {
 			document.getElementById("save").disabled = 'true';
 		}
	}


      <c:if test="${param._method != 'add'}">
           Insta.masterData=${categoriesLists};
     </c:if>
	</script>
</head>
<body onload="focus()">
<c:choose>
    <c:when test="${param._method != 'add'}">
        <h1 style="float:left">Edit Item Category</h1>
        <c:url var="searchUrl" value="/master/StoresMaster.do"/>
        <insta:findbykey keys="category,category_id" fieldName="category_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Item Category</h1>
    </c:otherwise>
</c:choose>
	<insta:feedback-panel/>

<form action="StoresMaster.do" name="categorymasterform" method="GET">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="category_id" value="${bean.map.category_id}"/>
	</c:if>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLegend">Category Details</legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel">Category Name:</td>
				<td>
					<input type="text" name="category" value="${bean.map.category}"	onblur="capWords(category);" class="required validate-length" length="200" title="Category name is required and max length of name can be 200" />
					<span class="star">&nbsp;*</span>
				</td>

				<td class="formlabel">Identification:</td>
				<td><insta:selectoptions name="identification" value="${bean.map.identification}"  opvalues="S,B" optexts="Serial NO,Batch No" /></td>

				<td class="formlabel">Issue Type:</td>
				<td><insta:selectoptions name="issue_type" value="${bean.map.issue_type}"
					opvalues="P,C,L,R" optexts="Permanent, Consumable, Reusable, Retail Only"  /></td>
			</tr>

			<tr>
				<td class="formlabel">Billable:</td>
				<td><insta:selectoptions name="billable" value="${bean.map.billable}" opvalues="true,false" optexts="Yes, No" /></td>
				<td class="formlabel">Retailable:</td>
				<td><insta:selectoptions name="retailable" value="${bean.map.retailable}" opvalues="true,false" optexts="Yes, No" /></td>
				<td class="formlabel">Claimable:</td>
				<td><insta:selectoptions name="claimable" value="${bean.map.claimable}" opvalues="true,false" optexts="Yes, No" /></td>
			</tr>
			<tr>
				<td class="formlabel">Validate Expiry Date:</td>
				<td><insta:selectoptions name="expiry_date_val" value="${bean.map.expiry_date_val}" opvalues="true,false" optexts="Yes, No" /></td>
				<td class="formlabel">Discount(%):</td>
				<td><input type="text" name="discount" id="discount" value="${empty bean.map.discount ? '0.00' : bean.map.discount}">
					<script>
						<c:if test="${not empty dsabled}" >
							document.categorymasterform.identification.disabled = ${dsabled};
							document.categorymasterform.issue_type.disabled = ${dsabled};
							document.categorymasterform.billable.disabled = ${dsabled};
							document.categorymasterform.retailable.disabled = ${dsabled};
							document.categorymasterform.claimable.disabled = ${dsabled};
							document.categorymasterform.expiry_date_val.disabled = ${dsabled};
						</c:if>
					</script>
				</td>
				<td class="formlabel">Prescribable</td>
				<td><insta:selectoptions name="prescribable" value="${bean.map.prescribable}" opvalues="true,false" optexts="Yes, No" /></td>
				
				</tr>
			<tr>
				<td class="formlabel">Asset Tracking:</td>
				<td><insta:selectoptions name="asset_tracking" value="${bean.map.asset_tracking}" opvalues="N,Y" optexts="No, Yes" /></td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active, Inactive" /></td>
				<td class="formlabel">Drug:</td>
				<td><insta:selectoptions name="is_drug" value="${bean.map.is_drug}" opvalues="N,Y" optexts="No, Yes" /></td>
			</tr>
		</table>
	</fieldset>
	<div style="display:${dsabled == true ? 'block':'none' }">
		 *** (This Category is connected to item you can't
		        update Identification,Issue Type & Billable fields) ***
	</div>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkDiscount()"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
		<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/StoresMaster.do?_method=add'">Add</a>
		|
		</c:if>
		 <a href="${cpath}/master/StoresMaster.do?_method=list&sortOrder=category&sortReverse=false&status=A">Back To Dash Board</a>
	</div>



</form>

</body>
</html>
