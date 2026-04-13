import javax.swing.text.html.Option;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class Main {

    // Database of tasks (in memory)
    static List<Task> tasks = new ArrayList<>();
    static List<Subtask> subtasks = new ArrayList<>();

    public static void main(String[] args) {

        // ✅ Load tasks from disk at program start
        loadTasks();
        checkRecurringTasks();
        Scanner scanner = new Scanner(System.in);
        String taskName;
        String attribute;
        String value;
        while (true) {
            System.out.println("\n1. Import Tasks from CSV");
            System.out.println("2. Search Tasks");
            System.out.println("3. View All Tasks");
            System.out.println("4. Export Tasks to CSV");
            System.out.println("5. Create task");//sub and recurring
            System.out.println("6. Update task");//project, tag sub and recurring
            System.out.println("7. Assign self to task");//barely a proper feature??
            System.out.println("8. Export to ical");
            System.out.println("9. Edit collaborator loads");
            System.out.println("10. View Task History");
            System.out.println("11. Exit");

            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter file name with .csv: ");
                    String importFile = scanner.nextLine();
                    importCSV(importFile);
                    break;

                case 2:
                    System.out.print("Enter keyword to search: ");
                    String keyword = scanner.nextLine();
                    searchTasks(keyword);
                    break;

                case 3:
                    viewTasks();
                    break;

                case 4:
                    System.out.print("Enter export file name: ");
                    String exportFile = scanner.nextLine();
                    exportCSV(exportFile);
                    break;

                    case 5:
                        createTask();
                        break;
                case 6:
                    System.out.print("Enter Task name: ");
                    taskName = scanner.nextLine();
                    int index = tasks.indexOf(taskName);
                    if (index == -1) {
                        System.out.println("task not found");
                        break;
                    }
                    else{
                        System.out.print("Enter attribute value: ");
                        attribute = scanner.nextLine();
                        System.out.print("Enter value for attribute: ");
                        value = scanner.nextLine();
                        updateTask(tasks.get(index),attribute,value);
                        break;
                    }

                case 10:
                    viewHistory();
                    break;

                case 11:
                    // ✅ Save tasks to disk before exiting
                    saveTasks();
                    System.out.println("Goodbye");
                    scanner.close();
                    return;
            }
        }
    }

    // =======================
    // CSV Import / Export (same as before)
    // =======================
    //Todo fix with changes in task
    //todo define class format in csv
    //split array with /
    public static void importCSV(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\kevin\\IdeaProjects\\SOEN-342\\ProofOfConcept\\ImportTasks\\" + filePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Task task = new Task(
                        values[0], values[1], values[2], values[3], LocalDate.parse(values[4]),
                        values[5], values[6], Boolean.parseBoolean(values[7]), values[8], values[9],
                        values[10],values[11], values[12]
                );
                tasks.add(task);
            }
            br.close();
            System.out.println("Tasks imported successfully");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }
    //todo format lists
    //Todo fix with project
    //split array with /
    public static void exportCSV(String filePath) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            for (Task t : tasks) {
                bw.write(
                        t.taskName + "," +
                                t.description + "," +
                                t.status + "," +
                                t.priority + "," +
                                t.dueDate + "," +
                                t.projectName + "," +
                                t.projectDescription + "," +
                                t.isRecurring+","+
                                t.recurringType+","+
                                t.recurringDescription+","+
                                t.tags+","+
                                t.subtasks+","
                );
                bw.newLine();
            }
            bw.close();
            System.out.println("Tasks exported successfully");
        } catch (IOException e) {
            System.out.println("Error writing file");
        }
    }

    // =======================
    // Search / View/
    // =======================
    //todo Add project
    public static void searchTasks(String keyword) {
        for (Task t : tasks) {
            if (t.taskName.contains(keyword) || t.description.contains(keyword)||t.projectName.contains(keyword) || t.projectDescription.contains(keyword)||t.status.contains(keyword)||t.tags.contains(keyword)) {
                System.out.println(t);
            }
        }
    }

    public static void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        for (Task t : tasks) {
            System.out.println(t);
        }
    }

    //constantly checks the due date constraint
    public static boolean checkTasks() {
        int missingDate=0;
        for (Task t : tasks) {
            if (t.dueDate==null) {
                missingDate++;
            }
        }
        for (Subtask s : subtasks) {
            if (s.dueDate==null) {
                missingDate++;
            }
        }
        return missingDate < 50;
    }
    //todo project implement
    //handles both normal creation and subtask
    public static void createTask() {
        Scanner sc = new Scanner(System.in);
        String[] recurrHandler=new String[2];
        boolean flag = true;
        String option;
        String taskName;
        String description="";
        String status;
        String priority;
        LocalDate dueDate=null;
        String projectName;
        String projectDescription;
        String recurringChoice;
        boolean recurring = false;
        String recurringType="";
        String recurringDescription="";
        List<String> tags = new ArrayList<>();
        List<Subtask> subtasks=  new ArrayList<>();
        List<History> history = new ArrayList<>();
        System.out.println("What type of event would you like to create?");
        System.out.println("1. Normal task/recurring task");
        System.out.println("2. Subtask");
        int choice = sc.nextInt();
        switch (choice) {
            case 1:
                System.out.println("What will be the name of the task?");
                taskName = sc.nextLine();
                System.out.println("Add description?(Y/N)");
                option = sc.nextLine();
                if (option.equals("Y")) {
                    System.out.println("What will be the description of the task?");
                    description = sc.nextLine();
                }
                System.out.println("What will be the status of the task?");
                status = sc.nextLine();
                System.out.println("What will be the priority of the task?");
                priority = sc.nextLine();
                System.out.println("Add due date?(Y/N)");
                option = sc.nextLine();
                if (option.equals("Y")) {
                    System.out.println("What will be the due date of the task?(YYYY-MM-DD)");
                    dueDate = LocalDate.parse(sc.nextLine());
                }else{
                    if(!checkTasks()){
                        System.out.println("Too many tasks missing due date. Please put one.(YYYY-MM-DD)");
                        dueDate = LocalDate.parse(sc.nextLine());
                    }
                }
                System.out.println("What will be the project name of the task?");
                projectName = sc.nextLine();
                System.out.println("What will be the project description of the task?");
                projectDescription = sc.nextLine();
                System.out.println("Is this task recurring?(Y/N)");
                recurringChoice = sc.nextLine();
                if (recurringChoice.equals("Y")) {
                        recurrHandler= handleReccuringCreation();
                        recurringType=recurrHandler[0];
                        recurringDescription=recurrHandler[1];
                }
                while(flag){
                    System.out.println("Add a tag?(Y/N)");
                    recurringChoice = sc.nextLine();
                    switch(recurringChoice){
                        case "Y":
                            System.out.println("Enter tag name: ");
                            tags.add(sc.nextLine());
                            break;

                            case "N":
                                flag = false;
                                break;

                    }
                }
                history.add(new History(LocalDate.now(), "task created"));
                tasks.add(new Task(taskName,description,status,priority,dueDate,projectName,projectDescription,recurring,recurringType,recurringDescription,tags,subtasks,history));
                sc.close();
                break;
                case 2:
                    System.out.println("What is the name of the task that you wish to add a sub task to?");
                    taskName = sc.nextLine();
                    int index = tasks.indexOf(taskName);
                    if (index == -1) {
                        System.out.println("task not found");
                    }else{
                        if(!checkTasks()){
                            System.out.println("This will create too many tasks without due dates. Please update a task.");
                            sc.close();
                        } else if (tasks.get(index).getSubtasks().size()<20) {
                            System.out.println("What is the name of the sub task?");
                            taskName = sc.nextLine();
                            tasks.get(index).createSubtask(taskName);
                            subtasks.add(tasks.get(index).getSubtasks().getLast());
                            tasks.get(index).history.add(new History(LocalDate.now(), "subtask added"));
                            subtasks.getLast().history.add(new History(LocalDate.now(), "task created"));
                            sc.close();
                        }else{
                            System.out.println("There are 20 sub tasks in this task");
                            sc.close();
                        }
                    }

        }
    }

    //updates one attribute at a time
    public static void updateTask(Task task, String Attribute,String value){

        switch (Attribute){
            case "title":
                task.setTaskName(value);
                task.history.add(new History(LocalDate.now(), "task title updated"));
                break;

                case "description":
                    task.setDescription(value);
                    task.history.add(new History(LocalDate.now(), "task description updated"));
                    break;

            case "status":
                        task.setStatus(value);
                        task.history.add(new History(LocalDate.now(), "task status updated"));
                break;

                case "priority":
                    task.setPriority(value);
                    task.history.add(new History(LocalDate.now(), "task priority updated"));
                    break;

                    case "dueDate":
                        if(checkTasks()){
                            task.setDueDate(LocalDate.parse(value));
                            task.history.add(new History(LocalDate.now(), "task due date updated"));
                            break;

                        }else{
                            if(value.isEmpty()){
                                Scanner sc = new Scanner(System.in);
                                LocalDate force;
                                System.out.println("Please enter a valid date, the 50 due date limit is reached");
                                String newValue = sc.nextLine();
                                task.setDueDate(LocalDate.parse(newValue));
                                task.history.add(new History(LocalDate.now(), "task due date updated"));
                                sc.close();
                            }
                        }

                        case "projectName":
                            task.setProjectName(value);
                            task.history.add(new History(LocalDate.now(), "project name updated"));
                            break;

                            case "projectDescription":
                                task.setProjectDescription(value);
                                task.history.add(new History(LocalDate.now(), "project description updated"));
                                break;

                                case "Tag":
                                    manageTags(task);
                                    break;



        }
    }
    public static void viewHistory(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Which task history would you like to view?");
        String taskName = sc.nextLine();
        int index = tasks.indexOf(taskName);
        if (index == -1) {
            System.out.println("task not found");
            sc.close();
        }
        else{
            System.out.print(tasks.get(index).getHistory());
            sc.close();
        }
    }

    //guard that runs on startup to preform recurrence
    public static void checkRecurringTasks(){
        LocalDate today=LocalDate.now();
        for (Task t : tasks) {
            if (t.isRecurring && t.dueDate.isBefore(today)){
                switch(t.recurringType) {
                    case "daily":
                        tasks.add(new Task(t.taskName,t.description,"open",t.priority,t.dueDate.plusDays(1),t.projectName,t.projectDescription,t.isRecurring,t.recurringType,t.recurringDescription,t.tags,t.subtasks,new ArrayList<>()));
                        tasks.getLast().history.add(new History(LocalDate.now(),"Task Creation"));
                        if (t.status.equals("open")) {
                            t.setStatus("canceled");
                            t.history.add(new History(LocalDate.now(), "Task canceled"));
                        }
                        break;
                    case "weekly":
                        int dayOfWeek= t.dueDate.getDayOfWeek().getValue();
                        String[] parts = t.recurringDescription.split("/");
                        boolean[] boolArray = new boolean[parts.length];
                        for (int i = 0; i < parts.length; i++) {
                            boolArray[i] = Boolean.parseBoolean(parts[i]);
                        }
                        if (dayOfWeek == 7) {
                            dayOfWeek = 1;
                        }else {
                            dayOfWeek++;
                        }
                        while(!boolArray[dayOfWeek - 1]){
                            if (dayOfWeek == 7) {
                                dayOfWeek = 1;
                            }else {
                                dayOfWeek++;
                            }
                        }
                        DayOfWeek day = DayOfWeek.of(dayOfWeek);
                        LocalDate next= t.dueDate.with(TemporalAdjusters.next(day));
                        tasks.add(new Task(t.taskName,t.description,"open",t.priority,next,t.projectName,t.projectDescription,t.isRecurring,t.recurringType,t.recurringDescription,t.tags,t.subtasks,new ArrayList<>()));
                        tasks.getLast().history.add(new History(LocalDate.now(),"Task Creation"));
                        if (t.status.equals("open")) {
                            t.setStatus("canceled");
                            t.history.add(new History(LocalDate.now(), "Task canceled"));
                        }
                        break;
                    case "monthly":
                        tasks.add(new Task(t.taskName,t.description,"open",t.priority,t.dueDate.plusMonths(1),t.projectName,t.projectDescription,t.isRecurring,t.recurringType,t.recurringDescription,t.tags,t.subtasks,new ArrayList<>()));
                        tasks.getLast().history.add(new History(LocalDate.now(),"Task Creation"));
                        if (t.status.equals("open")) {
                            t.setStatus("canceled");
                            t.history.add(new History(LocalDate.now(), "Task canceled"));
                        }
                        break;
                    case "numberOfDays":
                        String[] part = t.recurringDescription.split("/");
                        LocalDate[] startAndEnd = new LocalDate[part.length];
                        for (int i = 0; i < part.length; i++) {
                            startAndEnd[i] = LocalDate.parse(part[i]);
                        }
                        long daysBetween = ChronoUnit.DAYS.between(startAndEnd[0], startAndEnd[1]);
                        LocalDate startDate = startAndEnd[0].plusDays(daysBetween);
                        LocalDate endDate = startAndEnd[1].plusDays(daysBetween);
                        String newDescription = startDate+" "+endDate;
                        tasks.add(new Task(t.taskName,t.description,"open",t.priority,endDate,t.projectName,t.projectDescription,t.isRecurring,t.recurringType,newDescription,t.tags,t.subtasks,new ArrayList<>()));
                        tasks.getLast().history.add(new History(LocalDate.now(),"Task Creation"));
                        if (t.status.equals("open")) {
                            t.setStatus("canceled");
                            t.history.add(new History(LocalDate.now(), "Task canceled"));
                        }
                        break;
                }
            }
        }
    }
    //split array with /
    public static String[] handleReccuringCreation(){
        Scanner sc = new Scanner(System.in);
        String[] output = new String[3];
        System.out.print("Enter recurrence type:(daily,weekly,monthly,numberOfDays)");
        String choice=sc.nextLine();
        switch(choice) {
            case "daily":
                output[0] = "daily";
                output[1] = "";
                sc.close();
                return output;
            case "weekly":
                output[0] = "weekly";
                System.out.println("Enter the days of the week: (monday=1,sunday=7, separated by /)");
                output[1] = sc.nextLine();
                sc.close();
                return output;
            case "monthly":
                output[0] = "monthly";
                output[1] = "";
                sc.close();
                return output;
            case "numberOfDays":
                System.out.println("Enter start date:");
                output[0] = sc.nextLine();
                System.out.println("Enter end date:");
                output[1] = sc.nextLine();
                output[1]=output[0]+" "+output[1];
                output[0]="numberOfDays";
                sc.close();
                return output;


                default:
                    sc.close();
                    return output;
        }
    }
    // changes tags one at a time
    public static void manageTags(Task task){
        Scanner sc = new Scanner(System.in);
        System.out.println("Select an option");
        System.out.println("1. add tag");
        System.out.println("2. delete tag");
        int c =  sc.nextInt();
        switch (c){
            case 1:
                System.out.println("enter tag name:");
                String tagName = sc.next();
                if (task.tags.contains(tagName)){
                    System.out.println("tag already exists. try again");
                    sc.close();
                    break;
                }
                task.tags.add(tagName);
                task.history.add(new History(LocalDate.now(), "updated tags"));
                sc.close();
                break;
                case 2:
                    System.out.println("enter tag name:");
                    String tagNam = sc.next();
                    if (task.tags.contains(tagNam)){
                        task.tags.remove(tagNam);
                        task.history.add(new History(LocalDate.now(), "deleted tag"));
                        sc.close();
                        break;
                    }
                    System.out.println("tag not found. try again");
                    sc.close();
                    break;
        }
    }
    // =======================
    // Persistence methods
    // =======================

    public static void saveTasks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            out.writeObject(tasks);
            System.out.println("Tasks saved to tasks.dat");
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("subtasks.dat"))) {
            out.writeObject(tasks);
            System.out.println("Tasks saved to subtasks.dat");
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    public static void loadTasks() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("tasks.dat"))) {
            tasks = (List<Task>) in.readObject();
            System.out.println("Tasks loaded from tasks.dat");
        } catch (FileNotFoundException e) {
            System.out.println("No saved tasks found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("subtasks.dat"))) {
            tasks = (List<Task>) in.readObject();
            System.out.println("Tasks loaded from subtasks.dat");
        } catch (FileNotFoundException e) {
            System.out.println("No saved subtasks found. Starting fresh. May have errors");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }
}

// =======================
// Task class (Serializable)
// =======================
class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    String taskName;
    String description;
    String status;
    String priority;
    LocalDate dueDate;
    String projectName;
    String projectDescription;
    boolean isRecurring = false;
    String recurringType;
    String recurringDescription;
    List<String> tags = new ArrayList<>();
    List<Subtask> subtasks=  new ArrayList<>();
    List<History> history = new ArrayList<>();


    public Task(String taskName, String description, String status, String priority, LocalDate dueDate, String projectName, String projectDescription, boolean isRecurring, String recurringType, String recurringDescription, List<String> tags, List<Subtask> subtasks, List<History> history) {
        this.taskName = taskName;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.isRecurring = isRecurring;
        this.recurringType = recurringType;
        this.recurringDescription = recurringDescription;
        this.tags = tags;
        this.subtasks = subtasks;
        this.history = history;
    }

    //TODO:Proper Project implementation here
    @Override
    public String toString() {
        String progress;
        if (subtasks.isEmpty()){
            progress="";
        }else{
            int complete=0;
            for (Subtask subtask : subtasks){
                if (subtask.status.equals("complete")){
                    complete++;
                }
            }
            progress = " | completion ("+complete+"/"+subtasks.size()+")";
        }
        return "TaskName: " + taskName +
                " | Description: " + description +
                " | creation date: " + history.getFirst().timestamp +
                " | Status: " + status +
                " | Priority: " + priority +
                " | DueDate: " + dueDate +
                " | ProjectName: " + projectName +
                " | ProjectDescription: " + projectDescription +
                " | isRecurring: " + isRecurring +
                " | recurringType: " + recurringType+
                " | recurringDescription: " + recurringDescription +
                " | tags: " + tags
                + " | subtasks: " + subtasks+progress;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public void setRecurringType(String recurringType) {
        this.recurringType = recurringType;
    }

    public void setRecurringDescription(String recurringDescription) {
        this.recurringDescription = recurringDescription;
    }

    public List<History> getHistory() {
        return history;
    }

    //TODO:Proper Project implementation here
    //creates a subtask that is open and with recurrence values empty or false
    public void createSubtask(String title){
        this.subtasks.add(new Subtask(title,this.description,"Open", this.priority,this.dueDate,this.projectName,this.projectDescription,false,"","",this.tags,null,new ArrayList<>()));
    }

    public String getStatus() {
        return status;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }
}

// =======================
// History class
// =======================
class History{
    LocalDate timestamp;
    String description;

    public History(LocalDate timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Timestamp: "+this.timestamp+" | Description:"+this.description+"\n";
    }
}



//TODO:Proper Project implementation here
class Subtask extends Task {
    Collaborator collaborator;

    public Subtask(String taskName, String description, String status, String priority, LocalDate dueDate, String projectName, String projectDescription,boolean isRecurring, String recurringType, String recurringDescription, List<String> tags, List<Subtask> subtasks, List<History> history, Collaborator collaborator) {
        super(taskName, description, status, priority, dueDate, projectName, projectDescription, isRecurring, recurringType, recurringDescription, tags, subtasks, history);
        this.collaborator = collaborator;
    }
    public Subtask(String taskName, String description, String status, String priority, LocalDate dueDate, String projectName, String projectDescription,boolean isRecurring, String recurringType, String recurringDescription, List<String> tags, List<Subtask> subtasks, List<History> history) {
        super(taskName, description, status, priority, dueDate, projectName, projectDescription, isRecurring, recurringType, recurringDescription, tags, subtasks, history);
    }

    public void setCollaborator(Collaborator collaborator) {
        this.collaborator = collaborator;
    }
    //TODO:string reformat?
    @Override
    public String toString() {
        return "Subtask{" +
                "collaborator=" + collaborator +
                ", taskName='" + taskName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

//TODO:Proper collaborator implementation here
class Collaborator{

    @Override
    public String toString() {
        return "Collaborator{}";
    }
}