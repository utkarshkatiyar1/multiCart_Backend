package com.example.compare.Repository;

import com.example.compare.model.Compare;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompareRepo extends  MongoRepository<Compare,String> {
    List<Compare> findByUserId(String userId);

    Compare findListByUserId(String userId);
}
