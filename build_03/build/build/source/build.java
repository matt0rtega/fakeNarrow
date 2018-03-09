import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 

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

public void settings(){
  size(myStageW, myStageH, P3D);
  //fullScreen(P3D);

}

public void setup() {
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

public void draw() {

  //println(rects.size());

  fade.set("fadeLevel", map(mouseX, 0, width, 0.000f, 0.500f));
  filter(fade);

  time = PApplet.parseInt(millis() * 0.0833f);

  fft.forward(song.mix);
  beat.detect(song.mix);

  hint(ENABLE_DEPTH_TEST);

  //background(255);

  soundDebug();

  // noStroke();
  // fill(0, map(mouseX, 0, width, 0, 50));
  // rect(0, 0, width, height);

  hint(DISABLE_DEPTH_TEST);

  //lights();
  //directionalLight(255, 0, 255, 0, 1.0, 0);

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

public void scene1() {
  walker2.run();
  walker2.drawCircles();
  walker2.randomSteps(50);
  walker2.connectPoints(img);
  walker2.display(img);
  walker2.detectBeat(beat.isOnset());
}

public void scene2() {
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

public void scene3() {
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

public void mousePressed() {
  resetScene();
}

public void soundDebug(){
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

public void resetScene() {
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
    pushMatrix();
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

          if(offset > 5 && rects.size() < 20) {
            rects.add(new SoundRect(p.x, p.y, p.z, offset));
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
    popMatrix();

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

  float w, h;
  float offset;

  float lifespan;

  int bandSelector;

  SoundRect(float x, float y, float z, float offset){
    location = new PVector(x, y, z);

    w = random(25);
    h = random(-25, 25);

    this.offset = offset;

    bandSelector = (int)random(myAudioRange);

    lifespan = 255;
  }

  public void display(){
    pushMatrix();
    translate(location.x, location.y, location.z);
    noStroke();
    fill(255, lifespan);
    rect(0, 0, fftAmp[bandSelector], h * offset/2 - fftAmp[5]);
    popMatrix();

    lifespan-=0.5f;
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

  float rot = 0;
  float targetrot = 0;

  int dir = 1;
  float step = 5;

  float rotz = 0;
  float targetrotz = 0;

  float scale = 0;
  float targetscale = 0;

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

    targetrot = random(TWO_PI);
    rot = targetrot;
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

  public void colorize(){

  }

  public void constrainPoints(){
    target.x = constrain(target.x, -w, w);
    target.y = constrain(target.y, -h, h);
    target.z= constrain(target.z, -h, h);
  }

  public void update(){

    // Change step size basd on the volume
    step = map(fft.getBand(100), 0, 5, 1, 100);

    if(time % 5 == 0 && points.size() < (int)map(mouseY, 0, height, 10, 500)){
      addPoint(target.x, target.y, target.z);
    }

  }

  public void detectBeat(boolean beat){
    this.beat = beat;
  }

  private void rotateTransform(){
    // Transform object
    rotateY(rot);
    rotateX(rotz);
    //rotateZ(mouseX * 0.02);
  }

  public void slowRotate(float speed){
    rotateZ(time * 0.2f * speed * index);
  }

  public void display(PImage tempimg){
    pushMatrix();

    rotateTransform();
    slowRotate(0.001f);

    circle.display(points);

    // for(SoundRect r : rects){
    //   r.display();
    // }

    for (int i= rects.size()-1; i>= 0; i--){
      SoundRect r = rects.get(i);
      r.display();
      if (r.isDead()) {
        rects.remove(i);
      }
    }

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

  public void randomSteps(float range){

    // Set probability parameter
    float rand = random(1);
    float rand2 = map(step, 0, 20, 0.0f, 1.0f);

    float band = fftAmp[bandSelector];

    float randomStep = map(band, 0, myAudioMax, 5, 200);

    // Every n frames, take a step in a random direction
    if (time % 5 == 0)  {
      if(rand < rand2){
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

  public void circleStep(){
    float t = random(TWO_PI);
    t = time;
    float rand = random(1);
    float rand2 = map(step, 0, 20, 0.0f, 1.0f);
    float circleStep = random(10, 100);

    float x = map(sin(t * 0.01f), -1, 1, -width/2, width/2);
    float y = map(cos(t * 0.02f), -1, 1, -height/2, height/2);

    if (time % 5 == 0)  {
      if(rand < rand2){
        //target.x = circleStep * sin(t );
        position.x = x;
        //println("Trigger x");

      }

      if(rand > 0.3f && rand < 0.6f){
        //target.y = circleStep * cos(t);
        position.y = y;
        //println("Trigger y");
      }

      if(rand > 0.6f){
        target.z = circleStep * sin(t);
        //println("Trigger z");
      }
    }
  }

  public void rotateScene(){

    // Every beat, take a rotation in a random direction
    if ( beat ) {
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

  public void randomRotate(){
    targetrot = random(-PI, PI);
    rot = targetrot;
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
    points.add(new PVector(x, y, z));
  }

  public void drawSphere(){
    // Debug point
    noStroke();
    fill(255, 0, 0);
    sphere(10);
  }

  public void drawCircles(){
    pushMatrix();
    rotateTransform();

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

    rotateTransform();
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
