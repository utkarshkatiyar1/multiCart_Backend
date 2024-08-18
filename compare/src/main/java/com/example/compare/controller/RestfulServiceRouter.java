package com.example.compare.controller;


import com.example.compare.service.CompareDataServiceImpl;
import com.example.compare.model.ApplicationResponse;
import com.example.compare.model.Compare;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/multikart/compare")
public class RestfulServiceRouter {
    @Autowired
    private CompareDataServiceImpl compareService;

    @Operation(summary="get compare list")
    @Tag(name= "compare list")
    @GetMapping
    public ApplicationResponse getListByUserId(@RequestParam String userId) {
        return compareService.getListByUserId(userId);
    }

    @Operation(summary="add product to compare")
    @Tag(name= "add product to compare")
    @PostMapping("/add")
    public ApplicationResponse addToCompare(@RequestBody Compare compare) {
        return compareService.addToCompare(compare);
    }

    @Operation(summary="delete product from compare list")
    @Tag(name= "delete product from compare list")
    @DeleteMapping("/remove")
    public ApplicationResponse removeFromCompare(@RequestParam String userId, @RequestParam String productId, @RequestParam String variantId) {
        return compareService.removeFromCompare(userId, productId, variantId);
    }

    @Operation(summary="clear compare list")
    @Tag(name= "clear compare list")
    @DeleteMapping("/clear")
    public ApplicationResponse clearCompareList(@RequestParam String userId) {
        return compareService.clearCompareList(userId);
    }
}
