<%@ page import="settle.FtFoot" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftFoot.label', default: 'FtFoot')}"/>
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
    <g:hasErrors bean="${ftFootInstance}">
        <div class="errors">
            <g:renderErrors bean="${ftFootInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <g:hiddenField name="id" value="${ftFootInstance?.id}"/>
        <g:hiddenField name="version" value="${ftFootInstance?.version}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="srvCode"><g:message code="ftFoot.srvCode.label" default="Srv Code"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'srvCode', 'errors')}">
                        <g:textField name="srvCode" maxlength="20" value="${ftFootInstance?.srvCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="tradeCode"><g:message code="ftFoot.tradeCode.label" default="Trade Code"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'tradeCode', 'errors')}">
                        <g:textField name="tradeCode" maxlength="20" value="${ftFootInstance?.tradeCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="customerNo"><g:message code="ftFoot.customerNo.label"
                                                           default="Customer No"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'customerNo', 'errors')}">
                        <g:textField name="customerNo" maxlength="24" value="${ftFootInstance?.customerNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="type"><g:message code="ftFoot.type.label" default="Type"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'type', 'errors')}">
                        <g:textField name="type" value="${fieldValue(bean: ftFootInstance, field: 'type')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="footNo"><g:message code="ftFoot.footNo.label" default="Foot No"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'footNo', 'errors')}">
                        <g:textField name="footNo" maxlength="30" value="${ftFootInstance?.footNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="checkStatus"><g:message code="ftFoot.checkStatus.label"
                                                            default="Check Status"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'checkStatus', 'errors')}">
                        <g:textField name="checkStatus"
                                     value="${fieldValue(bean: ftFootInstance, field: 'checkStatus')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="checkOpId"><g:message code="ftFoot.checkOpId.label" default="Check Op Id"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'checkOpId', 'errors')}">
                        <g:textField name="checkOpId" value="${fieldValue(bean: ftFootInstance, field: 'checkOpId')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="checkDate"><g:message code="ftFoot.checkDate.label" default="Check Date"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'checkDate', 'errors')}">
                        <g:datePicker name="checkDate" precision="day" value="${ftFootInstance?.checkDate}"
                                      default="none" noSelection="['': '']"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="createOpId"><g:message code="ftFoot.createOpId.label"
                                                           default="Create Op Id"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'createOpId', 'errors')}">
                        <g:textField name="createOpId"
                                     value="${fieldValue(bean: ftFootInstance, field: 'createOpId')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="freezeAmount"><g:message code="ftFoot.freezeAmount.label"
                                                             default="Freeze Amount"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'freezeAmount', 'errors')}">
                        <g:textField name="freezeAmount"
                                     value="${fieldValue(bean: ftFootInstance, field: 'freezeAmount')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="autoCheck"><g:message code="ftFoot.autoCheck.label" default="Auto Check"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'autoCheck', 'errors')}">
                        <g:textField name="autoCheck" value="${fieldValue(bean: ftFootInstance, field: 'autoCheck')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="autoWithdraw"><g:message code="ftFoot.autoWithdraw.label"
                                                             default="Auto Withdraw"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'autoWithdraw', 'errors')}">
                        <g:textField name="autoWithdraw"
                                     value="${fieldValue(bean: ftFootInstance, field: 'autoWithdraw')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="withdraw"><g:message code="ftFoot.withdraw.label" default="Withdraw"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'withdraw', 'errors')}">
                        <g:textField name="withdraw" value="${fieldValue(bean: ftFootInstance, field: 'withdraw')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="amount"><g:message code="ftFoot.amount.label" default="Amount"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'amount', 'errors')}">
                        <g:textField name="amount" value="${fieldValue(bean: ftFootInstance, field: 'amount')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="feeType"><g:message code="ftFoot.feeType.label" default="Fee Type"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'feeType', 'errors')}">
                        <g:textField name="feeType" value="${fieldValue(bean: ftFootInstance, field: 'feeType')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="footDate"><g:message code="ftFoot.footDate.label" default="Foot Date"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'footDate', 'errors')}">
                        <g:datePicker name="footDate" precision="day" value="${ftFootInstance?.footDate}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="postFee"><g:message code="ftFoot.postFee.label" default="Post Fee"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'postFee', 'errors')}">
                        <g:textField name="postFee" value="${fieldValue(bean: ftFootInstance, field: 'postFee')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="preFee"><g:message code="ftFoot.preFee.label" default="Pre Fee"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'preFee', 'errors')}">
                        <g:textField name="preFee" value="${fieldValue(bean: ftFootInstance, field: 'preFee')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="transNum"><g:message code="ftFoot.transNum.label" default="Trans Num"/></label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: ftFootInstance, field: 'transNum', 'errors')}">
                        <g:textField name="transNum" value="${fieldValue(bean: ftFootInstance, field: 'transNum')}"/>
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
