package pl.piomin.jasperreport;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;

@SpringBootApplication
public class JasperApplication {

	public static void main(String[] args) {
		SpringApplication.run(JasperApplication.class, args);
	}

	@Bean
	JasperReport report() throws JRException {
		JasperReport jr = null;
		File f = new File("personReport.jasper");
		if (f.exists()) {
			jr = (JasperReport) JRLoader.loadObject(f);
		} else {
			jr = JasperCompileManager.compileReport("src/main/resources/report.jrxml");
			JRSaver.saveObject(jr, "personReport.jasper");
		}
		return jr;
	}
	
}
