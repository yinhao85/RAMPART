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
package uk.ac.tgac.rampart.tool.process.amp;

import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.tgac.conan.process.asmIO.AbstractAssemblyIOProcess;
import uk.ac.tgac.rampart.tool.RampartExecutorImpl;

import java.io.File;

/**
 * User: maplesod
 * Date: 25/03/13
 * Time: 11:10
 */
@Service
public class AmpStageExecutorImpl extends RampartExecutorImpl implements AmpStageExecutor {

    @Override
    public void executeAmpStage(AbstractAssemblyIOProcess ampProc)
            throws InterruptedException, ProcessExecutionException {

        // Add in this conan process service to the amp proc to run
        ampProc.setConanProcessService(this.conanProcessService);

        // Initialise the proc
        ampProc.initialise();

        // Run the amp proc.
        ampProc.execute(this.executionContext);
    }

}
