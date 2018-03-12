class CircleSystem {

  ArrayList<SoundCircle> circles;

  float size = 0;
  float targetsize = 0;
  float[] easing = new float[100];
  int bandSelector;

  CircleSystem(){
    circles = new ArrayList<SoundCircle>();

    for(int i=0; i<easing.length; i++){
      easing[i] = random(0.001, 0.05);
    }

    bandSelector = (int)random(0, myAudioRange);
  }

  void display(ArrayList<PVector> points){
    float band = fftAmp[bandSelector];

    float offsetz = map(band, 0, myAudioMax, -100, 100);

    int pointSelector = (int)map(band, 0, myAudioMax, 1, points.size()-1);


    if(points != null) {
      PVector p = points.get(pointSelector);
      if (beat.isOnset() && circles.size() < 25) circles.add(new SoundCircle(p.x, p.y, p.z + band, band * 2));
    }

    for (int i= circles.size()-1; i>= 0; i--){
      SoundCircle c = circles.get(i);
      c.run();
      if (c.isDead()) {
        circles.remove(i);
      }
    }

    selectRandomBand();
  }

  void selectRandomBand(){
    if(time % 100 == 0){
      bandSelector = (int)random(0, myAudioRange);
    }
  }
}
