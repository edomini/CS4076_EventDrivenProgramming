import java.util.List;

public class Lecture {
    public static final List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
    public static final List<String> times = List.of("9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00");
    private int dayNum;
    private int lecTime;
    private String moduleCode;
    private String roomNum;

    public Lecture(String moduleCode, String day, String time, String roomNum){
        this.moduleCode = moduleCode;
        dayNum = days.indexOf(day);
        lecTime = times.indexOf(time);
        this.roomNum = roomNum;
    }

    @Override
    public String toString() {
        return getModuleCode() + ", " + getDayNum() + ", " + getLecTime() + ", " + getRoomNum() + "\n";
    }

    public String getModuleCode(){
        return moduleCode;
    }

    public int getDayNum(){
        return dayNum;
    }

    public int getLecTime(){
        return lecTime;
    }

    public String getRoomNum(){
        return roomNum;
    }
}
