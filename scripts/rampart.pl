#!/usr/bin/perl

use strict;

#### Packages
use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use File::Basename;
use Cwd;
use Cwd 'abs_path';
use QsOptions;
use Configuration;
use SubmitJob;

#### Constants

# Project constants
my $JOB_PREFIX = $ENV{'USER'} . "-rampart-";


# Other constants
my $QUOTE = "\"";
my $PWD = getcwd;
my ($RAMPART, $RAMPART_DIR) = fileparse(abs_path($0));

# Assembly stats gathering constants
my $MASS_PATH = $RAMPART_DIR . "mass.pl";
my $MASS_SELECTOR_PATH = $RAMPART_DIR . "mass_selector.pl";
my $IMPROVER_PATH = $RAMPART_DIR . "improver.pl";

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->parseOptions();

# Gather Command Line options and set defaults
my %opt;

GetOptions (
	\%opt,
	'mass_args|ma=s',
	'improver_args|ia=s',
	'raw_config|rc=s',
	'qt_config|qtc=s',
	'simulate|sim',
	'help|usage|h|?',
	'man'
)
or pod2usage( "Try '$0 --help' for more information." );



# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};




#### Validation

die "Error: No raw library config file specified\n\n" unless $opt{raw_config};
die "Error: No quality trimmed library config file specified\n\n" unless $opt{qt_config};
#die "Error: Approximate genome size not specified\n\n" unless $opt{approx_genome_size};


# Interpret config files
my $raw_cfg = new Configuration( $opt{raw_config} );
my $qt_cfg = new Configuration( $opt{qt_config} );



#### Process (all steps to be controlled via cmd line options)
my $mass_job_prefix = $qst->getJobName() . "-mass";
my $ms_job_name = $qst->getJobName() . "-ms";
my $improver_job_prefix = $qst->getJobName() . "-improver";
my $best_path_file;
my $best_dataset_file;


## Run assemblies for both raw and qt datasets
if (1) {

	# Make assemblies output directories
	my $mass_dir = $qst->getOutput() . "/mass";
	mkdir $mass_dir;

	my $raw_mass_dir = $mass_dir . "/raw";
	mkdir $raw_mass_dir;

	my $qt_mass_dir = $mass_dir . "/qt";
	mkdir $qt_mass_dir;

	my $raw_input = join " ", $raw_cfg->getAllInputFiles();
	my $qt_input = join " ", $qt_cfg->getAllInputFiles();
	my $raw_mass_job_prefix = $mass_job_prefix . "-raw";
	my $qt_mass_job_prefix = $mass_job_prefix . "-qt";

	# Run the assembler script for each dataset
	run_mass($raw_input, $raw_mass_job_prefix, $raw_mass_dir);
	run_mass($qt_input, $qt_mass_job_prefix, $qt_mass_dir);


	# Run best assembly selector to find "best" assembly (mass will produce stats automatically for us to use here)
	my $raw_stats_file = $raw_mass_dir . "/stats.txt";
	my $qt_stats_file = $qt_mass_dir . "/stats.txt";
	$best_path_file = $mass_dir . "/best.path.txt";
	$best_dataset_file = $mass_dir . "/best.dataset.txt";

	my @ms_args = grep {$_} (
			$MASS_SELECTOR_PATH,
			$qst->getQueueingSystemAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $ms_job_name,
			"--wait_condition 'done(" . $mass_job_prefix . "*)'",
			$qst->getQueueAsParam(),
			"--output " . $mass_dir,
			$qst->isVerboseAsParam(),
			"--raw_stats_file " . $raw_stats_file,
			"--qt_stats_file " . $qt_stats_file,
			$opt{approx_genome_size} ? "--approx_genome_size " . $opt{approx_genome_size} : "" );
			
	my $ms_cmd_line = join " ", @ms_args;

	system($ms_cmd_line);
}


my ($best_file, $best_dataset) = getBest($best_path_file, $best_dataset_file, $ms_job_name, $mass_job_prefix . "-getbest");


## Improve best assembly

if (1) {

	my $imp_dir = $opt{output} . "/improver";
	mkdir $imp_dir;

	my @imp_args = grep {$_} (
			$IMPROVER_PATH,
			$qst->getQueueingSystemAsParam(),
			$qst->getProjectNameAsParam(),
			"--job_name " . $improver_job_prefix,
			"--wait_condition done(" . $ms_job_name . "*)",
			$qst->getQueueAsParam(),
			"--output " . $imp_dir,
			"--input " . $best_file,
			"--config " . ($best_dataset eq "raw") ? $opt{raw_config} : $opt{qt_config},
			"--get_best_mass_from_file",
			"--stats",
			$opt{simulate} ? "--simulate" : "",
			$opt{improver_args},
			$qst->isVerboseAsParam());

	system(join " ", @imp_args);
}

# Notify user of job submission
if ($qst->isVerbose()) {
	print 	"\n" .
			"RAMPART has successfully submitted all child jobs to the grid engine.  You will be notified by email when the jobs have completed.\n";
}


sub run_mass {

	my @mass_args = grep {$_} (	
		$MASS_PATH,
		$qst->getQueueingSystemAsParam(),
		$qst->getProjectNameAsParam(),
		"--job_name " . $_[1],
		$qst->getQueueAsParam(),
		"--output " . $_[2],
		$qst->isVerboseAsParam(),
		$opt{mass_args},
		"--stats",
		$opt{simulate} ? "--simulate" : "",
		$_[0] 
	);

	system(join " ", @mass_args);
}

sub getBest {
	my $best_path_file = shift;
	my $best_dataset_file = shift;
	my $wait_job = shift;
	my $job_name = shift;


	# Wait for mass selector to complete 
	my $best_wait = new QsOptions();
	$best_wait->setQueueingSystem($qst->getQueueingSystem());
	$best_wait->setProjectName($qst->getProjectName());
	$best_wait->setJobName($job_name);
	$best_wait->setWaitCondition("ended(" . $wait_job . ")");
	$best_wait->setExtraArgs("-I");	# This forces this job to stay connected to the terminal until the wait job has ended
	SubmitJob::submit($best_wait, "sleep(1)");


	# Now the the files exist read them and return the values they contain

	open BP, "<", $best_path_file or die "Error: Couldn't parse input file.\n\n";
	my @bplines = <BP>;
	die "Error: Was only expecting a single line.\n\n" unless (@bplines == 1);
	my $best_path = $bplines[0];
	close(BP);

	open BD, "<", $best_dataset_file or die "Error: Couldn't parse input file.\n\n";
	my @bdlines = <BD>;
	die "Error: Was only expecting a single line.\n\n" unless (@bdlines == 1);
	my $best_dataset = $bdlines[0];
	close(BD);

	return ($best_file, $best_dataset);
}



__END__

=pod

=head1 NAME

  rampart.pl


=head1 SYNOPSIS

  rampart.pl [options] --raw_config <file> --qt_config <file>

  For full documentation type: "rampart.pl --man"


=head1 DESCRIPTION

  Runs an assembly program with multiple k-mer settings with alternate 4 and 6 step increments.


=head1 OPTIONS

  job_prefix|job|j                    The prefix string for all rampart child jobs.
  project|p                           The project name for marking the job.
  extra_queue_args|eqa|q              Extra arguments to pass to the queueing system for each child job.  E.g. "-q normal" to move jobs from the production (default) queue to the normal queue.
  assembler|a                         The assembly program to use.
  extra_assembler_args|ea_args|eaa    Any additional arguments to pass to the assembler script.  Type assembler.pl --man for more information.  This script will automatically invoke the assembler script with the project, job_prefix, threads, memory, stats, in_dir, and out_dir settings.  Assembler arguments such as --kmin and --kmax should be set via this argument for example.
  approx_genome_size|ags              The approximate genome size for the organism that is being sequenced.  Used for determining best assembly.
  improver|i
  output|out|o=s                      The output directory.
  verbose|v                           Print extra status information during run.
  help|usage|h|?                      Print usage message and then exit.
  man                                 Display manual.



=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut


