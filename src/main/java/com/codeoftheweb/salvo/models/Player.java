package com.codeoftheweb.salvo.models;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity

public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String userName;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<Score> scores;

    private String password;

    public Player() {
    }

    public Player(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public long getId() {
        return id;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private Map<String,Object> mapaDePlayers(){
        Map <String, Object> obj = new LinkedHashMap<>();
        obj.put("idPlayer", this.getId());
        obj.put("email", this.getUserName());
        return obj;
    }


    public Map<String, Object> makePlayerDetail() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("Id", this.getId());
        dto.put("email", this.getUserName());
        return dto;
    }

    public Map<String,Object> makePlayerScoreDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("email", this.getUserName());
        dto.put("score", mapaDeScores2());
        return dto;
    }


    public Map <String, Object> mapaDeScores2() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("won",this.getWins());
        dto.put("tied",this.getTies());
        dto.put("lost",this.getLoses());
        dto.put("total",this.getTotal());
        dto.put("gamesPlayed",this.getGamesPlayed());
        return dto;
    }


    public long getWins(){
        return this.getScores().stream()
                                .filter(score -> score.getScore() == 1)
                                .count();
    }

    public long getTies(){
        return this.getScores().stream()
                .filter(score -> score.getScore() == 0.5)
                .count();
    }
    public long getLoses(){
        return this.getScores().stream()
                .filter(score -> score.getScore() == 0)
                .count();
    }
    public double getTotal(){
        return this.getWins() + getTies()*0.5;
    }

    public long getGamesPlayed(){
        return this.getWins() + this.getTies() + this.getLoses();
    }

}