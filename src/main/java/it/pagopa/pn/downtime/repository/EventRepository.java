package it.pagopa.pn.downtime.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.repository.PagingAndSortingRepository;

import it.pagopa.pn.downtime.model.Event;

/**
 * The Interface EventRepository.
 */
@EnableScanCount
@EnableScan
public interface EventRepository extends PagingAndSortingRepository<Event, String> {
    
    
}