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

float myAudioAmp = 30.0f;
float myAudioIndex = 0.05f;
float myAudioIndexAmp = myAudioIndex;
float myAudioIndexStep = 0.225f;

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

float stepMin = 10;
float stepMax = 100;

// *************************************************************************************************************


SyphonServer server;

// *************************************************************************************************************

ArrayList<SoundRect> rects;

int sceneSelector = 1;

Scene1 scene1;
Scene2 scene2;
Scene3 scene3;
Scene4 scene4;

PImage img;

int time;

PShader fade;

PGraphics pg;

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

  img = loadImage("img.png");
  img.resize(width, height);

  pg = createGraphics(width, height, P3D);

  minim = new Minim(this);
  // a beat detection object song SOUND_ENERGY mode with a sensitivity of 10 milliseconds
  beat = new BeatDetect();
  // use the getLineIn method of the Minim object to get an AudioInput
  in = minim.getLineIn();

  // Create syhpon server to send frames out.
  server = new SyphonServer(this, "Processing Syphon");

  // missing upper bands
  //song = minim.loadFile("clav-16.wav");
  //song.play();
  //song.loop();

  fft = new FFT( in.bufferSize(), in.sampleRate() );
  fft.linAverages(myAudioRange);
  fft.window(FFT.HAMMING);

  scene1 = new Scene1();
  scene2 = new Scene2();
  scene3 = new Scene3();
  scene4 = new Scene4();

  rects = new ArrayList<SoundRect>();

  fade = loadShader("fade.glsl");
}

public void draw() {

  info();

  filter(fade);

  time = PApplet.parseInt(millis() * 0.0833f);

  fft.forward(in.mix);
  beat.detect(in.mix);

  hint(ENABLE_DEPTH_TEST);

  //background(255);

  fftSound();

  hint(DISABLE_DEPTH_TEST);

  if(sceneSelector == 1)scene1.run();
  if(sceneSelector == 2)scene2.display();
  if(sceneSelector == 3)scene3.display();
  if(sceneSelector == 4)scene4.run();

  //pg.beginDraw();
  //pg.fill(fftAmp[3]);
  //pg.rect(0, 0, 100, 100);
  //pg.endDraw();

  //image(pg, 0, 0);

  server.sendScreen();
}

public void mousePressed() {
  resetScenes();

  //scene4.updateLastTime();
}

public void resetScenes(){
  scene1.resetScene();
  scene2.resetScene();
  scene3.resetScene();
  scene4.resetScene();
}

public void keyPressed(){
  switch(key){
    case 49:
    sceneSelector = 1;
    println(sceneSelector);
    break;
    case 50:
    sceneSelector = 2;
    println(sceneSelector);
    break;
    case 51:
    sceneSelector = 3;
    println(sceneSelector);
    break;
    case 52:
    sceneSelector = 4;
    println(sceneSelector);
    break;
  }

  resetScenes();
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
class CircleSystem {

  ArrayList<SoundCircle> circles;

  float size = 0;
  float targetsize = 0;
  float[] easing = new float[100];
  int bandSelector;

  CircleSystem(){
    circles = new ArrayList<SoundCircle>();

    for(int i=0; i<easing.length; i++){
      easing[i] = random(0.001f, 0.05f);
    }

    bandSelector = (int)random(0, myAudioRange);
  }

  public void display(ArrayList<PVector> points){
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

  public void selectRandomBand(){
    if(time % 100 == 0){
      bandSelector = (int)random(0, myAudioRange);
    }
  }
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

       cp5.addSlider("audioIndex")
          .plugTo(parent, "myAudioIndexStep")
          .setRange(0, 1)
          .setValue(0.01f)
          .setPosition(20, 120)
          .setSize(200, 30);
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
class HorizontalRect extends Particle{

  float size = 0;
  float targetSize = random(100);

  HorizontalRect(float x, float y, float z){
    super(x, y, z);

    co = color(random(100, 255));
  }

  public void display(){
    size = lerp(size, targetSize, 0.05f);

    pushMatrix();
    rectMode(CENTER);
    translate(location.x, location.y, location.z);
    if(size > targetSize - 10 && size > 100) { ellipse(size, 0, 20, 20); ellipse(-size, 0, 20, 20);}
    fill(co);
    noStroke();
    rect(0, 0, size, 10);
    popMatrix();
  }

  public void reset(){
    size = 0;
  }

  public void setTargetSize(float tempsize){
    targetSize = random(20, width);
  }
}
class Particle {

  PVector origin;
  PVector location;
  PVector velocity;
  PVector acceleration;

  float topspeed = 10;

  int co;

  float lifespan;
  float radius = random(100, 300);

  int bandSelector;
  float band;

  Particle(float x, float y, float z) {
    origin = new PVector(x, y, z);
    location = new PVector(x, y, z);
    velocity = new PVector(0, 0, 0);
    acceleration = new PVector(0, 0, 0);
    bandSelector = (int)random(myAudioRange);
    lifespan = 255;
  }

  public void run(){
    update();
    display();
  }

  public void updateSound(){
    band = fftAmp[bandSelector];
  }

  public void update() {
    float dist = PVector.dist(location, origin);

    if(dist > radius || dist < -radius) velocity = new PVector(0, 0, 0);

    location.add(velocity);
    velocity.add(acceleration);
    velocity.limit(topspeed);
    acceleration.mult(0);

    lifespan-=1;
  }

  public int colorize(PImage tempimg){
    co = tempimg.get((int)location.x + width/2, (int)location.y + height/2);
    return co;
  }

  public void display(){
    pushMatrix();
    translate(location.x, location.y, location.z);
    noStroke();
    strokeWeight(1);
    stroke(co, lifespan);
    point(0, 0);
    popMatrix();

  }

  public boolean isDead() {
    if (lifespan<0) {
      return true;
    } else {
      return false;
    }
  }
}
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

  public void runTimer(){
    currentTime = millis();
    getTimeEllapsed();
  }

  public void setFade(){
    fade.set("fadeLevel", map(mouseX, 0, width, 0.000f, 0.500f));
  }

  public void setFade(float fadeLevel){
    fade.set("fadeLevel", fadeLevel);
  }

  public void updateSound(){
    sceneband = fftAmp[20];
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
    }

    // Ease to the new transformations
    rot = lerp(rot, targetrot, 0.002f);
    rotz = lerp(rotz, targetrotz, 0.002f);

    rotateY(rot);
    rotateZ(rotz);
  }

  public void updateLastTime(){
    lastTime = currentTime;
  }

  public void getTimeEllapsed(){
    duration = currentTime - lastTime;
  }
}
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

  public void run(){
    //rotateScene();
    setFade();
    display();
  }

  public void display(){

    pushMatrix();
    translate(width/2, height/2);

    displayCircles();
    displayWalkers();

    popMatrix();
  }

  public void displayCircles(){
    circles.display(walker.points);
    circles2.display(walker2.points);
  }

  public void displayWalkers(){
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

  public void resetScene(){
    walker.resetPoints();
    walker2.resetPoints();
  }

}
class Scene2 extends Scene{

  Walker[] walkers = new Walker[5];

  Scene2(){
    for (int i=0; i<5; i++) {
      walkers[i] = new Walker(0, i);
    }
  }

  public void display(){
    pushMatrix();
    translate(width/2, height/2);
    rotateScene();
    for (int i=0; i<walkers.length; i++) {
      walkers[i].run();
      walkers[i].randomSteps();
      walkers[i].display(img);
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
        walkers[i][j] = new Walker(0, i + j);
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

  public void resetScene(){
    for (int i=0; i<gridStep; i++) {
      for (int j=0; j<gridStep; j++) {
        walkers[i][j].resetPoints();
      }
    }
  }
}
class Scene4 extends Scene{

  ArrayList<Splurb> splurbs;

  Scene4(){
    splurbs = new ArrayList<Splurb>();
  }

  public void run(){
    setFade(0.002f);
    runTimer();
    display();
  }

  public void display(){

    pushMatrix();
    translate(width/2, height/2);

    if(beat.isOnset() && splurbs.size() < 100 || duration > 5000.00f) {
      for(int i=0; i<10; i++){
        createSplurbs();
      }
      updateLastTime();
      println("Splurb created");
    }

    displaySplurbs();

    popMatrix();
  }

  public void resetScene(){

  }

  public void createSplurbs(){
    splurbs.add(new Splurb(random(-width/2, width/2), random(-height/2, height/2), 0));
  }

  public void displaySplurbs(){
    for (int i = splurbs.size()-1; i>= 0; i--){
      Splurb s = splurbs.get(i);
      s.update();
      s.display();
      if (s.isDead() || s.location.y > height/2 || s.location.y < -height/2 || s.location.x > width/2 || s.location.x < -width/2) {
        splurbs.remove(i);
      }
    }
  }
}
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

  public void run(){
    rotateScene();
    display();
  }

  public void display(){

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

  public void resetScene(){
    walker.resetPoints();
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

  public void createRects(){
    float row = height/fftAmp.length;
    for(int i=0; i < fftAmp.length; i++){
      hrects.add(new HorizontalRect(0, i * row, 0));
    }
  }

  public void displayHRects(){
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
class SoundCircle extends Particle {

  int bandSelector;
  float size = 10;

  int co;

  int mode = 0;

  SoundCircle(float x, float y, float z, float size){
    super(x, y, z);

    bandSelector = (int)random(0, myAudioRange);

    this.size = size;

    co = colorize(img);

    if(random(1) < 0.5f){
      mode = 1;
    } else {
      mode = 0;
    }
  }

  public void run(){
    display();
    update();
  }

  public void display(){
    float band = fftAmp[bandSelector];

    float offsetz = map(band, 0, myAudioMax, -200, 200);

    pushMatrix();
    translate(location.x, location.y, location.z + offsetz);
    if(mode == 0){
      noStroke();
      fill(co, lifespan);
    } else {
      noFill();
      stroke(co, lifespan);
    }
    ellipse(0, 0, size, size);
    popMatrix();
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
class Splurb extends Particle{

  float noiseoff = random(10000);

  float size = 0;
  float targetSize = 10;

  PVector target;
  PVector prevloc;

  float offx, offy;

  int mode;

  int co = color(255);

  float dir;
  float theta = 0;
  float inc = random(0.005f, 0.02f);
  float range;

  float blobxrange = random(25);
  float blobyrange = random(25);

  Splurb(float x, float y, float z){
    super(x, y, z);

    target = new PVector(random(-width/2, width/2), random(-width/2/ width/2));
    prevloc = location.copy();

    range = random(25, 300);

    if(random(1) < 0.5f) {
      dir = -1.0f;
    } else {
      dir = 1.0f;
    }


    float rand = random(1);

    if(rand < 0.33f){
      mode = 0;
    } else if(rand > 0.33f && rand < 0.66f){
      mode = 1;
    } else {
      mode = 2;
    }

    co = colorize(img);
  }

  public void update(){

    // Needs to happen before update
    setSize();

    prevloc = location.copy();

    updateSound();
    if(mode != 2) move();

    lifespan -= 0.5f;
  }

  public void setTarget(){

    PVector dir = PVector.sub(target, location);
    dir.normalize();
    dir.mult(0.1f);

    acceleration = dir;

    float d = PVector.dist(target, location);
    if(d < 200) target = new PVector(random(width), random(height));
  }

  public void setSize(){
    float d = PVector.dist(prevloc, location);
    size = map(sin(time * 0.04f + noiseoff), -1, 1, 0, d * 10 + band);

    if(mode == 0){
      if(beat.isOnset()) targetSize = map(band, 0, myAudioMax, 2, 20);
    } else {
      if(beat.isOnset()) targetSize = map(band, 0, myAudioMax, 1, 25);
    }

    size = lerp(size, targetSize, 0.01f);

    targetSize *= 0.85f;
  }

  public void display(){

    if(mode == 0) circleJitter();
    if(mode == 1) { offx = 0; offy = 0;}
    if(mode == 2) moveCircle();

    pushMatrix();
    translate(location.x + offx, location.y + offy);
    fill(co, lifespan);
    noStroke();
    blob();
    ellipse(0, 0, size, size);
    popMatrix();
  }

  public void moveCircle(){
    float inc = map(band, 0, myAudioMax, 0.001f, 0.100f);

    offx = map(sin(theta + noiseoff), -1, 1, -range, range);
    offy = map(cos(theta + noiseoff), -1, 1, -range, range);

    theta += inc * dir;
  }

  public void blob(){
    int numSteps = 10;
    float inc = TWO_PI / numSteps;

    float range = 50;
    beginShape();
    for(int i=0; i<=numSteps+2; i++){
      float x = map(sin(inc * i), -1, 1, -size, size);
      float y = map(cos(inc * i), -1, 1, -size, size);

      float xoff = map(noise(x * 0.02f, y * 0.02f, frameCount * 0.001f + noiseoff), 0, 1, -blobxrange, blobxrange);
      float yoff = map(noise(x * 0.02f, y * 0.02f, frameCount * 0.005f + noiseoff), 0, 1, -blobyrange, blobyrange);

      curveVertex(x + xoff, y + yoff);
    }
    endShape();
  }

  public void circleJitter(){
    float piband = map(band, 0, myAudioMax, 0, TWO_PI);
    offx = map(sin(piband), -1, 1, -100, 100);
    offy = map(cos(piband), -1, 1, -100, 100);
  }

  public void move(){
    float speed = 0.2f;
    float limit = map(band, 0, myAudioMax, 1, 3);

    float accx = map(noise(time * 0.02f, noiseoff), 0,1, -speed, speed);
    float accy = map(noise(time * 0.05f, noiseoff), 0,1, -speed, speed);
    acceleration = new PVector(accx, accy);

    location.add(velocity);
    velocity.add(acceleration);
    velocity.limit(limit);
    acceleration.mult(0);
  }

}
class Walker {

  ArrayList<PVector> points;
  ArrayList<Float> offsetx;
  ArrayList<Float> offsety;

  PVector location;

  int dir = 1;
  float stepMin = 5;
  float stepMax = 200;

  int vertexLimit = 500;

  // 0 - DEFAULT, 1 - POINTS, 2 - QUADS_STRIP
  int mode;
  int index;

  // Width and height for constraints
  int w, h;

  int co;

  int bandSelector;

  Walker(int mode, int index){
    points = new ArrayList<PVector>();
    offsetx = new ArrayList<Float>();
    offsety = new ArrayList<Float>();

    location = new PVector(0, 0, 0);

    points.add(new PVector(0, 0, 0));
    offsetx.add(0.0f);
    offsety.add(0.0f);

    bandSelector = (int)random(0, myAudioRange);

    co = 255;

    this.mode = mode;
    this.index = index;
  }

  public void run(){
    update();
  }

  public void constrainPoints(float w, float h){
    location.x = constrain(location.x, -w, w);
    location.y = constrain(location.y, -h, h);
    location.z= constrain(location.z, -h, h);
  }

  public void update(){

    float randAdd = (int)map(fftAmp[10], 0, myAudioMax, 5, 1);

    if(time % randAdd == 0 && points.size() < vertexLimit){
      addPoint(location.x, location.y, location.z);
    }
  }

  public void display(PImage tempimg){
    pushMatrix();

    noFill();

    if (mode == 0) beginShape();
    if (mode == 1) beginShape(POINTS);
    if (mode == 2) {beginShape(QUAD_STRIP); fill(255, 25);}


    int i=0;
    for(PVector p : points){

      float band = fftAmp[i % myAudioRange];

      co = tempimg.get((int)p.x + width/2, (int)p.y + height/2);

      stroke(co);
      strokeWeight(1);

      if (mode == 2) {fill(co); noStroke();}

      float range = 100;
      offsetx.set(i, map(noise(band * 0.01f + time * 0.01f + (i * 0.002f)), 0, 1, -band, band));
      offsety.set(i, map(noise(band * 0.06f + time * 0.06f + (i * 0.006f)), 0, 1, -band, band));

      float offx = offsetx.get(i);
      float offy = offsety.get(i);

      //p.x = p.x + offsetx;

      if(mode == 0){
        curveVertex(p.x + offx, p.y + offy, p.z);
      } else {
        vertex(p.x, p.y , p.z);
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

  public void randomSteps(){

    // Set probability parameter
    float rand = random(1);

    float band = fftAmp[bandSelector];

    float randomStep = map(band, 0, myAudioMax, stepMin, stepMax);

    // Every n frames, take a step in a random direction
    if (time % 5 == 0)  {
      if(rand < 0.3f){
        changeDirection();
        location.x += randomStep * dir;
        //println("Trigger x");
      }

      if(rand > 0.3f && rand < 0.6f){
        changeDirection();
        location.y += randomStep * dir;
        //println("Trigger y");
      }

      if(rand > 0.6f){
        changeDirection();
        location.z += randomStep * dir;
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

    location = new PVector(start.x, start.y, start.z);
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
    points.add(new PVector(x, y, z));
    offsetx.add(0.0f);
    offsety.add(0.0f);
  }

  public void connectPoints(){
    float band = fftAmp[bandSelector];

    float range = map(mouseY, 0, width, 0, band);

      int i=0;
      for(PVector p : points){
        int j=0;
        for (PVector other : points){
          if(p != other){

            float offx = offsetx.get(i);
            float offy = offsety.get(i);

            float otheroffx = offsetx.get(j);
            float otheroffy = offsety.get(j);
            // Need to add offset
            float dist = PVector.dist(p, other);

            stroke(255);

            if(dist < range){
              line(p.x + offx, p.y + offy, p.z, other.x + otheroffx, other.y + otheroffy, other.z);
            }
          }

        j++;
        }
        i++;
      }

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
