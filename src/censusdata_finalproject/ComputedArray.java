// Wrapper Class for holding ComputedData object
package censusdata_finalproject;

public class ComputedArray {
    private final ComputedData[][] computedData;
    public ComputedArray(ComputedData[][] inArray){
        this.computedData = inArray;
    }
    
    public ComputedData[][] Array(){
        return this.computedData;
    }
}
