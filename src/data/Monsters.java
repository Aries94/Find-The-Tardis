package data;

final class Monsters {
    enum States  {Wandering, Hunting, onSight};
    Angel[] angel;


    final int COUNT = 3;//number of angels in the maze
    final int MOVING_COOLDOWN=500;//in millis;
    final int HUNTING_COOLDOWN=6000;//in millis;
    final int TEXTURE_UPDATE_COOLDOWN=1500;//in millis;
    final double HALFWIDTH = 0.25;
    final double HUNTING_RANGE = 4.1;


    private Player player;
    private Maze maze;
    private Tardis tardis;
    private GameCamera gameCamera;

    //singlton
    private static Monsters instance = new Monsters();

    private Monsters(){
    }
    static Monsters getInstance(){
        return instance;
    }


    void init(){
        player=Player.getInstance();
        tardis=Tardis.getInstance();
        gameCamera=GameCamera.getInstance();
        maze=Maze.getInstance();

        angel=new Angel[COUNT];
        for (int i = 0; i <COUNT ; i++) {
            angel[i]=new Angel();
        }


    }

    void update(){
        for (int i = 0; i < COUNT; i++) {
            angel[i].state = angel[i].onSight ? States.onSight : (angel[i].near()? States.Hunting : States.Wandering);

            if (angel[i].state != States.onSight && System.currentTimeMillis()-TEXTURE_UPDATE_COOLDOWN>angel[i].textureTime){
                angel[i].textureID = Math.random() > 0.5 ? 0 : 1;
                angel[i].textureTime=System.currentTimeMillis();
            }

            switch (angel[i].state) {
                case Wandering:
                    if (System.currentTimeMillis() - angel[i].movingTime > MOVING_COOLDOWN * 2) {
                        angel[i].wander();
                        angel[i].movingTime = System.currentTimeMillis();
                        angel[i].huntingTime = System.currentTimeMillis();
                    }
                    break;
                case Hunting:
                    if (System.currentTimeMillis() - angel[i].huntingTime > HUNTING_COOLDOWN) {
                        angel[i].wander();
                        angel[i].huntingTime = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - angel[i].movingTime > MOVING_COOLDOWN) {
                        angel[i].hunt();
                        angel[i].movingTime = System.currentTimeMillis();
                    }
                    break;
                default:
                    angel[i].huntingTime = System.currentTimeMillis();
                    break;
            }
        }
    }



    //sort angels by distance to the player
    int[] sort() {
        int[] result = new int[COUNT];
        int temp;
        for (int i = 0; i < COUNT; i++) {
            result[i] = i;
        }
        for (int i = 0; i < COUNT - 1; i++) {
            for (int j = 0; j < COUNT - 1 - i; j++) {
                if (angel[result[j]].distanceToPlayer < angel[result[j + 1]].distanceToPlayer) {
                    temp = result[j];
                    result[j] = result[j + 1];
                    result[j + 1] = temp;
                }
            }
        }
        return result;
    }



    final class Angel extends Resources.Entity{
        boolean onSight=true;
        States state=States.Wandering;
        int textureID=0;

        //time of the latest changing of texture
        private long textureTime = System.currentTimeMillis();

        //time of the last move
        private long movingTime = System.currentTimeMillis();

        //time when the current hunt started
        private long huntingTime = System.currentTimeMillis();

        //angle between Player-Angel line and X-axis
        double alpha_angle;

        //distance between angel and player
        double distanceToPlayer;


        private Angel(){
            coords.setInfinity();
        }


        //returns true if player is within the hunting range
        private boolean near(){
            return (HUNTING_RANGE > maze.distanceBetween(coords,player.coords));
        }


        //returns true if player is within range
        boolean near(Maze.Coords falseCoords, double range) {
            return (range > maze.distanceBetween(player,falseCoords));
        }


        //returns true if new coords are within the halfwidth radius of any other angel
        boolean near(Angel angel, Maze.Coords falseCoords) {
            return (!(coords.equals(angel.coords)) && HALFWIDTH > maze.distanceBetween(angel,falseCoords));
        }




        private void hunt() {
            double newDistance = 2.0 / 4.0 * maze.distanceBetween(this,player);
            double angleBehind = alpha_angle - gameCamera.CIRCLE / 4;
            double angle;
            boolean nearOtherAngel;
            int count = 30;
            Maze.Coords falseCoords = new Maze.Coords();
            do {
                if (count == 0) break;
                nearOtherAngel = false;
                angle = angleBehind + Math.random() * gameCamera.CIRCLE / 2;
                falseCoords.x = player.coords.x + newDistance * Math.cos(angle);
                falseCoords.y = player.coords.y + newDistance * Math.sin(angle);
                for (Angel ang : angel) nearOtherAngel = nearOtherAngel || near(ang, falseCoords);
                count--;
            }
            while (gameCamera.falseScreen(falseCoords) || nearOtherAngel || !maze.validCoords(falseCoords) || (maze.map[(int) falseCoords.x][(int) falseCoords.y] != Resources.Blocks.Empty));
            if (count > 0) coords = falseCoords;

        }


        private void wander() {
            Maze.Coords falseCoords;
            boolean nearOtherAngel;
            do {
                falseCoords = maze.lookForEmpty();
                falseCoords.x += 0.5;
                falseCoords.y += 0.5;
                nearOtherAngel = false;
                for (Angel ang : angel) nearOtherAngel = nearOtherAngel || near(ang, falseCoords);
            }
            while (near(falseCoords, HALFWIDTH * 2) || nearOtherAngel || gameCamera.falseScreen(falseCoords));
            this.coords = falseCoords;
        }
    }

}


/*class Angel extends Resources.Entity{
    public final static int NUMBER_OF_ANGELS = 3;

    final static public double HUNTING_RANGE = 4.1;
    final static int MOVING_COOLDOWN = 500; //milliseconds;
    final static int HUNTING_COOLDOWN = 6000;
    final static int TEXTURE_UPDATE_COOLDOWN = 1500;
    final static double HALFWIDTH = 0.25;

    protected static int[] textureID = new int[NUMBER_OF_ANGELS];

    public enum States {Wandering, Hunting, OnSight}

    protected boolean isOnSight = false;
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
        return (HUNTING_RANGE > Maze.distanceBetween(player,falseCoords));
    }

    boolean near(Player player, Maze.Coords falseCoords, double range) {
        return (range > Maze.distanceBetween(player,falseCoords));
    }

    boolean near(Angel angel, Maze.Coords falseCoords) {
        return (!this.equals(angel) && HALFWIDTH > Maze.distanceBetween(angel,falseCoords));
    }


    private void hunt(GameCamera gameCamera, Maze maze, Player player, Angel[] angels) {
        double newDistance = 2.0 / 4.0 * Maze.distanceBetween(this,player);
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
        while (gameCamera.falseScreen(maze, player, falseCoords) || nearOtherAngel || !maze.validCoords(falseCoords) || (maze.map[(int) falseCoords.x][(int) falseCoords.y] != Resources.Blocks.Empty));
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

}*/
