package com.project.server;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.ArrayList;

public class EarlyLectures extends RecursiveTask<Boolean> {
    private Lecture[][] schedule;
    private int startCol;
    private int endCol;

    public EarlyLectures(Lecture[][] sch, int startCol, int endCol) {
        this.schedule = sch;
        this.startCol = startCol;
        this.endCol = endCol;
    }

    @Override
    protected Boolean compute() {
        // indicate whether any changes were made
        boolean changed = false;

        // implement rescursion
        if(endCol - startCol == 1) {
            // make an array of free slots in the col
            ArrayList<Integer> freeSlots = new ArrayList<>();
            
            for (int row = 0; row < schedule.length; row++) {
                // if slot is free, add to list
                if (schedule[row][startCol] == null) {
                    freeSlots.add(row);
                } else {
                    // if slot is taken, check if there are free slots before
                    if (!freeSlots.isEmpty()) {
                        // move to first free slot
                        int firstFreeSlot = freeSlots.remove(0);
                        synchronized (schedule) {
                            //update the time of the lecture
                            schedule[row][startCol].setLecTimeNum(firstFreeSlot);
                            // update the schedule array
                            schedule[firstFreeSlot][startCol] = schedule[row][startCol];
                            schedule[row][startCol] = null;
                            changed = true;
                        }
                        freeSlots.add(row); // add newly cleared slot to free slots
                    }
                }
            }
        } else {
            // split the task into two subtasks
            int midCol = (startCol + endCol) / 2;
            EarlyLectures leftTask = new EarlyLectures(schedule, startCol, midCol);
            EarlyLectures rightTask = new EarlyLectures(schedule, midCol, endCol);

            // fork the subtasks
            leftTask.fork();
            boolean rightResult = rightTask.compute(); // compute right task directly (current thread)
            boolean leftResult = leftTask.join(); // join left task (from new thread)

            changed = leftResult || rightResult; // combine results
        }

        return changed;
    }

    public static boolean moveLectures(Lecture[][] array) {
        /*
        boolean changed = false;
        List<EarlyLectures> tasks = new ArrayList<>();
    
        for (int col = 0; col < array[0].length; col++) {
            EarlyLectures task = new EarlyLectures(array, col);
            task.fork(); // launch task in parallel
            tasks.add(task);
        }

        for (EarlyLectures task : tasks) {
            boolean result = task.join(); // wait for each task to complete

            if(result){
                changed = true; // if any task changed the schedule, set changed to true
            }
        }
        return changed;
        */

        //return ForkJoinPool.commonPool().invoke(new EarlyLectures(array, 0, array[0].length));

        ForkJoinPool fivePool = new ForkJoinPool(array[0].length); // 1 thread per column
        try {
            return fivePool.invoke(new EarlyLectures(array, 0, array[0].length));
        } finally {
            fivePool.shutdown(); // ensure the pool is properly closed
        }
    }
}