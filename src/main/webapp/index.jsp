<html>
<body>
<h2>Hello World!</h2>

<!-- Guacamole -->
<script type="text/javascript"
        src="guacamole-common-js/all.js"></script>

<!-- Display -->
<div id="display"></div>

<!-- Init -->
<script type="text/javascript"> /* <![CDATA[ */

// Get display div from document
var display = document.getElementById("display");

// Instantiate client, using an HTTP tunnel for communications.
var guac = new Guacamole.Client(
    new Guacamole.HTTPTunnel("tunnel")
);

// Add client to display div
display.appendChild(guac.getDisplay().getElement());

// Error handler
guac.onerror = function(error) {
    alert(JSON.stringify(error));
};
let stringReader;
guac.onclipboard = function (stream, mimetype){
    // console.log(mimetype);
    // console.log("stream: " + stream.index);
     stringReader = new Guacamole.StringReader(stream);
    stringReader.ontext = function(text){
       // console.log(text);
    };
}

// Connect
guac.connect();

// Mouse
var mouse = new Guacamole.Mouse(guac.getDisplay().getElement());

mouse.onmousedown =
    mouse.onmouseup   =
        mouse.onmousemove = function(mouseState) {
            guac.sendMouseState(mouseState);
        };

// Keyboard
var keyboard = new Guacamole.Keyboard(document);

keyboard.onkeydown = function (keysym) {
    console.log("keydown: " + keysym);
    guac.sendKeyEvent(1, keysym);
};

keyboard.onkeyup = function (keysym) {
    guac.sendKeyEvent(0, keysym);
};

// Disconnect on close
window.onunload = function() {
    guac.disconnect();
}

function typeKey(keysym){
    guac.sendKeyEvent(1, keysym);
    guac.sendKeyEvent(0, keysym);
}

let typeUsername = function(){
    typeKey(122);
    typeKey(104);
    typeKey(97);
    typeKey(110);
    typeKey(103);
    typeKey(108);
    typeKey(105);
    typeKey(110);
    typeKey(113);
    typeKey(105);
    typeKey(97);
    typeKey(110);
    typeKey(103);
    typeKey(65293);
    setTimeout(typeUsername, 5 * 1000);
};
// typeUsername();
/* ]]> */ </script>
</body>
</html>
