package hu.tomlincoln.otpmobilhomework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class OtpmobilhomeworkApplication {

    public static volatile boolean done;

	public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(OtpmobilhomeworkApplication.class, args);
        // Of course never do following in production!
        while (done) {
            ctx.close();
            System.exit(0);
        }
	}

}
