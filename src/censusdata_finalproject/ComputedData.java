// Wrapper Class for holding computed data
package censusdata_finalproject;

public class ComputedData {
    private final int totalDiff;
    private final float percentInc;
    private final float growthRate;
    
    public ComputedData(int inTotalDiff, float inPercentInc, float inGrowthRate){
        this.totalDiff = inTotalDiff;
        this.percentInc = inPercentInc;
        this.growthRate = inGrowthRate;
    }
    
    public int getTotalDiff(){
        return this.totalDiff;
    }
    public float getPercentInc(){
        return this.percentInc;
    }
    public float getGrowthRate(){
        return this.growthRate;
    }
}
