<%@ page import="settle.FtTrade" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftTrade.label', default: 'FtTrade')}"/>
    <title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                           args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.create.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:form action="add">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="srvCode"><g:message code="ftTrade.srvCode.label" default="Srv Code"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="srvCode" maxlength="20" value="${params.srvCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="tradeCode"><g:message code="ftTrade.tradeCode.label" default="Trade Code"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="tradeCode" maxlength="20" value="${params.tradeCode}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="customerNo"><g:message code="ftTrade.customerNo.label"
                                                           default="Customer No"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="customerNo" maxlength="24" value="${params.customerNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="seqNo"><g:message code="ftTrade.seqNo.label" default="Seq No"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="seqNo" maxlength="24" value="${params.seqNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="channel"><g:message code="ftTrade.channel.label"
                                                            default="Channel"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="channel" maxlength="24" value="${params.channel}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="amount"><g:message code="ftTrade.amount.label" default="Amount"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="amount" maxlength="24" value="${params.amount}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="tradeDate"><g:message code="ftTrade.tradeDate.label" default="Trade Date"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="tradeDate" maxlength="24" value="${params.tradeDate}"/>
                    </td>
                </tr>


                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="billDate"><g:message code="ftTrade.billDate.label" default="Bill Date"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="billDate" maxlength="24" value="${params.billDate}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:submitButton name="create" class="save"
                                                 value="${message(code: 'default.button.create.label', default: 'Create')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
