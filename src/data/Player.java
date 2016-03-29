package data;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import java.util.HashSet;

public class Player {
    private final double STEP = 0.05;
    private final double ROTATE = Math.toRadians(3);
    public final double FIELD_OF_VIEW = Math.toRadians(100);
    final Image weapon = Resources.Textures.SONIC;

    public Maze.Coords coords;
    protected double point_of_view = Math.toRadians(0);
    //public int rotateFactor =0; // rF>0 - to the left; rf<0 - to the right;


    public Player(Maze maze, Tardis tardis) {
        Maze.Coords coords;
        do {
            coords = maze.lookForEmpty();
            //} while (maze.pathExists((int)coords.x,(int)coords.y,(int)tardis.coords.x,(int)tardis.coords.y));
        } while (maze.pathExists(coords, tardis.coords));
        this.coords = new Maze.Coords(maze, coords.x + 0.5, coords.y + 0.5);
    }


    public void update(HashSet<?> keySet, Maze maze, double dSceneX) {
        if (keySet.contains(KeyCode.W)) move(true, maze);
        if (keySet.contains(KeyCode.S)) move(false, maze);
        if (keySet.contains(KeyCode.A)|| dSceneX<0) rotate(true);
        if (keySet.contains(KeyCode.D)|| dSceneX>0) rotate(false);
    }


    void move(boolean moveForward, Maze maze) {
        double mult = moveForward ? 1.0 : -1.0;
        double getStepX = coords.x + mult * STEP * Math.cos(point_of_view);
        double getStepY = coords.y + mult * STEP * Math.sin(point_of_view);

        double newX = (maze.map[(int) (getStepX + STEP * mult)][(int) coords.y] == Resources.Blocks.EMPTY) ? getStepX : coords.x;
        double newY = (maze.map[(int) coords.x][(int) (getStepY + STEP * mult)] == Resources.Blocks.EMPTY) ? getStepY : coords.y;
        if (maze.map[(int) newX][(int) newY] == Resources.Blocks.EMPTY) {
            coords.x = newX;
            coords.y = newY;
        } else if (maze.map[(int) coords.x][(int) newY] == Resources.Blocks.EMPTY) {
            coords.y = newY;
        } else if (maze.map[(int) newX][(int) coords.y] == Resources.Blocks.EMPTY) {
            coords.x = newX;
        }

        GameCamera.weaponAngle += 0.1;
    }


    void rotate(boolean toTheLeft) {
        double mult = toTheLeft ? 1.0 : -1.0;
        point_of_view = (point_of_view + mult * ROTATE + GameCamera.CIRCLE) % (GameCamera.CIRCLE);
    }
}

