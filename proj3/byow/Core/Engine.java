package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.algs4.StdDraw;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;
    private TETile[][] world;
    private WorldGenerator worldGenerator;
    private boolean quitGame;
    private long seed;


    public Engine() {
        world = new TETile[WIDTH][HEIGHT];
        worldGenerator = null;
        quitGame = false;
    }
    public static void main(String[] args) { }
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        while (!quitGame) {
            // Display menu and listen for key inputs
            WorldGenerator.displayMenu();

            if (StdDraw.hasNextKeyTyped()) {
                char choice = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (choice == 'N') {
                    long sed =  WorldGenerator.getSeedFromUser();
                    worldGenerator = new WorldGenerator(sed);
                    world = worldGenerator.generateWorld();
                    quitGame = false;
                } else if (choice == 'L') {
                    // Load game logic
                    // Call worldGenerator.loadGameState() and handle loaded world
                    quitGame = false;
                } else if (choice == 'Q') {
                    // Quit the game
                    quitGame = true;
                }
            }

            // Handle keyboard inputs for movement if the game is not quitting
            if (!quitGame) {
                if (StdDraw.hasNextKeyTyped()) {
                    char move = Character.toUpperCase(StdDraw.nextKeyTyped());
                    if (move == ':') {
                        // Check if the next character is 'Q' to quit the game
                        if (StdDraw.hasNextKeyTyped() && Character.toUpperCase(StdDraw.nextKeyTyped()) == 'Q') {
                            // Save the game state and quit
                            worldGenerator.saveGameState(world);
                            quitGame = true;
                        }
                    } else {
                        // Move the avatar and render the frame
                        worldGenerator.moveAvatar(world, move);
                        ter.renderFrame(world);
                    }
                }
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, running both of these:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        //create string intializing an empty string nad loop through

        //TETile[][] finalWorldFrame = null;
        //return finalWorldFrame;
        // Extract the seed from the input string
        // Extract the seed from the input string
        if (input.isEmpty()) {
            System.out.println("Invalid input");
            return null;
        }

        if (input.charAt(0) != 'n') {
            System.out.println("Invalid input: Missing seed");
            return null;
        }

        StringBuilder seedBuilder = new StringBuilder();
        int seedEndIndex = 0;
        for (int i = 1; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (Character.isDigit(ch)) {
                seedBuilder.append(ch);
            } else if (ch == 's') {
                seedEndIndex = i;
                break;
            } else {
                System.out.println("Invalid input: Invalid character");
                return null;
            }
        }

        if (seedBuilder.length() == 0) {
            System.out.println("Invalid input: Missing seed digits");
            return null;
        }

        long inputSeed = Long.parseLong(seedBuilder.toString());
        worldGenerator = new WorldGenerator(inputSeed);
        world = worldGenerator.generateWorld();

        if (input.endsWith(":q")) {
            if (worldGenerator != null) {
                worldGenerator.saveGameState(world);
            }
            return world;
        }

        for (int i = seedEndIndex; i < input.length(); i++) {
            char command = Character.toUpperCase(input.charAt(i));
            if (command == 'W' || command == 'A' || command == 'S' || command == 'D') {
                worldGenerator.moveAvatar(world, command);
            } else {
                System.out.println("Invalid command: " + command);
            }
        }

        return world;
    }
}

