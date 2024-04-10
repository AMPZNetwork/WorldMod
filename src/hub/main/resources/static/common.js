function confirmAction(what, warn, action) {
    if (window.confirm("Are you sure you want to "+what+"? "+warn+"!"))
        action();
}


