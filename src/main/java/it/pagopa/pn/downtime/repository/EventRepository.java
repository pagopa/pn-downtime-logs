package it.pagopa.pn.downtime.repository;

import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import it.pagopa.pn.downtime.model.Event;

@EnableScanCount
@EnableScan
public interface EventRepository extends 
PagingAndSortingRepository<Event, String> {
    
    Optional<Event> findByUuid(String uuid);
    
    Page<Event> findAllByFunctionality(String functionality,Pageable secondPageWithFiveElements);
}