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
    Maze maze;
    Player player;
    Tardis tardis;
    GameCamera gameCamera;
    GameLoop gameLoop;
    Angel angel;

    private boolean DEBUG = false;

    final private double END_GAME_RANGE =0.3;
    private boolean paused  =false;

    HashSet<KeyCode> keySet;

    GraphicsContext gc;

    public static void main(String[] args) {
        launch(args);
    }

    public void init_actions(){
        if (DEBUG) DEBUG=true;
        maze = DEBUG? new Maze():new Maze(25);
        tardis = new Tardis(maze);
        player = new Player(maze, tardis);
        angel = new Angel(maze);
        paused=false;
    }

    public void init() {
        init_actions();
    }

    public void start(Stage stage) {
        stage.setTitle("Find the data.Tardis");

        stage.setOnCloseRequest((WindowEvent event) -> Platform.exit());

        FlowPane rootNode = new FlowPane();
        Scene scene = new Scene(rootNode, 700, 700);
        stage.setScene(scene);

        keySet = new HashSet<>();
        scene.setOnKeyPressed((KeyEvent event) -> keySet.add(event.getCode()));
        scene.setOnKeyReleased((KeyEvent event) -> keySet.remove(event.getCode()));


        Canvas canvas = new Canvas(700, 700);
        gc = canvas.getGraphicsContext2D();

        gameCamera = new GameCamera(gc, 350, player.FIELD_OF_VIEW,DEBUG);

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
                        gameCamera.buildScreen(maze, player, angel);
                        angel.update(gameCamera, maze, player);
                    }
                    if (Maze.distenceBetween(player.coords,tardis.coords)<END_GAME_RANGE*2){
                        paused=true;
                        gameCamera.endGameScreen("You won!");
                        endGameUpdate(keySet);

                    }
                    if (Maze.distenceBetween(player.coords,angel.coords)<END_GAME_RANGE){
                        paused=true;
                        gameCamera.endGameScreen("You lose!");
                        endGameUpdate(keySet);
                    }

                    return null;
                }
            };
        }

        private void endGameUpdate(HashSet<?> keySet){
            if (keySet.contains(KeyCode.DIGIT1)){
                init_actions();
            }
            if (keySet.contains(KeyCode.DIGIT2)){
                Platform.exit();
            }
        }
    }

    public void stop() {

    }



}
