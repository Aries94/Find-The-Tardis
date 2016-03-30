package data;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;



public class GameCamera {
    static final double CIRCLE = Math.PI * 2;
    static double weaponAngle = 0;

    final private int MAX_RESOLUTION = 900;
    final private int MAX_VIEW_DISTANCE = 20;
    final private double SHADING_DISTANCE = 4;
    final private int MIN_RAINDROPS = 0;
    final private int MAX_RAINDROPS = 1;
    final private double EPSILON = 0.0005;
    final private String version = "v2.0a";


    protected GraphicsContext gc;
    protected int resolution;

    private PrPlane prPlane;
    private double cosAngelAngel;
    private boolean debug;
    private Ray ray;
    private Ray falseRay;


    public GameCamera(GraphicsContext gc, int resolution, double fov, boolean debug) {
        this.resolution = (resolution > 5 && resolution < MAX_RESOLUTION) ? resolution : MAX_RESOLUTION;
        this.gc = gc;
        this.debug = debug;
        cosAngelAngel = Math.cos(fov / 1.5);
        prPlane = new PrPlane(fov);
        ray=new Ray();
        falseRay=new Ray();
    }


    private class PrPlane {
        double width;
        double height;
        double distance;
        double columnWidth;
        double coef;
        double halfFOV;
        int resolution;
        DoubleProperty width1=new SimpleDoubleProperty();
        DoubleProperty height1=new SimpleDoubleProperty();
        DoubleBinding coef1;
        DoubleBinding distance1;
        IntegerBinding resolution1;
       // DoubleBinding resolution;


        PrPlane(double FOV) {
            halfFOV=FOV/2.0;
            width1.bind(gc.getCanvas().widthProperty());
            height1.bind(gc.getCanvas().heightProperty());

            distance1=new DoubleBinding() {
                {
                    super.bind(width1);
                }
                @Override
                protected double computeValue() {
                    return width1.get()/2/Math.tan(halfFOV);
                }
            };

            coef1 = new DoubleBinding() {
                {
                    super.bind(width1,height1,distance1);
                }
                @Override
                protected double computeValue() {
                    return height1.get()*distance1.get()/width1.get()/Resources.Heights.PLAYER;
                }
            };

            resolution1 = new IntegerBinding() {
                {
                    super.bind(width1);
                }
                @Override
                protected int computeValue() {
                    return (int)width1.get()/2;
                }
            };

        }

        public void get() {
            width=width1.get();
            width-=(int)width %2;
            height=height1.get();
            distance=distance1.get();
            resolution=resolution1.get();
            coef=coef1.get();
            columnWidth=width/resolution;
        }
    }


    class Ray {
        double angle;
        RayPoint[] rayPoints;


        Ray(/*double angle, Maze maze, Maze.Coords startPoint*/) {
           // this.angle = angle;
           // cast(maze, startPoint, 0);
            rayPoints= new RayPoint[3];
            for(int i=1;i<3;i++){
                rayPoints[i]=new RayPoint();
            }
            refresh();
        }


        private void refresh (){
            rayPoints[Resources.Blocks.TARDIS].distance=
            rayPoints[Resources.Blocks.WALL].distance=Double.POSITIVE_INFINITY;
        }


        class RayPoint {
            double distance;
            double entry;

            void set(double distance, double entry) {
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
                    rayPoints[Resources.Blocks.WALL].set(newDist, entry(newPoint, angle, isX));
                    break;
                case Resources.Blocks.TARDIS:
                    rayPoints[Resources.Blocks.TARDIS].set(newDist, entry(newPoint, angle, isX));
                default:
                    if (newDist < GameCamera.this.MAX_VIEW_DISTANCE)
                        cast(maze, newPoint, newDist);
                    break;
            }
        }


    }


    private void drawTexture(Ray.RayPoint rPoint, Image texture, int number, double angle, double blockHeight, double alpha, double verticalLook) {
        double distance = rPoint.distance * Math.cos(angle);

        double texture_startX = texture.getWidth() * rPoint.entry;
        double texture_startY = 0;
        double texture_width = texture.getWidth() / resolution;
        double texture_height = texture.getHeight();

        double startX = prPlane.columnWidth * number;
        double width = prPlane.columnWidth;

        double height = blockHeight * prPlane.coef / distance;
        height += ((int) height) % 2;
        double startY = (prPlane.height / 2) * (1 + 1 / distance) - height+verticalLook;

        gc.setGlobalAlpha(alpha);
        gc.drawImage(texture, texture_startX, texture_startY, texture_width, texture_height, startX, startY, width, height);

        gc.setFill(Color.BLACK);

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


    private double[] drawColumn( int number, double angle, double verticalLook) {
        double[] distance = new double[]{Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY};
        if (ray.rayPoints[Resources.Blocks.WALL].distance<Double.POSITIVE_INFINITY) {
            drawTexture(ray.rayPoints[Resources.Blocks.WALL], Resources.Textures.WALL, number, angle, Resources.Heights.WALL, 1,verticalLook);
            distance[0] = ray.rayPoints[Resources.Blocks.WALL].distance;
        }
        if (ray.rayPoints[Resources.Blocks.TARDIS].distance<Double.POSITIVE_INFINITY) {
            drawTexture(ray.rayPoints[Resources.Blocks.TARDIS], Resources.Textures.TARDIS, number, angle, Resources.Heights.TARDIS, Tardis.alpha,verticalLook);
            distance[1] = ray.rayPoints[Resources.Blocks.TARDIS].distance;
        }
         /*  if (distance>0.3)
                drawRain(number);*/
        return distance;
    }


    private void drawAngel(int angelNumber, int number, double offset, double distance, double alpha, double vertikalLook) {
        double startX = prPlane.columnWidth * number;
        double width = prPlane.columnWidth;

        Image texture = Resources.angelTextures[Angel.textureID[angelNumber]];
        Image darkTexture = Resources.darkAngelTextures[Angel.textureID[angelNumber]];

        double texture_startX = texture.getWidth() * (offset + Angel.HALFWIDTH) / Angel.HALFWIDTH / 2;
        double texture_startY = 0;
        double texture_width = texture.getWidth() / resolution;
        double texture_height = texture.getHeight();

        double height = Resources.Heights.ANGEL * prPlane.coef / distance;
        height += ((int) height) % 2;
        double startY = (prPlane.height / 2) * (1 + 1 / distance) - height+vertikalLook;

        gc.setGlobalAlpha(alpha);
        gc.drawImage(texture, texture_startX, texture_startY, texture_width, texture_height, startX, startY, width, height);

        if (!debug) {
            gc.setGlobalAlpha(Math.min(distance < SHADING_DISTANCE ? distance / SHADING_DISTANCE : 1.0,alpha));
            gc.drawImage(darkTexture, texture_startX, texture_startY, texture_width, texture_height, startX, startY, width, height);

        }
        gc.setGlobalAlpha(1.0);
    }


    boolean[] buildColumn(Maze maze, Player player, int number, double[] alpha_angle, double[] distance_Ang_Pla) {
        boolean[] onSight = new boolean[Angel.NUMBER_OF_ANGELS];
        double[] angelOffset = new double[Angel.NUMBER_OF_ANGELS];

        double angle = Math.atan2(prPlane.columnWidth * number - prPlane.width / 2, prPlane.distance);

        int[] sortedAngel = sort(distance_Ang_Pla);

        //Ray ray = new Ray((player.point_of_view - angle + CIRCLE) % CIRCLE, maze, player.coords);

        ray.refresh();
        ray.angle=player.point_of_view - angle;
        ray.cast(maze,player.coords,0);

        double[] distance = drawColumn(number, angle,player.verticalLook);
        for (int i = 0; i < Angel.NUMBER_OF_ANGELS; i++) {
            if ((distance_Ang_Pla[sortedAngel[i]]<SHADING_DISTANCE)&&(distance_Ang_Pla[sortedAngel[i]] < distance[0]) && (Math.cos(alpha_angle[sortedAngel[i]] - player.point_of_view) > cosAngelAngel)) {
                angelOffset[sortedAngel[i]] = distance_Ang_Pla[sortedAngel[i]] * Math.sin(ray.angle - alpha_angle[sortedAngel[i]]);
                if (Math.abs(angelOffset[sortedAngel[i]]) < Angel.HALFWIDTH) {
                    //double angel_alpha=distance[1]<Double.POSITIVE_INFINITY?(distance[1]<distance_Ang_Pla[i]? 0:Tardis.alpha):1.0;
                    double angel_alpha = distance_Ang_Pla[sortedAngel[i]] < distance[1] ? 1.0 : distance[0] < Double.POSITIVE_INFINITY ? 1 - Tardis.alpha : 0;
                    drawAngel(sortedAngel[i],number, angelOffset[sortedAngel[i]], distance_Ang_Pla[sortedAngel[i]] * Math.cos(ray.angle - player.point_of_view), angel_alpha,player.verticalLook);
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
        prPlane.get();

        gc.save();

        // long time = System.currentTimeMillis();
        gc.drawImage(Resources.Textures.SKY, 0,200-player.verticalLook/2,Resources.Textures.SKY.getWidth(),Resources.Textures.SKY.getHeight()-400,0,0,prPlane.width,prPlane.height);

        double[] distance_Ang_Pla = new double[Angel.NUMBER_OF_ANGELS];
        double[] alpha_angle = new double[Angel.NUMBER_OF_ANGELS];

        for (int i = 0; i < Angel.NUMBER_OF_ANGELS; i++) {
            // System.out.println(i);
            distance_Ang_Pla[i] = Maze.distenceBetween(player.coords, angels[i].coords);
            //System.out.println(i);
            angels[i].alpha_angle= alpha_angle[i] = Math.acos((angels[i].coords.x - player.coords.x) / distance_Ang_Pla[i]) * (angels[i].coords.y - player.coords.y < 0 ? -1 : 1);

            //alpha_angle[i] = (alpha_angle[i] + CIRCLE) % CIRCLE;
            angels[i].isOnSight = false;
        }
        for (int i = 0; i < prPlane.resolution; i++) {
            boolean[] onSight = buildColumn(maze, player, i, alpha_angle, distance_Ang_Pla);
            for (int j = 0; j < Angel.NUMBER_OF_ANGELS; j++)
                if (onSight[j]) angels[j].isOnSight = true;
        }

        drawWeapon(player);
        gc.restore();
        // Runtime.getRuntime().gc();
        // System.out.println(System.currentTimeMillis()-time);
    }


    boolean falseScreen(Maze maze, Player player, Maze.Coords falseCoords) {
        double distance_Ang_Pla = Maze.distenceBetween(player.coords, falseCoords);
        double alpha_angle = Math.acos((falseCoords.x - player.coords.x) / distance_Ang_Pla) * (falseCoords.y - player.coords.y < 0 ? -1 : 1);

        boolean angelIsOnSight = false;
        for (int i = 0; i < prPlane.resolution1.get(); i++) {
            if (falseBuildColumn(maze, player, i, alpha_angle, distance_Ang_Pla))
                angelIsOnSight = true;
        }
        return angelIsOnSight;
    }


    private boolean falseBuildColumn(Maze maze, Player player, int number, double alpha_angle, double distance_Ang_Pla) {
        boolean angelIsOnSight = false;
        double angle = Math.atan2(prPlane.columnWidth * number - prPlane.width / 2, prPlane.distance);

        //Ray ray = new Ray((player.point_of_view - angle + CIRCLE) % CIRCLE, maze, player.coords);

        falseRay.refresh();
        falseRay.angle=player.point_of_view - angle;
        falseRay.cast(maze,player.coords,0);

        double distance = falseRay.rayPoints[Resources.Blocks.TARDIS] != null ? falseRay.rayPoints[Resources.Blocks.TARDIS].distance :
                falseRay.rayPoints[Resources.Blocks.WALL] != null ? falseRay.rayPoints[Resources.Blocks.WALL].distance :
                        Double.POSITIVE_INFINITY;

        if ((distance_Ang_Pla<SHADING_DISTANCE)&&(distance_Ang_Pla < distance) && (Math.cos(alpha_angle - player.point_of_view) > cosAngelAngel)) {
            double angel_offset = distance_Ang_Pla * Math.sin(falseRay.angle - alpha_angle);
            if (Math.abs(angel_offset) < Angel.HALFWIDTH) {
                angelIsOnSight = true;
            }
        }
        return angelIsOnSight;
    }


    public void endGameScreen(String message, int vc, int dc) {
        prPlane.get();

        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(1.0);

        gc.fillRect(0, 0, prPlane.width, prPlane.height);

        gc.setFill(Color.WHITE);
        gc.fillText(message, prPlane.width / 2 - message.length() / 2, prPlane.height / 3);

        gc.fillText("[1] Restart", prPlane.width / 2 - message.length() / 2, prPlane.height / 3 + 50);
        gc.fillText("[2] Quit", prPlane.width / 2 - message.length() / 2, prPlane.height / 3 + 75);
        gc.fillText("[3] Main Menu", prPlane.width / 2 - message.length() / 2, prPlane.height / 3 + 100);

        gc.fillText("You   : "+Integer.toString(vc),prPlane.width*3/4,prPlane.height*3/4);
        gc.fillText("Angels: "+Integer.toString(dc),prPlane.width*3/4,prPlane.height*3/4+20);
    }


    private void drawWeapon(Player player) {
        Image weapon = player.weapon;

        double width = 0.7 * weapon.getWidth();
        double height = 0.7 * weapon.getHeight();

        double startX = prPlane.width - width + 30 + 15 * Math.cos(weaponAngle);
        double startY = prPlane.height - height + 10 * Math.abs(Math.sin(weaponAngle));

        gc.drawImage(weapon, startX, startY, width, height);
    }


    public void menuScreen(){
        prPlane.get();
        gc.drawImage(Resources.Textures.MAIN_MENU,0,0,prPlane.width,prPlane.height);

        gc.setFill(Color.WHITE);
        gc.fillText("Find the Tardis", prPlane.width / 2-7 -20, prPlane.height / 3+100);

        gc.fillText("[1] Start", prPlane.width / 2-3- 20, prPlane.height / 3+150);
        gc.fillText("[2] Quit", prPlane.width / 2-3-20 , prPlane.height / 3+175);

        gc.fillText(version,prPlane.width-version.length()*5,prPlane.height-10);

    }

}
