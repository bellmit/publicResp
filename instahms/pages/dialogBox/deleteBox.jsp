<%@ page contentType='text/html' isELIgnored='false'%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/fmt' prefix='fmt' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/functions' prefix='fn' %>
<%@ taglib tagdir='/WEB-INF/tags' prefix='insta' %>

<head>
    <insta:link type='css' file='homePage/dialogBox.css' />
</head>
<!-- Delete box to remove favourite tabs -->
<div id="delete-dialog">
    <div class="bd dialog-style">
        <div id="deleteContent"> </div>
        <table>
            <tbody>
                <tr>
                    <td class="dialog-button-position">
                        <button id="removeDeleteDialog" type="button" value="" onclick="deleteTab()">
                            <b>Remove</b>
                        </button>
                    </td>
                    <td class="dialog-button-position">
                        <button type="button" id="closeDeleteDialog" value="Cancel">
                            <b>Cancel</b>
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
<!-- Deletion box ended -->