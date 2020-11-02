package com.nil.serviceImpl2;

import com.nil.service.PersonService;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl2 implements PersonService {

    public String getPerson(String id) {
        return "there is serviceImpl2:" + id;
    }
}
