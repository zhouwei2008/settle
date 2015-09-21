<%@ page import="settle.FtTrade" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftTrade.label', default: 'FtTrade')}"/>
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
                <td valign="top" class="name"><g:message code="ftTrade.id.label" default="Id"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "id")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.srvCode.label" default="Srv Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "srvCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.tradeCode.label" default="Trade Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "tradeCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.customerNo.label" default="Customer No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "customerNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.seqNo.label" default="Seq No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "seqNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.tradeDate.label" default="Trade Date"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftTradeInstance?.tradeDate}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.liquidateNo.label" default="Liquidate No"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "liquidateNo")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.preFee.label" default="Pre Fee"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "preFee")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.postFee.label" default="Post Fee"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "postFee")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.liqDate.label" default="Liq Date"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftTradeInstance?.liqDate}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.redo.label" default="Redo"/></td>

                <td valign="top" class="value"><g:formatBoolean boolean="${ftTradeInstance?.redo}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.netAmount.label" default="Net Amount"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "netAmount")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.redoTime.label" default="Redo Time"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftTradeInstance?.redoTime}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.feeType.label" default="Fee Type"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "feeType")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.freezeAmount.label"
                                                         default="Freeze Amount"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "freezeAmount")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.channelCode.label" default="Channel Code"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "channelCode")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.amount.label" default="Amount"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "amount")}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.billDate.label" default="Bill Date"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftTradeInstance?.billDate}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.dateCreated.label" default="Date Created"/></td>

                <td valign="top" class="value"><g:formatDate date="${ftTradeInstance?.dateCreated}"/></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="ftTrade.realtimeSettle.label"
                                                         default="Realtime Settle"/></td>

                <td valign="top" class="value">${fieldValue(bean: ftTradeInstance, field: "realtimeSettle")}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <g:hiddenField name="id" value="${ftTradeInstance?.id}"/>
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
