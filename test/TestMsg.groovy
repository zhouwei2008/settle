import javax.jms.DeliveryMode;


import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory

import javax.jms.Connection
import javax.jms.Session

ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, 'tcp://10.68.76.18:61616');
Connection connection = connectionFactory.createConnection();
connection.start();
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
javax.jms.MessageProducer producer = session.createProducer(session.createQueue('settle'));


def startTime
def endTime

startTime = System.currentTimeMillis()

//for (i in 1..2000) {
  javax.jms.MapMessage message = session.createMapMessage();
  message.setString('srvCode', 'online')
  message.setString('tradeCode', 'pay')
  message.setString('customerNo', '100000000000081')
  message.setInt('amount', 16)
  message.setString('seqNo', UUID.randomUUID().toString().replaceAll('-', '').substring(0, 23))
  message.setString('tradeDate', new Date().format('yyyy-MM-dd HH:mm:ss.SSS'))
  message.setString('billDate', new Date().format('yyyy-MM-dd HH:mm:ss.SSS'))
  producer.send(message);
//}

endTime = System.currentTimeMillis()
println "time ms = ${endTime - startTime}"

connection.close();

