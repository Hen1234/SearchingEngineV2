package Model;

public class main {

    public static void main(String[] args) {

        Parse p = new Parse();
        String query = "I want 55 Billion";
        String afterParse = p.parser(null,query,true, true);
        System.out.println(afterParse);

    }







}
