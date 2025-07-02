package com.gurula.stockMate.layout;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.http.ResponseEntity;

public interface LayoutRepository extends MongoRepository<Layout, String> {
}
