package Nim;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Nim model for use in the server side application
 *
 * @Author: Ezequiel Salas
 */
public class NimModel {
    private String name1;
    private String name2;
    private NimViewProxy view1;
    private NimViewProxy view2;
    private NimViewProxy turn;
    private boolean isFinished;
    ArrayList<int[]> board = new ArrayList<>();
    int[] og;
    private boolean verbose = false;

    /**
     * Constructor for server side model
     * @param piles
     * @param verbose
     */
    public NimModel(int[] piles, boolean verbose){
        this.verbose = verbose;
        board.clear();
        for (int i: piles){
            int[] a = new int[i];
            Arrays.fill(a,1);
            board.add(a);
        }
        og=piles;
    }

    /**
     * Method called when a player has joined this game
     * @param view ProxyView of that player
     * @param name
     */
    public synchronized void join
            (NimViewProxy view,
             String name)
    {
        if (name1 == null)
        {
            name1 = name;
            view1 = view;
            view1.opponentTurn();
        }
        else
        {
            name2 = name;
            view2 = view;
            doNewGame();
        }
    }

    /**
     * create new game
     * @param proxy
     */
    public synchronized void newGame(NimViewProxy proxy){
        if (name2 != null)  doNewGame();
    }

    /**
     * Set new game and set turns
     */
    private void doNewGame()
    {
        // Clear the board and inform the players.
        resetBoard();
        view1.resetGame();
        view2.resetGame();

        // Player 1 gets the first turn.
        turn = view1;
        view1.yourTurn();
        view2.opponentTurn();

        if (verbose){
            System.out.printf("%s vs. %s start game\n",name1,name2);
        }
    }

    /**
     * Reset board to original state
     */
    public void resetBoard(){
        board.clear();
        for (int i:og){
            int[] a =new int[i];
            Arrays.fill(a,1);
            board.add(a);
        }
    }

    /**
     * Quit the game for both clients
     * @param view
     */
    public synchronized void quit
            (NimViewProxy view)
    {
        if (view1 != null)
            view1.quitGame();
        if (view2 != null)
            view2.quitGame();
        if (verbose){
            System.out.printf("%s vs. %s ending game\n",name1,name2);
        }
        turn = null;
        isFinished = true;
    }

    /**
     * Getter for finished
     * @return boolean
     */
    public synchronized boolean isFinished()
    {
        return isFinished;
    }

    /**
     * Make a move on board, create any new piles if necessary.
     * And decide the winner and notify the clients
     * @param proxy
     * @param pile
     * @param pos
     * @param size
     */
    public synchronized void makeMove(NimViewProxy proxy , int pile, int pos, int size){
        if (proxy != turn) return;
        int j = 0;
        while (j<size){
            //System.out.println(board.size());
            board.get(pile)[pos+j] = 0;
            j+=1;
        }
        j=0;
        int l = 0,r = 0;
        boolean f = false;
        while(j<board.get(pile).length){
            if(!f && board.get(pile)[j]==1) l+=1;
            else if(f && board.get(pile)[j]==1)r+=1;
            else{
                f=true;
            }
            j++;
        }
        int[] left = new int[l];
        int[] right = new int[r];
        Arrays.fill(left,1);
        Arrays.fill(right,1);
        board.remove(pile);
        if (right.length!=0)board.add(pile,right);
        if (left.length!=0)board.add(pile,left);
        //System.out.println("sending message to clients");
        view1.move(pile,pos,size);
        view2.move(pile,pos,size);

        if (verbose){
            System.out.printf("%s vs. %s new state: ",name1,name2);
            for (int[] b: board){
                System.out.print(b.length+" ");
            }
            System.out.print("\n");
        }

        if (board.size() == 1 && board.get(0).length==1){
                //flipped logic because the current proxy is the one who made the move
                if (proxy==view1){
                    view1.winner();
                    view2.loser();
                }
                else{
                    view1.loser();
                    view2.winner();
                }
                if (verbose){
                    System.out.printf("%s vs. %s ending game\n",name1,name2);
                }
        }else{
            if (proxy==view1){
                view1.opponentTurn();
                view2.yourTurn();
                turn = view2;
            }
            else{
                view1.yourTurn();
                view2.opponentTurn();
                turn = view1;
            }
            if (verbose){
                if (turn==view1){
                    System.out.printf("%s vs. %s whose turn: %s\n",name1, name2, name1);
                }
                else{
                    System.out.printf("%s vs. %s whose turn: %s\n",name1, name2, name2);
                }
            }
        }

    }

}
