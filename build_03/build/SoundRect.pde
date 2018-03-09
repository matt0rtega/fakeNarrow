class SoundRect {

  PVector location;

  float w, h;
  float offset;

  float lifespan;

  int bandSelector;

  SoundRect(float x, float y, float z, float offset){
    location = new PVector(x, y, z);

    w = random(25);
    h = random(-25, 25);

    this.offset = offset;

    bandSelector = (int)random(myAudioRange);

    lifespan = 255;
  }

  void display(){
    pushMatrix();
    translate(location.x, location.y, location.z);
    noStroke();
    fill(255, lifespan);
    rect(0, 0, fftAmp[bandSelector], h * offset/2 - fftAmp[5]);
    popMatrix();

    lifespan-=0.5;
  }

  boolean isDead(){
          if(lifespan<0){
           return true;
          } else {
           return false;
          }
         }

}
