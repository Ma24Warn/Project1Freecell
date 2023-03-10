import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.File;
import java.util.Scanner;
import java.util.TreeMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;

/**
 * Contains the state of a FreeCell game
 *  
 * @author Matthew Warner
 * @version 23 February 2023
 * 
 */

public class GameState
{
    // instance variables - replace the example below with your own
    private ArrayList<Card> cells; // Numbered from 0-3; empty cells will always be last
    private int numCellsFree;
    private ArrayList<ArrayList<Card>> tableau; // In actions, numbered 1-8; we adjust the -1 manually.
    private int[] foundations = {0,0,0,0,0}; // We'll ignore the first one.
    private ArrayList<Action> actions = new ArrayList<Action>(); //ArrayList to keep track of actions
    private static ArrayList<String> visited = new ArrayList<String>(); //ArrayList to keep track of visited GameStates
    


    /**
     * Creates a random deal
     */
    public GameState()
    {
        cells = new ArrayList<Card>(4);
        numCellsFree = 4;
        tableau = new ArrayList<ArrayList<Card>>(8);
        
        ArrayList<Card> deck = new ArrayList<Card>(52);
        int i,j,k;
        for (i = 1; i <= 13; i++) {
            for (j = 1; j <= 4; j++) {
                deck.add(new Card(i,j));
            }
        }
        Collections.shuffle(deck);
        
        for (i = 0; i < 8; i++) {
            ArrayList<Card> current = new ArrayList<Card>();
            k = i<4 ? 7 : 6;    // first four piles get 7 cards; last four get 6 cards
            for (j = 0; j < k; j++) {
                current.add(deck.remove(0));
            }
            tableau.add(current);
        }
    }
    
    public GameState(GameState gs) {
        cells = new ArrayList<Card>(4);
        for (Card c : gs.cells) { cells.add(c); }
        numCellsFree = gs.numCellsFree;
        tableau = new ArrayList<ArrayList<Card>>(8);
        for (int i = 0; i < 8; i++) {
            ArrayList<Card> current = new ArrayList<Card>();
            ArrayList<Card> pile = gs.tableau.get(i);
            for (Card c : pile) { current.add(c); }
            tableau.add(current);
        }
        for (int i = 0; i < 5; i++) {
            foundations[i] = gs.foundations[i];
        }
    }
    
    // Note: input string must be full file path, unless file is in current working directory
    public GameState(String filename) throws FileNotFoundException {
        File f = new File(filename);
        Scanner sc = new Scanner(f);
        
        String s1 = sc.nextLine();
        String[] S = s1.split(" ");
        for (int i = 1; i <=4; i++) {
            foundations[i] = Integer.parseInt(S[i-1]);
        }
        
        cells = new ArrayList<Card>(4);
        String s2 = sc.nextLine();
        S = s2.split(" ");
        numCellsFree = Integer.parseInt(S[0]);
        for (int i = 0; i < (4 - numCellsFree); i++) {
            String s3 = S[i+1];
            cells.add(new Card(s3.charAt(0),s3.charAt(1)));
        }
        
        tableau = new ArrayList<ArrayList<Card>>(8);
        for (int i = 0; i < 8; i++) {
            ArrayList<Card> pile = new ArrayList<Card>();
            String s4 = sc.nextLine();
            S = s4.split(" ");
            if (!S[0].equals("--")) {
                for (int j = 0; j < S.length; j++) {
                    pile.add(new Card(S[j].charAt(0),S[j].charAt(1)));
                }
            }
            tableau.add(pile);
        }

        sc.close();
    }
    
    // Note: Modifies internal state; no "undo" available
    private boolean executeAction(Action a) {
        if (!isLegalAction(a)) { return false; }
        Card c;
        if (a.fromCell()) {
            c = cells.remove(a.get_src_pile());
            numCellsFree++;
        }
        else {
            ArrayList<Card> p1 = tableau.get(a.get_src_pile()-1);
            c = p1.remove(p1.size()-1);
        }
        int d = a.get_dest_pile();
        if (d == 0) {
            cells.add(c);
            numCellsFree--;
        }
        else if (d == 9) {
            foundations[c.getSuit()] = foundations[c.getSuit()] + 1;
        }
        else {
            ArrayList<Card> p2 = tableau.get(d-1);
            p2.add(c);
        }
        return true;
    }
        
    public boolean isLegalAction(Action a) {
        int s = a.get_src_pile();
        Card c;
        ArrayList<Card> pile;
        if (a.fromCell()) {
            c = cells.get(s);
        }
        else {
            pile = tableau.get(s-1);
            if (pile.size() > 0) {
                c = pile.get(pile.size()-1);
            }
            else {
                return false;
            }
        }
        if (!c.equals(a.getCard())) { return false; }    
        int d = a.get_dest_pile();
        if (d == 0 && numCellsFree > 0) { return true; }
        if (d == 9) {
            return c.getRank() == foundations[c.getSuit()] + 1;
        }
        else {
            pile = tableau.get(d-1);
            if (pile.size() == 0) { return true; }
            if (pile.size() > 0) {
                Card last = pile.get(pile.size()-1);
                return (last.getRank() == c.getRank() + 1) && (!last.sameColor(c));
            }
        }
        return false;
    }
    
    public ArrayList<Action> getLegalActions() {
        ArrayList<Action> result = new ArrayList<Action>();
        
        // Moves from tableau to cells
        if (numCellsFree > 0) {
            for (int i = 0; i < 8; i++) {
                ArrayList<Card> pile = tableau.get(i);
                if (pile.size() > 0) {
                    Card c = pile.get(pile.size()-1);
                    result.add(new Action(false,i+1,c,0));
                }
            }
        }
        
        // Moves to tableau
        boolean foundEmpty = false;
        for (int d = 0; d < 8; d++) {
            ArrayList<Card> pile = tableau.get(d);
            // non-empty pile - check all movable cards to see if they can go here.
            if (pile.size() > 0) {
                Card top = pile.get(pile.size() - 1);
                for (int s = 0; s<cells.size(); s++) {
                    Card c2 = cells.get(s);
                    if (!top.sameColor(c2) && (top.getRank() == c2.getRank()+1)) {
                        result.add(new Action(true,s,c2,d+1));
                    }
                }
                for (int s = 0; s < 8; s++) {
                    if (s == d) { continue; }
                    ArrayList<Card> p2 = tableau.get(s);
                    if (p2.size() == 0) { continue; }
                    Card c2 = p2.get(p2.size()-1);
                    if (!top.sameColor(c2) && (top.getRank() == c2.getRank()+1)) {
                        result.add(new Action(false,s+1,c2,d+1));
                    }                    
                }
            }
            else {  // empty pile - any card can go here
                if (!foundEmpty) {
                    foundEmpty = true;
                    for (int s = 0; s<cells.size(); s++) {
                        result.add(new Action(true,s,cells.get(s),d+1));
                    }
                    for (int s = 0; s < 8; s++) {
                        if (s == d) { continue; }
                        ArrayList<Card> p2 = tableau.get(s);
                        // No point in moving a single card from one tableau pile to an empty space
                        if (p2.size() >= 2) {
                            result.add(new Action(false,s+1,p2.get(p2.size()-1),d+1));
                        }
                    }
                }
            }
        }
        
        // Moves to foundation
        for (int s = 0; s<cells.size(); s++) {
            Card c2 = cells.get(s);
            if (c2.getRank() == foundations[c2.getSuit()] + 1) {
                result.add(new Action(true,s,c2,9));
            }
        }
        for (int s = 0; s < 8; s++) {
            ArrayList<Card> p2 = tableau.get(s);
            if (p2.size() > 0) {
                Card c2 = p2.get(p2.size()-1);
                if (c2.getRank() == foundations[c2.getSuit()] + 1) {
                    result.add(new Action(false,s+1,c2,9));
                }
            }
        }
        
        return result;
    }


    public GameState nextState(Action a) {
        GameState result = new GameState(this);
        if (!result.executeAction(a)) { return null; }


        //adds previous actions + current actions to the resulting GameState's action arraylist
        result.actions.addAll(this.actions);
        result.actions.add(a);
        

        return result;
    }


    public GameState resultState(ArrayList<Action> Alist) {
        GameState result = new GameState(this);
        for (Action a : Alist) {
            if (!result.executeAction(a)) { return null; }
            result.actions.add(a); //add all of the actions to the result GameState's actions arraylist
        }

        return result;
    }
    
    public String toDisplayString() {
        String s1 = "Foundations:";
        String s2 = "";
        for (int i = 1; i <= 4; i++) {
            s2 = s2 + " " + Card.rankString.charAt(foundations[i]) + Card.suitString.charAt(i);
        }
        String s3 = "Free cells:";
        for (Card c : cells) {
            s3 = s3 + " " + c.toString();
        }
        for (int i = 0; i < numCellsFree; i++) {
            s3 = s3 + " --";
        }
        String s4 = "Tableau (piles go left to right, right is top):";
        for (int j = 0; j < 8; j++) {
            ArrayList<Card> pile = tableau.get(j);
            s4 = s4 + System.lineSeparator() + " " + (j+1) + ":";
            if (pile.size() == 0) {
                s4 = s4 + " --";
            }
            else {
                for (Card c : pile) {
                    s4 = s4 + " " + c.toString();
                }
            }
        }
        return s1 + s2 + System.lineSeparator() + s3 + System.lineSeparator() + s4;
    }
    
    // The string format for a GameState is as follows:
    //   First line: four integers representing the foundations
    //   Second line: one integer for number of free cells, followed by list of cards in cells (if any)
    //   Lines 3-10: List of cards in each tableau pile. Empty piles are represented by "--"
    public String toString() {
        String result = "";
        for (int i = 1; i <= 4; i++) {
            result = result + foundations[i] + " ";
        }
        result = result + "\n";
        
        result = result + numCellsFree + " ";
        for (int i = 0; i < cells.size(); i++) {
            result = result + cells.get(i).toString() + " ";
        }
        result = result + "\n";
                
        for (int i = 0; i < 8; i++) {
            ArrayList<Card> pile = tableau.get(i);
            if (pile.size() == 0) {
                result = result + "--";
            }
            else {
                for (Card c : pile) {
                    result = result + c.toString() + " ";
                }
            }
            result = result + "\n";
        }
        return result;
    }
    
    public void dumpToFile(String filename) {
        try {
            FileWriter fw = new FileWriter(filename);
            fw.write(toString());
            fw.close();
        }
        catch (IOException e) {
            System.out.println("file dump failed, IOException");
        }
    }
    
    public void display() {
        System.out.println(toDisplayString());
    }
    
    public boolean isWin() {
        for (int i = 1; i <=4; i++) {
            if (foundations[i] < 13) { return false; }
        }
        return true;
    }
    
    public boolean gameover() {
        return (getLegalActions().size()) == 0;
    }
    
    public boolean isLoss() {
        return !isWin() && gameover();
    }


    //heuristic function
    public static int h(GameState gs) {
        //heuristic, amount of cards needed at foundation, and extra moves required by blocker cards
        int heuristic = 0, fSize = 0, extraMoves = 0; 
        ArrayList<Card> curTab; //holds the current tableau in the GameState
        Card curCard; //holds the current card in the tableau
        int[] lastSeen = {0, 0, 0, 0, 0}; //used to remember the last seen card of each suit for a tableau



        //gets the total amount of cards in the foundations
        for (int z = 1; z <= 4; z++) {
            fSize += gs.foundations[z];
        }

        //go through each tableau
        for (int x = 0; x < 8; x++) {

                
            //reset lastSeen array to default 0 values
            for (int i = 1; i < lastSeen.length; i++) {
                lastSeen[i] = 0;
            }

            //get an ArrayList of the current tableau's cards
            curTab = gs.tableau.get(x);
            
            //go through each card in the current tableau, starting at the end
            for (int y = curTab.size()-1; y >= 0; y--) {
                
                //get the current card
                curCard = curTab.get(y);

                //This is just for the first time seeing a card of each suit so that it doesn't count an extra move
                if (lastSeen[curCard.getSuit()] == 0) {
                    lastSeen[curCard.getSuit()] = curCard.getRank();
                }


                //if the current cards rank is lower than the last seen card of its respective suit
                if (curCard.getRank() < lastSeen[curCard.getSuit()]) {

                    lastSeen[curCard.getSuit()] = curCard.getRank(); //update lastSeen array
                    extraMoves++; //add an extra move
                    
                }

            }

        }

        heuristic = extraMoves; //set heuristic equal to the amount of extra moves
        heuristic += 52-fSize; //add the remaining number of cards needed at the tableau to the heuristic
        
        return heuristic;
    }



    //A* Search Algorithm
    public static ArrayList<Action> aStarSearch(GameState gs) {
        //priority queue (uses arraylists of GameState to account for collision)
        TreeMap<Integer, ArrayList<GameState>> PQ = new TreeMap<Integer, ArrayList<GameState>>();
        ArrayList<Action> empty = new ArrayList<Action>(); //empty arraylist to return if solution is not found
        ArrayList<GameState> startState = new ArrayList<GameState>(); //arraylist to hold the starting GameState
        GameState cur, next_state; //GameState variables to hold the current GameState and next Gamestates
        int numMoves; //Holds the value of djikstras + heuristic (to keep code simpler)

        //add the starting GameState to its arraylist and add that to the priority queue
        startState.add(gs);
        PQ.put(h(gs), startState);


        while (!PQ.isEmpty()) {

            //while the arraylist at PQ's first key is empty, remove it from PQ (because an empty arraylist would stay despite being empty)
            while (PQ.get(PQ.firstKey()).size() < 1) {
                PQ.remove(PQ.firstKey());
            }

            //pop gamestate from arraylist in PQ and store it in cur
            cur = PQ.get(PQ.firstKey()).get(0);
            PQ.get(PQ.firstKey()).remove(0);

            //add the current GameState to the visited arraylist
            visited.add(cur.toString());

            //if the current GameState is a win, return its actions arraylist
            if (cur.isWin()) {
                return cur.actions;
            }

            //get all of the current GameState's legal actions and go through each of them
            ArrayList<Action> legalActions = cur.getLegalActions();
            for (Action a : legalActions) {

                //get the next GameState using the current GameState and the first legal action
                next_state = cur.nextState(a);

                //set the number of moves to the size of next_state's actions arraylist + next_state's heuristic value
                numMoves = next_state.actions.size() + h(next_state);;

                //if the visited arraylist does not already contain next_state's GameState value
                if (!visited.contains(next_state.toString())) {

                    //if PQ does not already contain a key for the current value of numMoves, create a new
                    //arraylist, store next_state in it, and add that to PQ with the new key value
                    if (!PQ.containsKey(numMoves)) {
                        ArrayList<GameState> newAL = new ArrayList<GameState>();
                        newAL.add(next_state);
                        PQ.put(numMoves, newAL);
                    }
                    //else add the next_state to the beginning of the arraylist at the key
                    //value equal to numMoves
                    else {
                        PQ.get(numMoves).add(0, next_state);
                    }
                    
                }
                
            }


        }

        //if PQ eventually becomes empty and never returns a winning arraylist, then it will return this empty arraylist
        return empty;
        
    }


}