package gb.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.*;

public class ClientHandler {
    private static final Logger logger = Logger.getLogger("");
    String nick;
    private Server server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public ClientHandler(final Server server, final Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/addUser")) {
                                String[] tokens = str.split(" ");
                                AuthService.addNewUser(tokens[1], tokens[3], tokens[2]);
                                ClientHandler.this.createLog("addNewUser", tokens[1]);
                                ClientHandler.this.sendMsg("Добавлен новый пользователь!");
                            }

                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);

                                if (newNick != null) {
                                    ClientHandler.this.sendMsg("/authok");
                                    nick = newNick;
                                    server.subscribe(ClientHandler.this);
                                    break;
                                } else {
                                    ClientHandler.this.sendMsg("Неверный логин/пароль");
                                }
                            }
                        }
                        while (true) {
                            String str = in.readUTF();
                            if (str.equals("/end")) {
                                out.writeUTF("/serverclosed");
                                break;
                            }
                            server.broadcastMsg(nick + " " + str);
                            createMsgLog(nick, "send message:", str);
                            System.out.println("Client: " + str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createLog(String event, String nick) throws IOException {
        logger.setLevel(Level.SEVERE);
        logger.getHandlers()[0].setLevel(Level.SEVERE);
        logger.getHandlers()[0].setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String msg = record.getLevel() + "\t" + record.getMessage() + " "
                        + record.getMillis();
                return msg;
            }
        });

        Handler handler = new FileHandler("mylog.log");
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logger.log(Level.SEVERE, event + " " + nick);
    }

    public void createMsgLog(String nickFrom, String event, String msg) throws IOException {

        Handler handlerToMsg = new FileHandler("msglog.log");
        handlerToMsg.setLevel(Level.ALL);
        handlerToMsg.setFormatter(new SimpleFormatter());
        logger.addHandler(handlerToMsg);
        logger.log(Level.SEVERE, nickFrom + " " + event + " " + msg);
    }
}
