import processing.core.*; 
import processing.xml.*; 

import processing.video.*; 
import processing.opengl.*; 
import toxi.util.datatypes.*; 
import toxi.math.noise.*; 
import toxi.math.waves.*; 
import toxi.geom.*; 
import toxi.math.*; 
import toxi.math.conversion.*; 
import toxi.geom.mesh.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class CorrelationTool_1_0 extends PApplet {

/*

 Article Comparison Tool
 blprnt@blprnt.com
 Released: November, 2010
 
 Important stuff:
 -Text files & titles are defined immediately below
 -The "bad list" of words to ignore are defined on this tab, near the bottom in the check words function
 
 Dependencies:
 -This project requires the toxiclibs library, which you can downloaded here: http://hg.postspectacular.com/toxiclibs/downloads
 */












//Articles are loaded into Article objects, which in turn handle the indexing of words and phrases
Article a1;
Article a2;

//***** ------------- Text files & titles & dates & colours are defined here 
String title1 = "Tokyo";
String url1 = "asia.txt";
String desc1 = "Suntory Hall";
String date1 = "November 14th, 2009";
int color1 = 0xff140047;

String title2 = "Cairo";
String url2 = "cairo.txt";
String desc2 = "Cairo University";
String date2 = "June 4th, 2009";
int color2 = 0xff680014;

//The tool can also recognize and compare phrases of an arbitray length.
//This isn't a feature that I found useful, but I've left it in here just in case.
//Check out the Article.process() method for some more information about how this works.
int phraseLength = 3;

//Multiple HashTokens can be selected for comparison. This ArrayList holds those words
ArrayList focusTokens = new ArrayList();
//This is a list of all of the visible HashTokens on the screen
ArrayList visibleTokens = new ArrayList();

//MovieMaker used to output video.
//To record your own video, just use the 'm' key to toggle recording on and off.
MovieMaker mm;                             
boolean recording = false;

//These are the global hashes of words, phrases and sentences.
//These hold HashTokens, indexed by the string of the word, phrase, or sentence
HashMap wordHash = new HashMap();
HashMap phraseHash = new HashMap();
HashMap sentenceHash = new HashMap();

//These are the global lists of words, phrases, and sentences.
//These hold HashTokens, indexed by order of occurence. We eventually sort these by each HashToken's i value
ArrayList words = new ArrayList();
ArrayList phrases = new ArrayList();
ArrayList sentences = new ArrayList();

//Whis HashToken is currently focused?
HashToken focusToken = null;
//Where is the mouse? Used for hacky rollovers.
Vec2D mouse;

//These are the blocks at right and left that display text excerpts.
ExcerptBlock box1 = new ExcerptBlock();
ExcerptBlock box2 = new ExcerptBlock();

//Has a HashToken been clicked yet?
boolean clicked = false;

//Display font
PFont label;

//Left & right side padding
float padding = 640;
float tpadding = 500;

//A reference to the main app for the various classes to use.
PApplet app = this;

//This toggles a dynamic lens scaling effect.
//Can be toggled in-app with the 'z' key.
boolean scaling = false;

//Holds a list of available fonts on the system. Used for toggling through fonts, which is currently disabled.
//Really, it was a quick way for me to run through by fonts and pick something I thought looked good.
String[] fontList;
int fontc = 80;

public void setup() {
  size(1280,720,OPENGL);
  hint(ENABLE_OPENGL_4X_SMOOTH);
  background(255);
  smooth();

  //Build, load, and process the Articles
  a1 = new Article();
  a1.load(url1);
  a1.process();

  a2 = new Article();
  a2.load(url2);
  a2.process();

  //Position the excerpt boxes
  box1.pos.x = 75;
  box1.pos.y = 260;
  box1.c = color(71,53,29);

  box2.pos.x = (width - 350) + 75;
  box2.pos.y = 260;
  box2.c = color(150,12,7);

  //Sort arrays
  Collections.sort(words, (Comparator) new SizeComparator());
  Collections.sort(sentences, (Comparator) new SizeComparator());
  Collections.sort(phrases, (Comparator) new SizeComparator());
  Collections.reverse(words);
  Collections.reverse(sentences);
  Collections.reverse(phrases);

  //Print out a list of the top 10 3-word phrases in the texts, for shits and giggles.
  for (int i = 0; i < 10; i++) {
    HashToken ht = (HashToken) phrases.get(i);
    println(ht.i + ":" + ht.s);
  };

  //Construct the font
  fontList = PFont.list();
  setFont();

  //Set the display font & size 
  textFont(label);
  textSize(12);

  //Put 100 of the top words into the visibleTokens ArrayList
  //These are the tokens that render to screen.
  for (int i = 0; i < 100; i++) {
    HashToken ht = (HashToken) words.get(i);
    visibleTokens.add(ht);
    ht.init(a1,a2);
  };

  //Reverse the visibleTokens list - this puts the biggest words at the top when we render
  Collections.reverse(visibleTokens);
};

public void setFont() {
  //** Font cycling code below is disabled in favour of the default Helveticaness
  /*
  fontc ++;
   label = createFont(fontList[fontc], 48);
   println(fontc + ":" + fontList[fontc]);
   //*/
  label = createFont("HelveticaNeue", 48);
};

public void draw() {
  //Store the mouse position in a vector to use for distance calculations
  mouse = new Vec2D(mouseX, mouseY);
  background(255);

  //Draw edge lines
  stroke(240);
  line(padding,0,padding,height);
  line(width - padding, 0, width - padding, height);

  //Article title & date labels
  fill(240);
  rect(0,0,padding,height);
  fill(200);
  rect(padding-40,0,40,height);
  pushMatrix();
    translate(padding - 36,10);
    rotate(PI/2);
    textSize(24);
    textFont(label);
    fill(color2);
    text(title2,0,0);
    float tw1 = textWidth(title2);

    translate(0,16);
    textSize(12);
    fill(red(color2),green(color2),blue(color2),100);
    text(desc2,0,0);
  
    translate(0,16);
    textSize(12);
    text(date2,5,0);

  popMatrix();

  fill(220);
  rect(width - padding,0,padding,height);
  fill(240);
  rect(width - padding,0,40,height);
  pushMatrix();
    translate((width - padding) + 36,height - 10);
    rotate(-PI/2);
    textFont(label);
    textSize(48);

    fill(color1);
    text(title1,0,0);

    translate(0,16);
    textSize(12);
    fill(red(color1),green(color1),blue(color1),100);
    text(desc1,0,0);

    translate(0,16);
    textSize(12);
    text(date1,5,0);

  popMatrix();

  //Textexceprt boxes are always rendered after the original click - they are just sometimes offscreen.
  if (clicked) {
    box1.render();
    box2.render();
  };

  if (clicked) {
    //Left/right padding changes when a word is slected
    tpadding = (focusTokens.size() == 0) ? (100):(350);
  }

  //Padding is eased for nice animations
  padding += (float) (tpadding - padding)/10;
  
  //Finally, all of the visible HashTokens are rendered.
  for (int i = 0; i < visibleTokens.size(); i++) {
    HashToken ht = (HashToken) visibleTokens.get(i);
    ht.render();
  };

  //Add a frame to the MovieMaker if we're recording.
  if (recording) mm.addFrame();
};

//Global function used to update a hash with a string value.
//If there isn't yet a token for that value, we make one.
//After that, the token's i value is incremented every time a string is updated.
//This is the central method for tallying word counts
public HashToken updateHash(HashMap h, String s) {
  if (!h.containsKey(s)) {
    h.put(s, new HashToken());
  }
  HashToken ht = (HashToken) h.get(s);
  ht.i ++;
  ht.s = s;
  return(ht);
};

//I think this is the messiest part of the system right now.
//I wrote this before I had any understanding of regular expressions and it's a real hack.
//It also takes a long time to process the text.
//But it works. Kind of. 

//***** IT'S VERY IMPORTANT TO NOTE that you'll probably have to tweak the cleanBody and clean functions to work for your individual bodies of text.
//Maybe in some magical future where I have lots of free time and money I'll come back to this and build a smarter way of managing it all.
public String cleanBody(String msg) {
  msg = trim(msg);
  msg = msg.replaceAll("N.F.L.", "NFL");
  msg = msg.replaceAll("C.T.E", "CTE");
  msg = msg.replaceAll("Dr.", "Doctor");

  String[] msgs = split(msg, " ");
  String r = "";
  for (int i = 0; i < msgs.length; i ++) {
    Boolean chk = true;
    String t = msgs[i];
    if (match(t, "@") != null) {
      chk = false;
    }
    if (chk) r += t + " ";
  };

  try {
    r = URLDecoder.decode(r,"UTF-16");
  }
  catch (Exception e) {
  };
  return(r);
};

public String clean(String msg) {

  msg = trim(msg);

  msg = msg.replaceAll("\"", "");
  msg = msg.replaceAll("\\.", "");
  msg = msg.replaceAll(",", "");
  msg = msg.replaceAll("//?", "");
  msg = msg.replaceAll("//!", "");
  msg = msg.replaceAll("\u201d","");
  msg = msg.replaceAll("\u201c","");
  msg = msg.replaceAll("\u2019s","");

  String[] msgs = split(msg, " ");
  String r = "";
  for (int i = 0; i < msgs.length; i ++) {
    Boolean chk = true;
    String t = msgs[i];
    if (match(t, "@") != null) {
      chk = false;
    }
    if (chk) r += t + " ";
  };

  try {
    r = URLDecoder.decode(r,"UTF-16");
  }
  catch (Exception e) {
  };
  return(r);
};

public boolean checkWord(String s) {
  s = trim(s.toLowerCase());
  //***** ------------- BAD WORDS ---------- ***************
  String[] outs = {
    "and","if","a","the","of","to","in","that","was","i","he","is","his","on","had","with",
    "for","you","an","at","are","have","as","it","one","were","by","from","back","out","who"
      ,"all","this","be","up","so","been","has","she","her","not","would","about","but","like","said",
    "just","when","what","me","they","it's","other","it\u2019s","see","into","which","because","over","than",
    "two","can","there","or","them","got","only","don\u2019t","their","no","day","could","says","same","more","after",
    "went","we","did","why","then","my","how","now","do","get","i'm","i\u2019m","off","its","each","own","will","say","he's",
    "he\u2019s","some","him","much","way","go","those","any","even","times","never","that's","that\u2019s","your","new","most","saw","days","really","put",
    "where","every","around","away","very","still","here","came","he\u2019d","he'd","told","right","take","sent","seen","you're","you\u2019re","mike",
    "does","kind","look","going","found","think","know","man","guy","these","took","long","himself","well","-","\u2014","our"
  };
  boolean safe = true;
  for (int i = 0; i < outs.length; i++) {
    if (s.equals(outs[i]) || s.length() == 1) {
      safe = false;
    }
  }; 
  return(safe);
};

public void keyPressed() {
  if (key == 's') {
    println("SAVE");
    save(title1 + "_" + title2 + hour() + "_" + minute() + "_" + second() + ".png");
  }
  else if (key == 'r') {
    println("RESET");
    tpadding = 500;
  }
  else if (key == 'z') {
    scaling = !scaling;
  }
  else if (key == 'f') {
    setFont();
  }
  else if (key == 'm') {
    if (!recording) {
      mm = new MovieMaker(this, width, height, "Richmond" + day() + "_" + hour() + "_" + minute() + ".mov",
      30, MovieMaker.JPEG, MovieMaker.HIGH);
    };
    recording = !recording;
    if (!recording) {
      mm.finish();
    };
  }
};

public void mousePressed() {
  tpadding = 100;
  clicked = true;
};

/*

 Article Class
 - Each loaded text is stored as an Article object.
 - Word, phrase, and sentence collections are stored locally for each object.
 - Global word, phrase and sentence collections are also updated when Articles are processed.

*/


class Article {

  //This is the text of the article
  String body;

  //These are the local hashes of words, phrases and sentences.
  //These hold HashTokens, indexed by the string of the word, phrase, or sentence
  HashMap lwordHash = new HashMap();
  HashMap lphraseHash = new HashMap();
  HashMap lsentenceHash = new HashMap();

  //These are the local lists of words, phrases, and sentences.
  //These hold HashTokens, indexed by order of occurence. We eventually sort these by each HashToken's i value
  ArrayList lwords = new ArrayList();
  ArrayList lphrases = new ArrayList();
  ArrayList lsentences = new ArrayList();
  
  //Total wordcount of the article. Used to normalize word counts in comparison.
  int wordcount;

  Article() {

  };

  public void load(String url) {
    //Load the text from a file
    body = join(loadStrings(url), " ");
  };

  public void process() {
    println("PROCESS");
    //Make a first cleaning pass to remove common acronyms, etc.
    body = cleanBody(body);
    
    //Make an array of words, and set the wordcount.
    String[] fwa = splitTokens(body, " ");
    wordcount = fwa.length;
    int wc = 0;

    //Process sentences
    String[] sa = splitTokens(body, "\\.?!");//body.split(". ");
    for (int i = 0; i < sa.length; i++) {
      String s = sa[i];
      s = clean(s);
      HashToken lht = updateHash(lsentenceHash, s);
      HashToken ht = updateHash(sentenceHash, s);
      if (lht.i == 1) lsentences.add(lht);
      if (ht.i == 1) sentences.add(ht);
      String[] wa = splitTokens(s, " ");
      for (int j = 0; j < wa.length; j++) {
        String w = wa[j];
        w = clean(w);
        w = w.toLowerCase();
        wc ++;
        if (checkWord(w)) {
            HashToken lwht = updateHash(lwordHash, w);
            lwht.sentences.add(sa[i]);
            HashToken wht = updateHash(wordHash, w);
            wht.sentences.add(sa[i]);
            lwht.locArray.add(new LocationToken((float) wc/wordcount));
            if (lwht.i == 1) lwords.add(lwht);
            if (wht.i == 1) words.add(wht);
        };
      };
    };
    
    /*
    //Process words - This was an older more complex take at handling the individual words
    String[] wa = splitTokens(body, " ");//body.split(". ");
    wordcount = wa.length;
    for (int i = 0; i < wa.length; i++) {
      String w = wa[i];
      w = clean(w);
      w = w.toLowerCase();
      if (checkWord(w)) {
        HashToken lht = updateHash(lwordHash, w);
        HashToken ht = updateHash(wordHash, w);
        lht.locArray.add(new LocationToken((float) i/wa.length));
        if (lht.i == 1) lwords.add(lht);
        if (ht.i == 1) words.add(ht);
      };
    };
    */
    
    //Process Phrases
    int pl = phraseLength;
    for (int i = 0; i < sa.length; i++) {
      String s = clean(sa[i]);
      String[] swa = splitTokens(s, " ");
      if (swa.length >= pl) {
      for (int j = 0; j < swa.length - pl; j++) {
        String p = "";
        for (int k = 0; k < pl; k++) {
          p = p + " " + swa[j + k];
        };
        HashToken lht = updateHash(lphraseHash, p);
        HashToken ht = updateHash(phraseHash, p);
        if (lht.i == 1) lphrases.add(lht);
        if (ht.i == 1) phrases.add(ht);
      };
      };
    };

    //Sort arrays - all of the local ArrayLists of HashTokens are sorted by their i values.
    Collections.sort(lwords, (Comparator) new SizeComparator());
    Collections.sort(lsentences, (Comparator) new SizeComparator());
    Collections.reverse(lwords);
    Collections.reverse(lsentences);

  };

};





/*

 ExcerptBlock Class
 - Each loaded text is stored as an Article object.
 - Word, phrase, and sentence collections are stored locally for each object.
 - Global word, phrase and sentence collections are also updated when Articles are processed.

*/

public class ExcerptBlock {
  
  String body = "This is a test of the blah bleedy dah blah system.";
  
  Vec2D pos = new Vec2D();
  int c;
  
  float a = 0;      //Alpha value of the text
  float at = 0;     //Target alpha value of the text
  
  ExcerptBlock() {
    
  };
  
  public void render() {
    if (padding > 100) a += (at - a)/10;
    if (a > 10) {
    pushMatrix();
      translate(pos.x,pos.y);
      textFont(label);
      textSize(14);
      fill(red(c),green(c),blue(c),a);
      text(body, 0, 0, 200, a);
    popMatrix();
    };
  };
  
};
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
  
  int c1;                                 //Seperate colours, again one for each article
  int c2;
  
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

  public void init(Article a1, Article a2) {
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

  public void setPos() {
    //Sets the x position of this token depending on the balance between the two articles.
    float one = (ht1 == null) ? (0):((float)ht1.i);
    float f = (one/t);
    if (f == 0) f = 0.02f;
    if (f == 1) f = 0.95f;
    tpos.x = padding + (f * (width - (2 * padding)));
    pos.interpolateToSelf(tpos, 0.1f);
  };

  public void render() {
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
    taa = (fck) ? (1):(-0.1f);
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
    if (focusTokens.contains(this)) ts *= 1.5f;// = max(ts,20);
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
        scale(max(0.4f,sc));
      }
      else {
        sc = (fck) ? (1):(0);
      };
      
      fill(min(255,230 + i),200 * (aa + sc));
      if ((mouseCheck() && !focusTokens.contains(this)) || focusTokens.contains(this)) fill(0xffFFB52A);
      stroke(0,10);
      w = tsize + 6;
      h = ts + 6;
      rect(-3,-3 - ts/2,tsize + 6, ts + 6); 
      fill(0,10 + (i * 5) * (aa + sc));
     
      text(s, 2, ts/3);

    popMatrix();
    };

  };

  public void doClick() {
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
  
  public void setBoxes() {
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
  
  public boolean mouseCheck() {
    boolean c = false;
    if (mouse.distanceToSquared(pos) < 10000) {
     c = (mouse.x > pos.x && mouse.y > pos.y -3 - ts/2 && mouse.x < pos.x + w && mouse.y < pos.y -3 - ts/2 + h);
    };
    return(c);
  };

  public void mouseEvent(MouseEvent event) {
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



/*

 Location Tokens
 - These little guys just hold one value - where the string they are associated appeared in an article.

*/


class LocationToken {
  
  float l;
  LocationToken(float loc) { 
    l = loc;
   };
  
};
/*

 Size Comparator
 - Sorts a collection of HashTokens by their total counts.

*/


class SizeComparator implements Comparator {
  
  public int compare(Object o1, Object o2) {
    //Comparators expect Objects, so we immediatelyhave to cast them into HashTokens to access the required properties
    HashToken l1 = (HashToken) o1;
    HashToken l2 = (HashToken) o2;
    
    float f1 = l1.i;
    float f2 = l2.i;
    
    int r = 0;
    if (f1 > f2) {
      r = 1;
    }
    else if (f1 < f2) {
      r = -1;
    }
    return(r);
  };
  
};
  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--hide-stop", "CorrelationTool_1_0" });
  }
}
