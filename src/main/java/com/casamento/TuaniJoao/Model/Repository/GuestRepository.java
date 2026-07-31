package com.casamento.TuaniJoao.Model.Repository;

import com.casamento.TuaniJoao.Model.Entity.Guest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    List<Guest> findByNameContainingIgnoreCase(String name);

}
