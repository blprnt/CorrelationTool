/*

 ExcerptBlock Class
 - Each loaded text is stored as an Article object.
 - Word, phrase, and sentence collections are stored locally for each object.
 - Global word, phrase and sentence collections are also updated when Articles are processed.

*/

public class ExcerptBlock {
  
  String body = "This is a test of the blah bleedy dah blah system.";
  
  Vec2D pos = new Vec2D();
  color c;
  
  float a = 0;      //Alpha value of the text
  float at = 0;     //Target alpha value of the text
  
  ExcerptBlock() {
    
  };
  
  void render() {
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
