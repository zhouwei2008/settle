<%@ page import="settle.FtLiquidate" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'ftLiquidate.label', default: 'FtLiquidate')}"/>
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
                        <label for="customerNo"><g:message code="ftLiquidate.customerNo.label"
                                                           default="Customer No"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="customerNo" maxlength="24" value="${params.customerNo}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="liquidateNo"><g:message code="ftLiquidate.liquidateNo.label"
                                                            default="Liquidate No"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField name="liquidateNo" maxlength="24" value="${params.liquidateNo}"/>
                    </td>
               </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="startDateCreated"><g:message code="ftLiquidate.liqDate.label" default="liq Date"/></label>
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
                                  title="${message(code: 'ftLiquidate.customerNo.label', default: 'Customer No')}"/>

                <g:sortableColumn property="srvCode"
                                  title="${message(code: 'ftLiquidate.srvCode.label', default: 'Srv Code')}"/>

                <g:sortableColumn property="tradeCode"
                                  title="${message(code: 'ftLiquidate.tradeCode.label', default: 'Trade Code')}"/>

                <g:sortableColumn property="transNum"
                                  title="${message(code: 'ftLiquidate.transNum.label', default: 'trans Num')}"/>

                <g:sortableColumn property="amount" title="${message(code: 'ftLiquidate.amount.label', default: 'amount')}"/>

                <g:sortableColumn property="preFee"
                                  title="${message(code: 'ftLiquidate.preFee.label', default: 'Pre Fee')}"/>

                <g:sortableColumn property="postFee"
                                  title="${message(code: 'ftLiquidate.postFee.label', default: 'Post Fee')}"/>

                <g:sortableColumn property="settleType" title="${message(code: 'ftLiquidate.settleType.label', default: ' Settle Type')}"/>


                <g:sortableColumn property="liquidateNo" title="${message(code: 'ftLiquidate.liquidateNo.label', default: 'liquidate No')}"/>

                <g:sortableColumn property="liqDate"
                                  title="${message(code: 'ftLiquidate.liqDate.label', default: 'liq Date')}"/>



            </tr>
            </thead>
            <tbody>
            <g:each in="${ftLiquidateInstanceList}" status="i" var="ftLiquidateInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td>${fieldValue(bean: ftLiquidateInstance, field: "customerNo")}</td>

                    <td>${fieldValue(bean: ftLiquidateInstance, field: "srvCode")}</td>

                    <td>${fieldValue(bean: ftLiquidateInstance, field: "tradeCode")}</td>

                    <td>${fieldValue(bean: ftLiquidateInstance, field: "transNum")}</td>

                    <td><g:formatNumber number="${ftLiquidateInstance.amount?ftLiquidateInstance.amount:0.00}" format="#.##"/></td>
                    <td><g:formatNumber number="${ftLiquidateInstance.preFee?ftLiquidateInstance.preFee:0.00}" format="#.##"/></td>
                    <td><g:formatNumber number="${ftLiquidateInstance.postFee?ftLiquidateInstance.postFee:0.00}" format="#.##"/></td>

                    <td>${ftLiquidateInstance.settleType==0?'非实时':'实时'}</td>

                    <td>
                        <a href="${createLink(controller: 'ftTrade', action: 'index',params:['liqNo':ftLiquidateInstance.liquidateNo])}">
                     ${fieldValue(bean: ftLiquidateInstance, field: "liquidateNo")}
                    </a></td>

                    <td><g:formatDate date="${ftLiquidateInstance.liqDate}" format="yyyy-MM-dd"/></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${ftLiquidateInstanceTotal}" params="${params}"/>
    </div>
</div>
</body>
</html>
