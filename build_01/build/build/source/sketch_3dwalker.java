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

public class sketch_3dwalker extends PApplet {




Minim minim;
AudioPlayer song;
AudioInput in;
BeatDetect beat;
FFT fft;

PVector target;
PVector position;

float rot = 0;
float targetrot = 0;

float scale = 0;
float targetscale = 0;

float rotz = 0;
float targetrotz = 0;

int dir = 1;
float step = 5;

float band;

ArrayList<PVector> points;

PImage img;

public void setup(){
  
  //fullScreen(P3D);

  target = new PVector(0, 0, 0);
  position = new PVector(0, 0);

  points = new ArrayList<PVector>();

  points.add(new PVector(0, 0, 0));

  img = loadImage("img2.jpg");

  img.resize(width, height);

  minim = new Minim(this);
    // a beat detection object song SOUND_ENERGY mode with a sensitivity of 10 milliseconds
  beat = new BeatDetect();
  // use the getLineIn method of the Minim object to get an AudioInput
  in = minim.getLineIn();

  //song = minim.loadFile("clav.mp3", 2048);
  fft = new FFT( in.bufferSize(), in.sampleRate() );

  //song.play();

  targetrot = random(TWO_PI);
  rot = targetrot;

  targetscale = 1;
}

public void draw(){

  fft.forward(in.mix);
  beat.detect(in.mix);

  float rand = random(0, 1);

  step = map(fft.getBand(100), 0, 5, 1, 100);

  hint(ENABLE_DEPTH_TEST);
  background(255);

  noStroke();
  fill(0);
  rect(0, 0, width, height);

  hint(DISABLE_DEPTH_TEST);

  float rand2 = map(step, 0, 20, 0.0f, 1.0f);

  if (frameCount % 1 == 0)  {
    if(rand < rand2){
      direction();
      target.x += step * dir;
      println("Trigger x");
      addPoint(target.x, target.y, target.z);
    }

    if(rand > 0.3f && rand < 0.6f){
      direction();
      target.y += step * dir;
      println("Trigger y");
    }

    if(rand > 0.6f){
      direction();
      target.z += step * dir;
      println("Trigger z");
    }

  }

  println(rand, target.z, points.size());

  translate(width/2, height/2);

  pushMatrix();
  rotateY(rot);
  rotateX(rotz);
  scale(scale);
  rotateZ(mouseX * 0.02f);
  noStroke();
  fill(255, 0, 0);
  sphere(10);
  fill(255, 0, 0);

  int j=0;
  for (PVector p : points){

    if(j % 10 == 0){
    pushMatrix();
    noFill();
    stroke(255, 0, 0);
    strokeWeight(0.7f);
    translate(p.x, p.y, p.z);
    ellipse(0, 0, fft.getBand(j*10 % 2048) * 5, fft.getBand(j*10 % 2048) * 5);
    popMatrix();
    }
    j++;
  }




  beginShape();
  noFill();
  stroke(0);
  strokeWeight(2);

  if ( beat.isOnset() ) {

    if (random(1) < 0.3f){
      println("Rotated to:", targetrot);
      targetrot = random(-PI, PI);
    }

      if (random(1) < 0.6f || random(1) > 0.3f){
        println("Rotated to:", targetrot);
        targetrotz = random(-PI, PI);
    }

    if (random(1) > 0.6f){
        println("Rotated to:", targetrot);
        targetscale  = random(0.8f, 1.4f);
    }
  }

  rot = lerp(rot, targetrot, 0.001f);
  rotz = lerp(rotz, targetrotz, 0.001f);
  scale = lerp(scale, targetscale, 0.01f);

  int i=0;
  for(PVector p : points){
    stroke(255 * (i * 0.001f), 0, 255 * (i * 0.02f));
    strokeWeight(0.5f);



    int co = img.get((int)p.x + width/2, (int)p.y + height/2);

    stroke(co);

    float offsetx = 0;
    float offsety = 0;
    float offsetz = 0;


      //offsetx = map(fft.getBand(i*2), 0, 10, -50, 50);
      band = fft.getBand((i*10) % 2048);

      offsety = map(noise(band + (i * 0.2f)), 0, 1, band * -10, band * 10);
    //}



    vertex(p.x + offsetx, p.y + offsety, p.z);

    i++;
  }
  endShape();

  popMatrix();

  //println(target);

  position = PVector.lerp(position, target, 0.2f);

  float w = width/2;
  float h = height/2;

  if(position.x > w || position.x < -w || position.y > h || position.y < -h || position.z > h || position.z < -h){
    target = new PVector(0, 0, 0);
    position = new PVector(0, 0, 0);
  }
}

public void direction(){
  println("Change direction");
  if(random(1) < 0.5f){ dir = 1;} else {dir = -1;}
}

public void addPoint(float x, float y, float z){
  points.add(new PVector(x, y, z));
}

public void mousePressed(){
    for (int i = points.size()-1; i >= 0; i--) {
      PVector p = points.get(i);
        points.remove(i);
    }
}
  public void settings() {  size(500, 500, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_3dwalker" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
