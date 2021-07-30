<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>${param.method == 'add' ? 'Add' : 'Edit'} Fixed Asset Details - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

	<script>
		var JLocationMaster = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.LOCATION_MASTER) %>;
	    var JParentAssets = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_PARENT_ASSETS) %>;
	    var oAutoComp5;
        var oAutoComp6;
		var hiddenAssetId = '${bean.map.asset_id}';
		var backupName = '';
		var chkAssetName = <%= request.getAttribute("assetNames") %>
		var filelen = ${filelen};
		function init () {
			initLocationAutoComplete ();
			initParentAssetAutoComplete ();
		}

		function doClose() {
			window.location.href = "${cpath}/master/FixedAssetMaster.do?method=list";
		}


	      function viewDoc(id){
	      	var url= "${cpath}/master/FixedAssetMaster.do?method=getUplodedDocs&asset_file_seq="+id;
		    window.open(url);
		  }

		  function deleteDoc(id){
			if(!confirm("you want to delete uploded Doc ?")) return false;
			else{
				document.forms[0].action = "FixedAssetMaster.do?method=deleteUplodedDoc&asset_file_seq="+id+
	      			"&asset_seq="+${bean.map.asset_seq};
				document.forms[0].submit();
				return true;
				}
		  }
function initLocationAutoComplete() {
	if (oAutoComp5 != undefined) {
		oAutoComp5.destroy();
	}
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(JLocationMaster);
		oAutoComp5 = new YAHOO.widget.AutoComplete('asset_location', 'loc_dropdown', dataSource);
		oAutoComp5.maxResultsDisplayed = 20;
		oAutoComp5.allowBrowserAutocomplete = false;
		oAutoComp5.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp5.typeAhead = false;
		oAutoComp5.useShadow = false;
		oAutoComp5.minQueryLength = 0;
		oAutoComp5.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get('asset_location').value;
		if(sInputValue.length == 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		}
	});
	}

}

function initParentAssetAutoComplete() {
	if (oAutoComp6 != undefined) {
		oAutoComp6.destroy();
	}
	parentasset  = new Array();
	var j=0;
	for (var i=0;i<JParentAssets.length;i++) {
		if (JParentAssets[i] != '${bean.map.medicine_name}') parentasset[j++] = JParentAssets[i];
	}
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(parentasset);
		oAutoComp6 = new YAHOO.widget.AutoComplete('parent_asset_id', 'asset_dropdown', dataSource);
		oAutoComp6.maxResultsDisplayed = 20;
		oAutoComp6.allowBrowserAutocomplete = false;
		oAutoComp6.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp6.typeAhead = false;
		oAutoComp6.useShadow = false;
		oAutoComp6.forceSelection = true;
		oAutoComp6.minQueryLength = 0;
		oAutoComp6.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get('parent_asset_id').value;
		if(sInputValue.length == 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		}
	});
	}

}
	function onaddUpload (obj) {
		var table = document.getElementById("recordsTable");

        var rowCount = table.rows.length;
        var row = table.insertRow(rowCount);

        var cell1 = row.insertCell(0);
        cell1.innerHTML = "";

        var cell2 = row.insertCell(1);
        var element2 = document.createElement("input");
        element2.type = "file";
        element2.name = "file_upload";

        var element3 = document.createElement("input");
        element3.type = "hidden";
        element3.value = "new";
        element3.name = "record";
        cell2.appendChild(element2);
        cell2.appendChild(element3);
	}
	function ondeleteUpload(obj) {
        var table = document.getElementById("recordsTable");
        var rowCount = table.rows.length;
        if ((rowCount-filelen) > 1) table.deleteRow(rowCount-1);
    }
	function getThisRow(node) {
		return findAncestor(node, "TR");
	}

	function validate(){
		document.forms[0].submit();
	}

	</script>

</head>

<body onload="init();">

<form action="FixedAssetMaster.do?method=${param.method == 'add' ? 'create' : 'update'}" method="POST" enctype="multipart/form-data">
	<input type="hidden" name="method" value="${param.method == 'add' ? 'create' : 'update'}">
	<c:if test="${param.method == 'show'}">
		<input type="hidden" name="asset_id" value="${bean.map.asset_id}"/>
		<input type="hidden" name="asset_seq" value="${bean.map.asset_seq}"/>
	</c:if>

	<div class="pageHeader">${param.method == 'add' ? 'Add' : 'Edit'} Fixed Asset Details</div>
	<span align="center" class="error">${ifn:cleanHtml(error)}</span>

	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Asset</legend>
	<table class="formtable" width="100%">
		<tr>
			<td class="formlabel">Name:</td>
			<td class="forminfo">${bean.map.medicine_name}</td>

			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="asset_status" value="${bean.map.asset_status}" opvalues="A,I,R" optexts="Active,Inactive,Retired"/>
			</td>
			<td class="formlabel">Serial No:</td>
			<td class="forminfo"><input type="hidden" name="asset_serial_no" value="${bean.map.asset_serial_no}"/>${bean.map.asset_serial_no}</td>
			

		</tr>

		<tr>
			<td class="formlabel">Make:</td>
			<td>
				<input type="text" name="asset_make" value="${bean.map.asset_make}" onblur="capWords(asset_make);"/>
			</td>

			<td class="formlabel">Model:</td>
			<td>
				<input type="text" name="asset_model" value="${bean.map.asset_model}" onblur="capWords(asset_model);" />
			</td>
            <td class="formlabel">Installation Date:</td>
			<td>
				<insta:datewidget name="installation_date" id="installation_date" valueDate="${bean.map.installation_date}" calButton="true"/>
			</td>

		</tr>
		<tr>
			<td class="formlabel">Category:</td>
			<td class="forminfo">${bean.map.category }</td>

			<td class="formlabel">Purchase Value:</td>
			<td>
				<input type="text" name="asset_purchase_val" value="${bean.map.asset_purchase_val}" onkeypress="return enterNumOnly(event);"  />
			</td>
            <td class="formlabel">Bill No:</td>
			<td>
				<input type="text" name="asset_bill_no" value="${bean.map.asset_bill_no }"  />
			</td>
			
		</tr>
		<tr>
			<td class="formlabel">Parent Asset:</td>
			<td valign="top">
				<div id="asset_wrapper" style="width: 15em; padding-bottom:0.2em;">
			 		<input type="text" name="parent_asset_id" id="parent_asset_id" style="width: 15em" value="${bean.map.parent_asset }" />
			 	<div id="asset_dropdown" class="scrolForContainer"></div></div>
			</td>

			<td class="formlabel">Location:</td>
			<td valign="top">
				<div id="loc_wrapper" style="width: 15em; padding-bottom:0.2em;">
			 		<input type="text" name="asset_location" id="asset_location" style="width: 15em"  value="${bean.map.location_name }" />
			 	<div id="loc_dropdown" class="scrolForContainer"></div></div>
			</td>

		</tr>
		<tr>
			<td class="formlabel">Store:</td>
			<td class="forminfo"><input type="hidden" name="asset_dept" value="${bean.map.asset_dept}"/>${bean.map.dept_name }</td>

			<td class="formlabel">Remarks:</td>
			<td colspan="3"><textarea rows="2" cols="60" name="asset_remarks">${bean.map.asset_remarks }</textarea></td>
		</tr>
		</table>
		</fieldset>
		<fieldset class="fieldSetBorder">
	    <legend class="fieldSetLabel">Files List</legend>
			<table class="datatable" width="65%" id="recordsTable">
			<tr>
				<td>File Name</td>
				<td>upload</td>
			</tr>
			<c:forEach var="record" items="${fileseq}" varStatus="st">
			<tr>
				<td class="forminfo">${record.ASSET_FILE_NAME }</td>
				<td>
					<button type="button" name="viewdocform"  accesskey="V" value=""
						onclick="viewDoc(${record.ASSET_FILE_SEQ});">View</button>
					<button type="button" name="deldocform" accesskey="D" value=""
						onclick="return deleteDoc(${record.ASSET_FILE_SEQ});">Delete</button>
					<input type="hidden" name="record" value="old">
			   </td>
			  </tr>
			</c:forEach>
			</table>
			</fieldset></br>
		<button type="button" onclick="return onaddUpload(this);">Add Upload</button>
		<button type="button" onclick="return ondeleteUpload(this);" title="Delete only newly added files">Delete Upload</button>
        <div class="screenActions">
				<input type="button" value="Save" onclick="validate();" />&nbsp;|
				<a href="#" onclick="doClose();return true;">  Close</a>
		</div>
</fieldset>
</form>
</body>
</html>
