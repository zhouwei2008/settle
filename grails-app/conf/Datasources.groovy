import org.codehaus.groovy.grails.commons.ConfigurationHolder

datasources = {
    datasource(name: 'ismp') {
        domainClasses([
                ismp.CmCorporationInfo,
                ismp.CmCustomer,
                ismp.CmCustomerBankAccount,
                ismp.CmPersonalInfo,
                ismp.CmSeqCustno,
                ismp.TradeBase,
                ismp.TradeWithdrawn
        ])
        driverClassName('oracle.jdbc.OracleDriver')
        url(ConfigurationHolder.config.dataSource.ismp.url)
        username(ConfigurationHolder.config.dataSource.ismp.username)
        password(DESCodec.decode(ConfigurationHolder.config.dataSource.ismp.password))
        dbCreate(ConfigurationHolder.config.dataSource.ismp.dbCreate)
        pooled(true)
        logSql(false)
        dialect(org.hibernate.dialect.Oracle10gDialect)
        hibernate {
            cache {
                use_second_level_cache(false)
                use_query_cache(false)
                provider_class('net.sf.ehcache.hibernate.EhCacheProvider')
            }
        }
    }

    datasource(name: 'boss') {
        domainClasses([
                boss.BoBankDic,
                boss.BoInnerAccount,
                boss.BoCustomerService,
                boss.BoAgentPayServiceParams,
                boss.BoCustomerWithdrawCycle
        ])
        driverClassName('oracle.jdbc.OracleDriver')
        url(ConfigurationHolder.config.dataSource.boss.url)
        username(ConfigurationHolder.config.dataSource.boss.username)
        password(DESCodec.decode(ConfigurationHolder.config.dataSource.boss.password))
        dbCreate(ConfigurationHolder.config.dataSource.boss.dbCreate)
        pooled(true)
        logSql(false)
        dialect(org.hibernate.dialect.Oracle10gDialect)
        hibernate {
            cache {
                use_second_level_cache(false)
                use_query_cache(false)
                provider_class('net.sf.ehcache.hibernate.EhCacheProvider')
            }
        }
    }

    datasource(name: 'settle') {
        domainClasses([
                settle.FtFoot,
                settle.FtLiquidate,
                settle.FtSrvFootSetting,
                settle.FtSrvTradeType,
                settle.FtSrvType,
                settle.FtTrade,
                settle.FtTradeFee
        ])
        services([
                'liquidate',
                'message',
                'settle'
        ])
        driverClassName('oracle.jdbc.OracleDriver')
        url(ConfigurationHolder.config.dataSource.settle.url)
        username(ConfigurationHolder.config.dataSource.settle.username)
        password(DESCodec.decode(ConfigurationHolder.config.dataSource.settle.password))
        dbCreate(ConfigurationHolder.config.dataSource.settle.dbCreate)
        pooled(true)
        logSql(false)
        dialect(org.hibernate.dialect.Oracle10gDialect)
        hibernate {
            cache {
                use_second_level_cache(false)
                use_query_cache(false)
                provider_class('net.sf.ehcache.hibernate.EhCacheProvider')
            }
        }
    }
    datasource(name: 'account') {
        domainClasses([
                account.AcAccount
        ])
        driverClassName('oracle.jdbc.OracleDriver')
        url(ConfigurationHolder.config.dataSource.account.url)
        username(ConfigurationHolder.config.dataSource.account.username)
        password(DESCodec.decode(ConfigurationHolder.config.dataSource.account.password))
        dbCreate(ConfigurationHolder.config.dataSource.account.dbCreate)
        pooled(true)
        logSql(false)
        dialect(org.hibernate.dialect.Oracle10gDialect)
        hibernate {
            cache {
                use_second_level_cache(false)
                use_query_cache(false)
                provider_class('net.sf.ehcache.hibernate.EhCacheProvider')
            }
        }
    }
}
