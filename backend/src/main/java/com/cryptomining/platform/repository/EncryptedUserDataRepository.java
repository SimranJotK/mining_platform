package com.cryptomining.platform.repository;

import com.cryptomining.platform.entity.EncryptedUserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EncryptedUserDataRepository extends JpaRepository<EncryptedUserData, Long> {
    List<EncryptedUserData> findByUserId(Long userId);
    Optional<EncryptedUserData> findByUserIdAndDataType(Long userId, String dataType);
}
