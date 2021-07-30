<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page contentType="text/html" import="java.util.*" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.common.Encoder" %>
<%--
TODO:
 * Print No Data if result set is empty
--%>
<head>
<title>${reportDesc.title} Builder - Insta HMS</title>

	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="reports/std_report_builder.js" />
	<style>
		a.picture, a.picture:hover {
			text-decoration: none; background: #ffffff;
		}
		a.picture img.small {
			width:220px; height:150px;
			border:2px solid  #CCCCCC;
			display: block;
		}
		a.picture img.large {
			position: absolute;
			width: 450px;
			top: -85;
			left: -185px;
			border:2px solid  #CCCCCC;
			display: none;
		}
		a.picture:hover img.small { display: none; }
		a.picture:hover img.large { display: block; }

#myDialog_mask.mask {
		    z-index: 1;
		    display: none;
		    position: absolute;
		    top: 0;
		    left: 0;
		    -moz-opacity: 0.0001;
		    opacity: 0.0001;
		    filter: alpha(opacity=50);
		    background-color: #CCC;
	}
</style>

</head>
<%@page import="com.insta.hms.common.FavouriteReportDAO"%>
<html>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<c:set var="windowurl" value="${pageContext.request.contextPath}" />

	<body onload="onInit();showFiltActive('${ifn:cleanJavaScript(param._savedfavourite)}');" class="yui-skin-sam">
	<form name="inputform" method="GET" target="_blank">
		<div id="content"> <h1>${reportDesc.title} Builder</h1>
		<insta:feedback-panel />
		<div style="margin-bottom:10px; padding:10px 0 10px 10px; text-align:left; valign:center; border:1px #CCCCCC solid;" class="brB brT brL brR" id="msgDiv">
				${reportDesc.description}
		</div>
			<input type="hidden" name="screenId" value="${screenId}" />
			<input type="hidden" name="reportName" value="${reportDesc.title}" />
			<input type="hidden" id="ctpath" value="${cpath}" />
			<input type="hidden" name="method" value="getReport">
			<input type="hidden" name="outputMode" value="pdf">
			<input type="hidden" id="filterFieldNamez" value="${reportDesc.filterFieldNames}"/>
			<input type="hidden" name="_searchMethod" value="getScreen"/>
			<input type="hidden" name="_parent_report_name" value="${reportDesc.title} Builder"/>
			<input type="hidden" name="selDateRange" id="selDateRange" value="td"/>
			<input type="hidden" id="srjsFile" name="srjsFile" value="<%= request.getAttribute("srjsFile")==null?request.getParameter("srjsFile"):(String)request.getAttribute("srjsFile") %>"/>
			<input type="hidden" id="reptDescFile" name="reptDescFile" value="<%= Encoder.cleanHtmlAttribute((String)request.getAttribute("reptDescFile"))==null? Encoder.cleanHtmlAttribute(request.getParameter("reptDescFile")):Encoder.cleanHtmlAttribute((String)request.getAttribute("reptDescFile")) %>"/>
			<input type="hidden" id="reptDescProvider" name="reptDescProvider" value="<%=request.getAttribute("reptDescProvider")==null? Encoder.cleanHtmlAttribute(request.getParameter("reptDescProvider")):Encoder.cleanHtmlAttribute((String)request.getAttribute("reptDescProvider")) %>"/>
			<input type="hidden" id="isCustom" name="isCustom" value="<%= request.getAttribute("isCustom")!=null? Encoder.cleanHtmlAttribute(String.valueOf(request.getAttribute("isCustom"))):false %>"/>
			<input type="hidden" name="reportGroup" value="${reportDesc.reportGroup eq null? 'Misc. Reports' : reportDesc.reportGroup}"/>
			<input type="hidden" name="current_user" id="current_user" value="<%= Encoder.cleanHtmlAttribute((String) session.getAttribute("userid")) %>"/>
			<%
				String userid = (String) session.getAttribute("userid");
				String srxFl=  request.getAttribute("srjsFile")==null?request.getParameter("srjsFile"):(String)request.getAttribute("srjsFile");
				if(srxFl==null || srxFl.equals("") ||  srxFl.equals("null"))
					request.setAttribute("myfavourites",
						com.insta.hms.common.FavouriteReportDAO.getMyFavourites((String) request.getAttribute("actionId")) );
				else
					request.setAttribute("myfavourites",
						com.insta.hms.common.FavouriteReportDAO.getMyCustomFavourites((String) request.getAttribute("actionId"), srxFl));
			%>
			<div style="margin-bottom:10px; padding:10px 5px 10px 5px; text-align:left; valign:center;border:1px #CCCCCC solid;" class="brB brT brL brR" id="msgDiv">
				<table>
					<tr>
						<td width="200px">
							Title: <input type="text" name="print_title" id="print_title" value="${reportDesc.title}"/>
						</td>
						<td width="661px">
							<div id="_filters_active"><img width="16" height="16" src="<%=request.getContextPath()%>/images/arrow_down.png">
							<insta:truncLabel value="${param._savedfavourite}" length="30"/></div>
						</td>
						<td align="right">
							<select size="1" id="_myreport" name="_myreport" onchange="onReportChange(this.value,
										document.forms.inputform)" style="width:auto;" class="dropdown">
										<option value="nosearch">MyFavourites</option>
										<c:if test="${myfavourites ne null}">
										<c:forEach var="report" items="${myfavourites}">
											<option value="${report.map.report_id}">${report.map.report_title}</option>
										</c:forEach>
										</c:if>
							</select>
						</td>
					</tr>
				</table>
			</div>
			<table>
				<tr>
					<td rowspan="2" width="951" style="background-color:#f8fbfe;border:1px #CCCCCC solid;">
						<div id="optionalFilter" style="clear: both; display:'block';" >
						<table class="searchFormTable" style="border-top:medium none;">
							<tr>
								<td>
									<div class="sfLabel">Report Type</div>
									<div class="sfField">
										<input type="radio" name="reportType" id="reportType" value="list" id="list" onchange="onChangeReportType();changeListImg('${cpath}');"
										checked>
										<label for="list">Detailed List</label>
										<br/>
										<input type="radio" name="reportType" value="sum" id="sum" onchange="onChangeReportType();changeSummImg('${cpath}');">
										<label for="sum">Tabular Summary</label>
										<br/>
										<c:choose>
										<c:when test="${fn:length(reportDesc.dateFields) > 0 }">
											<input type="radio" name="reportType" value="trend" id="trend" onchange="onChangeReportType();changeTrendImg('${cpath}');">
											<label for="trend">Trend</label>
											<br/>
											<input type="radio" name="reportType" value="vtrend" id="vtrend" onchange="onChangeReportType();changeVerticalTrendImg('${cpath}');">
											<label for="trend">Vertical Trend</label>
										</c:when>
										</c:choose>
									</div>
								</td>
								<c:choose>
								<c:when test="${fn:length(reportDesc.dateFields) > 1 }">
								<td>
									<div class="sfLabel">Date Field</div>
									<div class="sfField">
										 <c:if test="${fn:length(reportDesc.dateFields) > 1}">
											   <select name="dateFieldSelection" id="dateFieldSelection" style="width:11em;" class="dropDown" onChange="checkIfNoneSelctd();" >
												   	<c:forEach var="dateField" items="${reportDesc.dateFields}" >
														<option value="${dateField}" <c:if test="${reportDesc.defaultDate eq dateField}">selected</c:if>>
												   		<c:choose>
												   			<c:when test="${dateField eq 'none' || dateField eq 'None' || dateField eq 'NONE'}">
												   				None
												   			</c:when>
												   			<c:otherwise>
												   				${(empty reportDesc.fields[dateField] ? reportDesc.filterOnlyFields[dateField]
												   					: reportDesc.fields[dateField]).displayName}
												   			</c:otherwise>
												   		</c:choose>
												   		</option>
												   	</c:forEach>
											   </select>
										 </c:if>
									</div>
								</td>
								</c:when>
								<c:otherwise>
								    <input type="hidden" id="dateFieldSelection" name="dateFieldSelection" value="${reportDesc.dateFields[0]}"/>
								</c:otherwise>
								</c:choose>

								<c:if test="${fn:length(reportDesc.dateFields) > 0}">
								<td style="border-right: medium none;">
									<div class="sfLabel">Date Range </div>
									<div class="sfField">
										<c:if test="${fn:length(reportDesc.dateFields) > 0}">
											<table class="search">
												<tr>
													<td width="100px">
														<select name="_sel" id="_sel" class="dropDown" onChange="setDateRangeforSel();">
															<option value="pd">Yesterday</option>
															<option value="td">Today</option>
															<option value="pm">Previous Month</option>
															<option value="tm">This Month</option>
															<option value="pf">Previous Financial Year</option>
															<option value="tf">This Financial Year</option>
															<option value="cstm">Custom Date</option>
														</select>
													</td>
													<td valign="top"  style="vertical-align: top; text-align:right; border-right: medium none;">
														From:
															<span style="text-align:left;" onclick="selectCustom();">
															<insta:datewidget name="fromDate" btnPos="left"/>
															</span>
														<br/>
														To:
															<span style="text-align:left;" onclick="selectCustom();">
															<insta:datewidget name="toDate" btnPos="left"/>
															</span>
													</td>
												</tr>
											</table>
										</c:if>
									</div>
								</td>
								</c:if>
							</tr>
						</table>
			 		</td>
				</tr>
			</table>

			<table>
				<tr>
					<td style="width:394px;" valign="top">
						<div id="listFieldsDiv" align="left">
							<fieldset class="fieldSetBorder" style="margin-left: 0px;">
								<legend class="fieldSetLabel">List Fields</legend>
								<table align="center" width="342" style="padding-right:5; padding-left:10px;border-width:0px; margin:0px;" >
									<tr>
										<td align="center" style="padding-right: 4pt; border-width:0px; margin:0px; width:134px;">
											Available Fields
											<br />
											<br />
											<select size="15" style="width:12em;padding-left:5;color:#666666;" multiple name="avlbListFlds" id="avlbListFlds" onDblClick="moveSelectedOptions(this,this.form.listFields);">
												<option value="_sl" title="Continuous Serial Number"> (Sl No.) </option>
												<option value="_gsl" title="Group-wise Serial Number"> (Grp. Sl No.) </option>
												<c:forEach var="fieldName" items="${fieldNamesSorted}" varStatus="i">
												<c:set var="field" value="${reportDesc.fields[fieldName]}"/>
													<option value="${fieldName}" title="${empty field.description ? field.displayName : field.description}">${field.displayName}</option>
												</c:forEach>
											</select>
										</td>
										<td valign="top" align="left" style="padding-right:0;">
											<br/><br/>
											<input type="button" name="addLstFldsButton" value=">"  title="ADD >" onclick="addListFields();"/>
										</td>
										<td valign="top" align="center" style="width:134px;padding-left:4pt;">
											Selected Fields
											<br />
											<br />
											<select size="15" style="width:12em;padding-left:5; color:#666666;" name="listFields" id="listFields" multiple onDblClick="moveSelectedOptions(this,this.form.avlbListFlds);">
											</select>
										</td>
										<td>
											<div align="center">
												<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionUp(listFields);"> <img src="${cpath}/icons/std_up.png" width=10 height=8/> </button>
												<br />
												<br />
												<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionDown(listFields);"><img src="${cpath}/icons/std_down.png" width=10 height=8/></button>
												<br />
												<br />
												<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="removeListFields();"><img src="${cpath}/icons/std_cancel.png"  height="8"  width="10"/></button>
												<br /><br />
												<br /><br />
												<br /><br />
												<br /><br />
												<br /><br />
											</div>
										</td>
									</tr>
								</table>
							</fieldset>
						</div>
						<div id="sumFieldsDiv" align="left" style="display: none">
							<fieldset class="fieldSetBorder" style="margin-left: 0px;">
								<legend class="fieldSetLabel">Summary Fields </legend>
								<table align="center" width="342" style="padding-right:5; padding-left:10px;border-width:0px; margin:0px;">
									<tr>
										<td align="center" style="padding-right: 4pt; border-width:0px; margin:0px; width:134px;">
											Available Fields
											<br />
											<br />
											<select size="15" style="width:12em;padding-left:5; color:#666666;" multiple name="avlbSummFlds" id="avlbSummFlds" onDblClick="moveSelectedOptions(this,this.form.sumFields);">
												<option value="_count" title="(Count of items)"> (Count of items) </option>
												<c:forEach var="fieldName" items="${reportDesc.aggregateFieldNames}" varStatus="i">
												<c:set var="field" value="${reportDesc.fields[fieldName]}"/>
													<option value="${fieldName}" title="${empty field.description ? field.displayName : field.description}"> ${field.displayName} </option>
												</c:forEach>
											</select>
										</td>
										<td valign="top" align="left" style="padding-right:0;">
											<br />
											<br />
											<input type="button" name="addLstFldsButton" value=">" onclick="addListFields();"/>

										</td>
										<td valign="top" align="center" style="width:134px;padding-left:4pt;">
											Selected Fields
											<br />
											<br />
											<select  size="15" style="width:12em;padding-left:5; color:#666666;" multiple id="sumFields" name="sumFields" onDblClick="moveSelectedOptions(this,this.form.avlbSummFlds);">
											</select>
										</td>
										<td>
											<div align="center">
												<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionUp(sumFields);"> <img src="${cpath}/icons/std_up.png" width=10 height=8/>  </button>
												<br />
												<br />
												<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionDown(sumFields);"><img src="${cpath}/icons/std_down.png" width=10 height=8/> </button>
												<br />
												<br />
												<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="removeListFields();"><img src="${cpath}/icons/std_cancel.png"  height="8"  width="10"/></button>
												<br /><br />
												<br /><br />
												<br /><br />
												<br /><br />
												<br/><br/>
											</div>
										</td>
									</tr>
								</table>
							</fieldset>
						</div>
						<table align="left" style="margin-top: 0em;" style="valign:top;">
							<tr>
								<td>
									<table style="padding-top: 6px;">
										<tr>
											<td>
												PDF Font size:
											</td>
											<td>
												<select name="baseFontSize" style="width:4em">
													<option>5</option>
													<option>6</option>
													<option>7</option>
													<option>8</option>
													<option>9</option>
													<option selected>10</option>
													<option>11</option>
													<option>12</option>
													<option>14</option>
													<option>16</option>
													<option>18</option>
													<option>20</option>
												</select>
											</td>
											<td>
												&nbsp;
											</td>
											<td>
												<button type="button" name="button1" id="button1" onclick="showDialog();" title="Additional PDF Customization Options" accesskey="O"/><b><u>O</u></b>ptions</button>
											</td>
										</tr>
									</table>
								</td>
							<tr>
							<tr>
								<td style="padding-top:5px;">
									<table style="align:left;">
										<tr>
										<td><button type="submit" accesskey="P" onclick="return onSubmit('pdf')" title="PDF Report"><b><u>P</u></b>DF</button></td>
										<td><button type="submit" accesskey="C" onclick="return onSubmit('csv')" title="CSV Report"><b><u>C</u></b>SV</button></td>
										<td><button type="submit" accesskey="T" onclick="return onSubmit('text')" title="Text Report"><b><u>T</u></b>ext</button></td>
										<c:if test="${chartsActivated}">
											<td><button id="chart" type="submit" accesskey="V" onclick="return onSubmit('chart')" title="Graphical Report"><b><u>C</u></b>hart</button></td>
										</c:if>
										<td>&nbsp;|&nbsp;</td>
										<td><a href="#" onclick="resetPage();" id="Reset" name="Reset" class="button"/>Reset</a></td>
										<td>&nbsp;|&nbsp;</td>
										<td><a href="${cpath}/reportdashboard/list.htm" target="_blank">Report Dashboard</a></td>
										<td>&nbsp;</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>

					<td style="width: 555px;padding-left:10;" valign="top" align="right">
						<c:set var="groupFieldNames" value="${reportDesc.groupFieldNames}"/>
						<c:set var="fixedGroupFieldNames" value="${reportDesc.fixedGroupFieldNames}"/>
						<c:if test="${not empty groupFieldNames}">
							<div id="listGroupsDiv" align="right">
								<fieldset class="fieldSetBorder" style="height: 170px; margin-right: 0px;">
									<legend class="fieldSetLabel">Groups and Sub-totals</legend>
									<table align="left" cellspacing="0" cellpadding="0">
										<tr>
											<td style="width: 300px;" valign="center">
												<table cellspacing="0" cellpadding="0">
													<c:forEach items="${groupFieldNames}" varStatus="i">
														<c:if test="${i.count < 4}">
															<tr align="left">
																<td width="108" style="padding: 5px; text-align:right;">Level <c:out value="${i.count}"/>: &nbsp;</td>
																<td width="142" style="padding: 5px;">
																	<select name="listGroups" id="listGroups${i.count}" style="width:11em;" class="dropDown" onchange="changeListImg('${cpath}');">
																		<option value="">-- Select --</option>
																		<c:choose>
																			<c:when test="${i.count eq 1}">
																				<c:forEach var="fieldName" items="${groupFieldNames}">
																					<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
																				</c:forEach>
																			</c:when>
																			<c:otherwise>
																				<c:forEach var="fieldName" items="${groupFieldNames}">
																					<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
																				</c:forEach>
																			</c:otherwise>
																		</c:choose>
																	</select>
																</td>
															</tr>
														</c:if>
													</c:forEach>
												</table>
											</td>
											<td align="center" valign="top" style="width: 250px; height: 160px; valign: top;">
												<div style="height: 160px; width: 250px; position: absolute;">
													<a class="picture" href="#" onclick="return false;">
														<img class="small" id="smallImg_list" src="${cpath}/images/std_rep/xxx_list_small.png" title="Sample"/>
														<img class="large" id="largeImg_list" src="${cpath}/images/std_rep/xxx_list_large.png" title="Sample"/>
													</a>
												</div>
											</td>
										</tr>
									</table>
								</fieldset>
							</div>
						</c:if>

						<div id="sumGroupsDiv" align="right" style="display:none">
							<fieldset class="fieldSetBorder" style="height: 170px; margin-right: 0px;">
								<legend class="fieldSetLabel">Groups</legend>
								<table align="left" cellspacing="0" cellpadding="0">
									<tr>
										<td style="width: 300px;" valign="center">
											<table cellspacing="0" cellpadding="0">
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Horizontal Axis:</td>
													<td width="142" style="padding: 5px">
														<select name="sumGroupHoriz" id="sumGroupHoriz" style="width:11em;" class="dropDown" onchange="changeSummImg('${cpath}');">
															<option value="">-- Select --</option>
															<option value="_data">(Summary Fields)</option>
															<c:forEach var="fieldName" items="${fixedGroupFieldNames}">
																<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
															</c:forEach>
														</select>
													</td>
												</tr>
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Vertical Axis:</td>
													<td width="142" style="padding: 5px;">
														<select name="sumGroupVert" id="sumGroupVert" style="width:11em;" class="dropDown" onchange="changeSummImg('${cpath}');">
															<option value="">-- Select --</option>
															<c:forEach var="fieldName" items="${groupFieldNames}">
																<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
															</c:forEach>
														</select>
														<font color="red">*</font>
													</td>
												</tr>
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right">Vertical Axis Sub:</td>
													<td width="142" style="padding: 5px;">
														<select name="sumGroupVertSub" id="sumGroupVertSub" style="width:11em;" class="dropDown" onchange="changeSummImg('${cpath}');">
															<option value="">-- Select --</option>
															<option value="_data">(Summary Fields)</option>
															<c:forEach var="fieldName" items="${groupFieldNames}">
																<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
															</c:forEach>
														</select>
													</td>
												</tr>
											</table>
										</td>
										<td align="center" valign="top" style="width: 250px; height: 160px; valign: top;">
											<div style="height: 160px; width: 250px; position: absolute;">
												<a class="picture" href="#" onclick="return false;">
													<img class="small" id="smallImg_summ" src="${cpath}/images/std_rep/xxx_summary_small.png" title="Sample"/>
													<img class="large" id="largeImg_summ" src="${cpath}/images/std_rep/xxx_summary_large.png" title="Sample"/></a>
												</div>
										</td>
									</tr>
								</table>
							</fieldset>
						</div>

						<div id="trendGroupsDiv" align="right" style="display:none">
							<fieldset class="fieldSetBorder" style="height: 170px; margin-right: 0px;">
							<legend class="fieldSetLabel">Groups</legend>
								<table align="left" cellspacing="0" cellpadding="0">
									<tr>
										<td style="width: 300px;" valign="center">
											<table cellspacing="0" cellpadding="0">
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Trend Type:</td>
													<td width="142" style="padding: 5px">
														<select name="trendType" id="trendType" style="width:11em;" class="dropDown" onchange="changeTrendImg('${cpath}');">
															<option value="month">Monthly</option>
															<option value="week">Weekly</option>
															<option value="day">Daily</option>
														</select>
													</td>
												</tr>
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Vertical Axis:</td>
													<td width="142" style="padding: 5px">
														<select name="trendGroupVert" id="trendGroupVert" style="width:11em;" class="dropDown" onchange="changeTrendImg('${cpath}');">
															<option value="">-- Select --</option>
															<c:forEach var="fieldName" items="${groupFieldNames}">
															<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
															</c:forEach>
														</select>
														<font color="red">*</font>
													</td>
												</tr>
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Vertical Axis Sub:</td>
													<td width="142" style="padding: 5px;">
														<select name="trendGroupVertSub"  id="trendGroupVertSub" style="width:11em;" class="dropDown" onchange="changeTrendImg('${cpath}');">
															<option value="">-- Select --</option>
															<option value="_data">(Summary Fields)</option>
															<c:forEach var="fieldName" items="${groupFieldNames}">
															<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
															</c:forEach>
														</select>
													</td>
												</tr>
											</table>
										</td>
										<td align="center" valign="top" style="width: 250px; height: 160px; valign: top;">
											<div style="height: 160px; width: 250px; position: absolute;">
												<a class="picture" href="#" onclick="return false;">
													<img class="small" id="smallImg_trend" src="${cpath}/images/std_rep/xxx_summary_small.png"/>
													<img class="large" id="largeImg_trend" src="${cpath}/images/std_rep/xxx_summary_large.png"/>
												</a>
											</div>
										</td>
									</tr>
								</table>
							</fieldset>
						</div>

						<div id="verticalTrendGroupsDiv" align="right" style="display:none">
							<fieldset class="fieldSetBorder" style="height: 170px; margin-right: 0px;">
							<legend class="fieldSetLabel">Groups</legend>
								<table align="left" cellspacing="0" cellpadding="0">
									<tr>
										<td style="width: 300px;" valign="center">
											<table cellspacing="0" cellpadding="0">
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Horizontal Axis:</td>
													<td width="142" style="padding: 5px">
														<select name="vtrendGroupHoriz" id="vtrendGroupHoriz" style="width:11em;" class="dropDown" onchange="changeVerticalTrendImg('${cpath}');">
															<option value="">-- Select --</option>
															<option value="_data">(Summary Fields)</option>
															<c:forEach var="fieldName" items="${fixedGroupFieldNames}">
															<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
															</c:forEach>
														</select>
													</td>
												</tr>
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Trend Type:</td>
													<td width="142" style="padding: 5px">
														<select name="vtrendType" id="vtrendType" style="width:11em;" class="dropDown" onchange="changeVerticalTrendImg('${cpath}');">
															<option value="month">Monthly</option>
															<option value="week">Weekly</option>
															<option value="day">Daily</option>
														</select>
														<font color="red">*</font>
													</td>
												</tr>
												<tr align="left">
													<td width="108" style="padding: 5px; text-align:right;">Vertical Axis Sub:</td>
													<td width="142" style="padding: 5px;">
														<select name="vtrendGroupVertSub"  id="vtrendGroupVertSub" style="width:11em;" class="dropDown" onchange="changeVerticalTrendImg('${cpath}');">
															<option value="">-- Select --</option>
															<option value="_data">(Summary Fields)</option>
															<c:forEach var="fieldName" items="${groupFieldNames}">
															<option value="${fieldName}">${reportDesc.fields[fieldName].displayName}</option>
															</c:forEach>
														</select>
													</td>
												</tr>
											</table>
										</td>
										<td align="center" valign="top" style="width: 250px; height: 160px; valign: top;">
											<div style="height: 160px; width: 250px; position: absolute;">
												<a class="picture" href="#" onclick="return false;">
													<img class="small" id="smallImg_vtrend" src="${cpath}/images/std_rep/xxx_summary_small.png"/>
													<img class="large" id="largeImg_vtrend" src="${cpath}/images/std_rep/xxx_summary_large.png"/>
												</a>
											</div>
										</td>
									</tr>
								</table>
							</fieldset>
						</div>

						<fieldset class="fieldSetBorder" style="height:40px; padding-right: 0px; padding-left: 5px; margin-right: 0px;" >
						<legend class="fieldSetLabel">Sort </legend>
							<table align="left" style="width: 545px;">
								<tr>
									<td>
										&nbsp;&nbsp;Sort 1:
										<select class="dropDown"  name="customOrder1" id="customOrder1"  >
													<option value="">-- Select --</option>
													<c:forEach var="fieldName" items="${reportDesc.fieldNames}" varStatus="i">
													<c:set var="field" value="${reportDesc.fields[fieldName]}"/>
														<option value="${fieldName}" title="${empty field.description ? field.displayName : field.description}">${field.displayName}</option>
													</c:forEach>
										</select>
										<span title="Descending Order"> <input type="checkbox" name="sort1" id="sort1" value="DESC" />Desc.</span>
									</td>
									<td align="right">
										Sort 2:
										<select class="dropDown"  name="customOrder2" id="customOrder2"  >
													<option value="">-- Select --</option>
													<c:forEach var="fieldName" items="${reportDesc.fieldNames}" varStatus="i">
													<c:set var="field" value="${reportDesc.fields[fieldName]}"/>
														<option value="${fieldName}" title="${empty field.description ? field.displayName : field.description}">${field.displayName}</option>
													</c:forEach>
										</select>
										<span title="Descending Order"> <input type="checkbox" name="sort2" id="sort2" value="DESC" />Desc.&nbsp;&nbsp;</span>
									</td>
								</tr>
							</table>
						</fieldset>
						<div align="left">
							<c:set var="filterFieldNames" value="${reportDesc.filterFieldNamesSorted}"/>
							<c:if test="${not empty filterFieldNames}">
								<fieldset class="fieldSetBorder" style="padding-right: 0px; padding-left: 5px; margin-right: 0px;" >
								<legend class="fieldSetLabel">Filter</legend>
									<table  style="padding-left: 5px;">
										<tr>
											<td width="">
												<select  class="dropDown filterfields" name="filter.1" id="filter.1" onchange="onChangeFilterBy(1);" style="width:11em;">
													<option selected value="">(No Filter)</option>
													<c:forEach var="fieldName" items="${filterFieldNames}">
														<c:set var="filterField"
															value="${empty reportDesc.fields[fieldName] ? reportDesc.filterOnlyFields[fieldName] : reportDesc.fields[fieldName]}"/>
														<option value="${fieldName}">${filterField.displayName}</option>
													</c:forEach>
												</select>
											</td>
											<td id="td3.1" style="padding-left: 4px;">
												<select  class="dropDown" name="filterOp.1" id="filterOp.1" style="margin: 0;width: 60px;" onchange="fillTBx(1);">
													<option value="eq" selected>=</option>
													<option value="ne">&ne;</option>
													<option value="lt"> &lt; </option>
													<option value="gt"> &gt; </option>
													<option value="le"> &le; </option>
													<option value="ge"> &ge; </option>
													<option value="in">Any of</option>
													<option value="nin">None of</option>
													<option value="ico">Contains</option>
													<option value="co">Contains (exact case) </option>
													<option value="isw">Starts with </option>
													<option value="sw">Starts with (exact case) </option>
													<option value="iew">Ends with </option>
													<option value="ew">Ends with (exact case) </option>
													<option value="between">Between </option>
													<option value="null">is empty</option>
												</select>
												</td>
												<td id="td4.1"style="padding-left: 4px;">
												<input id="txt.1" type="text" name="txt.1" style="margin: 0;width: 11em;"  disabled/>
												<select  class="dropDown" name="filterVal.1" id="filterVal.1" style="margin: 0;width:11em;">
													<option value="">--(All)--</option>
												</select>
											</td>
											<td  align="right" width="" style="padding-left: 5px; padding-right: 5px; padding-top: 9px; height: 18px; width: 17px;">&nbsp;</td>
										</tr>
										</table>
											<table id="addMore" style="padding-left: 5px;" ></table>
										<table  align="right">
											<tr>
												<td align="left"  style="padding-top: 5px;padding-right:27px;">
													<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="addfilterElements();" title="Add another filter"  accesskey="+"><img src="${cpath}/icons/Add.png"  /></button>
												</td>
											</tr>
										</table>
								</fieldset>
							</c:if>
						</div>
					</td>
				</tr>
				<tr>
					<td>
					</td>
					<td>
						<br/>
						<div id="addFav" style="position:relative;bottom:0;margin-left:auto;margin-right:auto;text-align:right;padding-top: 0px;">
								Add To Favourites
									<input name="_report_name" id="_report_name" type="text" value="" autocomplete="off"
										style="width:100px; margin-bottom:0px;" maxlength="99" onkeypress="return validateFavRepTextField(event,this);" />
									<input type="hidden" name="_actionId" value="${actionId}"/>
									<input type="button" value="Save"
										onclick="saveReport(document.inputform);"/>
						</div>
					</td>
				</tr>
			</table>
			<div id="myDialog" style="display:block;visibility:hidden;">
				<div class="bd" id="bd1">
				<table align="center">
					<tr>
						<td>
							<table align="left">
								<tr title="PDF only">
									<td align="right">User Name needed in footer:</td>
									<td align="left"> <input type="checkbox"  name="pdfcstm_option" id="pdfcstm_option1" value="un_needed" checked>
									<br>
										<input type="hidden" name="userNameNeeded" id="userNameNeeded" value="Y" />
									</td>
								</tr>
								<tr title="PDF only">
									<td align="right">Date & Time needed in footer:</td>
									<td align="left">
										<input type="checkbox" name="pdfcstm_option" id="pdfcstm_option2" value="dt_needed" checked>
										<input type="hidden" name="dt_needed" id="dt_needed" value="false" />
									</td>
								</tr>
								<tr title="PDF only">
									<td align="right">Hospital Name and Address needed in footer:</td>
									<td align="left">
										<input type="checkbox"  name="pdfcstm_option" id="pdfcstm_option3" value="hsp_needed" checked>
										<input type="hidden" name="hsp_needed" id="hsp_needed" value="false" />
									</td>
								</tr>
								<tr>
									<td align="right"> Hospital Name and Address needed in header:</td>
									<td align="left">
										<input type="checkbox" name="pdfcstm_option" id="pdfcstm_option7" value="hsp_needed_h" >
										<input type="hidden" name="hsp_needed_h" id="hsp_needed_h" value="false" />
									</td>
								</tr>
								<tr title="PDF only">
									<td align="right">Page Numbers needed in footer:</td>
									<td align="left">
										<input type="checkbox" name="pdfcstm_option" id="pdfcstm_option4" value="pgn_needed" checked>
										<input type="hidden" name="pgn_needed" id="pgn_needed" value="false" />
									</td>
								</tr>
								<tr title="Details List only">
									<td align="right">Group (Outline) Numbering:</td>
									<td align="left">
										<input type="checkbox"  name="pdfcstm_option" id="pdfcstm_option5" value="grpn_needed">
										<input type="hidden" name="grpn_needed" id="grpn_needed" value="false" />
									</td>
								</tr>
								<tr title="Details List only">
									<td align="right">Hide Repeated Row Values:</td>
									<td align="left">
										<input type="checkbox"  name="pdfcstm_option" id="pdfcstm_option8" value="grpn_needed">
										<input type="hidden" name="skip_repeated_values" id="skip_repeated_values" value="false" />
									</td>
								</tr>
								<tr>
									<td align="right">Display filter description:</td>
									<td align="left">
										<input type="checkbox" name="pdfcstm_option" id="pdfcstm_option6" value="filterDesc_needed" checked>
										<input type="hidden" name="filterDesc_needed" id="filterDesc_needed" value="false" />
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
				</div>
			</div>
		</div>
	</form>
	<script>
			var filterValues = ${filterValuesJSON};
			var allowNull = ${allowNullJSON};
			var filterNmz = ${filterDisplayNamesJSON};
			var filterTyps = ${filterTypesJSON};
			var fieldWidth = ${fieldWidthsJSON};
			var defaultShowFields = ${defaultShowFieldsJSON};
			var customReportIds = ${CustomReportIDsJSON};
			var favRepTitles = ${favRepTitlesJSON};
			var notHrzGrpble = ${notHrzGrpbleJSON};
			var filterOnlyNames = ${filterOnlyNamesJSON};
			function onInit() {
				document.inputform.submit.disabled=true;
				initSelectedFields();
				onChangeFilterBy(1);
				if (${fn:length(reportDesc.dateFields) > 0}){
					setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
					document.inputform._sel.disabled= false;
					document.inputform._sel.options[0].selected= true;
				}
				checkIfNoneSelctd();
				onChangeReportType();
				initDialog();
			}
	</script>
	</body>
</html>

