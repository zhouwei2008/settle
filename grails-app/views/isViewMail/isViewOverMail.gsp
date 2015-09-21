<%--
  Created by IntelliJ IDEA.
  User: zhaoshuang
  Date: 12-12-12
  Time: 上午10:09
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="ismp.CmCustomer" %>
<%@ page import="boss.BoCustomerService" %>
<html>
  <head><title>包流量费率超量提示</title></head>
  <body>
        您好，${CmCustomer.findByCustomerNo(item.customerNo)?.name}商户的${srvName}（服务类型）包流量费率已超量。

  <p style="padding-left:25px">系统自动发送邮件，请不要回复，谢谢。</p>
  <p style="padding-left:825px">天津荣程网络科技有限公司</p>
  </body>
</html>