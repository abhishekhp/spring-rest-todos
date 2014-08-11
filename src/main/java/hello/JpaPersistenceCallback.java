package hello;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.web.patch.diffsync.PersistenceCallback;

class JpaPersistenceCallback<T> implements PersistenceCallback<T> {
	
	private final CrudRepository<T, Long> repo;

	public JpaPersistenceCallback(CrudRepository<T, Long> repo) {
		this.repo = repo;
	}
	
	@Override
	public void persistChange(T itemToSave) {
		repo.save(itemToSave);
	}
	
	@Override
	public void persistChanges(List<T> itemsToSave, List<T> itemsToDelete) {
		repo.save(itemsToSave);
		repo.delete(itemsToDelete);
	}
}