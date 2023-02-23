import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Driver class for the Freecell game.
 *
 * @author Matthew Warner
 * @version 23 February 2023
 */
public class FreeCell
{
    public FreeCell() {}

    public static void main(String args[]) throws FileNotFoundException {
        ArrayList<Action> m = new ArrayList<>();
        

        //given test cases
        GameState game1 = new GameState("testCases/case_small_4.txt");
        GameState game2 = new GameState("testCases/case_easy_56.txt");
        GameState game3 = new GameState("testCases/case_minimal_52.txt");
        GameState game4 = new GameState("testCases/case_MS5152_82.txt");
        GameState game5 = new GameState("testCases/case_MS25_102.txt");

        //random test case
        GameState randomGame = new GameState();


        double startTime = System.currentTimeMillis();

        m = solve(game4);

        double endTime = System.currentTimeMillis();

        //print out solution
        System.out.println("Solution: ");
        System.out.println(m.toString());

        //print out total seconds it took for A* search to complete
        System.out.println("Total time taken: " + (endTime - startTime)/1000 + " seconds");


    }

    //calls the aStarSearch method with the given GameState, returning a list of actions it took to get the the
    //result state or an empty list if the result state was never reached
    public static ArrayList<Action> solve(GameState gs) {
        ArrayList<Action> moves = new ArrayList<>();

        moves = GameState.aStarSearch(gs);

        return moves;
    }
}
