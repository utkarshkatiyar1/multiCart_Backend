package multiKart.rating.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import multiKart.rating.service.RatingServiceImpl;
import multiKart.rating.model.Rating;
import multiKart.rating.model.ApplicationResponse;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/ratings")
public class RatingController {

    @Autowired
    private RatingServiceImpl ratingService;

    @Operation(summary="add rating of user")
    @Tag(name= "add rating")
    @PostMapping("/add")
    public ApplicationResponse create(@RequestBody Rating rating)
    {
        return ratingService.create(rating);
    }

    @Operation(summary="get all ratings")
    @Tag(name= "get all ratings")
    @GetMapping
    public ApplicationResponse getRatings()
    {
        return ratingService.getRating();
    }

    @Operation(summary="get all ratings of user")
    @Tag(name= "get user ratings")
    @GetMapping("/users")
    public ApplicationResponse getRatingByUserId(@RequestParam String userId)
    {
        return ratingService.getRatingByUserId(userId);
    }

    @Operation(summary="get all ratings of the variant")
    @Tag(name= "get variant ratings")
    @GetMapping("/byproduct")
    public ApplicationResponse getRatingByProductId(@RequestParam String productId, @RequestParam String variantId)
    {
        return ratingService.getRatingByProductId(productId,variantId);
    }

    @Operation(summary="delete rating of product")
    @Tag(name= "delete rating of product")
    @DeleteMapping("/remove")
    public ApplicationResponse removeRating(@RequestParam String userId, @RequestParam String productId, @RequestParam String variantId) {
        return ratingService.removeRating(userId, productId, variantId);
    }



}
