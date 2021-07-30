<%@tag pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/esapi.tld" prefix="esapi" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:if test="${not empty info || not empty warning || not empty error || not empty warnings}">
	<div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" class="brB brT brL brR" id="msgDiv">
		<div class="fltR" style="margin:-8px 0px 0 26px; width:17px;"> <img src="${cpath}/images/fileclose.png" onclick="document.getElementById('msgDiv').style.display='none';"/></div>
		<c:if test="${not empty info}">
			<div>
			<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;"> <img src="${cpath}/images/information.png" /></div>
				<c:choose>
				<c:when test="${info.getClass().simpleName eq 'String' || error.getClass().simpleName eq 'StringBuilder'}">
					<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;"> <esapi:encodeForHTML> ${info} </esapi:encodeForHTML> </div>
				</c:when>
				<c:otherwise>
					<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;">
						<c:forEach items="${info}" var="info">
						<c:choose>
                                                <c:when test="${isEncoded != 'false'}">
                                                    <c:out value="${info.key}"/> == <c:out value="${info.value}"/><br/>
                                                </c:when>
                                                <c:otherwise>
                                                    ${info.key} == ${info.value}<br/>
                                                </c:otherwise>
                                            </c:choose>

						</c:forEach>
					</div>
				</c:otherwise>
			</c:choose>
			</div>
		</c:if>
		<c:if test="${not empty warning}">
			<c:set var="margin" value="${empty info ? 'margin:0px 0 0 5px': 'margin:10px 0 0 5px'}"/>
			<div class="clrboth"></div>
			<div class="fltL" style="width: 25px; margin:5px 0 0 3px;"> <img src="${cpath}/images/warning.png" /></div>
			<div class="fltL"  style="${margin} ; width:865px;"> <esapi:encodeForHTML> ${warning} </esapi:encodeForHTML> </div>
		</c:if>
		
		<c:if test="${not empty warnings}">
			<c:set var="margin" value="${empty info ? 'margin:0px 0 0 5px': 'margin:10px 0 0 5px'}"/>
			<div class="clrboth"></div>
			<div class="fltL" style="width: 25px; margin:5px 0 0 3px;"> <img src="${cpath}/images/warning.png" /></div>
			<c:choose>
				<c:when test="${warnings.getClass().simpleName eq 'String' || warnings.getClass().simpleName eq 'StringBuilder'}">
					<div class="fltL"  style="${margin} ; width:865px;"> <esapi:encodeForHTML> ${warning} </esapi:encodeForHTML> </div>
				</c:when>
				<c:otherwise>
					<div class="fltL"  style="${margin} ; width:865px;"> 
					<c:forEach items="${warnings}" var="warnings">
						Line Number : <c:out value="${warnings.key}"/> : <c:out value="${warnings.value}"/><br/>
					</c:forEach>
					</div>
				</c:otherwise>
			</c:choose>
		</c:if>
		
		<c:if test="${not empty error}">
			<div class="clrboth"></div>
			<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;"> <img src="${cpath}/images/error.png" /></div>
			<c:choose>
				<c:when test="${error.getClass().simpleName eq 'String' || error.getClass().simpleName eq 'StringBuilder'}">
					<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;">
					<c:choose>
                        <c:when test="${isEncoded != 'false'}">
                            <esapi:encodeForHTML> ${error} </esapi:encodeForHTML>
                        </c:when>
                        <c:otherwise>
                            ${error}
                        </c:otherwise>
                    </c:choose>
					</div>
				</c:when>
				<c:otherwise>
					<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;">
					<c:forEach items="${error}" var="error">
						Line : <c:out value="${error.key}"/> == <c:out value="${error.value}"/><br/>
					</c:forEach>
					</div>
				</c:otherwise>
			</c:choose>
		</c:if>
		<div class="clrboth"></div>
	</div>
</c:if>
