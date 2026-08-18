package com.casamento.TuaniJoao.Model.Repository;

import com.casamento.TuaniJoao.Model.Entity.Gift;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {

    List<Gift> findByActiveTrue();

    @Modifying
    @Transactional
    @Query("UPDATE Gift g SET g.active = false WHERE g.id = :id")
    void softDeleteById(@Param("id") Long id);
}
