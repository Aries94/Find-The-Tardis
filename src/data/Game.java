package data;

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


final public class Game extends Application {
    final private double END_GAME_RANGE = 0.3;
    final private long STATE_CHANGE_COOLDOWN=200;


    private enum States {Game,Pause,Ending,MainMenu,Exit}
    private States state = States.MainMenu;
    private GameLoop gameLoop=new GameLoop();
    private MainMenuLoop mainMenuLoop=new MainMenuLoop();
    private EndingLoop endingLoop=new EndingLoop();
    private PauseLoop pauseLoop=new PauseLoop();


    private boolean debug = false;


    private boolean NOTantiMouseEvent = true;
    private int victory_counter = 0;
    private int defeat_counter = 0;
    private double lastSceneX=0,lastSceneY=0;
    private double dSceneX=0,dSceneY=0;
    private long lastStateChange=System.currentTimeMillis();
    private Maze maze=Maze.getInstance();
    private Player player=Player.getInstance();
    private Tardis tardis=Tardis.getInstance();
    private GameCamera gameCamera = GameCamera.getInstance();
    private Monsters monsters = Monsters.getInstance();


    private HashSet<KeyCode> keySet;
    private GraphicsContext gc;
    private Robot robot;
    private Scene gameScene;
    private Canvas canvas;
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    public void init_actions() {
        if (debug) debug = true;
        //maze = debug ? new Maze() : new Maze(50);
        if (debug)
            maze.init();
        else
            maze.init(50);
        tardis.init(debug);
        player.init();
        monsters.init();
    }





    public void init() {
        init_actions();
    }





    public void start(Stage stage) {
        StackPane rootNode = new StackPane();
        gameScene = new Scene(rootNode,500,500, Color.BLACK);

        canvas = new Canvas(500, 500);
        this.stage=stage;
        stage.setMinHeight(300);
        stage.setMinWidth(300);

        canvas.widthProperty().bind(gameScene.widthProperty().subtract(5));
        canvas.heightProperty().bind(gameScene.heightProperty().subtract(5));


        keySet = new HashSet<>();
        gc = canvas.getGraphicsContext2D();

        gameCamera.init(gc, 400, player.FIELD_OF_VIEW, debug);

        stage.setScene(gameScene);
        setActions(stage);
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






    public void stop() {
    }




    private void setActions(Stage stage){
        stage.setTitle("Find the Tardis");
        stage.setOnCloseRequest((WindowEvent event) -> Platform.exit());

        gameScene.setOnKeyPressed((KeyEvent event) -> keySet.add(event.getCode()));
        gameScene.setOnKeyReleased((KeyEvent event) -> keySet.remove(event.getCode()));


        gameLoop.setOnSucceeded((WorkerStateEvent event) -> reLoop());
        mainMenuLoop.setOnSucceeded((WorkerStateEvent event) -> reLoop());
        endingLoop.setOnSucceeded((WorkerStateEvent event) -> reLoop());
        pauseLoop.setOnSucceeded((WorkerStateEvent event) -> reLoop());

        //stage.setFullScreen(true);
        //gameScene.setCursor(Cursor.NONE);
    }




    private void reLoop(){
        switch (state){
            case Game:
                gameLoop.restart();
                break;
            case MainMenu:
                mainMenuLoop.restart();
                break;
            case Ending:
                endingLoop.restart();
                break;
            case Pause:
                pauseLoop.restart();
                break;
            case Exit:
                Platform.exit();
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    //returns true if state changed
    private boolean changeState(States newState) {
        if (state == newState && System.currentTimeMillis() - lastStateChange < STATE_CHANGE_COOLDOWN)
            return false;
        state = newState;
        lastStateChange = System.currentTimeMillis();
        return true;
    }


    private void setMouseEventEnabled(boolean turnOn){
        if (turnOn){
            canvas.setOnMouseExited(Game.this::mouseEventOn_onExited);
            canvas.setOnMouseMoved(Game.this::mouseEventOn_onMoved);
            gameScene.setCursor(Cursor.NONE);
        }else{
            canvas.setOnMouseExited(null);
            canvas.setOnMouseMoved(null);
            gameScene.setCursor(Cursor.DEFAULT);
        }
    }



    private void mouseEventOn_onMoved(MouseEvent event){
        if (NOTantiMouseEvent) {
            dSceneX = event.getSceneX() - lastSceneX;
            lastSceneX = event.getSceneX();

            dSceneY = event.getSceneY() - lastSceneY;
            lastSceneY = event.getSceneY();

            player.verticalLook-=dSceneY;
        } else {
            NOTantiMouseEvent=true;
        }
    }





    private void mouseEventOn_onExited(MouseEvent event){
        lastSceneX= gameScene.getWidth()/2;
        lastSceneY= gameScene.getHeight()/2;
        robot.mouseMove((int)(lastSceneX+stage.getX()),(int)(lastSceneY+stage.getY()));
        //System.out.println(lastSceneX+" "+gameScene.getX()+'\n'+lastSceneY+" "+gameScene.getY());

        NOTantiMouseEvent=false;
    }





    //__________________________________________________________________________________________________________________
    final private class GameLoop extends Service<Void> {
        private double nearestAngelDist;


        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    player.update(keySet,dSceneX);
                    gameCamera.gameScreen();
                    monsters.update();
                    tardis.update(keySet);
                    dSceneX = dSceneY = 0;


                    if (victory()) {
                        victory_counter++;
                        changeState(States.Ending);
                        return null;
                    }

                    if (defeat()){
                        defeat_counter++;
                        changeState(States.Ending);
                        return null;
                    }

                    if (keySet.contains(KeyCode.ESCAPE)){
                        changeState(States.Pause);
                    }
                    return null;
                }


            };
        }


        /*
         *Player wins if he reaches the Tardis while it is in "Standing" state
         */
        private boolean victory(){
            return (maze.distanceBetween(player,tardis)<END_GAME_RANGE *2&& tardis.isHere);
        }



        /*
         *Player loses if ine if the Monsters catches him
         */
        private boolean defeat(){
            nearestAngelDist = Double.POSITIVE_INFINITY;
            for (Monsters.Angel angel: monsters.angel){
                nearestAngelDist=Math.min(nearestAngelDist,maze.distanceBetween(angel,player));
            }
            return nearestAngelDist<END_GAME_RANGE;
        }
    }
    //__________________________________________________________________________________________________________________




    //__________________________________________________________________________________________________________________
    final private class MainMenuLoop extends Service<Void> {

        @Override
        protected Task<Void> createTask(){
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    gameCamera.mainMenuScreen();
                    update();
                    return null;
                }
            };
        }





        private void update(){
            if (keySet.contains(KeyCode.DIGIT1)) {
                //restart the game
                init_actions();
                changeState(States.Game);
                setMouseEventEnabled(true);
                return;
            }
            if (keySet.contains(KeyCode.DIGIT2)) {
                //close the app
                changeState(States.Exit);
            }
        }



    }
    //__________________________________________________________________________________________________________________





    //__________________________________________________________________________________________________________________
    final private class EndingLoop extends Service<Void>{

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    gameCamera.endingScreen("",victory_counter,defeat_counter);
                    update();
                    return null;
                }
            };
        }





        private void update(){
            if (keySet.contains(KeyCode.DIGIT1)) {
                //restart the game
                init_actions();
                changeState(States.Game);
                return;
            }
            if (keySet.contains(KeyCode.DIGIT2)) {
                //quit to main menu
                changeState(States.MainMenu);
                setMouseEventEnabled(false);
                keySet.remove(KeyCode.DIGIT2);
                return;
            }
            if (keySet.contains(KeyCode.DIGIT3)) {
                //close the app
                changeState(States.Exit);
            }
        }
    }
    //__________________________________________________________________________________________________________________




    //__________________________________________________________________________________________________________________
    final private class PauseLoop extends Service<Void>{
        private boolean onPause = false;

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    //gameCamera.gameScreen(maze,player,monsters);
                    if (keySet.contains(KeyCode.ESCAPE)){
                        if(onPause){
                            if (changeState(States.Game)) {
                                gameScene.setCursor(Cursor.NONE);
                                setMouseEventEnabled(true);
                                onPause = false;
                            }
                        }else {
                            gameScene.setCursor(Cursor.DEFAULT);
                            setMouseEventEnabled(false);
                            onPause=true;
                        }
                    }
                    return null;
                }
            };
        }
    }
    //__________________________________________________________________________________________________________________
}
