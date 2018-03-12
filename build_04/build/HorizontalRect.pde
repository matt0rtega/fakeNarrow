class HorizontalRect extends Particle{
  
  float size = random(100);
  float targetSize = size * 20;
  
  HorizontalRect(float x, float y, float z){
    super(x, y, z);
    
    co = color(random(100, 255));
  }
  
  void display(){
    size = lerp(size, targetSize, 0.05);
    
    pushMatrix();
    rectMode(CENTER);
    translate(location.x, location.y, location.z);
    fill(255, 0, 0);
    if(size > targetSize - 10 && size > 100) { ellipse(size, 0, 20, 20); ellipse(-size, 0, 20, 20);}
    fill(co);
    
    noStroke();
    rect(0, 0, size, 10);
    popMatrix();
    
    //lifespan -= 10;
  }
  
  void reset(){
    size = 0;
    //targetSize = random(0, width);
  }
  
  void setTargetSize(float tempsize){
    targetSize = random(20, tempsize * 10);
  }
}