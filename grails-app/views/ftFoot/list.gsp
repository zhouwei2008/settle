<%@ page import="settle.FtFoot" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftFoot.label', default: 'FtFoot')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="${message(code: 'ftFoot.id.label', default: 'Id')}"/>

                <g:sortableColumn property="srvCode"
                                  title="${message(code: 'ftFoot.srvCode.label', default: 'Srv Code')}"/>

                <g:sortableColumn property="tradeCode"
                                  title="${message(code: 'ftFoot.tradeCode.label', default: 'Trade Code')}"/>

                <g:sortableColumn property="customerNo"
                                  title="${message(code: 'ftFoot.customerNo.label', default: 'Customer No')}"/>

                <g:sortableColumn property="type" title="${message(code: 'ftFoot.type.label', default: 'Type')}"/>

                <g:sortableColumn property="footNo"
                                  title="${message(code: 'ftFoot.footNo.label', default: 'Foot No')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${ftFootInstanceList}" status="i" var="ftFootInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${ftFootInstance.id}">${fieldValue(bean: ftFootInstance, field: "id")}</g:link></td>

                    <td>${fieldValue(bean: ftFootInstance, field: "srvCode")}</td>

                    <td>${fieldValue(bean: ftFootInstance, field: "tradeCode")}</td>

                    <td>${fieldValue(bean: ftFootInstance, field: "customerNo")}</td>

                    <td>${fieldValue(bean: ftFootInstance, field: "type")}</td>

                    <td>${fieldValue(bean: ftFootInstance, field: "footNo")}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${ftFootInstanceTotal}"/>
    </div>
</div>
</body>
</html>
