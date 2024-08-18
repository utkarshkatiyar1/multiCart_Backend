package multiKart.rating.service;

import multiKart.rating.model.ApplicationResponse;
import multiKart.rating.model.Rating;

public interface RatingDataService {

    ApplicationResponse create(Rating rating);
    ApplicationResponse getRating();
    ApplicationResponse getRatingByUserId(String userId);
    ApplicationResponse getRatingByProductId(String productId, String variantId);
    ApplicationResponse removeRating(String userId, String productId, String variantId);
    double averageRating(String productId, String variantId);
}
