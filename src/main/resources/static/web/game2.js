$(function() {
    loadData();
    loadData2();
    $("#logout-btn").click(function () {
        logout(event);
      });
});

var horizontal = [1,2,3,4,5,6,7,8,9,10];
var vertical = ["A","B","C","D","E","F","G","H","I","J"];

//Function to create the header of the tables
function getHeader(){
var headers = "<th></th>";
var i;
for (i=0; i<=horizontal.length-1; i++){
headers += "<th>" + horizontal[i] + "</th>";
}
return headers;
}

//Function to put the header into the html
function renderHeadersTable(headerTable){
var html = getHeader();
document.getElementById(headerTable).innerHTML = html;
}

renderHeadersTable("headShip");
renderHeadersTable("headSalvo");

//Function to create the body of the tables
function getBody(id){
var body = "";
var headers = "";
var j;
var i=0;
for (i=0; i<=vertical.length-1; i++)
    {
        body += "<tr><td>" + vertical[i] + "</td>";
         for (j=0; j<=horizontal.length-1; j++)
              {
                 body += "<td ondrop='drop(event)' ondragover='allowDrop(event)' id='" + id + vertical[i] + horizontal[j]+ "'></td>";
              }
         body += "</tr>";
    }
 return body;
}

//Function to put the body of the tables inside the html
function renderBodyTable(bodyTable,id){
var html2 = getBody(id);
document.getElementById(bodyTable).innerHTML = html2;
}

renderBodyTable("bodyShip","Sh_");
renderBodyTable("bodySalvo", "Sal_");

//Function to get the parameter by name
function getParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
};

//Function to get the name of the gamePlayers, the ships and the salvoes. It will assign a class to them so they can be coloured in the table
function loadData(){
    $.get('/api/game_view/'+getParameterByName('gp'))
        .done(function(data) {
            let playerInfo;
            if(data.gamePlayers[0].id == getParameterByName('gp'))
                {playerInfo = [data.gamePlayers[0].player.userName,data.gamePlayers[1].player.userName];}
            else
                {playerInfo = [data.gamePlayers[1].player.userName,data.gamePlayers[0].player.userName];}

            $('#playerInfo').text(playerInfo[0] + '(you) vs ' + playerInfo[1]);

            let arrayLocations =[];
            //Function to apply a class to all the ships and put them in the first table (we do this through the id of the <td>)
            data.ships.forEach(function(ship){
            ship.locations.map(e => arrayLocations.push(e));
                ship.locations.forEach(function(shipLocation){
                    $('#Sh_'+shipLocation).addClass('ship-piece');
                })
            });

            //Function to apply a class (colour) and the number of turn to the salvoes of player 1 and put them in the second table
            data.salvoes.forEach(function(salvo){
            if(data.gamePlayers[0].player.userName,data.gamePlayers[1].id === salvo.player){
                salvo.locations.forEach(function(salvoLocation){
                    $('#Sal_'+salvoLocation).addClass('salvo-piece')
                    $('#Sal_'+salvoLocation).text(salvo.turn);
                });
                }
                else{ //For the salvoes of player 2 we put them in the first table. If the salvo hit a ship (e.g. the location of the salvo is the same as the location of one of the ships in the array arrayLocations), the ship turns purple and the turn number is written in it
                salvo.locations.forEach(function(salvoLocation){
                if(salvoLocation == arrayLocations.find(e => e == salvoLocation)){
                    $('#Sh_'+salvoLocation).addClass('shipHit')
                    $('#Sh_'+salvoLocation).text(salvo.turn);}
                    else{
                    $('#Sh_'+salvoLocation).addClass('salvo-piece')
                    }
                });
                }
            });

        })
        .fail(function( jqXHR, textStatus ) {
          alert( "Failed: " + textStatus );
        });
};

function loadData2 (){
fetch("/api/games").then(function (response) {
    if (response.ok) {
      return response.json();
    }
  }).then(function (json) {
    console.log(json);
    chequearUsuario(json.player.email);
  })
  .catch(function (error) {
    console.log("Request failed: " + error.status);
  });
  }

  //Función para que cuando actualice la página no vuelva a aparecer el log in si el usuario ya está loggeado
   function chequearUsuario (usuario){
   if (usuario != undefined){
      $("#player").text("Username: " + usuario);
   }
   }

   //Función para hacer log out
   function logout(evt) {
     evt.preventDefault();
     $.post("/api/logout")
     .done(function (data) {
           console.log("successful logout!!")
           //Redirecciona a la página principal luego de hacer log out
           window.location.replace("http://localhost:8080/web/games2.html");
         })
      .fail(function( jqXHR, textStatus ) {
                          alert( "Failed: " + textStatus );
                        });
   }


//Pasarlo por fetch o averiguar como modificar el post de jquery
function createShips(gpid, newType, newShipLocation ){
    $.post({
    url: "/api/games/players/" + gpid + "/ships",
    data: JSON.stringify([{type: newType, shipLocations: newShipLocation}]),
    dataType: "text",
    contentType: "application/json"
    })
    .done(function (response, status, jqXHR) {
        alert( "Ships added: " + response );
        window.location.reload();
    })
    .fail(function (jqXHR, status, httpError) {
        alert("Failed to add ship: " + httpError);
    })
}

function customPost(array){

fetch('/api/games/players/4/ships', {method: 'POST', headers:{'Content-Type': 'application/json;charset=UTF-8'}, body: JSON.stringify(array)})

}

function allowDrop(ev) {
  ev.preventDefault();
}

function drag(ev) {
  ev.dataTransfer.setData("text", ev.target.id);
}

function drop(ev) {
  ev.preventDefault();
  var data = ev.dataTransfer.getData("text");
  ev.target.appendChild(document.getElementById(data));
}