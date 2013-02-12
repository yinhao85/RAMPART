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
package uk.ac.tgac.rampart.conan.tool.proc.internal.mass.stats;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.DefaultConanParameter;
import uk.ac.tgac.rampart.conan.conanx.param.PathParameter;
import uk.ac.tgac.rampart.conan.conanx.param.ProcessParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: maplesod
 * Date: 11/02/13
 * Time: 15:49
 */
public class MassSelectorParams implements ProcessParams {

    private ConanParameter statsFiles;
    private ConanParameter configFiles;
    private ConanParameter outputDir;
    private ConanParameter weightings;

    public MassSelectorParams() {

        this.statsFiles = new DefaultConanParameter(
                "stats_files",
                "The stats files for each MASS run",
                false,
                true,
                false);

        this.configFiles = new DefaultConanParameter(
                "config_files",
                "The config files used for each MASS run",
                false,
                true,
                false);

        this.outputDir = new PathParameter(
                "output",
                "The output directory",
                true);

        this.weightings = new DefaultConanParameter(
                "weightings",
                "The weightings used to balance each Assembly metric according to its usefullness",
                false,
                true,
                false);
    }

    public ConanParameter getStatsFiles() {
        return statsFiles;
    }

    public ConanParameter getConfigFiles() {
        return configFiles;
    }

    public ConanParameter getOutputDir() {
        return outputDir;
    }

    public ConanParameter getWeightings() {
        return weightings;
    }

    @Override
    public List<ConanParameter> getConanParameters() {

        return new ArrayList<ConanParameter>(Arrays.asList(
                new ConanParameter[]{
                        this.statsFiles,
                        this.configFiles,
                        this.outputDir,
                        this.weightings}));
    }
}
