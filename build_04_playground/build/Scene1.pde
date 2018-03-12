class Scene1 extends Scene{

  Walker walker;
  Walker walker2;

  CircleSystem circles;
  CircleSystem circles2;

  Scene1(){
    walker = new Walker(0, 0);
    walker2 = new Walker(0, 0);

    circles = new CircleSystem();
    circles2 = new CircleSystem();
  }

  void run(){
    //rotateScene();
    setFade();
    display();
  }

  void display(){

    pushMatrix();
    translate(width/2, height/2);

    displayCircles();
    displayWalkers();

    popMatrix();
  }

  void displayCircles(){
    circles.display(walker.points);
    circles2.display(walker2.points);
  }

  void displayWalkers(){
    //Walker 1
    walker.update();
    walker.randomSteps();
    walker.constrainPoints(width/2, height/2);
    walker.connectPoints();
    walker.display(img);
    walker.setStepRange(stepMin, stepMax);

    //Walker 2
    walker2.update();
    walker2.randomSteps();
    walker2.constrainPoints(width/2, height/2);
    walker2.display(img);
    walker2.setStepRange(stepMin, stepMax);

  }

  void resetScene(){
    walker.resetPoints();
    walker2.resetPoints();
  }

}
