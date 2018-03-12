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
          .setValue(0.01)
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

  void draw() {
    background(190);
  }

  void controlEvent(ControlEvent theControlEvent) {
    if(theControlEvent.isFrom("stepRange")) {
      // min and max values are stored in an array.
      // access this array with controller().arrayValue().
      // min is at index 0, max is at index 1.
      stepMin = int(theControlEvent.getController().getArrayValue(0));
      stepMax = int(theControlEvent.getController().getArrayValue(1));
      println("range update, done." + stepMin + stepMax);
    }

  }
}
