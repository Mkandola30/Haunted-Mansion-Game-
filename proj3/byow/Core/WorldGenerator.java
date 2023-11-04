package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.util.Scanner;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 50;
    private static final int ROOM_MIN_WIDTH = 5;
    private static final int ROOM_MAX_WIDTH = 20;
    private static final int ROOM_MIN_HEIGHT = 5;
    private static final int ROOM_MAX_HEIGHT = 20;
    private int avatarX;
    private int avatarY;
    private static final char ARROW_UP = 'W';
    private static final char ARROW_DOWN = 'S';
    private static final char ARROW_LEFT = 'A';
    private static final char ARROW_RIGHT = 'D';

    private Random random;
    private boolean hasWon;
    private TETile[][] savedWorld;
    private long savedSeed;
    private TETile[][] loadedWorld;
    private TERenderer ter;
    private static final String SAVE_FILENAME = "savedWorld.ser";
    private static final String SAVED_STATE_FILENAME = "saved_state.txt";
    private int lightRadius;
    private boolean isLightOn;
    private String avatarName;


    public WorldGenerator(long seed) {
        random = new Random(seed);
        hasWon = false;
        savedSeed = seed;
        loadedWorld = null;
        lightRadius = 5;  // Set the initial light radius
        isLightOn = true; // Set the initial state of the light
        ter = new TERenderer();  // Initialize TERenderer here
        ter.initialize(WIDTH, HEIGHT);
    }

    public TETile[][] loadGameState() {
        try (Scanner scanner = new Scanner(new File(SAVED_STATE_FILENAME))) {
            long loadedSeed = Long.parseLong(scanner.nextLine());
            String[] positionTokens = scanner.nextLine().split(" ");
            int loadedPlayerX = Integer.parseInt(positionTokens[0]);
            int loadedPlayerY = Integer.parseInt(positionTokens[1]);

            // Regenerate the world using the loaded seed
            random = new Random(loadedSeed);
            loadedWorld = generateWorld();

            // Update the player's position
            avatarX = loadedPlayerX;
            avatarY = loadedPlayerY;

            // Initialize TERenderer and render the loaded world
            ter.initialize(WIDTH, HEIGHT);
            renderGame(loadedWorld, lightRadius);
        } catch (FileNotFoundException e) {
            // Handle the exception
            e.printStackTrace();
        }
        return loadedWorld;
    }
    public void clearLoadedWorld() {
        loadedWorld = null;
    }


    public void saveGameState(TETile[][] world) {
        // Save the random seed and player position to a file
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVED_STATE_FILENAME))) {
            writer.println(savedSeed);
            writer.println(avatarX + " " + avatarY); // Use avatarX and avatarY
        } catch (IOException e) {
            System.err.println("Error saving game state: " + e.getMessage());

        }
    }

    public static TETile[][] copyWorld(TETile[][] source) {
        if (source == null) {
            return null; // Return null if the source is null
        }
        TETile[][] copy = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                copy[x][y] = source[x][y];
            }
        }
        return copy;
    }
    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        WorldGenerator generator = new WorldGenerator(0);

        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        while (true) {
            displayMenu();
            if (StdDraw.hasNextKeyTyped()) {
                char choice = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (choice == 'N') {
                    // Start new game
                    long seed = getSeedFromUser();
                    generator = new WorldGenerator(seed);

                    // Ask for the avatar name
                    StdDraw.clear(StdDraw.BLACK);
                    StdDraw.setPenColor(StdDraw.WHITE);
                    StdDraw.setFont(new Font("Arial", Font.BOLD, 20));
                    StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Enter Avatar Name:");
                    StdDraw.show();

                    String avatarName = getAvatarNameFromUser(); // Get the avatar name
                    generator.avatarName = avatarName;

                    world = generator.generateWorld();
                    generator.saveGameState(world);
                    generator.interactWithKeyboard(world);
                } else if (choice == 'L') {
                    if (new File(SAVE_FILENAME).exists()) {
                        generator.loadGameState();
                        generator.interactWithKeyboard(generator.loadedWorld); // Start interaction with loaded world
                    }
                } else if (choice == 'Q') {
                    // Quit the game
                    System.exit(0);
                }
            }
        }
    }
    public static String getAvatarNameFromUser() {
        StringBuilder name = new StringBuilder();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == '\n') { // Enter key
                    return name.toString();
                } else if (key == '\b' && name.length() > 0) { // Backspace key
                    name.deleteCharAt(name.length() - 1);
                } else {
                    name.append(key);
                }

                StdDraw.clear(StdDraw.BLACK);
                StdDraw.setPenColor(StdDraw.WHITE);
                StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Enter Avatar Name: " + name.toString());
                StdDraw.show();
            }
        }
    }
    private void renderGame (TETile[][]world,int lightRadius){
            // Calculate the boundaries for rendering based on the player's position and light radius
            int renderMinX = Math.max(0, avatarX - lightRadius);
            int renderMaxX = Math.min(WIDTH - 1, avatarX + lightRadius);
            int renderMinY = Math.max(0, avatarY - lightRadius);
            int renderMaxY = Math.min(HEIGHT - 1, avatarY + lightRadius);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 12));
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.textLeft(1, HEIGHT - 2, "Avatar Name: " + avatarName);

        // Initialize the visibleWorld array with a default tile
            TETile[][] visibleWorld = new TETile[WIDTH][HEIGHT];
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    visibleWorld[x][y] = Tileset.NOTHING; // Set the default tile here
                }
            }

            // Copy the visible portion of the world within the light radius
            for (int x = renderMinX; x <= renderMaxX; x++) {
                for (int y = renderMinY; y <= renderMaxY; y++) {
                    visibleWorld[x][y] = world[x][y];
                }
            }

            ter.renderFrame(visibleWorld);
        }
    public static long getSeedFromUser() {
        StringBuilder seedStr = new StringBuilder();
        StdDraw.clear(StdDraw.BLACK); // Set the background to black
        StdDraw.setPenColor(StdDraw.WHITE); // Set the text color to white
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Enter Seed:");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == '\n' || key == 's' || key == 'S') { // Allow 's' or 'S' to proceed
                    try {
                        long seed = Long.parseLong(seedStr.toString());
                        StdDraw.clear(StdDraw.BLACK); // Clear the background to black
                        StdDraw.setPenColor(StdDraw.WHITE); // Set the text color to white
                        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Seed: " + seedStr.toString());
                        StdDraw.show();
                        StdDraw.pause(1000); // Display the seed for 1 second
                        return seed;
                    } catch (NumberFormatException e) {
                        displayErrorMessage("Invalid input. Please enter a valid number.");
                    }
                } else if (Character.isDigit(key)) {
                    seedStr.append(key);
                    StdDraw.clear(StdDraw.BLACK); // Clear the background to black
                    StdDraw.setPenColor(StdDraw.WHITE); // Set the text color to white
                    StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Enter Seed: " + seedStr.toString());
                    StdDraw.show();
                }
            }
        }
    }
    public String getTileNameAtMousePosition(int mouseX, int mouseY, TETile[][] world) {
        if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
            TETile tile = world[mouseX][mouseY];
            if (tile != null) {
                return tile.description(); // Return the tile's description (name)
            }
        }
        return "";
    }
    private static void displayErrorMessage(String message) {
        StdDraw.clear(StdDraw.BLACK); // Clear the background to black
        StdDraw.setPenColor(StdDraw.RED); // Set the text color to red for error message
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, message);
        StdDraw.show();
        StdDraw.pause(1000); // Display the error message for 1 second
    }
    public static void displayMenu() {
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 20)); // Set the font size to 20
        StdDraw.text(WIDTH / 2.0, HEIGHT - 10, "New Game (N)");
        StdDraw.text(WIDTH / 2.0, HEIGHT - 15, "Load Game (L)");
        StdDraw.text(WIDTH / 2.0, HEIGHT - 20, "Enter Avatar Name (E)"); // New option
        StdDraw.text(WIDTH / 2.0, HEIGHT - 25, "Quit Game (Q)");
        StdDraw.show();
    }

    private void initializeWorld(TETile[][] world) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }
    // Method to check if the player has won the game
    public boolean hasPlayerWon() {
        return hasWon;
    }

    public TETile[][] generateWorld() {
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        initializeWorld(world);
        //Generate a to b room
        int numRooms = RandomUtils.uniform(random, 20, 55);
        List<Room> rooms = new ArrayList<>();

        for (int i = 0; i < numRooms; i++) {
            int roomWidth = RandomUtils.uniform(random, ROOM_MIN_WIDTH, ROOM_MAX_WIDTH + 1);
            int roomHeight = RandomUtils.uniform(random, ROOM_MIN_HEIGHT, ROOM_MAX_HEIGHT + 1);
            int x = RandomUtils.uniform(random, 1, WIDTH - roomWidth - 1);
            int y = RandomUtils.uniform(random, 1, HEIGHT - roomHeight - 1);

            Room newRoom = new Room(x, y, roomWidth, roomHeight);
            boolean overlaps = false;

            //check if the new room overlaps with existing rooms
            for (Room room : rooms) {
                if (newRoom.intersects(room)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                rooms.add(newRoom);

                // Draw the room walls and floors
                for (int row = y; row < y + roomHeight; row++) {
                    for (int col = x; col < x + roomWidth; col++) {
                        if (row == y || row == y + roomHeight - 1 || col == x || col == x + roomWidth - 1) {
                            world[col][row] = Tileset.WALL;
                        } else {
                            world[col][row] = Tileset.FLOOR;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < rooms.size() - 1; i++) {
            Room currentRoom = rooms.get(i);
            Room nextRoom = rooms.get(i + 1);
            connectRooms(world, currentRoom, nextRoom);
        }

        // Place a door in one of the rooms (we'll choose a random room)
        Room randomRoom = rooms.get(RandomUtils.uniform(random, rooms.size()));
        Position treasurePosition = placeTreasure(world, randomRoom);

        // Place the avatar in one of the rooms
        Room avatarRoom = rooms.get(RandomUtils.uniform(random, rooms.size()));
        avatarX = RandomUtils.uniform(random, avatarRoom.x + 1, avatarRoom.x + avatarRoom.width - 1);
        avatarY = RandomUtils.uniform(random, avatarRoom.y + 1, avatarRoom.y + avatarRoom.height - 1);
        world[avatarX][avatarY] = Tileset.AVATAR;
        return world;
    }
    public void interactWithKeyboard(TETile[][] world) {
        ter.renderFrame(world);

        while (true) {
            int mouseX = (int) StdDraw.mouseX();
            int mouseY = (int) StdDraw.mouseY();
            String tileName = getTileNameAtMousePosition(mouseX, mouseY, world);

            // Clear the previous tile name display
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.filledRectangle(0, HEIGHT - 1, WIDTH, 1);

            // Display the tile name on the screen
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.textLeft(1, HEIGHT - 1, tileName);
            StdDraw.show();

            if (StdDraw.hasNextKeyTyped()) {
                char firstChar = StdDraw.nextKeyTyped();
                if (firstChar == ':') {
                    if (StdDraw.hasNextKeyTyped()) {
                        char secondChar = Character.toUpperCase(StdDraw.nextKeyTyped());
                        if (secondChar == 'Q') {
                            // Save the game state and quit
                            saveGameState(world);
                            System.exit(0);
                        }
                    }
                } else if (firstChar == 'Q' || firstChar == 'q') {
                    // Save the game state and quit
                    saveGameState(world);
                    System.exit(0);
                } else if (firstChar == 'T' || firstChar == 't') {
                    // Toggle light on/off
                    isLightOn = !isLightOn;
                    renderGame(world, lightRadius);
                } else {
                    // Move the avatar and render the frame
                    moveAvatar(world, firstChar);
                    renderGame(world, lightRadius); // Pass the avatar's position
                    ter.renderFrame(world);

                    // Handle the player winning condition after moving
                    if (hasPlayerWon()) {
                        // Display victory message and return to menu
                        StdDraw.clear();
                        ter.renderFrame(world);
                        StdDraw.setPenColor(StdDraw.WHITE);
                        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Congratulations! You've found the treasure and won the game!");
                        StdDraw.show();
                        StdDraw.pause(1000);
                        displayMenu(); // Return to the main menu
                        break;
                    } else if (firstChar == 'E' || firstChar == 'e') {
                        // Enter the avatar's name
                        enterAvatarName();
                        renderGame(world, lightRadius);
                    }
                }
            }
        }
    }
    private void enterAvatarName() {
        StringBuilder name = new StringBuilder();
        boolean nameEntered = false;

        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 20));
        StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Enter Avatar Name:");

        while (!nameEntered) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == '\n') { // Enter key
                    nameEntered = true;
                } else if (key == '\b' && name.length() > 0) { // Backspace key
                    name.deleteCharAt(name.length() - 1);
                } else {
                    name.append(key);
                }

                StdDraw.clear(StdDraw.BLACK);
                StdDraw.setPenColor(StdDraw.WHITE);
                StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "Enter Avatar Name: " + name.toString());
                StdDraw.show();
            }
        }

        avatarName = name.toString();
    }

        private Position placeTreasure (TETile[][]world, Room room){
            int treasureX = 0;
            int treasureY = 0;

            // Generate the treasure's position within the room (not on the walls)
            treasureX = RandomUtils.uniform(random, room.x + 1, room.x + room.width - 1);
            treasureY = RandomUtils.uniform(random, room.y + 1, room.y + room.height - 1);

            world[treasureX][treasureY] = Tileset.TREASURE; // Place the treasure in the room.
            return new Position(treasureX, treasureY);
        }
    private void connectRooms(TETile[][] world, Room room1, Room room2) {
        // Find the center coordinates of each room
        int x1 = room1.x + room1.width / 2;
        int y1 = room1.y + room1.height / 2;
        int x2 = room2.x + room2.width / 2;
        int y2 = room2.y + room2.height / 2;

        // Use a pathfinding algorithm to find a path between the centers of two adjacent rooms
        List<Position> path = findPath(world, x1, y1, x2, y2);

        // If the path is not empty, draw the hallway tiles between the two rooms
        if (!path.isEmpty()) {
            for (Position pos : path) {
                world[pos.x][pos.y] = Tileset.FLOOR;

                // Add walls around the hallway tiles
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = pos.x + dx;
                        int ny = pos.y + dy;
                        if (world[nx][ny] == Tileset.NOTHING) {
                            world[nx][ny] = Tileset.WALL;
                        }
                    }
                }
            }
        }
    }
    public void moveAvatar(TETile[][] world, char move) {
        int newX = avatarX;
        int newY = avatarY;

        if (move == ARROW_UP || move == 'W') {
            newY = Math.min(newY + 1, HEIGHT - 1);
        } else if (move == ARROW_DOWN || move == 'S') {
            newY = Math.max(newY - 1, 0);
        } else if (move == ARROW_LEFT || move == 'A') {
            newX = Math.max(newX - 1, 0);
        } else if (move == ARROW_RIGHT || move == 'D') {
            newX = Math.min(newX + 1, WIDTH - 1);
        }

        if (!world[newX][newY].equals(Tileset.WALL)) {
            // Check if the new position contains the treasure
            if (world[newX][newY].equals(Tileset.TREASURE)) {
                hasWon = true;
            }

            // Clear the previous avatar position
            world[avatarX][avatarY] = Tileset.FLOOR;
            // Update the avatar's position
            avatarX = newX;
            avatarY = newY;
            // Place the avatar at the new position
            world[avatarX][avatarY] = Tileset.AVATAR;
        }
    }
    // A helper class to represent positions on the grid
    private static class Position {
        private int x;
        private int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    private List<Position> findPath(TETile[][] world, int x1, int y1, int x2, int y2) {
        // Implement a pathfinding algorithm here (e.g., A* or BFS)
        // to find a path between (x1, y1) and (x2, y2).
        // Return the list of positions representing the path.
        // For simplicity, you can assume that the positions are adjacent in this grid-based world.
        // You can also implement your own pathfinding algorithm or use a library.

        // For simplicity, let's just use a simple straight path between the centers of the rooms
        List<Position> path = new ArrayList<>();
        int dx = x2 - x1;
        int dy = y2 - y1;
        // Horizontal segment
        for (int x = x1; x != x2; x += Integer.signum(dx)) {
            path.add(new Position(x, y1));
        }
        // Vertical segment
        for (int y = y1; y != y2; y += Integer.signum(dy)) {
            path.add(new Position(x2, y));
        }

        return path;
    }
    private static class Room {
        private int x;
        private int y;
        private int width;
        private int height;

        public Room(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean intersects(Room other) {
            return x < other.x + other.width && x + width > other.x
                    && y < other.y + other.height && y + height > other.y;
        }
    }
}



