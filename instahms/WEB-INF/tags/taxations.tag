<%@tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<script type="text/javascript">


function itemsubgroupinit(){
	subGroupDialog();
}

var itemSubGroupDialog;
var newRowinserted = true;
var currentRow = null;
var itemGroupTypeList = ${ifn:convertListToJson(itemGroupTypeList)}

function subGroupDialog() {
	var itemSubgroupDIV = document.getElementById("itemSubgroupDIV");
	itemSubgroupDIV.style.display = 'block';
	itemSubGroupDialog = new YAHOO.widget.Dialog('itemSubgroupDIV', {
				width:"300px",
				visible:false,
				modal:true,
		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleItemTaxationCancel,
	                                                scope:itemSubGroupDialog,
	                                                correctScope:true } );
	itemSubGroupDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	itemSubGroupDialog.cancelEvent.subscribe(cancel);
	itemSubGroupDialog.render();
}

function handleItemTaxationCancel() {
	itemSubGroupDialog.cancel();
}

function cancel() {
	newRowinserted = true;
	currentRow = null;
}

function onSelectItemGroupType(){
	selectedGroupType = document.getElementById("itemgrouptypeId").value;
	document.getElementById("itemgroupId").length = 1;
	var index = 1;
	for(var i=0; i<itemGroupList.length; i++) {
		if(itemGroupList[i].item_group_type_id == selectedGroupType) {
			document.getElementById("itemgroupId").length = index+1;
			document.getElementById("itemgroupId").options[index].value=itemGroupList[i].item_group_id;
			document.getElementById("itemgroupId").options[index].text=itemGroupList[i].item_group_name;
			index = index+1;
		}
	}
	
}
function onSelectItemGroupName(){
	selectedGroupName = document.getElementById("itemgroupId").value;
	document.getElementById("itemsubgroupId").length = 1;
	var index = 1;
	for(var i=0; i<itemSubGroupList.length; i++) {
		if(itemSubGroupList[i].item_group_id == selectedGroupName) {
			document.getElementById("itemsubgroupId").length = index+1;
			document.getElementById("itemsubgroupId").options[index].value=itemSubGroupList[i].item_subgroup_id;
			document.getElementById("itemsubgroupId").options[index].text=itemSubGroupList[i].item_subgroup_name;
			index = index+1;
		}
	}
}


function showItemSubgroupDialog(obj) {
	if(itemGroupTypeList.length == 1){
		document.getElementById('itemgrouptypeId').value = itemGroupTypeList[0].item_group_type_id;
		onSelectItemGroupType();
	}else{
		document.getElementById('itemgrouptypeId').value = '';
	}
	document.getElementById('itemgroupId').value = '';
	document.getElementById('itemsubgroupId').value = '';
	itemSubGroupDialog.cfg.setProperty("context", [obj, "tl", "bl"], false);
	itemSubGroupDialog.show();
}
function addToTableItemSubGroup() {
	if(document.getElementById('itemgrouptypeId').value == '') {
		alert("Please select Tax Group Type");
		return false;
	}
	if(document.getElementById('itemgroupId').value == '') {
		alert("Please select Tax Group.");
		return false;
	}
	if(document.getElementById('itemsubgroupId').value == '') {
		alert("Please select Tax Sub Group.");
		return false;
	}
	var itemgroupNames = document.getElementsByName("item_group_id");
	var itemSubgroupNames = document.getElementsByName("item_subgroup_id");
	for (var i=0;i<itemSubgroupNames.length;i++) {
		var id=getItemSubGroupRows();
		if ((i!=id) && (itemgroupNames[i].value == document.getElementById("itemgroupId").value)) {
			alert("You are restricted from mapping the same tax group to the item. Please check and try again.");
			document.getElementById("itemgroupId").focus();
				return false;
		}
	}
	/* var itemSubgroupNames = document.getElementsByName("item_subgroup_id");
	for (var i=0;i<itemSubgroupNames.length;i++) {
		var id=getItemSubGroupRows();
		if ((i!=id) && (itemSubgroupNames[i].value == document.getElementById("itemsubgroupId").value)) {
			alert("Duplicate Entry");
			document.getElementById("itemsubgroupId").focus();
				return false;
		}
	} */
	
	var tableObj = document.getElementById('itemsubgroupTbl');
	var currentRowIndex = -1;
	if (newRowinserted == false) {
		var rowObj = getThisRow(currentRow, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);
	}
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-2];
	var newRow = '';
	
	if (newRowinserted) {
		var id = rowsLength-2;
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = 'row'+(rowsLength-3);
		getElementByName(newRow, 'deleted').id = 'deleted'+id;
		getElementByName(newRow, 'addedrow').id = 'addedrow'+id;
		getElementByName(newRow, 'addedrow').value = 'Y';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
		newRow = getThisRow(currentRow, 'TR');
	}
	
	var tds = newRow.getElementsByTagName('td');
	
	tds[0].textContent = document.getElementById('itemgrouptypeId').
		options[document.getElementById('itemgrouptypeId').selectedIndex].text;

	getElementByName(newRow, 'item_group_type_id').value = document.getElementById('itemgrouptypeId').
		options[document.getElementById('itemgrouptypeId').selectedIndex].value;
	
	tds[1].textContent = document.getElementById('itemgroupId').
		options[document.getElementById('itemgroupId').selectedIndex].text;

	getElementByName(newRow, 'item_group_id').value = document.getElementById('itemgroupId').
		options[document.getElementById('itemgroupId').selectedIndex].value;

	tds[2].textContent = document.getElementById('itemsubgroupId').
			options[document.getElementById('itemsubgroupId').selectedIndex].text;

	getElementByName(newRow, 'item_subgroup_id').value = document.getElementById('itemsubgroupId').
			options[document.getElementById('itemsubgroupId').selectedIndex].value;

	newRowinserted = true;
	currentRow = null;
	removeClassName(newRow, 'editing');
	//itemSubGroupDialog.cancel();
	itemSubGroupDialog.show();
	document.getElementById("itemgrouptypeId").options.selectedIndex = 0;
	document.getElementById("itemgroupId").options.selectedIndex = 0;
	document.getElementById("itemsubgroupId").options.selectedIndex = 0;
	if(itemGroupTypeList.length == 1){
		document.getElementById('itemgrouptypeId').value = itemGroupTypeList[0].item_group_type_id;
		onSelectItemGroupType();
	}else{
		document.getElementById('itemgrouptypeId').value = '';
	}
}

function getItemSubGroupRows() {
	var table = document.getElementById("itemsubgroupTbl");
	return  (table.rows.length - 3);
}

function changeElsSubGroupDeleteColor(index, obj) {

	var row = document.getElementById("itemsubgroupTbl").rows[index];
	var trObj = getThisRow(obj);
	var tab = getThisTable(obj);
	var parts = trObj.id.split('row');
	var index = parseInt(parts[1])+1;

	var markRowForDelete = document.getElementById('deleted'+index).value == 'false' ? 'true' : 'false';
	document.getElementById('deleted'+index).value = document.getElementById('deleted'+index).value == 'false' ? 'true' :'false';

	if (markRowForDelete == 'true') {
		addClassName(trObj, 'delete');
   	}
   	else {
		removeClassName(trObj, 'delete');
   	}
}

</script>

	
<fieldset class="fieldSetBorder" style="width:726">
  			<legend class="fieldSetLabel" >Tax Sub Groups</legend>
  			<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="itemsubgroupTbl">
				<tr class="header">
					<th>Tax Group Type</th>
					<th>Tax Group</th>
					<th>Tax Sub Group</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
					<c:forEach items="${taxsubgroup}" var="itemSubGroup" varStatus="st">
					<tr id="row${st.index}">
						<input type="hidden" name="item_group_type_id" id="item_group_type_id" value="${itemSubGroup.item_group_type_id}" />
						<input type="hidden" name="item_group_type_name" id="item_group_type_name" value="${itemSubGroup.item_group_type_name}" />
						<input type="hidden" name="item_group_id" id="item_group_id" value="${itemSubGroup.item_group_id}" />
						<input type="hidden" name="item_group_name" id="item_group_name" value="${itemSubGroup.item_group_name}" />
						<input type="hidden" name="item_subgroup_id" id="item_subgroup_id" value="${itemSubGroup.item_subgroup_id}" />
						<input type="hidden" name="item_subgroup_name" id="item_subgroup_name" value="${itemSubGroup.item_subgroup_name}" />
						<input type="hidden" name="deleted" id="deleted${st.index+1}" value="false"/>
						<input type="hidden" name="addedrow" id="addedrow${st.index+1}" value="N"/>
						<td>${itemSubGroup.item_group_type_name}</td>
						<td>${itemSubGroup.item_group_name}</td>
						<td>${itemSubGroup.item_subgroup_name}</td>
						<td></td>
						<td style="text-align: center"><img src="${cpath}/icons/Delete.png" onclick="changeElsSubGroupDeleteColor(${st.index+1}, this);"/></td>
					</tr>
					<c:set var="newIndexFORdummyRow" value="${st.index+1}"/>
					</c:forEach>
					<tr id="" style="display: none">
						<input type="hidden" name="item_group_type_id" id="" value="" />
						<input type="hidden" name="item_group_type_name" id="" value="" />
						<input type="hidden" name="item_group_id" id="" value="" />
						<input type="hidden" name="item_group_name" id="" value="" />
						<input type="hidden" name="item_subgroup_id" id="" value="" />
						<input type="hidden" name="item_subgroup_name" id="" value="" />
						<input type="hidden" name="deleted" id="deleted0" value="false"/>
						<input type="hidden" name="addedrow" id="addedrow${st.index+1}" value="N"/>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td style="text-align: center"><img src="${cpath}/icons/Delete.png" onclick="changeElsSubGroupDeleteColor('${newIndexFORdummyRow}', this);"/></td>
					</tr>
					<tr>
						<td colspan="4"></td>
						<td style="text-align: right;">
							<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="showItemSubgroupDialog(this)" >
								<img src="${cpath}/icons/Add.png"/>
							</button>
						</td>
					</tr>
			</table>
</fieldset>	

<div id="itemSubgroupDIV" style="visibility: none;">
		<div class="bd">
			<fieldSet class="fieldSetBorder">
				<table class="formTable">
					<tr>
						<td>Tax Group Type:</td>
						<td>
							<select class="dropdown" name="itemgrouptypeId" id="itemgrouptypeId" onchange="onSelectItemGroupType();">
								 <option value="">-- Select --</option> 
								<c:forEach items="${itemGroupTypeList}" var="groupType">
									<option value="${groupType.item_group_type_id}">${groupType.item_group_type_name}</option>
								</c:forEach>
							</select>
						</td>
					</tr>
					<tr>
						<td>Tax Group:</td>
						<td>
							<select class="dropdown" name="itemgroupId" id="itemgroupId" onchange="onSelectItemGroupName();">
								<option value="">-- Select --</option>
							</select>
						</td>
					</tr>
					<tr>
						<td>Tax Sub Group:</td>
						<td>
							<select class="dropdown" name="itemsubgroupId" id="itemsubgroupId" onchange="">
								<option value="">-- Select --</option>
							</select>
						</td>
					</tr>
				</table>
			</fieldSet>
			<div>
				<input type="button" value="Add" onclick="addToTableItemSubGroup()"> |
				<input type="button" value="Cancel" onclick="handleItemTaxationCancel()">
			</div>
		</div>
</div>
