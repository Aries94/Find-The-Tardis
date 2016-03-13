package data;

import javafx.scene.image.Image;

public class Resources {
    class Blocks {
        final static int EMPTY = 0;
        final static int WALL = 1;
        final static int TARDIS = 2;
    }
    static  class Textures {
        final static Image SKY = new Image("/resources/deathvalley_panorama.jpg");
        final static Image WALL = new Image("/resources/wall_texture.jpg");
        final static Image TARDIS = new Image("/resources/tardis_main.png");
        final static Image ANGEL = new Image("/resources/angel.png");
        final static Image DARK_ANGEL = new Image("/resources/black_angel.png");
    }
    class Heights {
        final static double WALL = 2;
        final static double TARDIS = 0.9;
        final static double ANGEL = 0.7;
    }

    static double mod (double a, double b){
        return a<0? mod(a+b,b): a%b<0? a%b+b:a%b;
    }
}