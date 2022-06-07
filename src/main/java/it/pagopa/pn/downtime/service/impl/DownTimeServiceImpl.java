package it.pagopa.pn.downtime.service.impl;

import org.springframework.stereotype.Service;

import it.pagopa.pn.downtime.service.DownTimeService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DownTimeServiceImpl implements DownTimeService {
	
	//@Autowired
    //private final DownTimeRepository downTimeRepository;
	
	@Override
	public String getDocument(String document) {
		//return downTimeRepository.findByNomeDocumento(documento);
		return "prova";
	}

   
}
