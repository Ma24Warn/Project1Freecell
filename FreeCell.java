import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Driver class for the Freecell game.
 *
 * @author Dan DiTursi
 * @version 4 February 2023
 */
public class FreeCell
{
    public FreeCell() {}

    public static void main(String args[]) throws FileNotFoundException {
        ArrayList<Action> m = new ArrayList<>();
        /* 
        GameState game = new GameState();
        game.display();
        ArrayList<Action> moves = game.getLegalActions();
        for (Action a : moves) {
            System.out.println(a.toDisplayString());
        }
        Action a1 = moves.get(moves.size()-2);
        GameState g2 = game.nextState(a1);
        System.out.println();
        g2.display();
        moves = g2.getLegalActions();
        for (Action a : moves) {
            System.out.println(a.toDisplayString());
        }
        Action a2 = moves.get(moves.size()-3);
        GameState g3 = g2.nextState(a2);
        System.out.println();
        g3.display();
        moves = g3.getLegalActions();
        for (Action a : moves) {
            System.out.println(a.toDisplayString());
        }
        */

        GameState game = new GameState("testCases/case_small_4.txt");

        m = solve(game);
        m.toString();

        //THIS NEEDS TO CALL THE BELOW METHOD AND PRINT IT I THINK. WHAT DOES ABOVE DO?



    }

    public static ArrayList<Action> solve(GameState gs) {
        ArrayList<Action> moves = new ArrayList<>();

        moves = GameState.aStarSearch(gs);

        //I THINK THE A* WILL SEND ME THE ARRAYLIST? I JUST RETURN THAT AGAIN?

        return moves;
    }
}
