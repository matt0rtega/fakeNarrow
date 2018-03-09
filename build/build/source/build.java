import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 
import controlP5.*; 
import codeanticode.syphon.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class build extends PApplet {

// *************************************************************************************************************




Minim minim;
AudioPlayer song;
AudioInput in;
BeatDetect beat;
FFT fft;

boolean debugSound = false;

int myAudioRange = 50;
int myAudioMax = 100;

float myAudioAmp = 75.0f;
float myAudioIndex = 0.05f;
float myAudioIndexAmp = myAudioIndex;
float myAudioIndexStep = 0.25f;

float[] fftAmp = new float[myAudioRange];

// *************************************************************************************************************

int stageMargin = 100;
int myStageW = 800;
int myStageH = 600;
float spacing = (myStageW - (stageMargin * 2)) / myAudioRange;
int rectSize = (int)spacing;
int clrBG = 0xff333333;

float xStart = stageMargin;
float yStart = stageMargin;
int xSpacing = rectSize;

// *************************************************************************************************************



ControlP5 cp5;
ControlFrame cf;
Range range;

float stepMin, stepMax;

// *************************************************************************************************************



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

public void settings(){
  size(myStageW, myStageH, P3D);
  //fullScreen(P3D);

}

public void setup() {

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

public void draw() {

  info();

  fade.set("fadeLevel", map(mouseX, 0, width, 0.000f, 0.500f));
  filter(fade);

  time = PApplet.parseInt(millis() * 0.0833f);

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

public void mousePressed() {
  scene1.resetScene();
  scene2.resetScene();
  scene3.resetScene();
}

public void fftSound(){
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

public void createGUI(){
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

public void info(){
    String txt_fps = "FPS:" + frameRate;
    surface.setTitle(txt_fps);
  }
class ControlFrame extends PApplet {

  int w, h;
  PApplet parent;
  ControlP5 cp5;

  public ControlFrame(PApplet _parent, int _w, int _h, String _name) {
    super();
    parent = _parent;
    w=_w;
    h=_h;
    PApplet.runSketch(new String[]{this.getClass().getName()}, this);
  }

  public void settings() {
    size(w, h);
  }

  public void setup() {
    surface.setLocation(10, 10);
    cp5 = new ControlP5(this);

    cp5.addRange("stepRange")
               // disable broadcasting since setRange and setRangeValues will trigger an event
               .setBroadcast(false)
               .setPosition(20,20)
               .setSize(230,20)
               .setHandleSize(20)
               .setRange(1,255)
               .setRangeValues(50,100)
               // after the initialization we turn broadcast back on again
               .setBroadcast(true)
               .setColorForeground(color(255,40))
               .setColorBackground(color(255,40))
               ;

   cp5.addSlider("calibrateAudio")
      .plugTo(parent, "myAudioAmp")
      .setRange(0, 200)
      .setValue(1)
      .setPosition(20, 60)
      .setSize(200, 30);

    cp5.addToggle("showSoundDebug")
       .plugTo(parent, "debugSound")
       .setPosition(20, 100)
       .setSize(16, 16)
       .setValue(false);
    //
    // cp5.addKnob("blend")
    //    .plugTo(parent, "c3")
    //    .setPosition(20, 200)
    //    .setSize(200, 200)
    //    .setRange(0, 255)
    //    .setValue(200);
    //
    // cp5.addNumberbox("color-red")
    //    .plugTo(parent, "c0")
    //    .setRange(0, 255)
    //    .setValue(255)
    //    .setPosition(20, 300)
    //    .setSize(100, 20);
    //

  }

  public void draw() {
    background(190);
  }

  public void controlEvent(ControlEvent theControlEvent) {
    if(theControlEvent.isFrom("stepRange")) {
      // min and max values are stored in an array.
      // access this array with controller().arrayValue().
      // min is at index 0, max is at index 1.
      stepMin = PApplet.parseInt(theControlEvent.getController().getArrayValue(0));
      stepMax = PApplet.parseInt(theControlEvent.getController().getArrayValue(1));
      println("range update, done." + stepMin + stepMax);
    }

  }
}
class Scene1{

  Walker walker;

  ArrayList<Walker> walkers;

  float rot = 0;
  float targetrot = 0;

  float rotz = 0;
  float targetrotz = 0;

  Scene1(){
    walker = new Walker(0, width, height, 0);
    targetrot = random(TWO_PI);
    rot = targetrot;

    walkers = new ArrayList();
  }

  public void run(){
    rotateScene();
    display();
  }

  public void display(){

    pushMatrix();
    translate(width/2, height/2);
    rotateY(rot);
    rotateZ(rotz);
    displayRects();
    displayWalkers();
    walker.run();
    walker.addWalkers(walkers);
    walker.randomSteps(50);
    walker.detectBeat(beat.isOnset());
    walker.display(img);
    walker.setStepRange(stepMin, stepMax);
    popMatrix();


  }

  public void resetScene(){
    walker.resetPoints();

    for (int i= walkers.size()-1; i>= 0; i--){
      Walker w = walkers.get(i);
      rects.remove(i);
    }
  }

  public void displayWalkers(){
    for(Walker w : walkers){
      walker.run();
      walker.randomSteps(50);
      walker.detectBeat(beat.isOnset());
      walker.display(img);
      walker.setStepRange(stepMin, stepMax);
    }
  }

  public void displayRects(){
    for (int i= rects.size()-1; i>= 0; i--){
      SoundRect r = rects.get(i);
      r.display();
      if (r.isDead()) {
        rects.remove(i);
      }
    }
  }

  public void rotateScene(){

    // Every beat, take a rotation in a random direction
    if ( beat.isOnset() ) {
      if (random(1) < 0.3f){
        //println("Rotated to:", targetrot);
        targetrot = random(-PI, PI);
      }

      if (random(1) < 0.6f || random(1) > 0.3f){
        //println("Rotated to:", targetrot);
        targetrotz = random(-PI, PI);
      }
      //targetrot = random(-PI, PI);
    }

    // Ease to the new transformations
    rot = lerp(rot, targetrot, 0.002f);
    rotz = lerp(rotz, targetrotz, 0.002f);
  }
}
class Scene2{

  Walker[] walkers = new Walker[5];

  Scene2(){
    for (int i=0; i<5; i++) {
      walkers[i] = new Walker(0, width, height, i);
    }
  }

  public void display(){
    pushMatrix();
    translate(width/2, height/2);
    for (int i=0; i<walkers.length; i++) {
      walkers[i].run();
      walkers[i].randomSteps(50);
      //walkers[i].drawCircles();
      walkers[i].display(img);
      walkers[i].connectPoints(img);
      walkers[i].detectBeat(beat.isOnset());
      walkers[i].setStepRange(stepMin, stepMax);
    }
    popMatrix();
  }

  public void resetScene(){
    for(Walker w : walkers){
      w.resetPoints();
    }
  }
}
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

  public void display(){
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

  public void resetScene(){
    for (int i=0; i<gridStep; i++) {
      for (int j=0; j<gridStep; j++) {
        walkers[i][j].resetPoints();
      }
    }
  }
}
class SoundCircle {

  float size = 0;
  float targetsize = 0;
  float[] easing = new float[100];
  int bandSelector;

  SoundCircle(){
    for(int i=0; i<easing.length; i++){
      easing[i] = random(0.001f, 0.05f);
    }

    bandSelector = (int)random(0, myAudioRange);
  }

  public void display(ArrayList<PVector> points){
    float band = fftAmp[bandSelector];

    // Every n points add a circle
    int j=0;
    for (PVector p : points){
      if(j % 20 == 0){

        //float band = fftAmp[bandSelector];

        if(band > 50.0f){
          size = map(band, 0, myAudioMax, 0, 50);
        }

        size = lerp(size, 10, easing[j % easing.length]);



        for(int i=0; i<3; i++){
          float offset = (i * band * 0.5f);

          if(offset > 5) {
            rects.add(new SoundRect(p.x, p.y, p.z + offset, offset));
          }

          pushMatrix();
          noFill();
          stroke(255, 0, 0);
          strokeWeight(0.7f);
          translate(p.x, p.y, p.z + offset);
          ellipse(0, 0, size / i, size / i);
          popMatrix();
        }
      }
      j++;
    }

    selectRandomBand();
  }

  public void selectRandomBand(){
    if(time % 100 == 0){
      bandSelector = (int)random(0, myAudioRange);
    }
  }
}
class SoundRect {

  PVector location;
  PVector velocity;

  float w, h;
  float offset;

  float lifespan;

  int bandSelector;

  SoundRect(float x, float y, float z, float offset){
    location = new PVector(x, y, z);
    velocity = new PVector(0, random(-1, 1));

    w = random(25);
    h = random(-25, 25);

    this.offset = offset;

    bandSelector = (int)random(myAudioRange);

    lifespan = 255;
  }

  public void display(){

    float band = fftAmp[bandSelector];
    float offsetz = map(band, 0, myAudioMax, -100, 100);

    location.add(velocity);
    pushMatrix();
    translate(location.x, location.y, location.z + offsetz);
    noStroke();
    //fill(255, lifespan);
    strokeWeight(1);
    stroke(255, lifespan);
    point(0, 0);
    popMatrix();

    lifespan-=1;
  }

  public boolean isDead(){
          if(lifespan<0){
           return true;
          } else {
           return false;
          }
         }

}
class Walker {

  ArrayList<PVector> points;
  PVector target;
  PVector position;

  int dir = 1;
  float stepMin = 5;
  float stepMax = 200;

  float scale = 0;
  float targetscale = 0;

  int vertexLimit = 500;

  boolean beat;

  // 0 - DEFAULT, 1 - POINTS, 2 - QUADS_STRIP
  int mode;
  int index;

  // Width and height for constraints
  int w, h;

  int co;

  SoundCircle circle;

  int bandSelector;

  Walker(int mode, int w, int h, int index){
    points = new ArrayList<PVector>();
    target = new PVector(0, 0, 0);
    position = new PVector(0, 0);
    points.add(new PVector(0, 0, 0));

    targetscale = 1;

    circle = new SoundCircle();

    bandSelector = (int)random(0, myAudioRange);

    co = 255;

    this.mode = mode;
    this.index = index;
    this.w = w;
    this.h = h;
  }

  public void run(){
    update();
    //display();
    //randomSteps();
    //circleStep();
    //rotateScene();
    //randomRotate();
    //connectPoints();
  }

  public void constrainPoints(){
    target.x = constrain(target.x, -w, w);
    target.y = constrain(target.y, -h, h);
    target.z= constrain(target.z, -h, h);
  }

  public void update(){

    float randAdd = (int)map(fftAmp[10], 0, myAudioMax, 5, 1);

    if(time % randAdd == 0 && points.size() < vertexLimit){
      addPoint(target.x, target.y, target.z);
    }

  }

  public void addWalkers(ArrayList<Walker> tempwalkers){
    float band = fftAmp[10];

    if(band > 25){
      tempwalkers.add(new Walker(0, width, height, 0));
    }
  }

  public void detectBeat(boolean beat){
    this.beat = beat;
  }

  public void slowRotate(float speed){
    rotateZ(time * 0.2f * speed * index);
  }

  public void display(PImage tempimg){
    pushMatrix();

    //slowRotate(0.001);

    circle.display(points);

    noFill();

    if (mode == 0) beginShape();
    if (mode == 1) beginShape(POINTS);
    //noFill();
    if (mode == 2) {beginShape(QUAD_STRIP); fill(255, 25);}


    int i=0;
    for(PVector p : points){

      float band = fftAmp[i % myAudioRange];

      co = tempimg.get((int)p.x + width/2, (int)p.y + height/2);
      if (brightness(co) < 100){
        co = 255;
      }

      stroke(co);
      strokeWeight(0.5f);

      if (mode == 2) {fill(co); noStroke();}

      float offsetx = 0;
      float offsety = 0;
      float offsetz = 0;

      offsetx = map(noise(band * 0.01f + (i * 0.0001f)), 0, 1, -band, band);
      offsety = map(noise(band * 0.02f + (i * 0.002f)), 0, 1, -band, band);

      if(mode == 0){
        curveVertex(p.x + offsetx, p.y + offsety, p.z);
      } else {
        curveVertex(p.x + offsetx, p.y + offsety, p.z);
      }

      i++;
    }
    endShape();

    popMatrix();
  }

  public void setStepRange(float min, float max){
    stepMin = min;
    stepMax = max;
  }

  public void randomSteps(float range){

    // Set probability parameter
    float rand = random(1);

    float band = fftAmp[bandSelector];

    float randomStep = map(band, 0, myAudioMax, stepMin, stepMax);

    // Every n frames, take a step in a random direction
    if (time % 5 == 0)  {
      if(rand < 0.3f){
        changeDirection();
        target.x += randomStep * dir;
        //println("Trigger x");
      }

      if(rand > 0.3f && rand < 0.6f){
        changeDirection();
        target.y += randomStep * dir;
        //println("Trigger y");
      }

      if(rand > 0.6f){
        changeDirection();
        target.z += randomStep * dir;
        //prinln("Trigger z");
      }
    }
  }

  public void resetPoints(){
    // Move all points and start over
    for (int i = points.size()-1; i >= 0; i--) {
      PVector p = points.get(i);
        points.remove(i);
    }

    PVector start = new PVector(random(-100, 100), random(-100, 100), random(-100, 100));

    target = new PVector(start.x, start.y, start.z);
    position = new PVector(start.x, start.y, start.z);
    points.add(new PVector(start.x, start.y, start.z));
  }

  private void changeDirection(){
    // Change of changing direction
    if(random(1) < 0.5f){
      dir = 1;
    } else {
      dir = -1;
    }
  }

  public void addPoint(float x, float y, float z){
    if(points.size() < 100){
      points.add(new PVector(x, y, z));
    }
  }

  public void drawCircles(){
    pushMatrix();

    // Every n points add a circle
    int j=0;
    for (PVector p : points){
      if(j % 10 == 0){

        float size = map(fft.getAvg(j % myAudioRange), 0, myAudioMax, 0, 500);

        pushMatrix();
        noFill();
        stroke(255, 0, 0);
        strokeWeight(0.7f);
        translate(p.x, p.y, p.z);
        ellipse(0, 0, size, size);
        popMatrix();
      }
      j++;
    }

    popMatrix();
  }

  public void connectPoints(PImage tempimg){

    pushMatrix();

    float range = map(mouseY, 0, width, 0, 25);

    int j=0;
    for (PVector p : points){
      float band = fft.getBand(j*10 % 2048) * 5;
      co = tempimg.get((int)p.x + width/2, (int)p.y + height/2);
      stroke(co );
      float offset = map(noise(band + (j * 0.1f)), 0, 1, -band, band);
      for (PVector other : points){
        if(p != other){

          // Need to add offset
          float dist = PVector.dist(p, other);

          if(dist < range){
            line(p.x, p.y, p.z, other.x, other.y, other.z);
          }
        }
      }
      j++;
    }

    popMatrix();
  }

}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "build" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
