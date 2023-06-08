package com.example.spring.config

import com.example.spring.config.filter.CustomJwtAuthorizationFilter
import com.example.spring.config.filter.CustomUsernamePasswordAuthenticationFilter
import com.example.spring.config.filter.JwtAuthorizationExceptionFilter
import com.example.spring.config.filter.JwtFilter
import com.example.spring.member.application.port.out.MemberPort
import com.example.spring.member.application.service.JwtService
import com.example.spring.member.application.service.UserDetailsServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter


@Configuration
@EnableWebSecurity
class SpringSecurityConfig(
    private val jwtService: JwtService,
    private val authenticationConfiguration: AuthenticationConfiguration,
    private val memberPort: MemberPort,
    private val userDetailsServiceImpl: UserDetailsServiceImpl){
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf {
                disable()
            }

            authorizeRequests {
                authorize("/api/login",permitAll)
                authorize("/api/register",permitAll)
                authorize("/api/why",hasAuthority("b"))
                authorize(anyRequest, authenticated)
            }
            usernamePasswordAuthenticationFilter()?.let { addFilterAt<UsernamePasswordAuthenticationFilter>(it) }
            addFilterBefore<BasicAuthenticationFilter>(CustomJwtAuthorizationFilter(jwtService,memberPort,userDetailsServiceImpl))
            addFilterBefore<CustomJwtAuthorizationFilter>(JwtAuthorizationExceptionFilter(jwtService))
            formLogin { disable() }
            logout { }
        }
        return http.build()
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    fun usernamePasswordAuthenticationFilter(): UsernamePasswordAuthenticationFilter? {
        return CustomUsernamePasswordAuthenticationFilter(
            authenticationManager(authenticationConfiguration),
            jwtService,
            memberPort
        ).apply { setFilterProcessesUrl("/api/login") }
    }

}