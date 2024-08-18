package multiKart.rating.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import multiKart.rating.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import multiKart.rating.Repository.RatingRepo;
import multiKart.rating.common.Constants;
import multiKart.rating.model.ApplicationResponse;
import multiKart.rating.model.Rating;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RatingServiceImpl implements RatingDataService{
    @Autowired
    private RatingRepo ratingRepository;

    @Autowired
    private RestTemplate restTemplate;
    @Override
    public ApplicationResponse create(Rating rating) {
        try {
            List<Rating> ratings = ratingRepository.findByUserId(rating.getUserId());
            boolean ratingExists = false;
            for(Rating rating1 : ratings){
                if(rating1.getProductId().equals(rating.getProductId()) && rating1.getVariantId().equals(rating.getVariantId())){
                    ratingExists = true;
                    rating1.setComment(rating.getComment());
                    rating1.setRating(rating.getRating());
                    ratingRepository.save(rating1);
                    break;
                }
            }
            if(ratingExists==false)
                ratingRepository.save(rating);

            try {
                String variantId = rating.getVariantId();
                String productId = rating.getProductId();
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
                    double avgRating = averageRating(productId,variantId);
                    product.setAvgRating(String.valueOf(avgRating));

                    String productUpdateUrl = "http://localhost:8085/multikart/v1/product/updateAvgRating";
                    restTemplate.put(productUpdateUrl, product);

                } else {
                    log.warn("Failed to retrieve product details for productId: {} and variantId: {}", productId, rating.getRatingId());
                }
            } catch (Exception e) {
                log.error("An error occurred while fetching product details", e);
            }

            ApplicationResponse applicationResponse = new ApplicationResponse();
            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage("Rating of product is added successfully!");
            return applicationResponse;
        }catch (Exception e) {
            log.error("An error occurred while saving rating of product", e.getMessage());

            ApplicationResponse error = new ApplicationResponse();
            error.setStatus(Constants.INTERNAL_SERVER_ERROR);
            error.setMessage("An error occurred while adding saving rating");
            return error;
        }
    }

    @Override
    public ApplicationResponse getRating() {
        try {
            List<Rating> ratings = ratingRepository.findAll();
            ApplicationResponse response = new ApplicationResponse();
            response.setData(ratings);
            response.setMessage("List of all ratings");
            response.setStatus(Constants.OK);
            return response;
        }catch (Exception e) {
            log.error("An error occurred while getting all ratings", e.getMessage());

            ApplicationResponse error = new ApplicationResponse();
            error.setStatus(Constants.INTERNAL_SERVER_ERROR);
            error.setMessage("An error occurred while getting all ratings");
            return error;
        }
    }

    @Override
    public ApplicationResponse getRatingByUserId(String userId) {
        try {
            List<Rating> ratings  = ratingRepository.findByUserId(userId);
            ApplicationResponse response = new ApplicationResponse();
            response.setData(ratings);
            response.setMessage("List of all ratings by userid");
            response.setStatus(Constants.OK);
            return response;

        }catch (Exception e) {
            log.error("An error occurred while getting all ratings of products by userid", e.getMessage());

            ApplicationResponse error = new ApplicationResponse();
            error.setStatus(Constants.INTERNAL_SERVER_ERROR);
            error.setMessage("An error occurred while getting all ratings of products by userid");
            return error;
        }
    }

    @Override
    public ApplicationResponse getRatingByProductId(String productId, String variantId) {
        try {
            List<Rating> ratings = ratingRepository.findByProductIdAndVariantId(productId, variantId);
            ApplicationResponse response = new ApplicationResponse();
            response.setData(ratings);
            response.setMessage("List of all ratings by productid and varaintid");
            response.setStatus(Constants.OK);
            return response;

        }catch (Exception e) {
            log.error("An error occurred while getting all ratings of products by productid and variantid", e.getMessage());

            ApplicationResponse error = new ApplicationResponse();
            error.setStatus(Constants.INTERNAL_SERVER_ERROR);
            error.setMessage("An error occurred while getting all ratings of products by productid and variantid");
            return error;
        }

    }

    @Override
    public ApplicationResponse removeRating(String userId, String productId, String variantId) {
        try {
            List<Rating> ratings = ratingRepository.findByUserId(userId);
            ApplicationResponse applicationResponse = new ApplicationResponse();

            try {
                for(Rating rating : ratings) {
                    if(rating.getProductId().equals(productId) && rating.getVariantId().equals(variantId)) {
                        ratingRepository.delete(rating);
                    }
                }
            } catch (NumberFormatException e) {
                log.error("Error deleting rating of product variant: {}", variantId, e);
            }

            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage(Constants.OK_MESSAGE);
            return applicationResponse;


        } catch (Exception e) {
            log.error("An error occurred while removing rating of product", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while removing rating of product: " + e.getMessage());
            return errorResponse;
        }
    }

    public double averageRating(String productId, String variantId){
        List<Rating> ratings = ratingRepository.findByProductIdAndVariantId(productId, variantId);
        double avgRating = 0;
        for(Rating rating : ratings){
            avgRating += Integer.parseInt(rating.getRating());
        }
        avgRating = avgRating/ratings.size();
        return avgRating;
    }
}
