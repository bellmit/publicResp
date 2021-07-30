<%@page import="com.insta.hms.master.URLRoute" %>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagepath" value="<%=URLRoute.FORM_COMPONENTS_MASTER_PATH %>"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Hospital Forms List - Insta HMS</title>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<script>
		var enableModDevelop = ${preferences.modulesActivatedMap['mod_newcons'] eq 'Y'};
		var formCenterapplicable = ( ${(prefs > 1)} && enableModDevelop );
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagepath}/show.htm?',
				description: "Edit Component Details"
			},
			CenterApplicability : {
				title: "CenterApplicability",
				imageSrc: "icons/Edit.png",
				href: '${pagepath}/showCenter.htm?',
				description: "CenterApplicability",
				show : formCenterapplicable
			}
		}
		function init() {
			createToolbar(toolbar, 'Form_CONS');
			createToolbar(toolbar, 'Form_OP_FOLLOW_UP_CONS');
			createToolbar(toolbar, 'Form_IP');
			createToolbar(toolbar, 'Form_Serv');
			createToolbar(toolbar, 'Form_IA');
			createToolbar(toolbar, 'Form_OT');
			createToolbar(toolbar, 'Form_TRI');
			createToolbar(toolbar, 'Form_Gen');
		}
		function addForm(e) {
			if (e.button == 2 || e.button == 3) {
				YAHOO.util.Event.stopEvent(e);
				alert('sorry, right click is not allowed');
				return false
			}
			var form_type = document.getElementById('form_type').value;
			if (form_type == '') {
				alert('please select the form type');
				document.getElementById('form_type').focus();
				return false;
			} else {
				href = "form_type="+form_type;
				document.getElementById("add").href = document.getElementById("addUrl").value + href;
			}
		}
	</script>
</head>
<body onload="init()">
	<h1>Hospital Forms</h1>
	<div id="CollapsiblePanel1" class="CollapsiblePanel" style="margin-top: 10px">
	<c:set var="opFollowupFormRights" value="${preferences.modulesActivatedMap['mod_newcons'] eq 'Y'}"/>
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">OP Forms <label id="opCount"></label></div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="op_resultTable">
				<tr>
					<th>#</th>
					<th>Form Name</th>
					<th>Template</th>
					<th>Doctor</th>
					<th>Dept. Name</th>
					<th>Patient Sections Group</th>
				</tr>
				<c:set var="index" value="0"/>
				<c:forEach items="${componentsList}" var="bean" varStatus="st">
					<c:if test="${bean.form_type == 'Form_CONS'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'op_resultTable',
									{id:'${bean.id}', form_type: '${bean.form_type}' },[true,true],'Form_CONS');" id="toolbarRow${st.index}">
							<td>
								${index + 1}
								<c:set var="index" value="${index+1}"/>
							</td>
							<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
							<td>${bean.istemplate ? 'Yes' : 'No'}</td>
							<td>${bean.doctor_id == '-1' ? 'All' : bean.doctor_name}</td>
							<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
							<td>${bean.group_patient_sections == 'Y' ? 'Yes' : 'No'}</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
			<script>
				document.getElementById('opCount').textContent = '('+ ${index} +')'
			</script>
		</div>
	</div>
	<c:if test="${opFollowupFormRights}">
		<div id="CollapsiblePanel8" class="CollapsiblePanel" style="margin-top: 10px">
			<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
				<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">OP Follow Up Forms <label id=opFollowCount></label></div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
					<img src="${cpath}/images/down.png" />
				</div>
				<div class="clrboth"></div>
			</div>
			<div class="resultList" >
				<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="op_followup_cons_resultTable">
					<tr>
						<th>#</th>
						<th>Form Name</th>
						<th>Template</th>
						<th>Doctor</th>
						<th>Dept. Name</th>
						<th>Patient Sections Group</th>
					</tr>
					<c:set var="index" value="0"/>
					<c:forEach items="${componentsList}" var="bean" varStatus="st">
						<c:if test="${bean.form_type == 'Form_OP_FOLLOW_UP_CONS'}">
							<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
									onclick="showToolbar(${st.index}, event, 'op_followup_cons_resultTable',
										{id:'${bean.id}', form_type: '${bean.form_type}' },[true,true],'Form_OP_FOLLOW_UP_CONS');" id="toolbarRow${st.index}">
								<td>
									${index + 1}
									<c:set var="index" value="${index+1}"/>
								</td>
								<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
								<td>${bean.istemplate ? 'Yes' : 'No'}</td>
								<td>${bean.doctor_id == '-1' ? 'All' : bean.doctor_name}</td>
								<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
								<td>${bean.group_patient_sections == 'Y' ? 'Yes' : 'No'}</td>
							</tr>
						</c:if>
					</c:forEach>
				</table>
				<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
				<script>
					document.getElementById('opFollowCount').textContent = '('+ ${index} +')'
				</script>
			</div>
		</div>
	</c:if>
	<div id="CollapsiblePanel2" class="CollapsiblePanel" style="margin-top: 10px">
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Triage Forms <label id="tfCount"></label></div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="tr_resultTable">
				<tr>
					<th width="10%">#</th>
					<th width="35%">Form Name</th>
					<th width="10%">Template</th>
					<th width="35%">Dept. Name</th>
					<th width="10%">Patient Sections Group</th>
				</tr>
				<c:set var="index" value="0"/>
				<c:forEach items="${componentsList}" var="bean" varStatus="st">
					<c:if test="${bean.form_type == 'Form_TRI'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'tr_resultTable',
									{id:'${bean.id}', form_type: '${bean.form_type}' }, [true,false], 'Form_TRI');" id="toolbarRow${st.index}">
							<td>
								${index + 1}
								<c:set var="index" value="${index+1}"/>
							</td>
							<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
							<td>${bean.istemplate ? 'Yes' : 'No'}</td>
							<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
							<td>${bean.group_patient_sections == 'Y' ? 'Yes' : 'No'}</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
			<script>
				document.getElementById('tfCount').textContent = '('+ ${index} +')'
			</script>
		</div>
	</div>

	<div id="CollapsiblePanel3" class="CollapsiblePanel" style="margin-top: 10px">
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Assessment Forms <label id="afCount"></label></div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="ia_resultTable">
				<tr>
					<th width="10%">#</th>
					<th width="40%">Form Name</th>
					<th width="40%">Dept. Name</th>
					<th width="10%">Patient Sections Group</th>
				</tr>
				<c:set var="index" value="0"/>
				<c:forEach items="${componentsList}" var="bean" varStatus="st">
					<c:if test="${bean.form_type == 'Form_IA'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'ia_resultTable',
									{id:'${bean.id}', form_type: '${bean.form_type}' }, [true,false], 'Form_IA');" id="toolbarRow${st.index}">
							<td>
								${index + 1}
								<c:set var="index" value="${index+1}"/>
							</td>
							<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
							<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
							<td>${bean.group_patient_sections == 'Y' ? 'Yes' : 'No'}</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
			<script>
				document.getElementById('afCount').textContent = '('+ ${index} +')'
			</script>
		</div>
	</div>

	<div id="CollapsiblePanel4" class="CollapsiblePanel" style="margin-top: 10px">
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">IP Forms <label id="ipCount"></label></div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="i_resultTable">
				<tr>
					<th width="10%">#</th>
					<th width="40%">Form Name</th>
					<th>Template</th>
					<th width="40%">Dept. Name</th>
					<th width="10%">Patient Sections Group</th>
				</tr>
				<c:set var="index" value="0"/>
				<c:forEach items="${componentsList}" var="bean" varStatus="st">
					<c:if test="${bean.form_type == 'Form_IP'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'i_resultTable',
									{id:'${bean.id}', form_type: '${bean.form_type}' }, [true,false], 'Form_IP');" id="toolbarRow${st.index}">
							<td>
								${index + 1}
								<c:set var="index" value="${index+1}"/>
							</td>
							<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
							<td>${bean.istemplate ? 'Yes' : 'No'}</td>
							<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
							<td>${bean.group_patient_sections == 'Y' ? 'Yes' : 'No'}</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
			<script>
				document.getElementById('ipCount').textContent = '('+ ${index} +')'
			</script>
		</div>
	</div>

	<div id="CollapsiblePanel5" class="CollapsiblePanel" style="margin-top: 10px">
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Generic Forms <label id="genCount"></label></div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="sc_resultTable">
				<tr>
					<th width="10%">#</th>
					<th width="30%">Form Name</th>
					<th width="40%">Dept. Name</th>
					<th width="10%">Patient Sections Group</th>
					<th width="10%">Status</th>
				</tr>
				<c:set var="index" value="0"/>
				<c:forEach items="${componentsList}" var="bean" varStatus="st">
					<c:if test="${bean.form_type == 'Form_Gen'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'sc_resultTable',
									{id:'${bean.id}', form_type: '${bean.form_type}' }, [true,false], 'Form_Gen');" id="toolbarRow${st.index}">
							<td>
								${index + 1}
								<c:set var="index" value="${index+1}"/>
							</td>
							<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
							<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
							<td>${bean.group_patient_sections == 'Y' ? 'Yes' : 'No'}</td>
							<td>${bean.status == 'A' ? 'Active' : 'InActive' }</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
			<script>
				document.getElementById('genCount').textContent = '('+ ${index} +')'
			</script>
		</div>
	</div>

	<div id="CollapsiblePanel6" class="CollapsiblePanel" style="margin-top: 10px">
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Surgery Forms <label id="surCount"></label></div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="s_resultTable">
				<tr>
					<th>#</th>
					<th>Form Name</th>
					<th>Surgery</th>
					<th>Dept. Name</th>
				</tr>
				<c:set var="index" value="0"/>
				<c:forEach items="${componentsList}" var="bean" varStatus="st">
					<c:if test="${bean.form_type == 'Form_OT'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 's_resultTable',
									{id:'${bean.id}', form_type: '${bean.form_type}' }, [true,false], 'Form_OT');" id="toolbarRow${st.index}">
							<td>
								${index + 1}
								<c:set var="index" value="${index+1}"/>
							</td>
							<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
							<td><insta:truncLabel length="50" value="${bean.operation_id == '-1' ? 'All' : bean.operation_name}"/></td>
							<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
			<script>
				document.getElementById('surCount').textContent = '('+ ${index} +')'
			</script>
		</div>
	</div>

	<div id="CollapsiblePanel7" class="CollapsiblePanel" style="margin-top: 10px">
		<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Service Forms <label id="serCount"></label></div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<div class="resultList" >
			<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="sc_resultTable">
				<tr>
					<th>#</th>
					<th>Form Name</th>
					<th>Service</th>
					<th>Dept. Name</th>
					<th>Patient Sections Group</th>
				</tr>
				<c:set var="index" value="0"/>
				<c:forEach items="${componentsList}" var="bean" varStatus="st">
					<c:if test="${bean.form_type == 'Form_Serv'}">
						<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
								onclick="showToolbar(${st.index}, event, 'sc_resultTable',
									{id:'${bean.id}', form_type: '${bean.form_type}' }, [true,false],'Form_Serv');" id="toolbarRow${st.index}">
							<td>
								${index + 1}
								<c:set var="index" value="${index+1}"/>
							</td>
							<td><insta:truncLabel value="${bean.form_name}" length="50"/></td>
							<td><insta:truncLabel length="50" value="${bean.service_id == '-1' ? 'All' : bean.service_name}"/></td>
							<td>${bean.dept_id == '-1' ? 'All' : bean.dept_name}</td>
							<td>${bean.group_patient_sections == 'Y' ? 'Yes' : 'No'}</td>
						</tr>
					</c:if>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${not empty componentsList}" message="No Components Found."/>
			<script>
				document.getElementById('serCount').textContent = '('+ ${index} +')'
			</script>
		</div>
	</div>
	
	<script>
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:${false}});
		var CollapsiblePanel2 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel2", {contentIsOpen:${false}});
		var CollapsiblePanel3 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel3", {contentIsOpen:${false}});
		var CollapsiblePanel4 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel4", {contentIsOpen:${false}});
		var CollapsiblePanel5 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel5", {contentIsOpen:${false}});
		var CollapsiblePanel6 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel6", {contentIsOpen:${false}});
		var CollapsiblePanel7 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel7", {contentIsOpen:${false}});
		if(enableModDevelop)
			var CollapsiblePanel8 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel8", {contentIsOpen:${false}});
	</script>

	<table style="margin-top: 10px;float: left">
		<tr>
			<td>Form Type:&nbsp;</td>
			<td>
				<select name="form_type" id="form_type" class="dropdown">
					<option value="">-- Select --</option>
					<option value="Form_CONS">OP Form</option>
					<c:if test="${opFollowupFormRights}">
						<option value="Form_OP_FOLLOW_UP_CONS">OP Follow Up Form</option>
					</c:if>
					<option value="Form_TRI">Triage</option>
					<option value="Form_IA">Assessment Form</option>
					<option value="Form_IP">IP Form</option>
					<option value="Form_Serv">Service Form</option>
					<option value="Form_Gen">Generic Form</option>
					<c:if test="${preferences.modulesActivatedMap['mod_advanced_ot'] eq 'Y'}">
						<option value="Form_OT">Surgery Form</option>
					</c:if>
				</select>
				<input type="hidden" id="addUrl" name="addUrl" value="${cpath}/${pagepath}/add.htm?">
				<a id="add" href="" onmousedown="return addForm(event);">Add</a>
			</td>
		</tr>
	</table>
</body>
</html>