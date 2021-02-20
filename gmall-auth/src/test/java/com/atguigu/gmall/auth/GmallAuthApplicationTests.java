package com.atguigu.gmall.auth;

import com.atguigu.gmall.auth.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SpringBootTest
class GmallAuthApplicationTests {
    @Autowired
    JwtProperties jwtProperties;
    @Test
    void contextLoads() {
        System.out.println(jwtProperties.getPublicKey().getAlgorithm());
        System.out.println(jwtProperties.getSecret());
    }

    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(1);
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> collect = list.stream().collect(
                ArrayList::new,ArrayList::add,ArrayList::addAll);
        System.out.println(collect);


    }

}
