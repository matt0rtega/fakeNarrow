class Walker {

  ArrayList<PVector> points;
  PVector target;
  PVector position;

  int dir = 1;
  float stepMin = 5;
  float stepMax = 200;

  float scale = 0;
  float targetscale = 0;

  int vertexLimit = 500;

  boolean beat;

  // 0 - DEFAULT, 1 - POINTS, 2 - QUADS_STRIP
  int mode;
  int index;

  // Width and height for constraints
  int w, h;

  color co;

  SoundCircle circle;

  int bandSelector;

  Walker(int mode, int w, int h, int index){
    points = new ArrayList<PVector>();
    target = new PVector(0, 0, 0);
    position = new PVector(0, 0);
    points.add(new PVector(0, 0, 0));

    targetscale = 1;

    circle = new SoundCircle();

    bandSelector = (int)random(0, myAudioRange);

    co = 255;

    this.mode = mode;
    this.index = index;
    this.w = w;
    this.h = h;
  }

  void run(){
    update();
    //display();
    //randomSteps();
    //circleStep();
    //rotateScene();
    //randomRotate();
    //connectPoints();
  }

  void constrainPoints(){
    target.x = constrain(target.x, -w, w);
    target.y = constrain(target.y, -h, h);
    target.z= constrain(target.z, -h, h);
  }

  void update(){

    float randAdd = (int)map(fftAmp[10], 0, myAudioMax, 5, 1);

    if(time % randAdd == 0 && points.size() < vertexLimit){
      addPoint(target.x, target.y, target.z);
    }

  }

  void detectBeat(boolean beat){
    this.beat = beat;
  }

  void display(PImage tempimg){
    pushMatrix();

    circle.display(points);

    noFill();

    if (mode == 0) beginShape();
    if (mode == 1) beginShape(POINTS);
    //noFill();
    if (mode == 2) {beginShape(QUAD_STRIP); fill(255, 25);}


    int i=0;
    for(PVector p : points){

      float band = fftAmp[i % myAudioRange];

      co = tempimg.get((int)p.x + width/2, (int)p.y + height/2);
      if (brightness(co) < 100){
        co = 255;
      }

      stroke(co);
      strokeWeight(0.5);

      if (mode == 2) {fill(co); noStroke();}

      float offsetx = 0;
      float offsety = 0;
      float offsetz = 0;

      offsetx = map(noise(band * 0.01 + (i * 0.0001)), 0, 1, -band, band);
      offsety = map(noise(band * 0.02 + (i * 0.002)), 0, 1, -band, band);

      if(mode == 0){
        curveVertex(p.x + offsetx, p.y + offsety, p.z);
      } else {
        curveVertex(p.x + offsetx, p.y + offsety, p.z);
      }

      i++;
    }
    endShape();

    popMatrix();
  }

  void setStepRange(float min, float max){
    stepMin = min;
    stepMax = max;
  }

  void randomSteps(float range){

    // Set probability parameter
    float rand = random(1);

    float band = fftAmp[bandSelector];

    float randomStep = map(band, 0, myAudioMax, stepMin, stepMax);

    // Every n frames, take a step in a random direction
    if (time % 5 == 0)  {
      if(rand < 0.3){
        changeDirection();
        target.x += randomStep * dir;
        //println("Trigger x");
      }

      if(rand > 0.3 && rand < 0.6){
        changeDirection();
        target.y += randomStep * dir;
        //println("Trigger y");
      }

      if(rand > 0.6){
        changeDirection();
        target.z += randomStep * dir;
        //prinln("Trigger z");
      }
    }
  }

  void resetPoints(){
    // Move all points and start over
    for (int i = points.size()-1; i >= 0; i--) {
      PVector p = points.get(i);
        points.remove(i);
    }

    PVector start = new PVector(random(-100, 100), random(-100, 100), random(-100, 100));

    target = new PVector(start.x, start.y, start.z);
    position = new PVector(start.x, start.y, start.z);
    points.add(new PVector(start.x, start.y, start.z));
  }

  private void changeDirection(){
    // Change of changing direction
    if(random(1) < 0.5){
      dir = 1;
    } else {
      dir = -1;
    }
  }

  void addPoint(float x, float y, float z){
    if(points.size() < 100){
      points.add(new PVector(x, y, z));
    }
  }

  void drawCircles(){
    pushMatrix();

    // Every n points add a circle
    int j=0;
    for (PVector p : points){
      if(j % 10 == 0){

        float size = map(fft.getAvg(j % myAudioRange), 0, myAudioMax, 0, 500);

        pushMatrix();
        noFill();
        stroke(255, 0, 0);
        strokeWeight(0.7);
        translate(p.x, p.y, p.z);
        ellipse(0, 0, size, size);
        popMatrix();
      }
      j++;
    }

    popMatrix();
  }

  void connectPoints(PImage tempimg){

    pushMatrix();

    float range = map(mouseY, 0, width, 0, 25);

    int j=0;
    for (PVector p : points){
      float band = fft.getBand(j*10 % 2048) * 5;
      co = tempimg.get((int)p.x + width/2, (int)p.y + height/2);
      stroke(co );
      float offset = map(noise(band + (j * 0.1)), 0, 1, -band, band);
      for (PVector other : points){
        if(p != other){

          // Need to add offset
          float dist = PVector.dist(p, other);

          if(dist < range){
            line(p.x, p.y, p.z, other.x, other.y, other.z);
          }
        }
      }
      j++;
    }

    popMatrix();
  }

}