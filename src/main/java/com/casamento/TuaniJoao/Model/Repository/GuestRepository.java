package com.casamento.TuaniJoao.Model.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    List<Guest> findByConfirmadaTrue();

    List<Guest> findByNameContainingIgnoreCase(String name);

}
