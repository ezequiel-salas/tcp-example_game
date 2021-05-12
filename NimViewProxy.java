package Nim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Nim server side proxy
 *
 * @Author: Ezequiel Salas
 */
public class NimViewProxy {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private NimModel listener;

    /**
     * Contructor
     * @param socket
     */
    public NimViewProxy
            (Socket socket)
    {
        try
        {
            this.socket = socket;
            socket.setTcpNoDelay (true);
            out = new DataOutputStream (socket.getOutputStream());
            in = new DataInputStream (socket.getInputStream());
        }
        catch (IOException exc)
        {
            System.err.printf ("ViewProxy: %s%n", exc);
            System.exit (1);
        }
    }

    /**
     * Set the model and start up a Thread to listen
     * for messages
     * @param model
     */
    public void setListener(NimModel model){
        listener=model;
        new ReaderThread() .start();
    }

    /**
     * Send message to reset game
     */
    public void resetGame(){
        try {
            out.writeByte('R');
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }

    /**
     * Send message to quit game
     */
    public void quitGame(){
        try{
            out.writeByte('Q');
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }

    /**
     * Notify player to wait their turn
     */
    public void opponentTurn(){
        try{
            out.writeByte('P');
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }

    /**
     * Notify player of their turn
     */
    public void yourTurn(){
        try{
            out.writeByte('T');
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }

    /**
     * Notify player of their win
     */
    public void winner(){
        try{
            out.writeByte('W');
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }

    /**
     * Notify player of their loss
     */
    public void loser(){
        try{
            out.writeByte('L');
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }

    /**
     * Setup board to a different piles
     * @param piles
     */
    public void setup(int[] piles){
        try{
            out.writeByte('S');
            out.writeByte(piles.length);
            int i = 0;
            while (i<piles.length){
                out.writeByte(piles[i]);
                i++;
            }
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }

    /**
     * Send a message that a move has been made
     * @param pile
     * @param pos
     * @param size
     */
    public void move(int pile, int pos, int size){
        try{
            out.writeByte('M');
            out.writeByte(pile);
            out.writeByte(pos);
            out.writeByte(size);
            out.flush();
        }
        catch (IOException e){
            System.err.printf ("ViewProxy: %s%n", e);
            System.exit (1);
        }
    }


    /**
     * Private Thread object that reads messages from clients
     */
    private class ReaderThread
            extends Thread
    {
        /**
         * Run the reader thread.
         */
        public void run()
        {
            int op, i;
            String name;
            try
            {
                for (;;)
                {
                    op = in.readByte();
                    switch (op)
                    {
                        case 'J':
                            name = in.readUTF();
                            listener.join (NimViewProxy.this, name);
                            //System.out.println(name);
                            break;
                        case 'M':
                            i = in.readByte();
                            int p,s;
                            p = in.readByte();
                            s = in.readByte();
                            //System.out.println("move receieved");
                            listener.makeMove (NimViewProxy.this, i,p,s);
                            break;
                        case 'R':
                            listener.newGame (NimViewProxy.this);
                            break;
                        case 'Q':
                            listener.quit (NimViewProxy.this);
                            break;
                        default:
                            System.err.println ("Bad Message");
                            System.exit (1);
                            break;
                    }
                }
            }
            catch (EOFException exc)
            {
            }
            catch (IOException exc)
            {
                System.err.printf ("ViewProxy: %s%n", exc);
                System.exit (1);
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
