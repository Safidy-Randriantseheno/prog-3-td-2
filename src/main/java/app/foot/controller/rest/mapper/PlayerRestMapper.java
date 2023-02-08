package app.foot.controller.rest.mapper;

import app.foot.controller.rest.Player;
import app.foot.model.PutPlayer;
import app.foot.repository.PlayerRepository;
import app.foot.repository.TeamRepository;
import app.foot.repository.entity.PlayerEntity;
import app.foot.repository.entity.TeamEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class PlayerRestMapper {
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    public Player toRest(app.foot.model.Player domain) {
        return Player.builder()
                .id(domain.getId())
                .name(domain.getName())
                .isGuardian(domain.getIsGuardian())
                .teamName(domain.getTeamName())
                .build();
    }

    public app.foot.model.Player toDomain(Player rest) {
        return app.foot.model.Player.builder()
                .id(rest.getId())
                .isGuardian(rest.getIsGuardian())
                .name(rest.getName())
                .teamName(rest.getTeamName())
                .build();
    }
    public PlayerEntity toDomain(Integer id , PutPlayer player) {
        TeamEntity team = teamRepository.getById(player.getTeam());
        Optional<PlayerEntity> playerEntity = playerRepository.findById(id);

        if (playerEntity.isPresent()) {
            playerEntity.get().setName(player.getName());
            playerEntity.get().setTeam(TeamEntity.builder()
                    .id(player.getTeam())
                    .name(team.getName())
                    .build());
            return playerRepository.save(playerEntity.get());
        } else {
            return playerRepository.save(PlayerEntity.builder()
                    .name(player.getName())
                    .team(team)
                    .build());
        }
    }
}
