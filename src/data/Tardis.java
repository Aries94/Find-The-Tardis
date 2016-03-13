package data;

public class Tardis {
    public Maze.Coords coords;
    double alpha;

    public Tardis(Maze maze){
        Maze.Coords coords = maze.lookForEmpty();
        this.coords = new Maze.Coords(maze,coords.x+0.5,coords.y+0.5);
        maze.map[(int)coords.x][(int)coords.y]=Resources.Blocks.TARDIS;
        alpha=1;
    }
}
