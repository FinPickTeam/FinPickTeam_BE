package org.scoula.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.scoula.security.filter.AuthenticationErrorFilter;
import org.scoula.security.filter.JwtAuthenticationFilter;
import org.scoula.security.filter.JwtEmailPasswordAuthenticationFilter;
import org.scoula.security.handler.CustomAccessDeniedHandler;
import org.scoula.security.handler.CustomAuthenticationEntryPoint;
import org.scoula.security.handler.LoginFailureHandler;
import org.scoula.security.handler.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableScheduling // (2) 스케줄링 활성화
@Slf4j
// (3) 매퍼 스캔 경로 확장
@MapperScan(basePackages = {
        "org.scoula.security.account.mapper",
        "org.scoula.coin.mapper",
        "org.scoula.challenge.rank.mapper"
})
@ComponentScan(basePackages = {"org.scoula.security"})
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationErrorFilter authenticationErrorFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CharacterEncodingFilter encodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        // TODO: 운영 도메인 추가
        config.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // AT 전달용
        config.setExposedHeaders(List.of("Authorization"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("SecurityConfig - configure(AuthenticationManagerBuilder) 호출");
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) {
        // 정적/스웨거만 시큐리티 무시
        web.ignoring().antMatchers(
                "/assets/**",
                "/favicon.ico",
                "/swagger-ui.html", "/swagger-ui/**",
                "/swagger-resources/**",
                "/v2/api-docs", "/v3/api-docs/**",
                "/webjars/**"
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(encodingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authenticationErrorFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        jwtEmailPasswordAuthenticationFilter(
                                loginSuccessHandler, loginFailureHandler, authenticationManagerBean()
                        ),
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 인증 불필요 경로
                .antMatchers(HttpMethod.POST, "/api/user/signup").permitAll()
                .antMatchers(HttpMethod.GET,  "/api/user/email-check").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/password-reset").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/test-login").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

                // 스케줄러 테스트/조회 등
                .antMatchers(HttpMethod.GET, "/api/challenge/scheduler/**").permitAll()

                // (1) 코인 랭킹 관련은 인증 필요
                .antMatchers("/api/challenge/rank/coin/**").authenticated()

                // (1) 코인 랭킹 산정 트리거(프론트가 호출) - 임시로 인증만 요구
                // 운영 전환 시: .antMatchers(HttpMethod.POST, "/api/challenge/rank/test/coin/calc").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/challenge/rank/test/coin/calc").authenticated()

                // 그 외는 기본 차단(로그인 필요)
                .anyRequest().authenticated()
                .and()
                .cors()
                .and()
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public JwtEmailPasswordAuthenticationFilter jwtEmailPasswordAuthenticationFilter(
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            AuthenticationManager authenticationManager
    ) {
        JwtEmailPasswordAuthenticationFilter filter =
                new JwtEmailPasswordAuthenticationFilter(loginSuccessHandler, loginFailureHandler);
        filter.setAuthenticationManager(authenticationManager);
        // 로그인 URL (FE와 동일)
        filter.setFilterProcessesUrl("/api/auth/login");
        return filter;
    }
}
