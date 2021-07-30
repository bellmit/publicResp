<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<html>
<head>
	<title>Statistics</title>
	<insta:link type="js" file="ajax.js" />
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	
  	<script>
  		var index = 1;
		function initAccordion() {
			YAHOO.lutsr.accordion.properties.animation = true;
		}

  		function pageStatsHandler() {
				if(index <= 12) {
					loadingDiv = document.getElementById("load"+index);
					if (loadingDiv == null) {
						index++;
						pageStatsHandler();
					} else {
						startGetCount(index);
					}
				}
  		}

		function startGetCount(i) {
			if(i <= 12) {
				loadingDiv = document.getElementById("load"+i);
				if(loadingDiv != null) {
					YAHOO.util.Connect.asyncRequest('GET',
						cpath +"/pages/usermanager/PageStatsAction.do?method=getPageStatsJSON&statsNo="+i,
						{ 	success: onGetCount,
							failure: onGetCountFailure,
							argument: i,
							timeout: 60000}
					);
				} else {
					index++;
					startGetCount(index);
				}
			}

		}

  		function deployPageStats(){
  			if (!pageStatsPanel.isOpen()) {
    			    pageStatsHandler();
  			}
  		}

		function onGetCount(response) {
			if(((response.responseText).split(""))[0]!="{"){
				onGetCountFailure(response);
				return;
			}
			var i = response.argument;
			loadingDiv = document.getElementById("load"+index);
			eval("var details = "+ response.responseText);
			if(document.getElementById("load"+i) != null)
				document.getElementById("load"+i).innerHTML = '&nbsp;';
			if (response.responseText!= null && details["stats"+i]!='undefined'
						&& details["stats"+i]!=null && details["stats"+i]!='')
				document.getElementById("load"+i).innerHTML = details["stats"+i];
			// fetch the next
			if (++i <= 12) {
				startGetCount(i);
			}
  	}

  	function onGetCountFailure(response) {
  		var i = response.argument;
  		document.getElementById("load"+i).innerHTML = 'Loading...';
  	}


  	</script>
  	<style>
  		.CollapsiblePanelContent {
			margin: 0px;
			padding: 0px;
			border-bottom: solid 1px #CCC;
			height: 102px;
			width:99.9%;
		}

		.CollapsiblePanelTab {
			width:99.9%;
		}
  	</style>
  	
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<body onload="initAccordion();">

	<input type="hidden" name="chk" id="chk" value=""/>
		<h1><insta:ltext key="common.dashboard.statistics"/></h1>
		<center>
		<div id="pageStatsPanel" class="CollapsiblePanel" style="border-top-width: 0px;">
				<div class=" title CollapsiblePanelTab" style=" border-left:none;" onclick="deployPageStats();">
			    	<div class="fltL " style="width: 99%; margin:5px 0 0 10px;text-align:left;"> <insta:ltext key="instahms.common.homepage.statistics"/></div>
					<div class="fltR txtRT" style="margin:-10px 10 0 95%; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
					<div class="clrboth"></div>
				</div>
				<div class="CollapsiblePanelContent" style="height:102px;width:99%;">
						<table id="pgStatsTabl" width="100%" cellspacing="0" cellpadding="0">
							<tr>
								<c:forTokens var="col" items="0,4,8" delims=",">
									<td style="width: 314px; padding: ${col eq 4 ? '0 8px 10px 8px' : '0 0px 10px 0px'};">
										<table width="100%" cellspacing="0" cellpadding="0" class="brB brL brR brT">
											<c:forTokens var="row" items="1,2,3,4" delims=",">
												<c:set var="i" value="${col+row}"/>
												<c:set var="stat" value="stats${i}"/>
												<c:choose>
													<c:when test="${not empty displayNameMap[stat]}">
														<tr>
															<td width="80%" class="tableInn">
																<a href="${cpath}${urlMap[stat]}" target="_blank">${displayNameMap[stat]}</a>
															</td>
															<td width="20%" class="tableInnNum">
																<div id="load${i}" style="display:block;"><insta:ltext key="instahms.common.homepage.loading"/></div>
															</td>
														</tr>
													</c:when>
													<c:otherwise>		<%-- no stat configured, show empty row --%>
														<tr>
															<td width="80%" class="tableInn">&nbsp;</td>
															<td width="20%" class="tableInnNum">&nbsp;</td>
														</tr>
													</c:otherwise>
												</c:choose>
											</c:forTokens>
										</table>
									</td>
								</c:forTokens>
							</tr>
						</table>
				</div>
		</div>
		</center>
		
		
		<script>
			<c:forEach items="${displayGroups}" var="entry">
				<c:if test="${entry.value}">
					document.getElementById('th_${entry.key}').style.display = 'inline';
				</c:if>
			</c:forEach>
			var pageStatsPanel = new Spry.Widget.CollapsiblePanel("pageStatsPanel", { contentIsOpen: false });
		</script>
		
		
</body>

</html>