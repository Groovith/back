package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Refresh;
import org.springframework.data.repository.CrudRepository;

public interface RefreshRepository extends CrudRepository<Refresh, String> {
    //Boolean existsByRefresh(String refresh);

//    @Transactional
//    void deleteByRefresh(String refresh);
}