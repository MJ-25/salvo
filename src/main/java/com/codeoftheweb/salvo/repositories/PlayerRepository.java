package com.codeoftheweb.salvo.repositories;
import java.util.List;
import java.util.Optional;

import com.codeoftheweb.salvo.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {
    //Player findByUserName2(@Param("userName") String userName);
    Optional<Player> findByUserName(String  userName);
}
