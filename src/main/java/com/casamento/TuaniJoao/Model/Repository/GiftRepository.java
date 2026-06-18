package com.casamento.TuaniJoao.Model.Repository;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {
}
