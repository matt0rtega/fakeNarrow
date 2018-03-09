class Scene2{

  Walker[] walkers = new Walker[5];

  Scene2(){
    for (int i=0; i<5; i++) {
      walkers[i] = new Walker(0, width, height, i);
    }
  }

  void display(){
    pushMatrix();
    translate(width/2, height/2);
    for (int i=0; i<walkers.length; i++) {
      walkers[i].run();
      walkers[i].randomSteps(50);
      //walkers[i].drawCircles();
      walkers[i].display(img);
      walkers[i].connectPoints(img);
      walkers[i].detectBeat(beat.isOnset());
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
