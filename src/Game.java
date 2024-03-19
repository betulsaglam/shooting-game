import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends JFrame {
    boolean gameOver;
    ArrayList<Enemy> enemies;
    ArrayList<Friend> friends;
    ArrayList<Fire> fires;
    AirCraft airCraft;
    GamePanel gamePanel;
    Random random;
    ReentrantLock lock;


    Game(){
        setSize(500,500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel = new GamePanel();
        add(gamePanel);
        addKeyListener(gamePanel);

        setVisible(true);

        friends = new ArrayList<>();
        enemies = new ArrayList<>();
        fires =  new ArrayList<>();

        random = new Random();
        lock = new ReentrantLock();
        gameOver = false;


    }
    class ResultWindow extends JFrame {
        JLabel text;
        public ResultWindow(boolean result){

            setSize(300,200);
            setResizable(false);
            setLayout(null);
            setBackground(Color.WHITE);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            if (result)
                text = new JLabel("Oyunu kazandınız");
            else
                text = new JLabel("Oyunu kaybettiniz");

            text.setBounds(60,60,200,30);
            text.setOpaque(true);
            text.setFont(new Font("Arial",Font.PLAIN,20));

            add(text);
            setVisible(true);
        }

    }

    class GamePanel extends JPanel implements KeyListener, MouseListener {
        public GamePanel(){
            setPreferredSize(new Dimension(500,500));
            addMouseListener(this);
        }

        public void paintComponent(Graphics g){
            lock.lock();
            super.paintComponent(g);

            g.setColor(Color.BLACK);
            for (int i = 0; i < enemies.size(); i++){
                Enemy enemy = enemies.get(i);
                g.fillRect(enemy.x,enemy.y,10,10);
            }


            g.setColor(Color.GREEN);
            for (int i = 0; i < friends.size(); i++){
                Friend friend = friends.get(i);

                if (friend != null)
                    g.fillRect(friend.x,friend.y,10,10);
            }


            g.setColor(Color.RED);
            if (airCraft != null)
                g.fillRect(airCraft.x,airCraft.y,10,10);

            for (int i = 0; i < fires.size(); i++) {

                Fire fire = fires.get(i);

                if (fire != null && fire.isAlive()){
                    g.setColor(fire.color);
                    if (fire.x_left >= 0)
                        g.fillRect(fire.x_left, fire.y, 5, 5);

                    if (fire.x_right <= 470)
                        g.fillRect(fire.x_right, fire.y, 5, 5);
                }
            }
            lock.unlock();

        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {

            switch (e.getKeyChar()){
                case 'a':
                    if (airCraft.x - 10 >= 0)
                        airCraft.x -= 10;
                    break;
                case 'd':
                    if (airCraft.x + 10 <= 470)
                        airCraft.x += 10;
                    break;
                case 'w':
                    if (airCraft.y - 10 >= 0)
                        airCraft.y -= 10;
                    break;
                case 's':
                    if (airCraft.y + 10 <= 450)
                        airCraft.y += 10;
                    break;
            }
            repaint();
        }

        @Override
        public void keyReleased(KeyEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {

            Fire playerFire = new Fire(airCraft.x,airCraft.y,Color.ORANGE,false);
            fires.add(playerFire);
            playerFire.start();

        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
    }

    public Point getRandomPoint(){

        int x = random.nextInt(48)*10;
        int y = random.nextInt(46)*10;

        for (Friend f: friends)
            if (f.x == x && f.y == y)
                return getRandomPoint();

        for (Enemy e: enemies)
            if (e.x == x && e.y == y)
                return  getRandomPoint();

        Point point = new Point(x,y);

        return point;
    }

    public void crashed(){
        ArrayList<Friend> friendsCopy = new ArrayList<>(friends);
        ArrayList<Enemy> enemiesCopy = new ArrayList<>(enemies);

        for (Friend friend: friendsCopy){

            for (Enemy enemy: enemiesCopy){

                if (friend != null && enemy != null && friend.x == enemy.x && friend.y == enemy.y) {
                    friend.isAlive = false;
                    enemy.isAlive = false;
                }
            }
        }
    }

    public void game_finish(boolean result) {
        new ResultWindow(result);
        this.dispose();
        gameOver = true;
    }

    class Enemy extends Thread{
        int x,y;
        boolean isAlive;
        Fire enemyFire;
        ReentrantLock enemyLock;
        public Enemy(){
            Point p = getRandomPoint();
            x = p.x;
            y = p.y;
            enemyLock = lock;
            isAlive = true;
        }

        public void move(){
            int way = random.nextInt(4);

            switch (way){
                case 0:
                    if (x + 10 <= 470)
                        x += 10;
                    break;
                case 1:
                    if (y - 10 >= 0)
                        y -= 10;
                    break;
                case 2:
                    if (x - 10 >= 0)
                        x -= 10;
                    break;
                case 3:
                    if (y + 10 <= 450)
                        y += 10;
                    break;
            }
            crashed();

        }

        public void run(){

            enemies.add(this);

             while (!gameOver){

                if (!enemies.contains(this))
                    break;

                 enemyFire = new Fire(this.x,this.y,Color.BLUE,true,true);
                 enemyFire.start();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}

                move();

                repaint();

                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {}

                 move();

            }
             enemies.remove(this);
        }

    }

    class Friend extends Thread{
        int x,y;
        boolean isAlive;
        Fire friendFire;
        ReentrantLock friendLock;
        public Friend(){
            Point p = getRandomPoint();
            x = p.x;
            y = p.y;
            friendLock = lock;
            isAlive = true;
        }

        public void move(){
            int way = random.nextInt(4);

            switch (way){
                case 0:
                    if (x + 10 <= 470)
                        x += 10;
                    break;
                case 1:
                    if (y - 10 >= 0)
                        y -= 10;
                    break;
                case 2:
                    if (x - 10 >= 0)
                        x -= 10;
                    break;
                case 3:
                    if (y + 10 <= 450)
                        y += 10;
                    break;
            }
            crashed();

        }

        public void run(){

            friends.add(this);

            while (!gameOver){

                if (!friends.contains(this))
                    break;

                friendFire = new Fire(this.x, this.y, new Color(107, 27, 171), true,false);
                friendFire.start();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){}

                move();

                repaint();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){}

                move();

            }
            friends.remove(this);
        }
    }

    class AirCraft extends Thread{
        int x,y;
        public AirCraft(){
            x = 250;
            y = 250;
        }

        public void run(){
            airCraft = this;
        }

    }

    class Fire extends Thread{

        int x_left,x_right,y;
        boolean isNPC;
        boolean isEnemy;
        Color color;
        ReentrantLock fireLock;
        public Fire(int x, int y, Color color,boolean isNPC){
            x_left = x-5;
            x_right = x+10;
            this.y = y;
            this.color = color;
            this.isNPC = isNPC;
            fireLock = lock;
        }

        public Fire(int x, int y, Color color,boolean isNPC,boolean isEnemy){
            x_left = x-5;
            x_right = x+10;
            this.y = y;
            this.color = color;
            this.isNPC = isNPC;
            this.isEnemy = isEnemy;
            fireLock = lock;
        }

        public void run(){

            fires.add(this);

            while (!gameOver && (x_left >= 0 || x_right <= 470)){

                x_left -= 10;
                x_right += 10;

                repaint();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e){}

                lock.lock();
                if (!isNPC || !isEnemy){

                    Iterator<Enemy> enemyItr = enemies.iterator();

                    while(enemyItr.hasNext()){

                        Enemy enemy = enemyItr.next();


                        if ((x_left >= enemy.x && x_left <= enemy.x+5 && y >= enemy.y && y <= enemy.y+5) || (x_right >= enemy.x && x_right <= enemy.x+5 && y >= enemy.y && y <= enemy.y+5)) {
                            enemyItr.remove();
                            enemy.isAlive = false;
                        }

                        if (enemy.x == airCraft.x && enemy.y == airCraft.y) {
                            lock.unlock();
                            enemyItr.remove();
                            enemy.isAlive = false;
                            game_finish(false);
                            break;
                        }

                        if (!enemy.isAlive && enemies.contains(enemy))
                            enemyItr.remove();

                        if (enemies.size() == 0) {
                            lock.unlock();
                            game_finish(true);
                            break;
                        }
                    }

                }
                else{
                    if ((x_left >= airCraft.x && x_left <= airCraft.x+5 && y >= airCraft.y && y <= airCraft.y+5) || (x_right >= airCraft.x && x_right <= airCraft.x+5 && y >= airCraft.y && y <= airCraft.y+5)) {
                        lock.unlock();
                        game_finish(false);
                        break;
                    }

                    Iterator<Friend> friendItr = friends.iterator();

                    while (friendItr.hasNext()){
                        Friend friend = friendItr.next();

                        if (!friend.isAlive && friends.contains(friend))
                            friendItr.remove();

                        if ((x_left >= friend.x && x_left <= friend.x+5 && y >= friend.y && y <= friend.y+5) || (x_right >= friend.x && x_right <= friend.x+5 && y >= friend.y && y <= friend.y+5)) {
                            friendItr.remove();
                            friend.isAlive = false;

                            x_left = -1;
                            x_right = 471;
                            break;
                        }

                    }

                }
                if (!gameOver) lock.unlock();

            }
            fires.remove(this);
        }


    }


}

