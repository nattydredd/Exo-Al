package utilities;

// @author Nate

import java.util.Comparator;

//Comparator for sorting light curve file names in descending order
public class LightCurveComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        
        //Extracts number after 'Q' from light curve file name in format XXXXX_QX.csv
        Integer val1 = Integer.valueOf(s1.split("Q")[1].split("\\.")[0]);
        Integer val2 = Integer.valueOf(s2.split("Q")[1].split("\\.")[0]);
        
        return val1 - val2;
    }

}//End LightCurveComparator
