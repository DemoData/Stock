package com.hitales.functions;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class TestMain implements BaseMain{

    public void execute(){
        System.out.println("test");
    }
}
