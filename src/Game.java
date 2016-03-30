
import data.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.canvas.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.concurrent.*;

import java.awt.*;
import java.util.HashSet;

//TODO sound;
//TODO sonic screwdriver functions;
//TODO more angel textures;

public class Game extends Application {
    final private double END_GAME_RANGE = 0.3;

    private boolean debug = false;
    private boolean paused = false;
    private boolean inMenu = true;
    private int victory_counter = 0;
    private int defeat_counter = 0;
    private double lastSceneX=0,lastSceneY=0;
    private double dSceneX=0,dSceneY=0;
    private long time;
    private Maze maze;
    private Player player;
    private Tardis tardis;
    private GameCamera gameCamera;
    private GameLoop gameLoop;
    private MainMenuLoop mainMenuLoop;
    private Angel[] angels;
    private HashSet<KeyCode> keySet;
    private GraphicsContext gc;
    private Robot robot;
    Scene scene;


    public static void main(String[] args) {
        launch(args);
    }


    public void init_actions() {
        if (debug) debug = true;
        maze = debug ? new Maze() : new Maze(50);
        tardis = new Tardis(maze, debug);
        player = new Player(maze, tardis);
        angels = new Angel[Angel.NUMBER_OF_ANGELS];
        for (int i = 0; i < Angel.NUMBER_OF_ANGELS; i++) {
            angels[i] = new Angel(maze,i);
        }
        paused = false;
        inMenu=false;
    }


    public void init() {
        init_actions();
    }


    public void start(Stage stage) {





        //FlowPane rootNode = new FlowPane();
        StackPane rootNode = new StackPane();
        scene = new Scene(rootNode, 1200, 675);
        Canvas canvas = new Canvas(1200, 675);

        stage.setMinHeight(638);
        stage.setMinWidth(816);


        canvas.widthProperty().bind(scene.widthProperty().subtract(5));
        canvas.heightProperty().bind(scene.heightProperty().subtract(5));

        keySet = new HashSet<>();
        gc = canvas.getGraphicsContext2D();
        gameCamera = new GameCamera(gc, 400, player.FIELD_OF_VIEW, debug);
        gameLoop = new GameLoop();
        mainMenuLoop = new MainMenuLoop();
        time=System.currentTimeMillis();
        inMenu=true;
        stage.setScene(scene);
        setActions(scene,stage,canvas);
        mainMenuLoop.start();
        rootNode.getChildren().add(canvas);
        stage.show();
        try {
            robot=new Robot();
        } catch (AWTException e) {
            System.out.println("Robot init error");
        }

        lastSceneX=stage.getWidth()/2;
        lastSceneY=stage.getHeight()/2;
        robot.mouseMove((int)(lastSceneX+stage.getX()),(int)(lastSceneY+stage.getY()));
    }

    private boolean NOTantiMouseEvent = true;
    private void setActions(Scene scene, Stage stage, Canvas canvas){
        stage.setTitle("Find the Tardis");
        stage.setOnCloseRequest((WindowEvent event) -> Platform.exit());

        scene.setOnKeyPressed((KeyEvent event) -> keySet.add(event.getCode()));
        scene.setOnKeyReleased((KeyEvent event) -> keySet.remove(event.getCode()));

        canvas.setOnMouseMoved((MouseEvent event) -> {
            if (NOTantiMouseEvent) {
                dSceneX = event.getSceneX() - lastSceneX;
                lastSceneX = event.getSceneX();

                dSceneY = event.getSceneY() - lastSceneY;
                lastSceneY = event.getSceneY();

                player.verticalLook-=dSceneY;
            } else {
                NOTantiMouseEvent=true;
            }

        });
        canvas.setOnMouseExited((MouseEvent event) -> {
            lastSceneX=stage.getWidth()/2;
            lastSceneY=stage.getHeight()/2;
            robot.mouseMove((int)(lastSceneX+stage.getX()),(int)(lastSceneY+stage.getY()));
            NOTantiMouseEvent=false;
        });
       /* scene.setOnMouseExited((MouseEvent event) -> {
            lastSceneX=stage.getWidth()/2;
            lastSceneY=stage.getHeight()/2;

        });*/
      //  stage.setFullScreen(true);



        stage.setFullScreen(true);
        scene.setCursor(Cursor.NONE);
    }


    private class GameLoop extends Service<Void> {

        GameLoop(){
            setOnSucceeded((WorkerStateEvent event) -> {
                try {
                    if (!inMenu) restart();
                    else mainMenuLoop.restart();
                }
                catch (Exception e){ System.out.println("GameLoop setOnSucceeded");}
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


        }



        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    //    time=System.currentTimeMillis();
                    if (!paused) {
                        player.update(keySet, maze,dSceneX);
                        gameCamera.buildScreen(maze, player, angels);
                        Angel.update(angels, gameCamera, maze, player);
                        tardis.update(keySet, maze, player);
                        dSceneX=dSceneY=0;

                    }
                    if (Maze.distenceBetween(player.coords, tardis.coords) < END_GAME_RANGE * 2 && tardis.isHere) {
                        if (!paused) victory_counter++;
                        paused = true;
                        gameCamera.endGameScreen("You win!", victory_counter, defeat_counter);
                        endGameUpdate();

                    }

                    double nearestAngelDist = Double.POSITIVE_INFINITY;
                    for (Angel angel : angels)
                        nearestAngelDist = Math.min(nearestAngelDist, Maze.distenceBetween(player.coords, angel.coords));
                    if (nearestAngelDist < Angel.HUNTING_RANGE) {
                        gc.setFill(Color.RED);
                        gc.fillText("DANGER! " + Double.toString(nearestAngelDist), 20, 20);
                    }
                    if (nearestAngelDist < END_GAME_RANGE) {
                        if (!paused) defeat_counter++;
                        paused = true;
                        gameCamera.endGameScreen("You lose!", victory_counter, defeat_counter);
                        endGameUpdate();
                    }
                    //    System.out.println(System.currentTimeMillis()-time);
                    return null;
                }


            };
        }


        private void endGameUpdate() {
            if (keySet.contains(KeyCode.DIGIT1)) {
                init_actions();

            }
            if (keySet.contains(KeyCode.DIGIT2)) {
                Platform.exit();
            }

            if (keySet.contains(KeyCode.DIGIT3)){
                inMenu=true;
            }
        }

    }

    private class MainMenuLoop extends Service<Void> {

        MainMenuLoop(){
            setOnSucceeded((WorkerStateEvent event) -> {
                try {
                    if (inMenu) restart();
                    else gameLoop.restart();
                }
                catch (Exception e){ System.out.println("MainMenu setOnSucceeded");}
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        protected Task<Void> createTask(){
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    gameCamera.menuScreen();
                    menuUpdate();
                    victory_counter=defeat_counter=0;
                    return null;
                }
            };
        }

        private void menuUpdate(){
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
