package Model;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static Model.Indexer.pathDir;

/**
 * The class represents the index of the cities of the corpus
 */
public class CitiesIndexer {

    private HashMap<String, City> citiesAPI;
    private Object[] objArray;

    /**
     * Cosntructor- initialize the fields of the class
     */
    public CitiesIndexer() {
        citiesAPI = new HashMap<String, City>();

    }


    /**
     * The class operates a connection to the API in order to get the relevant information of each city
     */
    public void APIConnection() {

        OkHttpClient client = new OkHttpClient();
        Response res = null;
        try {
            Request request = new Request.Builder().url("https://restcountries.eu/rest/v2/all?fields=name;capital;currencies;population").build();
            //Response res = null;
            res = client.newCall(request).execute();

        } catch (IOException e) {
            return;
        }
        JSONParser parser = new JSONParser();
        Object temp = null;
        try {
            try {
                temp = parser.parse(res.body().string());
                res.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (temp != null) {
                String cityName = "";
                String country = "";
                String currency = "";
                Long population = 0L;
                objArray = ((JSONArray) temp).toArray();
//                objArray = ((JSONArray) temp).toArray();
                for (Object a : objArray) {
                    cityName = (String) ((JSONObject) a).get("capital");
                    country = (String) ((JSONObject) a).get("name");
                    JSONArray jsonArray = (JSONArray) (((JSONObject) a).get("currencies"));
                    for (Object o : jsonArray) {
                        currency = (String) ((JSONObject) o).get("code");
                    }
                    population = (Long) ((JSONObject) a).get("population");
                    citiesAPI.put(cityName, new City(cityName, country, currency, population));


                }

            }

        } catch (org.json.simple.parser.ParseException e) { ////////to check
            e.printStackTrace();

        }
    }

    /**
     * The class merge the two HashMap of cities- from the Pasre class and from the API
     * it updates the information for each city in the corpus according to the information from the API
     * and makes the inverted index of the city- the dictionary and the posting file
     *
     * @param cities- the cities arrives at the whole corpus
     * @throws IOException
     */
    public void mergeTheCities(HashMap<String, City> cities) throws IOException {

        StringBuilder dataDic = new StringBuilder();
        StringBuilder data = new StringBuilder("");

        //counter for the line pointer
        int linePointer = 1;
        //iterator for the city came from parser
        Iterator it = cities.entrySet().iterator();

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();

            City nextCity = (City) pair.getValue();
            if (citiesAPI.containsKey(nextCity.getName())) {
                City currentCityAPI = citiesAPI.get(nextCity.getName());

                if ((currentCityAPI.getCountry() == null && currentCityAPI.getCurrency() == null && currentCityAPI.getPopulation() == null))
                    continue;
                if (key.equals("") || key == null) continue;

                data.append(key).append(": ");
                HashMap<String, StringBuilder> currentCityHash = nextCity.getLocations();

                //iterator for the docs' locations
                Iterator itDocsLocations = currentCityHash.entrySet().iterator();
                data.append("Locations: ");
                while (itDocsLocations.hasNext()) {
                    Map.Entry tempPair = (Map.Entry) itDocsLocations.next();
                    String tempDoc = (String) tempPair.getKey();
                    StringBuilder tempLocations = (StringBuilder) tempPair.getValue();
                    data.append(tempDoc).append(",").append(tempLocations).append(" ");
                }

                data.append("Country: ").append(currentCityAPI.getCurrency()).append(", Currrency: ").append(currentCityAPI.getCountry()).append(", Population: ")
                        .append(currentCityAPI.getPopulation());
                data.append(System.lineSeparator());

                dataDic.append(key).append(", ").append(linePointer);
                dataDic.append(System.lineSeparator());
                linePointer++;
            } else {
                data.append(key);
                data.append(System.lineSeparator());
                dataDic.append(key).append(",").append(linePointer);
                dataDic.append(System.lineSeparator());
                linePointer++;
            }

        }

        try {
            FileOutputStream f = new FileOutputStream(new File(pathDir + "\\CitiesPost.txt"));
            OutputStreamWriter osr = new OutputStreamWriter(f);
            BufferedWriter bw = new BufferedWriter(osr);
            bw.write(data.toString());
            bw.flush();
            bw.close();
        } catch (Exception e) {
        }

        try {
            FileOutputStream fDic = new FileOutputStream(new File(pathDir + "\\CitiesDictionary.txt"));
            OutputStreamWriter osrDic = new OutputStreamWriter(fDic);
            BufferedWriter bwDic = new BufferedWriter(osrDic);
            bwDic.write(dataDic.toString());
            bwDic.flush();
            bwDic.close();
        } catch (Exception e) {
        }
    }
}



