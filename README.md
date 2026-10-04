const WebSocket = require('ws');

const server = new WebSocket.Server({ port: 8080 });
const clients = new Map();

server.on('connection', (socket) => {
  socket.on('message', (raw) => {
    try {
      const message = JSON.parse(raw.toString());
      const type = message.type || 'message';

      if (type === 'register') {
        const role = message.role || 'unknown';
        clients.set(socket, { role, id: message.id || String(Date.now()) });
        console.log(`Cliente registrado como ${role}`);
        return;
      }

      if (type === 'frame') {
        const sender = [...clients.entries()].find(([, data]) => data.role === 'sender')?.[0];
        const receiver = [...clients.entries()].find(([, data]) => data.role === 'receiver')?.[0];

        if (socket === sender && receiver) {
          receiver.send(JSON.stringify({ type: 'frame', data: message.data }));
        }
        return;
      }

      if (type === 'ping') {
        socket.send(JSON.stringify({ type: 'pong' }));
      }
    } catch (error) {
      console.error('Erro ao processar mensagem:', error.message);
    }
  });

  socket.on('close', () => {
    clients.delete(socket);
    console.log('Cliente desconectado');
  });
});

console.log('Servidor Tavin em execução na porta 8080');

