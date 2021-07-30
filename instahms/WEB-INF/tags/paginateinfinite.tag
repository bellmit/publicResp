<%@tag pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ attribute name="curPage" required="true"  type="java.lang.Integer"%>
<%@ attribute name="isLastPage" required="true"  type="java.lang.Boolean"%>
<%@ attribute name="sectionSize" required="false" type="java.lang.Integer" %>	<%-- defaults to 5 --%>
<%@ attribute name="baseUrl" required="false" %>	<%-- defaults to current URL if not specified --%>
<%@ attribute name="pageNumParam" required="false" %>	<%-- defaults to 'pageNum' if not specified --%>
<%@ attribute name="display" required="false"%> <%-- defaults to 'right' if not specified else starts from middle of the page --%>
<%@ attribute name="showTooltipButton" required="false"%>
<%-- Attributes required for PageSize --%>
<%@ attribute name="showPageSize" required="false" type="java.lang.Boolean" %> <%-- defaults to false --%>
<%@ attribute name="currPageSize" required="false" type="java.lang.Integer" %>
<c:set var="pageSizeParam" value="pageSize" />

<%-- Writes out the pagination controls required by dashboard. --%>
<c:set var="numPages" value="${ isLastPage ? curPage : (curPage + 10) }"/>
<%-- Set the default values for the attributes --%>
<c:if test="${empty sectionSize}"><c:set var="sectionSize" value="5"/></c:if>
<c:if test="${empty curPage || curPage == 0}"><c:set var="curPage" value="1"/></c:if>
<c:if test="${empty pageNumParam}"><c:set var="pageNumParam" value="pageNum"/></c:if>
<c:if test="${empty display}"><c:set var="display" value="right"/></c:if>
<c:if test="${empty showPageSize}"><c:set var="showPageSize" value="false"/></c:if>

<c:url var="urlWithoutPage" value="${baseUrl}">
	<c:forEach var="p" items="${param}">
		<c:if test="${p.key != pageNumParam && p.key != 'msg' && p.key != 'error' && p.key != 'prgkey'}">
			<c:forEach items="${paramValues[p.key]}" var="value">	<%-- handle multival params --%>
				<c:param name="${p.key}" value="${value}"/> <%-- cannot skip empty because order is important,eg for op=ge,le --%>
			</c:forEach>
		</c:if>
	</c:forEach>
</c:url>

<c:if test="${showPageSize == true}">
	<c:url var="urlWithoutPageSize" value="${baseUrl}">
		<c:forEach var="p" items="${param}">
			<c:if test="${p.key != pageSizeParam && p.key != 'msg' && p.key != 'error' && p.key != 'prgkey'}">
				<c:forEach items="${paramValues[p.key]}" var="value">	<%-- handle multival params --%>
					<c:param name="${p.key}" value="${value}"/> <%-- cannot skip empty because order is important,eg for op=ge,le --%>
				</c:forEach>
			</c:if>
		</c:forEach>
	</c:url>
</c:if>

<c:set var="urlWithoutPage" 
	value="${urlWithoutPage != '' ? fn:escapeXml(urlWithoutPage) : '?'}" />

<c:set var="showFirst" value="2"/>
<c:set var="showLast" value="2"/>
<c:set var="filter" value="3"/>
<c:set var="afterFirstLast" value="${numPages - (showFirst+showLast)}"/>
<c:set var="tooltipHelp"><insta:ltext key="paginate.tooltip.help"/></c:set>
<div class = "pagination-wrapper">
	<div style="display: ${empty showTooltipButton or not showTooltipButton ? 'none' : 'block' }"
			class="fltL tooltipactive" id="toolTipSwitch"
			title="${tooltipHelp}" ></div>

	<c:if test="${not empty param._savedsearch}">
		<div class="fltL "><insta:ltext key="paginate.using.saved.search"/>: <span class="bold">${ifn:cleanHtml(param._savedsearch)}</span></div>
	</c:if>

	<!-- start: ${startPage} end: ${endPage} num: ${numPages} cur: ${curPage} size: ${sectionSize} -->
	<c:if test="${numPages > 0}">
		<div class="fltR ${display == 'right'?'txtRT':''} pagination" >
			<ul>
				<c:choose>
					<c:when test="${numPages == 1}">
						<li ><insta:ltext key="paginate.page"/> : <a href="${urlWithoutPage}&amp;${pageNumParam}=1">1</a></li>
					</c:when>
					<c:when test="${numPages <= (showFirst+showLast+filter)}">
						<li style=" background-color:#fff; display: ${curPage == 1?'none':'inline'}"><a href="${urlWithoutPage}&amp;${pageNumParam}=${curPage-1}"> <insta:ltext key="paginate.prev"/></a> | </li>
						<c:forEach var="page" begin="1" end="${numPages}">
							<li class="${curPage == page?'selected':''}"><a href="${urlWithoutPage}&amp;${pageNumParam}=${page}">${page}</a></li>
						</c:forEach>
						<li style=" background-color:#fff; display: ${curPage == numPages?'none':'inline'}"> | <a href="${urlWithoutPage}&amp;${pageNumParam}=${curPage+1}"><insta:ltext key="paginate.next"/></a></li>
					</c:when>

					<c:otherwise>
						<li style=" background-color:#fff; display: ${curPage == 1?'none':'inline'}"><a href="${urlWithoutPage}&amp;${pageNumParam}=${curPage-1}"> <insta:ltext key="paginate.prev"/></a> | </li>
						<c:forEach var="page" begin="1" end="${showFirst}">
							<li class="${curPage == page?'selected':''}"><a href="${urlWithoutPage}&amp;${pageNumParam}=${page}">${page}</a></li>
						</c:forEach>

						<c:choose >

							<c:when test="${curPage <= (showFirst+filter)}">
								<c:forEach var="page" begin="${showFirst+1}" end="${(showFirst+filter)}">
									<li class="${curPage == page?'selected':''}"><a href="${urlWithoutPage}&amp;${pageNumParam}=${page}">${page}</a></li>
								</c:forEach>
								<c:if test="${(numPages - (showFirst+showLast+filter)) > 0}">
									<li style=" margin-right:-3px; font:normal 10px Tahoma; border:none; background-color:#FFF">....</li>
								</c:if>
							</c:when>

							<c:when test="${curPage > (numPages-(showLast+filter))}">
								<c:if test="${(numPages - (showFirst+showLast+filter)) > 0}">
									<li style=" margin-right:-3px; font:normal 10px Tahoma; border:none; background-color:#FFF">....</li>
								</c:if>
								<c:set var="index" value="${showLast+filter}"/>
								<c:forEach var="page" begin="${showLast+1}" end="${(showLast+filter)}">
									<c:set var="index" value="${index-1}"/>
									<li class="${curPage == numPages-index?'selected':''}"><a href="${urlWithoutPage}&amp;${pageNumParam}=${numPages-index}">${numPages-index}</a></li>
								</c:forEach>
							</c:when>

							<c:when test="${afterFirstLast > filter}">
								<li style=" margin-right:-3px; font:normal 10px Tahoma; border:none; background-color:#FFF">....</li>
								<c:forEach var="page" begin="1" end="${filter}" varStatus="status">
									<c:set var="index" value="${status.index-2}"/>
									<li class="${curPage == curPage+index?'selected':''}"><a href="${urlWithoutPage}&amp;${pageNumParam}=${curPage+index}">${curPage+index}</a></li>
								</c:forEach>
								<li style=" margin-right:-3px; font:normal 10px Tahoma; border:none; background-color:#FFF">....</li>
							</c:when>
						</c:choose>

						<c:set var="index" value="${showLast}"/>
						<c:forEach var="page" begin="1" end="${showLast}" varStatus="status">
							<c:set var="index" value="${index-1}"/>
							<li class="${curPage == numPages-index?'selected':''}">
								<a href="${urlWithoutPage}&amp;${pageNumParam}=${numPages-index}">${numPages-index}</a>
							</li>
						</c:forEach>
						<li style=" background-color:#fff; display: ${curPage == numPages?'none':'inline'}"> | <a href="${urlWithoutPage}&amp;${pageNumParam}=${curPage+1}"><insta:ltext key="paginate.next"/></a></li>
					</c:otherwise>
				</c:choose>
			</ul>
		</div>
		<div class="fltR" style="display: ${showPageSize == false ? 'none' : display }; margin-right: 10px; margin-bottom: 5px">			
			Records per page:
			<input type="text" name="pageSize" id="pageSize" maxlength="3" value="${currPageSize}" style="width: 4em;" onkeypress="submitForm(event)"/>
			<button type="button" onclick= "validatePageSize()" > Apply</button>
		</div>
		
		<script>
			function validatePageSize(){
				var pageSize = document.getElementById("pageSize").value.trim();
				if( !isNaN(pageSize) && pageSize > 0 && pageSize <= 999){
					window.location.href="${urlWithoutPageSize}&pageSize="+pageSize;
				}					
				else{
					alert("Records Per Page should be between 1 and 999");
				}					
			}
			function submitForm(event){
				var charCode = (event.keyCode) ? event.keyCode : event.which;
				//on press ENTER key
				if(charCode == 13){
					validatePageSize();
				}			
			}
		</script>		

	</c:if>
</div>
<div class="clrboth"></div>

