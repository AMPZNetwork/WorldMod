$(document).ready(()=> {
    refreshVersion();
});

function refreshVersion() {
    fetch('/api/open/info/commit')
        .then(rsp => rsp.text())
        .then(current => fetch('https://api.github.com/repos/comroid-git/mc-server-hub/commits')
            .then(rsp => rsp.json())
            .then(data => data[0].sha)
            .then(latest => {
                var txt = document.querySelector('#ui-version span')
                var ico = document.querySelector('#ui-version div')
                if (latest === current) {
                    txt.innerText = 'You are using the latest version';
                    ico.className = 'ui-icon icon-online';
                } else {
                    txt.innerText = 'There is an update available';
                    ico.className = 'ui-icon icon-offline';
                }
            }))
        .catch(console.error);
}
