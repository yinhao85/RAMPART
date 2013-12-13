RAMPART README


What does RAMPART do?
=====================

RAMPART is an automated de novo assembly pipeline designed to manage the generation of multiple assemblies using varying parameters, and the comparison of those assemblies with the aim of determining the "best" assembly.


Installation
============

Download the RAMPART jar to a directory of your choice.  The jar is a "shaded" jar which means that it contains all required dependencies, except for the java JRE.  JRE 1.7 or above is required to run RAMPART.

RAMPART uses a number of 3rd party tools during processing.  The current list is described below.  For full functionality, all these tools should be installed on your environment, however some of the tools are mandatory whereas others are optional.

Mandatory dependencies:
- JRE v1.7 or above

At least one of:
- MASS: Abyss V1.3; SOAPdenovo V2; ALLPATHS-LG V44837

Optional:
- MECQ: Sickle V1.1; Quake V0.3; Musket V1.0
- AMP: SSPACE Basic V2.0; SOAP GapCloser V1.12
- Misc: TGAC Subsampler V1.0 (enables subsampling for assemblers that do not have this functionality themselves)
- Analysis: Quast V2.2 (highly recommended); CEGMA V2.4

To save time finding all these tools on the internet RAMPART offers an option to download them all to a directory of your choice except for SSPACE, which requires you to fill out a form prior to download) into a specific directory.  To do this type: "java -jar rampart-<version>.jar download <dir>".  We do not attempt to actually install the tools however, as this is a complex process and you may wish to run a custom installation in order to get the best out of your particularly environment.

In case the specific tool versions requested are no longer available to download the project URLs are specified below.  It's possible alternative versions of the software may still work if the interfaces have not changed significantly.  If you find that a tool does not work in the RAMPART pipeline please contact daniel.mapleson@tgac.ac.uk and we will try to help.  Project URLs:

JRE             - http://www.oracle.com/technetwork/java/javase/downloads/index.html
Sickle          - https://github.com/najoshi/sickle
Abyss           - http://www.bcgsc.ca/platform/bioinfo/software/abyss
SSPACE_Basic    - http://www.baseclear.com/landingpages/basetools-a-wide-range-of-bioinformatics-solutions/sspacev12/
SOAP_GapCloser  - http://soap.genomics.org.cn/soapdenovo.html
SoapDeNovo      - http://soap.genomics.org.cn/soapdenovo.html
Quake           - http://www.cbcb.umd.edu/software/quake/
Musket          - http://musket.sourceforge.net/homepage.htm#latest
Quast           - http://bioinf.spbau.ru/quast
Cegma           - http://korflab.ucdavis.edu/datasets/cegma/
Subsampler      - https://github.com/homonecloco/subsampler
AllpathsLg      - http://www.broadinstitute.org/software/allpaths-lg/blog/?page_id=12



Supported Platforms
===================

RAMPART is a command line application written in java, so in theory is portable to any system running the Java Runtime Environment (JRE) 1.7 and above.  However, RAMPART utilise tools to do read error correction, de novo assembly, and assembly improvement and most of these tools are linux specific.  In addition, RAMPART does make use of some linux command line tools such as 'ln', 'awk' and 'wc'.  Tools such as cygwin on windows and MacPorts may help for porting to these platforms, although no guarantees are made.  In addition, RAMPART is designed to exploit High Performance Computing (HPC) architectures, and to use Scheduling systems that these architecture often employ.  In particular, Platform Load Sharing Facility (LSF) and Portable Batch System (PBS) are supported, often these scheduling systems only run on a unix platform.



Quick Start
===========

1. Create a job configuration file (details below) in a clean directory.
2. Then make this directory the working directory and type: "rampart run <name of config file>"

This will run RAMPART with its default settings on the data you've provided in the config file.  Details on how to construct a config file are described in the next section.

Upon starting for the first time RAMPART automatically copies a number of resources to the "~/.tgac/rampart" directory. It is worth perusing this directory to see what options are available.

More information on RAMPART usage can be obtained by typing "java -jar rampart-<version>.jar --help".



Job Config File
===============

An example configuration file can be found in "~/.tgac/rampart/config/job_config_example.cfg".  The configuration file takes a standard XML format, which can be validated using the schema present in "~/.tgac/rampart/config/job_config.xsd".



RAMPART Environment Configuration
=================================

It is expected that a file called "conan.properties" exists in "~/.tgac/rampart/"   In this file it is possible to describe
the execution context within which to run the RAMPART pipeline.  For example, you can specify the scheduling system to use and
if so, what queue to run on.  Known properties:

executionContext.scheduler          - Valid options {LSF}
executionContext.scheduler.queue    - Depends on Scheduling setup
executionContext.locality = LOCAL   - Always use this!

In addition, RAMPART uses SLF4J as a logging facade and is currently configured to use LOG4J.  If you which to alter to logging configuration then modify the "log4j.properties" file.  For details please consult:
"http://logging.apache.org/log4j/2.x/"

Often it's not possible to keep all these tools with the specified versions on the PATH.  It is possible to specify another file in the "conan.properties" file using the "externalProcessConfigFile" property.  This file can contain any commands that should be executed before running each process.  This enables the user to execute any commands that would bring the specified tools onto the PATH for the given environment.  Currently known process keys are as follows (note that these keys are hard coded, please keep the exact wording as below, even if you are using a different version of the software):

Sickle_V1.1
Abyss_V1.3.4
SSPACE_Basic_v2.0
GapCloser_v1.12
SoapDeNovo_V2.04
Quake_V0.3.4
Musket_V1.0.6
Quast_V2.2
Cegma_V2.4
Subsampler_V1.0
AllpathsLg_V44837

Format: <key>=<command to bring tool onto PATH>


Output
======

A typical RAMPART run will produce the following directory structure:

- /mecq     - A directory containing pre-processed versions of the input libraries as requested in the <mecq> element in your job configuration file.
- /mass     - A directory containing assemblies of your input libraries, as requested in the <mass> element in your job configuration file.
- /amp      - A directory containing the results of the assembly enhancement processes applied as requested in your job configuration file



High Performance Computing
==========================

If you are using an LSF cluster is possible to run RAMPART within a "bsub" command so that it executes on the cluster rather than on the head node.  RAMPART, can be configured to submit child jobs over the cluster as well.  In this case read the RAMPART Environment Configuration section later in this section.  Please try to keep the queue the same between the master job and child jobs.  Depending on your cluster's confi

RAMPART also supports PBS scheduling and there are plans to implement SGE/OGE scheduling in the future.



Extending the Software / Compilation
====================================

RAMPART is a java / maven project that requires JDK 1.7 or above to compile.  It should be possible to load the maven pom from each project into any maven aware IDE (IntelliJ Idea, Net Beans, Eclipse).  The exact steps to create the project locally on your machine are as follows:

- Ensure GIT, Maven and JDK v1.7+ is installed
- Open a terminal
- Type: "git clone https://github.com/TGAC/RAMPART.git"
- Type: "cd RAMPART"
- Type: "mvn clean install"

The final RAMPART jar containing all other java-based dependencies can be found in <rampart_dir>/target/rampart-<version>.jar.

Note: If you cannot clone the git repositories using "https", please try "ssh" instead.  Consult githib to obtain the specific URLs.


License
=======

RAMPART is available under GNU GLP V3.  For licensing details of other RAMPART dependencies please consult their own documentation.


Contact
=======

Daniel Mapleson (TGAC)
http://tgac.ac.uk/
daniel.mapleson@tgac.ac.uk


Acknowledgements
================

Bernardo Clavijo (TGAC)
Robert Davey (TGAC)
Tony Burdett (EBI)
Ricardo Ramirez (TGAC)
Nizar Drou (Formerly at TGAC)
David Swarbreck (TGAC)

