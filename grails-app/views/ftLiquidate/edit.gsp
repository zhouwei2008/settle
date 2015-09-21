<%@ page import="settle.FtLiquidate" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftLiquidate.label', default: 'FtLiquidate')}"/>
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
<g:hasErrors bean="${ftLiquidateInstance}">
    <div class="errors">
        <g:renderErrors bean="${ftLiquidateInstance}" as="list"/>
    </div>
</g:hasErrors>
<g:form method="post">
    <g:hiddenField name="id" value="${ftLiquidateInstance?.id}"/>
    <g:hiddenField name="version" value="${ftLiquidateInstance?.version}"/>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="srvCode"><g:message code="ftLiquidate.srvCode.label" default="Srv Code"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'srvCode', 'errors')}">
                    <g:textField name="srvCode" maxlength="20" value="${ftLiquidateInstance?.srvCode}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="tradeCode"><g:message code="ftLiquidate.tradeCode.label" default="Trade Code"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'tradeCode', 'errors')}">
                    <g:textField name="tradeCode" maxlength="20" value="${ftLiquidateInstance?.tradeCode}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="preFee"><g:message code="ftLiquidate.preFee.label" default="Pre Fee"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'preFee', 'errors')}">
                    <g:textField name="preFee" value="${fieldValue(bean: ftLiquidateInstance, field: 'preFee')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="postFee"><g:message code="ftLiquidate.postFee.label" default="Post Fee"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'postFee', 'errors')}">
                    <g:textField name="postFee" value="${fieldValue(bean: ftLiquidateInstance, field: 'postFee')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="customerNo"><g:message code="ftLiquidate.customerNo.label"
                                                       default="Customer No"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'customerNo', 'errors')}">
                    <g:textField name="customerNo" maxlength="24" value="${ftLiquidateInstance?.customerNo}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="liqDate"><g:message code="ftLiquidate.liqDate.label" default="Liq Date"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'liqDate', 'errors')}">
                    <g:datePicker name="liqDate" precision="day" value="${ftLiquidateInstance?.liqDate}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="liquidateNo"><g:message code="ftLiquidate.liquidateNo.label"
                                                        default="Liquidate No"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'liquidateNo', 'errors')}">
                    <g:textField name="liquidateNo" maxlength="24" value="${ftLiquidateInstance?.liquidateNo}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="footNo"><g:message code="ftLiquidate.footNo.label" default="Foot No"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'footNo', 'errors')}">
                    <g:textField name="footNo" maxlength="30" value="${ftLiquidateInstance?.footNo}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="status"><g:message code="ftLiquidate.status.label" default="Status"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'status', 'errors')}">
                    <g:textField name="status" value="${fieldValue(bean: ftLiquidateInstance, field: 'status')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="postFootNo"><g:message code="ftLiquidate.postFootNo.label"
                                                       default="Post Foot No"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'postFootNo', 'errors')}">
                    <g:textField name="postFootNo" maxlength="30" value="${ftLiquidateInstance?.postFootNo}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="postFootStatus"><g:message code="ftLiquidate.postFootStatus.label"
                                                           default="Post Foot Status"/></label>
                </td>
                <td valign="top"
                    class="value ${hasErrors(bean: ftLiquidateInstance, field: 'postFootStatus', 'errors')}">
                    <g:textField name="postFootStatus"
                                 value="${fieldValue(bean: ftLiquidateInstance, field: 'postFootStatus')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="settleType"><g:message code="ftLiquidate.settleType.label"
                                                       default="Settle Type"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'settleType', 'errors')}">
                    <g:textField name="settleType"
                                 value="${fieldValue(bean: ftLiquidateInstance, field: 'settleType')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="redoTime"><g:message code="ftLiquidate.redoTime.label" default="Redo Time"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'redoTime', 'errors')}">
                    <g:datePicker name="redoTime" precision="day" value="${ftLiquidateInstance?.redoTime}"
                                  default="none" noSelection="['': '']"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="freezeAmount"><g:message code="ftLiquidate.freezeAmount.label"
                                                         default="Freeze Amount"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'freezeAmount', 'errors')}">
                    <g:textField name="freezeAmount"
                                 value="${fieldValue(bean: ftLiquidateInstance, field: 'freezeAmount')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="channelCode"><g:message code="ftLiquidate.channelCode.label"
                                                        default="Channel Code"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'channelCode', 'errors')}">
                    <g:textField name="channelCode" maxlength="20" value="${ftLiquidateInstance?.channelCode}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="amount"><g:message code="ftLiquidate.amount.label" default="Amount"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'amount', 'errors')}">
                    <g:textField name="amount" value="${fieldValue(bean: ftLiquidateInstance, field: 'amount')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="feeType"><g:message code="ftLiquidate.feeType.label" default="Fee Type"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'feeType', 'errors')}">
                    <g:textField name="feeType" value="${fieldValue(bean: ftLiquidateInstance, field: 'feeType')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="liqAccNo"><g:message code="ftLiquidate.liqAccNo.label" default="Liq Acc No"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'liqAccNo', 'errors')}">
                    <g:textField name="liqAccNo" value="${ftLiquidateInstance?.liqAccNo}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="redo"><g:message code="ftLiquidate.redo.label" default="Redo"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'redo', 'errors')}">
                    <g:checkBox name="redo" value="${ftLiquidateInstance?.redo}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="transNum"><g:message code="ftLiquidate.transNum.label" default="Trans Num"/></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: ftLiquidateInstance, field: 'transNum', 'errors')}">
                    <g:textField name="transNum" value="${fieldValue(bean: ftLiquidateInstance, field: 'transNum')}"/>
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
