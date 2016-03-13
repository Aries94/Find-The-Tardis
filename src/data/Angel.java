package data;

public class Angel {
    public Maze.Coords coords;

    enum States {Wandering, Hunting, OnSight}

    States state;
    final double HUNTING_RANGE = 2;
    final int COOLDOWN = 1000; //milliseconds;
    final static double HALFWIDTH = 0.5;
    long time;
    boolean isOnSight = false;

    public Angel(Maze maze) {
        coords = new Maze.Coords(maze, 5.5, 5.5);
        time = System.currentTimeMillis();
        state = States.Wandering;
    }

    private void wander(GameCamera gameCamera, Maze maze, Player player) {
        Maze.Coords coords;
        do {
            coords = maze.lookForEmpty();
            coords.x += 0.5;
            coords.y += 0.5;
        } while (near(player) || gameCamera.falseScreen(maze, player, coords));
        this.coords=coords;
    }

    boolean near(Player player) {
        return (HUNTING_RANGE > Maze.distenceBetween(coords, player.coords));
    }

    void move(Maze.Coords coords, Maze maze) {

    }

    public void update(GameCamera gameCamera, Maze maze, Player player) {
        state = isOnSight ? States.OnSight : (near(player) ? States.Hunting : States.Wandering);
        switch (state) {
            case Wandering:
                if (System.currentTimeMillis() - time > COOLDOWN) {
                    wander(gameCamera,maze, player);
                    time = System.currentTimeMillis();
                }
                break;
            case Hunting:
                break;
            default:
                break;
        }
    }
}
