/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robot.datastructures;

import communication.Data;
import communication.messageData.patternformation.PositionData;
import configs.Settings;
import helper.Utility;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import sun.awt.image.BufferedImageGraphicsConfig;

/**
 *
 * @author Mahendra, Nadun, Tharuka
 */
public class PatternTable implements Data {

    /*
        * PatternTable Format
    |   PatternId   |   RespectiveParent    |   X Distance  |   Y Distance  |
        
        * Restriction
        One PatternId can only have one parent
     */
    private HashMap<Integer, TableRow> patterntable;

    public PatternTable() {
        patterntable = new HashMap<Integer, TableRow>();
        createPatternTable();
    }

    public void createPatternTable() {
        TableRow row = new TableRow(0, 50, 0);
        patterntable.put(1, row);

        row = new TableRow(0, -50, 0);
        patterntable.put(2, row);

        row = new TableRow(1, 50, 0);
        patterntable.put(3, row);

        row = new TableRow(2, -50, 0);
        patterntable.put(4, row);

        row = new TableRow(4, -50, 0);
        patterntable.put(5, row);

        row = new TableRow(3, 50, 0);
        patterntable.put(6, row);
        
//                row = new TableRow(5, 50, 0);
//        patterntable.put(6, row);
        /*  
           for (Map.Entry<Integer,TableRow> entry : patterntable.entrySet()) { 
                System.out.println("Key = " + entry.getKey() + ", Value = " + ((TableRow)entry.getValue()).getParentLabel());
           }
         */
    }

    public double getJoinLabelX(int joiningLabel) {
        TableRow row = patterntable.get(joiningLabel);
        return row.getXCoordinate();
    }

    public double getJoinLabelY(int joiningLabel) {
        TableRow row = patterntable.get(joiningLabel);
        return row.getYCoordinate();
    }

    public boolean positionValidation(int joiningLabel, PositionData myCoordinate) {
        boolean status = false;

        double target_x = getJoinLabelX(joiningLabel);
        double target_y = getJoinLabelY(joiningLabel);

        double my_x = myCoordinate.getX();
        double my_y = myCoordinate.getY();
        
        double x_upper_bound = target_x + Settings.DISTANCE_ERROR_THRESHOLD;
        double x_lower_bound = target_x - Settings.DISTANCE_ERROR_THRESHOLD;
                
        double y_upper_bound = target_y + Settings.DISTANCE_ERROR_THRESHOLD;
        double y_lower_bound = target_y - Settings.DISTANCE_ERROR_THRESHOLD;
        
        if(my_x >= x_lower_bound && my_x <= x_upper_bound){
            if(my_y >= y_lower_bound && my_y <= y_upper_bound){
                status = true;
            }
        }
        return status;
    }

    public double getTargetDistance(int joiningLabel,
            PositionData myCoordinate) {

        double target_x = getJoinLabelX(joiningLabel);
        double target_y = getJoinLabelY(joiningLabel);

        double my_x = myCoordinate.getX();
        double my_y = myCoordinate.getY();

        return Utility.distanceBetweenTwoPoints(new Point(my_x, my_y),
                new Point(target_x, target_y));
    }

    public double getTargetRotation(int joiningLabel, double currHeading,
            PositionData myCoordinate) {
        double turningAngle = 0;

        double target_x = getJoinLabelX(joiningLabel);
        double target_y = getJoinLabelY(joiningLabel);

        double my_x = myCoordinate.getX();
        double my_y = myCoordinate.getY();

        double bearing = Utility.calculateBearing(new Point(my_x, my_y),
                new Point(target_x, target_y), currHeading);

        //get the angle + (clockwise) - (counterclockwise)
        if (bearing > 180) {
            turningAngle = bearing - 360;
        }else{
            turningAngle = bearing;
        }

        return turningAngle;
    }

    public boolean checkJoinValidity(int parentLabel, int requestedPatternLabel) {
        boolean status = false;

        //Get the corresponding table row for the current robot
        TableRow row = patterntable.get(requestedPatternLabel);
        int parent = row.getParentLabel();
        if (parent == parentLabel) {
            status = true;
        }
        return status;
    }

    public double getTargetDistanceFromParent(int joinLabel) {
        TableRow row = patterntable.get(joinLabel);
        double x = row.getXCoordinate();
        double y = row.getYCoordinate();
        return (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
    }

    public double getTargetBearingFromParent(int joinLabel, double heading) {
        TableRow row = patterntable.get(joinLabel);

        double x = row.getXCoordinate();
        double y = row.getYCoordinate();

        double bearing = Utility.calculateBearing(new Point(0.0, 0.0),
                new Point(x, y), heading);

        return bearing;
    }

    public double getDistancetoTargetFromJoiningRobot(int joinLabel,
            double bearing, double distance) {

        TableRow row = patterntable.get(joinLabel);

        //get the x,y coordinates of the pattern label measured from leader robot(0,0)
        double x_target = row.getXCoordinate();
        double y_target = row.getYCoordinate();

        //get the x,y coordinates of the joining robot measured from leader robot(0,0)
        double x_join = distance * Math.cos(Math.toRadians(bearing));
        double y_join = distance * Math.sin(Math.toRadians(bearing));

        //get distance from joining robot to target robot
        double targetDistance = Utility.distanceBetweenTwoPoints(new Point(x_join, y_join),
                new Point(x_target, y_target));

        return targetDistance;
    }

    public double getHeadingtoTargetFromJoiningRobot(int joinLabel, double bearing, double distance) {

        //get perpendicular distance from leader robot to navigation path of the joining robot
        double dist_y = getPerpendicDistToNavPath(joinLabel, bearing, distance);

        //get the amount of deviation angle needed for the joining robot to go to the target location 
        double headingDeviation = Math.toDegrees(Math.asin(dist_y / distance));

        return headingDeviation;
    }

    public double getPerpendicDistToNavPath(int joinLabel, double bearing, double distance) {
        TableRow row = patterntable.get(joinLabel);

        //get the x,y coordinates of the pattern label measured from leader robot(0,0)
        double x_target = row.getXCoordinate();
        double y_target = row.getYCoordinate();

        //get the x,y coordinates of the joining robot measured from leader robot(0,0)
        double x_join = distance * Math.cos(Math.toRadians(bearing));
        double y_join = distance * Math.sin(Math.toRadians(bearing));

        //get the mid point of the navigation path
        double midPointNavPath_x = (x_join + x_target) / 2;
        double midPointNavPath_y = (y_join + y_target) / 2;

        //get the perpendicular distance from leader robot(0,0) to mid point of navigation path
        double distToNavPath = Utility.distanceBetweenTwoPoints(new Point(midPointNavPath_x, midPointNavPath_y),
                new Point(0.0, 0.0));

        return distToNavPath;
    }
}
