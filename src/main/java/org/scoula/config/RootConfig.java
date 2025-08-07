package org.scoula.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;


@Slf4j
@Configuration
@PropertySource("classpath:application.properties")
@MapperScan(basePackages = {
        "org.scoula.user.mapper",
        "org.scoula.finance.mapper",
        "org.scoula.transactions.mapper",
        "org.scoula.survey.mapper",
        "org.scoula.quiz.mapper",
        "org.scoula.dictionary.mapper",
        "org.scoula.bubble.mapper",
        "org.scoula.news.mapper",
        "org.scoula.challenge.mapper",
        "org.scoula.account.mapper",
        "org.scoula.challenge.mapper",
        "org.scoula.avatar.mapper",
        "org.scoula.card.mapper",
        "org.scoula.alarm.mapper",
        "org.scoula.monthreport.mapper",
        "org.scoula.coin.mapper",
        "org.scoula.agree.mapper",
        "org.scoula.challenge.rank.mapper"
})
@ComponentScan(basePackages = {
        "org.scoula.security",
        "org.scoula.user.service",
        "org.scoula.common.redis",
        "org.scoula.common.*", // 공통 유틸이나 예외 추가할 여지
        "org.scoula.finance.controller",
        "org.scoula.finance.service",
        "org.scoula.finance.util",
        "org.scoula.transactions.service",
        "org.scoula.survey.service",
        "org.scoula.quiz.service",
        "org.scoula.quiz.exception",
        "org.scoula.dictionary.service",
        "org.scoula.bubble.service",
        "org.scoula.news.service",
        "org.scoula.nhapi.service",
        "org.scoula.nhapi.client",
        "org.scoula.nhapi.parser",
        "org.scoula.challenge.service",
        "org.scoula.challenge.scheduler",
        "org.scoula.user.util",
        "org.scoula.avatar.service",
        "org.scoula.account.service",
        "org.scoula.alarm.service",
        "org.scoula.card.service",
        "org.scoula.monthreport.service",
        "org.scoula.monthreport.scheduler",
        "org.scoula.monthreport.util",
        "org.scoula.agree.service",
        "org.scoula.challenge.rank.scheduler",
        "org.scoula.challenge.rank.service",
        "org.scoula.challenge.rank.util",
        "org.scoula.common.aop",
        "org.scoula.summary.service",
        })
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class RootConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Value("${jdbc.driver}")
    private String jdbcDriver;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${jdbc.username}")
    private String jdbcUsername;

    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Bean
    public DataSource dataSource() {
        log.info("========== DB 연결 테스트 ================");
        log.info("driver: {}", jdbcDriver);
        log.info("url: {}", jdbcUrl);
        log.info("username: {}", jdbcUsername);
        log.info("password: {}", jdbcPassword);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(jdbcDriver);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUsername);
        config.setPassword(jdbcPassword);

        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());

        return sqlSessionFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

}
