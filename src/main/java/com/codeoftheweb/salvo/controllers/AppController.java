package com.codeoftheweb.salvo.controllers;
//import com.sun.javafx.collections.MappingChange;
import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
//import com.sun.tools.javac.code.Scope;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import com.codeoftheweb.salvo.repositories.ScoreRepository;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping ("/api")
public class AppController {

        @Autowired
        private ScoreRepository scoreRepository;

        @Autowired
        private GameRepository gameRepository;

        @Autowired
        private GamePlayerRepository gamePlayerRepository;

        @Autowired
        private PlayerRepository playerRepository;

@RequestMapping("/games")
    public Map <String, Object> allTheGames (Authentication authentication){
    Map <String, Object> dtoDeUser = new LinkedHashMap<>();
    if (isGuest(authentication)){
        dtoDeUser.put("player", "Guest");
    }else {
        Player player = playerRepository.findByUserName(authentication.getName()).get();
        dtoDeUser.put("player", player.makePlayerDetail());
    }
        dtoDeUser.put("games", gameRepository.findAll().stream().map(game -> mapaDeGames(game)).collect(Collectors.toList()));

return dtoDeUser;
}

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }


/*
//Cuando escriban /api/game me va a dar una lista de objetos llamada "getGameIDDetails" que la va a encontrar en el repo (GameRepository)
// y va a devolver todos los objetos en forma de stream (para poder usar las funciones propias de un stream, tales como map), los va a mapear aplicando la función mapadeGames y luego los va a coleccionar en una lista
    public List <Object> getGameIDDetails(){
    return repo.findAll().stream().map(e -> mapaDeGames(e)).collect(Collectors.toList());
}
*/
//En un Map (diferente de la función map) vamos a poner String (los key, por ejemplo "nombre:") y Objetos (por ejemplo "Juan Manuel"). Este Map se llama mapa y toma como parámetro el Game (hay que especificar el tipo de variable) e (viene del map anterior)
private Map<String, Object> mapaDeGames(Game e){
    Map <String, Object> obj = new LinkedHashMap<>();
//Put in the Object "obj" the following keys (e.g. "id") and values (e.g. "e.getID"). We get the method from the Game class (it's the getter of id)
    obj.put("idGame", e.getId());
    obj.put("created",e.getGameTime());
    obj.put("gamePlayers",getGamePlayersDetail(e.getGamePlayers()));

    return obj;
}

private List<Object> getGamePlayersDetail(Set<GamePlayer> o){
    return o.stream()
            .sorted(Comparator.comparing(GamePlayer::getId))
            .map(n-> mapaDeGamePlayers(n))
            .collect(Collectors.toList());
}


private Map <String, Object> mapaDeGamePlayers(GamePlayer n){
    Map <String, Object> obj = new LinkedHashMap<>();
    obj.put("idGamePlayer", n.getId());
    obj.put("player", mapaDePlayers(n.getPlayer()));
    return obj;
}

private Map<String,Object> mapaDePlayers(Player n){
    Map <String, Object> obj = new LinkedHashMap<>();
    obj.put("idPlayer", n.getId());
    obj.put("email", n.getUserName());
    return obj;
}

    @RequestMapping("/game_view/{nn}")
    public ResponseEntity <Map<String, Object>> getGameViewByGamePlayerID(@PathVariable Long nn, Authentication authentication) {
    //The Request Mapping takes the gamePlayer Id as a parameter (the nn number)
        Player newPlayer = playerRepository.findByUserName(authentication.getName()).get();
        GamePlayer gamePlayer = gamePlayerRepository.findById(nn).get();


        if (gamePlayer.getPlayer() == newPlayer) {


            ResponseEntity<Map<String,Object>> nuevaResponseEntity = new ResponseEntity<Map<String, Object>>(gameViewDto(gamePlayer), HttpStatus.ACCEPTED);

            return nuevaResponseEntity;
        }else{
            return new ResponseEntity<Map <String,Object>>(new HashMap<String, Object>(), HttpStatus.FORBIDDEN);
            //Constructor <tipo de dato> (tiene que coincidir con el tipo de dato, más información extra);
        }
    }

    @RequestMapping("/leaderBoard")
    public List <Object> getScoreDetails(){
        return playerRepository.findAll().stream().map(e -> e.makePlayerScoreDTO()).collect(Collectors.toList());
    }

    //Create new game in /api/games by using method post
    @RequestMapping(path= "/games", method = RequestMethod.POST)
    //ResponseEntity for it to return error whenever it's not possible. Authentication is the nfo from log in
    public ResponseEntity<Map<String, Object>> createGame (Authentication authentication){
    //If authentication is a guest (i.e. it's null), return an error message saying that you must log in
        if(isGuest(authentication)){
            return new ResponseEntity<>(createMap("error","You must log in!"), HttpStatus.FORBIDDEN);
    //If that is not the case, create a new Game (named newGame) with a parameter of Date and save it in the gameRepository.
    //Then create a new Player named newPlayer. Assign the value of the player in playerRepository that has the name of authentication
    //Then create a new GamePlayer named newGamePlayer with the parameters od player and game. Save it.
    //Once that is done, return a ResponseEntity with a key "gpid" (gamePlayerID) and a value of the ID + the HttpStatus of 201, created
        }else{
            Game newGame = new Game(new Date());
            gameRepository.save(newGame);
            Player newPlayer = playerRepository.findByUserName(authentication.getName()).get();
            GamePlayer newGamePlayer = new GamePlayer(newPlayer, newGame);
            gamePlayerRepository.save(newGamePlayer);
            return new ResponseEntity<>(createMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
    }

    //Join a game by using the method post
    @RequestMapping(path= "/games/{id}/players", method = RequestMethod.POST)
    //PathVariable to pass the {id} as a parameter
    public ResponseEntity<Map<String, Object>> joinGame (Authentication authentication,
                                                           @PathVariable Long id){
        //Check if authentication is a guest (i.e. null). The function is already declared in the controller. If that is the case, return an error (You must log in)
        if(isGuest(authentication)){
            return new ResponseEntity<>(createMap("error","You must log in!"), HttpStatus.FORBIDDEN);
        }
        //The return finishes the "if" so you can create a new one.

        Player newJoinPlayer = playerRepository.findByUserName(authentication.getName()).orElse(null);
        Game newJoinGame = gameRepository.findById(id).orElse(null);

        //Create a new game named newJoinGame. Assign the value of the game in gameRepository that has the same id as the one passed by parameter {id}. If you can't find the game, assign thee value "null"
        //Game newJoinGame = gameRepository.findById(id).orElse(null);

        //If newJoinGame is null (i.e. the id passed by parameter doesn't exist in the repository), return an error that tells you that the game you're looking for, doesn't exist
        if(newJoinGame == null){
            return new ResponseEntity<>(createMap("error","Game doesn't exist"), HttpStatus.FORBIDDEN);
        }

        if(newJoinPlayer == null){
            return new ResponseEntity<>(createMap("error","Player doesn't exist"), HttpStatus.FORBIDDEN);
        }

        //If the size of the list of gamePlayers of newJoinGame is larger or equal than 2, return an error that tells you that the game is full. You can't join it
        if(newJoinGame.getGamePlayers().size() >= 2){
            return new ResponseEntity<>(createMap("error game full ",newJoinGame), HttpStatus.FORBIDDEN);
        }
        //Create a new Player named newJoinPlayer. Assign the value of the player in repository that has the same name as the name in authentication
        //Player newJoinPlayer = playerRepository.findByUserName(authentication.getName()).get();
        //If the gamePlayers of newJoinGame contain any of the players of newJoinGame (i.e. Each game can have up to 2 gamePlayers.
        // You need to check if the userName of the new player that wants to join the game(newJoinGame) is already a participant (gamePlayer.getPlayer().getUserName() of that game. Meaning, you are already playing that game. You can't play against yourself)
        if(newJoinGame.getGamePlayers()
                .stream()
                .map(gamePlayer -> gamePlayer.getPlayer().getUserName())
                .collect(Collectors.toList())
                .contains(newJoinPlayer.getUserName())
        ){
            return new ResponseEntity<>(createMap("error","You're already a player in this game"), HttpStatus.FORBIDDEN);
        }
        //Create a new GamePlayer (newJoinGamePlayer). Assign by parameters the values of newJoinPlayer and newJoinGame. Save it.
        GamePlayer newJoinGamePlayer = new GamePlayer(newJoinPlayer, newJoinGame);
        gamePlayerRepository.save(newJoinGamePlayer);
        //If the post is correct (i.e. you have been able to join a new gamePlayer to a game), show the gamePlayer id (gpid) and the HttpStatus 201 CREATED
        return new ResponseEntity<>(createMap("gpid", newJoinGamePlayer.getId()), HttpStatus.CREATED);
    }




    //Map to be able to put a String and an Object inside the ResponseEntity
    public Map<String, Object> createMap (String str, Object obj){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(str, obj);
        return map;
    }



    public Map<String, Object> gameViewDto (GamePlayer gamePlayer){

        GamePlayer opponent = this.GetOpponent(gamePlayer);

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id de game", gamePlayer.getGame().getId());
        dto.put("gameState", getGameState(gamePlayer,opponent));
        dto.put("created", gamePlayer.getGame().getGameTime());
        dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers()
                .stream()
                .sorted(Comparator.comparing(GamePlayer::getId))
                .map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO())
                .collect(Collectors.toList())
        );
        dto.put("ships", gamePlayer.getShips()
                .stream()
                .map(ship1 -> ship1.makeShipDTO())
                .collect(Collectors.toList())
        );
        dto.put("salvoes", gamePlayer.getGame().getGamePlayers()
                .stream()
                //flatMap hace la misma función que map pero pone todos los elementos al mismo nivel (por ejemplo, un array con un solo objeto unido, en lugar de un array de varios objetos)
                .flatMap(gamePlayer1 -> gamePlayer1.getSalvoes()
                        .stream()
                        .sorted(Comparator.comparing(Salvo::getTurn))
                        .map(salvo -> salvo.makeSalvoDTO()))
                .collect(Collectors.toList())
        );
        dto.put("hits", gamePlayer.hitsDto(gamePlayer,opponent));
        return dto;

    }


    private String getGameState(GamePlayer gamePlayer, GamePlayer opponent){
        if(gamePlayer.getShips().size() == 0){
            return "Place ships";
        }

        if(opponent.getShips().size() == 0){
            return "Waiting for your opponent's ships";
        }
        if(gamePlayer.getSalvoes().size()==0){
            return "Place salvos";
        }
        if(opponent.getSalvoes().size()==0){
            return "Waiting for your opponent's salvos";
        }
        List <String> myShips = gamePlayer.getShips().stream().flatMap(ship -> ship.getShipLocations().stream()).collect(Collectors.toList());
        List <String> oppShips = opponent.getShips().stream().flatMap(ship -> ship.getShipLocations().stream()).collect(Collectors.toList());
        List<String> mySalvos = gamePlayer.getSalvoes().stream().flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList());;
        List<String> oppSalvos = opponent.getSalvoes().stream().flatMap(salvo -> salvo.getSalvoLocations().stream()).collect(Collectors.toList());;

        Boolean iWin = mySalvos.containsAll(oppShips);
        Boolean oppWins = oppSalvos.containsAll(myShips);

        if (mySalvos.size() == oppSalvos.size()) {
            Game game = gamePlayer.getGame();
            Player player = gamePlayer.getPlayer();

            if (iWin && oppWins) {
                Score score = new Score(0.5, game, player);
                if(!existScore(score, game)) {
                scoreRepository.save(score);
                }
                return "TIE";
            }

            if (oppWins) {
                Score score = new Score(0, game, player);
                if(!existScore(score, game)) {
                scoreRepository.save(score);
                }
                return "LOST";
            }

            if (iWin) {
                Score score = new Score(1, game, player);
                if(!existScore(score, game)) {
                scoreRepository.save(score);
                }

                return "WON";
            }
        }
        if(isTheSameTurn(gamePlayer,opponent)){
            return "Place your salvos";
        }
        return "Wait for your opponent";
    }

    private Boolean isTheSameTurn (GamePlayer gamePlayer, GamePlayer opponent){
    int mySalvos = gamePlayer.getSalvoes().size();
    int oppSalvos = opponent.getSalvoes().size();

    //If my salvos are the same as my opponent, we are in the same turn (true)
    if(mySalvos == oppSalvos){
        return true;
    }
    //If there is only a difference of one salvo with my opponent, I can still play
    if (mySalvos - oppSalvos == 1 || oppSalvos-mySalvos == 1){
    return false;
    //If the difference is larger, then that's a problem
    }return false;
    }

    public GamePlayer GetOpponent(GamePlayer gamePlayer){
        return gamePlayer.getGame().getGamePlayers()
                .stream()
                .filter(opponent -> gamePlayer.getId() != opponent.getId())
                .findFirst().orElse(new GamePlayer());
    }



    public  Boolean existScore(Score score, Game game){
        Set<Score> scores = game.getScores();
        for(Score s : scores){
            if(score.getPlayer().getUserName().equals(s.getPlayer().getUserName())){
                return true;
            }
        }
        return false;
    }





}
