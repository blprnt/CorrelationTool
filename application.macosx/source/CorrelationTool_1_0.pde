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

import processing.video.*;
import processing.opengl.*;

import toxi.util.datatypes.*;
import toxi.math.noise.*;
import toxi.math.waves.*;
import toxi.geom.*;
import toxi.math.*;
import toxi.math.conversion.*;
import toxi.geom.mesh.*;

//Articles are loaded into Article objects, which in turn handle the indexing of words and phrases
Article a1;
Article a2;

//***** ------------- Text files & titles & dates & colours are defined here 
String title1 = "Tokyo";
String url1 = "asia.txt";
String desc1 = "Suntory Hall";
String date1 = "November 14th, 2009";
color color1 = #140047;

String title2 = "Cairo";
String url2 = "cairo.txt";
String desc2 = "Cairo University";
String date2 = "June 4th, 2009";
color color2 = #680014;

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

void setup() {
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

void setFont() {
  //** Font cycling code below is disabled in favour of the default Helveticaness
  /*
  fontc ++;
   label = createFont(fontList[fontc], 48);
   println(fontc + ":" + fontList[fontc]);
   //*/
  label = createFont("HelveticaNeue", 48);
};

void draw() {
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
HashToken updateHash(HashMap h, String s) {
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
String cleanBody(String msg) {
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

String clean(String msg) {

  msg = trim(msg);

  msg = msg.replaceAll("\"", "");
  msg = msg.replaceAll("\\.", "");
  msg = msg.replaceAll(",", "");
  msg = msg.replaceAll("//?", "");
  msg = msg.replaceAll("//!", "");
  msg = msg.replaceAll("”","");
  msg = msg.replaceAll("“","");
  msg = msg.replaceAll("’s","");

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

boolean checkWord(String s) {
  s = trim(s.toLowerCase());
  //***** ------------- BAD WORDS ---------- ***************
  String[] outs = {
    "and","if","a","the","of","to","in","that","was","i","he","is","his","on","had","with",
    "for","you","an","at","are","have","as","it","one","were","by","from","back","out","who"
      ,"all","this","be","up","so","been","has","she","her","not","would","about","but","like","said",
    "just","when","what","me","they","it's","other","it’s","see","into","which","because","over","than",
    "two","can","there","or","them","got","only","don’t","their","no","day","could","says","same","more","after",
    "went","we","did","why","then","my","how","now","do","get","i'm","i’m","off","its","each","own","will","say","he's",
    "he’s","some","him","much","way","go","those","any","even","times","never","that's","that’s","your","new","most","saw","days","really","put",
    "where","every","around","away","very","still","here","came","he’d","he'd","told","right","take","sent","seen","you're","you’re","mike",
    "does","kind","look","going","found","think","know","man","guy","these","took","long","himself","well","-","—","our"
  };
  boolean safe = true;
  for (int i = 0; i < outs.length; i++) {
    if (s.equals(outs[i]) || s.length() == 1) {
      safe = false;
    }
  }; 
  return(safe);
};

void keyPressed() {
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

void mousePressed() {
  tpadding = 100;
  clicked = true;
};

