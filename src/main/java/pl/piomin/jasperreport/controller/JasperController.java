package pl.piomin.jasperreport.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@RestController
public class JasperController {

	protected Logger logger = Logger.getLogger(JasperController.class.getName());
	public static int count = 0;
	

	JRFileVirtualizer fv;
	JRSwapFileVirtualizer sfv;
	
	@Autowired
	DataSource datasource;
	@Autowired
	JasperReport jasperReport;
	
	public JasperController() {
//		try {
			fv = new JRFileVirtualizer(100, "C:\\Users\\Piotr\\pdf");
//			JRSwapFile sf = new JRSwapFile("C:\\Users\\Piotr\\pdf", 1024, 100);
//			sfv = new JRSwapFileVirtualizer(20, sf, true);
//		} catch (JRException e) {
//			e.printStackTrace();
//		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/pdf/{age}")
	public ResponseEntity<InputStreamResource> getReport(@PathVariable("age") int age) throws JRException, SQLException, IOException {
		logger.info("getReport(" + age + ")");
//		JRFileVirtualizer fv = new JRFileVirtualizer(20, "C:\\Users\\Piotr\\pdf");
//		JRSwapFile sf = new JRSwapFile("directory", 1024, 100);
//		JRSwapFileVirtualizer sfv = new JRSwapFileVirtualizer(50, sf, true);
		Map<String, Object> m = new HashMap<>();
		m.put(JRParameter.REPORT_VIRTUALIZER, fv);
		m.put("age", age);
		Connection cc = datasource.getConnection();
		JasperPrint p = JasperFillManager.fillReport(jasperReport, m, cc);
		cc.close();
		
//		JRSaver.saveObject(jasperReport, "employeeReport.jasper");
		JRPdfExporter exporter = new JRPdfExporter();
		
		String name = ++count + "personReport.pdf";
		SimpleOutputStreamExporterOutput c = new SimpleOutputStreamExporterOutput(name);
		exporter.setExporterInput(new SimpleExporterInput(p));
		exporter.setExporterOutput(c);
		exporter.exportReport();
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.valueOf("application/pdf"));
		responseHeaders.setContentDispositionFormData("attachment", name);
		FileInputStream st = new FileInputStream(name);
		responseHeaders.setContentLength(st.available());
	    InputStreamResource isr = new InputStreamResource(st);
	    
//	    if (fv != null) fv.cleanup();
	    
	    return new ResponseEntity<InputStreamResource>(isr, responseHeaders, HttpStatus.OK);
	}
	
}
