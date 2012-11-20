package uk.ac.tgac.rampart.frontend.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.ac.tgac.rampart.service.RampartJobService;
import uk.ac.tgac.rampart.service.ReportBuilderService;
import uk.ac.tgac.rampart.util.ApplicationContextProvider;
import uk.ac.tgac.rampart.util.Tools;

public class RampartHelper {
	
	private static Logger log = Logger.getLogger(RampartHelper.class.getName());
	
	@Autowired
	private ReportBuilderService reportBuilderService;
	
	@Autowired
	private RampartJobService rampartJobService;
	
	
	private RampartHelperOptions rhOptions;
	
	public RampartHelper(RampartHelperOptions options) {
		this.rhOptions = options;
	}
	
	public void process(Options options) {
		
		if (this.rhOptions.isHelp()) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("RampartHelper", options);
			return;
		}

		try {
			// Analyse the rampart job directory and build the job context object 
			VelocityContext context = this.rampartJobService.buildContext(this.rhOptions.getJobDir());
			
			// Build the report
			this.reportBuilderService.buildReport(this.rhOptions.getJobDir(), this.rhOptions.getProjectDir(), context);
			
			// Persist the context to the database
			this.rampartJobService.persistContext(context, true);
			
		} catch (Exception ioe) {
			log.error("Problem merging template and context into latex file: " + ioe.getMessage());
			log.error(Tools.getStackTrace(ioe));
			return;
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		log.info("Starting RAMPART Helper");
		
		// Setup Spring application context first
		new ApplicationContextProvider().setApplicationContext(new ClassPathXmlApplicationContext("applicationContext.xml"));
		
		log.debug("Spring configured");
		
		// Create the available options
		Options options = RampartHelperOptions.createOptions();
		
		log.debug("Command line options created");

		// Parse the actual arguments
		CommandLineParser parser = new PosixParser();
		RampartHelper rampartHelper = null;
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			RampartHelperOptions rhOptions = new RampartHelperOptions(line);
			rampartHelper = new RampartHelper(rhOptions);
			
		} catch (ParseException exp) {
			log.fatal("Options Parsing failed.  Reason: " + exp.getMessage());
			return;
		}		
		
		log.debug("Command line arguments processed");
		
		// Process report builder
		rampartHelper.process(options);		

		log.info("Finished RAMPART Helper");
		
		return;
	}
	
}
