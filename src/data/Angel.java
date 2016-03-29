package data;

public class Angel {
    public final static int NUMBER_OF_ANGELS = 1;

    final static public double HUNTING_RANGE = 4.1;
    final static int MOVING_COOLDOWN = 500; //milliseconds;
    final static int HUNTING_COOLDOWN = 6000;
    final static int TEXTURE_UPDATE_COOLDOWN = 1500;
    final static double HALFWIDTH = 0.25;

    protected static int[] textureID = new int[NUMBER_OF_ANGELS];

    public enum States {Wandering, Hunting, OnSight}

    protected boolean isOnSight = false;
    public Maze.Coords coords;
    public States state;
    public double alpha_angle;

    private long movingTime;
    private long huntingTime;
    private long textureTime;


    public Angel(Maze maze, int number) {
        coords = new Maze.Coords(maze, 5.5, 5.5);
        movingTime = System.currentTimeMillis();
        state = States.Wandering;
        textureID[number] = 0;
    }


    private void wander(GameCamera gameCamera, Maze maze, Player player, Angel[] angels) {
        Maze.Coords falseCoords;
        boolean nearOtherAngel;
        do {
            falseCoords = maze.lookForEmpty();
            falseCoords.x += 0.5;
            falseCoords.y += 0.5;
            nearOtherAngel = false;
            for (Angel angel : angels) nearOtherAngel = nearOtherAngel || near(angel, falseCoords);
        }
        while (near(player, falseCoords, HALFWIDTH * 2) || nearOtherAngel || gameCamera.falseScreen(maze, player, falseCoords));
        this.coords = falseCoords;
    }


    boolean near(Player player, Maze.Coords falseCoords) {
        return (HUNTING_RANGE > Maze.distenceBetween(falseCoords, player.coords));
    }

    boolean near(Player player, Maze.Coords falseCoords, double range) {
        return (range > Maze.distenceBetween(falseCoords, player.coords));
    }

    boolean near(Angel angel, Maze.Coords falseCoords) {
        return (!this.equals(angel) && HALFWIDTH > Maze.distenceBetween(falseCoords, angel.coords));
    }


    private void hunt(GameCamera gameCamera, Maze maze, Player player, Angel[] angels) {
        double newDistance = 2.0 / 4.0 * Maze.distenceBetween(coords, player.coords);
        double angleBehind = alpha_angle - GameCamera.CIRCLE / 4;
        double angle;
        boolean nearOtherAngel;
        int count = 30;
        Maze.Coords falseCoords = new Maze.Coords(maze, 3, 3);
        do {
            if (count == 0) break;
            nearOtherAngel = false;
            angle = angleBehind + Math.random() * GameCamera.CIRCLE / 2;
            falseCoords.x = player.coords.x + newDistance * Math.cos(angle);
            falseCoords.y = player.coords.y + newDistance * Math.sin(angle);
            for (Angel angel : angels) nearOtherAngel = nearOtherAngel || near(angel, falseCoords);
            count--;
        }
        while (gameCamera.falseScreen(maze, player, falseCoords) || nearOtherAngel || !maze.validCoords(falseCoords) || (maze.map[(int) falseCoords.x][(int) falseCoords.y] != Resources.Blocks.EMPTY));
        if (count > 0) coords = falseCoords;

    }



    public static void update(Angel[] angels, GameCamera gameCamera, Maze maze, Player player) {
        //   for (Angel angel : angels) {
        for (int i = 0; i < NUMBER_OF_ANGELS; i++) {
            Angel angel = angels[i];
            angel.state = angel.isOnSight ? States.OnSight : (angel.near(player, angel.coords) ? States.Hunting : States.Wandering);

            if (angel.state != States.OnSight && System.currentTimeMillis()-TEXTURE_UPDATE_COOLDOWN>angel.textureTime){
                textureID[i] = Math.random() > 0.5 ? 0 : 1;
                angel.textureTime=System.currentTimeMillis();
            }

            switch (angel.state) {
                case Wandering:
                    if (System.currentTimeMillis() - angel.movingTime > MOVING_COOLDOWN * 2) {
                        angel.wander(gameCamera, maze, player, angels);
                        angel.movingTime = System.currentTimeMillis();
                        angel.huntingTime = System.currentTimeMillis();
                    }
                    break;
                case Hunting:
                    if (System.currentTimeMillis() - angel.huntingTime > HUNTING_COOLDOWN) {
                        angel.wander(gameCamera, maze, player, angels);
                        angel.huntingTime = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - angel.movingTime > MOVING_COOLDOWN) {
                        angel.hunt(gameCamera, maze, player, angels);
                        angel.movingTime = System.currentTimeMillis();
                    }
                    break;
                default:
                    angel.huntingTime = System.currentTimeMillis();
                    break;
            }
        }
    }

}
