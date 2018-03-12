class SoundCircle extends Particle {

  int bandSelector;
  float size = 10;

  color co;

  int mode = 0;

  SoundCircle(float x, float y, float z, float size){
    super(x, y, z);

    bandSelector = (int)random(0, myAudioRange);

    this.size = size;

    co = colorize(img);

    if(random(1) < 0.5){
      mode = 1;
    } else {
      mode = 0;
    }
  }

  void run(){
    display();
    update();
  }

  void display(){
    float band = fftAmp[bandSelector];

    float offsetz = map(band, 0, myAudioMax, -200, 200);

    pushMatrix();
    translate(location.x, location.y, location.z + offsetz);
    if(mode == 0){
      noStroke();
      fill(co, lifespan);
    } else {
      noFill();
      stroke(co, lifespan);
    }
    ellipse(0, 0, size, size);
    popMatrix();
  }
}
