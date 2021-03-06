/*
 * Copyright 2006-2020 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.sf.mzmine.modules.rawdatamethods.peakpicking.diagnosticfilter;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.sf.mzmine.datamodel.MZmineProject;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

/**
 * DFBuilder Diagnostic fragmentation filtering and Chromatogram Builder. 
 * 
 * Screen input ms/ms scans for input diagnostic patterns, generate targeted features for 
 * precursors of interest in corresponding RT region. 
 * 
 * Module based on Diagnostic Fragmentation FIltering visualization module developed
 * by Shawn Hoogstra.
 * 
 */
public class DiagnosticFilterModule implements MZmineProcessingModule {

  private static final String MODULE_NAME = "DFBuilder - Diagnostic Fragment Builder";
  private static final String MODULE_DESCRIPTION =
      "This module filters MS/MS scans based on fragment ion-neutral loss combinations and builds"
          + "targeted feature chromatograms for precursors of interest in corresponding RT region";

  @Override
  public @Nonnull String getName() {
    return MODULE_NAME;
  }

  @Override
  public @Nonnull String getDescription() {
    return MODULE_DESCRIPTION;
  }

  @Override
  @Nonnull
  public ExitCode runModule(@Nonnull MZmineProject project, @Nonnull ParameterSet parameters,
      @Nonnull Collection<Task> tasks) {

    for(RawDataFile dataFile : parameters.getParameter(DiagnosticFilterParameters.dataFiles)
        .getValue().getMatchingRawDataFiles()) {
    	Task newTask = new DiagnosticFilterTask(project, dataFile, parameters);
    	tasks.add(newTask);
    }

    return ExitCode.OK;
  }

  @Override
  public @Nonnull MZmineModuleCategory getModuleCategory() {
	return MZmineModuleCategory.PEAKPICKING;
  }

  @Override
  public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
    return DiagnosticFilterParameters.class;
  }
}
