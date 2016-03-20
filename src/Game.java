import data.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.concurrent.*;
import java.util.HashSet;

public class Game extends Application {
    final private double END_GAME_RANGE = 0.3;

    private boolean DEBUG = false;
    private boolean paused = false;
    private Maze maze;
    private Player player;
    private Tardis tardis;
    private GameCamera gameCamera;
    private GameLoop gameLoop;
    private Angel[] angels;
    private HashSet<KeyCode> keySet;
    private GraphicsContext gc;


    public static void main(String[] args) {
        launch(args);
    }


    public void init_actions() {
        if (DEBUG) DEBUG = true;
        maze = DEBUG ? new Maze() : new Maze(20);
        tardis = new Tardis(maze, DEBUG);
        player = new Player(maze, tardis);
        angels = new Angel[Angel.NUMBER_OF_ANGELS];
        for (int i = 0; i < Angel.NUMBER_OF_ANGELS; i++) {
            angels[i] = new Angel(maze);
        }
        paused = false;
    }


    public void init() {
        init_actions();
    }


    public void start(Stage stage) {
        stage.setTitle("Find the Tardis");

        stage.setOnCloseRequest((WindowEvent event) -> Platform.exit());

        FlowPane rootNode = new FlowPane();
        Scene scene = new Scene(rootNode, 1200, 675);
        stage.setScene(scene);

        keySet = new HashSet<>();
        scene.setOnKeyPressed((KeyEvent event) -> keySet.add(event.getCode()));
        scene.setOnKeyReleased((KeyEvent event) -> keySet.remove(event.getCode()));

        Canvas canvas = new Canvas(1200, 675);
        gc = canvas.getGraphicsContext2D();

        gameCamera = new GameCamera(gc, 600, player.FIELD_OF_VIEW, DEBUG);

        gameLoop = new GameLoop();
        gameLoop.start();

        gameLoop.setOnSucceeded((WorkerStateEvent event) -> {
            gameLoop.restart();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        rootNode.getChildren().add(canvas);
        stage.show();
    }


    private class GameLoop extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    if (!paused) {
                        player.update(keySet, maze);
                        gameCamera.buildScreen(maze, player, angels);
                        Angel.update(angels, gameCamera, maze, player);
                        tardis.update(keySet, maze, player);
                    }
                    if (Maze.distenceBetween(player.coords, tardis.coords) < END_GAME_RANGE * 2 && tardis.isHere) {
                        paused = true;
                        gameCamera.endGameScreen("You won!");
                        endGameUpdate(keySet);

                    }
                    double nearestAngelDist = Double.POSITIVE_INFINITY;
                    for (Angel angel : angels)
                        nearestAngelDist = Math.min(nearestAngelDist, Maze.distenceBetween(player.coords, angel.coords));
                    if (nearestAngelDist < END_GAME_RANGE) {
                        paused = true;
                        gameCamera.endGameScreen("You lose!");
                        endGameUpdate(keySet);
                    }
                    return null;
                }
            };
        }


        private void endGameUpdate(HashSet<?> keySet) {
            if (keySet.contains(KeyCode.DIGIT1)) {
                init_actions();
            }
            if (keySet.contains(KeyCode.DIGIT2)) {
                Platform.exit();
            }
        }
    }


    public void stop() {
    }
}
