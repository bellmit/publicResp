<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<jsp:useBean id="date" class="java.util.Date" />
<div class="foottertxt" style="margin-top: 5px;">
    <table>
        <tr>
            <td>
                <a href="http://www.instahealthsolutions.com/" class="footer-supportlink"
                   title="Visit the web site (opens in a new window)" target="_blank">
                   Insta by Practo.
                </a> Version <fmt:message key="insta.software.version" />.
                 Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.
            </td>
        </tr>
    </table>
</div>