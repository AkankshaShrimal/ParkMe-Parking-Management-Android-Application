package com.java.parkme;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.java.parkme.config.FileStorageProperties;

@SpringBootApplication
@CrossOrigin(origins = "*")
@EnableConfigurationProperties({
	FileStorageProperties.class
})
public class ParkmeApplication {
	
    private static ConfigurableApplicationContext context;
	
	public static void main(String[] args) {
		context = SpringApplication.run(ParkmeApplication.class, args);
	}

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(ParkmeApplication.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }
}
