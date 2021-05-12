package Nim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * ModelProxy for use in client side Nim.
 * @Author: Ezequiel Salas
 */
public class NimModelProxy {
    private Socket socket;
    private DataOutputStream out;;
    private NimGame game;
    private DataInputStream in;
    private String name;

    /**
     * Create a proxy with given socket and name
     * @param socket
     * @param name
     * @throws IOException
     */
    public NimModelProxy(Socket socket, String name) throws IOException {
        this.name = name;
        this.socket = socket;
        socket.setTcpNoDelay(true);
        out = new DataOutputStream (socket.getOutputStream());
        in = new DataInputStream (socket.getInputStream());
    }

    /**
     * Set the game to be used and start listening for responses
     * @param modelListener
     */
    public void setGame (NimGame modelListener) {
        this.game = modelListener;
        new ReaderThread().start();
    }

    /**
     * Send a move request to server
     * @param pile
     * @param pos
     * @param num
     * @return Boolean, according if message was sent
     */
    public boolean moveRequest(int pile, int pos, int num){
        if (game.validMove(pile,pos,num)){
            try {
                out.writeByte ('M');
                out.writeByte (pile);
                out.writeByte (pos);
                out.writeByte(num);
                out.flush();
                return true;
            }
            catch (IOException e)
            {
                System.err.printf ("ModelProxy: %s%n", e);
                System.exit (1);
            }
        }
        else{
            System.out.println("Illegal move try again");
            System.out.print("Your turn > ");
        }
        return false;

    }

    /**
     * Prompt server to start a new game
     */
    public void newGame(){
        try{
            out.writeByte('R');
            out.flush();
        }
        catch (IOException e){
            System.err.printf("ModelProxy-newGame: %s%n",e);
            System.exit(1);
        }
    }

    /**
     * Prompt server to quit the game
     */
    public void quitGame(){
        try{
            out.writeByte('Q');
            out.flush();
            //or just have the server send a Q to both clients
            //socket.close();
        }catch (IOException e){
            System.err.printf("ModelProxy-quit: %s%n",e);
            System.exit(1);
        }
    }

    /**
     * Print help for client
     */
    public void help(){

        System.out.println("Command     Example");
        System.out.println("h   display this message");
        System.out.println("q   quit the game");
        System.out.println("n   restart the game");
        System.out.println("q# i# p#   remove q# pins starting at index i# from pile p#\n" +
                "\tCommands use 0-based indexing.\n");
        System.out.print("Your turn > ");
        //
    }

    /**
     * Send a message to server that the player has joined.
     */
    public void join(){
        try{
            out.writeByte('J');
            out.writeUTF(name);
            out.flush();
        }
        catch (IOException e){
            System.err.printf("ModelProxy-join: %s%n",e);
            System.exit(1);
        }
    }


    /**
     * Private Thread object that listens for messages from
     * the server and calls the appropriate methods client side.
     */
    private class ReaderThread
            extends Thread
    {
        public void run()
        {
            try
            {
                for (;;)
                {
                    int pile, pos, size;
                    byte b = in.readByte();
                    switch (b)
                    {
                        case 'S':
                            int n = in.readByte();
                            int j = 0;
                            int[] a = new int[n];
                            while(j<n){
                                a[j] = in.readByte();
                                j++;
                            }
                            //System.out.println(Arrays.toString(a));
                            game.setBoard(a);
                            break;
                        case 'M':
                            pile = in.readByte();
                            pos = in.readByte();
                            size = in.readByte();
                            //System.out.println("move made message");
                            game.modifyBoard(pile,pos,size);
                            break;
                        case 'R':
                            game.resetBoard();
                            break;
                        case 'Q':
                            //end game. prob just close socket and send some message to sdout.
                            game.close();
                            //socket.close();
                            break;
                        case 'P':
                            game.waiting();
                            break;
                        case 'T':
                            game.printBoard();
                            game.yourTurn();
                            break;
                        case 'W':
                            game.win();
                            //or whatever to close connection
                            game.close();
                            //socket.close();
                            break;
                        case 'L':
                            game.loss();
                            //or whatever to close connection
                            game.close();
                            //socket.close();
                            //System.exit(1);
                            break;
                        default:
                            System.err.println("Bad Message");
                            System.exit(1);
                            break;
                    }
                }
            }
            catch (EOFException exc)
            {
            }
            catch (IOException exc)
            {
                System.err.println(exc);
                System.exit(1);
            }
            finally
            {
                try
                {
                    socket.close();
                }
                catch (IOException exc)
                {
                }
            }
        }
    }
}
