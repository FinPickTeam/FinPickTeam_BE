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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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
        "org.scoula.news.mapper"
})
@ComponentScan(basePackages = {
        "org.scoula.security",
        "org.scoula.user.service",
        "org.scoula.common.redis",
        "org.scoula.common.*", // 공통 유틸이나 예외 추가할 여지
        "org.scoula.finance.controller",
        "org.scoula.finance.service",
        "org.scoula.transactions.service",
        "org.scoula.transactions.util",
        "org.scoula.transactions.exception",
        "org.scoula.survey.service",
        "org.scoula.quiz.service",
        "org.scoula.quiz.exception",
        "org.scoula.dictionary.service",
        "org.scoula.bubble.service",
        "org.scoula.news.service"
})
@EnableTransactionManagement
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

    /*
    static {
        Dotenv d = Dotenv.configure()
                .filename(".env")
                .load();
        d.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
    */
}
