class Scene4 extends Scene{

  ArrayList<Splurb> splurbs;

  Scene4(){
    splurbs = new ArrayList<Splurb>();
  }

  void run(){
    setFade(0.002);
    runTimer();
    display();
  }

  void display(){

    pushMatrix();
    translate(width/2, height/2);

    if(beat.isOnset() && splurbs.size() < 100 || duration > 5000.00) {
      for(int i=0; i<10; i++){
        createSplurbs();
      }
      updateLastTime();
      println("Splurb created");
    }

    displaySplurbs();

    popMatrix();
  }

  void resetScene(){

  }

  void createSplurbs(){
    splurbs.add(new Splurb(random(-width/2, width/2), random(-height/2, height/2), 0));
  }

  void displaySplurbs(){
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
