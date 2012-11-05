#!/usr/bin/perl

use strict;
use warnings;

# Add rampart modules directory to @INC
use FindBin;
use lib "$FindBin::Bin/modules";

# 3rd Part modules
use Getopt::Long;
Getopt::Long::Configure("pass_through");
use Pod::Usage;
use Cwd;

# RAMPART modules
use QsOptions;
use Configuration;
use SubmitJob;


# Script locations
my ( $RAMPART, $RAMPART_DIR ) = fileparse( abs_path($0) );
my $NIZAR_SCRIPT_DIR = $RAMPART_DIR . "tools/nizar/";
my $LEF_PATH 				= $NIZAR_SCRIPT_DIR . "length_extract_fasta";
my $LEFLTL_PATH   			= $NIZAR_SCRIPT_DIR . "length_extract_fasta_less_than_limit";
my $EXONERATE_PATH  		= $NIZAR_SCRIPT_DIR . "exonerate_cmd";
my $SEARCH_EXON_RYO_PATH    = $NIZAR_SCRIPT_DIR . "search_exonerate_ryo";
my $EX_SEQID_PATH    		= $NIZAR_SCRIPT_DIR . "extract_seq_ids_from_exonerate_ryo";
my $EX_FASTA_ID_PATH    	= $NIZAR_SCRIPT_DIR . "extract_fasta_records_on_ids";
my $SOURCE_EXONERATE		= "source exonerate-2.2.0;";

# Other constants
my $QUOTE = "\"";
my $PWD   = getcwd;

# Parse generic queueing tool options
my $qst = new QsOptions();
$qst->parseOptions();

# Parse tool specific options
my (%opt) = ( );

GetOptions( 
	\%opt, 
	'help|usage|h|?',
	'man' )
  or pod2usage("Try '$0 --help' for more information.");

# Display usage message or manual information if required
pod2usage( -verbose => 1 ) if $opt{help};
pod2usage( -verbose => 2 ) if $opt{man};

die "Error: No input scaffold file specified\n\n" unless $qst->getInput();

my $cmd_line = "";

# Display configuration settings if requested.
if ($qst->isVerbose()) {
	print 	"\n\n" .
			$qst->toString() . "\n\n";
}


#1) perl ~droun/bin/length_extract_fasta 1000 $INPUT_FILE > over_1kb.fasta
#2) perl ~droun/bin/length_extract_fasta_less_than_limit $INPUT_FILE > less_1kb.fasta
#3) ~droun/bin/exonerate_cmd less_1kb.fasta over_1kb.fasta sub_1kb_vs_over_1kb.exonerate
#4) perl ~droun/bin/search_exonerate_ryo sub_1kb_vs_over_1kb.exonerate 95 95 duplication 50 0_both > filtered.sub_1kb_vs_over_1kb.exonerate
#5) perl ~droun/bin/extract_seq_ids_from_exonerate_ryo.pl filtered.sub_1kb_vs_over_1kb.exonerate > ids.list
#6) sort -u ids.list > ids.list.sorted
#7) perl ~droun/bin/extract_fasta_records_on_ids -i ids.list.sorted -s $INPUT_FILE -c cleaned.$INPUT_FILE.fasta -r removed.$INPUT_FILE.fasta

# Setup files
my $basename_input = basename($qst->getInput());
my $o1k_file = $qst->getOutput() . "over_1kb.fasta";
my $l1k_file = $qst->getOutput() . "less_1kb.fasta";
my $exon_file = $qst->getOutput() . "less_1kb_vs_over_1kb.exonerate";
my $filtered_file = $qst->getOutput() . "filtered.sub_1kb_vs_over_1kb.exonerate";
my $ids_file = $qst->getOutput() . "ids.list";
my $sorted_ids_file = $qst->getOutput() . "ids.list.sorted";
my $output_cleaned = $qst->getOutput() . "cleaned.fasta";
my $output_removed = $qst->getOutput() . "removed.fasta";


# Step 1 - Get large scaffolds ( >= 1kb )
my @o1k_args = grep{$_} (
	$LEF_PATH,
	"1000",
	$qst->getInput(),
	">",
	$o1k_file
);
my $o1k_cmd = join " ", @o1k_args;

# Step 2 - Get small scaffolds ( < 1kb )
my @l1k_args = grep{$_} (
	$LEFLTL_PATH,
	"1000",
	$qst->getInput(),
	">",
	$l1k_file
);
my $l1k_cmd = join " ", @l1k_args;

# Step 3 - Exonerate small scaffolds against large scaffolds
my @exon_args = grep{$_} (
	$SOURCE_EXONERATE,
	$EXONERATE_PATH,
	$l1k_file,
	$o1k_file,
	$exon_file
);
my $exon_cmd = join " ", @exon_args;

# Step 4 - Filter exonerate results
my @filter_args = grep{$_} (
	$SEARCH_EXON_RYO_PATH,
	$exon_file,
	"95",
	"95",
	"duplication",
	"50",
	"0_both",
	">",
	$filtered_file
);
my $filter_cmd = join " ", @filter_args;


# Step 5 - Get seq IDs
my @get_ids_args = grep{$_} (
	$EX_SEQID_PATH,
	$filtered_file,
	">",
	$ids_file
);
my $get_ids_cmd = join " ", @get_ids_args;


#Step 6 - Sort IDs
my @sort_args = grep{$_} (
	"sort",
	"-u",
	$ids_file,
	">",
	$sorted_ids_file
);
my $sort_cmd = join " ", @sort_args;


# Step 7 - Extract FASTA Records based on the sorted IDs
my @extract_args = grep{$_} (
	$EX_FASTA_ID_PATH,
	"-i",
	$sorted_ids_file,
	"-s",
	$qst->getInput(),
	"-c",
	$output_cleaned,
	"-r",
	$output_removed
);
my $extract_cmd = join " ", @extract_args;


# Combine all the commands and submit to the Grid Engine
my @cmd_args = grep{$_} (
	$o1k_cmd,
	$l1k_cmd,
	$exon_cmd,
	$filter_cmd,
	$get_ids_cmd,
	$sort_cmd,
	$extract_cmd
);
my $full_cmd = join " ", @cmd_args;


# Submit the deduplication job
SubmitJob::submit($qst, $full_cmd);



__END__

=pod

=head1 NAME

  dedup.pl


=head1 SYNOPSIS

  dedup.pl [options] -i <input_scaffold_file>

  For full documentation type: "dedup.pl --man"


=head1 DESCRIPTION

  This script is designed to execute a fasta deduplication jobs on a grid engine.  The deduplication process involves separating the input scaffold file
  into 2 groups, those scaffolds that are under 1k and those that are over 1k in length.  The smaller scaffolds are exonerated against the larger scaffolds and
  the output is filtered and sorted to indentify those smaller scaffolds for which there is strong evidence of redundancy.  Two files are produced, one which
  has the redundant scaffolds removed, and another that contains the redundant scaffolds.


=head1 OPTIONS

  --grid_engine      	 --ge
              The grid engine to use.  Currently "LSF" and "PBS" are supported.

  --tool                 -t
              Currently supported tools include: (gapcloser).  Default tool: gapcloser.

  --tool_path            --tp
              The path to the tool, or name of the tool's binary file if on the path.

  --project_name         --project           -p
              The project name for the job that will be placed on the grid engine.

  --job_name             --job               -j
              The job name for the job that will be placed on the grid engine.

  --wait_condition       --wait              -w
              If this job shouldn't run until after some condition has been met (normally the condition being the successful completion of another job), then that wait condition is specified here.

  --queue                -q
              The queue to which this job should automatically be sent.

  --memory               --mem               -m
              The amount of memory to reserve for this job.

  --threads              -n
              The number of threads that this job is likely to use.  This is used to reserve cores from the grid engine.

  --extra_args           --ea
              Any extra arguments that should be sent to the grid engine.

  --input                --in                -i
              REQUIRED: The input scaffold file for this job.

  --output               --out               -o
              The output dir for this job.

  --verbose              -v
              Whether detailed debug information should be printed to STDOUT.


=head1 AUTHORS

  Daniel Mapleson <daniel.mapleson@tgac.ac.uk>
  Nizar Drou <nizar.drou@tgac.ac.uk>

=cut

