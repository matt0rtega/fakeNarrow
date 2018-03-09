class Scene1{

  Walker walker;

  float rot = 0;
  float targetrot = 0;

  float rotz = 0;
  float targetrotz = 0;

  Scene1(){
    walker = new Walker(0, width, height, 0);
    targetrot = random(TWO_PI);
    rot = targetrot;
  }

  void run(){
    rotateScene();
    display();
  }

  void display(){

    pushMatrix();
    translate(width/2, height/2);
    rotateY(rot);
    rotateZ(rotz);
    displayRects();
    walker.run();
    walker.randomSteps(50);
    walker.detectBeat(beat.isOnset());
    walker.display(img);
    walker.setStepRange(stepMin, stepMax);
    popMatrix();


  }

  void resetScene(){
    walker.resetPoints();
  }

  void displayRects(){
    for (int i= rects.size()-1; i>= 0; i--){
      SoundRect r = rects.get(i);
      r.display();
      if (r.isDead()) {
        rects.remove(i);
      }
    }
  }

  void rotateScene(){

    // Every beat, take a rotation in a random direction
    if ( beat.isOnset() ) {
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
}