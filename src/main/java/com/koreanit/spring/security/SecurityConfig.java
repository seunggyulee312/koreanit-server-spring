package com.koreanit.spring.security;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreanit.spring.common.error.ErrorCode;
import com.koreanit.spring.common.response.ApiResponse;
import com.koreanit.spring.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

  private final ObjectMapper objectMapper;

  public SecurityConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  private void writeJson(HttpServletResponse res, int status, ApiResponse<Void> body) {
    res.setStatus(status);
    res.setContentType("application/json; charset=UTF-8");

    try {
      objectMapper.writeValue(res.getWriter(), body);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public SessionAuthenticationFilter sessionAuthenticationFilter(UserRepository userRepository,
      UserRoleRepository userRoleRepository) {
    return new SessionAuthenticationFilter(userRepository, userRoleRepository);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      SessionAuthenticationFilter sessionFilter) throws Exception {

    http
        // CORS 적용
        .cors(cors -> {})
        
        // 기본 로그인 폼 비활성화
        .formLogin(f -> f.disable())

        // HTTP Basic 인증 비활성화
        .httpBasic(b -> b.disable())

        // CSRF 보호 비활성화 (JSON API 기준)
        .csrf(csrf -> csrf.disable())

        // 인증/인가 실패 시 JSON 응답 처리
        .exceptionHandling(e -> e

            // 미인증 접근 시 401 응답
            .authenticationEntryPoint((req, res, ex) -> {
              writeJson(
                  res,
                  ErrorCode.UNAUTHORIZED.getStatus().value(),
                  ApiResponse.fail(ErrorCode.UNAUTHORIZED.name(), "로그인이 필요합니다"));
            })

            // 권한 없는 접근 시 403 응답
            .accessDeniedHandler((req, res, ex) -> {
              writeJson(
                  res,
                  ErrorCode.FORBIDDEN.getStatus().value(),
                  ApiResponse.fail(ErrorCode.FORBIDDEN.name(), "권한이 없습니다"));
            }))

        // 요청 경로별 접근 권한 설정
        .authorizeHttpRequests(auth -> auth
            // CORS preflight 요청 허용
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // 회원 가입 허용
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

            // 로그인 요청 허용
            .requestMatchers(HttpMethod.POST, "/api/login").permitAll()

            // 로그아웃 요청 허용
            .requestMatchers(HttpMethod.POST, "/api/logout").permitAll()

            // 게시글목록/단건조회 허용
            .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/posts/{id}").permitAll()

            // 댓글목록 허용
            .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()

            // API 경로는 인증 필요
            .requestMatchers("/api/**").authenticated()

            // 그 외 요청은 모두 허용
            .anyRequest().permitAll())

        // 세션 기반 인증 필터 등록
        .addFilterBefore(sessionFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();

  }
}