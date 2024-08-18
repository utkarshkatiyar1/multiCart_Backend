package com.example.compare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.compare.common.Constants;
import com.example.compare.model.ApplicationResponse;
import com.example.compare.model.Product;
import com.example.compare.model.Compare;
import com.example.compare.Repository.CompareRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CompareDataServiceImpl implements CompareDataService {

    @Autowired
    CompareRepo compareRepo;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public ApplicationResponse getListByUserId(String userId) {
        ApplicationResponse response = new ApplicationResponse();
        Compare existingList = null;

        existingList = compareRepo.findListByUserId(userId);
        if(existingList==null)
        {
            log.warn("list is not found with userId {}", userId);
            response.setData(null);
            response.setStatus(Constants.NO_CONTENT);
            response.setMessage(Constants.NO_CONTENT_MESSAGE);
            return  response;
        }
        List<Product> products = new ArrayList<>();

        for (Compare.Compare_item compareItem : existingList.getCompareItems()) {
            try {
                String variantId = compareItem.getVariantId();
                String productId = compareItem.getProductId();
                String productApiUrl = "http://localhost:8085/multikart/v1/product/byvariantid";

                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(productApiUrl)
                        .queryParam("productId", productId)
                        .queryParam("variantId", variantId);

                ApplicationResponse<Map<String, Object>> productResponse = restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<ApplicationResponse<Map<String, Object>>>() {
                        }
                ).getBody();

                if (productResponse != null && productResponse.getStatus().equals(Constants.OK)) {
                    Map<String, Object> productMap = productResponse.getData().get(0);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Product product = objectMapper.convertValue(productMap, Product.class);
                    products.add(product);
                } else {
                    log.warn("Failed to retrieve product details for productId: {} and variantId: {}", productId, compareItem.getVariantId());
                }
            } catch (Exception e) {
                log.error("An error occurred while fetching product details", e);
            }
        }

        response.setStatus(Constants.OK);
        response.setMessage(Constants.OK_MESSAGE);
        response.setData(Collections.singletonList(products));

        return response;
    }

    @Override
    public ApplicationResponse addToCompare(Compare compare) {
        try{
            Compare existingList = compareRepo.findListByUserId(compare.getUserId());

            if (existingList == null) {
                existingList = new Compare();
                existingList.setUserId(compare.getUserId());
                existingList.setCompareItems(new ArrayList<>());
            }

            List<Compare.Compare_item> userItems = compare.getCompareItems();

            ApplicationResponse applicationResponse = new ApplicationResponse();
            for (Compare.Compare_item userItem : userItems) {
                boolean itemExists = false;
                for (Compare.Compare_item item : existingList.getCompareItems()) {
                    if (item.getVariantId().equals(userItem.getVariantId()) && item.getProductId().equals(userItem.getProductId()))
                    {
                        applicationResponse.setMessage("Product is already added to compare!!!");
                        itemExists = true;
                        break;
                    }
                }

                if (!itemExists) {
                    Compare.Compare_item newItem = new Compare.Compare_item();
                    newItem.setVariantId(userItem.getVariantId());
                    newItem.setProductId(userItem.getProductId());
                    existingList.getCompareItems().add(newItem);
                }

            }
            compareRepo.save(existingList);

            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage(Constants.OK_MESSAGE);
            return applicationResponse;

        } catch (Exception e)
        {
            log.error("An error occurred while adding product to compare", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while adding product to compare: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public ApplicationResponse removeFromCompare(String userId, String productId, String variantId) {
        try {
            Compare existingList = compareRepo.findListByUserId(userId);
            List<Compare.Compare_item> existUserItems = existingList.getCompareItems();
            ApplicationResponse applicationResponse = new ApplicationResponse();
            existUserItems.removeIf(compareItem -> {
                try {

                   return compareItem.getProductId().equals(productId) && compareItem.getVariantId().equals(variantId);


                } catch (NumberFormatException e) {
                    log.error("Error parsing variant_id to integer: {}", variantId, e);
                    return false;
                }
            });

                existingList.setCompareItems(existUserItems);
                compareRepo.save(existingList);
                applicationResponse.setStatus(Constants.OK);
                applicationResponse.setMessage(Constants.OK_MESSAGE);
                applicationResponse.setData(null);
                return applicationResponse;


        } catch (Exception e)
        {
            log.error("An error occurred while removing product from compare list", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while removing product from compare list: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public ApplicationResponse clearCompareList(String userId) {
        try {
            Compare existingList = compareRepo.findListByUserId(userId);
            List<Compare.Compare_item> existUserItems = existingList.getCompareItems();
            ApplicationResponse applicationResponse = new ApplicationResponse();
            existUserItems.clear();
            existingList.setCompareItems(existUserItems);
            compareRepo.save(existingList);
            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage(Constants.OK_MESSAGE);
            applicationResponse.setData(null);
            return applicationResponse;
        }catch (Exception e)
        {
            log.error("An error occurred while clearing compare list", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while clearing compare list: " + e.getMessage());
            return errorResponse;
        }
    }
}
