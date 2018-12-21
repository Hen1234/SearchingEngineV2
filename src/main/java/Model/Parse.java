package Model;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * The class is responsible of dismantling each doc to terms.
 * it gets each doc from the "ReadFile" class
 */
public class Parse {

    //tempDictionary for each doc
    private HashSet<Terms> tempDictionary;
    //helper HashMap for replacement chars
    private HashMap<String, String> replacements;
    //helper HashMap for holding the terms follow the numbers
    private HashSet<String> numTerms;
    //HashSet of the stopWords
    private HashSet<String> stopWords;
    //HashMap for the months
    private HashMap<String, String> months;
    //a Stemmer object
    private Stemmer stemmer;
    //boolean- toStemming or not
    private boolean toStem;
    //HashSet of Language
    private HashSet<String> Languages;
    //HashSet of cities
    private HashMap<String, City> cities;

    // Hashmap that hold as key - city name and as object the rqual city object
    public HashMap<String, City> getCities() {
        return cities;
    }

    /**
     * Constructor- initialize the fields and call the initial methods
     */
    public Parse() {

        replacements = new HashMap<String, String>();
        stemmer = new Stemmer();
        stopWords = new HashSet<String>();
        months = new HashMap<String, String>();
        numTerms = new HashSet<String>();
        tempDictionary = new HashSet<Terms>();
        toStem = false;
        Languages = new HashSet<String>();
        cities = new HashMap<String, City>();

        insertMonths();
        initialreplacment();
        insertNumTerms();
    }

    /**
     * Getter
     *
     * @return
     */
    public HashSet<String> getLanguages() {
        return Languages;
    }

    /**
     * Getter
     *
     * @return the tempDictionary
     */
    public HashSet<Terms> getTempDictionary() {
        return tempDictionary;
    }

    /**
     * Setter
     *
     * @param toStem
     */
    public void setToStem(boolean toStem) {
        this.toStem = toStem;
    }

    /**
     * Setter for the tempDictionary
     *
     * @param tempDictionary
     */
    public void setTempDictionary(HashSet<Terms> tempDictionary) {
        this.tempDictionary = tempDictionary;
    }

    /**
     * The parse method- parser each term
     *
     * @param doc- the current doc
     * @param t-   the text of the current doc
     */
    public String parser(Docs doc, String t, boolean toStem, boolean isQuery) {

        StringBuilder termsOfQuery = new StringBuilder("");
        //add the city to the HashMap
        if ((doc != null && doc.getCity() != null && (!cities.containsKey(doc.getCity())) && (!cities.containsKey(doc.getCity().toUpperCase())))) {

            cities.put(doc.getCity(), new City(doc.getCity()));
        }
        //add the language of the doc to the Languages HashSet
        if (Languages != null && doc != null) {
            Languages.add(doc.getLanguage());
        }


        setToStem(toStem);
        String[] text = t.split(" ");
        for (int i = 0; i < text.length; i++) {
            if (isStopWord(text[i]))
                continue;

            if (text[i].length() > 0 && (text[i].charAt(0) == '/' || text[i].charAt(0) == '|'
                    || text[i].charAt(0) == '-' || text[i].charAt(0) == '\''
                    || text[i].charAt(0) == '`' || text[i].charAt(0) == '/'
                    || text[i].charAt(0) == ' ')) {
                String noDashes = removeDashes(text[i]);
                if (noDashes != null) {
                    text[i] = noDashes;
                } else
                    continue;
            }

            // between 18 and 24
            if (i + 3 < text.length && text[i].equals("between") && isNumericStart(text[i + 1])
                    && text[i + 2].equals("and") && isNumericStart(text[i + 3])) {
                if (isStopWord(text[i + 1]) || isStopWord(text[i + 2]) || isStopWord(text[i + 3]))
                    continue;
                if (!isQuery) {  //if is not query
                    addTheDictionary(text[i] + " " + text[i + 1] + " " + text[i + 2] + " " + text[i + 3], doc, i);
                    addTheDictionary(text[i + 1] + "-" + text[i + 3], doc, i);
                    addTheDictionary(text[i + 1], doc, i);
                    addTheDictionary(text[i + 3], doc, i);

                } else {    //if is query
                    if (toStem){
                        termsOfQuery.append(stemmer.stemming(text[i] + " " + text[i + 1] + " " + text[i + 2] + " " + text[i + 3] + " "));
                        termsOfQuery.append(stemmer.stemming(text[i + 1] + "-" + text[i + 3] + " "));
                        termsOfQuery.append(stemmer.stemming(text[i + 1] + " "));
                        termsOfQuery.append(stemmer.stemming(text[i + 3] + " "));
                    }
                    else {
                        termsOfQuery.append(text[i] + " " + text[i + 1] + " " + text[i + 2] + " " + text[i + 3] + " ");
                        termsOfQuery.append(text[i + 1] + "-" + text[i + 3] + " ");
                        termsOfQuery.append(text[i + 1] + " ");
                        termsOfQuery.append(text[i + 3] + " ");
                    }

                }
                i = i + 3;
                continue;

            }
            if (isContainDash(text[i])) {
                if (!isQuery) {
                    addTheDictionary(text[i], doc, i);
                } else {
                    if (toStem){
                        termsOfQuery.append(text[i]+" ");
                    } else
                        termsOfQuery.append(stemmer.stemming(text[i]+" "));
                }

                continue;
            }
            if (isNumericStart(text[i])) {

                //text.set(i, replaceOby0(text.get(i))); /// replace O by 0
                text[i] = replaceOby0(text[i]); /// replace O by 0
                if (isUSDollars(text, i)) { // 320 million U.S. dollars
                    if (!isQuery) {
                        addTheDictionary(usDollarsConvert(text[i], text[i + 1]), doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(usDollarsConvert(text[i], text[i + 1])+" "));
                        }else
                            termsOfQuery.append(usDollarsConvert(text[i], text[i + 1])+" ");
                    }
                    i = i + 3;
                    continue;
                }

                if (i + 1 < text.length && bn(text[i + 1])) { // 100 bn Dollars
                    if (i + 2 < text.length && text[i + 2].equals("Dollars")) {
                        if (isStopWord(text[i + 1]) || isStopWord(text[i + 2]))
                            continue;
                        if (!isQuery) {
                            addTheDictionary(text[i] + "000" + " M Dollars", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(text[i] + "000" + " M Dollars "));
                            }
                                else
                                    termsOfQuery.append(text[i] + "000" + " M Dollars ");
                        }

                        //terms.add(doc.getText().get(i) + "000" + " M Dollars");
                        i = i + 2;
                        continue;
                    }
                }
                if (i + 1 < text.length && m(text[i + 1])) { // 20.6 m Dollars
                    if (i + 2 < text.length && text[i + 2].equals("Dollars")) {
                        if (isStopWord(text[i + 1]) || isStopWord(text[i + 2]))
                            continue;
                        if (!isQuery) {
                            addTheDictionary(text[i] + " M Dollars", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(text[i] + " M Dollars "));
                            }else
                                termsOfQuery.append(text[i] + " M Dollars ");
                        }

                        //terms.add(doc.getText().get(i) + " M Dollars");
                        i = i + 2;
                        continue;
                    }
                }
                if (i + 1 < text.length && (i + 2 < text.length) && isFruction(text[i + 1]) //22 3/4 Dollars
                        && text[i + 2].equals("Dollars")) {
                    if (isStopWord(text[i + 1]) || isStopWord(text[i + 2]))
                        continue;
                    //terms.add(doc.getText().get(i) + " " + doc.getText().get(i + 1) + " " + doc.getText().get(i + 2));
                    if (!isQuery) {
                        addTheDictionary(text[i] + " " + text[i + 1] + " " + text[i + 2], doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(text[i] + " " + text[i + 1] + " " + text[i + 2]+" "));
                        }else
                            termsOfQuery.append(text[i] + " " + text[i + 1] + " " + text[i + 2]+" ");
                    }

                    i = i + 2;
                    continue;
                }
                Double tmp = 0.0;


                if (i + 1 < text.length && text[i + 1].equals("Dollars")) { // 1,000,000 Dollars
                    if (greaterThanMillion(text[i])) {
                        if (isStopWord(text[i + 1]))
                            continue;
                        tmp = fromStringToDouble(text[i]) / 1000000;
                        if (isContainDot(tmp.toString())) { //////// string contain dot
                            int j = tmp.intValue();
                            if (!isQuery) {
                                addTheDictionary(j + " M Dollars", doc, i);
                            } else {
                                if (toStem){
                                    termsOfQuery.append(stemmer.stemming(j + " M Dollars "));
                                }else
                                    termsOfQuery.append(j + " M Dollars ");
                            }

                            i = i + 1;
                            continue;
                        }

                        if (!isQuery) {
                            addTheDictionary(Double.toString(tmp) + " M Dollars", doc, i);
                        } else {
                            try {
                                if (toStem) {
                                    termsOfQuery.append(stemmer.stemming(Double.toString(tmp) + " M Dollars "));
                                } else
                                    termsOfQuery.append(Double.toString(tmp) + " M Dollars ");
                            }
                                catch (Exception e){}
                        }

                        i = i + 1;
                        continue;
                    } else { //1.7320 Dollars
                        //terms.add(doc.getText().get(i) + " Dollars");
                        if (!isQuery) {
                            addTheDictionary(text[i] + " Dollars", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(text[i] + " Dollars "));
                            }else
                            termsOfQuery.append(text[i] + " Dollars ");
                        }

                        i = i + 1;
                        continue;
                    }
                }


                if (i + 1 < text.length && isInMonth(text[i + 1])) {  //14 MAY, 14 May
                    //terms.add(months.get(doc.getText().get(i + 1)) + "-" + doc.getText().get(i));
                    if (text[i].length() == 1)
                        if (isStopWord(text[i + 1]))
                            continue;
                    if (!isQuery) {
                        addTheDictionary(months.get(text[i + 1]) + "-0" + text[i], doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(months.get(text[i + 1]) + "-0" + text[i]+" "));
                        }else
                            termsOfQuery.append(months.get(text[i + 1]) + "-0" + text[i]+" ");
                    }

                    //else addTheDictionary(months.get(text[i + 1]) + "-" + text[i], doc);
                    i = i + 1;
                    continue;
                }

                if (i + 1 < text.length && isInNumTerms(text[i + 1])) {
                    if (text[i + 1].equals("Million")) {
                        if (isStopWord(text[i + 1]))
                            continue;
                        //terms.add(doc.getText().get(i) + "M");
                        if (!isQuery) {
                            addTheDictionary(text[i] + "M", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(text[i] + "M "));
                            }else
                            termsOfQuery.append(text[i] + "M ");
                        }

                        i++;
                        continue;
                    }
                }
                if (i + 1 < text.length && isInNumTerms(text[i + 1])) {
                    if (text[i + 1].equals("Billion")) {
                        if (isStopWord(text[i + 1]))
                            continue;
                        if (!isQuery) {
                            addTheDictionary(text[i] + "B", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(text[i] + "B "));
                            }else
                                termsOfQuery.append(text[i] + "B ");
                        }

                        i++;
                        continue;
                    }
                }
                if (i + 1 < text.length && isInNumTerms(text[i + 1])) {
                    if (text[i + 1].equals("trillion") || text[i + 1].equals("Trillion")) {
                        if (isStopWord(text[i + 1]))
                            continue;
                        if (!isQuery) {
                            addTheDictionary(text[i] + "00B", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(text[i] + "00B "));
                            }else
                                termsOfQuery.append(text[i] + "00B ");
                        }

                        i++;
                        continue;
                    }
                }
                if (i + 1 < text.length && isInNumTerms(text[i + 1])) {
                    if (text[i + 1].equals("Thousand")) {
                        if (isStopWord(text[i + 1]))
                            continue;
                        if (!isQuery) {
                            addTheDictionary(text[i] + "K", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(text[i] + "K "));
                            }else
                                termsOfQuery.append(text[i] + "K ");
                        }

                        i++;
                        continue;
                    }
                }

                // is % end or next word is percentage / percent
                if (text[i].length() > 1 && isNumericStart(text[i]) &&
                        lastCharIsPercents(text[i])) {
                    if (!isQuery) {
                        addTheDictionary(text[i], doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(text[i]+" "));
                        }else
                            termsOfQuery.append(text[i]+" ");
                    }

                    continue;
                }
                if (i + 1 < text.length && isNumericStart(text[i]) &&
                        (text[i + 1].equals("percent") || text[i + 1].equals("percentage"))) {
                    if (isStopWord(text[i + 1]))
                        continue;
                    if (!isQuery) {
                        addTheDictionary(text[i] + "%", doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(text[i] + "% "));
                        }else
                            termsOfQuery.append(text[i] + "% ");
                    }

                    i = i + 1;
                    continue;
                }


                //if(isContainTowDot(doc.getText().get(i)))
                Double num = fromStringToDouble(text[i]);
                if (num >= 1000 && num < 1000000) {
                    num = num / 1000;
                    //terms.add(num.toString() + "K");
                    if (!isQuery) {
                        addTheDictionary(num.toString() + "K", doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(num.toString() + "K "));
                        }else
                            termsOfQuery.append(num.toString() + "K ");
                    }

                    continue;
                }
                if (num >= 1000000 && num < 1000000000) {
                    num = num / 1000000;
                    //terms.add(num.toString() + "M");
                    if (!isQuery) {
                        addTheDictionary(num.toString() + "M", doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(num.toString() + "M "));
                        }else
                            termsOfQuery.append(num.toString() + "M ");
                    }

                    continue;
                }
                if (num > 1000000000) {
                    num = num / 1000000000;
                    //terms.add(num.toString() + "B");
                    if (!isQuery) {
                        addTheDictionary(num.toString() + "B", doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(num.toString() + "B "));
                        }else
                            termsOfQuery.append(num.toString() + "B ");
                    }

                    continue;
                }

            }// isNumeric //

            //begin with month
            if (isInMonth(text[i])) { //start with month
                if (i + 1 < text.length && text[i + 1].length() > 1 && Character.isDigit(text[i + 1].charAt(0)) &&
                        text[i + 1].length() <= 2) {
                    if (isStopWord(text[i + 1]))
                        continue;//June 4, JUNE 4
                    if (text[i + 1].length() == 1) {
                        if (!isQuery) {
                            addTheDictionary(months.get(text[i]) + "-0" + text[i + 1], doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(months.get(text[i]) + "-0" + text[i + 1]+" "));
                            }
                            else
                            termsOfQuery.append(months.get(text[i]) + "-0" + text[i + 1]+" ");
                        }

                    }
                    //else addTheDictionary(months.get(text[i]) + "-" + text[i + 1], doc);
                    //terms.add(months.get(doc.getText().get(i)) + "-" + doc.getText().get(i + 1));
                    i = i + 1;
                    continue;
                }

                if (i + 1 < text.length && text[i + 1].length() >= 1 && Character.isDigit(text[i + 1].charAt(0))) {      //May 1994, MAY 1994

                    //terms.add(doc.getText().get(i + 1) + "-" + months.get(doc.getText().get(i)));
                    if (isStopWord(text[i + 1]))
                        continue;
                    if (!isQuery) {
                        addTheDictionary(text[i + 1] + "-" + months.get(text[i]), doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(text[i + 1] + "-" + months.get(text[i])+" "));
                        }else
                            termsOfQuery.append(text[i + 1] + "-" + months.get(text[i])+" ");
                    }

                    i = i + 1;
                    continue;
                }
            }

            //begin with months//

            // isDollar
            if (isDollar((text[i]))) {
                text[i] = "$" + replaceOby0(text[i].substring(1, text[i].length()));
            }
            if (isDollar((text[i])) && isNumericStart(text[i].substring(1, (text[i].length() - 1)))) {
                if (i + 1 < text.length && text[i + 1].equals("billion")) { //$100 billion:
                    //terms.add(doc.getText().get(i).substring(1, (doc.getText().get(i).length() - 1)) + "000 M Dollars");
                    if (!isQuery) {
                        addTheDictionary(text[i].substring(1, (text[i]).length()) + "000 M Dollars", doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(text[i].substring(1, (text[i]).length()) + "000 M Dollars "));
                        }else
                        termsOfQuery.append(text[i].substring(1, (text[i]).length()) + "000 M Dollars ");
                    }

                    i = i + 1;
                    continue;
                }
                if (i + 1 < text.length && text[i + 1].equals("million")) { //$100 million:
                    //terms.add(doc.getText().get(i).substring(1, (doc.getText().get(i).length() - 1)) + " M Dollars");
                    if (isStopWord(text[i + 1]))
                        continue;
                    if (!isQuery) {
                        addTheDictionary(text[i].substring(1, (text[i].length())) + " M Dollars", doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(text[i].substring(1, (text[i].length())) + " M Dollars "));

                        }else
                        termsOfQuery.append(text[i].substring(1, (text[i].length())) + " M Dollars ");
                    }

                    i = i + 1;
                    continue;
                }
                String theNumber = text[i].substring(1, (text[i].length()));
                if (!Character.isDigit(text[i].charAt(text[i].length() - 1))) {
                    continue;
                }
                Double theNum = fromStringToDouble(theNumber);
                if (theNum != null && theNum < 1000000) {
                    //terms.add(theNumber + " Dollars");
                    if (!isQuery) {
                        addTheDictionary(theNumber + " Dollars", doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(theNumber + " Dollars "));
                        }else
                            termsOfQuery.append(theNumber + " Dollars ");
                    }

                    continue;
                }
                if (theNum != null && theNum > 1000000) {
                    theNum = theNum / 1000000;
                    if (isContainDot(theNum.toString())) { //////// string contain dot
                        int j = theNum.intValue();
                        if (!isQuery) {
                            addTheDictionary(j + " M Dollars", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(j + " M Dollars "));
                            }else
                            termsOfQuery.append(j + " M Dollars ");
                        }

                        continue;
                    } else {
                        //terms.add(theNum.toString() + " M Dollars");
                        if (!isQuery) {
                            addTheDictionary(theNum.toString() + " M Dollars", doc, i);
                        } else {
                            if (toStem){
                                termsOfQuery.append(stemmer.stemming(theNum.toString() + " M Dollars "));
                            }else
                                termsOfQuery.append(theNum.toString() + " M Dollars ");
                        }

                        continue;
                    }
                }
            }
            // isDollar //
            if (isTimes(text[i])) {
                if (i + 1 < text.length && (text[i + 1].equals("AM") || text[i + 1].equals("am")
                        || text[i + 1].equals("PM") || text[i + 1].equals("pm")
                        || text[i + 1].equals("p.m.") || text[i + 1].equals("a.m.")
                        || text[i + 1].equals("P.M.") || text[i + 1].equals("A.M."))) {
                    if (!isQuery) {
                        addTheDictionary(text[i] + " " + text[i + 1], doc, i);
                    } else {
                        if (toStem){
                            termsOfQuery.append(stemmer.stemming(text[i] + " " + text[i + 1]+" "));
                        }else
                            termsOfQuery.append(text[i] + " " + text[i + 1]+" ");
                    }

                    continue;

                }
            }

            if (!isQuery) {
                addTheDictionary(text[i], doc, i);
            }else{
                if (toStem){
                    termsOfQuery.append(stemmer.stemming(text[i]+" "));
                }else
                termsOfQuery.append(text[i]+" ");
            }

        } //for
        if(isQuery){
            if (toStem)
                System.out.println(termsOfQuery.toString());
            return termsOfQuery.toString();
        }else{
            return null;
        }
    }

    // replace 'o' with zero
    private String replaceOby0(String str) {
        boolean flag = false;
        String ans = "";
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != 'o' && str.charAt(i) != 'O' && !Character.isDigit(str.charAt(i))
                    && str.charAt(i) != '.' && str.charAt(i) != ',') {
                flag = true;
            }
        }
        if (!flag) {
            return replaceFromMap(str);
        }
        return str;
    }

    // is the given str is mounth
    private boolean isInMonth(String str) {

        return months.containsKey(str);
    }

    // if the last char of string is '%'
    private boolean lastCharIsPercents(String str) {
        return str.charAt(str.length() - 1) == '%';
    }

    // turn numeric string into atual number
    private double fromStringToDouble(String str) {

        String tmp = "";
        tmp = replaceFromMap(str);
        Double ans = 0.0;
        if (!isContainDot(tmp)) {
            try {
                ans = Double.parseDouble(tmp + ".0");
                return ans;
            } catch (Exception e) {
                return 0;
            }
        } else {
            try {
                ans = Double.parseDouble(tmp);
            } catch (Exception e) {
                return ans;
            }
        }
        return ans;
    }

    // is the given string is million / billion / bn ...
    private boolean isInNumTerms(String str) {
        return numTerms.contains(str);
    }

    // is the first chat in given string is number
    private boolean isNumericStart(String str) {

        int countDot = 0;
        for (int i = 0; i < str.length(); i++) { ///////////////////////////////  i < str.length()-1
            if (!Character.isDigit(str.charAt(i)) && (str.charAt(i) != ',') && str.charAt(i) != '.') {

                return false;
            } else {

                if (str.charAt(i) == '.') countDot++;
                if (countDot > 1) {
                    return false;
                }
            }

        }

        return true;
    }

    // is the first char in given string is '$'
    private boolean isDollar(String str) {
        return str.length() > 1 && str.charAt(0) == '$';
    }

    // is the given string contain '-'
    private boolean isContainDash(String str) {
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '-') return true;
        return false;
    }

    // is the given string contain '.'
    private boolean isContainDot(String str) {
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '.') return true;
        return false;
    }

    // intialize hashmap of mounth and their number in order to change in o(1)
    private void insertMonths() {
        months.put("January", "01");
        months.put("February", "02");
        months.put("February", "02");
        months.put("March", "03");
        months.put("April", "04");
        months.put("May", "05");
        months.put("June", "06");
        months.put("July", "07");
        months.put("August", "08");
        months.put("September", "09");
        months.put("October", "10");
        months.put("November", "11");
        months.put("December", "12");

        months.put("JANUARY", "01");
        months.put("FEBRUARY", "02");
        months.put("MARCH", "03");
        months.put("APRIL", "04");
        months.put("MAY", "05");
        months.put("JUNE", "06");
        months.put("JULY", "07");
        months.put("AUGUST", "08");
        months.put("SEPTEMBER", "09");
        months.put("OCTOBER", "10");
        months.put("NOVEMBER", "11");
        months.put("DECEMBER", "12");

        months.put("JAN", "01");
        months.put("FEB", "02");
        months.put("MAR", "03");
        months.put("APR", "04");
        months.put("MAY", "05");
        months.put("JUN", "06");
        months.put("JUL", "07");
        months.put("AUG", "08");
        months.put("SEP", "09");
        months.put("OCT", "10");
        months.put("NOV", "11");
        months.put("DEC", "12");

        months.put("Jan", "01");
        months.put("Feb", "02");
        months.put("Mar", "03");
        months.put("Apr", "04");
        months.put("May", "05");
        months.put("Jun", "06");
        months.put("Jul", "07");
        months.put("Aug", "08");
        months.put("Sep", "09");
        months.put("Oct", "10");
        months.put("Nov", "11");
        months.put("Dec", "12");
    }

    // insert stop words into hashmap
    public void insertStopWords(String stopWordsPath) throws IOException {
        try {
            FileInputStream fis = new FileInputStream(new File(stopWordsPath));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while (line != null) {
                stopWords.add(line);
                line = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("file not Found");
        }
    } // add with capital letter

    // is stopWord - return if given str is stop word
    private boolean isStopWord(String word) {
        return stopWords.contains(word) || stopWords.contains(word.toLowerCase());
    }

    // intialize hashmap which help to
    private void insertNumTerms() {

        numTerms.add("Thousand");
        numTerms.add("Million");
        numTerms.add("Billion");
        numTerms.add("Trillion");
        numTerms.add("billion");
        numTerms.add("trillion");
        numTerms.add("million");
        numTerms.add("thousand");


    }

    // return if the given term is Us Dollars
    private boolean isUSDollars(String[] text, int i) {

        if (i + 3 < text.length && text[i + 3].equals("dollars")
                && text[i + 2].equals("U.S.") && (text[i + 1].equals("trillion") ||
                text[i + 1].equals("million") || text[i + 1].equals("billion")))
            return true;

        return false;
    }

    //convert parsing by the rules
    private String usDollarsConvert(String number, String kind) {
        if (kind.equals("billion")) {
            return number + "000" + " M Dollars";
        }
        if (kind.equals("million")) {
            return number + " M Dollars";
        }
        if (kind.equals("trillion")) {
            return number + "000000" + " M Dollars";
        }
        return null;
    }

    // given string equal bn
    private boolean bn(String str) {
        return str.equals("bn");
    }

    // given string equal m
    private boolean m(String str) {
        return str.equals("m");
    }

    // given string is furction
    private boolean isFruction(String str) {
        if (!slashCounter(str))
            return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)) && !(str.charAt(i) == '/'))
                return false;
        }
        return true;
    }

    // count slashes in given string
    private boolean slashCounter(String str) {
        int i = 0;
        for (int j = 0; j < str.length(); j++) {
            if (str.charAt(j) == '/')
                i++;
        }
        return i == 1;
    }

    // replace map with term and with its replacment by the given rules
    private void initialreplacment() {
        replacements.put("O", "0");
        replacements.put("o", "0");
        replacements.put(",", "");
        replacements.put("%", "");
    }

    // return if number is greater than 1m
    private boolean greaterThanMillion(String str) {

        int countComma = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ',') {
                countComma++;
            }
        }
        if (countComma >= 2)
            return true;
        return false;
    }

    // return if given string present a template of time
    private boolean isTimes(String str) {

        for (int i = 0; i < str.length(); i++) {

            if (str.charAt(i) != ':' && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;

    }

    // replace a specific string in map
    public String replaceFromMap(String string) {
        StringBuilder sb = new StringBuilder(string);
        for (Map.Entry<String, String> entry : this.replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int start = sb.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }

    // remove dashes from given string
    private String removeDashes(String s) {
        String ans = "";
        for (int k = 0; k < s.length(); k++) {
            if (s.charAt(k) != '-' && s.charAt(k) != '/' && s.charAt(k) != '|' && s.charAt(k) != '\''
                    && s.charAt(k) != '/' && s.charAt(k) != '`' && s.charAt(k) != ' ') {
                ans = ans + s.charAt(k);
            }
        }
        return ans;
    }

    //update the Integer of the given doc (per term) and send add the term to the tempDictionary
    private void addTheDictionary(String termValue, Docs doc, int i) {
        doc.setDocLength(doc.getDocLength()+1);
        boolean isProblem = false;
        if (!(termValue.length() > 0) || termValue == null) {
            return;
        }
        if (toStem) {
            termValue = stemmer.stemming(termValue);
        }

        if (termValue.equals("tel")) isProblem = true;

        if (Character.isUpperCase(termValue.charAt(0))) {
            termValue = termValue.toUpperCase();
        }

        if (Character.isLowerCase(termValue.charAt(0))) {
            termValue = termValue.toLowerCase();
        }
        Iterator<Terms> iteratorDict = tempDictionary.iterator();

        for (Terms term : tempDictionary) {
            //while (iteratorDict.hasNext()) {
            //Terms term = iteratorDict.next();
            if (term.getValue().equals(termValue)) { //if the terms exist in the tempDictionary
                if (term.getDocsAndAmount().containsKey(doc)) { //if the term exists the given doc
                    Pair<Integer, StringBuilder> newPir = new Pair<Integer, StringBuilder>(term.getDocsAndAmount().get(doc).getKey() + 1, term.getDocsAndAmount().get(doc).getValue().append(" ,").append(i));
                    term.getDocsAndAmount().put(doc, newPir);
                    String tempCity = Character.toUpperCase(termValue.charAt(0)) + termValue.substring(1, termValue.length()).toLowerCase();
                    if (cities.containsKey(tempCity) && !isProblem) { //if a city
                        cities.get(tempCity).getLocations().get(doc.getDocNo()).append(" ,").append(i);

                    }


                } else {
                    term.getDocsAndAmount().put(doc, new Pair<Integer, StringBuilder>(1, new StringBuilder("").append(i))); //if the term does not exist the given doc
                    String tempCity = Character.toUpperCase(termValue.charAt(0)) + termValue.substring(1, termValue.length()).toLowerCase();
                    //String tempUpper = termValue.charAt(0) + termValue.substring(1, termValue.length()).toLowerCase();
                    if (cities.containsKey(tempCity) && !isProblem) {//|| cities.containsKey(tempCity.toUpperCase())|| cities.containsKey(tempCity.toLowerCase())) {  //if a city
                        cities.get(tempCity).getLocations().put(doc.getDocNo(), new StringBuilder("").append(i));

                    }
                }
                term.totalInCorpus++;
                return;
            }
            // DOG in tempdictionary and dog in termValue
            if(Character.isLowerCase(termValue.charAt(0)) && term.getValue().equals(termValue.toUpperCase())){
                term.setValue(termValue);
                if (term.getDocsAndAmount().containsKey(doc)) { //if the term exists the given doc
                    Pair<Integer, StringBuilder> newPir = new Pair<Integer, StringBuilder>(term.getDocsAndAmount().get(doc).getKey() + 1, term.getDocsAndAmount().get(doc).getValue().append(" ,").append(i));
                    term.getDocsAndAmount().put(doc, newPir);
                    String tempCity = Character.toUpperCase(termValue.charAt(0)) + termValue.substring(1, termValue.length()).toLowerCase();
                    if (cities.containsKey(tempCity) && !isProblem) { //if a city
                        cities.get(tempCity).getLocations().get(doc.getDocNo()).append(" ,").append(i);
                    }


                } else {
                    term.getDocsAndAmount().put(doc, new Pair<Integer, StringBuilder>(1, new StringBuilder("").append(i))); //if the term does not exist the given doc
                    String tempCity = Character.toUpperCase(termValue.charAt(0)) + termValue.substring(1, termValue.length()).toLowerCase();
                    //String tempUpper = termValue.charAt(0) + termValue.substring(1, termValue.length()).toLowerCase();
                    if (cities.containsKey(tempCity) && !isProblem) {//|| cities.containsKey(tempCity.toUpperCase())|| cities.containsKey(tempCity.toLowerCase())) {  //if a city
                        cities.get(tempCity).getLocations().put(doc.getDocNo(), new StringBuilder("").append(i));

                    }
                }
                term.totalInCorpus++;
                return;
            }
        }

        Terms newTerm = new Terms(termValue); //if the term does not exist the tempDictionary
        newTerm.getDocsAndAmount().put(doc, new Pair<Integer, StringBuilder>(1, new StringBuilder("").append(i)));
        newTerm.totalInCorpus++;
        tempDictionary.add(newTerm);
        String tempCity = Character.toUpperCase(termValue.charAt(0)) + termValue.substring(1, termValue.length()).toLowerCase();
        if (cities.containsKey(tempCity) && !isProblem) {  //if a city
            cities.get(tempCity).getLocations().put(doc.getDocNo(), new StringBuilder("").append(i));

        }
    }


}