package Model;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.nio.file.Files;
import java.nio.file.Paths;

public class main {

    public static void main(String[] args) {

        dashCase();
    }

    private static void dashCase() {

        String s="hen-";
        String newString="";
        if(s.charAt(s.length()-1)=='-'){

            newString = s.substring(0,s.length()-1);



        }else{

        }
        System.out.println(newString);


    }


}