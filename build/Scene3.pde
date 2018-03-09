class Scene3{

  int gridStep = 5;
  int w, h;

  Walker[][] walkers = new Walker[gridStep][gridStep];

  Scene3(){
    w = width/gridStep;
    h = height/gridStep;

    for (int i=0; i<gridStep; i++) {
      for (int j=0; j<gridStep; j++) {
        walkers[i][j] = new Walker(0, w, h, i + j);
      }
    }
  }

  void display(){
    pushMatrix();
    translate(50, 50);
    for (int i=0; i<gridStep; i++) {
      for (int j=0; j<gridStep; j++) {
        pushMatrix();
        translate((i*w), j*h);
        walkers[i][j].vertexLimit = 25;
        walkers[i][j].run();
        walkers[i][j].constrainPoints();
        //walkers[i][j].drawCircles();
        walkers[i][j].connectPoints(img);
        walkers[i][j].display(img);
        walkers[i][j].randomSteps(20);
        walkers[i][j].setStepRange(stepMin, stepMax);
        walkers[i][j].detectBeat(beat.isOnset());
        popMatrix();
      }
    }
    popMatrix();
  }

  void resetScene(){
    for (int i=0; i<gridStep; i++) {
      for (int j=0; j<gridStep; j++) {
        walkers[i][j].resetPoints();
      }
    }
  }
}
