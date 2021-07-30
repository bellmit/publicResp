<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Linen Usage Details</title>
<insta:link type="js" file="LinenChange/LinenChange.js"/>
<style >
.selectedRow {background-color: #C7E782;}
</style>
</head>

<body onload="editDialog();showEditDialog(1);">
	<h1>Edit Linen Usage Details</h1>
	<form name="LinenChangeForm">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" id="dialogId" name="dialogId">
	
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Linen Usage Details</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Linen Category:</td>
				<td><b>${linenCategoryBean.map.category_name}</b></td>
				<td class="formlabel">Linen User:</td>
				<td>
					<b>${linenUserBean.map.category_user_name}</b>
					<input type="hidden" name="linen_user_id" value="${linenUserBean.map.category_user_id}">
				</td>
			</tr>
			<tr>
				<td class="formlabel">Issue Date:</td>
				<td>
					<b><fmt:formatDate value="${linenUserMainBean.map.issue_date_time}" pattern="dd-MM-yyyy HH:mm"/></b>
				</td>
				<td class="formlabel">Store:</td>
				<td>
					<b>${storeNameBean.map.dept_name}</b>
				</td>
			</tr>
		</table>
	</fieldset>
	
	<table class="resultList" id="editLinenItemTable">
		<tr>
			<th>Linen Item</th>
			<th>Batch/Sl No</th>
			<th>Qty</th>
			<th>Remarks</th>
			<th>Cleaning Type</th>
			<th>Reuse</th>
			<th>Status</th>
			<th></th>
			<th></th>
		</tr>
		
		<c:set var="i" value="1"/>
		<c:forEach items="${linenItems}" var="linenItem" varStatus="status">
			<tr id="tableRow${i}" >
				<input type="hidden" name="transaction_id" id="transaction_id${i}" value="${linenItem.map.transaction_id}">
				<td>
					${linenItem.map.linen_name}
					<input type="hidden"  id="linen_item_name${i}" value="${linenItem.map.linen_name}">
				</td>
				<td>
					${linenItem.map.batch_no}
					<input type="hidden"  id="batch_no${i}" value="${linenItem.map.batch_no}">
				</td>
				<td>
					${linenItem.map.qty}
					<input type="hidden"  id="qty${i}" value="${linenItem.map.qty}">
				</td>
				<td>
					<label id="label_issue_remarks${i}">${linenItem.map.issue_remarks}</label>
					<input type="hidden"  name="issue_remarks" id="issue_remarks${i}" value="${linenItem.map.issue_remarks}">
				</td>
				<td>
					<label id="label_cleaning_type${i}">
						<c:if test="${linenItem.map.cleaning_type eq 'N'}">Normal</c:if>
						<c:if test="${linenItem.map.cleaning_type eq 'S'}">Sterilize</c:if>
					</label>
					<input type="hidden"  name="cleaning_type" id="cleaning_type${i}" value="${linenItem.map.cleaning_type}">
				</td>
				<td>
					<label id="label_reuse_flag${i}">
						<c:if test="${linenItem.map.reuse_flag eq 'Y'}">Yes</c:if>
						<c:if test="${linenItem.map.reuse_flag eq 'N'}">No</c:if>
					</label>
					<input type="hidden"  name="reuse_flag" id="reuse_flag${i}" value="${linenItem.map.reuse_flag}">
				</td>
				<td>
					<label id="label_status${i}">
						<c:if test="${linenItem.map.status eq 'I'}">In Use</c:if>
						<c:if test="${linenItem.map.status eq 'A'}">Availabel</c:if>
						<c:if test="${linenItem.map.status eq 'S'}">Send To Laundry</c:if>
					</label>
					<input type="hidden"  name="status" id="status${i}" value="${linenItem.map.status}">
				</td>
				<td>
				
				</td>
				<td><label id="itemCheck${i}">
					<img class="button" name="add" id="add${i}" src="${cpath}/icons/Edit.png"
							title="Edit Po Items" onclick="showEditDialog(${i})"   >
					</label>
				</td>
				<c:set var="i" value="${i+1}"/>
			</tr>
		</c:forEach>
	</table>
	
	<div id="editLinenItemDialog" style="display: none">
		<div class="hd">Edit Linen Item</div>
		<div class="bd">
			
			<table width="100%" cellspacing="1" cellpadding="1"  style="margin-top: 4px;">
				<tr>
					<td class="formlabel">Linen Name:</td>
					<td><label id="linenNameLabel"></label></td>
					<td class="formlabel">Batch No:</td>
					<td><label id="batchNoLabel"></label></td>
					<td class="formlabel">Qty:</td>
					<td colspan="3"><label id="qtyLabel"></label></td>
				</tr>
				
				<tr>
					<td class="formlabel">Remarks:</td>
					<td>
						<input type="text" name="eRemarks" id="eRemarks" style="margin-top: 4px;margin-right: 4px;">
					</td>
					<td class="formlabel">Cleaning Type:</td>
					<td>
						<insta:selectoptions name="eCleaningType" id="eCleaningType" value=""
							 opvalues=" ,N,S" optexts="-- Select --,Normal,Sterilze" style="margin-top: 4px;"/>
					</td>
					<td class="formlabel">Reuse:</td>
					<td>
						<insta:selectoptions name="eReuse" id="eReuse" value="" opvalues=" ,Y,N" 
							optexts="-- Select --,Yes,No" style="margin-top: 4px; margin-bottom: 4px;"/>
					</td>
					<td class="formlabel">Status:</td>
					<td>
						<insta:selectoptions name="eStatus" id="eStatus" value=""
							 opvalues=" ,I,A,S" optexts="-- Select --,In Use,Availabel,Send To Laundry" style="margin-top: 4px;"/>
					</td>
				</tr>
			</table>
			<table >
				<tr>
					<td>
						<button type="button" id="saveDialog" name="saveDialog" accesskey="S"   style="display: inline;"
							class="button" onclick="updateValues();" ><label><u><b>S</b></u>ave</label></button>
					</td>
					<td>
						<button type="button" id="prevDialog" name="prevDialog" accesskey="P"   style="display: inline;"
							class="button" onclick="addItems(this);" ><label> << <u><b>P</b></u>revious</label></button>
					</td>
					<td>
						<button type="button" id="nextDialog" name="nextDialog" accesskey="N"   style="display: inline;" 
							class="button" onclick="addItems(this);" ><label> <u><b>N</b></u>ext >></label></button>
					</td>
				</tr>
			</table>
			
		</div>
	</div>
	
	<c:url var="url" value="LinenChange.do">
		<c:param name="_method" value="list"/>
	</c:url>
	<table class="screenActions">
		<tr>
			<td>
				<input type="submit" value="Save">&nbsp;|&nbsp;
				<a href="<c:out value='${url}' />">Linen Usage Details list</a>
			</td>	
		</tr>
	</table>
	
	</form>
</body>

</html>
