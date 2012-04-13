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
