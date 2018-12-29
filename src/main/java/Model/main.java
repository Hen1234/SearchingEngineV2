package Model;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.nio.file.Files;
import java.nio.file.Paths;

public class main {

    public static void main(String[] args) {

//        Parse p = new Parse();
//        String query = "I want 55 Billion";
//        String afterParse = p.parser(null,query,true, true);
        //System.out.println(afterParse);
//        String f = "Falkland";
//        String f1 = "FALKLAND";
//        System.out.println(f1.equals(f.toUpperCase()));
        //System.out.println(Math.log10(2/3));

        String lineFromFile = "";
        try {
            //doc:FBIS3-29#2=27066 ,27079 doc:FBIS3-5232#1=481 DF- 2 TIC- 3
            lineFromFile = "doc:FBIS3-29#2=27066 ,27079 doc:FBIS3-5232#1=481 DF- 2 TIC- 3";
        } catch (Exception e) {
        }

        //ArrayList<String> docs = new ArrayList<>();
        //ArrayList<Integer> amountsPerDoc = new ArrayList<>();
        String docNo = "";
        String tfString = "";
        String locations = "";

        //update the hashMap of docs and df of the currentQueryTerm
        for (int k = 0; k < lineFromFile.length(); k++) {

            docNo = "";
            tfString = "";
            if (lineFromFile.charAt(k) == ':') {
                k++;

                //find the doc
                while (lineFromFile.charAt(k) != '#') {
                    docNo = docNo + lineFromFile.charAt(k);
                    k++;
                }
                k++;

                //find the amountAppearence in the doc
                while (lineFromFile.charAt(k) != '=') {
                    tfString = tfString + lineFromFile.charAt(k);
                    k++;
                }

                //find location in the doc
                k++;
                while (lineFromFile.charAt(k) != 'd') {
                    locations = locations + lineFromFile.charAt(k);
                    k++;
                }

                String[] locations1 = locations.split(" ");
                for (int i = 0; i < locations1.length; i++) {
                    for (int j = 0; j < locations1[i].length(); j++) {
                        if (locations1[i].charAt(j) == ',') {
                            locations1[i] = locations1[i].substring(i, locations1[i].length());
                            break;

                        }

                    }

                }


            }


        }
    }
}