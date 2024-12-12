import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class FolderProcessor extends RecursiveTask<List<String>> {

    private static final long serialVersionUID = 1L;
    // This attribute will store the full path of the folder this task is going to
    // process.
    private final String path;
    // This attribute will store the name of the extension of the files this
    // task is going to look for.
    private final String extension;

    // Implement the constructor of the class to initialize its attributes
    public FolderProcessor(String path, String extension) {
        this.path = path;
        this.extension = extension;
    }

    // Implement the compute() method. As you parameterized the RecursiveTask class
    // with the List<String> type,
    // this method has to return an object of that type.
    @Override
    protected List<String> compute() {

        // List to store the names of the files stored in the folder.
        List<String> list = new ArrayList<String>();

        // FolderProcessor tasks to store the subtasks that are going to process the
        // subfolders stored in the folder
        List<FolderProcessor> tasks = new ArrayList<FolderProcessor>();

        // Get the content of the folder.
        File file = new File(path);

        File content[] = file.listFiles();
        // For each element in the folder, if there is a subfolder, create a new
        // FolderProcessor object
        // and execute it asynchronously using the fork() method.
        if (content != null) {
            for (int i = 0; i < content.length; i++) {
                if (content[i].isDirectory()) {
                    FolderProcessor task = new FolderProcessor(content[i].getAbsolutePath(), extension);
                    task.fork();
                    tasks.add(task);
                }

                // Otherwise, compare the extension of the file with the extension you are
                // looking for using the checkFile() method
                // and, if they are equal, store the full path of the file in the list of
                // strings declared earlier.
                else {
                    if (checkFile(content[i].getName())) {
                        list.add(content[i].getAbsolutePath());
                    }
                }
            }
        }

        // If the list of the FolderProcessor subtasks has more than 50 elements,
        // write a message to the console to indicate this circumstance.
        if (tasks.size() > 50) {
            System.out.printf("%s: %d tasks ran.\n", file.getAbsolutePath(), tasks.size());
        }

        // add to the list of files the results returned by the subtasks launched by
        // this task.
        addResultsFromTasks(list, tasks);

        return list;
    }

    // For each task stored in the list of tasks, call the join() method that
    // will wait for its finalization and then will return the result of the task.
    // Add that result to the list of strings using the addAll() method.
    private void addResultsFromTasks(List<String> list, List<FolderProcessor> tasks) {
        for (FolderProcessor item : tasks) {
            list.addAll(item.join());
        }
    }

    // This method compares if the name of a file passed as a parameter ends with
    // the extension you are looking for.
    private boolean checkFile(String name) {
        return name.endsWith(extension);
    }
}

/*
 * If the task finds a folder, it creates another Task object to process that
 * folder and sends it to the pool using the fork() method. This method sends
 * the task to the pool that will execute it if it has a free worker thread, or
 * it can create a new one.
 * 
 * The method returns immediately, so the task can continue processing the
 * folder’s content. For every file, a task compares its extension with the one
 * it’s looking for and, if they are equal, adds the file’s name to the list of
 * results.
 * 
 * Once the task has processed the assigned folder’s content, it waits for the
 * finalization of all the tasks it sent to the pool using the join() method.
 * 
 * This method, called in a task, waits for the finalization of its execution
 * and returns the value returned by the compute() method. The task groups the
 * results of all the tasks it sent with its results and returns that list as a
 * return value of the compute() method.
 */