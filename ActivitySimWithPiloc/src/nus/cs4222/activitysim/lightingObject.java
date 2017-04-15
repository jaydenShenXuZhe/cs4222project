package nus.cs4222.activitysim;
public class lightingObject{

    private long time;
    private float light;

    public lightingObject(){

    }
    public void set(long time, float accel){
        this.time = time;
        this.light = light;
    }
    public long getTime(){
        return this.time;
    }
    public float getLux(){
        return this.light;
    }
}