package com.olatech.shopxauthservice.Repository;

import com.olatech.shopxauthservice.Model.shared.HistoryProductStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryProductStoreRepository extends JpaRepository<HistoryProductStore, Long> {
}
