package com.codeoftheweb.salvo.models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date joinTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")//Agregar columna con este nombre
    private Game game;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    private List<Ship> ships = new ArrayList<>();

    @OneToMany(mappedBy = "gamePlayer")
    private List<Salvo> salvoes;

    public GamePlayer() {
    }

    public GamePlayer(Player player, Game game) {
        this.joinTime = new Date();
        this.player = player;
        this.game = game;
    }


    public long getId() {
        return id;
    }

    public Date getjoinTime() {
        return this.joinTime;
    }

    @JsonIgnore
    public Player getPlayer() {
        return this.player;
    }
    @JsonIgnore
    public Game getGame() {
        return this.game;
    }


    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @JsonIgnore
    public List<Ship> getShips() { return ships; }


    @JsonIgnore
    public List <Salvo> getSalvoes() {
        return salvoes;
    }



    public Map<String,Object> makeGamePlayerDTO(){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("player", this.getPlayer().makePlayerDetail());
        return dto;
    }

    public GamePlayer GetOpponent(){
        return this.getGame().getGamePlayers()
                .stream()
                .filter(opponent -> this.getId() != opponent.getId())
                .findFirst().orElse(new GamePlayer());
    }
    public Map<String,Object> hitsDto(GamePlayer self,
                                       GamePlayer opponent){
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self", getHits(self,opponent));
        dto.put("opponent", getHits(opponent,self));
        return dto;
    }



    public List<Map<String, Object>> getHits(GamePlayer self,
                              GamePlayer opponent){

        //Crear una lista de maps llamada dto
        List<Map<String, Object>> dto = new ArrayList<>();

        //Inicializar las variables. Van a contar cuántos hits tienen los barcos en total en cada juego
        int carrierDamage = 0;
        int destroyerDamage = 0;
        int patrolboatDamage = 0;
        int submarineDamage = 0;
        int battleshipDamage = 0;

        //Crear listas de arrays para las locations de los barcos
        List<String> carrierLocations = new ArrayList<>();
        List<String> destroyerLocations = new ArrayList<>();
        List<String> submarineLocations = new ArrayList<>();
        List<String> patrolboatLocations = new ArrayList<>();
        List<String> battleshipLocations = new ArrayList<>();

        //Bring all the ships from self get the type and do the following: if the ship is a "carrier", put that location in "carrierLocations"
        for (Ship ship: self.getShips()) {
            switch (ship.getType()){
                case "carrier":
                    carrierLocations = ship.getShipLocations();
                    break ;
                case "battleship" :
                    battleshipLocations = ship.getShipLocations();
                    break;
                case "destroyer":
                    destroyerLocations = ship.getShipLocations();
                    break;
                case "submarine":
                    submarineLocations = ship.getShipLocations();
                    break;
                case "patrol_boat":
                    patrolboatLocations = ship.getShipLocations();
                    break;
            }
        }

        //Bring all the salvos from your opponent. Create variables for each ship that has been hit.
        // Because these variables are inside the "for", they will initialize again for each salvo, so they only count the damage made in each turn to each ship
        for (Salvo salvo : opponent.getSalvoes()) {
            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;

            //Create a variable to know how many locations of the salvos of your opponent have been missed
            Integer missedShots = salvo.getSalvoLocations().size();

            //Create new variables (hits and damages)
            Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
            Map<String, Object> damagesPerTurn = new LinkedHashMap<>();
            List<String> salvoLocationsList = new ArrayList<>();
            List<String> hitCellsList = new ArrayList<>();

            //Put all the salvo locations in the variable "salvoLocationsList"
            salvoLocationsList.addAll(salvo.getSalvoLocations());


            //For each element in salvoLocationsList (all the salvo locations), call it salvoShot and do the following:
            for (String salvoShot : salvoLocationsList) {
                //If  the ship called carrier contains the locations of the salvoShot (e.g. The salvo shot is "H3" and your carrier ship locations are ["H1", "H2", "H3", "H4"])
                if (carrierLocations.contains(salvoShot)) {
                    //Add 1 to the variable carrierDamage (Total hits to ship). Add 1 to carrierHitsInTurn (Hits to that ship just in this turn). Add the locations of the salvoShot to hitCellsList. missedShots was 5 (the size of the locations of salvos). Given that the salvo has hit a ship, we substract 1 of missedShots
                    carrierDamage++;
                    carrierHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (battleshipLocations.contains(salvoShot)) {
                    battleshipDamage++;
                    battleshipHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (submarineLocations.contains(salvoShot)) {
                    submarineDamage++;
                    submarineHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (destroyerLocations.contains(salvoShot)) {
                    destroyerDamage++;
                    destroyerHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (patrolboatLocations.contains(salvoShot)) {
                    patrolboatDamage++;
                    patrolboatHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
            }


            //Hits to ships each turn
            damagesPerTurn.put("carrierHits", carrierHitsInTurn);
            damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
            damagesPerTurn.put("submarineHits", submarineHitsInTurn);
            damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);

            //Hits to ships during the whole game
            damagesPerTurn.put("carrier", carrierDamage);
            damagesPerTurn.put("battleship", battleshipDamage);
            damagesPerTurn.put("submarine", submarineDamage);
            damagesPerTurn.put("destroyer", destroyerDamage);
            damagesPerTurn.put("patrolboat", patrolboatDamage);

            //Get turn in which the salvo was fired
            hitsMapPerTurn.put("turn", salvo.getTurn());

            //Get the locations of where the salvos were fired
            hitsMapPerTurn.put("hitLocations", hitCellsList);

            //Put all the damages (ships hit by turn and in total)
            hitsMapPerTurn.put("damages", damagesPerTurn);

            //How many salvos missed a ship
            hitsMapPerTurn.put("missed", missedShots);

            dto.add(hitsMapPerTurn);
        }

        return dto;
    }


}
