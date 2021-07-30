<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add New Items</title>
<insta:link type="js" file="LinenChange/LinenChange.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="ajax.js" />
<script>
	var popurl = '${pageContext.request.contextPath}';
	var linenUsersNames = ${linenUsers};
	var linenItems = ${linenItems};
</script>
<style>
	#userAutocomplete {
		width: 10em;
	}
</style>
</head>

<body onload="init();" class="yui-skin-sam">
	<h1>Add New Items</h1>
	<form name="LinenChangeForm">
	<insta:feedback-panel/>
	<input type="hidden" name="_method" value="insertNewItems">
	<input type="hidden" id="dialogId" value=""/>
	<input type="hidden" id="dialogStatus" value="">

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Linen Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Linen Category:</td>
					<td>
						<insta:selectdb id="temp_linen_category" name="temp_linen_category" value="" table="linen_user_category"
							valuecol="category_id"  displaycol="category_name"  filtered="false"
							class="dropdown"  onchange="loadLinenUser();" dummyvalue="-- Select --"  />
						<span class="star">*</span>
					</td>
					<td class="formlabel">Linen User:</td>
					<td>
						<select name="temp_linen_user" class="dropdown">
							<option value="">-- Select --</option>
						</select>
						<span class="star">*</span>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Issue Date:</td>
					<td>
						<insta:datewidget name="transferDate"  valid="past" value="today" btnPos="left" />
					</td>
					<td class="formlabel">Store:</td>
					<c:choose>
						<c:when test="${(roleId == 1) || (roleId == 2) || (multiStoreAccess == 'A')}">
						<td><insta:userstores username="${userid}" elename="store"  id="store" val="${defStoreVal}"/>
						</td>
						</c:when>
						<c:otherwise>
						<td><b>${ifn:cleanHtml(store_name)}</b>
						<input type = "hidden" name="store" id="store" value="${ifn:cleanHtmlAttribute(store_id)}" />
						</td>
						</c:otherwise>
					</c:choose>
				</tr>
			</table>
		</fieldset>

		<table class="datatable" width="100%" id="itemlistTable">
			<tr>
				<td>Linen Item</td>
				<td>Batch/Sl No</td>
				<td>Qty</td>
				<td>Remarks</td>
				<td></td>
				<td></td>
			</tr>

			<tr id="tableRow1">
				<td>
					<label id="linenName_label1"></label>
					<input type="hidden" name="linen_name" id="linen_name1"/>
				</td>
				<td>
					<label id="batchNo_label1"></label>
					<input type="hidden" name="batch_no" id="batch_no1"/>
				</td>
				<td>
					<label id="qty_label1"></label>
					<input type="hidden" name="qty" id="qty1"/>
				</td>
				<td>
					<label id="remarks_label1"></label>
					<input type="hidden" name="remarks" id="remarks1"/>
					<input type="hidden" name="cleaningType" id="cleaningType1"/>
					<input type="hidden" name="reuse" id="reuse1"/>
				</td>
				<td>
					<label id="itemRow1"></label>
					<input type="hidden" name="hdeleted" id="hdeleted1"/>
				</td>
				<td>
					<button name="addBut" id="addBut1" onclick="showAddNewItemDialog(1); return false;" class="imgButton" accesskey="+" title="Add New Item">
						<img class="button" name="add" id="add1" src="../icons/Add.png"
							style="cursor:pointer;" >
					</button>
				</td>
			</tr>


		</table>

		<c:url var="url" value="LinenChange.do">
			<c:param name="_method" value="list"/>
		</c:url>
		<table class="formtable">
			<tr>
				<td>
					<input type="button" value="Save" onclick="validate();">&nbsp;|&nbsp;
					<a href="<c:out value='${url}' />">Linen Usage Details list</a>
				</td>
			</tr>
		</table>

		<div id="addNewItem" style="display: none">
			<div class="hd">Add New Item Dialog</div>
			<div class="bd">

				<table class="dashboard">
					<tr>
						<td style="width: 13em">Linen Item</td>
						<td style="width: 10em">Batch/Sl No - Qty</td>
						<td style="width: 10em">Remarks</td>
						<td style="width: 10em">Cleaning Type</td>
						<td style="width: 10em">Reuse</td>
					</tr>
					<tr>
						<td valign="top" style="width: 10em; ">
							<div id="linenItemAutocomplete">
								<input type="text" name="linen_item" id="linen_item"
									class="field" style="width:140px;" />
								<div id="linenItemContainer"></div>
							</div>
						</td>
						<td style="width: 10em">
							<select name="batchNo" id="batchNo" class="dropdown" >
								<option value="">-- Select --</option>
							</select>
						</td>
						<td style="width: 10em">
							<input type="text" name="remarks" id="remarks">
						</td>
						<td style="width: 10em">
							<insta:selectoptions name="eCleaningType" id="eCleaningType" value=""
								 opvalues=" ,N,S" optexts="-- Select --,Normal,Sterilze" />
						</td>
						<td style="width: 10em">
							<insta:selectoptions name="eReuse" id="eReuse" value="" opvalues=" ,Y,N"
								optexts="-- Select --,Yes,No" />
						</td>
					</tr>
				</table>

				<table>
					<tr>
						<td style="padding-top: 5px;">
							<button type="button" id="okbtn" value="OK" accesskey="O"><b><u>O</u></b>K</button>
							<button type="button" id="cancelBtn" value="Cancel" >Cancel</button>
						</td>
					</tr>
				</table>
			</div>
		</div>

	</form>
</body>

</html>
