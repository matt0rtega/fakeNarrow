class Scene {

  float rot = 0;
  float targetrot = 0;

  float rotz = 0;
  float targetrotz = 0;

  float sceneband;

  float currentTime;
  float lastTime;
  float duration;

  Scene(){
    targetrot = random(TWO_PI);
    rot = targetrot;
  }

  void runTimer(){
    currentTime = millis();
    getTimeEllapsed();
  }

  void setFade(){
    fade.set("fadeLevel", map(mouseX, 0, width, 0.000, 0.500));
  }

  void setFade(float fadeLevel){
    fade.set("fadeLevel", fadeLevel);
  }

  void updateSound(){
    sceneband = fftAmp[20];
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
    }

    // Ease to the new transformations
    rot = lerp(rot, targetrot, 0.002);
    rotz = lerp(rotz, targetrotz, 0.002);

    rotateY(rot);
    rotateZ(rotz);
  }

  void updateLastTime(){
    lastTime = currentTime;
  }

  void getTimeEllapsed(){
    duration = currentTime - lastTime;
  }
}
