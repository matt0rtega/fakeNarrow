class Scene3{

  int gridStep = 5;
  int w, h;

  Walker[][] walkers = new Walker[gridStep][gridStep];

  Scene3(){
    w = width/gridStep;
    h = height/gridStep;

    for (int i=0; i<gridStep; i++) {
      for (int j=0; j<gridStep; j++) {
        walkers[i][j] = new Walker(0, i + j);
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
        walkers[i][j].vertexLimit = 50;
        walkers[i][j].run();
        walkers[i][j].constrainPoints(w, h);
        walkers[i][j].display(img);
        walkers[i][j].randomSteps();
        walkers[i][j].setStepRange(stepMin, stepMax);
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
