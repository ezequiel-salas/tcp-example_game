package Nim;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Nim client side program.
 *
 * @Author: Ezequiel Salas
 */
public class Nim {

    public static void main(String[] args){
        if (args.length != 3){
            usage();
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];

        try{
            Socket socket = new Socket();
            socket.connect (new InetSocketAddress(host, port));

            NimGame game = NimGame.create();
            NimModelProxy proxy = new NimModelProxy(socket,name);

            proxy.setGame(game);
            game.setProxy(proxy);

            proxy.join();

        }
        catch (ConnectException e){
            System.out.printf("Unable to connect to server: %s\n",host);
            e.printStackTrace (System.err);
            System.exit (1);
        }
        catch (IOException e){
            System.err.println ("GoClient: I/O error");
            e.printStackTrace (System.err);
            System.exit (1);
        }
    }

    /**
     * Print usage for client side Nim
     */
    public static void usage(){
        System.err.println("Usage: java Nim hostname port-number player-name");
        System.exit(1);
    }
}
