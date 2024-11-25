package com.groovith.groovith.repository;

import com.groovith.groovith.domain.PlayerSession;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlayerSessionRepository extends CrudRepository<PlayerSession, Long> {

}
