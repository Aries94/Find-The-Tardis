package data;

public class Angel {
    public Maze.Coords coords;

    enum States {Wandering, Hunting, OnSight}

    States state;
    final double HUNTING_RANGE = 6;
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
        Maze.Coords falseCoords;
        do {
            falseCoords = maze.lookForEmpty();
            falseCoords.x += 0.5;
            falseCoords.y += 0.5;
        } while (near(player) || gameCamera.falseScreen(maze, player, falseCoords));
        this.coords=falseCoords;
    }

    boolean near(Player player) {
        return (HUNTING_RANGE > Maze.distenceBetween(coords, player.coords));
    }

    private void hunt(GameCamera gameCamera, Maze maze, Player player){
        double newDistance = 3.0/4.0*Maze.distenceBetween(coords,player.coords);
        double angle;
        int count = 30;
        Maze.Coords falseCoords= new Maze.Coords(maze,3,3);
        do{
            angle=Math.random()*GameCamera.CIRCLE;
            falseCoords.x=player.coords.x+newDistance*Math.cos(angle);
            falseCoords.y=player.coords.y+newDistance*Math.sin(angle);
        }while(count--==0 || gameCamera.falseScreen(maze,player,falseCoords)|| !maze.validCoords(falseCoords) || (maze.map[(int)falseCoords.x][(int)falseCoords.y]!=Resources.Blocks.EMPTY));
        if (count>0)coords=falseCoords;

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
                if (System.currentTimeMillis() - time > COOLDOWN) {
                    hunt(gameCamera,maze, player);
                    time = System.currentTimeMillis();
                }
                break;
            default:
                break;
        }
    }
}
