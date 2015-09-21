<%@ page import="settle.FtTrade" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftTrade.label', default: 'FtTrade')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<script type="text/javascript">
    $(function() {
        var dates = $( "#startDateCreated, #endDateCreated" ).datepicker({
			dateFormat: 'yy-mm-dd',
            changeYear: true,
			changeMonth: true
		});
    });
    function checkDate() {
        if (!document.getElementById('endDateCreated').value.length == 0) {
            var startDateCreated = document.getElementById('startDateCreated').value;
            var endDateCreated = document.getElementById('endDateCreated').value;
            if (Number(startDateCreated > endDateCreated)) {
                alert('开始时间不能大于结束时间!');
                document.getElementById('endDateCreated').focus();
                return false;
            }
        }
    }

</script>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
    </span>
    <span class="menuButton"><g:link class="create" action="msg"><g:message code="default.new.label"
                                                                               args="[entityName]"/></g:link></span>
</div>

<div class="body">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
        <g:form action="list">
        <div class="dialog">
            <table>
                <tbody>

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
                        <label for="liquidateNo"><g:message code="ftTrade.liquidateNo.label"
                                                            default="Liquidate No"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="liquidateNo" maxlength="24" value="${params.liquidateNo}"/>
                    </td>
               </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="startDateCreated"><g:message code="ftTrade.billDate.label" default="bill Date"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="startDateCreated" id='startDateCreated' onchange="checkDate()" onblur="value=value.replace(/[ ]/g,'')" maxlength="24" value="${params.startDateCreated}" />--<g:textField name="endDateCreated"  id='endDateCreated' onblur="value=value.replace(/[ ]/g,'')" maxlength="24" value="${params.endDateCreated}" onchange="checkDate()" />

                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><g:submitButton name="create" class="save"
                                                 value="查询"/></span>
        </div>
    </g:form>
    <div class="list">
        <table>
            <thead>
            <tr>
                <g:sortableColumn property="customerNo"
                                  title="${message(code: 'ftTrade.customerNo.label', default: 'Customer No')}"/>
                <g:sortableColumn property="srvCode"
                                  title="${message(code: 'ftTrade.srvCode.label', default: 'Srv Code')}"/>

                <g:sortableColumn property="tradeCode"
                                  title="${message(code: 'ftTrade.tradeCode.label', default: 'Trade Code')}"/>



                <g:sortableColumn property="seqNo" title="${message(code: 'ftTrade.seqNo.label', default: 'Seq No')}"/>

                <g:sortableColumn property="channelCode" title="${message(code: 'ftTrade.channelCode.label', default: 'channel Code ')}"/>

                <g:sortableColumn property="amount" title="${message(code: 'ftTrade.amount.label', default: 'amount')}"/>
               <g:sortableColumn property="preFee" title="${message(code: 'ftTrade.preFee.label', default: 'preFee')}"/>
                <g:sortableColumn property="postFee" title="${message(code: 'ftTrade.postFee.label', default: 'postFee')}"/>

                <g:sortableColumn property="realtimeSettle" title="${message(code: 'ftTrade.realtimeSettle.label', default: 'realtime Settle')}"/>

                <g:sortableColumn property="liquidateNo" title="${message(code: 'ftTrade.liquidateNo.label', default: 'liquidate No')}"/>

                 <g:sortableColumn property="billDate"
                                  title="${message(code: 'ftTrade.billDate.label', default: 'Bill Date')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${ftTradeInstanceList}" status="i" var="ftTradeInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td>${fieldValue(bean: ftTradeInstance, field: "customerNo")}</td>

                    <td>${fieldValue(bean: ftTradeInstance, field: "srvCode")}</td>

                    <td>${fieldValue(bean: ftTradeInstance, field: "tradeCode")}</td>

                    <td>${fieldValue(bean: ftTradeInstance, field: "seqNo")}</td>
                    <td>${fieldValue(bean: ftTradeInstance, field: "channelCode")}</td>

                     <td><g:formatNumber number="${ftTradeInstance.amount?ftTradeInstance.amount:0.00}" format="#.##"/></td>

                    <td><g:formatNumber number="${ftTradeInstance.preFee?ftTradeInstance.preFee:0.00}" format="#.##"/></td>
                    <td><g:formatNumber number="${ftTradeInstance.postFee?ftTradeInstance.postFee:0.00}" format="#.##"/></td>

                     <td>${ftTradeInstance.realtimeSettle==0?'非实时':'实时'}</td>

                     <td>${fieldValue(bean: ftTradeInstance, field: "liquidateNo")}</td>

                    <td><g:formatDate date="${ftTradeInstance.billDate}" format="yyyy-MM-dd HH:mm:ss.SSS"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${ftTradeInstanceTotal}" params="${params}"/>
    </div>

    <g:if test="${backflag}">
        <div class="buttons">
            <span class="button"><input type="button" class="save back_btn" value="返回"/> </span>
        </div>
    </g:if>
</div>
</body>
</html>
