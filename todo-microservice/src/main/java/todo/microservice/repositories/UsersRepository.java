package todo.microservice.repositories;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;
import todo.microservice.domain.User;

import java.util.Set;

@Repository
public interface UsersRepository extends PageableRepository<User, Long> {

  Set<User> findByItemsId(Long id, Pageable pageable);

}
