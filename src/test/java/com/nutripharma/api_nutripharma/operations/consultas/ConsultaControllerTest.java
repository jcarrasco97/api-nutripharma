package com.nutripharma.api_nutripharma.operations.consultas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.nutripharma.api_nutripharma.core.events.NotificacionEventListener;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificacionEventListener notificacionEventListener;

    @Test
    void contextLoads() {
    }

    @Test
    void loginDevuelveJwt() throws Exception {
        String body = """
                {"username":"admin@nutripharma.com","password":"admin123"}
                """;
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
        assertThat(token).isNotBlank();
    }

    @Test
    void recursoInexistente404() throws Exception {
        String token = obtenerJwtAdmin();

        mockMvc.perform(get("/api/consultas/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinJwtDevuelve401o403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/consultas"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(401, 403);
    }

    @Test
    void endpointPublico200() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk());
    }

    private String obtenerJwtAdmin() throws Exception {
        String body = """
                {"username":"admin@nutripharma.com","password":"admin123"}
                """;
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }
}
