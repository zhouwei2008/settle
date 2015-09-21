import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.connection.SingleConnectionFactory
import org.codehaus.groovy.grails.commons.ConfigurationHolder

// Place your Spring DSL code here
beans = {
  jmsConnectionFactory(org.apache.activemq.ActiveMQConnectionFactory) {
    brokerURL = ConfigurationHolder.config.jms.url.toString()
    //'tcp://10.68.76.18:61616'
  }
//  jmsConnectionFactory(SingleConnectionFactory) {
//      targetConnectionFactory = { ActiveMQConnectionFactory cf ->
//          brokerURL = 'tcp://10.68.76.18:61616'
//      }
//  }

}
