package nus.cs4222.activitysim;
public class accelObject{

    private long time;
    private float accel;

    public accelObject(){

    }
    public void set(long time, float accel){
        this.time = time;
        this.accel = accel;
    }
    public long getTime(){
        return this.time;
    }
    public float getAccel(){
        return this.accel;
    }
}