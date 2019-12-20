package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Ship;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShipController {
@Autowired
private PasswordEncoder passwordEncoder;

@Autowired
private GamePlayerRepository gamePlayerRepository;

@Autowired
private ShipRepository shipRepository;

    @RequestMapping(value = "/games/players/{gpid}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addShips(@PathVariable long gpid, @RequestBody List<Ship> ships, Authentication authentication) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "You must log in!"), HttpStatus.UNAUTHORIZED);
        }

        GamePlayer gamePlayer = gamePlayerRepository.findById(gpid).orElse(null);

        if(gamePlayer == null) {
            return new ResponseEntity<Map<String, Object>>(makeMap("error", "Game Player doesn't exist"), HttpStatus.UNAUTHORIZED);

        }

        if(gamePlayer.getPlayer().getUserName() != authentication.getName()){
            return  new ResponseEntity<Map<String, Object>>(makeMap("error", "This is not your Game Player"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.getShips().size() > 7){
            return new ResponseEntity<Map<String, Object>>(makeMap("error", "You already have ships"), HttpStatus.FORBIDDEN);
        }

            ships.stream().forEach(ship -> {
            ship.setGamePlayer(gamePlayer);
            shipRepository.save(ship);
        });
        return new ResponseEntity<Map<String, Object>>(makeMap("OK", "Ships created"), HttpStatus.CREATED);
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }


    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
}