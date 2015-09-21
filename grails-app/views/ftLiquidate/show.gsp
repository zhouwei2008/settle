<%@ page import="settle.FtLiquidate" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftLiquidate.label', default: 'FtLiquidate')}"/>
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
                <td valign="top" class="name"><g:message code="ftLiquidate.id.label" default="Id"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "id")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.srvCode.label" default="Srv Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "srvCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.tradeCode.label" default="Trade Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "tradeCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.preFee.label" default="Pre Fee"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "preFee")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.postFee.label" default="Post Fee"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "postFee")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.customerNo.label"
                                                         default="Customer No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "customerNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.liqDate.label" default="Liq Date"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftLiquidateInstance?.liqDate}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.liquidateNo.label"
                                                         default="Liquidate No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "liquidateNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.footNo.label" default="Foot No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "footNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.status.label" default="Status"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "status")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.postFootNo.label"
                                                         default="Post Foot No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "postFootNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.postFootStatus.label"
                                                         default="Post Foot Status"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "postFootStatus")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.settleType.label"
                                                         default="Settle Type"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "settleType")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.redoTime.label" default="Redo Time"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftLiquidateInstance?.redoTime}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.freezeAmount.label"
                                                         default="Freeze Amount"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "freezeAmount")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.channelCode.label"
                                                         default="Channel Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "channelCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.amount.label" default="Amount"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "amount")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.dateCreated.label"
                                                         default="Date Created"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftLiquidateInstance?.dateCreated}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.feeType.label" default="Fee Type"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "feeType")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.liqAccNo.label" default="Liq Acc No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "liqAccNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.redo.label" default="Redo"/></td>

                <td valign="top" class="value"><g:formatBoolean boolean="${ftLiquidateInstance?.redo}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftLiquidate.transNum.label" default="Trans Num"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftLiquidateInstance, field: "transNum")}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <g:hiddenField name="id" value="${ftLiquidateInstance?.id}"/>
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
