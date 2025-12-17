package com.cubeai;

import com.cubeai.domain.auth.service.CustomOAuth2UserService;
import com.cubeai.global.jwt.JwtAuthenticationEntryPoint;
import com.cubeai.global.jwt.JwtProvider;
import com.cubeai.global.security.CustomOAuth2SuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

@SpringBootTest
@SuppressWarnings("removal")
class CubeaiApplicationTests {

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockBean
	private CustomOAuth2UserService customOAuth2UserService;

	@MockBean
	private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@MockBean
	private OAuth2AuthorizedClientService authorizedClientService;

	@MockBean
	private OAuth2AuthorizedClientRepository authorizedClientRepository;

	@Test
	void contextLoads() {
	}

}
