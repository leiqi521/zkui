package com.leiqi.zk.zkui.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 关闭csrf防护
        http.csrf().disable()
                .headers().frameOptions().disable()
                .and();

        //登录处理
        http.formLogin() //表单方式，或httpBasic
                .loginPage("/loginPage")
                .loginProcessingUrl("/form")
                .defaultSuccessUrl("/home") //成功登陆后跳转页面
                .failureUrl("/loginError")
                .permitAll()
                .and();
        // 授权配置
        http.authorizeRequests()
                //无需权限访问
                .antMatchers("/css/**", "/images/**","/js/**","/fonts/**").permitAll()
//                .antMatchers("/user/**").hasRole("USER")
                //其他接口需要登录后才能访问
                .anyRequest().authenticated()
                .and();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .passwordEncoder(passwordEncoder())
                .withUser("admin").password(passwordEncoder().encode("123456")).roles("ADMIN")
                .and()
                .withUser("test").password(passwordEncoder().encode("test123")).roles("USER");

    }
}