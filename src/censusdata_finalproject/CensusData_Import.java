// Data import class
package censusdata_finalproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class CensusData_Import {
    // Decalres/Initializes objects/variables 
    private ArrayList<DataArray> censusArrayList = new ArrayList<>();
    private HashMap<String,Integer> yearMap = new HashMap<>();
    private HashMap<String,Integer> stateMap = new HashMap<>();
    private HashMap<String,Integer> raceMap = new HashMap<>();
    private boolean isStateMapBuilt = false;
    
    //Contructor 
    public CensusData_Import(){
        this.BuildRaceMap();
        this.ReadInData();
    }
    
    // Getter for arrayy list of census data
    public ArrayList<DataArray> getCensusList(){
        return this.censusArrayList;
    }
    // Getter Year Hash Map
    public HashMap<String,Integer> getYearMap(){
        return this.yearMap;
    }
    // Getter State Hash Map
    public HashMap<String,Integer> getStateMap(){
        return this.stateMap;
    }
    // Getter Race Hash Map
    public HashMap<String,Integer> getRaceMap(){
        return this.raceMap;
    }
    
    // Builds race hash map
    private void BuildRaceMap(){
        raceMap.put("t", raceMap.size());  // Total - all races
        raceMap.put("w", raceMap.size());  // White non hispanic
        raceMap.put("b", raceMap.size());  // Black
        raceMap.put("na", raceMap.size()); // Native American/Alaskan Native
        raceMap.put("ap", raceMap.size()); // Asian/Pacific Islander
        raceMap.put("m", raceMap.size());  // Mixed Race
        raceMap.put("h", raceMap.size());  // Hispanic
        raceMap.put("hw", raceMap.size()); // Hispanic whtie
        raceMap.put("hnw", raceMap.size());// Hisanpic non-white
    }
    
    // Reads in data from Data Directory with two files types .txt & .xls
    private void ReadInData(){
        File folder = new File("Data");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if(file.isFile() && file.getName().endsWith(".txt")){
                this.ReadIntoArray_txt(file.getPath());
            }
            if(file.isFile() && file.getName().endsWith(".xls")){
                this.ReadIntoArray_xls(file.getPath());
            }
        }
    }

    // Reads from text files into local array list
    private void ReadIntoArray_txt(String inFileName){
        try {
            // Number of line that starts the overall totals data 
            int lineOfTotals = 15;
            // Number of line that starts the state totals data 
            int lineOfDataStart = 29;
            // Number of line that end the census data 
            int lineOfDataEnd = 79;

            int lineCount = 1;
            int stateCount = 0;
            String yearOfInput = "";
            // array that holds the data read from the file 
            // 52 total lines = 1-Overall Totals 50-State Totals 1-Washington D.C. total
            int[][] dataFromfile = new int[52][9];
            String line = "";
            FileReader fReader = new FileReader(inFileName);
            BufferedReader bReader = new BufferedReader(fReader);
            while((line = bReader.readLine()) != null){  
                    if (lineCount == 1){
                        // Year of data file being read
                        yearOfInput = line.substring(81, 85).trim();
                    }
                    if((lineCount <= lineOfDataEnd && lineCount >= lineOfDataStart)|| lineCount == lineOfTotals){
                    if (!isStateMapBuilt){
                        // get line name, i.e. state name, and build state hash map
                        stateMap.put(line.substring(0, 23).trim().toLowerCase(), stateCount);
                        if (stateCount == 52){
                            isStateMapBuilt = true;
                        }
                    }
                    // Overall Population totals
                    dataFromfile[stateCount][0] = Integer.parseInt(line.substring(23, 33).trim());
                    // White Non-hispanic population total
                    dataFromfile[stateCount][1] = Integer.parseInt(line.substring(53, 63).trim());
                    // Black Populaion total
                    dataFromfile[stateCount][2] = Integer.parseInt(line.substring(63, 73).trim());
                    // Native American & Alaskan native population total
                    dataFromfile[stateCount][3] = Integer.parseInt(line.substring(73, 83).trim());
                    // Asian and Pacific Islander total
                    dataFromfile[stateCount][4] = Integer.parseInt(line.substring(83, 93).trim());
                    // Mixed race population total - data does not exsits in 1990-1999 data
                    dataFromfile[stateCount][5] = 0;
                    // Hispanic total
                    dataFromfile[stateCount][6] = Integer.parseInt(line.substring(93, 102).trim());
                    // White Hispanic total
                    dataFromfile[stateCount][7] = Integer.parseInt(line.substring(43, 53).trim());
                    // Non Whtie Hispanic Total
                    dataFromfile[stateCount][8] = dataFromfile[stateCount][6] - dataFromfile[stateCount][7];

                    stateCount++;
                }
                lineCount++;
            }
            bReader.close();
            fReader.close();

            this.censusArrayList.add(new DataArray(dataFromfile));
            this.yearMap.put(yearOfInput, this.censusArrayList.size()-1);
        }
        catch(FileNotFoundException e){
            System.out.println("No file found : " + e.getMessage());
        }
        catch(IOException e){
            System.out.println("Eorror reading file: " + e.getMessage());
        }
    }
    
    // Reads from xls files into local array list
    private void ReadIntoArray_xls(String inFileName){
        try{
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(inFileName));
            HSSFWorkbook censusWb = new HSSFWorkbook(fs);
            HSSFSheet sheet = censusWb.getSheetAt(0);
            HSSFRow row;
            HSSFRow whiteNonHispRow;
            HSSFCell cell;
            
            int stateCount = 0;
            int lineOfDataStart = 4;
            int lineOfDataEnd = 56;
            int lineAdjustmentForHispanic = 0;
            String yearOfInput = "";
            // array that holds the data read from the file 
            // 52 total lines = 1-Overall Totals 50-State Totals 1-Washington D.C. total
            int[][] dataFromfile = new int[52][9];
            
            yearOfInput = sheet.getRow(1).getCell(0).getStringCellValue();
            Pattern regPat = Pattern.compile("(\\d{4}$)");
            Matcher match = regPat.matcher(yearOfInput);
            if(match.find()){
                yearOfInput = yearOfInput.substring(match.start(), match.end());
            }
            
            if(Integer.parseInt(yearOfInput) >= 2007){
                lineOfDataStart++;
                lineOfDataEnd++;
            }
            
            for(int i = lineOfDataStart; i < lineOfDataEnd; i++){
                row = sheet.getRow(i);
                // Overall Population totals
                dataFromfile[stateCount][0] = (int)row.getCell(1).getNumericCellValue();
                // White Non-hispanic population total
                whiteNonHispRow = sheet.getRow(i + 53);
                dataFromfile[stateCount][1] = (int)whiteNonHispRow.getCell(2).getNumericCellValue();
                // Black Populaion total
                dataFromfile[stateCount][2] = (int)row.getCell(3).getNumericCellValue();
                // Native American & Alaskan native population total
                dataFromfile[stateCount][3] = (int)row.getCell(4).getNumericCellValue();
                // Asian and Pacific Islander total -- combined due to seperate cols in the spreadsheet
                dataFromfile[stateCount][4] = (int)row.getCell(5).getNumericCellValue() + (int)row.getCell(6).getNumericCellValue();
                // Mixed race population total - data does not exsits in 1990-1999 data
                dataFromfile[stateCount][5] = (int)row.getCell(7).getNumericCellValue();
                // Hispanic total
                lineAdjustmentForHispanic = i + 106;
                row = sheet.getRow(lineAdjustmentForHispanic);
                dataFromfile[stateCount][6] = (int)row.getCell(1).getNumericCellValue();
                // White Hispanic total
                dataFromfile[stateCount][7] = (int)row.getCell(2).getNumericCellValue();
                // Non Whtie Hispanic Total
                dataFromfile[stateCount][8] = dataFromfile[stateCount][6] - dataFromfile[stateCount][7];
                
                stateCount++;
            }
            
            this.censusArrayList.add(new DataArray(dataFromfile));
            this.yearMap.put(yearOfInput, this.censusArrayList.size()-1);
            
            censusWb.close();
            fs.close();
            
        }
        catch(Exception poiExp){
            System.out.println("Error reading the excel file: " + poiExp.toString());
        }
    }
}