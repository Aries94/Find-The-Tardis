package data;

import javafx.scene.image.Image;

abstract class Resources {
    enum Blocks {Empty, Wall, Tardis}

    static class Textures {
        final static Image SKY = new Image("/resources/deathvalley_panorama.jpg");
        final static Image WALL = new Image("/resources/wall_texture.jpg");
        final static Image TARDIS = new Image("/resources/tardis_main.png");
        final static Image ANGEL_1 = new Image("/resources/angel2.png"); // textureID=0
        final static Image DARK_ANGEL_1 = new Image("/resources/angel2_black.png");// textureID=0
        final static Image SONIC = new Image("/resources/sonic_11.png");
        final static Image MAIN_MENU = new Image("resources/main_menu.jpg");
        final static Image ANGEL_2 = new Image("/resources/angel3.png");//textureID=1
        final static Image DARK_ANGEL_2 = new Image("/resources/angel3_black.png");//textureID=1
    }

    public static Image[] angelTextures = new Image[]{Textures.ANGEL_1,Textures.ANGEL_2};
    public static Image[] darkAngelTextures = new Image[]{Textures.DARK_ANGEL_1,Textures.DARK_ANGEL_2};

    class Heights {
        final static double WALL = 5;
        final static double TARDIS = 2.4;
        final static double ANGEL = 1.6;
        final static double PLAYER = 1;
    }

    static double mod(double a, double b) {
        return a < 0 ? mod(a + b, b) : a % b < 0 ? a % b + b : a % b;
    }

    static class Entity{
        Maze.Coords coords;
    }
}