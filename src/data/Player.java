package data;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import java.util.HashSet;


final class Player extends Resources.Entity{
    private final double STEP = 0.05;
    private final double ROTATE = Math.toRadians(2.5);
    public final double FIELD_OF_VIEW = Math.toRadians(100);
    final Image weapon = Resources.Textures.SONIC;

    protected double point_of_view = Math.toRadians(0);
    public double verticalLook=0;


    private Maze maze;
    private Tardis tardis;
    private Monsters monsters;
    private GameCamera gameCamera;

    //singlton
    private static Player instance = new Player();

    private Player(){
    }


    static Player getInstance(){
        return instance;
    }


    void init() {
        monsters = Monsters.getInstance();
        tardis=Tardis.getInstance();
        gameCamera=GameCamera.getInstance();
        maze=Maze.getInstance();

        Maze.Coords coords;
        do {
            coords = maze.lookForEmpty();
            //} while (maze.pathExists((int)coords.x,(int)coords.y,(int)tardis.coords.x,(int)tardis.coords.y));
        } while (maze.pathExists(coords, tardis.coords));
        this.coords.set(coords.x + 0.5, coords.y + 0.5);


    }


    void update(HashSet<?> keySet,double dSceneX) {
        verticalLook=verticalLook>200?200:(verticalLook<-200?-200:verticalLook);

        if (keySet.contains(KeyCode.W)) move(true);
        if (keySet.contains(KeyCode.S)) move(false);
        if (keySet.contains(KeyCode.A)) sideMove(true);
        if (keySet.contains(KeyCode.D)) sideMove(false);
        if (dSceneX<0) rotate(true);
        if (dSceneX>0) rotate(false);
    }

    private void sideMove(boolean toTheLeft){
        double mult = toTheLeft ? -1.0 : 1.0;
        double getStepX = coords.x + mult * STEP * Math.sin(point_of_view);
        double getStepY = coords.y - mult * STEP * Math.cos(point_of_view);

        double newX = (maze.map[(int) (getStepX + STEP * mult)][(int) coords.y] == Resources.Blocks.Empty) ? getStepX : coords.x;
        double newY = (maze.map[(int) coords.x][(int) (getStepY + STEP * mult)] == Resources.Blocks.Empty) ? getStepY : coords.y;
        if (maze.map[(int) newX][(int) newY] == Resources.Blocks.Empty) {
            coords.x = newX;
            coords.y = newY;
        } else if (maze.map[(int) coords.x][(int) newY] == Resources.Blocks.Empty) {
            coords.y = newY;
        } else if (maze.map[(int) newX][(int) coords.y] == Resources.Blocks.Empty) {
            coords.x = newX;
        }

        gameCamera.weaponAngle += 0.1;
    }

    private void move(boolean moveForward) {
        double mult = moveForward ? 1.0 : -1.0;
        double getStepX = coords.x + mult * STEP * Math.cos(point_of_view);
        double getStepY = coords.y + mult * STEP * Math.sin(point_of_view);

        double newX = (maze.map[(int) (getStepX + STEP * mult)][(int) coords.y] == Resources.Blocks.Empty) ? getStepX : coords.x;
        double newY = (maze.map[(int) coords.x][(int) (getStepY + STEP * mult)] == Resources.Blocks.Empty) ? getStepY : coords.y;
        if (maze.map[(int) newX][(int) newY] == Resources.Blocks.Empty) {
            coords.x = newX;
            coords.y = newY;
        } else if (maze.map[(int) coords.x][(int) newY] == Resources.Blocks.Empty) {
            coords.y = newY;
        } else if (maze.map[(int) newX][(int) coords.y] == Resources.Blocks.Empty) {
            coords.x = newX;
        }

        gameCamera.weaponAngle += 0.1;
    }


    private void rotate(boolean toTheLeft) {
        double mult = toTheLeft ? 1.0 : -1.0;
        point_of_view = (point_of_view + mult * ROTATE + gameCamera.CIRCLE) % (gameCamera.CIRCLE);
    }
}

