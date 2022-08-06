const {proxyServer} = require('tcp-local-tunnel');

proxyServer({
    proxyPort: 25565,
    tunnelPort: 5000
});