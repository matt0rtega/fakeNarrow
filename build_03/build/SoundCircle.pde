class SoundCircle {

  float size = 0;
  float targetsize = 0;
  float[] easing = new float[100];
  int bandSelector;

  SoundCircle(){
    for(int i=0; i<easing.length; i++){
      easing[i] = random(0.001, 0.05);
    }

    bandSelector = (int)random(0, myAudioRange);
  }

  void display(ArrayList<PVector> points){
    pushMatrix();
    float band = fftAmp[bandSelector];

    // Every n points add a circle
    int j=0;
    for (PVector p : points){
      if(j % 20 == 0){

        //float band = fftAmp[bandSelector];

        if(band > 50.0){
          size = map(band, 0, myAudioMax, 0, 50);
        }

        size = lerp(size, 10, easing[j % easing.length]);



        for(int i=0; i<3; i++){
          float offset = (i * band * 0.5);

          if(offset > 5 && rects.size() < 20) {
            rects.add(new SoundRect(p.x, p.y, p.z, offset));
          }

          pushMatrix();
          noFill();
          stroke(255, 0, 0);
          strokeWeight(0.7);
          translate(p.x, p.y, p.z + offset);
          ellipse(0, 0, size / i, size / i);
          popMatrix();
        }
      }
      j++;
    }
    popMatrix();

    selectRandomBand();
  }

  void selectRandomBand(){
    if(time % 100 == 0){
      bandSelector = (int)random(0, myAudioRange);
    }
  }
}
