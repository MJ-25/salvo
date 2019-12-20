$(function () {
  $("#logout-btn").hide();
  $("#newGame").hide();
  $("#login-btn").click(function () {
    login(event);
  });
  $("#signin-btn").click(function () {
    signin(event);
  });
  $("#logout-btn").click(function () {
    logout(event);
  });
  $("#newGame").click(function () {
    newGame(event);
  });
  loadData();
});

var error = "";
var playerId = "";
var gamePlayers = "";
var jason = "";


//Obtener Json desde /api/games y colocarlo en el html
function loadData() {
  fetch("/api/games").then(function (response) {
      if (response.ok) {
        return response.json();
      }
    }).then(function (json) {
      jason = json;
      //Id de player loggeado
      playerId = json.player.Id;
      document.getElementById("lista").innerHTML = json.games.map(listOfGameDates).join("");
      chequearUsuario(json.player.email);
      if (playerId != undefined) {
        $("#newGame").show();
      }
      setInterval(function(){ window.location.reload();}, 10000);
    })
    .catch(function (error) {
      console.log("Request failed: " + error.message);
    });
}


var idsDeGamePlayers = [];
var buttonRejoinGame = "";
var buttonJoinGame = "";



//Crear la lista para poner en el html
function listOfGameDates(game) {
  return "<li class='collection-item grey darken-3 white-text'> Horario: " + game.created + "   Jugadores: " + game.gamePlayers.map(emails) + createButtonRejoinGame(game) + createButtonJoinGame(game) + "</li>";
}

function emails(e) {
  var mails = " " + e.player.email;
  return mails;
}


//Function to create a button to go back to the game
function createButtonRejoinGame(game) {
  if (game.gamePlayers.length >= 2) {
    if (game.gamePlayers[0].player.idPlayer == playerId) {
      buttonRejoinGame = "<div class='right-align'><a class='joinGameButton waves-effect red darken-4 btn-small' href= 'http://localhost:8080/web/game2.html?gp=" + game.gamePlayers[0].idGamePlayer + "'>Go to game! </a> </div>";
      return buttonRejoinGame;
    } else if (game.gamePlayers[1] != undefined && game.gamePlayers[1].player.idPlayer == playerId) {
      buttonRejoinGame = "<div class='right-align'><a class='joinGameButton waves-effect red darken-4 btn-small' href= 'http://localhost:8080/web/game2.html?gp=" + game.gamePlayers[1].idGamePlayer + "'>Go to game! </a> </div>";
      return buttonRejoinGame;
    } else {
      buttonRejoinGame = "";
      return buttonRejoinGame;
    }
  } else {
    buttonRejoinGame = "";
    return buttonRejoinGame;
  }
}


//Function to create a button to join for the first time the game
function createButtonJoinGame(game) {
  if (playerId != undefined && game.gamePlayers.length < 2 && game.gamePlayers[0].player.idPlayer != playerId) {
    buttonJoinGame = "<div class='right-align'><button class='joinGame joinGameButton waves-effect red darken-4 btn-small' onclick='post(" + game.idGame + ")'> Join game! </button> </div>";
    return buttonJoinGame;
  } else {
    buttonJoinGame = "";
    return buttonJoinGame;
  }
}

//Function to post the new gamePlayer to the game and then go to the page to play the game
function post(idGame) {
  $.post("/api/games/" + idGame + "/players")
    .done(function (data) {
      window.location.href = "http://localhost:8080/web/game2.html?gp=" + data.gpid;
    })
    .fail(function (error) {
      console.log("game creation failed" + error.message);
    });
}




//Obtiene Json desde /api/leaderBoard y lo pone en el html
fetch("/api/leaderBoard").then(function (response) {
    if (response.ok) {
      return response.json();
    }
  }).then(function (json) {
    document.getElementById("bodyLeaderBoard").innerHTML = json.map(tableBody).join("");
    $("#blankField").hide();
    $("#wrongInfo").hide();
    $("#usedUser").hide();

  })
  .catch(function (error) {
    console.log("Request failed: " + error.message);
  });


//Crea el contenido que va adentro de la tabla leaderboard
function tableBody(e) {
  return "<tr class='white-text'><td>" + e.email +
    "</td><td>" + e.score.total +
    "</td><td>" + e.score.won +
    "</td><td>" + e.score.lost +
    "</td><td>" + e.score.tied +
    "</td><td>" + e.score.gamesPlayed +
    "</td></tr>";
}

//Función para hacer log in
function login(evt) {
  evt.preventDefault();
  var form = evt.target.form;
  $.post("/api/login", {
      name: form["name"].value,
      password: form["password"].value
    })
    .done(function (data) {
      showLogin(false);
      loadData();
    })
    .fail(function (jqXHR, textStatus) {
      error = jqXHR.status;
      if (error == 401) {
        $("#wrongInfo").show();
        $("#usedUser").hide();
        $("#blankField").hide();
      }
    });
};

//Función para hacer sign in
function signin(evt) {
  evt.preventDefault();
  var form = evt.target.form;
  $.post("/api/players", {
      email: form["name"].value,
      password: form["password"].value
    })
    .done(function (data) {
      $.post("/api/login", {
          name: form["name"].value,
          password: form["password"].value
        })
        .done(function (data) {
          showLogin(false);
          loadData();
        })
    })
    .fail(function (jqXHR, textStatus) {
      alert("Failed: " + jqXHR.status);

      error = jqXHR.status;
      if (error == 400) {
        $("#blankField").show();
        $("#wrongInfo").hide();
        $("#usedUser").hide();
      };
      if (error == 403) {
        $("#usedUser").show();
        $("#blankField").hide();
        $("#wrongInfo").hide();
      };
    });
};

//Función para que cuando actualice la página no vuelva a aparecer el log in si el usuario ya está loggeado
function chequearUsuario(usuario) {
  if (usuario != undefined) {
    showLogin(false);
    $("#player").text("Welcome " + usuario + "!");
  }
}

//Función para hacer log out
function logout(evt) {
  evt.preventDefault();
  $.post("/api/logout")
    .done(function (data) {
        showLogin(true);
      window.location.reload()
    })
    .fail(function (jqXHR, textStatus) {
      alert("Failed: " + textStatus);
    });
}
//Create a new game
function newGame(event) {
  event.preventDefault();
  $.post("/api/games")
    .done(function () {
      window.location.reload()
    })
    .fail(function () {
    });
}
//Función para que una vez hecho el log in desaparezca el form para hacer log in y aparezca el de log out
function showLogin(show) {
  if (show) {
    $("#login-info").show();
    $("#login-btn").show();
    $("#signin-btn").show();
    $("#logout-btn").hide();
    $("#player").hide();
    $(".joinGameButton").hide();
  } else {
    $("#logout-btn").show();
    $("#login-info").hide();
    $("#signin-btn").hide();
    $("#login-btn").hide();
    $("#player").show();
    $("#blankField").hide();
    $("#wrongInfo").hide();
    $("#usedUser").hide();
  }
}