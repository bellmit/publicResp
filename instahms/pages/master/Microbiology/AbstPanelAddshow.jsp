<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit ABST Panel - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="/masters/MicroABSTPanelMaster.js" />

<script>
	var organismList = <%= request.getAttribute("abstPanelNamesAndIds") %>;
	var organismId = '${bean.map.abst_panel_id}';
	var organismName = '${bean.map.abst_panel_name}';
	var backupName = '';

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].abst_panel_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/MicroAbstPanel.do?_method=list&sortOrder=abst_panel_name&sortReverse=false&status=A";
	}

	function checkduplicate(){
			var newOrganismName = trimAll(document.abstPanelForm.abst_panel_name.value);
			for(var i=0;i<organismList.length;i++){
				item = organismList[i];
				if(organismId!=item.abst_panel_id){
				   var actualOrganismName = item.abst_panel_name;
				    if (newOrganismName.toLowerCase() == actualOrganismName.toLowerCase()) {
				    	alert(document.abstPanelForm.abst_panel_name.value+" already exists pls enter other name");
				    	document.abstPanelForm.abst_panel_name.value=organismName;
				    	document.abstPanelForm.abst_panel_name.focus();
				    	return false;
				    }
			     }
			}

			document.abstPanelForm.abst_panel_name.value = trim(document.abstPanelForm.abst_panel_name.value);

			if (document.abstPanelForm.abst_panel_name.value == '') {
				alert('Abst Panel name is required');
				document.abstPanelForm.abst_panel_name.focus();
				return false;
			}

			if (document.abstPanelForm.org_group_id.value == '') {
				alert('Organism Group is required');
				document.abstPanelForm.org_group_id.focus();
				return false;
			}

			if (document.abstPanelForm.antibiotic_id.value == '') {
				alert('Antibiotic Name is required');
				document.abstPanelForm.antibiotic_id.focus();
				return false;
			}

      }

      <c:if test="${param._method != 'add'}">
  	  	Insta.masterData=${abstPanelNamesAndIds};
 	 </c:if>

</script>

</head>
<body onload="keepBackUp(); initDialogs()">

	<c:choose>
	     <c:when test="${param._method != 'add'}">
	        <h1 style="float:left">Edit ABST Panel</h1>
		    <c:url var="searchUrl" value="/master/MicroAbstPanel.do"/>
		    <insta:findbykey keys="abst_panel_name,abst_panel_id" method="show" fieldName="abst_panel_id" url="${searchUrl}" />
	     </c:when>
	     <c:otherwise>
	        <h1>Add ABST Panel</h1>
	     </c:otherwise>
	</c:choose>

	<form action="MicroAbstPanel.do" method="POST" name="abstPanelForm">
		<input type="hidden" name="_method"	value="${param._method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="abst_panel_id" value="${bean.map.abst_panel_id}" />

		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">ABST panel name:</td>
				<td><input type="text" name="abst_panel_name"
					value="${bean.map.abst_panel_name}" length="200"
					style="border-style: " /></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}"
					opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
			</tr>
		</table>

		<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="grpTable" border="0" style="width: 550px">
			<tr>
				<th>Organism Group</th>
				<th>Delete</th>
			</tr>
			<c:set var="grpLength" value="${fn:length(grpList)}" />
				<c:forEach begin="1" end="${grpLength+1}" var="i">
					<c:set var="bean" value="${grpList[i-1].map}"/>
					<c:if test="${empty bean}">
						<c:set var="style" value="style='display: none'" />
					</c:if>
					<tr ${style}>
						<td>
						<label>${bean.org_group_name}</label></td>
							<input type="hidden" name="micro_abst_orggr_id" value="${bean.micro_abst_orggr_id}"/>
							<input type="hidden" name="org_group_id" id="org_group_id" value="${bean.org_group_id}"/>
							<input type="hidden" name="org_deleted" id="org_deleted" value="false"/>

						<td>
							<a name="trashCanAnchor" href="javascript:Cancel Item" onclick="return cancelSIItem(this);" title="Cancel Item" >
								<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
							</a>
						</td>
					</tr>
				</c:forEach>
		</table>
		<table class="addButton" style="width: 550px">
			<tr>
				<td></td>
				<td width="16px" style="text-align: center">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add Org Groups (Alt_Shift_O)"
						onclick="showaddGrpdialog(this); return false;"
						accesskey="O" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
		</table>
		<table>
			<tr><td>&nbsp;</td></tr>
		</table>

		<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="antTable" border="0" style="width: 550px">
			<tr>
				<th>Antibiotic Name</th>
				<th>Delete</th>
			</tr>
			<c:set var="antibioticLength" value="${fn:length(antibioticList)}" />
				<c:forEach begin="1" end="${antibioticLength+1}" var="i">
					<c:set var="beanAnt" value="${antibioticList[i-1].map}"/>
					<c:set var="style" value="" />
					<c:if test="${empty beanAnt}">
						<c:set var="style" value="style='display: none'" />
					</c:if>
					<tr ${style}>
						<td>
							<label>${beanAnt.antibiotic_name}</label>
							<input type="hidden" name="antibiotic_id" value="${beanAnt.antibiotic_id}"/>
							<input type="hidden" name="antibiotic_id_check" value="${beanAnt.antibiotic_id}"/>
							<input type="hidden" name="ant_deleted" id="ant_deleted" value="false"/>
						</td>
						<td>
							<a name="trashCanAnchor" href="javascript:Cancel Item" onclick="return cancelAntibioticItem(this);" title="Cancel Item" >
								<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
							</a>
						</td>
					</tr>
				</c:forEach>
		</table>
		<table class="addButton" style="width: 550px">
			<tr>
				<td></td>
				<td width="16px" style="text-align: center">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add Antibiotics (Alt_Shift_O)"
						onclick="showaddAntibioticdialog(this); return false;"
						accesskey="O" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
		</table>

		</fieldset>

		<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a
				href="MicroAbstPanel.do?_method=add">Add</a>
		</c:if> | <a href="javascript:void(0)" onclick="doClose();">ABST Panels List</a></div>


	<div id="addGrpdialog" style="display: none">
	<div class="bd">
		<div id="addGrpDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Organism Group</legend>
				<table class="formtable">
					<tr>
						<td><insta:selectdb name="org_group_name" id="org_group_name" table="micro_org_group_master"
									valuecol="org_group_id" displaycol="org_group_name" dummyvalue="---Select---" dummyvalueId=""/></td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="GrpAdd" id="GrpAdd" >Add</button>
					<input type="button" name="GrpClose" value="Close" id="GrpClose"/>
				</td>
			</tr>
		</table>
	</div>
	</div>

	<div id="addAntibioticdialog" style="display: none">
	<div class="bd">
		<div id="addAntibioticDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Antibiotics</legend>
				<table class="formtable">
					<tr>
						<td><insta:selectdb name="antibiotic_name" id="antibiotic_name" table="micro_antibiotic_master"
									valuecol="antibiotic_id" displaycol="antibiotic_name" dummyvalue="---Select---" dummyvalueId=""/></td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="AntAdd" id="AntAdd" >Add</button>
					<input type="button" name="AntClose" value="Close" id="AntClose"/>
				</td>
			</tr>
		</table>
	</div>
	</div>

<%-- -- unused code is commented out
	<div id="editGrpdialog" style="display: none">
	<input type="hidden" name="grp_editRowId" id="s_ed_editRowId" value=""/>
	<div class="bd">
		<div id="addSIDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Oganism Group</legend>
				<table class="formtable">
					<tr>
						<td><insta:selectdb name="ed_org_group_name" id="ed_org_group_name" table="micro_org_group_master"
									valuecol="org_group_id" displaycol="org_group_name" dummyvalue="---Select---" dummyvalueId=""/></td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<input type="button" id="siOk" name="siok" value="Ok"/>
					<input type="button" id="siEditCancel" name="sicancel" value="Cancel" />
					<input type="button" id="siEditPrevious" name="siprevious" value="<<Previous" />
					<input type="button" id="siEditNext" name="sinext" value="Next>>" />
				</td>
			</tr>
		</table>
	</div>
	</div>  --%>
	</form>

</body>
</html>
