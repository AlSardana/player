package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService{

    private PlayerRepository playerRepository;

    @Autowired
    public void setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    private boolean checkParams(Player player){
        if (player.getName().length() < 1 || player.getName().length() > 12) return true;
        if (player.getTitle().length() < 1 || player.getTitle().length() > 30) return true;
        if (player.getExperience() < 0 || player.getExperience() > 10000000) return true;
        if (player.getBirthday().getTime() < 0) return true;
        Calendar date = Calendar.getInstance();
        date.setTime(player.getBirthday());
        if (date.get(Calendar.YEAR) < 2000 || date.get(Calendar.YEAR) > 3000) return true;

        return false;
    }
    private void setLevelAndExpUntilNextLevel(Player player) {
        player.setLevel(calculateLevel(player));
        player.setUntilNextLevel(calculateExpUntilNextLevel(player));
    }

    private int calculateLevel(Player player) {
        int exp = player.getExperience();
        return (int) ((Math.sqrt(2500 + 200 * exp) - 50) / 100);
    }

    private int calculateExpUntilNextLevel(Player player) {
        int exp = player.getExperience();
        int lvl = calculateLevel(player);
        return 50 * (lvl + 1) * (lvl + 2) - exp;
    }
    @Override
    public List<Player> getPlayersList(String name, String title, Race race, Profession profession,
                                       Long after, Long before, Boolean banned, Integer minExperience,
                                       Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> sortAllPlayers = playerRepository.findAll();
        if (name != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (title != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getTitle().contains(title))
                    .collect(Collectors.toList());
        }
        if (race != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getRace().equals(race))
                    .collect(Collectors.toList());
        }
        if (profession != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getProfession().equals(profession))
                    .collect(Collectors.toList());
        }
        if (after != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getBirthday().after(new Date(after)))
                    .collect(Collectors.toList());
        }
        if (before != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getBirthday().before(new Date(before)))
                    .collect(Collectors.toList());
        }
        if (banned != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.isBanned().equals(banned))
                    .collect(Collectors.toList());
        }
        if (minExperience != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getExperience()>=(minExperience))
                    .collect(Collectors.toList());
        }
        if (maxExperience != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getExperience()<=(maxExperience))
                    .collect(Collectors.toList());
        }
        if (minLevel != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getLevel()>=(minLevel))
                    .collect(Collectors.toList());
        }
        if (maxLevel != null){
            sortAllPlayers = sortAllPlayers.stream()
                    .filter(s -> s.getLevel()<=(maxLevel))
                    .collect(Collectors.toList());
        }
        return sortAllPlayers;
    }

    @Override
    public List<Player> getPlayersForPage(List<Player> sortAllPlayers, PlayerOrder order,
                                          Integer pageNumber, Integer pageSize) {
        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 3;
        return sortAllPlayers.stream()
                .sorted(getComparator(order))
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

    }

    /*@Override
    public List<Player> playerList(Specification<Player> specification) {
        return playerRepository.findAll(specification);
    }

    @Override
    public Page<Player> playerList(Specification<Player> specification, Pageable sorted) {
        return playerRepository.findAll(specification, sorted);
    }*/

    @Override
    public Player createPlayer(Player newPlayer) {
        if (newPlayer.getName()==null
                || newPlayer.getName().isEmpty()
                || newPlayer.getTitle()==null
                || newPlayer.getTitle().isEmpty()
                || newPlayer.getRace()==null
                || newPlayer.getProfession()==null
                || newPlayer.getExperience()==null
                || newPlayer.getBirthday()==null){
            throw new BadRequestException("Please add all required fields");
        }
        if (checkParams(newPlayer)){
            throw new BadRequestException("Please add all required fields");
        }
        if (newPlayer.isBanned() == null){
            newPlayer.setBanned(false);
        }
        setLevelAndExpUntilNextLevel(newPlayer);
        return playerRepository.saveAndFlush(newPlayer);
    }

    @Override
    public Player getPlayerById(Long id) {
        if (playerRepository.findById(id).isPresent())
        return playerRepository.findById(id).get();
        return null;
    }

    @Override
    public Player updatePlayer(Long id, Player requestPlayer) {
        if (!playerRepository.findById(id).isPresent()) return null;

        Player responsePlayer = getPlayerById(id);

        if (requestPlayer.getName() != null) responsePlayer.setName(requestPlayer.getName());
        if (requestPlayer.getTitle() != null) responsePlayer.setTitle(requestPlayer.getTitle());
        if (requestPlayer.getRace() != null) responsePlayer.setRace(requestPlayer.getRace());
        if (requestPlayer.getProfession() != null) responsePlayer.setProfession(requestPlayer.getProfession());
        if (requestPlayer.getBirthday() != null) responsePlayer.setBirthday(requestPlayer.getBirthday());
        if (requestPlayer.isBanned() != null) responsePlayer.setBanned(requestPlayer.isBanned());
        if (requestPlayer.getExperience() != null) responsePlayer.setExperience(requestPlayer.getExperience());

        setLevelAndExpUntilNextLevel(responsePlayer);
        return playerRepository.save(responsePlayer);
    }

    @Override
    public boolean deletePlayerById(Long id) {
        if (playerRepository.findById(id).isPresent()){
        playerRepository.deleteById(id);
        return true;}
        return false;
    }
    private Comparator<Player> getComparator(PlayerOrder order) {
        if (order == null){
            return Comparator.comparing(Player :: getId);
        }
        Comparator<Player> comparator = null;
        switch (order.getFieldName()){
            case "id":
                comparator = Comparator.comparing(Player :: getId);
            case "birthday":
                comparator = Comparator.comparing(Player :: getBirthday);
            case "experience":
                comparator = Comparator.comparing(Player :: getExperience);
            case "level":
                comparator = Comparator.comparing(Player :: getLevel);
        }
        return comparator;
    }

    /*@Override
    public Specification<Player> nameFilter(String name) {
        return ((root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.like(root.get("name"), "% + name + %"));
    }

    @Override
    public Specification<Player> titleFilter(String title) {
        return ((root, query, criteriaBuilder) -> title == null ? null : criteriaBuilder.like(root.get("title"), "% + title + %"));
    }

    @Override
    public Specification<Player> raceFilter(Race race) {
        return ((root, query, criteriaBuilder) -> race == null ? null : criteriaBuilder.like(root.get("race"), "% + race + %"));
    }

    @Override
    public Specification<Player> professionFilter(Profession profession) {
        return ((root, query, criteriaBuilder) -> profession == null ? null : criteriaBuilder.like(root.get("profession"), "% + profession + %"));
    }

    @Override
    public Specification<Player> experienceFilter(Integer minExperience, Integer maxExperience) {
        return ((root, query, criteriaBuilder) -> {
                if (minExperience == null && maxExperience == null){
                    return null;
                }
                if (minExperience == null){
                    return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience);
                }
                if(maxExperience == null){
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExperience);
                }
                return criteriaBuilder.between(root.get("experience"), minExperience, maxExperience);
        });
    }

    @Override
    public Specification<Player> levelFilter(Integer minLevel, Integer maxLevel) {
        return ((root, query, criteriaBuilder) -> {
            if (minLevel == null && maxLevel == null){
                return null;
            }
            if (minLevel == null){
                return criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel);
            }
            if (maxLevel == null){
                return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel);
            }
            return criteriaBuilder.between(root.get("level"), minLevel, maxLevel);

        });
    }

    @Override
    public Specification<Player> birthdayFilter(Long before, Long after) {
        return ((root, query, criteriaBuilder) -> {
            if (before == null && after == null){
                return null;
            }
            if (before == null){
                Date after1 = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), after1);
            }
            if (after == null){
                Date before1 = new Date(before);
                return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), before1);
            }
            Date before1 = new Date(before - 3600001);
            Date after1 = new Date(after);
            return criteriaBuilder.between(root.get("birthday"), before1, after1);
        });
    }

    @Override
    public Specification<Player> bannedFilter(Boolean isBanned) {
        return (root, query, criteriaBuilder) -> {
            if (isBanned == null) {
                return null;
            }
            if (isBanned) {
                return criteriaBuilder.isTrue(root.get("banned"));
            }
           else  {
                return criteriaBuilder.isFalse(root.get("banned"));
            }

        };
    }*/
}
