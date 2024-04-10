let stompClient = null;
let subscriptionDisconnect;
let subscriptionHandshake;
let subscriptionOutput;
let subscriptionError;
let user;
let server;

function connect() {
    let socket = new SockJS('/console');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        subscriptionDisconnect = stompClient.subscribe('/user/' + user.name + '/console/disconnect', function () {
            disconnect();
        });
        subscriptionHandshake = stompClient.subscribe('/user/' + user.name + '/console/handshake', function (msg) {
            handleHandshake();
        });
        stompClient.send('/console/connect', {}, JSON.stringify(server.id));
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.send('/console/disconnect', {}, agent.id);
        subscriptionDisconnect.unsubscribe();
        subscriptionOutput.unsubscribe();
        stompClient.disconnect();
    }
}

function handleHandshake() {
    console.log("Session started with status " + JSON.stringify(status));
    subscriptionHandshake.unsubscribe();
    subscriptionOutput = stompClient.subscribe('/user/' + user.name + '/console/output', function (msg) {
        handleOutput(msg.body);
    });
    subscriptionError = stompClient.subscribe('/user/' + user.name + '/console/error', function (msg) {
        handleError(msg.body);
    });
    writeLine("Connected");
    //writeLine("Type 'help' for a list of commands");
    //if (servers.length === 1 && servers[0].enabled)
    //    sendInput('attach '+servers[0].name)
}

function handleStatus(data) {
    let status = data.status;
    let btnStart = document.getElementById('ui-server-start');
    let btnStop = document.getElementById('ui-server-stop');

    btnStart.enabled = status !== "Offline";
    btnStop.enabled = status === "Offline";
}

function handleOutput(msg) {
    let output = document.getElementById('output');
    output.innerHTML += msg;
    output.scrollTop = output.scrollHeight;
}

function handleError(msg) {
    let output = document.getElementById('output');
    let span = document.createElement('span')
    span.className = 'stderr';
    span.innerText = msg;
    output.innerHTML += span;
    output.scrollTop = output.scrollHeight;
}

function writeLine(msg) {
    handleOutput(msg + '<br/>');
}

function sendMessage() {
    sendInput(document.getElementById('input').value);
    document.getElementById('input').value = '';
}

function sendInput(input) {
    stompClient.send('/console/input', {}, input);
}

function restartServer() {
    sendInput('stop');
}

async function runBackup(id) {
    stompClient.send('/console/backup');
}

async function load() {
    writeLine('Connecting...')
    user = await (await fetch('/api/webapp/user')).json();
    let li = window.location.href.lastIndexOf('/');
    let sid = window.location.href.substring(li+1);
    server = await (await fetch('/api/webapp/server/'+sid)).json();

    connect();

// Enables pressing the Enter Key in the Send Message Prompt
    document.getElementById('input')
        .addEventListener('keyup', function (event) {
            event.preventDefault();
            if (event.keyCode === 13) {
                document.getElementById("send").click();
            }
        });
}

function unload() {
    disconnect();
    writeLine("Disconnected")
}
