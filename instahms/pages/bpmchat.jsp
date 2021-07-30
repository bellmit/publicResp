<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"
    scope="request" />
<c:set var="liveChatSupport"
    value='<%=GenericPreferencesDAO.getAllPrefs().getMap().containsKey("live_chat_support") && ((Boolean) GenericPreferencesDAO.getAllPrefs().get("live_chat_support")) %>' />
<c:set var="hospitalName"
    value='<%=GenericPreferencesDAO.getAllPrefs().get("hospital_name") %>' />
<c:set var="hospitalAddress"
    value='<%=GenericPreferencesDAO.getAllPrefs().get("hospital_address") %>' />
<c:set var="isAdmin" value='${roleId == 1 || roleId == 2 || loggedInRoleId == 1 || loggedInRoleId == 2}'/>
<c:if test="${liveChatSupport && isAdmin}">
<!-- Start of LiveChat (www.livechatinc.com) code -->
<script type="text/javascript">
    window.__lc = window.__lc || {};
    window.__lc.license = 10420122;
    ;(function(n,t,c){function i(n){return e._h?e._h.apply(null,n):e._q.push(n)}var e={_q:[],_h:null,_v:"2.0",on:function(){i(["on",c.call(arguments)])},once:function(){i(["once",c.call(arguments)])},off:function(){i(["off",c.call(arguments)])},get:function(){if(!e._h)throw new Error("[LiveChatWidget] You can't use getters before load.");return i(["get",c.call(arguments)])},call:function(){i(["call",c.call(arguments)])},init:function(){var n=t.createElement("script");n.async=!0,n.type="text/javascript",n.src="https://cdn.livechatinc.com/tracking.js",t.head.appendChild(n)}};!n.__lc.asyncInit&&e.init(),n.LiveChatWidget=n.LiveChatWidget||e}(window,document,[].slice));
    LiveChatWidget.on('ready', function() {
        LiveChatWidget.call('minimize');
        LiveChatWidget.call('set_customer_name', userid + ' - ${centerName}, ${hospitalName} (' + schema + ')');
        LiveChatWidget.call('set_customer_email', userid + '@' + schema);
        LiveChatWidget.call('set_session_variables', {
          'Schema':schema,
          'User': userid,
          'Hospital': '${hospitalName}' ,
          'Address': '${hospitalAddress}' ,
          'Center': '${centerName}',
          'centerId': '${centerId}',
          'HMS Version': '<fmt:message key="insta.software.version" />'
        });

    });

</script>
<noscript><a href="https://www.livechatinc.com/chat-with/10420122/" rel="nofollow">Chat with us</a>, powered by <a href="https://www.livechatinc.com/?welcome" rel="noopener nofollow" target="_blank">LiveChat</a></noscript>
<!-- End of LiveChat code -->
</c:if>
