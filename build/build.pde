// *************************************************************************************************************

import ddf.minim.*;
import ddf.minim.analysis.*;

Minim minim;
AudioPlayer song;
AudioInput in;
BeatDetect beat;
FFT fft;

boolean debugSound = false;

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

import controlP5.*;

ControlP5 cp5;
ControlFrame cf;
Range range;

float stepMin = 10;
float stepMax = 100;

// *************************************************************************************************************

import codeanticode.syphon.*;

SyphonServer server;

// *************************************************************************************************************

ArrayList<SoundRect> rects;

Scene1 scene1;
Scene2 scene2;
Scene3 scene3;

PImage img;

int time;

PShader fade;

/*
TODO :
 - xProper sound mapping
 - x Control p5
 */

void settings(){
  size(myStageW, myStageH, P3D);
  //fullScreen(P3D);

}

void setup() {

  //cp5 = new ControlP5(this);
  cf = new ControlFrame(this, 340, 800, "Controls");
  surface.setLocation(350, 10);
  //createGUI();

  img = loadImage("img2.jpg");
  img.resize(width, height);

  minim = new Minim(this);
  // a beat detection object song SOUND_ENERGY mode with a sensitivity of 10 milliseconds
  beat = new BeatDetect();
  // use the getLineIn method of the Minim object to get an AudioInput
  in = minim.getLineIn();

  // Create syhpon server to send frames out.
  server = new SyphonServer(this, "Processing Syphon");

  // missing upper bands
  song = minim.loadFile("clav-16.wav");
  song.play();
  song.loop();

  fft = new FFT( song.bufferSize(), song.sampleRate() );
  fft.linAverages(myAudioRange);
  fft.window(FFT.HAMMING);

  scene1 = new Scene1();
  scene2 = new Scene2();
  scene3 = new Scene3();

  rects = new ArrayList<SoundRect>();

  fade = loadShader("fade.glsl");
}

void draw() {

  info();

  fade.set("fadeLevel", map(mouseX, 0, width, 0.000, 0.500));
  filter(fade);

  time = int(millis() * 0.0833);

  fft.forward(song.mix);
  beat.detect(song.mix);

  hint(ENABLE_DEPTH_TEST);

  //background(255);

  fftSound();

  hint(DISABLE_DEPTH_TEST);

  scene1.run();
  //scene2.display();
  //scene3.display();

  server.sendScreen();
}

void mousePressed() {
  scene1.resetScene();
  scene2.resetScene();
  scene3.resetScene();
}

void fftSound(){
  for (int i = 0; i < myAudioRange; ++i) {
    stroke(0); fill(255);
    float tempIndexAvg = (fft.getAvg(i) * myAudioAmp) * myAudioIndexAmp;
    float tempIndexCon = constrain(tempIndexAvg, 0, myAudioMax);

    fftAmp[i] = tempIndexCon;
    if(debugSound) rect( xStart + (i*spacing), yStart, rectSize, tempIndexCon);
    myAudioIndexAmp+=myAudioIndexStep;
  }
  myAudioIndexAmp = myAudioIndex;
}

void createGUI(){
  range = cp5.addRange("rangeController")
             // disable broadcasting since setRange and setRangeValues will trigger an event
             .setBroadcast(false)
             .setPosition(50,50)
             .setSize(100,40)
             .setHandleSize(20)
             .setRange(0,255)
             .setRangeValues(50,100)
             // after the initialization we turn broadcast back on again
             .setBroadcast(true)
             .setColorForeground(color(255,40))
             .setColorBackground(color(255,40))
             ;
   noStroke();
}

void info(){
    String txt_fps = "FPS:" + frameRate;
    surface.setTitle(txt_fps);
  }