package todo.microservice.repositories;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;
import todo.microservice.domain.User;

import java.util.List;

@Repository
public interface UsersRepository extends PageableRepository<User, Long> {

  List<User> findByItemsId(Long id, Pageable pageable);

}
