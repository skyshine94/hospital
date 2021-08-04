package com.myself.hosp.repository;

import com.myself.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {

    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
