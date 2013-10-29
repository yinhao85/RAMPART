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
package uk.ac.tgac.rampart.cli;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import uk.ac.tgac.rampart.RampartCLI;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

@Category(IntegrationTest.class)
public class RampartCLIIntegrationTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testRampart() throws URISyntaxException, IOException {

        File outputDir = temp.newFolder("rampartTest");

        File configFile = FileUtils.toFile(this.getClass().getResource("/tools/test_rampart_1.cfg"));


        RampartCLI.main(new String[]{
                "--config",
                configFile.getAbsolutePath(),
                "--output",
                outputDir.getAbsolutePath()
        });

    }

    @Test
    public void testHelp() throws URISyntaxException {

        RampartCLI.main(new String[]{
                "--help"
        });
    }



}