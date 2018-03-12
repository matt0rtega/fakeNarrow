class Walker {

  ArrayList<PVector> points;
  ArrayList<Float> offsetx;
  ArrayList<Float> offsety;

  PVector location;

  int dir = 1;
  float stepMin = 5;
  float stepMax = 200;

  int vertexLimit = 500;

  // 0 - DEFAULT, 1 - POINTS, 2 - QUADS_STRIP
  int mode;
  int index;

  // Width and height for constraints
  int w, h;

  color co;

  int bandSelector;

  Walker(int mode, int index){
    points = new ArrayList<PVector>();
    offsetx = new ArrayList<Float>();
    offsety = new ArrayList<Float>();

    location = new PVector(0, 0, 0);

    points.add(new PVector(0, 0, 0));
    offsetx.add(0.0);
    offsety.add(0.0);

    bandSelector = (int)random(0, myAudioRange);

    co = 255;

    this.mode = mode;
    this.index = index;
  }

  void run(){
    update();
  }

  void constrainPoints(float w, float h){
    location.x = constrain(location.x, -w, w);
    location.y = constrain(location.y, -h, h);
    location.z= constrain(location.z, -h, h);
  }

  void update(){

    float randAdd = (int)map(fftAmp[10], 0, myAudioMax, 5, 1);

    if(time % randAdd == 0 && points.size() < vertexLimit){
      addPoint(location.x, location.y, location.z);
    }
  }

  void display(PImage tempimg){
    pushMatrix();

    noFill();

    if (mode == 0) beginShape();
    if (mode == 1) beginShape(POINTS);
    if (mode == 2) {beginShape(QUAD_STRIP); fill(255, 25);}


    int i=0;
    for(PVector p : points){

      float band = fftAmp[i % myAudioRange];

      co = tempimg.get((int)p.x + width/2, (int)p.y + height/2);

      stroke(co);
      strokeWeight(1);

      if (mode == 2) {fill(co); noStroke();}

      float range = 100;
      offsetx.set(i, map(noise(band * 0.01 + time * 0.01 + (i * 0.002)), 0, 1, -band, band));
      offsety.set(i, map(noise(band * 0.06 + time * 0.06 + (i * 0.006)), 0, 1, -band, band));

      float offx = offsetx.get(i);
      float offy = offsety.get(i);

      //p.x = p.x + offsetx;

      if(mode == 0){
        curveVertex(p.x + offx, p.y + offy, p.z);
      } else {
        vertex(p.x, p.y , p.z);
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

  void randomSteps(){

    // Set probability parameter
    float rand = random(1);

    float band = fftAmp[bandSelector];

    float randomStep = map(band, 0, myAudioMax, stepMin, stepMax);

    // Every n frames, take a step in a random direction
    if (time % 5 == 0)  {
      if(rand < 0.3){
        changeDirection();
        location.x += randomStep * dir;
        //println("Trigger x");
      }

      if(rand > 0.3 && rand < 0.6){
        changeDirection();
        location.y += randomStep * dir;
        //println("Trigger y");
      }

      if(rand > 0.6){
        changeDirection();
        location.z += randomStep * dir;
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

    location = new PVector(start.x, start.y, start.z);
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
    offsetx.add(0.0);
    offsety.add(0.0);
  }

  void connectPoints(){
    float band = fftAmp[bandSelector];

    float range = map(mouseY, 0, width, 0, band);

      int i=0;
      for(PVector p : points){
        int j=0;
        for (PVector other : points){
          if(p != other){

            float offx = offsetx.get(i);
            float offy = offsety.get(i);

            float otheroffx = offsetx.get(j);
            float otheroffy = offsety.get(j);
            // Need to add offset
            float dist = PVector.dist(p, other);

            stroke(255);

            if(dist < range){
              line(p.x + offx, p.y + offy, p.z, other.x + otheroffx, other.y + otheroffy, other.z);
            }
          }

        j++;
        }
        i++;
      }

    }



}
