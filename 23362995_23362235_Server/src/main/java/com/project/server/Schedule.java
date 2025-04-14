package com.project.server;

import java.util.ArrayList;
import java.util.Arrays;

public class Schedule {
    private final Lecture[][] schedule; //schedule array for display
    private final ArrayList<String> modules;
    private int moduleCount;

    public Schedule(){
        this.schedule = new Lecture[Lecture.times.size()][Lecture.days.size()];
        this.modules = new ArrayList<>(5);
        this.moduleCount = 0;

        for (Lecture[] row : schedule) {
            Arrays.fill(row, null); //initialise to null
        }
    }

    public synchronized String addLecture(Lecture lec) throws IncorrectActionException {
        String response;

        // check if module is already in list
        if (!modules.contains(lec.getModuleCode())) {
            // if not, check if list size < 5
            if (moduleCount < 5) {
                //check if time slot is free
                if (schedule[lec.getLecTimeNum()][lec.getDayNum()] == null) {
                    //if free, add lecture to schedule
                    schedule[lec.getLecTimeNum()][lec.getDayNum()] = lec;
                    //add module to list
                    modules.add(lec.getModuleCode());
                    moduleCount++;
                    //send message back
                    response = "Success: Lecture added successfully.";
                } else {
                    //if not free, send error message
                    throw new IncorrectActionException("Time slot taken. Cannot add this lecture to the schedule.");
                }

            } else {
                throw new IncorrectActionException("Max 5 modules per course, cannot add another module.");
            }
        } else {
            // add lecture to schedule
            //check if room and time slot are free
            if (schedule[lec.getLecTimeNum()][lec.getDayNum()] == null) {
                //if free, add lecture to timetable
                schedule[lec.getLecTimeNum()][lec.getDayNum()] = lec;
                //send message back
                response = "Success: Lecture added successfully.";
            } else {
                //if not free, send error message
                throw new IncorrectActionException("Time slot taken. Cannot add this lecture to the schedule.");
            }
        }

        return response;
    }

    public synchronized String removeLecture(Lecture lec) throws IncorrectActionException {
        String response;

        //check if the lecture exists
        if (schedule[lec.getLecTimeNum()][lec.getDayNum()] != null &&
                schedule[lec.getLecTimeNum()][lec.getDayNum()].getModuleCode().equals(lec.getModuleCode()) &&
                schedule[lec.getLecTimeNum()][lec.getDayNum()].getRoomNum().equals(lec.getRoomNum())) {
            //if it exists, remove lecture from timetable
            schedule[lec.getLecTimeNum()][lec.getDayNum()] = null;

            //check if any other lectures of this module are in the schedule
            boolean moreLecs = false;
            outerloop:
            for(Lecture[] i : schedule){
                for(Lecture j : i){
                    if(j != null && lec.getModuleCode().equals(j.getModuleCode())){
                        moreLecs = true;
                        break outerloop;
                    }
                }
            }
            //if no other lectures for this module:
            if(!moreLecs){
                //remove that module from saved list
                modules.remove(lec.getModuleCode());
                //decrement module counter so a new module can be added
                moduleCount--;
            }
            //send message back
            response = String.format("Success: Lecture removed successfully.\nDay: %s, Time: %s, Room: %s", lec.getDay(), lec.getLecTime(), lec.getRoomNum());
        } else {
            //if it doesn't exist, send error message
            throw new IncorrectActionException("This lecture does not exist. Cannot remove from schedule.");
        }

        return response;
    }

    public synchronized String displaySchedule(){
        if(moduleCount == 0){
            return "Schedule empty.";
        }

        StringBuilder response = new StringBuilder();
        //parse the 2D array into a "send-able" format
        for (Lecture[] i : schedule) {
            for (Lecture j : i) {
                if (j != null) {
                    response.append(j);
                }
                //this adds each lecture as "module code, day number, lecture time, room number;" to the output string
                //it will have to separated by ";" to get the lectures, then by "," to get the module code, day, etc.
            }
        }
        return response.toString();
    }

    public synchronized String clearSchedule(){
        //reset module list and counter so a new 5 modules can be added
        modules.clear();
        moduleCount = 0;

        //set the schedule array back to null values
        for (Lecture[] row : schedule) {
            Arrays.fill(row, null); //reset to null
        }

        //set message for client
        return "Success: Schedule cleared successfully.";
    }
}
