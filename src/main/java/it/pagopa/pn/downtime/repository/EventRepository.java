package it.pagopa.pn.downtime.repository;

import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import it.pagopa.pn.downtime.model.Event;

@EnableScan
public interface EventRepository extends 
  CrudRepository<Event, String> {
    
    Optional<Event> findByUuid(String uuid);
}