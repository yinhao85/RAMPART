/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.tool.process.mecq;

import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.param.FilePair;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;
import uk.ac.tgac.conan.core.data.Library;
import uk.ac.tgac.conan.process.ec.*;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: maplesod
 * Date: 22/08/13
 * Time: 15:04
 */
public class MecqExecutorImpl extends RampartExecutorImpl implements MecqExecutor {

    @Override
    public void executeEcq(ErrorCorrector errorCorrector, File outputDir, String jobName, boolean runInParallel)
            throws InterruptedException, ProcessExecutionException {

        // Ensure downstream process has access to the process service
        errorCorrector.initialise();

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(
                jobName,
                executionContextCopy.usingScheduler() ? !runInParallel : true,
                new File(outputDir, jobName + ".log"));

        if (this.executionContext.usingScheduler()) {

            SchedulerArgs schedulerArgs = executionContextCopy.getScheduler().getArgs();
            ErrorCorrectorArgs ecArgs = errorCorrector.getArgs();

            schedulerArgs.setThreads(ecArgs.getThreads());
            schedulerArgs.setMemoryMB(ecArgs.getMemoryGb() * 1000);
        }

        this.conanProcessService.execute(errorCorrector, executionContextCopy);
    }

    @Override
    public void createInputLinks(Library library, ErrorCorrectorArgs args)
            throws ProcessExecutionException, InterruptedException {

        // Modify execution context so we execute these instructions straight away (i.e. no scheduling)
        ExecutionContext linkingExecutionContext = new DefaultExecutionContext(this.executionContext.getLocality(), null, null);

        if (library.isPairedEnd()) {

            FilePair pairedEndFiles = ((ErrorCorrectorPairedEndArgs)args).getPairedEndInputFiles();

            StringJoiner compoundLinkCmdLine = new StringJoiner(";");

            compoundLinkCmdLine.add(this.conanProcessService.makeLinkCommand(library.getFile1(), pairedEndFiles.getFile1()));
            compoundLinkCmdLine.add(this.conanProcessService.makeLinkCommand(library.getFile2(), pairedEndFiles.getFile2()));

            this.conanProcessService.execute(compoundLinkCmdLine.toString(), linkingExecutionContext);
        }
        else {
            conanProcessService.execute(this.conanProcessService.makeLinkCommand(library.getFile1(),
                    ((ErrorCorrectorSingleEndArgs)args).getSingleEndInputFile()), linkingExecutionContext);
        }
    }



    /**
     * Using a set of ECQ specific args, creates an ErrorCorrector object for execution
     * @param mecqArgs
     * @param inputLib
     * @param mecqDir
     * @return
     */
    @Override
    public ErrorCorrector makeErrorCorrector(EcqArgs mecqArgs, Library inputLib, File mecqDir) {

        File ecDir = new File(mecqDir, mecqArgs.getName());
        File ecqLibDir = new File(ecDir, inputLib.getName());

        // Make the error correctors output directory if it doesn'e exist
        ecqLibDir.mkdirs();

        ErrorCorrector ec = ErrorCorrectorFactory.valueOf(mecqArgs.getTool()).create();
        ErrorCorrectorArgs ecArgs = ec.getArgs();

        ecArgs.setMinLength(mecqArgs.getMinLen());
        ecArgs.setQualityThreshold(mecqArgs.getMinQual());
        ecArgs.setKmer(mecqArgs.getKmer());
        ecArgs.setThreads(mecqArgs.getThreads());
        ecArgs.setMemoryGb(mecqArgs.getMemory());
        ecArgs.setOutputDir(ecqLibDir);

        // Add files to ec (assumes ECQ tool and input libraries are compatible with regards to Paired / Single End)
        List<File> altInputFiles = new ArrayList<>();
        if (inputLib.isPairedEnd()) {
            altInputFiles.add(new File(ecqLibDir, inputLib.getFile1().getName()));
            altInputFiles.add(new File(ecqLibDir, inputLib.getFile2().getName()));
        }
        else {
            altInputFiles.add(new File(ecqLibDir, inputLib.getFile1().getName()));
        }
        ec.getArgs().setFromLibrary(inputLib, altInputFiles);


        return ec;
    }


}
