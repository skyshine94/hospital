package com.myself.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myself.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {

    List<Patient> findAllUserId(Long userId);

    Patient getByPatientId(Long id);
}
