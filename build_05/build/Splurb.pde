class Splurb extends Particle{

  float noiseoff = random(10000);

  float size = 0;
  float targetSize = 10;

  PVector target;
  PVector prevloc;

  float offx, offy;

  int mode;

  color co = color(255);

  float dir;
  float theta = 0;
  float inc = random(0.005, 0.02);
  float range;

  float blobxrange = random(25);
  float blobyrange = random(25);

  Splurb(float x, float y, float z){
    super(x, y, z);

    target = new PVector(random(-width/2, width/2), random(-width/2/ width/2));
    prevloc = location.copy();

    range = random(25, 300);

    if(random(1) < 0.5) {
      dir = -1.0;
    } else {
      dir = 1.0;
    }


    float rand = random(1);

    if(rand < 0.33){
      mode = 0;
    } else if(rand > 0.33 && rand < 0.66){
      mode = 1;
    } else {
      mode = 2;
    }

    co = colorize(img);
  }

  void update(){

    // Needs to happen before update
    setSize();

    prevloc = location.copy();

    updateSound();
    if(mode != 2) move();

    lifespan -= 0.5;
  }

  void setTarget(){

    PVector dir = PVector.sub(target, location);
    dir.normalize();
    dir.mult(0.1);

    acceleration = dir;

    float d = PVector.dist(target, location);
    if(d < 200) target = new PVector(random(width), random(height));
  }

  void setSize(){
    float d = PVector.dist(prevloc, location);
    size = map(sin(time * 0.04 + noiseoff), -1, 1, 0, d * 10 + band);

    if(mode == 0){
      if(beat.isOnset()) targetSize = map(band, 0, myAudioMax, 2, 20);
    } else {
      if(beat.isOnset()) targetSize = map(band, 0, myAudioMax, 1, 25);
    }

    size = lerp(size, targetSize, 0.01);

    targetSize *= 0.85;
  }

  void display(){

    if(mode == 0) circleJitter();
    if(mode == 1) { offx = 0; offy = 0;}
    if(mode == 2) moveCircle();

    pushMatrix();
    translate(location.x + offx, location.y + offy);
    fill(co, lifespan);
    noStroke();
    blob();
    ellipse(0, 0, size, size);
    popMatrix();
  }

  void moveCircle(){
    float inc = map(band, 0, myAudioMax, 0.001, 0.100);

    offx = map(sin(theta + noiseoff), -1, 1, -range, range);
    offy = map(cos(theta + noiseoff), -1, 1, -range, range);

    theta += inc * dir;
  }

  void blob(){
    int numSteps = 10;
    float inc = TWO_PI / numSteps;

    float range = 50;
    beginShape();
    for(int i=0; i<=numSteps+2; i++){
      float x = map(sin(inc * i), -1, 1, -size, size);
      float y = map(cos(inc * i), -1, 1, -size, size);

      float xoff = map(noise(x * 0.02, y * 0.02, frameCount * 0.001 + noiseoff), 0, 1, -blobxrange, blobxrange);
      float yoff = map(noise(x * 0.02, y * 0.02, frameCount * 0.005 + noiseoff), 0, 1, -blobyrange, blobyrange);

      curveVertex(x + xoff, y + yoff);
    }
    endShape();
  }

  void circleJitter(){
    float piband = map(band, 0, myAudioMax, 0, TWO_PI);
    offx = map(sin(piband), -1, 1, -100, 100);
    offy = map(cos(piband), -1, 1, -100, 100);
  }

  void move(){
    float speed = 0.2;
    float limit = map(band, 0, myAudioMax, 1, 3);

    float accx = map(noise(time * 0.02, noiseoff), 0,1, -speed, speed);
    float accy = map(noise(time * 0.05, noiseoff), 0,1, -speed, speed);
    acceleration = new PVector(accx, accy);

    location.add(velocity);
    velocity.add(acceleration);
    velocity.limit(limit);
    acceleration.mult(0);
  }

}
