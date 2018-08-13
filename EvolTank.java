


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


public class EvolTank extends JFrame  {

    Dimension totalSize;

    public static void main(String s[]) {
        EvolTank f = new EvolTank();
        final Tank t = new Tank();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ActionListener taskPerformer;
        taskPerformer = new ActionListener() {
            private int counter=0;
            @Override public void actionPerformed(ActionEvent evt) {
                counter++;
                if (counter>=t.speed) {
                  t.repaint();
                  counter=0;
                }
            }
        };
        f.addKeyListener(t);
        f.add(t);
        new javax.swing.Timer(12, taskPerformer).start();
        f.pack();
        f.setTitle("Evolution Tank");
        f.setSize(new Dimension(800,600));
        f.setVisible(true);
    }

}

class Tank extends JComponent implements KeyListener {

    public static final int TANK_WIDTH=650;  //both dimensions must be a multiple of 25
    public static final int TANK_HEIGHT=450;
    private int camX=0;
    private int camY=0;
    private int zoom=1;
    private boolean display=true;
    private int frames=0;
    public int speed=1;  //allows the simulation to be slowed down

    public static ArrayList<Photon> photons;
    public static ArrayList<Cell> cells;

    public Tank() {
        cells=new ArrayList(1000);
        int[] initcode=new int[Cell.CODE_SIZE];
        /*
        This is the code that the first cells start with:

        if >(energy,90) reproduce endif
        if <(cpx,0) moveleft endif
        if >(cpx,0) moveright endif
        if <(cpy,0) moveup endif
        if >(cpy,0) movedown endif

        (note that operators like '>' always appear before their arguments, so '>(energy,90)' means 'energy > 90')
        */
        initcode[0]=-1000001;
        initcode[1]=-1000024;
        initcode[2]=1000010;
        initcode[3]=90;
        initcode[4]=-1000008;
        initcode[5]=-1000010;
        initcode[6]=-1000001;
        initcode[7]=-1000025;
        initcode[8]=1000001;
        initcode[9]=0;
        initcode[10]=-1000004;
        initcode[11]=-1000010;
        initcode[12]=-1000001;
        initcode[13]=-1000024;
        initcode[14]=1000001;
        initcode[15]=0;
        initcode[16]=-1000005;
        initcode[17]=-1000010;
        initcode[18]=-1000001;
        initcode[19]=-1000025;
        initcode[20]=1000002;
        initcode[21]=0;
        initcode[22]=-1000006;
        initcode[23]=-1000010;
        initcode[24]=-1000001;
        initcode[25]=-1000024;
        initcode[26]=1000002;
        initcode[27]=0;
        initcode[28]=-1000007;
        initcode[29]=-1000010;
        for (int i=30; i<Cell.CODE_SIZE; i++)  //the unused portion of the code must be filled with blanks
            initcode[i]=-1000050;

        for (int i=0; i<100; i++)
            cells.add(new Cell((int)(Math.random()*TANK_WIDTH),(int)(Math.random()*TANK_HEIGHT),25,initcode,false,0));
        photons=new ArrayList(4000);
        for (int i=0; i<2000; i++)
            photons.add(new Photon((int)(Math.random()*TANK_WIDTH),(int)(Math.random()*TANK_HEIGHT)));
    }


    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'w'&&camY>-100) {
            camY-=25;
        }
        if (e.getKeyChar() == 's'&&camY<TANK_HEIGHT+100) {
            camY+=25;
        }
        if (e.getKeyChar() == 'a'&&camX>-100) {
            camX-=25;
        }
        if (e.getKeyChar() == 'd'&&camX<TANK_WIDTH+100) {
            camX+=25;
        }
        if ((e.getKeyChar()=='='||e.getKeyChar()=='+')&&zoom<4) {
            zoom++;
        }
        if (e.getKeyChar()=='-'&&zoom>1) {
            zoom--;
        }
        if (e.getKeyChar()==' ') {
            display=!display;
        }
        if (e.getKeyChar()=='1') {
            speed=1;
        }
        if (e.getKeyChar()=='2') {
            speed=2;
        }
        if (e.getKeyChar()=='3') {
            speed=6;
        }
        if (e.getKeyChar()=='i') {  //print statistical information
            System.out.println("~");
            System.out.println("Frames elapsed (in thousands): "+frames/1000);
            System.out.println("Cell population: "+cells.size());
            System.out.println("Photon population: "+photons.size());
            int codeLen=0, commandCt=0, age=0;
            for (int i=0; i<cells.size(); i++) {
                int j=0;
                while (j<Cell.CODE_SIZE&&cells.get(i).code[j]!=-1000050) {j++;}
                codeLen+=j;
                commandCt+=cells.get(i).commands;
                age+=cells.get(i).age;
            }
            System.out.println("Avg age: "+(float)age/cells.size());
            System.out.println("Avg code length: "+(float)codeLen/cells.size());
            System.out.println("Avg commands executed: "+(float)commandCt/cells.size());
            System.out.println("Top 3 phenotypes:");
            int first, second, third;
            first=second=third=0;
            int fc, sc, tc;
            fc=sc=tc=0;
            for (int i=0; i<100; i++) {
                  int c=0;
                 for (int j=0; j<cells.size(); j++)
                      if (cells.get(j).pheno==i)
                          c++;
                  if (c>=fc) {
                      third=second;
                      tc=sc;
                      second=first;
                      sc=fc;
                      first=i;
                      fc=c;
                  }
                  else if (c>=sc) {
                      third=second;
                      tc=sc;
                      second=i;
                      sc=c;
                  }
                  else if (c>=tc) {
                      third=i;
                      tc=c;
                  }
            }
            System.out.println("  "+first+": "+fc);
            System.out.println("  "+second+": "+sc);
            System.out.println("  "+third+": "+tc);
        }
    }


    @Override public void paint(Graphics g) {   //the main updating function called every frame
        Graphics2D g2 = (Graphics2D) g;
        if (display) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.black);
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(.02f,.02f,.02f));
            g2.fillRect(0-camX*zoom,0-camY*zoom,TANK_WIDTH*zoom,TANK_HEIGHT*zoom);
            g2.setColor(new Color(.4f,.4f,.4f));
          }

    //Create, update, and draw photons

        if (photons.size()<5000) //sanity cap
            for (int i=0; i<21; i++)  //regrowth rate for photons
                  photons.add(new Photon((int)(Math.random()*TANK_WIDTH),(int)(Math.random()*TANK_HEIGHT)));

        for (int i=0; i<photons.size(); i++)
            if (photons.get(i).alive) {
            photons.get(i).update();
            photons.get(i).x=Math.min(Math.max(photons.get(i).x,0), TANK_WIDTH-1);
            photons.get(i).y=Math.min(Math.max(photons.get(i).y,0), TANK_HEIGHT-1);
            }
        if (display)
            for (int i=0; i<photons.size(); i++)
                if (photons.get(i).alive) { g2.fillRect((photons.get(i).x-camX)*zoom, (photons.get(i).y-camY)*zoom, zoom, zoom);}


  //Get spatial data for cells
  //Each cell gets to know information about the four closest other cells. This is for collision detection as well as intelligence

        int csize=cells.size();
        int[] closestCells=new int[cells.size()];
        int[] nextCells=new int[cells.size()];
        int[] thirdCells=new int[cells.size()];
        int[] fourthCells=new int[cells.size()];


        ArrayList<Integer>[][] cBuckets=new ArrayList[TANK_WIDTH/25][TANK_HEIGHT/25];  //create a grid of 25x25 squares
        for (int i=0; i<TANK_WIDTH/25; i++)
            for (int j=0; j<TANK_HEIGHT/25; j++)
                cBuckets[i][j]=new ArrayList(10);
        for (int i=0; i<cells.size(); i++) {
            cells.get(i).x=Math.min(Math.max(cells.get(i).x,0), TANK_WIDTH-1);  //first ensure the cell isn't out of bounds
            cells.get(i).y=Math.min(Math.max(cells.get(i).y,0), TANK_HEIGHT-1);
            cBuckets[cells.get(i).x/25][cells.get(i).y/25].add(i);   //place the indices to the cells into the buckets
          }

        for (int i=0; i<cells.size(); i++) {
            int cminD, c2minD, c3minD, c4minD;
            cminD=c2minD=c3minD=c4minD=100;
            int currentDistance;
            for (int u=Math.max(0,cells.get(i).x/25-1); u<=Math.min(cells.get(i).x/25+1,TANK_WIDTH/25-1); u++)
                for (int v=Math.max(0,cells.get(i).y/25-1); v<=Math.min(cells.get(i).y/25+1,TANK_HEIGHT/25-1); v++) //iterate over the nine squares surrounding the cell, u is the x axis, v is the y
                    for (int j=0; j<cBuckets[u][v].size(); j++) {
                        currentDistance=Math.abs(cells.get(cBuckets[u][v].get(j)).x-cells.get(i).x)+Math.abs(cells.get(cBuckets[u][v].get(j)).y-cells.get(i).y);
                        if (currentDistance<cminD) {
                            if (currentDistance==0&&i!=cBuckets[u][v].get(j)) {  //We have a new winner, but first check for collision
                                //kick the collided cell into a random space
                                cells.get(i).x+=((int)(Math.random()*2)*2-1);
                                cells.get(i).y+=((int)(Math.random()*2)*2-1);
                                cells.get(i).energy--;
                                cells.get(i).x=Math.min(Math.max(cells.get(i).x,0), TANK_WIDTH-1);  //redo the boundary check
                                cells.get(i).y=Math.min(Math.max(cells.get(i).y,0), TANK_HEIGHT-1);
                                currentDistance=Math.abs(cells.get(cBuckets[u][v].get(j)).x-cells.get(i).x)+Math.abs(cells.get(cBuckets[u][v].get(j)).y-cells.get(i).y);  //recalculate the distance
                            }
                            c4minD=c3minD;
                            c3minD=c2minD;
                            c2minD=cminD;
                            cminD=currentDistance;
                            fourthCells[i]=thirdCells[i];
                            thirdCells[i]=nextCells[i];
                            nextCells[i]=closestCells[i];
                            closestCells[i]=cBuckets[u][v].get(j);
                        }
                        else if (currentDistance<c2minD) {
                            c4minD=c3minD;
                            c3minD=c2minD;
                            c2minD=currentDistance;
                            fourthCells[i]=thirdCells[i];
                            thirdCells[i]=nextCells[i];
                            nextCells[i]=cBuckets[u][v].get(j);
                        }
                        else if (currentDistance<c3minD) {
                            c4minD=c3minD;
                            c3minD=currentDistance;
                            fourthCells[i]=thirdCells[i];
                            thirdCells[i]=cBuckets[u][v].get(j);
                        }
                        else if (currentDistance<c4minD) {
                            c4minD=currentDistance;
                            fourthCells[i]=cBuckets[u][v].get(j);
                        }
                    }
            if (c4minD==100) {     //Our 25x25 net wasn't big enough to catch all neighbors
              for (int u=Math.max(0,cells.get(i).x/25-2); u<=Math.min(cells.get(i).x/25+2,TANK_WIDTH/25-1); u++)
                  for (int v=Math.max(0,cells.get(i).y/25-2); v<=Math.min(cells.get(i).y/25+2,TANK_HEIGHT/25-1); v++) {
                                  //^This time we're iterating over a 5x5 surrounding grid, instead of a 3x3. This will certainly catch all interactions
                      if (u<Math.max(0,cells.get(i).x/25-1)&&u>Math.min(cells.get(i).x/25+1,TANK_WIDTH/25-1)&&v<Math.max(0,cells.get(i).y/25-1)&&v>Math.min(cells.get(i).y/25+1,TANK_HEIGHT/25-1))
                                          //^If we've already checked this square, we can skip it
                          for (int j=0; j<cBuckets[u][v].size(); j++) {
                              currentDistance=Math.abs(cells.get(cBuckets[u][v].get(j)).x-cells.get(i).x)+Math.abs(cells.get(cBuckets[u][v].get(j)).y-cells.get(i).y);
                              //No need to check for collision since we're skipping the square that cells[i] actually resides in
                              if (currentDistance<cminD) {
                                  c4minD=c3minD;
                                  c3minD=c2minD;
                                  c2minD=cminD;
                                  cminD=currentDistance;
                                  fourthCells[i]=thirdCells[i];
                                  thirdCells[i]=nextCells[i];
                                  nextCells[i]=closestCells[i];
                                  closestCells[i]=cBuckets[u][v].get(j);
                              }
                              else if (currentDistance<c2minD) {
                                  c4minD=c3minD;
                                  c3minD=c2minD;
                                  c2minD=currentDistance;
                                  fourthCells[i]=thirdCells[i];
                                  thirdCells[i]=nextCells[i];
                                  nextCells[i]=cBuckets[u][v].get(j);
                              }
                              else if (currentDistance<c3minD) {
                                  c4minD=c3minD;
                                  c3minD=currentDistance;
                                  fourthCells[i]=thirdCells[i];
                                  thirdCells[i]=cBuckets[u][v].get(j);
                              }
                              else if (currentDistance<c4minD) {
                                  c4minD=currentDistance;
                                  fourthCells[i]=cBuckets[u][v].get(j);
                              }
                          }
                  }
            }
        }


//Get spatial data for photons

        int closestPhoton;
        int nextPhoton;

        ArrayList<Integer>[][] pBuckets=new ArrayList[TANK_WIDTH/25][TANK_HEIGHT/25];  //create a grid of 25x25 squares
        for (int i=0; i<TANK_WIDTH/25; i++)
            for (int j=0; j<TANK_HEIGHT/25; j++)
                pBuckets[i][j]=new ArrayList(10);
        for (int i=0; i<photons.size(); i++)
            if (photons.get(i).alive) pBuckets[photons.get(i).x/25][photons.get(i).y/25].add(i);   //place the indices to the photons into the buckets


        for (int i=0; i<csize; i++) {  //iterate over all the cells that existed prior to any updates this frame (newborns won't update until next frame)
            closestPhoton=nextPhoton=0;
            int pminD=100;
            int p2minD=100;
            int currentDistance;
            for (int u=Math.max(0,cells.get(i).x/25-1); u<=Math.min(cells.get(i).x/25+1,TANK_WIDTH/25-1); u++)
                for (int v=Math.max(0,cells.get(i).y/25-1); v<=Math.min(cells.get(i).y/25+1,TANK_HEIGHT/25-1); v++) //iterate over the nine squares surrounding the cell, u is the x axis, v is the y
                    for (int j=0; j<pBuckets[u][v].size(); j++) {
                        currentDistance=Math.abs(photons.get(pBuckets[u][v].get(j)).x-cells.get(i).x)+Math.abs(photons.get(pBuckets[u][v].get(j)).y-cells.get(i).y);     //pBuckets gets you the index, which you use to lookup the photon and check distance
                        if (currentDistance<pminD) {
                            p2minD=pminD;
                            pminD=currentDistance;
                            nextPhoton=closestPhoton;
                            closestPhoton=pBuckets[u][v].get(j);
                        }
                        else if (currentDistance<p2minD){
                            p2minD=currentDistance;
                            nextPhoton=pBuckets[u][v].get(j);
                        }
                    }
            if (p2minD==100) //Our 25x25 net wasn't wide enough to catch all neighbors
            for (int u=Math.max(0,cells.get(i).x/25-2); u<=Math.min(cells.get(i).x/25+2,TANK_WIDTH/25-1); u++)
                for (int v=Math.max(0,cells.get(i).y/25-2); v<=Math.min(cells.get(i).y/25+2,TANK_HEIGHT/25-1); v++)
                              //^This time we're iterating over a 5x5 surrounding grid, instead of a 3x3. This will certainly catch all interactions
                    if (u<Math.max(0,cells.get(i).x/25-1)&&u>Math.min(cells.get(i).x/25+1,TANK_WIDTH/25-1)&&v<Math.max(0,cells.get(i).y/25-1)&&v>Math.min(cells.get(i).y/25+1,TANK_HEIGHT/25-1))
                                //^If we've already checked this square, we can skip it
                        for (int j=0; j<pBuckets[u][v].size(); j++) {
                            currentDistance=Math.abs(photons.get(pBuckets[u][v].get(j)).x-cells.get(i).x)+Math.abs(photons.get(pBuckets[u][v].get(j)).y-cells.get(i).y);     //pBuckets gets you the index, which you use to lookup the photon and check distance
                            if (currentDistance<pminD) {
                                p2minD=pminD;
                                pminD=currentDistance;
                                nextPhoton=closestPhoton;
                                closestPhoton=pBuckets[u][v].get(j);
                            }
                            else if (currentDistance<p2minD){
                                p2minD=currentDistance;
                                nextPhoton=pBuckets[u][v].get(j);
                            }
                        }
            cells.get(i).update(closestPhoton, nextPhoton, closestCells[i], nextCells[i],thirdCells[i],fourthCells[i]);  //you now have all the information to update this cell
        }


//clean up and render

        for (int i=photons.size()-1; i>=0; i--) //backwards is required for array culling like this
            if (!photons.get(i).alive)
                photons.remove(i);

        for (int i=csize-1; i>=0; i--) {
            if (!cells.get(i).alive) {
                photons.add(new Photon(cells.get(i).x,cells.get(i).y)); //Other cells can eat its corpse
                cells.remove(i);
            }
        }

        if (display) {
          for (int i=0; i<cells.size(); i++) {
              g2.setColor(Color.getHSBColor((float)cells.get(i).pheno/100,1,.75f));
              g2.fillRect((cells.get(i).x-camX)*zoom, (cells.get(i).y-camY)*zoom, zoom, zoom);
            }
            super.paint(g);
          }

        frames++;
      }
}


class Photon  {
    public int x;
    public int y;
    boolean alive=true;
    public Photon(int nx, int ny) {
        x=nx;
        y=ny;
    }
    public void update() {
        if (Math.random()<.33)
            return;
        if (Math.random()<.5)
            x+=(int)(Math.random()*2)*2-1;
        else
            y+=(int)(Math.random()*2)*2-1;
    }
}


final class Cell {
    int x;
    int y;
    boolean alive=true;
    int commands;
    int energy=25;
    int moves;  //how many actions can be taken this frame, default 1
    int bursts;  //how many times the "burst" action has been taken this frame
    int mutations;  //how many times the cell has mutated since its phenotype last changed
    int age=120;
    int[] memory=new int[20];
    int[] code=new int[CODE_SIZE];
    int pheno;  //A value between 0 and 99 inclusive that determines the cell's color and helps to indicate its ancestry
    int closestPhoton, nextPhoton, closestCell, nextCell, thirdCell, fourthCell;  //indices to the closest photons and cells this frame

    public static final int CODE_SIZE=300;  //the size of the code buffer. The unused portion is filled with blanks

    //Movement functions. Moving sometimes costs energy. Energy cost must still be paid if a collision is detected and the movement fails.
    void moveLeft() {
        if (moves>0&&energy>0) {
            moves--;
            if (checkCollision((x-1),y)) {
                if (Math.random()<.4)
                    energy--;
                return;
            }
            if (Math.random()<.4)
                energy--;
            x--;
        }
    }
    void moveRight() {
        if (moves>0&&energy>0) {
            moves--;
            if (checkCollision((x+1),y)) {
                if (Math.random()<.4)
                    energy--;
                return;
            }
            if (Math.random()<.4)
                energy--;
            x++;
        }
    }
    void moveUp() {
        if (moves>0&&energy>0) {
            moves--;
            if (checkCollision(x,(y-1))) {
                if (Math.random()<.4)
                    energy--;
                return;
            }
            if (Math.random()<.4)
                energy--;
            y--;
        }
    }
    void moveDown() {
        if (moves>0&&energy>0) {
            moves--;
            if (checkCollision(x,(y+1))) {
                if (Math.random()<.4)
                    energy--;
                return;
            }
            if (Math.random()<.4)
                energy--;
            y++;
        }
    }

    //Offspring is created in the first available spot adjacent to the cell. Mutation rate is determined here
    void reproduce() {
        if (energy>47&&moves>0) {
            moves--;
            if (!checkCollision((x+1),y))
              Tank.cells.add(new Cell((x+1),y,pheno,code,Math.random()<0.15,mutations));
            else if (!checkCollision((x-1),y))
              Tank.cells.add(new Cell((x-1),y,pheno,code,Math.random()<0.15,mutations));
            else if (!checkCollision(x,(y+1)))
              Tank.cells.add(new Cell(x,(y+1),pheno,code,Math.random()<0.15,mutations));
            else if (!checkCollision(x,(y-1)))
              Tank.cells.add(new Cell(x,(y-1),pheno,code,Math.random()<0.15,mutations));
            else {
              energy--;
              return;
            }
            energy-=47;
        }
    }

    //This action allows the cell to take an additional action this frame, at the cost of additional energy. May only be used twice per frame.
    void burst() {
        if (bursts<2&&energy>0) {
            bursts++;
            moves++;
            if (Math.random()<.4)
                energy--;
        }
    }

/*Cell Code Key

    -1000000 through 1000000>>literals
        Cells can only compute numbers within this range. Values outside this range represent keywords, operators, "constants", etc.

    -1000001>>If(bool)
    -1000002>>Goto(destination)
    -1000003>>Set a to value(memloc,value)
    -1000004>>Move left
    -1000005>>Move right
    -1000006>>Move up
    -1000007>>Move down
    -1000008>>Reproduce
    -1000009>>Burst
    -1000010>>End if

    -1000020>>Addition(a+b)
    -1000021>>Subtraction(a-b)
    -1000022>>Multiplication(a*b)
    -1000023>>Division(a/b)

    -1000024>>Greater than(a>b)
    -1000025>>Less than(a<b)
    -1000026>>Equals(a==b)

    -1000027>>And(bool&&bool2)
    -1000028>>Or(bool||bool2)

    1000001>>cpx   "closest photon's X position" (relative to the cell)
    1000002>>cpy
    1000003>>ccx   "closest cell's X"
    1000004>>ccy
    1000005>npx    "next photon's X"
    1000006>>npy
    1000007>>ncx
    1000008>>ncy
    1000009>>age
    1000010>>energy
    1000011>>tcx   "third cell's X"
    1000012>>tcy
    1000013>>fcx   "fourth cell's X"
    1000014>>fcy
    1000015>>closest cell pheromone 1 (memlocs 0 and 1 are "pheromones", variables visible to others)
    1000016>>closest cell pheromone 2
    1000017>>next cell pheromone 1
    1000018>>next cell pheromone 2


    1000030 through 1000049>>memory locations 0-19

    -1000040>>true
    -1000041>false
    -1000050>>blank   (appear only to fill up the end of the cell's code buffer)

    -1000101 through -1001100>>goto destinations (passed as arguments)
    -1001101 through -1002100>>goto destinations (as the actual destination flags)



    */

    //Code execution algorithms
    void execute(int start) {
        for (int i=start; i<CODE_SIZE&&code[i]!=-1000050; i++) {
            if (++commands>1000) {  //check for abusive code or infinite loops
                alive=false;  //the punishment for violating this law is death
                return;
            }
            switch (code[i]) {
                case -1000001: //If
                    i++;
                    if (resolveBool(i)) {
                        execute(i);
                        return;
                    }
                    else
                        for (int j=i; j<CODE_SIZE; j++)
                            if (code[j]==-1000010) {
                                execute(j+1);
                                return;
                            }
                    break;
                case -1000002:  //Goto
                    i++;
                    for (int j=0; j<CODE_SIZE; j++)
                        if (code[j]==code[i]-1000) {  //the actual destination flag is 1000 less than the argument, so the two don't get confused
                            execute(j+1);
                            return;
                        }
                    break;
                case -1000003: //Set a to value
                    memory[code[i+1]-1000030]=resolveValue(i+2);
                    i+=2;
                    break;
                case -1000004:
                    moveLeft();
                    break;
                case -1000005:
                    moveRight();
                    break;
                case -1000006:
                    moveUp();
                    break;
                case -1000007:
                    moveDown();
                    break;
                case -1000008:
                    reproduce();
                    break;
                case -1000009:
                    burst();
                    break;
            }
        }
    }
    int resolveValue(int start) {
        int start2=0;    //the starting point for the second argument in operators that take two arguments
        int c=1;    //used to determine the starting point for the second argument
        if (code[start]>=-1000000&&code[start]<=1000000) //check if it's a simple literal
            return code[start];
        else {
            if (code[start]<=-1000020&&code[start]>=-1000023) {
                    //then it's an operator, so go ahead and calculate start2
                for (int i=start+1; i<CODE_SIZE; i++) {
                    if (code[i]>=-1000000)
                        c--;
                    else
                        c++;
                    if (c==0) {
                        start2=i+1;
                        break;
                    }
                }
            }
            switch (code[start]) {
                case -1000020:
                    return (resolveValue(start+1)+resolveValue(start2))%1000001; //Use % to ensure the value is within bounds
                case -1000021:
                    return (resolveValue(start+1)-resolveValue(start2))%1000001;
                case -1000022:
                    return (resolveValue(start+1)*resolveValue(start2))%1000001;
                case -1000023:
                    int check=resolveValue(start2); //check for divide-by-zero
                    if (check==0)
                        return 1000000;
                    else
                        return (resolveValue(start+1)/check);
                case 1000001:
                    return Tank.photons.get(closestPhoton).x-x;
                case 1000002:
                    return Tank.photons.get(closestPhoton).y-y;
                case 1000003:
                    return Tank.cells.get(closestCell).x-x;
                case 1000004:
                    return Tank.cells.get(closestCell).y-y;
                case 1000005:
                    return Tank.photons.get(nextPhoton).x-x;
                case 1000006:
                    return Tank.photons.get(nextPhoton).y-y;
                case 1000007:
                    return Tank.cells.get(nextCell).x-x;
                case 1000008:
                    return Tank.cells.get(nextCell).y-y;
                case 1000009:
                    return age;
                case 1000010:
                    return energy;
                case 1000011:
                    return Tank.cells.get(thirdCell).x-x;
                case 1000012:
                    return Tank.cells.get(thirdCell).y-y;
                case 1000013:
                    return Tank.cells.get(fourthCell).x-x;
                case 1000014:
                    return Tank.cells.get(fourthCell).y-y;
                case 1000015:
                    return Tank.cells.get(closestCell).memory[0];
                case 1000016:
                    return Tank.cells.get(closestCell).memory[1];
                case 1000017:
                    return Tank.cells.get(nextCell).memory[0];
                case 1000018:
                    return Tank.cells.get(nextCell).memory[1];
            }
        return memory[code[start]-1000030];  //if it wasn't one of those, it's a memloc
        }
    }
    boolean resolveBool(int start) {
        int start2=0;    //the starting point for the second argument in operators that take two arguments
        int c=1;    //used to determine the starting point for the second argument
        if (code[start]<-1000030)  //check if it's a binary literal
            return (code[start]==-1000040);  //return true if true
        else {
            if (code[start]<=-1000024&&code[start]>=-1000028) {
                    //then it's an operator, so go ahead and calculate start2
                for (int i=start+1; i<CODE_SIZE; i++) {
                    if (code[i]>=-1000000||code[i]==-1000040||code[i]==-1000041)
                        c--;
                    else
                        c++;
                    if (c==0) {
                        start2=i+1;
                        break;
                    }
                }
            }
            switch (code[start]) {
                case -1000024:
                    return resolveValue(start+1)>resolveValue(start2);
                case -1000025:
                    return resolveValue(start+1)<resolveValue(start2);
                case -1000026:
                    return resolveValue(start+1)==resolveValue(start2);
                case -1000027:
                    return resolveBool(start+1)&&resolveBool(start2);
                case -1000028:
                    return resolveBool(start+1)||resolveBool(start2);
            }
        }
        return false;
    }

    //Mutation algorithms
    int getRandValue() {
        if (Math.random()<.5)
            return (int)(Math.random()*2000001)-1000000; //random value between -1000000 and 1000000
        else
            if (Math.random()<.5)
              return (1000001+(int)(Math.random()*18)); //random constant
            else
              return (1000030+(int)(Math.random()*20)); //random memloc
    }

    boolean changeValue() {
        int valueCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]>=0)
                valueCount++;
        int toChange=(int)(Math.random()*valueCount);
        valueCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]>=0) {
                if (valueCount==toChange) {
                    if (Math.random()<.5||code[CODE_SIZE-2]!=-1000050)
                        code[i]=getRandValue();
                    else {
                        for (int j=CODE_SIZE-3; j>i; j--)
                            code[j+2]=code[j];
                        code[i]=(-1000020-(int)(Math.random()*4)); //+,-,*,/
                        code[i+1]=getRandValue();
                        code[i+2]=getRandValue();
                    }
                    return true;
                }
                else
                    valueCount++;
            }
        return false;

    }

    boolean changeBool() {
        int boolCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]==-1000040||code[i]==-1000041)
                boolCount++;
        int toChange=(int)(Math.random()*boolCount);
        boolCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]==-1000040||code[i]==-1000041) {
                if (boolCount==toChange) {
                    if (Math.random()<.1||code[CODE_SIZE-2]!=-1000050) //true, false
                        code[i]=-(int)(Math.random()*2)-1000040;
                    else {
                        if (Math.random()<.5) { //  insert <,>,=
                            for (int j=CODE_SIZE-3; j>i; j--)
                                code[j+2]=code[j];
                            code[i]=(-1000024-(int)(Math.random()*3));
                            code[i+1]=getRandValue();
                            code[i+2]=getRandValue();
                        }
                        else { // and, or
                          for (int j=CODE_SIZE-3; j>i; j--)
                              code[j+2]=code[j];
                          code[i]=(-1000027-(int)(Math.random()*2));
                          code[i+1]=-(int)(Math.random()*2)-1000040;
                          code[i+2]=-(int)(Math.random()*2)-1000040;
                        }
                    }
                    return true;
                }
                else
                    boolCount++;
            }
        return false;
    }
    boolean deleteValueOperand() {
        int opCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]<=-1000020&&code[i]>=-1000023&&code[i+1]>=-1000000&&code[i+2]>=-1000000)
                opCount++;
        int toChange=(int)(Math.random()*opCount);
        opCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]<=-1000020&&code[i]>=-1000023&&code[i+1]>=-1000000&&code[i+2]>=-1000000) {
                if (opCount==toChange) {
                    for (int j=i; j<CODE_SIZE-2; j++)
                        code[j]=code[j+2];
                    code[CODE_SIZE-1]=-1000050;
                    code[CODE_SIZE-2]=-1000050;
                    code[i]=getRandValue();
                    return true;
                }
                else
                    opCount++;
            }
        return false;
    }
    boolean deleteCompareOperand() {
        int opCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]<=-1000024&&code[i]>=-1000026&&code[i+1]>=-1000000&&code[i+2]>=-1000000)
                opCount++;
        int toChange=(int)(Math.random()*opCount);
        opCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]<=-1000024&&code[i]>=-1000026&&code[i+1]>=-1000000&&code[i+2]>=-1000000) {
                if (opCount==toChange) {
                    for (int j=i; j<CODE_SIZE-2; j++)
                        code[j]=code[j+2];
                    code[CODE_SIZE-1]=-1000050;
                    code[CODE_SIZE-2]=-1000050;
                    code[i]=-(int)(Math.random()*2)-1000040;
                    return true;
                }
                else
                    opCount++;
            }
        return false;
    }
    boolean deleteConjunction() {
        int opCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if ((code[i]==-1000027||code[i]==-1000028)&&code[i+1]<=-1000040&&code[i+2]<=-1000040)
                opCount++;
        int toChange=(int)(Math.random()*opCount);
        opCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if ((code[i]==-1000027||code[i]==-1000028)&&code[i+1]<=-1000040&&code[i+2]<=-1000040) {
                if (opCount==toChange) {
                    for (int j=i; j<CODE_SIZE-2; j++)
                        code[j]=code[j+2];
                    code[CODE_SIZE-1]=-1000050;
                    code[CODE_SIZE-2]=-1000050;
                    code[i]=-(int)(Math.random()*2)-1000040;
                    return true;
                }
                else
                    opCount++;
            }
        return false;
    }
    boolean deleteCommand() {
        int cmdCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]<-1000000&&code[i]>-1000010)
                cmdCount++;
        int toChange=(int)(Math.random()*cmdCount);
        cmdCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]<-1000000&&code[i]>-1000010) {
                if (cmdCount==toChange) {
                    int toCut;
                    switch (code[i]) {
                        case -1:         //if
                            for (toCut=i; code[toCut]!=-1000010; toCut++) {}
                            for (int j=i; j<CODE_SIZE-(toCut-i+1); j++)
                                code[j]=code[j+(toCut-i+1)];
                            for (int j=CODE_SIZE-(toCut-i+1); j<CODE_SIZE; j++)
                                code[j]=-1000050;
                            break;
                        case -2:         //goto
                            int destID=code[i+1]-1000;
                            for (int j=i; j<CODE_SIZE-2; j++)  //delete the goto statement
                                code[j]=code[j+2];
                            code[CODE_SIZE-1]=code[CODE_SIZE-2]=-1000050;
                            code[CODE_SIZE-1]=-1000050;
                            break;
                        case -3:          //set a to value
                            for (toCut=i+1; (code[toCut]<=-1000020&&code[toCut]>=-1000023)||code[toCut]>=-1000000; toCut++) {}
                            for (int j=i; j<CODE_SIZE-(toCut-i); j++)
                                code[j]=code[j+(toCut-i)];
                            for (int j=CODE_SIZE-(toCut-i); j<CODE_SIZE; j++)
                                code[j]=-1000050;
                            break;
                        default:
                            for (int j=i; j<CODE_SIZE-1; j++)
                                code[j]=code[j+1];
                            code[CODE_SIZE-1]=-1000050;
                            break;
                    }
                    return true;
                }
                else
                    cmdCount++;
            }
        return false;
    }
    boolean addCommand() {
        if (code[CODE_SIZE-3]!=-1000050) {
            return false;
        }
        int cmdCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if (code[i]<-1000000&&code[i]>=-1000010)
                cmdCount++;
        int toChange=(int)(Math.random()*(cmdCount+1));
        cmdCount=0;
        for (int i=0; i<CODE_SIZE; i++)
            if ((code[i]<-1000000&&code[i]>=-1000010)||code[i]==-1000050) {
                if (cmdCount==toChange) {
                    int r=(int)(Math.random()*9);
                    switch (r) {
                        case 0:              //if
                            for (int j=CODE_SIZE-4; j>=i; j--)
                                code[j+3]=code[j];
                            code[i]=-1000001;
                            code[i+1]=-(int)(Math.random()*2)-1000040;
                            code[i+2]=-1000010;
                            break;
                        case 1:              //goto
                            int lines=0;
                            int gotos=0;    //start by counting up "lines" so we can pick a random goto destination, and count gotos so we can get a unique destinationID
                            for (int j=0; j<CODE_SIZE; j++) {
                                if (code[j]<-1000000&&code[j]>-1000010&&j!=i)    //Don't count current line
                                    if (code[j]==-1000002)
                                        gotos++;
                                    else
                                        lines++;
                            }
                            if (code[i]!=-1000050)  //if we're NOT inserting at the very end
                                lines++;   //Then allow the very end as an additional destination
                            int dest=(int)(Math.random()*lines);
                            for (int j=CODE_SIZE-3; j>=i; j--)  //Now we'll insert the goto(dest) statement
                                code[j+2]=code[j];
                            code[i]=-1000002;
                            code[i+1]=-1000101-gotos;                 //the actual flag is 1000 less than when specified in the argument. This allows the execution algorithm to know the difference
                            lines=0;
                            for (int j=0; j<CODE_SIZE; j++)
                                if (code[j]==-1000050||(code[j]<-1000000&&code[j]>-1000010&&code[j]!=-1000002&&j!=i+2)) {  //Skip the same line as before, but now that's moved to i+2
                                    if (lines==dest) {
                                        for (int k=CODE_SIZE-2; k>=j; k--)
                                            code[k+1]=code[k];
                                        code[j]=-1001101-gotos;       //the destination flag is based on the current number of goto statements, that way every goto statement always has a unique flag
                                        break;
                                    }
                                    lines++;
                                }
                            break;
                        case 2:        //set a to value
                            for (int j=CODE_SIZE-4; j>=i; j--)
                                code[j+3]=code[j];
                            code[i]=-3;
                            code[i+1]=-1000030-(int)(Math.random()*20);
                            code[i+2]=getRandValue();
                            break;
                        default:
                            for (int j=CODE_SIZE-2; j>=i; j--)
                                code[j+1]=code[j];
                            code[i]=-1000000-(r+1);
                            break;
                    }
                    return true;
                }
                else
                    cmdCount++;
            }
        return false;
    }

    boolean checkCollision(int cx, int cy) {
        if (Tank.cells.get(closestCell).x==cx&&Tank.cells.get(closestCell).y==cy)
            return true;
        if (Tank.cells.get(nextCell).x==cx&&Tank.cells.get(nextCell).y==cy)
            return true;
        if (Tank.cells.get(thirdCell).x==cx&&Tank.cells.get(thirdCell).y==cy)
            return true;
        if (Tank.cells.get(fourthCell).x==cx&&Tank.cells.get(fourthCell).y==cy)
            return true;
        return cx<0||cy<0||cx>=Tank.TANK_WIDTH||cy>=Tank.TANK_HEIGHT;
    }

    //Constructor handles mutations
    public Cell(int nx, int ny, int np, int[] ncode, boolean mutate, int nm) {
        x=nx;
        y=ny;
        pheno=np;
        mutations=nm;
        boolean success;
        for (int i=0; i<CODE_SIZE; i++)
            code[i]=ncode[i];
        if (mutate)
            do {
                int r=(int)(Math.random()*10);
                switch (r) {
                    case 0:
                    case 1:
                        success=changeValue();
                        break;
                    case 2:
                    case 3:
                        success=changeBool();
                        break;
                    case 4:
                        success=deleteValueOperand();
                        break;
                    case 5:
                        success=deleteCompareOperand();
                        break;
                    case 6:
                        success=deleteCommand();
                        break;
                    case 7:
                        success=deleteConjunction();
                        break;
                    default:
                        success=addCommand();
                        break;
                }
                if (success)
                  mutations++;
                if (mutations>10) {
                  mutations=0;
                  pheno=(int)(Math.random()*100);
                }
            } while (!success||Math.random()<.65);
    }
    public void update(int cp, int np, int cc, int nc, int tc, int fc) {
        if (Math.random()<.03)
            energy--;
        if (energy<=0) {
            alive=false;
            return;
        }
        if (Math.random()<.02)
           age--;

        closestPhoton=cp;
        nextPhoton=np;
        closestCell=cc;
        nextCell=nc;
        thirdCell=tc;
        fourthCell=fc;

        moves=1;
        commands=0;
        bursts=0;

        execute(0);

        if (Tank.photons.get(closestPhoton).x==x&&Tank.photons.get(closestPhoton).y==y&&Tank.photons.get(closestPhoton).alive==true) {
            Tank.photons.get(closestPhoton).alive=false;
            energy+=20;
            if (energy>age) energy=age;
        }

    }
}
