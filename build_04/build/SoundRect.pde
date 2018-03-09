class SoundRect {

  PVector location;
  PVector velocity;

  float w, h;
  float offset;

  float lifespan;

  int bandSelector;

  SoundRect(float x, float y, float z, float offset){
    location = new PVector(x, y, z);
    velocity = new PVector(0, random(-1, 1));

    w = random(25);
    h = random(-25, 25);

    this.offset = offset;

    bandSelector = (int)random(myAudioRange);

    lifespan = 255;
  }

  void display(){

    float band = fftAmp[bandSelector];
    float offsetz = map(band, 0, myAudioMax, -100, 100);

    location.add(velocity);
    pushMatrix();
    translate(location.x, location.y, location.z + offsetz);
    noStroke();
    //fill(255, lifespan);
    strokeWeight(1);
    stroke(255, lifespan);
    point(0, 0);
    popMatrix();

    lifespan-=1;
  }

  boolean isDead(){
          if(lifespan<0){
           return true;
          } else {
           return false;
          }
         }

}
