package nus.cs4222.activitysim;

import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import android.hardware.*;
import android.util.*;

import nus.cs4222.activitysim.DataStructure.Fingerprint;
import nus.cs4222.activitysim.DataStructure.RadioMap;
import com.google.android.gms.maps.model.LatLng;


/**
   Class containing the activity detection algorithm.

   <p> You can code your activity detection algorithm in this class.
    (You may add more Java class files or add libraries in the 'libs' 
     folder if you need).
    The different callbacks are invoked as per the sensor log files, 
    in the increasing order of timestamps. In the best case, you will
    simply need to copy paste this class file (and any supporting class
    files and libraries) to the Android app without modification
    (in stage 2 of the project).

   <p> Remember that your detection algorithm executes as the sensor data arrives
    one by one. Once you have detected the user's current activity, output
    it using the {@link ActivitySimulator.outputDetectedActivity(UserActivities)}
    method. If the detected activity changes later on, then you need to output the
    newly detected activity using the same method, and so on.
    The detected activities are logged to the file "DetectedActivities.txt",
    in the same folder as your sensor logs.

   <p> To get the current simulator time, use the method
    {@link ActivitySimulator.currentTimeMillis()}. You can set timers using
    the {@link SimulatorTimer} class if you require. You can log to the 
    console/DDMS using either {@code System.out.println()} or using the
    {@link android.util.Log} class. You can use the {@code SensorManager.getRotationMatrix()}
    method (and any other helpful methods) as you would normally do on Android.

   <p> Note: Since this is a simulator, DO NOT create threads, DO NOT sleep(),
    or do anything that can cause the simulator to stall/pause. You 
    can however use timers if you require, see the documentation of the 
    {@link SimulatorTimer} class. 
    In the simulator, the timers are faked. When you copy the code into an
    actual Android app, the timers are real, but the code of this class
    does not need not be modified.
 */
public class ActivityDetection {


    /** Initialises the detection algorithm. */
    public void initDetection() 
        throws Exception {

        // Add initialisation code here, if any. If you use static variables in this class (avoid
        //  doing this, unless they are constants), please do remember to initialise them HERE. 
        //  Remember that the simulator will be run on multiple traces, and your algorithm's initialisation
        //  should be done here before each trace is simulated.

        // In this "dummy algorithm", we just show a dummy example of a timer that runs every 10 min, 
        //  outputting WALKING and INDOOR alternatively.
        // You will most likely not need to use Timers at all, it is just 
        //  provided for convenience if you require.
        // REMOVE THIS DUMMY CODE (2 lines below), otherwise it will mess up your algorithm's output

        // Assume the user is IDLE_INDOOR, then change state based on your algorithm
        // If you are using the Piloc API, then you must load a radio map (in this case, Hande
        //  has provided the radio map data for the pathways marked in the map image in IVLE
        //  workbin, which represents IDLE_COM1 state). You can use your own radio map data, or
        //  code your own localization algorithm in PilocApi. Please see the "onWiFiSensorChanged()"
        //  method.
        pilocApi = new PilocApi();
        RadioMap rm = pilocApi.loadRadioMap(new File( "radiomap.rm" ));
        if( rm == null ) {
            throw new IOException( "Unable to open radio map file, did you specify the correct path in ActivityDetection.java?" );
        }
    }

    /** De-initialises the detection algorithm. */
    public void deinitDetection() 
        throws Exception {
        // Add de-initialisation code here, if any
    }

    /** 
       Called when the accelerometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Accl x value (m/sec^2)
       @param   y            Accl y value (m/sec^2)
       @param   z            Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onAcclSensorChanged( long timestamp , 
                                     float x , 
                                     float y , 
                                     float z , 
                                     int accuracy ) {

        // Process the sensor data as they arrive in each callback, 
        //  with all the processing in the callback itself (don't create threads).

    }

    /** 
       Called when the gravity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gravity x value (m/sec^2)
       @param   y            Gravity y value (m/sec^2)
       @param   z            Gravity z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGravitySensorChanged( long timestamp , 
                                        float x , 
                                        float y , 
                                        float z , 
                                        int accuracy ) {
    }

    /** 
       Called when the linear accelerometer sensor has changed.

       @param   timestamp    Timestamp of th is sensor event
       @param   x            Linear Accl x value (m/sec^2)
       @param   y            Linear Accl y value (m/sec^2)
       @param   z            Linear Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onLinearAcclSensorChanged( long timestamp , 
                                           float x , 
                                           float y , 
                                           float z , 
                                           int accuracy ) {

        float acceleration = (float)Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
        accelObject ao = new accelObject();
        ao.set(timestamp,acceleration);
        accelList.add(ao);

        if(accelList.size()>200){

            for(int i = 0;i<accelList.size();i++) {
                accelObject curr = accelList.get(i);
                float accel = curr.getAccel();

                if(i == 0){
                    startAccel = curr.getTime();
                }
                if(i == accelList.size()-1){
                    endAccel = curr.getTime();
                }
                if (accel< idleThreshold) {
                    countIdle++;
                }else{
                    if(accel<walkingThreshold){
                        countVehicle++;
                    }else{
                        countWalking++;
                    }
                }
            }
            countLight();
            produceOutPut();
            while(accelList.size()>20){
                accelList.remove(0);
            }
            countIdle = 0;
            countWalking = 0;
            countVehicle = 0;
            countIndoor = 0;
            countOutdoor = 0;
        }

    }
    public void countLight(){


        for(int i = lightIndex; i< lightList.size();i++){
            lightingObject lo = lightList.get(i);
            long currLightTime = lo.getTime();
            float currLight = lo.getLux();
            if(currLightTime>startAccel){
                if(currLight > lightThreshold){
                    countOutdoor++;
                }else{
                    countIndoor++;
                }
            }
            if(currLightTime >= endAccel){
                lightIndex = i;
                break;
            }
        }
    }


    public void produceOutPut(){

        float sumMovement = (float)countWalking+ (float)countVehicle + (float)countIdle;
        float perCw = countWalking/sumMovement;
        float perCv = countVehicle/sumMovement;
        float perCi = countIdle/sumMovement;

        float sumEnvironment = (float)countIndoor+(float)countOutdoor;
        float perIn = countIndoor/sumEnvironment;
        float perOut = countOutdoor/sumEnvironment;
        System.out.println(perIn);
        if(perCi > 0.5){
            if(inCom1){
                ActivitySimulator.outputDetectedActivity(UserActivities.IDLE_COM1);
            }else if(perIn > 0.7){
                ActivitySimulator.outputDetectedActivity(UserActivities.IDLE_INDOOR);

            }else if(perOut > 0.7){
                ActivitySimulator.outputDetectedActivity(UserActivities.IDLE_OUTDOOR);

            }else{

            }
        }else if(perCw > 0.5){
            ActivitySimulator.outputDetectedActivity(UserActivities.WALKING);
        }else if(perCv > 0.5){
            ActivitySimulator.outputDetectedActivity(UserActivities.CAR);
        }
    }
    /** 
       Called when the magnetic sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Magnetic x value (microTesla)
       @param   y            Magnetic y value (microTesla)
       @param   z            Magnetic z value (microTesla)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onMagneticSensorChanged( long timestamp , 
                                         float x , 
                                         float y , 
                                         float z , 
                                         int accuracy ) {
    }

    /** 
       Called when the gyroscope sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gyroscope x value (rad/sec)
       @param   y            Gyroscope y value (rad/sec)
       @param   z            Gyroscope z value (rad/sec)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGyroscopeSensorChanged( long timestamp , 
                                          float x , 
                                          float y , 
                                          float z , 
                                          int accuracy ) {
    }

    /** 
       Called when the rotation vector sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Rotation vector x value (unitless)
       @param   y            Rotation vector y value (unitless)
       @param   z            Rotation vector z value (unitless)
       @param   scalar       Rotation vector scalar value (unitless)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onRotationVectorSensorChanged( long timestamp , 
                                               float x , 
                                               float y , 
                                               float z , 
                                               float scalar ,
                                               int accuracy ) {
    }

    /** 
       Called when the barometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   pressure     Barometer pressure value (millibar)
       @param   altitude     Barometer altitude value w.r.t. standard sea level reference (meters)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onBarometerSensorChanged( long timestamp , 
                                          float pressure , 
                                          float altitude , 
                                          int accuracy ) {
    }

    /** 
       Called when the light sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   light        Light value (lux)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */

    public void onLightSensorChanged( long timestamp ,
                                      float light , 
                                      int accuracy ) {
        lightingObject lo = new lightingObject();
        lo.set(timestamp,light);
        lightList.add(lo);
    }

    public float getAverage(ArrayList<Float> list){
        float sum = 0.0f;
        float average = 0.0f;
        for(int i = 0;i<list.size();i++){
            sum+=list.get(i);
        }
        average = sum /((float)list.size());
        return  average;
    }
    /** 
       Called when the proximity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   proximity    Proximity value (cm)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onProximitySensorChanged( long timestamp , 
                                          float proximity , 
                                          int accuracy ) {
    }

    /** 
       Called when the location sensor has changed.

       @param   timestamp    Timestamp of this location event
       @param   provider     "gps" or "network"
       @param   latitude     Latitude (deg)
       @param   longitude    Longitude (deg)
       @param   accuracy     Accuracy of the location data (you may use this) (meters)
       @param   altitude     Altitude (meters) (may be -1 if unavailable)
       @param   bearing      Bearing (deg) (may be -1 if unavailable)
       @param   speed        Speed (m/sec) (may be -1 if unavailable)
     */
    public void onLocationSensorChanged( long timestamp , 
                                         String provider , 
                                         double latitude , 
                                         double longitude , 
                                         float accuracy , 
                                         double altitude , 
                                         float bearing , 
                                         float speed ) {


    }

    /** 
       Called when the WiFi sensor has changed (i.e., a WiFi scan has been performed).

       @param   timestamp           Timestamp of this WiFi scan event
       @param   fingerprintVector   Vector of fingerprints from the WiFi scan
     */
    public void onWiFiSensorChanged( long timestamp , 
                                     Vector< Fingerprint > fingerprintVector ) {

        // You can use Piloc APIs here to figure out the indoor location in COM1, or do
        //  anything that will help you figure out the user activity.
        // You can use the method PilocApi.getLocation(fingerprintVector) to get the location
        //  in COM1 from the WiFi scan. You may use your own radio map, or even write your
        //  own localization algorithm in PilocApi.getLocation(). 
        if(pilocApi.getLocation(fingerprintVector)!=null){
            inCom1 = true;
        }else{
            inCom1 = false;
        }
        // NOTE: Please use the "pilocApi" object defined below to use the Piloc API.
    }

    /** Piloc API provided by Hande. */
    private PilocApi pilocApi;
    private RadioMap rm;
    /** Helper method to convert UNIX millis time into a human-readable string. */
    private static String convertUnixTimeToReadableString( long millisec ) {
        return sdf.format( new Date( millisec ) );
    }

    /** To format the UNIX millis time as a human-readable string. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-h-mm-ssa" );

    // Dummy variables used in the dummy timer code example

    private int numberTimers = 0;
    private final float lightThreshold = 1000;
    private long lightStartTime = 0;
    public ArrayList<lightingObject> lightList = new ArrayList<lightingObject>();
    int countIndoor;
    int countOutdoor;
    int lightIndex = 0;
    boolean isIndoor = false;
    //for linear accelerator sensor
    private final float idleThreshold = 0.30f;
    private final float walkingThreshold = 1.2f;
    public ArrayList<accelObject> accelList = new ArrayList<accelObject>();
    int countWalking;
    int countVehicle;
    int countIdle;
    long startAccel;
    long endAccel;


    boolean inCom1 = false;


    private Runnable task = new Runnable() {
            public void run() {

                // Logging to the DDMS (in the simulator, the DDMS log is to the console)
                System.out.println();
                Log.i( "ActivitySim" , "Timer " + numberTimers + ": Current simulator time: " + 
                       convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ) );
                System.out.println( "Timer " + numberTimers + ": Current simulator time: " + 
                                    convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ) );


                // Dummy example of outputting a detected activity 
                //  to the file "DetectedActivities.txt" in the trace folder.
                //  Here, we just alternate between indoor and walking every 10 min.


            }
        };
}


