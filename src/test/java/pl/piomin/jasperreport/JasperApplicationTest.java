package pl.piomin.jasperreport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class JasperApplicationTest {

    protected Logger logger = LoggerFactory.getLogger(JasperApplicationTest.class.getName());

    @Autowired
    TestRestTemplate template;

    @Test
    void testGetReport() throws InterruptedException {
        List<HttpStatusCode> responses = new ArrayList<>();
        Random r = new Random();
        int i = 0;

        long start = System.currentTimeMillis();
        for (; i < 20; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    int age = r.nextInt(99);
                    long start = System.currentTimeMillis();
                    ResponseEntity<byte[]> res = template.getForEntity("/pdf/fv/{age}", byte[].class, age);
                    logger.info("Response (" + (System.currentTimeMillis() - start) + "): " + res.getStatusCode());
                    responses.add(res.getStatusCode());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        while (responses.size() != i) {
            Thread.sleep(500);
            if (System.currentTimeMillis() - start > 10000)
                break;
        }

        logger.info("Test finished: ok->{}, expected->{}", responses.size(), i);
        Assertions.assertEquals(i, responses.size());
    }

}
