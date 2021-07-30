<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add Surgery/Procedure Template - Insta HMS</title>
		<style>
			.scrolForContainer .yui-ac-content{
				 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
				 width: 500px;
			}
		</style>
		<script>
			var operations = ${operations};
			function init() {
				var dataSource = new YAHOO.util.LocalDataSource({result : operations});
				dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
				dataSource.responseSchema = {
					resultsList : "result",
					fields : [  {key : "operation_name"},
								{key : "op_id"},
								{key : "dept_name"}
							 ]
				};

				var autoComplete = new YAHOO.widget.AutoComplete("operation_name", 'opContainer', dataSource);
				autoComplete.minQueryLength = 0;
				autoComplete.maxResultsDisplayed = 50;
				autoComplete.forceSelection = true;
				autoComplete.resultTypeList = false;
				autoComplete.typeAhead = false;
				autoComplete.useShadow = false;
				autoComplete.animVert = false;
				autoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
					var operation = oResultData;
					return operation.operation_name + " [" + operation.dept_name + "]";
				}
				if (autoComplete._elTextbox.value != '') {
					autoComplete._bItemSelected = true;
					autoComplete._sInitInputValue = autoComplete._elTextbox.value;
				}
				autoComplete.itemSelectEvent.subscribe(setIds);
				autoComplete.selectionEnforceEvent.subscribe(function(){
					document.getElementById('op_id').value = '';
					document.getElementById('operation_name').value = '';
				});
			}
			function setIds(sType, oArgs) {
				var record = oArgs[2];
				document.getElementById('op_id').value = record.op_id;
			}

			function validate() {
				var operation = document.getElementById('operation_name').value;
				var ot_template = document.getElementById('ot_template').value;
				if (operation == '') {
					alert("Please select the Operation.");
					document.getElementById('operation_name').focus();
					return false;
				}
				if (ot_template == '') {
					alert("Please select the Template.");
					document.getElementById('ot_template').focus();
					return false;
				}

			}
		</script>

	</head>
	<body onload="init();">
		<div class=pageHeader>${ifn:cleanHtml(param._method=='add' ? 'Add' : 'Edit')} Surgery/Procedure Template </div>
		<insta:feedback-panel/>
		<form name="outhouseCenterForm" action="OperationTemplates.do" method="POST" autocomplete="off">
			<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>

			<fieldset class="fieldSetBorder">
				<table class="formTable">
					<tr>
						<td class="formlabel">Surgery/Procedure:</td>
						<td>
							<div id="autocomplete" style="padding-bottom: 20px">
								<input type="text" name="operation_name" id="operation_name" value="${bean.map.operation_name}"/>
								<div id="opContainer" class="scrolForContainer" ></div>
							</div>
							<input type="hidden" name="op_id" value="${bean.map.op_id}"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Template</td>
						<td>
							<select name="ot_template" class="dropdown" id="ot_template">
								<option value="">-- Select --</option>
								<c:forEach var="template" items="${templatesList}">
									<c:set var="value_from_master" value="${template.map.template_id},${template.map.format}"/>
									<c:set var="value_from_bean" value="${bean.map.template_id},${bean.map.format}"/>
									<option value="${value_from_master}" ${value_from_master == value_from_bean ? 'selected' : ''}>
										${template.map.template_name}
									</option>
								</c:forEach>
							</select>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
			</fieldset>
			<div class="screenActions">
				<button type="submit" name="save" ${param._method == 'add' ? '' : 'disabled'} accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
				|
				<c:url var="url" value="OperationTemplates.do">
					<c:param name="_method" value="list"/>
				</c:url>
				<a href="<c:out value='${url}' />">List</a>
				<c:url var="addUrl" value="OperationTemplates.do">
					<c:param name="_method" value="add"/>
				</c:url>
				| <a href="<c:out value='${addUrl}' />">Add</a>

			</div>
		</form>
	</body>
</html>
