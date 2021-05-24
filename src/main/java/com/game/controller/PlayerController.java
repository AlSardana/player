package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import exceptions.BadRequestException;
import exceptions.PlayerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class PlayerController {

    private PlayerService playerService;

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    private boolean checkId(Long id){
        return id <= 0;
    }


    @GetMapping("/players")
    @ResponseStatus(HttpStatus.OK)
    public List<Player> getPlayersList(@RequestParam(required = false) String name,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(required = false) Race race,
                                       @RequestParam(required = false)Profession profession,
                                       @RequestParam(required = false) Long after,
                                       @RequestParam(required = false) Long before,
                                       @RequestParam(required = false) Boolean banned,
                                       @RequestParam(required = false) Integer minExperience,
                                       @RequestParam(required = false) Integer maxExperience,
                                       @RequestParam(required = false) Integer minLevel,
                                       @RequestParam(required = false) Integer maxLevel,
                                       @RequestParam(required = false) PlayerOrder order,
                                       @RequestParam(required = false) Integer pageNumber,
                                       @RequestParam(required = false) Integer pageSize){
        List<Player> players = playerService.getPlayersList(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel);

        return playerService.getPlayersForPage(players, order, pageNumber, pageSize);
    }
    @GetMapping("/players/count")
    @ResponseStatus(HttpStatus.OK)
    public Integer getCountSortPlayer(@RequestParam(required = false) String name,
                                      @RequestParam(required = false) String title,
                                      @RequestParam(required = false) Race race,
                                      @RequestParam(required = false)Profession profession,
                                      @RequestParam(required = false) Long after,
                                      @RequestParam(required = false) Long before,
                                      @RequestParam(required = false) Boolean banned,
                                      @RequestParam(required = false) Integer minExperience,
                                      @RequestParam(required = false) Integer maxExperience,
                                      @RequestParam(required = false) Integer minLevel,
                                      @RequestParam(required = false) Integer maxLevel){
        return playerService.getPlayersList(name, title, race, profession, after,
                before, banned, minExperience, maxExperience, minLevel, maxLevel).size();
    }

    @PostMapping("/players")
    public ResponseEntity<?> createPlayer(@RequestBody Player requestPlayer) {
        Player responsePlayer = playerService.createPlayer(requestPlayer);
        if (responsePlayer == null) {
            throw new PlayerNotFoundException();
        } else return new ResponseEntity<>(responsePlayer, HttpStatus.OK);
    }

    @GetMapping("/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable(name = "id") Long id) {
        if (checkId(id)) {
            throw new BadRequestException();
        }
        Player player = playerService.getPlayerById(id);
        if (player == null) {
            throw new PlayerNotFoundException();
        } else return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long id, @RequestBody Player requestPlayer) {
        if (checkId(id) || checkParams(requestPlayer)) {
            throw new BadRequestException();}
        Player responsePlayer = playerService.updatePlayer(id, requestPlayer);
        if (responsePlayer == null) {
            throw new PlayerNotFoundException();
        } else return new ResponseEntity<>(responsePlayer, HttpStatus.OK);
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable("id") Long id) {
        if (checkId(id)) {
            throw new BadRequestException();
        }
        if (playerService.deletePlayerById(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            throw new PlayerNotFoundException();
        }
    }


    private boolean checkParams(Player player) {
        return (player.getExperience() != null && (player.getExperience() < 0 || player.getExperience() > 10000000))
                || (player.getBirthday() != null && player.getBirthday().getTime() < 0);

    }
}
