class Scene2 extends Scene{

  Walker[] walkers = new Walker[5];

  Scene2(){
    for (int i=0; i<5; i++) {
      walkers[i] = new Walker(0, i);
    }
  }

  void display(){
    pushMatrix();
    translate(width/2, height/2);
    rotateScene();
    for (int i=0; i<walkers.length; i++) {
      walkers[i].run();
      walkers[i].randomSteps();
      walkers[i].display(img);
      walkers[i].setStepRange(stepMin, stepMax);
    }
    popMatrix();
  }

  void resetScene(){
    for(Walker w : walkers){
      w.resetPoints();
    }
  }
}
