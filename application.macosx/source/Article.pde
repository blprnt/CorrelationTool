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

  void load(String url) {
    //Load the text from a file
    body = join(loadStrings(url), " ");
  };

  void process() {
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





