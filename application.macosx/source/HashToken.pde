/*

 HashToken Class
 - Every word, phrase or sentence is stored as a HashToken.
 - These HashTokens are then stored into a various Collections which we sort and use to get counts and do comparisons.

*/


public class HashToken {

  int i = 0;                                 //Individual occurence count for the string that this HashToken represents.
  String s;                                  //The string that this HashToken represents.
  
  //This ArrayList stores all of the location tokens - a list of where in each article this word appeared
  ArrayList locArray = new ArrayList();
  
  //Render position, and target position (for animation)
  Vec2D pos = new Vec2D(width/2,height/2);
  Vec2D tpos = new Vec2D();
  
  //Here, I must have been drinking, since I started to use really short and obscure variable names.
  //Sorry.
  
  float tsize = 0;                          //Text size (total width of the string when rendered)
  int t = 0;                                

  float aa = 0;                             //Text alpha value
  float taa = 1;                            //Target text alpha value

  HashToken ht1;                            //These are separate counters, one for each article
  HashToken ht2;
  
  color c1;                                 //Seperate colours, again one for each article
  color c2;
  
  int bc1 = 0;                              //Seperate box counts (the index of the sentence being displayed in the excerpt box) colours, again one for each article
  int bc2 = 0;
  
  int activeCount = 0;                      //Active counter - used to detect a significant mouseOver
  
  float w = 0;                              //Width and height
  float h = 0;
  
  float lt;                                 //Float total of all locations. Used to find an average location.
  int c;                                    //Counter
  
  float ts;                                 //Point size of text
  
  ArrayList sentences = new ArrayList();    //All of the sentences in which this token has appeared. Used to populate ExcerptBlocks.

  HashToken() {
  };

  void init(Article a1, Article a2) {
    //Hook this object up to receive mouse events.
    app.registerMouseEvent(this);
    
    //Create individual HashTokens for each article, add their totals to i;
    ht1 = (HashToken) a1.lwordHash.get(s);
    ht2 = (HashToken) a2.lwordHash.get(s);
    if (ht1 != null) t += ht1.i;
    if (ht2 != null) t += ht2.i;
    //Report the count for this word (this only happens once, and of course could be disabled)
    if (ht1 != null && ht2 != null) println(s + ":" + i + "  --- " + ht1.i + ":" + ht2.i);

    //Objects are placed at the centre of the screen by default
    pos = new Vec2D(width/2,height/2);
    
    //Set X position according to the balance of word use between articles using the setPos method
    setPos();
    //Set Y position to the average position in the article when the word is used
    lt = 0;
    c = 0;
    if (ht1 != null) {
      for (int i = 0; i < ht1.locArray.size(); i++) {
        c++;
        LocationToken loc = (LocationToken) ht1.locArray.get(i);
        lt += loc.l;
      };
    }
    if (ht2 != null) {
      for (int i = 0; i < ht2.locArray.size(); i++) {
        c++;
        LocationToken loc = (LocationToken) ht2.locArray.get(i);
        lt += loc.l;
      };
    };
    tpos.y = (lt/c) * height;
  };

  void setPos() {
    //Sets the x position of this token depending on the balance between the two articles.
    float one = (ht1 == null) ? (0):((float)ht1.i);
    float f = (one/t);
    if (f == 0) f = 0.02;
    if (f == 1) f = 0.95;
    tpos.x = padding + (f * (width - (2 * padding)));
    pos.interpolateToSelf(tpos, 0.1);
  };

  void render() {
    //Is this token focused?
    if (focusTokens.contains(this)) {
      //How long as this token been active for?
      activeCount ++;
      //ExcerptBlocks 1 & 2 are de-activated at slightly different times
      if (activeCount % 300 == 240) {
        box1.at = 0;
      };
      if (activeCount % 300 == 260) {
        box2.at = 0;
      };
      if (activeCount % 300 == 0) {
        //After being active for 300 frames, this token sends sentences to the ExcerptBlocks.
        setBoxes();
        box1.a = 0;
        box2.a = 0;
      };
    }
    else {
      activeCount = 0;  
    };
       
    boolean fck = (focusTokens.contains(this) || focusTokens.size() == 0);
    taa = (fck) ? (1):(-0.1);
    if (padding < 400) aa += (taa - aa)/10;
    if (focusTokens.contains(this)) aa = 2;
    setPos();
    //Draw anchors - these are the lines that draw from the tokens to the various places where that token occurs/
    //This process is repeated twice, once for each sub-token
    if (ht1 != null && fck) {
      for (int i = 0; i < ht1.locArray.size(); i++) {
        float ad = (focusTokens.size() == 0) ? (5):(0);

        stroke(color1, (i * aa) + ad);

        if (focusTokens.contains(this)) stroke(150 + i,0,0,100);
        LocationToken loc = (LocationToken) ht1.locArray.get(i);

        noFill();
        bezier(width - padding, loc.l * height, pos.x + ((width - padding)-pos.x)/3, ((float) height/2 + pos.y)/2, pos.x + ((width - padding)-pos.x)/4, ((float) height/2 + pos.y)/2, pos.x + tsize, pos.y);
        
        if (focusTokens.contains(this)) {
          float ew = (i == bc1) ? (20):(5);
          //These are the little marker circles at the left and right edges.
          ellipse(width - padding, loc.l * height,ew,ew);
        }
      };
    };

    if (ht2 != null && fck) {
      for (int i = 0; i < ht2.locArray.size(); i++) {
        float ad = (focusTokens.size() == 0) ? (5):(0);

        stroke(color2, (i * aa) + ad);

        if (focusTokens.contains(this)) stroke(44,34,15,100);
        LocationToken loc = (LocationToken) ht2.locArray.get(i);

        noFill();
        bezier(pos.x, pos.y, padding + (float) (pos.x - padding) /4, ((float) height/2 + pos.y)/2, padding + (float) (pos.x - padding) /3, ((float) height/2 + pos.y)/2,padding, loc.l * height);
        if (focusTokens.contains(this)) {
          float ew = (i == bc2) ? (20):(5);
          ellipse(padding, loc.l * height, ew, ew);
        };
      };
    };

    //Draw text
    if (padding < 500) {
    textFont(label);
    ts = 6 + (i/4);
    if (focusTokens.contains(this)) ts *= 1.5;// = max(ts,20);
    textSize(ts);
    tsize = textWidth(s);
    pushMatrix();
      
      translate(pos.x, pos.y,(2 + (i/10)) * aa);
      //find the distance to the mouse
      float d = abs(pos.x - mouseX);
      //make it a fraction of the width
      float df = d/200;
      //use it to scale
      float sc = (1 - df) * 2;
      if (scaling) {
        scale(max(0.4,sc));
      }
      else {
        sc = (fck) ? (1):(0);
      };
      
      fill(min(255,230 + i),200 * (aa + sc));
      if ((mouseCheck() && !focusTokens.contains(this)) || focusTokens.contains(this)) fill(#FFB52A);
      stroke(0,10);
      w = tsize + 6;
      h = ts + 6;
      rect(-3,-3 - ts/2,tsize + 6, ts + 6); 
      fill(0,10 + (i * 5) * (aa + sc));
     
      text(s, 2, ts/3);

    popMatrix();
    };

  };

  void doClick() {
    if (!focusTokens.contains(this)) {
      focusTokens.add(this);
      setBoxes();
    }
    else {
      focusTokens = new ArrayList();
      box2.at = 0;
      box1.at = 0;
    };
    bc1 = 0;
    bc2 = 0;
 
  };
  
  void setBoxes() {
    if (ht2 != null) {
      box1.body = "..." + (String) ht2.sentences.get(bc2) + "...";
      bc2 ++;
      if (bc2 >= ht2.sentences.size()) bc2 = 0;
      box1.at = 255;
    }
    else {
      box1.body = "";
      box1.at = 0;
    }
    if (ht1 != null) {
      box2.body = "..." + (String) ht1.sentences.get(bc1) + "...";
      box2.at = 255;
      bc1 ++;
      if (bc1 >= ht1.sentences.size()) bc1 = 0;
    }
    else {
      box2.body = "";
      box2.at = 0;
    }

  };
  
  boolean mouseCheck() {
    boolean c = false;
    if (mouse.distanceToSquared(pos) < 10000) {
     c = (mouse.x > pos.x && mouse.y > pos.y -3 - ts/2 && mouse.x < pos.x + w && mouse.y < pos.y -3 - ts/2 + h);
    };
    return(c);
  };

  void mouseEvent(MouseEvent event) {
    // get the x and y pos of the event
    int x = event.getX();
    int y = event.getY();

    switch (event.getID()) {

    case MouseEvent.MOUSE_RELEASED:
      if (mouseCheck()) doClick();
      break;
    }
  }

};



