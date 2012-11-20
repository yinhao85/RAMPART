package uk.ac.tgac.rampart.dao;

import java.util.List;

import uk.ac.tgac.rampart.data.Library;
import uk.ac.tgac.rampart.data.Library.Dataset;

public interface LibraryDao {

	Library getLibrary(final Long id);
	
	List<Library> getAllLibraries();
	
	List<Library> getLibraries(final String name, final Dataset dataset);
	
	List<Library> getLibraries(final Long jobId);
	
	long count();
	
	void persist(final Library library, final boolean cascade);
	
	void persistList(final List<Library> libraryList, final boolean cascade);
}
