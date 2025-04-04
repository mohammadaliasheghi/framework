package com.m2a.util;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public abstract class NightJob {

    /**
     * This abstract class defines a template for nightly jobs.
     * It is marked as a Spring Component, making it eligible for Spring's dependency injection and scheduling.
     * The 'run' method is intended to be implemented by concrete subclasses, providing the specific job logic.
     * The @Async annotation ensures that the 'run' method is executed asynchronously, preventing blocking of the main thread.
     * The @Scheduled annotation uses a cron expression to schedule the job's execution.
     * The cron expression is read from the 'night.job.cron' property.<br/>
     * <strong>must be added <code>@EnableScheduling</code>,<code>@EnableAsync</code> to main class, Then Example:</strong>
     * <blockquote>
     * <pre>
     * import org.springframework.stereotype.Component;
     *  import java.time.LocalDateTime;
     *
     *  <code>@Component</code>
     *  public class MyNightJob extends NightJob {
     *
     *      <code>@Override</code>
     *      public void run() {
     *          System.out.println("MyNightJob executed at: " + LocalDateTime.now());
     *          // Your specific job logic here...
     *      }
     *  }
     * </pre>
     * </blockquote>
     * <strong>Example application.properties entry:</strong>
     * <p>night.job.cron=0 0 2 * * ?  // Runs at 2:00 AM every day.</p>
     * <p>night.job.cron=0 30 1 * * ? // Runs at 1:30 AM every day</p>
     * <p>night.job.cron=0 0/5 * * * ? // Runs every 5 minutes.</p>
     */

    @Async
    @Scheduled(cron = "${night.job.cron}") // Schedule the job based on the 'night.job.cron' property
    public abstract void run(); // Abstract method to be implemented by concrete subclasses
}