package com.project.server;

import java.util.concurrent.RecursiveAction;
import java.util.ArrayList;
import java.util.List;

public class EarlyLectures extends RecursiveAction {
    private Lecture[][] schedule;
    private int col;

    public EarlyLectures(Lecture[][] sch, int col) {
        this.schedule = sch;
        this.col = col;
    }

    @Override
    protected void compute() {
        // make an array of free slots in the col
        ArrayList<Integer> freeSlots = new ArrayList<>();
        
        for (int row = 0; row < schedule.length; row++) {
            // if slot is free, add to list
            if (schedule[row][col] == null) {
                freeSlots.add(row);
                System.out.println("Free slot at " + row + ", " + col);
            } else {
                // if slot is taken, check if there are free slots before
                if (!freeSlots.isEmpty()) {
                    // move to first free slot
                    int firstFreeSlot = freeSlots.remove(0);
                    synchronized (schedule) {
                        System.out.println("Moving lecture from row " + row + " to row " + firstFreeSlot + " in column " + col);

                        //update the time of the lecture
                        schedule[row][col].setLecTimeNum(firstFreeSlot);

                        schedule[firstFreeSlot][col] = schedule[row][col];
                        schedule[row][col] = null;
                        System.out.println("moved.");
                    }
                    freeSlots.add(row); // add newly cleared slot to free slots
                }
            }
        }
    }

    public static void moveLectures(Lecture[][] array) {
        //return ForkJoinPool.commonPool().invoke(new EarlyLectures(array, col));

        List<EarlyLectures> tasks = new ArrayList<>();
    
        for (int col = 0; col < array[0].length; col++) {
            EarlyLectures task = new EarlyLectures(array, col);
            task.fork(); // Launch task in parallel
            tasks.add(task);
        }

        for (EarlyLectures task : tasks) {
            task.join(); // Wait for each task to complete
        }

        System.out.println("All tasks completed.");
    }

}