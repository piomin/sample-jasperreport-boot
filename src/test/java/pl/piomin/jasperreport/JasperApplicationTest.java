package pl.piomin.jasperreport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class JasperApplicationTest {

	protected Logger logger = Logger.getLogger(JasperApplicationTest.class.getName());

	TestRestTemplate template = new TestRestTemplate();
	
	@Test
	public void testGetReport() throws InterruptedException {
		List<HttpStatus> responses = new ArrayList<>();
		Random r = new Random();
		int i = 0;
		for (; i < 20; i++) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					int age = r.nextInt(99);
					long start = System.currentTimeMillis();
					ResponseEntity<InputStreamResource> res = template.getForEntity("http://localhost:2222/pdf/fv/{age}", InputStreamResource.class, age);
					logger.info("Response (" +  (System.currentTimeMillis()-start) + "): " + res.getStatusCode());
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
		}
		
		logger.info("Test finished");
	}
		
}
