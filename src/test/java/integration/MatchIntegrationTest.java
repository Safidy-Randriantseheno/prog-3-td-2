package integration;

import app.foot.FootApi;
import app.foot.controller.rest.*;
import app.foot.controller.validator.GoalValidator;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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
    @MockBean
    GoalValidator validator;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private List<Match> convertFromHttpResponseToList(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType playerListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Match.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                playerListType);
    }
    private Match convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {

        return objectMapper.readValue(
                response.getContentAsString(),
                Match.class);
    }
    private static Match match1(Match match){
        return Match.builder()
                .id(1)
                .teamA(TeamMatch.builder()
                        .team(match.getTeamA().getTeam())
                        .score(match.getTeamA().getScore())
                        .scorers(match.getTeamA().getScorers())
                        .build())
                .teamB(TeamMatch.builder()
                        .team(match.getTeamB().getTeam())
                        .score(match.getTeamB().getScore())
                        .scorers(match.getTeamB().getScorers())
                        .build())
                .datetime(match.getDatetime())
                .stadium(match.getStadium())
                .build();
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
    private static Match expectedMatch2() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }
    private static Match expectedMatching() {
        return Match.builder()
                .id(3)
                .teamA(teamMatchWithGuardianGoaling())
                .teamB(teamMatchB())
                .stadium("S3")
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
    private static TeamMatch teamMatchWithGuardianGoaling() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                                .player(Player.builder()
                                        .isGuardian(true)
                                        .build())
                                .scoreTime(70)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player6())
                                .scoreTime(100)
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
    PlayerScorer playerScorer(){
        return PlayerScorer.builder()
                .scoreTime(30)
                .player(Player.builder()
                        .id(1)
                        .teamName("E1")
                        .name("J1")
                        .isGuardian(false)
                        .build())
                .isOG(false)
                .build();
    }
    private static PlayerScorer playerScoringInLess0(){
        return PlayerScorer.builder()
                .scoreTime(-100)
                .player(Player.builder()
                        .id(7)
                        .isGuardian(true)
                        .build())
                .isOG(false)
                .build();
    }
    @Test
    void get_match_by_id_ok() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(get("/matches/"+1))
                .andReturn()
                .getResponse();
        Match actual = convertFromHttpResponse(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(match1(actual) ,actual);
    }
    @Test
    void read_matches_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        List<Match> actual = convertFromHttpResponseToList(response);
        log.info(String.valueOf(actual.get(0)));
        log.info(actual.get(1).toString());
        assertEquals(3, actual.size());
        assertTrue(actual.contains(expectedMatch2()));
        // TODO: add these checks and its values
        assertTrue(actual.contains(expectedMatch1(actual.get(0))));
        assertTrue(actual.contains(expectedMatch3(actual.get(2))));

    }

    private Match expectedMatch3(Match match) {
        return Match.builder()
                .id(match.getId())
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .stadium(match.getStadium())
                .datetime(match.getDatetime())
                .build();
    }

    private Match expectedMatch1(Match match) {
        return Match.builder()
                .id(1)
                .teamA(match.getTeamA())
                .teamB(match.getTeamB())
                .stadium(match.getStadium())
                .datetime(match.getDatetime())
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

    @Test
    void post_match_adding_goal_ok() throws Exception {
        MockHttpServletResponse response = mockMvc
                .perform(post("/matches/"+3+"/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScorer())))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(54 , convertFromHttpResponse(response).getTeamA().getScore());
    }

    @Test
    void post_match_adding_goal_ko() {
        exceptionBuilder.append("Match#").append(expectedMatching().getId()).append(" not found.");
        ServletException error = assertThrows(ServletException.class , () -> mockMvc
                .perform(post("/matches/"+3+"/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScoringInLess0())))
                        .contentType("application/json")
                        .accept("application/json"))
                .andReturn()
                .getResponse());
        assertEquals(exceptionBuilder.toString() , error.getRootCause().getMessage());
    }
}
