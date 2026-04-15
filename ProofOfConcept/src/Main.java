import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class Main {
    static List<Project> projects = new ArrayList<>();
    // Database of tasks (in memory)
    static List<Task> tasks = new ArrayList<>();
    // Single shared scanner — never close it mid-program; closing System.in is permanent
    static Scanner scanner = new Scanner(System.in);

    static List<Collaborator> collaborators = new ArrayList<>();

    public static void main(String[] args) {
        loadTasks();
        checkRecurringTasks();

        while (true) {
            System.out.println("\n1. Import Tasks from CSV");
            System.out.println("2. Search Tasks");
            System.out.println("3. View All Tasks");
            System.out.println("4. Export Tasks to CSV");
            System.out.println("5. Create task / subtask");
            System.out.println("6. Update task");
            System.out.println("7. View Subtasks of a Task");
            System.out.println("8. Update Subtask Status");
            System.out.println("9. View Task History");
            System.out.println("10. Create Project");
            System.out.println("11. View Projects");
            System.out.println("12. Add Task to Project");
            System.out.println("13. Export to iCal");
            System.out.println("14. Create Collaborator");
            System.out.println("15. View Collaborators");
            System.out.println("16. Assign Task to Collaborator");
            System.out.println("17. View Overloaded Collaborators");
            System.out.println("18. Exit");

            System.out.print("Choose option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume trailing newline after nextInt()

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
                    String taskName = scanner.nextLine();
                    Optional<Task> found = findTask(taskName);
                    if (!found.isPresent()) {
                        System.out.println("Task not found.");
                    } else {
                        System.out.print("Enter attribute to update (title/description/status/priority/dueDate/projectName/projectDescription/Tag): ");
                        String attribute = scanner.nextLine();
                        System.out.print("Enter new value: ");
                        String value = scanner.nextLine();
                        updateTask(found.get(), attribute, value);
                    }
                    break;

                case 7:
                    viewSubtasks();
                    break;
                case 8:
                    updateSubtaskStatus();
                    break;

                case 9:
                    viewHistory();
                    break;

                case 10:

                    System.out.print("Project name: ");
                    String pname = scanner.nextLine();

                    System.out.print("Description (optional): ");
                    String pdesc = scanner.nextLine();

                    projects.add(new Project(pname, pdesc));
                    System.out.println("Project created.");
                    break;

                case 11:
                    viewProjects();
                    break;

                case 12:
                    assignTaskToProject();
                    break;

                case 13:
                    System.out.print("Enter .ics file name: ");
                    String icalFile = scanner.nextLine();
                    exportToICal(icalFile);
                    break;

                case 14: // Create a Collaborator
                    createCollaborator();
                    break;

                case 15: // View Collaborators
                    viewCollaborators();
                    break;

                case 16: // Assign Task to Collaborator
                    System.out.print("Enter parent task name: ");
                    String tName = scanner.nextLine();
                    Optional<Task> tOpt = tasks.stream().filter(t -> t.taskName.equalsIgnoreCase(tName)).findFirst();

                    System.out.print("Enter collaborator name: ");
                    String cName = scanner.nextLine();
                    Collaborator collab = null;
                    for (Collaborator c : collaborators) {
                        if (c.name.equalsIgnoreCase(cName)) {
                            collab = c;
                            break;
                        }
                    }

                    if (tOpt.isPresent() && collab != null) {
                        System.out.print("Enter a title for this new subtask: ");
                        String subTitle = scanner.nextLine();
                        assignCollaborator(tOpt.get(), collab, subTitle);
                    } else {
                        System.out.println("Task or Collaborator not found.");
                    }
                    break;

                case 17: // View Overloaded Collaborators
                    viewOverloadedCollaborators();
                    break;

                case 18:
                    saveTasks();
                    System.out.println("Goodbye.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid option. Please choose 1-8.");
            }
        }
    }

    // =======================
    // Helper: find a task by name
    // =======================
    public static Optional<Task> findTask(String name) {
        for (Task t : tasks) {
            if (t.taskName.equals(name)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    // =======================
    // CSV Import / Export
    // =======================
    // Field order: taskName,description,status,priority,dueDate,projectName,
    //              projectDescription,isRecurring,recurringType,recurringDescription,
    //              tags(/-separated),subtask names(/-separated)
    public static void importCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader("ProofOfConcept/ImportTasks/" + filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",", -1);
                if (v.length < 12) {
                    System.out.println("Skipping malformed line: " + line);
                    continue;
                }
                List<String> tags = v[10].isEmpty()
                        ? new ArrayList<>()
                        : new ArrayList<>(Arrays.asList(v[10].split("/")));
                List<History> history = new ArrayList<>();
                history.add(new History(LocalDate.now(), "imported from CSV"));
                Task task = new Task(
                        v[0], v[1], v[2], v[3],
                        v[4].equals("null") ? null : LocalDate.parse(v[4]),
                        v[5], v[6],
                        Boolean.parseBoolean(v[7]),
                        v[8], v[9], tags,
                        new ArrayList<>(), history
                );
                tasks.add(task);
            }
            System.out.println("Tasks imported successfully.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Tags and subtask names are joined with "/" so they don't break the comma split on re-import
    public static void exportCSV(String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("ProofOfConcept/ExportTasks/" + filePath))) {
            for (Task t : tasks) {
                // Build tag string (/-separated)
                StringBuilder tagStr = new StringBuilder();
                for (int i = 0; i < t.tags.size(); i++) {
                    if (i > 0) tagStr.append("/");
                    tagStr.append(t.tags.get(i));
                }
                // Build subtask name string (/-separated)
                StringBuilder subStr = new StringBuilder();
                for (int i = 0; i < t.subtasks.size(); i++) {
                    if (i > 0) subStr.append("/");
                    subStr.append(t.subtasks.get(i).taskName);
                }
                bw.write(
                        t.taskName + "," +
                                t.description + "," +
                                t.status + "," +
                                t.priority + "," +
                                t.dueDate + "," +
                                t.projectName + "," +
                                t.projectDescription + "," +
                                t.isRecurring + "," +
                                t.recurringType + "," +
                                t.recurringDescription + "," +
                                tagStr + "," +
                                subStr
                );
                bw.newLine();
            }
            System.out.println("Tasks exported successfully.");
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    // =======================
    // Search / View
    // =======================
    public static void searchTasks(String keyword) {
        boolean found = false;
        for (Task t : tasks) {
            if (t.taskName.contains(keyword) ||
                    t.description.contains(keyword) ||
                    t.projectName.contains(keyword) ||
                    t.projectDescription.contains(keyword) ||
                    t.status.contains(keyword) ||
                    t.tags.toString().contains(keyword)) {
                System.out.println(t);
                found = true;
            }
        }
        if (!found) System.out.println("No matching tasks found.");
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

    // Returns true when adding another due-date-less task is SAFE (under the limit).
    // Returns false when the limit is already hit and a due date must be provided.
    public static boolean canOmitDueDate() {
        int missingDate = 0;
        for (Task t : tasks) {
            if (t.dueDate == null) missingDate++;
            for (Subtask s : t.subtasks) {
                if (s.dueDate == null) missingDate++;
            }
        }
        return missingDate < 50;
    }

    // =======================
    // Create task
    // =======================
    public static void createTask() {
        String option;
        System.out.println("What type of event would you like to create?");
        System.out.println("1. Normal task / recurring task");
        System.out.println("2. Subtask");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline after nextInt()

        switch (choice) {
            case 1: {
                System.out.println("Task name:");
                String taskName = scanner.nextLine();

                String description = "";
                System.out.println("Add description? (Y/N)");
                if (scanner.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println("Description:");
                    description = scanner.nextLine();
                }

                System.out.println("Status:");
                String status = scanner.nextLine();

                System.out.println("Priority:");
                String priority = scanner.nextLine();

                LocalDate dueDate = null;
                System.out.println("Add due date? (Y/N)");
                if (scanner.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println("Due date (YYYY-MM-DD):");
                    dueDate = LocalDate.parse(scanner.nextLine());
                } else if (!canOmitDueDate()) {
                    System.out.println("Too many tasks missing a due date. Please enter one (YYYY-MM-DD):");
                    dueDate = LocalDate.parse(scanner.nextLine());
                }



                String projectName = "";
                String projectDescription = "";

                while (true) {
                    System.out.print("Assign to existing project? (Y/N): ");
                    if (!scanner.nextLine().equalsIgnoreCase("Y")) {
                        break;
                    }

                    System.out.print("Enter project name (or type 'cancel' to stop): ");
                    String pname = scanner.nextLine();

                    if (pname.equalsIgnoreCase("cancel")) {
                        break;
                    }

                    Optional<Project> proj = findProject(pname);

                    if (proj.isPresent()) {
                        projectName = pname;
                        projectDescription = proj.get().description;
                        System.out.println("Project assigned successfully.");
                        break;
                    } else {
                        System.out.println("Project not found. Try again.");
                    }
                }



                boolean recurring = false;
                String recurringType = "";
                String recurringDescription = "";
                System.out.println("Is this task recurring? (Y/N)");
                if (scanner.nextLine().equalsIgnoreCase("Y")) {
                    recurring = true; // FIX: was never set to true
                    String[] recurrHandler = handleRecurringCreation();
                    recurringType = recurrHandler[0];
                    recurringDescription = recurrHandler[1];
                }

                List<String> tags = new ArrayList<>();
                boolean addingTags = true;
                while (addingTags) {
                    System.out.println("Add a tag? (Y/N)");
                    if (scanner.nextLine().equalsIgnoreCase("Y")) {
                        System.out.println("Tag name:");
                        tags.add(scanner.nextLine());
                    } else {
                        addingTags = false;
                    }
                }

                List<History> history = new ArrayList<>();
                history.add(new History(LocalDate.now(), "task created"));
                tasks.add(new Task(taskName, description, status, priority, dueDate,
                        projectName, projectDescription, recurring,
                        recurringType, recurringDescription,
                        tags, new ArrayList<>(), history));
                System.out.println("Task created.");
                break;
            }

            case 2: {
                System.out.println("Name of the parent task:");
                String parentName = scanner.nextLine();
                Optional<Task> parent = findTask(parentName);
                if (!parent.isPresent()) {
                    System.out.println("Task not found.");
                    break;
                }
                Task parentTask = parent.get();
                if (parentTask.subtasks.size() >= 20) {
                    System.out.println("This task already has 20 subtasks.");
                    break;
                }
                if (!canOmitDueDate()) {
                    System.out.println("Too many tasks missing due dates. Please update an existing task first.");
                    break;
                }
                System.out.println("Subtask name:");
                String subName = scanner.nextLine();
                parentTask.createSubtask(subName);
                parentTask.history.add(new History(LocalDate.now(), "subtask added"));
                System.out.println("Subtask created.");
                break;
            }

            default:
                System.out.println("Invalid choice.");
        }
    }

    // =======================
    // Update task
    // =======================
    public static void updateTask(Task task, String attribute, String value) {
        switch (attribute) {
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
                if (value.isEmpty() && !canOmitDueDate()) {
                    System.out.println("50 due-date limit reached. Enter a valid date (YYYY-MM-DD):");
                    value = scanner.nextLine();
                }
                if (!value.isEmpty()) {
                    task.setDueDate(LocalDate.parse(value));
                    task.history.add(new History(LocalDate.now(), "task due date updated"));
                }
                break; // FIX: was missing, causing fall-through into projectName

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

            default:
                System.out.println("Unknown attribute: " + attribute);
        }
    }

    // =======================
    // View history
    // =======================
    public static void viewHistory() {
        System.out.print("Which task history would you like to view? ");
        String taskName = scanner.nextLine();
        Optional<Task> found = findTask(taskName);
        if (!found.isPresent()) {
            System.out.println("Task not found.");
        } else {
            for (History h : found.get().getHistory()) {
                System.out.print(h);
            }
        }
    }

    // =======================
    // Recurring task guard (runs on startup)
    // =======================
    public static void checkRecurringTasks() {
        LocalDate today = LocalDate.now();
        List<Task> toAdd = new ArrayList<>(); // avoid modifying list while iterating

        for (Task t : tasks) {
            if (!t.isRecurring || t.dueDate == null || !t.dueDate.isBefore(today)) continue;

            Task next = null;
            switch (t.recurringType) {
                case "daily":
                    next = new Task(t.taskName, t.description, "open", t.priority,
                            t.dueDate.plusDays(1), t.projectName, t.projectDescription,
                            true, t.recurringType, t.recurringDescription,
                            new ArrayList<>(t.tags), new ArrayList<>(), new ArrayList<>());
                    break;

                case "weekly": {
                    String[] parts = t.recurringDescription.split("/");
                    boolean[] days = new boolean[7];
                    for (int i = 0; i < parts.length && i < 7; i++) {
                        days[i] = Boolean.parseBoolean(parts[i]);
                    }
                    int dow = t.dueDate.getDayOfWeek().getValue() % 7; // 0=Sun..6=Sat using ISO mod
                    // advance to next enabled day
                    for (int i = 1; i <= 7; i++) {
                        int candidate = (dow + i) % 7;
                        if (days[candidate]) {
                            LocalDate nextDate = t.dueDate.with(
                                    TemporalAdjusters.next(DayOfWeek.of(candidate == 0 ? 7 : candidate)));
                            next = new Task(t.taskName, t.description, "open", t.priority,
                                    nextDate, t.projectName, t.projectDescription,
                                    true, t.recurringType, t.recurringDescription,
                                    new ArrayList<>(t.tags), new ArrayList<>(), new ArrayList<>());
                            break;
                        }
                    }
                    break;
                }

                case "monthly":
                    next = new Task(t.taskName, t.description, "open", t.priority,
                            t.dueDate.plusMonths(1), t.projectName, t.projectDescription,
                            true, t.recurringType, t.recurringDescription,
                            new ArrayList<>(t.tags), new ArrayList<>(), new ArrayList<>());
                    break;

                case "numberOfDays": {
                    // FIX: description stored as "startDate/endDate" (slash-separated)
                    String[] part = t.recurringDescription.split("/");
                    if (part.length < 2) break;
                    LocalDate start = LocalDate.parse(part[0]);
                    LocalDate end = LocalDate.parse(part[1]);
                    long span = ChronoUnit.DAYS.between(start, end);
                    LocalDate newStart = start.plusDays(span);
                    LocalDate newEnd = end.plusDays(span);
                    next = new Task(t.taskName, t.description, "open", t.priority,
                            newEnd, t.projectName, t.projectDescription,
                            true, t.recurringType, newStart + "/" + newEnd,
                            new ArrayList<>(t.tags), new ArrayList<>(), new ArrayList<>());
                    break;
                }
            }

            if (next != null) {
                next.history.add(new History(LocalDate.now(), "task created by recurrence"));
                toAdd.add(next);
                if (t.status.equals("open")) {
                    t.setStatus("canceled");
                    t.history.add(new History(LocalDate.now(), "task canceled by recurrence"));
                }
            }
        }
        tasks.addAll(toAdd);
    }

    // =======================
    // Recurring creation helper
    // =======================
    // FIX: numberOfDays now stored as "startDate/endDate" (slash-separated) to match checkRecurringTasks
    public static String[] handleRecurringCreation() {
        String[] output = new String[2];
        System.out.print("Recurrence type (daily/weekly/monthly/numberOfDays): ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "daily":
                output[0] = "daily";
                output[1] = "";
                break;
            case "weekly":
                output[0] = "weekly";
                System.out.println("Days of the week as true/false for Mon-Sun, separated by /");
                System.out.println("Example: true/false/true/false/true/false/false  (Mon, Wed, Fri)");
                output[1] = scanner.nextLine();
                break;
            case "monthly":
                output[0] = "monthly";
                output[1] = "";
                break;
            case "numberOfDays":
                output[0] = "numberOfDays";
                System.out.println("Start date (YYYY-MM-DD):");
                String start = scanner.nextLine();
                System.out.println("End date (YYYY-MM-DD):");
                String end = scanner.nextLine();
                output[1] = start + "/" + end; // FIX: use "/" not " "
                break;
            default:
                System.out.println("Unknown recurrence type. Defaulting to daily.");
                output[0] = "daily";
                output[1] = "";
        }
        return output;
    }

    // =======================
    // Tag management
    // =======================
    public static void manageTags(Task task) {
        System.out.println("1. Add tag   2. Delete tag");
        int c = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Enter tag name:");
        String tagName = scanner.nextLine();
        switch (c) {
            case 1:
                if (task.tags.contains(tagName)) {
                    System.out.println("Tag already exists.");
                } else {
                    task.tags.add(tagName);
                    task.history.add(new History(LocalDate.now(), "tag added"));
                }
                break;
            case 2:
                if (task.tags.remove(tagName)) {
                    task.history.add(new History(LocalDate.now(), "tag deleted"));
                } else {
                    System.out.println("Tag not found.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // =======================
    // Persistence
    // =======================
    public static void saveTasks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("tasks.dat"))) {
            // Save both lists to the same file
            out.writeObject(tasks);
            out.writeObject(projects);
            out.writeObject(collaborators);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadTasks() {
        File file = new File("tasks.dat");
        if (!file.exists()) {
            System.out.println("No saved data found. Starting fresh.");
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            // Read them back in the SAME order: Tasks then Projects
            tasks = (List<Task>) in.readObject();
            projects = (List<Project>) in.readObject();
            collaborators = (List<Collaborator>) in.readObject();
            System.out.println("Data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    public static Optional<Project> findProject(String name) {
        for (Project p : projects) {
            if (p.name.equals(name)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public static void viewProjects() {
        if (projects.isEmpty()) {
            System.out.println("No projects available.");
            return;
        }

        for (Project p : projects) {
            System.out.println("\n" + p);

            if (p.tasks.isEmpty()) {
                System.out.println("  No tasks in this project.");
            } else {
                System.out.println("  Tasks:");
                for (Task t : p.tasks) {
                    System.out.println("   - " + t.taskName + " (Status: " + t.status + ")");
                }
            }
        }
    }

    public static void assignTaskToProject() {
        System.out.print("Enter task name: ");
        String taskName = scanner.nextLine();

        Optional<Task> taskOpt = findTask(taskName);
        if (!taskOpt.isPresent()) {
            System.out.println("Task not found.");
            return;
        }

        System.out.print("Enter project name: ");
        String projectName = scanner.nextLine();

        Optional<Project> projOpt = findProject(projectName);
        if (!projOpt.isPresent()) {
            System.out.println("Project not found.");
            return;
        }

        Task task = taskOpt.get();
        Project project = projOpt.get();

        project.addTask(task);

        // Also update task fields (important for consistency)
        task.setProjectName(project.name);
        task.setProjectDescription(project.description);

        System.out.println("Task added to project.");
    }

    public static void exportToICal(String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("ProofOfConcept/ICAL/" + fileName+".ics"))) {

            // Calendar header
            bw.write("BEGIN:VCALENDAR\n");
            bw.write("VERSION:2.0\n");
            bw.write("PRODID:-//TaskManager//EN\n");

            for (Task t : tasks) {

                if (t.dueDate == null) continue; // skip tasks without dates

                String date = t.dueDate.toString().replace("-", "");

                bw.write("BEGIN:VEVENT\n");

                // Unique ID
                bw.write("UID:" + UUID.randomUUID() + "\n");

                // Timestamp (now)
                bw.write("DTSTAMP:" + LocalDate.now().toString().replace("-", "") + "T000000Z\n");

                // Event date (all-day event)
                bw.write("DTSTART;VALUE=DATE:" + date + "\n");
                bw.write("DTEND;VALUE=DATE:" + date + "\n");

                // Title
                bw.write("SUMMARY:" + t.taskName + "\n");

                // Description
                bw.write("DESCRIPTION:" + t.description + "\n");

                // Optional: project info
                if (t.projectName != null && !t.projectName.isEmpty()) {
                    bw.write("CATEGORIES:" + t.projectName + "\n");
                }

                bw.write("END:VEVENT\n");
            }

            bw.write("END:VCALENDAR\n");

            System.out.println("iCal file exported successfully.");

        } catch (IOException e) {
            System.out.println("Error writing iCal file: " + e.getMessage());
        }
    }


    public static void assignCollaborator(Task parentTask, Collaborator collab, String subtaskTitle) {
        // Check Iteration III Overload Constraint
        long openTasks = tasks.stream()
                .flatMap(t -> t.subtasks.stream())
                .filter(s -> s.collaborator != null && s.collaborator.name.equals(collab.name) && s.status.equalsIgnoreCase("open"))
                .count();

        if (openTasks >= collab.getTaskLimit()) {
            System.out.println("Error: Collaborator " + collab.name + " is at their limit of " + collab.getTaskLimit());
            return;
        }

        // Create the subtask and link it
        Subtask newSub = new Subtask(subtaskTitle, parentTask.description, "open",
                parentTask.priority, parentTask.dueDate,
                parentTask.projectName, parentTask.projectDescription,
                false, "", "", new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), collab);

        parentTask.subtasks.add(newSub);
        System.out.println("Assigned " +  subtaskTitle + " to " + collab.name );
    }


    public static void viewOverloadedCollaborators() {
        System.out.println("\n--- Overloaded Collaborators ---");
        boolean anyoneOverloaded = false;

        for (Collaborator c : collaborators) {
            // Count all subtasks assigned to this person that are still "open"
            long count = tasks.stream()
                    .flatMap(t -> t.subtasks.stream())
                    .filter(s -> s.collaborator != null &&
                            s.collaborator.name.equals(c.name) &&
                            s.status.equalsIgnoreCase("open"))
                    .count();

            if (count > c.getTaskLimit()) {
                System.out.println("ALERT: " + c.name + " (" + c.category + ") is overloaded!");
                System.out.println("  Current Open Tasks: " + count);
                System.out.println("  Allowed Limit: " + c.getTaskLimit());
                System.out.println("---------------------------------");
                anyoneOverloaded = true;
            }
        }

        if (!anyoneOverloaded) {
            System.out.println("No collaborators are currently overloaded.");
        }
    }

    public static void createCollaborator() {
        System.out.print("Enter collaborator name: ");
        String name = scanner.nextLine();

        System.out.println("Select Category:");
        System.out.println("1. Senior (Limit: 2)");
        System.out.println("2. Intermediate (Limit: 5)");
        System.out.println("3. Junior (Limit: 10)");
        System.out.print("Choice: ");

        int catChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        String category = "";
        switch (catChoice) {
            case 1: category = "Senior"; break;
            case 2: category = "Intermediate"; break;
            case 3: category = "Junior"; break;
            default:
                System.out.println("Invalid choice. Defaulting to Junior.");
                category = "Junior";
        }

        collaborators.add(new Collaborator(name, category));
        System.out.println("Collaborator " + name + " created successfully.");
    }

    public static void viewCollaborators() {
        if (collaborators.isEmpty()) {
            System.out.println("No collaborators have been created yet.");
            return;
        }

        System.out.println("\n--- Registered Collaborators ---");
        for (Collaborator c : collaborators) {
            // Count open tasks for this specific collaborator
            long openCount = tasks.stream()
                    .flatMap(t -> t.subtasks.stream())
                    .filter(s -> s.collaborator != null &&
                            s.collaborator.name.equalsIgnoreCase(c.name) &&
                            s.status.equalsIgnoreCase("open"))
                    .count();

            System.out.println("Name: " + c.name);
            System.out.println("  Category: " + c.category);
            System.out.println("  Workload: " + openCount + "/" + c.getTaskLimit() + " open tasks");
            System.out.println("---------------------------------");
        }
    }

    public static void viewSubtasks() {
        System.out.print("Enter parent task name to view its subtasks: ");
        String parentName = scanner.nextLine();

        Optional<Task> parentOpt = tasks.stream()
                .filter(t -> t.taskName.equalsIgnoreCase(parentName))
                .findFirst();

        if (parentOpt.isPresent()) {
            Task parent = parentOpt.get();
            if (parent.subtasks.isEmpty()) {
                System.out.println("This task has no subtasks.");
            } else {
                System.out.println("\n--- Subtasks for: " + parent.taskName + " ---");
                for (int i = 0; i < parent.subtasks.size(); i++) {
                    Subtask s = parent.subtasks.get(i);
                    String collabName = (s.collaborator != null) ? s.collaborator.name : "Unassigned";
                    System.out.println(i + ". [" + s.status.toUpperCase() + "] " + s.taskName +
                            " (Assigned to: " + collabName + ")");
                }
            }
        } else {
            System.out.println("Parent task not found.");
        }
    }

    public static void updateSubtaskStatus() {
        System.out.print("Enter parent task name: ");
        String parentName = scanner.nextLine();

        Optional<Task> parentOpt = tasks.stream()
                .filter(t -> t.taskName.equalsIgnoreCase(parentName))
                .findFirst();

        if (parentOpt.isPresent()) {
            Task parent = parentOpt.get();
            if (parent.subtasks.isEmpty()) {
                System.out.println("No subtasks found for this task.");
                return;
            }

            // Show subtasks so user can pick one
            for (int i = 0; i < parent.subtasks.size(); i++) {
                System.out.println(i + ". " + parent.subtasks.get(i).taskName + " [Current: " + parent.subtasks.get(i).status + "]");
            }

            System.out.print("Enter index of subtask to update: ");
            int index = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (index >= 0 && index < parent.subtasks.size()) {
                Subtask sub = parent.subtasks.get(index);
                System.out.print("Enter new status (open/closed): ");
                String newStatus = scanner.nextLine();

                sub.status = newStatus;
                sub.history.add(new History(LocalDate.now(), "Subtask status updated to " + newStatus));

                System.out.println("Subtask '" + sub.taskName + "' updated to " + newStatus + ".");
                if (newStatus.equalsIgnoreCase("closed")) {
                    System.out.println("Workload updated for collaborator: " + (sub.collaborator != null ? sub.collaborator.name : "N/A"));
                }
            } else {
                System.out.println("Invalid index.");
            }
        } else {
            System.out.println("Parent task not found.");
        }
    }
}

// =======================
// Task class
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
    List<Subtask> subtasks = new ArrayList<>();
    List<History> history = new ArrayList<>();

    public Task(String taskName, String description, String status, String priority,
                LocalDate dueDate, String projectName, String projectDescription,
                boolean isRecurring, String recurringType, String recurringDescription,
                List<String> tags, List<Subtask> subtasks, List<History> history) {
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
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>(); // FIX: guard null
        this.history = history;
    }

    @Override
    public String toString() {
        String progress = "";
        if (!subtasks.isEmpty()) {
            int complete = 0;
            for (Subtask s : subtasks) {
                if ("complete".equalsIgnoreCase(s.status)) complete++;
            }
            progress = " | completion (" + complete + "/" + subtasks.size() + ")";
        }
        return "Task: " + taskName +
                " | Desc: " + description +
                " | Created: " + (history.isEmpty() ? "unknown" : history.get(0).timestamp) +
                " | Status: " + status +
                " | Priority: " + priority +
                " | Due: " + dueDate +
                " | Project: " + projectName +
                " | Recurring: " + isRecurring +
                (isRecurring ? " (" + recurringType + ")" : "") +
                " | Tags: " + tags +
                " | Subtasks: " + subtasks.size() + progress;
    }

    // Creates a subtask inheriting sensible defaults from the parent
    public void createSubtask(String title) {
        List<History> subHistory = new ArrayList<>();
        subHistory.add(new History(LocalDate.now(), "subtask created"));
        // FIX: pass correct 12-arg constructor; subtasks list is new empty list, not null
        this.subtasks.add(new Subtask(
                title, this.description, "open", this.priority,
                this.dueDate, this.projectName, this.projectDescription,
                false, "", "", new ArrayList<>(this.tags),
                new ArrayList<>(), subHistory
        ));
    }

    public void setTaskName(String taskName)           { this.taskName = taskName; }
    public void setDescription(String description)     { this.description = description; }
    public void setStatus(String status)               { this.status = status; }
    public void setPriority(String priority)           { this.priority = priority; }
    public void setDueDate(LocalDate dueDate)          { this.dueDate = dueDate; }
    public void setProjectName(String projectName)     { this.projectName = projectName; }
    public void setProjectDescription(String pd)       { this.projectDescription = pd; }
    public void setRecurring(boolean recurring)        { this.isRecurring = recurring; }
    public void setRecurringType(String rt)            { this.recurringType = rt; }
    public void setRecurringDescription(String rd)     { this.recurringDescription = rd; }
    public String getStatus()                          { return status; }
    public List<Subtask> getSubtasks()                 { return subtasks; }
    public List<History> getHistory()                  { return history; }
}

// =======================
// History class
// =======================
class History implements Serializable { // FIX: must be Serializable since Task is
    private static final long serialVersionUID = 1L;

    LocalDate timestamp;
    String description;

    public History(LocalDate timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    @Override
    public String toString() {
        return "  [" + timestamp + "] " + description + "\n";
    }
}

// =======================
// Subtask class
// =======================
class Subtask extends Task {
    private static final long serialVersionUID = 2L;

    Collaborator collaborator;

    // Constructor without collaborator (used by createSubtask)
    public Subtask(String taskName, String description, String status, String priority,
                   LocalDate dueDate, String projectName, String projectDescription,
                   boolean isRecurring, String recurringType, String recurringDescription,
                   List<String> tags, List<Subtask> subtasks, List<History> history) {
        super(taskName, description, status, priority, dueDate, projectName, projectDescription,
                isRecurring, recurringType, recurringDescription, tags, subtasks, history);
    }

    // Constructor with collaborator
    public Subtask(String taskName, String description, String status, String priority,
                   LocalDate dueDate, String projectName, String projectDescription,
                   boolean isRecurring, String recurringType, String recurringDescription,
                   List<String> tags, List<Subtask> subtasks, List<History> history,
                   Collaborator collaborator) {
        super(taskName, description, status, priority, dueDate, projectName, projectDescription,
                isRecurring, recurringType, recurringDescription, tags, subtasks, history);
        this.collaborator = collaborator;
    }

    public void setCollaborator(Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    @Override
    public String toString() {
        return "Subtask{name='" + taskName + "', status='" + status + "', collaborator=" + collaborator + "}";
    }
}

// =======================
// Collaborator class
// =======================
class Collaborator implements Serializable { // FIX: must be Serializable
    private static final long serialVersionUID = 3L;

    @Override
    public String toString() {
        return "Collaborator{}";
    }

    String name;
    String category; // Senior, Intermediate, or Junior

    public Collaborator(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public int getTaskLimit() {
        if (category.equalsIgnoreCase("Senior")) return 2;
        if (category.equalsIgnoreCase("Intermediate")) return 5;
        if (category.equalsIgnoreCase("Junior")) return 10;
        return 0;
    }


}



class Project implements Serializable {
    private static final long serialVersionUID = 4L;

    String name;
    String description;
    List<Task> tasks = new ArrayList<>();

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    public void removeTask(String taskName) {
        tasks.removeIf(t -> t.taskName.equals(taskName));
    }

    @Override
    public String toString() {
        return "Project: " + name +
                " | Desc: " + description +
                " | Tasks: " + tasks.size();
    }

}


