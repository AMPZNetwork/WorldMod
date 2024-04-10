$(document).ready(()=> {
    refreshServerList();
});

function refreshServerList() {
    document.querySelectorAll('.serverEntry').forEach(entry => {
        entry.querySelector('.statusIcon').className = 'statusIcon icon-loading';
        entry.querySelector('.motd').innerHTML = 'Fetching MOTD ...';
        entry.querySelector('.players').innerHTML = 'Fetching players ...';
        fetch('/api/webapp/server/'+entry.id+'/status')
            .then(resp => {
                if (resp.status !== 200)
                    console.error('status request was not successful')
                return resp;
            })
            .then(resp => resp.json())
            .then(data => {
                entry.querySelector('.statusIcon').className = 'statusIcon icon-'+statusIconName(data.status);
                entry.querySelector('.motd').innerHTML = data.status==='offline'?'---':data.motdSanitized;
                entry.querySelector('.players').innerHTML = data.status==='offline'?'---':`${data.playerCount}/${data.playerMax}`;
            })
            .catch(error => console.log('could not update status of '+entry.id, error))
    })
}

function statusIconName(status) {
    switch (status) {
        case 'unknown_status': return 'warning';
        case 'offline': return 'offline';
        case 'starting': return 'maintenance';
        case 'in_maintenance_mode': return 'maintenance';
        case 'running_backup': return 'maintenance';
        case 'updating': return 'warning';
        case 'in_Trouble': return 'warning';
        case 'online': return 'online';
        case 'shutting_down': return 'offline';
    }
}
