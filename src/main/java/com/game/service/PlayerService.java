package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import java.util.List;

public interface PlayerService {
    //List<Player> playerList(Specification<Player> specification);

    //Page<Player> playerList(Specification<Player> specification, Pageable sorted);
    List<Player> getPlayersList(String name, String title, Race race, Profession profession,
                                Long after, Long before, Boolean banned, Integer minExperience,
                                Integer maxExperience, Integer minLevel, Integer maxLevel);
    List<Player> getPlayersForPage(List<Player> sortAllPlayers, PlayerOrder order,
                                   Integer pageNumber, Integer pageSize);

    Player createPlayer(Player newPlayer);

    Player getPlayerById(Long id);

    Player updatePlayer(Long id, Player newChar);

    boolean deletePlayerById(Long id);

    /*Specification<Player> nameFilter(String name);
    Specification<Player> titleFilter(String title);
    Specification<Player> raceFilter(Race race);
    Specification<Player> professionFilter(Profession profession);
    Specification<Player> experienceFilter(Integer minExperience, Integer maxExperience);
    Specification<Player> levelFilter(Integer minLevel, Integer maxLevel);
    Specification<Player> birthdayFilter(Long before, Long after);
    Specification<Player> bannedFilter(Boolean isBanned);*/

}
