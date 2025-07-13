package com.olatech.shopxauthservice.Service;

import com.olatech.shopxauthservice.Model.shared.HistoryProductMethod;
import com.olatech.shopxauthservice.Model.Product;
import com.olatech.shopxauthservice.Model.shared.SyncStatus;
import com.olatech.shopxauthservice.Model.shared.HistoryProductStore;
import com.olatech.shopxauthservice.Repository.HistoryProductStoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HistoryProductStoreService {
    @Autowired
    private HistoryProductStoreRepository historyProductStoreRepository;



    public void saveHistoryProductStore(Product product, HistoryProductMethod method, SyncStatus status) {
        HistoryProductStore historyProductStore = new HistoryProductStore();
        historyProductStore.setProduct(product);
        historyProductStore.setMethod(method);
        historyProductStore.setSyncStatus(status);
        historyProductStore.setStoreId(product.getStore().getId());
        log.info("Saving history product store" + historyProductStore);
        historyProductStoreRepository.save(historyProductStore);
    }
}
