package demo.Crypto_Portfolio.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import demo.Crypto_Portfolio.app.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
