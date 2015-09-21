<%@ page import="settle.FtFoot" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftFoot.label', default: 'FtFoot')}"/>
    <title><g:message code="default.show.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                           args="[entityName]"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.show.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.id.label" default="Id"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "id")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.srvCode.label" default="Srv Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "srvCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.tradeCode.label" default="Trade Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "tradeCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.customerNo.label" default="Customer No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "customerNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.type.label" default="Type"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "type")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.footNo.label" default="Foot No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "footNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.checkStatus.label" default="Check Status"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "checkStatus")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.checkOpId.label" default="Check Op Id"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "checkOpId")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.checkDate.label" default="Check Date"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftFootInstance?.checkDate}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.createOpId.label" default="Create Op Id"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "createOpId")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.freezeAmount.label" default="Freeze Amount"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "freezeAmount")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.autoCheck.label" default="Auto Check"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "autoCheck")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.autoWithdraw.label" default="Auto Withdraw"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "autoWithdraw")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.withdraw.label" default="Withdraw"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "withdraw")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.amount.label" default="Amount"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "amount")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.feeType.label" default="Fee Type"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "feeType")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.footDate.label" default="Foot Date"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftFootInstance?.footDate}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.postFee.label" default="Post Fee"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "postFee")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.preFee.label" default="Pre Fee"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "preFee")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftFoot.transNum.label" default="Trans Num"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftFootInstance, field: "transNum")}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <g:hiddenField name="id" value="${ftFootInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="edit"
                                                 value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" action="delete"
                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
