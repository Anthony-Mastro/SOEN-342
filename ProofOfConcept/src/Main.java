import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class Main {

    // Database of tasks (in memory)
    static List<Task> tasks = new ArrayList<>();

    public static void main(String[] args) {

        // ✅ Load tasks from disk at program start
        loadTasks();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            //todo fix menu
            System.out.println("\n1. Import Tasks from CSV");
            System.out.println("2. Search Tasks");
            System.out.println("3. View All Tasks");
            System.out.println("4. Export Tasks to CSV");
            System.out.println("4. Update task");//project, tag sub and recurring
            System.out.println("4. Create task");//sub and recurring
            System.out.println("4. Assign self to task");//barely a proper feature??
            System.out.println("4. Export to ical");
            System.out.println("4. Edit collaborator loads");
            System.out.println("4. View Task History");
            System.out.println("5. Exit");

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
                    // ✅ Save tasks to disk before exiting
                    saveTasks();
                    System.out.println("Goodbye");
                    return;
            }
        }
    }

    // =======================
    // CSV Import / Export (same as before)
    // =======================
    public static void importCSV(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\kevin\\IdeaProjects\\SOEN-342\\ProofOfConcept\\ImportTasks\\" + filePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Task task = new Task(
                        values[0], values[1], values[2], values[3], values[4],
                        values[5], values[6], values[7], values[8], values[9]
                );
                tasks.add(task);
            }
            br.close();
            System.out.println("Tasks imported successfully");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

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
    // Search / View
    // =======================
    public static void searchTasks(String keyword) {
        for (Task t : tasks) {
            if (t.taskName.contains(keyword) || t.description.contains(keyword)) {
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
    public static void createTask() {}
    public static void updateTask() {}
    public static void viewHistory(){}
    public static boolean checkDueDates(){
        return false;
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
    }
    //Todo:do recurring ckeck here
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
    String dueDate;
    String projectName;
    String projectDescription;
    boolean isRecurring = false;
    String recurringType;
    String recurringDescription;
    List<String> tags = new ArrayList<>();
    List<Subtask> subtasks=  new ArrayList<>();
    List<History> history = new ArrayList<>();


    public Task(String taskName, String description, String status, String priority, String dueDate, String projectName, String projectDescription, boolean isRecurring, String recurringType, String recurringDescription, List<String> tags, List<Subtask> subtasks, List<History> history) {
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
    //TODO:fix to string
    @Override
    public String toString() {
        return "TaskName: " + taskName +
                " | Description: " + description +
                " | Status: " + status +
                " | Priority: " + priority +
                " | DueDate: " + dueDate +
                " | ProjectName: " + projectName +
                " | ProjectDescription: " + projectDescription +
                " | isRecurring: " + isRecurring
                ;
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

    public void setDueDate(String dueDate) {
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
    public void createSubtask(String title){
        this.subtasks.add(new Subtask(title,this.description,"Open", this.priority,this.dueDate,this.projectName,this.projectDescription,false,"","",this.tags,null,new ArrayList<>()));
    }

}

// =======================
// History class
// =======================
class History{
    LocalDate timestamp;
    String description;

    @Override
    public String toString() {
        return "Timestamp: "+this.timestamp+" | Description:"+this.description;
    }
}



//TODO:Proper Project implementation here
class Subtask extends Task {
    Collaborator collaborator;

    public Subtask(String taskName, String description, String status, String priority, String dueDate, String projectName, String projectDescription,boolean isRecurring, String recurringType, String recurringDescription, List<String> tags, List<Subtask> subtasks, List<History> history, Collaborator collaborator) {
        super(taskName, description, status, priority, dueDate, projectName, projectDescription, isRecurring, recurringType, recurringDescription, tags, subtasks, history);
        this.collaborator = collaborator;
    }
    public Subtask(String taskName, String description, String status, String priority, String dueDate, String projectName, String projectDescription,boolean isRecurring, String recurringType, String recurringDescription, List<String> tags, List<Subtask> subtasks, List<History> history) {
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