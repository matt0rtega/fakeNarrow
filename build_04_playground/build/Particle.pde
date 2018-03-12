class Particle {

  PVector origin;
  PVector location;
  PVector velocity;
  PVector acceleration;

  float topspeed = 10;

  color co;

  float lifespan;
  float radius = random(100, 300);

  int bandSelector;
  float band;

  Particle(float x, float y, float z) {
    origin = new PVector(x, y, z);
    location = new PVector(x, y, z);
    velocity = new PVector(0, 0, 0);
    acceleration = new PVector(0, 0, 0);
    bandSelector = (int)random(myAudioRange);
    lifespan = 255;
  }

  void run(){
    update();
    display();
  }

  void updateSound(){
    band = fftAmp[bandSelector];
  }

  void update() {
    float dist = PVector.dist(location, origin);

    if(dist > radius || dist < -radius) velocity = new PVector(0, 0, 0);

    location.add(velocity);
    velocity.add(acceleration);
    velocity.limit(topspeed);
    acceleration.mult(0);

    lifespan-=1;
  }

  color colorize(PImage tempimg){
    co = tempimg.get((int)location.x + width/2, (int)location.y + height/2);
    return co;
  }

  void display(){
    pushMatrix();
    translate(location.x, location.y, location.z);
    noStroke();
    strokeWeight(1);
    stroke(co, lifespan);
    point(0, 0);
    popMatrix();

  }

  boolean isDead() {
    if (lifespan<0) {
      return true;
    } else {
      return false;
    }
  }
}
