package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SettingRepository extends JpaRepository<Setting, String> {}
