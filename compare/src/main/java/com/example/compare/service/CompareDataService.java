package com.example.compare.service;

import com.example.compare.model.ApplicationResponse;
import com.example.compare.model.Compare;

public interface CompareDataService
{

    ApplicationResponse getListByUserId(String userId);

    ApplicationResponse addToCompare(Compare compare);

    ApplicationResponse removeFromCompare(String userId, String productId, String variantId);
    ApplicationResponse clearCompareList(String userId);

}
