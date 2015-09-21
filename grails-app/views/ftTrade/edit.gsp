<%@ page import="settle.FtTrade" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftTrade.label', default: 'FtTrade')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
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
    <h1><g:message code="default.edit.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${ftTradeInstance}">
        <div class="errors">
            <g:renderErrors bean="${ftTradeInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <g:hiddenField name="id" value="${ftTradeInstance?.id}"/>
        <g:hiddenField name="version" value="${ftTradeInstance?.version}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="srvCode"><g:message code="ftTrade.srvCode.label" default="Srv Code"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'srvCode', 'errors')}">
                        <g:textField name="srvCode" maxlength="20" value="${ftTradeInstance?.srvCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="tradeCode"><g:message code="ftTrade.tradeCode.label" default="Trade Code"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'tradeCode', 'errors')}">
                        <g:textField name="tradeCode" maxlength="20" value="${ftTradeInstance?.tradeCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="customerNo"><g:message code="ftTrade.customerNo.label"
                                                           default="Customer No"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'customerNo', 'errors')}">
                        <g:textField name="customerNo" maxlength="24" value="${ftTradeInstance?.customerNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="seqNo"><g:message code="ftTrade.seqNo.label" default="Seq No"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'seqNo', 'errors')}">
                        <g:textField name="seqNo" maxlength="24" value="${ftTradeInstance?.seqNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="tradeDate"><g:message code="ftTrade.tradeDate.label" default="Trade Date"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'tradeDate', 'errors')}">
                        <g:datePicker name="tradeDate" precision="day" value="${ftTradeInstance?.tradeDate}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="liquidateNo"><g:message code="ftTrade.liquidateNo.label"
                                                            default="Liquidate No"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'liquidateNo', 'errors')}">
                        <g:textField name="liquidateNo" value="${ftTradeInstance?.liquidateNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="preFee"><g:message code="ftTrade.preFee.label" default="Pre Fee"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'preFee', 'errors')}">
                        <g:textField name="preFee" value="${fieldValue(bean: ftTradeInstance, field: 'preFee')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="postFee"><g:message code="ftTrade.postFee.label" default="Post Fee"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'postFee', 'errors')}">
                        <g:textField name="postFee" value="${fieldValue(bean: ftTradeInstance, field: 'postFee')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="liqDate"><g:message code="ftTrade.liqDate.label" default="Liq Date"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'liqDate', 'errors')}">
                        <g:datePicker name="liqDate" precision="day" value="${ftTradeInstance?.liqDate}" default="none"
                                      noSelection="['': '']"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="redo"><g:message code="ftTrade.redo.label" default="Redo"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'redo', 'errors')}">
                        <g:checkBox name="redo" value="${ftTradeInstance?.redo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="netAmount"><g:message code="ftTrade.netAmount.label" default="Net Amount"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'netAmount', 'errors')}">
                        <g:textField name="netAmount" value="${fieldValue(bean: ftTradeInstance, field: 'netAmount')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="redoTime"><g:message code="ftTrade.redoTime.label" default="Redo Time"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'redoTime', 'errors')}">
                        <g:datePicker name="redoTime" precision="day" value="${ftTradeInstance?.redoTime}"
                                      default="none" noSelection="['': '']"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="feeType"><g:message code="ftTrade.feeType.label" default="Fee Type"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'feeType', 'errors')}">
                        <g:textField name="feeType" value="${fieldValue(bean: ftTradeInstance, field: 'feeType')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="freezeAmount"><g:message code="ftTrade.freezeAmount.label"
                                                             default="Freeze Amount"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'freezeAmount', 'errors')}">
                        <g:textField name="freezeAmount"
                                     value="${fieldValue(bean: ftTradeInstance, field: 'freezeAmount')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="channelCode"><g:message code="ftTrade.channelCode.label"
                                                            default="Channel Code"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'channelCode', 'errors')}">
                        <g:textField name="channelCode" maxlength="20" value="${ftTradeInstance?.channelCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="amount"><g:message code="ftTrade.amount.label" default="Amount"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'amount', 'errors')}">
                        <g:textField name="amount" value="${fieldValue(bean: ftTradeInstance, field: 'amount')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="billDate"><g:message code="ftTrade.billDate.label" default="Bill Date"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftTradeInstance, field: 'billDate', 'errors')}">
                        <g:datePicker name="billDate" precision="day" value="${ftTradeInstance?.billDate}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="realtimeSettle"><g:message code="ftTrade.realtimeSettle.label"
                                                               default="Realtime Settle"/></label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: ftTradeInstance, field: 'realtimeSettle', 'errors')}">
                        <g:textField name="realtimeSettle"
                                     value="${fieldValue(bean: ftTradeInstance, field: 'realtimeSettle')}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="update"
                                                 value="${message(code: 'default.button.update.label', default: 'Update')}"/></span>
            <span class="button"><g:actionSubmit class="delete" action="delete"
                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
