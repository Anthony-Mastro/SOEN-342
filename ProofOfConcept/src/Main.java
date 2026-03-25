// to import tasks selection option 1 and then type in task1.csv
import java.io.*;
import java.util.*;

public class Main {

    // Database of tasks
    static List<Task> tasks = new ArrayList<>();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println("\n1. Import Tasks from CSV");
            System.out.println("2. Search Tasks");
            System.out.println("3. View All Tasks");
            System.out.println("4. Export Tasks to CSV");
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
                    System.out.println("Goodbye");
                    return;
            }
        }
    }

    // Import CSV
    public static void importCSV(String filePath) {

        try {
            BufferedReader br = new BufferedReader(new FileReader("ImportTasks/" + filePath));
            String line;

            while ((line = br.readLine()) != null) {

                String[] values = line.split(",");
// Extract all fields from the CSV array
                String taskName            = values[0];
                String description         = values[1];
                String subtask             = values[2]; // optional
                String status              = values[3];
                String priority            = values[4];
                String dueDate             = values[5];
                String projectName         = values[6]; // optional
                String projectDescription  = values[7]; // optional
                String collaborator        = values[8]; // optional
                String collaboratorCategory= values[9]; // optional

// Create Task object using the new constructor
                Task task = new Task(
                        taskName,
                        description,
                        subtask,
                        status,
                        priority,
                        dueDate,
                        projectName,
                        projectDescription,
                        collaborator,
                        collaboratorCategory
                );
                tasks.add(task);
            }

            br.close();
            System.out.println("Tasks imported successfully");

        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    // Export CSV
    public static void exportCSV(String filePath) {

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("ExportTasks/"+ filePath));

            for (Task t : tasks) {
                bw.write(
                        t.taskName + "," +
                                t.description + "," +
                                t.subtask + "," +
                                t.status + "," +
                                t.priority + "," +
                                t.dueDate + "," +
                                t.projectName + "," +
                                t.projectDescription + "," +
                                t.collaborator + "," +
                                t.collaboratorCategory
                );
                bw.newLine();

            }

            bw.close();
            System.out.println("Tasks exported successfully");

        } catch (IOException e) {
            System.out.println("Error writing file");
        }
    }

    // Search Tasks
    public static void searchTasks(String keyword) {

        for (Task t : tasks) {
            if (t.taskName.contains(keyword) || t.description.contains(keyword)) {
                System.out.println(t);
            }
        }
    }

    // View All Tasks
    public static void viewTasks() {

        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }

        for (Task t : tasks) {
            System.out.println(t);
        }
    }
}

// Task class
class Task {

    String taskName;
    String description;
    String subtask;             // optional
    String status;
    String priority;
    String dueDate;
    String projectName;         // optional
    String projectDescription;  // optional
    String collaborator;        // optional
    String collaboratorCategory;// optional

    public Task(String taskName,
                String description,
                String subtask,
                String status,
                String priority,
                String dueDate,
                String projectName,
                String projectDescription,
                String collaborator,
                String collaboratorCategory) {

        this.taskName = taskName;
        this.description = description;
        this.subtask = subtask;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.collaborator = collaborator;
        this.collaboratorCategory = collaboratorCategory;
    }

    @Override
    public String toString() {
        return "TaskName: " + taskName +
                " | Description: " + description +
                " | Subtask: " + subtask +
                " | Status: " + status +
                " | Priority: " + priority +
                " | DueDate: " + dueDate +
                " | ProjectName: " + projectName +
                " | ProjectDescription: " + projectDescription +
                " | Collaborator: " + collaborator +
                " | CollaboratorCategory: " + collaboratorCategory;
    }
}