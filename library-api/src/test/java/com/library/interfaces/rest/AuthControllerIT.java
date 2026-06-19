package com.library.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.reader.ReaderRepository;
import com.library.interfaces.dto.auth.LoginRequest;
import com.library.interfaces.dto.auth.RegisterRequest;
import com.library.support.JpaIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIT extends JpaIntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    ReaderRepository readerRepository;

    @Test
    void registerAndLoginFlow() throws Exception {
        String username = "test" + java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(username);
        registerReq.setPassword("password123");
        registerReq.setName("Test User");
        registerReq.setPhone("13800138000");
        registerReq.setEmail("test@test.com");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));

        Long userId = userAccountRepository.findByUsername(username).orElseThrow().id();
        org.assertj.core.api.Assertions.assertThat(readerRepository.findByUserAccountId(userId)).isPresent();

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
    }
}
