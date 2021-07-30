<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>

<head>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="usermanager/role.js"/>
	<script>

		function initSelVals(){
			var selElement = new Array();
			for (var i=1; i<=12; ++i){
				selElement[i] = document.getElementById("stats_item"+i);
			}

				for (var sel in selElement){
					var el = selElement[sel];
					var len = 1;
					for (var report in jsonReportsList) {
						var es = jsonReportsList[report];
						var selArr = el.id.split("stats_item");
						var index = parseInt(selArr[1]);
						el.length = len+1;
						el.options[len].value = es.report_id;
						el.options[len].text = es.display_name;

								switch(index){
									case 1:
											if(es.report_id == 'ACTIVE_OP_PATIENTS')
												el.options[len].selected = true;
											break;
									case 2:
											if(es.report_id == 'ACTIVE_IP_PATIENTS')
												el.options[len].selected = true;
											break;
									case 3:
											if(es.report_id == 'TODAYS_DISCHARGES')
												el.options[len].selected = true;
											break;
									case 4:
											if(es.report_id == 'TODAYS_ADMISSIONS')
												el.options[len].selected = true;
											break;
									case 5:
											if(es.report_id == 'TODAYS_OP_REGISTRATIONS')
												el.options[len].selected = true;
											break;
								}
							len ++;

					}
			}
			if(document.getElementById("newRole").value=='' || document.getElementById("newRole").value== null
					|| document.getElementById("newRole").value== 'undefined'){
				for (var sel in selElement){
					var el = selElement[sel]
					var selArr = el.id.split("stats_item");
					var index = parseInt(selArr[1]);
					for(var len=1; len<el.length; len++){
						if(document.getElementById('pstats_item'+index)
							&& document.getElementById('pstats_item'+index).value == ''){
							el.options[0].selected = true;
						} else if(document.getElementById('pstats_item'+index)
							&& document.getElementById('pstats_item'+index).value == el.options[len].value){
							el.options[len].selected = true;
						}
					}
				}
			}
		}

		function resetSelVals(){
			var selElement = new Array();
			for (var i=1; i<=12; ++i){
				selElement[i] = document.getElementById("stats_item"+i);
			}
			for (var sel in selElement){
				var el = selElement[sel];
				el.options[0].selected = true;
				var len = 1;
				for (var report in jsonReportsList) {
					var es = jsonReportsList[report];
					var selArr = el.id.split("stats_item");
					var index = parseInt(selArr[1]);
					el.length = len+1;
					el.options[len].value = es.report_id;
					el.options[len].text = es.display_name;

						switch(index){
							case 1:
									if(es.report_id == 'ACTIVE_OP_PATIENTS')
										el.options[len].selected = true;
									break;
							case 2:
									if(es.report_id == 'ACTIVE_IP_PATIENTS')
										el.options[len].selected = true;
									break;
							case 3:
									if(es.report_id == 'TODAYS_DISCHARGES')
										el.options[len].selected = true;
									break;
							case 4:
									if(es.report_id == 'TODAYS_ADMISSIONS')
										el.options[len].selected = true;
									break;
							case 5:
									if(es.report_id == 'TODAYS_OP_REGISTRATIONS')
										el.options[len].selected = true;
									break;
						}
					len ++;
				}
			}
		}
	</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="userList" value="${pagedList.dtoList}" />
<c:set var="results" value="${not empty userList}"/>

<body class="yui-skin-sam" onload="initSelVals();">
<div class="pageHeader"> Page Stats</div>
<insta:feedback-panel/>
<form>
		<input type="hidden" id="pstats_item1" value="${statsList.stats_item1}"/>
		<input type="hidden" id="pstats_item2" value="${statsList.stats_item2}"/>
		<input type="hidden" id="pstats_item3" value="${statsList.stats_item3}"/>
		<input type="hidden" id="pstats_item4" value="${statsList.stats_item4}"/>
		<input type="hidden" id="pstats_item5" value="${statsList.stats_item5}"/>
		<input type="hidden" id="pstats_item6" value="${statsList.stats_item6}"/>
		<input type="hidden" id="pstats_item7" value="${statsList.stats_item7}"/>
		<input type="hidden" id="pstats_item8" value="${statsList.stats_item8}"/>
		<input type="hidden" id="pstats_item9" value="${statsList.stats_item9}"/>
		<input type="hidden" id="pstats_item10" value="${statsList.stats_item10}"/>
		<input type="hidden" id="pstats_item11" value="${statsList.stats_item11}"/>
		<input type="hidden" id="pstats_item12" value="${statsList.stats_item12}"/>
		<input type="hidden" id="newRole" value="${ifn:cleanHtmlAttribute(param.newRole)}"/>
		<input type="hidden" name="roleName" value="${ifn:cleanHtmlAttribute(param.roleName)}"/>
		<input type="hidden" name="roleId" value="${ifn:cleanHtmlAttribute(param.roleId)}"/>
		<input type="hidden" name="method" value="updatePageStats"/>

		<table class="formtable" width="100%" align="left">
		<tr>
			<td>
				<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">
					Select the Page Stats to be displayed on Home Page for the role: <i>${ifn:cleanHtml(param.roleName)}</i>
				</legend>
				<br/>
				<table width="100%" cellspacing="0" cellpadding="0" align="left" class="formtable">
					<tr>
						<td align="right">Stats-1</td>
						<td>
							<select name="stats_item1"  id="stats_item1" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-5</td>
						<td>
							<select name="stats_item5" id="stats_item5" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-9&nbsp;</td>
						<td>
							<select name="stats_item9" id="stats_item9" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
					</tr>
					<tr>
						<td align="right">Stats-2</td>
						<td>
							<select name="stats_item2" id="stats_item2" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-6</td>
						<td>
							<select name="stats_item6" id="stats_item6" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-10</td>
						<td>
							<select name="stats_item10" id="stats_item10" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
					</tr>
					<tr>
						<td align="right">Stats-3</td>
						<td>
							<select name="stats_item3" id="stats_item3" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-7</td>
						<td>
							<select name="stats_item7" id="stats_item7" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-11</td>
						<td>
							<select name="stats_item11" id="stats_item11" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
					</tr>
					<tr>
						<td align="right">Stats-4</td>
						<td>
							<select name="stats_item4" id="stats_item4" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-8</td>
						<td>
							<select name="stats_item8" id="stats_item8" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
						<td align="right">Stats-12</td>
						<td>
							<select name="stats_item12" id="stats_item12" style="width:12em">
								<option value="">...Select...</option>
							</select>
						</td>
					</tr>
					<tr>
						<td>
							<br />
						</td>
					</tr>
				</table>
			</fieldset>
		</td>
	</tr>
	<tr>
		<td>
			<input type="submit" value="Save" onclick=""/>
			|<a href="#" onclick="resetSelVals()"> Reset</a>
			<c:if test="${param.newRole eq null}">
				|<a href="${cpath}/pages/usermanager/UserDashBoard.do?_method=list">User Dashboard</a>
				|<a href="${cpath}/pages/usermanager/RoleAction.do?method=getRoleScreen&roleId=${ifn:cleanURL(param.roleId)}&roleName=${ifn:cleanURL(param.roleName)}&userName=">Edit Role</a>
			</c:if>
		</td>
	</tr>
</table>
</form>
<script>
		var jsonReportsList = ${jsonReportsList};
</script>
</body>
</html>
