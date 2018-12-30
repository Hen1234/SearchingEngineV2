package Model;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.nio.file.Files;
import java.nio.file.Paths;

public class main {

    public static void main(String[] args) {

        temp();
    }
    public static boolean temp(){

        String str = "hen...osher";
        String beforeDots="";
        String afterDots="";
        for (int i = 0; i <str.length() ; i++) {



            if(!(str.charAt(i)=='.')) {
                beforeDots = beforeDots + str.charAt(i);
            }
            else{
                if(i+1<str.length() && str.charAt(i+1)=='.') {
                    if (i + 2 < str.length() && str.charAt(i + 2) == '.') {
                        for (int j = i + 3; j < str.length(); j++) {
                            afterDots = afterDots + str.charAt(j);
                        }
//                        addTheDictionary(beforeDots, currentDoc, position);
//                        addTheDictionary(afterDots, currentDoc, position);
                        return true;
                    }
                }

            }
        }
        return false;

    }


}