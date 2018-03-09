// *************************************************************************************************************

import ddf.minim.*;
import ddf.minim.analysis.*;

Minim minim;
AudioPlayer song;
AudioInput in;
BeatDetect beat;
FFT fft;

int myAudioRange = 50;
int myAudioMax = 100;

float myAudioAmp = 75.0;
float myAudioIndex = 0.05;
float myAudioIndexAmp = myAudioIndex;
float myAudioIndexStep = 0.25;

float[] fftAmp = new float[myAudioRange];

// *************************************************************************************************************

int stageMargin = 100;
int myStageW = 800;
int myStageH = 600;
float spacing = (myStageW - (stageMargin * 2)) / myAudioRange;
int rectSize = (int)spacing;
color clrBG = #333333;

float xStart = stageMargin;
float yStart = stageMargin;
int xSpacing = rectSize;

// *************************************************************************************************************

ArrayList<SoundRect> rects;

Walker walker;
Walker walker2;

int gridStep = 10;
int w, h;

Walker[][] walkersGrid = new Walker[gridStep][gridStep];
Walker[] walkers = new Walker[5];

PImage img;

int time;

PShader fade;

/*
TODO :
 - Proper sound mapping
 -
 */

void settings(){
  size(myStageW, myStageH, P3D);
  //fullScreen(P3D);

}

void setup() {
noCursor();
  img = loadImage("img2.jpg");
  img.resize(width, height);

  minim = new Minim(this);
  // a beat detection object song SOUND_ENERGY mode with a sensitivity of 10 milliseconds
  beat = new BeatDetect();
  // use the getLineIn method of the Minim object to get an AudioInput
  in = minim.getLineIn();

  // missing upper bands
  song = minim.loadFile("clav-16.wav");
  song.play();
  song.loop();

  fft = new FFT( song.bufferSize(), song.sampleRate() );
  fft.linAverages(myAudioRange);
  fft.window(FFT.HAMMING);

  walker = new Walker(0, width, height, 0);
  walker2 = new Walker(0, width, height, 0);

  rects = new ArrayList<SoundRect>();

  w = width/gridStep;
  h = height/gridStep;

  for (int i=0; i<gridStep; i++) {
    for (int j=0; j<gridStep; j++) {
      walkersGrid[i][j] = new Walker(0, w, h, i + j);
    }
  }

  for (int i=0; i<5; i++) {
    walkers[i] = new Walker(0, width, height, i);
  }

  fade = loadShader("fade.glsl");
}

void draw() {

  //println(rects.size());

  fade.set("fadeLevel", map(mouseX, 0, width, 0.000, 0.500));
  filter(fade);

  time = int(millis() * 0.0833);

  fft.forward(song.mix);
  beat.detect(song.mix);

  hint(ENABLE_DEPTH_TEST);

  //background(255);

  soundDebug();

  hint(DISABLE_DEPTH_TEST);

  pushMatrix();
  translate(width/2, height/2);
  //walker.run();
  //walker.randomSteps();
  //walker.detectBeat(beat.isOnset());

  //scene1();


  popMatrix();

  scene2();
  //scene3();

  //if (frameCount % 200 == 0) walker.resetPoints();
  //if (frameCount % 402 == 0) walker2.resetPoints();

  //if (beat.isOnset() && random(1) < 0.2) {
  //  resetScene();
  //}
}

void scene1() {
  walker2.run();
  walker2.drawCircles();
  walker2.randomSteps(50);
  walker2.connectPoints(img);
  walker2.display(img);
  walker2.detectBeat(beat.isOnset());
}

void scene2() {
  pushMatrix();
  translate(width/2, height/2);
  for (int i=0; i<walkers.length; i++) {
    walkers[i].run();
    walkers[i].randomSteps(50);
    //walkers[i].drawCircles();
    walkers[i].rotateScene();
    walkers[i].display(img);
    walkers[i].connectPoints(img);
    walkers[i].detectBeat(beat.isOnset());
  }
  popMatrix();
}

void scene3() {
  pushMatrix();
  translate(50, 50);
  for (int i=0; i<gridStep; i++) {
    for (int j=0; j<gridStep; j++) {
      pushMatrix();
      translate((i*w), j*h);
      walkersGrid[i][j].run();
      walkersGrid[i][j].constrainPoints();
      //walkersGrid[i][j].drawCircles();
      walkersGrid[i][j].connectPoints(img);
      walkersGrid[i][j].display(img);
      walkersGrid[i][j].randomSteps(20);
      walkersGrid[i][j].detectBeat(beat.isOnset());
      popMatrix();
    }
  }
  popMatrix();

  println(walkersGrid[0][0].points.size());
}

void mousePressed() {
  resetScene();
}

void soundDebug(){
  for (int i = 0; i < myAudioRange; ++i) {
    stroke(0); fill(255);
    float tempIndexAvg = (fft.getAvg(i) * myAudioAmp) * myAudioIndexAmp;
    float tempIndexCon = constrain(tempIndexAvg, 0, myAudioMax);

    fftAmp[i] = tempIndexCon;
    //rect( xStart + (i*spacing), yStart, rectSize, tempIndexCon);
    myAudioIndexAmp+=myAudioIndexStep;
  }
  myAudioIndexAmp = myAudioIndex;
}

void resetScene() {
  walker.resetPoints();
  walker2.resetPoints();

  for (int i=0; i<walkers.length; i++) {
    walkers[i].resetPoints();
  }

  for (int i=0; i<gridStep; i++) {
    for (int j=0; j<gridStep; j++) {
      walkersGrid[i][j].resetPoints();
    }
  }
}
