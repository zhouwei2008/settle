package account

import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.POST

/**
 * 账务系统调用客户端
 * 配置：
 * 1. 安装grails插件rest: grails InstallPlugin rest
 * 2. 在Config.groovy中增加一行
 * account.serverUrl = "http://www.testpay.org:8111/Account/"
 * 错误码表：
 * 00 - 正常
 * ff - 系统未知错误
 * a1 - 交易重复  a2 - 参数错误
 * 01 - 账户不存在， 02 - 账户状态不正常， 03 - 账户余额不足， 04 - 账户不支持冻结
 */
class AccountClientService {

  static transactional = true

  /**
   * 开户调用
   * @param accName 账户名,必须参数
   * @param direction 账户方向，debit 为借记账户，credit 为贷记账户,必须参数
   * @return {result: 'true or false',errorCode:'', errorMsg: '', accountNo: ''}
   * result: true为成功， false 为失败,
   * errorCode: 当result为false时，返回误号
   * errorMsg: 当result为false时，返回误原因,
   * accountNo: 当result为true时，返回账户账号
   * @throws Exception
   */
  def openAcc(accName, direction) throws Exception {
    def http = new HTTPBuilder(ConfigurationHolder.config.account.serverUrl)
    http.request(POST, JSON) { req ->
      uri.path = 'rpc/openAcc'
      body = [accountName: accName, direction: direction]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp ->
        throw new Exception('request error')
      }
    }
  }

  /**
   * 冻结账户调用
   * @param accNo 账户账号,必须参数
   * @return {result: 'true or false', errorCode: '', errorMsg: ''}
   * result: true为成功， false 为失败,
   * errorCode: 当result为false时，返回误号
   * errorMsg: 当result为false时，返回误原因,
   * @throws Exception
   */
  def freezeAcc(accNo)  throws Exception {
    def http = new HTTPBuilder(ConfigurationHolder.config.account.serverUrl)
    http.request(POST, JSON) { req ->
      uri.path = 'rpc/freezeAcc'
      body = [accountNo: accNo]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp ->
        throw new Exception('request error')
      }
    }
  }

  /**
   * 解冻冻账户调用
   * @param accNo 账户账号,必须参数
   * @return {result: 'true or false', errorCode: '', errorMsg: ''}
   * result: true为成功， false 为失败,
   * errorCode: 当result为false时，返回误号
   * errorMsg: 当result为false时，返回误原因,
   * @throws Exception
   */
  def unfreezeAcc(accNo)  throws Exception {
    def http = new HTTPBuilder(ConfigurationHolder.config.account.serverUrl)
    http.request(POST, JSON) { req ->
      uri.path = 'rpc/unfreezeAcc'
      body = [accountNo: accNo]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp ->
        throw new Exception('request error')
      }
    }
  }
  
  /**
   * 关闭账户调用
   * @param accNo 账户账号,必须参数
   * @return {result: 'true or false', errorCode: '', errorMsg: ''}
   * result: true为成功， false 为失败,
   * errorCode: 当result为false时，返回误号
   * errorMsg: 当result为false时，返回误原因,
   * @throws Exception
   */
  def closeAcc(accNo)  throws Exception {
    def http = new HTTPBuilder(ConfigurationHolder.config.account.serverUrl)
    http.request(POST, JSON) { req ->
      uri.path = 'rpc/closeAcc'
      body = [accountNo: accNo]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp ->
        throw new Exception('request error')
      }
    }
  }
       /**
   * 查询帐户余额调用
   * @param accNo 账户账号,必须参数
   * @return {result: 'true or false', errorCode: '', errorMsg: '', accName: '', balance: '', freezBal:'', direc:'debit or credit', status:'norm or freeze or closed'}
   * result: true为成功， false 为失败,
   * errorCode: 当result为false时，返回误号
   * errorMsg: 当result为false时，返回误原因,
   * @throws Exception
   */
  def queryAcc(accNo)  throws Exception {
    def http = new HTTPBuilder(ConfigurationHolder.config.account.serverUrl)
    http.request(POST, JSON) { req ->
      uri.path = 'rpc/queryAcc'
      body = [accountNo: accNo]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp ->
        throw new Exception('request error')
      }
    }
  }

  /**
   * 交易指令集调用
   * @param commandSeqno 外部指令序号，不可重复，最好用uuid生成
   * @param commandList : 目前支持3种指令：
   *  {commandType:'transfer', fromAccountNo:'', toAccountNo:'', amount:'', transferType:'', tradeNo:'', outTradeNo:'', subjict:''},
   *  {commandType:'freeze', fromAccountNo:'', amount:'', transferType:'', tradeNo:'', outTradeNo:'', subjict:''},
   *  {commandType:'unfreeze', fromAccountNo:'', amount:'', transferType:'', tradeNo:'', outTradeNo:'', subjict:''},
   *  可用build方法创建
   * @return {result: 'true or false', transCode:'', transIds:['id1', 'id2',...] errorCode:'', errorMsg: ''}
   * result: true为成功， false 为失败,
   * transCode: 账务事务凭证号
   * transIds: 账户指令id集合
   * errorCode: 当result为false时，返回误号
   * errorMsg: 当result为false时，返回误原因,
   * @throws Exception
   */
  def batchCommand(commandSeqno, commandList) throws Exception {
    def http = new HTTPBuilder(ConfigurationHolder.config.account.serverUrl)
    http.request(POST, JSON) { req ->
      uri.path = 'rpc/batchCommand'
      body = [commandSeqno: commandSeqno,
              commandLs: commandList
      ]
      response.success = { resp, json ->
        return json
      }
      response.failure = { resp ->
        throw new Exception('request error')
      }
    }
  }

  /**
   * 创建转账交易指令，并插入到commandList后面
   * @param commandList 指令集合list，如果为null则会自动创建一个list返回
   * @param fromAccountNo 发起账户号
   * @param toAccountNo 转到账户号
   * @param amount 金额，必须大于0
   * @param transferType 交易类型，系统自由定义
   * @param tradeNo 交易流水号，外部系统交易流水号
   * @param outTradeNo 商户订单号
   * @return 包含本指令的指令集合
   * @throws Exception
   */
  def buildTransfer(commandList, fromAccountNo, toAccountNo, amount, transferType, tradeNo, outTradeNo, subjict) throws Exception {
    if (!fromAccountNo || !toAccountNo || amount==null || !amount.class == Long || !transferType || !tradeNo || !outTradeNo) {
       throw new Exception("参数错误:fromAccountNo:${fromAccountNo},toAccountNo:${toAccountNo},amount:${amount},transferType:${transferType},tradeNo:${tradeNo},outTradeNo:${outTradeNo}")
    }
    if (!commandList) {
      commandList = []
    }
    if (amount != 0) {
      commandList.add([commandType: 'transfer', fromAccountNo: fromAccountNo, toAccountNo: toAccountNo, amount: amount, transferType: transferType, tradeNo: tradeNo, outTradeNo: outTradeNo, subjict: subjict])
    }
    return commandList
  }

  /**
   * 创建冻结交易指令，并插入到commandList后面
   * @param accountNo 冻结金额账户号
   * @param commandList 指令集合list，如果为null则会自动创建一个list返回
   * @param amount 金额，必须大于0
   * @param transferType 交易类型，系统自由定义
   * @param tradeNo 交易流水号，外部系统交易流水号
   * @param outTradeNo 商户订单号
   * @return 包含本指令的指令集合
   * @throws Exception
   */
  def buildFreeze(commandList, accountNo, amount, transferType, tradeNo, outTradeNo, subjict) throws Exception {
    if (!accountNo || !amount || !amount.class == Long || !transferType || !tradeNo || !outTradeNo) {
       throw new Exception("参数错误:accountNo:${accountNo},amount:${amount},transferType:${transferType},tradeNo:${tradeNo},outTradeNo:${outTradeNo}")
    }
    if (!commandList) {
      commandList = []
    }
    commandList.add([commandType: 'freeze', fromAccountNo: accountNo, amount: amount, transferType: transferType, tradeNo: tradeNo, outTradeNo: outTradeNo, subjict: subjict])
    return commandList
  }

  /**
   * 创建解冻冻交易指令，并插入到commandList后面
   * @param commandList 指令集合list，如果为null则会自动创建一个list返回
   * @param accountNo 解冻金额账户号
   * @param amount 金额，必须大于0
   * @param transferType 交易类型，系统自由定义
   * @param tradeNo 交易流水号，外部系统交易流水号
   * @param outTradeNo 商户订单号
   * @return 包含本指令的指令集合
   * @throws Exception
   */
  def buildUnfreeze(commandList, accountNo, amount, transferType, tradeNo, outTradeNo, subjict) throws Exception {
    if (!accountNo || !amount || !amount.class == Long || !transferType || !tradeNo || !outTradeNo) {
       throw new Exception("参数错误:accountNo:${accountNo},amount:${amount},transferType:${transferType},tradeNo:${tradeNo},outTradeNo:${outTradeNo}")
    }
    if (!commandList) {
      commandList = []
    }
    commandList.add([commandType: 'unfreeze', fromAccountNo: accountNo, amount: amount, transferType: transferType, tradeNo: tradeNo, outTradeNo: outTradeNo, subjict: subjict])
    return commandList
  }
}
