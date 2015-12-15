// Main Class
package censusdata_finalproject;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class CensusData_FinalProject {

    
    public static void main(String[] args) {
        // Initializes objects
        CensusData_Import runProject = new CensusData_Import();
        HashMap<String,Integer> localStateMap = runProject.getStateMap();
        HashMap<String,Integer> localRaceMap = runProject.getRaceMap();
        HashMap<String,Integer> localYearMap = runProject.getYearMap();
        HashMap<String,Integer> computedTimeFrameMap = new HashMap<String, Integer>(0);
        int[][] beginDataArr;
        int[][] endDataArr;     
        ArrayList<ComputedArray> computedList = new ArrayList<ComputedArray>();
        
        // 2D array used for grouping during data analysis
        int[][] loopControl = new int[][]{
            {1990,1994,5},
            {1994,1999,5},
            {1999,2004,5},
            {2004,2009,5},
            {1990,1999,10},
            {1999,2009,10},
            {1990,2011,21}
        };
        
        //Building data time frame hash map
        for(int i = 0; i < loopControl.length;i++){
            beginDataArr = runProject.getCensusList().get(localYearMap.get(String.valueOf(loopControl[i][0]))).Array();
            endDataArr   = runProject.getCensusList().get(localYearMap.get(String.valueOf(loopControl[i][1]))).Array();
            computedList = AnalyzeData(computedList, beginDataArr, endDataArr,loopControl[i][2] );
            computedTimeFrameMap.put(loopControl[i][0] + "-" + loopControl[i][1], i);
        }
        
        //Creates and writes JSON object to .js files in Output/ folder
        SerializeToJSON_AllStateData(runProject.getCensusList(), localYearMap, localStateMap, localRaceMap);
        SerializeToJSON_ComputedData(computedList, computedTimeFrameMap, localStateMap, localRaceMap);                
    }
    
    // Calculates total difference, percent increase, and growth rate of population data
    public static ArrayList<ComputedArray> AnalyzeData(ArrayList<ComputedArray> inList, int[][] inFirArray, int[][] inSecArray, float timeFrame){
        int[][] firstArray = inFirArray;
        int[][] secondArray = inSecArray;
        ComputedData[][] resultArray = new ComputedData[52][9];
        int totalDiff;
        float percentInc;
        float growthRate;
        
        for(int i = 0; i < firstArray.length; i++){
            for(int j = 0; j < firstArray[0].length; j++){
                if(secondArray[i][j] > 0 && firstArray[i][j] > 0){
                    totalDiff = secondArray[i][j] - firstArray[i][j];
                    percentInc = ((float)totalDiff/(float)firstArray[i][j]) * 100;
                    growthRate = percentInc/timeFrame;
                }
                else{
                    totalDiff = 0;
                    percentInc = 0;
                    growthRate = 0;
                }
                ComputedData data = new ComputedData(totalDiff, percentInc, growthRate);
                resultArray[i][j] = data;
            }
        }
        ComputedArray ca = new ComputedArray(resultArray);
        inList.add(ca);
        
        return inList;
    }
    
    // Creates and writes JSON Object for all data to js file 
    public static void SerializeToJSON_AllStateData(ArrayList<DataArray> inDataArray, HashMap<String,Integer> inYearMap, HashMap<String,Integer> inStateMap, HashMap<String,Integer> inRaceMap){
        String jsonString = "allDataFromFile = {";
        for(HashMap.Entry<String,Integer> year : inYearMap.entrySet()){
            jsonString += "\"" + year.getKey() + "\":{";
            for(HashMap.Entry<String,Integer> state : inStateMap.entrySet()){
                jsonString += "\"" + state.getKey() + "\":{";
                for(HashMap.Entry<String,Integer> race : inRaceMap.entrySet()){
                    jsonString += "\"" + race.getKey() + "\":" + inDataArray.get(year.getValue()).Array()[state.getValue()][race.getValue()] + ",";
                }
                jsonString = jsonString.substring(0, jsonString.lastIndexOf(","));
                jsonString += "},";
            }
            jsonString = jsonString.substring(0, jsonString.lastIndexOf(","));
            jsonString += "},";
        }
        jsonString = jsonString.substring(0, jsonString.lastIndexOf(","));
        jsonString += "}";
        
        try{
            PrintWriter writer = new PrintWriter("Output/AllStateData.js", "UTF-8");
            writer.println(jsonString);
            writer.close();
        }
        catch(FileNotFoundException ex){
            System.out.println("Error writing JSON file: " + ex.toString());
        }
        catch(UnsupportedEncodingException ex){
            System.out.println("Error writing JSON file - Encoding: " + ex.toString());
        }
    }
    
    // Creates and writes JSON Object for computed data to js file 
    public static void SerializeToJSON_ComputedData(ArrayList<ComputedArray> inDataArray, HashMap<String,Integer> inYearMap, HashMap<String,Integer> inStateMap, HashMap<String,Integer> inRaceMap){
        String jsonString = "computedDataFromFile = {";
        for(HashMap.Entry<String,Integer> year : inYearMap.entrySet()){
            jsonString += "\"" + year.getKey() + "\":{";
            for(HashMap.Entry<String,Integer> state : inStateMap.entrySet()){
                jsonString += "\"" + state.getKey() + "\":{";
                for(HashMap.Entry<String,Integer> race : inRaceMap.entrySet()){
                    jsonString += "\"" + race.getKey() + "\":{" +
                            "\"total difference\":" + inDataArray.get(year.getValue()).Array()[state.getValue()][race.getValue()].getTotalDiff() + "," +
                            "\"percent increase\":" + inDataArray.get(year.getValue()).Array()[state.getValue()][race.getValue()].getPercentInc() + "," +
                            "\"growth rate\":" + inDataArray.get(year.getValue()).Array()[state.getValue()][race.getValue()].getGrowthRate()+ "},";
                            
                }
                jsonString = jsonString.substring(0, jsonString.lastIndexOf(","));
                jsonString += "},";
            }
            jsonString = jsonString.substring(0, jsonString.lastIndexOf(","));
            jsonString += "},";
        }
        jsonString = jsonString.substring(0, jsonString.lastIndexOf(","));
        jsonString += "}";
        
        try{
            PrintWriter writer = new PrintWriter("Output/ComputedStateData.js", "UTF-8");
            writer.println(jsonString);
            writer.close();
        }
        catch(FileNotFoundException ex){
            System.out.println("Error writing JSON file: " + ex.toString());
        }
        catch(UnsupportedEncodingException ex){
            System.out.println("Error writing JSON file - Encoding: " + ex.toString());
        }
    }
    
}

