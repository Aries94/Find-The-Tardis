package data;

import javafx.scene.image.Image;

public class Resources {
    class Blocks {
        final static int EMPTY = 0;
        final static int WALL = 1;
        final static int TARDIS = 2;
    }

    public static class Textures {
        final static Image SKY = new Image("/resources/deathvalley_panorama.jpg");
        final static Image WALL = new Image("/resources/wall_texture.jpg");
        final static Image TARDIS = new Image("/resources/tardis_main.png");
        final static Image ANGEL = new Image("/resources/angel2.png");
        final static Image DARK_ANGEL = new Image("/resources/angel2_black.png");
        final static Image SONIC = new Image("/resources/sonic_11.png");
        final static Image MAIN_MENU = new Image("resources/main_menu.jpg");
    }

    class Heights {
        final static double WALL = 5;
        final static double TARDIS = 2.4;
        final static double ANGEL = 1.7;
        final static double PLAYER = 1;
    }

    static double mod(double a, double b) {
        return a < 0 ? mod(a + b, b) : a % b < 0 ? a % b + b : a % b;
    }
}