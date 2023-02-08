package integration;

import app.foot.FootApi;
import app.foot.controller.rest.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.servlet.ServletException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
@Transactional
@Slf4j
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    StringBuilder exceptionBuilder = new StringBuilder();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();  //Allow 'java.time.Instant' mapping

    @Test
    void read_match_by_id_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(expectedMatch2(), actual);
    }
    private static Match matchNotFound(){
        return Match.builder()
                .id(100)
                .teamA(TeamMatch.builder()
                        .build())
                .teamB(TeamMatch.builder()
                        .build())
                .build();
    }
    @Test
    void read_matches_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        List<Match> actual = convertFromHttpResponse(response);

        assertEquals(3, actual.size());
        assertTrue(actual.contains(expectedMatch2()));
        //TODO: add these checks and its values
        assertTrue(actual.contains(expectedMatch1()));
        //assertTrue(actual.contains(expectedMatch3()));
    }

    private static Match expectedMatch2() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }
    private static Match expectedMatch1() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static TeamMatch teamMatchB() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchA() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                                .player(player3())
                                .scoreTime(70)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player6())
                                .scoreTime(80)
                                .isOG(true)
                                .build()))
                .build();
    }

    private static Team team3() {
        return Team.builder()
                .id(3)
                .name("E3")
                .build();
    }

    private static Player player6() {
        return Player.builder()
                .id(6)
                .name("J6")
                .teamName("E3")
                .isGuardian(false)
                .build();
    }

    private static Player player3() {
        return Player.builder()
                .id(3)
                .name("J3")
                .teamName("E2")
                .isGuardian(false)
                .build();
    }

    private static Team team2() {
        return Team.builder()
                .id(2)
                .name("E2")
                .build();
    }
    private  static PlayerScorer playerScorer(){
        return PlayerScorer.builder()
                .isOG(false)
                .scoreTime(30)
                .player(Player.builder()
                        .id(1)
                        .isGuardian(false)
                        .name("j1")
                        .teamName("E1")
                        .build())
                .build();
    }
    @Test
    void get_match_by_id_ko() throws Exception {
        exceptionBuilder.append("Match#").append(matchNotFound().getId()).append(" not found.");
        ServletException error = assertThrows(ServletException.class , () -> mockMvc
                .perform(get("/matches/"+matchNotFound().getId() ,exceptionBuilder.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse());
        assertEquals(exceptionBuilder.toString() , error.getRootCause().getMessage());
    }

    private List<Match> convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType playerListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Match.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                playerListType);
    }
    private Match convertFromHttpResponseToMatch(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {

        return objectMapper.readValue(
                response.getContentAsString(),
                Match.class);
    }
    @Test
    void add_goals_ok_id_3() throws Exception {
        MockHttpServletResponse response =
                mockMvc.perform(post("/matches/"+3+"/goals")
                                .content(objectMapper.writeValueAsString(List.of(playerScorer())))
                                .contentType("APPLICATION/JSON")
                                .accept(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(),response.getStatus());
    assertEquals(0, convertFromHttpResponseToMatch(response).getTeamA().getScore());
}
        @Test
    void add_goals_ko_id_3() throws Exception {
                mockMvc.perform(post("/matches/"+3+"/goals")
                        .content(List.of(playerScorer()).toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    result.getResponse();
                    assertTrue(true);
                }).andReturn().getResponse();

    }

}
