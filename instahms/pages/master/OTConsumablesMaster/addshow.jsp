<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>OT Consumables Master - Insta HMS</title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="master/OTConsumables/OTConsumablesMaster.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<script type="text/javascript">
var cPath = '${cpath}';
var itList = <%=StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.CONSUMABLE_ITEMS) %>;

<c:if test="${param._method != 'add'}">
      Insta.masterData=${otConsumablesLists};
</c:if>
</script>
</head>
<body>

<c:choose>
    <c:when test="${param._method != 'add'}">
        <h1 style="float:left">Edit OT Consumable</h1>
        <c:url var="searchUrl" value="/master/OTConsumablesMaster.do"/>
        <insta:findbykey keys="operation_name,op_id" fieldName="operation_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add OT Consumable</h1>
    </c:otherwise>
</c:choose>
	<form name="otconsumablesform" method="POST" action="OTConsumablesMaster.do">
	<input type="hidden" name="_method" id="_method" value="${param._method == 'add' ? 'create' : 'update'}">
		<fieldset class="fieldSetBorder">
		<table class="formtable">
			<insta:feedback-panel/>

			<tr>
			<td class="formlabel"><table><tr><td class="formlabel">Operation Name:</td>
			<td align="left">
				<c:if test="${param._method == 'add'}">
					<select name="op_id" id="op_id" class="dropdown">
						<option value="">...Operation Name...</option>
						<c:forEach var="operation" items="${operations}">
							<option value="${operation.map.op_id}">${operation.map.operation_name}</option>
						</c:forEach>
					</select>
					</c:if>

				<c:if test="${param._method == 'show'}">
				<input type="hidden" name="op_id" id="op_id" value="${consumables[0].map.op_id}"/>
					<label><b>${consumables[0].map.operation_name}</b></label>
				</c:if>
			</td>
			<td width="370" style="visibility: ${ifn:cleanHtmlAttribute(status)}" align="right">Inactive:</td>
			<td style="visibility: ${ifn:cleanHtmlAttribute(status)}">
				<input type="checkbox" name="status" id="status" ${active } <c:if test="${consumables[0].map.status eq 'false'}">checked="checked"</c:if> />
			</td>
			</tr></table>
		</tr>
		<tr><td><fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Add Consumables</legend>
			<table>
			<tr>(only items of issue type consumable can be added here)</tr>
			<tr><td class="formlabel">Category: </td>
			<td><insta:selectdb name="category_id" table="store_category_master" valuecol="category_id" displaycol="category"
					dummyvalue="....Category...." onchange="getConsumableItems();" filtered="true"  filtercol="issue_type" filtervalue="C" orderby="category"/></td>
					<td>Consumable Name:</td>
			<td>
			<select name="consumable_id" id="consumable_id" class="dropdown" onchange="setIsuueDetails(this.value);" >
				<option>...Consumable...</option>
			</select>
			</td>
			<td class="formlabel">Unit UOM:</td>
			<td id="issueqty" style="font-weight: bold"></td>

			<td class="formlabel">Quantity Needed:</td>
			<td>
				<input type="text"  class="number" name="qty_needed" id="qty_needed" maxlength="100" onkeypress="return enterNumAndDot(event);" onblur="return makeingDec(this)" />
			</td>
			<td>
				<input type="button" name="add" id="name" value="Add" onclick="return addReagents(op_id,consumable_id,qty_needed)" />
			</td></tr></table></fieldset></td>
		</tr>
		<tr>
			<td>
				<table id="reagentstable" class="delActionTable" border="0" cellpadding="0" cellspacing="0" width="40%">
					<tr id="reagentRow0" class="header">
						<td class="first">Consumable Name</td>
						<td>Qty</td>
						<td class="last">&nbsp;</td>
					</tr>

					<c:forEach items="${consumables }" var="otconsumable"  varStatus="loop">
						<tr id="${loop.index + 1}">
							<td class="border">${otconsumable.map.item_name }
							<input type="hidden" name="consumable" id="consumable${loop.index +1}" value="${otconsumable.map.consumable_id}"/></td>
							<td class="border"><input type="text" name="qty" id="qty${loop.index +1}" class="number" value="${otconsumable.map.qty_needed }" size="3"
								onkeypress="return enterNumAndDot(event)", onblur="makeingDecValidate(this)"; style="border-left : 1px #cad6e3 solid;"/></td>
							<td class="last"><img src="${cpath}/icons/Delete.png" name="delItem" id="delItem${loop.index +1}" onclick="deleteItem(this,${loop.index +1});"/>
							<input type="hidden" name="hdeleted" id="hdeleted${loop.index + 1}" value="false"/></td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
	</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S" onclick="return checkduplicate();"><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
				<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/OTConsumablesMaster.do?_method=add'">Add</a>
			|
			</c:if>
			<a href="javascript:void(0)" onclick="doClose();return true;">Consumable List</a>
		</div>

	</form>
</body>
</html>