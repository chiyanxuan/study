package com.nil.serviceImpl1;

import com.nil.service.PersonService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "PersonServiceImpl.ext", havingValue = "false", matchIfMissing = true)
public class PersonServiceImpl implements PersonService {

    @Override
    public String getPerson(String id) {
        return "there is serviceImpl1:" + id;
    }
}
