import java.io.*;
import java.util.*;

public class Main {

    // Database of tasks (in memory)
    static List<Task> tasks = new ArrayList<>();

    public static void main(String[] args) {

        // ✅ Load tasks from disk at program start
        loadTasks();

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
            BufferedReader br = new BufferedReader(new FileReader("ImportTasks/" + filePath));
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
            BufferedWriter bw = new BufferedWriter(new FileWriter("ExportTasks/" + filePath));
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
    String subtask;
    String status;
    String priority;
    String dueDate;
    String projectName;
    String projectDescription;
    String collaborator;
    String collaboratorCategory;

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