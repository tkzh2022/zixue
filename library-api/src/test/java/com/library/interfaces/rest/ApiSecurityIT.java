package com.library.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.support.JpaIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ApiSecurityIT extends JpaIntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void protectedEndpointsUseUnified401And403Responses() throws Exception {
        mockMvc.perform(get("/api/v1/books").header("X-Trace-Id", "trace-unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is(8401)))
                .andExpect(jsonPath("$.traceId", is("trace-unauthorized")));

        String username = "reader" + UUID.randomUUID().toString().substring(0, 8);
        String body = """
                {"username":"%s","password":"password123","name":"Reader","phone":"13800138000","email":"reader@example.com"}
                """.formatted(username);
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        String token = json.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/books")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(8404)));

        mockMvc.perform(get("/api/v1/catalog/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }
}
