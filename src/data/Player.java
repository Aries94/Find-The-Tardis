package data;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import java.util.HashSet;


class Player extends Resources.Entity{
    private final double STEP = 0.05;
    private final double ROTATE = Math.toRadians(2.5);
    public final double FIELD_OF_VIEW = Math.toRadians(100);
    final Image weapon = Resources.Textures.SONIC;

    protected double point_of_view = Math.toRadians(0);
    public double verticalLook=0;


    //singlton
    private static Player instance = new Player();

    private Player(){
    }


    static Player getInstance(){
        return instance;
    }


    void init(Maze maze, Tardis tardis) {
        Maze.Coords coords;
        do {
            coords = maze.lookForEmpty();
            //} while (maze.pathExists((int)coords.x,(int)coords.y,(int)tardis.coords.x,(int)tardis.coords.y));
        } while (maze.pathExists(coords, tardis.coords));
        this.coords = new Maze.Coords(maze, coords.x + 0.5, coords.y + 0.5);
    }


    void update(HashSet<?> keySet, Maze maze, double dSceneX) {
        verticalLook=verticalLook>200?200:(verticalLook<-200?-200:verticalLook);

        if (keySet.contains(KeyCode.W)) move(true, maze);
        if (keySet.contains(KeyCode.S)) move(false, maze);
        if (keySet.contains(KeyCode.A)) sideMove(true, maze);
        if (keySet.contains(KeyCode.D)) sideMove(false, maze);
        if (dSceneX<0) rotate(true);
        if (dSceneX>0) rotate(false);
    }

    private void sideMove(boolean toTheLeft, Maze maze){
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

        GameCamera.weaponAngle += 0.1;
    }

    private void move(boolean moveForward, Maze maze) {
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

        GameCamera.weaponAngle += 0.1;
    }


    private void rotate(boolean toTheLeft) {
        double mult = toTheLeft ? 1.0 : -1.0;
        point_of_view = (point_of_view + mult * ROTATE + GameCamera.CIRCLE) % (GameCamera.CIRCLE);
    }
}

