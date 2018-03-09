class Walker {

  ArrayList<PVector> points;
  PVector target;
  PVector position;

  float rot = 0;
  float targetrot = 0;

  int dir = 1;
  float step = 5;

  float rotz = 0;
  float targetrotz = 0;

  float scale = 0;
  float targetscale = 0;

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

    targetrot = random(TWO_PI);
    rot = targetrot;
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

  void colorize(){

  }

  void constrainPoints(){
    target.x = constrain(target.x, -w, w);
    target.y = constrain(target.y, -h, h);
    target.z= constrain(target.z, -h, h);
  }

  void update(){

    // Change step size basd on the volume
    step = map(fft.getBand(100), 0, 5, 1, 100);

    if(time % 5 == 0 && points.size() < (int)map(mouseY, 0, height, 10, 500)){
      addPoint(target.x, target.y, target.z);
    }

  }

  void detectBeat(boolean beat){
    this.beat = beat;
  }

  private void rotateTransform(){
    // Transform object
    rotateY(rot);
    rotateX(rotz);
    //rotateZ(mouseX * 0.02);
  }

  void slowRotate(float speed){
    rotateZ(time * 0.2 * speed * index);
  }

  void display(PImage tempimg){
    pushMatrix();

    rotateTransform();
    slowRotate(0.001);

    circle.display(points);

    // for(SoundRect r : rects){
    //   r.display();
    // }

    for (int i= rects.size()-1; i>= 0; i--){
      SoundRect r = rects.get(i);
      r.display();
      if (r.isDead()) {
        rects.remove(i);
      }
    }

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

  void randomSteps(float range){

    // Set probability parameter
    float rand = random(1);
    float rand2 = map(step, 0, 20, 0.0, 1.0);

    float band = fftAmp[bandSelector];

    float randomStep = map(band, 0, myAudioMax, 5, 200);

    // Every n frames, take a step in a random direction
    if (time % 5 == 0)  {
      if(rand < rand2){
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

  void circleStep(){
    float t = random(TWO_PI);
    t = time;
    float rand = random(1);
    float rand2 = map(step, 0, 20, 0.0, 1.0);
    float circleStep = random(10, 100);

    float x = map(sin(t * 0.01), -1, 1, -width/2, width/2);
    float y = map(cos(t * 0.02), -1, 1, -height/2, height/2);

    if (time % 5 == 0)  {
      if(rand < rand2){
        //target.x = circleStep * sin(t );
        position.x = x;
        //println("Trigger x");

      }

      if(rand > 0.3 && rand < 0.6){
        //target.y = circleStep * cos(t);
        position.y = y;
        //println("Trigger y");
      }

      if(rand > 0.6){
        target.z = circleStep * sin(t);
        //println("Trigger z");
      }
    }
  }

  void rotateScene(){

    // Every beat, take a rotation in a random direction
    if ( beat ) {
      if (random(1) < 0.3){
        //println("Rotated to:", targetrot);
        targetrot = random(-PI, PI);
      }

      if (random(1) < 0.6 || random(1) > 0.3){
        //println("Rotated to:", targetrot);
        targetrotz = random(-PI, PI);
      }
      //targetrot = random(-PI, PI);
    }

    // Ease to the new transformations
    rot = lerp(rot, targetrot, 0.002);
    rotz = lerp(rotz, targetrotz, 0.002);
  }

  void randomRotate(){
    targetrot = random(-PI, PI);
    rot = targetrot;
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
    points.add(new PVector(x, y, z));
  }

  void drawSphere(){
    // Debug point
    noStroke();
    fill(255, 0, 0);
    sphere(10);
  }

  void drawCircles(){
    pushMatrix();
    rotateTransform();

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

    rotateTransform();
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
