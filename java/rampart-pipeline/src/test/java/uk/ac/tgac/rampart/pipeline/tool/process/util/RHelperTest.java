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
package uk.ac.tgac.rampart.pipeline.tool.process.util;

import org.junit.Test;
import uk.ac.tgac.rampart.pipeline.util.RHelper;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * User: maplesod
 * Date: 31/01/13
 * Time: 12:00
 */
public class RHelperTest {

    @Test
    public void testStatsPlotterInternal() {

        File statsPlotterFile = RHelper.STATS_PLOTTER.getInternalScript();

        assertTrue(statsPlotterFile != null);
        assertTrue(statsPlotterFile.exists());
    }

    /*@Test
    public void testStatsPlotterExternal() {

        File statsPlotterFile = RHelper.STATS_PLOTTER.getExternalScript();

        String correctPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + RHelper.STATS_PLOTTER.getPath();

        assertTrue(statsPlotterFile != null);
        assertTrue(statsPlotterFile.getAbsolutePath().equals(correctPath));
    } */
}
