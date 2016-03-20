package data;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;



public class GameCamera {
    static final double CIRCLE = Math.PI * 2;
    static double weaponAngle = 0;

    final private int MAX_RESOLUTION = 900;
    final private int MAX_VIEW_DISTANCE = 20;
    final private double SHADING_DISTANCE = 3;
    final private int MIN_RAINDROPS = 0;
    final private int MAX_RAINDROPS = 1;
    final private double EPSILON = 0.0005;


    protected GraphicsContext gc;
    protected int resolution;

    private PrPlane prPlane;
    private double cosAngelAngel;
    private boolean debug;


    public GameCamera(GraphicsContext gc, int resolution, double fov, boolean debug) {
        this.resolution = (resolution > 5 && resolution < MAX_RESOLUTION) ? resolution : MAX_RESOLUTION;
        this.gc = gc;
        this.debug = debug;
        cosAngelAngel = Math.cos(fov / 1.5);
        prPlane = new PrPlane(gc.getCanvas().getWidth(), gc.getCanvas().getHeight(), fov);
    }


    private class PrPlane {
        double width;
        double height;
        double distance;
        double columnWidth;
        double coef;

        PrPlane(double width, double height, double FOV) {
            this.width = width;
            this.height = height;
            this.distance = (width / 2) / Math.tan(FOV / 2);

            columnWidth = width / resolution;
            coef = this.height * this.distance / this.width / Resources.Heights.PLAYER;
        }
    }


    class Ray {
        double angle;
        RayPoint[] rayPoints = new RayPoint[3];


        Ray(double angle, Maze maze, Maze.Coords startPoint) {
            this.angle = angle;
            cast(maze, startPoint, 0);
        }


        class RayPoint {
            double distance;
            double entry;

            RayPoint(double distance, double entry) {
                this.distance = distance;
                this.entry = entry;
            }
        }


        private double entry(Maze.Coords point, double angle, boolean isX) {
            if (angle < CIRCLE / 4) {
                if (isX)
                    return (Math.floor(point.y + 1) - point.y);
                else
                    return (point.x - Math.ceil(point.x - 1));
            } else if (angle < CIRCLE / 2) {
                if (isX)
                    return (point.y - Math.ceil(point.y - 1));
                else
                    return (point.x - Math.ceil(point.x - 1));
            } else if (angle < 3 * CIRCLE / 4) {
                if (isX)
                    return (point.y - Math.ceil(point.y - 1));
                else
                    return (Math.floor(point.x + 1) - point.x);
            } else {
                if (isX)
                    return (Math.floor(point.y + 1) - point.y);
                else
                    return (Math.floor(point.x + 1) - point.x);
            }
        }


        private void cast(Maze maze, Maze.Coords point, double distance) {
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            int switcher;
            double newDist;
            Maze.Coords newPoint;
            boolean isX;

            if (Math.abs(cos) < EPSILON) {
                newPoint = new Maze.Coords(maze, point.x, sin > 0 ? Math.floor(point.y + 1) : Math.ceil(point.y - 1));
                switcher = maze.map[(int) Math.floor(newPoint.x)][(int) Math.floor(newPoint.y - (sin > 0 ? 0 : 1))];
                newDist = distance + Math.abs(point.y - newPoint.y);
                isX = false;
            } else if (Math.abs(sin) < EPSILON) {
                newPoint = new Maze.Coords(maze, cos > 0 ? Math./**/floor(point.x + 1) : Math./**/ceil(point.x - 1), point.y);
                switcher = maze.map[(int) Math.floor(newPoint.x - (cos > 0 ? 0 : 1))][(int) Math.floor(newPoint.y)];
                newDist = distance + Math.abs(point.x - newPoint.x);
                isX = true;
            } else {
                double stepX = cos > 0 ? Math.floor(point.x + 1) - point.x : Math.ceil(point.x - 1) - point.x;
                double stepY = sin > 0 ? Math.floor(point.y + 1) - point.y : Math.ceil(point.y - 1) - point.y;

                if (stepX / cos < stepY / sin) {
                    switcher = maze.map[(int) (cos > 0 ? Math.floor(point.x + 1) : Math.ceil(point.x - 2))][(int) (sin < 0 ? Math.ceil(point.y - 1) : Math.floor(point.y))];
                    newDist = distance + stepX / cos;
                    newPoint = new Maze.Coords(maze, point.x + stepX, point.y + stepX / cos * sin);
                    isX = true;
                } else {
                    switcher = maze.map[(int) (cos < 0 ? Math.ceil(point.x - 1) : Math.floor(point.x))][(int) (sin > 0 ? Math.floor(point.y + 1) : Math.ceil(point.y - 2))];
                    newDist = distance + stepY / sin;
                    newPoint = new Maze.Coords(maze, point.x + stepY / sin * cos, point.y + stepY);
                    isX = false;
                }
            }
            switch (switcher) {
                case Resources.Blocks.WALL:
                    rayPoints[Resources.Blocks.WALL] = new RayPoint(newDist, entry(newPoint, angle, isX));
                    break;
                case Resources.Blocks.TARDIS:
                    rayPoints[Resources.Blocks.TARDIS] = new RayPoint(newDist, entry(newPoint, angle, isX));
                default:
                    if (newDist < GameCamera.this.MAX_VIEW_DISTANCE)
                        cast(maze, newPoint, newDist);
                    break;
            }
        }


    }


    private void drawTexture(Ray.RayPoint rPoint, Image texture, int number, double angle, double blockHeight, double alpha) {
        double distance = rPoint.distance * Math.cos(angle);

        double texture_startX = texture.getWidth() * rPoint.entry;
        double texture_startY = 0;
        double texture_width = texture.getWidth() / resolution;
        double texture_height = texture.getHeight();

        double startX = prPlane.columnWidth * number;
        double width = prPlane.columnWidth;

        double height = blockHeight * prPlane.coef / distance;
        height += ((int) height) % 2;
        double startY = (prPlane.height / 2) * (1 + 1 / distance) - height;// prPlane.player_dHeight;

        gc.setGlobalAlpha(alpha);
        gc.drawImage(texture, texture_startX, texture_startY, texture_width, texture_height, startX, startY, width, height);

        if (!debug) {
            gc.setGlobalAlpha(distance < SHADING_DISTANCE ? distance / SHADING_DISTANCE : 1.0);
            gc.fillRect(startX, startY, width, height);
        }
        gc.setGlobalAlpha(1.0);

    }


    private void drawRain(int number) {
        int rain = number % 2 == 0 ? (int) Math.ceil(Math.random() * (MAX_RAINDROPS - MIN_RAINDROPS)) + MIN_RAINDROPS : 0;
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.5);
        while (rain-- > 0) {
            double startX = prPlane.columnWidth * number;
            double startY = Math.random() * prPlane.height;
            double height = 50;

            gc.fillRect(startX, startY, 1, height);

        }
        gc.setGlobalAlpha(1.0);
        gc.setFill(Color.BLACK);
    }


    private double[] drawColumn(Ray ray, int number, double angle) {
        double[] distance = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        if (ray.rayPoints[Resources.Blocks.WALL] != null) {
            drawTexture(ray.rayPoints[Resources.Blocks.WALL], Resources.Textures.WALL, number, angle, Resources.Heights.WALL, 1);
            distance[0] = ray.rayPoints[Resources.Blocks.WALL].distance;
        }
        if (ray.rayPoints[Resources.Blocks.TARDIS] != null) {
            drawTexture(ray.rayPoints[Resources.Blocks.TARDIS], Resources.Textures.TARDIS, number, angle, Resources.Heights.TARDIS, Tardis.alpha);
            distance[1] = ray.rayPoints[Resources.Blocks.TARDIS].distance;
        }
         /*  if (distance>0.3)
                drawRain(number);*/
        return distance;
    }


    private void drawAngel(int number, double offset, double distance, double alpha) {
        double startX = prPlane.columnWidth * number;
        double width = prPlane.columnWidth;

        Image texture = Resources.Textures.ANGEL;

        double texture_startX = texture.getWidth() * (offset + Angel.HALFWIDTH) / Angel.HALFWIDTH / 2;
        double texture_startY = 0;
        double texture_width = texture.getWidth() / resolution;
        double texture_height = texture.getHeight();

        double height = Resources.Heights.ANGEL * prPlane.coef / distance;
        height += ((int) height) % 2;
        double startY = (prPlane.height / 2) * (1 + 1 / distance) - height;

        gc.setGlobalAlpha(alpha);
        gc.drawImage(texture, texture_startX, texture_startY, texture_width, texture_height, startX, startY, width, height);

        if (!debug) {
            gc.setGlobalAlpha(distance < SHADING_DISTANCE ? distance / SHADING_DISTANCE : 1.0);
            gc.drawImage(Resources.Textures.DARK_ANGEL, texture_startX, texture_startY, texture_width, texture_height, startX, startY, width, height);

        }
        gc.setGlobalAlpha(1.0);
    }


    boolean[] buildColumn(Maze maze, Player player, int number, double[] alpha_angle, double[] distance_Ang_Pla) {
        boolean[] onSight = new boolean[Angel.NUMBER_OF_ANGELS];
        double[] angelOffset = new double[Angel.NUMBER_OF_ANGELS];

        double angle = Math.atan2(prPlane.columnWidth * number - prPlane.width / 2, prPlane.distance);

        int[] sortedAngel = sort(distance_Ang_Pla);

        Ray ray = new Ray((player.point_of_view - angle + CIRCLE) % CIRCLE, maze, player.coords);

        double[] distance = drawColumn(ray, number, angle);
        for (int i = 0; i < Angel.NUMBER_OF_ANGELS; i++) {
            if ((distance_Ang_Pla[sortedAngel[i]] < distance[0]) && (Math.cos(alpha_angle[sortedAngel[i]] - player.point_of_view) > cosAngelAngel)) {
                angelOffset[sortedAngel[i]] = distance_Ang_Pla[sortedAngel[i]] * Math.sin(ray.angle - alpha_angle[sortedAngel[i]]);
                if (Math.abs(angelOffset[sortedAngel[i]]) < Angel.HALFWIDTH) {
                    //double angel_alpha=distance[1]<Double.POSITIVE_INFINITY?(distance[1]<distance_Ang_Pla[i]? 0:Tardis.alpha):1.0;
                    double angel_alpha = distance_Ang_Pla[sortedAngel[i]] < distance[1] ? 1.0 : distance[0] < Double.POSITIVE_INFINITY ? 1 - Tardis.alpha : 0;
                    drawAngel(number, angelOffset[sortedAngel[i]], distance_Ang_Pla[sortedAngel[i]] * Math.cos(ray.angle - player.point_of_view), angel_alpha);
                    onSight[sortedAngel[i]] = angel_alpha != 0;
                }
            }
        }
        if (Math.min(distance[0], distance[1]) > 0.3 && !debug)
            drawRain(number);
        return onSight;
    }


    private int[] sort(double[] array) {
        int[] result = new int[Angel.NUMBER_OF_ANGELS];
        int temp;
        for (int i = 0; i < Angel.NUMBER_OF_ANGELS; i++) {
            result[i] = i;
        }
        for (int i = 0; i < Angel.NUMBER_OF_ANGELS - 1; i++) {
            for (int j = 0; j < Angel.NUMBER_OF_ANGELS - 1 - i; j++) {
                if (array[result[j]] < array[result[j + 1]]) {
                    temp = result[j];
                    result[j] = result[j + 1];
                    result[j + 1] = temp;
                }
            }
        }
        return result;
    }


    public void buildScreen(Maze maze, Player player, Angel[] angels) {
        // long time = System.currentTimeMillis();
        gc.drawImage(Resources.Textures.SKY, 0, 0);

        double[] distance_Ang_Pla = new double[Angel.NUMBER_OF_ANGELS];
        double[] alpha_angle = new double[Angel.NUMBER_OF_ANGELS];

        for (int i = 0; i < Angel.NUMBER_OF_ANGELS; i++) {
            // System.out.println(i);
            distance_Ang_Pla[i] = Maze.distenceBetween(player.coords, angels[i].coords);
            //System.out.println(i);
            alpha_angle[i] = Math.acos((angels[i].coords.x - player.coords.x) / distance_Ang_Pla[i]) * (angels[i].coords.y - player.coords.y < 0 ? -1 : 1);

            alpha_angle[i] = (alpha_angle[i] + CIRCLE) % CIRCLE;
            angels[i].isOnSight = false;
        }
        for (int i = 0; i < resolution; i++) {
            boolean[] onSight = buildColumn(maze, player, i, alpha_angle, distance_Ang_Pla);
            for (int j = 0; j < Angel.NUMBER_OF_ANGELS; j++)
                if (onSight[j]) angels[j].isOnSight = true;
        }
        drawWeapon(player);
        // Runtime.getRuntime().gc();
        // System.out.println(System.currentTimeMillis()-time);
    }


    boolean falseScreen(Maze maze, Player player, Maze.Coords falseCoords) {
        double distance_Ang_Pla = Maze.distenceBetween(player.coords, falseCoords);
        double alpha_angle = Math.acos((falseCoords.x - player.coords.x) / distance_Ang_Pla) * (falseCoords.y - player.coords.y < 0 ? -1 : 1);

        boolean angelIsOnSight = false;
        for (int i = 0; i < resolution; i++) {
            if (falseBuildColumn(maze, player, i, alpha_angle, distance_Ang_Pla))
                angelIsOnSight = true;
        }
        return angelIsOnSight;
    }


    private boolean falseBuildColumn(Maze maze, Player player, int number, double alpha_angle, double distance_Ang_Pla) {
        boolean angelIsOnSight = false;
        double angle = Math.atan2(prPlane.columnWidth * number - prPlane.width / 2, prPlane.distance);

        Ray ray = new Ray((player.point_of_view - angle + CIRCLE) % CIRCLE, maze, player.coords);

        double distance = ray.rayPoints[Resources.Blocks.TARDIS] != null ? ray.rayPoints[Resources.Blocks.TARDIS].distance :
                ray.rayPoints[Resources.Blocks.WALL] != null ? ray.rayPoints[Resources.Blocks.WALL].distance :
                        Double.POSITIVE_INFINITY;

        if ((distance_Ang_Pla < distance) && (Math.cos(alpha_angle - player.point_of_view) > cosAngelAngel)) {
            double angel_offset = distance_Ang_Pla * Math.sin(ray.angle - alpha_angle);
            if (Math.abs(angel_offset) < Angel.HALFWIDTH) {
                angelIsOnSight = true;
            }
        }
        return angelIsOnSight;
    }


    public void endGameScreen(String message) {
        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(1.0);

        gc.fillRect(0, 0, prPlane.width, prPlane.height);

        gc.setFill(Color.WHITE);
        gc.fillText(message, prPlane.width / 2 - message.length() / 2, prPlane.height / 3);

        gc.fillText("Restart?  [1]", prPlane.width / 2 - message.length() / 2, prPlane.height / 3 + 50);
        gc.fillText("Quit?     [2]", prPlane.width / 2 - message.length() / 2, prPlane.height / 3 + 75);
    }


    private void drawWeapon(Player player) {
        Image weapon = player.weapon;

        double width = 0.7 * weapon.getWidth();
        double height = 0.7 * weapon.getHeight();

        double startX = prPlane.width - width + 30 + 15 * Math.cos(weaponAngle);
        double startY = prPlane.height - height + 10 * Math.abs(Math.sin(weaponAngle));

        gc.drawImage(weapon, startX, startY, width, height);
    }

}
