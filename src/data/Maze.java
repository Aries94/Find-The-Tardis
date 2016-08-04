package data;

import java.util.*;

final class Maze {
    private final int MAX_SIZE = 400;


    private Player player;
    private Tardis tardis;
    private Monsters monsters;
    private GameCamera gameCamera;

    private int size;
    //int[][] map;

    Resources.Blocks[][] map;



    //singlton
    private static Maze instance = new Maze();

    private Maze(){
    }


    static Maze getInstance(){
        return instance;
    }

    void init() {
        player=Player.getInstance();
        monsters = Monsters.getInstance();
        tardis=Tardis.getInstance();
        gameCamera=GameCamera.getInstance();

        size = 10;
        mapInit(size);
        map[7][2] = map[7][3] = map[6][3] = Resources.Blocks.Wall;
        defaultMaze();



    }


    void init(int size) {
        player=Player.getInstance();
        monsters = Monsters.getInstance();
        tardis=Tardis.getInstance();
        gameCamera=GameCamera.getInstance();



        this.size = (size > 5) && (size < MAX_SIZE) ? size : 10;
        mapInit(this.size);
        defaultMaze();
        randoMaze();
    }

    private void mapInit(int size){
        map = new Resources.Blocks[size+1][];
        for (int i=0;i<size;i++)
            map[i]=new Resources.Blocks[size];
        map[size]=new Resources.Blocks[1]; //point at infinity

        map[size][0]= Resources.Blocks.Empty;
    }


    static class Coords {
        double x;
        double y;

        Coords(double x, double y) {
            set(x,y);
        }

        Coords(){
            //point at infinity;
            setInfinity();
        }

        void setInfinity(){
            x=instance.size-0.5;
            y=0.5;
        }

        void set(double x,double y){
            if ((x < 0 || x > instance.size) || (y < 0 || y > instance.size)){
                //set point at infinity
                setInfinity();
                return;
            }
            this.x=x;
            this.y=y;
        }
    }


    private void defaultMaze() {
        for (int i = 0; i < size; i++) {
            map[0][i] =
                    map[size - 1][i] =
                            map[i][0] =
                                    map[i][size - 1] = Resources.Blocks.Wall;
        }
    }


    private void randoMaze() {
        for (int i = 1; i < size - 1; i++)
            for (int j = 1; j < size - 1; j++)
                map[i][j] = Math.random() < 0.3 ? Resources.Blocks.Wall : Resources.Blocks.Empty;
    }



    double distanceBetween(Maze.Coords point1, Maze.Coords point2){
        //Gob bless Pythagoras!
        return Math.sqrt(Math.pow(point1.x-point2.x,2)+Math.pow(point1.y-point2.y,2));
    }


    double distanceBetween(Resources.Entity entity1, Resources.Entity entity2){
        return distanceBetween(entity1.coords,entity2.coords);
    }

    double distanceBetween(Resources.Entity entity,Maze.Coords point){
        return distanceBetween(entity.coords,point);
    }



    Coords lookForEmpty() {
        int x, y;
        do {
            x = (int) (Math.random() * size);
            y = (int) (Math.random() * size);
        } while (map[x][y] != Resources.Blocks.Empty);
        return new Coords(x, y);
    }


    boolean validCoords(Coords coords) {
        return (coords.x > 1 && coords.x < size - 1 && coords.y > 1 && coords.y < size - 1);
    }


    boolean pathExists(int x1, int y1, int x2, int y2) {
        if (map[x1][y1] == Resources.Blocks.Wall || map[x2][y2] == Resources.Blocks.Wall || (x1 != x2 && y1 != y2))
            return false;

        ArrayDeque<Coords> adq = new ArrayDeque<>((size-1)*(size-1));
        boolean[][] visited = new boolean[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (map[i][j] == Resources.Blocks.Wall) visited[i][j] = true;

        Coords coord = new Coords(x1, y1);
        adq.addFirst(coord);
        visited[x1][y1] = true;

        while (!adq.isEmpty()) {
            coord = adq.pollFirst();
            if (coord.x == x2 && coord.y == y2) return true;
            visited[(int) coord.x][(int) coord.y] = true;

            if (!visited[(int) coord.x + 1][(int) coord.y])
                adq.addLast(new Coords(coord.x + 1, coord.y));

            if (!visited[(int) coord.x - 1][(int) coord.y])
                adq.addLast(new Coords(coord.x - 1, coord.y));

            if (!visited[(int) coord.x][(int) coord.y + 1])
                adq.addLast(new Coords(coord.x, coord.y + 1));

            if (!visited[(int) coord.x][(int) coord.y - 1])
                adq.addLast(new Coords(coord.x, coord.y - 1));
        }
        return false;
    }


    boolean pathExists(Coords p1, Coords p2) {
        return pathExists((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
    }


}
