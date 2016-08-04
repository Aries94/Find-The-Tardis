package data;

import javafx.scene.input.KeyCode;

import java.util.HashSet;

final class Tardis extends Resources.Entity{
    static public double alpha;

    private final long D_COOLDOWN = 120000;
    private final long A_COOLDOWN = 5000;

    public boolean isHere;

    private enum States {Standing, Appearing, Disappearing, inVortex}
    private double[] moving_Alphas = new double[]{1, 1, 0.25, 0.75, 0.25, 0.75, 0, 0};
    private double movingSpeed = 0.025;
    private int currentMovingStage;
    private States state;
    private long time;
    private boolean debug;

    private Player player;
    private Maze maze;
    private Monsters monsters;
    private GameCamera gameCamera;


    //singlton
    private static Tardis instance = new Tardis();

    private Tardis(){
    }


    static Tardis getInstance(){
        return instance;
    }


    void init(boolean debug) {
        player=Player.getInstance();
        monsters = Monsters.getInstance();
        gameCamera=GameCamera.getInstance();
        maze=Maze.getInstance();


        Maze.Coords coords = maze.lookForEmpty();
        this.coords.set(coords.x + 0.5, coords.y + 0.5);
        maze.map[(int) coords.x][(int) coords.y] = Resources.Blocks.Tardis;
        alpha = 1;
        state = States.Standing;
        isHere = true;
        time = System.currentTimeMillis();
        this.debug = debug;


    }


    public void update(HashSet<?> keySet) {
        //System.out.println(state);
        switch (state) {
            case Standing:
                if (keySet.contains(KeyCode.E) || (System.currentTimeMillis() - time > D_COOLDOWN && !debug)) {
                    state = States.Disappearing;
                    isHere = false;
                    time = System.currentTimeMillis();
                }
                break;
            case inVortex:
                if (keySet.contains(KeyCode.E) || (System.currentTimeMillis() - time > A_COOLDOWN && !debug)) {
                    time = System.currentTimeMillis();
                    state = States.Appearing;
                    Maze.Coords coords;
                    do {
                        coords = maze.lookForEmpty();
                    } while (maze.pathExists(coords, player.coords));
                    this.coords.set(coords.x + 0.5, coords.y + 0.5);
                    maze.map[(int) coords.x][(int) coords.y] = Resources.Blocks.Tardis;
                }
                break;
            case Disappearing:
                currentMovingStage = moving(currentMovingStage, 1);
                if (currentMovingStage == moving_Alphas.length - 2) {
                    state = States.inVortex;
                    maze.map[(int) coords.x][(int) coords.y] = Resources.Blocks.Empty;
                }
                break;
            case Appearing:
                currentMovingStage = moving(currentMovingStage, -1);
                if (currentMovingStage == 1) {
                    state = States.Standing;
                    isHere = true;
                }
                break;
        }
    }


    private int moving(int stage, int factor) {
        int mult = moving_Alphas[stage] < moving_Alphas[stage + 1] ? 1 : -1;
        alpha += mult * movingSpeed;
        switch (mult) {
            case 1:
                if (alpha >= moving_Alphas[stage + factor]) {
                    return (stage + factor);
                } else {
                    return (stage);
                }
            case -1:
                if (this.alpha <= moving_Alphas[stage + factor]) {
                    return (stage + factor);
                } else {
                    return (stage);
                }
            default:
                return -1;
        }
    }
}
