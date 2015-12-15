// Wrapper Class to store the census data arrays
package censusdata_finalproject;

public class DataArray {
    private final int[][] dataArray;
    public DataArray(int[][] inArray){
        this.dataArray = inArray;
    }
    
    public int[][] Array(){
        return this.dataArray;
    }
}
