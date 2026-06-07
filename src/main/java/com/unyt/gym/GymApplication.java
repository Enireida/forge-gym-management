package com.unyt.gym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Forge - Gym Management System.
 * <p>
 * Main entry point for the Spring Boot application. This class bootstraps the
 * embedded Tomcat server, the JPA persistence context, the H2 database and the
 * Thymeleaf view engine.
 * </p>
 *
 * <h2>Course requirements covered at the application level:</h2>
 * <ul>
 *   <li><b>R5 Concurrency</b> — {@link EnableAsync} activates {@code @Async}
 *       support so background jobs run on a managed {@code ExecutorService}.</li>
 *   <li><b>R7 JPA</b> — Spring Boot auto-configures Hibernate as the JPA
 *       provider through the starter on the classpath.</li>
 *   <li><b>R8 REST</b> — {@code spring-boot-starter-web} enables the embedded
 *       servlet container and JSON message converters used by the REST
 *       controllers under {@code com.unyt.gym.controller.api}.</li>
 *   <li><b>R9 Design Patterns</b> — Spring's IoC container itself is a
 *       textbook example of the Singleton and Dependency Injection patterns
 *       (every bean is, by default, a singleton in the container scope).</li>
 * </ul>
 *
 * @author Aston Bahja
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync       // R5 - enable @Async background execution
@EnableScheduling  // R5 - enable scheduled tasks (membership-expiry checker)
public class GymApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments forwarded to Spring Boot
     */
    public static void main(String[] args) {
        printBanner();
        SpringApplication.run(GymApplication.class, args);
    }

    /**
     * Prints a small ASCII banner to the console on startup.
     * Purely cosmetic — keeps the demo session pleasant.
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("");
        System.out.println("   FORGE   ");


        System.out.println();
    }
}
