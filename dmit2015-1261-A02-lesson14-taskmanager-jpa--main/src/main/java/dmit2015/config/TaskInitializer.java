package dmit2015.config;


import dmit2015.model.Task;
import dmit2015.service.TaskJpaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.datafaker.Faker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
public class TaskInitializer {
    private final Logger logger = Logger.getLogger(TaskInitializer.class.getName());

    @Inject
    private TaskJpaService taskJpaService;


    /**
     * Using the combination of `@Observes` and `@Initialized` annotations, you can
     * intercept and perform additional processing during the phase of beans or events
     * in a CDI container.
     * <p>
     * The @Observers is used to specify this method is in observer for an event
     * The @Initialized is used to specify the method should be invoked when a bean type of `ApplicationScoped` is being
     * initialized
     * <p>
     * Execute code to create the test data for the entity.
     * This is an alternative to using a @WebListener that implements a ServletContext listener.
     * <p>
     * ]    * @param event
     */
    public void initialize(@Observes @Initialized(ApplicationScoped.class) Object event) {
        logger.info("Initializing tasks");

        if (taskJpaService.getAllTasks().isEmpty()) {
            /* You have three options for creating the test data:
                Option 1) Hard code the test data when testing a small dataset.
                Option 2) Read the test data from a text file when testing a large dataset.
                Option 3) Generate the test data using DataFaker.
                          When used with Integration Testing you will need to save the generated data
                          to a file that can be read later to compare with expected values.
             */

            try {

                var faker= new Faker();

                for(int count = 1; count <=10; count++){

                    Task currentTask= dmit2015.model.Task.of(faker);
                    taskJpaService.createTask(currentTask);
                }


            } catch (Exception ex) {
                logger.warning(ex.getMessage());
            }

            logger.info("Created " + taskJpaService.getAllTasks().size() + " records.");
        }
    }
}