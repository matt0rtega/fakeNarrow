class HorizontalRect extends Particle{

  float size = 0;
  float targetSize = random(100);

  HorizontalRect(float x, float y, float z){
    super(x, y, z);

    co = color(random(100, 255));
  }

  void display(){
    size = lerp(size, targetSize, 0.05);

    pushMatrix();
    rectMode(CENTER);
    translate(location.x, location.y, location.z);
    if(size > targetSize - 10 && size > 100) { ellipse(size, 0, 20, 20); ellipse(-size, 0, 20, 20);}
    fill(co);
    noStroke();
    rect(0, 0, size, 10);
    popMatrix();
  }

  void reset(){
    size = 0;
  }

  void setTargetSize(float tempsize){
    targetSize = random(20, width);
  }
}
