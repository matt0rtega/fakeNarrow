class Scene5 extends Scene{

  Walker walker;

  ArrayList<HorizontalRect> hrects;

  float rot = 0;
  float targetrot = 0;

  float rotz = 0;
  float targetrotz = 0;

  float sceneband;

  Scene5(){
    walker = new Walker(2, 0);
    hrects = new ArrayList<HorizontalRect>();

    targetrot = random(TWO_PI);
    rot = targetrot;

    createRects();
  }

  void run(){
    rotateScene();
    display();
  }

  void display(){

    sceneband = fftAmp[20];

    pushMatrix();
    translate(width/2, height/2);
    rotateY(rot);
    rotateZ(rotz);
    walker.run();
    walker.randomSteps();
    walker.display(img);
    walker.setStepRange(stepMin, stepMax);
    popMatrix();

    pushMatrix();
    translate(width/2, 0);
    displayHRects();
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

  void createRects(){
    float row = height/fftAmp.length;
    for(int i=0; i < fftAmp.length; i++){
      hrects.add(new HorizontalRect(0, i * row, 0));
    }
  }

  void displayHRects(){
    for (int i= hrects.size()-1; i>= 0; i--){
      HorizontalRect h = hrects.get(i);
      h.update();
      h.display();
    }

    for(int i=0; i<fftAmp.length; i++){
        HorizontalRect h = hrects.get(i);
        float fftband = fftAmp[i];

        if(h.size > h.targetSize - 10 || fftband > 50){
          h.setTargetSize(fftband);
          h.reset();
        }
      }
  }

}
