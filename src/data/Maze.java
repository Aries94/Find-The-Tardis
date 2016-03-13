package data;

import java.io.*;
import java.util.*;


public class Maze{
    int size;
    int[][] map;

    static class Coords {
        double x;
        double y;

        Coords(Maze maze, double x, double y){
            this.x = ( x>0 && x<maze.size )? x : 1 ;
            this.y = ( y>0 && y<maze.size )? y : 1 ;
        }
    }

    public Maze(){
      size = 10;
      map=new int[size][size];
      map[7][2]=map[7][3]=map[6][3]=Resources.Blocks.WALL;
      defaultMaze();
    }

    public Maze(int size){
        this.size=(size > 2)&&(size<200)? size:10;
        map=new int[size][size];
        defaultMaze();
        randoMaze();
    }

    private void defaultMaze(){
        for (int i=0;i<size;i++){
            map[0][i]=
            map[size-1][i]=
            map[i][0]=
            map[i][size-1]= Resources.Blocks.WALL;
        }
    }

    private void randoMaze(){
        for (int i = 1;i<size-1;i++)
            for (int j = 1;j<size-1;j++)
                map[i][j]= Math.random() < 0.2 ? Resources.Blocks.WALL : Resources.Blocks.EMPTY;
    }

    public static double distenceBetween(Coords point1, Coords point2){
        return Math.sqrt((point1.x-point2.x)*(point1.x-point2.x)+(point1.y-point2.y)*(point1.y-point2.y));
        //Gob bless Pythagoras!
    }

    Coords lookForEmpty (){
        int x,y;
        do {
            x = (int) (Math.random() * size);
            y = (int) (Math.random() * size);
        } while (map[x][y] != Resources.Blocks.EMPTY);
        return new Coords(this,x,y);
    }

    boolean pathExists (int x1, int y1, int x2, int y2){
        if (map[x1][y1] == Resources.Blocks.WALL || map[x2][y2]==Resources.Blocks.WALL || ( x1 != x2&& y1 !=y2)) return false;

        ArrayDeque<Coords> adq = new ArrayDeque<>();
        boolean[][] visited = new boolean[size][size];
        for (int i = 0;i<size;i++)
            for (int j = 0;j<size;j++)
                if (map[i][j]==Resources.Blocks.WALL) visited[i][j]=true;

        Coords coord = new Coords(this,x1,y1);
        adq.addFirst(coord);
        visited[x1][y1]=true;

        while (!adq.isEmpty()){
            coord = adq.pollFirst();
            if (coord.x==x2 && coord.y==y2) return true;
            visited[(int)coord.x][(int)coord.y]=true;

            if (!visited[(int)coord.x+1][(int)coord.y])
                adq.addLast(new Coords(this,coord.x+1,coord.y));

            if (!visited[(int)coord.x-1][(int)coord.y])
                adq.addLast(new Coords(this,coord.x-1,coord.y));

            if (!visited[(int)coord.x][(int)coord.y+1])
                adq.addLast(new Coords(this,coord.x,coord.y+1));

            if (!visited[(int)coord.x][(int)coord.y-1])
                adq.addLast(new Coords(this,coord.x,coord.y-1));
        }
        return false;
    }

     void debugOut(Player player){
        int [][] debugMap;
        debugMap = map.clone();
        debugMap[(int) player.coords.x][(int)player.coords.y] =3;
        try (FileWriter file = new FileWriter("debug.txt",false)){
            for (int i=0;i<size;i++){
                String s = "";
                for (int j=0;j<size;j++)
                    s = s+debugMap[i][j]+" ";
                s+='\n';
                file.write(s);
            }
        } catch (FileNotFoundException e) { System.out.println(1);
        } catch (IOException e) { System.out.println(2);
        }
    }


}
