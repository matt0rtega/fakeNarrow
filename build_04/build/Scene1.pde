class Scene1{

  Walker walker;
  
  ArrayList<HorizontalRect> hrects;

  float rot = 0;
  float targetrot = 0;

  float rotz = 0;
  float targetrotz = 0;
  
  float band;

  Scene1(){
    walker = new Walker(2, width, height, 0);
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
    
    band = fftAmp[4];

    pushMatrix();
    translate(width/2, height/2);
    //rotateY(rot);
    //rotateZ(rotz);
    //displayRects();
    
    //displayParticles();
    //walker.run();
    //walker.randomSteps(50);
    //walker.detectBeat(beat.isOnset());
    //walker.display(img);
    //walker.setStepRange(stepMin, stepMax);
    popMatrix();
    
    pushMatrix();
    translate(width/2, 0);
    //if(band > 50) createRects();
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
    //int rectSelector = (int)random(fftAmp.length);
    
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
  
  void displayParticles(){
    for (int i= particles.size()-1; i>= 0; i--){
      Particle p = particles.get(i);
      p.update();
      p.display();
      if (p.isDead() || particles.size() > 1000) {
        particles.remove(i);
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