package good.stuff.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(BackendApplication.class, args);
    }

}
