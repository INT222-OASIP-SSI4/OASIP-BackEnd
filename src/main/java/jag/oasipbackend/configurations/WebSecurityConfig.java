package jag.oasipbackend.configurations;

import jag.oasipbackend.responses.ResponseAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private UserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16,32,1,4096,3);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF for this example
        httpSecurity.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
                .and()
//                .anonymous().principal("guest").authorities("ROLE_guest").and()
//                .exceptionHandling().accessDeniedHandler(new JwtAccessDenied()).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/users/login").permitAll()
                .antMatchers("/api/users/loginms").permitAll()
                .antMatchers(HttpMethod.POST,"/api/users/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/files/upload").permitAll()
                .antMatchers("/api/files/**").permitAll()
                .antMatchers("/api/eventcategories/**").permitAll()
                .antMatchers("/api/events/validate").permitAll()
                .antMatchers("/api/users/","/api/match/**").hasRole("admin")
                .antMatchers(HttpMethod.GET, "/api/events").hasAnyRole("admin","student","lecturer")
                .antMatchers(HttpMethod.GET, "/api/events/{eventId}").hasAnyRole("admin","student","lecturer")
                .antMatchers(HttpMethod.POST, "/api/events").hasAnyRole("admin","student")
                .antMatchers(HttpMethod.PUT, "/api/events/{eventId}").hasAnyRole("admin","student")
                .antMatchers(HttpMethod.DELETE, "/api/events/{eventId}").hasAnyRole("admin","student")
                .anyRequest().authenticated();
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//                .antMatchers("/api/events/**").hasRole("student")
//                .antMatchers("/api/events/**").access("hasRole('admin') or hasRole('student')")
//                .antMatchers("/api/refresh").permitAll()

//        method we'll configure patterns to define protected/unprotected API endpoints. Please note that we have disabled CSRF protection because we are not using Cookies.
//
//
//        We don't need CSRF for this example
//        httpSecurity.csrf().disable().cors().disable()
//                dont authenticate this particular request
//                .authorizeRequests().antMatchers("/api/login").permitAll()
//                .antMatchers("/api/users/signup").permitAll() //user sign
//                all other requests need to be authenticated
//                .anyRequest().authenticated().and().sessionManagement().
//                sessionCreationPolicy(SessionCreationPolicy.STATELESS).
//                and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);
//                make sure we use stateless session; session won't be used to
//                store user's state.
//                        exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//        Add a filter to validate the tokens with every request
//        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new ResponseAccessDeniedHandler();
    }
}