package com.marketplace.security;

import com.marketplace.security.JwtAuthenticationEntryPoint;
import com.marketplace.security.JwtAuthenticationFilter;
import com.marketplace.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for better security
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> 
                auth
                    // Public endpoints - no authentication required
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/webhooks/**").permitAll()
                    .requestMatchers("/api/health/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    
                    // Public read-only endpoints
                    .requestMatchers(HttpMethod.GET, "/api/images").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/images/{imageId}").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/images/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/images/trending").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/images/featured").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/images/user/{userId}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/sellers").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/users/{userId}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auctions/active").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auctions/ending-soon").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auctions/{imageId}/bids").permitAll()
                    
                    // User endpoints - require authentication
                    .requestMatchers("/api/users/me").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                    .requestMatchers("/api/users/upgrade-to-seller").authenticated()
                    .requestMatchers("/api/notifications/**").authenticated()
                    .requestMatchers("/api/transactions/purchases").authenticated()
                    .requestMatchers("/api/images/my-collection").authenticated()
                    .requestMatchers("/api/images/{imageId}/download").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/images/{imageId}/like").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/transactions/purchase").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/auctions/bid").authenticated()
                    .requestMatchers("/api/auctions/my-bids").authenticated()
                    
                    // Seller endpoints - require SELLER role
                    .requestMatchers(HttpMethod.POST, "/api/images").hasRole("SELLER")
                    .requestMatchers(HttpMethod.PUT, "/api/images/{imageId}").hasRole("SELLER")
                    .requestMatchers(HttpMethod.DELETE, "/api/images/{imageId}").hasRole("SELLER")
                    .requestMatchers("/api/images/my-uploads").hasRole("SELLER")
                    .requestMatchers("/api/transactions/sales").hasRole("SELLER")
                    .requestMatchers("/api/transactions/analytics/seller").hasRole("SELLER")
                    
                    // Category management - admin only
                    .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/categories/{categoryId}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/categories/{categoryId}").hasRole("ADMIN")
                    
                    // Admin endpoints - require ADMIN role
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    
                    // All other requests need authentication
                    .anyRequest().authenticated()
            );
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins in production
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",  // React dev server
            "http://localhost:3001",  // Alternative port
            "https://marketplace.com", // Production domain
            "https://*.marketplace.com" // Subdomains
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}