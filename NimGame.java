package Nim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Holds game board for client side program.
 *
 * @Author: Ezequiel Salas
 */
public class NimGame{
    ArrayList<int[]> board = new ArrayList<>();
    int[] og = {3,4,5};
    boolean turn;
    NimModelProxy proxy;

    /**
     * Default board is {3,4,5}
     */
    public NimGame(){
        int[] a = new int[]{1,1,1};
        int[] b = new int[]{1,1,1,1};
        int[] c = new int[]{1,1,1,1,1};
        board.add(a);
        board.add(b);
        board.add(c);
    }
//    public NimGame(int[] piles){
//        for (int i: piles){
//            int[] a = new int[i];
//            Arrays.fill(a,1);
//            board.add(a);
//        }
//        og=piles;
//    }

    /**
     * Create and return a NimGame
     * And also create a scanner thread to
     * read user input
     * @return NimGame
     */
    public static NimGame create(){
        NimGame game = new NimGame();
        ref r = new ref();
        r.game = game;
        r.start();
        return game;
    }

    /**
     * Private thread object that listens
     * to user input and calls the proper
     * proxy methods.
     */
    private static class ref extends Thread{
        public NimGame game;
        private Scanner input;

        @Override
        public void run() {
            input = new Scanner(System.in);
            while (input.hasNextLine()){
                if (game.turn){
                    String cmd = input.nextLine();
                    if (cmd.startsWith("q"))game.proxy.quitGame();
                    else if (cmd.startsWith("n"))game.proxy.newGame();
                    else if (cmd.startsWith("h"))game.proxy.help();
                    else{
                        try{
                            String[] a = cmd.split(" ");
                            int p,n,s;
                            p = Integer.parseInt(a[0]);
                            n = Integer.parseInt(a[1]);
                            s = Integer.parseInt(a[2]);
                            game.proxy.moveRequest(p,n,s);
                        }
                        catch (NumberFormatException e){
                            System.out.println("Error Parsing Input - type h for help");
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            System.out.println("Error Parsing Input - type h for help");
                        }
                    }
                }
            }
        }
    }

    /**
     * Set proxy
     * @param proxy
     */
    public void setProxy(NimModelProxy proxy){
        this.proxy = proxy;
    }

    /**
     * Set board to a different pile
     * @param piles
     */
    public void setBoard(int[] piles){
        board.clear();
            for (int i: piles){
                int[] a = new int[i];
                Arrays.fill(a,1);
                board.add(a);
            }
        og=piles;
    }

    /**
     * Check if a move is valid
     * @param pile
     * @param pos
     * @param num
     * @return boolean
     */
    public boolean validMove(int pile, int pos, int num){
        if (pile<0 || pile>board.size()) return false;
        if (pos<0 || pos+num>board.get(pile).length) return false;
        if (num<1 || num>board.get(pile).length) return false;
        return true;
    }

    /**
     * Modify board and create new piles if necessary
     * @param pile
     * @param pos
     * @param num
     */
    public void modifyBoard(int pile, int pos, int num){
        int j = 0;
        while (j<num){
            board.get(pile)[pos+j] = 0;
            j+=1;
        }
        j=0;
        int l = 0,r = 0;
        boolean f = false;
        while(j<board.get(pile).length){
            if(!f && board.get(pile)[j]==1) l+=1;
            else if(f && board.get(pile)[j]==1)r+=1;
            else{ f=true;}
            j++;
        }
        int[] left = new int[l];
        int[] right = new int[r];
        Arrays.fill(left,1);
        Arrays.fill(right,1);
        board.remove(pile);
        if (right.length!=0)board.add(pile,right);
        if (left.length!=0)board.add(pile,left);

    }

    /**
     * Reset board to starting state
     */
    public void resetBoard(){
        System.out.println("New game started!");
        board.clear();
        for (int i:og){
            int[] a =new int[i];
            Arrays.fill(a,1);
            board.add(a);
        }
    }

    /**
     * Alert player to wait for opponent
     */
    public void waiting(){
        System.out.println("Waiting for opponent...");
        turn=false;
    }

    /**
     * Alert player of their turn
     */
    public void yourTurn(){
        System.out.print("Your turn > ");
        turn=true;
    }

    /**
     * Alert player of their win
     */
    public void win(){
        System.out.println("You won congrats!");
    }

    /**
     * Alert player of their loss
     */
    public void loss(){
        System.out.println("Sadly you lost. :(");
    }

    /**
     * Close the program
     */
    public void close(){
        System.exit(0);
    }

    /**
     * Print the board.
     */
    public void printBoard(){
        System.out.print("Piles: ");
        for (int[] b: board){
            System.out.print(b.length+ " ");
        }
        System.out.print("\n");
    }

}